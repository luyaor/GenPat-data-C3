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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.Util;

public class JavaProjectTests extends ModifyingResourceTests {
public JavaProjectTests(String name) {
	super(name);
}
protected void assertResources(String message, String expected, IResource[] resources) {
	// sort in alphabetical order
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object a, Object b) {
			IResource resourceA = (IResource)a;
			IResource resourceB = (IResource)b;
			return resourceA.getFullPath().toString().compareTo(resourceB.getFullPath().toString());
		}
	};
	Util.sort(resources, comparer);
	
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = resources.length; i < length; i++) {
		buffer.append(((IResource)resources[i]).getFullPath());
		if (i != length-1) {
			buffer.append("\n");
		}
	}

	String actual = buffer.toString();
	if (!expected.equals(actual)) {
		System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(buffer.toString(), 2));
	}
	
	assertEquals(message, expected, actual);
}
public static Test suite() {
	
	if (false) {
		TestSuite suite = new Suite(JavaProjectTests.class.getName());
		suite.addTest(new JavaProjectTests("testPackageFragmentRootRawEntryWhenDuplicate"));
		return suite;
	}
	TestSuite suite = new Suite(JavaProjectTests.class.getName());
	suite.addTest(new JavaProjectTests("testPackageFragmentRootRawEntry"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRootRawEntryWhenDuplicate"));
	suite.addTest(new JavaProjectTests("testProjectGetChildren"));
	suite.addTest(new JavaProjectTests("testProjectGetPackageFragments"));
	suite.addTest(new JavaProjectTests("testRootGetPackageFragments"));
	suite.addTest(new JavaProjectTests("testRootGetPackageFragments2"));
	suite.addTest(new JavaProjectTests("testInternalArchiveCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testExternalArchiveCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testProjectCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testPackageFragmentCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testPackageFragmentHasSubpackages"));
	suite.addTest(new JavaProjectTests("testIsDefaultPackage"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRootCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testJarPackageFragmentCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testCompilationUnitCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testClassFileCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testArchiveClassFileCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testBinaryTypeCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testSourceMethodCorrespondingResource"));
	suite.addTest(new JavaProjectTests("testOutputLocationNotAddedAsPackageFragment"));
	suite.addTest(new JavaProjectTests("testOutputLocationNestedInRoot"));
	suite.addTest(new JavaProjectTests("testChangeOutputLocation"));
	suite.addTest(new JavaProjectTests("testFindElementPackage"));
	suite.addTest(new JavaProjectTests("testFindElementClassFile"));
	suite.addTest(new JavaProjectTests("testFindElementCompilationUnit"));
	suite.addTest(new JavaProjectTests("testFindElementCompilationUnitDefaultPackage"));
	suite.addTest(new JavaProjectTests("testFindElementInvalidPath"));
	suite.addTest(new JavaProjectTests("testFindElementPrereqSimpleProject"));
	suite.addTest(new JavaProjectTests("testProjectClose"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRenameAndCreate"));
	suite.addTest(new JavaProjectTests("testFolderWithDotName"));
	suite.addTest(new JavaProjectTests("testPackageFragmentNonJavaResources"));
	suite.addTest(new JavaProjectTests("testPackageFragmentRootNonJavaResources"));
	suite.addTest(new JavaProjectTests("testAddNonJavaResourcePackageFragmentRoot"));
	suite.addTest(new JavaProjectTests("testFindPackageFragmentRootFromClasspathEntry"));
	suite.addTest(new JavaProjectTests("testGetClasspathOnClosedProject"));
	suite.addTest(new JavaProjectTests("testGetRequiredProjectNames"));
	suite.addTest(new JavaProjectTests("testGetNonJavaResources1"));
	suite.addTest(new JavaProjectTests("testGetNonJavaResources2"));
	suite.addTest(new JavaProjectTests("testGetNonJavaResources3"));
	suite.addTest(new JavaProjectTests("testGetNonJavaResources4"));
	suite.addTest(new JavaProjectTests("testSourceFolderWithJarName"));
	
	// The following test must be at the end as it deletes a package and this would have side effects
	// on other tests
	suite.addTest(new JavaProjectTests("testDeletePackageWithAutobuild"));
	return suite;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("JavaProjectTests");
	setUpJavaProject("JavaProjectSrcTests");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaProjectTests");
	deleteProject("JavaProjectSrcTests");
	super.tearDownSuite();
}


/**
 * Test adding a non-java resource in a package fragment root that correspond to
 * the project.
 * (Regression test for PR #1G58NB8)
 */
public void testAddNonJavaResourcePackageFragmentRoot() throws JavaModelException, CoreException {
	// get resources of source package fragment root at project level
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources", 2, resources.length); // .classpath and .project files
	assertTrue("resource should be an IFile",  resources[0] instanceof IFile);
	IFile resource = (IFile)resources[0];
	IPath newPath = root.getUnderlyingResource().getFullPath().append("TestNonJavaResource.abc");
	try {
		// copy and rename resource
		resource.copy(
			newPath, 
			true, 
			null);
		
		// ensure the new resource is present
		resources = root.getNonJavaResources();
		assertResources(
			"incorrect non java resources", 
			"/JavaProjectTests/.classpath\n" +
			"/JavaProjectTests/.project\n" +
			"/JavaProjectTests/TestNonJavaResource.abc",
			(IResource[])resources);
	} finally {
		// clean up
		resource.getWorkspace().getRoot().getFile(newPath).delete(true, null);
	}
}
/**
 * Test that a class file in a jar has no corresponding resource.
 */
public void testArchiveClassFileCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IClassFile cf= element.getClassFile("X.class");
	IResource corr = cf.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that a binary type
 * has a corresponding resource.
 */
public void testBinaryTypeCorrespondingResource() throws JavaModelException {
	IClassFile element= getClassFile("JavaProjectTests", "", "p", "Y.class");
	IType type= element.getType();
	IResource corr= type.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * When the output location is changed, package fragments can be added/removed
 */
public void testChangeOutputLocation() throws JavaModelException, CoreException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IContainer underLyingResource = (IContainer)project.getUnderlyingResource();
	IFolder folder= underLyingResource.getFolder(new Path("output"));

	try {
		startDeltas();
		project.setOutputLocation(folder.getFullPath(), null);
		IPackageFragment fragment= root.getPackageFragment("bin");
		assertTrue("bin package fragment should appear", getDeltaFor(fragment).getKind() == IJavaElementDelta.ADDED);
	} finally {
		stopDeltas();
		try {
			startDeltas();
			folder= underLyingResource.getFolder(new Path("bin"));	
			project.setOutputLocation(folder.getFullPath(), null);
			IPackageFragment fragment= root.getPackageFragment("bin");
			assertTrue("bin package fragment should be removed", getDeltaFor(fragment).getKind() == IJavaElementDelta.REMOVED);
		} finally {
			stopDeltas();
		}
	}
}
/**
 * Test that a class file
 * has a corresponding resource.
 */
public void testClassFileCorrespondingResource() throws JavaModelException {
	IClassFile element= getClassFile("JavaProjectTests", "", "p", "Y.class");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("p").getFile("Y.class");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a compilation unit
 * has a corresponding resource.
 */
public void testCompilationUnitCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("q").getFile("A.java");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project is incorrect for the compilation unit", "JavaProjectTests", corr.getProject().getName());
}
/**
 * Tests the fix for "1FWNMKD: ITPJCORE:ALL - Package Fragment Removal not reported correctly"
 */
public void testDeletePackageWithAutobuild() throws JavaModelException, CoreException, IOException {
	// close all project except JavaProjectTests so as to avoid side effects while autobuilding
	IProject[] projects = getWorkspaceRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		IProject project = projects[i];
		if (project.getName().equals("JavaProjectTests")) continue;
		project.close(null);
	}

	// turn autobuilding on
	IWorkspace workspace = getWorkspace();
	boolean autoBuild = workspace.isAutoBuilding();
	IWorkspaceDescription description = workspace.getDescription();
	description.setAutoBuilding(true);
	workspace.setDescription(description);

	startDeltas();
	IPackageFragment frag = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder folder = (IFolder) frag.getUnderlyingResource();
	try {
		folder.delete(true, null);
		assertTrue("should have been notified of package removal", getDeltaFor(frag) != null);
	} finally {
		stopDeltas();
		
		// turn autobuild off
		description.setAutoBuilding(autoBuild);
		workspace.setDescription(description);

		// reopen projects
		projects = getWorkspaceRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.getName().equals("JavaProjectTests")) continue;
			project.open(null);
		}
	}
}
/**
 * Test that an (external) jar
 * has no corresponding resource.
 */
public void testExternalArchiveCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot element= project.getPackageFragmentRoot(getExternalJCLPathString());
	IResource corr= element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that a compilation unit can be found for a binary type
 */
public void testFindElementClassFile() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("java/lang/Object.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.CLASS_FILE
		&& element.getElementName().equals("Object.class"));
}
/**
 * Test that a compilation unit can be found
 */
public void testFindElementCompilationUnit() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y/Main.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("Main.java"));
}
/**
 * Test that a compilation unit can be found in a default package
 */
public void testFindElementCompilationUnitDefaultPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("B.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("B.java"));
}
/**
 * Test that an invlaid path throws an exception
 */
public void testFindElementInvalidPath() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	boolean failed= false;
	try {
		project.findElement(null);
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);
	
	failed = false;
	try {
		project.findElement(new Path("/something/absolute"));
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);

	IJavaElement element= project.findElement(new Path("does/not/exist/HelloWorld.java"));
	assertTrue("should get no element", element == null);
}
/**
 * Test that a package can be found
 */
public void testFindElementPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y"));
	assertTrue("package not found" , element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT
		&& element.getElementName().equals("x.y"));
}
/**
 * Test that a class can be found even if the project prereq a simple project
 * (regression test for bug 28434 Open Type broken when workspace has build path problems)
 */
public void testFindElementPrereqSimpleProject() throws CoreException {
	try {
		this.createProject("R");
		IJavaProject project = this.createJavaProject("J", new String[] {"src"}, new String[] {}, new String[] {"/R"}, "bin");
		this.createFile(
			"J/src/X.java",
			"public class X {\n" +
			"}"
		);
		assertTrue("X.java not found", project.findElement(new Path("X.java")) != null);
	} finally {
		this.deleteProject("R");
		this.deleteProject("J");
	}
}
/**
 * Test that a package fragment root can be found from a classpath entry.
 */
