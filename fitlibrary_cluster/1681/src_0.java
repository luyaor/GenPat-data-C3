package fitlibrary.suite;

import java.util.concurrent.ArrayBlockingQueue;

import fitlibrary.flow.actor.DoFlowActor;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.FitLibraryServerSingleStep.ReportAction;
import fitlibrary.table.Tables;

public class BatchFitLibrarySingleStep extends BatchFitLibrary {
	@Override
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		runStorytest(theTables);
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

}
