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

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugsTests extends AbstractJavaSearchTests implements IJavaSearchConstants {
	
	public JavaSearchBugsTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildTestSuite(JavaSearchBugsTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//		org.eclipse.jdt.internal.codeassist.SelectionEngine.DEBUG = true;
//		TESTS_PREFIX =  "testBug73112";
//		TESTS_NAMES = new String[] { "testBug83304" };
		TESTS_NUMBERS = new int[] { 79378 };
//		TESTS_RANGE = new int[] { 83304, -1 };
		}

	IJavaSearchScope getJavaSearchScopeBugs() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}
	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScopeBugs();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}
	protected void search(IJavaElement element, int limitTo) throws CoreException {
		search(element, limitTo, EXACT_RULE, getJavaSearchScopeBugs(), resultCollector);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule) throws CoreException {
		search(element, limitTo, matchRule, getJavaSearchScopeBugs(), resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
		search(patternString, searchFor, limitTo, EXACT_RULE, getJavaSearchScopeBugs(), resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo, int matchRule) throws CoreException {
		search(patternString, searchFor, limitTo, matchRule, getJavaSearchScopeBugs(), resultCollector);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.5");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}
	protected void setUp () throws Exception {
		super.setUp();
		resultCollector.showAccuracy = true;
	}
	/**
	 * Bug 41018: Method reference not found
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018"
	 */
	public void testBug41018() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		try {
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b41018/A.java",
				"package b41018;\n" +
				"public class A {\n" + 
				"	protected void anotherMethod() {\n" + 
				"		methodA(null);\n" + 
				"	}\n" + 
				"	private Object methodA(ClassB.InnerInterface arg3) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n" + 
				"class ClassB implements InterfaceB {\n" + 
				"}\n" + 
				"interface InterfaceB {\n" + 
				"	interface InnerInterface {\n" + 
				"	}\n" + 
				"}\n"
				);
			IType type = workingCopies[0].getType("A");
			IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
			search(method, REFERENCES);
			assertSearchResults(
				"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}
	/**
	 * Bug 70827: [Search] wrong reference match to private method of supertype
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827"
	 */
	public void testBug70827() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b70827/A.java",
			"package b70827;\n" + 
			"class A {\n" + 
			"	private void privateMethod() {\n" + 
			"	}\n" + 
			"}\n" + 
			"class Second extends A {\n" + 
			"	void call() {\n" + 
			"		int i= privateMethod();\n" + 
			"	}\n" + 
			"	int privateMethod() {\n" + 
			"		return 1;\n" + 
			"	}\n" + 
			"}\n"
			);
		IType type = workingCopies[0].getType("A");
		IMethod method = type.getMethod("privateMethod", new String[] {});
		search(method, REFERENCES);
		assertSearchResults(
			""
		);
	}

	/**
	 * Bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=71279"
	 */
	public void testBug71279() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector() {
		    public void beginReporting() {
		        results.append("Starting search...");
	        }
		    public void endReporting() {
		        results.append("\nDone searching.");
	        }
		};
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b71279/AA.java",
			"package b71279;\n" + 
			"public class AA {\n" + 
			"	Unknown ref;\n" + 
			"}\n"
			);
		new SearchEngine(workingCopies).searchDeclarationsOfReferencedTypes(workingCopies[0], result, null);
		assertSearchResults(
			"Starting search...\n" + 
			"Done searching.",
			result);
	}

	/**
	 * Bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=72866"
	 */
	public void testBug72866() throws CoreException {
		workingCopies = new ICompilationUnit[4];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b72866/A.java",
			"package b72866;\n" + 
			"public abstract class A {\n" + 
			"	public abstract void foo(V v);\n" + 
			"}\n",
			owner,
			true
			);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b72866/SX.java",
			"package b72866;\n" + 
			"public class SX extends A {\n" + 
			"	public void foo(V v) {\n" + 
			"	    v.bar(this);\n" + 
			"	}\n" + 
			"}\n"	,
			owner,
			true);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b72866/V.java",
			"package b72866;\n" + 
			"public class V {\n" + 
			"	void bar(A a) {}\n" + 
			"	void bar(X x) {}\n" + 
			"	void bar(SX s) {}\n" + 
			"}\n"	,
			owner,
			true);
		workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b72866/X.java",
			"package b72866;\n" + 
			"public class X extends A {\n" + 
			"	public void foo(V v) {\n" + 
			"	    v.bar(this);\n" + 
			"	}\n" + 
			"}\n"	,
			owner,
			true	);
		IType type = workingCopies[2].getType("V");
		IMethod method = type.getMethod("bar", new String[] {"QX;"});
		search(method, REFERENCES);
		assertSearchResults(
			"src/b72866/X.java void b72866.X.foo(V) [bar(this)] EXACT_MATCH"
		);
	}

	/**
	 * Bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73112"
	 */
	public void testBug73112a() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73112/A.java",
			"package b73112;\n" + 
			"public class A {\n" + 
			"    int fieldA73112a = 1, fieldA73112b = new Integer(2).intValue(), fieldA73112c = fieldA73112a + fieldA73112b;\n" + 
			"    int fieldA73112d;\n" + 
			"    \n" + 
			"    public void method(){}\n" + 
			"}\n");
		// search field references to first multiple field
		search("fieldA73112*", FIELD, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b73112/A.java b73112.A.fieldA73112a [fieldA73112a] EXACT_MATCH\n" + 
			"src/b73112/A.java b73112.A.fieldA73112b [fieldA73112b] EXACT_MATCH\n" + 
			"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112c] EXACT_MATCH\n" + 
			"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112a] EXACT_MATCH\n" + 
			"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112b] EXACT_MATCH\n" + 
			"src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d] EXACT_MATCH"
		);
	}
	public void testBug73112b() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = super.getWorkingCopy("/JavaSearchBugs/src/b73112/B.java",
			"package b73112;\n" + 
			"public class B {\n" + 
			"    int fieldB73112a, fieldB73112b = 10;\n" + 
			"    int fieldB73112c = fieldB73112a + fieldB73112b, fieldB73112d = fieldB73112c + fieldB73112a, fieldB73112e;\n" + 
			"    \n" + 
			"    public void method(){}\n" + 
			"}\n");
		// search field references to first multiple field
		search("fieldB73112*", FIELD, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b73112/B.java b73112.B.fieldB73112a [fieldB73112a] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112b [fieldB73112b] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112c] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112a] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112b] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112d] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112c] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112a] EXACT_MATCH\n" + 
			"src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e] EXACT_MATCH"
		);
	}

	/**
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73336"
	 */
	public void testBug73336() throws CoreException {
		workingCopies = new ICompilationUnit[6];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336/A.java",
			"package b73336;\n" + 
			"public class A {}\n",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336/AA.java",
			"package b73336;\n" + 
			"public class AA extends A {}\n",
			owner,
			true);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336/B.java",
			"package b73336;\n" + 
			"public class B extends X<A, A> {\n" + 
			"	<T> void foo(T t) {}\n" + 
			"}\n",
			owner,
			true);
		workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336/C.java",
			"package b73336;\n" + 
			"public class C implements I<A> {\n" + 
			"	public void foo() {\n" + 
			"		B b = new B();\n" + 
			"		b.<A>foo(new A());\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true	);
		workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b73336/I.java",
			"package b73336;\n" + 
			"public interface I<T>  {\n" + 
			"	public void foo();\n" + 
			"}\n",
			owner,
			true	);
		workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b73336/X.java",
			"package b73336;\n" + 
			"public class X<T, U> {\n" + 
			"	<V> void foo(V v) {}\n" + 
			"	class Member<T> {\n" + 
			"		void foo() {}\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true	);
		// search for first and second method should both return 2 inaccurate matches
		IType type = workingCopies[0].getType("A");
		search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
		assertSearchResults(
			"src/b73336/AA.java b73336.AA [A] EXACT_MATCH\n" + 
			"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
			"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
			"src/b73336/C.java b73336.C [A] EXACT_MATCH\n" + 
			"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH\n" + 
			"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH"
		);
	}
	public void testBug73336b() throws CoreException {
		workingCopies = new ICompilationUnit[4];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336b/A.java",
			"package b73336b;\n" + 
			"public class A {}\n",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336b/B.java",
			"package b73336b;\n" + 
			"public class B extends X<A, A> {\n" + 
			"}\n",
			owner,
			true);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336b/C.java",
			"package b73336b;\n" + 
			"public class C extends X<A, A>.Member<A> {\n" + 
			"	public C() {\n" + 
			"		new X<A, A>().super();\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true);
		workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336b/X.java",
			"package b73336b;\n" + 
			"public class X<T, U> {\n" + 
			"	<V> void foo(V v) {}\n" + 
			"	class Member<T> {\n" + 
			"		void foo() {}\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true	);
		// search for first and second method should both return 2 inaccurate matches
		IType type = workingCopies[0].getType("A");
