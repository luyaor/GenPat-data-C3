/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class CreateMembersTests extends AbstractJavaModelTests {

	public CreateMembersTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new Suite(CreateMembersTests.class);
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		setUpJavaProject("CreateMembers");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("CreateMembers");

		super.tearDownSuite();
	}

	public void test001() throws JavaModelException {
		ICompilationUnit compilationUnit = getCompilationUnit("CreateMembers", "src", "", "A.java");
		assertNotNull("No compilation unit", compilationUnit);
		IType[] types = compilationUnit.getTypes();
		assertNotNull("No types", types);
		assertEquals("Wrong size", 1, types.length);
		IType type = types[0];
		type.createMethod("\tpublic void foo() {\n\t\tSystem.out.println(\"Hello World\");\n\t}\n", null, true, new NullProgressMonitor());
		String expectedSource = 
			"public class A {\n" + 
			"\n" + 
			"	public void foo() {\n" + 
			"		System.out.println(\"Hello World\");\n" + 
			"	}\n" +
			"}";
		assertSourceEquals("Unexpected source", expectedSource, type.getSource());
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86906
	public void test002() throws JavaModelException {
		Hashtable oldOptions = JavaCore.getOptions();
		try {
			Hashtable options = new Hashtable(oldOptions);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
			JavaCore.setOptions(options);
			ICompilationUnit compilationUnit = getCompilationUnit("CreateMembers", "src", "", "E.java");
			assertNotNull("No compilation unit", compilationUnit);
			IType[] types = compilationUnit.getTypes();
			assertNotNull("No types", types);
			assertEquals("Wrong size", 1, types.length);
			IType type = types[0];
			IField sibling = type.getField("j");
			type.createField("int i;", sibling, true, null);
			String expectedSource = 
				"public enum E {\n" + 
				"	E1, E2;\n" + 
				"	int i;\n" + 
				"	int j;\n" + 
				"}";
			assertSourceEquals("Unexpected source", expectedSource, type.getSource());
		} finally {
			JavaCore.setOptions(oldOptions);
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86906
	public void test003() throws JavaModelException {
		Hashtable oldOptions = JavaCore.getOptions();
		try {
			Hashtable options = new Hashtable(oldOptions);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
			JavaCore.setOptions(options);
			ICompilationUnit compilationUnit = getCompilationUnit("CreateMembers", "src", "", "Annot.java");
			assertNotNull("No compilation unit", compilationUnit);
			IType[] types = compilationUnit.getTypes();
			assertNotNull("No types", types);
			assertEquals("Wrong size", 1, types.length);
			IType type = types[0];
			IMethod sibling = type.getMethod("foo", new String[]{});
			type.createMethod("String bar();", sibling, true, null);
			String expectedSource = 
				"public @interface Annot {\n" + 
				"	String bar();\n" + 
				"\n" + 
				"	String foo();\n" + 
				"}";
			assertSourceEquals("Unexpected source", expectedSource, type.getSource());
		} finally {
			JavaCore.setOptions(oldOptions);
		}
	}
}
