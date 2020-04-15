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

import fit.ColumnFixture;
import fit.Fixture;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Table;
import fitlibrary.traverse.FitHandler;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlowWithFixture {
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
	public void runWithFixture() throws FitParseException {
		final MockFixture mockFixture = context.mock(MockFixture.class);
		final Fixture evaluator = new ColumnFixture() {
			@Override
			public void doTable(Parse parse) {
				mockFixture.doTable(parse);
			}
		};
		final GenericTypedObject typedResult1 = new GenericTypedObject(evaluator);
		final Parse parse = new Parse("<table><tr><td>1</td></tr></table>");
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(runtime).pushTestResults(with(any(TestResults.class)));
			allowing(runtime).isAbandoned(with(any(TestResults.class))); will(returnValue(false));
		
			oneOf(runtime).setCurrentTable(table);
			oneOf(runtime).setCurrentRow(table.at(0));
			oneOf(flowEvaluator).interpretRow(table.at(0),testResults);
			  will(returnValue(typedResult1));
			oneOf(table).asTableOnParse(); will(returnValue(table));
			oneOf(flowEvaluator).fitHandler(); will(returnValue(new FitHandler()));
			oneOf(table).asParse(); will(returnValue(parse));
			oneOf(mockFixture).doTable(parse);
			
			oneOf(table).replaceAt(0, table.at(0));
			oneOf(table).replaceAt(1, table.at(1));
			oneOf(runtime).popTestResults();
		}});
		doFlow.runTable(table,tableListener);
	}

	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
	static interface MockFixture {
		void doTable(Parse parse);
	}
}
