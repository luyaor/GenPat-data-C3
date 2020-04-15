/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */

package fitlibrary.suite;

import java.lang.reflect.Method;
import java.util.Stack;

import fit.FitServerBridge;
import fit.Fixture;
import fitlibrary.DoFixture;
import fitlibrary.DomainFixture;
import fitlibrary.SetUpFixture;
import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.object.DomainCheckTraverse;
import fitlibrary.object.DomainFixtured;
import fitlibrary.object.DomainInjectionTraverse;
import fitlibrary.object.DomainTraverser;
import fitlibrary.runtime.RuntimeContextImplementation;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SetUpTearDownManager.MethodCaller;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.RuntimeContextual;
import fitlibrary.traverse.SwitchingEvaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

/**
 * This integrates various pieces of functionality:
 * o Ordinary Do flow
 * o DomainFixture flow, with switching for 3 phases: inject, do, check.
 * o SuiteFixture
 */
public class DoFlow implements DomainTraverser, SwitchingEvaluator {
	public static final boolean IS_ACTIVE = true;
	private DomainInjectionTraverse domainInject = null;
	private DomainCheckTraverse domainCheck = null;
	private SwitchingEvaluator current = this;
	private Stack<TypedObject> tableStack = new Stack<TypedObject>();
	private SuiteEvaluator suiteFixture = null;
	private final FlowEvaluator flowEvaluator;
	private RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
	private final SetUpTearDownManager setUpTearDownManager = new SetUpTearDownManager();