//		search(type, REFERENCES, getJavaSearchScopeBugs("b73336b", false));
		search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
		assertSearchResults(
			"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
			"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH"
		);
	}
	// Verify that no NPE was raised on following case (which produces compiler error)
	public void testBug73336c() throws CoreException {
		workingCopies = new ICompilationUnit[4];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336c/A.java",
			"package b73336c;\n" + 
			"public class A {}\n",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336c/B.java",
			"package b73336c;\n" + 
			"public class B extends X<A, A> {\n" + 
			"}\n",
			owner,
			true);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336c/C.java",
			"package b73336c;\n" + 
			"public class C implements X<A, A>.Interface<A>  {\n" + 
			"	void bar() {}\n" + 
			"}\n",
			owner,
			true);
		workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336c/X.java",
			"package b73336c;\n" + 
			"public class X<T, U> {\n" + 
			"	interface Interface<V> {\n" + 
			"		void bar();\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true	);
		// search for first and second method should both return 2 inaccurate matches
		IType type = workingCopies[0].getType("A");
//		search(type, REFERENCES, getJavaSearchScopeBugs("b73336c", false));
		search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
		assertSearchResults(
			"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
			"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
			"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
			"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
			"src/b73336c/C.java b73336c.C [A] EXACT_MATCH"
		);
	}

	/**
	 * Bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696"
	 */
	public void testBug73696() throws CoreException {
		workingCopies = new ICompilationUnit[2];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73696/C.java",
			"package b73696;\n" + 
			"public class C implements  I {\n" + 
			"}",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73696/I.java",
			"package b73696;\n" + 
			"public interface I {}\n",
			owner,
			true);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(workingCopies);
		
		// Interface declaration
		TypeDeclarationPattern pattern = new TypeDeclarationPattern(
			null,
			null,
			null,
			IIndexConstants.INTERFACE_SUFFIX,
			SearchPattern.R_PATTERN_MATCH
		);
		new SearchEngine(new ICompilationUnit[] {workingCopies[1]}).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			resultCollector,
			null);
		// Class declaration
		pattern = new TypeDeclarationPattern(
			null,
			null,
			null,
			IIndexConstants.CLASS_SUFFIX,
			SearchPattern.R_PATTERN_MATCH
		);
		new SearchEngine(new ICompilationUnit[] {workingCopies[0]}).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			resultCollector,
			null);
		assertSearchResults(
			"src/b73696/I.java b73696.I [I] EXACT_MATCH\n" + 
			"src/b73696/C.java b73696.C [C] EXACT_MATCH"
		);
	}

	/**
	 * Bug 74776: [Search] Wrong search results for almost identical method
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776"
	 */
	public void testBug74776() throws CoreException {
		workingCopies = new ICompilationUnit[3];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b74776/A.java",
			"package b74776;\n" + 
			"public class A {\n" + 
			"	/**\n" + 
			"	 * @deprecated Use {@link #foo(IRegion)} instead\n" + 
			"	 * @param r\n" + 
			"	 */\n" + 
			"	void foo(Region r) {\n" + 
			"		foo((IRegion)r);\n" + 
			"	}\n" + 
			"	void foo(IRegion r) {\n" + 
			"	}\n" + 
			"}\n",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b74776/IRegion.java",
			"package b74776;\n" + 
			"public interface IRegion {\n" + 
			"}\n",
			owner,
			true);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b74776/Region.java",
			"package b74776;\n" + 
			"public class Region implements IRegion {\n" + 
			"\n" + 
			"}\n",
			owner,
			true);
		// search method references
		IType type = workingCopies[0].getType("A");
		IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
		search(method, REFERENCES);
		assertSearchResults("");
	}

	/**
	 * Bug 77093: [search] No references found to method with member type argument
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=77093"
	 */
	public void testBug77093constructor() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77093/X.java",
			"package b77093;\n" + 
			"public class X {\n" + 
			"	class Z {\n" + 
			"	}\n" + 
			"	Z[][] z_arrays;\n" + 
			"	X() {\n" + 
			"		this(new Z[10][]);\n" + 
			"	}\n" + 
			"	X(Z[][] arrays) {\n" + 
			"		z_arrays = arrays;\n" + 
			"	}\n" + 
			"	private void foo(Z[] args) {\n" + 
			"	}\n" + 
			"	void bar() {\n" + 
			"		for (int i=0; i<z_arrays.length; i++)\n" + 
			"			foo(z_arrays[i]);\n" + 
			"	}\n" + 
			"}");
		IType type = workingCopies[0].getType("X");
		IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
		// Search for constructor declarations and references
		search(method, ALL_OCCURRENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b77093/X.java b77093.X() [this(new Z[10][])] EXACT_MATCH\n"+
			"src/b77093/X.java b77093.X(Z[][]) [X] EXACT_MATCH"
		);
	}
	public void testBug77093field() throws CoreException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = workingCopies[0].getType("X");
		IField field = type.getField("z_arrays");
		// Search for field declarations and references
		search(field, ALL_OCCURRENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b77093/X.java b77093.X.z_arrays [z_arrays] EXACT_MATCH\n" +
			"src/b77093/X.java b77093.X(Z[][]) [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH"
		);
	}
	public void testBug77093method() throws CoreException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = workingCopies[0].getType("X");
		IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
		search(method, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b77093/X.java void b77093.X.foo(Z[]) [foo] EXACT_MATCH\n" +
			"src/b77093/X.java void b77093.X.bar() [foo(z_arrays[i])] EXACT_MATCH"
		);
	}

	/**
	 * Bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
	 */
	public void testBug77388() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77388/Test.java",
			"package b77388;\n" + 
			"class Test {\n" + 
			"	Test(int a, int b) {	}\n" + 
			"	void take(Test mc) { }\n" + 
			"	void run() {\n" + 
			"		take( new Test(1, 2) ); // space in \") )\" is in match\n" + 
			"	}\n" + 
			"}");
		IType type = workingCopies[0].getType("Test");
		IMethod method = type.getMethod("Test", new String[] {"I", "I"});
		// Search for constructor references
		search(method, REFERENCES);
		assertSearchResults(
			"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH"
		);
	}
	/**
	 * Bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=78082"
	 */
	public void testBug78082() throws CoreException {
		workingCopies = new ICompilationUnit[2];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b78082/M.java",
			"package b78082;\n" + 
			"public class M {\n" + 
			"	static int VAL=78082;\n" + 
			"}\n",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b78082/XY.java",
			"package b78082;\n" + 
			"import static b78082.M.VAL;\n" + 
			"public class XY {\n" + 
			"	double val = VAL;\n" + 
			"	double val2= b78082.M.VAL;\n" + 
			"}\n",
			owner,
			true);
		// search field references
		IType type = workingCopies[0].getType("M");
		IField field = type.getField("VAL");
		search(field, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b78082/M.java b78082.M.VAL [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java b78082.XY.val [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java b78082.XY.val2 [VAL] EXACT_MATCH"
		);
	}

	/**
	 * Bug 79267: [search] Refactoring of static generic member fails partially
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79267"
	 */
	public void testBug79267() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79267/Test.java",
			"package b79267;\n" + 
			"public class Test {\n" + 
			"	private static final X<String, String> BEFORE	= new X<String, String>(4);\n" + 
			"\n" + 
			"	static {\n" + 
			"		BEFORE.put(\"key1\",\"value1\");\n" + 
			"		BEFORE.put(\"key2\",\"value2\");\n" + 
			"	}\n" + 
			"	\n" + 
			"	private static final X<Y, Object>	objectToPrimitiveMap	= new X<Y, Object>(8);\n" + 
			"\n" + 
			"	static {\n" + 
			"		objectToPrimitiveMap.put(new Y<Object>(new Object()), new Object());\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"class X<T, U> {\n" + 
			"	X(int x) {}\n" + 
			"	void put(T t, U u) {}\n" + 
			"}\n" + 
			"\n" + 
			"class Y<T> {\n" + 
			"	Y(T t) {}\n" + 
			"}\n");
		// search field references
		IType type = workingCopies[0].getType("Test");
		IField field = type.getField("BEFORE");
		search(field, REFERENCES);
		field = type.getField("objectToPrimitiveMap");
		search(field, REFERENCES);
		assertSearchResults(
			"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
			"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
			"src/b79267/Test.java b79267.Test.static {} [objectToPrimitiveMap] EXACT_MATCH"
		);
	}

	/**
	 * Bug 79378: [search] IOOBE when inlining a method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79378"
	 */
	public void testBug79378() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
			"package b79378;\n" + 
			"public class Test {\n" + 
			"	void foo79378(String s, RuntimeException[] exceptions) {}\n" + 
			"	void foo79378(RuntimeException[] exceptions) {}\n" + 
			"	void call() {\n" + 
			"		String s= null; \n" + 
			"		Exception[] exceptions= null;\n" + 
			"		foo79378(s, exceptions);\n" + 
			"	}\n" + 
			"}\n"
		);
		IMethod[] methods = workingCopies[0].getType("Test").getMethods();
		assertEquals("Invalid number of methods", 3, methods.length);
		search(methods[0], REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b79378/A.java void b79378.Test.call() [foo79378(s, exceptions)] POTENTIAL_MATCH"
		);
	}
	public void testBug79378b() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IMethod[] methods = workingCopies[0].getType("Test").getMethods();
		assertEquals("Invalid number of methods", 3, methods.length);
		search(methods[1], REFERENCES);
		assertSearchResults("");
	}

	/**
	 * Bug 79803: [1.5][search] Search for references to type A reports match for type variable A
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79803"
	 */
	public void testBug79803() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
			"package b79803;\n" + 
			"class A<A> {\n" + 
			"    A a;\n" + 
			"    b79803.A pa= new b79803.A();\n" + 
			"}\n"	);
		IType type = workingCopies[0].getType("A");
		search(type, REFERENCES, ERASURE_RULE);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH"
		);
	}
	public void testBug79803string() throws CoreException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("A", TYPE, REFERENCES);
		assertSearchResults(
			"src/b79803/A.java b79803.A.a [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH"
		);
	}

	/**
	 * Bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79860"
	 */
	public void testBug79860() throws CoreException {
		workingCopies = new ICompilationUnit[2];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
			"package b79860;\n" + 
			"public class X<T extends A> { }\n" + 
			"class A { }",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
			"package b79860;\n" + 
			"public class Y<T extends B&I1&I2&I3> { }\n" + 
			"class B { }\n" + 
			"interface I1 {}\n" + 
			"interface I2 {}\n" + 
			"interface I3 {}\n",
			owner,
			true);
		IType type = workingCopies[0].getType("A");
		search(type, REFERENCES, getJavaSearchScopeBugs("b79860", false));
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b79860/X.java b79860.X [A] EXACT_MATCH"
		);
	}
	public void testBug79860string() throws CoreException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		search("I?", TYPE, REFERENCES, getJavaSearchScopeBugs("b79860", false), resultCollector);
		assertSearchResults(
			"src/b79860/Y.java b79860.Y [I1] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I2] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I3] EXACT_MATCH"
		);
	}

	/**
	 * Bug 80084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80084"
	 */
	public void testBug80084() throws CoreException, JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80084/Test.java",
			"package b80084;\n" + 
			"class List<T> {}\n" + 
			"public class Test {\n" + 
			"  void foo(List<Exception> le) {}\n" + 
			"  void bar() {\n" + 
			"    List<Exception> le = new List<Exception>();\n" + 
			"    foo(le);\n" + 
			"  }\n" + 
			"}\n"
			);
		IType type = workingCopies[0].getType("Test");
		IMethod method = type.getMethod("foo", new String[] { "QList<QException;>;" } );
		search(method, REFERENCES);
		assertSearchResults(
			"src/b80084/Test.java void b80084.Test.bar() [foo(le)] EXACT_MATCH"
		);
	}

	/**
	 * Bug 80194: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80194"
	 */
	public void testBug80194() throws CoreException, JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80194/Test.java",
			"package b80194;\n" + 
			"interface Map<K, V> {}\n" + 
			"class HashMap<K, V> implements Map {}\n" + 
			"public class Test {\n" + 
			"	void callDoSomething() {\n" + 
			"		final Map<String, Object> map = new HashMap<String, Object>();\n" + 
			"		doSomething(map);\n" + 
			"		doSomething(map, true);\n" + 
			"		doSomething(true);\n" + 
			"	}\n" + 
			"	void doSomething(final Map<String, Object> map) {}\n" + 
			"	void doSomething(final Map<String, Object> map, final boolean flag) {}\n" + 
			"	void doSomething(final boolean flag) {}\n" + 
			"}\n"
		);
		IType type = workingCopies[0].getType("Test");
		IMethod method = type.getMethod("doSomething", new String[] { "QMap<QString;QObject;>;" } );
		search(method, REFERENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH"
		);
	}
	public void testBug80194b() throws CoreException, JavaModelException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = workingCopies[0].getType("Test");
		IMethod method = type.getMethod("doSomething", new String[] { "QMap<QString;QObject;>;", "Z" } );
		search(method, REFERENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map, true)] EXACT_MATCH"
		);
	}
	public void testBug80194string1() throws CoreException, JavaModelException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("doSomething(boolean)", METHOD, ALL_OCCURRENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH\n" + 
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH\n" + 
			"src/b80194/Test.java void b80194.Test.doSomething(boolean) [doSomething] EXACT_MATCH"
		);
	}
	public void testBug80194string2() throws CoreException, JavaModelException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("doSomething(Map<String,Object>)", METHOD, ALL_OCCURRENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH\n" + 
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH\n" + 
			"src/b80194/Test.java void b80194.Test.doSomething(Map<String,Object>) [doSomething] EXACT_MATCH"
		);
	}
	public void testBug80194string3() throws CoreException, JavaModelException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("doSomething(Map<String,Object>,boolean)", METHOD, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map, true)] EXACT_MATCH\n" + 
			"src/b80194/Test.java void b80194.Test.doSomething(Map<String,Object>, boolean) [doSomething] EXACT_MATCH"
		);
	}

	/**
	 * Bug 80223: [search] Declaration search doesn't consider visibility to determine overriding methods
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80223"
	 */
	public void testBug80223() throws CoreException {
		workingCopies = new ICompilationUnit[2];
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80223/a/A.java",
			"package b80223.a;\n" + 
			"public class A {\n" + 
			"    void m() {}\n" + 
			"}",
			owner,
			true);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b80223/b/B.java",
			"package b80223.b;\n" + 
			"public class B extends b80223.a.A {\n" + 
			"    void m() {}\n" + 
			"}",
			owner,
			true);
		// search for method declaration should find only A match
		IType type = workingCopies[0].getType("A");
		IMethod method = type.getMethod("m", new String[0]);
		search(method, DECLARATIONS);
		assertSearchResults(
			"src/b80223/a/A.java void b80223.a.A.m() [m] EXACT_MATCH"
		);
	}

	/**
	 * Bug 80890: [search] Strange search engine behaviour
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80890"
	 */
	public void testBug80890() throws CoreException, JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80890/A.java",
			"package b80890;\n" + 
			"public class A {\n" + 
			"	protected void foo(Exception e) {}\n" + 
			"	protected void foo(String s) {}\n" + 
			"}\n" + 
			"class B1 extends A {\n" + 
			"	public void bar1() {\n" + 
			"		foo(null);\n" + 
			"	}\n" + 
			"}\n" + 
			"class B2 extends A {\n" + 
			"	public void bar2() {\n" + 
			"		foo(null);\n" + 
			"	}\n" + 
			"}\n"
			);
		// search for first and second method should both return 2 inaccurate matches
		IType type = workingCopies[0].getType("A");
		IMethod method = type.getMethods()[0];
		search(method, REFERENCES);
		method = type.getMethods()[1];
		search(method, REFERENCES);
		assertSearchResults(
			"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
			"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH\n" + 
			"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
			"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH"
		);
	}

	/**
	 * Bug 80918: [1.5][search] ClassCastException when searching for references to binary type
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80918"
	 */
	public void testBug80918() throws CoreException {
		IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Exception.class").getType();
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"" // do not expect to find anything, just verify that no CCE happens
		);
	}

	/**
	 * Bug 81084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81084"
	 */
	public void testBug81084a() throws CoreException, JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
			"package b81084a;\n" + 
			"class List<E> {}\n" + 
			"public class Test {\n" + 
			"	class Element{}\n" + 
			"	static class Inner {\n" + 
			"		private final List<Element> fList1;\n" + 
			"		private final List<Test.Element> fList2;\n" + 
			"		public Inner(List<Element> list) {\n" + 
			"			fList1 = list;\n" + 
			"			fList2 = list;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
			);
		IType type = workingCopies[0].getType("Test").getType("Inner");
		IField field1 = type.getField("fList1");
		search(field1, REFERENCES);
		IField field2 = type.getField("fList2");
		search(field2, REFERENCES);
		discard = false; // keep working copies for next test (set before assertion as an error is raised...)
		assertSearchResults(
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" + 
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH"
		);
	}
	public void testBug81084string() throws CoreException, JavaModelException {
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("fList1", FIELD, REFERENCES);
		search("fList2", FIELD, REFERENCES);
		assertSearchResults(
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" + 
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH"
		);
	}
	public void testBug81084b() throws CoreException, JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084b/Test.java",
			"package b81084b;\n" + 
			"class List<E> {}\n" + 
			"public class Test {\n" + 
			"	class Element{}\n" + 
			"	static class Inner {\n" + 
			"		private final List<? extends Element> fListb1;\n" + 
			"		private final List<? extends Test.Element> fListb2;\n" + 
			"		public Inner(List<Element> list) {\n" + 
			"			fListb1 = list;\n" + 
			"			fListb2 = list;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
			);
		// search element patterns
		IType type = workingCopies[0].getType("Test").getType("Inner");
		IField field1 = type.getField("fListb1");
		search(field1, REFERENCES);
		IField field2 = type.getField("fListb2");
		search(field2, REFERENCES);
		assertSearchResults(
			"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb1] EXACT_MATCH\n" + 
			"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb2] EXACT_MATCH"
		);
	}

	/**
	 * Bug 81556: [search] correct results are missing in java search
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81556"
	 */
	public void testBug81556() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b81556.a", "X81556.java");
		IType type = unit.getType("X81556");
		IMethod method = type.getMethod("foo", new String[0]);
		search(method, REFERENCES);
		assertSearchResults(
			"src/b81556/a/A81556.java void b81556.a.A81556.bar(XX81556) [foo()] EXACT_MATCH"
		);
	}

	/**
	 * Test fix for bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
	 */
	public void testBug82088method() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/m/Test.java",
			"package b82088.m;\n" +
			"/**\n" + 
			" * @see #setA(A)\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	A a;\n" + 
			"	public void setA(A a) {\n" + 
			"		this.a = a;\n" + 
			"	}\n" + 
			"}\n" + 
			"class A {}\n"
			);
		IType type = workingCopies[0].getType("A");
		search(type, REFERENCES);
		assertSearchResults(
			"src/b82088/m/Test.java b82088.m.Test [A] EXACT_MATCH\n" + 
			"src/b82088/m/Test.java b82088.m.Test.a [A] EXACT_MATCH\n" + 
			"src/b82088/m/Test.java void b82088.m.Test.setA(A) [A] EXACT_MATCH"
		);
	}
	public void testBug82088constructor() throws CoreException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/c/Test.java",
			"package b82088.c;\n" +
			"/**\n" + 
			" * @see #Test(A)\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	A a;\n" + 
			"	Test(A a) {\n" + 
			"		this.a = a;\n" + 
			"	}\n" + 
			"}\n" + 
			"class A {}\n"
			);
		IType type = workingCopies[0].getType("A");
		search(type, REFERENCES);
		assertSearchResults(
			"src/b82088/c/Test.java b82088.c.Test [A] EXACT_MATCH\n" + 
			"src/b82088/c/Test.java b82088.c.Test.a [A] EXACT_MATCH\n" + 
			"src/b82088/c/Test.java b82088.c.Test(A) [A] EXACT_MATCH"
		);
	}

	/**
	 * Bug 83304: [search] correct results are missing in java search
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83304"
	 */
	public void testBug83304() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Test.java",
			"package b83304;\n" + 
			"public class Test {\n" + 
			"	void foo() {\n" + 
			"		Class<? extends Throwable> l1= null;\n" + 
			"		Class<Exception> l2= null;\n" + 
			"		\n" + 
			"		Class<String> string_Class;\n" + 
			"	}\n" + 
			"}\n"
			);
		IType type = selectType(workingCopies[0], "Class", 3);
		search(type, REFERENCES, ERASURE_RULE);
		assertSearchResults(
			"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" + 
			"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" + 
			"src/b83304/Test.java void b83304.Test.foo() [Class] EXACT_MATCH\n" + 
			getExternalJCLPathString("1.5") + " java.lang.Class java.lang.Object.getClass() EQUIVALENT_RAW_MATCH"
		);
	}
	public void testBug83304_TypeParameterizedElementPattern() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Types.java",
			"package b83304;\n" + 
			"import g1.t.s.def.Generic;\n" + 
			"public class Types {\n" + 
			"	public Generic gen;\n" + 
			"	public Generic<Object> gen_obj;\n" + 
			"	public Generic<Exception> gen_exc;\n" + 
			"	public Generic<?> gen_wld;\n" + 
			"	public Generic<? extends Throwable> gen_thr;\n" + 
			"	public Generic<? super RuntimeException> gen_run;\n" + 
			"}\n"
			);
		IType type = selectType(workingCopies[0], "Generic", 4);
		search(type, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_thr [Generic] EQUIVALENT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_run [Generic] EQUIVALENT_MATCH\n" + 
			"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"
		);
	}
	public void testBug83304_TypeGenericElementPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g1.t.s.def", "Generic.class").getType();
		search(type, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen [Generic] ERASURE_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_exc [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_wld [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH\n" + 
			"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"
		);
	}
	public void testBug83304_TypeStringPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("Generic<? super Exception>", TYPE, REFERENCES, ERASURE_RULE);
		assertSearchResults(
			"src/b83304/Types.java [Generic] EQUIVALENT_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_obj [Generic] EQUIVALENT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_exc [Generic] EQUIVALENT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH\n" + 
			"src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH\n" + 
			"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"
		);
	}
	public void testBug83304_MethodParameterizedElementPattern() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Methods.java",
			"package b83304;\n" + 
			"import g5.m.def.Single;\n" + 
			"public class Methods {\n" + 
			"	void test() {\n" + 
			"		Single<Exception> gs = new Single<Exception>();\n" + 
			"		Exception exc = new Exception();\n" + 
			"		gs.<Throwable>generic(exc);\n" + 
			"		gs.<Exception>generic(exc);\n" + 
			"		gs.<String>generic(\"\");\n" + 
			"	}\n" + 
			"}\n"
			);
		IMethod method = selectMethod(workingCopies[0], "generic", 2);
		search(method, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
		);
	}
	public void testBug83304_MethodGenericElementPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.m.def", "Single.class").getType();
		IMethod method = type.getMethod("generic", new String[] { "TU;" });
		search(method, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
		);
	}
	public void testBug83304_MethodStringPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("<Exception>generic", METHOD, REFERENCES, ERASURE_RULE);
		assertSearchResults(
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" + 
			"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
		);
	}
	public void testBug83304_ConstructorGenericElementPattern() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Constructors.java",
			"package b83304;\n" + 
			"import g5.c.def.Single;\n" + 
			"public class Constructors {\n" + 
			"	void test() {\n" + 
			"		Exception exc= new Exception();\n" + 
			"		new <Throwable>Single<String>(\"\", exc);\n" + 
			"		new <Exception>Single<String>(\"\", exc);\n" + 
			"		new <String>Single<String>(\"\", \"\");\n" + 
			"	}\n" + 
			"}\n"
			);
		IMethod method = selectMethod(workingCopies[0], "Single", 3);
		search(method, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] EXACT_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH"
		);
	}
	public void testBug83304_ConstructorParameterizedElementPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.c.def", "Single.class").getType();
		IMethod method = type.getMethod("Single", new String[] { "TT;", "TU;" });
		search(method, REFERENCES, ERASURE_RULE);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] ERASURE_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH"
		);
	}
	public void testBug83304_ConstructorStringPattern() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 1, workingCopies.length);
		search("<Exception>Single", CONSTRUCTOR, REFERENCES, ERASURE_RULE);
		assertSearchResults(
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] EXACT_MATCH\n" + 
			"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH\n" + 
			"lib/JavaSearch15.jar g5.m.def.Single<T> g5.m.def.Single.returnParamType() ERASURE_MATCH"
		);
	}

	/**
	 * Bug 84100: [1.5][search] Search for varargs method not finding match
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84100"
	 */
	public void testBug84100() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[2];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84100/X.java",
			"package b84100;\n" + 
			"public class X {\n" + 
			"	void foo() {}\n" + 
			"	void foo(String s) {}\n" + 
			"	void foo(String... xs) {}\n" + 
			"	void foo(int x, String... xs) {}\n" + 
			"	void foo(String s, int x, String... xs) {}\n" + 
			"}\n"
			);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84100/Z.java",
			"package b84100;\n" + 
			"public class Z {\n" + 
			"	X x;\n" + 
			"	void foo() {\n" + 
			"		x.foo();\n" + 
			"		x.foo(\"\");\n" + 
			"		x.foo(\"\", \"\");\n" + 
			"	 	x.foo(\"\", \"\", null);\n" + 
			"		x.foo(3, \"\", \"\");\n" + 
			"		x.foo(\"\", 3, \"\", \"\");\n" + 
			"	}\n" + 
			"}\n"
			);
		IMethod method = selectMethod(workingCopies[0], "foo", 1);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84100/Z.java void b84100.Z.foo() [foo()] EXACT_MATCH"
		);
	}
	public void testBug84100b() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "foo", 2);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84100/Z.java void b84100.Z.foo() [foo(\"\")] EXACT_MATCH"
		);
	}
	public void testBug84100c() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "foo", 3);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\")] EXACT_MATCH\n" + 
			"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\", null)] EXACT_MATCH"
		);
	}
	public void testBug84100d() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "foo", 4);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84100/Z.java void b84100.Z.foo() [foo(3, \"\", \"\")] EXACT_MATCH"
		);
	}
	public void testBug84100e() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "foo", 5);
		search(method, REFERENCES);
		assertSearchResults(
			"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", 3, \"\", \"\")] EXACT_MATCH"
		);
	}

	/**
	 * Bug 84724: [1.5][search] Search for varargs method not finding match
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84724"
	 */
	public void testBug84724() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[2];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84724/X.java",
			"package b84724;\n" + 
			"public class X {\n" + 
			"	X(String s) {}\n" + 
			"	X(String... v) {}\n" + 
			"	X(int x, String... v) {}\n" + 
			"	X(String s, int x, String... v) {}\n" + 
			"}\n"
			);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84724/Z.java",
			"package b84724;\n" + 
			"public class Z {\n" + 
			"	void foo() {\n" + 
			"		new X();\n" + 
			"		new X(\"\");\n" + 
			"		new X(\"\", \"\");\n" + 
			"		new X(\"\", \"\", null);\n" + 
			"		new X(3, \"\", \"\");\n" + 
			"		new X(\"\", 3, \"\", \"\");\n" + 
			"	}\n" + 
			"}\n"
			);
		IMethod method = selectMethod(workingCopies[0], "X", 2);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84724/Z.java void b84724.Z.foo() [new X(\"\")] EXACT_MATCH"
		);
	}
	public void testBug84724b() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "X", 3);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84724/Z.java void b84724.Z.foo() [new X()] EXACT_MATCH\n" + 
			"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\")] EXACT_MATCH\n" + 
			"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\", null)] EXACT_MATCH"
		);
	}
	public void testBug84724c() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "X", 4);
		search(method, REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84724/Z.java void b84724.Z.foo() [new X(3, \"\", \"\")] EXACT_MATCH"
		);
	}
	public void testBug84724d() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 2, workingCopies.length);
		IMethod method = selectMethod(workingCopies[0], "X", 5);
		search(method, REFERENCES);
		assertSearchResults(
			"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", 3, \"\", \"\")] EXACT_MATCH"
		);
	}

	/**
	 * Bug 84727: [1.5][search] String pattern search does not work with multiply nested types
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84727"
	 */
	public void testBug84727() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[3];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84727/A.java",
			"package b84727;\n" + 
			"public interface A {\n" + 
			"	Set<Set<Exception>> getXYZ(List<Set<Exception>> arg);\n" + 
			"	void getXYZ(String s);\n" + 
			"}\n"
			);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84727/X.java",
			"package b84727;\n" + 
			"public class X {\n" + 
			"	A a;\n" + 
			"	void foo() {\n" + 
			"		a.getXYZ(new ArrayList());\n" + 
			"		a.getXYZ(\"\");\n" + 
			"	}\n" + 
			"}\n"
			);
		workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b84727/List.java",
			"package b84727;\n" + 
			"public interface List<E> {}\n" + 
			"interface Set<E> {}\n" + 
			"class ArrayList<E> implements List<E> {}"
			);
		IMethod[] methods = workingCopies[0].getType("A").getMethods();
		assertEquals("Invalid number of methods", 2, methods.length);
		search(methods[0], REFERENCES);
		discard = false; // use working copy for next test
		assertSearchResults(
			"src/b84727/X.java void b84727.X.foo() [getXYZ(new ArrayList())] EXACT_MATCH"
		);
	}
	public void testBug84727b() throws CoreException {
		resultCollector.showRule = true;
		assertNotNull("Problem in tests processing", workingCopies);
		assertEquals("Problem in tests processing", 3, workingCopies.length);
		IMethod[] methods = workingCopies[0].getType("A").getMethods();
		assertEquals("Invalid number of methods", 2, methods.length);
		search(methods[1], REFERENCES);
		assertSearchResults(
			"src/b84727/X.java void b84727.X.foo() [getXYZ(\"\")] EXACT_MATCH"
		);
	}

	/**
	 * Bug 85810: [1.5][search] Missed type parameter reference in implements clause
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=85810"
	 */
	public void testBug85810() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b85810/Test.java",
			"package b85810;\n" + 
			"public class Test<E> implements In<Test<E>> {\n" + 
			"	E e;\n" + 
			"}\n" +
			"interface In<T> {}\n"
			);
		ITypeParameter param = selectTypeParameter(workingCopies[0], "E");
		search(param, REFERENCES);
		assertSearchResults(
			"src/b85810/Test.java b85810.Test [E] EXACT_MATCH\n" + 
			"src/b85810/Test.java b85810.Test.e [E] EXACT_MATCH"
		);
	}

	/**
	 * Bug 86596: [search] Search for type finds segments in import
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86596"
	 */
	public void testBug86596() throws CoreException {
		resultCollector.showRule = true;
		workingCopies = new ICompilationUnit[2];
		workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86596/A.java",
			"package b86596.com.ibm.link;\n" + 
			"public interface A {}\n"
			);
		workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86596/X.java",
			"package b86596;\n" + 
			"public class X {\n" + 
			"	A a;\n" + 
			"}\n"
			);
		search("link", TYPE, REFERENCES);
		assertSearchResults("");
	}
	public void testBug86596b() throws CoreException {
		resultCollector.showRule = true;
		search("util", TYPE, REFERENCES);
		assertSearchResults("");
	}
}
