/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class AssignmentTest extends AbstractRegressionTest {
	
public AssignmentTest(String name) {
	super(name);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 45 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	Test suite = buildAllCompliancesTestSuite(testClass());
	return suite;
}
/*
 * no effect assignment bug
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27235
 */
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    int i;	\n" +
			"    X(int j) {	\n" +
			"    	i = j;	\n" +
			"    }	\n" +
			"    X() {	\n" +
			"    }	\n" +
			"    class B extends X {	\n" +
			"        B() {	\n" +
			"            this.i = X.this.i;	\n" +
			"        }	\n" +
			"    }	\n" +
			"    public static void main(String[] args) {	\n" +
			"        X a = new X(3);	\n" +
			"        System.out.print(a.i + \" \");	\n" +
			"        System.out.print(a.new B().i);	\n" +
			"	}	\n" +
			"}	\n",
		},
		"3 3");
}

public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int a;	\n" + 
			"	X next;	\n" + 
			"	public void foo(int arg){	\n" + 
			"	\n" + 
			"		zork = zork;	\n" +
			"		arg = zork;	\n" +
			"	\n" + 
			"		arg = arg;  // noop	\n" + 
			"		a = a;  // noop	\n" + 
			"		this.next = this.next; // noop	\n" + 
			"		this.next = next; // noop	\n" + 
			"	\n" + 
			"		next.a = next.a; // could raise NPE	\n" + 
			"		this.next.next.a = next.next.a; // could raise NPE	\n" + 
			"		a = next.a; // could raise NPE	\n" + 
			"		this. a = next.a; 	\n" + 
			"	}	\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	zork = zork;	\n" + 
		"	^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	zork = zork;	\n" + 
		"	       ^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	arg = zork;	\n" + 
		"	      ^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	arg = arg;  // noop	\n" + 
		"	^^^^^^^^^\n" + 
		"The assignment to variable arg has no effect\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 10)\n" + 
		"	a = a;  // noop	\n" + 
		"	^^^^^\n" + 
		"The assignment to variable a has no effect\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 11)\n" + 
		"	this.next = this.next; // noop	\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable next has no effect\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 12)\n" + 
		"	this.next = next; // noop	\n" + 
		"	^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable next has no effect\n" + 
		"----------\n");
}
public void test003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int portNumber;\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X x = new X();\n" + 
			"		x.portNumber = Integer.parseInt(\"12\");\n" + 
			"		x.run();\n" + 
			"	}\n" + 
			"	private void run() {\n" + 
			"		System.out.println(portNumber);\n" + 
			"	}\n" + 
			"}", // =================

		},
		"12");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151787
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    // correctly passes compilation\n" + 
			"    static class Test1 {\n" + 
			"        private final Object o;\n" + 
			"        \n" + 
			"        Test1() {\n" + 
			"            o = new Object();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    // correctly passes compilation\n" + 
			"    static class Test2 {\n" + 
			"        private final Object o;\n" + 
			"        \n" + 
			"        Test2() {\n" + 
			"            this.o = new Object();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    // correctly fails compilation\n" + 
			"    static class Test3 {\n" + 
			"        private final Object o;\n" + 
			"        \n" + 
			"        Test3() {\n" + 
			"            System.out.println(o); // illegal; o is not definitely assigned\n" + 
			"            o = new Object();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    // correctly passes compilation\n" + 
			"    static class Test4 {\n" + 
			"        private final Object o;\n" + 
			"        \n" + 
			"        Test4() {\n" + 
			"            System.out.println(this.o); // legal\n" + 
			"            o = new Object();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    \n" + 
			"    // incorrectly passes compilation\n" + 
			"    static class Test5 {\n" + 
			"        private final Object o;\n" + 
			"        \n" + 
			"        Test5() {\n" + 
			"            Test5 other = this;\n" + 
			"            other.o = new Object(); // illegal!  other.o is not assignable\n" + 
			"        } // error: this.o is not definitely assigned\n" + 
			"    }\n" + 
			"    \n" + 
			"    // flags wrong statement as error\n" + 
			"    static class Test6 {\n" + 
			"        private final Object o;\n" + 
			"        static Test6 initing;\n" + 
			"        \n" + 
			"       Test6() {\n" + 
			"           initing = this;\n" + 
			"           System.out.println(\"greetings\");\n" + 
			"           Test6 other = initing;\n" + 
			"           other.o = new Object(); // illegal!  other.o is not assignable\n" + 
			"           o = new Object(); // legal\n" + 
			"       }\n" + 
			"    }\n" + 
			"}\n", // =================
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	private final Object o;\n" + 
		"	                     ^\n" + 
		"The field X.Test1.o is never read locally\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 13)\n" + 
		"	private final Object o;\n" + 
		"	                     ^\n" + 
		"The field X.Test2.o is never read locally\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 25)\n" + 
		"	System.out.println(o); // illegal; o is not definitely assigned\n" + 
		"	                   ^\n" + 
		"The blank final field o may not have been initialized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 42)\n" + 
		"	private final Object o;\n" + 
		"	                     ^\n" + 
		"The field X.Test5.o is never read locally\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 44)\n" + 
		"	Test5() {\n" + 
		"	^^^^^^^\n" + 
		"The blank final field o may not have been initialized\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 46)\n" + 
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" + 
		"	      ^\n" + 
		"The final field X.Test5.o cannot be assigned\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 52)\n" + 
		"	private final Object o;\n" + 
		"	                     ^\n" + 
		"The field X.Test6.o is never read locally\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 59)\n" + 
		"	other.o = new Object(); // illegal!  other.o is not assignable\n" + 
		"	      ^\n" + 
		"The final field X.Test6.o cannot be assigned\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190391
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	final int contents;\n" + 
			"	\n" + 
			"	X() {\n" + 
			"		contents = 3;\n" + 
			"	}\n" + 
			"	X(X other) {\n" + 
			"		other.contents = 5;\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X one = new X();\n" + 
			"		System.out.println(\"one.contents: \" + one.contents);\n" + 
			"		X two = new X(one);\n" + 
			"		System.out.println(\"one.contents: \" + one.contents);\n" + 
			"		System.out.println(\"two.contents: \" + two.contents);\n" + 
			"	}\n" + 
			"}\n", // =================
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	X(X other) {\n" + 
		"	^^^^^^^^^^\n" + 
		"The blank final field contents may not have been initialized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	other.contents = 5;\n" + 
		"	      ^^^^^^^^\n" + 
		"The final field X.contents cannot be assigned\n" + 
		"----------\n");
}
// final multiple assignment
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		final int v;\n" + 
			"		for (int i = 0; i < 10; i++) {\n" + 
			"			v = i;\n" + 
			"		}\n" + 
			"		v = 0;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	v = i;\n" + 
		"	^\n" + 
		"The final local variable v may already have been assigned\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	v = 0;\n" + 
		"	^\n" + 
		"The final local variable v may already have been assigned\n" + 
		"----------\n");
}

