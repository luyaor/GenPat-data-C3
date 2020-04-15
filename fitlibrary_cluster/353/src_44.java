/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fitlibrary.DoFixture;
import fitlibrary.definedAction.ParameterSubstitution;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.CollectionUtility;

public class TestMacroSubstitution extends TestCase {
	DoFixture evaluator = new DoFixture();
	public void testNoParameters() {
		Tables tables = bodyTables("a","b");
		ParameterSubstitution macro = new ParameterSubstitution(new ArrayList<String>(), tables,"");
		Tables substituted = macro.substitute(new ArrayList<Object>());
		assertEquals(tables,substituted);
	}
	private Tables bodyTables(String a, String b) {
		return new Tables(new Table(new Row(a,b)));
	}
	public void testOneParameter() {
		Tables tables = bodyTables("A","b");
		String[] ss = {"A"};
		ParameterSubstitution macro = new ParameterSubstitution(list(ss), tables,"");
		List<Object> actualParameterList = actuals("a");
		Tables substituted = macro.substitute(actualParameterList);
		assertEquals(bodyTables("a","b"),substituted);
	}
	public void testOneParameterSubstitutedTwice() {
		Tables tables = bodyTables("A","A");
		String[] ss = {"A"};
		ParameterSubstitution macro = new ParameterSubstitution(list(ss), tables,"");
		Tables substituted = macro.substitute(actuals("a"));
		assertEquals(bodyTables("a","a"),substituted);
	}
	public void testTwoParameters() {
		Tables tables = bodyTables("A","B");
		String[] ss = {"A", "B"};
		ParameterSubstitution macro = new ParameterSubstitution(list(ss), tables,"");
		Tables substituted = macro.substitute(actuals("a","b"));
		assertEquals(bodyTables("a","b"),substituted);
	}
	public void testNoDoubleSubstitutions() {
		Tables tables = bodyTables("A","B");
		String[] ss = {"A", "B"};
		ParameterSubstitution macro = new ParameterSubstitution(list(ss), tables,"");
		Tables substituted = macro.substitute(actuals("B","b"));
		assertEquals(bodyTables("B","b"),substituted);
	}
	private List<Object> actuals(String... ss) {
		return list((Object[])ss);
	}
	private <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
}
