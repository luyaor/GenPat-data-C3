/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *							bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *                          Bug 384687 - [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
 *     Stephan Herrmann - Contribution for
 *							bug 404649 - [1.8][compiler] detect illegal reference to indirect or redundant super via I.super.m() syntax
 *							bug 404728 - [1.8]NPE on QualifiedSuperReference error
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
public class NegativeLambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testSuperReference03"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public NegativeLambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382818, ArrayStoreException while compiling lambda
public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = () -> {\n" +
				"      int z = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I i = () -> {\n" + 
			"      int z = 10;\n" + 
			"    };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				" void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = (p, q) -> {\n" +
				"      int r = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				" void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = null, i2 = (p, q) -> {\n" +
				"      int r = 10;\n" +
				"    }, i3 = null;\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on syntactically valid lambda expression
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX {\n" +
				"    public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"     IX i = () -> 42;\n" +
				"     int x\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	IX i = () -> 42;\n" + 
			"	             ^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	int x\n" + 
			"	    ^\n" + 
			"Syntax error, insert \";\" to complete FieldDeclaration\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383085 super::identifier not accepted.
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX{\n" +
				"	public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	IX i = super::toString;\n" +
				"   Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on *syntactically* valid reference expression
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface One{}\n" +
				"interface Two{}\n" +
				"interface Three{}\n" +
				"interface Four{}\n" +
				"interface Five{}\n" +
				"interface Blah{}\n" +
				"interface Outer<T1,T2>{interface Inner<T3,T4>{interface Leaf{ <T> void method(); } } }\n" +
				"interface IX{\n" +
				"	public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" +
				"   int x\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The member type Outer<One,Two>.Inner cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Outer<One,Two>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Outer.Inner.Deeper cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 12)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                                                       ^^^\n" + 
			"Six cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 13)\n" + 
			"	int x\n" + 
			"	    ^\n" + 
			"Syntax error, insert \";\" to complete FieldDeclaration\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383096, NullPointerException with a wrong lambda code snippet
public void _test007() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {}\n" +
					"public class X {\n" +
					"    void foo() {\n" +
					"            I t1 = f -> {{};\n" +
					"            I t2 = () -> 42;\n" +
					"        } \n" +
					"        }\n" +
					"}\n",
				},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int\n" + 
			"	^^^\n" + 
			"Syntax error on token \"int\", delete this token\n" + 
			"----------\n" /* expected compiler log */,
			true /* perform statement recovery */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test008() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(X x);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i = (X this) -> 10;  \n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	I i = (X this) -> 10;  \n" + 
				"	         ^^^^\n" + 
				"Lambda expressions cannot declare a this parameter\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test009() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.awt.event.ActionListener;\n" +
					"interface I {\n" +
					"    void doit(String s1, String s2);\n" +
					"}\n" +
					"public class X {\n" +
					"  public void test1(int x) {\n" +
					"    ActionListener al = (public xyz) -> System.out.println(xyz); \n" +
					"    I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	ActionListener al = (public xyz) -> System.out.println(xyz); \n" + 
				"	                            ^^^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter xyz as its type is elided\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" + 
				"	                      ^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter s as its type is elided\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" + 
				"	                                   ^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter t as its type is elided\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test010() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> {\n" +
					"		      return ia.clone();\n" +
					"	      };\n" +
					"	I i2 = int[]::clone;\n" +
					"	Zork z;\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382701, [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expressions.
public void test011() {
	// This test checks that common semantic checks are indeed 
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> {\n" +
					"		Zork z;\n" +  // Error: No such type
					"		unknown = 0;\n;" + // Error: No such variable
					"		int a = 42 + ia;\n" + // Error: int + int[] is wrong 
					"		return ia.clone();\n" +
					"	};\n" +
					"	static void staticLambda() {\n" +
					"		I i = (int [] ia) -> this;\n" + // 'this' is static
					"	}\n" +
					"	I j = array -> {\n" +
					"		int a = array[2] + 3;\n" + // No error, ia must be correctly identifies as int[]
					"		int b = 42 + array;\n" + // Error: int + int[] is wrong - yes it is!
					"		System.out.println(\"i(array) = \" + i.foo(array));\n" + // fields are accessible!
					"		return;\n" + // Error here, expecting Object, not void
					"	};\n" +
					"	Runnable r = () -> { return 42; };\n" + // Runnable.run not expecting return value
					"	void anotherLambda() {\n" +
					"		final int beef = 0;\n" +
					"		I k = (int [] a) -> a.length + beef;\n" + // No error, beef is in scope
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	unknown = 0;\n" + 
				"	^^^^^^^\n" + 
				"unknown cannot be resolved to a variable\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	;		int a = 42 + ia;\n" + 
				"	 		        ^^^^^^^\n" + 
				"The operator + is undefined for the argument type(s) int, int[]\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 12)\n" + 
				"	I i = (int [] ia) -> this;\n" + 
				"	                     ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" +
				"5. ERROR in X.java (at line 16)\n" + 
				"	int b = 42 + array;\n" + 
				"	        ^^^^^^^^^^\n" + 
				"The operator + is undefined for the argument type(s) int, int[]\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 18)\n" + 
				"	return;\n" + 
				"	^^^^^^^\n" + 
				"This method must return a result of type Object\n" +
				"----------\n" + 
				"7. ERROR in X.java (at line 20)\n" + 
				"	Runnable r = () -> { return 42; };\n" + 
				"	                     ^^^^^^^^^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n"
);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test012() {
	// This test checks that common semantic checks are indeed 
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"	static void foo() {\n" +
					"		I i = () -> {\n" +
					"			System.out.println(this);\n" +
					"			I j = () -> {\n" +
					"				System.out.println(this);\n" +
					"				I k = () -> {\n" +
					"					System.out.println(this);\n" +
					"				};\n" +
					"			};\n" +
					"		};\n" +
					"	}\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n"
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test013() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo(Zork z) {\n" +
					"		I i = () -> {\n" +
					"			System.out.println(this);\n" +
					"			I j = () -> {\n" +
					"				System.out.println(this);\n" +
					"				I k = () -> {\n" +
					"					System.out.println(this);\n" +
					"				};\n" +
					"			};\n" +
					"		};\n" +
					"	}\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	void foo(Zork z) {\n" + 
				"	         ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n"
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384595, Reject illegal modifiers on lambda arguments.
public void test014() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int x, int y, int z);	\n" +
					"}\n" +
					"public class X {\n" +
					"     I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                             ^^^^^^^^^\n" + 
				"Undefined cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                              ^^^^^^\n" + 
				"Lambda expression\'s parameter o is expected to be of type int\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                                     ^\n" + 
				"Illegal modifier for parameter o; only final is permitted\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                                                            ^\n" + 
				"Illegal modifier for parameter p; only final is permitted\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                                                                  ^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399534, [1.8][compiler] Lambda parameters must be checked for compatibility with the single abstract method of the functional interface.
public void test015() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.Collection;\n" +
					"import java.util.List;\n" +
					"interface I { void run(int x); }\n" +
					"interface J { void run(int x, String s); }\n" +
					"interface K { void run(Collection<String> jobs); }\n" +
					"class X {\n" +
					"    I i1 = (String y) -> {};\n" +
					"    I i2 = (y) -> {};\n" +
					"    I i3 = y -> {};\n" +
					"    I i4 = (int x, String y) -> {};\n" +
					"    I i5 = (int x) -> {};\n" +
					"    J j1 = () -> {};\n" +
					"    J j2 = (x, s) -> {};\n" +
					"    J j3 = (String x, int s) -> {};\n" +
					"    J j4 = (int x, String s) -> {};\n" +
					"    J j5 = x ->  {};\n" +
					"    K k1 = (Collection l) -> {};\n" +
					"    K k2 = (Collection <Integer> l) -> {};\n" +
					"    K k3 = (Collection <String> l) -> {};\n" +
					"    K k4 = (List <String> l) -> {};\n" +
					"    K k5 = (l) -> {};\n" +
					"    K k6 = l -> {};\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i1 = (String y) -> {};\n" + 
				"	        ^^^^^^\n" + 
				"Lambda expression\'s parameter y is expected to be of type int\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i4 = (int x, String y) -> {};\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 12)\n" + 
				"	J j1 = () -> {};\n" + 
				"	       ^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 14)\n" + 
				"	J j3 = (String x, int s) -> {};\n" + 
				"	        ^^^^^^\n" + 
				"Lambda expression\'s parameter x is expected to be of type int\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 14)\n" + 
				"	J j3 = (String x, int s) -> {};\n" + 
				"	                  ^^^\n" + 
				"Lambda expression\'s parameter s is expected to be of type String\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 16)\n" + 
				"	J j5 = x ->  {};\n" + 
				"	       ^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"7. WARNING in X.java (at line 17)\n" + 
				"	K k1 = (Collection l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 17)\n" + 
				"	K k1 = (Collection l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 18)\n" + 
				"	K k2 = (Collection <Integer> l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 20)\n" + 
				"	K k4 = (List <String> l) -> {};\n" + 
				"	        ^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test016() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  String foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> {};\n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to String\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to String\n" +
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test017() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  Integer foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> {};\n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i2 = () -> \"Hello\";\n" + 
				"	             ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to Integer\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i4 = () -> { return \"Hello\"; };\n" + 
				"	                      ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to Integer\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test018() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  I foo();\n" +
					"}\n" +
					"class P implements I {\n" +
					"   public I foo() { return null; }\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> { return new P(); };\n" +
					"  }\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to I\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = () -> \"Hello\";\n" + 
				"	             ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to I\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to I\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 12)\n" + 
				"	I i4 = () -> { return \"Hello\"; };\n" + 
				"	                      ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to I\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test019() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> System.out.println();\n" +
					"    I i5 = () -> { System.out.println(); };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	               ^^^^^^^^^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test020() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"This method must return a result of type int\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); throw new NullPointerException(); };\n" +
					"    Zork z;\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test022() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  J foo();\n" +
					"}\n" +
					"interface J {\n" +
					"  int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I I = () -> () -> 10;\n" +
					"    Zork z;\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test023() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  J foo();\n" +
					"}\n" +
					"interface J {\n" +
					"  int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I i1 = () -> 10;\n" +
					"    I i2 = () -> { return 10; };\n" +
					"    I i3 = () -> () -> 10;\n" +
					"    I i4 = () -> { return () -> 10; };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	I i1 = () -> 10;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to J\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i2 = () -> { return 10; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to J\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test024() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I3 {\n" +
					"  Object foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I3 i = () -> 42; // Warning: Autoboxing, but casting to Object??\n" +
					"  }\n" +
					"  Object foo(Zork z) {\n" +
					"	  return 42;\n" +
					"  }\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	Object foo(Zork z) {\n" + 
				"	           ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test025() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  String foo();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    I i = () -> 42;\r\n" + 
			"    I i2 = () -> \"Hello, Lambda\";\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = () -> 42;\n" + 
			"	            ^^\n" + 
			"Type mismatch: cannot convert from int to String\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test026() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  String foo();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    I i = () -> {\r\n" +
			"      return 42;\r\n" +
			"    };\r\n" + 
			"    I i2 = () -> {\r\n" +
			"      return \"Hello, Lambda as a block!\";\r\n" +
			"    };\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	return 42;\n" + 
			"	       ^^\n" + 
			"Type mismatch: cannot convert from int to String\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test027() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  int baz();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    I i1 = () -> {\n" + 
			"      System.out.println(\"No return\");\n" + 
			"    }; // Error: Lambda block should return value\n" + 
			"    I i2 = () -> {\n" + 
			"      if (Math.random() < 0.5) return 42;\n" + 
			"    }; // Error: Lambda block doesn't always return a value\n" + 
			"    I i3 = () -> {\n" + 
			"      return 42;\n" + 
			"      System.out.println(\"Dead!\");\n" + 
			"    }; // Error: Lambda block has dead code\n" + 
			"  }\n" + 
			"  public static I doesFlowInfoEscape() {\n" + 
			"    I i1 = () -> {\n" + 
			"      return 42;\n" + 
			"    };\n" + 
			"    return i1; // Must not complain about unreachable code!\n" + 
			"  }\n" + 
			"  public static I areExpresionsCheckedForReturns() {\n" + 
			"    I i1 = () -> 42;  // Must not complain about missing return!\n" + 
			"    return i1;\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i1 = () -> {\n" + 
			"      System.out.println(\"No return\");\n" + 
			"    }; // Error: Lambda block should return value\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This method must return a result of type int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	I i2 = () -> {\n" + 
			"      if (Math.random() < 0.5) return 42;\n" + 
			"    }; // Error: Lambda block doesn\'t always return a value\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This method must return a result of type int\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 14)\n" + 
			"	System.out.println(\"Dead!\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n");
}
// Bug 399979 - [1.8][compiler] Statement expressions should be allowed in non-block lambda body when return type is void (edit) 
public void test028() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"  int data;\n" +
			"  public void main(String[] args) {\n" +
			"    I i1 = () -> data++;\n" +
			"    I i2 = () -> data = 10;\n" +
			"    I i3 = () -> data += 10;\n" +
			"    I i4 = () -> --data;\n" +
			"    I i5 = () -> bar();\n" +
			"    I i6 = () -> new X();\n" +
			"    I i7 = () -> 0;\n" +
			"    I i = () -> 1 + data++;\n" +
			"  }\n" +
			"  int bar() {\n" +
			"	  return 0;\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	I i7 = () -> 0;\n" + 
			"	             ^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	I i = () -> 1 + data++;\n" + 
			"	            ^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n");
}
// Bug 384600 - [1.8] 'this' should not be allowed in lambda/Reference expressions in contexts that don't allow it
public void test029() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"	void doit();\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	static void foo() {\n" +
			"		I i1 = this::zoo;\n" +
			"		I i2 = super::boo;\n" +
			"		I i3 = () -> super.zoo();\n" +
			"		I i4 = () -> this.boo();\n" +
			"	}\n" +
			"	void boo () {\n" +
			"		I i1 = this::zoo;\n" +
			"		I i2 = super::boo;\n" +
			"		I i3 = () -> super.zoo();\n" +
			"		I i4 = () -> this.boo();\n" +
			"	}\n" +
			"	void zoo() {\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	void boo() {\n" +
			"	}\n" +
			"	void zoo() {\n" +
			"	}\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i1 = this::zoo;\n" + 
			"	       ^^^^\n" + 
			"Cannot use this in a static context\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	I i2 = super::boo;\n" + 
			"	       ^^^^^\n" + 
			"Cannot use super in a static context\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	I i3 = () -> super.zoo();\n" + 
			"	             ^^^^^\n" + 
			"Cannot use super in a static context\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	I i4 = () -> this.boo();\n" + 
			"	             ^^^^\n" + 
			"Cannot use this in a static context\n" + 
			"----------\n");
}
// Bug 382713 - [1.8][compiler] Compiler should reject lambda expressions when target type is not a functional interface
public void test030() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"  void goo();\n" +
			"}\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = () -> 10;\n" +
			"    I i = () -> 10;\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	X x = () -> 10;\n" + 
			"	      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	I i = () -> 10;\n" + 
			"	      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// Bug 398267 - [1.8][compiler] Variables in the body of the lambda expression should be valid
