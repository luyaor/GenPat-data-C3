/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.definedAction.MultiParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
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
			getRuntimeContext().pushLocal();
			Row parameterRow = table.row(1);
			multiParameterSubstitution.verifyParameters(parameterRow,this);
			parameterRow.pass(testResults);
			for (int r = 2; r < table.size(); r++) {
				Row row = table.row(r);
				if (testResults.isAbandoned())
					row.ignore(testResults);
				else
					try {
						Tables body = multiParameterSubstitution.getCopyOfBody();
						TestResults subTestResults = new TestResults(testResults);
						try {
							CallManager.startCall(multiParameterSubstitution);
							multiParameterSubstitution.bind(parameterRow,row,getDynamicVariables(),this);
							runBody(body,testResults,subTestResults);
							colourReport(row, testResults, subTestResults);
						} finally {
							CallManager.endCall(multiParameterSubstitution);
						}
						if (runtime.toExpandDefinedActions() || subTestResults.problems() || testResults.isAbandoned())
							row.addCell(new Cell("Defined action call:",body));
						else if (CallManager.readyToShow())
							row.addCell(new Cell(new Tables(CallManager.getShowsTable())));
					} catch (Exception e) {
						row.error(testResults, e);
					}
			}
			getRuntimeContext().popLocal();
		} catch (Exception e) {
			table.error(testResults, e);
		}
		return null;
	}
	private void runBody(Tables body, TestResults testResults, TestResults subTestResults) {
		for (int t = 0; t < body.size(); t++) {
			Table table = body.table(t);
			if (testResults.isAbandoned())
				table.ignore(subTestResults);
			else
				doTraverse.interpretWholeTable(table,subTestResults);
		}
	}
	private void colourReport(Row row, TestResults testResults, TestResults subTestResults) {
		if (testResults.isAbandoned())
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
