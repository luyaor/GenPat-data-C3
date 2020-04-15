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
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic efficiency tests of the image builder.
 */
public class EfficiencyTests extends Tests {
	public EfficiencyTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(EfficiencyTests.class);
	}
	
	public void testEfficiency() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Collaborator", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Collaborator extends Indicted{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild(projectPath);
		
		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"   public abstract void foo();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p2.Collaborator", "p1.Indicted"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.Indicted", "p2.Collaborator"}); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testMethodAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void bar(){}	\n" + //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p2.Y"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.X", "p2.Y" }); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testLocalTypeAddition() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" }); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testLocalTypeAddition2() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1", "p1.X$2"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1", "p1.X$2" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testLocalTypeRemoval() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X"}); //$NON-NLS-1$
		expectingCompilingOrder(new String[]{"p1.X" }); //$NON-NLS-1$
	}

	public void testLocalTypeRemoval2() throws JavaModelException {

		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new Object(){	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Y extends X{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.addClass(root, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Z{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild(projectPath);
		
		env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		new X(){	\n" + //$NON-NLS-1$
			"			void bar(){}	\n" + //$NON-NLS-1$
			"		};	\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);

		expectingCompiledClasses(new String[]{"p1.X", "p1.X$1"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.X", "p1.X$1" }); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
