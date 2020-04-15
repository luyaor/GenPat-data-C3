package fitlibrary.suite;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.flow.actor.DoFlowActor;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.tableOnParse.TableElementOnParse;

public class BatchFitLibrarySingleStep extends BatchFitLibrary {
	@Override
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		runStorytest(theTables);
		DynamicVariablesRecording recorder = doFlow.getRuntimeContext().getDynamicVariableRecorder();
		if (recorder.isRecording()) {
			try {
				recorder.write();
			} catch (IOException e) {
				Table errorTable = TableFactory.table(TableFactory.row("note",TableElementOnParse.label("Problem on writing property file:")+"<hr/>"+e.getMessage()));
				errorTable.at(0).at(1).error(tableListener.getTestResults());
				theTables.add(errorTable);
			}
		}
		System.out.println("Finished BatchFitLibrarySingleStep");
		return tableListener.getTestResults();
	}

	private void runStorytest(Tables tables) {
		final ArrayBlockingQueue<ReportAction> reportQueue = new ArrayBlockingQueue<ReportAction>(
				5);
		DoFlowActor actor = new DoFlowActor(doFlow, reportQueue,tableListener.getTestResults());
		for (int t = 0; t < tables.size(); t++)
			actor.addTable(tables.at(t));
		actor.endStorytest();

		new Thread(actor).start();
		System.out.println("Running actor3 version");
		try {
			while (true) {
				ReportAction action;
				action = reportQueue.take();
				action.run(tableListener);
				if (action.isDone()) {
					System.out.println("Finished actor version");
					return;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Finished actor version: "+e);
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
