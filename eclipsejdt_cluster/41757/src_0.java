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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
public class WorkingCopySearchTests extends JavaSearchTests {
	ICompilationUnit workingCopy;
	
public WorkingCopySearchTests(String name) {
	super(name);
}

public static Test suite() {
	if (false) {
		TestSuite suite = new Suite(WorkingCopySearchTests.class.getName());
		suite.addTest(new WorkingCopySearchTests("testAllTypeNames3"));
		return suite;
	}
	
	// NOTE: cannot use 'new Suite(WorkingCopySearchTests.class)' as this would include tests from super class
	TestSuite suite = new Suite(WorkingCopySearchTests.class.getName());
	
	suite.addTest(new WorkingCopySearchTests("testAddNewType"));
	suite.addTest(new WorkingCopySearchTests("testAllTypeNames1"));
	suite.addTest(new WorkingCopySearchTests("testAllTypeNames2"));
	suite.addTest(new WorkingCopySearchTests("testAllTypeNames3"));
	suite.addTest(new WorkingCopySearchTests("testAllTypeNames4"));
	suite.addTest(new WorkingCopySearchTests("testRemoveType"));
	suite.addTest(new WorkingCopySearchTests("testMoveType"));
	suite.addTest(new WorkingCopySearchTests("testHierarchyScopeOnWorkingCopy"));
	suite.addTest(new WorkingCopySearchTests("testDeclarationOfReferencedTypes"));

	return suite;
}

/**
 * Get a new working copy.
 */
protected void setUp() throws Exception {
	super.setUp();
	try {
		this.workingCopy = this.getCompilationUnit("JavaSearch", "src", "wc", "X.java").getWorkingCopy(null);
	} catch (JavaModelException e) {
		e.printStackTrace();
	}
}

/**
 * Destroy the working copy.
 */
protected void tearDown() throws Exception {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = null;
	super.tearDown();
}

/**
 * Hierarchy scope on a working copy test.
 */
public void testHierarchyScopeOnWorkingCopy() throws CoreException {
	ICompilationUnit unit = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
	ICompilationUnit copy = unit.getWorkingCopy(null);
	try {
		IType type = copy.getType("A");
		IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
		assertTrue("a9.A should be included in hierarchy scope", scope.encloses(type));
		assertTrue("a9.C should be included in hierarchy scope", scope.encloses(copy.getType("C")));
		assertTrue("a9.B should be included in hierarchy scope", scope.encloses(copy.getType("B")));
		IPath path = unit.getUnderlyingResource().getFullPath();
		assertTrue("a9/A.java should be included in hierarchy scope", scope.encloses(path.toString()));
	} finally {
		copy.discardWorkingCopy();
	}
}

/**
 * Type declaration in a working copy test.
 * A new type is added in the working copy only.
 */
public void testAddNewType() throws CoreException {
	this.workingCopy.createType(
		"class NewType {\n" +
		"}",
		null,
		false,
		null);
	
	IJavaSearchScope scope = 
		SearchEngine.createJavaSearchScope(
			new IJavaElement[] {this.workingCopy.getParent()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	SearchPattern pattern = SearchPattern.createPattern(
		"NewType",
		TYPE,
		DECLARATIONS, 
		SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	new SearchEngine(new ICompilationUnit[] {this.workingCopy}).search(
		pattern, 
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope, 
		resultCollector,
		null);
	assertSearchResults(
		"src/wc/X.java wc.NewType [NewType]", 
		resultCollector);
}

/*
 * Search all type names in working copies test.
 * (Regression test for bug 40793 Primary working copies: Type search does not find type in modified CU)
 */
public void testAllTypeNames1() throws CoreException {
	this.workingCopy.getBuffer().setContents(
		"package wc;\n" +
		"public class Y {\n" +
		"  interface I {\n" +
		"  }\n" +
		"}" 
	);
	this.workingCopy.makeConsistent(null);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {this.workingCopy.getParent()});
	SearchTests.TypeNameRequestor requestor = new SearchTests.TypeNameRequestor();
	new SearchEngine(new ICompilationUnit[] {this.workingCopy}).searchAllTypeNames(
		null,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		scope, 
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null		
	);
	assertSearchResults(
		"Unexpected all type names",
		"wc.Y\n" +
		"wc.Y$I",
		requestor);
}

/*
 * Search all type names in working copies test (without reconciling working copies).
 * (Regression test for bug 40793 Primary working copies: Type search does not find type in modified CU)
 */
public void testAllTypeNames2() throws CoreException {
	this.workingCopy.getBuffer().setContents(
		"package wc;\n" +
		"public class Y {\n" +
		"  interface I {\n" +
		"  }\n" +
		"}" 
	);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {this.workingCopy.getParent()});
	SearchTests.TypeNameRequestor requestor = new SearchTests.TypeNameRequestor();
	new SearchEngine(new ICompilationUnit[] {this.workingCopy}).searchAllTypeNames(
		null,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		scope, 
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null		
	);
	assertSearchResults(
		"Unexpected all type names",
		"wc.Y\n" +
		"wc.Y$I",
		requestor);
}

/*
 * Search all type names with a prefix in a primary working copy.
 * (regression test for bug 44884 Wrong list displayed while code completion)
 */
public void testAllTypeNames3() throws CoreException {
	ICompilationUnit wc = getCompilationUnit("/JavaSearch/wc3/X44884.java");
	try {
		wc.becomeWorkingCopy(null, null);
		wc.getBuffer().setContents(
			"package wc3;\n" +
			"public class X44884 {\n" +
			"}\n" +
			"interface I {\n" +
			"}"
		);
		wc.makeConsistent(null);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {wc.getParent()});
		SearchTests.TypeNameRequestor requestor = new SearchTests.TypeNameRequestor();
		new SearchEngine().searchAllTypeNames(
			"wc3".toCharArray(),
			"X".toCharArray(),
			SearchPattern.R_PREFIX_MATCH, // case insensitive
			TYPE,
			scope, 
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null		
		);
		assertSearchResults(
			"Unexpected all type names",
			"wc3.X44884",
			requestor);
	} finally {
		wc.discardWorkingCopy();
	}
}

/*
 * Search all type names with a prefix in a primary working copy (without reconciling it).
 * (regression test for bug 44884 Wrong list displayed while code completion)
 */
public void testAllTypeNames4() throws CoreException {
	ICompilationUnit wc = getCompilationUnit("/JavaSearch/wc3/X44884.java");
	try {
		wc.becomeWorkingCopy(null, null);
		wc.getBuffer().setContents(
			"package wc3;\n" +
			"public class X44884 {\n" +
			"}\n" +
			"interface I {\n" +
			"}"
		);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {wc.getParent()});
		SearchTests.TypeNameRequestor requestor = new SearchTests.TypeNameRequestor();
		new SearchEngine().searchAllTypeNames(
			"wc3".toCharArray(),
			"X".toCharArray(),
			SearchPattern.R_PREFIX_MATCH, // case insensitive
			TYPE,
			scope, 
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null		
		);
		assertSearchResults(
			"Unexpected all type names",
			"wc3.X44884",
			requestor);
	} finally {
		wc.discardWorkingCopy();
	}
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 5355 search: NPE in searchDeclarationsOfReferencedTypes  )
 */
