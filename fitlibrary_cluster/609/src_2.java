package fitlibrary.flow.actor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import fitlibrary.flow.DoFlow;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;

public class DoFlowActor {
	public static Logger logger = FitLibraryLogger.getLogger(DoFlowActor.class);
	private final Queue<FlowAction> queue = new ConcurrentLinkedQueue<FlowAction>();
	protected final DoFlow doFlow;
	protected ITableListener tableListener;

	public DoFlowActor(DoFlow doFlow) {
		this.doFlow = doFlow;
	}
	public void start(ITableListener nextTableListener) {
		queue.add(new StartAction(nextTableListener));
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
			if (action.isDone())
				return;
		}
	}

	public abstract class FlowAction {
		public abstract void run();
		
		public boolean isDone() { 
			return false;
		}
	}

	class StartAction extends FlowAction {
		private final ITableListener nextTableListener;
		
		public StartAction(ITableListener tableListener) {
			this.nextTableListener = tableListener;
		}
		@Override
		public void run() {
			tableListener = nextTableListener;
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
			TestResults testResults = tableListener.getTestResults();
			doFlow.runSingleTable(testResults, table);
			doFlow.finishTable(table, testResults);
			doFlow.addAccumulatedFoldingText(table);
			tableListener.tableFinished(table);
		}
	}
	
	class EndStoryTestAction extends FlowAction {
		@Override
		public void run() {
			Table errorTable = TableFactory.table(TableFactory
					.row("<i>Error in storytest tear down: </i>"));
			doFlow.finishLastTable(errorTable, tableListener.getTestResults());
			doFlow.addAccumulatedFoldingText(errorTable);
			if (errorTable.size() > 1 || errorTable.at(0).size() > 1
					|| errorTable.at(0).at(0).hadError() || !errorTable.getTrailer().isEmpty()) {
				errorTable.setLeader("\n<br/>");
				tableListener.tableFinished(errorTable);
			}
			DoFlowActor.logger.trace("Finished storytest");
			tableListener.storytestFinished();
//			doFlow.exit(); // Later enable this!!!!!!!!!!!!!!
		}
		@Override
		public boolean isDone() { 
			return true;
		}
	}
}
