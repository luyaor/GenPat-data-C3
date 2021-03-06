/*
a * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fitlibrary.DefineAction;
import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.definedAction.DefineActionsOnPage;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.global.PlugBoard;
import fitlibrary.parser.Parser;
import fitlibrary.parser.graphic.GraphicParser;
import fitlibrary.parser.graphic.ObjectDotGraphic;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.traverse.FitHandler;
import fitlibrary.traverse.function.CalculateTraverse;
import fitlibrary.traverse.function.ConstraintTraverse;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.traverse.workflow.special.PrefixSpecialAction;
import fitlibrary.traverse.workflow.special.SpecialActionContext;
import fitlibrary.traverse.workflow.special.PrefixSpecialAction.NotSyle;
import fitlibrary.typed.NonGenericTyped;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.xref.CrossReferenceFixture;

public class DoTraverse extends DoTraverseInterpreter implements SpecialActionContext, FlowEvaluator{
	private final PrefixSpecialAction prefixSpecialAction = new PrefixSpecialAction(this);
	public static final String BECOMES_TIMEOUT = "becomes";
	// Methods that can be called within DoTraverse.
	// Each element is of the form "methodName/argCount"
	private final static String[] methodsThatAreVisibleAsActions = {
		"calculate/0", "start/1", "constraint/0", "failingConstraint/0",
		"addAs/2"
	}; // The rest of the methods that used to be here are now in GlobalScope
	//------------------- Methods that are visible as actions (the rest are hidden):
	public List<String> methodsThatAreVisible() {
		return Arrays.asList(methodsThatAreVisibleAsActions);
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

	//--- FIXTURE WRAPPERS FOR THIS (and so not available in GlobalScope):
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

	//------ THE FOLLOWING ARE HERE SO THAT THEY'RE STILL ACCESSIBLE FROM A SUBCLASS:
	
	//--- BECOMES, ETC TIMEOUTS:
	public void becomesTimeout(int timeout) {
		global().becomesTimeout(timeout);
	}
	public int becomesTimeout() {
		return global().becomesTimeout();
	}
	public int getTimeout(String name) {
		return global().getTimeout(name);
	}
	public void putTimeout(String name, int timeout) {
		global().putTimeout(name,timeout);
	}
	//--- STOP ON ERROR AND ABANDON:
	/** When (stopOnError), don't continue interpreting a table if there's been a problem */
	public void setStopOnError(boolean stopOnError) {
		global().setStopOnError(stopOnError);
	}
	public void abandonStorytest() {
		global().abandonStorytest();
	}
	//--- DYNAMIC VARIABLES:
	public boolean addDynamicVariablesFromFile(String fileName) {
		return global().addDynamicVariablesFromFile(fileName);
	}
	public void addDynamicVariablesFromUnicodeFile(String fileName) throws IOException {
		global().addDynamicVariablesFromUnicodeFile(fileName);
	}
	public boolean clearDynamicVariables() {
		return global().clearDynamicVariables();
	}
	public boolean setSystemPropertyTo(String property, String value) {
		return global().setSystemPropertyTo(property, value);
	}
	public void setFitVariable(String variableName, Object result) {
		global().setFitVariable(variableName, result);
	}
	public Object getSymbolNamed(String fitSymbolName) {
		return global().getSymbolNamed(fitSymbolName);
	}
	//--- SLEEP & STOPWATCH:
	public boolean sleepFor(int milliseconds) {
		return global().sleepFor(milliseconds);
	}
	public void startStopWatch() {
		global().startStopWatch();
	}
	public long stopWatch() {
		return global().stopWatch();
	}
	//--- FIXTURE SELECTION
	public SetVariableTraverse setVariables() {
		return global().setVariables();
	}
	public DoTraverse file(String fileName) {
		return global().file(fileName);
	}
	public CrossReferenceFixture xref(String suiteName) {
		return global().xref(suiteName);
	}
	//--- DEFINED ACTIONS
	public DefineAction defineAction(String wikiClassName) {
		return global().defineAction(wikiClassName);
	}
	public DefineAction defineAction() {
		return global().defineAction();
	}
	public DefineActionsOnPageSlowly defineActionsSlowlyAt(String pageName) {
		return global().defineActionsSlowlyAt(pageName);
	}
	public DefineActionsOnPage defineActionsAt(String pageName) {
		return global().defineActionsAt(pageName);
	}
	public DefineActionsOnPage defineActionsAtFrom(String pageName, String rootLocation) {
		return global().defineActionsAtFrom(pageName,rootLocation);
	}
	public void clearDefinedActions() {
		global().clearDefinedActions();
	}
	public boolean toExpandDefinedActions() {
		return global().toExpandDefinedActions();
	}
	public void setExpandDefinedActions(boolean expandDefinedActions) {
		global().setExpandDefinedActions(expandDefinedActions);
	}
	//--- RANDOM, TO, GET, FILE, HARVEST
	public RandomSelectTraverse selectRandomly(String var) {
		return global().selectRandomly(var);
	}
	public boolean harvestUsingPatternFrom(String[] vars, String pattern, String text) {
		return global().harvestUsingPatternFrom(vars, pattern, text);
	}
	//--- FILE LOGGING
	public void recordToFile(String fileName) {
		global().recordToFile(fileName);
	}
	public void startLogging(String fileName) {
		global().startLogging(fileName);
	}
	public void logMessage(String s) {
		global().logMessage(s);
	}
	//--- SHOW
	@Override
	public void show(Row row, String text) {
		global().show(row, text);
	}

	//------------------- Postfix Special Actions:
	/** Check that the result of the action in the first part of the row is the same as
	 *  the expected value in the last cell of the row.
	 */
	public void is(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		target.invokeAndCheckForSpecial(row.fromTo(1,row.size()-2),expectedCell,testResults,row,operatorCell(row));
	}
	public void equals(TestResults testResults, Row row) throws Exception {
		is(testResults,row);
	}
	/** Check that the result of the action in the first part of the row is not the same as
	 *  the expected value in the last cell of the row.
	 */
	public void isNot(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		Cell specialCell = operatorCell(row);
		Cell expectedCell = row.last();
		try {
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Object result = target.invoke(row.fromTo(1,row.size()-2),testResults,true);
			target.notResult(expectedCell, result, testResults);
        } catch (IgnoredException e) {
            //
        } catch (InvocationTargetException e) {
        	Throwable embedded = e.getTargetException();
        	if (embedded instanceof FitLibraryShowException) {
        		specialCell.error(testResults);
        		row.error(testResults, e);
        	} else
        		expectedCell.exceptionExpected(false, e, testResults);
        } catch (Exception e) {
        	expectedCell.exceptionExpected(false, e, testResults);
        }
	}
	/** Check that the result of the action in the first part of the row is less than
	 *  the expected value in the last cell of the row.
	 */
	public void lessThan(TestResults testResults, Row row) throws Exception {
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
	public void lessThanEquals(TestResults testResults, Row row) throws Exception {
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
	public void greaterThan(TestResults testResults, Row row) throws Exception {
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
	public void greaterThanEquals(TestResults testResults, Row row) throws Exception {
		Comparison compare = new Comparison() {
			@SuppressWarnings("unchecked")
			public boolean compares(Comparable actual, Comparable expected) {
				return actual.compareTo(expected) >= 0;
			}
		};
		comparison(testResults, row, compare);
	}
	@SuppressWarnings("unchecked")
	private void comparison(TestResults testResults, Row row, Comparison compare) {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseIs");
		Cell specialCell = operatorCell(row);
		Cell expectedCell = row.last();
		try {
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Object result = target.invoke(row.fromTo(1,row.size()-2),testResults,true);
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
        		expectedCell.exceptionExpected(false, e, testResults);
        } catch (Exception e) {
        	expectedCell.exceptionExpected(false, e, testResults);
        }
	}
	public interface Comparison {
		@SuppressWarnings("unchecked")
		boolean compares(Comparable actual, Comparable expected);
	}
	private Cell operatorCell(Row row) {
		return row.at(row.size()-2);
	}
	/** Check that the result of the action in the first part of the row, as a string, matches
	 *  the regular expression in the last cell of the row.
	 */
	public void matches(TestResults testResults, Row row) throws Exception {
		try
		{
			int less = 3;
			if (row.size() < less)
				throw new MissingCellsException("DoTraverseMatches");
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Cell expectedCell = row.last();
			String result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
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
	public void eventuallyMatches(TestResults testResults, Row row) throws Exception {
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
			result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
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
	public void doesNotMatch(TestResults testResults, Row row) throws Exception {
		try
		{
			int less = 3;
			if (row.size() < less)
				throw new MissingCellsException("DoTraverseMatches");
			ICalledMethodTarget target = findMethodFromRow222(row,0,less);
			Cell expectedCell = row.last();
			String result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
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
	public void contains(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("contains");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
		boolean matches = result.contains(expectedCell.text(this));
		if (matches)
			expectedCell.pass(testResults);
		else
			expectedCell.failWithStringEquals(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void eventuallyContains(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("contains");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = "";
		long start = System.currentTimeMillis();
		int becomesTimeout = getTimeout(BECOMES_TIMEOUT);
		while (System.currentTimeMillis() - start < becomesTimeout ) {
			result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
			boolean matches = result.contains(expectedCell.text(this));
			if (matches) {
				expectedCell.pass(testResults);
				return;
			}
		}
		expectedCell.failWithStringEquals(testResults,result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string, contains
	 *  the string in the last cell of the row.
	 */
	public void doesNotContain(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("doesNoContain");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		String result = target.invokeForSpecial(row.fromTo(1,row.size()-2),testResults,false,operatorCell(row)).toString();
		boolean matches = result.contains(expectedCell.text(this));
		if (!matches)
			expectedCell.pass(testResults);
		else
			expectedCell.fail(testResults, result,this);
	}
	/** Check that the result of the action in the first part of the row, as a string becomes equals
	 *  to the given value within the timeout period.
	 */
	public void becomes(TestResults testResults, Row row) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseMatches");
		ICalledMethodTarget target = findMethodFromRow222(row,0,less);
		Cell expectedCell = row.last();
		Row actionPartOfRow = row.fromTo(1,row.size()-2);
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
	public TwoStageSpecial check(Row row) throws Exception {
		return prefixSpecialAction.check(row);
	}
	public TwoStageSpecial reject(Row row) throws Exception {
		return not(row);
	}
    /** Same as reject()
     * @param suiteTestResults 
     */
	public TwoStageSpecial not(Row row) throws Exception {
		return prefixSpecialAction.not(row,NotSyle.PASSES_ON_EXCEPION);
	}
	public TwoStageSpecial notTrue(Row row) throws Exception {
		return prefixSpecialAction.not(row,NotSyle.ERROR_ON_EXCEPION);
	}
	/** Add a cell containing the result of the action in the rest of the row.
     *  HTML is not altered, so it can be viewed directly.
     */
	public TwoStageSpecial show(Row row) throws Exception {
		return prefixSpecialAction.show(row);
	}
	/** Adds the result of the action in the rest of the row to a folding area after the table.
     */
	public TwoStageSpecial showAfter(Row row) throws Exception {
		return prefixSpecialAction.showAfter(row);
	}
	/** Adds the result of the action in the rest of the row to a folding area after the table.
     */
	public TwoStageSpecial showAfterAs(Row row) throws Exception {
		return prefixSpecialAction.showAfterAs(row);
	}
	/** Add a cell containing the result of the action in the rest of the row.
     *  HTML is escaped so that the underlying layout text can be viewed.
     */
	public TwoStageSpecial showEscaped(Row row) throws Exception {
		return prefixSpecialAction.showEscaped(row);
	}
	/** Log result to a file
	 */
	public TwoStageSpecial log(Row row) throws Exception {
		return prefixSpecialAction.log(row);
	}
	/** Set the dynamic variable name to the result of the action, or to the string if there's no action.
	 */
	public TwoStageSpecial set(Row row) throws Exception {
		return prefixSpecialAction.set(row);
	}
	/** Set the named FIT symbol to the result of the action, or to the string if there's no action.
	 */
	public TwoStageSpecial setSymbolNamed(Row row) throws Exception {
		return prefixSpecialAction.setSymbolNamed(row);
	}
	/** Add a cell containing the result of the rest of the row,
     *  shown as a Dot graphic.
	 * @param testResults 
     */
	public void showDot(Row row, TestResults testResults) throws Exception {
		Parser adapter = new GraphicParser(new NonGenericTyped(ObjectDotGraphic.class));
		try {
		    Object result = callMethodInRow(row,testResults, true,row.at(0));
		    row.addCell(adapter.show(new ObjectDotGraphic(result)));
		} catch (IgnoredException e) { // No result, so ignore
		}
	}
	/** Checks that the action in the rest of the row succeeds.
     *  o If a boolean is returned, it must be true.
     *  o For other result types, no exception should be thrown.
     *  It's no longer needed, because the same result can now be achieved with a boolean method.
	 * @param suiteTestResults 
     */
	public TwoStageSpecial ensure(Row row) throws Exception {
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
			row.at(0).pass(testResults);
		} else {
			String results = testResults.toString();
			testResults.clear();
			row.at(0).fail(testResults,results,this);
		}
	}
	public Object oo(Row row, TestResults testResults) throws Exception {
		if (row.size() < 3)
			throw new MissingCellsException("DoTraverseOO");
		String object = row.text(1,this);
		Object className = getDynamicVariable(object+".class");
		if (className == null || "".equals(className))
			className = object; // then use the object name as a class name
		Row macroRow = row.fromAt(2);
		TypedObject typedObject = new DefinedActionCaller(object,className.toString(),macroRow,getRuntimeContext()).run(row, testResults);
		return typedObject.getSubject();
	}
	/** Don't mind that the action succeeds or not, just as long as it's not a FitLibraryException (such as action unknown) 
     */
	public void optionally(Row row, TestResults testResults) throws Exception {
		try {
		    Object result = callMethodInRow(row,testResults, true,row.at(0));
		    if (result instanceof Boolean && !((Boolean)result).booleanValue()) {
		    	row.addCell("false").shown();
		    	getRuntimeContext().getDefinedActionCallManager().addShow(row);
		    }
		} catch (FitLibraryException e) {
			row.at(0).error(testResults,e);
		} catch (Exception e) {
			row.addCell(PlugBoard.exceptionHandling.exceptionMessage(e)).shown();
			getRuntimeContext().getDefinedActionCallManager().addShow(row);
		}
		row.at(0).pass(testResults);
	}
	/*
	 * |''add named''|name|...action or fixture|
	 */
	public void addNamed(Row row, TestResults testResults) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("addNamed");
		TypedObject typedObject = interpretRow(row.fromAt(2), testResults);
		getRuntimeContext().getTableEvaluator().addNamedObject(row.text(1,this),typedObject,row,testResults);
	}
	/*
	 * |''add global''|...action or fixture|
	 */
	public void addGlobal(Row row, TestResults testResults) throws Exception {
		int less = 2;
		if (row.size() < less)
			throw new MissingCellsException("addGlobal");
		TypedObject typedObject = interpretRow(row.fromAt(1), testResults);
		if (typedObject.classType() == DoTraverse.class)
			typedObject = typedObject.getTypedSystemUnderTest();
		typedObject.injectRuntime(getRuntimeContext());
		getRuntimeContext().getScope().addGlobal(typedObject);
		row.at(0).pass(testResults);
	}
	@Override
	public FitHandler fitHandler() {
		return getFitHandler();
	}
}
