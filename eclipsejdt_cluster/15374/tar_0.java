/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all parser regression tests
 */
public class TestAll extends TestCase {

/**
 * TestAll constructor comment.
 * @param testName java.lang.String
 */
public TestAll(String testName) {
	super(testName);
}
/**
 * Adds all the tests in the given class to the suite except
 * the ones that are excluded.
 */
public static void addTest(TestSuite suite, Class testClass) {
	TestSuite innerSuite = new TestSuite(testClass);
	suite.addTest(innerSuite);
}
public static Test suite() {
	TestSuite suite = new TestSuite(TestAll.class.getName());
	
	/* completion tests */
	addTest(suite, AllocationExpressionCompletionTest.class);
	addTest(suite, ClassLiteralAccessCompletionTest.class);
	addTest(suite, CompletionParserTest.class);
	addTest(suite, CompletionRecoveryTest.class);
	addTest(suite, DietCompletionTest.class);
	addTest(suite, ExplicitConstructorInvocationCompletionTest.class);
	addTest(suite, FieldAccessCompletionTest.class);
	addTest(suite, InnerTypeCompletionTest.class);
	addTest(suite, LabelStatementCompletionTest.class);
	addTest(suite, MethodInvocationCompletionTest.class);
	addTest(suite, NameReferenceCompletionTest.class);
	addTest(suite, ReferenceTypeCompletionTest.class);
	addTest(suite, CompletionParserTest2.class);
	addTest(suite, CompletionParserTestKeyword.class);

	/* selection tests */
	addTest(suite, ExplicitConstructorInvocationSelectionTest.class);
	addTest(suite, SelectionTest.class);
	addTest(suite, SelectionTest2.class);

	/* recovery tests */
	addTest(suite, DietRecoveryTest.class);

	/* source element parser tests */
	addTest(suite, SourceElementParserTest.class);

	/* syntax error diagnosis tests */
	addTest(suite, SyntaxErrorTest.class);

	return suite;
}
}
