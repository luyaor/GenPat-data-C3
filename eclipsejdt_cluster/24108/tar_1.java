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

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingTypeDeclTest extends ASTRewritingTest {
	
	private static final Class THIS= ASTRewritingTypeDeclTest.class;
	
	public ASTRewritingTypeDeclTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test suite() {
		if (true) {
			return allTests();
		}
		TestSuite suite= new Suite("one test");
		suite.addTest(new ASTRewritingTypeDeclTest("testVariableDeclarationFragment"));
		return suite;
	}
		
	public void testTypeDeclChanges() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");		
		buf.append("    }\n");		
		buf.append("    private int i;\n");	
		buf.append("    private int k;\n");	
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");		
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // rename type, rename supertype, rename first interface, replace inner class with field
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			SimpleName name= type.getName();
			SimpleName newName= ast.newSimpleName("X");
			
			rewrite.replace(name, newName, null);
			
			Name superClass= type.getSuperclass();
			assertTrue("Has super type", superClass != null);
			
			SimpleName newSuperclass= ast.newSimpleName("Object");
			rewrite.replace(superClass, newSuperclass, null);

			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			
			SimpleName newSuperinterface= ast.newSimpleName("Cloneable");
			rewrite.replace((ASTNode) superInterfaces.get(0), newSuperinterface, null);
			
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
			
			FieldDeclaration newFieldDecl= createNewField(ast, "fCount");
			
			rewrite.replace((ASTNode) members.get(0), newFieldDecl, null);
		}
		{ // replace method in F, change to interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");
			
			// change flags
			int newModifiers= 0;
			rewrite.set(type, TypeDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);
			
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", members.size() == 1);

			MethodDeclaration methodDecl= createNewMethod(ast, "newFoo", true);

			rewrite.replace((ASTNode) members.get(0), methodDecl, null);
		}
		
		{ // change to class, add supertype
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");
			
			// change flags
			int newModifiers= 0;
			rewrite.set(type, TypeDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			// change to class
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.FALSE, null);
			
			
			SimpleName newSuperclass= ast.newSimpleName("Object");
			rewrite.set(type, TypeDeclaration.SUPERCLASS_PROPERTY, newSuperclass, null);
		}			
					

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X extends Object implements Cloneable, Serializable {\n");
		buf.append("    private double fCount;\n");
		buf.append("    private int i;\n");	
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface F extends Runnable {\n");
		buf.append("    private abstract void newFoo(String str);\n");
		buf.append("}\n");				
		buf.append("class G extends Object {\n");
		buf.append("}\n");			
		assertEqualString(preview, buf.toString());

	}


	
	public void testTypeDeclRemoves() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");		
		buf.append("    }\n");		
		buf.append("    private int i;\n");	
		buf.append("    private int k;\n");	
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");		
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		{ // change to interface, remove supertype, remove first interface, remove field
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			
			// change flags
			int newModifiers= 0;
			rewrite.set(type, TypeDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);
		
			Name superClass= type.getSuperclass();
			assertTrue("Has super type", superClass != null);
			
			rewrite.remove(superClass, null);

			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			
			rewrite.remove((ASTNode) superInterfaces.get(0), null);
			
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
					
			rewrite.remove((ASTNode) members.get(1), null);
			
			MethodDeclaration meth= findMethodDeclaration(type, "hee");
			rewrite.remove(meth, null);
		}
		{ // remove superinterface & method, change to interface & final
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");
					
			// change flags
			int newModifiers= Modifier.FINAL;
			rewrite.set(type, TypeDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);
			
			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			rewrite.remove((ASTNode) superInterfaces.get(0), null);
			
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", members.size() == 1);

			rewrite.remove((ASTNode) members.get(0), null);			
		}			
		{ // remove class G
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");
			rewrite.remove(type, null);		
		}				

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("interface E extends Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");		
		buf.append("    }\n");		
		buf.append("    private int k;\n");	
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("final interface F {\n");
		buf.append("}\n");				
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclInserts() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");		
		buf.append("    }\n");		
		buf.append("    private int i;\n");	
		buf.append("    private int k;\n");	
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");		
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AST ast= astRoot.getAST();
		{ // add interface & set to final
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
					
			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.FINAL;
			rewrite.set(type, TypeDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
				
			SimpleName newSuperinterface= ast.newSimpleName("Cloneable");
			
			rewrite.getListRewrite(type, TypeDeclaration.SUPER_INTERFACES_PROPERTY).insertFirst(newSuperinterface, null);
			
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
			
			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

/*		bug 22161
			SimpleName newSuperclass= ast.newSimpleName("Exception");
			innerType.setSuperclass(newSuperclass);
			rewrite.markAsInserted(newSuperclass);
*/

			FieldDeclaration newField= createNewField(ast, "fCount");
			
			rewrite.getListRewrite(innerType, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newField, null);
			
			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAt(newMethodDecl, 4, null);
		}
		{ // add exception, add method
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");
			
			SimpleName newSuperclass= ast.newSimpleName("Exception");
			rewrite.set(type, TypeDeclaration.SUPERCLASS_PROPERTY, newSuperclass, null);
					
			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl, null);
		}			
		{ // insert interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");
						
			SimpleName newInterface= ast.newSimpleName("Runnable");
			rewrite.getListRewrite(type, TypeDeclaration.SUPER_INTERFACES_PROPERTY).insertLast(newInterface, null);
			
			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", true);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl,  null);
		}			

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public final class E extends Exception implements Cloneable, Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        private double fCount;\n");
		buf.append("\n");				
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");		
		buf.append("    private int i;\n");	
		buf.append("    private int k;\n");	
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    private void newMethod(String str) {\n");
		buf.append("    }\n");		
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F extends Exception implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void newMethod(String str) {\n");
		buf.append("    }\n");		
		buf.append("}\n");				
		buf.append("interface G extends Runnable {\n");
		buf.append("\n");		
		buf.append("    private abstract void newMethod(String str);\n");		
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testTypeDeclInsertFields1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		buf.append("class F {\n");
		buf.append("}\n");		
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AST ast= astRoot.getAST();
		{ 	
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			
			VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("x"));
			
			FieldDeclaration decl= ast.newFieldDeclaration(frag);
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(decl, null);
			
		}
		{ 	
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");
			
			VariableDeclarationFragment frag1= ast.newVariableDeclarationFragment();
			frag1.setName(ast.newSimpleName("x"));
			
			FieldDeclaration decl1= ast.newFieldDeclaration(frag1);
			decl1.setType(ast.newPrimitiveType(PrimitiveType.INT));
			
			VariableDeclarationFragment frag2= ast.newVariableDeclarationFragment();
			frag2.setName(ast.newSimpleName("y"));
			
			FieldDeclaration decl2= ast.newFieldDeclaration(frag2);
			decl2.setType(ast.newPrimitiveType(PrimitiveType.INT));			
						
			ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertFirst(decl1, null);
			listRewrite.insertAfter(decl2, decl1, null);
		}				

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    int x;\n");
		buf.append("}\n");
		buf.append("class F {\n");
		buf.append("\n");
		buf.append("    int x;\n");
		buf.append("    int y;\n");	
		buf.append("}\n");		
		assertEqualString(preview, buf.toString());

	}	
	
	public void testBug22161() throws Exception {
	//	System.out.println(getClass().getName()+"::" + getName() +" disabled (bug 22161)");
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class T extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");		
		buf.append("    }\n");		
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);				

		CompilationUnit astRoot= createAST(cu);
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		
		TypeDeclaration type= findTypeDeclaration(astRoot, "T");
		assertTrue("Outer type not found", type != null);
		
		List members= type.bodyDeclarations();
		assertTrue("Cannot find inner class", members.size() == 1 &&  members.get(0) instanceof TypeDeclaration);

		TypeDeclaration innerType= (TypeDeclaration) members.get(0);
		
		SimpleName name= innerType.getName();
		assertTrue("Name positions not correct", name.getStartPosition() != -1 && name.getLength() > 0);
		
	}
	
	public void testAnonymousClassDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E2 {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new Runnable() {\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            int i= 8;\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            int i= 8;\n");
		buf.append("        };\n");		
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("E2.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E2");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{	// insert body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);
			
			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 0", decls.size() == 0);
			
			MethodDeclaration newMethod= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(anonym, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newMethod, null);
		}
		{	// remove body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);
			
			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 1", decls.size() == 1);

			rewrite.remove((ASTNode) decls.get(0), null);
		}		
		{	// replace body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);
			
			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 1", decls.size() == 1);
			
			MethodDeclaration newMethod= createNewMethod(ast, "newMethod", false);

			rewrite.replace((ASTNode) decls.get(0), newMethod, null);
		}	
					
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E2 {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new Runnable() {\n");
		buf.append("\n");
		buf.append("            private void newMethod(String str) {\n");
		buf.append("            }\n");	
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            private void newMethod(String str) {\n");
		buf.append("            }\n");	
		buf.append("        };\n");		
		buf.append("    }\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
			
	public void testImportDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.text.*;\n");					
		buf.append("public class Z {\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		
		List imports= astRoot.imports();
		assertTrue("Number of imports not 4", imports.size() == 4);
		
		{ // rename import
			ImportDeclaration imp= (ImportDeclaration) imports.get(0);
			
			Name name= ast.newName(new String[] { "org", "eclipse", "X" });
			rewrite.replace(imp.getName(), name, null);
		}
		{ // change to import on demand
			ImportDeclaration imp= (ImportDeclaration) imports.get(1);
			
			Name name= ast.newName(new String[] { "java", "util" });
			rewrite.replace(imp.getName(), name, null);
			
			rewrite.set(imp, ImportDeclaration.ON_DEMAND_PROPERTY, Boolean.TRUE, null);
		}
		{ // change to single import
			ImportDeclaration imp= (ImportDeclaration) imports.get(2);
			
			rewrite.set(imp, ImportDeclaration.ON_DEMAND_PROPERTY, Boolean.FALSE, null);
		}
		{ // rename import
			ImportDeclaration imp= (ImportDeclaration) imports.get(3);
			
			Name name= ast.newName(new String[] { "org", "eclipse" });
			rewrite.replace(imp.getName(), name, null);
		}		
		
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import org.eclipse.X;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net;\n");
		buf.append("import org.eclipse.*;\n");			
		buf.append("public class Z {\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testPackageDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		
		{ // rename package
			PackageDeclaration packageDeclaration= astRoot.getPackage();
			
			Name name= ast.newName(new String[] { "org", "eclipse" });
			
			rewrite.replace(packageDeclaration.getName(), name, null);
		}
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package org.eclipse;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testCompilationUnit() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		
		{
			PackageDeclaration packageDeclaration= astRoot.getPackage();
			rewrite.remove(packageDeclaration, null);
		}
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("\n");	
		buf.append("public class Z {\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testCompilationUnit2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class Z {\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		
		{
			PackageDeclaration packageDeclaration= ast.newPackageDeclaration();
			Name name= ast.newName(new String[] { "org", "eclipse" });
			packageDeclaration.setName(name);
			
			rewrite.set(astRoot, CompilationUnit.PACKAGE_PROPERTY, packageDeclaration, null);
		}
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package org.eclipse;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}	
	
	public void testSingleVariableDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, final int[] k, int[] x[]) {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		List arguments= methodDecl.parameters();
		{ // add modifier, change type, change name, add extra dimension
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(0);
					
			int newModifiers= Modifier.FINAL;
			rewrite.set(decl, SingleVariableDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			rewrite.set(decl, SingleVariableDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);
			
			ArrayType newVarType= ast.newArrayType(ast.newPrimitiveType(PrimitiveType.FLOAT), 2);
			rewrite.replace(decl.getType(), newVarType, null);
			
			Name newName= ast.newSimpleName("count");
			rewrite.replace(decl.getName(), newName, null);
		}
		{ // remove modifier, change type
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(1);
						
			int newModifiers= 0;
			rewrite.set(decl, SingleVariableDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			Type newVarType= ast.newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(decl.getType(), newVarType, null);
		}
		{ // remove extra dim
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(2);
			
			rewrite.set(decl, SingleVariableDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(0), null);
		}			
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(final float[][] count[], float k, int[] x) {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}	
	
	public void testVariableDeclarationFragment() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i, j, k= 0, x[][], y[]= {0, 1};\n");		
		buf.append("    }\n");
		buf.append("}\n");	
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		
		VariableDeclarationStatement variableDeclStatement= (VariableDeclarationStatement) statements.get(0);
		List fragments= variableDeclStatement.fragments();
		assertTrue("Number of fragments not 5", fragments.size() == 5);
		
		{ // rename var, add dimension
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);
			
			ASTNode name= ast.newSimpleName("a");
			rewrite.replace(fragment.getName(), name, null);
			
			rewrite.set(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS_PROPERTY, new Integer(2), null);
		}
		
		{ // add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(1);
			
			assertTrue("Has initializer", fragment.getInitializer() == null);
			
			Expression initializer= ast.newNumberLiteral("1");
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);
		}
		
		{ // remove initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(2);
			
			assertTrue("Has no initializer", fragment.getInitializer() != null);
			rewrite.remove(fragment.getInitializer(), null);
		}
		{ // add dimension, add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(3);			
			
			rewrite.set(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS_PROPERTY, new Integer(4), null);

			assertTrue("Has initializer", fragment.getInitializer() == null);
			
			Expression initializer= ast.newNullLiteral();
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);
		}
		{ // remove dimension
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(4);			
			
			rewrite.set(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS_PROPERTY, new Integer(0), null);
		}					
			
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int a[][], j = 1, k, x[][][][] = null, y= {0, 1};\n");		
		buf.append("    }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testTypeDeclSpacingMethods1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");		
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // insert method
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
			
			MethodDeclaration newMethodDecl= createNewMethod(ast, "foo", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl, null);
			
		}
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");		
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("\n");		
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");		
		buf.append("}\n");		
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclSpacingMethods2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");			
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
			
			MethodDeclaration newMethodDecl= createNewMethod(ast, "foo", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newMethodDecl, null);
		}
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");			
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");		
		assertEqualString(preview, buf.toString());

	}
	
	public void testTypeDeclSpacingFields() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int x;\n");
		buf.append("    private int y;\n");
		buf.append("\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");			
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());
			
			FieldDeclaration newField= createNewField(ast, "fCount");
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newField, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private double fCount;\n");
		buf.append("    private int x;\n");
		buf.append("    private int y;\n");
		buf.append("\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");			
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}

	
}
