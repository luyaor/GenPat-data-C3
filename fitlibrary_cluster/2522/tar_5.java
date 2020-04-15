/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import java.lang.reflect.InvocationTargetException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.parse.BadNumberException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;

@RunWith(JMock.class)
public class TestEnsure extends TestSpecialAction {
	class EnsureExpectations extends Expectations {
		public EnsureExpectations() throws Exception {
			allowing(initialRow).size();will(returnValue(3));
			allowing(initialRow).cell(0);will(returnValue(firstCell));
			allowing(initialRow).rowFrom(2);will(returnValue(subRow));
			one(actionContext).setExpectedResult(true);
			one(actionContext).findMethodFromRow(initialRow,1,0);
			   will(returnValue(target));
		}
	}
	@Test
	public void passesWithNullResult() throws Exception {
		resultWhenNoException(null,true);
	}
	@Test
	public void passesWithTrueResult() throws Exception {
		resultWhenNoException(true,true);
	}
	@Test
	public void failsWithFalseResult() throws Exception {
		resultWhenNoException(false,false);
	}
	public void resultWhenNoException(final Object result, final boolean pass) throws Exception {
		context.checking(new EnsureExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,true,firstCell);
			  will(returnValue(result));
			one(firstCell).passOrFail(testResults,pass);
		}});
		TwoStageSpecial lazySpecial = special.ensure(initialRow);
		lazySpecial.run(testResults);
	}
	@Test
	public void ignoredWithIgnoredException() throws Exception {
		context.checking(new EnsureExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,true,firstCell);
			   will(throwException(new IgnoredException()));
		}});
		TwoStageSpecial lazySpecial = special.ensure(initialRow);
		lazySpecial.run(testResults);
	}
	@Test
	public void errorWithEmbeddedException() throws Exception {
		final BadNumberException embeddedException = new BadNumberException();
		context.checking(new EnsureExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,true,firstCell);
			   will(throwException(new InvocationTargetException(embeddedException)));
			one(initialRow).error(testResults,embeddedException);
		}});
		TwoStageSpecial lazySpecial = special.ensure(initialRow);
		lazySpecial.run(testResults);
	}
	@Test
	public void errorWithException() throws Exception {
		final BadNumberException exception = new BadNumberException();
		context.checking(new EnsureExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,true,firstCell);
			   will(throwException(exception));
			one(initialRow).error(testResults,exception);
		}});
		TwoStageSpecial lazySpecial = special.ensure(initialRow);
		lazySpecial.run(testResults);
	}
	@Test(expected=RuntimeException.class)
	public void hasMissingMethod() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,0);will(throwException(new RuntimeException()));
		}});
		special.ensure(initialRow);
	}
	@Test(expected=MissingCellsException.class)
	public void rowIsTooSmall() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(1));
		}});
		special.ensure(initialRow);
	}
}
