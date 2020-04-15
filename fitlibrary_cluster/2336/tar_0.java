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
import fitlibrary.runtime.RuntimeContextContainer;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
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
	final TestResults testResults = TestResultsFactory.testResults();
	final ITableListener tableListener = context.mock(ITableListener.class);
	final RuntimeContextContainer runtime = new RuntimeContextContainer();
	DoFlow doFlow;
	
	final Tables tables = context.mock(Tables.class);
	final Table table1 = context.mock(Table.class,"table1");
	final Row row1 = context.mock(Row.class,"row1");
	final Cell cell1 = context.mock(Cell.class,"cell1");
	final Row row2 = context.mock(Row.class,"row2");
	
	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
//			oneOf(flowEvaluator).setRuntimeContext(with(aNonNull(RuntimeContextInternal.class)));
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(scopeStack).clearAllButSuite();
			oneOf(tableListener).storytestFinished();
			allowing(flowEvaluator).getRuntimeContext(); will(returnValue(runtime));
		}});
		doFlow = new DoFlow(flowEvaluator,scopeStack,runtime);
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
	// FOLLOWING CHANGE STACK:
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
		final DoEvaluatorWithSetupTearDown doEval = context.mock(DoEvaluatorWithSetupTearDown.class);
		context.checking(new Expectations() {{
			exactly(2).of(doEval).setRuntimeContext(with(aNonNull(RuntimeContextInternal.class)));
			allowing(doEval).getSystemUnderTest(); will(returnValue(null));
			exactly(2).of(doEval).setUp();
			exactly(2).of(doEval).tearDown();
		}});
		verifyScopePush(doEval,doEval);
	}
	interface DoEvaluatorWithSetupTearDown extends DoEvaluator {
		void setUp();
		void tearDown();
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
			
			exactly(2).of(flowEvaluator).interpretRow(row2,testResults);
			  will(returnValue(typedResult2));
			exactly(2).of(scopeStack).push(genT);
			
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(genS)));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(genS)));
			exactly(2).of(tableListener).tableFinished(table1);
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	
	
	private void verifyScopePush(final Object result, final Object sut) {
		expectTwoRowsInOneTableAndOneInAnother();
		context.checking(new Expectations() {{
			exactly(2).of(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(new GenericTypedObject(result)));
			exactly(2).of(scopeStack).push(new GenericTypedObject(sut));
			exactly(2).of(flowEvaluator).interpretRow(row2,testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(new GenericTypedObject(sut))));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(new GenericTypedObject(sut))));
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
			allowing(tables).elementAt(0); will(returnValue(table1));
			allowing(tables).elementAt(1); will(returnValue(table1));
			allowing(tables).last(); will(returnValue(table1));
			allowing(table1).size(); will(returnValue(2));
			allowing(table1).isPlainTextTable(); will(returnValue(false));
			allowing(table1).elementAt(0); will(returnValue(row1));
			allowing(table1).elementAt(1); will(returnValue(row2));
			allowing(row1).elementAt(0); will(returnValue(cell1));
			allowing(row1).size(); will(returnValue(2));
			allowing(row2).size(); will(returnValue(2));
			allowing(cell1).hasEmbeddedTables(); will(returnValue(false));
			allowing(row2).elementAt(0); will(returnValue(cell1));
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
