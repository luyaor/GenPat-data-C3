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

import java.io.UnsupportedEncodingException;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;

public class EncodingTests extends ModifyingResourceTests {
	IProject encodingProject;
	IJavaProject encodingJavaProject;
	IFile utf8File;
	ISourceReference utf8Source;
	static String vmEncoding = System.getProperty("file.encoding");
	static String wkspEncoding = vmEncoding;

	public EncodingTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return suite(EncodingTests.class, null);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "Bug51529a", "Bug51529b" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 16, -1 };
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		wkspEncoding = getWorkspaceRoot().getDefaultCharset();
		System.out.println("Encoding tests using Workspace charset: "+wkspEncoding+" and VM charset: "+vmEncoding);
		this.encodingJavaProject = setUpJavaProject("Encoding");
		this.encodingProject = (IProject) this.encodingJavaProject.getResource();
		this.utf8File = (IFile) this.encodingProject.findMember("src/testUTF8/Test.java");
	}

	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		getWorkspaceRoot().setDefaultCharset(null);
	}
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 *  (non-Javadoc)
	 * Reset UTF-8 file and project charset to default.
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		this.encodingProject.setDefaultCharset(null);
		this.utf8File.setCharset(null);
		if (this.utf8Source != null) ((IOpenable) this.utf8Source).close();
		this.encodingJavaProject.close();
	}

	/**
	 * Check that the compilation unit is saved with the proper encoding.
	 */
	public void testCreateCompilationUnitAndImportContainer() throws JavaModelException, CoreException {
		String savedEncoding = null;
		try {
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			
			savedEncoding = preferences.getString(ResourcesPlugin.PREF_ENCODING);
			String encoding = "UTF-8";
			preferences.setValue(ResourcesPlugin.PREF_ENCODING, encoding);
			
			ResourcesPlugin.getPlugin().savePluginPreferences();

			IJavaProject newProject = createJavaProject("P", new String[] { "" }, "");
			IPackageFragment pkg = getPackageFragment("P", "", "");
			String source = "public class A {\r\n" +
				"	public static main(String[] args) {\r\n" +
				"		System.out.println(\"\u00e9\");\r\n" +
				"	}\r\n" +
				"}";
			ICompilationUnit cu= pkg.createCompilationUnit("A.java", source, false, new NullProgressMonitor());
			assertCreation(cu);
			cu.rename("B.java", true, new NullProgressMonitor());
			cu = pkg.getCompilationUnit("B.java");
			cu.rename("A.java", true, new NullProgressMonitor());
			cu = pkg.getCompilationUnit("A.java");
			byte[] tab = null;
			try {
				tab = cu.getSource().getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			byte[] encodedContents = Util.getResourceContentsAsByteArray(newProject.getProject().getWorkspace().getRoot().getFile(cu.getPath()));
			assertTrue("wrong size of encoded string", tab.length == encodedContents.length);
			for (int i = 0, max = tab.length; i < max; i++) {
				assertTrue("wrong size of encoded character at" + i, tab[i] == encodedContents[i]);
			}
		} finally {
			deleteProject("P");
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			preferences.setValue(ResourcesPlugin.PREF_ENCODING, savedEncoding);
			ResourcesPlugin.getPlugin().savePluginPreferences();
		}
	}	

	/*
	##################
	#	Test with compilation units
	##################
	/*
	 * Get compilation unit source on a file written in UTF-8 charset using specific UTF-8 encoding for file.
	 * Verify first that source is the same than file contents read using UTF-8 encoding...
	 * Also verify that bytes array converted back to UTF-8 is the same than the file bytes array.
	 */
	public void test001() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Set file encoding
		String encoding = "UTF-8";
		this.utf8File.setCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);

		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(encoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		for (int i = 0, max = sourceBytes.length; i < max; i++) {
			assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
		}
	}	

	/*
	 * Get compilation unit source on a file written in UTF-8 charset using UTF-8 encoding for project.
	 * Verify first that source is the same than file contents read using UTF-8 encoding...
	 * Also verify that bytes array converted back to UTF-8 is the same than the file bytes array.
	 */
	public void test002() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Set project encoding
		String encoding = "UTF-8";
		this.encodingProject.setDefaultCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);

		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(encoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		for (int i = 0, max = sourceBytes.length; i < max; i++) {
			assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
		}
	}	

	/*
	 * Get compilation unit source on a file written in UTF-8 charset using workspace default encoding.
	 * Verify that source is the same than file contents read using workspace default encoding...
	 * Also verify that bytes array converted back to wokrspace default encoding is the same than the file bytes array.
	 * Do not compare array contents in case of VM default encoding equals to "ASCII" as meaningful bit 7 is lost
	 * during first conversion...
	 */
	public void test003() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);
			
		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(wkspEncoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		// Do not compare arrays contents as system encoding may have lost meaningful bit 7 during convertion...)
//		if (!"ASCII".equals(vmEncoding)) {
//			for (int i = 0, max = sourceBytes.length; i < max; i++) {
//				assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
//			}
//		}
	}

	/*
	 * Get compilation unit source on a file written in UTF-8 charset using an encoding
	 * for file different than VM default one.
	 * Verify that source is different than file contents read using VM default encoding...
	 */
	public void test004() throws JavaModelException, CoreException {

		// Set file encoding
		String encoding = "UTF-8".equals(vmEncoding) ? "Cp1252" : "UTF-8";
		this.utf8File.setCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, vmEncoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}

	/*
	 * Get compilation unit source on a file written in UTF-8 charset using an encoding
	 * for project different than VM default one.
	 * Verify that source is different than file contents read using VM default encoding...
	 */
	public void test005() throws JavaModelException, CoreException {

		// Set project encoding
		String encoding = "UTF-8".equals(vmEncoding) ? "Cp1252" : "UTF-8";
		this.encodingProject.setDefaultCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, vmEncoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}	

	/*
	 * Get compilation unit source on a file written in UTF-8 charset using workspace default encoding.
	 * Verify that source is different than file contents read using VM default encoding or another one
	 * if VM and Workspace default encodings are identical...
	 */
	public void test006() throws JavaModelException, CoreException {

		// Set encoding different than workspace default one
		String encoding = wkspEncoding.equals(vmEncoding) ? ("UTF-8".equals(wkspEncoding) ? "Cp1252" : "UTF-8") : vmEncoding;

		// Get source and compare with file contents
		this.utf8Source = getCompilationUnit(this.utf8File.getFullPath().toString());
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, encoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}

	/*
	##############
	#	Tests with class file
	##############
	/* Same config than test001  */
	public void test011() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Set file encoding
		String encoding = "UTF-8";
		this.utf8File.setCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);

		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(encoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		for (int i = 0, max = sourceBytes.length; i < max; i++) {
			assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
		}
	}	

	/* Same config than test002  */
	public void test012() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Set project encoding
		String encoding = "UTF-8";
		this.encodingProject.setDefaultCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);

		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(encoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		for (int i = 0, max = sourceBytes.length; i < max; i++) {
			assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
		}
	}	

	/* Same config than test003  */
	public void test013() throws JavaModelException, CoreException, UnsupportedEncodingException {

		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = this.utf8Source.getSource();
		String systemSource = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, systemSource);
			
		// Now compare bytes array
		byte[] sourceBytes = source.getBytes(wkspEncoding);
		byte[] encodedBytes = Util.getResourceContentsAsByteArray(this.utf8File);
		assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
		// Do not compare arrays contents as system encoding may have lost meaningful bit 7 during convertion...)
