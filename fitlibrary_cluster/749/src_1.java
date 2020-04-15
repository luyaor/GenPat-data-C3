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

import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.exception.FitLibraryShowException.Show;
import fitlibrary.exception.parse.BadNumberException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.traverse.workflow.special.PrefixSpecialAction.NotSyle;

@RunWith(JMock.class)
public class TestNot extends SpecialActionTest {
	class NotExpectations extends Expectations {
		public NotExpectations() throws Exception {
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,0);will(returnValue(target));

			allowing(initialRow).fromAt(2);will(returnValue(subRow));
			allowing(initialRow).at(0);will(returnValue(firstCell));
		}
	}
	@Test
	public void reportsPassWithFalseResult() throws Exception {
		context.checking(new NotExpectations() {{			
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);will(returnValue(false));
			one(firstCell).pass(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsPassWithParseExceptionThrown() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			  will(throwException(new BadNumberException()));
			one(firstCell).pass(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithParseExceptionThrown() throws Exception {
		final BadNumberException exception = new BadNumberException();
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			  will(throwException(exception));
			one(initialRow).error(testResults,exception);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.ERROR_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsFailWithTrueResult() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);will(returnValue(true));
			one(firstCell).fail(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithNonBooleanResult() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);will(returnValue("not a bool"));
			one(firstCell).error(with(testResults),with(any(NotRejectedException.class)));
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsNothingWithIgnoredException() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			   will(throwException(new IgnoredException()));
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsPassWithParseExceptionInIgnoredException() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			   will(throwException(new IgnoredException(new BadNumberException())));
			one(firstCell).pass(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsFailWithParseExceptionInIgnoredException() throws Exception {
		final BadNumberException embeddedException = new BadNumberException();
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			  will(throwException(new IgnoredException(embeddedException)));
			one(initialRow).error(testResults,embeddedException);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.ERROR_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithOtherFitLibraryExceptionThrown() throws Exception {
		final FitLibraryException exception = new FitLibraryException("");
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			    will(throwException(exception));
			one(initialRow).error(testResults,exception);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsPassWithInvocationTargetExceptionThrown() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			    will(throwException(new InvocationTargetException(new RuntimeException())));
			one(firstCell).pass(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithInvocationTargetExceptionThrown() throws Exception {
		final RuntimeException embeddedException = new RuntimeException();
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
				will(throwException(new InvocationTargetException(embeddedException)));
			one(initialRow).error(testResults,embeddedException);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.ERROR_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithFitLibraryInsideInvocationTargetExceptionThrown() throws Exception {
		final FitLibraryShowException embeddedException = new FitLibraryShowException(new Show(""));
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
				will(throwException(new InvocationTargetException(embeddedException)));
			one(initialRow).error(testResults,embeddedException);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsPassWithOtherExceptionThrown() throws Exception {
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell); 
			  will(throwException(new RuntimeException("")));
			one(firstCell).pass(testResults);
		}});
		TwoStageSpecial lazySpecial = special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
		lazySpecial.run(testResults);
	}
	@Test
	public void reportsErrorWithOtherExceptionThrown() throws Exception {
		final RuntimeException exception = new RuntimeException("");
		context.checking(new NotExpectations() {{
			one(target).invokeForSpecial(subRow,testResults,false,firstCell);
			  will(throwException(exception));
			one(initialRow).error(testResults,exception);
		}});
		TwoStageSpecial twoStageSpecial = special.not(initialRow,NotSyle.ERROR_ON_EXCEPION);
		twoStageSpecial.run(testResults);
	}
	@Test(expected=RuntimeException.class)
	public void hasMissingMethod() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(3));
			one(actionContext).findMethodFromRow(initialRow,1,0);will(throwException(new RuntimeException()));
		}});
		special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
	}
	@Test(expected=MissingCellsException.class)
	public void rowIsTooSmall() throws Exception {
		context.checking(new Expectations() {{
			allowing(initialRow).size();will(returnValue(1));
		}});
		special.not(initialRow,NotSyle.PASSES_ON_EXCEPION);
	}
}
