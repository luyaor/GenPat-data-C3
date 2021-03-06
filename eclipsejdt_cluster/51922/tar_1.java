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
package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.*;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RunAllTests extends junit.framework.TestCase {
public RunAllTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		org.eclipse.jdt.core.tests.dom.RunConverterTests.class,
		org.eclipse.jdt.core.tests.dom.ASTTest.class,
		org.eclipse.jdt.core.tests.dom.ASTVisitorTest.class,
		org.eclipse.jdt.core.tests.dom.ASTMatcherTest.class,
		org.eclipse.jdt.core.tests.dom.ASTStructuralPropertyTest.class,
		org.eclipse.jdt.core.tests.dom.ASTParserTest.class,
		org.eclipse.jdt.core.tests.dom.ASTModelBridgeTests.class,
		org.eclipse.jdt.core.tests.dom.BatchASTCreationTests.class,
//		org.eclipse.jdt.core.tests.dom.BindingEqualityTest.class,
		org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest.class,
		org.eclipse.jdt.core.tests.rewrite.modifying.ASTRewritingModifyingTest.class,
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunAllTests.class.getName());

	Class[] testClasses = getAllTestClasses();
	// Reset forgotten subsets of tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS = null;
	TestCase.TESTS_RANGE = null;

	for (int i = 0; i < testClasses.length; i++) {
		Class testClass = testClasses[i];

		// call the suite() method and add the resulting suite to the suite
		try {
			Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
			Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
			ts.addTest(suite);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	return ts;
}
}
