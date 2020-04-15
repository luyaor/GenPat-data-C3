/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.Fixture;
import fitlibrary.DefineAction;
import fitlibrary.definedAction.DefineActionsOnPage;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SuiteFixture;
import fitlibrary.table.Row;
import fitlibrary.traverse.CommentTraverse;
import fitlibrary.traverse.RuntimeContextual;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.RandomSelectTraverse;
import fitlibrary.traverse.workflow.SetVariableTraverse;
import fitlibrary.traverse.workflow.StopWatch;
import fitlibrary.utility.FileHandler;
import fitlibrary.xref.CrossReferenceFixture;

public class GlobalActionScope implements RuntimeContextual {
	public static final String STOP_WATCH = "$$STOP WATCH$$";
	public static final String BECOMES_TIMEOUT = "becomes";
	private RuntimeContextInternal runtimeContext;

	//--- BECOMES, ETC TIMEOUTS:
	public void becomesTimeout(int timeout) {
		putTimeout(BECOMES_TIMEOUT,timeout);
	}
	public int becomesTimeout() {
		return getTimeout(BECOMES_TIMEOUT);
	}
	public int getTimeout(String name) {
		return runtimeContext.getTimeout(name,1000);
	}
	public void putTimeout(String name, int timeout) {
		runtimeContext.putTimeout(name,timeout);
	}
	//--- STOP ON ERROR AND ABANDON:
	/** When (stopOnError), don't continue interpreting a table if there's been a problem */
	public void setStopOnError(boolean stopOnError) {
		runtimeContext.setStopOnError(stopOnError);
	}
	public void abandonStorytest() {
		runtimeContext.setAbandon(true);
	}
	//--- DYNAMIC VARIABLES:
    public DynamicVariables getDynamicVariables() {
    	return runtimeContext.getDynamicVariables();
    }
	public void setDynamicVariable(String key, Object value) {
		getDynamicVariables().put(key, value);
	}
	public Object getDynamicVariable(String key) {
		return getDynamicVariables().get(key);
	}
	public boolean clearDynamicVariables() {
		getDynamicVariables().clearAll();
		return true;
	}
	public boolean addDynamicVariablesFromFile(String fileName) {
		return getDynamicVariables().addFromPropertiesFile(fileName);
	}
	public void addDynamicVariablesFromUnicodeFile(String fileName) throws IOException {
		getDynamicVariables().addFromUnicodePropertyFile(fileName);
	}
	public boolean setSystemPropertyTo(String property, String value) {
		System.setProperty(property,value);
		setDynamicVariable(property, value);
		return true;
	}
	public void setFitVariable(String variableName, Object result) {
		Fixture.setSymbol(variableName, result);
	}
	public Object getSymbolNamed(String fitSymbolName) {
		return Fixture.getSymbol(fitSymbolName);
	}
	//--- SLEEP & STOPWATCH:
	public boolean sleepFor(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// Nothing to do
		}
		return true;
	}
	public void startStopWatch() {
		setDynamicVariable(STOP_WATCH, new StopWatch());
	}
	public long stopWatch() {
		return getStopWatch().delay();
	}
	private StopWatch getStopWatch() {
		StopWatch stopWatch = (StopWatch) getDynamicVariable(STOP_WATCH);
		if (stopWatch == null)
			throw new FitLibraryException("No stopwatch started");
		return stopWatch;
	}
	//--- FIXTURE SELECTION
    /** The rest of the table is ignored (and not coloured) */
	public CommentTraverse comment() {
		return new CommentTraverse();
	}
    /** The rest of the table is ignored (and the first row is coloured as ignored) */
	public CommentTraverse ignored() {
		return ignore();
	}
	public CommentTraverse ignore() {
		return new CommentTraverse(true);
	}
	public CommentTraverse ignoreTable() {
		return new CommentTraverse(true);
	}
	public CrossReferenceFixture xref(String suiteName) {
		return new CrossReferenceFixture(suiteName);
	}
	public SetVariableTraverse setVariables() {
		return new SetVariableTraverse();
	}
	public DoTraverse file(String fileName) {
		return new DoTraverse(new FileHandler(fileName));
	}
	public SuiteFixture suite() {
		return new SuiteFixture();
	}
	//--- DEFINED ACTIONS
	public DefineAction defineAction(String wikiClassName) {
		DefineAction defineAction = new DefineAction(wikiClassName);
		defineAction.setRuntimeContext(runtimeContext);
		return defineAction;
	}
	public DefineAction defineAction() {
		return new DefineAction();
	}
	public void defineActionsSlowlyAt(String pageName) throws Exception {
		new DefineActionsOnPageSlowly(pageName,runtimeContext).process();
	}
	public void defineActionsAt(String pageName) throws Exception {
		new DefineActionsOnPage(pageName,runtimeContext).process();
	}
	public void defineActionsAtFrom(String pageName, String rootLocation) throws Exception {
		new DefineActionsOnPage(pageName,rootLocation,runtimeContext).process();
	}
	public void clearDefinedActions() {
		TemporaryPlugBoardForRuntime.definedActionsRepository().clear();
	}
	public boolean toExpandDefinedActions() {
		return runtimeContext.toExpandDefinedActions();
	}
	public void setExpandDefinedActions(boolean expandDefinedActions) {
		runtimeContext.setExpandDefinedActions(expandDefinedActions);
	}
	//--- RANDOM, TO, GET, FILE, HARVEST
	public RandomSelectTraverse selectRandomly(String var) {
		return new RandomSelectTraverse(var);
	}
	public String to(String s) {
		return s;
	}
	public String get(String s) {
		return s;
	}
	public void removeFile(String fileName) {
		new File(fileName).delete();
	}
	public boolean harvestUsingPatternFrom(String[] vars, String pattern, String text) {
		Matcher matcher = Pattern.compile(pattern).matcher(text);
	    if (!matcher.find())
	    	throw new FitLibraryException("Pattern doesn't match");
	    int groups = matcher.groupCount();
		if (vars.length > groups)
			throw new FitLibraryException("Expected " + expectedGroups(vars) + ", but there " + actualGroups(groups));
		for (int v = 0; v < vars.length && v < groups; v++)
			setDynamicVariable(vars[v], matcher.group(v+1));
		return true;
	}
	private String expectedGroups(String[] vars) {
		if (vars.length == 1)
			return "1 bracketed group";
		return vars.length + " bracketed groups";
	}
	private String actualGroups(int groups) {
		if (groups == 1)
			return "is only 1";
		return "are only "+groups;
	}
	//--- FILE LOGGING
	public void recordToFile(String fileName) {
		runtimeContext.recordToFile(fileName);
		try {
			addDynamicVariablesFromFile(fileName);
		} catch (Exception e) {
			//
		}
	}
	public void startLogging(String fileName) {
		runtimeContext.startLogging(fileName);
	}
	public void logMessage(String s) {
		try {
			runtimeContext.printToLog(s);
		} catch (IOException e) {
			throw new FitLibraryException(e.getMessage());
		}
	}
	//--- SHOW
	public void show(Row row, String text) {
		row.addCell(text).shown();
		runtimeContext.getDefinedActionCallManager().addShow(row);
	}
	public void showAsAfterTable(String title,String s) {
		runtimeContext.showAsAfterTable(title,s);
	}
	@Override
	public void setRuntimeContext(RuntimeContextInternal runtime) {
		this.runtimeContext = runtime;
	}
	@Override
	public Object getSystemUnderTest() {
		return null;
	}
	//--- SELECT
	public void select(String name) {
		runtimeContext.getTableEvaluator().select(name);
	}
}
