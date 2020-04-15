/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import static fitlibrary.matcher.TableBuilderForTests.cell;
import static fitlibrary.matcher.TableBuilderForTests.row;
import static fitlibrary.matcher.TableBuilderForTests.table;
import static fitlibrary.matcher.TableBuilderForTests.tables;

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
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlowWithNestedTables {
	final Mockery context = new Mockery();
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final IScopeState scopeState = context.mock(IScopeState.class);
	final RuntimeContextInternal runtime = context.mock(RuntimeContextInternal.class);
	final SetUpTearDown setUpTearDown = context.mock(SetUpTearDown.class);
	DoFlow doFlow;
	
	final Tables tables = makeTables();
	final Table table = tables.at(0);
	final Row firstRow = table.at(0);
	final Row innerRow = table.at(1).at(0).at(0).at(0);

	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(scopeStack).clearAllButSuite();
			oneOf(scopeStack).setAbandon(false);
			oneOf(tableListener).storytestFinished();
			oneOf(runtime).setStopOnError(false);
			oneOf(runtime).reset();
			oneOf(runtime).setCurrentTable(tables.at(0));
			oneOf(runtime).pushTestResults(with(any(TestResults.class)));
			allowing(runtime).isAbandoned(with(any(TestResults.class))); will(returnValue(false));
			oneOf(runtime).setCurrentRow(firstRow);
			oneOf(runtime).setCurrentRow(innerRow);
			oneOf(runtime).popTestResults();
			oneOf(runtime).addAccumulatedFoldingText(table);
			
			allowing(table).isPlainTextTable(); will(returnValue(false));
		}});
		doFlow = new DoFlow(flowEvaluator,scopeStack,runtime,setUpTearDown);
	}
	private Tables makeTables() {
		return tables().with(table().with(
				row().with(cell(),cell()),
				row().with(
						cell().with(
								table().with(row().with(cell()))),
						cell())
		)).mock(context);
	}
	
	@Test
	public void innerTableIsRun() {
		final GenericTypedObject typedResult1 = new GenericTypedObject(new DoTraverse("s"));
		final GenericTypedObject typedResult2 = new GenericTypedObject(new DoTraverse("t"));
		final GenericTypedObject genS = new GenericTypedObject("s");
		final GenericTypedObject genT = new GenericTypedObject("t");
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).interpretRow(firstRow,testResults);
			  will(returnValue(typedResult1));
			oneOf(scopeStack).push(genS);
			oneOf(setUpTearDown).callSetUpSutChain("s", firstRow, testResults);
			oneOf(setUpTearDown).callTearDownSutChain("s", firstRow, testResults);
			
			oneOf(scopeStack).currentState(); will(returnValue(scopeState));
			
			oneOf(flowEvaluator).interpretRow(innerRow,testResults);
			  will(returnValue(typedResult2));
			oneOf(scopeStack).push(genT);
			oneOf(setUpTearDown).callSetUpSutChain("t", innerRow, testResults);
			oneOf(setUpTearDown).callTearDownSutChain("t", firstRow, testResults);
			
			oneOf(scopeState).restore();

			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(genT,genS)));
			oneOf(tableListener).tableFinished(table);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
