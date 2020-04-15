/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingPackageDeclTest extends ASTRewritingTest {

	public ASTRewritingPackageDeclTest(String name) {
		super(name);
	}
	public ASTRewritingPackageDeclTest(String name, int apiLevel) {
		super(name, apiLevel);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=328400
	 */
	public void testAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		{ // insert annotation first
			PackageDeclaration packageDeclaration = astRoot.getPackage();
			ListRewrite listRewrite= rewrite.getListRewrite(packageDeclaration, PackageDeclaration.ANNOTATIONS_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Deprecated"));
			listRewrite.insertFirst(annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("@Deprecated\n");
		buf.append("package test1;\n");
		assertEqualString(preview, buf.toString());
	}
}
