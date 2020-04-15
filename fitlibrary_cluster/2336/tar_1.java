/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.runtime.RuntimeContextContainer;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.utility.CollectionUtility;
import fitlibrary.utility.ITableListener;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.TestResultsFactory;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlowWithNestedTables {
	final Mockery context = new Mockery();
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final IScopeState scopeState = context.mock(IScopeState.class);
	final RuntimeContextContainer runtime = new RuntimeContextContainer();
	DoFlow doFlow;
	
	final Tables tables = context.mock(Tables.class,"tables");
	final Table table1 = context.mock(Table.class,"table1");
	final Row row1 = context.mock(Row.class,"row1");
	final Cell cell1 = context.mock(Cell.class,"cell1");
	final Row row2 = context.mock(Row.class,"row2");
	final Cell cell2 = context.mock(Cell.class,"cell2");
	final Tables innerTables = context.mock(Tables.class,"innerTables");
	final Table innerTable1 = context.mock(Table.class,"innerTable1");
	final Row innerRow1 = context.mock(Row.class,"innerRow1");
	final Cell innerCell = context.mock(Cell.class,"innerCell");

	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(scopeStack).clearAllButSuite();
			oneOf(tableListener).storytestFinished();
			allowing(flowEvaluator).getRuntimeContext(); will(returnValue(runtime));
		}});
		expectTwoRowsInFirstCellOfTable();
		doFlow = new DoFlow(flowEvaluator,scopeStack,runtime);
	}
	private void expectTwoRowsInFirstCellOfTable() {
		context.checking(new Expectations() {{
			allowing(tables).size(); will(returnValue(1));
			allowing(tables).table(0); will(returnValue(table1));
			allowing(tables).last(); will(returnValue(table1));
			allowing(table1).size(); will(returnValue(2));
			allowing(table1).row(0); will(returnValue(row1));
			allowing(row1).cell(0); will(returnValue(cell1));
			allowing(cell1).hasEmbeddedTable(); will(returnValue(false));
			allowing(cell1).hadError(); will(returnValue(false));
			allowing(row1).size(); will(returnValue(2));
			allowing(row2).size(); will(returnValue(2));
			
			allowing(table1).isPlainTextTable(); will(returnValue(false));
			allowing(table1).row(1); will(returnValue(row2));
			allowing(row2).cell(0); will(returnValue(cell2));
			allowing(cell2).hasEmbeddedTable(); will(returnValue(true));
			allowing(cell2).getEmbeddedTables(); will(returnValue(innerTables));
			allowing(cell2).hadError(); will(returnValue(false));
			allowing(innerTables).size(); will(returnValue(1));
			allowing(innerTables).table(0); will(returnValue(innerTable1));
			allowing(innerTable1).size(); will(returnValue(1));
			allowing(innerTable1).row(0); will(returnValue(innerRow1));
			allowing(innerRow1).cell(0); will(returnValue(innerCell));
			allowing(innerCell).hasEmbeddedTable(); will(returnValue(false));
			allowing(innerCell).hadError(); will(returnValue(false));
			allowing(innerRow1).size(); will(returnValue(2));
		}});
	}
	
	@Test
	public void innerTableIsRun() {
		final GenericTypedObject typedResult1 = new GenericTypedObject(new DoTraverse("s"));
		final GenericTypedObject typedResult2 = new GenericTypedObject(new DoTraverse("t"));
		final GenericTypedObject genS = new GenericTypedObject("s");
		final GenericTypedObject genT = new GenericTypedObject("t");
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(typedResult1));
			oneOf(scopeStack).push(genS);
			
			oneOf(scopeStack).currentState(); will(returnValue(scopeState));
			
			oneOf(flowEvaluator).interpretRow(innerRow1,testResults);
			  will(returnValue(typedResult2));
			oneOf(scopeStack).push(genT);
			
			oneOf(scopeState).restore();

			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(genS)));
			oneOf(tableListener).tableFinished(table1);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
