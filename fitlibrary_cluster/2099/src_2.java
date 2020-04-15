/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fitlibrary.DoFixture;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.ref.EntityReference;
import fitlibrary.runtime.RuntimeContextImplementation;
import fitlibrary.table.Cell;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class TestReferenceParser extends TestCase {
    List<MyClass> list;
	public MyClass aProp;
	
	@Override
	public void setUp() {
		list = new ArrayList<MyClass>();
		list.add(new MyClass(1));
		list.add(new MyClass(2));
		list.add(new MyClass(3));
        aProp = list.get(1);
	}
	public void testParseAlone() throws Exception {
		Parser parser = Traverse.asTyped(MyClass.class).parser(new MyDoFixture());
        Object first = list.get(0);
        checkReference(parser, "the", first);
        checkReference(parser, "the first", first);
        checkReference(parser, "the first MyClass", first);
        checkReference(parser, "the first myClass", first);
        checkReference(parser, "1st", first);
        assertEquals("the second MyClass",parser.show(list.get(1)));
        assertEquals("the third MyClass",parser.show(list.get(2)));
	}
    private void checkReference(Parser adapter, String text, Object element) throws Exception {
        Cell cell = new Cell(text);
        TestResults testResults = new TestResults();
		assertEquals(element,adapter.parseTyped(cell,testResults).getSubject());
        assertTrue(adapter.matches(cell, element,testResults));
        assertEquals("the first MyClass",adapter.show(element));
    }
    public void testParseFails() throws Exception {
        Parser parser = Traverse.asTyped(MyClass.class).parser(new MyDoFixture());
        checkReferenceFails(parser, "th");
        checkReferenceFails(parser, "the forst");
        checkReferenceFails(parser, "the first My Class");
        checkReferenceFails(parser, "the first myClass.");
        checkReferenceFails(parser, "2nd");
    }
    private void checkReferenceFails(Parser adapter, String text) {
        try {
            Cell cell = new Cell(text);
            adapter.parseTyped(cell,new TestResults());
            fail("Should throw and exception with '"+text+"'");
        } catch (Exception e) {
        	//
        }
    }
	public void testParseWithMethod() throws Exception {
		Method method = getClass().getMethod("aMethod", new Class[] {});
		ResultParser adapter = Traverse.asTypedObject(this).resultParser(new MyDoFixture(), method);
		adapter.setTarget(this);
		assertEquals(list.get(2),adapter.getResult());
		assertEquals("the third MyClass",adapter.show(adapter.getResult()));
	}
	public MyClass aMethod() {
		return list.get(2);
	}
	public class MyDoFixture extends DoFixture {
		public MyDoFixture() {
			setRuntimeContext(new RuntimeContextImplementation());
		}
		public MyClass findMyClass(int index) {
            return list.get(index);
        }
        public MyClass findMyClass(String text) {
            if ("1st".equals(text))
                return findMyClass(0);
            throw new RuntimeException("Unavailable: MyClass from '"+text+"'");
        }
        public String showMyClass(MyClass object) {
            return EntityReference.reference(object,list);
        }
    	public MyClass aMethod() {
    		return list.get(2);
    	}
	}
	public static class MyClass {
		private int value;

		public MyClass(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
        @Override
		public String toString() {
            return "MyClass-"+value;
        }
	}
}
