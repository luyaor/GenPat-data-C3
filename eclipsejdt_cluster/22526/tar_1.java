/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import junit.framework.Test;

public class CastingContextTest extends AbstractRegressionTest {

static {
	//	TESTS_NAMES = new String[] { "test380112e"};
	//	TESTS_NUMBERS = new int[] { 50 };
	//	TESTS_RANGE = new int[] { 11, -1 };
}

public CastingContextTest(String name) {
	super(name);
}

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	return defaultOptions;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object o = () -> {};\n" +
				"	Object p = (I) () -> {};\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	Object o = () -> {};\n" + 
			"	           ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (I & J) () -> {};\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (I & J) () -> {};\n" + 
			"	                   ^^^^^^^^\n" + 
			"The target type of this expression is not a functional interface: more than one of the intersecting interfaces are functional\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (int & I & J) () -> {};\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (int & I & J) () -> {};\n" + 
			"	            ^^^\n" + 
			"Base types are not allowed in intersection cast operator\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Object p = (int & I & J) () -> {};\n" + 
			"	                         ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (X[] & Serializable & Cloneable) new X[0];\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (X[] & Serializable & Cloneable) new X[0];\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Arrays are not allowed in intersection cast operator\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Object p = (X[] & Serializable & Cloneable) new X[0];\n" + 
			"	                  ^^^^^^^^^^^^\n" + 
			"Serializable cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (I & X) () -> {};\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (I & X) () -> {};\n" + 
			"	                ^\n" + 
			"The type X is not an interface; it cannot be specified as a bounded parameter\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Object p = (I & X) () -> {};\n" + 
			"	                   ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (I & J & I) () -> {};\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (I & J & I) () -> {};\n" + 
			"	                    ^\n" + 
			"Duplicate type in intersection cast operator\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Object p = (I & J & I) () -> {};\n" + 
			"	                       ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test007() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"interface I<T> {\n" +
				"	void doit(List<T> x);\n" +
				"	default void doitalso () {}\n" +
				"	boolean equals(Object o);\n" +
				"}\n" +
				"public class X {\n" +
				"	I<String> i = (List<String> p) -> {};\n" +
				"	I<X> i2 = (List<String> p) -> {};\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	I<X> i2 = (List<String> p) -> {};\n" + 
			"	           ^^^^\n" + 
			"Lambda expression\'s parameter p is expected to be of type List<X>\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test008() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"interface J {\n" +
				"	void doit();\n" +
				"	default void doitalso () {}\n" +
				"}\n" +
				"public class X {\n" +
				"	Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};\n" +
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};\n" + 
			"	            ^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};\n" + 
			"	                                ^^^^^^^^\n" + 
			"Readonly cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};\n" + 
			"	                                                         ^^^^^^^^\n" + 
			"The target type of this expression is not a functional interface: more than one of the intersecting interfaces are functional\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test009() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"interface I<T> {\n" +
				"	void doit(List<T> x);\n" +
				"	boolean equals(Object o);\n" +
				"}\n" +
				"public class X {\n" +
				"	I<String> i = (List<String> p) -> {};\n" +
				"	I<X> i2 = (Map<String, String> & I<X>) null;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 9)\n" + 
			"	I<X> i2 = (Map<String, String> & I<X>) null;\n" + 
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from null to Map<String,String> & I<X>\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test010() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"interface I<T> {\n" +
				"	void doit(List<T> x);\n" +
				"	boolean equals(Object o);\n" +
				"}\n" +
				"public class X {\n" +
				"	I<String> i = (List<String> p) -> {};\n" +
				"	I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The member type Map<String,String>.Entry cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;\n" + 
			"	                                              ^^^^^^^^^^^^\n" + 
			"Serializable cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test011() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"public class X {\n" +
				"	X X = (X & J & K) new Y();\n" +
				"}\n" +
				"class Y {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	X X = (X & J & K) new Y();\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot cast from Y to X & J & K\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test012() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"public class X {\n" +
				"	X X = (X & J & K) new Y();\n" +
				"}\n" +
				"class Y extends X implements Zork {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	X X = (X & J & K) new Y();\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from Y to X & J & K\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	class Y extends X implements Zork {\n" + 
			"	                             ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test013() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"public class X {\n" +
				"	X X = (X & J & K) new Y();\n" +
				"}\n" +
				"final class Y extends X {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	X X = (X & J & K) new Y();\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot cast from Y to X & J & K\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	X X = (X & J & K) new Y();\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from Y to X & J & K\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test014() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"public class X {\n" +
				"   I i = null;\n" +
				"	X X = (X & J & K) i;\n" +
				"}\n" +
				"final class Y extends P {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	final class Y extends P {\n" + 
			"	                      ^\n" + 
			"P cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test015() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"final public class X {\n" +
				"   I i = null;\n" +
				"	X X = (X & J & K) i;\n" +
				"   Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	X X = (X & J & K) i;\n" + 
			"	      ^^^^^^^^^^^^^\n" + 
			"Cannot cast from I to X & J & K\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test016() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"final public class X implements I {\n" +
				"   I i = null;\n" +
				"	X X = (X & J & K) i;\n" +
				"   Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test017() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"}\n" +
				"interface J {\n" +
				"}\n" +
				"interface K {\n" +
				"}\n" +
				"public class X {\n" +
				"   I i = null;\n" +
				"	X X = (X & J & K) (X & K & J) i;\n" +
				"   Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 9)\n" + 
			"	X X = (X & J & K) (X & K & J) i;\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from X & K & J to X & J & K\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
public static Class testClass() {
	return CastingContextTest.class;
}
}