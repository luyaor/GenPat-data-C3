/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;

import junit.framework.TestCase;
import fitlibrary.DoFixture;
import fitlibrary.definedAction.ParameterBinder;
import fitlibrary.dynamicVariable.GlobalDynamicVariables;
import fitlibrary.matcher.TablesMatcher;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class TestMacroSubstitution extends TestCase {
	DoFixture evaluator = new DoFixture();
	public void testNoParameters() {
		Tables tables = bodyTables("a","b");
		ParameterBinder macro = new ParameterBinder(new ArrayList<String>(), tables,"",evaluator);
		Tables substituted = macro.substitute(new ArrayList<Object>());
		assertThat(substituted, matchesTables(tables));
	}
	private Tables bodyTables(String a, String b) {
		return TableFactory.tables(TableFactory.table(TableFactory.row(a,b)));
	}
	public void testOneParameter() {
		Tables tables = bodyTables("A","b");
		String[] ss = {"A"};
		ParameterBinder macro = new ParameterBinder(list(ss), tables,"",evaluator);
		List<Object> actualParameterList = actuals("a");
		Tables substituted = macro.substitute(actualParameterList);
		assertThat(substituted, matchesTables(bodyTables("a","b")));
	}
	public void testOneParameterSubstitutedTwice() {
		Tables tables = bodyTables("A","A");
		String[] ss = {"A"};
		ParameterBinder macro = new ParameterBinder(list(ss), tables,"",evaluator);
		Tables substituted = macro.substitute(actuals("a"));
		assertThat(substituted, matchesTables(bodyTables("a","a")));
	}
	public void testTwoParameters() {
		Tables tables = bodyTables("A","B");
		String[] ss = {"A", "B"};
		ParameterBinder macro = new ParameterBinder(list(ss), tables,"",evaluator);
		Tables substituted = macro.substitute(actuals("a","b"));
		assertThat(substituted, matchesTables(bodyTables("a","b")));
	}
	public void testNoDoubleSubstitutions() {
		Tables tables = bodyTables("A","B");
		String[] ss = {"A", "B"};
		ParameterBinder macro = new ParameterBinder(list(ss), tables,"",evaluator);
		Tables substituted = macro.substitute(actuals("B","b"));
		assertThat(substituted, matchesTables(bodyTables("B","b")));
	}
	private List<Object> actuals(String... ss) {
		return list((Object[])ss);
	}
	private <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
	private TablesMatcher matchesTables(Tables expected) {
		return new TablesMatcher(expected,new GlobalDynamicVariables());
	}
}
