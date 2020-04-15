/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Basic tests of the image builder.
 */
public class CopyResourceTests extends BuilderTests {
	
	public CopyResourceTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildTestSuite(CopyResourceTests.class);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=117302
	public void testFilteredResources() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(src, "x", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package x;"+ //$NON-NLS-1$
			"public class A extends q.B {}" //$NON-NLS-1$
		);
		env.addClass(src, "q", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package q;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
		);
		env.addFile(src.append("q"), "test.txt", "test file"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		org.eclipse.jdt.core.IJavaProject p = env.getJavaProject("P");
		java.util.Map options = p.getOptions(true);
		options.put(org.eclipse.jdt.core.JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, "q*"); //$NON-NLS-1$
		p.setOptions(options);

		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1;
			fullBuild();
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
		}
		expectingNoProblems();
		expectingNoPresenceOf(projectPath.append("bin/q/test.txt")); //$NON-NLS-1$
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$
		expectingPresenceOf(p.append("p.txt")); //$NON-NLS-1$
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		env.addFile(src, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});

		env.removeFile(src2.append("zz.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src2, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src2/p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/z.txt"), //$NON-NLS-1$
			projectPath.append("bin") //$NON-NLS-1$
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/z.txt"), //$NON-NLS-1$
			projectPath.append("bin2/zz.txt"), //$NON-NLS-1$
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("bin1/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin2/z.txt"), //$NON-NLS-1$
		});
	}

	public void test2ProjectWith1Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P7"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath projectPath2 = env.addProject("P8"); //$NON-NLS-1$
		IPath binLocation = env.getProject(projectPath).getFolder("bin").getLocation(); //$NON-NLS-1$
		env.setExternalOutputFolder(projectPath2, "externalBin", binLocation); //$NON-NLS-1$
		env.addExternalJars(projectPath2, Util.getJavaClassLibs());

		env.addFile(projectPath2, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("z.txt")); //$NON-NLS-1$
	}
}
