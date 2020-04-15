//Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
//Copyright (c) 2002 Cunningham & Cunningham, Inc.
//Released under the terms of the GNU General Public License version 2 or later.
//Copyright (C) 2003,2004 by Robert C. Martin and Micah D. Martin. All rights reserved.
//Released under the terms of the GNU General Public License version 2 or later.
//This is the same as fit.FitServer except that newFixture() has been made protected.
//Altered by Rick Mugridge, December 2005, to allow changes in a subclass.
package fit;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import util.StreamReader;
import fit.exception.FitParseException;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsOnCounts;
import fitlibrary.suite.ReportListener;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;

/* This is a variation of FitServer that's needed to run FitLibrary
 */
public abstract class FitServerBridge {
	static Logger logger = FitLibraryLogger.getLogger(FitServerBridge.class);
	protected ReportListener reportListener = new TableReportListener();
	protected TestResults suiteTestResults = new TestResultsOnCounts();
	protected OutputStream socketOutput;
	private StreamReader socketReader;
	private boolean verbose = false;
	private String host;
	private int port;
	private int socketToken;
	private Socket socket;
	protected boolean exit = true;
	public static String FITNESSE_URL = "";

	public boolean isExit() {
		return exit;
	}

	private void setFitNesseUrl(String host, int port) {
		this.host = host;
		this.port = port;
		FITNESSE_URL = "http://" + host + ":" + port + "/";
	}

	public void run(String argv[]) throws Exception {
		args(argv);
		establishConnection();
		validateConnection();
		process();
		socket.close();
		exit();
	}

	public void process() {
		logger.trace("Ready to received messages");
		try {
			while (true) {
				print("FitServerBridge: Reading size...");
				int size = FitProtocol.readSize(socketReader);
				logger.trace("Received message of size " + size);
				print("FitServerBridge: Size is " + size);
				if (size == 0)
					break;
				try {
					print("FitServerBridge: Processing document of size: "
							+ size);
					String document = FitProtocol.readDocument(socketReader,
							size);
					TestResults storyTestResults = doTables(document);
					print("\tresults: " + storyTestResults + "\n");
					logger.trace("Finished storytest");
					suiteTestResults.add(storyTestResults);
				} catch (FitParseException e) {
					exception(e);
				}
			}
			print("FitServerBridge: Completion signal received");
		} catch (Exception e) {
			exception(e);
		}
	}

	public abstract TestResults doTables(String html);

	public String readDocument() throws Exception {
		int size = FitProtocol.readSize(socketReader);
		return FitProtocol.readDocument(socketReader, size);
	}

	public void args(String[] argv) {
		printArgs(argv);
		int i = gatherOptions(argv);
		String hostName = argv[i++];
		int portNo = Integer.parseInt(argv[i++]);
		setFitNesseUrl(hostName, portNo);
		socketToken = Integer.parseInt(argv[i++]);
	}

	private int gatherOptions(String[] argv) {
		int i = 0;
		while (argv[i].startsWith("-")) {
			String arg = argv[i];
			if ("-v".equals(arg))
				verbose = true;
			else if ("-x".equals(arg))
				exit = false;
			else
				usage();
			i++;
		}
		return i;
	}

	private void printArgs(String[] argv) {
		String result = "Arguments: ";
		for (String s : argv)
			result += s + " ";
		print(result);
	}

	protected void usage() {
		System.out
				.println("usage: java fit.FitServer [-v] host port socketTicket");
		System.out.println("\t-v\tverbose");
		System.exit(-1);
	}

	protected void exception(Exception e) {
		printExceptionDetails(e);
		Table table = TableFactory.table(TableFactory
				.row("Exception occurred: "));
		table.at(0).at(0).error(suiteTestResults, e);
		reportListener.tableFinished(table);
		reportListener.tablesFinished(suiteTestResults);
	}

	public void printExceptionDetails(Exception e) {
		print("FitServerBridge: Exception: " + e.getMessage());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(out));
		print(out.toString() + "\n");
	}

	public void exit() throws Exception {
		print("FitServerBridge: exiting");
		print("FitServerBridge: end results: "
				+ suiteTestResults.getCounts().toString());
	}

	public int exitCode() {
		return suiteTestResults.getCounts().wrong
				+ suiteTestResults.getCounts().exceptions;
	}

	public void establishConnection() throws Exception {
		establishConnection(makeHttpRequest());
	}

	public void establishConnection(String httpRequest) throws Exception {
		print("FitServerBridge: Connecting to " + host + " : " + port);
		socket = new Socket(host, port);
		print("FitServerBridge: Connected");
		socketOutput = socket.getOutputStream();
		socketReader = new StreamReader(socket.getInputStream());
		byte[] bytes = httpRequest.getBytes("UTF-8");
		socketOutput.write(bytes);
		socketOutput.flush();
		print("http request sent");
	}

	private String makeHttpRequest() {
		return "GET /?responder=socketCatcher&ticket=" + socketToken
				+ " HTTP/1.1\r\n\r\n";
	}

	public void validateConnection() throws Exception {
		print("FitServerBridge: Validating connection...");
		int statusSize = FitProtocol.readSize(socketReader);
		if (statusSize == 0)
			print("FitServerBridge: ...ok");
		else {
			String errorMessage = FitProtocol.readDocument(socketReader,
					statusSize);
			print("...failed because: " + errorMessage + "\n");
			System.out.println("An error occured while connecting to client.");
			System.out.println(errorMessage);
			System.exit(-1);
		}
	}

	public void print(String message) {
		if (verbose) {
			System.out.println(message);
			try {
				FileWriter fileWriter = new FileWriter("running.txt", true);
				fileWriter.write(message + "\n");
				fileWriter.close();
			} catch (IOException e) {
				//
			}
		}
	}

	public static byte[] readTable(Parse table) throws Exception {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer,
				"UTF-8");
		PrintWriter writer = new PrintWriter(streamWriter);
		Parse more = table.more;
		table.more = null;
		if (table.trailer == null)
			table.trailer = "";
		table.print(writer);
		table.more = more;
		writer.close();
		return byteBuffer.toByteArray();
	}

	public static byte[] readTable(Table table) throws Exception {
		StringBuilder builder = new StringBuilder();
		table.toHtml(builder);
		return builder.toString().getBytes("UTF-8");
	}

	class TableReportListener implements ReportListener {
		@Override
		public void tableFinished(Table table) {
			print("FitServerBridge table is finished");
			logger.trace("Sending table report");
			try {
				byte[] bytes = readTable(table);
				if (bytes.length > 0)
					FitProtocol.writeData(bytes, socketOutput);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void tablesFinished(TestResults testResults) {
			logger.trace("Sending results");
			try {
				FitProtocol.writeCounts(testResults.getCounts(), socketOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void setFitNesseUrl(String url) {
		// Nasty hack but (indirectly) unavoidable while SpecifyFixture is a
		// Fixture instead of a Traverse
		// See use of the global in BatchFitLibrary
		FITNESSE_URL = url;
	}
}
