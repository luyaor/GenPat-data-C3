/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import junit.framework.TestCase;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.runtime.RuntimeContextImplementation;

public class TestDynamicVariables extends TestCase {
	private RuntimeContextInternal varEmpty;
	private RuntimeContextInternal varFull;
	
	@Override
	public void setUp() {
		varEmpty = new RuntimeContextImplementation();
		String[] vars = { "a","A",
				"b","B" };
		varFull = new RuntimeContextImplementation(vars);
	}
	public void testEmptyString() {
		assertEquals("", varEmpty.dynamicVariables().resolve(""));
		assertEquals("", varFull.dynamicVariables().resolve(""));
	}
	public void testMatchSingle() {
		assertEquals("@{a}", varEmpty.dynamicVariables().resolve("@{a}"));
		assertEquals("A", varFull.dynamicVariables().resolve("@{a}"));
	}
	public void testMatchDouble() {
		assertEquals("@{a}@{b}", varEmpty.dynamicVariables().resolve("@{a}@{b}"));
		assertEquals("AB", varFull.dynamicVariables().resolve("@{a}@{b}"));
	}
	public void testInfinite() {
		varFull.dynamicVariables().put("a", "@{a}");
		assertEquals("INFINITE SUBSTITUTION!", varFull.dynamicVariables().resolve("@{a}"));
	}
	public void testInfinite2() {
		varFull.dynamicVariables().put("a", "@{a}A");
		assertEquals("INFINITE SUBSTITUTION!", varFull.dynamicVariables().resolve("@{a}"));
	}
	public void testDoubleSubstitution() {
		varFull.dynamicVariables().put("a", "@{b}");
		assertEquals("BBBBBBB", varFull.dynamicVariables().resolve("@{a}@{a}@{a}@{a}@{a}@{a}@{a}"));
	}
	public void testFourSubstitutions() {
		varFull.dynamicVariables().put("a", "@{b}A");
		varFull.dynamicVariables().put("b", "@{c}B");
		varFull.dynamicVariables().put("c", "@{d}C");
		varFull.dynamicVariables().put("d", "D");
		assertEquals("DCBA", varFull.dynamicVariables().resolve("@{a}"));
	}
	public void testMatchSingleA() {
		assertEquals("@{@{a}}", varEmpty.dynamicVariables().resolve("@{@{a}}"));
		varFull.dynamicVariables().put("a", "b");
		assertEquals("B", varFull.dynamicVariables().resolve("@{@{a}}"));
	}
	public void testMatchOutOfSystemProperties() {
		System.getProperties().put("a", "A");
		assertEquals("A", new RuntimeContextImplementation().dynamicVariables().resolve("@{a}"));
	}
}
