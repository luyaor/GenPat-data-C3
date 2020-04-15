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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.*;

public class ResolveTests extends AbstractJavaModelTests {


public static Test suite() {
	if (false) {
		TestSuite suite = new Suite(ResolveTests.class.getName());
		suite.addTest(new ResolveTests("testLocalNameForClassFile"));
		return suite;
	}
	return new Suite(ResolveTests.class);
}

public ResolveTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Resolve");
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	
	super.tearDownSuite();
}
/**
 * Resolve default abstract method
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=23594
 */
public void testAbstractMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveAbstractMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo() {key=LSuperInterface;.foo()V} [in SuperInterface [in SuperInterface.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve an argument name
 */
public void testArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo(Object, int) [in ResolveArgumentName [in ResolveArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve an argument name with base type
 */
public void testArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo(Object, int) [in ResolveArgumentName [in ResolveArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the field "length" of an array
 */
public void testArrayLength() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayLength.java");
	IJavaElement[] elements = codeSelect(cu, "length", "length");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}

/**
 * Resolve an argument name inside catch statement
 */
public void testCatchArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveCatchArgumentName [in ResolveCatchArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve an argument name inside catch statement with base type
 */
public void testCatchArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveCatchArgumentName [in ResolveCatchArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType1.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 {key=LX1$Y1;} [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType2.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 {key=LX1$Y1;} [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (field type).
 */
public void testClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass1.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (local variable type).
 */
public void testClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass2.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X'(array initializer type).
 */
public void testClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass3.java");
	IJavaElement[] elements = codeSelect(cu, "X[]{", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (return type).
 */
public void testClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass4.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (method argument).
 */
public void testClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass5.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'SuperClass' (super class).
 */
public void testClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass6.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp1/X;} [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve a constructor
 */
public void testConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructor.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructor(\"", "ResolveConstructor");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructor(String) {key=LResolveConstructor;.(Ljava/lang/String;)V} [in ResolveConstructor [in ResolveConstructor.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor call
 */
public void testConstructorCallOfMemberType() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType.class");
	IJavaElement[] elements = codeSelect(cf, "Inner()", "Inner");
	assertElementsEqual(
		"Unexpected elements",
		"Inner(ResolveConstructorCallOfMemberType) {key=LResolveConstructorCallOfMemberType$Inner;.(LResolveConstructorCallOfMemberType;)V} [in Inner [in ResolveConstructorCallOfMemberType$Inner.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor declaration
 */
public void testConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructorDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructorDeclaration(i", "ResolveConstructorDeclaration");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorDeclaration(int) [in ResolveConstructorDeclaration [in ResolveConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelection() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection.java");
	IJavaElement[] elements = codeSelect(cu, "ject", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelection2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection2.java");
	IJavaElement[] elements = codeSelect(cu, "Obj", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelectionOnMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEmptySelectionOnMethod.java");
	IJavaElement[] elements = codeSelect(cu, "oo();", "");
	assertElementsEqual(
		"Unexpected elements",
		"foo() {key=LResolveEmptySelectionOnMethod;.foo()V} [in ResolveEmptySelectionOnMethod [in ResolveEmptySelectionOnMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit super constructor call
 */
public void testExplicitSuperConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitSuperConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "super(", "super");
	assertElementsEqual(
		"Unexpected elements",
		"SuperClass(int) {key=LSuperClass;.(I)V} [in SuperClass [in SuperClass.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit this constructor call
 */
public void testExplicitThisConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitThisConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "this(", "this");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveExplicitThisConstructorCall() {key=LResolveExplicitThisConstructorCall;.()V} [in ResolveExplicitThisConstructorCall [in ResolveExplicitThisConstructorCall.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the field "foo"
 */
public void testField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveField.java");
	IJavaElement[] elements = codeSelect(cu, "foo =", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo {key=LResolveField;.foo} [in ResolveField [in ResolveField.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve field declaration
 */
public void testFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveFieldDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveFieldDeclaration [in ResolveFieldDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve in import
 */
public void testImport() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveImport.java");
	IJavaElement[] elements = codeSelect(cu, "ImportedClass", "ImportedClass");
	assertElementsEqual(
		"Unexpected elements",
		"ImportedClass {key=La/b/ImportedClass;} [in ImportedClass.java [in a.b [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Tests code resolve on a class file without attached source.
 */
public void testInClassFileWithoutSource() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "p4.jar", "p4", "X.class");
	String selection = "Object";
	int start = 34;
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Tests code resolve on a class file with attached source.
 */
public void testInClassFileWithSource() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "p3.jar", "p3", "X.class");
	IJavaElement[] elements = codeSelect(cf, "Object", "Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25687
 */
public void testInnerClassAsParamater() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInnerClassAsParamater.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i)", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(Inner) {key=LResolveInnerClassAsParamater;.foo(LResolveInnerClassAsParamater$Inner;)V} [in ResolveInnerClassAsParamater [in ResolveInnerClassAsParamater.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the interface "Y"
 */
public void testInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInterface.java");
	IJavaElement[] elements = codeSelect(cu, "Y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=Lp1/Y;} [in Y.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Attempt to resolve outside of the range of the compilation unit.
 */
public void testInvalidResolve() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "p1", "X.java");
	try {
		cu.codeSelect(-1, 10); 
	} catch (JavaModelException e) {
		return;
	}
	assertTrue("Exception should have been thrown for out of bounds resolution", false);
}
/**
 * Resolve the local class 'Y' (field type).
 */
public void testLocalClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass1.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass1$73;} [in foo() [in ResolveLocalClass1 [in ResolveLocalClass1.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (local variable type).
 */
public void testLocalClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass2.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass2$80;} [in foo() [in ResolveLocalClass2 [in ResolveLocalClass2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y'(array initializer type).
 */
public void testLocalClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass3.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]{", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass3$73;} [in foo() [in ResolveLocalClass3 [in ResolveLocalClass3.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (return type).
 */
public void testLocalClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass4.java");
	IJavaElement[] elements = codeSelect(cu, "Y bar()", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass4$73;} [in foo() [in ResolveLocalClass4 [in ResolveLocalClass4.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (method argument).
 */
public void testLocalClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass5.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass5$73;} [in foo() [in ResolveLocalClass5 [in ResolveLocalClass5.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'SuperClass' (super class).
 */
public void testLocalClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass6.java");
	IJavaElement[] elements = codeSelect(cu, "Y { // superclass", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y {key=LResolveLocalClass6$58;} [in foo() [in ResolveLocalClass6 [in ResolveLocalClass6.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalClass7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass7.java");
	IJavaElement[] elements = codeSelect(cu, "X var", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=LResolveLocalClass7$131;} [in <anonymous #2> [in foo2() [in ResolveLocalClass7 [in ResolveLocalClass7.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve a local constructor
 */
public void testLocalConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalConstructor.java");
	IJavaElement[] elements = codeSelect(cu, "Y(\"", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y(String) {key=LResolveLocalConstructor$63;.(Ljava/lang/String;)V} [in Y [in foo() [in ResolveLocalConstructor [in ResolveLocalConstructor.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local constructor declaration
 */
public void testLocalConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalConstructorDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "Y(i", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y(int) {key=LResolveLocalConstructorDeclaration$74;.(I)V} [in Y [in foo() [in ResolveLocalConstructorDeclaration [in ResolveLocalConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve the local field "fred"
 */
public void testLocalField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalField.java");
	IJavaElement[] elements = codeSelect(cu, "fred =", "fred");
	assertElementsEqual(
		"Unexpected elements",
		"fred {key=LResolveLocalField$57;.fred} [in Y [in foo() [in ResolveLocalField [in ResolveLocalField.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalField2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalField2.java");
	IJavaElement[] elements = codeSelect(cu, "var =", "var");
	assertElementsEqual(
		"Unexpected elements",
		"var {key=LResolveLocalField2$110;.var} [in <anonymous #2> [in foo2() [in ResolveLocalField2 [in ResolveLocalField2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local field declaration
 */
public void testLocalFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalFieldDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "fred", "fred");
	assertElementsEqual(
		"Unexpected elements",
		"fred {key=LResolveLocalFieldDeclaration$68;.fred} [in Y [in foo() [in ResolveLocalFieldDeclaration [in ResolveLocalFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local member type declaration
 */
public void testLocalMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMemberTypeDeclaration1.java");
	IJavaElement[] elements = codeSelect(cu, "Member {", "Member");
	assertElementsEqual(
		"Unexpected elements",
		"Member {key=LResolveLocalMemberTypeDeclaration1$88;} [in Y [in foo() [in ResolveLocalMemberTypeDeclaration1 [in ResolveLocalMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve member type declaration
 */
public void testLocalMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMemberTypeDeclaration2.java");
	IJavaElement[] elements = codeSelect(cu, "MemberOfMember", "MemberOfMember");
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember {key=LResolveLocalMemberTypeDeclaration2$108;} [in Member [in Y [in foo() [in ResolveLocalMemberTypeDeclaration2 [in ResolveLocalMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]]]",
		elements
	);
}
/**
 * Resolve the method "foo"
 */
public void testLocalMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(String) {key=LResolveLocalMethod$58;.foo(Ljava/lang/String;)V} [in Y [in bar() [in ResolveLocalMethod [in ResolveLocalMethod.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalMethod2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethod2.java");
	IJavaElement[] elements = codeSelect(cu, "bar();", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() {key=LResolveLocalMethod2$111;.bar()V} [in <anonymous #2> [in foo2() [in ResolveLocalMethod2 [in ResolveLocalMethod2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration
 */
public void testLocalMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethodDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) {key=LResolveLocalMethodDeclaration$69;.foo(I)V} [in Y [in bar() [in ResolveLocalMethodDeclaration [in ResolveLocalMethodDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name
 */
public void testLocalName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var1 = new Object();", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name with base type
 */
public void testLocalName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var2 = 1;", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var1.toString();", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var2++;", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var3.hashCode();", "var3");
	assertElementsEqual(
		"Unexpected elements",
		"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var3.toString();", "var3");
	assertElementsEqual(
		"Unexpected elements",
		"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var4;", "var4");
	assertElementsEqual(
		"Unexpected elements",
		"var4 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Resolve a local reference and ensure it returns true when asked isStructureKnown().
 * (regression test for bug 48422 Calling isStructureKnown() on ILocalVaraible throws JavaModelExceptions)
 */
public void testLocalVarIsStructureKnown() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var1 = new Object();", "var1");
	assertTrue(localVar.isStructureKnown());
}
/*
 * Resolve a local reference and ensure its type signature is correct.
 */
public void testLocalVarTypeSignature1() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var1 = new Object();", "var1");
	assertEquals(
		"Unexpected type signature",
		"QObject;",
		localVar.getTypeSignature());
}
/*
 * Resolve a local reference and ensure its type signature is correct.
 */
public void testLocalVarTypeSignature2() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var2 = 1;", "var2");
	assertEquals(
		"Unexpected type signature",
		"I",
		localVar.getTypeSignature());
}
/**
 * Resolve member type declaration
 */
public void testMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration1.java");
	IJavaElement[] elements = codeSelect(cu, "MemberInterface", "MemberInterface");
	assertElementsEqual(
		"Unexpected elements",
		"MemberInterface [in ResolveMemberTypeDeclaration1 [in ResolveMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve member type declaration located in default package
 */
public void testMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration2.java");
	IJavaElement[] elements = codeSelect(cu, "MemberOfMember", "MemberOfMember");
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember [in Member [in ResolveMemberTypeDeclaration2 [in ResolveMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Try to resolve message send on base type.
 */
public void testMessageSendOnBaseType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMessageSendOnBaseType.java");
	IJavaElement[] elements = codeSelect(cu, "hello", "hello");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the method "foo"
 */
public void testMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(String) {key=LResolveMethod;.foo(Ljava/lang/String;)V} [in ResolveMethod [in ResolveMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method declaration
 */
public void testMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in ResolveMethodDeclaration [in ResolveMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 45655 exception while editing java file)
 */
public void testMethodDeclarationInAnonymous() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous.java");
	IJavaElement[] elements = codeSelect(cu, "bar()", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() {key=LResolveMethodDeclarationInAnonymous$84;.bar()V} [in <anonymous #1> [in foo() [in ResolveMethodDeclarationInAnonymous [in ResolveMethodDeclarationInAnonymous.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 45786 No selection on method declaration in field initializer)
 */
public void testMethodDeclarationInAnonymous2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous2.java");
	IJavaElement[] elements = codeSelect(cu, "foo()", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo() {key=LResolveMethodDeclarationInAnonymous2$73;.foo()V} [in <anonymous #1> [in field [in ResolveMethodDeclarationInAnonymous2 [in ResolveMethodDeclarationInAnonymous2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 47795 NPE selecting method in anonymous 2 level deep)
 */
public void testMethodDeclarationInAnonymous3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous3.java");
	IJavaElement[] elements = codeSelect(cu, "selectMe(", "selectMe");
	assertElementsEqual(
		"Unexpected elements",
		"selectMe() {key=LResolveMethodDeclarationInAnonymous3$200;.selectMe()V} [in <anonymous #1> [in bar() [in <anonymous #1> [in foo() [in ResolveMethodDeclarationInAnonymous3 [in ResolveMethodDeclarationInAnonymous3.java [in <default> [in src [in Resolve]]]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 47214 Cannot open declaration on a selected method of an anonymous class)
 */
public void testMethodDeclarationInAnonymous4() throws JavaModelException {
	IClassFile classFile = getClassFile("Resolve", "test47214.jar", "p", "X.class");
	IJavaElement[] elements = codeSelect(classFile, "bar(", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() {key=Lp/X$60;.bar()V} [in <anonymous> [in X$1.class [in p [in test47214.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the method
 */
public void testMethodWithIncorrectParameter() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodWithIncorrectParameter.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"String", "foo");
	assertElementsEqual(
		"Unexpected elements",
			"foo(int) {key=LResolveMethodWithIncorrectParameter;.foo(I)V} [in ResolveMethodWithIncorrectParameter [in ResolveMethodWithIncorrectParameter.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method in inner type.
 */
public void testMethodWithInnerTypeInClassFile() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "test", "test");
	assertElementsEqual(
		"Unexpected elements",
		"test() [in MyClass [in MyClass.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * bug 33785
 */
public void testMethodWithInnerTypeInClassFile2() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass2$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "method", "method");
	assertElementsEqual(
		"Unexpected elements",
		"method(MyClass2.Inner[]) [in MyClass2 [in MyClass2.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
		
	IMethod method = (IMethod) elements[0];
	ISourceRange sourceRange = method.getSourceRange();
	String methodString = "void method(MyClass2.Inner[] arg){}";
	int o = cf.getSource().indexOf(methodString);
	int l = methodString.length();
	assertEquals("Unexpected offset", o, sourceRange.getOffset());
	assertEquals("Unexpected length", l, sourceRange.getLength());
}
/**
 * Tries to resolve the type "lang. \u0053tring" which doesn't exist.
 */
public void testNegativeResolveUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "lang.\\u0053tring", "lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the package "java.lang"
 */
public void testPackage() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePackage.java");
	IJavaElement[] elements = codeSelect(cu, "lang", "lang");
	assertElementsEqual(
		"Unexpected elements",
		"java.lang [in " + getExternalJCLPathString() + " [in Resolve]]",
		elements
	);
}
/**
 * Try to resolve the qualified type "lang.Object"
 */
public void testPartiallyQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePartiallyQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "lang.Object", "lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the qualified type "java.lang.Object"
 */
public void testQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.Object", "java.lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25888
 */
public void testStaticClassConstructor() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test25888.jar", "", "ResolveStaticClassConstructor.class");
	IJavaElement[] elements = codeSelect(cu, "StaticInnerClass();", "StaticInnerClass");
	assertElementsEqual(
		"Unexpected elements",
		"StaticInnerClass() {key=LResolveStaticClassConstructor$StaticInnerClass;.()V} [in StaticInnerClass [in ResolveStaticClassConstructor$StaticInnerClass.class [in <default> [in test25888.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve type declaration
 */
public void testTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "OtherType", "OtherType");
	assertElementsEqual(
		"Unexpected elements",
		"OtherType [in ResolveTypeDeclaration.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve type in comment.
 */
public void testTypeInComment() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeInComment.java");
	IJavaElement[] elements = codeSelect(cu, "X */", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X {key=Lp2/X;} [in X.java [in p2 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the type "java.lang. \u0053ring"
 */
public void testUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.\\u0053tring", "java.lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47177
 */
public void testLocalNameForClassFile() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test47177.jar", "", "ResolveLocalName.class");

	//Resolve a local declaration name
	IJavaElement[] elements = codeSelect(cu, "var1 = new Object();", "var1");
	assertElementsEqual(
			"Unexpected elements",
			"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local declaration name with base type
	elements = codeSelect(cu, "var2 = 1;", "var2");
	assertElementsEqual(
			"Unexpected elements",
			"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var1.toString();", "var1");
	assertElementsEqual(
			"Unexpected elements",
			"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var2++;", "var2");
	assertElementsEqual(
			"Unexpected elements",
			"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var3.hashCode();", "var3");
	assertElementsEqual(
			"Unexpected elements",
			"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var3.toString();", "var3");
	assertElementsEqual(
			"Unexpected elements",
			"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);
	
	// Resolve a local variable reference
	elements = codeSelect(cu, "var4;", "var4");
	assertElementsEqual(
			"Unexpected elements",
			"var4 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42365
 */
public void testMethodDeclarationInInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInInterface.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
			"Unexpected elements",
			"foo() {key=LQI;.foo()V} [in QI [in QI.class [in <default> [in jj.jar [in Resolve]]]]]",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=57414
 */
public void testEndOfFile() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEndOfFile.java");
	IJavaElement[] elements = codeSelectAt(cu, "zzz");
	assertElementsEqual(
			"Unexpected elements",
			"",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65259
 */
public void testDuplicateMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo() [in ResolveDuplicateMethodDeclaration [in ResolveDuplicateMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65259
 */
public void testDuplicateMethodDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo()#2 [in ResolveDuplicateMethodDeclaration [in ResolveDuplicateMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Object) [in ResolveDuplicateMethodDeclaration3 [in ResolveDuplicateMethodDeclaration3.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Object)#2 [in ResolveDuplicateMethodDeclaration3 [in ResolveDuplicateMethodDeclaration3.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration5.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in ResolveDuplicateMethodDeclaration5 [in ResolveDuplicateMethodDeclaration5.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration5.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in ResolveDuplicateMethodDeclaration5 [in ResolveDuplicateMethodDeclaration5.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration7.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner [in ResolveDuplicateMethodDeclaration7 [in ResolveDuplicateMethodDeclaration7.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration8() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration7.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner [in ResolveDuplicateMethodDeclaration7 [in ResolveDuplicateMethodDeclaration7.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration9() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration9.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo(/*1*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner [in ResolveDuplicateMethodDeclaration9 [in ResolveDuplicateMethodDeclaration9.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration10() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration9.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo(/*1*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner [in ResolveDuplicateMethodDeclaration9 [in ResolveDuplicateMethodDeclaration9.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration11() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration11.java");
	
	String str = cu.getSource();
	int start = str.indexOf("foo(/*2*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner#2 [in ResolveDuplicateMethodDeclaration11 [in ResolveDuplicateMethodDeclaration11.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration12() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration11.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo(/*2*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner#2 [in ResolveDuplicateMethodDeclaration11 [in ResolveDuplicateMethodDeclaration11.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration.java");
	
	String str = cu.getSource();
	int start = str.indexOf("var;/*1*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"var [in Inner [in ResolveDuplicateFieldDeclaration [in ResolveDuplicateFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("var;/*1*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"var#2 [in Inner [in ResolveDuplicateFieldDeclaration [in ResolveDuplicateFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.indexOf("var;/*2*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"var [in Inner#2 [in ResolveDuplicateFieldDeclaration3 [in ResolveDuplicateFieldDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("var;/*2*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"var#2 [in Inner#2 [in ResolveDuplicateFieldDeclaration3 [in ResolveDuplicateFieldDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration.java");
	
	String str = cu.getSource();
	int start = str.indexOf("Inner");
	int length = "Inner".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner [in ResolveDuplicateTypeDeclaration [in ResolveDuplicateTypeDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("Inner");
	int length = "Inner".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner#2 [in ResolveDuplicateTypeDeclaration [in ResolveDuplicateTypeDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.indexOf("Inner2/*1*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner2 [in Inner [in ResolveDuplicateTypeDeclaration3 [in ResolveDuplicateTypeDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration3.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("Inner2/*1*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner2#2 [in Inner [in ResolveDuplicateTypeDeclaration3 [in ResolveDuplicateTypeDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration5.java");
	
	String str = cu.getSource();
	int start = str.indexOf("Inner2/*2*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner2 [in Inner#2 [in ResolveDuplicateTypeDeclaration5 [in ResolveDuplicateTypeDeclaration5.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration5.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("Inner2/*2*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"Inner2#2 [in Inner#2 [in ResolveDuplicateTypeDeclaration5 [in ResolveDuplicateTypeDeclaration5.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testArrayParameterInsideParent1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayParameterInsideParent1.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("var");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"var [in test(int[]) [in ResolveArrayParameterInsideParent1 [in ResolveArrayParameterInsideParent1.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDeepLocalVariable() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDeepLocalVariable.java");
	
	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);
	
	assertElementsEqual(
			"Unexpected elements",
			"foo [in D9() [in D9 [in D8 [in D7 [in D6 [in D5 [in D4 [in D3 [in D2 [in D1 [in ResolveDeepLocalVariable [in ResolveDeepLocalVariable.java [in <default> [in src [in Resolve]]]]]]]]]]]]]]]",
			elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalVariable() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalVariable.java");
	IJavaElement[] elements = codeSelect(cu, "var =", "var");
	assertElementsEqual(
		"Unexpected elements",
		"var [in toto() [in <anonymous #2> [in foo2() [in ResolveLocalVariable [in ResolveLocalVariable.java [in <default> [in src [in Resolve]]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName1.java");
	
	IJavaElement[] elements = codeSelect(cu, "pp.qq.XX.YY.ZZ", "pp.qq.XX.YY.ZZ");
	
	assertElementsEqual(
		"Unexpected elements",
		"ZZ {key=Lpp/qq/XX$YY$ZZ;} [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName2.java");
	
	IJavaElement[] elements = codeSelect(cu, "qq.XX.YY.ZZ", "qq.XX.YY.ZZ");
	
	assertElementsEqual(
		"Unexpected elements",
		"ZZ {key=Lpp/qq/XX$YY$ZZ;} [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName3.java");
	
	IJavaElement[] elements = codeSelect(cu, "XX.YY.ZZ", "XX.YY.ZZ");
	
	assertElementsEqual(
		"Unexpected elements",
		"ZZ {key=Lpp/qq/XX$YY$ZZ;} [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName4.java");
	
	IJavaElement[] elements = codeSelect(cu, "YY.ZZ", "YY.ZZ");
	
	assertElementsEqual(
		"Unexpected elements",
		"ZZ {key=Lpp/qq/XX$YY$ZZ;} [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName5.java");
	
	IJavaElement[] elements = codeSelect(cu, "YY.ZZ", "YY.ZZ");
	
	assertElementsEqual(
		"Unexpected elements",
		"ZZ {key=LResolveQualifiedName5$XX$YY$ZZ;} [in YY [in XX [in ResolveQualifiedName5 [in ResolveQualifiedName5.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
}
