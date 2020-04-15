package fitlibrary.flow.actor;

import fitlibrary.flow.DoFlow;
import fitlibrary.flow.IScopeStack;
import fitlibrary.flow.SetUpTearDown;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.FlowEvaluator;

/*
 * This only runs a single storytest, in single-step mode.
 */
public class DoFlowRunningActor extends DoFlow {
	public DoFlowRunningActor(FlowEvaluator flowEvaluator, IScopeStack scopeStack, RuntimeContextInternal runtime, SetUpTearDown setUpTearDown) {
		super(flowEvaluator, scopeStack, runtime, setUpTearDown);
	}

	@Override
	public void runStorytest(Tables tables, ITableListener tableListener) {
		DoFlowActor actor = new DoFlowActor(this);
		
		actor.start(tableListener);
		for (int t = 0; t < tables.size(); t++)
			actor.addTable(tables.at(t));
		actor.endStorytest(); // Temporary, until threaded, when this will be done in exit() below.
		
		actor.run();
		System.out.println("Running actor version");
		
		
//		TestResults testResults = tableListener.getTestResults();
//		logger.trace("Running storytest");
//		resetToStartStorytest();
//		for (int t = 0; t < tables.size(); t++) {
//			Table table = tables.at(t);
//			runSingleTable(testResults, table);
//			finishTable(table, testResults);
//			addAccumulatedFoldingText(table);
//			tableListener.tableFinished(table);
//		}
//		Table errorTable = TableFactory.table(TableFactory.row("<i>Error in storytest tear down: </i>"));
//		finishLastTable(errorTable, testResults);
//		addAccumulatedFoldingText(errorTable);
//		if (errorTable.size() > 1 || errorTable.at(0).size() > 1 || errorTable.at(0).at(0).hadError() || !errorTable.getTrailer().isEmpty()) {
//			errorTable.setLeader("\n<br/>");
//			tables.add(errorTable); // Needed just for embedded tables, as with SpecifyFixture.
//			tableListener.tableFinished(errorTable);
//		}
//		logger.trace("Finished storytest");
//		tableListener.storytestFinished();
	}
	@Override
	public void exit() {
		super.exit(); // Later have to: queue.add(new endStorytest()); //!!!!!!!!!!!!!!!!!
	}
}

// Next step is to take out TableListener from DoFlowActor and use a queue back