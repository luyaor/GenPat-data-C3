/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import fit.FixtureBridge;
import fit.Parse;
import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.runtime.RuntimeContext;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

/**
 * An abstract superclass of all the flow-style fixtures.
 * It manages differences between the Fit and FitNesse versions of the FitLibrary,
 * by delegating to an object of class fitlibrary.FitNesseDifference.
 * This class is different in the builds between the two versions, but is
 * needed here to avoid compiletime conflicts. It also has to be created 
 * reflectively, because we can't mention its name here, except in a String.
 */
public abstract class FitLibraryFixture extends FixtureBridge implements Evaluator {
	private Traverse traverse;
	private TypedObject typedObjectUnderTest = Traverse.asTypedObject(null);

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
	 *  If an action can't be satisfied by the fixture, the systemUnderTest
	 *  is tried instead. Thus the fixture is an adapter with methods just
	 *  when they're needed.
	 */
	public void setSystemUnderTest(Object sut) {
		typedObjectUnderTest = Traverse.asTypedObject(sut);
	}
	public Object getSystemUnderTest() {
		return typedObjectUnderTest.getSubject();
	}
	public TypedObject getTypedSystemUnderTest() {
		return typedObjectUnderTest;
	}
	public void setOuterContext(Evaluator outerContext) {
		traverse().setOuterContext(outerContext);
	}
	public Evaluator getNextOuterContext() {
		return traverse().getNextOuterContext();
	}
	public Object getOutermostContext() {
		return traverse().getOutermostContext();
	}
    public final Traverse traverse() {
    	return traverse;
    }
    protected void setTraverse(Traverse traverse) {
    	this.traverse = traverse;
    }
    @Override
    public void doTable(Parse parseTable) {
    	Table table = new Table(parseTable);
    	TestResults testResults = createTestResults();
    	try {
    		setUp(table, testResults);
    		interpretAfterFirstRow(table, testResults);
    	} catch (Exception e) {
    		table.error(testResults,e);
    	} finally {
    		tearDown(table, testResults);
    	}
    }
    public void doWithin(Table table, Evaluator evaluator, TestResults testResults) {
        setOuterContext(evaluator);
        counts = testResults.getCounts();
        interpretAfterFirstRow(table,testResults);
    }
	public boolean doEmbeddedTablePasses(Table table, Evaluator evaluator, TestResults testResults) {
		return traverse().doesInnerTablePass(table,evaluator,testResults);
	}
	public TestResults createTestResults() {
		return TestResults.create(counts);
	}
    public Object interpretAfterFirstRow(Table table, TestResults testResults) {
    	traverse().setUp(table,testResults);
    	Object result = traverse().interpretAfterFirstRow(table,testResults);
    	traverse().tearDown(table,testResults);
		return result;
    }
    public RuntimeContext runtime() {
		return traverse().runtime();
	}
    public DynamicVariables getDynamicVariables() {
    	return runtime().dynamicVariables();
    }
	public void setRuntimeContext(RuntimeContext propertyValues) {
		traverse().setRuntimeContext(propertyValues);
	}
	public void setDynamicVariable(String key, Object value) {
		traverse().setDynamicVariable(key, value);
	}
	public void setUp() throws Exception {
		traverse().setUp();
	}
	public void setUp(Table firstTable, TestResults testResults) {
		traverse().setUp(firstTable, testResults);
	}
	public void tearDown(Table firstTable, TestResults testResults) {
		traverse().tearDown(firstTable, testResults);
	}
	public void tearDown() throws Exception {
		traverse().tearDown();
	}
}
