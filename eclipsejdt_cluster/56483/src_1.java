/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class ReconcilerTests extends ModifyingResourceTests {
	
	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;
	
	class ProblemRequestor implements IProblemRequestor {
		StringBuffer problems;
		int problemCount;
		public ProblemRequestor() {
			this.initialize();
		}
		public void acceptProblem(IProblem problem) {
				problems.append(++problemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
				problems.append(" in " + new String(problem.getOriginatingFileName()));
				try {
					problems.append(((DefaultProblem)problem).errorReportSource((org.eclipse.jdt.internal.compiler.env.ICompilationUnit)workingCopy));
					problems.append("\n");
					problems.append(problem.getMessage());
					problems.append("\n");
				} catch (Exception e) {
				}
		}
		public void beginReporting() {
			this.problems.append("----------\n");
		}
		public void endReporting() {
			problems.append("----------\n");
		}
		public boolean isActive() {
			return true;
		}
		public void initialize() {
			this.problems = new StringBuffer();
			this.problemCount = 0;
		}
	}
	
/**
 */
public ReconcilerTests(String name) {
	super(name);
}
protected void assertProblems(String message, String expected) {
	String actual = this.problemRequestor.problems.toString();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 2));
	}
	assertEquals(
		message,
		expected,
		actual);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	ICompilationUnit cu = getCompilationUnit("Reconciler", "src", "p1", "X.java");
	this.problemRequestor =  new ProblemRequestor();
	this.workingCopy = (ICompilationUnit)cu.getWorkingCopy(null, null, this.problemRequestor);
	this.problemRequestor.initialize();
	this.startDeltas();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.createJavaProject("Reconciler", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
	this.createFolder("/Reconciler/src/p1");
	this.createFolder("/Reconciler/src/p2");
	this.createFile(		"/Reconciler/src/p1/X.java", 
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
}
public static Test suite() {
	TestSuite suite = new Suite(ReconcilerTests.class.getName());
	suite.addTest(new ReconcilerTests("testAddDuplicateMember"));
	suite.addTest(new ReconcilerTests("testAddFieldAndConstructor"));
	suite.addTest(new ReconcilerTests("testAddImports"));
	suite.addTest(new ReconcilerTests("testAddMethod1"));
	suite.addTest(new ReconcilerTests("testAddPartialMethod1"));
	suite.addTest(new ReconcilerTests("testAddPartialMethod1and2"));
	suite.addTest(new ReconcilerTests("testChangeMethodVisibility"));
	suite.addTest(new ReconcilerTests("testDeleteMethod1"));
	suite.addTest(new ReconcilerTests("testDeleteTwoMethods"));
	suite.addTest(new ReconcilerTests("testGrowImports"));
	suite.addTest(new ReconcilerTests("testMethodWithError"));
	suite.addTest(new ReconcilerTests("testMethodWithError2"));
	suite.addTest(new ReconcilerTests("testMethodWithError3"));
	suite.addTest(new ReconcilerTests("testMethodWithError4"));
	suite.addTest(new ReconcilerTests("testNoChanges1"));
	suite.addTest(new ReconcilerTests("testNoChanges2"));
	suite.addTest(new ReconcilerTests("testRenameMethod1"));
	suite.addTest(new ReconcilerTests("testRenameWithSyntaxError"));
	return suite;
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	if (this.workingCopy != null) {
		this.workingCopy.destroy();
	}
	this.stopDeltas();
	super.tearDown();
}
public void tearDownSuite() throws Exception {
	this.deleteProject("Reconciler");
	super.tearDownSuite();
}
/**
 * Ensures that the reconciler handles duplicate members correctly.
 */
public void testAddDuplicateMember() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddFieldAndConstructor() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  int i;\n" +
		"  X(int i) {\n" +
		"    this.i = i;\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	i[+]: {}\n" +
		"	X[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddImports() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"import java.lang.reflect.*;\n" +
		"import java.util.Vector;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"[import container][*]: {CHILDREN | FINE GRAINED}\n" +
		"	import java.lang.reflect.*[+]: {}\n" +
		"	import java.util.Vector[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a method.
 */
public void testAddMethod1() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testAddPartialMethod1() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	some[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.  Ensures that when a
 * second part is added to the new method no structural changes are recognized.
 */
public void testAddPartialMethod1and2() throws JavaModelException {
	// Add partial method before foo
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	
	// Add { on partial method
	this.clearDeltas();
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some() {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method visibility change.
 */
public void testChangeMethodVisibility() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  private void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo[*]: {MODIFIERS CHANGED}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method being deleted.
 */
public void testDeleteMethod1() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of two methods being deleted.
 */
public void testDeleteTwoMethods() throws JavaModelException {
	// create 2 methods
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	
	// delete the 2 methods
	this.clearDeltas();
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar[-]: {}\n" +
		"	foo[-]: {}"
	);
}
/**
 * Start with no imports, add an import, and then append to the import name.
 */
public void testGrowImports() throws JavaModelException {
	// no imports
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile();
	
	// add an import
	this.clearDeltas();
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"[import container][+]: {}"
	);
		
	// append to import name
	this.clearDeltas();
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta", 
		"[import container][*]: {CHILDREN | FINE GRAINED}\n" +
		"	import p2[+]: {}\n" +
		"	import p[-]: {}"
	);
}
/**
 * Introduces a syntax error in the modifiers of a method.
 */
public void testMethodWithError() throws JavaModelException, CoreException {
	// Introduce syntax error
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta after syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", \"boolean\", \"void\", \"byte\", \"short\", \"int\", \"long\", \"char\", \"float\", \"double\", \"Identifier\", \"interface\", \"class\" expected\n" + 
		"----------\n"
	);

	// Fix the syntax error
	this.clearDeltas();
	this.problemRequestor.initialize();
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta after fixing syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"----------\n"
	);
}
/**
 * Test reconcile force flag
 */