// null part has been repeated into NullReferenceTest#test1033
public void test033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		String a,b;\n" + 
			"		do{\n" + 
			"		   a=\"Hello \";\n" + 
			"		}while(a!=null);\n" + 
			"				\n" + 
			"		if(a!=null)\n" + 
			"		{\n" + 
			"		   b=\"World!\";\n" + 
			"		}\n" + 
			"		System.out.println(a+b);\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	}while(a!=null);\n" + 
		"	       ^\n" + 
		"Redundant null check: The variable a cannot be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	if(a!=null)\n" + 
		"	   ^\n" + 
		"Null comparison always yields false: The variable a can only be null at this location\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(a+b);\n" + 
		"	                     ^\n" + 
		"The local variable b may not have been initialized\n" + 
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84215
//TODO (philippe) should move to InitializationTest suite
public void test034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X \n" + 
			"{\n" + 
			"	public static String vdg;\n" + 
			"	public static final String aa = null;\n" + 
			"	public static final int a = 14;\n" + 
			"	public static final int b = 3;\n" + 
			"	private static final int c = 12;\n" + 
			"	private static final int d = 2; \n" + 
			"	private static final int e = 3; \n" + 
			"	private static final int f = 34; \n" + 
			"	private static final int g = 35; \n" + 
			"	private static final int h = 36; \n" + 
			"	private static final int j = 4;\n" + 
			"	private static final int k = 1;\n" + 
			"	public static final int aba = 1;\n" + 
			"	public static final int as = 11;\n" + 
			"	public static final int ad = 12;\n" + 
			"	public static final int af = 13;\n" + 
			"	public static final int ag = 2;\n" + 
			"	public static final int ah = 21;\n" + 
			"	public static final int aj = 22;\n" + 
			"	public static final int ak = 3;\n" + 
			"	public static final String aaad = null;\n" + 
			"	public static final int aaaf = 1;\n" + 
			"	public static final int aaag = 2;\n" + 
			"	public static final int aaha = 2;\n" + 
			"	static int cxvvb = 1;\n" + 
			"	static int z = a;\n" + 
			"	String asdff;\n" + 
			"	public static String ppfp;\n" + 
			"	public static int ppfpged;\n" + 
			"	boolean asfadf;\n" + 
			"	boolean cbxbx;\n" + 
			"	private static long tyt, rrky;\n" + 
			"	private static int dgjt, ykjr6y;\n" + 
			"	private static final int krykr = 1;\n" + 
			"	protected static int rykr5;\n" + 
			"	protected static int dhfg;\n" + 
			"	private static int dthj;\n" + 
			"	private static int fkffy;\n" + 
			"	private static String fhfy;\n" + 
			"	protected static String fhmf;\n" + 
			"	protected String ryur6;\n" + 
			"	protected String dhdthd;\n" + 
			"	protected String dth5;\n" + 
			"	protected String kfyk;\n" + 
			"	private String ntd;\n" + 
			"	public int asdasdads;\n" + 
			"	public static final int dntdr = 7;\n" + 
			"	public static final int asys = 1;\n" + 
			"	public static final int djd5rwas = 11;\n" + 
			"	public static final int dhds45rjd = 12;\n" + 
			"	public static final int srws4jd = 13;\n" + 
			"	public static final int s4ts = 2;\n" + 
			"	public static final int dshes4 = 21;\n" + 
			"	public static final int drthed56u = 22;\n" + 
			"	public static final int drtye45 = 23;\n" + 
			"	public static final int xxbxrb = 3;\n" + 
			"	public static final int xfbxr = 31;\n" + 
			"	public static final int asgw4y = 32;\n" + 
			"	public static final int hdtrhs5r = 33;\n" + 
			"	public static final int dshsh = 34;\n" + 
			"	public static final int ds45yuwsuy = 4;\n" + 
			"	public static final int astgs45rys = 5;\n" + 
			"	public static final int srgs4y = 6;\n" + 
			"	public static final int srgsryw45 = -6;\n" + 
			"	public static final int srgdtgjd45ry = -7;\n" + 
			"	public static final int srdjs43t = 1;\n" + 
			"	public static final int sedteued5y = 2;\n" + 
			"	public static int jrfd6u;\n" + 
			"	public static int udf56u;\n" + 
			"	private String jf6tu;\n" + 
			"	private String jf6tud;\n" + 
			"	String bsrh;\n" + 
			"	protected X(String a)\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private long sfhdsrhs;\n" + 
			"	private boolean qaafasdfs;\n" + 
			"	private int sdgsa;\n" + 
			"	private long dgse4;\n" + 
			"	long sgrdsrg;\n" + 
			"	public void gdsthsr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private int hsrhs;\n" + 
			"	private void hsrhsdsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private String dsfhshsr;\n" + 
			"	protected void sfhsh4rsrh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected void shsrhsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected void sfhstuje56u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	public void dhdrt6u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	public void hdtue56u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private void htdws4()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	String mfmgf;\n" + 
			"	String mgdmd;\n" + 
			"	String mdsrh;\n" + 
			"	String nmdr;\n" + 
			"	private void oyioyio()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected static long oyioyreye()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"	protected static long etueierh()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"	protected static void sdfgsgs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected static void fhsrhsrh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	long dcggsdg;\n" + 
			"	int ssssssgsfh;\n" + 
			"	long ssssssgae;\n" + 
			"	long ssssssfaseg;\n" + 
			"	public void zzzdged()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	String t;\n" + 
			"	protected void xxxxxcbsg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	\n" + 
			"	public void vdg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private int[] fffcvffffffasdfaef;\n" + 
			"	private int[] fffcffffffasdfaef;\n" + 
			"	private long[] ffcvfffffffasdfaef;\n" + 
			"	private int fffffghffffasdfaef; \n" + 
			"	private int fffffdffffasdfaef; \n" + 
			"	private String ffafffffffasdfaef;\n" + 
			"	\n" + 
			"	private void fffffffffasdfaef()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private boolean aaaadgasrg;\n" + 
			"	private void ddddgaergnj()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void aaaadgaeg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void aaaaaaefadfgh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void addddddddafge()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	static boolean aaaaaaaefae;\n" + 
			"	protected void aaaaaaefaef()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void ggggseae()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static void ggggggsgsrg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static synchronized void ggggggfsfgsr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void aaaaaadgaeg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void aaaaadgaerg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void bbbbbbsfryghs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void bfbbbbbbfssreg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbbbfssfb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbbbfssb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbfdssb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	boolean dggggggdsg;\n" + 
			"\n" + 
			"	public void hdfhdr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dhdrtdrs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dghdthtdhd()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dhdhdtdh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void fddhdsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private boolean sdffgsdg()\n" + 
			"	{\n" + 
			"		return true;\n" + 
			"	}\n" + 
			"			\n" + 
			"	private static boolean sdgsdg()\n" + 
			"	{\n" + 
			"		return false;\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static final void sfdgsg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	static int[] fghtys;\n" + 
			"\n" + 
			"	protected static final int sdsst = 1;\n" + 
			"	private static X asdfahnr;\n" + 
			"	private static int ssdsdbrtyrtdfhd, ssdsrtyrdbdfhd;\n" + 
			"	protected static int ssdsrtydbdfhd, ssdsrtydffbdfhd;\n" + 
			"	protected static int ssdrtyhrtysdbdfhd, ssyeghdsdbdfhd;\n" + 
			"	private static int ssdsdrtybdfhd, ssdsdehebdfhd;\n" + 
			"	protected static int ssdthrtsdbdfhd, ssdshethetdbdfhd;\n" + 
			"	private static String sstrdrfhdsdbdfhd;\n" + 
			"	protected static int ssdsdbdfhd, ssdsdethbdfhd;\n" + 
			"	private static long ssdshdfhchddbdfhd;\n" + 
			"	private static long ssdsdvbbdfhd;\n" + 
			"	\n" + 
			"	\n" + 
			"	protected static long ssdsdbdfhd()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static long sdgsrsbsf()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void sfgsfgssghr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static String sgsgsrg()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void sdgshsdygra()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static String sdfsdfs()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	static boolean ryweyer;\n" + 
			"\n" + 
			"	protected static void adfadfaghsfh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static void ghasghasrg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static void aadfadfaf()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void aadfadf()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private static int fgsfhwr()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static int gdfgfgrfg()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static int asdfsfs()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static String sdgs;\n" + 
			"	protected static String sdfsh4e;\n" + 
			"	protected static final int gsregs = 0;\n" + 
			"	\n" + 
			"	protected static String sgsgsd()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	private byte[] sdhqtgwsrh(String rsName, int id)\n" + 
			"	{\n" + 
			"		String rs = null;\n" + 
			"		try\n" + 
			"		{\n" + 
			"			rs = \"\";\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"		catch (Exception ex)\n" + 
			"		{\n" + 
			"		}\n" + 
			"		finally\n" + 
			"		{\n" + 
			"			if (rs != null)\n" + 
			"			{\n" + 
			"				try\n" + 
			"				{\n" + 
			"					rs.toString();\n" +
			"				}\n" + 
			"				catch (Exception ex)\n" + 
			"				{\n" + 
			"				}\n" + 
			"			}\n" + 
			"		}\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	private void dgagadga()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private String adsyasta;\n" + 
			"}\n",
		},
		"");
}
/*
 * Check scenario:  i = i++
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480
 * disabled: https://bugs.eclipse.org/bugs/show_bug.cgi?id=111898
 */
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int f;\n" + 
			"	void foo(int i) {\n" + 
			"		i = i++;\n" + 
			"		i = ++i;\n" + 
			"		f = f++;\n" + 
			"		f = ++f;\n" + 
			"		Zork z;" +
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	i = ++i;\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable i has no effect\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	f = ++f;\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable f has no effect\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	Zork z;	}\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"	void foo() {\n" + 
			"		Object o = new Object();\n" + 
			"		do {\n" + 
			"			o = null;\n" + 
			"		} while (o != null);\n" + 
			"		if (o == null) {\n" + 
			"			// throw new Exception();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	} while (o != null);\n" + 
		"	         ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	if (o == null) {\n" + 
		"	    ^\n" + 
		"Redundant null check: The variable o can only be null at this location\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93588
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Object implements Runnable {\n" + 
			"	int interval = 5;\n" + 
			"	public void run() {\n" + 
			"		try {\n" + 
			"			Thread.sleep(interval = interval + 100);\n" + 
			"			Thread.sleep(interval += 100);\n" + 
			"		} catch (InterruptedException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().run();\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111703
public void test038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.awt.event.*;\n" +
			"\n" +
			"import javax.swing.*;\n" +
			"import javax.swing.event.*;\n" +
			"\n" +
			"public class X {\n" +
			"    JButton myButton = new JButton();\n" +
			"    JTree myTree = new JTree();\n" +
			"    ActionListener action;\n" +
			"    X() {\n" +
			"        action = new ActionListener() {\n" +
			"            public void actionPerformed(ActionEvent e) {\n" +
			"                if (true) {\n" +
			"                    // unlock document\n" +
			"                    final Object document = new Object();\n" +
			"                    myButton.addActionListener(new ActionListener() {\n" +
			"                        private static boolean selectionChanged;\n" +
			"                        static TreeSelectionListener list = new TreeSelectionListener() {\n" +
			"                            public void valueChanged(TreeSelectionEvent e) {\n" +
			"                                selectionChanged = true;\n" +
			"                            }\n" +
			"                        };\n" +
			"                      static {\n" +
			"                      myTree.addTreeSelectionListener(list);\n" +
			"                      }\n" +
			"                        public void actionPerformed(ActionEvent e) {\n" +
			"                            if(!selectionChanged)\n" +
			"                            myButton.removeActionListener(this);\n" +
			"                        }\n" +
			"                    });\n" +
			"                }\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X();\n" +
			"    }\n" +
			"\n" +
			"}",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 19)\n" + 
		"	public void valueChanged(TreeSelectionEvent e) {\n" + 
		"	                                            ^\n" + 
		"The parameter e is hiding another local variable defined in an enclosing type scope\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 23)\n" + 
		"	static {\n" + 
		"	       ^\n" + 
		"Cannot define static initializer in inner type new ActionListener(){}\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 24)\n" + 
		"	myTree.addTreeSelectionListener(list);\n" + 
		"	^^^^^^\n" + 
		"Cannot make a static reference to the non-static field myTree\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 26)\n" + 
		"	public void actionPerformed(ActionEvent e) {\n" + 
		"	                                        ^\n" + 
		"The parameter e is hiding another local variable defined in an enclosing type scope\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111898
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		int a = 1;\n" + 
			"	    a = a++;\n" + 
			"		System.out.print(\"a=\"+a);\n" + 
			"		\n" + 
			"		int b = 1;\n" + 
			"		System.out.print(b = b++);\n" + 
			"		System.out.println(\"b=\"+b);\n" + 
			"	}\n" + 
			"}\n",
		},
		"a=11b=1");
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
public void test040() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(boolean b) {\n" + 
			"    b = false;\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	b = false;\n" + 
		"	^\n" + 
		"The parameter b should not be assigned\n" + 
		"----------\n",
		null, true, options);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// diagnose within fake reachable code
