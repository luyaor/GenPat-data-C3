/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.Util;

public class WorkingCopyNotInClasspathTests extends ModifyingResourceTests {

	private ICompilationUnit workingCopy;

public WorkingCopyNotInClasspathTests(String name) {
	super(name);
}

public static Test suite() {
	if (false) {
		Suite suite = new Suite(WorkingCopyNotInClasspathTests.class.getName());
		suite.addTest(new WorkingCopyNotInClasspathTests("testCommit3"));
		return suite;
	}
	return new Suite(WorkingCopyNotInClasspathTests.class);
}

public void setUp() throws Exception {
	super.setUp();
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("P/txt");
		IFile file = this.createFile("P/txt/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit cu = (ICompilationUnit)JavaCore.create(file);	
		this.workingCopy = (ICompilationUnit)cu.getWorkingCopy();
	} catch (CoreException e) {
		e.printStackTrace();
	}
}

public void tearDown() throws Exception {
	try {
		if (this.workingCopy != null) {
			this.workingCopy.destroy();
			this.workingCopy = null;
		}
		this.deleteProject("P");
	} catch (CoreException e) {
		e.printStackTrace();
	}
	super.tearDown();
}

public void testCommit() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue("Original element should not be null", original != null);

	IBuffer workingCopyBuffer = this.workingCopy.getBuffer();
	assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);

	String newContents = 
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}";
	workingCopyBuffer.setContents(newContents);
	this.workingCopy.commit(false, null);
	
	IFile originalFile = (IFile)original.getResource();
	assertSourceEquals(
		"Unexpected contents", 
		newContents, 
		new String(Util.getResourceContentsAsCharArray(originalFile)));
}

/*
 * Ensure that a working copy outside the classpath does not exist 
 * (but can still be opened).
 */
public void testExistence() throws CoreException {
	assertTrue("Working copy should exist", this.workingCopy.exists());
}
public void testGetSource() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("P1", new String[] {}, "bin");
		this.createFolder("/P1/src/junit/test");
		String source = 
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/P1/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = (ICompilationUnit) cu.getWorkingCopy();
		assertEquals(
			"Unexpected source",
			source,
			copy.getSource());
	} finally {
		if (copy != null) copy.destroy();
		this.deleteProject("P1");
	}
}
public void testParentExistence() throws CoreException {
	assertTrue("Working copy's parent should not exist", !this.workingCopy.getParent().exists());
}
/*
 * Ensures that a working copy created on a non-existing project can be reconciled.
 * (regression test for bug 40322 Error creating new Java projects)
 */
public void testReconcileNonExistingProject() throws CoreException {
	ICompilationUnit wc = null;
	try {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getProject("NonExisting").getFile("A.java");
		wc = JavaCore.createCompilationUnitFrom(file).getWorkingCopy(null);
		wc.reconcile();
	} finally {
		if (wc != null) {
			wc.destroy();
		}
	}
}
/*
 * Ensure that a working copy created on a .java file in a simple project can be opened.
 * (regression test for bug 33748 Cannot open working copy on .java file in simple project)
 */
public void testSimpleProject() throws CoreException {
	IParent copy = null;
	try {
		createProject("SimpleProject");
		IFile file = createFile(
			"/SimpleProject/X.java",
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = (IParent)cu.getWorkingCopy();
		try {
			copy.getChildren();
		} catch (JavaModelException e) {
			assertTrue("Should not get JavaModelException", false);
		}
	} finally {
		if (copy != null) {
			((IWorkingCopy)copy).destroy();
		}
		deleteProject("SimpleProject");
	}
}

/*
 * Ensure that a original cu (which is outside the classpath) does not exist.
 */
public void testOriginalExistence() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue(
		"Original compilation unit should not exist", 
		!original.exists());
}
public void testOriginalParentExistence() throws CoreException {
	assertTrue(
		"Original compilation unit's parent should not exist", 
		!this.workingCopy.getOriginalElement().getParent().exists());
}
public void testIsOpen() throws CoreException {
	assertTrue("Working copy should be open", this.workingCopy.isOpen());
}
/*
 * Ensure that a original cu (which is outside the classpath) is not opened.
 */
public void testOriginalIsOpen() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue(
		"Original compilation should not be opened", 
		!original.isOpen());
}
// 31799 - asking project options on non-Java project populates the perProjectInfo cache incorrectly
public void testIsOnClasspath() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/junit/test");
		String source = 
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/SimpleProject/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = (ICompilationUnit) cu.getWorkingCopy();
		
		// working creation will cause it to open, and thus request project options
		boolean isOnClasspath = copy.getJavaProject().isOnClasspath(copy);
		assertTrue("working copy shouldn't answer to isOnClasspath", !isOnClasspath);
	} finally {
		if (copy != null) copy.destroy();
		this.deleteProject("SimpleProject");
	}
}

// 42281
public void testCommit2() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("JavaProject", new String[] {"src"}, "bin");
		this.createFolder("/JavaProject/src/native.1");
		String source = 
			"class X {}";
		IFile file = this.createFile("/JavaProject/src/native.1/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = (ICompilationUnit) cu.getWorkingCopy();
		
		IBuffer workingCopyBuffer = copy.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents = 
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";
			
		workingCopyBuffer.setContents(newContents);
		copy.reconcile(true, null);
		copy.commit(true, null);
		
		IFile originalFile = (IFile)cu.getResource();
		assertSourceEquals(
			"Unexpected contents", 
			newContents, 
			new String(Util.getResourceContentsAsCharArray(originalFile)));
	} catch(JavaModelException e) {
		e.printStackTrace();		
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (copy != null) copy.destroy();
		this.deleteProject("Project");
	}
}

// 41583
public void testCommit3() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/native.1");
		String source = 
			"class X {}";
		IFile file = this.createFile("/SimpleProject/src/native.1/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = (ICompilationUnit) cu.getWorkingCopy();
		
		IBuffer workingCopyBuffer = copy.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents = 
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";
			
		workingCopyBuffer.setContents(newContents);
		copy.reconcile(true, null);
		copy.commit(true, null);
		
		IFile originalFile = (IFile)cu.getResource();
		assertSourceEquals(
			"Unexpected contents", 
			newContents, 
			new String(Util.getResourceContentsAsCharArray(originalFile)));
	} catch(JavaModelException e) {
		e.printStackTrace();		
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (copy != null) copy.destroy();
		this.deleteProject("SimpleProject");
	}
}
}
