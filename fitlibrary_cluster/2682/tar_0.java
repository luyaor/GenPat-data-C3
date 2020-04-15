package fitlibrary.flow.actor;

import java.util.concurrent.ArrayBlockingQueue;

import fitlibrary.flow.DoFlow;
import fitlibrary.flow.IScopeStack;
import fitlibrary.flow.SetUpTearDown;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.FlowEvaluator;

/*
 * This only runs a single storytest, in single-step mode.
 */
public class DoFlowRunningActor extends DoFlow {
	private final ArrayBlockingQueue<ReportAction> reportQueue = new ArrayBlockingQueue<ReportAction>(
			5);
	private final DoFlowActor actor = new DoFlowActor(this, reportQueue);

	public DoFlowRunningActor(FlowEvaluator flowEvaluator, IScopeStack scopeStack,
			RuntimeContextInternal runtime, SetUpTearDown setUpTearDown) {
		super(flowEvaluator, scopeStack, runtime, setUpTearDown);
	}

	@Override
	public void runStorytest(Tables tables, ITableListener tableListener) {
		actor.start(tableListener.getTestResults());
		for (int t = 0; t < tables.size(); t++)
			actor.addTable(tables.at(t));
		actor.endStorytest();

		new Thread(actor).start();
		System.out.println("Running actor version");
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

	@Override
	public void exit() {
		// Do nothing here, as we already handle exit inside endStorytest().
	}

	interface ReportAction {
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

// Next step is to push functionality here back into a variation of BatchFitLibrary