/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Signature;

import junit.framework.Test;

public class BindingKeyTests extends AbstractJavaModelTests {
	
	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "test028"};
	}

	public BindingKeyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(BindingKeyTests.class);
	}
	
	protected void assertBindingKeyEquals(String expected, String key) {
		if (!(expected.equals(key)))
			System.out.println(displayString(key, 3) + ",");
		assertEquals(expected, key);
	}
	
	protected void assertBindingKeySignatureEquals(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		String signature = bindingKey.toSignature();
		if (!(expected.equals(signature)))
			System.out.println(displayString(signature, 3) + ",");
		assertEquals(expected, signature);
	}
	
	protected void assertBindingKeyFlagsEquals(int expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		int flags = bindingKey.getFlags();
		assertEquals(expected, flags);
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
			"Lp/X;^1"
		);
	}

	/*
	 * Top level type in default package.
	 */
	public void test003() {
		assertBindingKeySignatureEquals(
			"LClazz;",
			"LClazz;^33"
		);
	}

	/*
	 * Member type
	 */
	public void test004() {
		assertBindingKeySignatureEquals(
			"Lp.X$Member;",
			"Lp/X$Member;^1"
		);
	}

	/*
	 * Member type (2 levels deep)
	 */
	public void test005() {
		assertBindingKeySignatureEquals(
			"Lp1.X$Member1$Member2;",
			"Lp1/X$Member1$Member2;^1"
		);
	}

	/*
	 * Anonymous type
	 */
	public void test006() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1;",
			"Lp1/X$1;^0"
		);
	}

	/*
	 * Local type
	 */
	public void test007() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1$Y;",
			"Lp1/X$1$Y;^0"
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
			"<T:>Lp1.X;",
			"Lp1/X<TT;>;^1"
		);
	}

	/*
	 * Generic type
	 */
	public void test010() {
		assertBindingKeySignatureEquals(
			"<T:U:>Lp1.X;",
			"Lp1/X<TT;TU;>;^1"
		);
	}

	/*
	 * Parameterized type
	 */
	public void test011() {
		assertBindingKeySignatureEquals(
			"Lp1.X<Ljava.lang.String;>;",
			"Lp1/X<Ljava/lang/String;>;^0"
		);
	}

	/*
	 * Secondary type
	 */
	public void test012() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary;",
			"Lp1/X~Secondary;^0"
		);
	}

	/*
	 * Anonymous in a secondary type
	 */
	public void test013() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary$1;",
			"Lp1/X~Secondary$1;^0"
		);
	}

	/*
	 * Method
	 * (regression test for bug 85811 BindingKey.toSignature should return method signature for methods)
	 */
	public void test014() {
		assertBindingKeySignatureEquals(
			"(Ljava.lang.String;I)Z",
			"Lp1/X;.foo(Ljava/lang/String;I)Z^1"
		);
	}
	
	/*
	 * Create a type binding key from a fully qualified name
	 */
	public void test015() {
		String key = BindingKey.createTypeBindingKey("java.lang.Object");
		assertBindingKeyEquals(
			"Ljava/lang/Object;",
			key);
	}

	/*
	 * Create a type binding key from a primitive type name
	 */
	public void test016() {
		String key = BindingKey.createTypeBindingKey("int");
		assertBindingKeyEquals(
			"I",
			key);
	}

	/*
	 * Create a type binding key from an array type name
	 */
	public void test017() {
		String key = BindingKey.createTypeBindingKey("boolean[]");
		assertBindingKeyEquals(
			"[Z",
			key);
	}
	
	/*
	 * Create a parameterized type binding key
	 */
	public void test018() {
		String key = BindingKey.createParameterizedTypeBindingKey("Ljava/util/Map<TK;TV;>;", new String[] {"Ljava/lang/String;", "Ljava/lang/Object;"});
		assertBindingKeyEquals(
			"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;",
			key);
	}

	/*
	 * Create a raw type binding key
	 */
	public void test019() {
		String key = BindingKey.createParameterizedTypeBindingKey("Ljava/util/List<TE:>;", new String[] {});
		assertBindingKeyEquals(
			"Ljava/util/List<>;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test020() {
		String key = BindingKey.createArrayTypeBindingKey("Ljava/lang/Object;", 1);
		assertBindingKeyEquals(
			"[Ljava/lang/Object;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test021() {
		String key = BindingKey.createArrayTypeBindingKey("I", 2);
		assertBindingKeyEquals(
			"[[I",
			key);
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test022() {
		String key = BindingKey.createWilcardTypeBindingKey(null, Signature.C_STAR);
		assertBindingKeyEquals(
			"*",
			key);
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test023() {
		String key = BindingKey.createWilcardTypeBindingKey("Ljava/util/List<TE;>;", Signature.C_SUPER);
		assertBindingKeyEquals(
			"-Ljava/util/List<TE;>;",
			key);
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test024() {
		String key = BindingKey.createWilcardTypeBindingKey("Ljava/util/ArrayList;", Signature.C_EXTENDS);
		assertBindingKeyEquals(
			"+Ljava/util/ArrayList;",
			key);
	}

	/*
	 * Create a type variable binding key
	 */
	public void test025() {
		String key = BindingKey.createTypeVariableBindingKey("T", "Ljava/util/List<TE;>;");
		assertBindingKeyEquals(
			"Ljava/util/List<TE;>;:TT;",
			key);
	}

	/*
	 * Create a type variable binding key
	 */
	public void test026() {
		String key = BindingKey.createTypeVariableBindingKey("SomeTypeVariable", "Lp/X;.foo()V");
		assertBindingKeyEquals(
			"Lp/X;.foo()V:TSomeTypeVariable;",
			key);
	}
	
	/*
	 * Parameterized member type
	 */
	public void test027() {
		assertBindingKeySignatureEquals(
			"Lp1.X<Ljava.lang.String;>.Member;",
			"Lp1/X<Ljava/lang/String;>.Member;"
		);
	}

	/*
	 * Wildcard binding (no bounds)
	 */
	public void test028() {
		assertBindingKeySignatureEquals(
			"*",
			"*"
		);
	}

	/*
	 * Wildcard binding (super bounds)
	 */
	public void test029() {
		assertBindingKeySignatureEquals(
			"-<E:>Ljava.util.List;",
			"-Ljava/util/List<TE;>;"
		);
	}

	/*
	 * Wildcard binding (extends bounds)
	 */
	public void test030() {
		assertBindingKeySignatureEquals(
			"+Ljava.util.ArrayList;",
			"+Ljava/util/ArrayList;"
		);
	}

	/*
	 * Capture binding (no bounds)
	 */
	public void test031() {
		assertBindingKeySignatureEquals(
			"*",
			"Ljava/util/List;!*123;"
		);
	}

	/*
	 * Capture binding (super bounds)
	 */
	public void test032() {
		assertBindingKeySignatureEquals(
			"-<E:>Ljava.util.List;",
			"Ljava/util/List;!-Ljava/util/List<TE;>;123;"
		);
	}

	/*
	 * Capture binding (extends bounds)
	 */
	public void test033() {
		assertBindingKeySignatureEquals(
			"+Ljava.util.ArrayList;",
			"Ljava/util/List;!+Ljava/util/ArrayList;123;"
		);
	}
	
	/*
	 * Flags of a top level type
	 */
	public void test034() {
		assertBindingKeyFlagsEquals(
			1,
			"Lp/X;^1"
		);
	}
	
	/*
	 * Ensure that isConstructor returns true for a constructor binding key.
	 */
	public void test035() {
		BindingKey key = new BindingKey("Lp/X;.()V");
		assertTrue(key + " should be a constructor key", key.isConstructor());
	}

	/*
	 * Ensure that isConstructor returns false for a method binding key.
	 */
	public void test036() {
		BindingKey key = new BindingKey("Lp/X;.foo()V");
		assertTrue(key + " should not be a constructor key", !key.isConstructor());
	}
	
	/*
	 * Ensure that isConstructor returns false for a type binding key.
	 */
	public void test037() {
		BindingKey key = new BindingKey("Lp/X;");
		assertTrue(key + " should not be a constructor key", !key.isConstructor());
	}
}