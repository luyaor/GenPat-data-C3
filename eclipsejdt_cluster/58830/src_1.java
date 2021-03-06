/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class BatchASTCreationTests extends AbstractASTTests {
	
	public class TestASTRequestor extends ASTRequestor {
		public ArrayList asts = new ArrayList();
		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			this.asts.add(ast);
		}
		public void acceptBinding(String bindingKey, IBinding binding) {
		}
	}
	
	public WorkingCopyOwner owner = new WorkingCopyOwner() {};

	public BatchASTCreationTests(String name) {
		super(name);
	}

	public static Test suite() {
		if (false) {
			Suite suite = new Suite(BatchASTCreationTests.class.getName());
			suite.addTest(new BatchASTCreationTests("test036"));
			return suite;
		}
		return new Suite(BatchASTCreationTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}
	
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}
	
	/*
	 * Resolves the given cus and binding key as a batch. 
	 * While resolving, for the ASTNode that is marked, ensures that its binding key is the expected one.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertRequestedBindingFound(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		ICompilationUnit[] workingCopies = null;
		try {
			final MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
			workingCopies = createWorkingCopies(markerInfos, this.owner);
			class Requestor extends TestASTRequestor {
				String bindingKey;
				int index = -1;
				String foundKey;
				public void acceptAST(ICompilationUnit source, CompilationUnit cu) {
					super.acceptAST(source, cu);
					ASTNode node = findNode(cu, markerInfos[++this.index]);
					if (node != null && !(node instanceof CompilationUnit)) {
						IBinding binding = null;
						if (node instanceof PackageDeclaration) {
							binding = ((PackageDeclaration) node).resolveBinding();
						} else if (node instanceof TypeDeclaration) {
							binding = ((TypeDeclaration) node).resolveBinding();
						} else if (node instanceof AnonymousClassDeclaration) {
							binding = ((AnonymousClassDeclaration) node).resolveBinding();
						} else if (node instanceof TypeDeclarationStatement) {
							binding = ((TypeDeclarationStatement) node).resolveBinding();
						}
						this.bindingKey = binding == null ? null : binding.getKey();
					}
				}
				public void acceptBinding(String key, IBinding binding) {
					super.acceptBinding(key, binding);
					this.foundKey = binding.getKey();
				}
			};
			Requestor requestor = new Requestor();
			resolveASTs(workingCopies, new String[] {expectedKey}, requestor, getJavaProject("P"), this.owner);
			
			if (!expectedKey.equals(requestor.bindingKey))
				System.out.println(Util.displayString(expectedKey, 3));
			assertEquals("Unexpected binding for marked node", expectedKey, requestor.bindingKey);
			
			if (!expectedKey.equals(requestor.foundKey)) {
				System.out.println(Util.displayString(requestor.foundKey, 3));
			}
			assertEquals("Unexpected binding found by acceptBinding", expectedKey, requestor.foundKey);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Creates working copies from the given path and sources.
	 * Resolves a dummy cu as a batch and on the first accept, create a binding with the expected key.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertBindingCreated(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		ICompilationUnit[] workingCopies = null;
		try {
			workingCopies = createWorkingCopies(pathAndSources);
			class Requestor extends TestASTRequestor {
				String createdBindingKey;
				public void acceptAST(ICompilationUnit source, CompilationUnit cu) {
					super.acceptAST(source, cu);
					IBinding[] bindings = createBindings(new String[] {expectedKey});
					if (bindings != null && bindings.length > 0 && bindings[0] != null)
						this.createdBindingKey = bindings[0].getKey();
				}
			};
			Requestor requestor = new Requestor();
			ICompilationUnit[] dummyWorkingCopies = null;
			try {
				dummyWorkingCopies = createWorkingCopies(new String[] {
					"/P/Test.java",
					"public class Test {\n" +
					"}"
				});
				resolveASTs(dummyWorkingCopies, new String[] {}, requestor, getJavaProject("P"), this.owner);
			} finally {
				discardWorkingCopies(dummyWorkingCopies);
			}
			
			if (!expectedKey.equals(requestor.createdBindingKey))
				System.out.println(Util.displayString(requestor.createdBindingKey, 3));
			assertEquals("Unexpected created binding", expectedKey, requestor.createdBindingKey);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	private void createASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.createASTs(cus, new String[] {}, requestor, null);
	}
	
	protected ICompilationUnit[] createWorkingCopies(String[] pathAndSources) throws JavaModelException {
		return createWorkingCopies(pathAndSources, this.owner);
	}
	
	private void resolveASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		resolveASTs(cus, new String[0], requestor, getJavaProject("P"), this.owner);
	}
	
	/*
	 * Tests the batch creation of 2 ASTs without resolving.
	 */
	public void test001() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor();
			createASTs(workingCopies, requestor);
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}
	
	/*
	 * Tests the batch creation of 2 ASTs with resolving.
	 */
	public void test002() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends /*start*/Y/*end*/ {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			});
			workingCopies = createWorkingCopies(markerInfos, this.owner);
			TestASTRequestor requestor = new TestASTRequestor();
			resolveASTs(workingCopies, requestor);
			
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
			
			// compare the bindings coming from the 2 ASTs
			Type superX = (Type) findNode((CompilationUnit) requestor.asts.get(0), markerInfos[0]);
			TypeDeclaration typeY = (TypeDeclaration) findNode((CompilationUnit) requestor.asts.get(1), markerInfos[1]);
			IBinding superXBinding = superX.resolveBinding();
			IBinding typeYBinding = typeY.resolveBinding();
			assertTrue("Super of X and Y should be the same", superXBinding == typeYBinding);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Ensures that ASTs that are required by original source but were not asked for are not handled.
	 */
	public void test003() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		ICompilationUnit[] otherWorkingCopies = null;
		try {
			workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
			});
			otherWorkingCopies = createWorkingCopies(new String[] {
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor();
			resolveASTs(workingCopies, requestor);
			
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			discardWorkingCopies(workingCopies);
			discardWorkingCopies(otherWorkingCopies);
		}
	}
	
	/*
	 * Ensures that a package binding can be retrieved using its key.
	 */
	public void test004() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"/*start*/package p1;/*end*/\n" +
				"public class X {\n" +
				"}",
			}, 
			"p1");
	}

	/*
	 * Ensures that a type binding can be retrieved using its key.
	 */
	public void test005() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/public class X extends Y {\n" +
				"}/*end*/",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			}, 
			"Lp1/X;");
	}

	/*
	 * Ensures that a type binding can be retrieved using its key.
	 */
	public void test006() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			}, 
			"Lp1/Y;");
	}
	
	/*
	 * Ensures that a member type binding can be retrieved using its key.
	 */
	public void test007() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/class Member {\n" +
				"  }/*end*/" +
				"}",
			}, 
			"Lp1/X$Member;");
	}

	/*
	 * Ensures that a member type binding can be retrieved using its key.
	 */
	public void test008() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member1 {\n" +
				"    /*start*/class Member2 {\n" +
				"    }/*end*/" +
				"  }\n" +
				"}",
			}, 
			"Lp1/X$Member1$Member2;");
	}
	/*
	 * Ensures that an anonymous type binding can be retrieved using its key.
	 */
	public void test009() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			}, 
			"Lp1/X$1;");
	}
	/*
	 * Ensures that a local type binding can be retrieved using its key.
	 */
	public void test010() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    /*start*/class Y {\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			}, 
			"Lp1/X$1$Y;");
	}

	/*
	 * Ensures that a package binding can be created using its key.
	 */
	public void test011() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			}, 
			"p1");
	}

	/*
	 * Ensures that a type binding can be created using its key.
	 */
	public void test012() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			"Lp1/X;");
	}

	/*
	 * Ensures that a type binding can be created using its key.
	 */
	public void test013() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			"Lp1/Y;");
	}
	
	/*
	 * Ensures that a member type binding can be created using its key.
	 */
	public void test014() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member {\n" +
				"  }" +
				"}",
			},
			"Lp1/X$Member;");
	}

	/*
	 * Ensures that a member type binding can be created using its key.
	 */
	public void test015() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member1 {\n" +
				"    class Member2 {\n" +
				"    }" +
				"  }\n" +
				"}",
			},
			"Lp1/X$Member1$Member2;");
	}
	
	/*
	 * Ensures that an anonymous type binding can be created using its key.
	 */
	public void test016() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    new X() {\n" +
				"    };" +
				"  }\n" +
				"}",
			},
			"Lp1/X$1;");
	}
	
	/*
	 * Ensures that a local type binding can be created using its key.
	 */
	public void test017() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    };" +
				"  }\n" +
				"}",
			},
			"Lp1/X$1$Y;");
	}
	
	/*
	 * Ensures that a method binding can be created using its key.
	 */
	public void test018() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V");
	}
	
	/*
	 * Ensures that a method binding can be created using its key.
	 */
	public void test019() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo(Ljava/lang/Object;)V");
	}
	
	/*
	 * Ensures that a method binding can be created using its key.
	 */
	public void test020() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  X(Object o) {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.(Ljava/lang/Object;)V");
	}

	/*
	 * Ensures that a field binding can be created using its key.
	 */
	public void test021() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  int field;\n" +
				"}",
			},
			"Lp1/X;.field");
	}

	/*
	 * Ensures that a base type binding can be created using its key.
	 */
	public void test022() throws CoreException {
		assertBindingCreated(new String[0],"I");
	}
	
	/*
	 * Ensures that an array binding can be created using its key.
	 */
	public void test023() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			"[Lp1/X;");
	}
	
	/*
	 * Ensures that an array binding can be created using its key.
	 */
	public void test024() throws CoreException {
		assertBindingCreated(new String[0],"[[I");
	}
	
	/* 
	 * Ensures that a method binding in an anonymous type with several kind of parameters can be created using its key
	 */
	public void test025() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Y {\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    new X() {\n" +
				"      void bar(int i, X x, String[][] s, Y[] args, boolean b, Object o) {\n" +
				"      }\n" +
				"    };\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X$1;.bar(ILp1/X;[[Ljava/lang/String;[Lp1/X$Y;ZLjava/lang/Object;)V");
	}
	
	/*
	 * Ensures that a generic type binding can be created using its key.
	 */
	public void test026() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"}",
			},
			"Lp1/X<TT;>;");
	}

	/*
	 * Ensures that a generic type binding can be created using its key.
	 */
	public void test027() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T extends Y & I, U extends Y> {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
				"/P/p1/I.java",
				"package p1;\n" +
				"public interface I {\n" +
				"}",
			},
			"Lp1/X<TT;TU;>;");
	}

	/*
	 * Ensures that a parameterized type binding can be created using its key.
	 */
	public void test028() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  X<String> field;\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;");
	}

	/*
	 * Ensures that a member parameterized type binding can be created using its key.
	 */
	public void test029() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"  class Y<V> {\n" +
				"    X<Error,Exception>.Y<String> field;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y<Ljava/lang/String;>;");
	}

	/*
	 * Ensures that a raw type binding can be created using its key.
	 */
	public void test030() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"   X field;\n" +
				"}",
			},
			"Lp1/X;");
	}

	/*
	 * Ensures that a member raw type binding can be created using its key.
	 */
	public void test031() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"  class Y<V> {\n" +
				"    X<Error,Exception>.Y field;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y;");
	}
	
	/* 
	 * Ensures that a parameterized method binding can be created using its key
	 */
	public void test032() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  <T> void foo() {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo<T:Ljava/lang/Object;>()V");
	}

	/* 
	 * Ensures that a local variable binding can be created using its key
	 */
	public void test033() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    int i;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V#i");
	}

	/* 
	 * Ensures that a local variable binding can be created using its key
	 */
	public void test034() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    int i = 1;\n" +
				"    if (i == 0) {\n" +
				"      int a;\n" +
				"    } else {\n" +
				"      int b;\n" +
				"    }\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V#1#b");
	}

	/*
	 * Ensures that a parameterized method binding can be created using its key.
	 */
	public void test035() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo(T t) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    new X<String>().foo(\"\");\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo(TT;)V");
	}

	/*
	 * Ensures that a parameterized generic method binding can be created using its key.
	 */
	public void test036() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(T t, U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    new X<String>().foo(\"\", this);\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo<U:Ljava/lang/Object;>(TT;TU;)V%<Lp1/X;>");
	}

	/*
	 * Ensures that a raw generic method binding can be created using its key.
	 */
	public void test037() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(T t, U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    new X().foo(\"\", this);\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo<U:Ljava/lang/Object;>(TT;TU;)V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an unbound wildcard) can be created using its key.
	 */
	public void test038() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<?> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<*>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an extends wildcard) can be created using its key.
	 */
	public void test039() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? extends Object> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<+Ljava/lang/Object;>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is a super wildcard) can be created using its key.
	 */
	public void test040() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? super Error> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<-Ljava/lang/Error;>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameters contain wildcards) can be created using its key.
	 */
	public void test041() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T, U, V, W> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? super Error, ?, String, ? extends Object> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<-Ljava/lang/Error;*Ljava/lang/String;+Ljava/lang/Object;>;.foo()V");
	}
	
	/*
	 * Ensures that requesting 2 bindings and an AST for the same compilation unit reports the 2 bindings.
	 */
	public void test042() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy(
				"/P/X.java",
				"public class X {\n" +
				"  int field;\n" +
				"}"
			);
			BindingRequestor requestor = new BindingRequestor();
			resolveASTs(
				new ICompilationUnit[] {workingCopy}, 
				new String[] {
					"LX;",
					"LX;.field"
				},
				requestor,
				getJavaProject("P"),
				workingCopy.getOwner()
			);
			assertBindingsEqual(
				"LX;\n" + 
				"LX;.field",
				requestor.getBindings());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

}
