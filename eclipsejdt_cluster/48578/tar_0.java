/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class ConditionalExpressionTest extends AbstractRegressionTest {

	public ConditionalExpressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test003" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ConditionalExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100162
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    final boolean isA = true;\n" +
				"    public static void main(String[] args) {\n" +
				"        X x = new X();\n" +
				"        System.out.print(x.isA ? \"SUCCESS\" : \"FAILURE\");\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107193
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class RecipeElement {\n" +
				"    public static final RecipeElement[] NO_CHILDREN= new RecipeElement[0]; \n" +
				"}\n" +
				"class Ingredient extends RecipeElement { }\n" +
				"class X extends RecipeElement {\n" +
				"    private Ingredient[] fIngredients;\n" +
				"    public RecipeElement[] getChildren() {\n" +
				"        return fIngredients == null ? NO_CHILDREN : fIngredients;\n" +
				"    }\n" +
				"}",
			},
			""
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426078, Bug 426078 - [1.8] VerifyError when conditional expression passed as an argument
	public void test003() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean isOdd(boolean what) {\n" +
				"		return square(what ? new Integer(1) : new Integer(2)) % 2 == 1; // trouble here\n" +
				"	}\n" +
				"	<T> int square(int i) {\n" +
				"		return i * i;\n" +
				"	}\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(new X().isOdd(true));\n" +
				"	}\n" +
				"}\n",
			},
			"true"
		);
	}
	
}
