/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import fit.Fixture;
import fitlibrary.DefineAction;
import fitlibrary.definedAction.DefineActionsOnPage;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.global.PlugBoard;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.log.ConfigureLogger;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.polling.Eventually;
import fitlibrary.polling.PassFail;
import fitlibrary.polling.PollForPass;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.special.DoAction;
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
	private static Logger logger = FitLibraryLogger.getLogger(GlobalActionScope.class);
	public static final String STOP_WATCH = "$$STOP WATCH$$";
	public static final String BECOMES_TIMEOUT = "becomes";
	private RuntimeContextInternal runtimeContext;

	@Override
	public void setRuntimeContext(RuntimeContextInternal runtime) {
		this.runtimeContext = runtime;
	}
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
	public void autoTranslateDefinedActionParameters() {
		setDynamicVariable(DefineAction.AUTO_TRANSLATE_DEFINED_ACTION_PARAMETERS, "true");
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
	//--- LOGGING
	public ConfigureLogger withLog4j() {
		return runtimeContext.getConfigureLog4j().withNormalLog4j();
	}
	public ConfigureLogger withFitLibraryLogger() {
		return runtimeContext.getConfigureLog4j().withFitLibraryLogger();
	}
	public ConfigureLogger withFixturingLogger() {
		return runtimeContext.getConfigureLog4j().withFixturingLogger();
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
	public void logText(String s) {
		runtimeContext.getConfigureLog4j().log(s);
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
	public Object getSystemUnderTest() {
		return null;
	}
	//--- SELECT
	public void select(String name) {
		runtimeContext.getTableEvaluator().select(name);
	}
	
	//-------------------------------------- SPECIALS -----------------------------------------
	/** Check that the result of the action in the first part of the row is less than
	 *  the expected value in the last cell of the row.
	 */
	public void lessThan(DoAction action, Object expect) throws Exception {
		comparison(action,expect,new Comparison(){
			@Override @SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) < 0;
			}
		});
	}
	/** Check that the result of the action in the first part of the row is less than
	 *  or equal to the expected value in the last cell of the row.
	 */
	public void lessThanEquals(DoAction action, Object expect) throws Exception {
		comparison(action,expect,new Comparison(){
			@Override @SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) <= 0;
			}
		});
	}
	/** Check that the result of the action in the first part of the row is greater than
	 *  the expected value in the last cell of the row.
	 */
	public void greaterThan(DoAction action, Object expect) throws Exception {
		comparison(action,expect,new Comparison(){
			@Override @SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) > 0;
			}
		});
	}
	/** Check that the result of the action in the first part of the row is greater than
	 *  or equal to the expected value in the last cell of the row.
	 */
	public void greaterThanEquals(DoAction action, Object expect) throws Exception {
		comparison(action,expect,new Comparison(){
			@Override @SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) >= 0;
			}
		});
	}
	@SuppressWarnings("unchecked")
	private void comparison(DoAction action, Object expected, Comparison compare) throws Exception {
		if (!(expected instanceof Comparable<?>))
			throw new FitLibraryException("Expected value is not a Comparable");
		Object actual = action.run();
		if (actual instanceof Comparable<?>) {
			if (compare.compares((Comparable)actual,(Comparable)expected))
				action.cellAt(1).pass();
			else
				action.cellAt(1).fail(actual.toString());
		} else if (actual == null)
			throw new FitLibraryException("Actual value is null");
		else
			throw new FitLibraryException("Actual value is not a Comparable: "+actual.getClass().getName());
	}
	public interface Comparison {
		@SuppressWarnings("unchecked")
		boolean compares(Comparable actual, Comparable expected);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void contains(DoAction action, String s) throws Exception {
		if (s == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		Object run = action.run();
		if (run == null) {
			action.cellAt(1).fail("result is null");
			return;
		}
		String result = run.toString();
		if (result.contains(s))
			action.cellAt(1).pass();
		else
			action.cellAt(1).fail(result);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void doesNotContain(DoAction action, String s) throws Exception {
		if (s == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		Object run = action.run();
		if (run == null) {
			action.cellAt(1).fail("result is null");
			return;
		}
		String result = run.toString();
		if (!result.contains(s))
			action.cellAt(1).pass();
		else if (result.equals(s))
			action.cellAt(1).fail();
		else
			action.cellAt(1).fail(result);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void eventuallyContains(final DoAction action, final String s) throws Exception {
		if (s == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		Eventually eventually = new Eventually(getTimeout(BECOMES_TIMEOUT));
		PassFail answer = eventually.poll(new PollForPass() {
			@Override
			public PassFail result() throws Exception {
				Object run = action.run();
				if (run == null)
					return new PassFail(false, null);
				String result = run.toString();
				return new PassFail(result.contains(s), result);
			}
		});
		if (answer != null && answer.result != null) {
			if (answer.hasPassed)
				action.cellAt(1).pass();
			else
				action.cellAt(1).fail(answer.result.toString());
		} else
			action.cellAt(1).fail("result is null");
	}
	public void show(DoAction action) throws Exception {
		Object result = action.run();
		if (result != null)
			action.showResult(result);
	}
	public void showEscaped(DoAction action) throws Exception {
		Object result = action.run();
		if (result != null)
			action.show(Fixture.escape(result.toString()));
	}
	public void showAfter(DoAction action) throws Exception {
		Object result = action.run();
		if (result != null)
			action.showAfter(result);
	}
	public void showAfterAs(String title, DoAction action) throws Exception {
		Object result = action.run();
		if (result != null)
			action.showAfterAs(title,result);
	}
	/** Check that the result of the action in the first part of the row, as a string, matches
	 *  the regular expression in the last cell of the row.
	 */
	public void matches(DoAction action, String pattern) throws Exception {
		if (pattern == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		Object run = action.run();
		if (run == null) {
			action.cellAt(1).fail("result is null");
			return;
		}
		String result = run.toString();
		boolean matches = Pattern.compile(".*"+pattern+".*",Pattern.DOTALL).matcher(result).matches();
		if (matches)
			action.cellAt(1).pass();
		else
			action.cellAt(1).fail(result);
	}
	/** Check that the result of the action in the first part of the row, as a string, does not match
	 *  the regular expression in the last cell of the row.
	 */
	public void doesNotMatch(DoAction action, String pattern) throws Exception {
		if (pattern == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		Object run = action.run();
		if (run == null) {
			action.cellAt(1).fail("result is null");
			return;
		}
		String result = run.toString();
		boolean matches = Pattern.compile(".*"+pattern+".*",Pattern.DOTALL).matcher(result).matches();
		if (!matches)
			action.cellAt(1).pass();
		else if (result.equals(pattern))
			action.cellAt(1).fail();
		else
			action.cellAt(1).fail(result);
	}
	/** Check that the result of the action in the first part of the row, as a string, eventually matches
	 *  the regular expression in the last cell of the row.
	 */
	public void eventuallyMatches(final DoAction action, final String s) throws Exception {
		if (s == null) {
			action.cellAt(1).fail("expected is null");
			return;
		}
		final Pattern pattern = Pattern.compile(".*"+s+".*",Pattern.DOTALL);
		Eventually eventually = new Eventually(getTimeout(BECOMES_TIMEOUT));
		PassFail answer = eventually.poll(new PollForPass() {
			@Override
			public PassFail result() throws Exception {
				Object run = action.run();
				if (run == null)
					return new PassFail(false, null);
				String result = run.toString();
				return new PassFail(pattern.matcher(result).matches(), result);
			}
		});
		if (answer != null && answer.result != null) {
			if (answer.hasPassed)
				action.cellAt(1).pass();
			else if (answer.result != null)
				action.cellAt(1).fail(answer.result.toString());
			else
				action.cellAt(1).fail();
		} else
			action.cellAt(1).fail("result is null");
	}
	/** Check that the action in the rest of the row succeeds.
     *  o If a boolean is returned, it must be true.
     *  o For other result types, no exception should be thrown.
     *  It's no longer needed, because the same result can now be achieved with a boolean method.
     */
	public Boolean ensure(DoAction action) throws Exception {
		Object result = action.run();
		if (result instanceof Boolean)
			return ((Boolean)result).booleanValue();
		if (result == null)
			return true;
		return null;
	}
	public Boolean not(DoAction action) throws Exception {
		Object result = null;
		try {
			result = action.runWithNoColouring();
			if (result instanceof Boolean)
				return !((Boolean)result).booleanValue();
		} catch (IgnoredException e) {
			if (e.getIgnoredException() != null)
				action.show(e.getIgnoredException().getMessage());
			return true;
		} catch (Exception e) {
			Throwable embedded = PlugBoard.exceptionHandling.unwrapThrowable(e);
			if (embedded instanceof FitLibraryShowException)
				action.show(((FitLibraryShowException)embedded).getResult().getHtmlString());
			return true;
		}
		if (result == null)
			throw new NotRejectedException();
		return null;
	}
	public boolean notTrue(DoAction action) throws Exception {
		Object result = action.run();
		if (result instanceof Boolean)
			return !((Boolean)result).booleanValue();
		throw new NotRejectedException();
	}
	public boolean reject(DoAction action) throws Exception {
		return not(action);
	}
	/** Log result to a file
	 */
	public void log(DoAction action) throws Exception {
		Object result = action.run();
		if (result != null)
			logMessage(result.toString());
	}
	/** Log result to log4j
	 */
	public void logged(DoAction action) throws Exception {
		logger.trace("called logged");
		Object result = action.run();
		if (result != null)
			runtimeContext.getConfigureLog4j().log(result.toString());
	}
	
//	public void is(DoAction action, Object expected) throws Exception {
//		Object result = action.run();
//		if (action.equals(result,expected))
//			action.cellAt(1).pass();
//		else
//			action.cellAt(1).fail(result.toString());
//	}
}
