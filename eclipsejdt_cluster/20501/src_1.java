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
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.compiler.regression.RegressionTestSetup;

public class ComplianceDiagnoseTest extends AbstractRegressionTest {
	public ComplianceDiagnoseTest(String name) {
		super(name);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "Bug51529a", "Bug51529b" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 3, 7, 10, 21 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 21, 50 };
//		testsRange = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//		testsRange = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(buildTestSuite(testClass()), highestComplianceLevels());
		} else {
			// To run a specific test, just uncomment line with testNumbers in static initializer above
			// and put numbers of tests you want to perform
			return setupSuite(testClass());
		}
	}
public static Class testClass() {
	return ComplianceDiagnoseTest.class;
}
public void runComplianceParserTest(
	String[] testFiles,
	String expected13ProblemLog,
	String expected14ProblemLog,
	String expected15ProblemLog){
	if(COMPLIANCE_1_3.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected13ProblemLog);
	} else if(COMPLIANCE_1_4.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected14ProblemLog);
	} else if(COMPLIANCE_1_5.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected15ProblemLog);
	}
}
public void test0001() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.*;\n" +
		"public class X {\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0002() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.CCC;\n" +
		"public class X {\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0003() {
	String[] testFiles = new String[] {
		"X.java",
		"public enum X {\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public enum X {\n" + 
		"	       ^^^^\n" + 
		"Syntax error on token \"enum\", interface expected\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0004() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(String o: c) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	    ^^^^^^^^^^^\n" + 
		"Syntax error, \'for each\' statements are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	              ^\n" + 
		"c cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	              ^\n" + 
		"c cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0005() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(Z ... arg){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^^^^^^^^^\n" + 
		"Syntax error, varargs are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0006() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0007() {
	String[] testFiles = new String[] {
		"X.java",
		"public interface X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                                                   ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                                                   ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0008() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> int foo(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> int foo(){\n" + 
		"	                                   ^^^^^\n" + 
		"This method must return a result of type int\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0009() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> X(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> X(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0010() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z<Y1, Y2> var;\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	  ^^^^^^\n" + 
		"Syntax error, parameterized types are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0011() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public X(){\n" +
		"		<Y1, Y2>this(null);\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	 ^^^^^^\n" + 
		"Syntax error, parameterized types are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	 ^^\n" + 
		"Y1 cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	     ^^\n" + 
		"Y2 cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	 ^^\n" + 
		"Y1 cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	     ^^\n" + 
		"Y2 cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0012() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"  void foo() {\n" +
		"    assert true;\n" +
		"  }\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	assert true;\n" + 
		"	^^^^^^\n" + 
		"\'assert\' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	assert true;\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"assert\", AssignmentOperator expected after this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		"";
	
	String expected15ProblemLog = 
		expected14ProblemLog;
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0013() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.*\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*\n" + 
		"	                  ^\n" + 
		"Syntax error on token \"*\", ; expected after this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*\n" + 
		"	                  ^\n" + 
		"Syntax error on token \"*\", ; expected after this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0014() {
	String[] testFiles = new String[] {
		"X.java",
		"public enum X \n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public enum X \n" + 
		"}\n" + 
		"	       ^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public enum X \n" + 
		"	            ^\n" + 
		"Syntax error on token \"X\", { expected after this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0015() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(String o: c) {\n" +
		"			#\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	    ^^^^^^^^^^^\n" + 
		"Syntax error, \'for each\' statements are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0016() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(Z ... arg){\n" +
		"	}\n" +
		"	#\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^^^^^^^^^\n" + 
		"Syntax error, varargs are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0017() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"	#\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0018() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	}\n" +
		"	#\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> int foo(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0019() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z<Y1, Y2> var;\n" +
		"	#\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	  ^^^^^^\n" + 
		"Syntax error, parameterized types are only available if source level is 1.5\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0020() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"  void foo() {\n" +
		"    assert true;\n" +
		"    #\n" +
		"  }\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	assert true;\n" + 
		"	^^^^^^\n" + 
		"\'assert\' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	assert true;\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"assert\", AssignmentOperator expected after this token\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	#\n" + 
		"	^\n" + 
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n";
	
	String expected15ProblemLog = 
		expected14ProblemLog;
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0021() {
	String[] testFiles = new String[] {
		"X.java",
		"import staic aaa.*;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import staic aaa.*;\n" + 
		"	       ^^^^^\n" + 
		"The import staic cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import staic aaa.*;\n" + 
		"	             ^^^\n" + 
		"Syntax error on token \"aaa\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import staic aaa.*;\n" + 
		"	       ^^^^^\n" + 
		"Syntax error on token \"staic\", static expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import staic aaa.*;\n" + 
		"	       ^^^^^\n" + 
		"The import staic cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0022() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.*.*;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*.*;\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*.*;\n" + 
		"	                   ^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.*.*;\n" + 
		"	                  ^\n" + 
		"Syntax error on token \"*\", Identifier expected\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0023() {
	String[] testFiles = new String[] {
		"X.java",
		"import static for;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static for;\n" + 
		"	       ^^^^^^^^^^\n" + 
		"Syntax error on tokens, Name expected instead\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static for;\n" + 
		"	              ^^^\n" + 
		"Syntax error on token \"for\", Identifier expected\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}

//TODO (david) reenable once bug is fixed
public void _test0024() {
	String[] testFiles = new String[] {
		"X.java",
		"import static {aaa};\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static {aaa};\n" + 
		"	       ^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, Name expected instead\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static {aaa};\n" + 
		"	              ^^^^^\n" + 
		"Syntax error on tokens, Name expected instead\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0025() {
	String[] testFiles = new String[] {
		"X.java",
		"static aaa.*;\n" +
		"public class X {\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	static aaa.*;\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"static\", import expected\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	static aaa.*;\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"static\", import expected before this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0026() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(Object o ? c){\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(Object o ? c){\n" + 
		"	    ^^^^^^\n" + 
		"Syntax error on token \"Object\", ( expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	for(Object o ? c){\n" + 
		"	           ^^^\n" + 
		"Syntax error on token(s), misplaced construct(s)\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	for(Object o ? c){\n" + 
		"	                ^\n" + 
		"Syntax error, insert \"AssignmentOperator ArrayInitializer\" to complete ArrayInitializerAssignement\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 3)\n" + 
		"	for(Object o ? c){\n" + 
		"	                ^\n" + 
		"Syntax error, insert \"; ; ) Statement\" to complete BlockStatements\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(Object o ? c){\n" + 
		"	             ^\n" + 
		"Syntax error on token \"?\", : expected\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0027() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(Object o : switch){\n" +
		"		}\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(Object o : switch){\n" + 
		"	           ^\n" + 
		"Syntax error, insert \"; ; ) Statement\" to complete BlockStatements\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(Object o : switch){\n" + 
		"	               ^^^^^^\n" + 
		"Syntax error on token \"switch\", invalid Expression\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0028() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... ){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... ){\n" + 
		"	             ^^^\n" + 
		"Syntax error on token \"ELLIPSIS\", invalid VariableDeclaratorId\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... ){\n" + 
		"	             ^^^\n" + 
		"Syntax error on token \"ELLIPSIS\", VariableDeclaratorId expected after this token\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0029() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... for){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... for){\n" + 
		"	             ^^^^^^^\n" + 
		"Syntax error on tokens, VariableDeclaratorId expected instead\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... for){\n" + 
		"	                 ^^^\n" + 
		"Syntax error on token \"for\", invalid VariableDeclaratorId\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0030() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int .. aaa){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int .. aaa){\n" + 
		"	             ^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int .. aaa){\n" + 
		"	             ^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0031() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(int ... aaa bbb){\n" +
		"	}\n" +
		"}\n" +
		"\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... aaa bbb){\n" + 
		"	         ^^^^^^^^^^^\n" + 
		"Syntax error, varargs are only available if source level is 1.5\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... aaa bbb){\n" + 
		"	             ^^^^^^^\n" + 
		"Syntax error on token(s), misplaced construct(s)\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(int ... aaa bbb){\n" + 
		"	                     ^^^\n" + 
		"Syntax error on token \"bbb\", delete this token\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0032() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2 extends Y {\n" +
		"	\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2 extends Y {\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error on token(s), misplaced construct(s)\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2 extends Y {\n" + 
		"	                                              ^\n" + 
		"Syntax error, insert \">\" to complete ReferenceType1\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2 extends Y {\n" + 
		"	                                              ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0033() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X T1 extends String, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X T1 extends String, T2> extends Y {\n" + 
		"	               ^^\n" + 
		"Syntax error on token \"T1\", delete this token\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X T1 extends String, T2> extends Y {\n" + 
		"	                          ^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X T1 extends String, T2> extends Y {\n" + 
		"	             ^\n" + 
		"Syntax error on token \"X\", < expected after this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0034() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extnds String, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	               ^\n" + 
		"Syntax error on token \"<\", { expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, InterfaceHeaderName expected instead\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	                   ^^^^^^\n" + 
		"extnds cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	                          ^^^^^^\n" + 
		"Syntax error on token \"String\", delete this token\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 3)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	                   ^^^^^^\n" + 
		"Syntax error on token \"extnds\", extends expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extnds String, T2> extends Y {\n" + 
		"	                   ^^^^^^\n" + 
		"extnds cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0035() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends for, T2> extends Y {\n" +
		"	\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends for, T2> extends Y {\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends for, T2> extends Y {\n" + 
		"	                           ^^^\n" + 
		"Syntax error on token \"for\", invalid ReferenceType\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0036() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> foo(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> foo(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> foo(){\n" + 
		"	                               ^^^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> foo(){\n" + 
		"	                               ^^^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0037() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extnds String, T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extnds String, T2> int foo(){\n" + 
		"	       ^^^\n" + 
		"Syntax error on token(s), misplaced construct(s)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extnds String, T2> int foo(){\n" + 
		"	        ^^\n" + 
		"T1 cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extnds String, T2> int foo(){\n" + 
		"	                            ^\n" + 
		"Syntax error on token \">\", ; expected\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extnds String, T2> int foo(){\n" + 
		"	        ^^\n" + 
		"T1 cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extnds String, T2> int foo(){\n" + 
		"	           ^^^^^^\n" + 
		"Syntax error on token \"extnds\", extends expected\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0038() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String T2> int foo(){\n" + 
		"	       ^^^^^^^^^^^\n" + 
		"Syntax error on token(s), misplaced construct(s)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String T2> int foo(){\n" + 
		"	                            ^\n" + 
		"Syntax error on token \">\", ; expected\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String T2> int foo(){\n" + 
		"	                          ^^\n" + 
		"Syntax error on token \"T2\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0039() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z Y1, Y2> var;\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z Y1, Y2> var;\n" + 
		"	        ^\n" + 
		"Syntax error on token \">\", , expected\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z Y1, Y2> var;\n" + 
		"	        ^\n" + 
		"Syntax error on token \">\", , expected\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0040() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z <Y1, Y2 var;\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, Y2 var;\n" + 
		"	       ^^\n" + 
		"Syntax error, insert \">\" to complete ReferenceType1\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, Y2 var;\n" + 
		"	       ^^\n" + 
		"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, Y2 var;\n" + 
		"	       ^^\n" + 
		"Y2 cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, Y2 var;\n" + 
		"	       ^^\n" + 
		"Syntax error, insert \">\" to complete ReferenceType1\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, Y2 var;\n" + 
		"	       ^^\n" + 
		"Y2 cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0041() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z <Y1, for Y2> var;\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, for Y2> var;\n" + 
		"	  ^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z <Y1, for Y2> var;\n" + 
		"	       ^^^\n" + 
		"Syntax error on token \"for\", delete this token\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0042() {
	String[] testFiles = new String[] {
		"X.java",
		"void ___eval() {\n" +
		"	new Runnable() {\n" +
		"		int ___run() throws Throwable {\n" +
		"			return blah;\n" +
		"		}\n" +
		"		private String blarg;\n" +
		"		public void run() {\n" +
		"		}\n" +
		"	};\n" +
		"}\n" +
		"public class X {\n" +
		"	private static int x;\n" +
		"	private String blah;\n" +
		"	public static void main(String[] args) {\n" +
		"	}\n" +
		"	public void hello() {\n" +
		"	}\n" +
		"	public boolean blah() {\n" +
		"		return false;\n" +
		"	}\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	public void run() {\n" + 
		"		}\n" + 
		"	};\n" + 
		"}\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;
	
	String expected15ProblemLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	void ___eval() {\n" + 
		"	^^^^\n" + 
		"Syntax error on token \"void\", AT expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	void ___eval() {\n" + 
		"	             ^\n" + 
		"Syntax error on token \")\", delete this token\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 9)\n" + 
		"	};\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", { expected\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 23)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 23)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"}\" to complete MemberValueArrayInitializer\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 23)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \")\" to complete Modifiers\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 23)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"enum Identifier\" to complete EnumHeader\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 23)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"EnumBody\" to complete CompilationUnit\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
}