public void testFindPackageFragmentRootFromClasspathEntry() throws JavaModelException {
	IJavaProject project = getJavaProject("JavaProjectTests");
	
	// existing classpath entry
	IClasspathEntry entry = JavaCore.newLibraryEntry(new Path("/JavaProjectTests/lib.jar"), null, null);
	IPackageFragmentRoot[] roots = project.findPackageFragmentRoots(entry);
	assertEquals("Unexpected number of roots for existing entry", 1, roots.length);
	assertEquals("Unexpected root", "/JavaProjectTests/lib.jar", roots[0].getPath().toString());
	
	// non-existing classpath entry
	entry = JavaCore.newSourceEntry(new Path("/JavaProjectTests/nonExisting"));
	roots = project.findPackageFragmentRoots(entry);
	assertEquals("Unexpected number of roots for non existing entry", 0, roots.length);

}
/**
 * Test that a folder with a dot name does not relate to a package fragment
 */
public void testFolderWithDotName() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IContainer folder= (IContainer)root.getCorrespondingResource();
	try {
		startDeltas();
		folder.getFolder(new Path("org.eclipse")).create(false, true, null);
		assertTrue("should be one Java Delta", this.deltaListener.deltas.length == 1);
		
		stopDeltas();
		IJavaElement[] children = root.getChildren();
		IPackageFragment bogus = root.getPackageFragment("org.eclipse");
		for (int i = 0; i < children.length; i++) {
			assertTrue("org.eclipse should not be present as child", !children[i].equals(bogus));
		}
		assertTrue("org.eclipse should not exist", !bogus.exists());
	} finally {
		folder.getFolder(new Path("org.eclipse")).delete(true, null);
	}	
}
/*
 * Ensures that getting the classpath on a closed project throws a JavaModelException
 * (regression test for bug 25358 Creating a new Java class - Browse for parent)
 */ 
