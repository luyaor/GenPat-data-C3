package fitlibrary.flow.actor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import fitlibrary.flow.DoFlow;
import fitlibrary.flow.actor.DoFlowRunningActor.ReportAction;
import fitlibrary.flow.actor.DoFlowRunningActor.ReportFinished;
import fitlibrary.flow.actor.DoFlowRunningActor.TableReport;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;

public class DoFlowActor implements Runnable {
	public static Logger logger = FitLibraryLogger.getLogger(DoFlowActor.class);
	private final Queue<FlowAction> queue = new ConcurrentLinkedQueue<FlowAction>();
	protected final DoFlow doFlow;
	protected final Queue<ReportAction> reportQueue;
	protected TestResults testResults;

	public DoFlowActor(DoFlow doFlow, Queue<ReportAction> reportQueue) {
		this.doFlow = doFlow;
		this.reportQueue = reportQueue;
	}
	public void start(TestResults theTestResults) {
		queue.add(new StartAction(theTestResults));
	}
	public void addTable(Table table) {
		queue.add(new TableAction(table));
	}
	public void endStorytest() {
		queue.add(new EndStoryTestAction());
	}
	public void run() {
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

	class StartAction extends FlowAction {
		private final TestResults theTestResults;
		
		public StartAction(TestResults testResults) {
			this.theTestResults = testResults;
		}
		@Override
		public void run() {
			DoFlowActor.this.testResults = theTestResults;
			DoFlowActor.logger.trace("Running storytest");
			doFlow.resetToStartStorytest();
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
