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
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

public class ASTRewritingMethodDeclTest extends ASTRewritingTest {

	/**
	 * Internal synonym for deprecated constant MethodDeclaration#EXTRA_DIMENSIONS_PROPERTY
	 * to alleviate deprecated warnings.
	 * @deprecated
	 */
	private static final SimplePropertyDescriptor INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY = MethodDeclaration.EXTRA_DIMENSIONS_PROPERTY;

	public ASTRewritingMethodDeclTest(String name) {
		super(name);
	}

	public ASTRewritingMethodDeclTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	/** @deprecated using deprecated code */
   private  Type getReturnType(MethodDeclaration methodDecl) { 
    	return this.apiLevel < AST.JLS3 ? methodDecl.getReturnType() : methodDecl.getReturnType2(); 
    } 

   /** @deprecated using deprecated code */
   private ChildPropertyDescriptor getMethodReturnTypeProperty(AST ast) { 
   	return ast.apiLevel() < AST.JLS3 ? MethodDeclaration.RETURN_TYPE_PROPERTY : MethodDeclaration.RETURN_TYPE2_PROPERTY; 
   } 

   private ASTNode createNewExceptionType(AST ast, String name) {
    	return ast.apiLevel() < AST.JLS8 ? ast.newSimpleName(name) : (ASTNode) ast.newSimpleType(ast.newSimpleName(name));
    }

    private List getThrownExceptions(MethodDeclaration methodDecl) { 
    	return this.apiLevel < AST.JLS8 ? methodDecl.thrownExceptions() : methodDecl.thrownExceptionTypes(); 
    } 

    private ChildListPropertyDescriptor getMethodThrownExceptionsProperty(AST ast) { 
    	return ast.apiLevel() < AST.JLS8 ? MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY : MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY; 
    } 

	public void testMethodDeclChanges() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // convert constructor to method: insert return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, getMethodReturnTypeProperty(ast), newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		{ // change return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
			assertTrue("Has no return type: gee", getReturnType(methodDecl) != null);