public void testGetClasspathOnClosedProject() throws CoreException {
	IProject project = getProject("JavaProjectTests");
	try {
		project.close(null);
		boolean gotException = false;
		IJavaProject javaProject = JavaCore.create(project);
		try {
			javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			if (e.isDoesNotExist()) {
				gotException = true;
			}
		}
		assertTrue("Should get a not present exception for getRawClasspath()", gotException);
		gotException = false;
		try {
			javaProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			if (e.isDoesNotExist()) {
				gotException = true;
			}
		}
		assertTrue("Should get a not present exception for getResolvedClasspath(true)", gotException);
	} finally {
		project.open(null);
	}
}
/*
 * Ensures that the non-java resources for a project do not contain the project output location. 
 */
public void testGetNonJavaResources1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		assertResources(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			(IResource[])project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project do not contain a custom output location. 
 * (regression test for 27494  Source folder output folder shown in Package explorer)
 */
public void testGetNonJavaResources2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin1", new String[] {"bin2"});
		assertResources(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			(IResource[])project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project do not contain a folder that should be a package fragment.
 */
public void testGetNonJavaResources3() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("/P/p1");
		assertResources(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			(IResource[])project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project contain a folder that have an invalid name for a package fragment.
 * (regression test for bug 31757 Folder with invalid pkg name should be non-Java resource)
 */
public void testGetNonJavaResources4() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P");
		this.createFolder("/P/x.y");
		assertResources(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" + 
			"/P/.project\n" + 
			"/P/x.y",
			(IResource[])project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
} 
/*
 * Ensures that getRequiredProjectNames() returns the project names in the classpath order
 * (regression test for bug 25605 [API] someJavaProject.getRequiredProjectNames(); API should specify that the array is returned in ClassPath order)
 */
public void testGetRequiredProjectNames() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject(
			"P", 
			new String[] {}, 
			new String[] {}, 
			new String[] {"/JavaProjectTests", "/P1", "/P0", "/P2", "/JavaProjectSrcTests"}, 
			"");
		String[] requiredProjectNames = project.getRequiredProjectNames();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = requiredProjectNames.length; i < length; i++) {
			buffer.append(requiredProjectNames[i]);
			if (i != length-1) {
				buffer.append(", ");
			}
		}
		assertEquals(
			"Unexpected required project names",
			"JavaProjectTests, P1, P0, P2, JavaProjectSrcTests",
			buffer.toString());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that an (internal) jar
 * has a corresponding resource.
 */
public void testInternalArchiveCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFile("lib.jar");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test IJavaPackageFragment.isDefaultPackage().
 */
public void testIsDefaultPackage() throws JavaModelException {
	IPackageFragment def = getPackageFragment("JavaProjectTests", "", "");
	assertTrue("should be default package", def.isDefaultPackage());
	IPackageFragment y =
		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("x.y should not be default pakackage", !y.isDefaultPackage());

	IPackageFragment def2 = getPackageFragment("JavaProjectTests", "lib.jar", "");
	assertTrue("lib.jar should have default package", def2.isDefaultPackage());
	IPackageFragment p =
		getPackageFragment("JavaProjectTests", "lib.jar", "p");
	assertTrue("p should not be default package", !p.isDefaultPackage());
}
/**
 * Test that a package fragment in a jar has no corresponding resource.
 */
public void testJarPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IResource corr = element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that an output location can't be set to a location inside a package fragment
 * root, except the root project folder.
 */
public void testOutputLocationNestedInRoot() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectSrcTests", "src");
	IFolder folder= (IFolder) root.getUnderlyingResource();
	IJavaProject project= getJavaProject("JavaProjectSrcTests");
	folder= folder.getFolder("x");
	boolean failed= false;
	try {
		project.setOutputLocation(folder.getFullPath(), null);
	} catch (JavaModelException e) {
		assertTrue("should be an invalid classpath", e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_CLASSPATH);
		failed= true;
	}
	assertTrue("should have failed", failed);
	
}
/**
 * Test that an output location folder is not created as a package fragment.
 */
public void testOutputLocationNotAddedAsPackageFragment() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] packages= root.getChildren();
	assertEquals("unepected number of packages", 5, packages.length);
	assertTrue("should be default", packages[0].getElementName().equals(""));


	// create a nested folder in the output location and make sure it does not appear
	// as a package fragment
	IContainer underLyingResource = (IContainer)root.getUnderlyingResource();
	IFolder newFolder= underLyingResource.getFolder(new Path("bin")).getFolder(new Path("nested"));
	try {
		startDeltas();
		newFolder.create(false, true, null);
		assertTrue("should be one delta (resource deltas)", this.deltaListener.deltas != null || this.deltaListener.deltas.length == 1);
	} finally {
		stopDeltas();
		newFolder.delete(true, null);
	}
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragment element= getPackageFragment("JavaProjectTests", "", "x.y");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("x").getFolder("y");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentHasSubpackages() throws JavaModelException {
	IPackageFragment def=		getPackageFragment("JavaProjectTests", "", "");
	IPackageFragment x=		getPackageFragment("JavaProjectTests", "", "x");
	IPackageFragment y=		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("default should have subpackages",							def.hasSubpackages());
	assertTrue("x should have subpackages",								x.hasSubpackages());
	assertTrue("x.y should NOT have subpackages",		!y.hasSubpackages());

	IPackageFragment java = getPackageFragment("JavaProjectTests", getExternalJCLPathString(), "java");
	IPackageFragment lang= getPackageFragment("JavaProjectTests", getExternalJCLPathString(), "java.lang");

	assertTrue("java should have subpackages",					java.hasSubpackages());
	assertTrue("java.lang  should NOT have subpackages",			!lang.hasSubpackages());
}
/**
 * Test getting the non-java resources from a package fragment.
 */
