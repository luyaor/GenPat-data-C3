/*
a * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fit.Fixture;
import fitlibrary.DefineAction;
import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.definedAction.DefineActionsOnPage;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.dynamicVariable.RecordDynamicVariables;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.global.PlugBoard;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.parser.Parser;
import fitlibrary.parser.graphic.GraphicParser;
import fitlibrary.parser.graphic.ObjectDotGraphic;
import fitlibrary.table.Cell;
import fitlibrary.table.IRow;
import fitlibrary.table.Row;
import fitlibrary.traverse.FitHandler;
import fitlibrary.traverse.CommentTraverse;
import fitlibrary.traverse.function.CalculateTraverse;
import fitlibrary.traverse.function.ConstraintTraverse;
import fitlibrary.traverse.workflow.caller.CallManager;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.traverse.workflow.special.PrefixSpecialAction;
import fitlibrary.traverse.workflow.special.SpecialActionContext;
import fitlibrary.traverse.workflow.special.PrefixSpecialAction.NotSyle;
import fitlibrary.typed.NonGenericTyped;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.FileHandler;
import fitlibrary.utility.TestResults;
import fitlibrary.xref.CrossReferenceFixture;

public class DoTraverse extends DoTraverseInterpreter implements SpecialActionContext, FlowEvaluator{
	private final PrefixSpecialAction prefixSpecialAction = new PrefixSpecialAction(this);
	private static final String STOP_WATCH = "$$STOP WATCH$$";
	public static final String BECOMES_TIMEOUT = "becomes";
	// Methods that can be called within DoTraverse.
	// Each element is of the form "methodName/argCount"
	private final static String[] methodsThatAreVisibleAsActions = {
		"calculate/0", "start/1", "constraint/0", "failingConstraint/0",
		"useTemplate/1", "template/1", "abandonStorytest/0", "setStopOnError/1",
		"becomesTimeout/0", "becomesTimeout/1", "getTimeout/1",
		"comment/0", "ignore/0", "ignored/0", "ignoreTable/0",
		"clearDynamicVariables/0", "addDynamicVariablesFromFile/1", "recordToFile/1",
		"setVariables/0", "to/1", "get/1", "getDynamicVariables/0", 
		"getSymbolNamed/1", "setSymbolNamed/1",
		"setExpandDefinedActions/1", // defined in superclass
		"selectRandomly/1",
		"defineAction/0", "defineAction/1", "defineActionsAt/1",
		"defineActionsAtFrom/2",
		"defineActionsSlowlyAt/1", "clearDefinedActions/0", 
		"startLogging/1", "logMessage/1", "showAfterTable/1",
		"addDynamicVariablesFromUnicodeFile/1", "file/1",
		"xref/1", "harvestUsingPatternFrom/3",
		"setSystemPropertyTo/2",
		"startStopWatch/0", "stopWatch/0", "sleepFor/1",
		"autoWrapPojoWithDoFixture/0"
	};
	public void autoWrapPojoWithDoFixture() {
		//
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
	// SLEEP
	public boolean sleepFor(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// Nothing to do
		}
		return true;
	}

	public DoTraverse() {
		super();
	}
	public DoTraverse(Object sut) {
		super(sut);
	}
	public DoTraverse(TypedObject typedObject) {
		super(typedObject);
	}

	//------------------- Methods that are visible as actions (the rest are hidden):
	public List<String> methodsThatAreVisible() {
		return Arrays.asList(methodsThatAreVisibleAsActions);
	}
	/** To allow for a CalculateTraverse to be used for the rest of the table.
     */
	public CalculateTraverse calculate() {
		CalculateTraverse traverse;
		if (this.getClass() == DoTraverse.class)
			traverse = new CalculateTraverse(getTypedSystemUnderTest());
		else
			traverse = new CalculateTraverse(this);
		return traverse;
	}
    /** To allow for DoTraverse to be used without writing any fixturing code.
     */
	public void start(String className) {
		try {
		    setSystemUnderTest(ClassUtility.newInstance(className));
		} catch (Exception e) {
		    throw new FitLibraryException("Unknown class: "+className);
		}
	}
	/** To allow for a ConstraintTraverse to be used for the rest of the table.
     */
	public ConstraintTraverse constraint() {
		return new ConstraintTraverse(this);
	}
	/** To allow for a failing ConstraintTraverse to be used for the rest of the table.
     */
	public ConstraintTraverse failingConstraint() {
		ConstraintTraverse traverse = new ConstraintTraverse(this,false);
		return traverse;
	}
	public void becomesTimeout(int timeout) {
		runtimeContext.putTimeout(BECOMES_TIMEOUT,timeout);
	}
	public int becomesTimeout() {
		return runtimeContext.getTimeout(BECOMES_TIMEOUT,1000);
	}
