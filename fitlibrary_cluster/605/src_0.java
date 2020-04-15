/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.definedAction.ParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.TestResults;

public class DefinedActionCaller extends DoCaller {
	private ParameterSubstitution parameterSubstitution;
	private String methodName;
	private DoTraverseInterpreter doTraverse;
	private List<Object> actualArgs = new ArrayList<Object>();

	public DefinedActionCaller(Row row, DoTraverseInterpreter doTraverse) {
		this.doTraverse = doTraverse;
		methodName = row.methodNameForCamel(doTraverse);
		actualArgs = actualArgs(row);
		//		System.out.println("DefinedActionCaller.methodName : '"+methodName+"'");
		parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByCamel(methodName, actualArgs.size());
		if (parameterSubstitution == null) {
			Object objectName = doTraverse.getDynamicVariable("this");
			if (objectName != null) {
				Object className = doTraverse.getDynamicVariable(objectName+".class");
				actualArgs.add(0,objectName.toString());
				if (className != null && !"".equals(className))
					parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel(className.toString(), methodName, (actualArgs.size()-1), doTraverse.runtime());
			}
		}
	}
	public DefinedActionCaller(String object, String className, Row row, DoTraverseInterpreter doTraverse) {
		this.doTraverse = doTraverse;
		methodName = row.methodNameForCamel(doTraverse);
		actualArgs.add(object);
		actualArgs(row,actualArgs);
		this.parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel(className, methodName, (actualArgs.size()-1), doTraverse.runtime());
		if (parameterSubstitution == null)
			throw new FitLibraryException("Unknown defined action for object of class "+className);
	}
	@Override
	public boolean isValid() {
		return parameterSubstitution != null;
	}
	@Override
	public Object run(Row row, TestResults testResults) {
		CallManager.startCall(parameterSubstitution);
		try {
			Object oldThisValue = doTraverse.getDynamicVariable("this");
			if (!actualArgs.isEmpty())
				doTraverse.setDynamicVariable("this", actualArgs.get(0));
			Object result = processDefinedAction(parameterSubstitution.substitute(actualArgs,doTraverse),row,testResults);
			doTraverse.setDynamicVariable("this", oldThisValue);
		} finally {
			CallManager.endCall(parameterSubstitution);
		}
		if (!doTraverse.toExpandDefinedActions() && CallManager.readyToShow() && !testResults.isAbandoned())
			row.addCell(new Cell(new Tables(CallManager.getShowsTable())));
		return null;
	}
	@Override
	public String ambiguityErrorMessage() {
		return "defined action "+methodName;
	}
	private List<Object> actualArgs(Row row) {
		return actualArgs(row, new ArrayList<Object>());
	}
	private List<Object> actualArgs(Row row, List<Object> result) {
		for (int i = 1; i < row.size(); i += 2) {
			Cell cell = row.cell(i);
			if (cell.hasEmbeddedTable())
				result.add(cell.getEmbeddedTables());
			else
				result.add(cell.text(doTraverse));
		}
		return result;
	}
	private Object processDefinedAction(Tables definedActionBody, Row row, TestResults testResults) {
		Object lastResult = null;
		TestResults subTestResults = new TestResults(testResults);
		for (int i = 0; i < definedActionBody.size(); i++) {
			Table table = definedActionBody.table(i);
			if (testResults.isAbandoned()) {
				table.ignore(subTestResults);
				lastResult = null;
			} else
				lastResult = doTraverse.interpretWholeTable(table,subTestResults);
		}
		colourReport(definedActionBody, row, testResults, subTestResults);
		return lastResult;
	}
	private void colourReport(Tables body, Row row,
			TestResults testResults, TestResults subTestResults) {
		if (doTraverse.toExpandDefinedActions() || subTestResults.problems() || testResults.isAbandoned()) {
			if (testResults.isAbandoned())
				; // Leave it to caller to ignore the row
			else if (subTestResults.passed())
				row.passKeywords(testResults);
			else if (subTestResults.errors())
				for (int i = 0; i < row.size(); i += 2)
					row.cell(i).error(testResults, new FitLibraryException(""));
			else if (subTestResults.failed())
				for (int i = 0; i < row.size(); i += 2)
					row.cell(i).fail(testResults);
			else
				for (int i = 0; i < row.size(); i += 2)
					row.cell(i).ignore(testResults);
			row.addCell(new Cell("Defined action call:",body));
		} else if (!testResults.isAbandoned())
			row.passKeywords(testResults);
	}
}