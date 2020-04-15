/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */

package fitlibrary.suite;

import java.util.Stack;

import fit.Fixture;
import fitlibrary.DoFixture;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DoFlow extends DoTraverse {
	private Stack<Object> tableStack = new Stack<Object>();

	public void runStorytest(Tables tables, TableListener tableListener) {
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.table(t);
			runTable(table, tableListener);
			tableListener.tableFinished(table);
		}
		tearDownFlowObject(tables, tableListener);
		tableListener.storytestFinished();
		// Could return whether we hit a suite fixture...
	}
	private void runTable(Table table, TableListener tableListener) {
		TestResults testResults = tableListener.getTestResults();
		for (int rowNo = 0; rowNo < table.size(); rowNo++) {
			Row row = table.row(rowNo);
			if (testResults.isAbandoned())
				row.ignore(testResults);
			else
				try {
					Fixture fixtureByName = fixtureOrDoTraverseByName(table,testResults);
					if (fixtureByName != null && fixtureByName.getClass() == Fixture.class)
						fixtureByName = null;
					Object result;
					if (fixtureByName instanceof DoFixture) {
						result = fixtureByName;
						fixtureByName = null;
					} else
						result = interpretRow(row,testResults,fixtureByName);
					if (result instanceof DoEvaluator) {
						DoEvaluator doEvaluator = (DoEvaluator) result;
						doEvaluator.setRuntimeContext(runtimeContext);
						doEvaluator.setUp(table, testResults);
						pushSut(doEvaluator);
					} else if (result instanceof Evaluator) {
						Evaluator evaluator = (Evaluator) result;
						evaluator.setRuntimeContext(runtimeContext);
						evaluator.setUp(table, testResults);
						evaluator.interpretAfterFirstRow(table, testResults);
						evaluator.tearDown(table, testResults);
						break; // have finished table
					} else if (getAlienTraverseHandler().isAlienTraverse(result)) {
						getAlienTraverseHandler().doTable(result,new Table(row),testResults,this);
						break; // have finished table
					} else if (result != null) {
						pushSut(result);
					}
				} catch (Exception ex) {
					row.error(testResults, ex);
				}
		}
		popLocalSut(table,tableListener);
	}
	private void pushSut(Object sut) {
		Object currentSut = getSystemUnderTest();
		if (currentSut != null)
			tableStack.push(currentSut);
		setSystemUnderTest(sut);
	}
	private void popLocalSut(Table table, TableListener tableListener) {
		while (!tableStack.isEmpty()) {
			try {
				tearDownSut();
			} catch (Exception e) {
				table.error(tableListener, e);
			}
			setSystemUnderTest(tableStack.pop());
		}
	}
	private void tearDownSut() throws Exception {
		Object sut = getSystemUnderTest();
		if (sut != null && sut instanceof Evaluator)
			((Evaluator) sut).tearDown();
	}
	private void tearDownFlowObject(Tables tables, TableListener tableListener) {
		try {
			tearDownSut();
		} catch (Exception e) {
			tables.table(0).error(tableListener, e);
		}
	}
}
