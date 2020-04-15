/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.collection;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import fitlibrary.DoFixture;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.table.Cell;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class TestArrayParser extends TestCase {
	public int[] aProp = {5,6,7};

	public void testAdapterAlone() throws Exception {
		int[] ints = {1,2,3};
		Parser parser = Traverse.asTyped(ints).parser(new DoFixture());
		String cellText = "1, 2, 3";
		Cell cell = new Cell(cellText);
		int[] expectedResult = {1,2,3};
		assertArrayEquals(expectedResult,(int[])parser.parseTyped(cell,new TestResults()).getSubject());
		assertTrue(parser.matches(cell, expectedResult,new TestResults()));
		assertEquals(cellText,parser.show(expectedResult));
	}
	private void assertArrayEquals(int[] expectedResult, int[] actual) {
		assertEquals(expectedResult.length,actual.length);
		for (int i = 0; i < actual.length; i++)
			assertEquals("Element #"+i,expectedResult[i],actual[i]);
	}
	public void testAdapterWithMethod() throws Exception {
		int[] ints = {4,5,6};
		Method method = getClass().getMethod("aMethod", new Class[] {});
		ResultParser adapter = Traverse.asTypedObject(this).resultParser(new DoFixture(), method);
		adapter.setTarget(this);
		Object actual = adapter.getResult();
		assertEquals(ints.getClass(),actual.getClass());
		assertArrayEquals(ints,(int[])actual);
		assertEquals("4, 5, 6",adapter.show(adapter.getResult()));
	}
	public int[] aMethod() {
		return new int[] {4,5,6};
	}
	public void testAdapterWithProperty() throws Exception {
		int[] ints = {5,6,7};
		Parser adapter = Traverse.asTyped(ints).parser(new DoFixture());
		assertEquals("5, 6, 7",adapter.show(ints));
		Object parse = adapter.parseTyped(new Cell("5,6,7"), new TestResults()).getSubject();
		int[] results = (int[]) parse;
		assertArrayEquals(results,ints);
	}
}
