/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocBugsTest extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;

	public JavadocBugsTest(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocBugsTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug83127";
//		TESTS_NAMES = new String[] { "testBug68017javadocWarning2" };
		TESTS_NUMBERS = new int[] { 129241 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildTestSuite(javadocTestClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		if (this.reportJavadocDeprecation != null) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, reportJavadocDeprecation);
		}
		if (reportMissingJavadocComments != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportInvalidJavadoc);
		}
		if (reportMissingJavadocTags != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportMissingJavadocTags);
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportInvalidJavadoc);
		}
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, reportDeprecation);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		docCommentSupport = CompilerOptions.ENABLED;
		reportInvalidJavadoc = CompilerOptions.ERROR;
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		reportDeprecation = CompilerOptions.ERROR;
	}

	/**
	 * Bug 45596.
	 * When this bug happened, compiler wrongly complained on missing parameter javadoc
	 * entries for method declaration in anonymous class.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
	 */
	public void testBug45596() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
			 	+ "class X {\n"
					+ "	void foo(int x, String str) {}\n"
			  		+ "}\n",
				"test/Y.java",
				"package test;\n"
			   		+ "class Y {\n"
			   		+ "  /** */\n"
			   		+ "  protected X field = new X() {\n"
			   		+ "    void foo(int x, String str) {}\n"
			   		+ "  };\n"
			   		+ "}\n"});
	}

	/**
	 * Additional test for bug 45596.
	 * Verify correct complain about missing parameter javadoc entries in anonymous class.
	 * Since bug 47132, @param, @return and @throws tags are not resolved in javadoc of anonymous
	 * class...
	 */
	public void testBug45596a() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(int x, String str) {}\n" + 
				"}\n",
				"Y1.java",
				"public class Y1 {\n" + 
				"	/** */\n" +
				"	protected X field = new X() {\n" + 
				"		/** Invalid javadoc comment in anonymous class */\n" + 
				"		void foo(String str) {}\n" + 
				"	};\n" + 
				"}\n",
				"Y2.java",
				"public class Y2 {\n" + 
				"	/** */\n" + 
				"	void foo() {\n" + 
				"		X x = new X() {\n" + 
				"			/** Invalid javadoc comment in anonymous class */\n" + 
				"			void foo(String str) {}\n" + 
				"		};\n" + 
				"		x.foo(0, \"\");\n" + 
				"	}\n" + 
				"}\n",
				"Y3.java",
				"public class Y3 {\n" + 
				"	static X x;\n" + 
				"	static {\n" + 
				"		x = new X() {\n" + 
				"			/** Invalid javadoc comment in anonymous class */\n" + 
				"			void foo(String str) {}\n" + 
				"		};\n" + 
				"	}\n" + 
				"}\n" }
			);
	}

	/**
	 * Additional test for bug 45596.
	 * Verify no complain about missing parameter javadoc entries.
	 */
	public void testBug45596b() {
		runConformTest(
			new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo(int x, String str) {}\n" + 
		"}\n",
		"Y1.java",
		"public class Y1 {\n" + 
		"	/** */\n" + 
		"	protected X field = new X() {\n" + 
		"		/**\n" + 
		"		 * Valid javadoc comment in anonymous class.\n" + 
		"		 * @param str String\n" + 
		"		 * @return int\n" + 
		"		 */\n" + 
		"		int bar(String str) {\n" + 
		"			return 10;\n" + 
		"		}\n" + 
		"	};\n" + 
		"}\n",
		"Y2.java",
		"public class Y2 {\n" + 
		"	/** */\n" + 
		"	void foo() {\n" + 
		"		X x = new X() {\n" + 
		"			/**\n" + 
		"			 * Valid javadoc comment in anonymous class.\n" + 
		"			 * @param str String\n" + 
		"			 * @return int\n" + 
		"			 */\n" + 
		"			int bar(String str) {\n" + 
		"				return 10;\n" + 
		"			}\n" + 
		"		};\n" + 
		"		x.foo(0, \"\");\n" + 
		"	}\n" + 
		"}\n",
		"Y3.java",
		"public class Y3 {\n" + 
		"	static X x;\n" + 
		"	static {\n" + 
		"		x = new X() {\n" + 
		"			/**\n" + 
		"			 * Valid javadoc comment in anonymous class.\n" + 
		"			 * @param str String\n" + 
		"			 * @return int\n" + 
		"			 */\n" + 
		"			int bar(String str) {\n" + 
		"				return 10;\n" + 
		"			}\n" + 
		"		};\n" + 
		"	}\n" + 
		"}\n"}
			);
	}

	/**
	 * Bug 45592.
	 * When this bug happened, a NullPointerException occured during the compilation.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45592">45592</a>
	 */
	public void testBug45592() {
		runConformTest(
			new String[] {
		"a/Y.java",
		"package a;\n" + 
		"\n" + 
		"/** */\n" + 
		"public class Y {\n" + 
		"	protected boolean bar(Object obj) {\n" + 
		"		return obj == null;\n" + 
		"	}\n" + 
		"}\n",
		"test/X.java",
		"package test;\n" + 
		"public class X {\n" + 
		"	public static Boolean valueOf(boolean bool) {\n" + 
		"		if (bool) {\n" + 
		"			return Boolean.TRUE;\n" + 
		"		} else {\n" + 
		"			return Boolean.FALSE;\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
		"test/YY.java",
		"package test;\n" + 
		"\n" + 
		"import a.Y;\n" + 
		"\n" + 
		"/** */\n" + 
		"public class YY extends Y {\n" + 
		"	/**\n" + 
		"	 * Returns a Boolean.\n" + 
		"	 * @param key\n" + 
		"	 * @return A Boolean telling whether the key is null or not.\n" + 
		"	 * @see #bar(Object)\n" + 
		"	 */\n" + 
		"	protected Boolean foo(Object key) {\n" + 
		"		return X.valueOf(bar(key));\n" + 
		"	}\n" + 
		"}\n"
		}
			);
	}

	/**
	 * Bug 45737.
	 * When this bug happened, compiler complains on return type and argument of method bar.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45737">45737</a>
	 */
	public void testBug45737() {
		runConformTest(
			new String[] {
				"Y.java",
				"class Y {\n" + 
				"	void foo() {\n" + 
				"		X x = new X() {\n" + 
				"			/**\n" + 
				"			 * Valid javadoc comment in anonymous class.\n" + 
				"			 * @param str String\n" + 
				"			 * @return int\n" + 
				"			 */\n" + 
				"			int bar(String str) {\n" + 
				"				return 10;\n" + 
				"			}\n" + 
				"		};\n" + 
				"		x.foo();\n" + 
				"	}\n" + 
				"}\n",
				"X.java",
				"class X {\n" + 
				"	void foo() {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 45669.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45669">45669</a>
	 */
	public void testBug45669() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * Valid javadoc comment with tags mixed order\n" + 
				"	 * @param str first param\n" + 
				"	 * 		@see String\n" + 
				"	 * @param dbl second param\n" + 
				"	 * 		@see Double\n" + 
				"	 * 		also\n" + 
				"	 * 		@see \"String ref\"\n" + 
				"	 * @return int\n" + 
				"	 * @throws InterruptedException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	int foo(String str, Double dbl) throws InterruptedException {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	/*
	 * Additional test for bug 45669.
	 * Verify that compiler complains when @throws tag is between @param tags.
	 */
	public void testBug45669a() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * Javadoc comment with tags invalid mixed order\n" + 
				"	 * @param str first param\n" + 
				"	 * 		@see String\n" + 
				"	 * @throws InterruptedException\n" + 
				"	 * @param dbl second param\n" + 
				"	 * 		@see Double\n" + 
				"	 * 		also\n" + 
				"	 * 		@see \"String ref\"\n" + 
				"	 * @return int\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	public int foo(String str, Double dbl) throws InterruptedException {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	* @param dbl second param\n" + 
		"	   ^^^^^\n" + 
		"Javadoc: Unexpected tag\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	public int foo(String str, Double dbl) throws InterruptedException {\n" + 
		"	                                  ^^^\n" + 
		"Javadoc: Missing tag for parameter dbl\n" + 
		"----------\n"
		);
	}

	/**
	 * Bug 45958.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45958">45958</a>
	 */
	public void testBug45958() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int x;\n" + 
				"	public X(int i) {\n" + 
				"		x = i;\n" + 
				"	}\n" + 
				"	/**\n" + 
				"	 * @see #X(int)\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug45958a() {
		runNegativeTest(
			new String[] {
			   "X.java",
		   		"public class X {\n" + 
		   		"	int x;\n" + 
		   		"	public X(int i) {\n" + 
		   		"		x = i;\n" + 
		   		"	}\n" + 
		   		"	/**\n" + 
		   		"	 * @see #X(String)\n" + 
		   		"	 */\n" + 
		   		"	public void foo() {\n" + 
		   		"	}\n" + 
		   		"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	* @see #X(String)\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: The constructor X(String) is undefined\n" + 
				"----------\n"
		);
	}
	public void testBug45958b() {
		runNegativeTest(
			new String[] {
			   "X.java",
		   		"public class X {\n" + 
		   		"	int x;\n" + 
		   		"	public X(int i) {\n" + 
		   		"		x = i;\n" + 
		   		"	}\n" + 
		   		"	/**\n" + 
		   		"	 * @see #X(int)\n" + 
		   		"	 */\n" + 
		   		"	public void foo() {\n" + 
		   		"	}\n" + 
		   		"}\n",
		   		"XX.java",
		   		"public class XX extends X {\n" + 
		   		"	/**\n" + 
		   		"	 * @param i\n" + 
		   		"	 * @see #X(int)\n" + 
		   		"	 */\n" + 
		   		"	public XX(int i) {\n" + 
		   		"		super(i);\n" + 
		   		"		x++;\n" + 
		   		"	}\n" + 
		   		"}\n"
			},
			"----------\n" + 
				"1. ERROR in XX.java (at line 4)\n" + 
				"	* @see #X(int)\n" + 
				"	        ^\n" + 
				"Javadoc: The method X(int) is undefined for the type XX\n" + 
				"----------\n"
			);
	}
	public void testBug45958c() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int x;\n" + 
				"	public X(int i) {\n" + 
				"		x = i;\n" + 
				"	}\n" + 
				"	/**\n" + 
				"	 * @see #X(String)\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"	void X(String str) {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 46901.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=46901">46901</a>
	 */
	public void testBug46901() {
		runConformTest(
			new String[] {
				"A.java",
				"public abstract class A {\n" + 
				"	public A() { super(); }\n" + 
				"}\n",
				"X.java",
				"/**\n" + 
				" * @see A#A()\n" + 
				" */\n" + 
				"public class X extends A {\n" + 
				"	public X() { super(); }\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 47215.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47215">47215</a>
	 */
	public void testBug47215() {
		runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n" + 
				"	 * @see X\n" + 
				"	 * @see X#X(int)\n" + 
				"	 * @see X(double)\n" + 
				"	 * @see X   (double)\n" + 
				"	 * @see X[double]\n" + 
				"	 * @see X!=}}\n" + 
				"	 * @see foo()\n" + 
				"	 * @see foo  ()\n" + 
				"	 */\n" + 
				"	public class X {\n" + 
				"		public X(int i){}\n" + 
				"		public void foo() {}\n" + 
				"	}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @see X(double)\n" + 
				"	       ^^^^^^^^^\n" + 
				"Javadoc: Missing #: \"X(double)\"\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @see X[double]\n" + 
				"	       ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @see X!=}}\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see foo()\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Missing #: \"foo()\"\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* @see foo  ()\n" + 
				"	       ^^^\n" + 
				"Javadoc: foo cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 47341.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47341">47341</a>
	 */
	public void testBug47341() {
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
				"public class X {\n" + 
				"	void foo_package() {}\n" + 
				"	protected void foo_protected() {}\n" + 
				"}\n",
				"p1/Y.java",
				"package p1;\n" + 
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see #foo_package()\n" + 
				"	 */\n" + 
				"	protected void bar() {\n" + 
				"		foo_package();\n" + 
				"	}\n" + 
				"}\n",
				"p2/Y.java",
				"package p2;\n" + 
				"import p1.X;\n" + 
				"\n" + 
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#foo_protected()\n" + 
				"	 */\n" + 
				"	protected void bar() {\n" + 
				"		foo_protected();\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 47132.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47132">47132</a>
	 */
	public void testBug47132() {
		reportMissingJavadocComments = CompilerOptions.ERROR;
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"  /** */\n" + 
				"  public void foo(){\n" + 
				"    new Object(){\n" + 
				"		public int x;\n" + 
				"       public void bar(){}\n" + 
				"    };\n" + 
				"  }\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 47339.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47339">47339</a>
	 */
	public void testBug47339() {
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X implements Comparable {\n" + 
				"	/**\n" + 
				"	 * @see java.lang.Comparable#compareTo(java.lang.Object)\n" + 
				"	 */\n" + 
				"	public int compareTo(Object o) {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	/** @see Object#toString() */\n" + 
				"	public String toString(){\n" + 
				"		return \"\";\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug47339a() {
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X extends RuntimeException {\n" + 
				"	\n" + 
				"	/**\n" + 
				"	 * @see RuntimeException#RuntimeException(java.lang.String)\n" + 
				"	 */\n" + 
				"	public X(String message) {\n" + 
				"		super(message);\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug47339b() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X implements Comparable {\n" + 
				"	/** */\n" + 
				"	public int compareTo(Object o) {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	/** */\n" + 
				"	public String toString(){\n" + 
				"		return \"\";\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	public int compareTo(Object o) {\n" + 
				"	       ^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public int compareTo(Object o) {\n" + 
				"	                            ^\n" + 
				"Javadoc: Missing tag for parameter o\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	public String toString(){\n" + 
				"	       ^^^^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n"
		);
	}
	public void testBug47339c() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X extends RuntimeException {\n" + 
				"	\n" + 
				"	/** */\n" + 
				"	public X(String message) {\n" + 
				"		super(message);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public class X extends RuntimeException {\n" + 
			"	             ^\n" + 
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	public X(String message) {\n" + 
			"	                ^^^^^^^\n" + 
			"Javadoc: Missing tag for parameter message\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 48064.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48064">48064</a>
	 */
	public void testBug48064() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public X(String str) {}\n" + 
				"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#X(STRING)\n" + 
				"	 */\n" + 
				"	public Y(String str) {super(str);}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	* @see X#X(STRING)\n" + 
			"	           ^^^^^^\n" + 
			"Javadoc: STRING cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 5)\n" + 
			"	public Y(String str) {super(str);}\n" + 
			"	                ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n"
		);
	}
	public void testBug48064a() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void foo(String str) {}\n" + 
				"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#foo(STRING)\n" + 
				"	 */\n" + 
				"	public void foo(String str) {super.foo(str);}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	* @see X#foo(STRING)\n" + 
			"	             ^^^^^^\n" + 
			"Javadoc: STRING cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 5)\n" + 
			"	public void foo(String str) {super.foo(str);}\n" + 
			"	                       ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 48523.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48523">48523</a>
	 */
	public void testBug48523() {
		runConformTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
					"public class X {\n" + 
					"	public void foo() throws IOException {}\n" + 
					"}\n",
				"Y.java",
				"import java.io.IOException;\n" + 
					"public class Y extends X {\n" + 
					"	/**\n" + 
					"	 * @throws IOException\n" + 
					"	 * @see X#foo()\n" + 
					"	 */\n" + 
					"	public void foo() throws IOException {}\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Bug 48711.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48711">48711</a>
	 */
	public void testBug48711() {
		runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @throws IOException\n" + 
				"	 * @throws EOFException\n" + 
				"	 * @throws FileNotFoundException\n" + 
				"	 */\n" + 
				"	public void foo() throws IOException {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 45782.
	 * When this bug happened, compiler wrongly complained on missing parameters declaration
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45782">45782</a>
	 */
	public void testBug45782() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X implements Comparable {\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Overridden method with return value and parameters.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public boolean equals(Object obj) {\n" + 
					"		return super.equals(obj);\n" + 
					"	}\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Overridden method with return value and thrown exception.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public Object clone() throws CloneNotSupportedException {\n" + 
					"		return super.clone();\n" + 
					"	}\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Implemented method (Comparable)  with return value and parameters.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public int compareTo(Object o) { return 0; }\n" + 
					"}\n"
			});
	}
	public void testBug45782a() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unefficient inheritDoc tag on a method which is neither overridden nor implemented...\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	* {@inheritDoc}\n" + 
			"	    ^^^^^^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
			"	       ^^^\n" + 
			"Javadoc: Missing tag for return type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
			"	                      ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
			"	                                  ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: Missing tag for declared exception IllegalArgumentException\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 49260.
	 * When this bug happened, compiler wrongly complained on Invalid parameters declaration
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49260">49260</a>
	 */
	public void testBug49260() {
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public final class X {\n" + 
					"	int bar(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"	/**\n" + 
					"	 * Valid method reference on several lines\n" + 
					"	 * @see #bar(String str,\n" + 
					"	 * 		int var,\n" + 
					"	 * 		Vector list,\n" + 
					"	 * 		char[] array)\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n" });
	}

	/**
	 * Bug 48385.
	 * When this bug happened, compiler does not complain on CharOperation references in @link tags
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48385">48385</a>
	 */
	public void testBug48385() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 * @param str\n" + 
					"	 * @param var tag description not empty\n" + 
					"	 * @param list third param with embedded tag: {@link Vector}\n" + 
					"	 * @param array fourth param with several embedded tags on several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@linkplain CharOperation Label not empty} tag description not empty\n" + 
					"	 * @throws IllegalAccessException\n" + 
					"	 * @throws NullPointerException tag description not empty\n" + 
					"	 * @return an integer\n" + 
					"	 * @see String\n" + 
					"	 * @see Vector tag description not empty\n" + 
					"	 * @see Object tag description includes embedded tags and several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 */\n" + 
					"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"}\n"},
			"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 12)\n" + 
				"	*  2) {@linkplain CharOperation Label not empty} tag description not empty\n" + 
				"	                  ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 20)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	public void testBug48385And49620() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link\n" + 
					"	 * 				String} tag description not empty\n" + 
					"	 *  2) {@link\n" + 
					"	 * 				CharOperation Label not empty} tag description not empty\n" + 
					"	 * @param\n" + 
					"	 * 				str\n" + 
					"	 * @param\n" + 
					"	 * 				var tag description not empty\n" + 
					"	 * @param list third param with embedded tag: {@link\n" + 
					"	 * 				Vector} but also on several lines: {@link\n" + 
					"	 * 				CharOperation}\n" + 
					"	 * @param array fourth param with several embedded tags on several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 * @throws\n" + 
					"	 * 					IllegalAccessException\n" + 
					"	 * @throws\n" + 
					"	 * 					NullPointerException tag description not empty\n" + 
					"	 * @return\n" + 
					"	 * 					an integer\n" + 
					"	 * @see\n" + 
					"	 * 			String\n" + 
					"	 * @see\n" + 
					"	 * 		Vector\n" + 
					"	 * 		tag description not empty\n" + 
					"	 * @see Object tag description includes embedded tags and several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 */\n" + 
					"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"}\n"},
			"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	* 				CharOperation Label not empty} tag description not empty\n" + 
				"	  				^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 15)\n" + 
				"	* 				CharOperation}\n" + 
				"	  				^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 18)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 32)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved to a type\n" + 
				"----------\n"
		);
	}
	public void testBug48385a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link } Missing reference\n" + 
					"	 *  2) {@link Unknown} Cannot be resolved\n" + 
					"	 *  3) {@link *} Missing reference\n" + 
					"	 *  4) {@link #} Invalid reference\n" + 
					"	 *  5) {@link String } } Valid reference\n" + 
					"	 *  6) {@link String {} Invalid tag\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  1) {@link } Missing reference\n" + 
				"	        ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	*  2) {@link Unknown} Cannot be resolved\n" + 
				"	             ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	*  3) {@link *} Missing reference\n" + 
				"	        ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	*  4) {@link #} Invalid reference\n" + 
				"	             ^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	*  6) {@link String {} Invalid tag\n" + 
				"	      ^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 49491.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49491">49491</a>
	 */
	public void testBug49491() {
		runConformTest(
			new String[] {
				"X.java",
				"public final class X {\n" + 
					"	/**\n" + 
					"	 * Now valid duplicated throws tag\n" + 
					"	 * @throws IllegalArgumentException First comment\n" + 
					"	 * @throws IllegalArgumentException Second comment\n" + 
					"	 * @throws IllegalArgumentException Last comment\n" + 
					"	 */\n" + 
					"	void foo() throws IllegalArgumentException {}\n" + 
					"}\n" });
	}
	public void testBug49491a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public final class X {\n" + 
					"	/**\n" + 
					"	 * Duplicated param tags should be still flagged\n" + 
					"	 * @param str First comment\n" + 
					"	 * @param str Second comment\n" + 
					"	 * @param str Last comment\n" + 
					"	 */\n" + 
					"	void foo(String str) {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param str Second comment\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param str Last comment\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 48376.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48376">48376</a>
	 */
	public void testBug48376() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* 			</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			</a>\n" + 
					"	* @see Object\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug48376a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page<\n" + 
					"	* 			/a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			\n" + 
					"	* @see Unknown\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">\n" + 
				"	*          IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	*          IBM Home Page<\n" + 
				"	                        ^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">\n" + 
				"	*\n" + 
				"	*          IBM\n" + 
				"	*\n" + 
				"	*          Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 16)\n" + 
				"	* @see Unknown\n" + 
				"	       ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 50644.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50644">50644</a>
	 */
	public void testBug50644() {
		reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Should not be @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"p2/Y.java",
				"package p2;\n" + 
					"import p1.X;\n" + 
					"public class Y {\n" + 
					"	public void foo() {\n" + 
					"		X x = new X();\n" + 
					"		x.foo();\n" + 
					"	}\n" + 
					"}\n"
		 });
	}

	/**
	 * Bug 50695.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50695">50695</a>
	 */
	public void testBug50695() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * @see java\n" + 
					"	 * @see java.util\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
		 });
	}
	public void testBug50695b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * @see java.unknown\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			 },
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @see java.unknown\n" + 
				"	       ^^^^^^^^^^^^\n" + 
				"Javadoc: java.unknown cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 51626.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51626">51626</a>
	 */
	public void testBug51626() {
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * @see String\n" + 
					"	 * toto @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"p2/Y.java",
				"package p2;\n" + 
					"import p1.*;\n" + 
					"public class Y {\n" + 
					"	void foo() {\n" + 
					"		X x = new X(); \n" + 
					"		x.foo();\n" + 
					"	}\n" + 
					"}\n"
		 });
	}

	/**
	 * Bug 52216.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=52216">52216</a>
	 */
	public void testBug52216() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * Valid ref with white spaces at the end\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug52216a() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"* @see \"Valid ref with white spaces at the end\"	   \n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug52216b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" + 
					"* @see \"Valid ref with white spaces at the end\"\n" + 
					"* @see \"Valid ref with white spaces at the end\"	   \n" + 
					"* @see \"Invalid ref\"	   .\n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
			 },
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" + 
				"	                                                                            ^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	* @see \"Invalid ref\"	   .\n" + 
				"	                    ^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 51529.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51529">51529</a>
	 */
	public void testBug51529() {
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
		 });
	}
	public void testBug51529a() {
		reportInvalidJavadoc = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
			}
		);
	}
	public void testBug51529b() {
		docCommentSupport = CompilerOptions.DISABLED;
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	import java.util.Vector;\n" + 
				"	       ^^^^^^^^^^^^^^^^\n" + 
				"The import java.util.Vector is never used\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 51911.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51911">51911</a>
	 */
	public void testBug51911() {
		// Warn an ambiguous method reference
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public void foo(int i, float f) {}\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see #foo\n" + 
				"	        ^^^\n" + 
				"Javadoc: foo is an ambiguous method reference or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug51911a() {
		// Accept unambiguous method reference
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}
	public void testBug51911b() {
		// Accept field reference with method name
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public int foo;\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}
	public void testBug51911c() {
		// Accept field reference with ambiguous method name
		runConformTest(
			new String[] {
				"X.java",
					"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public int foo;\n" +
					"	public void foo() {}\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}

	/**
	 * Bug 53279: [Javadoc] Compiler should complain when inline tag is not terminated
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53279">53279</a>
	 */
	public void testBug53279() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link Object\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link Object\n" + 
				"	   ^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}
	public void testBug53279a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link Object\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link Object\n" + 
				"	   ^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}
	public void testBug53279b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link        \n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link        \n" + 
				"	   ^^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	*  {@link        \n" + 
				"	     ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n"
		);
	}
	public void testBug53279c() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link\n" + 
				"	   ^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	*  {@link\n" + 
				"	     ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 53290: [Javadoc] Compiler should complain when tag name is not correct
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53290">53290</a>
	 */
	public void testBug53290() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * See as inline tag\n" + 
					"	 *  {@see Object}\n" + 
					"	 *  @see Object\n" + 
					"	 *  @link Object\n" + 
					"	 *  {@link Object}\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@see Object}\n" + 
				"	     ^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	*  @link Object\n" + 
				"	    ^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 62812: Some malformed javadoc tags are not reported as malformed
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=62812">62812</a>
	 */
	public void testBug62812() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * @see Object#clone())\n" + 
					" * @see Object#equals(Object)}\n" + 
					" * @see Object#equals(Object))\n" + 
					" * @see Object#equals(Object)xx\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 2)\n" + 
				"	* @see Object#clone())\n" + 
				"	                   ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in Test.java (at line 3)\n" + 
				"	* @see Object#equals(Object)}\n" + 
				"	                    ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in Test.java (at line 4)\n" + 
				"	* @see Object#equals(Object))\n" + 
				"	                    ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in Test.java (at line 5)\n" + 
				"	* @see Object#equals(Object)xx\n" + 
				"	                    ^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug62812a() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * {@link Object#clone())}\n" + 
					" * {@link Object#equals(Object)}\n" + 
					" * {@link Object#equals(Object))}\n" + 
					" * {@link Object#equals(Object)xx}\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 2)\n" + 
				"	* {@link Object#clone())}\n" + 
				"	                     ^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in Test.java (at line 4)\n" + 
				"	* {@link Object#equals(Object))}\n" + 
				"	                      ^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in Test.java (at line 5)\n" + 
				"	* {@link Object#equals(Object)xx}\n" + 
				"	                      ^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 51606: [Javadoc] Compiler should complain when tag name is not correct
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51606">51606</a>
	 */
	public void testBug51606() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"  *  @param a {@inheritDoc}\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Y.java (at line 5)\n" + 
				"	public void foo(int a, int b) {\n" + 
				"	                           ^\n" + 
				"Javadoc: Missing tag for parameter b\n" + 
				"----------\n"
		);
	}
	public void testBug51606a() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			},
			""
		);
	}
	public void testBug51606b() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * Text before inherit tag\n" + 
					"   * {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			}
		);
	}
	public void testBug51606c() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * Text before inherit tag {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Bug 65174: Spurious "Javadoc: Missing reference" error
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65174">65174</a>
	 */
	public void testBug65174() {
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174a() {
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link    		\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link   		\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174b() {
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link java.lang.\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link java.lang.\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174c() {
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link Object\n" + 
					" * valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with no error: {@link Object\n" + 
					"	 * valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174d() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	/** Comment previously with no error: {@link Object valid} comment on one line */\n" + 
					"	void foo1() {}\n" + 
					"	/** Comment previously with no error: {@link Object valid}       */\n" + 
					"	void foo2() {}\n" + 
					"	/** Comment previously with no error: {@link Object valid}*/\n" + 
					"	void foo3() {}\n" + 
					"	/**                    {@link Object valid} comment on one line */\n" + 
					"	void foo4() {}\n" + 
					"	/**{@link Object valid} comment on one line */\n" + 
					"	void foo5() {}\n" + 
					"	/**       {@link Object valid} 				*/\n" + 
					"	void foo6() {}\n" + 
					"	/**{@link Object valid} 				*/\n" + 
					"	void foo7() {}\n" + 
					"	/**				{@link Object valid}*/\n" + 
					"	void foo8() {}\n" + 
					"	/**{@link Object valid}*/\n" + 
					"	void foo9() {}\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Bug 65180: Spurious "Javadoc: xxx cannot be resolved or is not a field" error with inner classes
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65180">65180</a>
	 */
	public void testBug65180() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 6)\r\n" + 
				"	* @see #testFunc\r\n" + 
				"	        ^^^^^^^^\n" + 
				"Javadoc: testFunc cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug65180a() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc()\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180b() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see Test#testFunc\n" + 
					"		 * @see Test#testFunc()\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180c() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"	public void testFunc(String str) {}\n" + 
					"}\n" + 
					"\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 6)\n" + 
				"	* @see #testFunc\n" + 
				"	        ^^^^^^^^\n" + 
				"Javadoc: testFunc cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug65180d() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	int testField;\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testField\n" + 
					"		 * @see #testFunc(int)\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc(testField);\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc(int test) {\n" + 
					"		testField = test; \n" + 
					"	}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180e() {
		runConformTest(
			new String[] {
				"ITest.java",
				"public interface ITest {\n" + 
					"	/**\n" + 
					"	 * @see #foo() \n" + 
					"	 */\n" + 
					"	public static int field = 0;\n" + 
					"	/**\n" + 
					"	 * @see #field\n" + 
					"	 */\n" + 
					"	public void foo();\n" + 
					"}\n"
			}
		);
	}
	public void testBug65180f() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    static class SuperInner {\n" + 
					"    	public int field;\n" + 
					"        public void foo() {}\n" + 
					"     }\n" + 
					"    \n" + 
					"	public static class Inner extends SuperInner {\n" + 
					"		/**\n" + 
					"		 * @see #field\n" + 
					"		 */\n" + 
					"		public static int f;\n" + 
					"		/**\n" + 
					"		 * @see #foo()\n" + 
					"		 */\n" + 
					"		public static void bar() {}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void foo() {}\n" + 
					"}"
			}
		);
	}

	/**
	 * Bug 65253: [Javadoc] @@tag is wrongly parsed as @tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65253">65253</a>
	 */
	public void testBug65253() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment \n" + 
					" * @@@@see Unknown Should not complain on ref\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/**\n" + 
					"	 * Comment\n" + 
					"	 * @@@param xxx Should not complain on param\n" + 
					"	 * @@return int\n" + 
					"	 */\n" + 
					"	int foo() { // should warn on missing tag for return type\n" + 
					"		return 0;\n" + 
					"	}\n" + 
					"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 3)\n" + 
			"	* @@@@see Unknown Should not complain on ref\n" + 
			"	   ^^^^^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 8)\n" + 
			"	* @@@param xxx Should not complain on param\n" + 
			"	   ^^^^^^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n" + 
			"3. ERROR in Test.java (at line 9)\n" + 
			"	* @@return int\n" + 
			"	   ^^^^^^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n" + 
			"4. ERROR in Test.java (at line 11)\n" + 
			"	int foo() { // should warn on missing tag for return type\n" + 
			"	^^^\n" + 
			"Javadoc: Missing tag for return type\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 66551: Error in org.eclipse.swt project on class PrinterData
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66551">66551</a>
	 */
	public void testBug66551() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    int field;\n" + 
					"    /**\n" + 
					"     *  @see #field\n" + 
					"     */\n" + 
					"    void foo(int field) {\n" + 
					"    }\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551a() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    static int field;\n" + 
					"    /**\n" + 
					"     *  @see #field\n" + 
					"     */\n" + 
					"    static void foo(int field) {\n" + 
					"    }\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551b() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	int field;\n" + 
					"	/**\n" + 
					"	 * {@link #field}\n" + 
					"	 */\n" + 
					"	void foo(int field) {\n" + 
					"	}\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551c() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	static int field;\n" + 
					"	/**\n" + 
					"	 * {@link #field}\n" + 
					"	 */\n" + 
					"	static void foo(int field) {\n" + 
					"	}\n" + 
					"\n" + 
					"}\n"
			}
		);
	}	

	/**
	 * Bug 66573: Shouldn't bind to local constructs
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66573">66573</a>
	 */
	public void testBug66573() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    /**\n" + 
					"     * @see Local\n" + 
					"     */\n" + 
					"    void foo() {\n" + 
					"        class Local { \n" + 
					"            // shouldn\'t be seen from javadoc\n" + 
					"         }\n" + 
					"    }\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 3)\n" + 
				"	* @see Local\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Local cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 68017: Javadoc processing does not detect missing argument to @return
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68017">68017</a>
	 */
	public void testBug68017conform() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@return valid integer*/\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return #\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			}
		);
	}
	public void testBug68017negative() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@return*/\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**@return        */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"	/**@return****/\n" + 
					"	public int foo3() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 */\n" + 
					"	public int foo4() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@return*/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@return        */\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@return****/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 9)\n" + 
				"	*	@return\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n"
		);
	}
	// Javadoc issue a warning on following tests
	public void testBug68017javadocWarning1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return* */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**@return** **/\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@return* */\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	/**@return** **/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n"
		);
	}
	public void testBug68017javadocWarning2() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 *	@return #\n" + 
				"	 */\n" + 
				"	public int foo1() {return 0; }\n" + 
				"	/**\n" + 
				"	 *	@return @\n" + 
				"	 */\n" + 
				"	public int foo2() {return 0; }\n" + 
				"}\n"
			}
		);
	}
	public void testBug68017javadocWarning3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return#\n" + 
					"	 *	@return#text\n" + 
					"	 */\n" + 
					"	public int foo() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	*	@return#\n" + 
			"	 	 ^^^^^^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	*	@return#text\n" + 
			"	 	 ^^^^^^^^^^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 68025: Javadoc processing does not detect some wrong links
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68025">68025</a>
	 */
	public void testBug68025conform() {
		runConformTest(
			new String[] {
				"Y.java",
				"public class Y {\n" + 
					"	public int field;\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Z.java",
				"public class Z {\n" + 
					"	/**\n" + 
					"	 *	@see Y#field #valid\n" + 
					"	 *	@see Y#foo #valid\n" + 
					"	 */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Y#field     # valid*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Y#foo		# valid*/\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Y#foo()\n" + 
					"	 *# valid*/\n" + 
					"	public void foo4() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug68025negative() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	public int field;\n" + 
					"	public void foo() {}\n" + 
					"	/**\n" + 
					"	 *	@see #field#invalid\n" + 
					"	 *	@see #foo#invalid\n" + 
					"	 */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Y#field# invalid*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Y#foo#	invalid*/\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Y#foo()#\n" + 
					"	 *valid*/\n" + 
					"	public void foo4() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	*	@see #field#invalid\n" + 
				"	 	     ^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	*	@see #foo#invalid\n" + 
				"	 	     ^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 9)\n" + 
				"	/**@see Y#field# invalid*/\n" + 
				"	         ^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 11)\n" + 
				"	/**@see Y#foo#	invalid*/\n" + 
				"	         ^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 13)\n" + 
				"	/**@see Y#foo()#\n" + 
				"	             ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 69272: [Javadoc] Invalid malformed reference (missing separator)
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69272">69272</a>
	 */
	public void testBug69272classValid() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Object****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see Object		****/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272classInvalid() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see Object* */\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see Object*** ***/\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@see Object***\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug69272fieldValid() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	int field;\n" + 
					"	/**@see #field*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see #field\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see #field    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see #field****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see #field		********/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272fieldInvalid() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	int field;\n" + 
					"	/**@see #field* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see #field*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see #field***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	/**@see #field* */\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	/**@see #field*** ***/\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	/**@see #field***\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug69272methodValid() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object#wait()*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object#wait()\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object#wait()    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Object#wait()****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see Object#wait()		****/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272methodInvalid() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object#wait()* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object#wait()*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object#wait()***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see Object#wait()* */\n" + 
				"	                   ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see Object#wait()*** ***/\n" + 
				"	                   ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@see Object#wait()***\n" + 
				"	                   ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 69275: [Javadoc] Invalid warning on @see link
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69275">69275</a>
	 */
	public void testBug69275conform() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
					"	void foo1() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>\n" + 
					"	*/\n" + 
					"	void foo2() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>		*/\n" + 
					"	void foo3() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>**/\n" + 
					"	void foo4() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>     *****/\n" + 
					"	void foo5() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug69275negative() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" + 
					"	void foo1() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" + 
					"	void foo2() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>**\n" + 
					"	*/\n" + 
					"	void foo3() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" + 
				"	                                              ^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" + 
				"	                                              ^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 69302: [Javadoc] Invalid reference warning inconsistent with javadoc tool
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69302"
	 */
	public void testBug69302conform1() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Object <a href=\"http://www.eclipse.org\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**\n" + 
					"	 *	@see Object \"Valid string reference\"\n" + 
					"	 */\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug69302negative1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Unknown <a href=\"http://www.eclipse.org\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**\n" + 
					"	 *	@see Unknown \"Valid string reference\"\n" + 
					"	 */\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	*	@see Unknown <a href=\"http://www.eclipse.org\">Eclipse</a>\n" + 
			"	 	     ^^^^^^^\n" + 
			"Javadoc: Unknown cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	*	@see Unknown \"Valid string reference\"\n" + 
			"	 	     ^^^^^^^\n" + 
			"Javadoc: Unknown cannot be resolved to a type\n" + 
			"----------\n"
		);
	}
	public void testBug69302negative2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
					"	void foo1() {}\n" + 
					"	/**@see Unknown blabla \"Valid string reference\"*/\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
			"	        ^^^^^^^\n" + 
			"Javadoc: Unknown cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	/**@see Unknown blabla \"Valid string reference\"*/\n" + 
			"	        ^^^^^^^\n" + 
			"Javadoc: Unknown cannot be resolved to a type\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 68726: [Javadoc] Target attribute in @see link triggers warning
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68726">68726</a>
	 */
	public void testBug68726conform1() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Object <a href=\"http://www.eclipse.org\" target=\"_top\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**@see Object <a href=\"http://www.eclipse.org\" target=\"_top\" target1=\"_top1\" target2=\"_top2\">Eclipse</a>*/\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug68726conform2() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* 			</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			</a>\n" + 
					"	* @see Object\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"	
			}
		);
	}
	public void testBug68726negative1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Invalid URL link references\n" + 
					"	 *\n" + 
					"	 * @see <a href=\"invalid\" target\n" + 
					"	 * @see <a href=\"invalid\" target=\n" + 
					"	 * @see <a href=\"invalid\" target=\"\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\"\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid<\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @see <a href=\"invalid\" target\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @see <a href=\"invalid\" target=\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @see <a href=\"invalid\" target=\"\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\"\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 12)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 13)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid<\n" + 
				"	                                              ^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 14)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</\n" + 
				"	                                              ^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 15)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</a\n" + 
				"	                                              ^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"12. ERROR in X.java (at line 16)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" + 
				"	                                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n"
		);
	}
	public void testBug68726negative2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page<\n" + 
					"	* 			/a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			\n" + 
					"	* @see Unknown\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
				"	*          IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	*          IBM Home Page<\n" + 
				"	                        ^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
				"	*\n" + 
				"	*          IBM\n" + 
				"	*\n" + 
				"	*          Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 16)\n" + 
				"	* @see Unknown\n" + 
				"	       ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892">70892</a>
	 * These two tests should pass whatever the source level...
	 */
	public void testBug70892conform1() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * {@value}\n" + 
					" * {@value }\n" + 
					" * {@value #field}\n" + 
					" */\n" + 
					"public class X {\n" + 
					"	static int field;\n" + 
					"}\n"
			}
		);
	}
	public void testBug70892conform2() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**{@value #field}*/\n" + 
					"	static int field;\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Bug 73348: [Javadoc] Missing description for return tag is not always warned
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73348">73348</a>
	 */
	public void testBug73348conform() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	int\n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	int\n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			}
		);
	}
	public void testBug73348negative() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@return\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	*	@return      \n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
 				"----------\n"
 		);
 	}

	/**
	 * Bug 73479: [Javadoc] Improve error message for invalid link in @see tags
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73479">73479</a>
	 */
	public void testBug73479() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see <a href=\"spec.html#section\">Java Spec<a>\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@see <a href=\"spec.html#section\">Java Spec<a>\n" + 
				"	 	                                          ^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
 				"----------\n"
 		);
 	}

	/**
	 * Bug 73995: [Javadoc] Wrong warning for missing return type description for @return {@inheritdoc}
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73995">73995</a>
	 */
	public void testBug73995() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return {@link Object}     \n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/** @return {@inheritedDoc} */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 *		{@unknown_tag}\n" + 
					"	 */\n" + 
					"	public int foo3() {return 0; }\n" + 
					"}\n"
			}
 		);
 	}

	/**
	 * Bug 74369: [Javadoc] incorrect javadoc in local class
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=74369">74369</a>
	 */
	public void testBug74369() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"   public void method() {\n" + 
					"       /**\n" + 
					"        * @see #hsgdfdj\n" + 
					"        */\n" + 
					"        System.out.println(\"println\");\n" + 
					"        class Local {}\n" + 
					"    }\n" + 
					"}"
			}
 		);
 	}
	public void testBug74369deprecated() {
		runNegativeTest(
			new String[] {
				"p/Y.java",
				"package p;\n" + 
					"\n" + 
					"\n" + 
					"public class Y {\n" + 
					"   /**\n" + 
					"    * @deprecated\n" + 
					"    */\n" + 
					"   public void bar() {}\n" + 
					"}\n",
				"X.java",
				"import p.Y;\n" + 
					"\n" + 
					"public class X {\n" + 
					"	Object obj = new Object() {\n" + 
					"		public boolean equals(Object o) {\n" + 
					"			/**\n" + 
					"			 * @deprecated\n" + 
					"			 */\n" + 
					"	        System.out.println(\"println\");\n" + 
					"	        class Local {\n" + 
					"	        	void bar() {\n" + 
					"					new Y().bar();\n" + 
					"	        	}\n" + 
					"	        }\n" + 
					"			return super.equals(o);\n" + 
					"		}\n" + 
					"	};\n" + 
					"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	new Y().bar();\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"The method bar() from the type Y is deprecated\n" + 
			"----------\n"
 		);
 	}

	/**
	 * Bug 76324: [Javadoc] Wrongly reports invalid link format in @see and @link
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=76324">76324</a>
	 */
	public void testBug76324() {
		runConformTest(
			new String[] {
				"X.java",
				"\n" + 
					"/**\n" + 
					" * Subclasses perform GUI-related work in a dedicated thread. For instructions\n" + 
					" * on using this class, see\n" + 
					" * {@link <a  href=\"http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html\"> Swing tutorial </a>}\n" + 
					" * \n" + 
					" * @see <a\n" + 
					" *      href=\"http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html\">\n" + 
					" *      EDU.oswego.cs.dl.util.concurrent </a>\n" + 
					" * @see <a\n" + 
					" *      href=\"http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/package-summary.html\">\n" + 
					" *      JDK 5.0 </a>\n" + 
					" * @author {@link <a href=\"http://gee.cs.oswego.edu/dl\">Doug Lea</a>}\n" + 
					" * @author {@link <a href=\"http://home.pacbell.net/jfai\">J?rgen Failenschmid</a>}\n" + 
					"  *\n" + 
					"  * It is assumed that you have read the introductory document\n" + 
					"  * {@link <a HREF=\"../../../../../internat/overview.htm\">\n" + 
					"  * Internationalization</a>}\n" + 
					"  * and are familiar with \n" + 
					" */\n" + 
					"public class X {\n" + 
					"\n" + 
					"}\n"
			}
 		);
 	}
	// URL Link references
	public void testBug76324url() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"	/**\n" +
					"	 * Invalid inline URL link references \n" +
					"	 *\n" +
					"	 * {@link <}\n" +
					"	 * {@link <a}\n" +
					"	 * {@link <a hre}\n" +
					"	 * {@link <a href}\n" +
					"	 * {@link <a href=}\n" +
					"	 * {@link <a href=\"}\n" +
					"	 * {@link <a href=\"invalid}\n" +
					"	 * {@link <a href=\"invalid\"}\n" +
					"	 * {@link <a href=\"invalid\">}\n" +
					"	 * {@link <a href=\"invalid\">invalid}\n" +
					"	 * {@link <a href=\"invalid\">invalid<}\n" +
					"	 * {@link <a href=\"invalid\">invalid</}\n" +
					"	 * {@link <a href=\"invalid\">invalid</a}\n" +
					"	 * {@link <a href=\"invalid\">invalid</a> no text allowed after}\n" +
					"	 */\n" +
					"	public void s_foo() {\n" +
					"	}\n" +
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* {@link <}\n" + 
				"	         ^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* {@link <a}\n" + 
				"	         ^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* {@link <a hre}\n" + 
				"	         ^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* {@link <a href}\n" + 
				"	         ^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* {@link <a href=}\n" + 
				"	         ^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	* {@link <a href=\"}\n" + 
				"	         ^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	* {@link <a href=\"invalid}\n" + 
				"	         ^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 12)\n" + 
				"	* {@link <a href=\"invalid\"}\n" + 
				"	         ^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 13)\n" + 
				"	* {@link <a href=\"invalid\">}\n" + 
				"	         ^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 14)\n" + 
				"	* {@link <a href=\"invalid\">invalid}\n" + 
				"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 15)\n" + 
				"	* {@link <a href=\"invalid\">invalid<}\n" + 
				"	                                  ^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"12. ERROR in X.java (at line 16)\n" + 
				"	* {@link <a href=\"invalid\">invalid</}\n" + 
				"	                                  ^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"13. ERROR in X.java (at line 17)\n" + 
				"	* {@link <a href=\"invalid\">invalid</a}\n" + 
				"	                                  ^^^^\n" + 
				"Javadoc: Malformed link reference\n" + 
				"----------\n" + 
				"14. ERROR in X.java (at line 18)\n" + 
				"	* {@link <a href=\"invalid\">invalid</a> no text allowed after}\n" + 
				"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n"
		);
	}
	// String references
	public void testBug76324string() {
		runNegativeTest(
			new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" + 
			"	 * Inline string references \n" + 
			"	 *\n" + 
			"	 * {@link \"}\n" + 
			"	 * {@link \"unterminated string}\n" + 
			"	 * {@link \"invalid string\"\"}\n" + 
			"	 * {@link \"valid string\"}\n" + 
			"	 * {@link \"invalid\" no text allowed after the string}\n" + 
			"	 */\n" + 
			"	public void s_foo() {\n" + 
			"	}\n" + 
			"}\n" },
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	* {@link \"}\n" + 
			"	         ^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	* {@link \"unterminated string}\n" + 
			"	         ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	* {@link \"invalid string\"\"}\n" + 
			"	                         ^^\n" + 
			"Javadoc: Unexpected text\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	* {@link \"invalid\" no text allowed after the string}\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: Unexpected text\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 77510: [javadoc] compiler wrongly report deprecation when option "process javadoc comments" is not set
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=77510">77510</a>
	 */
	public void testBug77510enabled() {
		runNegativeTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
					"	/** \\u0009 @deprecated */\n" + 
					"	static int i0009;\n" + 
					"	/** \\u000a @deprecated */\n" + 
					"	static int i000a;\n" + 
					"	/** \\u000b @deprecated */\n" + 
					"	static int i000b;\n" + 
					"	/** \\u000c @deprecated */\n" + 
					"	static int i000c;\n" + 
					"	/** \\u001c @deprecated */\n" + 
					"	static int i001c;\n" + 
					"	/** \\u001d @deprecated */\n" + 
					"	static int i001d;\n" + 
					"	/** \\u001e @deprecated */\n" + 
					"	static int i001e;\n" + 
					"	/** \\u001f @deprecated */\n" + 
					"	static int i001f;\n" + 
					"	/** \\u2007 @deprecated */\n" + 
					"	static int i2007;\n" + 
					"	/** \\u202f @deprecated */\n" + 
					"	static int i202f;\n" + 
					"}\n",
				"X.java",
				"public class X {\n" + 
					"	int i0 = A.i0009;\n" + 
					"	int i1 = A.i000a;\n" + 
					"	int i2 = A.i000b;\n" + 
					"	int i3 = A.i000c;\n" + 
					"	int i4 = A.i001c;\n" + 
					"	int i5 = A.i001d;\n" + 
					"	int i6 = A.i001e;\n" + 
					"	int i7 = A.i001f;\n" + 
					"	int i8 = A.i2007;\n" + 
					"	int i9 = A.i202f;\n" + 
					"}\n" },
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int i0 = A.i0009;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i0009 is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	int i1 = A.i000a;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i000a is deprecated\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	int i3 = A.i000c;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i000c is deprecated\n" + 
			"----------\n"
		);
	}
	public void testBug77510disabled() {
		docCommentSupport = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
					"	/** \\u0009 @deprecated */\n" + 
					"	static int i0009;\n" + 
					"	/** \\u000a @deprecated */\n" + 
					"	static int i000a;\n" + 
					"	/** \\u000b @deprecated */\n" + 
					"	static int i000b;\n" + 
					"	/** \\u000c @deprecated */\n" + 
					"	static int i000c;\n" + 
					"	/** \\u001c @deprecated */\n" + 
					"	static int i001c;\n" + 
					"	/** \\u001d @deprecated */\n" + 
					"	static int i001d;\n" + 
					"	/** \\u001e @deprecated */\n" + 
					"	static int i001e;\n" + 
					"	/** \\u001f @deprecated */\n" + 
					"	static int i001f;\n" + 
					"	/** \\u2007 @deprecated */\n" + 
					"	static int i2007;\n" + 
					"	/** \\u202f @deprecated */\n" + 
					"	static int i202f;\n" + 
					"}\n",
				"X.java",
				"public class X {\n" + 
					"	int i0 = A.i0009;\n" + 
					"	int i1 = A.i000a;\n" + 
					"	int i2 = A.i000b;\n" + 
					"	int i3 = A.i000c;\n" + 
					"	int i4 = A.i001c;\n" + 
					"	int i5 = A.i001d;\n" + 
					"	int i6 = A.i001e;\n" + 
					"	int i7 = A.i001f;\n" + 
					"	int i8 = A.i2007;\n" + 
					"	int i9 = A.i202f;\n" + 
					"}\n" },
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int i0 = A.i0009;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i0009 is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	int i1 = A.i000a;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i000a is deprecated\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	int i3 = A.i000c;\n" + 
			"	           ^^^^^\n" + 
			"The field A.i000c is deprecated\n" + 
			"----------\n"
		);
	}
	
	/**
	 * Test bug 77260: [Javadoc] deprecation warning should not be reported when @deprecated tag is set
	 */
	public void testBug77260() {
		runConformTest(
			new String[] {
				"X.java",
				"/** @deprecated */\n" + 
					"public class X {\n" + 
					"	public int x;\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Y.java",
				"/**\n" + 
					" * @see X\n" + 
					" * @deprecated\n" + 
					" */\n" + 
					"public class Y {\n" + 
					"	/** @see X#x */\n" + 
					"	public int x;\n" + 
					"	/** @see X#foo() */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Z.java",
				"public class Z {\n" + 
					"	/** \n" + 
					"	 * @see X#x\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public int x;\n" + 
					"	/**\n" + 
					"	 * @see X#foo()\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n" }
		);
	}
	public void testBug77260nested() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"/** @deprecated */\n" + 
					"public class X {\n" + 
					"	public int x;\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Y.java",
				"/**\n" + 
					" * @see X\n" + 
					" * @deprecated\n" + 
					" */\n" + 
					"public class Y {\n" + 
					"	/** @see X#x */\n" + 
					"	public int x;\n" + 
					"	/** @see X#foo() */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Z.java",
				"public class Z {\n" + 
					"	/** \n" + 
					"	 * @see X#x\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public int x;\n" + 
					"	/**\n" + 
					"	 * @see X#foo()\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n" },
			"----------\n" + 
				"1. ERROR in Y.java (at line 2)\n" + 
				"	* @see X\n" + 
				"	       ^\n" + 
				"Javadoc: The type X is deprecated\n" + 
				"----------\n" + 
				"2. ERROR in Y.java (at line 6)\n" + 
				"	/** @see X#x */\n" + 
				"	         ^\n" + 
				"Javadoc: The type X is deprecated\n" + 
				"----------\n" + 
				"3. ERROR in Y.java (at line 6)\n" + 
				"	/** @see X#x */\n" + 
				"	           ^\n" + 
				"Javadoc: The field X.x is deprecated\n" + 
				"----------\n" + 
				"4. ERROR in Y.java (at line 8)\n" + 
				"	/** @see X#foo() */\n" + 
				"	         ^\n" + 
				"Javadoc: The type X is deprecated\n" + 
				"----------\n" + 
				"5. ERROR in Y.java (at line 8)\n" + 
				"	/** @see X#foo() */\n" + 
				"	           ^^^^^\n" + 
				"Javadoc: The method foo() from the type X is deprecated\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. ERROR in Z.java (at line 3)\n" + 
				"	* @see X#x\n" + 
				"	       ^\n" + 
				"Javadoc: The type X is deprecated\n" + 
				"----------\n" + 
				"2. ERROR in Z.java (at line 3)\n" + 
				"	* @see X#x\n" + 
				"	         ^\n" + 
				"Javadoc: The field X.x is deprecated\n" + 
				"----------\n" + 
				"3. ERROR in Z.java (at line 8)\n" + 
				"	* @see X#foo()\n" + 
				"	       ^\n" + 
				"Javadoc: The type X is deprecated\n" + 
				"----------\n" + 
				"4. ERROR in Z.java (at line 8)\n" + 
				"	* @see X#foo()\n" + 
				"	         ^^^^^\n" + 
				"Javadoc: The method foo() from the type X is deprecated\n" + 
				"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug77260nested_disabled() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.DISABLED);
		runConformTest(
			new String[] {
				"X.java",
				"/** @deprecated */\n" + 
					"public class X {\n" + 
					"	public int x;\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Y.java",
				"/**\n" + 
					" * @see X\n" + 
					" * @deprecated\n" + 
					" */\n" + 
					"public class Y {\n" + 
					"	/** @see X#x */\n" + 
					"	public int x;\n" + 
					"	/** @see X#foo() */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Z.java",
				"public class Z {\n" + 
					"	/** \n" + 
					"	 * @see X#x\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public int x;\n" + 
					"	/**\n" + 
					"	 * @see X#foo()\n" + 
					"	 * @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n"
			},
			"",
			null,
			true,
			null,
			options,
			null
		);
	}

	/**
	 * Bug 77602: [javadoc] "Only consider members as visible as" is does not work for syntax error
	 */
	public void testBug77602public() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PUBLIC);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @see\n" + 
					"   * @see UnknownClass\n" + 
					"   */\n" + 
					"  protected void foo() {\n" + 
					"  }\n" + 
				"}\n"
			},
			"",
			null,
			true,
			null,
			options,
			null
		);
	}
	public void testBug77602private() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @see\n" + 
					"   * @see UnknownClass\n" + 
					"   */\n" + 
					"  protected void foo() {\n" + 
					"  }\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @see\n" + 
				"	   ^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @see UnknownClass\n" + 
				"	       ^^^^^^^^^^^^\n" + 
				"Javadoc: UnknownClass cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 78091: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=78091">78091</a>
	 */
	public void testBug78091() {
		reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param xxx.yyy invalid\n" + 
					"	 * @param obj(x) invalid\n" + 
					"	 */\n" + 
					"	public void foo(int xxx, Object obj) {}\n" + 
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param xxx.yyy invalid\n" + 
				"	         ^^^^^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param obj(x) invalid\n" + 
				"	         ^^^^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	public void foo(int xxx, Object obj) {}\n" + 
				"	                    ^^^\n" + 
				"Javadoc: Missing tag for parameter xxx\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	public void foo(int xxx, Object obj) {}\n" + 
				"	                                ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}

	/**
	 * Bug 80910: [javadoc] Invalid missing reference warning on @see or @link tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80910"
	 */
	public void testBug80910() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	int field;\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * @param key\'s toto\n" + 
				"	 * @see #field\n" + 
				"	 */\n" + 
				"	public void foo(int x) {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 5)\n" + 
			"	* @param key\'s toto\n" + 
			"	         ^^^^^\n" + 
			"Javadoc: Invalid param tag name\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
	 */
	public void testBug82088() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	int field;\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * @param key\'s toto\n" + 
				"	 * @see #field\n" + 
				"	 */\n" + 
				"	public void foo(int x) {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 5)\n" + 
			"	* @param key\'s toto\n" + 
			"	         ^^^^^\n" + 
			"Javadoc: Invalid param tag name\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 83285: [javadoc] Javadoc reference to constructor of secondary type has no binding / not found by search
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83285"
	 */
	public void testBug83285a() {
		runConformTest(
			new String[] {
				"p/A.java",
				"package p;\n" + 
				"class A { }\n" + 
				"class C {\n" + 
				"    /**\n" + 
				"     * Link {@link #C(String)} was also wrongly warned...\n" + 
				"     */\n" + 
				"    private String fGerman;\n" + 
				"    public C(String german) {\n" + 
				"        fGerman = german;\n" + 
				"    }\n" + 
				"}"
			}
		);
	}
	public void testBug83285b() {
		runConformTest(
			new String[] {
				"p/A.java",
				"package p;\n" + 
				"class A {\n" + 
				"	A(char c) {}\n" + 
				"}\n" + 
				"class B {\n" + 
				"	B(Exception ex) {}\n" + 
				"	void foo() {} \n" + 
				"	class C { \n" + 
				"	    /**\n" + 
				"	     * Link {@link #B(Exception)} OK\n" + 
				"	     * Link {@link #C(String)} OK\n" + 
				"	     * Link {@link #foo()} OK\n" + 
				"	     * Link {@link #bar()} OK\n" + 
				"	     */\n" + 
				"	    public C(String str) {}\n" + 
				"		void bar() {}\n" + 
				"	}\n" + 
				"}"
			}
		);
	}
	public void testBug83285c() {
		runNegativeTest(
			new String[] {
				"p/A.java",
				"package p;\n" + 
				"class A {\n" + 
				"	A(char c) {}\n" + 
				"}\n" + 
				"class B {\n" + 
				"	B(Exception ex) {}\n" + 
				"	void foo() {}\n" + 
				"	class C { \n" + 
				"	    /**\n" + 
				"	     * Link {@link #A(char)} KO\n" + 
				"	     * Link {@link #B(char)}  KO\n" + 
				"	     * Link {@link #C(char)} KO\n" + 
				"	     * Link {@link #foo(int)} KO\n" + 
				"	     * Link {@link #bar(int)} KO\n" + 
				"	     */\n" + 
				"	    public C(String str) {}\n" + 
				"		void bar() {}\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in p\\A.java (at line 10)\n" + 
			"	* Link {@link #A(char)} KO\n" + 
			"	               ^\n" + 
			"Javadoc: The method A(char) is undefined for the type B.C\n" + 
			"----------\n" + 
			"2. ERROR in p\\A.java (at line 11)\n" + 
			"	* Link {@link #B(char)}  KO\n" + 
			"	               ^\n" + 
			"Javadoc: The method B(char) is undefined for the type B.C\n" + 
			"----------\n" + 
			"3. ERROR in p\\A.java (at line 12)\n" + 
			"	* Link {@link #C(char)} KO\n" + 
			"	               ^^^^^^^\n" + 
			"Javadoc: The constructor B.C(char) is undefined\n" + 
			"----------\n" + 
			"4. ERROR in p\\A.java (at line 13)\n" + 
			"	* Link {@link #foo(int)} KO\n" + 
			"	               ^^^\n" + 
			"Javadoc: The method foo(int) is undefined for the type B.C\n" + 
			"----------\n" + 
			"5. ERROR in p\\A.java (at line 14)\n" + 
			"	* Link {@link #bar(int)} KO\n" + 
			"	               ^^^\n" + 
			"Javadoc: The method bar() in the type B.C is not applicable for the arguments (int)\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 87404: [javadoc] Unexpected not defined warning on constructor
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87404"
	 */
	public void testBug87404() {
		runConformTest(
			new String[] {
				"p/A.java",
				"package p;\n" + 
				"class A {\n" + 
				"	A(char c) {}\n" + 
				"	class B {\n" + 
				"		B(Exception ex) {}\n" + 
				"	}\n" + 
				"	void foo() {}\n" + 
				"    /**\n" + 
				"     * Link {@link #A(char)} OK \n" + 
				"     * Link {@link #A(String)} OK\n" + 
				"     * Link {@link #foo()} OK\n" + 
				"     * Link {@link #bar()} OK\n" + 
				"     */\n" + 
				"    public A(String str) {}\n" + 
				"	void bar() {}\n" + 
				"}"
			}
		);
	}

	/**
	 * Bug 90302: [javadoc] {@inheritedDoc} should be inactive for non-overridden method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=90302"
	 */
	public void testBug90302() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
				" * @see #foo(String)\n" + 
				" */\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * Static method\n" + 
				"	 * @param str\n" + 
				"	 * @return int\n" + 
				"	 * @throws NumberFormatException\n" + 
				"	 */\n" + 
				"	static int foo(String str) throws NumberFormatException{\n" + 
				"		return Integer.parseInt(str);\n" + 
				"	}\n" + 
				"}\n",
				"Y.java",
				"/**\n" + 
				" * @see #foo(String)\n" + 
				" */\n" + 
				"public class Y extends X { \n" + 
				"	/**\n" + 
				"	 * Static method: does not override super\n" + 
				"	 * {@inheritDoc}\n" + 
				"	 */\n" + 
				"	static int foo(String str) throws NumberFormatException{\n" + 
				"		return Integer.parseInt(str);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 7)\n" + 
			"	* {@inheritDoc}\n" + 
			"	    ^^^^^^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	       ^^^\n" + 
			"Javadoc: Missing tag for return type\n" + 
			"----------\n" + 
			"3. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	                      ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n" + 
			"4. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	                                  ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: Missing tag for declared exception NumberFormatException\n" + 
			"----------\n"
		);
	}
	public void testBug90302b() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"}\n",
				"Y.java",
				"/**\n" + 
				" * @see #foo(String)\n" + 
				" */\n" + 
				"public class Y extends X { \n" + 
				"	/**\n" + 
				"	 * Simple method: does not override super\n" + 
				"	 * {@inheritDoc}\n" + 
				"	 */\n" + 
				"	static int foo(String str) throws NumberFormatException{\n" + 
				"		return Integer.parseInt(str);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 7)\n" + 
			"	* {@inheritDoc}\n" + 
			"	    ^^^^^^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	       ^^^\n" + 
			"Javadoc: Missing tag for return type\n" + 
			"----------\n" + 
			"3. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	                      ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n" + 
			"4. ERROR in Y.java (at line 9)\n" + 
			"	static int foo(String str) throws NumberFormatException{\n" + 
			"	                                  ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: Missing tag for declared exception NumberFormatException\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 116464: [javadoc] Unicode tag name are not correctly parsed
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=116464"
	 */
	public void testBug116464() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @\\u0070\\u0061\\u0072\\u0061\\u006d str xxx\n" + 
				"	 */\n" + 
				"	void foo(String str) {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Bug 125903: [javadoc] Treat whitespace in javadoc tags as invalid tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=125903"
	 */
	public void testBug125903() {
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
				" * {@ link java.lang.String}\n" + 
				" * @ since 2.1\n" + 
				" */\n" + 
				"public class X {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	* {@ link java.lang.String}\n" + 
			"	   ^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	* @ since 2.1\n" + 
			"	  ^^\n" + 
			"Javadoc: Invalid tag\n" + 
			"----------\n"
		);
	}
	/**
	 * Bug 128954: Javadoc problems with category CAT_INTERNAL
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954"
	 */
	public void testBug128954() {
		this.reportInvalidJavadoc = CompilerOptions.WARNING;
		this.reportDeprecation = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"X.java", //========================
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar()\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"		Zork z;\n" +
				"	}\n" + 
				"}\n",
				"p/A.java",  //========================
				"package p;\n" +
				"public class A {\n" + 
				"	/** @deprecated */\n" +
				"	public void bar() {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	* @see p.A#bar()\n" + 
			"	           ^^^^^\n" + 
			"[@cat:javadoc] Javadoc: The method bar() from the type A is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"[@cat:type] Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true,
			null,
			false,
			true,
			true);
	}	
	/**
	 * Bug 128954: Javadoc problems with category CAT_INTERNAL - variation
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954" 
	 */
	public void testBug128954a() {
		this.reportInvalidJavadoc = CompilerOptions.WARNING;
		this.reportDeprecation = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar()\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"		Zork z;\n" +
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	* @see p.A#bar()\n" + 
			"	       ^^^\n" + 
			"[@cat:javadoc] Javadoc: p cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"[@cat:type] Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true,
			null,
			false,
			true,
			true);
	}	

	/**
	 * Bug 129241: [Javadoc] deprecation warning wrongly reported when ignoring Malformed Javadoc comments
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=129241"
	 */
	public void testBug129241a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"/** @deprecated */\n" +
				"public class A {\n" + 
				"	void bar() {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	* @see p.A#bar\n" + 
			"	       ^^^\n" + 
			"Javadoc: The type A is deprecated\n" + 
			"----------\n"
		);
	}
	public void testBug129241b() {
		this.reportDeprecation = CompilerOptions.IGNORE;
//		this.reportJavadocDeprecation = CompilerOptions.ENABLED;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"/** @deprecated */\n" +
				"public class A {\n" + 
				"	void bar() {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	* @see p.A#bar\n" + 
			"	       ^^^\n" + 
			"Javadoc: The type A is deprecated\n" + 
			"----------\n"
		);
	}
	public void testBug129241c() {
		this.reportJavadocDeprecation = CompilerOptions.DISABLED;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"/** @deprecated */\n" +
				"public class A {\n" + 
				"	void bar() {}\n" + 
				"}\n"
			}
		);
	}
	public void testBug129241d() {
		this.reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see p.A#bar\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"/** @deprecated */\n" +
				"public class A {\n" + 
				"	void bar() {}\n" + 
				"}\n"
			}
		);
	}
}
