/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import junit.framework.TestCase;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.runtime.RuntimeContextContainer;

public class TestDynamicVariables extends TestCase {
	private RuntimeContextInternal varEmpty;
	private RuntimeContextInternal varFull;
	
	@Override
	public void setUp() {
		varEmpty = new RuntimeContextContainer();
		String[] vars = { "a","A",
				"b","B" };
		varFull = new RuntimeContextContainer(vars);
	}
	public void testEmptyString() {
		assertEquals("", varEmpty.getDynamicVariables().resolve(""));
		assertEquals("", varFull.getDynamicVariables().resolve(""));
	}
	public void testMatchSingle() {
		assertEquals("@{a}", varEmpty.getDynamicVariables().resolve("@{a}"));
		assertEquals("A", varFull.getDynamicVariables().resolve("@{a}"));
	}
	public void testMatchDouble() {
		assertEquals("@{a}@{b}", varEmpty.getDynamicVariables().resolve("@{a}@{b}"));
		assertEquals("AB", varFull.getDynamicVariables().resolve("@{a}@{b}"));
	}
	public void testInfinite() {
		varFull.getDynamicVariables().put("a", "@{a}");
		assertEquals("INFINITE SUBSTITUTION!", varFull.getDynamicVariables().resolve("@{a}"));
	}
	public void testInfinite2() {
		varFull.getDynamicVariables().put("a", "@{a}A");
		assertEquals("INFINITE SUBSTITUTION!", varFull.getDynamicVariables().resolve("@{a}"));
	}
	public void testDoubleSubstitution() {
		varFull.getDynamicVariables().put("a", "@{b}");
		assertEquals("BBBBBBB", varFull.getDynamicVariables().resolve("@{a}@{a}@{a}@{a}@{a}@{a}@{a}"));
	}
	public void testFourSubstitutions() {
		varFull.getDynamicVariables().put("a", "@{b}A");
		varFull.getDynamicVariables().put("b", "@{c}B");
		varFull.getDynamicVariables().put("c", "@{d}C");
		varFull.getDynamicVariables().put("d", "D");
		assertEquals("DCBA", varFull.getDynamicVariables().resolve("@{a}"));
	}
	public void testMatchSingleA() {
		assertEquals("@{@{a}}", varEmpty.getDynamicVariables().resolve("@{@{a}}"));
		varFull.getDynamicVariables().put("a", "b");
		assertEquals("B", varFull.getDynamicVariables().resolve("@{@{a}}"));
	}
	public void testMatchOutOfSystemProperties() {
		System.getProperties().put("a", "A");
		assertEquals("A", new RuntimeContextContainer().getDynamicVariables().resolve("@{a}"));
	}
}
