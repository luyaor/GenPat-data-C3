/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.dynamicVariable.LocalDynamicVariables;
import fitlibrary.table.Tables;

@RunWith(JMock.class)
public class TestMultiParameterSubstitution {
	Mockery context = new Mockery();
	protected DynamicVariables vars = context.mock(DynamicVariables.class);

	@Test
	public void fileName() {
		MultiParameterSubstitution substitutes = 
			new MultiParameterSubstitution(new ArrayList<String>(), new Tables(), "fileName");
		assertThat(substitutes.getAbsoluteFileName(),is("fileName"));
	}
	@Test
	public void actualArgumentsProvided() {
		context.checking(new Expectations() {{
			one(vars).get("c"); will(returnValue("3"));
		}});

		String[] formals = { "a", "b" };
		MultiParameterSubstitution substitutes = 
			new MultiParameterSubstitution(Arrays.asList(formals), new Tables(), "fileName");
		String[] actuals = { "1", "2" };
		LocalDynamicVariables localVars = new LocalDynamicVariables(vars);
		substitutes.createMappingsForCall(Arrays.asList(actuals),localVars);
		assertThat(localVars.get("a"),is((Object)"1"));
		assertThat(localVars.get("b"),is((Object)"2"));
		assertThat(localVars.get("c"),is((Object)"3"));
	}
}
