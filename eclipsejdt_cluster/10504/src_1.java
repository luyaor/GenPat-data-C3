/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;

public class JavaElement8Tests extends AbstractJavaModelTests { 

	static {
//		TESTS_NAMES = new String[] {"testBug428178"};
	}

	public JavaElement8Tests(String name) {
		super(name);
		this.endChar = "";
	}
	public static Test suite() {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
			return buildModelTestSuite(JavaElement8Tests.class);
		}
		TestSuite suite = new Suite(JavaElement8Tests.class.getName());
		suite.addTest(new JavaElement8Tests("testBug428178"));
		suite.addTest(new JavaElement8Tests("testBug428178a"));
		suite.addTest(new JavaElement8Tests("testBug429641"));
		suite.addTest(new JavaElement8Tests("testBug429641a"));
		return suite;
	}
	public void testBug428178() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
				String fileContent =  "package p;\n" +
						 "public interface Test {\n" +
						 "	static void main(String[] args) {\n" +
						 "		System.out.println(\"Hello\");\n" +
						 "	}\n" +
						 "}";
				createFolder("/Bug428178/src/p");
				createFile(	"/Bug428178/src/p/Test.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Bug428178/src/p/Test.java");
				IMethod method = unit.getTypes()[0].getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
	public void testBug428178a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		System.out.println(\"Hello\");\n" +
					 "	}\n" +
					 "}";
			addLibrary(project, 
							"lib.jar", 
							"src.zip", new 
							String[] {"p/Test.java", fileContent},
							JavaCore.VERSION_1_8);
				IType type = getPackageFragmentRoot("Bug428178", "lib.jar").getPackageFragment("p").getClassFile("Test.class").getType();
				IMethod method = type.getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
	public void testBug429641() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429641", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		I i = (x) -> {};\n" +
					 "	}\n" +
					 "}\n" + 
					 "interface I {\n" + 
					 "  public void foo(int x);\n" +
					 "}";
			createFolder("/Bug429641/src/p");
			createFile(	"/Bug429641/src/p/Test.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429641/src/p/Test.java");
			int start = fileContent.indexOf("x) ->");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should be a lambda method", method.isLambdaMethod());
		}
		finally {
			deleteProject("Bug429641");
		}
	}
	public void testBug429641a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429641", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		I i = (x) -> {};\n" +
					 "	}\n" +
					 "}\n" + 
					 "interface I {\n" + 
					 "  public void foo(int x);\n" +
					 "}";
			createFolder("/Bug429641/src/p");
			createFile(	"/Bug429641/src/p/Test.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429641/src/p/Test.java");
			int start = fileContent.lastIndexOf("x");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should not be a lambda method", !method.isLambdaMethod());
		}
		finally {
			deleteProject("Bug429641");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown when a lambda expression is nested
	public void test429948() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429948", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface Supplier<T> {\n" +
					"    T get();\n" +
					"}\n" +
					"interface Runnable {\n" +
					"    public abstract void run();\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		execute(() -> {\n" +
					"			executeInner(() -> {\n" +
					"			});\n" +
					"			return null;\n" +
					"		});\n" +
					"		System.out.println(\"done\");\n" +
					"	}\n" +
					"	static <R> R execute(Supplier<R> supplier) {\n" +
					"		return null;\n" +
					"	}\n" +
					"	static void executeInner(Runnable callback) {\n" +
					"	}\n" +
					"}\n";
			createFile(	"/Bug429948/src/X.java",	fileContent);
			IType type = getCompilationUnit("/Bug429948/src/X.java").getType("X");
			ITypeHierarchy h = type.newSupertypeHierarchy(null);
			assertHierarchyEquals(
					"Focus: X [in X.java [in <default> [in src [in Bug429948]]]]\n" + 
					"Super types:\n" + 
					"  Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]\n" + 
					"Sub types:\n",
					h);
		}
		finally {
			deleteProject("Bug429948");
		}
	}
}
