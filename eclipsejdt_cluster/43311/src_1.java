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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.DeltaProcessingState;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.Test;

/*
 * Test variable initializers and container initializers.
 */
public class ClasspathInitializerTests extends ModifyingResourceTests {
	
public static class DefaultVariableInitializer implements VariablesInitializer.ITestInitializer {
	Map variableValues;
	
	/*
	 * values is [<var name>, <var value>]*
	 */
	public DefaultVariableInitializer(String[] values) {
		variableValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			variableValues.put(values[i], new Path(values[i+1]));
		}
	}
	
	public void initialize(String variable) throws JavaModelException {
		if (variableValues == null) return;
		JavaCore.setClasspathVariable(
			variable, 
			(IPath)variableValues.get(variable), 
			null);
	}
}

public static class DefaultContainerInitializer implements ContainerInitializer.ITestInitializer {
	
	Map containerValues;
	CoreException exception;
	
	/*
	 * values is [<project name>, <lib path>[,<lib path>]* ]*
	 */
	public DefaultContainerInitializer(String[] values) {
		containerValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			final String projectName = values[i];
			final char[][] libPaths = CharOperation.splitOn(',', values[i+1].toCharArray());
			containerValues.put(
				projectName, 
				new IClasspathContainer() {
					public IClasspathEntry[] getClasspathEntries() {
						int length = libPaths.length;
						IClasspathEntry[] entries = new IClasspathEntry[length];
						for (int j = 0; j < length; j++) {
						    IPath path = new Path(new String(libPaths[j]));
						    if (path.segmentCount() == 1) {
						        entries[j] = JavaCore.newProjectEntry(path);
						    } else {
								entries[j] = JavaCore.newLibraryEntry(path, null, null);
						    }
						}
						return entries;
					}
					public String getDescription() {
						return "Test container";
					}
					public int getKind() {
						return IClasspathContainer.K_APPLICATION;
					}
					public IPath getPath() {
						return new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
					}
				}
			);
		}
	}
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (containerValues == null) return;
		try {
			JavaCore.setClasspathContainer(
				containerPath, 
				new IJavaProject[] {project},
				new IClasspathContainer[] {(IClasspathContainer)containerValues.get(project.getElementName())}, 
				null);
		} catch (CoreException e) {
			this.exception = e;
			throw e;
		}
	}
}
// Simple container initializer, which keeps setting container to null
// (30920 - stackoverflow when setting container to null)
public class NullContainerInitializer implements ContainerInitializer.ITestInitializer {
	public boolean hasRun = false;
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		hasRun = true;
		JavaCore.setClasspathContainer(
			containerPath, 
			new IJavaProject[] {project}, 
			new IClasspathContainer[] { null }, 
			null);
	}
}

public ClasspathInitializerTests(String name) {
	super(name);
}
public static Test suite() {
	if (false) {
		Suite suite = new Suite(ClasspathInitializerTests.class.getName());
		suite.addTest(new ClasspathInitializerTests("testContainerInitializer5"));
		return suite;
	}
	return new Suite(ClasspathInitializerTests.class);
}
protected void tearDown() throws Exception {
	// Cleanup caches
	JavaModelManager.Containers = new HashMap(5);
	JavaModelManager.Variables = new HashMap(5);

	super.tearDown();
}
public void testContainerInitializer1() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = this.createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(this.getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
public void testContainerInitializer2() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = this.createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup (flush containers, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionContainers = JavaModelManager.Containers;
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
public void testContainerInitializer3() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = this.createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// change value of TEST_CONTAINER
		this.createFile("/P1/lib2.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib2.jar"}));

		// simulate state on startup (flush containers, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionContainers = JavaModelManager.Containers;
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/* Ensure that initializer is not callled when resource tree is locked.
 * (regression test for bug 29585 Core Exception as resource tree is locked initializing classpath container)
 */
