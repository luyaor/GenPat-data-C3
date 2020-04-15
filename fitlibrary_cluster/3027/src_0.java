/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 10/12/2006
*/

package fitlibrary.suite;

import fit.FitServerBridge;
import fit.FixtureBridge;
import fitlibrary.DomainFixture;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.AlienTraverseHandler;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class IndependentSuiteRunner implements SuiteRunner {
	private static final AlienTraverseHandler alienTraverseHandler = Traverse.getAlienTraverseHandler();
	private Object firstObjectOfFirstStorytest;

	public IndependentSuiteRunner(Object firstObjectOfFirstStorytest) {
		this.firstObjectOfFirstStorytest = firstObjectOfFirstStorytest;
	}
	public void runFirstStorytest(Tables tables, TableListener tableListener) {
		runEachStorytest(firstObjectOfFirstStorytest,tables,tableListener);
	}
	public void runStorytest(Tables tables, TableListener tableListener) {
		Object firstObject = new FixtureBridge().firstObject(tables.parse(),tableListener.getTestResults());
		runEachStorytest(firstObject,tables,tableListener);
	}
	private void runEachStorytest(Object objectInitial, Tables tables, TableListener tableListener) {
		Object object = objectInitial;
		TestResults testResults = tableListener.getTestResults();
		if (object == null || testResults.isAbandoned()) { // Bad class name or abandoned
			tables.ignoreAndFinished(tableListener);
			return;
		}
		if (!evaluator(object))
			object = new DomainFixture(object);
		if (object instanceof DoEvaluator) {
			DoEvaluator doEvaluator = (DoEvaluator)object;
			doEvaluator.setDynamicVariable(Traverse.FITNESSE_URL_KEY,FitServerBridge.FITNESSE_URL);
			
			Table firstTable = tables.table(0);
			doEvaluator.setUp(firstTable,testResults);
			doEvaluator.interpretAfterFirstRow(firstTable,testResults);
			tableListener.tableFinished(firstTable);
			new InFlowPageRunner(doEvaluator,testResults).run(tables,1,tableListener,true);
//			doEvaluator.tearDown(firstTable,testResults);
		} else 
			 new OutOfFlowPageRunner(object,tables,tableListener);
		tableListener.storytestFinished();
	}
	private boolean evaluator(Object object) {
		return alienTraverseHandler.isAlienTraverse(object) || object instanceof Evaluator;
	}
	public void exit() {
		//
	}
}