public void testPackageFragmentNonJavaResources() throws JavaModelException {
	// regular source package with resources
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "x");
	Object[] resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 1)", 2, resources.length);

	// regular source package without resources
	pkg = getPackageFragment("JavaProjectTests", "", "x.y");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 2)", 0, resources.length);

	// source default package with potentialy resources
	pkg = getPackageFragment("JavaProjectTests", "", "");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 3)", 0, resources.length);

	// regular zip package with resources
	// TO DO

	// regular zip package without resources
	pkg = getPackageFragment("JavaProjectTests", "lib.jar", "p");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 5)", 0, resources.length);

	// zip default package with potentialy resources
	// TO DO
	
	// zip default package with potentialy no resources
	pkg = getPackageFragment("JavaProjectTests", "lib.jar", "");
	resources = pkg.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 7)", 0, resources.length);
	
}
/**
 * Tests that after a package "foo" has been renamed into "bar", it is possible to recreate
 * a "foo" package.
 * @see 1FWX0HY: ITPCORE:WIN98 - Problem after renaming a Java package
 */
public void testPackageFragmentRenameAndCreate() throws JavaModelException, CoreException {
	IPackageFragment y = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder yFolder = (IFolder) y.getUnderlyingResource();
	IPath yPath = yFolder.getFullPath();
	IPath fooPath = yPath.removeLastSegments(1).append("foo");
	
	yFolder.move(fooPath, true, null);
	try {
		yFolder.create(true, true, null);
	} catch (Throwable e) {
		e.printStackTrace();
		assertTrue("should be able to recreate the y folder", false);
	}
	// restore the original state
	yFolder.delete(true, null);
	IPackageFragment foo = getPackageFragment("JavaProjectTests", "", "x.foo");
	IFolder fooFolder = (IFolder) foo.getUnderlyingResource();
	fooFolder.move(yPath, true, null);
}
/**
 * Test that a package fragment root (non-external, non-jar, non-default root)
 * has a corresponding resource.
 */
