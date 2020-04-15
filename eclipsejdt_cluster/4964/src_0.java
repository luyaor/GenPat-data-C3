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
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import org.eclipse.jdt.core.dom.*;

public class ASTRewritingModifyingCopyTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingCopyTest.class;

	public ASTRewritingModifyingCopyTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test suite() {
		if (true) {
			return allTests();
		} else {
			TestSuite suite= new Suite("one test");
			suite.addTest(new ASTRewritingModifyingCopyTest("test0009"));
			return suite;
		}
	}
	
	public void test0001() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		types.add(typeDeclaration2);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/*
	 * Known limitation: a copied node cannot be modified.
	 */
	public void test0002() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		typeDeclaration2.setName(a.newSimpleName("A"));
		types.add(typeDeclaration2);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y /**/ {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	public void test0003() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		Name name2 = (Name)ASTNode.copySubtree(a, name);
		typeDeclaration2.setSuperclass(name2);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	public void test0004() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0004", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		QualifiedName name2 = (QualifiedName)ASTNode.copySubtree(a, name);
		Name name3 = name2.getQualifier();
		name2.setQualifier(a.newSimpleName("A"));
		typeDeclaration2.setSuperclass(name3);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y extends Z1\n");
		buf.append("                        .Z2 {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	public void test0005() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0005", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		QualifiedName name2 = (QualifiedName)ASTNode.copySubtree(a, name);
		QualifiedName name3 = (QualifiedName)name2.getQualifier();
		name2.setQualifier(a.newSimpleName("A"));
		name3.getName().setIdentifier("B");
		typeDeclaration2.setSuperclass(name3);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y extends Z1\n");
		buf.append("                        .Z2 {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	public void test0006() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0006", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    void foo() {\n");
		buf.append("        bar1();\n");
		buf.append("        \n");
		buf.append("        //comment1\n");
		buf.append("        bar2();//comment2\n");
		buf.append("        //comment3\n");
		buf.append("        bar3();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST a = astRoot.getAST();
		
		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		Statement statement1 = (Statement)statements.get(1);
		Statement statement2 = (Statement)ASTNode.copySubtree(a, statement1);
		statements.add(statement2);
		
		String preview = evaluateRewrite(cu, astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    void foo() {\n");
		buf.append("        bar1();\n");
		buf.append("        \n");
		buf.append("        //comment1\n");
		buf.append("        bar2();//comment2\n");
		buf.append("        //comment3\n");
		buf.append("        bar3();\n");
		buf.append("        //comment1\n");
		buf.append("        bar2();//comment2\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
//	public void test0007() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0007", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0007;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    void foo() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("    void bar() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
//		
//		CompilationUnit astRoot= parseCompilationUnit(cu, false);
//		
//		astRoot.recordModifications();
//		
//		AST a = astRoot.getAST();
//		
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		MethodDeclaration methodDeclaration1 = typeDeclaration.getMethods()[0];
//		MethodDeclaration methodDeclaration2 = typeDeclaration.getMethods()[1];
//		Javadoc javadoc1 = methodDeclaration1.getJavadoc();
//		Javadoc javadoc2 = (Javadoc)ASTNode.copySubtree(a, javadoc1);
//		methodDeclaration2.setJavadoc(javadoc2);
//		
//		String preview = evaluateRewrite(cu, astRoot);
//		
//		buf= new StringBuffer();
//		buf.append("package test0007;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    void foo() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    void bar() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
}
