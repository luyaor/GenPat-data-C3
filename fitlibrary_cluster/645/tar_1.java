/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.runtime.RuntimeContextContainer;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.TestResultsFactory;

@RunWith(JMock.class)
public class TestDoFlowWithSelect {
	final Mockery context = new Mockery();
	final Row row = context.mock(Row.class);
	final FlowEvaluator flowEvaluator = context.mock(FlowEvaluator.class);
	final IScopeStack scopeStack = context.mock(IScopeStack.class);
	final Object something = "something";
	final TypedObject someTypedObject = context.mock(TypedObject.class);
	final RuntimeContextContainer runtime = new RuntimeContextContainer();
	final DoFlow doFlow = new DoFlow(flowEvaluator,scopeStack,runtime);

	@Test(expected=FitLibraryException.class)
	public void selectUnknown() {
		context.checking(new Expectations() {{
			oneOf(scopeStack).select("unknown"); will(throwException(new FitLibraryException("")));
		}});
		doFlow.select("unknown");
	}
	@Test
	public void addNamedObjectAndSelect() {
		final TestResults testResults = TestResultsFactory.testResults();
		context.checking(new Expectations() {{
			oneOf(someTypedObject).getSubject(); will(returnValue(something));
			oneOf(scopeStack).addNamedObject("x",someTypedObject,row, testResults);
			oneOf(scopeStack).select("x");
		}});
		doFlow.addNamedObject("x",someTypedObject,row,testResults);
		doFlow.select("x");
	}
}