public void test041() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(boolean b) {\n" + 
			"    if (false) {\n" + 
			"      b = false;\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	b = false;\n" + 
		"	^\n" + 
		"The parameter b should not be assigned\n" + 
		"----------\n",
		null, true, options);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// diagnose within fake reachable code
public void test042() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(boolean b) {\n" + 
			"    if (true) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"    b = false;\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	b = false;\n" + 
		"	^\n" + 
		"The parameter b should not be assigned\n" + 
		"----------\n",
		null, true, options);
}
// warn upon parameter assignment
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// we only show the 'assignment to final' error here
public void test043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo(final boolean b) {\n" + 
			"    if (false) {\n" + 
			"      b = false;\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	b = false;\n" + 
		"	^\n" + 
		"The final local variable b cannot be assigned. It must be blank and not using a compound assignment\n" + 
		"----------\n",
		null, true, options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100369
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int length1 = 0;\n" + 
			"	{\n" + 
			"		length1 = length1; // already detected\n" + 
			"	}\n" + 
			"	int length2 = length2 = 0; // not detected\n" + 
			"	int length3 = 0;\n" + 
			"	{\n" + 
			"		length3 = length3 = 0; // not detected\n" + 
			"	}\n" + 
			"	static void foo() {\n" + 
			"		int length1 = 0;\n" + 
			"		length1 = length1; // already detected\n" + 
			"		int length2 = length2 = 0; // not detected\n" + 
			"		int length3 = 0;\n" + 
			"		length3 = length3 = 0; // not detected\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	length1 = length1; // already detected\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length1 has no effect\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	int length2 = length2 = 0; // not detected\n" + 
		"	    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length2 has no effect\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 9)\n" + 
		"	length3 = length3 = 0; // not detected\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length3 has no effect\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 13)\n" + 
		"	length1 = length1; // already detected\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length1 has no effect\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 14)\n" + 
		"	int length2 = length2 = 0; // not detected\n" + 
		"	    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length2 has no effect\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 16)\n" + 
		"	length3 = length3 = 0; // not detected\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length3 has no effect\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133351
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		int length2 = length2 = 0; // first problem\n" + 
			"		int length3 = 0;\n" + 
			"		length3 = length3 = 0; // second problem\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	int length2 = length2 = 0; // first problem\n" + 
		"	    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length2 has no effect\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	length3 = length3 = 0; // second problem\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable length3 has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static String s;\n" +
			"	void foo(String s1) {\n" + 
			"		X.s = s;" +
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	X.s = s;	}\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable s has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static X MyX;\n" +
			"	public static String s;\n" +
			"	void foo(String s1) {\n" + 
			"		X.MyX.s = s;" + // MyX could hold any extending type, hence we must not complain
			"	}\n" + 
			"}\n",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// we could decide that MyX won't change, hence that the assignment