	public DoFlow(FlowEvaluator flowEvaluator) {
		this.flowEvaluator = flowEvaluator;
		flowEvaluator.setRuntimeContext(runtimeContext);
		runtimeContext.setDynamicVariable(Traverse.FITNESSE_URL_KEY,FitServerBridge.FITNESSE_URL);
	}
	public void runStorytest(Tables tables, TableListener tableListener) {
		reset();
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.table(t);
			if (domainCheck != null)
				handleDomainPhases(table);
			current.runTable(table, tableListener);
			popLocalSut(table,tableListener);
			tableListener.tableFinished(table);
		}
		tearDownFlowObject(tables, tableListener);
		tableListener.storytestFinished();
		// Could return whether we hit a suite fixture...
	}
	private void reset() {
		tableStack.clear();
		current = this;
		domainInject = null;
		domainCheck = null;
		flowEvaluator.setSystemUnderTest(suiteFixture); // May be null
		if (suiteFixture != null)
			runtimeContext = suiteFixture.getCopyOfRuntimeContext();
	}
	private void handleDomainPhases(Table table) {
        int phaseBreaks = table.phaseBoundaryCount();
		if (phaseBreaks > 0) {
        	for (int i = 0; i < phaseBreaks; i++) {
        		if (current == domainInject)
        			setCurrentAction();
        		else if (current == this)
        			setCurrentCheck();
        	}
        }
	}
	public void runTable(Table table, TableListener tableListener) {
		TestResults testResults = tableListener.getTestResults();
		for (int rowNo = 0; rowNo < table.size(); rowNo++) {
			Row row = table.row(rowNo);
			if (testResults.isAbandoned()) {
				if (suiteFixture == null)
					row.ignore(testResults);
			} else if (domainCheck != null && row.size() == 1 && row.text(0, flowEvaluator).equals("checks")) {
				setCurrentCheck(); // Remove this hack later
			} else
				try {
//					System.out.println("DoFlow row "+row);
					Object result = flowEvaluator.interpretRow(row,testResults,null);
//					System.out.println("DoFlow got "+result);
					if (result == null) {
						// Can't do anything useful with a null
					} else if (result.getClass() == Fixture.class) {
						// Ignore it, as it does nothing.
					} else if (result.getClass() == DoFixture.class || result.getClass() == DoTraverse.class) {
						DoEvaluator doEvaluator = (DoEvaluator)result;
						// Unwrap an auto-wrap, keeping the type information, unless it has an outer context
						if (doEvaluator.getNextOuterContext() != null)
							aDoEvaluator(doEvaluator,table,testResults);
						else if (doEvaluator.getSystemUnderTest() != null)
							pushSut(doEvaluator.getTypedSystemUnderTest(),table,testResults);
					} else if (result instanceof DomainFixtured || result instanceof DomainFixture) {
						pushSut(result,table,testResults);
						domainInject = new DomainInjectionTraverse(this);
						domainInject.setSystemUnderTest(result);
						setRuntimeContext(domainInject);
						domainCheck = new DomainCheckTraverse(this);
						domainCheck.setSystemUnderTest(result);
						setRuntimeContext(domainCheck);
						current = domainInject;
					} else if (result instanceof SuiteEvaluator) {
						suiteFixture = (SuiteEvaluator) result;
						setRuntimeContext(suiteFixture); // Subsequent tables are global for now.
						callMethod(suiteFixture, "suiteSetUp", table,tableListener.getTestResults());
						pushSut(result,table,testResults);
					} else if (fixtureThatIsRelevantSubclassOfDoFixture(result)) {
						flowEvaluator.fitHandler().doTable(result,new Table(row),testResults,flowEvaluator);
						return; // have finished table
					} else if (result instanceof CollectionSetUpTraverse) {
						DoTraverse doTraverse = (DoTraverse) result;
						setRuntimeContext(doTraverse);
						doTraverse.interpretAfterFirstRow(new Table(row), testResults); // It could be any row
						break;// have finished table
					} else if (result instanceof DoEvaluator) {
						aDoEvaluator((DoEvaluator) result, table, testResults);
					} else if (result instanceof Evaluator) { // Calculate, etc
						Evaluator evaluator = (Evaluator) result;
						setRuntimeContext(evaluator);
						callSetUpSutChain(evaluator,table, testResults);
						evaluator.interpretAfterFirstRow(new Table(row), testResults); // It could be any row
						callTearDownSutChain(evaluator,table, testResults);
						return; // have finished table
					} else {
						if (result instanceof Fixture) {
							flowEvaluator.fitHandler().doTable(result,new Table(row),testResults,flowEvaluator);
							return; // have finished table
						} else if (CalledMethodTarget.canAutoWrap(result))
							pushSut(result,table,testResults);
					} // But only when it comes from a class name!
				} catch (Exception ex) {
					row.error(testResults, ex);
				}
		}
	}
	private void setRuntimeContext(Object object) {
		if (object instanceof RuntimeContextual)
			((RuntimeContextual)object).setRuntimeContext(runtimeContext);
		if (!(object instanceof Evaluator) && object instanceof DomainAdapter)
			setRuntimeContext(((DomainAdapter)object).getSystemUnderTest());
	}
	private void aDoEvaluator(DoEvaluator doEvaluator, Table table, TestResults testResults) {
		doEvaluator.setRuntimeContext(flowEvaluator.getRuntimeContext());
		pushSut(doEvaluator,table,testResults);
	}
	private boolean fixtureThatIsRelevantSubclassOfDoFixture(Object result) {
		return result instanceof SetUpFixture;
	}
	private void pushSut(Object sut, Table table, TestResults testResults) {
		Object currentSut = flowEvaluator.getSystemUnderTest();
		if (currentSut != null)
			tableStack.push(flowEvaluator.getTypedSystemUnderTest());
		flowEvaluator.setSystemUnderTest(sut); // This will take account of any sut being type-wrapped.
		callSetUpSutChain(sut, table, testResults);
		if (currentSut != null && currentSut == suiteFixture)
			flowEvaluator.setRuntimeContext(suiteFixture.getCopyOfRuntimeContext());
	}
	private void callSetUpSutChain(Object sutInitially, final Table table, final TestResults testResults) {
		Object sut = sutInitially;
		if (sut instanceof TypedObject)
			sut = ((TypedObject)sut).getSubject();
		setUpTearDownManager.addReferences(sut, new MethodCaller(){
			public void setUp(Object object) {
				callMethod(object,"setUp",table,testResults);
			}
			public void tearDown(Object object) {
				callMethod(object,"tearDown",table,testResults);
			}
		});
	}
	private void popLocalSut(Table table, TableListener tableListener) {
		while (!tableStack.isEmpty() && tableStack.peek().getSubject() != suiteFixture) {
			callTearDownSutChain(flowEvaluator.getSystemUnderTest(),table,tableListener.getTestResults());
			flowEvaluator.setSystemUnderTest(tableStack.pop());
		}
	}
	private void callTearDownSutChain(Object sut, final Table table, final TestResults testResults) {
		setUpTearDownManager.removeReferences(sut, new MethodCaller(){
			public void setUp(Object object) {
				callMethod(object,"setUp",table,testResults);
			}
			public void tearDown(Object object) {
				callMethod(object,"tearDown",table,testResults);
			}
		});
	}
	private void tearDownFlowObject(Tables tables, TableListener tableListener) {
		callTearDownSutChain(flowEvaluator.getSystemUnderTest(),tables.last(),tableListener.getTestResults());
	}
	@Override
	public void setCurrentAction() {
		current = this;
	}
	@Override
	public void setCurrentCheck() {
		current = domainCheck;
	}
	protected void callMethod(Object object, String methodName, Table table, TestResults results) {
		try {
			Method method = object.getClass().getMethod(methodName, new Class[]{});
			method.invoke(object, new Object[]{});
		} catch (NoSuchMethodException e) {
			//
		} catch (Exception e) {
			table.error(results, e);
		}
	}
	public void exit() {
		if (suiteFixture != null)
			callMethod(suiteFixture,"suiteTearDown",new Table(new Row()),new TestResults());
	}
}
