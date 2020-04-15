/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import junit.framework.Test;

public class LocalElementTests extends ModifyingResourceTests {
	
	public LocalElementTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new Suite(LocalElementTests.class);
	}

	public void setUpSuite() throws Exception {
		createJavaProject("P");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("P");
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType1() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    void foo()\n" + 
				"      class <anonymous #1>\n" + 
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType2() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  public class Y {\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"    run(new Y() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    class Y\n" +
				"    void foo()\n" + 
				"      class <anonymous #1>\n" + 
				"      class <anonymous #2>\n" + 
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType3() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"      void bar() {\n" +
				"        run(new X() {\n" +
				"        });\n" +
				"      }\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    void foo()\n" + 
				"      class <anonymous #1>\n" + 
				"        void bar()\n" +
				"          class <anonymous #1>\n" + 
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType4() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  {\n" +
				"      field = new Vector() {\n" +
				"      };\n" +
				"  }\n" +
				"  Object field = new Object() {\n" +
				"  };\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    <initializer #1>\n" + 
				"      class <anonymous #1>\n" + 
				"    Object field\n" + 
				"      class <anonymous #1>\n" + 
				"    void foo()\n" + 
				"      class <anonymous #1>\n" + 
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}
	
	/*
	 * Anonymous type test.
	 * (regression test for bug 69028 Anonymous type in argument of super() is not in type hierarchy)
	 */
	public void testAnonymousType5() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  X(Object o) {\n" +
				"  }\n" +
				"}\n" +
				"class Y extends X {\n" +
				"  Y() {\n" +
				"    super(new Object() {});\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    X(Object)\n" + 
				"  class Y\n" + 
				"    Y()\n" + 
				"      class <anonymous #1>",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}
	
	/*
	 * IType.getSuperclassName() test
	 */
	public void testGetSuperclassName() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			IType type = cu.getType("X").getMethod("foo", new String[0]).getType("", 1);
			assertEquals(
				"Unexpected superclass name",
				"X",
				type.getSuperclassName());
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * IMember.getType(...) test
	 */
	public void testGetType() {
		ICompilationUnit cu = getCompilationUnit("P/X.java");
		IType topLevelType = cu.getType("X");
		IJavaElement[] types = new IJavaElement[5];
		types[0] = topLevelType.getInitializer(1).getType("", 1);
		types[1] = topLevelType.getInitializer(1).getType("Y", 1);
		types[2] = topLevelType.getField("f").getType("", 1);
		types[3] = topLevelType.getMethod("foo", new String[] {"I", "QString;"}).getType("", 1);
		types[4] = topLevelType.getMethod("foo", new String[] {"I", "QString;"}).getType("Z", 1);
		assertElementsEqual(
			"Unexpected types",
			"<anonymous #1> [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" + 
			"Y [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" + 
			"<anonymous #1> [in f [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" + 
			"<anonymous #1> [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" + 
			"Z [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]",
			types);
	}
	
	/*
	 * Local type test.
	 */
	public void testLocalType1() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    void foo()\n" + 
				"      class Y",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType2() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    }\n" +
				"    class Z {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    void foo()\n" + 
				"      class Y\n" + 
				"      class Z", 
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType3() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"      void bar() {\n" +
				"        class Z {\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    void foo()\n" + 
				"      class Y\n" + 
				"        void bar()\n" +
				"          class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType4() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  {\n" +
				"      class Y {\n" +
				"      }\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    class Z {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" + 
				"  class X\n" + 
				"    <initializer #1>\n" + 
				"      class Y\n" + 
				"    void foo()\n" + 
				"      class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

}
