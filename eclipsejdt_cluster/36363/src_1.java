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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractComparisonTest extends AbstractRegressionTest {

	class Logger extends Thread {
		StringBuffer buffer;
		InputStream inputStream;
		String type;
		Logger(InputStream inputStream, String type) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = new StringBuffer();
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					this.buffer./*append(this.type).append("->").*/append(line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// to enable set VM args to -Dcompliance=1.5 -Drun.javac=enabled
	public static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	public static boolean runJavac = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	public IPath dirPath;

	public AbstractComparisonTest(String name) {
		super(name);
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_ReportFinalParameterBound, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
		return options;
	}

	/*######################################
	 * Specific method to let tests Sun javac compilation available...
	 #######################################*/
	/*
	 * Cleans up the given directory by removing all the files it contains as well
	 * but leaving the directory.
	 * @throws TargetException if the target path could not be cleaned up
	 */
	protected void cleanupDirectory(File directory) throws TargetException {
		if (!directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete()) {
					throw new TargetException("Could not delete file " + file.getPath());
				}
			}
		}
		if (!directory.delete()) {
			throw new TargetException("Could not delete directory " + directory.getPath());
		}
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void printFiles(String[] testFiles) {
		for (int i=0, length=testFiles.length; i<length; i++) {
			System.out.println(testFiles[i++]);
			System.out.println(testFiles[i]);
		}
		System.out.println("");
	}

	/*#########################################
	 * Override basic runConform and run Negative methods to compile test files
	 * with Sun compiler (if specified) and compare its results with ours.
	 ##########################################*/
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String)
	 */
	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory, String[] vmArguments,
			Map customOptions, ICompilerRequestor clientRequestor) {
		try {
			super.runConformTest(testFiles, expectedSuccessOutputString,
					classLib, shouldFlushOutputDirectory, vmArguments,
					customOptions, clientRequestor);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, null);
		}
	}

	/*
	 * Run Sun compilation using javac.
	 * Use JRE directory to retrieve javac bin directory and current classpath for
	 * compilation.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 */
	protected void runJavac(String[] testFiles, final String expectedProblemLog) {
		try {
			// Write files in dir
			IPath dirFilePath = writeFiles(testFiles);
			
			String testName = shortTestName();
			Process process = null;
			try {
				// Compute classpath
				String[] classpath = getDefaultClassPaths();
				StringBuffer cp = new StringBuffer();
				int length = classpath.length;
				for (int i = 0; i < length; i++) {
					if (classpath[i].indexOf(" ") != -1) {
						cp.append("\"" + classpath[i] + "\"");
					} else {
						cp.append(classpath[i]);
					}
					if (i<(length-1)) cp.append(";");
				}
				// Compute command line
				IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
				IPath javacPath = jdkDir.append("bin").append("javac.exe");
				StringBuffer cmdLine = new StringBuffer(javacPath.toString());
				cmdLine.append(" -classpath ");
				cmdLine.append(cp);
				cmdLine.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable recommended warnings
				if (this.dirPath.equals(dirFilePath)) {
					cmdLine.append("*.java");
				} else {
					IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(this.dirPath.segmentCount());
					String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
					cmdLine.append(subDirName);
				}
//				System.out.println(testName+": "+cmdLine.toString());
//				System.out.println(GenericTypeTest.this.dirPath.toFile().getAbsolutePath());
				// Launch process
				process = Runtime.getRuntime().exec(cmdLine.toString(), null, this.dirPath.toFile());
	            // Log errors
	            Logger errorLogger = new Logger(process.getErrorStream(), "ERROR");            
	            
	            // Log output
	            Logger outputLogger = new Logger(process.getInputStream(), "OUTPUT");
	                
	            // start the threads to run outputs (standard/error)
	            errorLogger.start();
	            outputLogger.start();

	            // Wait for end of process
				int exitValue = process.waitFor();

				// Compare compilation results
				if (expectedProblemLog == null) {
					if (exitValue != 0) {
						System.out.println("========================================");
						System.out.println(testName+": javac has found error(s) although we're expecting conform result:\n");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
					}
					else if (errorLogger.buffer.length() > 0) {
						System.out.println("========================================");
						System.out.println(testName+": javac displays warning(s) although we're expecting conform result:\n");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
					}
				} else if (exitValue == 0) {
					if (errorLogger.buffer.length() == 0) {
						System.out.println("========================================");
						System.out.println(testName+": javac has found no error/warning although we're expecting negative result:");
						System.out.println(expectedProblemLog);
						printFiles(testFiles);
					} else if (expectedProblemLog.indexOf("ERROR") >0 ){
						System.out.println("========================================");
						System.out.println(testName+": javac has found warning(s) although we're expecting error(s):");
						System.out.println("javac:");
						System.out.println(errorLogger.buffer.toString());
						System.out.println("eclipse:");
						System.out.println(expectedProblemLog);
						printFiles(testFiles);
					} else {
						// TODO (frederic) compare warnings in each result and verify they are similar...
//						System.out.println(testName+": javac has found warnings :");
//						System.out.print(errorLogger.buffer.toString());
//						System.out.println(testName+": we're expecting warning results:");
//						System.out.println(expectedProblemLog);
					}
				} else if (errorLogger.buffer.length() == 0) {
					System.out.println("========================================");
					System.out.println(testName+": javac displays no output although we're expecting negative result:\n");
					System.out.println(expectedProblemLog);
					printFiles(testFiles);
				}
			} catch (IOException ioe) {
				System.out.println(testName+": Not possible to launch Sun javac compilation!");
			} catch (InterruptedException e1) {
				if (process != null) process.destroy();
				System.out.println(testName+": Sun javac compilation was aborted!");
			}

			// Clean up written file(s)
			IPath testDir =  new Path(Util.getOutputDirectory()).append(shortTestName());
			cleanupDirectory(testDir.toFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * Override to compile test files with Sun compiler if specified and compare its results with ours.
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput) {
		try {
			super.runNegativeTest(testFiles, expectedProblemLog, classLib,
					shouldFlushOutputDirectory, customOptions, generateOutput);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, expectedProblemLog);
		}
	}

	/*
	 * Get short test name (without compliance info)
	 */
	protected String shortTestName() {
		String fname = getName();
		int idx = fname.indexOf(" - "); //$NON-NLS-1$
		if (idx < 0) {
			return fname;
		}
		return fname.substring(idx+3);
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected IPath writeFiles(String[] testFiles) {
		// Compute and create specific dir
		IPath outDir = new Path(Util.getOutputDirectory());
		this.dirPath =  outDir.append(shortTestName());
		File dir = this.dirPath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// For each given test files
		IPath dirFilePath = null;
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			IPath filePath = this.dirPath.append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				dir = filePath.removeLastSegments(1).toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			if (dirFilePath == null|| (filePath.segmentCount()-1) < dirFilePath.segmentCount()) {
				dirFilePath = filePath.removeLastSegments(1);
			}
			Util.writeToFile(contents, filePath.toString());
		}
		return dirFilePath;
	}
}