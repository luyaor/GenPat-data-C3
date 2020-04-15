package fitlibrary.flow.actor;

import java.util.concurrent.ArrayBlockingQueue;

import fitlibrary.flow.DoFlow;
import fitlibrary.runResults.ITableListener;
import fitlibrary.suite.BatchFitLibrarySingleStep.ReportAction;
import fitlibrary.table.Tables;

/*
 * This only runs a single storytest, in single-step mode.
 */
public class DoFlowRunningActor {
	private final ArrayBlockingQueue<ReportAction> reportQueue = new ArrayBlockingQueue<ReportAction>(
			5);

	public void runStorytest(DoFlow doFlow, Tables tables, ITableListener tableListener) {
		DoFlowActor actor = new DoFlowActor(doFlow, reportQueue);
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

}

// Next step is to push functionality here back into a variation of BatchFitLibrary