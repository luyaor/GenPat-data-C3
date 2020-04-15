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
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class IncrementalTests extends Tests {

	public IncrementalTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(IncrementalTests.class);
	}

	public void testDefaultPackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testDefaultPackage2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// TODO excluded test
	public void _testNewJCL() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		IPath object = env.addClass(root, "java.lang", "Object", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Object {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			

		incrementalBuild();
		expectingSpecificProblemFor(object, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.Throwable (typically some required class file is referencing a type outside the classpath)", object)); //$NON-NLS-1$ //$NON-NLS-2$
		
		//----------------------------
		//           Step 3
		//----------------------------
		IPath throwable = env.addClass(root, "java.lang", "Throwable", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Throwable {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			

		incrementalBuild();
		expectingSpecificProblemFor(object, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.RuntimeException (typically some required class file is referencing a type outside the classpath)", object)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(throwable, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.RuntimeException (typically some required class file is referencing a type outside the classpath)", throwable)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17329
	 */
	public void testRenameMainType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		/* A.java */
		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		/* B.java */
		IPath pathToB = env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class B extends A {}"); //$NON-NLS-1$

		/* C.java */
		IPath pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class _A {}"); //$NON-NLS-1$

		pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B { }"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToA, pathToB, pathToC });
		expectingSpecificProblemFor(pathToA, new Problem("_A", "The public type _A must be defined in its own file", pathToA)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToB, new Problem("B", "A cannot be resolved to a type", pathToB)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToC, new Problem("C", "The hierarchy of the type C is inconsistent", pathToC)); //$NON-NLS-1$ //$NON-NLS-2$

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 1
	 */
	public void testRemoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		IPath pathToAB = env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove AZ and touch BB */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToAB });
		expectingSpecificProblemFor(pathToAB, new Problem("AB", "AZ cannot be resolved to a type", pathToAB)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 2
	 */
	public void testRemoveSecondaryType2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		IPath pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove ZA and touch BB */
		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToBB });
		expectingSpecificProblemFor(pathToBB, new Problem("BB.foo()", "ZA cannot be resolved to a type", pathToBB)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveSecondaryType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveMemberType() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"import p.AZ.*; \n"+ //$NON-NLS-1$
			"import p.ZA.*; \n"+ //$NON-NLS-1$
			"public class AB extends M {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {static class M{}}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.AZ is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});
	}

	public void testMovePackage() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath[] exclusionPatterns = new Path[] {new Path("src2/")}; //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", exclusionPatterns, null); //$NON-NLS-1$ 
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src1/src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(src1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.removePackage(src1, "p"); //$NON-NLS-1$
		env.addClass(src2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeFromClassFile() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A extends Z {M[] m;}"); //$NON-NLS-1$

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z {static class M {}}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z { static class M {} }"); //$NON-NLS-1$

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		incrementalBuild(projectPath);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
	}
	
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=27658
	public void testObjectWithSuperInterfaces() throws JavaModelException {
		try {
			IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
			env.addExternalJars(projectPath, Util.getJavaClassLibs());
	
			// remove old package fragment root so that names don't collide
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
	
			IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
			env.addClass(root, "java.lang", "Object", //$NON-NLS-1$ //$NON-NLS-2$
				"package java.lang; \n"+ //$NON-NLS-1$
				"public class Object implements I {} \n"+ //$NON-NLS-1$
				"interface I {}	\n");	//$NON-NLS-1$
	
			fullBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root, 
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
				});
	
			env.addClass(root, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"public class X {}\n"); //$NON-NLS-1$
	
			incrementalBuild(projectPath);
	
			expectingOnlySpecificProblemsFor(
				root, 
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
				});

			env.addClass(root, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
				"package p; \n"+ //$NON-NLS-1$
				"public class Y extends X {}\n"); //$NON-NLS-1$
	
			incrementalBuild(projectPath);

			expectingOnlySpecificProblemsFor(
				root, 
				new Problem[]{
					new Problem("", "The type java.lang.Object cannot have a superclass or superinterfaces", new Path("/Project/src/java/lang/Object.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
				});

		} catch(StackOverflowError e){
			assertTrue("Infinite loop in cycle detection", false); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	
	/**
	 * Bugs 6461 
	 * TODO excluded test
	 */
	public void _testWrongCompilationUnitLocation() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath x = env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("X.class")); //$NON-NLS-1$
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingProblemsFor(x);
		expectingNoPresenceOf(bin.append("X.class")); //$NON-NLS-1$
	}
	
}
