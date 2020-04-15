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
	DoFlowActor actor = new DoFlowActor(this);
	
	public DoFlowRunningActor(FlowEvaluator flowEvaluator, IScopeStack scopeStack, RuntimeContextInternal runtime, SetUpTearDown setUpTearDown) {
		super(flowEvaluator, scopeStack, runtime, setUpTearDown);
	}

	@Override
	public void runStorytest(Tables tables, ITableListener tableListener) {
		actor.start(tableListener);
		for (int t = 0; t < tables.size(); t++)
			actor.addTable(tables.at(t));
		actor.endStorytest();
		
		new Thread(actor).start();
		System.out.println("Running actor version");
	}
	@Override
	public void exit() {
		// Do nothing here, as we already handle exit inside endStorytest().
	}
}

// Next step is to take out TableListener from DoFlowActor and use a queue back