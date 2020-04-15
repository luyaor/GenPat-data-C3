package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class JavadocTest extends AbstractRegressionTest {
	
boolean useLibrary = false;
static String zipFile = "/TestJavadocVisibility.zip";
static final String LINE_SEPARATOR = System.getProperty("line.separator");
public static ArrayList allTestClasses = null;

static {
	allTestClasses = new ArrayList(6);
	allTestClasses.add(JavadocTestForClass.class);
	allTestClasses.add(JavadocTestForConstructor.class);
	allTestClasses.add(JavadocTestForField.class);
	allTestClasses.add(JavadocTestForInterface.class);
	allTestClasses.add(JavadocTestForMethod.class);
	allTestClasses.add(JavadocTestMixed.class);
	allTestClasses.add(JavadocTestOptions.class);
}


public static void addTest(TestSuite suite, Class testClass) {
	TestSuite innerSuite = new TestSuite(testClass);
	suite.addTest(innerSuite);
}
public static Test suite() {
	TestSuite suite = new TestSuite(JavadocTest.class.getName());
	for (int i=0; i<allTestClasses.size(); i++) {
		addTest(suite, (Class) allTestClasses.get(i));
	}
	return new RegressionTestSetup(suite, COMPLIANCE_1_4);
}

public JavadocTest(String name) {
	super(name);
}

public static boolean equals(String c, String s) {
	if (c == null) {
		if (s == null)
			return true;
		else
			return false;
	}
	return c.equals(s);
}

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
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
}