//		if (!"ASCII".equals(vmEncoding)) {
//			for (int i = 0, max = sourceBytes.length; i < max; i++) {
//				assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
//			}
//		}
	}

	/* Same config than test004  */
	public void test014() throws JavaModelException, CoreException {

		// Set file encoding
		String encoding = "UTF-8".equals(vmEncoding) ? "Cp1252" : "UTF-8";
		this.utf8File.setCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, vmEncoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}

	/* Same config than test005  */
	public void test015() throws JavaModelException, CoreException {

		// Set project encoding
		String encoding = "UTF-8".equals(vmEncoding) ? "Cp1252" : "UTF-8";
		this.encodingProject.setDefaultCharset(encoding);
		
		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, vmEncoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}	

	/* Same config than test006  */
	public void test016() throws JavaModelException, CoreException {

		// Set encoding different than workspace default one
		String encoding = wkspEncoding.equals(vmEncoding) ? ("UTF-8".equals(wkspEncoding) ? "Cp1252" : "UTF-8") : vmEncoding;

		// Get source and compare with file contents
		this.utf8Source = getClassFile("Encoding" , "bins", "testUTF8", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.utf8Source.getSource());
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, encoding));
		encodedContents = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(encodedContents);
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}

	/*
	###############################
	#	Tests with jar file and source attached in zip file
	###############################
	/**
	 * Get class file from jar file with an associated source written in UTF-8 charset using no specific encoding for file.
	 * Verification is done by comparing source with file contents read directly with VM encoding...
	 */
	public void test021() throws JavaModelException, CoreException {

		// Get class file and compare source
		IPackageFragmentRoot root = getPackageFragmentRoot("Encoding", "testUTF8.jar");
		this.utf8Source = root.getPackageFragment("testUTF8").getClassFile("Test.class");
		assertNotNull(this.utf8Source);
		String source = this.utf8Source.getSource();
		assertNotNull(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, vmEncoding));
		assertSourceEquals("Encoded UTF-8 source should have been decoded the same way!", source, encodedContents);

		// Cannot compare bytes array without encoding as we're dependent of linux/windows os for new lines delimiter
	}

	/*
	 * Get class file from jar file with an associated source written in UTF-8 charset using specific UTF-8 encoding for project.
	 * Verification is done by comparing source with file contents read directly with UTF-8 encoding...
	 */
	public void test022() throws JavaModelException, CoreException {

		// Set project encoding
		String encoding = "UTF-8";
		this.encodingProject.setDefaultCharset(encoding);

		// Get class file and compare source (should not be the same as modify charset on zip file has no effect...)
		IPackageFragmentRoot root = getPackageFragmentRoot("Encoding", "testUTF8.jar");
		this.utf8Source = root.getPackageFragment("testUTF8").getClassFile("Test.class");
		assertNotNull(this.utf8Source);
		String source = this.utf8Source.getSource();
		assertNotNull(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, encoding));
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
	}

	/*
	 * Get class file from jar file with an associated source written in UTF-8 charset using specific UTF-8 encoding for file.
	 * Verification is done by comparing source with file contents read directly with UTF-8 encoding...
	 */
	public void test023() throws JavaModelException, CoreException {

		// Set file encoding
		String encoding = "UTF-8";
		IFile zipFile = (IFile) this.encodingProject.findMember("testUTF8.zip"); //$NON-NLS-1$
		assertNotNull("Cannot find class file!", zipFile);
		zipFile.setCharset(encoding);

		// Get class file and compare source (should not be the same as modify charset on zip file has no effect...)
		IPackageFragmentRoot root = getPackageFragmentRoot("Encoding", "testUTF8.jar");
		this.utf8Source = root.getPackageFragment("testUTF8").getClassFile("Test.class");
		assertNotNull(this.utf8Source);
		String source = this.utf8Source.getSource();
		assertNotNull(source);
		String encodedContents = new String (Util.getResourceContentsAsCharArray(this.utf8File, encoding));
		assertFalse("Sources should not be the same as they were decoded with different encoding!", encodedContents.equals(source));
		
		// Reset zip file encoding
		zipFile.setCharset(null);
	}

	/**
	 * Test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55930.
	 * Verify Buffer.save(IProgressMonitor, boolean) method.
	 */
	public void test030() throws JavaModelException, CoreException {
		ICompilationUnit workingCopy = null;
		try {
			String encoding = "UTF-8";
			this.createJavaProject("P", new String[] {""}, "");
			String initialContent = "/**\n"+
				" */\n"+
				"public class Test {}";
			IFile file = this.createFile("P/Test.java", initialContent);
			file.setCharset(encoding);
			ICompilationUnit cu = this.getCompilationUnit("P/Test.java"); 
			
			// Modif direct the buffer
			String firstModif = "/**\n"+
				" * Caract?res exotiques:\n"+
				" * ?|#|?|?|?|?|?|?|?|?|??\n"+
				" */\n"+
				"public class Test {}";
			cu.getBuffer().setContents(firstModif);
			cu.getBuffer().save(null, true);
			String source = cu.getBuffer().getContents();
			
			// Compare strings and bytes arrays
			String encodedContents = new String (Util.getResourceContentsAsCharArray(file, encoding));
			assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, source);
			byte[] sourceBytes = source.getBytes(encoding);
			byte[] encodedBytes = Util.getResourceContentsAsByteArray(file);
			assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
			for (int i = 0, max = sourceBytes.length; i < max; i++) {
				assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
			}
		} catch (UnsupportedEncodingException e) {
		} finally {
			this.stopDeltas();
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
			this.deleteProject("P");
		}

	}

	/**
	 * Test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55930.
	 * Verify CommitWorkingCopyOperation.executeOperation() method.
	 */
	public void test031() throws JavaModelException, CoreException {
		ICompilationUnit workingCopy = null;
		try {
			String encoding = "UTF-8";
			this.createJavaProject("P", new String[] {""}, "");
			String initialContent = "/**\n"+
				" */\n"+
				"public class Test {}";
			IFile file = this.createFile("P/Test.java", initialContent);
			file.setCharset(encoding);
			ICompilationUnit cu = this.getCompilationUnit("P/Test.java"); 
			
			// Modif using working copy
			workingCopy = cu.getWorkingCopy(null);
			String secondModif = "/**\n"+
				" * Caract?res exotiques:\n"+
				" * ?|#|?|?|?|?|?|?|?|?|??\n"+
				" * Autres caract?res exotiques:\n"+
				" * ?|?|?|?|?|?\n"+
				" */\n"+
				"public class Test {}";
			workingCopy.getBuffer().setContents(secondModif);
			workingCopy.commitWorkingCopy(true, null);
			String source = workingCopy.getBuffer().getContents();
			
			// Compare strings and bytes arrays
			String encodedContents = new String (Util.getResourceContentsAsCharArray(file));
			assertEquals("Encoded UTF-8 source should have been decoded the same way!", encodedContents, source);
			byte[] sourceBytes = source.getBytes(encoding);
			byte[] encodedBytes = Util.getResourceContentsAsByteArray(file);
			assertEquals("Wrong size of encoded string", encodedBytes.length, sourceBytes.length);
			for (int i = 0, max = sourceBytes.length; i < max; i++) {
				assertTrue("Wrong size of encoded character at " + i, sourceBytes[i] == encodedBytes[i]);
			}
		} catch (UnsupportedEncodingException e) {
		} finally {
			this.stopDeltas();
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
			this.deleteProject("P");
		}

	}
}
