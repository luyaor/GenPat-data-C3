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

import junit.framework.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;


public class CodeCorrectionTests extends AbstractJavaModelTests {
	public static boolean fgDebug = false;
	public static boolean fgSpecific = false;
	
public CodeCorrectionTests(String name) {
	super(name);
}
private IMarker[] getMarkers(ICompilationUnit unit){
	try {
		IResource resource = unit.getCorrespondingResource();
		return resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
	}
	return new IMarker[0];
}
private IMarker getMarker(ICompilationUnit unit, String message) throws CoreException {
	IMarker[] markers = getMarkers(unit);
	for (int i = 0; i < markers.length; i++) {
		IMarker marker = markers[i];
		if (message.equals(marker.getAttribute(IMarker.MESSAGE))) {
			return marker;
		}
	}
	return null;
}
/**
 * Return the project names to load in the solution
 * when an independent test suite is being run.
 */
public static String[] getProjectNames() {
	return new String[] {"Compiler", "CodeCorrection"};
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	IJavaProject project = setUpJavaProject("CodeCorrection");
	
	// dummy query for waiting until the indexes are ready
	SearchEngine engine = new SearchEngine();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
	try {
		engine.searchAllTypeNames(
			project.getProject().getWorkspace(),
			null,
			"!@$#!@".toCharArray(),
			SearchPattern.R_PATTERN_MATCH,
			true, // case sensitive
			IJavaSearchConstants.CLASS,
			scope, 
			new ITypeNameRequestor() {
				public void acceptClass(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {}
				public void acceptInterface(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {}
			},
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
	} catch (CoreException e) {
	}
	//	do a full build to create markers
	JavaCore.setOptions(JavaCore.getDefaultOptions());
	try {
		getWorkspace().getRoot().getProject("CodeCorrection").build(IncrementalProjectBuilder.FULL_BUILD,null);
		waitForAutoBuild();
	} catch (CoreException e) {
		assertTrue("building failed", false);
	}
}
public void tearDownSuite() throws Exception {
	deleteProject("CodeCorrection");
	
	super.tearDownSuite();
}
public static Test suite() {
	Suite suite = new Suite(CodeCorrectionTests.class.getName());
	if (fgSpecific) {
		suite.addTest(new CodeCorrectionTests("testCorrectMethod1"));
		
		return suite;
	} else {
		suite.addTest(new CodeCorrectionTests("testCorrectFieldType1"));
		suite.addTest(new CodeCorrectionTests("testCorrectFieldType2"));
		suite.addTest(new CodeCorrectionTests("testCorrectFieldType3"));
		suite.addTest(new CodeCorrectionTests("testCorrectLocalVariableType1"));
		suite.addTest(new CodeCorrectionTests("testCorrectLocalVariableType2"));
		suite.addTest(new CodeCorrectionTests("testCorrectImport1"));
		suite.addTest(new CodeCorrectionTests("testCorrectImport2"));
		suite.addTest(new CodeCorrectionTests("testCorrectImport3"));
		suite.addTest(new CodeCorrectionTests("testCorrectSuperClass1"));
		suite.addTest(new CodeCorrectionTests("testCorrectSuperClass2"));
		suite.addTest(new CodeCorrectionTests("testCorrectSuperInterface1"));
		suite.addTest(new CodeCorrectionTests("testCorrectSuperInterface2"));
		suite.addTest(new CodeCorrectionTests("testCorrectException1"));
		suite.addTest(new CodeCorrectionTests("testCorrectException2"));
		suite.addTest(new CodeCorrectionTests("testCorrectMethod1"));
		suite.addTest(new CodeCorrectionTests("testCorrectMethod2"));
		suite.addTest(new CodeCorrectionTests("testCorrectField1"));
		suite.addTest(new CodeCorrectionTests("testCorrectField2"));
		suite.addTest(new CodeCorrectionTests("testCorrectLocalVariable1"));
		suite.addTest(new CodeCorrectionTests("testCorrectArgument1"));
		suite.addTest(new CodeCorrectionTests("testCorrectReturnType1"));
		suite.addTest(new CodeCorrectionTests("testCorrectReturnType2"));
		
		return suite;
	}
}
public void testCorrectFieldType1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectFieldType1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);
	
	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
		
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectFieldType2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectFieldType2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectFieldType3() throws CoreException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectFieldType3.java");
	IMarker marker = getMarker(cu, "AClassz cannot be resolved (or is not a valid type) for the field CorrectFieldType3.field");
	assertTrue("Marker not found", marker != null);
	try {
		engine.computeCorrections(marker, null, 0, requestor);
	} catch(CoreException e) {
	}
	
	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectLocalVariableType1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectLocalVariableType1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);
	
	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectLocalVariableType2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectLocalVariableType2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectImport1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectImport1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectImport2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectImport2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectImport3() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectImport3.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectSuperClass1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectSuperClass1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectSuperClass2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectSuperClass2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectSuperInterface1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectSuperInterface1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "cccz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions",
		"ccc\n"+
		"cccInterface",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectSuperInterface2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectSuperInterface2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AListenerz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AListener\n"+
		"AListenerEvent",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectException1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectException1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bbbz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions",
		"bbb\n"+
		"bbb.ccc",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectException2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectException2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AnExceptionz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions",
		"AnException",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectMethod1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectMethod1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectMethod2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectMethod2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectField1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectField1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectField2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectField2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectLocalVariable1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectLocalVariable1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectArgument1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectArgument1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "bar";
	int start = src.lastIndexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have one suggestion",
		"bar0",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		""+start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		""+end,
		requestor.getEnds());
}
public void testCorrectReturnType1() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectReturnType1.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "dddz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"ddd\n"+
		"ddd.eee",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
public void testCorrectReturnType2() throws JavaModelException {
	CorrectionEngine engine = new CorrectionEngine(JavaCore.getOptions());
	CodeCorrectionTestsRequestor requestor = new CodeCorrectionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("CodeCorrection", "src", "", "CorrectReturnType2.java");
	IMarker[] markers = getMarkers(cu);
	assertTrue("should have one problem",markers.length == 1);
	engine.computeCorrections(markers[0], null, 0, requestor);

	String src = cu.getSource();
	String error = "AClassz";
	int start = src.indexOf(error);
	int end = start + error.length();
	
	assertEquals(
		"should have two suggestions", 
		"AClass\n"+
		"AClass2",
		requestor.getSuggestions());
	assertEquals(
		"a start of a suggestion is not correct", 
		start+"\n"+
		start,
		requestor.getStarts());
	assertEquals(
		"a end of a suggestion is not correct", 
		end+"\n"+
		end,
		requestor.getEnds());
}
}
