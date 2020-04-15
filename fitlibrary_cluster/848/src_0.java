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

import util.StreamReader;
import fit.exception.FitParseException;

/* This is a variation of FitServer that changes the doTables() call in 
 * the middle of the process() method.
 */
public abstract class FitServerBridge {
	public String input;
	public Fixture fixture = new Fixture();
	public FixtureListener fixtureListener = new TablePrintingFixtureListener();
	private Counts counts = new Counts();
	protected OutputStream socketOutput;
	private StreamReader socketReader;
	private boolean verbose = false;
	private String host;
	private int port;
	private int socketToken;
	private Socket socket;
	protected int numberOfPages = 0;
	protected boolean showAllReports = false;
	private boolean exit = true;
	private boolean sentinel;
	public static String FITNESSE_URL = "";

	public FitServerBridge(String host, int port, boolean verbose) {
		setFitNesseUrl(host, port);
		this.verbose = verbose;
	}
	public FitServerBridge() {
		//
	}
	public boolean isExit() {
		return exit;
	}
	private void setFitNesseUrl(String host, int port) {
		this.host = host;
		this.port = port;
		FITNESSE_URL = "http://" + host + ":" + port + "/";
	}
	protected String fitNesseUrl() {
		return host + ":" + port;
	}
	public void run(String argv[]) throws Exception {
		args(argv);
		establishConnection();
		validateConnection();
		process();
		closeConnection();
		exit();
	}
	public void closeConnection() throws IOException {
		socket.close();
	}
	public void process() {
		fixture.listener = fixtureListener;
		try {
			while (true) {
				print("FitServerBridge: Reading size...");
				int size = FitProtocol.readSize(socketReader);
				print("FitServerBridge: Size is " + size);
				if (size == 0)
					break;
				try {
					print("FitServerBridge: Processing document of size: "
							+ size);
					String document = FitProtocol.readDocument(socketReader,
							size);
					doTables(document);
					print("\tresults: " + fixture.counts() + "\n");
					counts.tally(fixture.counts);
					numberOfPages++;
				} catch (FitParseException e) {
					exception(e);
				}
			}
			print("FitServerBridge: Completion signal received");
		} catch (Exception e) {
			exception(e);
		}
	}
	public abstract void doTables(String html);

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
			else if ("-s".equals(arg))
				sentinel = true;
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
		System.out.println("usage: java fit.FitServer [-v] host port socketTicket");
		System.out.println("\t-v\tverbose");
		System.exit(-1);
	}
	protected void exception(Exception e) {
		printExceptionDetails(e);
		Parse tables = new Parse("span", "Exception occurred: ", null, null);
		fixture.exception(tables, e);
		counts.exceptions += 1;
		fixture.listener.tableFinished(tables);
		fixture.listener.tablesFinished(counts); // TODO shouldn't this be fixture.counts
	}
	public void printExceptionDetails(Exception e) {
		print("FitServerBridge: Exception: " + e.getMessage());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(out));
		print(out.toString()+"\n");
	}
	public void exit() throws Exception {
		print("FitServerBridge: exiting");
		print("FitServerBridge: end results: " + counts.toString());
	}
	public int exitCode() {
		return counts.wrong + counts.exceptions;
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
	public Counts getCounts() {
		return counts;
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
	@SuppressWarnings("unused")
	public void writeCounts(Counts count) throws IOException {
		// TODO This can't be right.... which counts should be used?
		FitProtocol.writeCounts(counts, socketOutput);
	}

	public void showAllReports() {
		showAllReports = true;
	}

	class TablePrintingFixtureListener implements FixtureListener {
		public void tableFinished(Parse table) {
			try {
				byte[] bytes = readTable(table);
				if (bytes.length > 0)
					FitProtocol.writeData(bytes, socketOutput);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public void tablesFinished(Counts count) {
			try {
				FitProtocol.writeCounts(count, socketOutput);
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