public void testContainerInitializer4() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		IJavaProject p2 = this.createJavaProject(
				"P2", 
				new String[] {""}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup (flush containers, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionContainers = JavaModelManager.Containers;
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		this.createFile("/P2/X.java", "public class X {}");
		
		assertEquals("Should not get exception", null, initializer.exception);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/* 
 * 30920 - Stack overflow when container resolved to null
 */
public void testContainerInitializer5() throws CoreException {
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		IJavaProject p1 = this.createJavaProject(
				"P1", 
				new String[] {""}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup (flush containers, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionContainers = JavaModelManager.Containers;
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p1);
		p1.close();
		
		startDeltas();

		// will trigger classpath resolution (with null container value)
		this.createFile("/P1/X.java", "public class X {}");
		assertDeltas(
			"Unexpected delta on startup", 
			"P1[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
		assertTrue("initializer did not run", nullInitializer.hasRun);
		
		// next cp resolution request will rerun the initializer
		waitForAutoBuild();
		nullInitializer.hasRun = false; // reset		
		p1.getResolvedClasspath(true);
		assertTrue("initializer did not run", nullInitializer.hasRun); // initializer should have run again (since keep setting to null)

		// assigning new (non-null) value to container
		waitForAutoBuild();
		this.createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		p1.getResolvedClasspath(true);
		assertDeltas(
			"Unexpected delta after setting container", 
			"P1[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}\n" + 
			"\n" + 
			"P1[*]: {CONTENT}\n" + 
			"	ResourceDelta(/P1/lib.jar)[+]"
		);

	} catch (StackOverflowError e) {
		e.printStackTrace();
		assertTrue("stack overflow assigning container", false);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
	}
}
/*
 * Ensures that running the initializer during a reconcile operation just after workspace startup
 * doesn't throw a NPE
 * (regression test for bug 48818 NPE in delta processor)
  */
public void testContainerInitializer6() throws CoreException {
    ICompilationUnit workingCopy = null;
	try {
		this.createProject("P1");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", ""}));
		IJavaProject p2 = this.createJavaProject(
				"P2", 
				new String[] {"src"}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"bin");
		createFile(
			"/P2/src/X,java",
			"public class X {\n" +
			"}"
		);
		workingCopy = getCompilationUnit("/P2/src/X.java");
				
		// change value of TEST_CONTAINER
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		// simulate state on startup (flush containers, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionContainers = JavaModelManager.Containers;
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(JavaModelManager.getJavaModelManager().deltaState);
		JavaModelManager.getJavaModelManager().deltaState = new DeltaProcessingState();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			JavaModelManager.getJavaModelManager().deltaState,
			IResourceChangeEvent.PRE_AUTO_BUILD
					| IResourceChangeEvent.POST_AUTO_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);
		
		startDeltas();
		workingCopy.becomeWorkingCopy(null, null);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CONTENT}\n" + 
			"	ResourceDelta(/P2/.project)[*]"
		);
	} finally {
		stopDeltas();
		if (workingCopy != null) workingCopy.discardWorkingCopy();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
public void testVariableInitializer1() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(this.getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer2() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		this.createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(this.getFile("/P1/lib.jar"));
		assertEquals("Unexpected source attachment path", "/P1/src.zip", root.getSourceAttachmentPath().toString());
		assertEquals("Unexpected source attachment root path", "src", root.getSourceAttachmentRootPath().toString());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer3() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		this.createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// simulate state on startup (flush variables, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionVariables = JavaModelManager.Variables;
		JavaModelManager.Variables = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer4() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		this.createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		this.deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer5() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				JavaCore.getClasspathVariable("TEST_SRC");
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		this.createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Initializing TEST_SRC\n" +
			"Setting variable TEST_SRC to test_src\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		this.deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that if the initializer doesn't initialize a variable, it can be
 * initialized later on.
 */
public void testVariableInitializer6() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) {
				// do nothing
				buffer.append("Ignoring request to initialize");
			}
		});
		IPath path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after initializer was called",
			null,
			path);
		IPath varValue = new Path("src.zip");
		JavaCore.setClasspathVariable("TEST_SRC", varValue, null);
		path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after setting it",
			varValue,
			path);
	} finally {
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer7() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		this.createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// change value of TEST_LIB
		this.createFile("/P1/lib2.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib2.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));

		// simulate state on startup (flush variables, and preserve their previous values)
		waitUntilIndexesReady();
		JavaModelManager.PreviousSessionVariables = JavaModelManager.Variables;
		JavaModelManager.Variables = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
}
