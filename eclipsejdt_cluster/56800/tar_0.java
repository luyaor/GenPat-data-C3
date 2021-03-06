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
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.util.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.batch.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.*;
import java.util.*;
import junit.framework.*;

public abstract class AbstractRegressionTest extends StopableTestCase {
	public static String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "regression";


	protected INameEnvironment javaClassLib;
	protected String[] classpaths;
	protected TestVerifier verifier;
	protected boolean createdVerifier;
public AbstractRegressionTest(String name) {
	super(name);
}
protected CompilationUnit[] compilationUnits(String[] testFiles) {
	int length = testFiles.length / 2;
	CompilationUnit[] result = new CompilationUnit[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		result[i] = new CompilationUnit(testFiles[index + 1].toCharArray(), testFiles[index], null);
		index += 2;
	}
	return result;
}
protected INameEnvironment[] getClassLibs() {
	String encoding = (String) getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
	if ("".equals(encoding)) encoding = null;
	
	INameEnvironment[] classLibs = new INameEnvironment[1];
	classLibs[0] =
		new FileSystem(
			classpaths, 
			new String[] {}, // ignore initial file names
			encoding // default encoding			
		);
	return classLibs;
}
protected Map getCompilerOptions() {
		Map defaultOptions = new CompilerOptions().getMap();
		defaultOptions.put(
			CompilerOptions.OPTION_LocalVariableAttribute,
			CompilerOptions.GENERATE);
		defaultOptions.put(
			CompilerOptions.OPTION_ReportUnusedPrivateMember, 
			CompilerOptions.WARNING);
		defaultOptions.put(
			CompilerOptions.OPTION_ReportLocalVariableHiding,
			CompilerOptions.WARNING);
		defaultOptions.put(
			CompilerOptions.OPTION_ReportFieldHiding,
			CompilerOptions.WARNING);
		defaultOptions.put(
			CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment,
			CompilerOptions.WARNING);
		defaultOptions.put(
			CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
			CompilerOptions.WARNING);
		defaultOptions.put(
			CompilerOptions.OPTION_PreserveUnusedLocal, 
			CompilerOptions.PRESERVE);
		return defaultOptions;
}
protected String[] getDefaultClassPaths() {
	return Util.concatWithClassLibs(OUTPUT_DIR, false);
}
protected IErrorHandlingPolicy getErrorHandlingPolicy() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors() {
			return true;
		}
	};
}
/*
 * Will consider first the source units passed as arguments, then
 * investigate the classpath: jdklib + output dir 
 */
protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
	this.classpaths = 
		classPaths == null ? 
			this.getDefaultClassPaths() :
			classPaths;
	return 
		new InMemoryNameEnvironment(
			testFiles, 
			this.getClassLibs()
		);
}
protected IProblemFactory getProblemFactory() {
	return new DefaultProblemFactory(Locale.getDefault());
}
protected void runConformTest(String[] testFiles) {
	this.runConformTest(testFiles, null, null, true, null);
}

protected void runConformTest(String[] testFiles, String[] vmArguments) {
	this.runConformTest(testFiles, null, null, true, vmArguments);
}
protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] vmArguments) {
	this.runConformTest(testFiles, expectedSuccessOutputString, null, true, vmArguments);
}

protected void runConformTest(String[] testFiles, String expectedSuccessOutputString) {
	this.runConformTest(testFiles, expectedSuccessOutputString, null, true, null);
}
protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments) {
	this.runConformTest( testFiles,  expectedSuccessOutputString, classLib,  shouldFlushOutputDirectory, vmArguments, null);
}

protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments, Map customOptions) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
	IProblemFactory problemFactory = getProblemFactory();
	Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, false);
	
	Map options = getCompilerOptions();
	if (customOptions != null){
		options.putAll(customOptions);
	}
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			options,
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');

		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}		
		boolean passed = this.verifier.verifyClassFiles(sourceFile, className, expectedSuccessOutputString, this.classpaths, null, vmArguments);
		assertTrue(
			this.verifier.failureReason, // computed by verifyClassFiles(...) action
			passed
		);
		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}		
	} else {
		assertTrue("Unexpected problems: " + requestor.problemLog, false);
	}
}

protected void runConformTestThrowingError(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
	IProblemFactory problemFactory = getProblemFactory();
	Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, false);
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			getCompilerOptions(),
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
		
		boolean passed = this.verifier.verifyClassFilesThrowingError(sourceFile, className, expectedSuccessOutputString, this.classpaths, null, vmArguments);
		assertTrue(
			this.verifier.failureReason, // computed by verifyClassFiles(...) action
			passed
		);
	} else {
		assertTrue("Unexpected problems: " + requestor.problemLog, false);
	}
}
/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
	this.runNegativeTest(testFiles, expectedProblemLog, null, true);
}

/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog, String[] classLib, boolean shouldFlushOutputDirectory) {
	this.runNegativeTest(testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory, null);
}
/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog, String[] classLib, boolean shouldFlushOutputDirectory, Map customOptions){
	this.runNegativeTest(testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory, customOptions, false);
}
/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog, String[] classLib, boolean shouldFlushOutputDirectory, Map customOptions, boolean generateOutput) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));

	IProblemFactory problemFactory = getProblemFactory();
	Requestor requestor = new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, generateOutput);
	Map options = getCompilerOptions();
	if (customOptions != null){
		options.putAll(customOptions);
	}
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			options,
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	String computedProblemLog = requestor.problemLog.toString();
	if (!expectedProblemLog.equals(computedProblemLog)){
		System.out.println(this.getClass().getName() + '#' + this.getName());
	 	System.out.println(Util.displayString(computedProblemLog, 2));
	 	for (int i = 0; i < testFiles.length; i+=2) {
	 		System.out.print(testFiles[i]);
	 		System.out.println(" ["); //$NON-NLS-1$
	 		System.out.println(testFiles[i+1]);
	 		System.out.println("]"); //$NON-NLS-1$
	 	}
	}
	assertEquals(
		"Invalid problem log ",
		expectedProblemLog,
		computedProblemLog);
}
protected void setUp() throws Exception {
	if (this.verifier == null) {
		this.verifier = new TestVerifier(true);
		this.createdVerifier = true;
	}
}
public static Test setupSuite(Class clazz) {
	return new RegressionTestSetup(suite(clazz));
}
public void stop() {
	this.verifier.shutDown();
}
public static Test suite(Class evaluationTestClass) {
	TestSuite suite = new TestSuite(evaluationTestClass);
	return suite;
}
protected void tearDown() throws Exception {
	if (this.createdVerifier) {
		this.stop();
	}
}
}
