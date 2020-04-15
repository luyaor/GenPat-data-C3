/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runtime;

import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.flow.GlobalActionScope;
import fitlibrary.flow.IScope;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.tableProxy.CellProxy;
import fitlibrary.tableProxy.RowProxy;
import fitlibrary.traverse.TableEvaluator;
import fitlibrary.traverse.workflow.caller.DefinedActionCallManager;

public interface RuntimeContextInternal extends RuntimeContext {
	RuntimeContextInternal copyFromSuite();
	void putTimeout(String name, int timeout);
	int getTimeout(String name, int defaultTimeout);
	void pushLocalDynamicVariables();
	void popLocalDynamicVariables();
	boolean toExpandDefinedActions();
	void setExpandDefinedActions(boolean expandDefinedActions);
	boolean hasScope();
	IScope getScope();
	TableEvaluator getTableEvaluator();
	GlobalActionScope getGlobal();
	void addAccumulatedFoldingText(Table table);
	void showAsAfterTable(String title, String s);
	void recordToFile(String fileName);
	DynamicVariablesRecording getDynamicVariableRecorder();
	void setAbandon(boolean b);
	boolean isAbandoned(TestResults testResults);
	void setStopOnError(boolean stop);
	DefinedActionCallManager getDefinedActionCallManager();
	void setCurrentTable(Table table);
	void setCurrentRow(Row row);
	boolean hasRowsAfter(Row row);
	TestResults getTestResults();
	void pushTestResults(TestResults testResults);
	void popTestResults();
	CellProxy cellAt(int i);
	RowProxy currentRow();
	void reset();
}
