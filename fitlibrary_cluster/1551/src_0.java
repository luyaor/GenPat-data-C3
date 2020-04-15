/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import static fitlibrary.matcher.TableBuilderForTests.cell;
import static fitlibrary.matcher.TableBuilderForTests.row;
import static fitlibrary.matcher.TableBuilderForTests.table;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.SetUpFixture;
import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Table;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlowWithEvaluator {
	final Mockery context = new Mockery();
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final IScopeState scopeState = context.mock(IScopeState.class);
	final RuntimeContextInternal runtime = context.mock(RuntimeContextInternal.class);
	final SetUpTearDown setUpTearDown = context.mock(SetUpTearDown.class);
	final DoFlow doFlow = new DoFlow(flowEvaluator,scopeStack,runtime,setUpTearDown);
	
	final Table table = table().with(
			row().with(cell(),cell()),
			row().with(cell(),cell())).mock(context);
	
	@Test
	public void runWithCollectionSetUpTraverse() {
		final Evaluator mockEvaluator = context.mock(Evaluator.class);
		final Evaluator evaluator = new CollectionSetUpTraverse() {
			@Override
			public Object interpretAfterFirstRow(Table table2, TestResults testResults2) {
				return mockEvaluator.interpretAfterFirstRow(table2, testResults2);
			}
		};
		verifyWithEvaluator(evaluator, mockEvaluator);
	}
	@Test
	public void runWithCollectionSetUpFixture() {
		final Evaluator mockEvaluator = context.mock(Evaluator.class);
		final Evaluator evaluator = new SetUpFixture() {
			@Override
			public Object interpretAfterFirstRow(Table table2, TestResults testResults2) {
				return mockEvaluator.interpretAfterFirstRow(table2, testResults2);
			}
		};
		verifyWithEvaluator(evaluator, mockEvaluator);
	}
	@Test
	public void runWithEvaluator() {
		final Evaluator mockEvaluator = context.mock(Evaluator.class);
		context.checking(new Expectations() {{
			allowing(mockEvaluator).setRuntimeContext(runtime);
			allowing(mockEvaluator).getSystemUnderTest(); will(returnValue(null));
		}});
		verifyWithEvaluator(mockEvaluator, mockEvaluator);
	}

	private void verifyWithEvaluator(final Evaluator evaluator, final Evaluator mockEvaluator) {
		final GenericTypedObject typedResult1 = new GenericTypedObject(evaluator);
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(runtime).pushTestResults(with(any(TestResults.class)));
			allowing(runtime).isAbandoned(with(any(TestResults.class))); will(returnValue(false));

			oneOf(runtime).setCurrentTable(table);
			oneOf(runtime).setCurrentRow(table.at(0));
			oneOf(flowEvaluator).interpretRow(table.at(0),testResults);
			  will(returnValue(typedResult1));
			oneOf(setUpTearDown).callSetUpSutChain(evaluator, table.at(0), testResults);
			oneOf(scopeStack).push(typedResult1);
			oneOf(mockEvaluator).interpretAfterFirstRow(table, testResults);
			oneOf(setUpTearDown).callTearDownSutChain(evaluator, table.at(0), testResults);
			oneOf(runtime).popTestResults();
		}});
		doFlow.runTable(table,tableListener);
	}
	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
