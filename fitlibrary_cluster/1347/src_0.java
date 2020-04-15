/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;

@RunWith(JMock.class)
public class TestShow extends TestSpecialAction {
	@Test
	public void textIsShown() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,0);will(returnValue(target));
			allowing(initialRow).rowFrom(2);will(returnValue(subRow));
			allowing(initialRow).cell(0);will(returnValue(firstCell));
			one(target).invokeForSpecial(subRow,testResults,true,firstCell);will(returnValue("result"));
			one(target).getResultString("result");will(returnValue("Result"));
			one(initialRow).addCell("Result");will(returnValue(expectedCell));
			one(expectedCell).shown();
		}});
		TwoStageSpecial lazySpecial = special.show(initialRow);
		assertThat(lazySpecial.run(testResults),is((Object)null));
	}
	@Test(expected=RuntimeException.class)
	public void hasMissingMethod() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,0);will(throwException(new RuntimeException()));
		}});
		special.show(initialRow);
	}
	@Test(expected=MissingCellsException.class)
	public void rowIsTooSmall() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(1));
		}});
		special.show(initialRow);
	}
}
