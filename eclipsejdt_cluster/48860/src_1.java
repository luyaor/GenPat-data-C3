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
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewriter;
import org.eclipse.jdt.core.dom.rewrite.NewASTRewrite;

public class ASTRewritingInsertBoundTest extends ASTRewritingTest {

	private static final Class THIS= ASTRewritingInsertBoundTest.class;
	
	public ASTRewritingInsertBoundTest(String name) {
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
			suite.addTest(new ASTRewritingInsertBoundTest("testRemove3"));
			return suite;
		}
	}
	
	private MethodDeclaration newMethodDeclaration(AST ast, String name) {
		MethodDeclaration decl= ast.newMethodDeclaration();
		decl.setName(ast.newSimpleName(name));
		decl.setBody(null);
		decl.setReturnType(ast.newPrimitiveType(PrimitiveType.VOID));
		return decl;
	}
	
	private FieldDeclaration newFieldDeclaration(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration decl= ast.newFieldDeclaration(frag);
		decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
		return decl;
	}	
	
	
	public void testInsert1() throws Exception {
		// insert first and last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testInsert3() throws Exception {
		// insert 2 x beween 
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		
		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testInsert2() throws Exception {
		// insert first and last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");
				
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertAfter(decl2, decl1, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("    void new3();\n");
		buf.append("\n");
		buf.append("    void new4();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}	

	public void testInsert1Before() throws Exception {
		// insert 2x first and 2xlast
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testInsert2Before() throws Exception {
		// insert 2x first and 2 x last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertAfter(decl2, decl1, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);
		
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("    int new3;\n");
		buf.append("\n");
		buf.append("    int new4;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}	
	
	public void testInsert3Before() throws Exception {
		// insert 2 x beween 
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("    int new2;\n");	
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemove1() throws Exception {
		// remove first and last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemove2() throws Exception {
		// remove second
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemove3() throws Exception {
		// remove 2nd and 3rd
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemove4() throws Exception {
		// remove all
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testRemoveInsert1() throws Exception {
		// remove first add before first, remove last add after last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert2() throws Exception {
		// remove first add 2x first, remove last add 2x  last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");
		
		ASTNode firstDecl= (ASTNode) decls.get(0);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);
		
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    void new3();\n");
		buf.append("\n");
		buf.append("    void new4();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert3() throws Exception {
		// remove middle, add before, add after
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		
		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testRemoveInsert1Before() throws Exception {
		// remove first add before first, remove last add after last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
				
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
		
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert2Before() throws Exception {
		// remove first add 2x first, remove last add 2x  last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    int new3;\n");
		buf.append("\n");
		buf.append("    int new4;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert3Before() throws Exception {
		// remove middle, add before, add after
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");		
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert4() throws Exception {
		// remove first and add after first, remove last and add before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);	   
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}

	public void testRemoveInsert4Before() throws Exception {
		// remove first and add after first, remove last and add before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);	
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}	
	
	public void testRemoveInsert5() throws Exception {
		// remove first and add after and before first, remove last and add after and before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");
		
		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertBefore(decl3, lastDecl, null);
		listRewrite.insertAfter(decl4, lastDecl, null);
		
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    void new3();\n");
		buf.append("\n");
		buf.append("    void new4();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}

	public void testRemoveInsert5Before() throws Exception {
		// remove first and add after first, remove last and add before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertBefore(decl3, lastDecl, null);
		listRewrite.insertAfter(decl4, lastDecl, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("    int new3;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    int new4;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}	
	

	public void testRemoveInsert6() throws Exception {
		// remove all, add before first and after last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert6Before() throws Exception {
		// remove all, add before first and after last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	

	public void testRemoveInsert7() throws Exception {
		// remove all, add after first and before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public void foo1();\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public void foo2();\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public void foo3();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    void new1();\n");
		buf.append("\n");
		buf.append("    void new2();\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testRemoveInsert7Before() throws Exception {
		// remove all, add after first and before last
		
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("//c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("//c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
	
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		NewASTRewrite rewrite= new NewASTRewrite(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.markAsRemoved((ASTNode) decls.get(0), null);
		rewrite.markAsRemoved((ASTNode) decls.get(1), null);
		rewrite.markAsRemoved((ASTNode) decls.get(2), null);
		
		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		
		ListRewriter listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("//c1\n");
		buf.append("\n");
		buf.append("    int new1;\n");
		buf.append("\n");
		buf.append("    int new2;\n");
		buf.append("\n");
		buf.append("//c4\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	

}



