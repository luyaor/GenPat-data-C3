/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.BindingKey;

import junit.framework.Test;

public class BindingKeyTests extends AbstractJavaModelTests {
	
	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "test010"};
	}

	public BindingKeyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(BindingKeyTests.class);
	}
	
	protected void assertBindingKeySignatureEquals(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		String signature = bindingKey.toSignature();
		if (!(expected.equals(signature)))
			System.out.println(displayString(signature, 3) + ",");
		assertEquals(expected, signature);
	}
	
	/*
	 * Package.
	 */
	public void test001() {
		assertBindingKeySignatureEquals(
			"p",
			"p"
		);
	}

	/*
	 * Top level type in non default package.
	 */
	public void test002() {
		assertBindingKeySignatureEquals(
			"Lp.X;",
			"Lp/X;"
		);
	}

	/*
	 * Top level type in default package.
	 */
	public void test003() {
		assertBindingKeySignatureEquals(
			"LClazz;",
			"LClazz;"
		);
	}

	/*
	 * Member type
	 */
	public void test004() {
		assertBindingKeySignatureEquals(
			"Lp.X$Member;",
			"Lp/X$Member;"
		);
	}

	/*
	 * Member type (2 levels deep)
	 */
	public void test005() {
		assertBindingKeySignatureEquals(
			"Lp1.X$Member1$Member2;",
			"Lp1/X$Member1$Member2;"
		);
	}

	/*
	 * Anonymous type
	 */
	public void test006() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1;",
			"Lp1/X$1;"
		);
	}

	/*
	 * Local type
	 */
	public void test007() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1$Y;",
			"Lp1/X$1$Y;"
		);
	}

	/*
	 * Array type
	 */
	public void test008() {
		assertBindingKeySignatureEquals(
			"[Lp1.X;",
			"[Lp1/X;"
		);
	}

	/*
	 * Generic type
	 */
	public void test009() {
		assertBindingKeySignatureEquals(
			"Lp1.X<TT;>;",
			"Lp1/X<TT;>;"
		);
	}

	/*
	 * Generic type
	 */
	public void test010() {
		assertBindingKeySignatureEquals(
			"Lp1.X<TT;TU;>;",
			"Lp1/X<TT;TU;>;"
		);
	}

	/*
	 * Parameterized type
	 */
	public void test011() {
		assertBindingKeySignatureEquals(
			"Lp1.X<Ljava.lang.String;>;",
			"Lp1/X<Ljava/lang/String;>;"
		);
	}

	/*
	 * Secondary type
	 */
	public void test012() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary;",
			"Lp1/X~Secondary;"
		);
	}

	/*
	 * Anonymous in a secondary type
	 */
	public void test013() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary$1;",
			"Lp1/X~Secondary$1;"
		);
	}

}
