/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MethodVerifyTest extends AbstractComparableTest {

	public MethodVerifyTest(String name) {
		super(name);
	}

	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
	}
	
	public static Class testClass() {
		return MethodVerifyTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n" +
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"interface I<U> { public void foo(U u); }\n" +
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// J is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n" +
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n" +
				"interface I<U> { public void foo(U u); }\n"
			},
			""
		);
	}

	public void test002() { // separate files
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// J is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			""
		);
	}

	public void test003() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n",
			// J is not abstract and does not override abstract method foo(A) in I
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n",
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
			null,
			false,
			null
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
	}

	public void test004() { // all together
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"class A {}\n" +
				"class B {}\n" +
				"class X<U> { public U foo() {return null;} }\n" +
				"interface I<U> { public U foo(); }\n" +

				"class J<T> implements I<B> { public T foo() {return null;} }\n" +
				"class K<T> implements I<T> { public T foo() {return null;} }\n" +
				"class L<T> implements I { public T foo() {return null;} }\n" +

				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" +
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n" +
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 5)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"3. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"4. ERROR in ALL.java (at line 10)\n" + 
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
			/*
			ALL.java:5: J is not abstract and does not override abstract method foo() in I
			ALL.java:5: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			ALL.java:8: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			ALL.java:8: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			                                                           ^
			ALL.java:10: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			 */
		);
	}

	public void test005() { // separate files
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",

				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in W.java (at line 1)\n" + 
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
			/*
			J.java:1: J is not abstract and does not override abstract method foo() in I
			J.java:1: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			W.java:1: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			Y.java:1: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			Y.java:1: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			 */
		);
	}

	public void test006() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in W.java (at line 1)\n" + 
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n",
			/*
			J.java:1: J is not abstract and does not override abstract method foo() in I
			J.java:1: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			W.java:1: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			Y.java:1: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			Y.java:1: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			 */
			null,
			false,
			null
		);
	}

	public void test007() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I extends J { String foo(); }\n" +
				"interface J { Object foo(); }\n",
				"X.java",
				"abstract class X1 extends A implements J {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I extends J { Object foo(); }\n" +
				"interface J { String foo(); }\n",
				"X.java",
				"abstract class X2 extends A implements J {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 1)\r\n" + 
			"	abstract class A implements I {}\r\n" + 
			"	               ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 2)\r\n" + 
			"	interface I extends J { Object foo(); }\r\n" + 
			"	                               ^^^^^\n" + 
			"The return type is incompatible with J.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X2 extends A implements J {}\r\n" + 
			"	               ^^\n" + 
			"The return type is incompatible with I.foo(), J.foo()\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X3 extends A implements J {}\n" +
				"interface J { Object foo(); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X4 extends A implements J {}\n" +
				"interface J { String foo(); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public String foo() { return null; } }\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X5 extends A implements I {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public Object foo() { return null; } }\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X6 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X6 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { int get(short i, short s) { return i; } }\n" +
				"class B extends A { short get(short i, short s) {return i; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class B extends A { short get(short i, short s) {return i; } }\r\n" + 
			"	                          ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The return type is incompatible with A.get(short, short)\n" + 
			"----------\n"
		);
	}

	public void test008() { // covariance test
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"interface I { I foo(); }\n" +
				"class A implements I { public A foo() { return null; } }\n" +
				"class B extends A { public B foo() { return null; } }\n" +
				"class C extends B { public A foo() { return null; } }\n" +
				"class D extends B implements I {}\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 4)\r\n" + 
			"	class C extends B { public A foo() { return null; } }\r\n" + 
			"	                             ^^^^^\n" + 
			"The return type is incompatible with B.foo()\n" + 
			"----------\n"
			// foo() in C cannot override foo() in B; attempting to use incompatible return type
		);
	}

	public void test009() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { void foo(G<I> x); }\n" +
				"abstract class A implements I { void foo(G<A> x) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { void foo(G<A> x) {} }\r\n" + 
			"	                                     ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { I foo(G<I> x); }\n" +
				"abstract class A implements I { I foo(G<A> x) { return null; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { I foo(G<A> x) { return null; } }\r\n" + 
			"	                                  ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test010() { // executable bridge method case
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public X foo() {\n" +
				"        System.out.println(\"Did NOT add bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"    public static void main(String[] args) throws Exception {\n" +
				"        X x = new A();\n" +
				"        x.foo();\n" +
				"        System.out.print(\" + \");\n" +
				"        I i = new A();\n" +
				"        i.foo();\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public I foo();\n" +
				"}\n" +
				"class A extends X implements I {\n" +
				"    public A foo() {\n" +
				"        System.out.print(\"Added bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n"
			},
			"Added bridge method + Added bridge method"
		);
	}

	public void test011() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The inherited method A.foo(T) cannot hide the public abstract method in I\n" + 
			"----------\n"
			// <T>foo(T) in A cannot implement <T>foo(T) in I; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X2 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(T) in A and <T>foo(T) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T, S> void foo(T t); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X3 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in A and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T s) {} }\n" +
				"class Y1 extends A { void foo(Object s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y1 extends A { void foo(Object s) {} }\r\n" + 
			"	                          ^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object) in Y1 cannot override <T>foo(T) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T[] s) {} }\n" +
				"class Y2 extends A { void foo(Object[] s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y2 extends A { void foo(Object[] s) {} }\n" + 
			"	                          ^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object[]) in Y2 cannot override <T>foo(T[]) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public void foo(Class<Object> s) {} }\n" +
				"class Y3 extends A { void foo(Class<Object> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y3 extends A { void foo(Class<Object> s) {} }\r\n" + 
			"	                          ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Class<java.lang.Object>) in Y3 cannot override foo(java.lang.Class<java.lang.Object>) in A; attempting to assign weaker access privileges; was public
		);
	}

	public void test013() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X0 extends A implements I {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X2 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> S foo(Class<T> s) { return null; } }\n" +
				"interface I { <T> Object foo(Class<T> s); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X3 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> Object foo(Class<T> s) { return null; } }\n" +
				"interface I { <T, S> S foo(Class<T> s); }\n",
				"X.java",
				"abstract class X4 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X4 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",

				"X.java",
				"class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\r\n" + 
			"	                                                  ^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(Class<T>) of type X5 has the same erasure as foo(Class<T>) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in X5 and <T,S>foo(java.lang.Class<T>) in A have the same erasure, yet neither overrides the other
		);
	}

	public void test014() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String>[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String> a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String>[] a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in X have the same erasure, yet neither overrides the other
		);
	}

	public void test015() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String>[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String> a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X extends Y implements I { }\r\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X extends Y implements I { }\r\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test016() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static <E extends A> void m(E e) { System.out.print(\"A=\"+e.getClass()); }\n" + 
				"	static <E extends B> void m(E e) { System.out.print(\"B=\"+e.getClass()); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		m(new A());\n" +
				"		m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"A=class AB=class B"
		);
	}

	public void test017() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<? extends Number> a) {} }\n" + 
				"class Z extends Y { void test(X<Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	class Z extends Y { void test(X<Number> a) {} }\r\n" + 
			"	                         ^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<Number>) of type Z has the same erasure as test(X<? extends Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<java.lang.Number>) in Z and test(X<? extends java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<Number> a) {} }\n" + 
				"class Z extends Y { void test(X<? extends Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	class Z extends Y { void test(X<? extends Number> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<? extends Number>) of type Z has the same erasure as test(X<Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<? extends java.lang.Number>) in Z and test(X<java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
	}

	public void test018() { // 77861
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X implements Comparable<X> {\n" + 
				"	public int compareTo(Object o) { return 0; }\n" + 
				"	public int compareTo(X o) { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	public void test019() { // 78140
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n"
			},
			""
		);
	}

	public void test020() { // 78232
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		AbstractBase ab = new AbstractBase();\n" + 
				"		Derived d = new Derived();\n" + 
				"		AbstractBase ab2 = new Derived();\n" + 
				"		Visitor<String, String> v = new MyVisitor();\n" + 
				"		System.out.print(ab.accept(v, ab.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(d.accept(v, d.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(ab2.accept(v, ab2.getClass().getName()));\n" + 
				"	}\n" + 
				"	static class MyVisitor implements Visitor<String, String> {\n" + 
				"		public String visitBase(AbstractBase ab, String obj) { return \"Visited base: \" + obj; }\n" + 
				"		public String visitDerived(Derived d, String obj) { return \"Visited derived: \" + obj; }\n" + 
				"	}\n" + 
				"}\n" + 
				"interface Visitor<R, T> {\n" + 
				"	R visitBase(AbstractBase ab, T obj);\n" + 
				"	R visitDerived(Derived d, T obj);\n" + 
				"}\n" + 
				"interface Visitable {\n" + 
				"	<R, T> R accept(Visitor<R, T> v, T obj);\n" + 
				"}\n" + 
				"class AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitBase(this, obj); }\n" + 
				"}\n" + 
				"class Derived extends AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitDerived(this, obj); }\n" + 
				"}\n"
			},
			"Visited base: AbstractBase+Visited derived: Derived+Visited derived: Derived"
		);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n",
				"B.java",
				"class B extends A {\n" + 
				"	void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
		);
		// now save A & pick it up as a binary type
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"class B extends A {\n" + 
				"	void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n",
			null,
			false,
			null
		);
	}

	public void test022() { // 77562
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List getList() { return null; } }\n" + 
				"class B extends A { List<String> getList() { return null; } }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List<String> getList() { return null; } }\n" + 
				"class B extends A { List getList() { return null; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { List getList() { return null; } }\n" + 
			"	                    ^^^^\n" + 
			"Type safety: The return type List of the method getList() of type B needs unchecked conversion to conform to the return type List<String> of inherited method\n" + 
			"----------\n"
			// unchecked warning on B.getList()
		);
	}

	public void test023() { // 80739
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A<T> {\n" + 
				"	void foo(T t) {}\n" + 
				"	void foo(String i) {}\n" + 
				"}\n" + 
				"class B extends A<String> {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	class B extends A<String> {}\r\n" + 
			"	      ^\n" + 
			"Duplicate methods named foo with the parameters (String) and (T) are defined by the type A<String>\n" + 
			"----------\n"
			// methods foo(T) from A<java.lang.String> and foo(java.lang.String) from A<java.lang.String> are inherited with the same signature
		);
	}

	public void test024() { // 80626
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n"
			},
			""
			// no complaint
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type B has the same erasure as m(Object) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in B and m(java.lang.Object) in A have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n" + 
				"class C extends B {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 8)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type C has the same erasure as m(Object) of type B but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in C and m(java.lang.Object) in B have the same erasure, yet neither overrides the other
		);
	}

	public void test025() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	Integer test() { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	Integer test() { return 1; }\n" + 
			"	^^^^^^^\n" + 
			"Type safety: The return type Integer of the method test() of type B needs unchecked conversion to conform to the return type T of inherited method\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T[] test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	Integer[] test() { return new Integer[] {2}; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	Integer[] test() { return new Integer[] {2}; }\n" + 
			"	^^^^^^^^^\n" + 
			"Type safety: The return type Integer[] of the method test() of type B needs unchecked conversion to conform to the return type T[] of inherited method\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Integer>test(new Integer(1)));\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T> T test(T t) { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	<T> T test(T t) { return t; }\n" + 
				"}\n"
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Number>test(1));\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {\n" + 
				"	<U> T test(U u) { return null; }\n" + 
				"}\n" +
				"class B extends A<Integer> {\n" + 
				"	<U> Integer test(U u) { return 1; }\n" + 
				"}\n"
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.concurrent.Callable;\n" + 
				"public class A {\n" + 
				"	public static void main(String[] args) throws Exception {\n" + 
				"		Callable<Integer> integerCallable = new Callable<Integer>() {\n" + 
				"			public Integer call() { return new Integer(1); }\n" + 
				"		};\n" + 
				"		System.out.println(integerCallable.call());\n" + 
				"	}\n" + 
				"}\n"
			},
			"1"
		);
	}

	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(\n" + 
				"			new B().test().getClass() + \" & \"\n" + 
				"			+ new C().test().getClass() + \" & \"\n" + 
				"			+ new D().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	A test() { return super.test(); }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	A<Integer> test() { return super.test(); }\n" + 
				"}\n" +
				"class D<U, V extends Number> extends A<V> {\n" + 
				"	A<V> test() { return super.test(); }\n" + 
				"}\n"
			},
			"class B & class C & class D"
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"public abstract class A<E> {\n" + 
				"	public abstract A<E> test();\n" + 
				"}\n" +
				"class H<K,V> {\n" + 
				"	class M extends A<K> {\n" + 
				"		public A<K> test() { return null; }\n" + 
				"	}\n" +
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends java.util.AbstractMap {\n" + 
				"	public java.util.Set entrySet() { return null; }\n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(new C().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	A test() { return super.test(); }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	A test() { return super.test(); }\n" + 
			"	^\n" + 
			"Type safety: The return type A of the method test() of type C needs unchecked conversion to conform to the return type A<T> of inherited method\n" + 
			"----------\n"
			// warning: test() in C overrides test() in A; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { <T> void test() {} }\n" + 
				"class Y extends X { void test() {} }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test() {} }\n" + 
				"class Y extends X { <T> void test() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { <T> void test() {} }\n" + 
			"	                             ^^^^^^\n" + 
			"Name clash: The method test() of type Y has the same erasure as test() of type X but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo() in Y and foo() in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class Y<T> extends X<T> { void test(Object o) {} }\r\n" + 
			"	                               ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Object) of type Y<T> has the same erasure as test(T) of type X<T> but does not override it\n" + 
			"----------\n"
			// no error unless you try to do a super send which then fails
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o, T t) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o, T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class Y<T> extends X<T> { void test(Object o, T t) {} }\r\n" + 
			"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Object, T) of type Y<T> has the same erasure as test(T, T) of type X<T> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Object,T) in Y<T> and test(T,T) in X<T> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void test() {\n" + 
				"		Pair<Double, Integer> p = new InvertedPair<Integer, Double>();\n" + 
				"		p.setA(new Double(1.1));\n" + 
				"	}\n" + 
				"}\n" +
				"class Pair<A, B> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n" +
				"class InvertedPair<A, B> extends Pair<B, A> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	public void setA(A a) {}\n" + 
			"	            ^^^^^^^^^\n" + 
			"Name clash: The method setA(A) of type InvertedPair<A,B> has the same erasure as setA(A) of type Pair<A,B> but does not override it\n" + 
			"----------\n"
			// name clash: setA(A) in InvertedPair<A,B> and setA(A) in Pair<B,A> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81727
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I<X>{\n" + 
				"	public X foo() { return null; }\n" + 
				"}\n" +
				"interface I<T extends I> { T foo(); }\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81568
	public void test029() {
		this.runConformTest(
			new String[] {
				"I.java",
				"public interface I {\n" + 
				"	public I clone();\n" + 
				"}\n" +
				"interface J extends I {}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81535
	public void test030() {
		java.util.Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);	

		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.OutputStreamWriter;\n" + 
				"import java.io.PrintWriter;\n" + 
				"public class X extends PrintWriter implements Runnable {\n" + 
				"	public X(OutputStreamWriter out, boolean flag) { super(out, flag); }\n" +
				"	public void run() {}\n" +
				"}\n"
			},
			"",
			null, // use default class-path
			false, // do not flush previous output dir content
			null, // no special vm args
			options,
			null
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80743
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X { long hashCode(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	interface X { long hashCode(); }\r\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"The return type is incompatible with Object.hashCode()\n" + 
			"----------\n"
			// hashCode() in X cannot override hashCode() in java.lang.Object; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736
	public void test032() {
		// NOTE: javac only reports these errors when the problem type follows the bounds
		// if the type X is defined first, then no errors are reported
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { Float foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X<T extends I&J> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	public class X<T extends I&J> {}\r\n" + 
			"	             ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n"
			// types J and I are incompatible; both define foo(), but with unrelated return types
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { String foo(); }\n" +
				"class A { public Object foo() { return null; } }" +
				"public class X<T extends A&I> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class A { public Object foo() { return null; } }public class X<T extends A&I> {}\r\n" + 
			"	                                                             ^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
			// foo() in A cannot implement foo() in I; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo() {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(A a); }\n" +
				"interface J<T> { Integer foo(A<T> a); }\n" +
				"class A<T>{}\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo(A a) {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034() {
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(String i) {} }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> {}\n" +
				"class C extends B { public void test(Object i) {} }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n" + 
			"	                                                             ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"abstract class AA<E extends Comparable> { abstract void test(E element); }\n" +
				"class A extends AA<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type AA<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in AA<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"interface I<U>{ int compareTo(U o); }\n" +
				"abstract class F<T extends F<T>> implements I<T>{ public final int compareTo(T o) { return 0; } }\n" +
				"public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 3)\n" + 
			"	public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n" + 
			"	                                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type E has the same erasure as compareTo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in E and compareTo(U) in I<E> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	;\n" +
				"	public int compareTo(Object o) { return 0; }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036() { // 2 interface cases
		// no bridge methods are created in these conform cases so no name clashes can occur
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T extends Number> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<S> implements Equivalent<S>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<S> implements Equivalent<S>, EqualityComparable<S> {}\n" +
				"class Z<U> extends Y<U> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(T other) { return true; }\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {}\n" +
				"class Z<U extends Comparable, V extends Number> extends Y<U, V> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"	public boolean equalTo(V other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<S extends Number> { boolean equalTo(S other); }\n"
			},
			""
		);

		// in these cases, bridge methods are needed once abstract/concrete methods are defiined (either in the abstract class or a concrete subclass)
		this.runConformTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Number other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
			// no bridge methods are created here since Y does not define an equalTo(?) method which equals an inherited equalTo method
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Object other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(java.lang.Object) in Y and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
		// NOTE: javac has a bug, reverse the implemented interfaces & the name clash goes away
		// but eventually when a concrete subclass must define the remaining method, the error shows up
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(String other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type Equivalent<T> has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in Equivalent<java.lang.String> and equalTo(T) in EqualityComparable<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" +
				"	public boolean equalTo(Integer other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in EqualityComparable<java.lang.Integer> and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}

	public void test037() { // test inheritance scenarios
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X implements I, J { }\n" +
				"abstract class Y implements J, I { }\n" +
				"abstract class Z implements K { }\n" +

				"class YYY implements J, I { public void foo(A a) {} }\n" +
				"class XXX implements I, J { public void foo(A a) {} }\n" +
				"class ZZZ implements K { public void foo(A a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public abstract class XX implements I, J { public abstract void foo(A<String> a); }\n" +
				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\r\n" + 
			"	public abstract class XX implements I, J { public abstract void foo(A<String> a); }\r\n" + 
			"	                                                                ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in XX and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public class XX implements I, J { public void foo(A<String> a) {} }\n" +
				"class YY implements J, I { public void foo(A<String> a) {} }\n" +
				"class ZZ implements K { public void foo(A<String> a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	             ^^\n" + 
			"The type XX must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	                                              ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"3. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type YY must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"4. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type YY has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"5. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type ZZ must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"6. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type ZZ has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// XX/YY/ZZ is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A a); }\n" +
				"class Y { void foo(A<String> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A<String> a); }\n" +
				"class Y { void foo(A a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"The inherited method Y.foo(A) cannot hide the public abstract method in I\n" + 
			"----------\n"
			// foo(A) in Y cannot implement foo(A<java.lang.String>) in I; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { <T, S> void foo(T t); }\n" +
				"class Y { <T> void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(T) of type Y has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in Y and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends H<Object> { void foo(A<?> a) { super.foo(a); } }\n" +
				"class H<T extends Object> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends H<Number> { void foo(A<?> a) {} }\n" +
				"class H<T extends Number> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X extends H<Number> { void foo(A<?> a) {} }\r\n" + 
			"	                                        ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<?>) of type X has the same erasure as foo(A<? extends T>) of type H<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<?>) in X and foo(A<? extends T>) in H<java.lang.Number> have the same erasure, yet neither overrides the other
			// with    public class X extends H<Number> { void foo(A<?> a) { super.foo(a); } }
			// foo(A<? extends java.lang.Number>) in H<java.lang.Number> cannot be applied to (A<capture of ?>)
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83573
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"      Test test = new Test();\n" + 
				"      This test2 = new Test();\n" + 
				"      System.out.println(test.get());\n" + 
				"   }\n" + 
				"   interface This {\n" + 
				"      public Object get();\n" + 
				"   }\n" + 
				" \n" + 
				"   interface That extends This {\n" + 
				"      public String get();\n" + 
				" \n" + 
				"   }\n" + 
				" \n" + 
				"   static class Test implements That {\n" + 
				" \n" + 
				"      public String get() {\n" + 
				"         return \"That\";\n" + 
				" \n" + 
				"      }\n" + 
				"   }\n" + 
				"}\n"
			},
			"That"
		);
	}	
}