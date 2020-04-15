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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import junit.framework.Test;

public class ClassFileTests extends ModifyingResourceTests {
	
	IPackageFragmentRoot jarRoot;
	
	public ClassFileTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new Suite(ClassFileTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject javaProject = createJavaProject("P");
		String[] pathAndContents = new String[] {
			"nongeneric/A.java", 
			"package nongeneric;\n" +
			"public class A {\n" + 
			"}",			
			"generic/X.java", 
			"package generic;\n" +
			"public class X<T> {\n" + 
			"  <U extends Exception> X<T> foo(X<T> x) throws RuntimeException, U {\n" +
			"    return null;\n" +
			"  }\n" +
			"  <K, V> V foo(K key, V value) throws Exception {\n" +
			"    return value;\n" +
			"  }\n" +
			"}",
			"generic/Y.java", 
			"package generic;\n" +
			"public class Y<K, V> {\n" + 
			"}",
			"generic/Z.java", 
			"package generic;\n" +
			"public class Z<T extends Object & I<? super T>> {\n" + 
			"}",
			"generic/I.java", 
			"package generic;\n" +
			"public interface I<T> {\n" + 
			"}",
			"generic/W.java", 
			"package generic;\n" +
			"public class W<T extends X<T> , U extends T> {\n" + 
			"}",
		};
		add1_5Library(javaProject, "lib.jar", "libsrc.zip", pathAndContents);
		this.jarRoot = javaProject.getPackageFragmentRoot(getFile("/P/lib.jar"));
			
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P");
	}
	
	/*
	 * Ensure that the exception types of a binary method are correct.
	 */
	public void testExceptionTypes1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertStringsEqual(
			"Unexpected return type",
			"Ljava.lang.Exception;\n",
			method.getExceptionTypes());
	}

	/*
	 * Ensure that the exception types of a binary method is correct.
	 */
	public void testExceptionTypes2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
		assertStringsEqual(
			"Unexpected return type",
			"Ljava.lang.RuntimeException;\n" + 
			"TU;\n",
			method.getExceptionTypes());
	}

	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("nongeneric").getClassFile("A.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures3() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Y.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"V:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}

	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures4() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Z.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;:Lgeneric.I<-TT;>;\n",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures5() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("W.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Lgeneric.X<TT;>;\n" + 
			"U:TT;\n",
			type.getTypeParameterSignatures());
	}

	/**
	 * Ensure that the type parameter signatures of a binary method are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures6() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"V:Ljava.lang.Object;\n",
			method.getTypeParameterSignatures());
	}
	
	/*
	 * Ensure that the return type of a binary method is correct.
	 */
	public void testReturnType1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertEquals(
			"Unexpected return type",
			"TV;",
			method.getReturnType());
	}

	/*
	 * Ensure that the return type of a binary method is correct.
	 */
	public void testReturnType2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
		assertEquals(
			"Unexpected return type",
			"Lgeneric.X<TT;>;",
			method.getReturnType());
	}
}
