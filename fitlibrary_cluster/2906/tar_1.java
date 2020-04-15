/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.definedAction.MultiParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.runResults.TableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.traverse.TableEvaluator;
import fitlibrary.traverse.Traverse;

public class MultiDefinedActionTraverse extends Traverse {
	private MultiParameterSubstitution multiParameterSubstitution;
	private RuntimeContextInternal runtime;

	public MultiDefinedActionTraverse(MultiParameterSubstitution multiParameterSubstitution, RuntimeContextInternal runtime) {
		this.multiParameterSubstitution = multiParameterSubstitution;
		this.runtime = runtime;
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		try {
			if (table.size() < 3)
				throw new FitLibraryException("Missing data rows in table");
			getRuntimeContext().pushLocalDynamicVariables();
			Row parameterRow = table.at(1);
			multiParameterSubstitution.verifyParameters(parameterRow,this);
			parameterRow.pass(testResults);
			for (int r = 2; r < table.size(); r++) {
				Row row = table.at(r);
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
		TestResults subTestResults = TestResultsFactory.testResults();
		DefinedActionCallManager definedActionCallManager = runtime.getDefinedActionCallManager();
		try {
			definedActionCallManager.startCall(multiParameterSubstitution);
			multiParameterSubstitution.bind(parameterRow,row,getDynamicVariables(),this);
			runBody(body,subTestResults);
			colourReport(row, testResults, subTestResults);
		} finally {
			definedActionCallManager.endCall(multiParameterSubstitution);
		}
		if (runtime.toExpandDefinedActions() || subTestResults.problems() || runtime.isAbandoned(testResults)) {
			Cell cell = TableFactory.cell(body);
			cell.addPrefixToFirstInnerTable("Defined action call:");
			row.add(cell);
		} else if (definedActionCallManager.readyToShow())
			row.add(TableFactory.cell(TableFactory.tables(definedActionCallManager.getShowsTable())));
	}
	private void runBody(Tables body, TestResults subTestResults) {
		TableEvaluator tableEvaluator = runtime.getTableEvaluator();
		for (Table table: body)
			tableEvaluator.runTable(table, new TableListener(subTestResults));
	}
	private void colourReport(Row row, TestResults testResults, TestResults subTestResults) {
		if (runtime.isAbandoned(testResults))
			row.ignore(testResults);
		else if (runtime.toExpandDefinedActions() || subTestResults.problems()) {
			if (subTestResults.passed())
				row.passKeywords(testResults);
			else if (subTestResults.errors())
				for (int i = 0; i < row.size(); i++)
					row.at(i).error(testResults, new FitLibraryException(""));
			else if (subTestResults.failed())
				for (int i = 0; i < row.size(); i++)
					row.at(i).fail(testResults);
			else
				for (int i = 0; i < row.size(); i++)
					row.at(i).pass(testResults);
		} else
			row.pass(testResults);
	}
}
