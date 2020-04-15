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

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all compiler regression tests
 */
public class TestAll extends TestCase {

public TestAll(String testName) {
	super(testName);
}
public static Test suite() {
	ArrayList standardTests = new ArrayList();
	standardTests.addAll(JavadocTest.allTestClasses);
	standardTests.add(ArrayTest.class);
	standardTests.add(AssignmentTest.class);
	standardTests.add(BatchCompilerTest.class);
	standardTests.add(BooleanTest.class);
	standardTests.add(CastTest.class);
	standardTests.add(ClassFileComparatorTest.class);
	standardTests.add(ClassFileReaderTest.class);
	standardTests.add(ConstantTest.class);
	standardTests.add(DeprecatedTest.class);
	standardTests.add(LocalVariableTest.class);
	standardTests.add(LookupTest.class);
	standardTests.add(NumericTest.class);
	standardTests.add(ProblemConstructorTest.class);
	standardTests.add(ScannerTest.class);
	standardTests.add(SwitchTest.class);
	standardTests.add(TryStatementTest.class);
	standardTests.add(UtilTest.class);
	standardTests.add(XLargeTest.class);

	TestSuite all = new TestSuite(TestAll.class.getName());
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
	    ArrayList tests_1_3 = (ArrayList)standardTests.clone();
		tests_1_3.add(Compliance_1_3.class);
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_3, RegressionTestSetup.class, tests_1_3));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
	    ArrayList tests_1_4 = (ArrayList)standardTests.clone();
		tests_1_4.add(AssertionTest.class);
		tests_1_4.add(Compliance_1_4.class);
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_4, RegressionTestSetup.class, tests_1_4));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
	    ArrayList tests_1_5 = (ArrayList)standardTests.clone();
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_5, RegressionTestSetup.class, tests_1_5));
	}
	return all;
}
}