// on line a has no effect, but we accept this as a limit
public void _test048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static final X MyX = new X();\n" +
			"	public static String s;\n" +
			"	void foo(String s1) {\n" + 
			"		X.MyX.s = s;" + // a
			"	}\n" + 
			"}\n",
		},
		"ERR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// adding a package to the picture
public void test049() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"public class X {\n" + 
			"	public static String s;\n" +
			"	void foo(String s1) {\n" + 
			"		p.X.s = s;" +
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 5)\n" + 
		"	p.X.s = s;	}\n" + 
		"	^^^^^^^^^\n" + 
		"The assignment to variable s has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// adding an inner class to the picture
public void test050() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"public class X {\n" + 
			"  class XX {\n" + 
			"	 public static String s;\n" +
			"	 void foo(String s1) {\n" + 
			"      X.XX.s = s;" +
			"    }\n" + 
			"  }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 4)\n" + 
		"	public static String s;\n" + 
		"	                     ^\n" + 
		"The field s cannot be declared static; static fields can only be declared in static or top level types\n" + 
		"----------\n" + 
		"2. ERROR in p\\X.java (at line 6)\n" + 
		"	X.XX.s = s;    }\n" + 
		"	^^^^^^^^^^\n" + 
		"The assignment to variable s has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200724
// swap lhs and rhs
public void test051() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static String s;\n" +
			"	void foo(String s1) {\n" + 
			"		s = X.s;" +
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	s = X.s;	}\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable s has no effect\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206017
public void test052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" + 
			"    int i = \"aaa\";\n" + 
			"    i = \"bbb\";\n" + 
			"  }\n" + 
			"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	int i = \"aaa\";\n" + 
			"	        ^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	i = \"bbb\";\n" + 
			"	    ^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206017
public void test053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i = \"aaa\";\n" + 
			"  { \n" + 
			"    i = \"bbb\";\n" + 
			"  }\n" + 
			"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int i = \"aaa\";\n" + 
			"	        ^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	i = \"bbb\";\n" + 
			"	    ^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n");
}
public static Class testClass() {
	return AssignmentTest.class;
}
}