			Type returnType= getReturnType(methodDecl);
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(returnType, newReturnType, null);
		}
		{ // remove return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			assertTrue("Has no return type: hee", getReturnType(methodDecl) != null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		{ // rename method name
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");

			SimpleName name= methodDecl.getName();
			SimpleName newName= ast.newSimpleName("xii");

			rewrite.replace(name, newName, null);
		}
		{ // rename first param & last throw statement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.replace((ASTNode) parameters.get(0), newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			ASTNode newThrownException= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);
		}
		{ // rename first and second param & rename first and last exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			ASTNode newThrownException1= createNewExceptionType(ast, "ArrayStoreException");
			ASTNode newThrownException2= createNewExceptionType(ast, "InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), newThrownException1, null);
			rewrite.replace((ASTNode) thrownExceptions.get(2), newThrownException2, null);
		}
		{ // rename all params & rename second exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			SingleVariableDeclaration newParam3= createNewParam(ast, "m3");
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);
			rewrite.replace((ASTNode) parameters.get(2), newParam3, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			ASTNode newThrownException= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E(int p1, int p2, int p3) {}\n");
		buf.append("    public float gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void xii(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(float m, int p2, int p3) throws IllegalArgumentException, ArrayStoreException {}\n");
		buf.append("    public abstract void kee(float m1, float m2, int p3) throws ArrayStoreException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, float m2, float m3) throws IllegalArgumentException, ArrayStoreException, SecurityException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testMethodTypeParameterAdds_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    E(int p1) {}\n");
		buf.append("    E(int p1, int p2) {}\n");
		buf.append("    public E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    void gee(int p1) {}\n");
		buf.append("    void hee(int p1, int p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {

			// add type parameter
			MethodDeclaration methodDecl= methods[i];
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.TYPE_PARAMETERS_PROPERTY);
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("X"));
			listRewrite.insertFirst(typeParameter, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> E(int p1) {}\n");
		buf.append("    <X> E(int p1, int p2) {}\n");
		buf.append("    public <X> E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> void gee(int p1) {}\n");
		buf.append("    <X> void hee(int p1, int p2) {}\n");
		buf.append("    public <X> void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMethodTypeParameterRemoves_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> E(int p1) {}\n");
		buf.append("    <X> E(int p1, int p2) {}\n");
		buf.append("    public <X> E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> void gee(int p1) {}\n");
		buf.append("    <X> void hee(int p1, int p2) {}\n");
		buf.append("    public <X> void hee(int p1, byte p2) {}\n");
		buf.append("    public<X>void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {

			// add type parameter
			MethodDeclaration methodDecl= methods[i];
			rewrite.remove((ASTNode) methodDecl.typeParameters().get(0), null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    E(int p1) {}\n");
		buf.append("    E(int p1, int p2) {}\n");
		buf.append("    public E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    void gee(int p1) {}\n");
		buf.append("    void hee(int p1, int p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void testMethodReturnTypeChanges_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC | Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // remove return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC | Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testMethodReturnTypeChanges2_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // remove return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}



	public void testMethodReturnTypeChangesAST3_only_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // remove return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testMethodReturnTypeChanges2AST3_only_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // remove return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}




	public void testListRemoves_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete first param
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
		}
		{ // delete second param & remove exception & remove public
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
		}
		{ // delete last param
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(2), null);
		}
		{ // delete first and second param & remove first exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
		}
		{ // delete first and last param & remove second
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
		}
		{ // delete second and last param & remove first exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
		}
		{ // delete all params & remove first and last exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p2, int p3) {}\n");
		buf.append("    void gee(int p1, int p3) {}\n");
		buf.append("    public void hee(int p1, int p2) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p3) throws IllegalAccessException {}\n");
		buf.append("    public void jee(int p2) throws IllegalArgumentException {}\n");
		buf.append("    public abstract void kee(int p1) throws IllegalArgumentException, SecurityException;\n");
		buf.append("    public abstract void lee() throws IllegalAccessException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListRemoves2_only_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(String property1) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);

		{ // delete param, insert new
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
			List parameters= methodDecl.parameters();
			rewrite.remove((ASTNode) parameters.get(0), null);

			SingleVariableDeclaration decl= ast.newSingleVariableDeclaration();
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
			decl.setName(ast.newSimpleName("property11"));

			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(decl, null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(int property11) {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331111
	public void _testListRemoves3() throws Exception {
		Map options = this.project1.getOptions(true);
		Map newOptions = this.project1.getOptions(true);
		try {
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION,
					JavaCore.INSERT);
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION,
					JavaCore.INSERT);
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION,
					JavaCore.DO_NOT_INSERT);
			this.project1.setOptions(newOptions);
			IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo( String s ) {}\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
	
			CompilationUnit astRoot= createAST(cu);	
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);
	
			{ // delete param, insert new
				MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
				List parameters= methodDecl.parameters();
				rewrite.remove((ASTNode) parameters.get(0), null);
			}
			String preview= evaluateRewrite(cu, rewrite);
	
			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo() {}\n");
			buf.append("}\n");
	
			assertEqualString(preview, buf.toString());
		} finally {
			this.project1.setOptions(options);
		}
	}

	public void testListInserts_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert before first param & insert an exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertFirst(newThrownException, null);

		}
		{ // insert before second param & insert before first exception & add synchronized
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED;
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode secondParam= (ASTNode) parameters.get(1);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertBefore(newParam, secondParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertBefore(newThrownException, firstException, null);
		}
		{ // insert after last param & insert after first exception & add synchronized, static
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED | Modifier.STATIC;
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, firstException, null);

		}
		{ // insert 2 params before first & insert between two exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode firstParam= (ASTNode) parameters.get(0);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");

			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
			listRewrite.insertBefore(newParam1, firstParam, null);
			listRewrite.insertBefore(newParam2, firstParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, firstException, null);
		}
		{ // insert 2 params after first & replace the second exception and insert new after
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			ASTNode firstParam= (ASTNode) parameters.get(0);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertAfter(newParam2, firstParam, null);
			listRewrite.insertAfter(newParam1, firstParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);

			Name newThrownException1= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException1, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException2, null);
		}
		{ // insert 2 params after last & remove the last exception and insert new after
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
			ASTNode lastParam= (ASTNode) parameters.get(2);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");

			listRewrite.insertAfter(newParam2, lastParam, null);
			listRewrite.insertAfter(newParam1, lastParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(lastException, null);

			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertBefore(newThrownException, lastException, null);
		}
		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			ASTNode secondException= (ASTNode) thrownExceptions.get(1);
			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(secondException, null);
			rewrite.remove(lastException, null);

			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, secondException, null);

		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m, int p1, int p2, int p3) throws InterruptedException {}\n");
		buf.append("    public synchronized void gee(int p1, float m, int p2, int p3) throws InterruptedException, IllegalArgumentException {}\n");
		buf.append("    public static synchronized void hee(int p1, int p2, int p3, float m) throws IllegalArgumentException, InterruptedException {}\n");
		buf.append("    public void iee(float m1, float m2, int p1, int p2, int p3) throws IllegalArgumentException, InterruptedException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, float m1, float m2, int p2, int p3) throws IllegalArgumentException, ArrayStoreException, InterruptedException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3, float m1, float m2) throws IllegalArgumentException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListInsert() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);

			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException, null);
		}



		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombinations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete all and insert after & insert 2 exceptions
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);


			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			ASTNode newThrownException1= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException1, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);

		}
		{ // delete first 2, replace last and insert after & replace first exception and insert before
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);

			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam2, null);


			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode modifiedThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), modifiedThrownException, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);

		}
		{ // delete first 2, replace last and insert at first & remove first and insert before
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);

			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			rewrite.remove((ASTNode) thrownExceptions.get(0), null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void gee(float m1, float m2) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void hee(float m2, float m1) throws ArrayStoreException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombination() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete all and insert after & insert 2 exceptions
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			ASTNode newThrownException1= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException1, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);


		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombination2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		Arrays.sort(methods, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((MethodDeclaration) o1).getName().getIdentifier().compareTo(((MethodDeclaration) o2).getName().getIdentifier());
			}
		});

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		for (int i= 0; i < methods.length; i++) {
			ASTNode copy= rewrite.createMoveTarget(methods[i]);
			listRewrite.insertLast(copy, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}


	public void testMethodBody_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // replace block
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");

			Block body= methodDecl.getBody();
			assertTrue("No body: E", body != null);

			Block newBlock= ast.newBlock();

			rewrite.replace(body, newBlock, null);
		}
		{ // delete block & set abstract
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.ABSTRACT;
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			Block body= methodDecl.getBody();
			assertTrue("No body: gee", body != null);

			rewrite.remove(body, null);
		}
		{ // insert block & set to private
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");

			// change flags
			int newModifiers= Modifier.PRIVATE;
			rewrite.set(methodDecl, MethodDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);


			Block body= methodDecl.getBody();
			assertTrue("Has body", body == null);

			Block newBlock= ast.newBlock();
			rewrite.set(methodDecl, MethodDeclaration.BODY_PROPERTY, newBlock, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {\n");
		buf.append("    }\n");
		buf.append("    public abstract void gee(int p1, int p2, int p3) throws IllegalArgumentException;\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    private void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException {\n");
		buf.append("    }\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodDeclarationExtraDimensions_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    public Object foo2() throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo3()[][] { return null; }\n");
		buf.append("    public Object foo4()[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5()[][] { return null; }\n");
		buf.append("    public Object foo6(int i)[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i)[][] { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);

			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);
		}
		{ // remove extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);

			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);
		}
		{ // add params, add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam1, null);


			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(4), null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // remove params, add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");

			rewrite.remove((ASTNode) methodDecl.parameters().get(0), null);

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(4), null);

			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);
		}
		{ // remove block
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			rewrite.remove(methodDecl.getBody(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo2()[] { return null; }\n");
		buf.append("    public Object foo3()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo4()[] { return null; }\n");
		buf.append("    public Object foo5(float m1)[][][][] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo6()[][][][] { return null; }\n");
		buf.append("    public Object foo7(int i)[][];\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testModifiersAST3_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("    Object foo5() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo6() { return null; }\n");
		buf.append("    public Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public static Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo9() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert first and last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD), null);
		}
		{ // insert 2x first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD), null);
		}
		{ // remove and insert first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove and insert last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // insert first and insert Javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove modifier and remove javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			rewrite.remove(methodDecl.getJavadoc(), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove modifier and insert javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");

			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);

			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove all
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo8");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);
		}
		{ // insert (first) with javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo9");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    final public synchronized Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    static final public Object foo2() { return null; }\n");
		buf.append("    final Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo4() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    final Object foo5() { return null; }\n");
		buf.append("    Object foo6() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo9() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testModifiersAST3WithAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert annotation first before normal
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // insert annotation first before annotation
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // remove annotation before normal
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove annotation before annotation
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    @Override\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Override\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testModifiersAST3WithAnnotations2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    Object foo1() { return null; }\n");
		buf.append("    Object foo2() { return null; }\n");
		buf.append("    @Deprecated()Object foo3() { return null; }\n");
		buf.append("    @Deprecated()Object foo4() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert annotation first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // insert modifier first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertFirst(modifier, null);
		}
		{ // insert modifier last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertLast(modifier, null);
		}
		{ // insert modifier first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertFirst(modifier, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    @Override\n");
		buf.append("    Object foo1() { return null; }\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    @Deprecated()\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    public @Deprecated()Object foo4() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void testFieldDeclaration_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    int i1= 1;\n");
		buf.append("    int i2= 1, k2= 2, n2= 3;\n");
		buf.append("    static final int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");

		FieldDeclaration[] fieldDeclarations= type.getFields();
		assertTrue("Number of fieldDeclarations not 3", fieldDeclarations.length == 3);
		{	// add modifier, change type, add fragment
			FieldDeclaration decl= fieldDeclarations[0];

			// add modifier
			int newModifiers= Modifier.FINAL;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			PrimitiveType newType= ast.newPrimitiveType(PrimitiveType.BOOLEAN);
			rewrite.replace(decl.getType(), newType, null);

			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k1"));
			frag.setInitializer(null);

			rewrite.getListRewrite(decl, FieldDeclaration.FRAGMENTS_PROPERTY).insertLast(frag, null);

		}
		{	// add modifiers, remove first two fragments, replace last
			FieldDeclaration decl= fieldDeclarations[1];

			// add modifier
			int newModifiers= Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

			List fragments= decl.fragments();
			assertTrue("Number of fragments not 3", fragments.size() == 3);

			rewrite.remove((ASTNode) fragments.get(0), null);
			rewrite.remove((ASTNode) fragments.get(1), null);

			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k2"));
			frag.setInitializer(null);

			rewrite.replace((ASTNode) fragments.get(2), frag, null);
		}
		{	// remove modifiers
			FieldDeclaration decl= fieldDeclarations[2];

			// change modifier
			int newModifiers= 0;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    final boolean i1= 1, k1;\n");
		buf.append("    static final transient int k2;\n");
		buf.append("    int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testInitializer_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    {\n");
		buf.append("        foo();\n");
		buf.append("    }\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");

		List declarations= type.bodyDeclarations();
		assertTrue("Number of fieldDeclarations not 2", declarations.size() == 2);
		{	// change modifier, replace body
			Initializer initializer= (Initializer) declarations.get(0);

			// add modifier
			int newModifiers= Modifier.STATIC;
			rewrite.set(initializer, Initializer.MODIFIERS_PROPERTY, new Integer(newModifiers), null);


			Block block= ast.newBlock();
			block.statements().add(ast.newReturnStatement());

			rewrite.replace(initializer.getBody(), block, null);
		}
		{	// change modifier
			Initializer initializer= (Initializer) declarations.get(1);

			int newModifiers= 0;
			rewrite.set(initializer, Initializer.MODIFIERS_PROPERTY, new Integer(newModifiers), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    static {\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}


	public void testMethodDeclarationParamShuffel_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			List params= methodDecl.parameters();

			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);
			rewrite.replace(first.getName(), ast.newSimpleName("x"), null);
			rewrite.replace(second.getName(), ast.newSimpleName("y"), null);

			ASTNode copy1= rewrite.createCopyTarget(first);
			ASTNode copy2= rewrite.createCopyTarget(second);

			rewrite.replace(first, copy2, null);
			rewrite.replace(second, copy1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean y, int x) { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testMethodDeclarationParamShuffel1_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			List params= methodDecl.parameters();

			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);

			ASTNode copy2= rewrite.createCopyTarget(second);

			rewrite.replace(first, copy2, null);
			rewrite.remove(second, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean b) { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodDeclaration_bug24916_only_2_3_4() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private int DD()[]{\n");
		buf.append("    };\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "DD");

			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, new Integer(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private DD(){\n");
		buf.append("    };\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments2_only_2_3_4() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ASTNode node= rewrite.createCopyTarget(methodDecl);

			ASTNode firstDecl= (ASTNode) type.bodyDeclarations().get(0);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAfter(node, firstDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testBUG_38447() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("\n"); // missing closing bracket
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments4_only_2_3_4() throws Exception {


		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			ASTNode copy= rewrite.createCopyTarget(methodDecl);

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(copy, null);

			MethodDeclaration newMethodDecl= createNewMethod(astRoot.getAST(), "xoo", false);
			rewrite.replace(methodDecl, newMethodDecl, null);

			//MethodDeclaration methodDecl2= findMethodDeclaration(type, "foo1");
			//rewrite.markAsReplaced(methodDecl2, copy);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void xoo(String str) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	/** @deprecated using deprecated code */
	public void testInsertFieldAfter_only_2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("fColor"));
			FieldDeclaration newField= ast.newFieldDeclaration(frag);
			newField.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			newField.setModifiers(Modifier.PRIVATE);

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAt(newField, 1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("    private char fColor;\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testVarArgs_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object... args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, Object[] args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(PrimitiveType.INT), null);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, int... args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testAnnotationTypeMember_since_4() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public @interface DD {\n");
		buf.append("    public String value1();\n");
		buf.append("    String value2() default 1;\n");
		buf.append("    String value3() default 2;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AnnotationTypeDeclaration type= (AnnotationTypeDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(0);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(PrimitiveType.BOOLEAN), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.NAME_PROPERTY, ast.newSimpleName("test"), null);

			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, ast.newNumberLiteral("1"), null);
		}
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(1);
			rewrite.getListRewrite(methodDecl, AnnotationTypeMemberDeclaration.MODIFIERS2_PROPERTY).insertFirst(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, ast.newNumberLiteral("2"), null);
		}
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(2);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, null, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public @interface DD {\n");
		buf.append("    boolean test() default 1;\n");
		buf.append("    public String value2() default 2;\n");
		buf.append("    String value3();\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration1_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1(1), E2, E3(), E4(1, 2)\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(0);
			rewrite.set(enumConst, EnumConstantDeclaration.NAME_PROPERTY, ast.newSimpleName("X"), null);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.remove((ASTNode) enumConst.arguments().get(0), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(1);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(ast.newNumberLiteral("1"), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(ast.newNumberLiteral("2"), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(3);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.remove((ASTNode) enumConst.arguments().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    X, E2(1), E3(2), E4(2)\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1Add(1),\n");
		buf.append("    E2Add,\n");
		buf.append("    E3Add(1),\n");
		buf.append("    E4Add(1),\n");
		buf.append("    E5Add(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E1Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E2Remove {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E3Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E4Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(0);
			assertNull(enumConst.getAnonymousClassDeclaration());

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(1);
			assertNull(enumConst.getAnonymousClassDeclaration());

			ListRewrite argsRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			argsRewrite.insertFirst(ast.newNumberLiteral("1"), null);

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);

		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			assertNull(enumConst.getAnonymousClassDeclaration());

			rewrite.remove((ASTNode) enumConst.arguments().get(0), null);

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(3);
			assertNull(enumConst.getAnonymousClassDeclaration());

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(4);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(5);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(6);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			ListRewrite argsRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			argsRewrite.insertFirst(ast.newNumberLiteral("1"), null);

			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(7);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove((ASTNode) enumConst.arguments().get(0), null);
			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(8);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove((ASTNode) classDecl.bodyDeclarations().get(0), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E2Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E3Add {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E4Add(1) {\n");
		buf.append("    },\n");
		buf.append("    E5Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E1Remove(1),\n");
		buf.append("    E2Remove(1),\n");
		buf.append("    E3Remove,\n");
		buf.append("    E4Remove(1) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration_bug114119_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    RED, BROWN(), GREEN(){};\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			assertNotNull(enumConst.getAnonymousClassDeclaration());

			rewrite.remove(enumConst.getAnonymousClassDeclaration(), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    RED, BROWN(), GREEN();\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testMethodDeclChangesBug77538() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("// comment\n");
		buf.append("public class A {\n");
		buf.append("	public int foo() {\n");
		buf.append("		return 0;\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		// Get method declaration and its body
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block body = methodDecl.getBody();

	   // start record of the modifications
	   astRoot.recordModifications();

	   // Modify method body
		Block newBody = ast.newBlock();
		methodDecl.setBody(newBody);
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("lock"));
		fragment.setInitializer(ast.newQualifiedName(ast.newSimpleName("OS"), ast.newSimpleName("lock")));
		VariableDeclarationExpression variableDeclarationExpression = ast.newVariableDeclarationExpression(fragment);
		variableDeclarationExpression.setType(ast.newSimpleType(ast.newSimpleName("Lock")));
		ExpressionStatement expressionStatement = ast.newExpressionStatement(variableDeclarationExpression);
		newBody.statements().add(expressionStatement);
		TryStatement tryStatement = ast.newTryStatement();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName("lock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		ExpressionStatement expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		body.statements().add(0, expressionStatement2);
		tryStatement.setBody(body);
		Block finallyBlock = ast.newBlock();
		tryStatement.setFinally(finallyBlock);
		methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName("unLock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		finallyBlock.statements().add(expressionStatement2);
		newBody.statements().add(tryStatement);

		// Verify that body extended length does not become negative!
		assertFalse("Invalid extended length for "+body, astRoot.getExtendedLength(body)<0);
	}

	public void testAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@An\n");
		buf.append("@An()\n");
		buf.append("@An(val=1, val=2)\n");
		buf.append("@An(val=1, val=2)\n");
		buf.append("@An(1)\n");
		buf.append("class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List modifiers= type.modifiers();
		assertEquals(5, modifiers.size());
		{
			MarkerAnnotation an= (MarkerAnnotation) modifiers.get(0);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, MarkerAnnotation.TYPE_NAME_PROPERTY, newName, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(1);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);

			MemberValuePair newPair= ast.newMemberValuePair();
			newPair.setName(ast.newSimpleName("foo"));
			newPair.setValue(ast.newNumberLiteral("0"));

			listRewrite.insertFirst(newPair, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(2);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			List values= an.values();

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);
			listRewrite.remove((ASTNode) values.get(0), null);

			MemberValuePair p= (MemberValuePair) values.get(1);
			SimpleName newMember= ast.newSimpleName("Y");
			SimpleName newValue= ast.newSimpleName("Z");
			rewrite.set(p, MemberValuePair.NAME_PROPERTY, newMember, null);
			rewrite.set(p, MemberValuePair.VALUE_PROPERTY, newValue, null);

			MemberValuePair newPair= ast.newMemberValuePair();
			newPair.setName(ast.newSimpleName("foo"));
			newPair.setValue(ast.newNumberLiteral("0"));

			listRewrite.insertLast(newPair, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(3);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			List values= an.values();

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);
			listRewrite.remove((ASTNode) values.get(0), null);
			listRewrite.remove((ASTNode) values.get(1), null);
		}
		{
			SingleMemberAnnotation an= (SingleMemberAnnotation) modifiers.get(4);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, SingleMemberAnnotation.TYPE_NAME_PROPERTY, newName, null);
			rewrite.set(an, SingleMemberAnnotation.VALUE_PROPERTY, ast.newBooleanLiteral(true), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("@X\n");
		buf.append("@X(foo = 0)\n");
		buf.append("@X(Y=Z, foo = 0)\n");
		buf.append("@X()\n");
		buf.append("@X(true)\n");
		buf.append("class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testParameterAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@A int a, @B1 @B2 int b, int c, @D int d) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		MethodDeclaration methodDecl= (MethodDeclaration) findTypeDeclaration(astRoot, "E").bodyDeclarations().get(0);
		List params= methodDecl.parameters();
		assertEquals(4, params.size());
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(0);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(1);

			rewrite.remove((ASTNode) decl.modifiers().get(0), null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(2);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(3);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			rewrite.replace((ASTNode) decl.modifiers().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@X @A int a, @B2 int b, @X int c, @X int d) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
		
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, JavaCore.INSERT);
		
		preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@X\n    @A int a, @B2 int b, @X\n    int c, @X int d) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testExtraDimwithAnnotations_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo2()[] { return null; }\n");
		buf.append("    public Object foo3() @Annot1 [] @Annot2 [] { return null; }\n");
		buf.append("    public Object foo4()@Annot1 [] @Annot2 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5() @Annot1 []   @Annot2 [] { return null; }\n");
		buf.append("    public Object foo6(int i)  @Annot1 [] @Annot2 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i) @Annot1 []  @Annot2 [] { return null; }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n"); 
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSION_INFOS_PROPERTY);
			ExtraDimension dim= ast.newExtraDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 1, null);

			dim= ast.newExtraDimension();
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 2, null);
			
			ASTNode exception = (ASTNode) methodDecl.thrownExceptionTypes().get(0);
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY).remove(exception, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSION_INFOS_PROPERTY);

			ExtraDimension dim= ast.newExtraDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			listRewrite.insertAt(dim, 1, null);
			
			Type exception= ast.newSimpleType(ast.newSimpleName("ArrayStoreException"));
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY).insertFirst(exception, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSION_INFOS_PROPERTY);

			ExtraDimension dim= ast.newExtraDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			ExtraDimension dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			listRewrite.insertAt(markerAnnotation, 0, null);

			dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(1);
			listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			listRewrite.insertAt(markerAnnotation, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			ExtraDimension dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(0);
			Annotation annot= (Annotation) dim.annotations().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);

			dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(1);
			listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);
			listRewrite.insertAt(annot, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			ExtraDimension dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(0);
			Annotation annot= (Annotation) dim.annotations().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);

			dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(1);
			annot= (Annotation) dim.annotations().get(0);
			listRewrite= rewrite.getListRewrite(dim, ExtraDimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSION_INFOS_PROPERTY);
			ExtraDimension dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(0);
			listRewrite.remove(dim, null);
			dim= (ExtraDimension) methodDecl.extraDimensionInfos().get(1);
			listRewrite.remove(dim, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] @Annot1 [] @Annot2 [] { return null; }\n");
		buf.append("    public Object foo2()[] @Annot1 [] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo3() @Annot1 [] @Annot1 @Annot2 [] @Annot2 [] { return null; }\n");
		buf.append("    public Object foo4()@Annot2 @Annot1 [] @Annot2 @Annot1 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5()  []   @Annot2 @Annot1 [] { return null; }\n");
		buf.append("    public Object foo6(int i)   []  [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i) { return null; }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n"); 
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		assertEqualString(preview, buf.toString());
	}
}
