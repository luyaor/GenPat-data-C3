/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.table.ICell;
import fitlibrary.table.IRow;
import fitlibrary.traverse.workflow.caller.LazySpecial;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.Option;

@RunWith(JMock.class)
public class TestSpecialAction {
	Mockery context = new Mockery();
	SpecialActionContext actionContext = context.mock(SpecialActionContext.class);
	IRow initialRow = context.mock(IRow.class,"initialRow");
	IRow subRow = context.mock(IRow.class,"subRow");
	ICell expectedCell = context.mock(ICell.class,"expected cell");
	ICell firstCell = context.mock(ICell.class,"first cell");
	TestResults testResults = new TestResults();
	SpecialAction special = new SpecialAction(actionContext);
	ICalledMethodTarget target = context.mock(ICalledMethodTarget.class);
	
	@Test
	public void checkWorksOK() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,3);will(returnValue(target));
			allowing(initialRow).last();will(returnValue(expectedCell));
			one(actionContext).isGatherExpectedForGeneration();will(returnValue(false));
			allowing(initialRow).rowFrom(2);will(returnValue(subRow));
			allowing(initialRow).cell(0);will(returnValue(firstCell));
			one(target).invokeAndCheckForSpecial(subRow,expectedCell,testResults,initialRow,firstCell);
		}});
		Option<LazySpecial> lazySpecial = special.check(initialRow, testResults);
		assertThat(lazySpecial.isSome(),is(true));
		assertThat(lazySpecial.get().run(testResults),is((Object)null));
	}
	@Test
	public void checkIsNone() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,3);will(throwException(new RuntimeException()));
		}});
		Option<LazySpecial> lazySpecial = special.check(initialRow, testResults);
		assertThat(lazySpecial.isNone(),is(true));
	}
	@Test(expected=MissingCellsException.class)
	public void rowIsTooSmallForCheck() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(2));
		}});
		special.check(initialRow, testResults);
	}
}
