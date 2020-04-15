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

import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunConverterTests extends TestCase {
public RunConverterTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		ASTConverterTest.class,		
		ASTConverterTest2.class,
		ASTConverterJavadocTest.class,
		ASTConverter15Test.class,
		ASTConverterAST3Test.class,
		ASTConverterTestAST3_2.class
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunConverterTests.class.getName());

	Class[] testClasses = getAllTestClasses();
	// Reset forgotten subsets of tests
	AbstractJavaModelTests.testsNames = null;
	AbstractJavaModelTests.testsNumbers = null;
	AbstractJavaModelTests.testsRange = null;

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
