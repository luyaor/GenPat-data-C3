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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class JavadocTest extends AbstractRegressionTest {
		
	boolean useLibrary = false;
	static String zipFile = "/TestJavadocVisibility.zip";
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static ArrayList allTestClasses = null;
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");
//	String docCommentSupport;
	boolean supportJavadoc;
	static boolean debug = false;

	static {
		allTestClasses = new ArrayList(6);
		allTestClasses.add(JavadocTestForClass.class);
		allTestClasses.add(JavadocTestForConstructor.class);
		allTestClasses.add(JavadocTestForField.class);
		allTestClasses.add(JavadocTestForInterface.class);
		allTestClasses.add(JavadocTestForMethod.class);
		allTestClasses.add(JavadocTestMixed.class);
		allTestClasses.add(JavadocTestOptions.class);
		// Reset forgotten subsets tests
		testsNames = null;
		testsNumbers= null;
		testsRange = null;
	}
	
	
	public static void addTest(TestSuite suite, Class testClass) {
		TestSuite innerSuite = new TestSuite(testClass);
		suite.addTest(innerSuite);
	}

	public static Test buildSuite(Class testClass) {
		TestSuite suite = new TestSuite(testClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(suiteForComplianceLevel(COMPLIANCE_1_3, testClass));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(suiteForComplianceLevel(COMPLIANCE_1_4, testClass));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(suiteForComplianceLevel(COMPLIANCE_1_5, testClass));
		}
		return suite;
	}
	
	public static Test suiteForComplianceLevel(String level, Class testClass) {
		TestSuite suite = new TestSuite(level);
		try {
			Class[] paramTypes = new Class[] { String.class };
			Constructor constructor = testClass.getConstructor(paramTypes);
			// Javadoc ENABLED
			String support = CompilerOptions.DISABLED;
			if (DOC_COMMENT_SUPPORT == null) {
				suite.addTest(suiteForJavadocSupport(level, testClass, constructor, CompilerOptions.ENABLED));
			} else {
				support =  DOC_COMMENT_SUPPORT;
			}

			// Javadoc DISABLED
			suite.addTest(suiteForJavadocSupport(level, testClass, constructor, support));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return suite;
	}

	public static Test suiteForJavadocSupport(String level, Class testClass, Constructor constructor, String support) {
		Test suite = suite(testClass, "Doc "+support);
		return new RegressionTestSetup(suite, level, support);
	}


	public static Test suite() {
		TestSuite suite = new TestSuite(JavadocTest.class.getName());
		for (int i=0; i<allTestClasses.size(); i++) {
			suite.addTest(buildSuite((Class) allTestClasses.get(i)));
		}
		return suite;
	}
	
	public JavadocTest(String name) {
		super(name);
	}
	/**
	 * @return Returns the docCommentSupport.
	 */
	public String getNamePrefix() {
		return "Doc "+(this.supportJavadoc?"on":"off")+" - ";
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		if (this.docCommentSupport == null) {
			return super.getName();
		} else {
			return getNamePrefix()+super.getName();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.util.AbstractCompilerTest#initialize(org.eclipse.jdt.core.tests.util.CompilerTestSetup)
	 */
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		this.supportJavadoc = !CompilerOptions.DISABLED.equals(this.docCommentSupport);
	}
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		return options;
	}
	
	protected String[] getDefaultClassPaths() {
		if (useLibrary) {
			String[] classLibs = super.getDefaultClassPaths();
			final int length = classLibs.length;
			String[] newClassPaths = new String[length + 1];
			System.arraycopy(classLibs, 0, newClassPaths, 0, length);
			newClassPaths[length] = getClass().getResource(zipFile).getPath();
			return newClassPaths;
		} else {
			return super.getDefaultClassPaths();
		}
	}
	
	static String[] referencedClasses = null;
	static {
		referencedClasses =
			new String[] {
				"test/AbstractVisibility.java",
				"package test;\n"
					+ "public abstract class AbstractVisibility {\n"
					+ "	private class AvcPrivate {\n"
					+ "		private int avf_private = 10;\n"
					+ "		public int avf_public = avf_private;\n"
					+ "		private int avm_private() {\n"
					+ "			avf_private = (new AvcPrivate()).avf_private;\n"
					+ "			return avf_private;\n"
					+ "		}\n"
					+ "		public int avm_public() {\n"
					+ "			return avm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class AvcPublic {\n"
					+ "		private int avf_private = 10;\n"
					+ "		public int avf_public = avf_private;\n"
					+ "		private int avm_private() {\n"
					+ "			avf_private = (new AvcPrivate()).avf_private;\n"
					+ "			return avf_private;\n"
					+ "		}\n"
					+ "		public int avm_public() {\n"
					+ "			return avm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int avf_private = 100;\n"
					+ "	public int avf_public = avf_private;\n"
					+ "	\n"
					+ "	private int avm_private() {\n"
					+ "		avf_private = (new AvcPrivate()).avf_private;\n"
					+ "		return avf_private;\n"
					+ "	}\n"
					+ "	public int avm_public() {\n"
					+ "		return avm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/Visibility.java",
				"package test;\n"
					+ "public class Visibility extends AbstractVisibility {\n"
					+ "	private class VcPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VcPrivate()).vf_private;\n"
					+ "			avf_private = vf_private;\n"
					+ "			return vf_private+avf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	};\n"
					+ "	public class VcPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VcPrivate()).vf_private;\n"
					+ "			avf_private = vf_private;\n"
					+ "			return vf_private+avf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	};\n"
					+ "	private int vf_private = 100;\n"
					+ "	private int avf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	public int avf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VcPrivate()).vf_private;\n"
					+ "		avf_private = vf_private;\n"
					+ "		return vf_private+avf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/copy/VisibilityPackage.java",
				"package test.copy;\n"
					+ "class VisibilityPackage {\n"
					+ "	private class VpPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class VpPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int vf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VpPrivate()).vf_private;\n"
					+ "		return vf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/copy/VisibilityPublic.java",
				"package test.copy;\n"
					+ "public class VisibilityPublic {\n"
					+ "	private class VpPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class VpPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int vf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VpPrivate()).vf_private;\n"
					+ "		return vf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n" };
	}
	
	protected void runConformReferenceTest(String[] testFiles) {
		String[] completedFiles = testFiles;
		if (!useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
		}
		runConformTest(completedFiles);
	}
	protected void runNegativeReferenceTest(String[] testFiles, String expected) {
		String[] completedFiles = testFiles;
		if (!useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
		}
		runNegativeTest(completedFiles, expected);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
	 */
	protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
		if (this.supportJavadoc) {
			super.runNegativeTest(testFiles, expectedProblemLog);
		} else {
			StringTokenizer tokenizer = new StringTokenizer(expectedProblemLog);
			int errors=0, javadocs=0;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if ("ERROR".equals(token) || "WARNING".equals(token)) {
					errors++;
				} else if ("Javadoc:".equals(token)) {
					javadocs++;
				}
			}
			if (errors == javadocs) {
				super.runConformTest(testFiles);
			} else if (javadocs == 0) {
				super.runNegativeTest(testFiles, expectedProblemLog);
			} else {
				if (debug) System.out.println("Test "+getName()+" skipped due to non-javadoc compiler errors...");
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[])
	 */
	protected void runConformTest(String[] testFiles) {
		if (this.supportJavadoc) {
			super.runConformTest(testFiles);
		} else {
			if (debug) System.out.println("Test "+getName()+" skipped as identical when Javadoc is enabled...");
		}
	}
}
