/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.dynamicVariable.DynamicVariablesRecordingThatFails;
import fitlibrary.dynamicVariable.DynamicVariablesRecordingToFile;
import fitlibrary.dynamicVariable.GlobalDynamicVariables;
import fitlibrary.dynamicVariable.LocalDynamicVariables;
import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.flow.GlobalScope;
import fitlibrary.flow.IScope;
import fitlibrary.log.FileLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.tableProxy.CellProxy;
import fitlibrary.traverse.TableEvaluator;
import fitlibrary.traverse.workflow.caller.DefinedActionCallManager;

public class RuntimeContextContainer implements RuntimeContextInternal {
	private static final String EXPAND_DEFINED_ACTIONS = "$$expandDefinedActions$$";
	protected DynamicVariables dynamicVariables = new GlobalDynamicVariables();
	private Map<String,Integer> timeouts = new HashMap<String, Integer>();
	private FileLogger fileLogger = new FileLogger();
	private IScope scope;
	private TableEvaluator tableEvaluator;
	private GlobalScope global;
	// Remember to copy across any added valuable thing inside freshCopy()
	// Following are local to a storytest and so are not copied across a suite:
	private boolean abandonedStorytest = false;
	private boolean stopOnError = false;
	private DynamicVariablesRecording dynamicVariablesRecording = new DynamicVariablesRecordingThatFails();
	private DefinedActionCallManager definedActionCallManager = new DefinedActionCallManager();
	private FoldingTexts foldingTexts = new FoldingTexts();
	protected Row currentRow;
	protected Table currentTable;
	protected TestResults testResults;
	private Stack<TestResults> testResultsStack = new Stack<TestResults>();

	public RuntimeContextContainer() {
		//
	}
	public RuntimeContextContainer(IScope scope, GlobalScope global) {
		this.scope = scope;
		this.global = global;
	}
	public RuntimeContextContainer(String[] s) {
		for (int i = 0; i < s.length-1; i += 2)
			dynamicVariables.put(s[i],s[i+1]);
	}
	public RuntimeContextContainer(DynamicVariables dynamicVariables, Map<String,Integer> timeouts, 
			FileLogger fileLogger, IScope scope, TableEvaluator tableEvaluator, GlobalScope global) {
		this.dynamicVariables = dynamicVariables;
		this.timeouts = timeouts;
		this.fileLogger = fileLogger;
		this.scope = scope;
		this.tableEvaluator = tableEvaluator;
		this.global = global;
	}
	public RuntimeContextInternal freshCopy() {
		return new RuntimeContextContainer(
				new GlobalDynamicVariables(dynamicVariables.top()),
				timeouts,
				fileLogger,
				scope,
				tableEvaluator,
				global);
	}
	public DynamicVariables getDynamicVariables() {
		return dynamicVariables;
	}
	@Override
	public String toString() {
		return getDynamicVariables().toString();
	}
	public void putTimeout(String name, int timeout) {
		timeouts.put(name,timeout);
	}
	public int getTimeout(String name, int defaultTimeout) {
		Integer timeout = timeouts.get(name);
		if (timeout == null)
			return defaultTimeout;
		return timeout;
	}
	public void startLogging(String fileName) {
		fileLogger.start(fileName);
	}
	public void printToLog(String s) throws IOException {
		fileLogger.println(s);
	}
	public void pushLocalDynamicVariables() {
		dynamicVariables = new LocalDynamicVariables(dynamicVariables);
	}
	public void popLocalDynamicVariables() {
		dynamicVariables = dynamicVariables.popLocal();
	}
	public void setDynamicVariable(String key, Object value) {
		dynamicVariables.put(key, value);
	}
	public Object getDynamicVariable(String key) {
		return dynamicVariables.get(key);
	}
	public boolean toExpandDefinedActions() {
		return "true".equals(getDynamicVariable(EXPAND_DEFINED_ACTIONS));
	}
	public void setExpandDefinedActions(boolean expandDefinedActions) {
		setDynamicVariable(EXPAND_DEFINED_ACTIONS, ""+expandDefinedActions);
	}
	@Override
	public IScope getScope() {
		return scope;
	}
	@Override
	public boolean hasScope() {
		return scope != null;
	}
	public void SetTableEvaluator(TableEvaluator evaluator) {
		this.tableEvaluator = evaluator;
	}
	@Override
	public TableEvaluator getTableEvaluator() {
		return tableEvaluator;
	}
	@Override
	public GlobalScope getGlobal() {
		return global;
	}
	public void showAsAfterTable(String title, String s) {
		foldingTexts.logAsAfterTable(title, s);
	}
	@Override
	public void addAccumulatedFoldingText(Table table) {
		foldingTexts.addAccumulatedFoldingText(table);
	}
	@Override
	public void recordToFile(String fileName) {
		dynamicVariablesRecording = new DynamicVariablesRecordingToFile(fileName);
	}
	@Override
	public DynamicVariablesRecording getDynamicVariableRecorder() {
		return dynamicVariablesRecording;
	}
	public boolean abandonedStorytest() {
		return abandonedStorytest;
	}
	@Override
	public void setAbandon(boolean b) {
		abandonedStorytest = b;
	}
	@Override
	public boolean isAbandoned(TestResults testResults) {
		return abandonedStorytest || (stopOnError && testResults.problems());
	}
	@Override
	public void setStopOnError(boolean stop) {
		stopOnError = stop;
	}
	@Override
	public DefinedActionCallManager getDefinedActionCallManager() {
		return definedActionCallManager ;
	}
	@Override
	public VariableResolver getResolver() {
		return getDynamicVariables();
	}
	@Override
	public void setCurrentRow(Row row) {
		currentRow = row;
	}
	@Override
	public void setCurrentTable(Table table) {
		currentTable = table;
	}
	@Override
	public boolean hasRowsAfter(Row row) {
		if (currentTable == null || currentRow == null)
			return false;
		return currentTable.hasRowsAfter(currentRow);
	}
	@Override
	public void addShow(String s) {
		currentRow.addCell(s).shown();
	}
	@Override
	public TestResults getTestResults() {
		return testResults;
	}
	public void pushTestResults(TestResults results) {
		testResultsStack.push(this.testResults);
		this.testResults = results;
	}
	@Override
	public void popTestResults() {
		this.testResults = testResultsStack.pop();
	}
	@Override
	public CellProxy cellAt(final int i) {
		return new CellProxy() {
			@Override
			public void pass() {
				currentRow.at(i).pass(testResults);
			}
			@Override
			public void fail(String msg) {
				if (msg.isEmpty())
					currentRow.at(i).fail(testResults);
				else
					currentRow.at(i).fail(testResults,msg,dynamicVariables);
			}
			@Override
			public void error(String msg) {
				if (msg.isEmpty())
					currentRow.at(i).error(testResults);
				else
					currentRow.at(i).error(testResults,msg);
			}
		};
	}
}
