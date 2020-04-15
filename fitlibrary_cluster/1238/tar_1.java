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
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Cell;
import fitlibrary.table.IRow;
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
	private RuntimeContextInternal runtime;
	private List<Object> actualArgs = new ArrayList<Object>();

	public DefinedActionCaller(Row row, DoTraverseInterpreter doTraverse) {
		this.doTraverse = doTraverse;
		this.runtime = doTraverse.getRuntimeContext();
		methodName = row.methodNameForCamel(doTraverse);
		actualArgs = actualArgs(row);
		parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByCamel(methodName, actualArgs.size());
		if (parameterSubstitution == null) {
			Object objectName = runtime.getDynamicVariable("this");
			if (objectName != null) {
				Object className = runtime.getDynamicVariable(objectName+".class");
				actualArgs.add(0,objectName.toString());
				if (className != null && !"".equals(className))
					parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel(className.toString(), methodName, (actualArgs.size()-1), doTraverse.getRuntimeContext());
			}
		}
	}
	public DefinedActionCaller(String object, String className, Row row, DoTraverseInterpreter doTraverse) {
		this.doTraverse = doTraverse;
		this.runtime = doTraverse.getRuntimeContext();
		methodName = row.methodNameForCamel(doTraverse);
		actualArgs.add(object);
		actualArgs(row,actualArgs);
		this.parameterSubstitution = TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel(className, methodName, (actualArgs.size()-1), doTraverse.getRuntimeContext());
		if (parameterSubstitution == null)
			throw new FitLibraryException("Unknown defined action for object of class "+className);
	}
	@Override
	public boolean isValid() {
		return parameterSubstitution != null;
	}
	@Override
	public Object run(IRow row, TestResults testResults) {
		CallManager.startCall(parameterSubstitution);
		try {
			Object oldThisValue = runtime.getDynamicVariable("this");
			if (!actualArgs.isEmpty())
				runtime.setDynamicVariable("this", actualArgs.get(0));
			processDefinedAction(parameterSubstitution.substitute(actualArgs),row,testResults);
			runtime.setDynamicVariable("this", oldThisValue);
		} finally {
			CallManager.endCall(parameterSubstitution);
		}
		if (!runtime.toExpandDefinedActions() && CallManager.readyToShow() && !testResults.isAbandoned())
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
				result.add(cell.text(runtime));
		}
		return result;
	}
	private void processDefinedAction(Tables definedActionBody, IRow row, TestResults testResults) {
		TestResults subTestResults = new TestResults(testResults);
		for (int i = 0; i < definedActionBody.size(); i++) {
			Table table = definedActionBody.table(i);
			if (testResults.isAbandoned())
				table.ignore(subTestResults);
			else
				doTraverse.interpretWholeTable(table,subTestResults);
		}
		colourReport(definedActionBody, row, testResults, subTestResults);
	}
	private void colourReport(Tables body, IRow row,
			TestResults testResults, TestResults subTestResults) {
		if (runtime.toExpandDefinedActions() || subTestResults.problems() || testResults.isAbandoned()) {
			if (testResults.isAbandoned()) {
				// Leave it to caller to ignore the row
			} else if (subTestResults.passed())
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
			String pageName = parameterSubstitution.getPageName();
			row.addCell(new Cell(link(pageName),body));
		} else if (!testResults.isAbandoned())
			row.passKeywords(testResults);
	}
	public static String link(String pageName) {
		if (pageName.startsWith("from storytest"))
			return "Defined action call:";
		return "Defined action call <a href='"+pageName+"'>."+pageName+"</a>:";
	}
	public static String link2(String pageName) {
		if (pageName.startsWith("from storytest"))
			return "storytest";
		return "<a href='"+pageName+"'>."+pageName+"</a>:";
	}
}