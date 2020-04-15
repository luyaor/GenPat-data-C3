/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;
import junit.framework.*;

public class ResolveTests2 extends ModifyingResourceTests {

static {
//	TESTS_NAMES = new String[] { "testSecondaryTypes" };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests2.class);
}

public ResolveTests2(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("Resolve");
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");

	super.tearDownSuite();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822a() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" +
				"  java.lang.Object var;\n"+
				"}");

		waitUntilIndexesReady();

		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");

		String str = cu.getSource();

		String selection = "java.lang.Object";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);

		assertElementsEqual(
			"Unexpected elements",
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]", // Object is found in another project with Object on his classpath
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822b() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" +
				"  javaz.lang.Objectz var;\n"+
				"}");

		waitUntilIndexesReady();

		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");

		String str = cu.getSource();

		String selection = "javaz.lang.Objectz";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822c() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" +
				"  java var;\n"+
				"}");

		waitUntilIndexesReady();

		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");

		String str = cu.getSource();

		String selection = "java";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822d() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" +
				"  javaz var;\n"+
				"}");

		waitUntilIndexesReady();

		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");

		String str = cu.getSource();

		String selection = "javaz";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880a() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test2/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1, externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test2");
		this.createFile(
				"/PS1/attachment/test2/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test2", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test1 [in "+outputDirectory + File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880b() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test2/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test2");
		this.createFile(
				"/PS1/attachment/test2/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test2", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880c() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test2/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test2");
		this.createFile(
				"/PS1/attachment/test2/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1},
			 "bin");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test2", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test1 [in "+outputDirectory+File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880d() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		String source1 =
			"package test1;\n" + //$NON-NLS-1$
			"public class IResource {\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
			
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					source1
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test2/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test2");
		this.createFile(
				"/PS1/attachment/test2/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			 "bin");

		this.createFolder("/PS2/src/test1");
		this.createFile(
				"/PS2/src/test1/IResource.java",
				source1);

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test2", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.java [in test1 [in src [in PS2]]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880e() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		String source1_1 =
			"package test1;\n" + //$NON-NLS-1$
			"public class CoreException extends Exception {\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		String source1_2 =
			"package test1;\n" + //$NON-NLS-1$
			"public class IResource {\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
			
		Util.createJar(
				new String[] {
					"test1/CoreException.java", //$NON-NLS-1$
					source1_1,
					"test1/IResource.java", //$NON-NLS-1$
					source1_2
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2_1 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.CoreException;\n" + //$NON-NLS-1$
			"public class JavaModelException extends CoreException {\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		String source2_2 =
			"package test2;\n" + //$NON-NLS-1$
			"import test1.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	void foo1() throws JavaModelException {}\n" + //$NON-NLS-1$
			"	IResource foo2() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test2/JavaModelException.java", //$NON-NLS-1$
					source2_1,
					"test2/IJavaElement.java", //$NON-NLS-1$
					source2_2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test2");
		this.createFile(
				"/PS1/attachment/test2/IJavaElement.java",
				source2_2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			 "bin");

		this.createFolder("/PS2/src/test1");
		this.createFile(
				"/PS2/src/test1/IResource.java",
				source1_2);

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test2", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.java [in test1 [in src [in PS2]]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880f() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}", //$NON-NLS-1$
					"test2/IResource.java", //$NON-NLS-1$
					"package test2;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test3;\n" + //$NON-NLS-1$
			"import test2.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test3/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test3");
		this.createFile(
				"/PS1/attachment/test3/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1},
			 "bin");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test3", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test2 [in "+outputDirectory+File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880g() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}", //$NON-NLS-1$
					"test2/IResource.java", //$NON-NLS-1$
					"package test2;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test3;\n" + //$NON-NLS-1$
			"import test2.*;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test3/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test3");
		this.createFile(
				"/PS1/attachment/test3/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1},
			 "bin");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test3", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test2 [in "+outputDirectory+File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232880
public void testBug232880h() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}", //$NON-NLS-1$
					"test2/IResource.java", //$NON-NLS-1$
					"package test2;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test3;\n" + //$NON-NLS-1$
			"import test1.*;\n" + //$NON-NLS-1$
			"import test2.IResource;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test3/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test3");
		this.createFile(
				"/PS1/attachment/test3/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1},
			 "bin");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test3", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test2 [in "+outputDirectory+File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
public void testBug232880i() throws Exception {
	String outputDirectory = Util.getOutputDirectory();
	String externalJar1 = outputDirectory + File.separator + "bug232880a.jar"; //$NON-NLS-1$
	String externalJar2 = outputDirectory + File.separator + "bug232880b.jar"; //$NON-NLS-1$
	try {
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test1/IResource.java", //$NON-NLS-1$
					"package test1;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}", //$NON-NLS-1$
					"test2/IResource.java", //$NON-NLS-1$
					"package test2;\n" + //$NON-NLS-1$
					"public class IResource {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		String source2 =
			"package test3;\n" + //$NON-NLS-1$
			"public class IJavaElement {\n" + //$NON-NLS-1$
			"	test2.IResource foo() {return null;}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		
		Util.createJar(
				new String[] {
					"test3/IJavaElement.java", //$NON-NLS-1$
					source2
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		IJavaProject project1 = this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			 "bin");
		
		this.createFolder("/PS1/attachment/test3");
		this.createFile(
				"/PS1/attachment/test3/IJavaElement.java",
				source2);
		
		IPackageFragmentRoot root = project1.getPackageFragmentRoot(externalJar2);
		attachSource(root, "/PS1/attachment/", "");
		
		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1},
			 "bin");

		waitUntilIndexesReady();

		// do code select
		IClassFile cf = getClassFile("PS1", externalJar2, "test3", "IJavaElement.class");
		
		IJavaElement[] elements = codeSelect(cf, "IResource foo", "IResource");

		assertElementsEqual(
			"Unexpected elements",
			"IResource [in IResource.class [in test1 [in "+outputDirectory+File.separator+"bug232880a.jar]]]\n" + 
			"IResource [in IResource.class [in test2 [in "+outputDirectory+File.separator+"bug232880a.jar]]]",
			elements
		);
	} finally {
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
		refreshExternalArchives(getJavaProject("PS1")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		refreshExternalArchives(getJavaProject("PS2")); // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083
		this.deleteProject("PS1");
		this.deleteProject("PS2");
	}
}
}
