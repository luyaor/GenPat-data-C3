/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.Closure;
import fitlibrary.differences.DifferenceInterface;
import fitlibrary.differences.FitNesseDifference;
import fitlibrary.differences.LocalFile;
import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.exception.CycleException;
import fitlibrary.global.PlugBoard;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.runtime.RuntimeContext;
import fitlibrary.table.Table;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedFactory;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;
import fitlibraryGeneric.typed.GenericTypedFactory;
import fitlibraryGeneric.typed.GenericTypedObject;

public abstract class Traverse implements Evaluator {
	protected static DifferenceInterface FITNESSE_DIFFERENCES = new FitNesseDifference();
	protected static AlienTraverseHandler ALIEN_TRAVERSE_HANDLER = new AlienTraverseHandler();
	public static final String FITNESSE_URL_KEY = "fitNesse.url";
	private final static TypedFactory factory = new GenericTypedFactory();
	private TypedObject typedObjectUnderTest = new GenericTypedObject(null);
	private Evaluator outerContext = null;
	private boolean setUpAlreadyCalled = false;
	private boolean tearDownAlreadyCalled = false;
	protected boolean canTearDown = true;
	protected RuntimeContext runtimeContext = new RuntimeContext();

	public Traverse() {
    	// No SUT
	}
    public Traverse(Object sut) {
    	setSystemUnderTest(sut);
	}
    public Traverse(TypedObject typedObjectUnderTest) {
    	this.typedObjectUnderTest = typedObjectUnderTest;
	}
	/** Registers a delegate, a class that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class<?> type, Class<?> parseDelegate) {
		ParseDelegation.registerParseDelegate(type,parseDelegate);
	}
	/** Registers a delegate object that will
	 * handle parsing of other types of values.
	 */
	protected void registerParseDelegate(Class<?> type, Object parseDelegate) {
		ParseDelegation.registerParseDelegate(type,parseDelegate);
	}
	/** Registers a delegate object that will
	 * handle parsing of the given type and any subtype.
	 */
	protected void registerSuperParseDelegate(Class<?> type, Object superParseDelegate) {
		ParseDelegation.registerSuperParseDelegate(type,superParseDelegate);
	}
	/** Set the systemUnderTest. 
	 *  If an action can't be satisfied by the Traverse, the systemUnderTest
	 *  is tried instead. Thus the Traverse is an adapter with methods just
	 *  when they're needed.
	 */
	public void setSystemUnderTest(Object sut) {
		if (cycleSUT(this,sut))
			throw new CycleException("systemUnderTest",this,sut);
		this.typedObjectUnderTest = asTypedObject(sut);
	}
	private boolean cycleSUT(DomainAdapter domainAdapter, Object sut) {
		if (domainAdapter == sut)
			return true;
		if (sut instanceof DomainAdapter)
			return cycleSUT(domainAdapter,((DomainAdapter)sut).getSystemUnderTest());
		return false;
	}
	public Object getSystemUnderTest() {
		if (typedObjectUnderTest == null)
			return null;
		return typedObjectUnderTest.getSubject();
	}
	public TypedObject getTypedSystemUnderTest() {
		return typedObjectUnderTest;
	}
	public void setTypedSystemUnderTest(TypedObject typedObjectUnderTest) {
		this.typedObjectUnderTest = typedObjectUnderTest;
	}
	public void setOuterContext(Evaluator outerContext) {
		if (this.outerContext == outerContext || this == outerContext)
			return;
		if (cycleOC(this,outerContext))
			throw new CycleException("outerContext",this,outerContext);
		this.outerContext = outerContext;
	}
	private static boolean cycleOC(DomainAdapter domainAdapter, Evaluator oc) {
		if (domainAdapter == oc)
			return true;
		if (oc != null)
			return cycleOC(domainAdapter,oc.getNextOuterContext());
		return false;
	}
	public Evaluator getNextOuterContext() {
		return outerContext;
	}
	public Object getOutermostContext() {
		Evaluator context = getNextOuterContext();
		if (context == null)
			return this;
		return context.getOutermostContext();
	}
    public static void setDifferenceStrategy(DifferenceInterface difference) {
		FITNESSE_DIFFERENCES = difference;
	}
    public static LocalFile getLocalFile(String localFileName) {
    	return FITNESSE_DIFFERENCES.getLocalFile(localFileName);
    }
    public static LocalFile getGlobalFile(String localFileName) {
    	return FITNESSE_DIFFERENCES.getGlobalFile(localFileName);
    }
    public static String htmlLink(File file) {
    	return FITNESSE_DIFFERENCES.getGlobalFile(file).htmlLink();
    }
	public static void setContext(File reportDiry) {
        FITNESSE_DIFFERENCES.setContext(reportDiry);
    }
	public static String escapeHtml(String s) {
		if (s == null)
			return "";
		return s.replaceAll("<","&lt;").replaceAll(">","&gt;");
	}
	protected String camelCase(String suppliedName) {
		return ExtendedCamelCase.camel(suppliedName);
	}
	public void theSetUpTearDownAlreadyHandled() {
		setUpAlreadyCalled = true;
		tearDownAlreadyCalled = true;
	}
	public void setUp(Table table, TestResults testResults) {
		try {
			setUp();
		} catch (Exception e) {
			table.error(testResults,e);
		}
	}
	public void setUp() throws Exception {
		if (setUpAlreadyCalled)
			return;
		setUpAlreadyCalled = true;
		CalledMethodTarget methodTarget = asTypedObject().optionallyFindMethodOnTypedObject("setUp",0,this,false);
		if (methodTarget == null)
			return;
		methodTarget.invoke();
	}
	public void tearDown(Table table, TestResults testResults) {
		try {
			if (!testResults.inSuiteFixtureSoDoNotTearDown())
				tearDown();
		} catch (Exception e) {
			table.error(testResults,e);
		}
	}
	public void tearDown() throws Exception {
		if (!canTearDown || tearDownAlreadyCalled)
			return;
		tearDownAlreadyCalled = true;
		CalledMethodTarget methodTarget = asTypedObject().optionallyFindMethodOnTypedObject("tearDown",0,this,false);
		if (methodTarget == null)
			return;
		methodTarget.invoke();
	}
    public void interpretWithinContext(Table table, Evaluator evaluator, TestResults testResults) {
        setOuterContext(evaluator);
        interpretAfterFirstRow(table,testResults);
    }
    public void interpretInnerTable(Table table, Evaluator evaluator, TestResults testResults) {
		setOuterContext(evaluator);
		interpretAfterFirstRow(table.withDummyFirstRow(),testResults);
	}
	public boolean doesInnerTablePass(Table table, Evaluator evaluator, TestResults testResults) {
		TestResults innerResults = new TestResults();
		interpretInnerTable(table,evaluator,innerResults);
        testResults.add(innerResults);
		return innerResults.passed();
	}
	public boolean doesTablePass(Table table, Evaluator evaluator, TestResults testResults) {
		setOuterContext(evaluator);
		TestResults innerResults = new TestResults();
		interpretAfterFirstRow(table,innerResults);
        testResults.add(innerResults);
		return innerResults.passed();
	}
	public static AlienTraverseHandler getAlienTraverseHandler() {
		return ALIEN_TRAVERSE_HANDLER;
	}
	public static void setAlienTraverseHandler(AlienTraverseHandler handler) {
		ALIEN_TRAVERSE_HANDLER = handler;
	}
	public static Typed asTyped(Class<?> type) {
		return factory.asTyped(type);
	}
	public static Typed asTyped(Object object) {
		return factory.asTyped(object.getClass());
	}
	public static Typed asTyped(Method method) {
		return factory.asTyped(method);
	}
	protected TypedObject asTypedObject() {
		return factory.asTypedObject(this);
	}
	public static TypedObject asTypedObject(Object sut) {
		if (sut instanceof TypedObject)
			return (TypedObject) sut;
		return factory.asTypedObject(sut);
	}
	public void callStartCreatingObjectMethod(TypedObject object) throws IllegalAccessException, InvocationTargetException {
		if (object != null)
			callCreatingMethod("startCreatingObject", object.getSubject());
	}
	public void callStartCreatingObjectMethod(Object element) throws IllegalAccessException, InvocationTargetException {
		callCreatingMethod("startCreatingObject", element);
	}
	public void callEndCreatingObjectMethod(TypedObject object) throws IllegalAccessException, InvocationTargetException {
		if (object != null)
			callCreatingMethod("endCreatingObject", object.getSubject());
	}
	public void callEndCreatingObjectMethod(Object element) throws IllegalAccessException, InvocationTargetException {
		callCreatingMethod("endCreatingObject", element);
	}
    public RuntimeContext runtime() {
		return runtimeContext;
	}
    public DynamicVariables getDynamicVariables() {
    	return runtime().dynamicVariables();
    }
	public void setRuntimeContext(RuntimeContext globalRuntimeContext) {
		this.runtimeContext = globalRuntimeContext;
	}
	public void setDynamicVariable(String key, Object value) {
		getDynamicVariables().put(key, value);
	}
	public Object getDynamicVariable(String key) {
		return getDynamicVariables().get(key);
	}
	protected String fitNesseUrl() {
		return getDynamicVariable(FITNESSE_URL_KEY).toString();
	}
	private void callCreatingMethod(String creatingMethodName, Object element) throws IllegalAccessException, InvocationTargetException {
		Closure startCreatingMethod = PlugBoard.lookupTarget.findFixturingMethod(this, creatingMethodName, (new Class[]{ Object.class}));
        if (startCreatingMethod != null)
        	startCreatingMethod.invoke(new Object[] { element });
	}
	public abstract Object interpretAfterFirstRow(Table table, TestResults testResults);
}
