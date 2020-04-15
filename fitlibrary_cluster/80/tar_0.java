package fitlibrary.suite;

import java.util.concurrent.ArrayBlockingQueue;

import fitlibrary.flow.actor.DoFlowActor;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.FitLibraryServerSingleStep.ReportAction;

public class BatchFitLibrarySingleStep extends BatchFitLibrary {
	public DoFlowActor actor(ArrayBlockingQueue<ReportAction> reportQueue, TestResults testResults) {
		DoFlowActor actor = new DoFlowActor(doFlow,reportQueue,testResults);
		new Thread(actor).start();
		return actor;
	}
}
