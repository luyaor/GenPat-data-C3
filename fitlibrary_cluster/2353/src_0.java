/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import static fitlibrary.matcher.TableBuilderForTests.*;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SuiteEvaluator;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlowWithSuiteEvaluator {
	final Mockery context = new Mockery();
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final IScopeState scopeState = context.mock(IScopeState.class);
	final RuntimeContextInternal runtime = context.mock(RuntimeContextInternal.class);
	final RuntimeContextInternal runtimeCopy = context.mock(RuntimeContextInternal.class,"runtimeCopy");
	final SetUpTearDown setUpTearDown = context.mock(SetUpTearDown.class);
	final DoFlow doFlow = new DoFlow(flowEvaluator,scopeStack,runtime,setUpTearDown);
	
	final Tables storytest1 = tables().with(
			table().with(
					row().with(cell(),cell()))
			).mock(context,"storytest1");
	final Table table1 = storytest1.at(0);
	
	final Tables storytest2 = tables().with(
			table().with(
					row().with(cell(),cell()))
			).mock(context,"storytest2");
	final Table table2 = storytest2.at(0);

	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
			oneOf(runtime).reset();
		}});
	}
	@Test
	public void runWithPlainSuiteFixture() {
		final SuiteEvaluator suiteEvaluator = context.mock(SuiteEvaluator.class);
		verifyWithEvaluator(suiteEvaluator);
	}

	private void verifyWithEvaluator(final SuiteEvaluator suiteEvaluator) {
		final GenericTypedObject typedResult1 = new GenericTypedObject(suiteEvaluator);
		
		storytestWithOneTableIsRunWithoutError(storytest1,runtime);
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));

			allowing(suiteEvaluator).setRuntimeContext(runtime);
			oneOf(flowEvaluator).interpretRow(table1.at(0),testResults);
			  will(returnValue(typedResult1));
			allowing(suiteEvaluator).getSystemUnderTest(); will(returnValue(null));
			oneOf(setUpTearDown).callSuiteSetUp(suiteEvaluator, table1.at(0), testResults);
			oneOf(scopeStack).push(typedResult1);
			oneOf(setUpTearDown).callSetUpSutChain(suiteEvaluator, table1.at(0), testResults);
		}});
		doFlow.runStorytest(storytest1,tableListener);

		storytestWithOneTableIsRunWithoutError(storytest2,runtimeCopy);
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).setRuntimeContext(runtimeCopy);
			oneOf(suiteEvaluator).getCopyOfRuntimeContext(); will(returnValue(runtimeCopy));
			allowing(flowEvaluator).getSystemUnderTest(); will(returnValue(null));

			oneOf(flowEvaluator).interpretRow(table2.at(0),testResults);
			  will(returnValue(GenericTypedObject.NULL));
		}});
		doFlow.runStorytest(storytest2,tableListener);

		context.checking(new Expectations() {{
			oneOf(setUpTearDown).callSuiteTearDown(with(suiteEvaluator),with(any(TestResults.class)));
		}});
		doFlow.exit();
	}
	private void storytestWithOneTableIsRunWithoutError(Tables storytest, final RuntimeContextInternal runtimeLocal) {
		final Table table = storytest.at(0);
		context.checking(new Expectations() {{
			allowing(table).isPlainTextTable(); will(returnValue(false));
			allowing(runtimeLocal).isAbandoned(with(any(TestResults.class))); will(returnValue(false));
			
			oneOf(scopeStack).setAbandon(false);
			oneOf(scopeStack).setStopOnError(false);
			oneOf(scopeStack).clearAllButSuite();

			oneOf(runtimeLocal).setCurrentTable(table);
			oneOf(runtimeLocal).pushTestResults(with(any(TestResults.class)));
			oneOf(runtimeLocal).setCurrentRow(table.at(0));
			oneOf(runtimeLocal).popTestResults();
			oneOf(runtimeLocal).addAccumulatedFoldingText(table);
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list()));
			oneOf(tableListener).tableFinished(table);
			oneOf(tableListener).storytestFinished();
		}});
	}
	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
