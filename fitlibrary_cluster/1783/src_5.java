package fitlibrary.suite;

import java.util.Date;

import org.apache.log4j.Logger;

import fit.FitServerBridge;
import fit.exception.FitParseException;
import fitlibrary.log.ConfigureLoggingThroughFiles;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsOnCounts;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class FitLibraryServerSingleStep extends FitServerBridge {
	static Logger logger = FitLibraryLogger.getLogger(FitLibraryServer.class);
	private BatchFitLibrary batching = new BatchFitLibrarySingleStep();

	@Override
	public TestResults doTables(String html) {
		try {
			return doTables(TableFactory.tables(html));
		} catch (FitParseException e) {
			e.printStackTrace();
		}
		return new TestResultsOnCounts();
	}
	public TestResults doTables(Tables theTables) {
		TableListener tableListener = new TableListener(reportListener);
		batching.doTables(theTables,tableListener);
		return tableListener.getTestResults();
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
	public static void main(String[] args) {
		ConfigureLoggingThroughFiles.configure();
		FitServerBridge fitServer = new FitLibraryServerSingleStep();
		try {
			logger.trace(new Date());
			fitServer.run(args);
			logger.trace(("Exit: "+fitServer.exitCode()));
			if (fitServer.isExit())
				System.exit(fitServer.exitCode());
		} catch (Exception e) {
			fitServer.printExceptionDetails(e);
		}
    }

	public interface ReportAction {
		void run(ITableListener tableListener);

		boolean isDone();
	}

	public static class TableReport implements ReportAction {
		private final Table table;

		public TableReport(Table table) {
			this.table = table;
		}

		@Override
		public void run(ITableListener tableListener) {
			tableListener.tableFinished(table);
		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	public static class ReportFinished implements ReportAction {
		@Override
		public void run(ITableListener tableListener) {
			tableListener.storytestFinished();
		}

		@Override
		public boolean isDone() {
			return true;
		}
	}
}
