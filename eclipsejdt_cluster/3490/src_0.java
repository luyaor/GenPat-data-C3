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
package org.eclipse.jdt.core.tests.builder;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Base class for Java image builder tests
 */
public class Tests extends TestCase {
	protected static boolean DEBUG = false;
	protected static TestingEnvironment env = null;
	protected EfficiencyCompilerRequestor debugRequestor = null;
	
	private static String[] EXCLUDED_TESTS = {
	};

	public Tests(String name) {
		super(name);
	}
	
	/** Execute the given class. Expecting output and error must be specified.
	 */
	protected void executeClass(IPath projectPath, String className, String expectingOutput, String expectedError){
		TestVerifier verifier = new TestVerifier(false);
		Vector classpath = new Vector(5);
			
		IPath workspacePath = env.getWorkspaceRootPath();
			
		classpath.addElement(workspacePath.append(env.getOutputLocation(projectPath)).toOSString());
		IClasspathEntry[] cp = env.getClasspath(projectPath);
		for (int i = 0; i < cp.length; i++) {
			IPath c = cp[i].getPath();
			String ext = c.getFileExtension();
			if(ext != null && (ext.equals("zip") || ext.equals("jar"))){ //$NON-NLS-1$ //$NON-NLS-2$
				if(c.getDevice() == null) {
					classpath.addElement(workspacePath.append(c).toOSString());
				} else {
					classpath.addElement(c.toOSString());
				}
			}
		}
		
		verifier.execute(className, (String[])classpath.toArray(new String[0]));
		
		if(DEBUG){
			System.out.println("ERRORS\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(verifier.getExecutionError()));
			
			System.out.println("OUTPUT\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(verifier.getExecutionOutput()));
		}
		String actualError = verifier.getExecutionError();
		
		// workaround pb on 1.3.1 VM (line delimitor is not the platform line delimitor)
		char[] error = actualError.toCharArray();
		actualError = new String(CharOperation.replace(error, System.getProperty("line.separator").toCharArray(), new char[] {'\n'})); //$NON-NLS-1$
		
		if (actualError.indexOf(expectedError) == -1){
			System.out.println("ERRORS\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualError));
		}
		assertTrue("unexpected error : " + actualError + " expected : " + expectedError, actualError.indexOf(expectedError) != -1); //$NON-NLS-1$ //$NON-NLS-2$

		String actualOutput = verifier.getExecutionOutput();
		if (actualOutput.indexOf(expectingOutput) == -1){
			System.out.println("OUTPUT\n"); //$NON-NLS-1$
			System.out.println(Util.displayString(actualOutput));
		}
		assertTrue("unexpected output", actualOutput.indexOf(expectingOutput) != -1); //$NON-NLS-1$
		
	}
	
	/** Verifies that given element is not present.
	 */
	protected void expectingPresenceOf(IPath path) {
		expectingPresenceOf(new IPath[]{path});
	}
	
	/** Verifies that given elements are not present.
	 */
	protected void expectingPresenceOf(IPath[] paths) {
		IPath wRoot = env.getWorkspaceRootPath();
		
		for (int i = 0; i < paths.length; i++){
			assertTrue(paths[i] +" is not present", wRoot.append(paths[i]).toFile().exists()); //$NON-NLS-1$
		}
	}
	
	/** Verifies that given element is not present.
	 */
	protected void expectingNoPresenceOf(IPath path) {
		expectingNoPresenceOf(new IPath[]{path});
	}
	
	/** Verifies that given elements are not present.
	 */
	protected void expectingNoPresenceOf(IPath[] paths) {
		IPath wRoot = env.getWorkspaceRootPath();
		
		for (int i = 0; i < paths.length; i++){
			assertTrue(paths[i] +" is present", !wRoot.append(paths[i]).toFile().exists()); //$NON-NLS-1$
		}
	}
	
	/** Verifies that given classes have been compiled.
	 */
	protected void expectingCompiledClasses(String[] expected){
		String[] actual = debugRequestor.getCompiledClasses();
		org.eclipse.jdt.internal.core.Util.sort(actual);
		org.eclipse.jdt.internal.core.Util.sort(expected);	
		expectingCompiling(actual, expected, "unexpected recompiled units"); //$NON-NLS-1$
	}
	
	/** Verifies that given classes have been compiled in the specified order.
	 */
	protected void expectingCompilingOrder(String[] expected){
		expectingCompiling(debugRequestor.getCompiledClasses(), expected, "unexpected compiling order"); //$NON-NLS-1$
	}
	
	private void expectingCompiling(String[] actual, String[] expected, String message){
		if(DEBUG) {
			for (int i = 0; i < actual.length; i++) {
				System.out.println(actual[i]);
			}
		}	
		
		StringBuffer actualBuffer = new StringBuffer("{"); //$NON-NLS-1$
		for (int i = 0; i < actual.length; i++){
			if (i > 0) actualBuffer.append(","); //$NON-NLS-1$
			actualBuffer.append(actual[i]);
		}
		actualBuffer.append('}');
		StringBuffer expectedBuffer = new StringBuffer("{"); //$NON-NLS-1$
		for (int i = 0; i < expected.length; i++){
			if (i > 0) expectedBuffer.append(","); //$NON-NLS-1$
			expectedBuffer.append(expected[i]);
		}
		expectedBuffer.append('}');		
		assertEquals(message, expectedBuffer.toString(), actualBuffer.toString());
	}
	
	/** Verifies that the workspace has no problems.
	 */
	protected void expectingNoProblems() {				
		expectingNoProblemsFor(env.getWorkspaceRootPath());
	}
	
	/** Verifies that the given element has no problems.
	 */
	protected void expectingNoProblemsFor(IPath root) {
		expectingNoProblemsFor(new IPath[]{root});
	}
	
	/** Verifies that the given elements have no problems.
	 */
	protected void expectingNoProblemsFor(IPath[] roots) {
		if(DEBUG)
			printProblemsFor(roots);
		
		for (int i = 0; i < roots.length; i++) {
			IPath root = roots[i];
			Problem[] problems = env.getProblemsFor(root);
			
			if(problems.length != 0) {
				assertTrue("unexpected problem(s) : " + problems[0], false); //$NON-NLS-1$
			}
		}
	}
	
	/** Verifies that the given element has problems and
	 * only the given element.
	 */
	protected void expectingOnlyProblemsFor(IPath expected) {
		expectingOnlyProblemsFor(new IPath[]{expected});
	}
	
	/** Verifies that the given elements have problems and
	 * only the given elements.
	 */
	protected void expectingOnlyProblemsFor(IPath[] expected) {
		if(DEBUG)
			printProblems();
		
		Problem[] rootProblems = env.getProblems();
		
		Hashtable actual = new Hashtable(rootProblems.length*2+1);
		for (int i = 0; i < rootProblems.length; i++) {
			IPath culprit = rootProblems[i].getResourcePath();
			actual.put(culprit, culprit);
		}
		
		for (int i = 0; i < expected.length; i++) {
			if (!actual.containsKey(expected[i])) {
				assertTrue("missing expected problem with " + expected[i].toString(), false); //$NON-NLS-1$
			}
		}

		if (actual.size() > expected.length) {
			for (Enumeration e = actual.elements(); e.hasMoreElements();) {
				IPath path = (IPath)e.nextElement();
				boolean found = false;
				for (int i = 0; i < expected.length; ++i) {
					if (path.equals(expected[i])) {
						found = true;
						break;
					}
				}
				if (!found) {
					assertTrue("unexpected problem(s) with " + path.toString(), false); //$NON-NLS-1$
				}
			}
		}
	}
	
	/** Verifies that the given element has a specific problem and
	 * only the given problem.
	 */
	protected void expectingOnlySpecificProblemFor(IPath root, Problem problem){
		expectingOnlySpecificProblemsFor(root, new Problem[]{problem});
	}
	
	/** Verifies that the given element has specifics problems and
	 * only the given problems.
	 */
	protected void expectingOnlySpecificProblemsFor(IPath root, Problem[] expectedProblems){

		StringBuffer expectation = new StringBuffer(20);
		for (int k = 0; k < expectedProblems.length; k++){
			expectation.append("\tnew Problem(\""); //$NON-NLS-1$
			expectation.append(expectedProblems[k].getLocation());
			expectation.append("\", \""); //$NON-NLS-1$
			expectation.append(expectedProblems[k].getMessage());
			expectation.append("\", new Path(\""); //$NON-NLS-1$
			expectation.append(expectedProblems[k].getResourcePath());
			expectation.append("\"))\n");				 //$NON-NLS-1$
		}
		String expectationString = expectation.toString();
		
		StringBuffer actual = new StringBuffer(20);
		Problem[] rootProblems = env.getProblemsFor(root);
		for (int k = 0; k < rootProblems.length; k++){
			actual.append("\tnew Problem(\""); //$NON-NLS-1$
			actual.append(rootProblems[k].getLocation());
			actual.append("\", \""); //$NON-NLS-1$
			actual.append(rootProblems[k].getMessage());
			actual.append("\", new Path(\""); //$NON-NLS-1$
			actual.append(rootProblems[k].getResourcePath());
			actual.append("\"))\n");				 //$NON-NLS-1$
		}
		String actualString = actual.toString();
		
		if (!actualString.equals(expectationString)) {
			System.out.println(actualString);
		}		
		assertEquals("unexpected problems", expectationString, actualString); //$NON-NLS-1$
	}
	
	/** Verifies that the given element has problems.
	 */
	protected void expectingProblemsFor(IPath expected) {
		expectingProblemsFor(new IPath[]{expected});
	}
	
	/** Verifies that the given elements have problems.
	 */
	protected void expectingProblemsFor(IPath[] expected) {
		if(DEBUG)
			printProblemsFor(expected);
		
		for (int i = 0; i < expected.length; i++) {
			IPath path = expected[i];
			
			/* get the leaf problems for this type */
			Problem[] problems = env.getProblemsFor(path);
			assertTrue("missing expected problem with " + expected[i].toString(), problems.length > 0); //$NON-NLS-1$
		}
	}

	/** Verifies that the given element has a specific problem.
	 */
	protected void expectingSpecificProblemFor(IPath root, Problem problem){
		expectingSpecificProblemsFor(root, new Problem[]{problem});
	}
	
	/** Verifies that the given element has specific problems.
	 */
	protected void expectingSpecificProblemsFor(IPath root, Problem[] problems){
		if(DEBUG)
			printProblemsFor(root);
		
		Problem[] rootProblems = env.getProblemsFor(root);
				
		next : for (int i = 0; i < problems.length; i++) {
			Problem problem = problems[i];
			for (int j = 0; j < rootProblems.length; j++) {
				Problem rootProblem = rootProblems[j];
				if(rootProblem != null){
					if(problem.equals(rootProblem)){
						rootProblems[j] = null;
						continue next;
					}
				}
			}
			for (int j = 0; j < rootProblems.length; j++) {
				Problem pb = rootProblems[j];
				System.out.println("got pb:		new Problem(\""+pb.getLocation()+"\", \""+pb.getMessage()+"\", \""+pb.getResourcePath()+"\")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			assertTrue("missing expected problem : "+ problem, false); //$NON-NLS-1$
		}
	}
	
	/** Batch builds the workspace.
	 */
	protected void fullBuild(){
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.fullBuild();
		debugRequestor.deactivate();
	}
	
	/** Batch builds the given project.
	 */
	protected void fullBuild(IPath projectPath){
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.fullBuild(projectPath);
		debugRequestor.deactivate();
	}
	
	/** Incrementally builds the given project.
	 */
	protected void incrementalBuild(IPath projectPath){
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.incrementalBuild(projectPath);
		debugRequestor.deactivate();
	}
	
	/** Incrementally builds the workspace.
	 */
	protected void incrementalBuild(){
		debugRequestor.clearResult();
		debugRequestor.activate();
		env.incrementalBuild();
		debugRequestor.deactivate();
	}
	
	protected void printProblems(){
		printProblemsFor(env.getWorkspaceRootPath());
	}
	
	protected void printProblemsFor(IPath root){
		printProblemsFor(new IPath[]{root});
	}
	
	protected void printProblemsFor(IPath[] roots){
		for (int i = 0; i < roots.length; i++) {
			IPath path = roots[i];
			
			/* get the leaf problems for this type */
			Problem[] problems = env.getProblemsFor(path);
			for (int j = 0; j < problems.length; j++) {
				System.out.println(problems[j].toString());
			}
		}
	}
	
	/** Sets up this test.
	 */
	protected void setUp() {
		debugRequestor = new EfficiencyCompilerRequestor();
		Compiler.DebugRequestor = debugRequestor;
		if(env == null) {
			env = new TestingEnvironment();
			env.openEmptyWorkspace();
		}
		env.resetWorkspace();
		
	}
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		env.resetWorkspace();
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		
		/* tests */
		suite.addTest(AbstractMethodTests.suite());
		suite.addTest(BasicBuildTests.suite());
		suite.addTest(ClasspathTests.suite());
		suite.addTest(CompilationUnitLocationTests.suite());
		suite.addTest(CopyResourceTests.suite());
		suite.addTest(ErrorsTests.suite());
		suite.addTest(EfficiencyTests.suite());
		suite.addTest(ExecutionTests.suite());
		suite.addTest(IncrementalTests.suite());
		suite.addTest(JCLTests.suite());
		suite.addTest(MultiProjectTests.suite());
		suite.addTest(OutputFolderTests.suite());
		suite.addTest(PackageTests.suite());
		
		return suite;
	}
}