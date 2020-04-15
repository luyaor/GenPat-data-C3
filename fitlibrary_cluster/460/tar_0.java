/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.definedAction.MultiParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.CellOnParse;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.table.TablesOnParse;
import fitlibrary.traverse.TableEvaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class MultiDefinedActionTraverse extends Traverse {
	private MultiParameterSubstitution multiParameterSubstitution;
	private DoTraverseInterpreter doTraverse;
	private RuntimeContextInternal runtime;

	public MultiDefinedActionTraverse(MultiParameterSubstitution multiParameterSubstitution, DoTraverseInterpreter doTraverse) {
		this.multiParameterSubstitution = multiParameterSubstitution;
		this.doTraverse = doTraverse;
		this.runtime = doTraverse.getRuntimeContext();
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		try {
			if (table.size() < 3)
				throw new FitLibraryException("Missing data rows in table");
			getRuntimeContext().pushLocalDynamicVariables();
			Row parameterRow = table.row(1);
			multiParameterSubstitution.verifyParameters(parameterRow,this);
			parameterRow.pass(testResults);
			for (int r = 2; r < table.size(); r++) {
				Row row = table.row(r);
				if (runtime.isAbandoned(testResults))
					row.ignore(testResults);
				else
					try {
						runRow(row, parameterRow, testResults);
					} catch (Exception e) {
						row.error(testResults, e);
					}
			}
			getRuntimeContext().popLocalDynamicVariables();
		} catch (Exception e) {
			table.error(testResults, e);
		}
		return null;
	}
	private void runRow(Row row, Row parameterRow, TestResults testResults) {
		Tables body = multiParameterSubstitution.getCopyOfBody();
		TestResults subTestResults = new TestResults();
		DefinedActionCallManager definedActionCallManager = doTraverse.getRuntimeContext().getDefinedActionCallManager();
		try {
			definedActionCallManager.startCall(multiParameterSubstitution);
			multiParameterSubstitution.bind(parameterRow,row,getDynamicVariables(),this);
			runBody(body,subTestResults);
			colourReport(row, testResults, subTestResults);
		} finally {
			definedActionCallManager.endCall(multiParameterSubstitution);
		}
		if (runtime.toExpandDefinedActions() || subTestResults.problems() || runtime.isAbandoned(testResults))
			row.addCell(new CellOnParse("Defined action call:",body));
		else if (definedActionCallManager.readyToShow())
			row.addCell(new CellOnParse(new TablesOnParse(definedActionCallManager.getShowsTable())));
	}
	private void runBody(Tables body, TestResults subTestResults) {
		TableEvaluator tableEvaluator = doTraverse.getRuntimeContext().getTableEvaluator();
		for (int t = 0; t < body.size(); t++) {
			Table table = body.table(t);
			tableEvaluator.runTable(table, new TableListener(subTestResults));
		}
	}
	private void colourReport(Row row, TestResults testResults, TestResults subTestResults) {
		if (runtime.isAbandoned(testResults))
			row.ignore(testResults);
		else if (runtime.toExpandDefinedActions() || subTestResults.problems()) {
			if (subTestResults.passed())
				row.passKeywords(testResults);
			else if (subTestResults.errors())
				for (int i = 0; i < row.size(); i++)
					row.cell(i).error(testResults, new FitLibraryException(""));
			else if (subTestResults.failed())
				for (int i = 0; i < row.size(); i++)
					row.cell(i).fail(testResults);
			else
				for (int i = 0; i < row.size(); i++)
					row.cell(i).pass(testResults);
		} else
			row.pass(testResults);
	}
}