public void testDeclarationOfReferencedTypes() throws CoreException {
	IMethod method = this.workingCopy.getType("X").createMethod(
		"public void foo() {\n" +
		"  X x = new X();\n" +
		"}",
		null,
		true,
		null);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/wc/X.java wc.X [X]", 
		resultCollector);
}

/**
 * Type declaration in a working copy test.
 * A type is moved from one working copy to another.
 */
public void testMoveType() throws CoreException {
	
	// move type X from working copy in one package to a working copy in another package
	ICompilationUnit workingCopy1 = getCompilationUnit("JavaSearch", "src", "wc1", "X.java").getWorkingCopy(null);
	ICompilationUnit workingCopy2 = getCompilationUnit("JavaSearch", "src", "wc2", "Y.java").getWorkingCopy(null);
	
	try {
		workingCopy1.getType("X").move(workingCopy2, null, null, true, null);
		
		SearchEngine searchEngine = new SearchEngine(new ICompilationUnit[] {workingCopy1, workingCopy2});
		
		// type X should not be visible in old package
		IJavaSearchScope scope1 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy1.getParent()});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		
		SearchPattern pattern = SearchPattern.createPattern(
			"X",
			TYPE,
			DECLARATIONS, 
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		searchEngine.search(
			pattern, 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope1, 
			resultCollector,
			null);
		assertEquals(
			"", 
			resultCollector.toString());
		
		// type X should be visible in new package
		IJavaSearchScope scope2 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy2.getParent()});
		resultCollector = new JavaSearchResultCollector();
		searchEngine.search(
			pattern, 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope2, 
			resultCollector,
			null);
		assertSearchResults(
			"src/wc2/Y.java wc2.X [X]", 
			resultCollector);
	} finally {
		workingCopy1.discardWorkingCopy();
		workingCopy2.discardWorkingCopy();
	}
}

/**
 * Type declaration in a working copy test.
 * A type is removed from the working copy only.
 */
public void testRemoveType() throws CoreException {
	this.workingCopy.getType("X").delete(true, null);
	
	IJavaSearchScope scope = 
		SearchEngine.createJavaSearchScope(
			new IJavaElement[] {this.workingCopy.getParent()});
	
	// type X should not be visible when working copy hides it
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	SearchPattern pattern = SearchPattern.createPattern(
		"X",
		TYPE,
		DECLARATIONS, 
		SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	new SearchEngine(new ICompilationUnit[] {this.workingCopy}).search(
		pattern, 
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope, 
		resultCollector,
		null);
	assertSearchResults(
		"", 
		resultCollector);
		
	// ensure the type is still present in the compilation unit
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		pattern, 
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope, 
		resultCollector,
		null);
	assertSearchResults(
		"src/wc/X.java wc.X [X]", 
		resultCollector);

}

}