//	/** To support defined actions */
//	public UseTemplateTraverse useTemplate(String name) {
//		return new UseTemplateTraverse(name);
//	}
//	/** To support defined actions */
//	public DefinedActionTraverse template(@SuppressWarnings("unused") String name) {
//		return new DefinedActionTraverse();
//	}
	/** When (stopOnError), don't continue intepreting a table if there's been a problem */
	public void setStopOnError(boolean stopOnError) {
		TestResults.setStopOnError(stopOnError);
	}
	public void abandonStorytest() {
		TestResults.setAbandoned();
	}
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
	public boolean clearDynamicVariables() {
		getDynamicVariables().clearAll();
		return true;
	}
	public boolean addDynamicVariablesFromFile(String fileName) {
		return getDynamicVariables().addFromPropertiesFile(fileName);
	}
	public void recordToFile(String fileName) {
		RecordDynamicVariables.recordToFile(fileName);
		try {
			addDynamicVariablesFromFile(fileName);
		} catch (Exception e) {
			//
		}
	}
	public SetVariableTraverse setVariables() {
		return new SetVariableTraverse();
	}
	public String to(String s) {
		return s;
	}
	public String get(String s) {
		return s;
	}
	public DefineAction defineAction() {
		return new DefineAction();
	}
	public DefineAction defineAction(String wikiClassName) {
		DefineAction defineAction = new DefineAction(wikiClassName);
		defineAction.setRuntimeContext(getRuntimeContext());
		return defineAction;
	}
	public DefineActionsOnPageSlowly defineActionsSlowlyAt(String pageName) {
		return new DefineActionsOnPageSlowly(pageName);
	}
	public DefineActionsOnPage defineActionsAt(String pageName) {
		return new DefineActionsOnPage(pageName);
	}
	public DefineActionsOnPage defineActionsAtFrom(String pageName, String rootLocation) {
		return new DefineActionsOnPage(pageName,rootLocation);
	}
	public void clearDefinedActions() {
		TemporaryPlugBoardForRuntime.definedActionsRepository().clear();
	}
	public RandomSelectTraverse selectRandomly(String var) {
		return new RandomSelectTraverse(var);
	}
	// FILE LOGGING
	public void startLogging(String fileName) {
		getRuntimeContext().startLogging(fileName);
	}
	public void logMessage(String s) {
		try {
			getRuntimeContext().printToLog(s);
		} catch (IOException e) {
			throw new FitLibraryException(e.getMessage());
		}
	}
	public void showAfterTable(String s) {
		TestResults.logAfterTable(s+"\n");
	}
	public void addDynamicVariablesFromUnicodeFile(String fileName) throws IOException {
		getDynamicVariables().addFromUnicodePropertyFile(fileName);
	}
	public DoTraverse file(String fileName) {
		return new DoTraverse(new FileHandler(fileName));
	}
