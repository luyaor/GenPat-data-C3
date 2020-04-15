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
package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

public class ProfilingASTConvertionTest extends AbstractJavaModelTests {
	
	static class Result implements Comparable {
		long length;
		long time;
		String unitName;
		Result(String unitName, long time, long length) {
			this.time = time;
			this.unitName = unitName;
			this.length = length / 1024;
		}
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			Result result = (Result) o;
			if (this.time < result.time) {
				return -1;
			} else if (this.time == result.time) {
				return 0;
			}
			return 1;
		}
	}
	private static final int INCREMENTS = 100;
	
	private static boolean RESOLVE_BINDINGS = false;

	protected static String getConverterJCLPath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMin.jar"; //$NON-NLS-1$
	}

	protected static String getConverterJCLRootSourcePath() {
		return ""; //$NON-NLS-1$
	}

	protected static String getConverterJCLSourcePath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMinsrc.zip"; //$NON-NLS-1$
	}

	public static Test suite() {
		if (true) {
			return new Suite(ProfilingASTConvertionTest.class);		
		}
		TestSuite suite = new Suite(ProfilingASTConvertionTest.class.getName());
		suite.addTest(new ProfilingASTConvertionTest("test0001"));
		return suite;
	}

	ICompilationUnit[] compilationUnits;

	public ProfilingASTConvertionTest(String name) {
		super(name);
	}
	
	public String display(int value, int numberOfFiguresForRange) {
		int numberOfFigures = value == 0 ? 1 : (int) (Math.log(value)/ Math.log(10));
		if ((value % 10) == 0) {
			numberOfFigures = (int) (Math.log(value + 1)/ Math.log(10));
		}
		StringBuffer buffer = new StringBuffer();
		while(numberOfFigures < numberOfFiguresForRange) {
			buffer.append(" ");
			numberOfFigures++;
		}
		buffer.append(value);
		return String.valueOf(buffer);
	}

	/**
	 * @param array
	 * @param increment
	 */
	private void printDistribution(long[] array, int increment) {
		int bound = increment;
		int counter = 0;
		int totalCounter = 0;
		int length = array.length;
		long max = array[length - 1];
		int numberOfFiguresForRange = (int) (Math.log(max)/ Math.log(10));
		if ((max % increment) == 0) {
			numberOfFiguresForRange = (int) (Math.log(max + 1)/ Math.log(10));
		}
		int numberOfFiguresForCounter = (int) (Math.log(length)/ Math.log(10));
		if ((length % increment) == 0) {
			numberOfFiguresForCounter = (int) (Math.log(length + 1)/ Math.log(10));
		}
		for (int i = 0; i < length; i++) {
			if (array[i] < bound) {
				counter++;
			} else {
				i--;
				totalCounter += counter;
				printRange(counter, bound, increment, totalCounter, length, numberOfFiguresForRange, numberOfFiguresForCounter);
				counter = 0;
				bound += increment;
			}
		}
		totalCounter += counter;
		printRange(counter, bound, increment, totalCounter, length, numberOfFiguresForRange, numberOfFiguresForCounter);
	}
		
	/**
	 * @param counter
	 * @param bound
	 */
	private void printRange(int counter, int bound, int increment, int totalCounter, int length, int numberOfFiguresForRange, int numberOfFiguresForCounters) {
		if (counter != 0) {
			StringBuffer buffer = new StringBuffer();
			int low = bound - increment;
			if (low != 0) {
				low++;
			}
			DecimalFormat format = new DecimalFormat("###.##");
			buffer
				.append(display(low, numberOfFiguresForRange))
				.append(" - ")
				.append(display(bound, numberOfFiguresForRange))
				.append(" : ")
				.append(display(counter, numberOfFiguresForCounters))
				.append("\t\t")
				.append(format.format(100.0 * ((double) totalCounter / length)));
			System.out.println(String.valueOf(buffer));
		}
	}
	
	/**
	 * @param totalTime
	 * @param length
	 * @param times
	 * @param arrayList
	 */
	private void reportResults(int apiLevel, long totalTime, int length, long[] times, ArrayList arrayList) {
		System.out.println("===============================================================================");
		System.out.print("================================ ");
		switch(apiLevel) {
			case AST.JLS2 :
				System.out.print("JLS2");
				break;
			case AST.JLS3 :
				System.out.print("JLS3");
				break;
		}
		System.out.print(" BINDING IS ");
		System.out.print(RESOLVE_BINDINGS ? "ON  " : "OFF ");
		System.out.println("==========================");
		System.out.println("===============================================================================");
		Arrays.sort(times);
		System.out.println("===================================== TIMES ===================================");
		System.out.println("Fastest = " + times[0] + "ms");
		long maxTime = times[length - 1];
		System.out.println("Slowest = " + maxTime + "ms");
		System.out.println("Total = " + totalTime + "ms");
		System.out.println("================================== DISTRIBUTION ===============================");
		printDistribution(times, INCREMENTS);
		System.out.println("================================= SORTED BY TIME ==============================");
		Collections.sort(arrayList);
		for (Iterator iterator = arrayList.iterator(); iterator.hasNext(); ) {
			final Result next = (Result) iterator.next();
			System.out.println(next.unitName + "(" + next.length + "KB) - " + next.time + "ms");
		}
		System.out.println("================================ SORTED BY LENGTH =============================");
		Collections.sort(arrayList, new Comparator() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				Result r1 = (Result) o1;
				Result r2 = (Result) o2;
				if (r1.length < r2.length) {
					return -1;
				} else if (r1.length == r2.length) {
					return 0;
				}
				return 1;
			}
		});
		for (Iterator iterator = arrayList.iterator(); iterator.hasNext(); ) {
			final Result next = (Result) iterator.next();
			System.out.println(next.unitName + "(" + next.length + "KB) - " + next.time + "ms");
		}
	}

	public void setupConverterJCL() throws IOException {
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL"; //$NON-NLS-1$
		String localJCLPath =getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
		EXTERNAL_JAR_DIR_PATH = localJCLPath;
		java.io.File jclDir = new java.io.File(localJCLPath);
		java.io.File jclMin =
			new java.io.File(localJCLPath + separator + "converterJclMin.jar"); //$NON-NLS-1$
		java.io.File jclMinsrc = new java.io.File(localJCLPath + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir); //$NON-NLS-1$
			}
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "converterJclMin.jar"); //$NON-NLS-1$
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
			copy(resourceJCLMinsrc, jclMinsrc);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "converterJclMin.jar"); //$NON-NLS-1$
			if (jclMin.lastModified() < resourceJCLMin.lastModified() || jclMin.length() != resourceJCLMin.length()) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
			if (jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified() || jclMinsrc.length() < resourceJCLMinsrc.length()) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}
	
	public IJavaProject setUpJavaProject(String projectName, Map options) throws CoreException, IOException {
		IJavaProject project = super.setUpJavaProject(projectName);
		project.setOptions(options);
		return project;
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setupConverterJCL();
		Map options = JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);

		IJavaProject javaProject = setUpJavaProject("Compiler", options); //$NON-NLS-1$

		// ensure variables are set
		if (JavaCore.getClasspathVariable("ConverterJCL_LIB") == null) { //$NON-NLS-1$
			JavaCore.setClasspathVariables(
				new String[] {"CONVERTER_JCL_LIB", "CONVERTER_JCL_SRC", "CONVERTER_JCL_SRCROOT"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new Path[] {new Path(ConverterTestSetup.getConverterJCLPath()), new Path(ConverterTestSetup.getConverterJCLSourcePath()), new Path(ConverterTestSetup.getConverterJCLRootSourcePath())},
				null);
		}		
		assertNotNull("No java project", javaProject);
		IPackageFragment[] packageFragments = javaProject.getPackageFragments();
		assertNotNull("No package fragments", packageFragments);
		ArrayList collector = new ArrayList();
		for (int i = 0, max = packageFragments.length; i < max; i++) {
			ICompilationUnit[] units = packageFragments[i].getCompilationUnits();
			if (units != null) {
				for (int j = 0, max2 = units.length; j < max2; j++) {
					collector.add(units[j]);
				}
			}
		}
		this.compilationUnits = new ICompilationUnit[collector.size()];
		collector.toArray(this.compilationUnits);
	}

	public void test0000() throws JavaModelException {
		try {
			RESOLVE_BINDINGS = true;
			final int apiLevel = AST.JLS3;
			ASTParser parser = ASTParser.newParser(apiLevel);
			parser.setResolveBindings(RESOLVE_BINDINGS);
			long totalTime = 0;
			int length = this.compilationUnits.length;
			long[] times = new long[length];
			ArrayList arrayList = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				parser.setSource(this.compilationUnits[i]);
				parser.setResolveBindings(RESOLVE_BINDINGS);
				long time = System.currentTimeMillis();
				ASTNode node = parser.createAST(null);
				times[i] = System.currentTimeMillis() - time;
				totalTime += times[i];
				assertNotNull("No node", node);
				assertEquals("Wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit unit = (CompilationUnit) node;
				assertEquals("Has problem", 0, unit.getProblems().length);
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				StringBuffer buffer = new StringBuffer();
				buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
				IResource resource = this.compilationUnits[i].getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					File f = new File(file.getLocation().toOSString());
					if (f.exists()) {
						arrayList.add(new Result(String.valueOf(buffer), times[i], f.length()));
					}
				}
			}
		} finally {
			RESOLVE_BINDINGS = false;
		}
	}
	
	public void test0001() throws JavaModelException {
		try {
			RESOLVE_BINDINGS = true;
			final int apiLevel = AST.JLS3;
			ASTParser parser = ASTParser.newParser(apiLevel);
			parser.setResolveBindings(RESOLVE_BINDINGS);
			long totalTime = 0;
			int length = this.compilationUnits.length;
			long[] times = new long[length];
			ArrayList arrayList = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				parser.setSource(this.compilationUnits[i]);
				parser.setResolveBindings(RESOLVE_BINDINGS);
				long time = System.currentTimeMillis();
				ASTNode node = parser.createAST(null);
				times[i] = System.currentTimeMillis() - time;
				totalTime += times[i];
				assertNotNull("No node", node);
				assertEquals("Wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit unit = (CompilationUnit) node;
				assertEquals("Has problem", 0, unit.getProblems().length);
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				StringBuffer buffer = new StringBuffer();
				buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
				IResource resource = this.compilationUnits[i].getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					File f = new File(file.getLocation().toOSString());
					if (f.exists()) {
						arrayList.add(new Result(String.valueOf(buffer), times[i], f.length()));
					}
				}
			}
			reportResults(apiLevel, totalTime, length, times, arrayList);
		} finally {
			RESOLVE_BINDINGS = false;
		}
	}

	public void test0002() throws JavaModelException {
		try {
			RESOLVE_BINDINGS = false;
			final int apiLevel = AST.JLS3;
			ASTParser parser = ASTParser.newParser(apiLevel);
			parser.setResolveBindings(RESOLVE_BINDINGS);
			long totalTime = 0;
			int length = this.compilationUnits.length;
			long[] times = new long[length];
			ArrayList arrayList = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				parser.setSource(this.compilationUnits[i]);
				parser.setResolveBindings(RESOLVE_BINDINGS);
				long time = System.currentTimeMillis();
				ASTNode node = parser.createAST(null);
				times[i] = System.currentTimeMillis() - time;
				totalTime += times[i];
				assertNotNull("No node", node);
				assertEquals("Wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit unit = (CompilationUnit) node;
				assertEquals("Has problem", 0, unit.getProblems().length);
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				StringBuffer buffer = new StringBuffer();
				buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
				IResource resource = this.compilationUnits[i].getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					File f = new File(file.getLocation().toOSString());
					if (f.exists()) {
						arrayList.add(new Result(String.valueOf(buffer), times[i], f.length()));
					}
				}
			}
			reportResults(apiLevel, totalTime, length, times, arrayList);
		} finally {
			RESOLVE_BINDINGS = false;
		}
	}
	
	public void test0003() throws JavaModelException {
		try {
			RESOLVE_BINDINGS = true;
			final int apiLevel = AST.JLS2;
			ASTParser parser = ASTParser.newParser(apiLevel);
			parser.setResolveBindings(RESOLVE_BINDINGS);
			long totalTime = 0;
			int length = this.compilationUnits.length;
			long[] times = new long[length];
			ArrayList arrayList = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				parser.setSource(this.compilationUnits[i]);
				parser.setResolveBindings(RESOLVE_BINDINGS);
				long time = System.currentTimeMillis();
				ASTNode node = parser.createAST(null);
				times[i] = System.currentTimeMillis() - time;
				totalTime += times[i];
				assertNotNull("No node", node);
				assertEquals("Wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit unit = (CompilationUnit) node;
				assertEquals("Has problem", 0, unit.getProblems().length);
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				StringBuffer buffer = new StringBuffer();
				buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
				IResource resource = this.compilationUnits[i].getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					File f = new File(file.getLocation().toOSString());
					if (f.exists()) {
						arrayList.add(new Result(String.valueOf(buffer), times[i], f.length()));
					}
				}
			}
			reportResults(apiLevel, totalTime, length, times, arrayList);
		} finally {
			RESOLVE_BINDINGS = false;
		}
	}

	public void test0004() throws JavaModelException {
		try {
			RESOLVE_BINDINGS = false;
			final int apiLevel = AST.JLS2;
			ASTParser parser = ASTParser.newParser(apiLevel);
			parser.setResolveBindings(RESOLVE_BINDINGS);
			long totalTime = 0;
			int length = this.compilationUnits.length;
			long[] times = new long[length];
			ArrayList arrayList = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				parser.setSource(this.compilationUnits[i]);
				parser.setResolveBindings(RESOLVE_BINDINGS);
				long time = System.currentTimeMillis();
				ASTNode node = parser.createAST(null);
				times[i] = System.currentTimeMillis() - time;
				totalTime += times[i];
				assertNotNull("No node", node);
				assertEquals("Wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit unit = (CompilationUnit) node;
				assertEquals("Has problem", 0, unit.getProblems().length);
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				StringBuffer buffer = new StringBuffer();
				buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
				IResource resource = this.compilationUnits[i].getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					File f = new File(file.getLocation().toOSString());
					if (f.exists()) {
						arrayList.add(new Result(String.valueOf(buffer), times[i], f.length()));
					}
				}
			}
			reportResults(apiLevel, totalTime, length, times, arrayList);
		} finally {
			RESOLVE_BINDINGS = false;
		}
	}
}

