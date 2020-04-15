/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import static fitlibrary.matcher.TableBuilderForTests.cell;
import static fitlibrary.matcher.TableBuilderForTests.row;
import static fitlibrary.matcher.TableBuilderForTests.table;
import static fitlibrary.matcher.TableBuilderForTests.tables;

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
import fitlibrary.SelectFixture;
import fitlibrary.matcher.TableBuilderForTests.TableBuilder;
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
	
	final Tables tables = makeTables();
	final Table table0 = tables.at(0);
	final Row row0 = table0.at(0);
	final Cell cell0 = row0.at(0);
	final Row row1 = table0.at(1);
	
	@Before
	public void createDoFlow() {
		context.checking(new Expectations() {{
			allowing(tableListener).getTestResults(); will(returnValue(testResults));
			oneOf(scopeStack).clearAllButSuite();
			oneOf(scopeStack).setAbandon(false);
			oneOf(scopeStack).setStopOnError(false);
			oneOf(runtime).reset();
			oneOf(runtime).setCurrentTable(table0);
			exactly(2).of(runtime).pushTestResults(with(any(TestResults.class)));
			allowing(runtime).isAbandoned(with(any(TestResults.class))); will(returnValue(false));
			oneOf(runtime).setCurrentRow(row0);
			oneOf(runtime).setCurrentRow(row1);
			exactly(2).of(runtime).popTestResults();
			oneOf(runtime).addAccumulatedFoldingText(table0);
			oneOf(tableListener).storytestFinished();

			oneOf(runtime).setCurrentTable(tables.at(1));
			oneOf(runtime).setCurrentRow(tables.at(1).at(0));
			oneOf(runtime).setCurrentRow(tables.at(1).at(1));
			oneOf(runtime).addAccumulatedFoldingText(tables.at(1));

			allowing(table0).isPlainTextTable(); will(returnValue(false));
			allowing(tables.at(1)).isPlainTextTable(); will(returnValue(false));
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
	@Test
	public void aSelectFixtureWithNoSutSoNoScopeChange() {
		context.checking(new Expectations() {{
			exactly(2).of(runtime).showAsAfterTable(with(any(String.class)),with(any(String.class)));
		}});
		verifyNoScopeChangeWith(new SelectFixture());
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
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).interpretRow(row0,testResults);
			  will(returnValue(typedResult1));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(0),testResults);
			  will(returnValue(typedResult1));
			exactly(2).of(scopeStack).push(genS);
			oneOf(setUpTearDown).callSetUpSutChain("s", row0, testResults);
			oneOf(setUpTearDown).callSetUpSutChain("s", tables.at(1).at(0), testResults);
			oneOf(setUpTearDown).callTearDownSutChain("s", row0, testResults);
			oneOf(setUpTearDown).callTearDownSutChain("s", tables.at(1).at(0), testResults);
			
			oneOf(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(typedResult2));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(1),testResults);
			  will(returnValue(typedResult2));
			exactly(2).of(scopeStack).push(genT);
			oneOf(setUpTearDown).callSetUpSutChain("t", row1, testResults);
			oneOf(setUpTearDown).callSetUpSutChain("t", tables.at(1).at(1), testResults);
			oneOf(setUpTearDown).callTearDownSutChain("t", row0, testResults);
			oneOf(setUpTearDown).callTearDownSutChain("t", tables.at(1).at(0), testResults);
			
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(genT,genS)));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(genT,genS)));
			oneOf(tableListener).tableFinished(table0);
			oneOf(tableListener).tableFinished(tables.at(1));
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	
	
	private void verifyScopePush(final Object result, final Object sut) {
		final GenericTypedObject typedSut = new GenericTypedObject(sut);
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).interpretRow(row0,testResults);
			  will(returnValue(new GenericTypedObject(result)));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(0),testResults);
			  will(returnValue(new GenericTypedObject(result)));
			exactly(2).of(scopeStack).push(typedSut);
			oneOf(setUpTearDown).callSetUpSutChain(sut, row0, testResults);
			oneOf(setUpTearDown).callSetUpSutChain(sut, tables.at(1).at(0), testResults);
			oneOf(setUpTearDown).callTearDownSutChain(sut, row0, testResults);
			oneOf(setUpTearDown).callTearDownSutChain(sut, tables.at(1).at(0), testResults);
			oneOf(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(1),testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list(typedSut)));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list(typedSut)));
			oneOf(tableListener).tableFinished(table0);
			oneOf(tableListener).tableFinished(tables.at(1));
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	private void verifyNoScopeChangeWith(final Object result) {
		context.checking(new Expectations() {{
			oneOf(flowEvaluator).interpretRow(row0,testResults);
			  will(returnValue(new GenericTypedObject(result)));
			oneOf(flowEvaluator).interpretRow(row1,testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(0),testResults);
			  will(returnValue(new GenericTypedObject(result)));
			oneOf(flowEvaluator).interpretRow(tables.at(1).at(1),testResults);
			  will(returnValue(GenericTypedObject.NULL));
			oneOf(scopeStack).poppedAtEndOfTable(); will(returnValue(list()));
			oneOf(scopeStack).poppedAtEndOfStorytest(); will(returnValue(list()));
			oneOf(tableListener).tableFinished(table0);
			oneOf(tableListener).tableFinished(tables.at(1));
		}});
		doFlow.runStorytest(tables,tableListener);
	}
	private Tables makeTables() {
		return tables().with(
				tableWith2RowsOf2(),
				tableWith2RowsOf2()
		).mock(context);
	}
	private TableBuilder tableWith2RowsOf2() {
		return table().with(
				row().with(cell(),cell()),
				row().with(cell(),cell())
		);
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
