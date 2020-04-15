package fitlibrary.suite;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import fit.FitServerBridge;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Table;

public class Reporter implements Runnable {
	private final ArrayBlockingQueue<fitlibrary.suite.Reporter.ReportAction> reportQueue;
	private final FitServerBridge fitServerBridge;
	private final CountDownLatch endGate;
	private final TestResults testResults;

	public Reporter(ArrayBlockingQueue<ReportAction> reportQueue, 
			FitServerBridge fitServerBridge, 
			CountDownLatch endGate,
			TestResults testResults) {
		this.reportQueue = reportQueue;
		this.fitServerBridge = fitServerBridge;
		this.endGate = endGate;
		this.testResults = testResults;
	}
	public void run() {
		System.out.println("Running Reporter 1");
		try {
			while (true) {
				ReportAction action;
				action = reportQueue.take();
				action.run(fitServerBridge, testResults);
				if (action.isDone()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			//
		}
		endGate.countDown(); // We can now finish
		System.out.println("Finished Reporter 1");
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
