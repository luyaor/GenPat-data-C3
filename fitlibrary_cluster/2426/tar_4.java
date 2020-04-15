package fitlibrary.flow.actor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import fitlibrary.flow.DoFlow;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.BatchFitLibrarySingleStep.ReportAction;
import fitlibrary.suite.BatchFitLibrarySingleStep.ReportFinished;
import fitlibrary.suite.BatchFitLibrarySingleStep.TableReport;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;

public class DoFlowActor implements Runnable {
	public static Logger logger = FitLibraryLogger.getLogger(DoFlowActor.class);
	private final Queue<FlowAction> queue = new ConcurrentLinkedQueue<FlowAction>();
	protected final DoFlow doFlow;
	protected final Queue<ReportAction> reportQueue;
	protected final TestResults testResults;

	public DoFlowActor(DoFlow doFlow, Queue<ReportAction> reportQueue, TestResults testResults) {
		this.doFlow = doFlow;
		this.reportQueue = reportQueue;
		this.testResults = testResults;
	}
	public void addTable(Table table) {
		queue.add(new TableAction(table));
	}
	public void endStorytest() {
		queue.add(new EndStoryTestAction());
	}
	public void run() {
		DoFlowActor.logger.trace("Running storytest");
		doFlow.resetToStartStorytest();
		while (true) {
			FlowAction action = queue.remove();
			action.run();
			if (action.isDone()) {
				System.out.println("DoFlowActor thread done.");
				return;
			}
		}
	}

	abstract class FlowAction {
		public abstract void run();
		
		public boolean isDone() { 
			return false;
		}
	}

	class TableAction extends FlowAction {
		private final Table table;
		
		public TableAction(Table table) {
			this.table = table;
		}
		@Override
		public void run() {
			doFlow.runSingleTable(testResults, table);
			doFlow.finishTable(table, testResults);
			doFlow.addAccumulatedFoldingText(table);
			reportQueue.add(new TableReport(table));
		}
	}
	
	class EndStoryTestAction extends FlowAction {
		@Override
		public void run() {
			Table errorTable = TableFactory.table(TableFactory
					.row("<i>Error in storytest tear down: </i>"));
			doFlow.finishLastTable(errorTable, testResults);
			doFlow.addAccumulatedFoldingText(errorTable);
			if (errorTable.size() > 1 || errorTable.at(0).size() > 1
					|| errorTable.at(0).at(0).hadError() || !errorTable.getTrailer().isEmpty()) {
				errorTable.setLeader("\n<br/>");
				reportQueue.add(new TableReport(errorTable));
			}
			DoFlowActor.logger.trace("Finished storytest");
			reportQueue.add(new ReportFinished());
			doFlow.exit();
		}
		@Override
		public boolean isDone() { 
			return true;
		}
	}
}
