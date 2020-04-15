/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fit.Fixture;
import fitlibrary.DoFixture;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.RuntimeContextual;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

@RunWith(JMock.class)
public class TestDoFlow {
	final Mockery context = new Mockery();
	final States stackStates = context.states("stack").startsAs("empty");
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final RuntimeContextual global = context.mock(RuntimeContextual.class);
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final RuntimeContextInternal runtime = context.mock(RuntimeContextInternal.class);
	final SetUpTearDown setUpTearDown = context.mock(SetUpTearDown.class);
	DoFlow doFlow;
	
	final Tables tables = context.mock(Tables.class);
	final Table table1 = context.mock(Table.class,"table1");
	final Row row1 = context.mock(Row.class,"row1");
	final Cell cell1 = context.mock(Cell.class,"cell1");
	final Row row2 = context.mock(Row.class,"row2");
	
	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(scopeStack).clearAllButSuite();
			oneOf(scopeStack).setAbandon(false);
			oneOf(runtime).setStopOnError(false);
			oneOf(runtime).reset();
			exactly(2).of(runtime).setCurrentTable(table1);
			exactly(2).of(runtime).pushTestResults(with(any(TestResults.class)));
			allowing(runtime).isAbandoned(with(any(TestResults.class))); will(returnValue(false));
			exactly(2).of(runtime).setCurrentRow(row1);
			exactly(2).of(runtime).setCurrentRow(row2);
			exactly(2).of(runtime).popTestResults();
			exactly(2).of(runtime).addAccumulatedFoldingText(table1);
			oneOf(tableListener).storytestFinished();
		}});
		doFlow = new DoFlow(flowEvaluator,scopeStack,runtime,setUpTearDown);
	}
	// FOLLOWING DON'T CHANGE SCOPE AT ALL:
	@Test
	public void nullSoNoScopeChange() {
		verifyNoScopeChangeWith(null);
	}
	@Test
	public void anObjectOfTheClassFixtureSoNoScopeChange() {
		verifyNoScopeChangeWith(new Fixture());
	}
	@Test
	public void aNonWrappableSoNoScopeChange() {
		verifyNoScopeChangeWith("string");
	}
	@Test
	public void anObjectOfTheClassDoFixtureWithNoSutSoNoScopeChange() {
		verifyNoScopeChangeWith(new DoFixture());
	}
	@Test
	public void aDoTraverseWithNoSutSoNoScopeChange() {
		verifyNoScopeChangeWith(new DoTraverse());
	}
	// THE FOLLOWING CHANGE THE STACK:
	@Test
	public void aDoTraverseWithSutSoScopeChange() {
		verifyScopePush(new DoTraverse(new Point()),new Point());
	}
	@Test
	public void aPojoWithSutSoScopeChange() {
		PojoWithSut pojoWithSut = new PojoWithSut();
		verifyScopePush(new DoTraverse(pojoWithSut),pojoWithSut);
	}
	static class PojoWithSut implements DomainAdapter {
		@Override
		public Object getSystemUnderTest() {
			return new Point();
		}
	}
	@Test
	public void aDoEvaluatorSoScopeChange() {
		final DoEvaluatorExample doEval = context.mock(DoEvaluatorExample.class);
		context.checking(new Expectations() {{
			exactly(2).of(doEval).setRuntimeContext(with(aNonNull(RuntimeContextInternal.class)));
			allowing(doEval).getSystemUnderTest(); will(returnValue(null));
		}});
		verifyScopePush(doEval,doEval);
	}
	interface DoEvaluatorExample extends DoEvaluator {
		//
	}
	@Test
	public void aDoEvaluatorWithSetUpTearDownSoScopeChange() {
		final DoEvaluator doEval = context.mock(DoEvaluator.class);
		context.checking(new Expectations() {{
			exactly(2).of(doEval).setRuntimeContext(with(aNonNull(RuntimeContextInternal.class)));
			allowing(doEval).getSystemUnderTest(); will(returnValue(null));
		}});
		verifyScopePush(doEval,doEval);
	}
	@Test
	public void twoDoTraversesSoScopeStackChange() {
		final GenericTypedObject typedResult1 = new GenericTypedObject(new DoTraverse("s"));
		final GenericTypedObject typedResult2 = new GenericTypedObject(new DoTraverse("t"));
		final GenericTypedObject genS = new GenericTypedObject("s");
		final GenericTypedObject genT = new GenericTypedObject("t");
		expectTwoRowsInOneTableAndOneInAnother();
		context.checking(new Expectations() {{
			exactly(2).of(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(typedResult1));
			exactly(2).of(scopeStack).push(genS);
			exactly(2).of(setUpTearDown).callSetUpSutChain("s", row1, testResults);
			exactly(2).of(setUpTearDown).callTearDownSutChain("s", row1, testResults);
			
			exactly(2).of(flowEvaluator).interpretRow(row2,testResults);
			  will(returnValue(typedResult2));
			exactly(2).of(scopeStack).push(genT);
			exactly(2).of(setUpTearDown).callSetUpSutChain("t", row2, testResults);
			exactly(2).of(setUpTearDown).callTearDownSutChain("t", row1, testResults);
			
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(genT,genS)));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(genT,genS)));
			exactly(2).of(tableListener).tableFinished(table1);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	
	
	private void verifyScopePush(final Object result, final Object sut) {
		expectTwoRowsInOneTableAndOneInAnother();
		final GenericTypedObject typedSut = new GenericTypedObject(sut);
		context.checking(new Expectations() {{
			exactly(2).of(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(new GenericTypedObject(result)));
			exactly(2).of(scopeStack).push(typedSut);
			exactly(2).of(setUpTearDown).callSetUpSutChain(sut, row1, testResults);
			exactly(2).of(setUpTearDown).callTearDownSutChain(sut, row1, testResults);
			exactly(2).of(flowEvaluator).interpretRow(row2,testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(typedSut)));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(typedSut)));
			exactly(2).of(tableListener).tableFinished(table1);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	private void verifyNoScopeChangeWith(final Object result) {
		expectTwoRowsInOneTableAndOneInAnother();
		context.checking(new Expectations() {{
			exactly(2).of(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(new GenericTypedObject(result)));
			exactly(2).of(flowEvaluator).interpretRow(row2,testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list()));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list()));
			exactly(2).of(tableListener).tableFinished(table1);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	private void expectTwoRowsInOneTableAndOneInAnother() {
		context.checking(new Expectations() {{
			allowing(tables).size(); will(returnValue(2));
			allowing(tables).at(0); will(returnValue(table1));
			allowing(tables).at(1); will(returnValue(table1));
			allowing(tables).last(); will(returnValue(table1));
			allowing(table1).size(); will(returnValue(2));
			allowing(table1).isPlainTextTable(); will(returnValue(false));
			allowing(table1).at(0); will(returnValue(row1));
			allowing(table1).at(1); will(returnValue(row2));
			allowing(table1).last(); will(returnValue(row2));
			allowing(row1).at(0); will(returnValue(cell1));
			allowing(row1).size(); will(returnValue(2));
			allowing(row2).size(); will(returnValue(2));
			allowing(cell1).hasEmbeddedTables(); will(returnValue(false));
			allowing(row2).at(0); will(returnValue(cell1));
			allowing(cell1).hadError(); will(returnValue(false));
		}});
	}
	protected ArrayList<TypedObject> scopeList(Object... objects) {
		ArrayList<TypedObject> list = new ArrayList<TypedObject>();
		for (Object obj: objects)
			list.add(new GenericTypedObject(obj));
		return list;
	}
	static Matcher<List<TypedObject>> hasSubjects(Object... expected) {
		return new HasTypedSubjectsMatcher(expected);
	}
	protected <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