public void test031() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"  public void main(String[] args) {\n" +
			"    I i = () -> {\n" +
			"            		p = 10;\n" +
			"            		Zork z = this.blank;\n" +
			"            		super.foo();\n" +
			"            		goo();\n" +
			"           	};\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	p = 10;\n" + 
			"	^\n" + 
			"p cannot be resolved to a variable\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	Zork z = this.blank;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	Zork z = this.blank;\n" + 
			"	              ^^^^^\n" + 
			"blank cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	super.foo();\n" + 
			"	      ^^^\n" + 
			"The method foo() is undefined for the type Object\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 10)\n" + 
			"	goo();\n" + 
			"	^^^\n" + 
			"The method goo() is undefined for the type X\n" + 
			"----------\n");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test032() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface IA {\r\n" + 
			"  void snazz();\r\n" + 
			"}\r\n" + 
			"interface IB {\r\n" + 
			"  void baz() throws java.io.IOException;\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    IA i1 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Error: not declared\n" + 
			"    };\n" + 
			"    IB i2 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Fine: IOException is declared\n" + 
			"    }; // No error, it's all good\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	throw new java.io.EOFException(); // Error: not declared\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EOFException\n" + 
			"----------\n");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test033() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface IA {\r\n" + 
			"  void snazz();\r\n" + 
			"}\r\n" + 
			"interface IB {\r\n" + 
			"  void baz() throws java.io.IOException;\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    IA i1 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Error: not declared\n" + 
			"    };\n" + 
			"    IB i2 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Fine: IOException is declared\n" + 
			"    }; // No error, it's all good\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	throw new java.io.EOFException(); // Error: not declared\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EOFException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test034() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  int foo(int x, int y);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    int x = 2;\r\n" + 
			"    I i = (a, b) -> {\r\n" + 
			"      return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int\r\n" + 
			"    };\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from double to int\n" + 
			"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test035() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> ia.clone();\n" +
					"	I i2 = int[]::clone;\n" +
					"	Zork z;\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727,  Lambda expression parameters and locals cannot shadow variables from context
public void test036() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  void foo(int x, int y);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    int x, y;\r\n" + 
			"    I i = (x, y) -> { // Error: x,y being redeclared\r\n" + 
			"      int args = 10; //  Error args is being redeclared\r\n" + 
			"    };\r\n" + 
			"  }\r\n" + 
			"}"}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I i = (x, y) -> { // Error: x,y being redeclared\n" + 
			"	       ^\n" + 
			"Lambda expression\'s parameter x cannot redeclare another local variable defined in an enclosing scope. \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	I i = (x, y) -> { // Error: x,y being redeclared\n" + 
			"	          ^\n" + 
			"Lambda expression\'s parameter y cannot redeclare another local variable defined in an enclosing scope. \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	int args = 10; //  Error args is being redeclared\n" + 
			"	    ^^^^\n" + 
			"Lambda expression\'s local variable args cannot redeclare another local variable defined in an enclosing scope. \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382702 - [1.8][compiler] Lambda expressions should be rejected in disallowed contexts
public void test037() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  int foo1(String x);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" +
			"    System.out.println(\"Lambda in illegal context: \" + (() -> \"Illegal Lambda\"));\r\n" +
			"    System.out.println(\"Method Reference in illegal context: \" + System::exit);\r\n" +
			"    System.out.println(\"Constructor Reference in illegal context: \" + String::new);\r\n" +
			"    I sam1 = (x) -> x.length(); // OK\r\n" +
//			"    I sam2 = ((String::length)); // OK\r\n" +
//			"    I sam3 = (Math.random() > 0.5) ? String::length : String::hashCode; // OK\r\n" +
//			"    I sam4 = (I)(String::length); // OK\r\n" +
            "    int x = (x) -> 10;\n" +
            "    X x2 = (x) -> 10;\n" +
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	System.out.println(\"Lambda in illegal context: \" + (() -> \"Illegal Lambda\"));\n" + 
			"	                                                   ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(\"Method Reference in illegal context: \" + System::exit);\n" + 
			"	                                                             ^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	System.out.println(\"Constructor Reference in illegal context: \" + String::new);\n" + 
			"	                                                                  ^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	int x = (x) -> 10;\n" + 
			"	        ^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	X x2 = (x) -> 10;\n" + 
			"	       ^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test038() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.EOFException;\n" +
			"import java.io.IOException;\n" +
			"interface I { void m() throws IOException; }\n" +
			"interface J { void m() throws EOFException; }\n" +
			"interface K { void m() throws ClassNotFoundException; }\n" +
			"interface IJ extends I, J {}\n" +
			"interface IJK extends I, J, K {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	IJ ij = () -> {\n" +
			"		if (var == 0) {\n" +
			"			throw new IOException();\n" +
			"		} else if (var == 2) {\n" +
			"			throw new EOFException();\n" +
			"		} else {\n" +
			"			throw new ClassNotFoundException(); \n" +
			"		}\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	throw new IOException();\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 16)\n" + 
			"	throw new ClassNotFoundException(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type ClassNotFoundException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test039() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.EOFException;\n" +
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"import java.sql.SQLTransientException;\n" +
			"import java.util.List;\n" +
			"import java.util.concurrent.TimeoutException;\n" +
			"interface A {\n" +
			"  List<String> foo(List<String> arg) throws IOException, SQLTransientException;\n" +
			"}\n" +
			"interface B {\n" +
			"  List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;\n" +
			"}\n" +
			"interface C {\n" +
			"  List foo(List arg) throws Exception;\n" +
			"}\n" +
			"interface D extends A, B {}\n" +
			"interface E extends A, B, C {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	D d = (x) -> {\n" +
			"		switch (var) {\n" +
			"		case 0 : throw new EOFException();\n" +
			"		case 1: throw new IOException();\n" +
			"		case 2: throw new SQLException();\n" +
			"		case 3: throw new SQLTransientException();\n" +
			"		case 4: throw new TimeoutException();\n" +
			"		default: throw new NullPointerException();\n" +
			"		}\n" +
			"	};\n" +
			"	E e = (x) -> {\n" +
			"		switch (var) {\n" +
			"		case 0 : throw new EOFException();\n" +
			"		case 1: throw new IOException();\n" +
			"		case 2: throw new SQLException();\n" +
			"		case 3: throw new SQLTransientException();\n" +
			"		case 4: throw new TimeoutException();\n" +
			"		default: throw new NullPointerException();\n" +
			"		}\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 11)\n" + 
			"	List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 14)\n" + 
			"	List foo(List arg) throws Exception;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	List foo(List arg) throws Exception;\n" + 
			"	         ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 23)\n" + 
			"	case 1: throw new IOException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 24)\n" + 
			"	case 2: throw new SQLException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type SQLException\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 26)\n" + 
			"	case 4: throw new TimeoutException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type TimeoutException\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 33)\n" + 
			"	case 1: throw new IOException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 34)\n" + 
			"	case 2: throw new SQLException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type SQLException\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 36)\n" + 
			"	case 4: throw new TimeoutException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type TimeoutException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test040() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  <P extends Exception> Object m() throws P;\n" +
			"}\n" +
			"interface J {\n" +
			"  <Q extends Exception> String m() throws Exception;\n" +
			"}\n" +
			"interface G extends I, J {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	G g1 = () -> {\n" +
			"	    throw new Exception(); \n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	G g1 = () -> {\n" + 
			"	    throw new Exception(); \n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type J is generic \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test041() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"interface G1 {\n" +
			"  <E extends Exception> Object m(E p) throws E;\n" +
			"}\n" +
			"interface G2 {\n" +
			"  <F extends Exception> String m(F q) throws Exception;\n" +
			"}\n" +
			"interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F\n" +
			"public class X {\n" +
			"	G g = (x) -> { // Elided type is inferred from descriptor to be F\n" +
			"	    throw x;    // ~== throw new F()\n" +
			"	};\n" +
			"}\n" +
			"class Y implements G {\n" +
			"	public <T extends Exception> String m(T t) throws T {\n" +
			"		throw t;\n" +
			"	}\n" +
			"	void foo(G1 g1) {\n" +
			"			g1.m(new IOException());\n" +
			"	}\n" +
			"	void foo(G2 g2) {\n" +
			"			g2.m(new SQLException());\n" +
			"	}\n" +
			"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	G g = (x) -> { // Elided type is inferred from descriptor to be F\n" + 
			"	    throw x;    // ~== throw new F()\n" + 
			"	};\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 20)\n" + 
			"	g1.m(new IOException());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 23)\n" + 
			"	g2.m(new SQLException());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type Exception\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test042() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"interface G1 {\n" +
			"  <E extends Exception> Object m(E p) throws E;\n" +
			"}\n" +
			"interface G2 {\n" +
			"  <F extends Exception> String m(F q) throws Exception;\n" +
			"}\n" +
			"interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F\n" +
			"public class X {\n" +
			"	G g1 = (F x) -> {\n" +
			"	    throw x;\n" +
			"	};\n" +
			"	G g2 = (IOException x) -> {\n" +
			"	    throw x;\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	G g1 = (F x) -> {\n" + 
			"	    throw x;\n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	G g2 = (IOException x) -> {\n" + 
			"	    throw x;\n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399224 - [1.8][compiler][internal] Implement TypeBinding.getSingleAbstractMethod 
public void test043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.IGNORE);

	this.runNegativeTest(
			new String[] {
			"X.java",
            "import java.util.List;\n" +
			"interface A { void foo(); }\n" +  // yes
			"interface B { boolean equals(Object obj); }\n" + // no
			"interface C extends B { void foo(); }\n" + // yes
			"interface D<T> { boolean equals(Object obj); void foo(); }\n" + // yes
			"interface E { void foo(); Object clone(); }\n" + // no
			"interface F { void foo(List<String> p); }\n" + // yes
            "interface G { void foo(List<String> p); }\n" + // yes
            "interface H extends F, G {}\n" + // yes
            "interface I { List foo(List<String> p); }\n" + // yes
            "interface J { List<String> foo(List arg); }\n" + // yes
            "interface K extends I, J {}\n" + // yes
            "interface L { void foo(List<Integer> p); }\n" +  // yes
            "interface M extends I, L {}\n" + // no
            "interface N { void foo(List<String> p, Class q); }\n" + // yes
            "interface O { void foo(List p, Class<?> q); }\n" + // yes
            "interface P extends N, O {}\n" + // no
            "interface Q { long foo(); }\n" + // yes
            "interface R { int foo(); }\n" + // yes
            "interface S extends Q, R {}\n" + // no
            "interface T<P> { void foo(P p); }\n" + // yes
            "interface U<P> { void foo(P p); }\n" + // yes
            "interface V<P, Q> extends T<P>, U<Q> {}\n" + // no
            "interface W<T, N extends Number> { void m(T arg); void m(N arg); }\n" + // no
            "interface X extends W<String, Integer> {}\n" + // no
            "interface Y extends W<Integer, Integer> {}\n" + // yes

            "class Z {\n" +
            "    A a              =    () -> {};\n" +
            "    B b              =    () -> {};\n" +
            "    C c              =    () -> {};\n" +
            "    D<?> d           =    () -> {};\n" +
            "    E e              =    () -> {};\n" +
            "    F f              =    (p0) -> {};\n" +
            "    G g              =    (p0) -> {};\n" +
            "    H h              =    (p0) -> {};\n" +
            "    I i              =    (p0) -> { return null; };\n" +
            "    J j              =    (p0) -> { return null; };\n" +
            "    K k              =    (p0) -> { return null; };\n" +
            "    L l              =    (p0) -> {};\n" +
            "    M m              =    (p0) -> {};\n" +
            "    N n              =    (p0, q0) -> {};\n" +
            "    O o              =    (p0, q0) -> {};\n" +
            "    P p              =    (p0, q0) -> {};\n" +
            "    Q q              =    () -> { return 0;};\n" +
            "    R r              =    () -> { return 0;};\n" +
            "    S s              =    () -> {};\n" +
            "    T<?> t           =    (p0) -> {};\n" +
            "    U<?> u           =    (p0) -> {};\n" +
            "    V<?,?> v         =    (p0) -> {};\n" +
            "    W<?,?> w         =    (p0) -> {};\n" +
            "    X x              =    (p0) -> {};\n" +
            "    Y y              =    (p0) -> {};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	interface P extends N, O {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(List, Class<?>) of type O has the same erasure as foo(List<String>, Class) of type N but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 20)\n" + 
			"	interface S extends Q, R {}\n" + 
			"	          ^\n" + 
			"The return types are incompatible for the inherited methods Q.foo(), R.foo()\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 23)\n" + 
			"	interface V<P, Q> extends T<P>, U<Q> {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(P) of type U<P> has the same erasure as foo(P) of type T<P> but does not override it\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 29)\n" + 
			"	B b              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 32)\n" + 
			"	E e              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 40)\n" + 
			"	M m              =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 43)\n" + 
			"	P p              =    (p0, q0) -> {};\n" + 
			"	                      ^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 46)\n" + 
			"	S s              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 49)\n" + 
			"	V<?,?> v         =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 50)\n" + 
			"	W<?,?> w         =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 51)\n" + 
			"	X x              =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399224 - [1.8][compiler][internal] Implement TypeBinding.getSingleAbstractMethod 
public void test044() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.IGNORE);

	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.util.List;\n" +
			"interface A { <T> T foo(List<T> p); }\n" +
			"interface B { <S> S foo(List<S> p); }\n" +
			"interface C { <T, S> S foo(List<T> p); }\n" +
			"interface D extends A, B {}\n" +
			"interface E extends A, C {}\n" +

			"class Z {\n" +
	        "    A a              =    (p) -> { return null;};\n" +
	        "    B b              =    (p) -> { return null;};\n" +
	        "    C c              =    (p) -> { return null;};\n" +
	        "    D d              =    (p) -> { return null;};\n" +
	        "    E e              =    (p) -> { return null;};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	interface E extends A, C {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(List<T>) of type C has the same erasure as foo(List<T>) of type A but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	A a              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type A is generic \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	B b              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type B is generic \n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	C c              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type C is generic \n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	D d              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type A is generic \n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 12)\n" + 
			"	E e              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400386 - [1.8][spec] Broken example in 9.8, discussion box - bullet 2 ?
public void test045() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I { Object m(); }\n" +
			"interface J<S> { S m(); }\n" +
			"interface K<T> { T m(); }\n" +
			"interface Functional<S,T> extends I, J<S>, K<T> {}\n" +
			"class X {\n" +
			"    Functional<String,Integer> f = () -> { };\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	interface Functional<S,T> extends I, J<S>, K<T> {}\n" + 
			"	          ^^^^^^^^^^\n" + 
			"The return types are incompatible for the inherited methods I.m(), J<S>.m(), K<T>.m()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	Functional<String,Integer> f = () -> { };\n" + 
			"	                               ^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
public void test046() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.util.List;\n" +
			"interface A { void f(List<String> ls); }\n" +
			"interface B { void f(List<Integer> li); }\n" +
			"interface C extends A,B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	interface C extends A,B {}\n" + 
			"	          ^\n" + 
			"Name clash: The method f(List<Integer>) of type B has the same erasure as f(List<String>) of type A but does not override it\n" + 
			"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test047() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    int var = 2;\n" + 
					"    I x = new I() {\n" + 
					"      public void doit() {\n" + 
					"        System.out.println(args); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"        System.out.println(var); // Error: var is not effectively final\n" + 
					"      }\n" + 
					"    };\n" + 
					"    var=2;\n" + 
					"  }\n" + 
					"}" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	System.out.println(var); // Error: var is not effectively final\n" + 
				"	                   ^^^\n" + 
				"Variable var is required to be final or effectively final\n" + 
				"----------\n"
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test048() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    int var = 2;\n" + 
					"    I x2 = () -> {\n" + 
					"      System.out.println(var); // Error: var is not effectively final\n" + 
					"    };\n" + 
					"    var=2;\n" + 
					"  }\n" + 
					"}" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	System.out.println(var); // Error: var is not effectively final\n" + 
				"	                   ^^^\n" + 
				"Variable var is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test049() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    int var = 2;\n" + 
					"    I x2 = () -> {\n" + 
					"      System.out.println(args); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"    };\n" + 
					"    var=2;\n" + 
					"  }\n" + 
					"}" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test050() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    try {\n" + 
					"      new java.io.File(\"dweep\").getCanonicalPath();\n" + 
					"    } catch (java.io.IOException ioe) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" + 
					"    };\n"+
					"  }\n" +
					"}\n"
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test051() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    java.util.List<String> list = new java.util.ArrayList<>();\n" + 
					"    for (String s : list) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" + 
					"    };\n" + 
					"  }\n" + 
					"\n" +
					"}\n" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test052() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    java.util.List<String> list = new java.util.ArrayList<>();\n" + 
					"    for (String s2 : list) {\n" + 
					"      s2 = \"Nice!\";\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(s2); // Error: var is not effectively final\n" + 
					"      };\n" + 
					"    };\n" + 
					"  }\n" + 
					"\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	System.out.println(s2); // Error: var is not effectively final\n" + 
				"	                   ^^\n" + 
				"Variable s2 is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test053() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo() {\n" + 
					"    try {\n" + 
					"       System.out.println(\"try\");\n" +
					"  } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {\n" + 
					"    I i = () -> {\n" + 
					"      System.out.println(e);\n" + 
					"     };\n" + 
					"    }\n" + 
					"  }\n" +
					"}\n" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test054() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo2(String[] args) {\n" + 
					"   int var;\n" + 
					"   if (args != null)\n" + 
					"      var = args.length;\n" + 
					"   else\n" + 
					"      var = 2;\n" + 
					"   I x = new I() {\n" + 
					"     public void doit() {\n" + 
					"       System.out.println(var);\n" +  // no error here.
					"       args = null;\n" + // error here.
					"     }\n" + 
					"   };\n" + 
					"  }\n" +
					"}\n" ,
				}, 		
				"----------\n" + 
				"1. ERROR in X.java (at line 14)\n" + 
				"	args = null;\n" + 
				"	^^^^\n" + 
				"Variable args is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test055() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo(final int x) {\n" + 
					"    I i = () -> {\n" + 
					"      x = 10;\n" + 
					"     };\n" + 
					"  }\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	x = 10;\n" + 
				"	^\n" + 
				"The final local variable x cannot be assigned. It must be blank and not using a compound assignment\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test056() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo(final int x) {\n" + 
					"    X i = new X() {\n" + 
					"      { x = 10; }\n" + 
					"     };\n" + 
					"  }\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	{ x = 10; }\n" + 
				"	  ^\n" + 
				"The final local variable x cannot be assigned, since it is defined in an enclosing type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test057() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo(int x) {\n" + 
					"    I i = () -> {\n" + 
					"      x = 10;\n" + 
					"     };\n" + 
					"  }\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	x = 10;\n" + 
				"	^\n" + 
				"Variable x is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test058() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo(int x) {\n" + 
					"    X i = new X() {\n" + 
					"      { x = 10; }\n" + 
					"     };\n" + 
					"  }\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	{ x = 10; }\n" + 
				"	  ^\n" + 
				"Variable x is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test059() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"class X {\n" +
					"	void foo(int [] p) {\n" +
					"		for (int is : p) {\n" +
					"			I j = new I () {\n" +
					"				public void foo() {\n" +
					"					System.out.println(is);\n" +
					"				};\n" +
					"			};\n" +
					"		}\n" +
					"	}\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test060() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"class X {\n" +
					"	void foo(int [] p) {\n" +
					"		for (int is : p) {\n" +
					"			I j = () -> {\n" +
					"					System.out.println(is);\n" +
					"			};\n" +
					"		}\n" +
					"	}\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test061() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  void foo2(String[] args) {\n" + 
					"   int var;\n" + 
					"   if (args != null)\n" + 
					"      var = args.length;\n" + 
					"   else\n" + 
					"      var = 2;\n" + 
					"   I x = () ->  {\n" + 
					"       System.out.println(var);\n" +  // no error here.
					"       args = null;\n" + // error here.
					"   };\n" + 
					"  }\n" +
					"}\n" ,
				}, 		
				"----------\n" + 
				"1. ERROR in X.java (at line 13)\n" + 
				"	args = null;\n" + 
				"	^^^^\n" + 
				"Variable args is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test062() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    int var;\n" +
					"    if (args != null) {\n" +
					"       var = args.length;\n" +
					"       I x = new I() {\n" +
					"         public void doit() {\n" +
					"           System.out.println(var);\n" +
					"         }\n" +
					"       };\n" +
					"    } else {\n" +
					"       var = 2; // HERE\n" +
					"    }\n" +
					"  }\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test063() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.io.IOException;\n" +
					"interface I {\n" +
					"    void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) throws IOException {\n" +
					"\n" +
					"	try {\n" +
					"		throw new IOException();\n" +
					"	} catch (Exception e) {\n" +
					"		if (args == null) {\n" +
					"			throw e;\n" +
					"		} \n" +
					"                else {\n" +
					"			e = null;\n" +
					"		}\n" +
					"	}\n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 12)\n" + 
				"	throw e;\n" + 
				"	^^^^^^^^\n" + 
				"Unhandled exception type Exception\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 14)\n" + 
				"	else {\n" + 
				"			e = null;\n" + 
				"		}\n" + 
				"	     ^^^^^^^^^^^^^^^^^^\n" + 
				"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test064() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.io.IOException;\n" +
					"interface I {\n" +
					"    void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) throws IOException {\n" +
					"\n" +
					"	try {\n" +
					"		throw new IOException();\n" +
					"	} catch (Exception e) {\n" +
					"		if (args == null) {\n" +
					"			throw e;\n" +
					"		} \n" +
					"	}\n" +
					"  }\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test065() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"class X {\n" +
					"	void foo() {\n" +
					"		int x = 10;\n" +
					"		I i = () -> {\n" +
					"			System.out.println(x++);\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	System.out.println(x++);\n" + 
				"	                   ^\n" + 
				"Variable x is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test066() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.io.IOException;\n" +
					"class X {\n" +
					"	void foo(int x) throws IOException {\n" +
					"		try {\n" +
					"			throw new IOException();\n" +
					"		} catch (Exception e) {\n" +
					"			if (x == 0) {\n" +
					"				throw e;\n" +
					"			} else {\n" +
					"				e = null;\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	throw e;\n" + 
				"	^^^^^^^^\n" + 
				"Unhandled exception type Exception\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 9)\n" + 
				"	} else {\n" + 
				"				e = null;\n" + 
				"			}\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
				"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test067() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit ();\n" +
					"}\n" +
					"class X {\n" +
					"	int p;\n" +
					"	void foo(int p) {\n" +
					"		int i = 10;\n" +
					"		X x = new X();\n" +
					"		x = new X();\n" +
					"		I l = () -> {\n" +
					"			x.p = i++;\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 6)\n" + 
				"	void foo(int p) {\n" + 
				"	             ^\n" + 
				"The parameter p is hiding a field from type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 11)\n" + 
				"	x.p = i++;\n" + 
				"	      ^\n" + 
				"Variable i is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test068() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit ();\n" +
					"}\n" +
					"class X {\n" +
					"	int p;\n" +
					"	void foo(int p) {\n" +
					"		int i = 10;\n" +
					"		X x = new X();\n" +
					"		x = new X();\n" +
					"		I l = () -> {\n" +
					"			x.p = i;\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 6)\n" + 
				"	void foo(int p) {\n" + 
				"	             ^\n" + 
				"The parameter p is hiding a field from type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 11)\n" + 
				"	x.p = i;\n" + 
				"	^\n" + 
				"Variable x is required to be final or effectively final\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test069() {
	// Lambda argument hides a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p);\n" +
					"}\n" +
					"public class X {\n" +
					"	int f1;\n" +
					"	int f2;\n" +
					"\n" +
					"	void foo() {\n" +
					"		I i = (int f1)  -> {\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i = (int f1)  -> {\n" + 
				"	           ^^\n" + 
				"The parameter f1 is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test070() {
	// Lambda argument redeclares outer method argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p);\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo(int x) {\n" +
					"		I i = (int x)  -> {\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	I i = (int x)  -> {\n" + 
				"	           ^\n" + 
				"Lambda expression\'s parameter x cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test071() {
	// Lambda argument redeclares outer method local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p);\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo(int x) {\n" +
					"       int l;\n" +
					"		I i = (int l)  -> {\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i = (int l)  -> {\n" + 
				"	           ^\n" + 
				"Lambda expression\'s parameter l cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test072() {
	// Lambda redeclares its own argument
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo(int x) {\n" +
					"       int l;\n" +
					"		I i = (int p, int p)  -> {\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i = (int p, int p)  -> {\n" + 
				"	                  ^\n" + 
				"Duplicate parameter p\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test073() {
	// Lambda local hides a field
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo() {\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           int f;\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 8)\n" + 
				"	int f;\n" + 
				"	    ^\n" + 
				"The local variable f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test074() {
	// Lambda local redeclares the enclosing method's argument
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           int a;\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	int a;\n" + 
				"	    ^\n" + 
				"Lambda expression\'s local variable a cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test075() {
	// Lambda local redeclares the enclosing method's local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           int loc;\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	int loc;\n" + 
				"	    ^^^\n" + 
				"Lambda expression\'s local variable loc cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test076() {
	// Lambda local redeclares its own parameter
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           int p;\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	int p;\n" + 
				"	    ^\n" + 
				"Duplicate local variable p\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test077() {
	// Lambda local redeclares its own self
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           int self, self;\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	int self, self;\n" + 
				"	          ^^^^\n" + 
				"Duplicate local variable self\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test078() {
	// Nested Lambda argument redeclares a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = (int f, int p0) -> {};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = (int f, int p0) -> {};\n" + 
				"	            ^\n" + 
				"The parameter f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test079() {
	// Nested Lambda argument redeclares outer method's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = (int f, int outerp) -> {};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = (int f, int outerp) -> {};\n" + 
				"	            ^\n" + 
				"The parameter f is hiding a field from type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i2 = (int f, int outerp) -> {};\n" + 
				"	                   ^^^^^^\n" + 
				"Lambda expression\'s parameter outerp cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test080() {
	// Nested Lambda argument redeclares outer method's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = (int locouter, int outerp) -> {};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	I i2 = (int locouter, int outerp) -> {};\n" + 
				"	            ^^^^^^^^\n" + 
				"Lambda expression\'s parameter locouter cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i2 = (int locouter, int outerp) -> {};\n" + 
				"	                          ^^^^^^\n" + 
				"Lambda expression\'s parameter outerp cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test081() {
	// Nested Lambda argument redeclares outer lambda's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = (int p, int q) -> {};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	I i2 = (int p, int q) -> {};\n" + 
				"	            ^\n" + 
				"Lambda expression\'s parameter p cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i2 = (int p, int q) -> {};\n" + 
				"	                   ^\n" + 
				"Lambda expression\'s parameter q cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test082() {
	// Nested Lambda argument redeclares outer lambda's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = (int lamlocal, int q) -> {};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {};\n" + 
				"	            ^^^^^^^^\n" + 
				"Lambda expression\'s parameter lamlocal cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {};\n" + 
				"	                          ^\n" + 
				"Lambda expression\'s parameter q cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test083() {
	// Nested Lambda local redeclares a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = (int lamlocal, int q) -> {int f;};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int f;};\n" + 
				"	            ^^^^^^^^\n" + 
				"Lambda expression\'s parameter lamlocal cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int f;};\n" + 
				"	                          ^\n" + 
				"Lambda expression\'s parameter q cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int f;};\n" + 
				"	                                     ^\n" + 
				"The local variable f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test084() {
	// Nested Lambda local redeclares outer methods local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = (int lamlocal, int q) -> {int locouter;};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int locouter;};\n" + 
				"	            ^^^^^^^^\n" + 
				"Lambda expression\'s parameter lamlocal cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int locouter;};\n" + 
				"	                          ^\n" + 
				"Lambda expression\'s parameter q cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int lamlocal, int q) -> {int locouter;};\n" + 
				"	                                     ^^^^^^^^\n" + 
				"Lambda expression\'s local variable locouter cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test085() {
	// Nested Lambda local redeclares outer lambda's argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = (int j, int q) -> {int p, lamlocal;};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int j, int q) -> {int p, lamlocal;};\n" + 
				"	                   ^\n" + 
				"Lambda expression\'s parameter q cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int j, int q) -> {int p, lamlocal;};\n" + 
				"	                              ^\n" + 
				"Lambda expression\'s local variable p cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int j, int q) -> {int p, lamlocal;};\n" + 
				"	                                 ^^^^^^^^\n" + 
				"Lambda expression\'s local variable lamlocal cannot redeclare another local variable defined in an enclosing scope. \n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test086() {
	// Nested Lambda local redeclares its own argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = (int x1, int x2) -> {int x1, x2;};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int x1, int x2) -> {int x1, x2;};\n" + 
				"	                                ^^\n" + 
				"Duplicate local variable x1\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = (int x1, int x2) -> {int x1, x2;};\n" + 
				"	                                    ^^\n" + 
				"Duplicate local variable x2\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test087() {
	// Inner class (!) inside Lambda hides field
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           class X { void foo(int f) {} }\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	class X { void foo(int f) {} }\n" + 
				"	      ^\n" + 
				"The nested type X cannot hide an enclosing type\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	class X { void foo(int f) {} }\n" + 
				"	                       ^\n" + 
				"The parameter f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test088() {
	// class inside lambda (!) redeclares a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int a) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = new I() { public void foo(int f, int p0) {};\n" +
					"		};};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo(int f, int p0) {};\n" + 
				"	                                     ^\n" + 
				"The parameter f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test089() {
	// class inside lambda redeclares outer method's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int loc;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = new I() { public void foo(int f, int outerp) {}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo(int f, int outerp) {}};\n" + 
				"	                                     ^\n" + 
				"The parameter f is hiding a field from type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo(int f, int outerp) {}};\n" + 
				"	                                            ^^^^^^\n" + 
				"The parameter outerp is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test090() {
	// class inside lambda redeclares outer method's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = new I() { public void foo(int locouter, int outerp)  {}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo(int locouter, int outerp)  {}};\n" + 
				"	                                     ^^^^^^^^\n" + 
				"The parameter locouter is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo(int locouter, int outerp)  {}};\n" + 
				"	                                                   ^^^^^^\n" + 
				"The parameter outerp is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test091() {
	// class inside lambda redeclares outer lambda's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"           I i2 = new I() { public void foo (int p, int q) {}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo (int p, int q) {}};\n" + 
				"	                                      ^\n" + 
				"The parameter p is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 9)\n" + 
				"	I i2 = new I() { public void foo (int p, int q) {}};\n" + 
				"	                                             ^\n" + 
				"The parameter q is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test092() {
	// class inside lambda redeclares outer lambda's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = new I() { public void foo (int lamlocal, int q)  {} };\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo (int lamlocal, int q)  {} };\n" + 
				"	                                      ^^^^^^^^\n" + 
				"The parameter lamlocal is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo (int lamlocal, int q)  {} };\n" + 
				"	                                                    ^\n" + 
				"The parameter q is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test093() {
	// local of class inside lambda redeclares a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};\n" + 
				"	                                      ^^^^^^^^\n" + 
				"The parameter lamlocal is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};\n" + 
				"	                                                    ^\n" + 
				"The parameter q is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};\n" + 
				"	                                                            ^\n" + 
				"The local variable f is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test094() {
	// local of class under lambda redeclares outer methods local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};\n" + 
				"	                                     ^^^^^^^^\n" + 
				"The parameter lamlocal is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};\n" + 
				"	                                                   ^\n" + 
				"The parameter q is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};\n" + 
				"	                                                           ^^^^^^^^\n" + 
				"The local variable locouter is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test095() {
	// local of class under lambda redeclares outer lambda's argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};\n" + 
				"	                                            ^\n" + 
				"The parameter q is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};\n" + 
				"	                                                    ^\n" + 
				"The local variable p is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};\n" + 
				"	                                                       ^^^^^^^^\n" + 
				"The local variable lamlocal is hiding another local variable defined in an enclosing scope\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test096() {
	// local of class under lambda redeclares its own argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"       int lamlocal;\n" +
					"           I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};\n" +
					"		};\n" +
					"	}	\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};\n" + 
				"	                                                      ^^\n" + 
				"Duplicate local variable x1\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};\n" + 
				"	                                                          ^^\n" + 
				"Duplicate local variable x2\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384687 [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
public void test097() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"class Action<K> {\r\n" + 
			"  static <T1> int fooMethod(Object x) { return 0; }\r\n" + 
			"}\r\n" + 
			"interface I {\r\n" + 
			"  int foo(Object x);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    I functional = Action::<?>fooMethod;\r\n" + // no raw type warning here, Action:: is really Action<>::
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	I functional = Action::<?>fooMethod;\n" + 
			"	                        ^\n" + 
			"Wildcard is not allowed at this location\n" + 
			"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=384687 [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
public void test098() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"class Action<K> {\r\n" + 
			"  int foo(Object x, Object y, Object z) { return 0; }\r\n" + 
			"}\r\n" + 
			"interface I {\r\n" + 
			"  void foo(Object x);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    Action<Object> exp = new Action<Object>();\r\n" + 
			"    int x,y,z;\r\n" + 
			"    I len6 = foo->exp.<?>method(x, y, z);\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	I len6 = foo->exp.<?>method(x, y, z);\n" + 
			"	                   ^\n" + 
			"Wildcard is not allowed at this location\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399770: [1.8][compiler] Implement support for @FunctionalInterface
public void test_bug399770_1() {
	this.runConformTest(
			new String[] {
					"YYY.java",
					"interface BASE { void run(); }\n" +
					"@FunctionalInterface\n" +
					"interface DERIVED extends BASE {void run();}" +
					"public class YYY {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(\"Hello\");" +
					"	}\n" +
					"}",
			},
			"Hello"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399770: [1.8][compiler] Implement support for @FunctionalInterface
public void test_bug399770_2() {
	this.runNegativeTest(
			new String[] {
					"YYY.java",
					"interface BASE { void run(); }\n" +
					"@FunctionalInterface\n" +
					"interface DERIVED extends BASE {void run1();}" +
					"@FunctionalInterface public class YYY {\n" +
					"   @FunctionalInterface int x;" +
					"	@FunctionalInterface public static void main(String[] args) {\n" +
					"       @FunctionalInterface int y;" +
					"		System.out.println(\"Hello\");" +
					"	}\n" +
					"}",
			},
			"----------\n" + 
			"1. ERROR in YYY.java (at line 3)\n" + 
			"	interface DERIVED extends BASE {void run1();}@FunctionalInterface public class YYY {\n" + 
			"	          ^^^^^^^\n" + 
			"Invalid \'@FunctionalInterface\' annotation; DERIVED is not a functional interface\n" + 
			"----------\n" + 
			"2. ERROR in YYY.java (at line 3)\n" + 
			"	interface DERIVED extends BASE {void run1();}@FunctionalInterface public class YYY {\n" + 
			"	                                                                               ^^^\n" + 
			"Invalid \'@FunctionalInterface\' annotation; YYY is not a functional interface\n" + 
			"----------\n" + 
			"3. ERROR in YYY.java (at line 4)\n" + 
			"	@FunctionalInterface int x;	@FunctionalInterface public static void main(String[] args) {\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"The annotation @FunctionalInterface is disallowed for this location\n" + 
			"----------\n" + 
			"4. ERROR in YYY.java (at line 4)\n" + 
			"	@FunctionalInterface int x;	@FunctionalInterface public static void main(String[] args) {\n" + 
			"	                           	^^^^^^^^^^^^^^^^^^^^\n" + 
			"The annotation @FunctionalInterface is disallowed for this location\n" + 
			"----------\n" + 
			"5. ERROR in YYY.java (at line 5)\n" + 
			"	@FunctionalInterface int y;		System.out.println(\"Hello\");	}\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"The annotation @FunctionalInterface is disallowed for this location\n" + 
			"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400745, [1.8][compiler] Compiler incorrectly allows shadowing of local class names.
public void test400745() {
	// Lambda redeclares a local class from its outer scope.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		class Y {};\n" + 
					"		I i = ()  -> {\n" +
					"			class Y{} ;\n" +
					"		};\n" +
					"	}\n" +	
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	class Y{} ;\n" + 
			"	      ^\n" + 
			"Duplicate nested type Y\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400745, [1.8][compiler] Compiler incorrectly allows shadowing of local class names.
public void test400745a() {
	// local type hiding scenario 
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"public class X {\n" +
					"	private void foo() {\n" +
					"		class Y {}\n" +
					"		X x = new X() {\n" +
					"			private void foo() {\n" +
					"				class Y {};\n" +
					"			}\n" +
					"		};\n" +
					"		I i = () -> {\n" +
					"			class LX {\n" +
					"				void foo() {\n" +
					"					class Y {};\n" +
					"				}\n" +
					"			};\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	private void foo() {\n" + 
			"	             ^^^^^\n" + 
			"The method foo() from the type X is never used locally\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 6)\n" + 
			"	class Y {}\n" + 
			"	      ^\n" + 
			"The type Y is never used locally\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 8)\n" + 
			"	private void foo() {\n" + 
			"	             ^^^^^\n" + 
			"The method foo() from the type new X(){} is never used locally\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 9)\n" + 
			"	class Y {};\n" + 
			"	      ^\n" + 
			"The type Y is hiding the type Y\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 9)\n" + 
			"	class Y {};\n" + 
			"	      ^\n" + 
			"The type Y is never used locally\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 13)\n" + 
			"	class LX {\n" + 
			"	      ^^\n" + 
			"The type LX is never used locally\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 14)\n" + 
			"	void foo() {\n" + 
			"	     ^^^^^\n" + 
			"The method foo() from the type LX is never used locally\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 15)\n" + 
			"	class Y {};\n" + 
			"	      ^\n" + 
			"The type Y is hiding the type Y\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 15)\n" + 
			"	class Y {};\n" + 
			"	      ^\n" + 
			"The type Y is never used locally\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"public interface I<P extends ParameterType> {\n" +
					"	<T extends ExceptionType , R extends ReturnType> R doit(P p) throws T;\n" +
					"}\n" +
					"\n" +
					"class ReturnType {\n" +
					"}\n" +
					"\n" +
					"class ParameterType {\n" +
					"}\n" +
					"\n" +
					"class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I i = (p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\I.java (at line 12)\n" + 
			"	class ExceptionType extends Exception {\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	^\n" + 
			"I is a raw type. References to generic type I<P> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ReturnType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ParameterType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ExceptionType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556a() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"public interface I<P extends ParameterType> {\n" +
					"	<T extends ExceptionType , R extends ReturnType> R doit(P p) throws T;\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n" +
					"\n" +
					"class ParameterType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I i = (p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	^\n" + 
			"I is a raw type. References to generic type I<P> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ParameterType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556b() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType> {\n" +
					"	<T extends ExceptionType , R extends ReturnType> R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n" +
					"\n" +
					"class ReturnType {\n" +
					"}\n" +
					"\n" +
					"class ParameterType {\n" +
					"}\n" +
					"\n" +
					"class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I i = (p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\I.java (at line 13)\n" + 
			"	class ExceptionType extends Exception {\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	^\n" + 
			"I is a raw type. References to generic type I<P> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ReturnType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	I i = (p) -> { return null; };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ExceptionType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556c() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n" +
					"class ParameterType {\n" +
					"}\n" +
					"class ReturnType {\n" +
					"}\n" +
					"class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I<?, ?, ?> i = (p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\I.java (at line 10)\n" + 
			"	class ExceptionType extends Exception {\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	I<?, ?, ?> i = (p) -> { return null; };\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ReturnType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I<?, ?, ?> i = (p) -> { return null; };\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ParameterType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	I<?, ?, ?> i = (p) -> { return null; };\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The type ExceptionType from the descriptor computed for the target context is not visible here.  \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556d() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I<?, ?, ?> i = (p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556e() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I<?, ?, ?> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	I<?, ?, ?> i = (String p) -> { return null; };\n" + 
			"	                ^^^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List<? extends List<ParameterType>>[]\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556f() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I<? extends p.ParameterType, ? extends p.ExceptionType, ? extends p.ReturnType> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	I<? extends p.ParameterType, ? extends p.ExceptionType, ? extends p.ReturnType> i = (String p) -> { return null; };\n" + 
			"	                                                                                     ^^^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List<? extends List<ParameterType>>[]\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556g() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"class P extends p.ParameterType {}\n" +
					"class T extends p.ExceptionType {}\n" +
					"class R extends p.ReturnType {}\n" +
					"public class X {\n" +
					"	I<P, T, R> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class T extends p.ExceptionType {}\n" + 
			"	      ^\n" + 
			"The serializable class T does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	I<P, T, R> i = (String p) -> { return null; };\n" + 
			"	                ^^^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List<? extends List<P>>[]\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556h() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"class P extends p.ParameterType {}\n" +
					"class T extends p.ExceptionType {}\n" +
					"class R extends p.ReturnType {}\n" +
					"public class X {\n" +
					"	I<T, R, P> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class T extends p.ExceptionType {}\n" + 
			"	      ^\n" + 
			"The serializable class T does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	I<T, R, P> i = (String p) -> { return null; };\n" + 
			"	  ^\n" + 
			"Bound mismatch: The type T is not a valid substitute for the bounded parameter <P extends ParameterType> of the type I<P,T,R>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	I<T, R, P> i = (String p) -> { return null; };\n" + 
			"	     ^\n" + 
			"Bound mismatch: The type R is not a valid substitute for the bounded parameter <T extends ExceptionType> of the type I<P,T,R>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	I<T, R, P> i = (String p) -> { return null; };\n" + 
			"	        ^\n" + 
			"Bound mismatch: The type P is not a valid substitute for the bounded parameter <R extends ReturnType> of the type I<P,T,R>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 6)\n" + 
			"	I<T, R, P> i = (String p) -> { return null; };\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression is not a well formed parameterized type due to bound(s) mismatch\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556i() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"class P extends p.ParameterType {}\n" +
					"class T extends p.ExceptionType {}\n" +
					"class R extends p.ReturnType {}\n" +
					"public class X {\n" +
					"	I<? super P, ? super T, ? super R> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class T extends p.ExceptionType {}\n" + 
			"	      ^\n" + 
			"The serializable class T does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	I<? super P, ? super T, ? super R> i = (String p) -> { return null; };\n" + 
			"	                                        ^^^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List<? extends List<P>>[]\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556j() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends P , R extends T> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"class P extends p.ParameterType {}\n" +
					"class T extends p.ExceptionType {}\n" +
					"class R extends p.ReturnType {}\n" +
					"public class X {\n" +
					"	I<?, ?, ?> i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in p\\I.java (at line 4)\n" + 
			"	R doit(List<? extends List<P>>[] p) throws T;\n" + 
			"	                                           ^\n" + 
			"No exception of type T can be thrown; an exception type must be a subclass of Throwable\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class T extends p.ExceptionType {}\n" + 
			"	      ^\n" + 
			"The serializable class T does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	I<?, ?, ?> i = (String p) -> { return null; };\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression is not a well formed parameterized type due to bound(s) mismatch\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556k() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {\n" +
					"	R doit(List<? extends List<P>>[] p) throws T;\n" +
					"}\n",
					"p/ParameterType.java",
					"package p;\n" +
					"public class ParameterType {\n" +
					"}\n",
					"p/ReturnType.java",
					"package p;\n" +
					"public class ReturnType {\n" +
					"}\n",
					"p/ExceptionType.java",
					"package p;\n" +
					"public class ExceptionType extends Exception {\n" +
					"}\n",
					"X.java",
					"import p.I;\n" +
					"public class X {\n" +
					"	I i = (String p) -> { return null; };\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\ExceptionType.java (at line 2)\n" + 
			"	public class ExceptionType extends Exception {\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"The serializable class ExceptionType does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	I i = (String p) -> { return null; };\n" + 
			"	^\n" + 
			"I is a raw type. References to generic type I<P,T,R> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	I i = (String p) -> { return null; };\n" + 
			"	       ^^^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List[]\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo(int x, String p);\n" +
					"}\n" +
					"public class X {\n" +
					"	int x = 0;\n" +
					"	I i = x::zoo;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = x::zoo;\n" + 
			"	      ^\n" + 
			"Cannot invoke zoo(int, String) on the primitive type int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750a() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo(int x, String p);\n" +
					"}\n" +
					"public class X {\n" +
					"	int x = 0;\n" +
					"	I i = I::new;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = I::new;\n" + 
			"	      ^\n" + 
			"Cannot instantiate the type I\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750b() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo(int x, String p);\n" +
					"}\n" +
					"abstract public class X {\n" +
					"	int x = 0;\n" +
					"	I i = X::new;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = X::new;\n" + 
			"	      ^\n" + 
			"Cannot instantiate the type X\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750c() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo(int x, String p);\n" +
					"}\n" +
					"abstract public class X {\n" +
					"	int x = 0;\n" +
					"	I i = E::new;\n" +
					"}\n" +
					"enum E {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = E::new;\n" + 
			"	      ^\n" + 
			"Cannot instantiate the type E\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750d() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> {\n" +
					"	X(int x, String p) {}\n" +
					"	I i = X<? extends String>::new;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = X<? extends String>::new;\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot instantiate the type X<? extends String>\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750e() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> {\n" +
					"	X(int x, String p) {}\n" +
					"	I i = T::new;\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = T::new;\n" + 
			"	      ^\n" + 
			"Cannot instantiate the type T\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750f() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> {\n" +
					"	X(int x, String p) {}\n" +
					"	I i = Annot::new;\n" +
					"}\n" +
					"@interface Annot {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = Annot::new;\n" + 
			"	      ^^^^^\n" + 
			"Cannot instantiate the type Annot\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750g() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> {\n" +
					"	X(int x, String p) {}\n" +
					"   static {\n" +
					"	    I i = this::foo;\n" +
					"   }\n" +
					"   X<?> foo(int x, String p) { return null; }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I i = this::foo;\n" + 
			"	      ^^^^\n" + 
			"Cannot use this in a static context\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750h() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> {\n" +
					"	X(int x, String p) {}\n" +
					"	I i = this::foo;\n" +
					"   X<?> foo(int x, String p) { return null; }\n" +
					"}\n"
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750i() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> extends Y {\n" +
					"   static {\n" +
					"	    I i = super::foo;\n" +
					"   }\n" +
					"}\n" +
					"class Y {\n" +
					"    X<?> foo(int x, String p) { return null; }\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 6)\n" + 
					"	I i = super::foo;\n" + 
					"	      ^^^^^\n" + 
					"Cannot use super in a static context\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750j() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  X<?> zoo(int x, String p);\n" +
					"}\n" +
					"public class X<T> extends Y {\n" +
					"	I i = super::foo;\n" +
					"}\n" +
					"class Y {\n" +
					"    X<?> foo(int x, String p) { return null; }\n" +
					"}\n"
					},
					"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750k() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo();\n" +
					"}\n" +
					"class Z {\n" +
					"	void zoo() {}\n" +
					"}\n" +
					"class X extends Z {\n" +
					"    static class N {\n" +
					"    	I i = X.super::zoo;\n" +
					"    }\n" +
					"}\n" 
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 9)\n" + 
					"	I i = X.super::zoo;\n" + 
					"	      ^^^^^^^\n" + 
					"No enclosing instance of the type X is accessible in scope\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750l() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void zoo();\n" +
					"}\n" +
					"class Z {\n" +
					"	void zoo() {}\n" +
					"}\n" +
					"class X extends Z {\n" +
					"    class N {\n" +
					"    	I i = X.super::zoo;\n" +
					"    }\n" +
					"}\n" 
					},
					"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750m() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"interface J {\n" +
					"	int size(ArrayList<String> als);\n" +
					"}\n" +
					"class X {\n" +
					"   I i1 = ArrayList::new;\n" +
					"   I i2 = ArrayList<String>::new;\n" +
					"   I i3 = ArrayList<Integer>::new;\n" +
					"   I i4 = List<String>::new;\n" +
					"   J j1 = String::length;\n" +
					"   J j2 = List::size;\n" +
					"   J j3 = List<String>::size;\n" +
					"   J j4 = List<Integer>::size;\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 12)\n" + 
					"	I i3 = ArrayList<Integer>::new;\n" + 
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"The constructed object of type ArrayList<Integer> is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 13)\n" + 
					"	I i4 = List<String>::new;\n" + 
					"	       ^^^^^^^^^^^^\n" + 
					"Cannot instantiate the type List<String>\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 14)\n" + 
					"	J j1 = String::length;\n" + 
					"	       ^^^^^^^^^^^^^^\n" + 
					"The type String does not define length(ArrayList<String>) that is applicable here\n" + 
					"----------\n" + 
					"4. WARNING in X.java (at line 15)\n" + 
					"	J j2 = List::size;\n" + 
					"	       ^^^^\n" + 
					"List is a raw type. References to generic type List<E> should be parameterized\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 17)\n" + 
					"	J j4 = List<Integer>::size;\n" + 
					"	       ^^^^^^^^^^^^^^^^^^^\n" + 
					"The type List<Integer> does not define size(ArrayList<String>) that is applicable here\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750n() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"public class X {\n" +
					"   I i1 = ArrayList<String>[]::new;\n" +
					"   I i2 = List<String>[]::new;\n" +
					"   I i3 = ArrayList<String>::new;\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 7)\n" + 
					"	I i1 = ArrayList<String>[]::new;\n" + 
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Cannot create a generic array of ArrayList<String>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 8)\n" + 
					"	I i2 = List<String>[]::new;\n" + 
					"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
					"Cannot create a generic array of List<String>\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750o() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> [] doit();\n" +
					"}\n" +
					"interface J {\n" +
					"	List<String> [] doit(long l);\n" +
					"}\n" +
					"interface K {\n" +
					"	List<String> [] doit(String s, long l);\n" +
					"}\n" +
					"interface L {\n" +
					"	List<String> [] doit(short s);\n" +
					"}\n" +
					"interface M {\n" +
					"	List<String> [] doit(byte b);\n" +
					"}\n" +
					"interface N {\n" +
					"	List<String> [] doit(int i);\n" +
					"}\n" +
					"interface O {\n" +
					"	List<String> [] doit(Integer i);\n" +
					"}\n" +
					"interface P {\n" +
					"	List<String> [] doit(Short i);\n" +
					"}\n" +
					"interface Q {\n" +
					"	List<String> [] doit(Float i);\n" +
					"}\n" +
					"interface R {\n" +
					"	List<String> [] doit(int i);\n" +
					"}\n" +
					"public class X {\n" +
					"   I i = List[]::new;\n" +
					"   J j = ArrayList[]::new;\n" +
					"   K k = ArrayList[]::new;\n" +
					"   L l = ArrayList[]::new;\n" +
					"   M m = ArrayList[]::new;\n" +
					"   N n = ArrayList[]::new;\n" +
					"   O o = ArrayList[]::new;\n" +
					"   P p = ArrayList[]::new;\n" +
					"   Q q = ArrayList[]::new;\n" +
					"   R r = ArrayList[][][]::new;\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 34)\n" + 
					"	I i = List[]::new;\n" + 
					"	      ^^^^^^^^^^^^\n" + 
					"Incompatible parameter list for array constructor. Expected (int), but found ()\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 35)\n" + 
					"	J j = ArrayList[]::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^^\n" + 
					"Incompatible parameter list for array constructor. Expected (int), but found (long)\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 36)\n" + 
					"	K k = ArrayList[]::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^^\n" + 
					"Incompatible parameter list for array constructor. Expected (int), but found (String, long)\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 42)\n" + 
					"	Q q = ArrayList[]::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^^\n" + 
					"Incompatible parameter list for array constructor. Expected (int), but found (Float)\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 43)\n" + 
					"	R r = ArrayList[][][]::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Constructed array ArrayList[][][] cannot be assigned to List<String>[] as required in the interface descriptor  \n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750p() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"class X<T> {\n" +
					"	static void foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = x::foo;\n" +
					"		I i2 = X<String>::foo;\n" +
					"		I i3 = X::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 9)\n" + 
					"	I i1 = x::foo;\n" + 
					"	       ^^^^^^\n" + 
					"The method foo() from the type X<String> should be accessed in a static way \n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 10)\n" + 
					"	I i2 = X<String>::foo;\n" + 
					"	       ^^^^^^^^^^^^^^\n" + 
					"The method foo() from the type X<String> should be accessed in a static way \n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 11)\n" + 
					"	I i3 = X::foo;\n" + 
					"	       ^^^^^^\n" + 
					"The type of foo() from the type X is void, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750q() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"class X<T> {\n" +
					"	void foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = x::foo;\n" +
					"		I i2 = X<String>::foo;\n" +
					"		I i3 = X::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 9)\n" + 
					"	I i1 = x::foo;\n" + 
					"	       ^^^^^^\n" + 
					"The type of foo() from the type X<String> is void, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 10)\n" + 
					"	I i2 = X<String>::foo;\n" + 
					"	       ^^^^^^^^^^^^^^\n" + 
					"Cannot make a static reference to the non-static method foo() from the type X<String>\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 11)\n" + 
					"	I i3 = X::foo;\n" + 
					"	       ^^^^^^\n" + 
					"Cannot make a static reference to the non-static method foo() from the type X\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750r() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> xs);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	void foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = X::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 9)\n" + 
					"	I i1 = X::foo;\n" + 
					"	       ^\n" + 
					"X is a raw type. References to generic type X<T> should be parameterized\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 9)\n" + 
					"	I i1 = X::foo;\n" + 
					"	       ^^^^^^\n" + 
					"The type of foo() from the type X<String> is void, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750s() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> xs);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	void foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = X<String>::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 9)\n" + 
					"	I i1 = X<String>::foo;\n" + 
					"	       ^^^^^^^^^^^^^^\n" + 
					"The type of foo() from the type X<String> is void, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750t() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> xs);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	static List<String> foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = X<String>::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 9)\n" + 
					"	I i1 = X<String>::foo;\n" + 
					"	       ^^^^^^^^^^^^^^\n" + 
					"The method foo() from the type X<String> should be accessed in a static way \n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750u() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> xs, int x);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	static List<String> foo() {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = X::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 9)\n" + 
					"	I i1 = X::foo;\n" + 
					"	       ^\n" + 
					"X is a raw type. References to generic type X<T> should be parameterized\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 9)\n" + 
					"	I i1 = X::foo;\n" + 
					"	       ^^^^^^\n" + 
					"The type X does not define foo(X<String>, int) that is applicable here\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750v() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> xs, int x);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	static List<String> foo(X<String> xs, int x) {}\n" +
					"	List<String> foo(int x) {}\n" +
					"	{\n" +
					"		X<String> x = new X<String>();\n" +
					"		I i1 = X::foo;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 10)\n" + 
					"	I i1 = X::foo;\n" + 
					"	       ^^^^^^\n" + 
					"Ambiguous method reference: both foo(int) and foo(X<String>, int) from the type X<String> are eligible\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750w() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	X<String> doit(int x);\n" +
					"}\n" +
					"interface J {\n" +
					"	X<String> doit(int x, int p);\n" +
					"}\n" +
					"interface K {\n" +
					"	X<String> doit(int x);\n" +
					"   void goo();\n" +
					"}\n" +
					"interface L {\n" +
					"	X<String> doit(short x);\n" +
					"}\n" +
					"interface M {\n" +
					"	X<String> doit(String s);\n" +
					"}\n" +
					"class X<T> {\n" +
					"	X(int x, int y) {}\n" +
					"	X(int x) {}\n" +
					"	{\n" +
					"		I i = X::new;\n" +
					"       J j = X<Integer>::new;\n" +
					"       K k = X::new;\n" +
					"       L l = X<String>::new;\n" +
					"       M m = X<String>::new;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 23)\n" + 
					"	J j = X<Integer>::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^\n" + 
					"The constructed object of type X<Integer> is incompatible with the descriptor\'s return type: X<String>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 24)\n" + 
					"	K k = X::new;\n" + 
					"	      ^^^^^^^\n" + 
					"The target type of this expression must be a functional interface\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 26)\n" + 
					"	M m = X<String>::new;\n" + 
					"	      ^^^^^^^^^^^^^^^\n" + 
					"The type X<String> does not define X(String) that is applicable here\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750x() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.io.IOException;\n" +
					"import java.io.FileNotFoundException;\n" +		
					"interface I {\n" +
					"	X<String> doit(int x);\n" +
					"}\n" +
					"interface J {\n" +
					"	X<String> doit(int x) throws IOException;\n" +
					"}\n" +
					"interface K {\n" +
					"	X<String> doit(int x) throws FileNotFoundException;\n" +
					"}\n" +
					"interface L {\n" +
					"	X<String> doit(short x) throws Exception;\n" +
					"}\n" +
					"class X<T> {\n" +
					"	X(int x) throws IOException, FileNotFoundException {}\n" +
					"	{\n" +
					"		I i = X::new;\n" +
					"       J j = X<Integer>::new;\n" +
					"       K k = X::new;\n" +
					"       L l = X<String>::new;\n" +
					"	}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 19)\n" + 
					"	I i = X::new;\n" + 
					"	      ^^^^^^^\n" + 
					"Unhandled exception type IOException\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 19)\n" + 
					"	I i = X::new;\n" + 
					"	      ^^^^^^^\n" + 
					"Unhandled exception type FileNotFoundException\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 20)\n" + 
					"	J j = X<Integer>::new;\n" + 
					"	      ^^^^^^^^^^^^^^^^\n" + 
					"The constructed object of type X<Integer> is incompatible with the descriptor\'s return type: X<String>\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 21)\n" + 
					"	K k = X::new;\n" + 
					"	      ^^^^^^^\n" + 
					"Unhandled exception type IOException\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750y() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"abstract class Y {\n" +
					"    abstract void foo();\n" +
					"}\n" +
					"class X extends Y {\n" +
					"	void foo() {}\n" +
					"   I i = super::foo;\n" +
					"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 10)\n" + 
					"	I i = super::foo;\n" + 
					"	      ^^^^^^^^^^\n" + 
					"Cannot directly invoke the abstract method foo() for the type Y\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"class Y {\n" +
					"    static void foo() {}\n" +
					"}\n" +
					"class X extends Y {\n" +
					"   I i = X::foo;\n" +
					"}\n"
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 8)\n" + 
					"	I i = X::foo;\n" + 
					"	      ^^^^^^\n" + 
					"The static method foo() from the type Y should be accessed directly \n" + 
					"----------\n",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"class X extends Y {\n" +
					"   I i = X::foo;\n" +
					"}\n",
					"Y.java", 
					"@Deprecated class Y {\n" +
					"    @Deprecated static void foo() {}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 4)\n" + 
					"	class X extends Y {\n" + 
					"	                ^\n" + 
					"The type Y is deprecated\n" + 
					"----------\n" + 
					"2. WARNING in X.java (at line 5)\n" + 
					"	I i = X::foo;\n" + 
					"	      ^^^^^^\n" + 
					"The method foo() from the type Y is deprecated\n" + 
					"----------\n",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z2() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeArgumentsForMethodInvocation, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"class X extends Y {\n" +
					"   I i = X::<String>foo;\n" +
					"}\n",
					"Y.java", 
					"@Deprecated class Y {\n" +
					"    @Deprecated static void foo() {}\n" +
					"}\n"
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 4)\n" + 
					"	class X extends Y {\n" + 
					"	                ^\n" + 
					"The type Y is deprecated\n" + 
					"----------\n" + 
					"2. WARNING in X.java (at line 5)\n" + 
					"	I i = X::<String>foo;\n" + 
					"	      ^^^^^^^^^^^^^^\n" + 
					"The method foo() from the type Y is deprecated\n" + 
					"----------\n" + 
					"3. WARNING in X.java (at line 5)\n" + 
					"	I i = X::<String>foo;\n" + 
					"	          ^^^^^^\n" + 
					"Unused type arguments for the non generic method foo() of type Y; it should not be parameterized with arguments <String>\n" + 
					"----------\n",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z3() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"class X {\n" +
					"   String foo() { return null; }\n" +
					"   I i = new X()::foo;\n" +
					"}\n",
					},
					"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z4() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"class X {\n" +
					"   void foo() { return; }\n" +
					"   I i = new X()::foo;\n" +
					"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 7)\n" + 
					"	I i = new X()::foo;\n" + 
					"	      ^^^^^^^^^^^^\n" + 
					"The type of foo() from the type X is void, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z5() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.util.ArrayList;\n" +
					"interface I {\n" +
					"	List<String> doit();\n" +
					"}\n" +
					"class X {\n" +
					"   ArrayList<Integer> foo() { return null; }\n" +
					"   I i = new X()::foo;\n" +
					"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 8)\n" + 
					"	I i = new X()::foo;\n" + 
					"	      ^^^^^^^^^^^^\n" + 
					"The type of foo() from the type X is ArrayList<Integer>, this is incompatible with the descriptor\'s return type: List<String>\n" + 
					"----------\n");
}
//  https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z6() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.util.ArrayList;\n" +
					"interface I {\n" +
					"	List<String> doit(X<String> x, int y);\n" +
					"}\n" +
					"class X<T> {\n" +
					"   ArrayList<String> foo(int x) { return null; }\n" +
					"   I i = X::foo;\n" +
					"}\n",
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 8)\n" + 
					"	I i = X::foo;\n" + 
					"	      ^\n" + 
					"X is a raw type. References to generic type X<T> should be parameterized\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z7() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"import java.util.List;\n" +
				"import java.util.ArrayList;\n" +
				"interface I {\n" +
				"	List<String> doit(X x, int y);\n" +
				"}\n" +
				"class X<T> {\n" +
				"   ArrayList<Integer> foo(int x) { return null; }\n" +
				"   I i = X::foo;\n" +
				"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	List<String> doit(X x, int y);\n" + 
				"	                  ^\n" + 
				"X is a raw type. References to generic type X<T> should be parameterized\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 8)\n" + 
				"	I i = X::foo;\n" + 
				"	      ^\n" + 
				"X is a raw type. References to generic type X<T> should be parameterized\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z8() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	int [] doit(int [] ia);\n" +
				"}\n" +
				"class X<T> {\n" +
				"   I i = int []::clone;\n" +
				"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z9() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	int [] doit(X x);\n" +
				"}\n" +
				"public class X {\n" +
    			"	Zork foo() {\n" +
    			"	}\n" +
    			"   I i = X::foo;\n" +
    			"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	Zork foo() {\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	I i = X::foo;\n" + 
				"	      ^^^^^^\n" + 
				"The method foo() from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	I i = X::foo;\n" + 
				"	      ^^^^^^\n" + 
				"The type of foo() from the type X is Zork, this is incompatible with the descriptor\'s return type: int[]\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610() {
this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {\n" +
				"		System.out.println(\"foo\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"	}\n" +
				"}\n",
				},
				"foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610a() {
this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"    void foo(int x, int y);\n" +
				"}\n" +
				"interface K {\n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	void foo(K k) {\n" +
				"		System.out.println(\"foo(K)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"		new X().foo((x, y)->{});\n" +
				"		new X().foo((s)->{});\n" +
				"	}\n" +
				"}\n",
				},
				"foo(I)\n" + 
				"foo(J)\n" + 
				"foo(K)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610b() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"    void foo(int x, int y);\n" +
				"}\n" +
				"interface K {\n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"		new X().foo((x, y)->{});\n" +
				"		new X().foo((s)->{});\n" +
				"	}\n" +
				"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 20)\n" + 
				"	new X().foo((s)->{});\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610c() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"interface K {\n" +
				"    String foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	void foo(K k) {\n" +
				"		System.out.println(\"foo(K)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{ return \"\";});\n" +
				"		new X().foo(()-> 10);\n" +
				"		new X().foo((s)->{});\n" +
				"		new X().foo((s)->{ return;});\n" +
				"		new X().foo((s)->{ return \"\";});\n" +
				"		new X().foo((s)-> \"hello\");\n" +
				"		new X().foo(()->{ return;});\n" +
				"		new X().foo(()-> System.out.println());\n" +
				"	}\n" +
				"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 15)\n" + 
				"	new X().foo(()->{ return \"\";});\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments (() -> {\n" + 
				"  return \"\";\n" + 
				"})\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 16)\n" + 
				"	new X().foo(()-> 10);\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments (() -> 10)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 17)\n" + 
				"	new X().foo((s)->{});\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {\n" + 
				"})\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 18)\n" + 
				"	new X().foo((s)->{ return;});\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {\n" + 
				"  return ;\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610d() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"    <T> T id(T arg) { return arg; }\n" +
				"    Runnable r = id(() -> { System.out.println(); });\n" +
				"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610e() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I<T extends String> {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	<T> T foo(I<T> it) { return null; }\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"	}\n" +
				"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 1)\n" + 
				"	interface I<T extends String> {\n" + 
				"	                      ^^^^^^\n" + 
				"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	<T> T foo(I<T> it) { return null; }\n" + 
				"	            ^\n" + 
				"Bound mismatch: The type T is not a valid substitute for the bounded parameter <T extends String> of the type I<T>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	new X().foo(()->{});\n" + 
				"	        ^^^\n" + 
				"The method foo(I<T>) in the type X is not applicable for the arguments (() -> {\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610f() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I<T> {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	<T> T foo(I<T> it) { return null; }\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"	}\n" +
				"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610g() {
this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J { \n" +
				"    String foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I it) { System.out.println(\"foo(I)\");}\n" +
				"	void foo(J it) { System.out.println(\"foo(J)\");}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{});\n" +
				"	}\n" +
				"}\n",
				},
				"foo(I)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610h() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J { \n" +
				"    String foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I it) { System.out.println(\"foo(I)\");}\n" +
				"	void foo(J it) { System.out.println(\"foo(J)\");}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(()->{ return 10; });\n" +
				"	}\n" +
				"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 11)\n" + 
				"	new X().foo(()->{ return 10; });\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments (() -> {\n" + 
				"  return 10;\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610i() {
this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J { \n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I it) { System.out.println(\"foo(I)\");}\n" +
				"	void foo(J it) { System.out.println(\"foo(J)\");}\n" +
				
				"	public static void main(String[] args) {\n" +
				"		new X().foo((String s)->{});\n" +
				"	}\n" +
				"}\n",
				},
				"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610j() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J { \n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I it) { System.out.println(\"foo(I)\");}\n" +
				"	void foo(J it) { System.out.println(\"foo(J)\");}\n" +
				
				"	public static void main(String[] args) {\n" +
				"		new X().foo((Object o)->{});\n" +
				"	}\n" +
				"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 11)\n" + 
				"	new X().foo((Object o)->{});\n" + 
				"	        ^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments ((Object o) -> {\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401789, [1.8][compiler] Enable support for method/constructor references in non-overloaded method calls.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401790, Follow up of bug 401610, explicit constructor calls and allocation expressions needs updates too.
public void test401789_401790() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X {\n" +
				"	int foo(I i) { return 10;}\n" +
				"	class Y {\n" +
				"		Y (I i) {}\n" +
				"		Y() {\n" +
				"			this(X::goo);\n" +
				"		}\n" +
				"	}\n" +
				"	X(I i) {}\n" +
				"	X() {\n" +
				"		this((x) -> { return 10;});\n" +
				"	}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(X::goo);\n" +
				"		new X((x)->{ return 10;});\n" +
				"		new X((x)->{ return 10;}).new Y((x) -> { return 0;});\n" +
				"		new X((x)->{ return 10;}) {};\n" +
				"	}\n" +
				"}\n" +
				"class Z extends X {\n" +
				"	Z() {\n" +
				"		super(X::goo);\n" +
				"	}\n" +
				"	Z(int i) {\n" +
				"		super (x -> 10);\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 31)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401789, [1.8][compiler] Enable support for method/constructor references in non-overloaded method calls.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401790, Follow up of bug 401610, explicit constructor calls and allocation expressions needs updates too.
public void test401789_401790a() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	String foo(X x);\n" +
				"}\n" +
				"public class X {\n" +
				"	int foo(I i) { return 10;}\n" +
				"	class Y {\n" +
				"		Y (I i) {}\n" +
				"		Y() {\n" +
				"			this(X::goo);\n" +
				"		}\n" +
				"	}\n" +
				"	X(I i) {}\n" +
				"	X() {\n" +
				"		this((x) -> { return 10;});\n" +
				"	}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(X::goo);\n" +
				"		new X((x)->{ return 10;});\n" +
				"		new X((x)->{ return 10;}).new Y((x) -> { return 0;});\n" +
				"		new X((x)->{ return 10;}) {};\n" +
				"	}\n" +
				"}\n" +
				"class Z extends X {\n" +
				"	Z() {\n" +
				"		super(X::goo);\n" +
				"	}\n" +
				"	Z(int i) {\n" +
				"		super (x -> 10);\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	this(X::goo);\n" + 
				"	^^^^^^^^^^^^^\n" + 
				"The constructor X.Y(X::goo) is undefined\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 14)\n" + 
				"	this((x) -> { return 10;});\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The constructor X((<no type> x) -> {\n" + 
				"  return 10;\n" + 
				"}) is undefined\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 18)\n" + 
				"	foo(X::goo);\n" + 
				"	^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments (X::goo)\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 19)\n" + 
				"	new X((x)->{ return 10;});\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The constructor X((<no type> x) -> {\n" + 
				"  return 10;\n" + 
				"}) is undefined\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 20)\n" + 
				"	new X((x)->{ return 10;}).new Y((x) -> { return 0;});\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The constructor X((<no type> x) -> {\n" + 
				"  return 10;\n" + 
				"}) is undefined\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 20)\n" + 
				"	new X((x)->{ return 10;}).new Y((x) -> { return 0;});\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The constructor X.Y((<no type> x) -> {\n" + 
				"  return 0;\n" + 
				"}) is undefined\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 21)\n" + 
				"	new X((x)->{ return 10;}) {};\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The constructor X((<no type> x) -> {\n" + 
				"  return 10;\n" + 
				"}) is undefined\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 26)\n" + 
				"	super(X::goo);\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"The constructor X(X::goo) is undefined\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 29)\n" + 
				"	super (x -> 10);\n" + 
				"	^^^^^^^^^^^^^^^^\n" + 
				"The constructor X((<no type> x) -> 10) is undefined\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 31)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X extends Zork {\n" +
				"	int foo(I ...i) { return 10;}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(X::goo);\n" +
				"		foo((x)-> {return 10;});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public class X extends Zork {\n" + 
			"	                       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X {\n" +
				"	int foo(I [] ...i) { return 10;}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(X::goo);\n" +
				"		foo((x)-> {return 10;});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	foo(X::goo);\n" + 
			"	^^^\n" + 
			"The method foo(I[]...) in the type X is not applicable for the arguments (X::goo)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	foo((x)-> {return 10;});\n" + 
			"	^^^\n" + 
			"The method foo(I[]...) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
			"  return 10;\n" + 
			"})\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X extends Zork {\n" +
				"	X(I ...i) {}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		new X(X::goo);\n" +
				"		new X((x)-> {return 10;});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public class X extends Zork {\n" + 
			"	                       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X extends Zork {\n" +
				"	X(I ...i) {}\n" +
				"   X() {\n" +
				"       this((x)-> {return 10;});\n" +
				"}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		new X(X::goo);\n" +
				"		new X((x)-> {return 10;});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public class X extends Zork {\n" + 
			"	                       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X extends Zork {\n" +
				"    class Y {\n" +
				"        Y(I ... i) {}\n" +
				"    }\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		new X().new Y(X::goo);\n" +
				"		new X().new Y((x)-> {return 10;});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public class X extends Zork {\n" + 
			"	                       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X extends Zork {\n" +
				"	X(I ...i) {}\n" +
				"   X() {\n" +
				"       this((x)-> {return 10;});\n" +
				"}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		new X(X::goo) {};\n" +
				"		new X((x)-> {return 10;}){};\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public class X extends Zork {\n" + 
			"	                       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401847, [1.8][compiler] Polyconditionals not accepted in method invocation contexts.
public void test401847() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo(X x);\n" +
				"}\n" +
				"public class X {\n" +
				"	int foo(I ...i) { return 10;}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(true ? X::goo : X::goo);\n" +
				"		foo(true ? x-> 1 : x->0);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	foo(true ? X::goo : X::goo);\n" + 
			"	                    ^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	foo(true ? x-> 1 : x->0);\n" + 
			"	                   ^^^^\n" + 
			"Dead code\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401847, [1.8][compiler] Polyconditionals not accepted in method invocation contexts.
public void test401847a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String foo(X x);\n" +
				"}\n" +
				"public class X {\n" +
				"	int foo(I ...i) { return 10;}\n" +
				"	int goo() { return 0;}\n" +
				"	{\n" +
				"		foo(true ? X::goo : X::goo);\n" +
				"		foo(true ? x-> 1 : x->0);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	foo(true ? X::goo : X::goo);\n" + 
			"	^^^\n" + 
			"The method foo(I...) in the type X is not applicable for the arguments ((true ? X::goo : X::goo))\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	foo(true ? x-> 1 : x->0);\n" + 
			"	^^^\n" + 
			"The method foo(I...) in the type X is not applicable for the arguments ((true ? (<no type> x) -> 1 : (<no type> x) -> 0))\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo(I i) {}\n" +
				"	I i = ()->{ throw new RuntimeException(); }; // OK\n" +
				"	{\n" +
				"		foo(()->{ throw new RuntimeException(); });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	void foo(I i) {}\n" + 
				"	           ^\n" + 
				"The parameter i is hiding a field from type X\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo(I i) {}\n" +
				"	I i = ()->{ throw new RuntimeException(); }; // OK\n" +
				"	{\n" +
				"		foo(()->{ if (1 == 2) throw new RuntimeException(); });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	void foo(I i) {}\n" + 
				"	           ^\n" + 
				"The parameter i is hiding a field from type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	foo(()->{ if (1 == 2) throw new RuntimeException(); });\n" + 
				"	^^^\n" + 
				"The method foo(I) in the type X is not applicable for the arguments (() -> {\n" + 
				"  if ((1 == 2))\n" + 
				"      throw new RuntimeException();\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    String foo(String x) throws Exception;\n" +
				"}\n" +
				"public class X {\n" +
				"	static final boolean FALSE = false;\n" +
				"	static final boolean TRUE = true;\n" +
				"	void goo(I i) {\n" +
				"	}\n" +
				"	void zoo() {\n" +
				"		final boolean NIJAM = true;\n" +
				"		final boolean POI = false;\n" +
				"       final boolean BLANK;\n" +
				"       BLANK = true;\n" +
				"		goo((x) -> { while (FALSE) throw new Exception(); });\n" +
				"		goo((x) -> { while (TRUE) throw new Exception(); });\n" +
				"		goo((x) -> { while (NIJAM) throw new Exception(); });\n" +
				"		goo((x) -> { while (POI) throw new Exception(); });\n" +
				"		goo((x) -> { if (TRUE) throw new Exception(); else throw new Exception(); });\n" +
				"		goo((x) -> { if (TRUE) throw new Exception(); });\n" +
				"		goo((x) -> { if (true) throw new Exception(); else throw new Exception(); });\n" +
				"		goo((x) -> { if (false) throw new Exception(); else throw new Exception(); });\n" +
				"		goo((x) -> { while (BLANK) throw new Exception(); });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 14)\n" + 
				"	goo((x) -> { while (FALSE) throw new Exception(); });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  while (FALSE)    throw new Exception();\n" + 
				"})\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 17)\n" + 
				"	goo((x) -> { while (POI) throw new Exception(); });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  while (POI)    throw new Exception();\n" + 
				"})\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 19)\n" + 
				"	goo((x) -> { if (TRUE) throw new Exception(); });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  if (TRUE)\n" + 
				"      throw new Exception();\n" + 
				"})\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 22)\n" + 
				"	goo((x) -> { while (BLANK) throw new Exception(); });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  while (BLANK)    throw new Exception();\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    String foo(String x) throws Exception;\n" +
				"}\n" +
				"public class X {\n" +
				"	void goo(I i) {\n" +
				"	}\n" +
				"	void zoo() {\n" +
				"		goo((x) -> { if (x) return null; });\n" +
				"		goo((x) -> {});\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	goo((x) -> { if (x) return null; });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  if (x)\n" + 
				"      return null;\n" + 
				"})\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	goo((x) -> {});\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    String foo(boolean x) throws Exception;\n" +
				"}\n" +
				"public class X {\n" +
				"	void goo(I i) {\n" +
				"	}\n" +
				"	void zoo() {\n" +
				"		goo((x) -> { if (x) return null; });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	goo((x) -> { if (x) return null; });\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"This method must return a result of type String\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo(boolean x) throws Exception;\n" +
				"}\n" +
				"public class X {\n" +
				"	void goo(I i) {\n" +
				"	}\n" +
				"	void zoo() {\n" +
				"		goo((x) -> { return null; });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	goo((x) -> { return null; });\n" + 
				"	^^^\n" + 
				"The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {\n" + 
				"  return null;\n" + 
				"})\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo(boolean x) throws Exception;\n" +
				"}\n" +
				"public class X {\n" +
				"	void goo(I i) {\n" +
				"	}\n" +
				"	void zoo() {\n" +
				"		goo((x) -> { throw new Exception(); });\n" +
				"	}\n" +
				"}\n",			},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String foo(String s1, String s2);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(X x1, X x2);\n" +
				"}\n" +
				"public class X { \n" +
				"	void goo(I i) {}\n" +
				"	void goo(J j) {}\n" +
				"    public static void main(String [] args) {\n" +
				"		new X().goo((p1, p2) -> p1 = p1 + p2);\n" +
				"    }\n" +
				"    Zork z;\n" +
				"}\n",			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String s1, String s2);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(X x1, X x2);\n" +
				"}\n" +
				"public class X { \n" +
				"	void goo(I i) {/* */}\n" +
				"	void goo(J j) {/* */}\n" +
				"    public static void main(String [] args) {\n" +
				"		new X().goo((p1, p2) -> {});\n" +
				"    }\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 11)\n" + 
				"	new X().goo((p1, p2) -> {});\n" + 
				"	                        ^^\n" + 
				"Empty block should be documented\n" + 
				"----------\n",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String foo(String s1, String s2);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(X x1, X x2);\n" +
				"}\n" +
				"public class X { \n" +
				"	void goo(I i) {}\n" +
				"	void goo(J j) {}\n" +
				"    public static void main(String [] args) {\n" +
				"		new X().goo((p1, p2) -> p1 + p2);\n" +
				"    }\n" +
				"    Zork z;\n" +
				"}\n",			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402259, [1.8][compiler] NPE during overload resolution when there are syntax errors. 
public void test402259() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {};\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(() -> { return () -> { return}; });\n" +
				"	}\n" +
				"}\n",			},
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	new X().foo(() -> { return () -> { return}; });\n" + 
				"	                                   ^^^^^^\n" + 
				"Syntax error, insert \";\" to complete BlockStatements\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes.. 
public void test402261() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {};\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(() -> { class local { void foo() { return; }} return () -> { return;}; });\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes.. 
public void test402261a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {};\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(() -> { J j = () -> { return; }; return () -> { return;}; });\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes.. 
public void test402261b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {};\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(() -> { J j = new J() { public void foo() { return; } }; return () -> { return;}; });\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes.. 
public void test402261c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	void foo(I i) {};\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(() -> { return new J() { public void foo() { return; } }; });\n" +
				"	}\n" +
				"   Zork z;\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401769, [1.8][compiler] Explore solutions with better performance characteristics than LambdaExpression#copy()
public void test401769() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void g(I i) {}\n" +
				"	void g(J j) {}\n" +
				"	int f;\n" +
				"	{\n" +
				"		g(() -> f++);\n" +
				"	}\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	g(() -> f++);\n" + 
			"	^\n" + 
			"The method g(I) is ambiguous for the type X\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"abstract class Y {\n" +
				"	abstract void foo();\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	void f(I i) {}\n" +
				"	void f(J j) {}\n" +
				"	\n" +
				"	void foo() {\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		f(super::foo);\n" +
				"	}\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 18)\n" + 
			"	f(super::foo);\n" + 
			"	^\n" + 
			"The method f(I) in the type X is not applicable for the arguments (super::foo)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	f(super::foo);\n" + 
			"	  ^^^^^\n" + 
			"Cannot use super in a static context\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"abstract class Y {\n" +
				"	abstract void foo();\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	void f(I i) {}\n" +
				"	\n" +
				"	void foo() {\n" +
				"	}\n" +
				"	\n" +
				"	public void main(String[] args) {\n" +
				"		f(super::foo);\n" +
				"       I i = super::foo;\n" +
				"	}\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	f(super::foo);\n" + 
			"	  ^^^^^^^^^^\n" + 
			"Cannot directly invoke the abstract method foo() for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	I i = super::foo;\n" + 
			"	      ^^^^^^^^^^\n" + 
			"Cannot directly invoke the abstract method foo() for the type Y\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"abstract class Y {\n" +
				"	abstract void foo();\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	void f(I i) {}\n" +
				"	void f(J j) {}\n" +
				"	\n" +
				"	void foo() {\n" +
				"	}\n" +
				"	\n" +
				"	public void zoo(String[] args) {\n" +
				"		f(super::foo);\n" +
				"	}\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 18)\n" + 
			"	f(super::foo);\n" + 
			"	^\n" + 
			"The method f(I) is ambiguous for the type X\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"abstract class Y {\n" +
				"	void foo() {}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	void f(I i) {}\n" +
				"	void f(J j) {}\n" +
				"	\n" +
				"	void foo() {\n" +
				"	}\n" +
				"	\n" +
				"	public void main(String[] args) {\n" +
				"		f(super::foo);\n" +
				"	}\n" +
				"}\n",			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 18)\n" + 
			"	f(super::foo);\n" + 
			"	^\n" + 
			"The method f(I) is ambiguous for the type X\n" + 
			"----------\n");
}

// 15.28:
// https://bugs.eclipse.org/382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
public void testSuperReference01() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements I2, I1 {\n" +
			"	@Override\n" +
			"	public void print() {\n" +
			"		System.out.print(\"!\");" +
			"	}\n" +
			"   void test() {\n" +
			"		doOutput(I1.super::print); // illegal attempt to skip I2.print()\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		new X().test();\n" +
			"	}\n" +
			"   void doOutput(CanPrint printer) {\n" +
			"      printer.print();" +
			"   }\n" +
			"}\n" +
			"interface CanPrint {\n" +
			"	void print();\n" +
			"}\n" +
			"interface I1 {\n" +
			"	default void print() {\n" +
			"		System.out.print(\"O\");\n" +
			"	}\n" +
			"}\n" +
			"interface I2 extends I1 {\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	doOutput(I1.super::print); // illegal attempt to skip I2.print()\n" + 
		"	         ^^^^^^^^\n" + 
		"Illegal reference to super type I1, cannot bypass the more specific direct super type I2\n" + 
		"----------\n"
	);
}

// 15.28.1:
// https://bugs.eclipse.org/382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
public void testSuperReference02() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I0 {\n" + 
			"	default void print() { System.out.println(\"I0\"); }\n" + 
			"}\n" + 
			"\n" + 
			"interface IA extends I0 {}\n" + 
			"\n" + 
			"interface IB extends I0 {\n" + 
			"	@Override default void print() {\n" + 
			"		System.out.println(\"IB\");\n" + 
			"	}\n" + 
			"}\n" + 
			"public class X implements IA, IB {\n" +
			"	@Override\n" +
			"	public void print() {\n" +
			"		System.out.print(\"!\");" +
			"	}\n" +
			"   void test() {\n" +
			"		doOutput(IA.super::print); // illegal attempt to skip IB.print()\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		new X().test();\n" +
			"	}\n" +
			"   void doOutput(CanPrint printer) {\n" +
			"      printer.print();" +
			"   }\n" +
			"}\n" +
			"interface CanPrint {\n" +
			"	void print();\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 17)\n" + 
		"	doOutput(IA.super::print); // illegal attempt to skip IB.print()\n" + 
		"	         ^^^^^^^^^^^^^^^\n" + 
		"Illegal reference to super method print() from type I0, cannot bypass the more specific override from type IB\n" + 
		"----------\n"
	);
}

public void testSuperReference03() {
	this.runNegativeTest(
			new String[] {
				"XY.java",
				"interface J {\n" + 
				"	void foo(int x);\n" + 
				"}\n" + 
				"class XX {\n" + 
				"	public  void foo(int x) {}\n" + 
				"}\n" + 
				"class Y extends XX {\n" + 
				"	static class Z {\n" + 
				"		public static void foo(int x) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"		public void foo(int x) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"}\n" + 
				"\n" + 
				"public class XY extends XX {\n" + 
				"	@SuppressWarnings(\"unused\")\n" + 
				"	public  void bar(String [] args) {\n" + 
				"		 Y y = new Y();\n" + 
				"		 J jj = y :: foo;\n" + 
				"		 J jx = y.super ::  foo;\n" + 
				"	}\n" + 
				"	public static void main (String [] args) {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in XY.java (at line 23)\n" + 
			"	J jx = y.super ::  foo;\n" + 
			"	       ^\n" + 
			"y cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406614, [1.8][compiler] Missing and incorrect errors for lambda in explicit constructor call. 
public void test406614() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int doit();\n" +
				"}\n" +
				"public class X {\n" +
				"	int f;\n" +
				"	X(I i) {\n" +
				"	}\n" +
				"	X() {\n" +
				"		this(() -> this.f);\n" +
				"	}\n" +
				"	X(short s) {\n" +
				"		this(() -> this.g());\n" +
				"	}\n" +
				"	X (int x) {\n" +
				"	    this(() -> f);\n" +
				"	}\n" +
				"	X (long x) {\n" +
				"	    this(() -> g());\n" +
				"	}\n" +
				"	int g() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	this(() -> this.f);\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X(() -> this.f) is undefined\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	this(() -> this.g());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X(() -> this.g()) is undefined\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 15)\n" + 
			"	this(() -> f);\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"The constructor X(() -> f) is undefined\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 18)\n" + 
			"	this(() -> g());\n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"The constructor X(() -> g()) is undefined\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test406588() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	X.Y.Z makeY(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"	class Y {\n" +
				"		Y(I i) {\n" +
				"			\n" +
				"		}\n" +
				"		Y() {\n" +
				"			this(Z::new);\n" +
				"		}\n" +
				"		class Z {\n" +
				"			Z(int x) {\n" +
				"\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	this(Z::new);\n" + 
			"	     ^^^^^^^\n" + 
			"No enclosing instance of the type X.Y is accessible in scope\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406586, [1.8][compiler] Missing error about unavailable enclosing instance 
public void test406586() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	X.Y makeY();\n" +
				"}\n" +
				"public class X {\n" +
				"	public class Y {\n" +
				"	}\n" +
				"	static void foo() {\n" +
				"		I i = Y::new;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	I i = Y::new;\n" + 
			"	      ^^^^^^^\n" + 
			"No enclosing instance of the type X is accessible in scope\n" + 
			"----------\n");
}
public static Class testClass() {
	return NegativeLambdaExpressionsTest.class;
}
}