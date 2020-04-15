/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class DeprecatedTest extends AbstractRegressionTest {
public DeprecatedTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public void test1() {
	this.runNegativeTest(new String[] {
		"p/B.java",
		"package p;\n" + 
		"class B extends A {\n" + 
		"    float x = super.x;\n" + 
		"}\n",

		"p/A.java",
		"package p;\n" + 
		"class A {\n" + 
		"    /** @deprecated */\n" + 
		"    int x = 1;\n" + 
		"}\n",
	}, 
		"----------\n" + 
		"1. WARNING in p\\B.java (at line 3)\n" + 
		"	float x = super.x;\n" + 
		"	      ^\n" + 
		"The field B.x is hiding a field from type A\n" + 
		"----------\n" + 
		"2. WARNING in p\\B.java (at line 3)\n" + 
		"	float x = super.x;\n" + 
		"	          ^^^^^^^\n" + 
		"The field A.x is deprecated\n" + 
		"----------\n"
	);
}
public void test2() {
	this.runNegativeTest(new String[] {
		"p/C.java",
		"package p;\n" + 
		"class C {\n" + 
		"    static int x = new A().x;\n" + 
		"}\n",
		
		"p/A.java",
		"package p;\n" + 
		"class A {\n" + 
		"    /** @deprecated */\n" + 
		"    int x = 1;\n" + 
		"}\n",

	}, 
		"----------\n" + 
		"1. WARNING in p\\C.java (at line 3)\n" + 
		"	static int x = new A().x;\n" + 
		"	                       ^\n" + 
		"The field A.x is deprecated\n" + 
		"----------\n"
	);
}
public void test3() {
	this.runNegativeTest(new String[] {
		"p/Top.java",
		"package p;\n" + 
		"public class Top {\n" + 
		"  \n" + 
		"  class M1 {\n" + 
		"    class M2 {}\n" + 
		"  };\n" + 
		"  \n" + 
		"  static class StaticM1 {\n" + 
		"    static class StaticM2 {\n" + 
		"      class NonStaticM3{}};\n" + 
		"  };\n" + 
		"  \n" + 
		"public static void main(String argv[]){\n" + 
		"  Top tip = new Top();\n" + 
		"  System.out.println(\"Still alive 0\");\n" + 
		"  tip.testStaticMember();\n" + 
		"  System.out.println(\"Still alive 1\");\n" + 
		"  tip.testStaticMember1();\n" + 
		"  System.out.println(\"Still alive 2\");\n" + 
		"  tip.testStaticMember2();\n" + 
		"  System.out.println(\"Still alive 3\");\n" + 
		"  tip.testStaticMember3();\n" + 
		"  System.out.println(\"Still alive 4\");\n" + 
		"  tip.testStaticMember4();\n" + 
		"  System.out.println(\"Completed\");\n" + 
		"}\n" + 
		"  void testMember(){\n" + 
		"    new M1().new M2();}\n" + 
		"  void testStaticMember(){\n" + 
		"    new StaticM1().new StaticM2();}\n" + 
		"  void testStaticMember1(){\n" + 
		"    new StaticM1.StaticM2();}\n" + 
		"  void testStaticMember2(){\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3();}\n" + 
		"  void testStaticMember3(){\n" + 
		"    // define an anonymous subclass of the non-static M3\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3(){};\n" + 
		"  }   \n" + 
		"  void testStaticMember4(){\n" + 
		"    // define an anonymous subclass of the non-static M3\n" + 
		"    new StaticM1.StaticM2().new NonStaticM3(){\n" + 
		"      Object hello(){\n" + 
		"        return new StaticM1.StaticM2().new NonStaticM3();\n" + 
		"      }};\n" + 
		"      \n" + 
		"  }    \n" + 
		"}\n",
		}, 
		"----------\n" + 
		"1. ERROR in p\\Top.java (at line 30)\n" + 
		"	new StaticM1().new StaticM2();}\n" + 
		"	^^^^^^^^^^^^^^\n" + 
		"Illegal enclosing instance specification for type Top.StaticM1.StaticM2\n" + 
		"----------\n"	);
}
/**
 * Regression test for PR #1G9ES9B
 */
public void test4() {
	this.runNegativeTest(new String[] {
		"p/Warning.java",
		"package p;\n" + 
		"import java.util.Date;\n" +
		"public class Warning {\n" +
		"public Warning() {\n" +
		"     super();\n" +
		"     Date dateObj = new Date();\n" +
		"     dateObj.UTC(1,2,3,4,5,6);\n" +
		"}\n" +
		"}\n",
		}, 
		"----------\n" + 
		"1. WARNING in p\\Warning.java (at line 7)\n" + 
		"	dateObj.UTC(1,2,3,4,5,6);\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method UTC(int, int, int, int, int, int) from the type Date is deprecated\n" + 
		"----------\n" + 
		"2. WARNING in p\\Warning.java (at line 7)\n" + 
		"	dateObj.UTC(1,2,3,4,5,6);\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The static method UTC(int, int, int, int, int, int) from the type Date should be accessed in a static way\n" + 
		"----------\n"

	);
}
public void test5() {
	this.runConformTest(
		new String[] {
			"X.java",
		  "public class X {\n"
			+ "/**\n"
			+ " * @deprecated\n"
			+ " */\n"
			+ " 	public static class Y {\n"
			+ "	}\n" +
			"   public static void main(String[] args) {	\n" +
			"        System.out.print(\"SUCCESS\");	\n" +
			"	}	\n"
			+ "}"
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A extends X.Y {}"
		},
		"----------\n" + 
		"1. WARNING in A.java (at line 1)\n" + 
		"	public class A extends X.Y {}\n" + 
		"	             ^\n" + 
		"The constructor X.Y() is deprecated\n" + 
		"----------\n" + 
		"2. WARNING in A.java (at line 1)\n" + 
		"	public class A extends X.Y {}\n" + 
		"	                       ^^^\n" + 
		"The type X.Y is deprecated\n" + 
		"----------\n",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=40839
public void test6() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	  @deprecated\n" +
			"	 */\n" +
			"	; // line comment\n" +
			"	static int i;\n" +
			"   public static void main(String[] args) {	\n" +
			"        System.out.print(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}"
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"   public static void main(String[] args) {	\n" +
			"        System.out.print(X.i);	\n" +
			"	}	\n" +
			"}"
		},
		"",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
public static Class testClass() {
	return DeprecatedTest.class;
}
}
