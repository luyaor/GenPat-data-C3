package fitlibrary.suite;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import fit.FitProtocol;
import fit.FitServerBridge;
import fit.exception.FitParseException;
import fitlibrary.flow.actor.DoFlowActor;
import fitlibrary.log.ConfigureLoggingThroughFiles;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsOnCounts;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class FitLibraryServerSingleStep extends FitServerBridge {
	static Logger logger = FitLibraryLogger.getLogger(FitLibraryServer.class);
	private BatchFitLibrarySingleStep batching = new BatchFitLibrarySingleStep();
	protected final ArrayBlockingQueue<ReportAction> reportQueue = new ArrayBlockingQueue<ReportAction>(
			5);
	private final DoFlowActor actor = batching.actor(reportQueue, suiteTestResults);
	protected final CountDownLatch endGate = new CountDownLatch(1);

	@Override
	public TestResults doTables(String html) {
		try {
			return doTables(TableFactory.tables(html));
		} catch (FitParseException e) {
			e.printStackTrace();
		}
		return new TestResultsOnCounts();
	}

	public TestResults doTables(Tables tables) {
		for (int t = 0; t < tables.size(); t++)
			actor.addTable(tables.at(t));
		return suiteTestResults;
	}

	@Override
	public void exit() throws Exception {
		batching.exit();
		super.exit();
	}

	@Override
	protected void usage() {
		logger.trace("usage: java fitlibrary.suite.FitLibraryServerSingleStep [-v] host port socketTicket");
		System.exit(-1);
	}

	@Override
	public void process() {
		logger.trace("Ready to receive tables from ZiBreve");
		new Thread(new Reporter()).start();
		try {
			while (true) {
				logger.trace("Reading table size...");
				int size = FitProtocol.readSize(socketReader);
				if (size == 0)
					break;
				logger.trace("Received table of size " + size + " from ZiBreve");
				try {
					String document = FitProtocol.readDocument(socketReader, size);
					doTables(document);
					actor.endStorytest();
					logger.trace("Finished running table");
				} catch (FitParseException e) {
					exception(e);
				}
			}
			logger.trace("No more tables to receive from ZiBreve");
			endGate.await();
		} catch (Exception e) {
			exception(e);
		}
	}

	public static void main(String[] args) {
		ConfigureLoggingThroughFiles.configure();
		FitServerBridge fitServer = new FitLibraryServerSingleStep();
		try {
			logger.trace(new Date());
			fitServer.run(args);
			logger.trace(("Exit: " + fitServer.exitCode()));
			if (fitServer.isExit())
				System.exit(fitServer.exitCode());
		} catch (Exception e) {
			fitServer.printExceptionDetails(e);
		}
	}

	class Reporter implements Runnable {
		@SuppressWarnings("synthetic-access")
		public void run() {
			System.out.println("Running actor12 version");
			try {
				while (true) {
					ReportAction action;
					action = reportQueue.take();
					action.run(FitLibraryServerSingleStep.this, suiteTestResults);
					if (action.isDone()) {
						break;
					}
				}
			} catch (InterruptedException e) {
				//
			}
			endGate.countDown(); // We can now finish
			System.out.println("Finished actor version");
		}
	}

	public interface ReportAction {
		void run(FitServerBridge fitLibraryServer, TestResults testResults);

		boolean isDone();
	}

	public static class TableReport implements ReportAction {
		private final Table table;

		public TableReport(Table table) {
			this.table = table;
		}

		@Override
		public void run(FitServerBridge fitLibraryServer, TestResults testResults) {
			fitLibraryServer.sendTableReport(table);
		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	public static class ReportFinished implements ReportAction {
		@Override
		public void run(FitServerBridge fitLibraryServer, TestResults testResults) {
			fitLibraryServer.sendTestResults(testResults);
		}

		@Override
		public boolean isDone() {
			return true;
		}
	}
}
