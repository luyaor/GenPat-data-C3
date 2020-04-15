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

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Run all parser regression tests
 */
public class TestAll extends TestCase {

	public TestAll(String testName) {
		super(testName);
	}
	
	public static Test suite() {
		ArrayList testClasses = new ArrayList();

		/* completion tests */
		testClasses.add(AllocationExpressionCompletionTest.class);
		testClasses.add(ClassLiteralAccessCompletionTest.class);
		testClasses.add(CompletionParserTest.class);
		testClasses.add(CompletionRecoveryTest.class);
		testClasses.add(DietCompletionTest.class);
		testClasses.add(ExplicitConstructorInvocationCompletionTest.class);
		testClasses.add(FieldAccessCompletionTest.class);
		testClasses.add(InnerTypeCompletionTest.class);
		testClasses.add(LabelStatementCompletionTest.class);
		testClasses.add(MethodInvocationCompletionTest.class);
		testClasses.add(NameReferenceCompletionTest.class);
		testClasses.add(ReferenceTypeCompletionTest.class);
		testClasses.add(CompletionParserTest2.class);
		testClasses.add(CompletionParserTestKeyword.class);
		testClasses.add(GenericsCompletionParserTest.class);
		testClasses.add(AnnotationCompletionParserTest.class);

		/* selection tests */
		testClasses.add(ExplicitConstructorInvocationSelectionTest.class);
		testClasses.add(SelectionTest.class);
		testClasses.add(SelectionTest2.class);
		testClasses.add(GenericsSelectionTest.class);
		testClasses.add(AnnotationSelectionTest.class);
		
		/* recovery tests */
		testClasses.add(DietRecoveryTest.class);
		testClasses.add(GenericDietRecoveryTest.class);
		testClasses.add(EnumDietRecoveryTest.class);
		testClasses.add(AnnotationDietRecoveryTest.class);
		
		/* source element parser tests */
		testClasses.add(SourceElementParserTest.class);

		/* document element parser tests */
		testClasses.add(DocumentElementParserTest.class);

		/* syntax error diagnosis tests */
		testClasses.add(SyntaxErrorTest.class);
		testClasses.add(DualParseSyntaxErrorTest.class);
		testClasses.add(ParserTest.class);
		testClasses.add(ComplianceDiagnoseTest.class);

		return AbstractCompilerTest.suite(TestAll.class.getName(), CompilerTestSetup.class, testClasses);
	}
}