//	private Object getLeafSut() {
//		Object sut = this;
//		while (sut instanceof Evaluator) {
//			Object sut2 = ((Evaluator)sut).getSystemUnderTest();
//			if (sut2 != null)
//				sut = sut2;
//			else
//				break;
//		}
//		return sut;
//	}
	public CrossReferenceFixture xref(String suiteName) {
		return new CrossReferenceFixture(suiteName);
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
	public boolean setSystemPropertyTo(String property, String value) {
		System.setProperty(property,value);
		setDynamicVariable(property, value);
		return true;
	}
	//------------------- Postfix Special Actions:
	/** Check that the result of the action in the first part of the row is the same as
	 *  the expected value in the last cell of the row.
	 */
	public void is(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		target.invokeAndCheckForSpecial(row.rowTo(1,row.size()-2),expectedCell,testResults,row,operatorCell(row));
	}
	public void equals(TestResults testResults, final Row row) throws Exception {
		is(testResults,row);
	}
	/** Check that the result of the action in the first part of the row is not the same as
	 *  the expected value in the last cell of the row.
	 */
	public void isNot(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		Cell specialCell = operatorCell(row);
		Cell expectedCell = row.last();
		try {
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Object result = target.invoke(row.rowTo(1,row.size()-2),testResults,true);
			target.notResult(expectedCell, result, testResults);
        } catch (IgnoredException e) {
            //
        } catch (InvocationTargetException e) {
        	Throwable embedded = e.getTargetException();
        	if (embedded instanceof FitLibraryShowException) {
        		specialCell.error(testResults);
        		row.error(testResults, e);
        	} else
        		expectedCell.exceptionMayBeExpected(false, e, testResults);
        } catch (Exception e) {
        	expectedCell.exceptionMayBeExpected(false, e, testResults);
        }
	}
	/** Check that the result of the action in the first part of the row is less than
	 *  the expected value in the last cell of the row.
	 */
	public void lessThan(TestResults testResults, final Row row) throws Exception {
		Comparison compare = new Comparison() {
			@SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) < 0;
			}
		};
		comparison(testResults, row, compare);
	}
	/** Check that the result of the action in the first part of the row is less than
	 *  or equal to the expected value in the last cell of the row.
	 */
	public void lessThanEquals(TestResults testResults, final Row row) throws Exception {
		Comparison compare = new Comparison() {
			@SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) <= 0;
			}
		};
		comparison(testResults, row, compare);
	}
	/** Check that the result of the action in the first part of the row is greater than
	 *  the expected value in the last cell of the row.
	 */
	public void greaterThan(TestResults testResults, final Row row) throws Exception {
		Comparison compare = new Comparison() {
			@SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) > 0;
			}
		};
		comparison(testResults, row, compare);
	}
	/** Check that the result of the action in the first part of the row is greater than
	 *  or equal to the expected value in the last cell of the row.
	 */
	public void greaterThanEquals(TestResults testResults, final Row row) throws Exception {
		Comparison compare = new Comparison() {
			@SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) >= 0;
			}
		};
		comparison(testResults, row, compare);
	}
	@SuppressWarnings("unchecked")
	private void comparison(TestResults testResults, final Row row,
			Comparison compare) {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		Cell specialCell = operatorCell(row);
		Cell expectedCell = row.last();
		try {
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Object result = target.invoke(row.rowTo(1,row.size()-2),testResults,true);
			if (result instanceof Comparable) {
				target.compare(expectedCell, (Comparable)result, testResults, compare);
			} else
				throw new FitLibraryException("Unable to compare, as not Comparable");
        } catch (IgnoredException e) {
            //
        } catch (InvocationTargetException e) {
        	Throwable embedded = e.getTargetException();
        	if (embedded instanceof FitLibraryShowException) {
        		specialCell.error(testResults);
        		row.error(testResults, e);
        	} else
        		expectedCell.exceptionMayBeExpected(false, e, testResults);
        } catch (Exception e) {
        	expectedCell.exceptionMayBeExpected(false, e, testResults);
        }
	}
	public interface Comparison {
		@SuppressWarnings("unchecked")
		boolean compares(Comparable actual, Comparable expected);
	}
	private Cell operatorCell(final Row row) {
		return row.cell(row.size()-2);
	}
	/** Check that the result of the action in the first part of the row, as a string, matches
	 *  the regular expression in the last cell of the row.
	 */
	public void matches(TestResults testResults, final Row row) throws Exception {
		try
		{
			int less = 3;
			if (row.size() < less)
				throw new MissingCellsException("DoTraverseMatches");
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Cell expectedCell = row.last();
			String result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
			boolean matches = Pattern.compile(".*"+expectedCell.text(this)+".*",Pattern.DOTALL).matcher(result).matches();
			if (matches)
				expectedCell.pass(testResults);
			else
				expectedCell.fail(testResults, result,this);
		} catch (PatternSyntaxException e) {
			throw new FitLibraryException(e.getMessage());
		}
	}
	/** Check that the result of the action in the first part of the row, as a string, eventually matches
	 *  the regular expression in the last cell of the row.
	 */
	public void eventuallyMatches(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("eventuallyMatches");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		Pattern compile = Pattern.compile(".*"+expectedCell.text(this)+".*",Pattern.DOTALL);
		
		String result = "";
		long start = System.currentTimeMillis();
		int becomesTimeout = getTimeout(BECOMES_TIMEOUT);
		while (System.currentTimeMillis() - start < becomesTimeout ) {
			result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
			boolean matches = compile.matcher(result).matches();
			if (matches) {
				expectedCell.pass(testResults);
				return;
			}
			try {
				Thread.sleep(Math.max(500, Math.min(100,becomesTimeout/10)));
			} catch (Exception e) {
				//
			}
		}
		expectedCell.fail(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string, does not match
	 *  the regular expression in the last cell of the row.
	 */
	public void doesNotMatch(TestResults testResults, final Row row) throws Exception {
		try
		{
			int less = 3;
			if (row.size() < less)
				throw new MissingCellsException("DoTraverseMatches");
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Cell expectedCell = row.last();
			String result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
			if (!Pattern.compile(".*"+expectedCell.text(this)+".*",Pattern.DOTALL).matcher(result).matches())
				expectedCell.pass(testResults);
			else if (expectedCell.text(this).equals(result))
				expectedCell.fail(testResults);
			else
				expectedCell.fail(testResults,result,this);
		} catch (PatternSyntaxException e) {
			throw new FitLibraryException(e.getMessage());
		}
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void contains(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("contains");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
		boolean matches = result.contains(expectedCell.text(this));
		if (matches)
			expectedCell.pass(testResults);
		else
			expectedCell.failWithStringEquals(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void eventuallyContains(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("contains");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = "";
		long start = System.currentTimeMillis();
		int becomesTimeout = getTimeout(BECOMES_TIMEOUT);
		while (System.currentTimeMillis() - start < becomesTimeout ) {
			result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
			boolean matches = result.contains(expectedCell.text(this));
			if (matches) {
				expectedCell.pass(testResults);
				return;
			}
		}
		expectedCell.failWithStringEquals(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void doesNotContain(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("doesNoContain");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = target.invokeForSpecial(row.rowTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
		boolean matches = result.contains(expectedCell.text(this));
		if (!matches)
			expectedCell.pass(testResults);
		else
			expectedCell.fail(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string becomes equals
	 *  to the given value within the timeout period.
	 */
	public void becomes(TestResults testResults, final Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseMatches");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		Row actionPartOfRow = row.rowTo(1,row.size()-2);
		long start = System.currentTimeMillis();
		int becomesTimeout = getTimeout(BECOMES_TIMEOUT);
		while (System.currentTimeMillis() - start < becomesTimeout ) {
			Object result = target.invokeForSpecial(actionPartOfRow, testResults, false,operatorCell(row));
			if (target.getResultParser().matches(expectedCell, result, testResults))
				break;
			try {
				Thread.sleep(Math.min(100,becomesTimeout/10));
			} catch (Exception e) {
				//
			}
		}
		target.invokeAndCheckForSpecial(actionPartOfRow,expectedCell,testResults,row,operatorCell(row));
	}

	//------------------- Prefix Special Actions:
	/** Check that the result of the action in the rest of the row matches
	 *  the expected value in the last cell of the row.
	 */
	public TwoStageSpecial check(final IRow row) throws Exception {
		return prefixSpecialAction.check(row);
	}
	public TwoStageSpecial reject(final IRow row) throws Exception {
		return not(row);
	}
    /** Same as reject()
     * @param testResults 
     */
	public TwoStageSpecial not(final IRow row) throws Exception {
		return prefixSpecialAction.not(row,NotSyle.PASSES_ON_EXCEPION);
	}
	public TwoStageSpecial notTrue(final IRow row) throws Exception {
		return prefixSpecialAction.not(row,NotSyle.ERROR_ON_EXCEPION);
	}
	/** Add a cell containing the result of the action in the rest of the row.
     *  HTML is not altered, so it can be viewed directly.
     */
	public TwoStageSpecial show(final IRow row) throws Exception {
		return prefixSpecialAction.show(row);
	}
	/** Adds the result of the action in the rest of the row to a folding area after the table.
     */
	public TwoStageSpecial showAfter(final IRow row) throws Exception {
		return prefixSpecialAction.showAfter(row);
	}
	/** Add a cell containing the result of the action in the rest of the row.
     *  HTML is escaped so that the underlying layout text can be viewed.
     */
	public TwoStageSpecial showEscaped(final IRow row) throws Exception {
		return prefixSpecialAction.showEscaped(row);
	}
	/** Log result to a file
	 */
	public TwoStageSpecial log(final IRow row) throws Exception {
		return prefixSpecialAction.log(row);
	}
	/** Set the dynamic variable name to the result of the action, or to the string if there's no action.
	 */
	public TwoStageSpecial set(final IRow row) throws Exception {
		return prefixSpecialAction.set(row);
	}
	/** Set the named FIT symbol to the result of the action, or to the string if there's no action.
	 */
	public TwoStageSpecial setSymbolNamed(final IRow row) throws Exception {
		return prefixSpecialAction.setSymbolNamed(row);
	}
	/** Add a cell containing the result of the rest of the row,
     *  shown as a Dot graphic.
	 * @param testResults 
     */
	public void showDot(Row row, TestResults testResults) throws Exception {
		Parser adapter = new GraphicParser(new NonGenericTyped(ObjectDotGraphic.class));
		try {
		    Object result = callMethodInRow(row,testResults, true,row.cell(0));
		    row.addCell(adapter.show(new ObjectDotGraphic(result)));
		} catch (IgnoredException e) { // No result, so ignore
		}
	}
	/** Checks that the action in the rest of the row succeeds.
     *  o If a boolean is returned, it must be true.
     *  o For other result types, no exception should be thrown.
     *  It's no longer needed, because the same result can now be achieved with a boolean method.
	 * @param testResults 
     */
	public TwoStageSpecial ensure(final IRow row) throws Exception {
		return prefixSpecialAction.ensure(row);
	}

	/** The rest of the row is ignored. 
     */
	@SuppressWarnings("unused")
	public void note(Row row, TestResults testResults) throws Exception {
		//		Nothing to do
	}
	/** To allow for example storytests in user guide to pass overall, even if they have failures within them. */
	public void expectedTestResults(Row row, TestResults testResults) throws Exception {
		if (testResults.matches(row.text(1,this),row.text(3,this),row.text(5,this),row.text(7,this))) {
			testResults.clear();
			row.cell(0).pass(testResults);
		} else {
			String results = testResults.toString();
			testResults.clear();
			row.cell(0).fail(testResults,results,this);
		}
	}
	public Object oo(final Row row, TestResults testResults) throws Exception {
		if (row.size() < 3)
			throw new MissingCellsException("DoTraverseOO");
		String object = row.text(1,this);
		Object className = getDynamicVariable(object+".class");
		if (className == null || "".equals(className))
			className = object; // then use the object name as a class name
		Row macroRow = row.rowFrom(2);
		return new DefinedActionCaller(object,className.toString(),macroRow,this).run(row, testResults);
	}
	public void runPlain(final Row row, TestResults testResults) throws Exception {
		PlainText plainText = new PlainText(row,testResults,this);
		plainText.analyse();
	}
	/** Don't mind that the action succeeds or not, just as long as it's not a FitLibraryException (such as action unknown) 
     */
	public void optionally(Row row, TestResults testResults) throws Exception {
		try {
		    Object result = callMethodInRow(row,testResults, true,row.cell(0));
		    if (result instanceof Boolean && !((Boolean)result).booleanValue()) {
		    	row.addCell("false").shown();
		    	CallManager.addShow(row);
		    }
		} catch (FitLibraryException e) {
			row.cell(0).error(testResults,e);
		} catch (Exception e) {
			row.addCell(PlugBoard.exceptionHandling.exceptionMessage(e)).shown();
			CallManager.addShow(row);
		}
		row.cell(0).pass(testResults);
	}
	public int getTimeout(String name) {
		return getRuntimeContext().getTimeout(name,1000);
	}
	protected void putTimeout(String name, int timeout) {
		getRuntimeContext().putTimeout(name,timeout);
	}
	@Override
	public void setFitVariable(String variableName, Object result) {
		Fixture.setSymbol(variableName, result);
	}
	public Object getSymbolNamed(String fitSymbolName) {
		return Fixture.getSymbol(fitSymbolName);
	}
	@Override
	public void show(IRow row, String text) {
		row.addCell(text).shown();
		CallManager.addShow(row);
	}
	@Override
	public FitHandler fitHandler() {
		return getFitHandler();
	}
}
