package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

import junit.framework.Test;

public class AutoBoxingTest extends AbstractComparisonTest {

	public AutoBoxingTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
//	}
	public static Test suite() {
		return buildTestSuite(testClass());
	}
	
	public static Class testClass() {
		return AutoBoxingTest.class;
	}

	public void test001() { // constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(1);\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test((byte)127);\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test('b');\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(-0.0f);\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(0.0);\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Long.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Short.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(false);\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test002() { // non constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static byte bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static char bar() {return 'c';}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static float bar() {return 0.0f;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static double bar() {return 0.0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static long bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static short bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean bar() {return true;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test003() { // Number -> base type
		// Integer -> int
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Integer(1));\n" +
				"	}\n" +
				"	public static void test(int i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Character -> char
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Character('c'));\n" +
				"	}\n" +
				"	public static void test(char c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Float -> float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Float(0.0f));\n" +
				"	}\n" +
				"	public static void test(float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Double -> double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Double(0.0));\n" +
				"	}\n" +
				"	public static void test(double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Long -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Long(0L));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Short -> short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Short((short) 0));\n" +
				"	}\n" +
				"	public static void test(short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Boolean -> boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Boolean.TRUE);\n" +
				"	}\n" +
				"	public static void test(boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test004() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test005() { // this is NOT an ambiguous case as 'long' is matched before autoboxing kicks in
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i) { System.out.print('n'); }\n" +
				"	void test(long i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test006() {
		this.runNegativeTest( // Integers are not compatible with Longs, even though ints are compatible with longs
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test(1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Long, int) in the type Y is not applicable for the arguments (int, int)\n" + 
			"----------\n"
			// test(java.lang.Long,int) in Y cannot be applied to (int,int)
		);
		this.runNegativeTest( // likewise with Byte and Integer
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test((byte) 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test((byte) 1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) in the type Y is not applicable for the arguments (byte, int)\n" + 
			"----------\n"
			// test(java.lang.Integer,int) in Y cannot be applied to (byte,int)
		);
	}

	public void test007() {
		this.runConformTest( // this is NOT an ambiguous case as Long is not a match for int
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"	void test(long i, Integer j) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test008() { // test autoboxing AND varargs method match
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1, new Integer(2), -3);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(int ... i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test009() {
		this.runNegativeTest( // 2 of these sends are ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" + // reference to test is ambiguous, both method test(java.lang.Integer,int) in Y and method test(int,java.lang.Integer) in Y match
				"		new Y().test(new Integer(1), new Integer(1));\n" + // reference to test is ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) {}\n" +
				"	void test(int i, Integer j) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test(1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new Y().test(new Integer(1), new Integer(1));\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) is ambiguous for the type Y\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Integer(1), 1);\n" +
				"		new Y().test(1, new Integer(1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print(1); }\n" +
				"	void test(int i, Integer j) { System.out.print(2); }\n" +
				"}\n",
			},
			"12"
		);
	}

	public void test010() { // local declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int i = Y.test();\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Object o = Y.test();\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test011() { // field declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int i = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Object o = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test012() { // varargs and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer x = new Integer(15); \n" +
				"		int y = 32;\n" +
				"		System.out.printf(\"%x + %x\", x, y);\n" +
				"	}\n" +
				"}",
			},
			"f + 20"
		);
	}

	public void test013() { // foreach and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
				"		for (final Integer e : tab) {\n" + 
				"			System.out.print(e);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"123456789"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer[] tab = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
				"		for (final int e : tab) {\n" + 
				"			System.out.print(e);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"123456789"
		);
	}

	public void test014() { // switch
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		switch(i) {\n" +
				"			case 1 : System.out.print('y');\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test015() { // return statement
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static Integer foo1() {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	static int foo2() {\n" + 
				"		return new Integer(0);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(foo1());\n" + 
				"		System.out.println(foo2());\n" + 
				"	}\n" + 
				"}\n",
			},
			"00"
		);
	}

	public void test016() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = args.length == 0 ? 0 : new Integer(1);\n" + 
				"		System.out.println(i);\n" + 
				"	}\n" + 
				"}\n",
			},
			"0"
		);
	}

	public void test017() { // cast expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = new Integer(1);\n" + 
				"		System.out.println((int)i);\n" + 
				"	}\n" + 
				"}\n",
			},
			"1"
		);
	}

	public void test018() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Float f = args.length == 0 ? new Float(0) : 0;\n" + 
				"		System.out.println((int)f);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	System.out.println((int)f);\n" + 
			"	                   ^^^^^^\n" + 
			"Cannot cast from Float to int\n" + 
			"----------\n"
		);
	}

	public void test019() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println((Integer) 0);\n" + 
				"		System.out.println((Float) 0);\n" + 
				"		\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	System.out.println((Float) 0);\n" + 
			"	                   ^^^^^^^^^\n" + 
			"Cannot cast from int to Float\n" + 
			"----------\n"
		);
	}

	public void test020() { // binary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"      System.out.println(2 + b);\n" + 
				"    }\n" + 
				"}\n",
			},
			"3"
		);
	}

	public void test021() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    Integer i = +b + (-b);\n" + 
				"		System.out.println(i);\n" + 
				"    }\n" + 
				"}\n",
			},
			"0"
		);
	}

	public void test022() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    Integer i = 0;\n" + 
				"	    int n = b + i;\n" + 
				"		System.out.println(n);\n" + 
				"    }\n" + 
				"}\n",
			},
			"1"
		);
	}

	public void test023() { // 78849
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Character cValue = new Character('c');\n" + 
				"		if ('c' == cValue) System.out.println('y');\n" + 
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test024() { // 79254
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) { test(2); }\n" +
				"	static void test(Object o) { System.out.println('y'); }\n" + 
				"}\n",
			},
			"y"
		);
	}

	public void test025() { // 79641
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) { test(true); }\n" +
				"	static void test(Object ... o) { System.out.println('y'); }\n" + 
				"}\n",
			},
			"y"
		);
	}
	
	public void test026() { // compound assignment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    Integer i = 0;\n" + 
				"	    i += b;\n" + 
				"		System.out.println(i);\n" + 
				"    }\n" + 
				"}\n",
			},
			"1"
		);
	}			
	
	public void test027() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		if (0 == new X()) {\n" + 
				"			System.out.println();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	if (0 == new X()) {\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Incompatible operand types int and X\n" + 
			"----------\n"
		);
	}
	
	public void test028() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    int i = +b;\n" + 
				"		System.out.println(i);\n" + 
				"    }\n" + 
				"}\n",
			},
			"1"
		);
	}
	
	public void test029() { // generic type case
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	sum += iterator.next();\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}
	
	public void test030() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = Boolean.TRUE;\n" +
				"		\n" +
				"		if (b && !b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"ELSE"
		);
	}
	
	public void test031() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean foo() { return Boolean.FALSE; }\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = foo();\n" +
				"		\n" +
				"		if (!b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"THEN"
		);
	}
	
	public void test032() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] s) {\n" +
				"      if (new Integer(1) == new Integer(0)) {\n" +
				"         System.out.println();\n" +
				"      }\n" +
				"      System.out.print(\"SUCCESS\");\n" +
				"   }\n" +
				"}",
			},
			"SUCCESS"
		);
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 4, Locals: 1\n" + 
			"  public static void main(String[] s);\n" + 
			"     0  new #17 java/lang/Integer\n" + 
			"     3  dup\n" + 
			"     4  iconst_1\n" + 
			"     5  invokespecial #20 <Method java/lang/Integer.<init>(I)V>\n" + 
			"     8  new #17 java/lang/Integer\n" + 
			"    11  dup\n" + 
			"    12  iconst_0\n" + 
			"    13  invokespecial #20 <Method java/lang/Integer.<init>(I)V>\n" + 
			"    16  if_acmpne 25\n" + 
			"    19  getstatic #26 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
			"    22  invokevirtual #31 <Method java/io/PrintStream.println()V>\n" + 
			"    25  getstatic #26 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
			"    28  ldc #33 <String \"SUCCESS\">\n" + 
			"    30  invokevirtual #37 <Method java/io/PrintStream.print(Ljava/lang/String;)V>\n" + 
			"    33  return\n";
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
	public void test033() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] s) {\n" +
				"      System.out.print(Boolean.TRUE || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"true"
		);
	}
	
	public void test034() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    int i = b++;\n" + 
				"		System.out.print(i);\n" + 
				"		System.out.print(b);\n" + 
				"    }\n" + 
				"}\n",
			},
			"12"
		);
	}
	
	public void test035() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    int i = b--;\n" + 
				"		System.out.print(i);\n" + 
				"		System.out.print(b);\n" + 
				"    }\n" + 
				"}\n",
			},
			"10"
		);
	}
	
	public void test036() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    int i = ++b;\n" + 
				"		System.out.print(i);\n" + 
				"		System.out.print(b);\n" + 
				"    }\n" + 
				"}\n",
			},
			"22"
		);
	}
	
	public void test037() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Byte b = new Byte((byte)1);\n" + 
				"	    int i = --b;\n" + 
				"		System.out.print(i);\n" + 
				"		System.out.print(b);\n" + 
				"    }\n" + 
				"}\n",
			},
			"00"
		);
	}
	
	public void test038() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean foo() { return false; }\n" +
				"   public static void main(String[] s) {\n" +
				"		boolean b = foo();\n" +
				"      System.out.print(b || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"false"
		);
	}
	
	public void test039() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int i = 0;\n" + 
				"		if (i != null) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	if (i != null) {\n" + 
			"	    ^^^^^^^^^\n" + 
			"The operator != is undefined for the argument type(s) int, null\n" + 
			"----------\n"
		);
	}	
	
	public void test040() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		if (i == null)\n" +
				"			i++;\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);	
	}
	
	public void test041() { // equal expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = 0;\n" + 
				"		if (i != null) {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS"
		);
	}	

	public void test042() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean bar() { return Boolean.TRUE; } \n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = bar() ? new Integer(1) : null;\n" +
				"		int j = i;\n" +
				"		System.out.print(j);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);
	}

	public void test043() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = 0;\n" + 
				"		i += \"aaa\";\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	i += \"aaa\";\n" + 
			"	^^^^^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) Integer, String\n" + 
			"----------\n");
	}

	public void test044() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = 0;\n" + 
				"		i += null;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	i += null;\n" + 
			"	^^^^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) Integer, null\n" + 
			"----------\n");
	}

	public void test045() { // binary expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Integer i = 0;\n" + 
				"		i = i + null;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	i = i + null;\n" + 
			"	    ^^^^^^^^\n" + 
			"The operator + is undefined for the argument type(s) Integer, null\n" + 
			"----------\n");
	}
	
	public void test046() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		Byte b = new Byte((byte)1);\n" + 
				"		b++;\n" + 
				"		System.out.println((Byte)b);\n" + 
				"	}\n" + 
				"}\n",
			},
			"2");
	}	

	public void test047() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		Byte b = new Byte((byte)1);\n" + 
				"		b++;\n" + 
				"		if (b instanceof Byte) {\n" + 
				"			System.out.println(\"SUCCESS\" + b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS2");
	}
	
	public void test048() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" + 
				"		b++;\n" + 
				"		if (b instanceof Byte) {\n" + 
				"			System.out.print(\"SUCCESS\" + b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS2");
	}
	
	public void test049() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static class Y {\n" + 
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" + 
				"		X.Y.b++;\n" + 
				"		if (X.Y.b instanceof Byte) {\n" + 
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS2");
	}

	public void test050() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" + 
				"		++b;\n" + 
				"		if (b instanceof Byte) {\n" + 
				"			System.out.print(\"SUCCESS\" + b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS2");
	}
	
	public void test051() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static class Y {\n" + 
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" + 
				"		++X.Y.b;\n" + 
				"		if (X.Y.b instanceof Byte) {\n" + 
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS2");
	}

	public void test052() { // boxing in var decl
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		Byte b = 0;\n" + 
				"		++b;\n" + 
				"		foo(0);\n" + 
				"	}\n" + 
				"	static void foo(Byte b) {\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	foo(0);\n" + 
			"	^^^\n" + 
			"The method foo(Byte) in the type X is not applicable for the arguments (int)\n" + 
			"----------\n");
	}	
	
	public void test053() { // boxing in var decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		Byte b = 1;\n" + 
				"		++b;\n" + 
				"		if (b instanceof Byte) {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	
	public void test054() { // boxing in field decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static Byte b = 1;\n" + 
				"	public static void main(String[] s) {\n" + 
				"		++b;\n" + 
				"		if (b instanceof Byte) {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test055() { // boxing in foreach
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		byte[] bytes = {0, 1, 2};\n" + 
				"		for(Integer i : bytes) {\n" + 
				"			System.out.print(i);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	for(Integer i : bytes) {\n" + 
			"	                ^^^^^\n" + 
			"Type mismatch: cannot convert from element type byte to Integer\n" + 
			"----------\n");
	}	
	
	public void test056() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		int[] ints = {0, 1, 2};\n" + 
				"		for(Integer i : ints) {\n" + 
				"			System.out.print(i);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"012");
	}		
	
	public void test057() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		byte[] bytes = {0, 1, 2};\n" + 
				"		for(Byte b : bytes) {\n" + 
				"			System.out.print(b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"012");
	}		
	
	public void test058() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Integer> list = new ArrayList<Integer>();\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i);\n" + 
				"	    }\n" + 
				"	    int sum = 0;\n" + 
				"	    for (Integer i : list) {\n" + 
				"	    	sum += i;\n" + 
				"	    }	    \n" + 
				"        System.out.print(sum);\n" + 
				"    }\n" + 
				"}\n",
			},
			"10");
	}
	
	public void test059() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Integer> list = new ArrayList<Integer>();\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i);\n" + 
				"	    }\n" + 
				"	    int sum = 0;\n" + 
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	if (1 == iterator.next()) {\n" + 
				"	    		System.out.println(\"SUCCESS\");\n" + 
				"	    		break;\n" + 
				"	    	}\n" + 
				"	    }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test060() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Boolean> list = new ArrayList<Boolean>();\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i % 2 == 0);\n" + 
				"	    }\n" + 
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	if (iterator.next()) {\n" + 
				"	    		System.out.println(\"SUCCESS\");\n" + 
				"	    		break;\n" + 
				"	    	}\n" + 
				"	    }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	
	public void test061() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Boolean> list = new ArrayList<Boolean>();\n" + 
				"		boolean b = true;\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add((i % 2 == 0) && b);\n" + 
				"	    }\n" + 
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	if (iterator.next()) {\n" + 
				"	    		System.out.println(\"SUCCESS\");\n" + 
				"	    		break;\n" + 
				"	    	}\n" + 
				"	    }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	
	public void test062() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Integer> list = new ArrayList<Integer>();\n" + 
				"		boolean b = true;\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i);\n" + 
				"	    }\n" + 
				"		int sum = 0;\n" + 
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	sum = sum + iterator.next();\n" + 
				"	    }\n" + 
				"	    System.out.println(sum);\n" + 
				"    }\n" + 
				"}\n",
			},
			"10");
	}
	
	public void test063() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Integer> list = new ArrayList<Integer>();\n" + 
				"		boolean b = true;\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i);\n" + 
				"	    }\n" + 
				"		int val = 0;\n" + 
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	val = ~ iterator.next();\n" + 
				"	    }\n" + 
				"	    System.out.println(val);\n" + 
				"    }\n" + 
				"}\n",
			},
			"-5");
	}		
	
	public void test064() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Integer> list = new ArrayList<Integer>();\n" + 
				"		boolean b = true;\n" + 
				"		for (int i = 0; i < 5; i++) {\n" + 
				"			list.add(i);\n" + 
				"	    }\n" + 
				"		int val = 0;\n" + 
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" + 
				"	    	val += (int) iterator.next();\n" + 
				"	    }\n" + 
				"	    System.out.println(val);\n" + 
				"    }\n" + 
				"}\n",
			},
			"10");
	}
	
	public void test065() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}
	
	public void test066() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] tab = new Integer[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}
	
	public void test067() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int[] tab = new int[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}
	
	public void test068() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}
	
	public void test069() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Boolean bool = true;\n" + 
				"		assert bool : \"failed\";\n" + 
				"	    System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test070() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" + 
				"		lb.add(true);\n" + 
				"		Iterator<Boolean> iterator = lb.iterator();\n" + 
				"		assert iterator.next() : \"failed\";\n" + 
				"	    System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test071() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" + 
				"		lb.add(true);\n" + 
				"		Iterator<Boolean> iterator = lb.iterator();\n" + 
				"		assert args != null : iterator.next();\n" + 
				"	    System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	
}