public void testPackageFragmentRootCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project incorrect for folder resource", "JavaProjectTests", corr.getProject().getName());
}
/**
 * Test getting the non-java resources from a package fragment root.
 */
public void testPackageFragmentRootNonJavaResources() throws JavaModelException {
	// source package fragment root with resources
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 1)", 2, resources.length);

	// source package fragment root without resources
 	root = getPackageFragmentRoot("JavaProjectSrcTests", "src");
	resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 2)", 0, resources.length);

	// zip package fragment root with resources
	// TO DO
	
	// zip package fragment root without resources
	root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	resources = root.getNonJavaResources();
	assertEquals("incorrect number of non java resources (test case 4)", 0, resources.length);
}
/**
 * Test raw entry inference performance for package fragment root
 */
public void testPackageFragmentRootRawEntry() throws CoreException, IOException {
	File libDir = null;
	try {
		String libPath = EXTERNAL_JAR_DIR_PATH + File.separator + "lib";
		JavaCore.setClasspathVariable("MyVar", new Path(libPath), null);
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		libDir = new File(libPath);
		libDir.mkdirs();
		final int length = 200;
		IClasspathEntry[] classpath = new IClasspathEntry[length];
		for (int i = 0; i < length; i++){
			File libJar = new File(libDir, "lib"+i+".jar");
			libJar.createNewFile();
			classpath[i] = JavaCore.newVariableEntry(new Path("/MyVar/lib"+i+".jar"), null, null);
		}
		proj.setRawClasspath(classpath, null);
		
		IPackageFragmentRoot[] roots = proj.getPackageFragmentRoots();
		assertEquals("wrong number of entries:", length, roots.length);
		long start = System.currentTimeMillis();
		for (int i = 0; i < roots.length; i++){
			IClasspathEntry rawEntry = roots[i].getRawClasspathEntry();
			assertEquals("unexpected root raw entry:", classpath[i], rawEntry);
		}
		System.out.println((System.currentTimeMillis() - start)+ "ms for "+roots.length+" roots");
	} finally {
		if (libDir != null) {
			String[] libJars = libDir.list();
			if (libJars != null) {
				for (int i = 0, length = libJars.length; i < length; i++) {
					new File(libDir, libJars[i]).delete();
				}
			}
			libDir.delete();
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * Test raw entry inference performance for package fragment root in case
 * original classpath had duplicate entries pointing to it: first raw entry should be found
 */
public void testPackageFragmentRootRawEntryWhenDuplicate() throws CoreException, IOException {
	File libDir = null;
	try {
		String libPath = EXTERNAL_JAR_DIR_PATH + File.separator + "lib";
		JavaCore.setClasspathVariable("MyVar", new Path(EXTERNAL_JAR_DIR_PATH), null);
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		libDir = new File(libPath);
		libDir.mkdirs();
		IClasspathEntry[] classpath = new IClasspathEntry[2];
		File libJar = new File(libDir, "lib.jar");
		libJar.createNewFile();
		classpath[0] = JavaCore.newLibraryEntry(new Path(libPath).append("lib.jar"), null, null);
		classpath[1] = JavaCore.newVariableEntry(new Path("/MyVar").append("lib.jar"), null, null);
		proj.setRawClasspath(classpath, null);
		JavaCore.setClasspathVariable("MyVar", new Path(libPath), null); // change CP var value to cause collision
		
		IPackageFragmentRoot[] roots = proj.getPackageFragmentRoots();
		assertEquals("wrong number of entries:", 1, roots.length);
		IClasspathEntry rawEntry = roots[0].getRawClasspathEntry();
		assertEquals("unexpected root raw entry:", classpath[0], rawEntry); // ensure first entry is associated to the root
	} finally {
		if (libDir != null) {
			String[] libJars = libDir.list();
			if (libJars != null) {
				for (int i = 0, length = libJars.length; i < length; i++) {
					new File(libDir, libJars[i]).delete();
				}
			}
			libDir.delete();
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * Tests that closing and opening a project triggers the correct deltas.
 */
public void testProjectClose() throws JavaModelException, CoreException {
	IJavaProject jproject= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot[] originalRoots = jproject.getPackageFragmentRoots();
	IProject project= jproject.getProject();

	try {
		startDeltas();
		project.close(null);
		IJavaElementDelta delta= getDeltaFor(jproject);
		assertTrue("should be a removed delta", delta != null && delta.getKind() == IJavaElementDelta.REMOVED);
	} finally {
		try {
			clearDeltas();
			
			project.open(null);
			IJavaElementDelta delta= getDeltaFor(jproject);
			assertTrue("should be an added delta", delta != null && delta.getKind() == IJavaElementDelta.ADDED);

			IPackageFragmentRoot[] openRoots = jproject.getPackageFragmentRoots();
			assertTrue("should have same number of roots", openRoots.length == originalRoots.length);
			for (int i = 0; i < openRoots.length; i++) {
				assertTrue("root not the same", openRoots[i].equals(originalRoots[i]));
			}
		} finally {
			stopDeltas();
		}
	}
}
/**
 * Test that a project has a corresponding resource.
 */
public void testProjectCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IResource corr= project.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that the correct children exist in a project
 */
public void testProjectGetChildren() throws JavaModelException {
	IJavaProject project = getJavaProject("JavaProjectTests");
	IJavaElement[] roots= project.getChildren();
	assertTrue("should be package 3 package fragment root children in 'JavaProjectTests', were " + roots.length , roots.length == 3);
}
/**
 * Test that the correct package fragments exist in the project.
 */
public void testProjectGetPackageFragments() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragment[] fragments= project.getPackageFragments();
	assertTrue("should be package 12 package fragments in 'JavaProjectTests', were " + fragments.length , fragments.length == 12);
}
/**
 * Test that the correct package fragments exist in the project.
 */
public void testRootGetPackageFragments() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] fragments= root.getChildren();
	assertTrue("should be package 5 package fragments in source root, were " + fragments.length , fragments.length == 5);

	root= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	fragments= root.getChildren();	
	assertTrue("should be package 3 package fragments in lib.jar, were " + fragments.length , fragments.length == 3);
}
/**
 * Test that the correct package fragments exist in the project.
 * (regression test for bug 32041 Multiple output folders fooling Java Model)
 */
public void testRootGetPackageFragments2() throws CoreException {
	try {
		this.createJavaProject("P");
		this.createFolder("/P/bin");
		this.createFolder("/P/bin2");
		this.editFile(
			"/P/.classpath", 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" output=\"bin2\" path=\"\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		IPackageFragmentRoot root = getPackageFragmentRoot("/P");
		assertElementsEqual(
			"Unexpected packages",
			"",
			root.getChildren());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensure a source folder can have a name ending with ".jar"
 */
public void testSourceFolderWithJarName() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src.jar"}, "bin");
		IFile file = createFile("/P/src.jar/X.java", "class X {}");
		ICompilationUnit unit = (ICompilationUnit)JavaCore.create(file);
		unit.getAllTypes(); // force to open
	} catch (CoreException e) {
		assertTrue("unable to open unit in 'src.jar' source folder", false);
	} finally {
		this.deleteProject("P");
	}
}/**
 * Test that a method
 * has no corresponding resource.
 */
public void testSourceMethodCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IMethod[] methods = element.getType("A").getMethods();
	assertTrue("missing methods", methods.length > 0);
	IResource corr= methods[0].getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
}
