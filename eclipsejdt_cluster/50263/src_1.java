/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ASTConverter18Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS8);
	}

	public ASTConverter18Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter18Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	/*
	 * Type Annotations on Variable Arguments
	 */
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391898
	public void test0001() throws JavaModelException {
		String contents =
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"public class X {\n" +
			"	public void foo(int @Marker... args) {\n" +
			"	}\n" +
			" 	public void bar(@Marker int @Marker... args) {\n" +
			" 	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
		List annotations = parameter.varargsAnnotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		ASTNode annotation = (ASTNode) annotations.get(0);
		checkSourceRange(annotation, "@Marker", contents);
		node = getASTNode(compilationUnit,1,1);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		parameters = methodDeclaration.parameters();
		assertEquals("Wrong number of parameters", 1, parameters.size());
		parameter = (SingleVariableDeclaration) parameters.get(0);
		annotations = parameter.varargsAnnotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		annotation = (ASTNode) annotations.get(0);
		checkSourceRange(annotation, "@Marker", contents);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests annotations on
	 * QTR in multiple scenarios of occurrence.
	 * 
	 * @throws JavaModelException
	 */
	public void test0002() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0002/X.java",
				true/* resolve */);
		String contents = "package test0002;\n"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		Outer outer = new Outer();\n"
				+ "		Object myObject = new Object();\n"
				+ "		String myString;\n"
				+ "		myString = (java.lang.@Marker String) myObject;\n"
				+ "		Outer.Inner first = outer.new Inner();\n"
				+ "		Outer. @Marker2 Inner second = outer.new Inner() ;\n"
				+ "		Outer.Inner. @Marker1 Deeper deeper = second.new Deeper();\n"
				+ "		Outer.@Marker1 Inner.@Marker2 Deeper deeper2 =  second.new Deeper();\n"
				+ "	}\n" + "}\n" + "class Outer {\n"
				+ "	public class Inner {\n" + "		public class Deeper {\n"
				+ "		}\n" + "	}\n" + "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		int sCount = 3;

		// case 1 - annotation just before the last field
		ExpressionStatement expressionStatement = (ExpressionStatement) statements.get(sCount++);
		Assignment assignment = (Assignment) expressionStatement.getExpression();
		assertNotNull(assignment);
		CastExpression castExpression = (CastExpression) assignment.getRightHandSide();
		assertNotNull(castExpression);
		PackageQualifiedType packageQualifiedType = (PackageQualifiedType) castExpression.getType();
		assertNotNull(packageQualifiedType);
		assertEquals("java.lang.@Marker String", packageQualifiedType.toString());
		List annotations = packageQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker", annotations.get(0).toString());

		// case 2 - QualifiedType without annotations.
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 0);

		// case 3 - Qaulified Type with outer without annotations and inner with
		// annotations.
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertNotNull(qualifiedType);
		assertEquals("Outer.@Marker2 Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker2", annotations.get(0).toString());

		// case 4 - Multiple levels with annotations at the last only.
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertNotNull(qualifiedType);
		assertEquals("Outer.Inner.@Marker1 Deeper", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker1", annotations.get(0).toString());

		// case 5 - Multiple annotations
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertNotNull(qualifiedType);
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker2", annotations.get(0).toString());
		SimpleName simpleName = qualifiedType.getName();
		assertEquals("Deeper", simpleName.toString());
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("Outer.@Marker1 Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker1", annotations.get(0).toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests the
	 * representation of type annotations on a possible JAVA 7 and 8 place.
	 * 
	 * @throws JavaModelException
	 */
	public void test0003() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0003/X.java",
				true/* resolve */);
		String contents = "package test0003;\n"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		@Marker Outer.Inner first[] = new Outer.Inner[1];\n"
				+ "	}\n" + "}\n" + "class Outer {\n"
				+ "	public class Inner {\n" + "	}\n" + "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		int sCount = 0;

		// Current design expects annotation only at the JAVA 7 place if it is
		// expected at JAVA 8.
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount);
		List modifiers = variableDeclarationStatement.modifiers();
		assertTrue(modifiers.size() == 1);
		Annotation annotation = (Annotation) modifiers.get(0);
		assertEquals("@Marker", annotation.toString());
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner", simpleType.toString());
		List annotations = simpleType.annotations();
		assertTrue(annotations.size() == 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests QTR with
	 * annotations
	 * 
	 * @throws JavaModelException
	 */
	public void test0004() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0004/X.java",
				true/* resolve */);
		String contents = "package test0004;"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X implements One</*start*/@Marker1 Outer<Integer>. @Marker2 Inner<Double>[]/*end*/> {\n"
				+ "}\n" + "interface One<T> {}\n" + "class Outer<T> {\n"
				+ "	public class Inner<S> {}\n" + "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		ArrayType type = (ArrayType) ((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(0);
		assertNotNull("No annotation", type);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>[]", binding.getQualifiedName());
		Type componentType = type.getComponentType();
		binding = componentType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name",
				"test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		assertTrue("Not parameterized", componentType.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) componentType;
		Type type2 = parameterizedType.getType();
		assertTrue("Not qualified", type2.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type2;
		binding = qualifiedType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name","test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		Type qualifier = qualifiedType.getQualifier();
		assertTrue("Not parameterized", qualifier.isParameterizedType());
		binding = qualifier.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0004.Outer<java.lang.Integer>", binding.getQualifiedName());
		parameterizedType = (ParameterizedType) qualifier;
		type2 = parameterizedType.getType();
		assertTrue("Not simple type", type2.isSimpleType());
		binding = type2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name","test0004.Outer<java.lang.Integer>", binding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests QTR with
	 * annotations
	 * 
	 * @throws JavaModelException
	 */
	public void test0005() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0005/X.java",
				true/* resolve */);
		String contents = "package test0005;"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X implements One< Outer.Inner > {\n"
				+ "}\n"
				+ "class Y implements One< Outer. @Marker1 Inner > {\n"
				+ "}\n"
				+ "class Z implements One< @Marker1 Outer.Inner > {\n"
				+ "}\n"
				+ "class W implements One< @Marker1 Outer. @Marker2 Inner > {\n"
				+ "}\n" + "interface One<T> {}\n" + "class Outer {\n"
				+ "	public class Inner {}\n" + "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		int tCount = 0;

		// case 1 - no annotations Outer.Inner
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		ParameterizedType parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		List typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		Type type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		assertEquals("Outer.Inner", type.toString());

		// case 2 - QTR with one annotation Outer.@Marker1 Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration
				.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertEquals("Outer.@Marker1 Inner", qualifiedType.toString());
		List annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 3 - QTR with one annotation at the beginning @Marker1
		// Outer.Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isQualifiedType());
		assertEquals("@Marker1 Outer.Inner", type.toString());
		qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		SimpleType simpleType = (SimpleType) qualifierType;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 4 - QTR with annotations at both the types @Marker1
		// Outer.@Marker2 Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isQualifiedType());
		assertEquals("@Marker1 Outer.@Marker2 Inner", type.toString());
		qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		simpleType = (SimpleType) qualifierType;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests PQTR with
	 * annotations part
	 * 
	 * @throws JavaModelException
	 */
	public void test0006() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0006/X.java",
				true);
		String contents = "package test0006;"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X implements One<Outer.  Inner.Deeper<Double>> {\n"
				+ "}\n"
				+ "class X1 implements One<Outer. @Marker1 Inner.Deeper<Double>> {\n"
				+ "}\n"
				+ "class X2 implements One<Outer. @Marker1 Inner.@Marker2 Deeper<Double>> {\n"
				+ "}\n"
				+ "class X3 implements One<@Marker1 Outer. @Marker2 Inner. Deeper<Double>> {\n"
				+ "}\n"
				+ "class Y implements One<Outer1. Inner<Integer>. Deeper<Double>> {\n"
				+ "}\n"
				+ "class Y1 implements One<Outer1. Inner<Integer>. @Marker1 Deeper<Double>> {\n"
				+ "}\n"
				+ "class Y2 implements One<Outer1. @Marker1 Inner<Integer>. Deeper<Double>> {\n"
				+ "}\n"
				+ "class Y3 implements One<@Marker1 Outer1. Inner<Integer>. Deeper<Double>> {\n"
				+ "}\n"
				+ "class Y4 implements One<@Marker1 Outer1. @Marker2 Inner<Integer>. Deeper<Double>> {\n"
				+ "}\n"
				+ "class Z implements One<Outer2<Integer>.Inner.Deeper<Double>> {\n"
				+ "}\n"
				+ "class Z1 implements One<@Marker1 Outer2<Integer>.Inner.Deeper<Double>> {\n"
				+ "}\n"
				+ "class Z2 implements One<Outer2<Integer>. @Marker1 Inner.@Marker2 Deeper<Double>> {\n"
				+ "}\n"
				+ "class W implements One<Outer3<Double>. @Marker1 @Marker2 Inner<Integer, Character>. Deeper<Double>> {\n"
				+ "}\n" + "interface One<T> {}\n" + "class Outer {\n"
				+ "	public class Inner {\n"
				+ "       public class Deeper<S> {\n" + "       }\n" + "   }\n"
				+ "}\n" + "class Outer1 {\n" + "	public class Inner<T> {\n"
				+ "       public class Deeper<S> {\n" + "       }\n" + "   }\n"
				+ "}\n" + "class Outer2 <T> {\n" + "	public class Inner {\n"
				+ "       public class Deeper<S> {}\n" + "   }\n" + "}\n"
				+ "class Outer3 <T> {\n" + "	public class Inner<K, V> {\n"
				+ "       public class Deeper<S> {}\n" + "   }\n" + "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		int tCount = 0;

		// case 1: vanilla case without annotations and with single typeArgument
		// Outer.Inner.Deeper<Double>
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		ParameterizedType parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		List typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		Type type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner.Deeper", simpleType.toString());
		Name name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		assertEquals("Outer.Inner.Deeper", qualifiedName.toString());

		// case 2 - One annotation after the first class
		// Outer. @Marker1 Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.@Marker1 Inner.Deeper<Double>",	parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertEquals("Outer.@Marker1 Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		List annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		simpleType = (SimpleType) qualifierType;
		assertEquals("Outer.@Marker1 Inner", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 3 - Inner types annotated with outer not annotated with last
		// type arg
		// Outer. @Marker1 Inner.@Marker2 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		simpleType = (SimpleType) qualifierType;
		assertEquals("Outer.@Marker1 Inner", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 4 - one annotation on the outermost, one in middle and one
		// typearg in innermost
		// @Marker1 Outer. @Marker2 Inner. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration
				.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer.@Marker2 Inner.Deeper<Double>", parametrizedType.toString());
		ITypeBinding typeBinding = parametrizedType.resolveBinding();
		assertNotNull("Binding non-null", typeBinding);
		assertEquals("wrong qualified name", "test0006.Outer.Inner.Deeper<java.lang.Double>", typeBinding.getQualifiedName());
		assertTrue("Not a Parameterized Type", typeBinding.isParameterizedType());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer.@Marker2 Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("@Marker1 Outer.@Marker2 Inner", qualifierType.toString());
		typeBinding = qualifiedType.resolveBinding();
		assertNotNull("Binding non-null", typeBinding);
		typeBinding = qualifiedType.resolveBinding();
		assertEquals("wrong qualified name", "test0006.Outer.Inner", typeBinding.getQualifiedName());
		assertTrue(qualifierType.isAnnotatable());
		AnnotatableType annotatableType = (AnnotatableType) qualifierType;
		annotations = annotatableType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 5 - without annotations, but with typeargs at second and third
		// types
		// Outer1. Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.Inner<Integer>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		assertEquals("Outer1.Inner", qualifiedName.toString());

		// case 6 - Annot in between two PQRT with outermost neither annotated
		// nor having typeargs
		// Outer1. Inner<Integer>. @Marker1 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.Inner<Integer>.@Marker1 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.Inner<Integer>.@Marker1 Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		assertEquals("Outer1.Inner", qualifiedName.toString());

		// case 7 - Outermost still empty (no annotations, no type args),
		// followed by annotation, and then typeargs
		// Outer1. @Marker1 Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.@Marker1 Inner<Integer>.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.@Marker1 Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.@Marker1 Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		assertEquals("Outer1.@Marker1 Inner", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 8 - Similar to above, but with the major difference of
		// annotation shifted to outermost.
		// @Marker1 Outer1. Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer1.Inner<Integer>.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("@Marker1 Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		name = qualifiedType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		name = simpleType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Outer1", name.toString());

		// case 9: scenario of the above case plus another annotation at
		// mid-level.
		// @Marker1 Outer1.@Marker2 Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.@Marker2 Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		name = qualifiedType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		name = simpleType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Outer1", name.toString());

		// case 10 - PQRT with two type args but without annotations
		// Outer2<Integer>.Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer2<Integer>.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer2<Integer>.Inner.Deeper", qualifiedType.toString());
		ITypeBinding binding = qualifiedType.resolveBinding();
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		binding = qualifiedType.resolveBinding();
		assertEquals("Outer2<Integer>.Inner", qualifiedType.toString());
		assertEquals("wrong qualified binding", "test0006.Outer2<java.lang.Integer>.Inner",	binding.getQualifiedName());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("Outer2", type.toString());

		// case 11 - annotation at outermost in addition to scenario in case 10.
		// @Marker1 Outer2<Integer>.Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer2<Integer>.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer2<Integer>.Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("@Marker1 Outer2<Integer>.Inner", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("@Marker1 Outer2", type.toString());
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 12 - No annotations at outermost, but outermost has
		// typeAnnotations.
		// Outer2<Integer>. @Marker1 Inner.@Marker2 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer2<Integer>.@Marker1 Inner.@Marker2 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer2<Integer>.@Marker1 Inner.@Marker2 Deeper",qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("Outer2<Integer>.@Marker1 Inner", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("Outer2", type.toString());
		assertTrue(type.isSimpleType());

		// case 13 - a list of annotations and multiple typeArgument element
		// lists.
		// Outer3<Double>. @Marker1 @Marker2 Inner<Integer, Character>.
		// Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer3<Double>.@Marker1 @Marker2 Inner<Integer,Character>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer3<Double>.@Marker1 @Marker2 Inner<Integer,Character>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 2);
		assertEquals("Integer", typeArguments.get(0).toString());
		assertEquals("Character", typeArguments.get(1).toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 2);
		assertEquals("@Marker1", annotations.get(0).toString());
		assertEquals("@Marker2", annotations.get(1).toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		assertEquals("Double", typeArguments.get(0).toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		assertEquals("Outer3", simpleType.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391893
	public void test0007() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(@Marker @Marker2 X this, @Marker2 @Marker int i){}\n" +
			"}\n" +
			"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker {}\n" +
			"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker2 {}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		AnnotatableType receiver = method.getReceiverType();
		assertEquals("Not an annotatable type", ASTNode.SIMPLE_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver signature", "@Marker @Marker2 X", ((SimpleType) receiver).toString());
		assertEquals("Incorrect annotations on receiver", 2, ((SimpleType) receiver).annotations().size());
		assertNull("Incorrect receiver qualfier", method.getReceiverQualifier());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391893
	public void test0008() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	class Y {\n" +
			"		public Y(@Marker @Marker2 X X.this, @Marker2 @Marker int i){}\n" +
			"	}\n" +
			"}\n" +
			"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker {}\n" +
			"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker2 {}";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration innerType = (TypeDeclaration) node;
		assertEquals("Incorrect no of methods", 1, innerType.getMethods().length);
		MethodDeclaration method = innerType.getMethods()[0];
		AnnotatableType receiver = method.getReceiverType();
		assertEquals("Not an annotatable type", ASTNode.SIMPLE_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver signature", "@Marker @Marker2 X", ((SimpleType) receiver).toString());
		assertEquals("Incorrect annotations on receiver", 2, ((SimpleType) receiver).annotations().size());
		assertNotNull("Incorrect receiver qualfier", method.getReceiverQualifier());
		assertEquals("Incorrect receiver qualfier", "X", method.getReceiverQualifier().getFullyQualifiedName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391895
	public void test0009() throws JavaModelException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
						"public class X {\n" +
						" 	class Y {\n" +
						"		@Annot int @Annot1 [] a @Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 [], b @Annot2 @Annot3 [] @Annot4 [], c [][][];\n" +  
						"		public void foo1(@Annot int @Annot1 [] p @Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 @Annot3 []) {}\n" +
						"		public void foo2(@Annot int p [][]) {}\n" +
						"		@Annot String @Annot1 [] foo3() @Annot1 @Annot2 [][] { return null; }\n" + 
						"	}\n" +
						"}\n" +
						"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" + 
						"@interface Annot {}\n" +
						"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" + 
						"@interface Annot1 {}\n" +
						"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" +
						"@interface Annot2 {}\n" +
						"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" + 
						"@interface Annot3 {}\n" +
						"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" +
						"@interface Annot4 {}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration) node;
		FieldDeclaration field = type.getFields()[0];
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 3, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "@Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 []");
		fragment = (VariableDeclarationFragment) fragments.get(1);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "@Annot2 @Annot3 [] @Annot4 []");		
		fragment = (VariableDeclarationFragment) fragments.get(2);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "[] [] []");
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Incorrect no of methods", 3, methods.length);
		MethodDeclaration method = methods[0];
		List parameters = method.parameters();
		assertEquals("Incorrect no of parameters", 1, parameters.size());
		assertExtraDimensionsEqual("Incorrect extra dimensions", ((SingleVariableDeclaration) parameters.get(0)).extraDimensions(), "@Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 @Annot3 []");

		method = methods[1];
		parameters = method.parameters();
		assertEquals("Incorrect no of parameters", 1, parameters.size());
		assertExtraDimensionsEqual("Incorrect extra dimensions", ((SingleVariableDeclaration) parameters.get(0)).extraDimensions(), "[] []");

		method = methods[2];
		assertExtraDimensionsEqual("Incorrect extra dimensions", method.extraDimensions(), "@Annot1 @Annot2 [] []");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399600
	public void test0010() throws JavaModelException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
						"public class X {\n" +
						"	@Marker int foo(@Marker(\"Blah\") int z) @Marker [] @Marker [] {\n" +
						"		return null;\n" +
						"	}\n" +
						"}\n" +
						"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
						"@interface Marker {\n" +
						"	String value() default \"Blah\";\n" +
						"}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertExtraDimensionsEqual("Incorrect extra dimensions", method.extraDimensions(), "@Marker [] @Marker []");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	public void test0011() throws JavaModelException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"public class X {\n" +
				" 	public void foo() {\n" +
				"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker2 @Marker3 [bar()] @Marker3 @Marker []; \n" +  
				"		int @Marker [][][] j = new @Marker int @Marker3 @Marker [2] @Marker @Marker2 [X.bar2(2)] @Marker2 @Marker3 [];\n" +
				"	}\n" +
				"	public int bar() {\n" +
				"		return 2;\n" +
				"	}\n" +
				"	public static int bar2(int k) {\n" +
				"		return k;\n" +
				"	}\n" +
				"}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker3 {}";
		
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 2, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		VariableDeclarationStatement statement2 = (VariableDeclarationStatement) list.get(1);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		checkSourceRange(type, "@Marker2 int @Marker @Marker2 [2] @Marker2 @Marker3 [bar()] @Marker3 @Marker []", contents.toCharArray());
		assertEquals("Incorrect annotations", "@Marker3 @Marker ", convertAnnotationsList(((ArrayType) type).annotations()));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Incorrect annotations", "@Marker2 @Marker3 ", convertAnnotationsList(((ArrayType) type).annotations()));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Incorrect annotations", "@Marker @Marker2 ", convertAnnotationsList(((ArrayType) type).annotations()));
		List dimensions = creation.dimensions();
		assertEquals("Incorrect expressions", 2, dimensions.size());
		assertEquals("Incorrect expressions", "2", dimensions.get(0).toString());
		assertEquals("Incorrect expressions", "bar()", dimensions.get(1).toString());
		
		list = statement2.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		fragment = (VariableDeclarationFragment) list.get(0);
		creation = (ArrayCreation) fragment.getInitializer();
		checkSourceRange(creation.getType(), "@Marker int @Marker3 @Marker [2] @Marker @Marker2 [X.bar2(2)] @Marker2 @Marker3 []", contents.toCharArray());
		
		type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Incorrect annotations", "@Marker2 @Marker3 ", convertAnnotationsList(((ArrayType) type).annotations()));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Incorrect annotations", "@Marker @Marker2 ", convertAnnotationsList(((ArrayType) type).annotations()));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Incorrect annotations", "@Marker3 @Marker ", convertAnnotationsList(((ArrayType) type).annotations()));
		dimensions = creation.dimensions();
		assertEquals("Incorrect expressions", 2, dimensions.size());
		assertEquals("Incorrect expressions", "2", dimensions.get(0).toString());
		assertEquals("Incorrect expressions", "X.bar2(2)", dimensions.get(1).toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	public void test0012() throws JavaModelException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"public class X {\n" +
				" 	public void foo() {\n" +
				"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker [] {{{1, 2, 3}}}; \n" +  
				"	}\n" +
				"}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker3 {}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 1, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		checkSourceRange(type, "@Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker []", contents.toCharArray());
		ArrayInitializer initializer = creation.getInitializer();
		checkSourceRange(initializer, "{{{1, 2, 3}}}", contents.toCharArray());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	// Force to use JLS4 and confirm malformed flags are set.
	public void test0021() throws JavaModelException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"public class X {\n" +
				" 	public void foo() {\n" +
				"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker [] {{{1, 2, 3}}}; \n" +  
				"	}\n" +
				"}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" + 
				"@interface Marker3 {}";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		CompilationUnit unit = (CompilationUnit) buildAST(getJLS4(), contents, this.workingCopy, true, true, true);
		
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 1, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (type.getFlags() & ASTNode.MALFORMED));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (type.getFlags() & ASTNode.MALFORMED));
		type = ((ArrayType) type).getComponentType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (type.getFlags() & ASTNode.MALFORMED));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 * 
	 * @throws JavaModelException
	 */
	public void test0013() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0010/X.java",
				true/* resolve */);
		String contents = "package test0010;"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X implements One<@Marker1 Integer, @Marker2 Boolean> {\n"
				+ "}\n" 		
				+ "class Y implements One<@Marker1 @Marker2 Integer, @Marker2 @Marker1 Double> {\n"
				+ "}\n" 		
				+ "interface One<T, U> {}\n" 
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		// simple types for generic type arguments to parameterized classes
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		Type type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(0);
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		List annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 1, annotations.size());
		assertEquals("@Marker1", annotations.get(0).toString());
		assertNotNull("No annotation", type);
		typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(0);
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker2", annotations.get(1).toString());
		assertNotNull("No annotation", type);	
		type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(1);
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker1", annotations.get(1).toString());
		assertNotNull("No annotation", type);	
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 * 
	 * @throws JavaModelException
	 */
	public void test0014() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0011/X.java",
				true/* resolve */);
		String contents = "package test0011;"
				+ "import java.lang.annotation.Target;\n"
				+ "public class X {\n"
				+ "	public void foo() {\n"
				+ " 	Y y = new <@Marker2 @Marker1 String> Y(new String(\"Hello\"));\n"
				+ " 	len = y.<@Marker1 @Marker2 String> bar(new String(\"World\"));\n"
				+ " }\n"
				+ "	public int len;\n"
				+ "}\n" 		
				+ "class Y {\n"
				+ "	public <T> Y(T t) {\n"
				+ "		len = t instanceof String ? ((String)t).length() : 0;\n"
				+ "	}\n"
				+ "	public <T> int bar(T t) {\n"
				+ "		return t instanceof String ? ((String)t).length() : len;\n"
				+ "	}\n"
				+ " private int len;\n"
				+ "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		// simple tye for generic type arguments in a generic method or constructor invocation
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		Statement statement = (Statement)statements.get(0);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		Type type = (Type) classInstanceCreation.typeArguments().get(0);
		assertEquals("@Marker2 @Marker1 String", type.toString());
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		List annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker2", annotations.get(0).toString());
		statement = (Statement) statements.get(1);
		Assignment assignment  = (Assignment) ((ExpressionStatement)statement).getExpression();
		expression = assignment.getRightHandSide();
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		simpleType = (SimpleType)methodInvocation.typeArguments().get(0);
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker1", annotations.get(0).toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 * 
	 * @throws JavaModelException
	 */
	public void test0015() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0012/X.java",
				true/* resolve */);
		String contents = "package test0012;"
				+ "import java.lang.annotation.Target;\n"
				+ "import java.io.File;\n"
				+ "public class X <@Marker1 @Marker3 F extends @Marker1 @Marker2 File> {\n"
				+ "	public int foo(F f) {\n"
				+ " 	Y <@Marker2 @Marker3 ? super @Marker1 @Marker2 File> y = new @Marker2 @Marker1 Y<File>();\n"
				+ "		Outer o = new @Marker1 @Marker2 Outer();\n"	
				+ "		Outer.Inner inner = o.new @Marker1 @Marker2 Inner();\n"	
				+  " 	ZZ zz = new <String> @Marker1 @Marker2 ZZ();\n" 
				+ " 	return f.getName().length() + y.hashCode() + inner.hashCode();\n"
				+ " }\n"
				+ "}\n" 		
				+ "class Y<@Marker3 T> {\n"
				+ "	public int bar(T t) {\n"
				+ "		return t instanceof @Marker1 @Marker2 File ? t.toString().length() : 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "class Outer {\n"
				+ "	public class Inner {\n" 
				+ "		public class Deeper {\n"
				+ "		}\n" 
				+ "	}\n"
				+ "}\n"
				+ "class ZZ {\n"
				+ "public @Marker1 @Marker2 <T> ZZ() {\n"
				+ "	T t = null;\n"
				+ "	len =  t instanceof String ? t.hashCode() : 0;\n"
				+ "}\n"
				+ "public @Marker1 int  getint(@Marker2 @Marker1 ZZ this) {return len;}\n"
				+ "public int len;\n"
				+ "}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker1 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker2 {}\n"
				+ "@Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n"
				+ "@interface Marker3 {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		TypeParameter typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);

		// TypeParameter with TYPE_USE and TYPE_PARAMETER annotation combination.
		assertEquals("@Marker1 @Marker3 F extends @Marker1 @Marker2 File", typeParameter.toString());
		assertTrue(typeParameter.annotations().size() == 2);
		Annotation annotation = (Annotation)typeParameter.annotations().get(1);
		assertEquals("@Marker3", annotation.toString());
		IAnnotationBinding abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// simpletype for type parameter bounds
		SimpleType simpleType = (SimpleType) typeParameter.typeBounds().get(0);
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		Statement statement = (Statement)statements.get(0);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isParameterizedType());
		type = (Type)((ParameterizedType)type).typeArguments().get(0);
		assertTrue(type.isWildcardType());
		
		// for constructor invocation results 1/4
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("new @Marker2 @Marker1 Y<File>()", expression.toString());
		simpleType = (SimpleType) ((ParameterizedType)((ClassInstanceCreation)expression).getType()).getType();
		assertEquals("@Marker2 @Marker1 Y", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker1", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker1()", abinding.toString());
		
		// for constructor invocation results 2/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(1);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("new @Marker1 @Marker2 Outer()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 Outer", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());
		
		// for constructor invocation results 3/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(2);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("o.new @Marker1 @Marker2 Inner()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 Inner", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// for constructor invocation results 4/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(3);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("new <String>@Marker1 @Marker2 ZZ()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 ZZ", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// annotations on wildcardtypes with TYPE_USE and TYPE_PARAMETER combination.
		WildcardType wildCardType = (WildcardType) type;
		assertEquals("@Marker2 @Marker3 ? super @Marker1 @Marker2 File", wildCardType.toString());
		assertTrue(wildCardType.annotations().size() == 2);
		annotation = (Annotation) wildCardType.annotations().get(1);
		assertEquals("@Marker3", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// simpleType for  wildcard bounds
		simpleType = (SimpleType) wildCardType.getBound();
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// class declaration with TYPE_PARAMETER annotation
		typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);
		assertEquals("@Marker3 T", typeParameter.toString());
		assertTrue(typeParameter.annotations().size() == 1);
		annotation = (Annotation)typeParameter.annotations().get(0);
		assertEquals("@Marker3", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// for type tests
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 1, 0);
		statements = methodDeclaration.getBody().statements();
		statement = (Statement)statements.get(0);
		ConditionalExpression conditionalExpression = (ConditionalExpression)((ReturnStatement)statement).getExpression();
		simpleType = (SimpleType) ((InstanceofExpression)conditionalExpression.getExpression()).getRightOperand();
		assertEquals("@Marker1 @Marker2 File", simpleType.toString());
		assertTrue(simpleType.annotations().size() == 2);
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());

		// type annotation in front of a constructor declaration
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 3, 0);
		annotation = (Annotation) methodDeclaration.modifiers().get(2);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// type annotation on "this"
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 3, 1);
		simpleType = (SimpleType) methodDeclaration.getReceiverType();
		assertEquals("@Marker2 @Marker1 ZZ", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker1", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker1()", abinding.toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 * 
	 * @throws JavaModelException
	 */
	public void test399793a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = "package test399793;"
				+ "interface I {\n"
				+ "	int foo(int x);\n"
				+ "}\n" 
				+ "public class X {\n"
				+ " I i =  vlambda -> {return 200;};\n"
				+"}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("vlambda -> {\n  return 200;\n}\n", lambdaExpression.toString());
		assertTrue(lambdaExpression.parameters().size() == 1);
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("private static int lambda$0(int) ", binding.toString());
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof VariableDeclarationFragment);
		fragment = (VariableDeclarationFragment)variableDeclaration;
		assertEquals("vlambda", fragment.toString());		
		IVariableBinding variableBinding = fragment.resolveBinding();		
		ITypeBinding typeBinding = variableBinding.getType();
		assertNotNull("Null Binding for lambda argument", typeBinding);
		assertEquals("binding of int expected for lambda","int",typeBinding.getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 * 
	 * @throws JavaModelException
	 */
	public void test399793b() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = "package test399793;"
				+ "interface I {\n"
				+ "	int foo(int x);\n"
				+ "}\n" 
				+ "public class X {\n"
				+ " I i =  vlambda -> 200;\n"
				+"}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("vlambda -> 200", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("private static int lambda$0(int) ", binding.toString());
		assertTrue(lambdaExpression.parameters().size() == 1);
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof VariableDeclarationFragment);
		fragment = (VariableDeclarationFragment)variableDeclaration;
		assertEquals("vlambda", fragment.toString());		
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 * 
	 * @throws JavaModelException
	 */
	public void test399793c() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = "package test399793;"
				+ "interface I {\n"
				+ "	Object foo(int [] ia);\n"
				+ "}\n" 
				+ "public class X {\n"
				+ " I i = (int [] ia) ->{\n"
				+ "  	return ia.clone();"
				+ "};\n"
				+"}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("(int[] ia) -> {\n  return ia.clone();\n}\n", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("private static java.lang.Object lambda$0(int[]) ", binding.toString());
		assertTrue(lambdaExpression.parameters().size() == 1);
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof SingleVariableDeclaration);
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)variableDeclaration;
		assertEquals("int[] ia", singleVariableDeclaration.toString());		
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 * 
	 * @throws JavaModelException
	 */
	public void test399793d() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = "package test399793;" +
				"interface I {\n" +
				"	void doit();\n" +
				"}\n" +
				"public class X {\n" +
				"		I i = () -> {\n" +
				"			System.out.println(this);\n" +
				"			I j = () -> {\n" +
				"				System.out.println(this);\n" +
				"				I k = () -> {\n" +
				"					System.out.println(this);\n" +
				"				};\n" +
				"			};\n" +
				"		};\n" +
				"	}\n"; 
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertEquals("test399793.I i", variableBinding.toString());
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("() -> {\n  System.out.println(this);\n  I j=() -> {\n    System.out.println(this);\n    I k=() -> {\n      System.out.println(this);\n    }\n;\n  }\n;\n}\n", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("private void lambda$0() ", binding.toString());
		assertTrue(lambdaExpression.parameters().size() == 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399794
	 * ReferenceExpression Family Tests
	 * 
	 * @throws JavaModelException
	 */
	public void test399794() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399794/X.java",
				true/* resolve */);
		String contents = "package test399794;" +
				"import java.lang.annotation.*;\n " +
				"interface I {\n" +
				"    Object copy(int [] ia);\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"class XX {\n" +
				"	public  void foo(int x) {}\n" +
				"}\n" +
				"\n" +
				"class Y {\n" +
				"       static class Z {\n" +
				"               public static void foo(int x) {\n" +
				"                       System.out.print(x);\n" +
				"               }\n" +
				"       }\n" +
				"       public void foo(int x) {\n" +
				"               System.out.print(x);\n" +
				"       }\n" +
				"		public <T> void foo(T t){t.hashCode();}\n" +
				"}\n" +
				"\n" +
				"public class X extends XX {\n" +
				"       @SuppressWarnings(\"unused\")\n" +
				"       public  void bar(String [] args) {\n" +
				"                Y y = new Y();\n" +
				"                I i = @Marker int []::<String>clone;\n" +
				"                J j = Y.@Marker Z  :: foo;\n" +
				"                J j1 = Y.@Marker Z  :: <String> foo;\n" +
				"                J jdash = @Marker W<@Marker Integer> :: <String> new ;\n" +
				"                J jj = y :: foo;\n" +
				"                J jx = super ::  foo;\n" +
				"		 	     class Z {\n" +
				"					void foo() {\n" +
				"						J jz = X.super :: foo;\n" +
		    	"					}\n" +
				"				}\n" +		
				"       }\n" +
				"       public static void main (String [] args) {}\n" +
				"}\n" +
				"class W<T> extends Y {\n" +
				"       public W(T x) {}\n" +
				"}\n" +
				"\n" +
				"@Target (ElementType.TYPE_USE)\n" +
				"@interface Marker {}";
			
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 4);
		MethodDeclaration method = typeDeclaration.getMethods()[0];
		List statements = method.getBody().statements();
		assertTrue(statements.size() == 8);
		int fCount = 1;
		
		// type method reference with primitive type with type arguments
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(fCount++);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		TypeMethodReference typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "@Marker int []::<String>clone", contents);
		ITypeBinding typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		IMethodBinding methodBinding = typeMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		Type type = typeMethodReference.getType();
		checkSourceRange(type, "@Marker int []", contents);
		List typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		checkSourceRange(type, "String", contents);
		assertTrue(type.isSimpleType());
		SimpleName name = typeMethodReference.getName();
		checkSourceRange(name, "clone", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// type method reference with qualified type without type arguments
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "Y.@Marker Z  :: foo", contents);
		typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = typeMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		type = typeMethodReference.getType();
		assertTrue(type.isQualifiedType());
		checkSourceRange(type, "Y.@Marker Z", contents);
		typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = typeMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// type method reference with qualified type with type arguments
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "Y.@Marker Z  :: <String> foo", contents);
		typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = typeMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		type = typeMethodReference.getType();
		assertTrue(type.isQualifiedType());
		checkSourceRange(type, "Y.@Marker Z", contents);
		typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		checkSourceRange(type, "String", contents);
		name = typeMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// creation method reference
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof CreationReference);
		CreationReference creationReference = (CreationReference) expression;
		checkSourceRange(creationReference, "@Marker W<@Marker Integer> :: <String> new ", contents);
		typeBinding = creationReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = creationReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		Expression lhs = creationReference.getExpression();
		checkSourceRange(lhs, "@Marker W<@Marker Integer> ", contents);
		typeArguments = creationReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		checkSourceRange(type, "String", contents);

		// expression method reference
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof ExpressionMethodReference);
		ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference) expression;
		checkSourceRange(expressionMethodReference, "y :: foo", contents);
		typeBinding = expressionMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = expressionMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		lhs = expressionMethodReference.getExpression();
		checkSourceRange(lhs, "y", contents);
		typeArguments = expressionMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = expressionMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// super method reference without qualifier
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof SuperMethodReference);
		SuperMethodReference superMethodReference = (SuperMethodReference) expression;
		checkSourceRange(superMethodReference, "super ::  foo", contents);
		typeBinding = superMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = superMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		assertNull(superMethodReference.getQualifier());		
		typeArguments = superMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = superMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// super method reference with qualifier
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) statements.get(fCount);
		typeDeclaration = (TypeDeclaration) typeDeclarationStatement.getDeclaration();
		method = typeDeclaration.getMethods()[0];
		statements = method.getBody().statements();
		assertTrue(statements.size() == 1);
		statement = (VariableDeclarationStatement) statements.get(0);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof SuperMethodReference);
		superMethodReference = (SuperMethodReference) expression;
		checkSourceRange(superMethodReference, "X.super :: foo", contents);
		typeBinding = superMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = superMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		name = (SimpleName) superMethodReference.getQualifier();
		checkSourceRange(name, "X", contents);		
		typeArguments = superMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = superMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);
	
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 * 
	 * @throws JavaModelException
	 */
	public void test399793e() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = "package test399793;" +
				"interface I {\n" +
				"  J foo();\n" +
				"}\n" +
				"interface J {\n" +
				"  int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"    I I = () -> () -> 10;\n" +
				"}\n";
			
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 2);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("() -> () -> 10", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("private static test399793.J lambda$0() ", binding.toString());
		assertTrue(lambdaExpression.parameters().size() == 0);
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402665
	 * 
	 * @throws JavaModelException
	 */
	public void test402665a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test402665/X.java",
				true/* resolve */);
		String contents = "package test402665;" +
				"public class X {\n" +
				"  public static interface StringToInt {\n" +
				"   	int stoi(String s);\n" +
				"  }\n" +
				"  public static interface ReduceInt {\n" +
				"      int reduce(int a, int b);\n" +
				"  }\n" +
				"  void foo(StringToInt s) { }\n" +
				"  void bar(ReduceInt r) { }\n" +
				"  void bar() {\n" +
				"      foo(s -> s.length());\n" +
				"      foo((s) -> s.length());\n" +
				"      foo((String s) -> s.length()); //SingleVariableDeclaration is OK\n" +
				"      bar((x, y) -> x+y);\n" +
				"      bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK\n" +
				"  }\n" +
				"}\n";
			
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		MethodDeclaration methoddecl = (MethodDeclaration)typedeclaration.bodyDeclarations().get(4);
		List statements = methoddecl.getBody().statements();
		int sCount = 0;
		
		ExpressionStatement statement = (ExpressionStatement)statements.get(sCount++);
		MethodInvocation methodInvocation = (MethodInvocation)statement.getExpression();
		LambdaExpression lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "s", contents);
		
		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "s", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		SingleVariableDeclaration singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(0);
		checkSourceRange(singleVarDecl, "String s", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "x", contents);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(1);
		checkSourceRange(fragment, "y", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(0);
		checkSourceRange(singleVarDecl, "int x", contents);
		singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(1);
		checkSourceRange(singleVarDecl, "int y", contents);
	}
	public void testBug403132() throws JavaModelException {
		String contents =
			"import java.lang.annotation.*;\n" +
			"public class X {\n" +
			"	class Y {\n" +
			"		class Z {\n" +
			"			public Z(@A X.@B Y Y.this,String str){\n}" +
			"    	 	public void foo(@A X.@B Y.@C Z this,String str){\n}\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface A {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface B {}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface C {}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		TypeDeclaration type = (TypeDeclaration)node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		type = (TypeDeclaration) node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		AnnotatableType receiver = method.getReceiverType();
		assertEquals("Not an annotatable type", ASTNode.QUALIFIED_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver", "@A X.@B Y", ((QualifiedType) receiver).toString());
		assertEquals("Incorrect method signature", "public Z(@A X.@B Y Y.this,String str){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		receiver = method.getReceiverType();
		assertEquals("Incorrect receiver", "@A X.@B Y.@C Z", ((QualifiedType) receiver).toString());
		assertEquals("Incorrect method signature", "public void foo(@A X.@B Y.@C Z this,String str){\n}\n", method.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403410
	public void testBug403410() throws JavaModelException {
		String contents =
			"import java.lang.annotation.*;\n" +
			"public class X {\n" +
			"	class Y {\n" +
			"		class Z {\n" +
			"			public Z(final Y Y.this){\n}" +
			"    	 	public void foo(static @A Z this){\n}\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface A {}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		TypeDeclaration type = (TypeDeclaration)node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		type = (TypeDeclaration) node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402674
	 * 
	 * @throws JavaModelException
	 */
	public void test402674() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test402674/X.java",
				true/* resolve */);
		String contents = "package test402674;" +
				"public class X {\n" +
				"  public static interface StringToInt {\n" +
				"   	int stoi(String s);\n" +
				"  }\n" +
				"  public static interface ReduceInt {\n" +
				"      int reduce(int a, int b);\n" +
				"  }\n" +
				"  void foo(StringToInt s) { }\n" +
				"  void bar(ReduceInt r) { }\n" +
				"  void bar() {\n" +
				"      foo(s -> s.length());\n" +
				"      foo((s) -> s.length());\n" +
				"      foo((String s) -> s.length()); //SingleVariableDeclaration is OK\n" +
				"      bar((x, y) -> x+y);\n" +
				"      bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK\n" +
				"  }\n" +
				"}\n";
			
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		MethodDeclaration methoddecl = (MethodDeclaration)typedeclaration.bodyDeclarations().get(4);
		List statements = methoddecl.getBody().statements();
		int sCount = 0;
		
		ExpressionStatement statement = (ExpressionStatement)statements.get(sCount++);
		MethodInvocation methodInvocation = (MethodInvocation)statement.getExpression();
		LambdaExpression lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		ITypeBinding binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());
		
		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("ReduceInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("ReduceInt", binding.getName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399791
	public void testBug399791() throws JavaModelException {
		String contents =
			"public interface X {\n" +
			"	static void foo(){}\n" +
			"   public default void foo(int i){}\n" +
			"   native void foo(float f){}\n" +
			"   abstract void foo(long l){}\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));
		List modifiers = method.modifiers();
		assertEquals("Incorrect no of modfiers", 1, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(0);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getKeyword());

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));

		modifiers = method.modifiers();
		assertEquals("Incorrect no of modfiers", 2, modifiers.size());
		modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.DEFAULT_KEYWORD, modifier.getKeyword());
		assertTrue("Incorrect modifier", modifier.isDefault());
		assertEquals("Incorrect AST", "public default void foo(int i){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(2);
		assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));

		method = (MethodDeclaration) type.bodyDeclarations().get(3);
		assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404489
	public void testBug404489a() throws JavaModelException {
		String contents =
		"package test404489.bug;\n" +
		"public class X { \n" +
		"	class Y { \n" +
		"		class Z {\n" +
		"			public Z(@A X.@B Y Y.this){}\n" +
		"			}\n" +
		"  		}\n" +
		"  		Object o=(@A X.@B Y.@Marker  Z)null;\n" +
		"	}\n" +
		"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {} \n" +
		"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface A {} \n" +
		"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface B {} \n";
		this.workingCopy = getWorkingCopy("/Converter18/src/test404489/bug/X.java", true/* resolve */);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		node = (ASTNode) ((TypeDeclaration)node).bodyDeclarations().get(0);
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		node = (ASTNode) ((TypeDeclaration)node).bodyDeclarations().get(0);		
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));
		AnnotatableType annotatableType = method.getReceiverType();
		assertTrue(annotatableType.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) annotatableType;
		assertEquals("wrong qualified type", "@A X.@B Y", qualifiedType.toString());
		ITypeBinding binding = qualifiedType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.X.Y", binding.getQualifiedName());
		List annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		MarkerAnnotation marker	= (MarkerAnnotation) annotations.get(0);
		assertEquals("wrong annotation name", "@B", marker.toString());
		binding = marker.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.B", binding.getQualifiedName());
		IAnnotationBinding annotationBinding = marker.resolveAnnotationBinding();
		assertNotNull(annotationBinding);
		assertEquals("wrong annotation binding", "B", annotationBinding.getName());
		Name name = marker.getTypeName();
		assertTrue(name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		assertEquals("wrong type name", "B", simpleName.toString());
		assertEquals("wrong simple name", "B",simpleName.getIdentifier());
		binding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.B", binding.getQualifiedName());
		assertTrue(qualifiedType.getQualifier().isSimpleType());
		SimpleType simpleType = (SimpleType) qualifiedType.getQualifier();
		assertEquals("incorrect type", "@A X", simpleType.toString());
		binding = simpleType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.X.Y", binding.getQualifiedName());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404489
	public void testBug404489b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter18" , "src", "test404489.bug", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		TypeDeclaration typeDeclaration =  (TypeDeclaration) compilationUnit.types().get(0);

		node = (ASTNode) typeDeclaration.bodyDeclarations().get(2);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());		
		MethodDeclaration methodDecl = (MethodDeclaration) node;
		Type type = methodDecl.getReturnType2();
		assertTrue(type.isPackageQualifiedType());
		PackageQualifiedType packageQualifiedType = (PackageQualifiedType) type;
		checkSourceRange(packageQualifiedType, "test404489.bug.@NonNull IOException", source);
		ITypeBinding typeBinding = packageQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.IOException", typeBinding.getQualifiedName());
		
		// qualifier of the package qualified type
		Name name = packageQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		IBinding binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		
		// annotations of package qualified type
		List annotations = packageQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);
		
		// name of the package qualified type
		simpleName = packageQualifiedType.getName();
		checkSourceRange(simpleName, "IOException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);
		
		// parameter
		SingleVariableDeclaration param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
		type = param.getType();
		assertTrue(type.isPackageQualifiedType());
		packageQualifiedType = (PackageQualifiedType) type;
		checkSourceRange(packageQualifiedType, "test404489.bug.@NonNull FileNotFoundException", source);
		typeBinding = packageQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.FileNotFoundException", typeBinding.getQualifiedName());
		
		// qualifier of the package qualified type
		name = packageQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		
		// annotations of package qualified type
		annotations = packageQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);
		
		// name of the package qualified type
		simpleName = packageQualifiedType.getName();
		checkSourceRange(simpleName, "FileNotFoundException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);
		
		// throws
		type = (Type) methodDecl.thrownExceptionTypes().get(0);
		assertTrue(type.isPackageQualifiedType());
		packageQualifiedType = (PackageQualifiedType) type;
		checkSourceRange(packageQualifiedType, "test404489.bug.@NonNull EOFException", source);
		typeBinding = packageQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.EOFException", typeBinding.getQualifiedName());
		
		// qualifier of the package qualified type
		name = packageQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		
		// annotations of package qualified type
		annotations = packageQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);
		
		// name of the package qualified type
		simpleName = packageQualifiedType.getName();
		checkSourceRange(simpleName, "EOFException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);

		node = (ASTNode) typeDeclaration.bodyDeclarations().get(3);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());		
		FieldDeclaration field = (FieldDeclaration) node;
		type = field.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		packageQualifiedType = (PackageQualifiedType)qualifiedType.getQualifier();
		checkSourceRange(packageQualifiedType, "test404489.bug.@NonNull X", source);
		typeBinding = packageQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X", typeBinding.getQualifiedName());
		name = packageQualifiedType.getName();
		assertSame("bindings different for package qualified type and assocated name", typeBinding, name.resolveTypeBinding());
		
		// qualifier of the package qualified type
		name = packageQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		
		// annotations of package qualified type
		annotations = packageQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);
		
		// name of the package qualified type
		simpleName = packageQualifiedType.getName();
		checkSourceRange(simpleName, "X", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);
		
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399792
	public void testBug399792() throws JavaModelException {
		String content =
				"import java.lang.annotation.ElementType;\n" +
				"import java.io.Serializable;\n" +
				"public class X {\n" +
				"      Object o = (@Marker1 @Marker2 Serializable & I & @Marker3 @Marker1 J) () -> {};" +
				"      public Serializable main(Object o) {\n" +
				"    	  Serializable oo = (Serializable & @Marker3 @Marker1 @Marker2 I & J) o;\n" +
				"    	  return oo;\n" +
				"      }\n" +
				"}\n" +
				"interface I {\n" +
				"  public void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n" +
				"interface K {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker3 {}";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(content, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a field Declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration field = (FieldDeclaration) node;
		assertEquals("Field should not be malformed", 0, (field.getFlags() & ASTNode.MALFORMED));

		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		CastExpression cast = (CastExpression) fragment.getInitializer();
		Type castType = cast.getType();
		assertEquals("Not an intersection cast type", ASTNode.INTERSECTION_TYPE, castType.getNodeType());
		assertTrue("Not an intersection cast type", castType.isIntersectionType());
		assertEquals("Type should not be malformed", 0, (castType.getFlags() & ASTNode.MALFORMED));

		List intersectionTypes = ((IntersectionType) castType).types();
		assertEquals("Incorrect no of types", 3, intersectionTypes.size());
		castType = (Type) intersectionTypes.get(0);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		SimpleName name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "Serializable", name.getIdentifier());

		List annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 2, annotations.size());
		assertEquals("Incorrect receiver", "@Marker1 @Marker2 Serializable", castType.toString());

		castType = (Type) intersectionTypes.get(1);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "I", name.getIdentifier());

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "I", castType.toString());

		castType = (Type) intersectionTypes.get(2);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "J", name.getIdentifier());

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 2, annotations.size());
		assertEquals("Incorrect receiver", "@Marker3 @Marker1 J", castType.toString());

		node = (ASTNode) type.bodyDeclarations().get(1);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));
		
		List statements = method.getBody().statements();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		cast = (CastExpression) fragment.getInitializer();
		castType = cast.getType();
		
		intersectionTypes = ((IntersectionType) castType).types();
		assertEquals("Incorrect no of types", 3, intersectionTypes.size());
		castType = (Type) intersectionTypes.get(0);

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "Serializable", castType.toString());

		castType = (Type) intersectionTypes.get(1);
		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 3, annotations.size());
		assertEquals("Incorrect receiver", "@Marker3 @Marker1 @Marker2 I", castType.toString());

		castType = (Type) intersectionTypes.get(2);

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "J", castType.toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=406505
	 * tests the source range issue that resulted in bad ast node.
	 * 
	 * @throws JavaModelException
	 */
	public void testBug406505() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test406505/X.java",
				true/* resolve */);
		String contents = "package test406505;"
				+ "import java.lang.annotation.Target;\n"
				+ "import java.io.File;\n"
				+ "public class X {\n"
				+ "	class Folder<@Marker  F extends File> { }\n"
				+ "}\n" 		
				+ "@Target (java.lang.annotation.ElementType.TYPE_USE)\n"
				+ "@interface Marker {}\n";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);		
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		typedeclaration = (TypeDeclaration)typedeclaration.bodyDeclarations().get(0);
		TypeParameter typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);
		checkSourceRange(typeParameter, "@Marker  F extends File", contents);
	}

}