public void testMethodWithError2() throws JavaModelException, CoreException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	this.problemRequestor.initialize();

	// use force flag to refresh problems			
	this.workingCopy.reconcile(true, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", \"boolean\", \"void\", \"byte\", \"short\", \"int\", \"long\", \"char\", \"float\", \"double\", \"Identifier\", \"interface\", \"class\" expected\n" + 
		"----------\n"
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError3() throws JavaModelException, CoreException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile();
	this.problemRequestor.initialize();

	// use force flag to refresh problems			
	this.workingCopy.reconcile(false, null);
	assertProblems(
		"Unexpected problems",
		""
	);
}
/**
 * Test reconcile force flag + cancel
 */
public void testMethodWithError4() throws JavaModelException, CoreException {

	final IProgressMonitor myMonitor = new IProgressMonitor() {
		boolean isCanceled = false;
		public void beginTask(String name, int totalWork) {}
		public void done() {}
		public void internalWorked(double work) {}
		public boolean isCanceled() {
			return this.isCanceled;
		}
		public void setCanceled(boolean value) {
			this.isCanceled = value;
		}
		public void setTaskName(String name) {}
		public void subTask(String name) {}
		public void worked(int work) {}
	};

	class CancelingProblemRequestor extends ProblemRequestor {
		boolean isCanceling = false;
		public void acceptProblem(IProblem problem) {
			if (isCanceling) myMonitor.setCanceled(true); // auto-cancel on first problem
			super.acceptProblem(problem);
		}		
	};
	CancelingProblemRequestor myPbRequestor = new CancelingProblemRequestor();
	
	this.workingCopy.destroy();
	ICompilationUnit cu = getCompilationUnit("Reconciler", "src", "p1", "X.java");
	 this.problemRequestor = myPbRequestor;
	this.workingCopy = (ICompilationUnit) cu.getWorkingCopy(null, null,this.problemRequestor);
													
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"public class X {\n" +
		"	Zork f;	\n"+
		"	void foo(Zork z){\n"+
		"	}\n"+
		"}	\n");

	this.workingCopy.reconcile();
	this.problemRequestor.initialize();

	// use force flag to refresh problems			
	myPbRequestor.isCanceling = true;
	this.workingCopy.reconcile(true, myMonitor);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Zork f;	\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved (or is not a valid type) for the field X.f\n" + 
		"----------\n"
	);
}

/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testNoChanges1() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(this.workingCopy.getSource());
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta",
		""
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with has the same structure as the current contents.
 */
public void testNoChanges2() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta",
		""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a renaming a method; the original method deleted and the new method added structurally.
 */
public void testRenameMethod1() throws JavaModelException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar[+]: {}\n" + 
		"	foo[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testRenameWithSyntaxError() throws JavaModelException, CoreException {
	this.workingCopy.getBuffer().setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar( {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile();
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar[+]: {}\n" + 
		"	foo[-]: {}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	public void bar( {\n" + 
		"	                 ^\n" + 
		"Syntax error on token \"{\", \"float\", \"double\", \"byte\", \"short\", \"int\", \"long\", \"char\", \"boolean\", \"void\", \"Identifier\" expected\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	public void bar( {\n" + 
		"	               ^\n" + 
		"Unmatched bracket\n" + 
		"----------\n"
	);
}
}
