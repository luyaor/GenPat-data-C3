package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LocalVariableTest extends AbstractRegressionTest {
	
public LocalVariableTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new LocalVariableTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"        int foo(){\n" + 
		"                int i;\n" + 
		"                return 1;\n" + 
		"        }\n" + 
		"}\n",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  void foo() {\n" + 
		"    String temp;\n" + 
		"    try {\n" + 
		"      return;\n" + 
		"    }\n" + 
		"    catch (Exception e){\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  void foo() {\n" + 
		"    String temp;\n" + 
		"    try {\n" + 
		"      return;\n" + 
		"    }\n" + 
		"    catch (Exception e) {\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"     int i = 1;\n" + 
		"    System.out.println(i);\n" + 
		"  }\n" + 
		"  X(int j){\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test005() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int j;\n" + 
		"  void f1() {\n" + 
		"    int l;\n" + 
		"    switch (j) {\n" + 
		"      case 0 :\n" + 
		"        l = 10;\n" + 
		"		 l++;\n" + // at least one read usage
		"        break;\n" + 
		"      case 1 :\n" + 
		"        l = 20;\n" + 
		"        break;\n" + 
		"      case 2 :\n" + 
		"        l = 30;\n" + 
		"        break;\n" + 
		"      default :\n" + 
		"        l = 10;\n" + 
		"        break;\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public static void main(String args[]) {\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Truc.java",
		"package p;\n" + 
		"public class Truc{\n" + 
		"   void foo(){\n" + 
		"      final int i; \n" +
		"	   i = 1;\n" + 
		"      if (false) i = 2;\n" + 
		"   } \n" + 
		"	public static void main(java.lang.String[] args) {\n" + 
		"  		System.out.println(\"SUCCESS\"); \n" + 
		"	}	\n" +
		"}",
	},
	"SUCCESS");
}

public void test007() {
	this.runConformTest(new String[] {
		"p/A.java",
		"package p;\n" + 
		"import p.helper.Y;\n" + 
		"class A extends Y {\n" + 
		"  class Y {\n" + 
		"    int j = i;// i is a protected member inherited from Y\n" + 
		"  }\n" + 
		"}",

		"p/helper/Y.java",
		"package p.helper;\n" + 
		"public class Y {\n" + 
		"  protected int i = 10;\n" + 
		"  public inner in = new inner();\n" + 
		"    \n" + 
		"  protected class inner {\n" + 
		"    public int  f() {\n" + 
		"      return 20;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}",

	});
}

public static Class testClass() {
	return LocalVariableTest.class;
}
}
