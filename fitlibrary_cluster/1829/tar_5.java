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
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fit.Fixture;
import fitlibrary.DoFixture;
import fitlibrary.SelectFixture;
import fitlibrary.matcher.TableBuilderForTests.TableBuilder;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.CollectionUtility;

@RunWith(JMock.class)
public class TestDoFlow {
	final Mockery context = new Mockery();
	final DoFlowDriver doFlowDriver = new DoFlowDriver(context);
	final Tables tables = makeTables();

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
			exactly(2).of(doFlowDriver.getRuntime()).showAsAfterTable(with(any(String.class)),with(any(String.class)));
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
		String s = "s";
		String t = "t";
		final Object doS = new DoTraverse(s);
		final Object doT = new DoTraverse(t);

		Table table0 = tables.at(0);
		Table table1 = tables.at(1);
		
		doFlowDriver.startingOnTable(table0);
		doFlowDriver.interpretingRowReturning(table0.at(0), doS);
		doFlowDriver.pushingObjectOnScopeStack(s);
		doFlowDriver.callingSetUpOn(s,table0.at(0));
		doFlowDriver.interpretingRowReturning(table0.at(1), doT);
		doFlowDriver.pushingObjectOnScopeStack(t);
		doFlowDriver.callingSetUpOn(t,table0.at(1));
		doFlowDriver.poppingScopeStackAtEndOfTable(list(t,s));
		doFlowDriver.callingTearDownOn(t, table0.at(0));
		doFlowDriver.callingTearDownOn(s, table0.at(0));
		doFlowDriver.finishingTable(table0);

		doFlowDriver.startingOnTable(table1);
		doFlowDriver.interpretingRowReturning(table1.at(0), doS);
		doFlowDriver.pushingObjectOnScopeStack(s);
		doFlowDriver.callingSetUpOn(s,table1.at(0));
		doFlowDriver.interpretingRowReturning(table1.at(1), doT);
		doFlowDriver.pushingObjectOnScopeStack(t);
		doFlowDriver.callingSetUpOn(t,table1.at(1));
		doFlowDriver.poppingScopeStackAtEndOfLastTable(list(t,s));
		doFlowDriver.callingTearDownOn(t, table1.at(0));
		doFlowDriver.callingTearDownOn(s, table1.at(0));
		doFlowDriver.finishingTable(table1);

		doFlowDriver.runStorytest(tables);
	}

	private void verifyScopePush(final Object result, final Object sut) {
		Table table0 = tables.at(0);
		Table table1 = tables.at(1);

		doFlowDriver.startingOnTable(table0);
		doFlowDriver.interpretingRowReturning(table0.at(0), result);
		doFlowDriver.pushingObjectOnScopeStack(sut); // -- extra
		doFlowDriver.callingSetUpOn(sut,table0.at(0)); // -- extra
		doFlowDriver.interpretingRowReturning(table0.at(1), null);
		doFlowDriver.poppingScopeStackAtEndOfTable(list(result));
		doFlowDriver.callingTearDownOn(result, table0.at(0));
		doFlowDriver.finishingTable(table0);

		doFlowDriver.startingOnTable(table1);
		doFlowDriver.interpretingRowReturning(table1.at(0), result);
		doFlowDriver.pushingObjectOnScopeStack(sut); // -- extra
		doFlowDriver.callingSetUpOn(sut,table1.at(0)); // -- extra
		doFlowDriver.interpretingRowReturning(table1.at(1), null);
		doFlowDriver.poppingScopeStackAtEndOfLastTable(list(result));
		doFlowDriver.callingTearDownOn(result, table1.at(0));
		doFlowDriver.finishingTable(table1);

		doFlowDriver.runStorytest(tables);
	}
	private void verifyNoScopeChangeWith(final Object result) {
		Table table0 = tables.at(0);
		Table table1 = tables.at(1);
		
		doFlowDriver.startingOnTable(table0);
		doFlowDriver.interpretingRowReturning(table0.at(0),result);
		doFlowDriver.interpretingRowReturning(table0.at(1), null);
		doFlowDriver.poppingAtEndOfTable();
		doFlowDriver.finishingTable(table0);

		doFlowDriver.startingOnTable(table1);
		doFlowDriver.interpretingRowReturning(table1.at(0), result);
		doFlowDriver.interpretingRowReturning(table1.at(1), null);
		doFlowDriver.poppingAtEndOfLastTable();
		doFlowDriver.finishingTable(table1);

		doFlowDriver.runStorytest(tables);
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
	protected List<Object> list(Object... ss) {
		return CollectionUtility.list(ss);
	}
}
