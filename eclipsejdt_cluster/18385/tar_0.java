/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
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
	
	class BindingResolver extends TestASTRequestor {
		String bindingKey;
		int index = -1;
		String foundKey;
		MarkerInfo[] markerInfos;
		public BindingResolver(MarkerInfo[] markerInfos) {
			this.markerInfos = markerInfos;
		}
		public void acceptAST(ICompilationUnit source, CompilationUnit cu) {
			super.acceptAST(source, cu);
			ASTNode node = findNode(cu, this.markerInfos[++this.index]);
			if (node != null && !(node instanceof CompilationUnit)) {
				IBinding binding = null;
				switch (node.getNodeType()) {
					case ASTNode.PACKAGE_DECLARATION:
						binding = ((PackageDeclaration) node).resolveBinding();
						break;
					case ASTNode.TYPE_DECLARATION:
						binding = ((TypeDeclaration) node).resolveBinding();
						break;
					case ASTNode.ANONYMOUS_CLASS_DECLARATION:
						binding = ((AnonymousClassDeclaration) node).resolveBinding();
						break;
					case ASTNode.TYPE_DECLARATION_STATEMENT:
						binding = ((TypeDeclarationStatement) node).resolveBinding();
						break;
					case ASTNode.METHOD_DECLARATION:
						binding = ((MethodDeclaration) node).resolveBinding();
						break;
					case ASTNode.METHOD_INVOCATION:
						binding = ((MethodInvocation) node).resolveMethodBinding();
						break;
					case ASTNode.TYPE_PARAMETER:
						binding = ((TypeParameter) node).resolveBinding();
						break;
					case ASTNode.PARAMETERIZED_TYPE:
						binding = ((ParameterizedType) node).resolveBinding();
						break;
					case ASTNode.WILDCARD_TYPE:
						binding = ((WildcardType) node).resolveBinding();
						break;
					case ASTNode.SIMPLE_NAME:
						binding = ((SimpleName) node).resolveBinding();
						break;
				}
				this.bindingKey = binding == null ? null : binding.getKey();
				
				// case of a capture binding
				if (this.bindingKey != null && this.bindingKey.indexOf('!') != -1 && binding.getKind() == IBinding.METHOD) {
					this.bindingKey = ((IMethodBinding) binding).getReturnType().getKey();
				}
			}
		}
		public void acceptBinding(String key, IBinding binding) {
			super.acceptBinding(key, binding);
			this.foundKey = binding == null ? null : binding.getKey();
		}
	}
	
	public WorkingCopyOwner owner = new WorkingCopyOwner() {};

	public BatchASTCreationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(BatchASTCreationTests.class);
	}
	
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "test059" };
//		TESTS_NUMBERS = new int[] { 83230 };
//		TESTS_RANGE = new int[] { 83304, -1 };
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
		BindingResolver resolver = requestBinding(pathAndSources, expectedKey);
		
		if (!expectedKey.equals(resolver.bindingKey))
			System.out.println(Util.displayString(resolver.bindingKey, 3));
		assertEquals("Unexpected binding for marked node", expectedKey, resolver.bindingKey);
		
		if (!expectedKey.equals(resolver.foundKey)) {
			System.out.println(Util.displayString(resolver.foundKey, 3));
		}
		assertEquals("Unexpected binding found by acceptBinding", expectedKey, resolver.foundKey);
	}

	/*
	 * Creates working copies from the given path and sources.
	 * Resolves a dummy cu as a batch and on the first accept, create a binding with the expected key using ASTRequestor#createBindings.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertBindingCreated(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		ICompilationUnit[] copies = null;
		try {
			copies = createWorkingCopies(pathAndSources);
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
			
			String actualKey = requestor.createdBindingKey;
			if (!expectedKey.equals(actualKey)) {
				BindingResolver resolver = requestBinding(pathAndSources, null);
				if (resolver.bindingKey != null) {
					if (!expectedKey.equals(resolver.bindingKey))
						System.out.println(Util.displayString(resolver.bindingKey, 3));
					assertEquals("Inconsistent expected key ", expectedKey, resolver.bindingKey);
				} 
				System.out.println(Util.displayString(actualKey, 3));
			}
			assertEquals("Unexpected created binding", expectedKey, actualKey);
		} finally {
			discardWorkingCopies(copies);
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
	
	private BindingResolver requestBinding(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		ICompilationUnit[] copies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
			copies = createWorkingCopies(markerInfos, this.owner);
			BindingResolver resolver = new BindingResolver(markerInfos);
			resolveASTs(copies, expectedKey == null ? new String[0] : new String[] {expectedKey}, resolver, getJavaProject("P"), this.owner);
			return resolver;
		} finally {
			discardWorkingCopies(copies);
		}
	}

	
	/*
	 * Tests the batch creation of 2 ASTs without resolving.
	 */
	public void test001() throws CoreException {
		this.workingCopies = createWorkingCopies(new String[] {
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
		createASTs(this.workingCopies, requestor);
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
	}
	
	/*
	 * Tests the batch creation of 2 ASTs with resolving.
	 */
	public void test002() throws CoreException {
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
		this.workingCopies = createWorkingCopies(markerInfos, this.owner);
		TestASTRequestor requestor = new TestASTRequestor();
		resolveASTs(this.workingCopies, requestor);
		
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
	}

	/*
	 * Ensures that ASTs that are required by original source but were not asked for are not handled.
	 */
	public void test003() throws CoreException {
		ICompilationUnit[] otherWorkingCopies = null;
		try {
			this.workingCopies = createWorkingCopies(new String[] {
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
			resolveASTs(this.workingCopies, requestor);
			
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			// Note: this.workingCopies are discarded in tearDown
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
			"Lp1/X;^1");
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
			"Lp1/Y;^1");
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
			"Lp1/X$Member;^0");
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
			"Lp1/X$Member1$Member2;^0");
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
			"Lp1/X$52;^1");
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
			"Lp1/X$54;^1");
	}

	/*
	 * Ensures that a package binding can be created using its key in ASTRequestor#createBindings.
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
	 * Ensures that a type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X;^1");
	}

	/*
	 * Ensures that a type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/Y;^1");
	}
	
	/*
	 * Ensures that a member type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X$Member;^0");
	}

	/*
	 * Ensures that a member type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X$Member1$Member2;^0");
	}
	
	/*
	 * Ensures that an anonymous type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test016() throws CoreException {
		assertBindingCreated(
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
			"Lp1/X$52;^1");
	}
	
	/*
	 * Ensures that a local type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test017() throws CoreException {
		assertBindingCreated(
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
			"Lp1/X$54;^1");
	}
	
	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X;.foo()V^0");
	}
	
	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X;.foo(Ljava/lang/Object;)V^0");
	}
	
	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X;.(Ljava/lang/Object;)V^0");
	}

	/*
	 * Ensures that a field binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X;.field^0");
	}

	/*
	 * Ensures that a base type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test022() throws CoreException {
		assertBindingCreated(new String[0],"I");
	}
	
	/*
	 * Ensures that an array binding can be created using its key in ASTRequestor#createBindings.
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
	 * Ensures that an array binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test024() throws CoreException {
		assertBindingCreated(new String[0],"[[I");
	}
	
	/* 
	 * Ensures that a method binding in an anonymous type with several kind of parameters can be created using its key in ASTRequestor#createBindings
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
				"      /*start*/void bar(int i, X x, String[][] s, Y[] args, boolean b, Object o) {\n" +
				"      }/*end*/\n" +
				"    };\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X$68;.bar(ILp1/X;[[Ljava/lang/String;[Lp1/X$Y;ZLjava/lang/Object;)V^0"
		);
	}
	
	/*
	 * Ensures that a generic type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test026() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"}",
			},
			"Lp1/X<TT;>;^1");
	}

	/*
	 * Ensures that a generic type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<TT;TU;>;^1");
	}

	/*
	 * Ensures that a parameterized type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<Ljava/lang/String;>;^1");
	}

	/*
	 * Ensures that a member parameterized type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y<Ljava/lang/String;>;^0");
	}

	/*
	 * Ensures that a raw type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<>;^1");
	}

	/*
	 * Ensures that a member raw type binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y<>;^0");
	}
	
	/* 
	 * Ensures that a parameterized method binding can be created using its key in ASTRequestor#createBindings
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
			"Lp1/X;.foo<T:Ljava/lang/Object;>()V^0");
	}

	/* 
	 * Ensures that a local variable binding can be created using its key in ASTRequestor#createBindings
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
			"Lp1/X;.foo()V#i^0");
	}

	/* 
	 * Ensures that a local variable binding can be created using its key in ASTRequestor#createBindings
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
			"Lp1/X;.foo()V#1#b^0");
	}

	/*
	 * Ensures that a parameterized method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<Ljava/lang/String;>;.foo(TT;)V^0");
	}

	/*
	 * Ensures that a parameterized generic method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<Ljava/lang/String;>;.foo<U:Ljava/lang/Object;>(TT;TU;)V^0%<Lp1/X<>;>");
	}

	/*
	 * Ensures that a raw generic method binding can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<>;.foo<U:Ljava/lang/Object;>(TT;TU;)V^0");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an unbound wildcard) can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<*>;.foo()V^0");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an extends wildcard) can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<+Ljava/lang/Object;>;.foo()V^0");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is a super wildcard) can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<-Ljava/lang/Error;>;.foo()V^0");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameters contain wildcards) can be created using its key in ASTRequestor#createBindings.
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
			"Lp1/X<-Ljava/lang/Error;*Ljava/lang/String;+Ljava/lang/Object;>;.foo()V^0");
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
			String[] bindingKeys = 
				new String[] {
					"LX;^1",
					"LX;.field^0"
				};
			resolveASTs(
				new ICompilationUnit[] {workingCopy}, 
				bindingKeys,
				requestor,
				getJavaProject("P"),
				workingCopy.getOwner()
			);
			assertBindingsEqual(
				"LX;^1\n" + 
				"LX;.field^0",
				requestor.getBindings(bindingKeys));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that a source parameterized type binding (where the parameters contain wildcard with a super bound) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test043() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  X<? super T> field;\n" +
				"}",
			},
			"Lp1/X<-Lp1/X<TT;>;:TT;>;^1");
	}
	
	/*
	 * Ensures that a binary parameterized type binding (where the parameters contain wildcard with a super bound) can be created using its key in ASTRequestor#createBindings.
	 * (regression test for 83499 ClassCastException when restoring ITypeBinding from key)
	 */
	public void test044() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<E> {\n" +
				"  Class<? extends E> field;\n" +
				"}",
			},
			"Ljava/lang/Class<+Lp1/X<TE;>;:TE;>;^33");
	}
	
	/*
	 * Ensures that restoring a second key that references a type in a first key doesn't throw a NPE
	 * (regression test for bug 83499 NPE when restoring ITypeBinding from key)
	 */
	public void test045() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y<E> {\n" +
				"}"
			},
			new String[] {
				"Lp1/X;^1",
				"Lp1/Y<+Lp1/X;>;^1"
			}
		);
		assertBindingsEqual(
			"Lp1/X;^1\n" +
			"Lp1/Y<+Lp1/X;>;^1",
			bindings);
	}

	/*
	 * Ensures that a binary array parameterized type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test046() throws CoreException {
		assertBindingCreated(
			new String[] {},
			"[Ljava/lang/Class<Ljava/lang/Object;>;");
	}
	
	/*
	 * Ensures that the null type binding can be created using its key in batch creation.
	 */
	public void test047() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"N"});
		assertBindingsEqual(
				"N", 
				bindings);
	}

	/*
	 * Ensures that a binary array type binding can be created using its key in batch creation.
	 */
	public void test048() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"[Ljava/lang/Object;"});
		assertBindingsEqual(
				"[Ljava/lang/Object;", 
				bindings);
	}

	/*
	 * Ensures that a type variable binding can be created using its key in batch creation.
	 */
	public void test049() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Ljava/lang/Class<TT;>;:TT;"});
		assertBindingsEqual(
				"Ljava/lang/Class<TT;>;:TT;", 
				bindings);
	}
	
	/*
	 * Ensures that a parameterized type binding with a wildcard that extends an array the can be created using its key in batch creation.
	 */
	public void test050() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Ljava/lang/Class<+[Ljava/lang/Object;>;"});
		assertBindingsEqual(
				"Ljava/lang/Class<+[Ljava/lang/Object;>;^33", 
				bindings);
	}
	
	/*
	 * Ensures that attempting to create a top level type that doesn't exist using its key i in batch creation.
	 * returns null.
	 */
	public void test051() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Lp1/DoesNotExist;"});
		assertBindingsEqual(
				"<null>", 
				bindings);
	}
	
	/*
	 * Ensures that a secondary type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test052() throws CoreException {
		try {
			createFolder("/P/p1");
			createFile(
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}\n" +
				"class Y {\n" +
				"}"
			);
			assertBindingCreated(new String[] {}, "Lp1/X~Y;^0");
		} finally {
			deleteFolder("/P/p1");
		}
	}

	/*
	 * Ensures that an anonymous type binding coming from secondary type can be created using its key in ASTRequestor#createBindings.
	 */
	public void test053() throws CoreException {
		try {
			createFolder("/P/p1");
			createFile(
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}\n" +
				"class Y {\n" +
				"  void foo() {\n" +
				"    new Y() {};\n" +
				"  }\n" +
				"}"
			);
			assertBindingCreated(new String[] {}, "Lp1/X~Y$64;^0");
		} finally {
			deleteFolder("/P/p1");
		}
	}

	/*
	 * Ensures that an anonymous type binding inside a local type can be retrieved using its key.
	 */
	public void test054() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"      void bar() {\n" +
				"        new X() /*start*/{\n" +
				"        }/*end*/;" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}",
			}, 
			"Lp1/X$89;^1"
		);
	}
	
	/*
	 * Ensures that a parameterized generic method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test055() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    /*start*/new X<String>().foo(new X() {})/*end*/;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo<U:Ljava/lang/Object;>(TU;)V^0%<Lp1/X<TT;>$101;>"
		);
	}

	/*
	 * Ensures that creating a binary member type binding returns the correct binding
	 * (regression test for bug 86967 [1.5][dom] NPE in BindingKeyResolver for multi-level parameterized type binding)
	 */
	public void test056() throws CoreException, IOException {
		try {
			IJavaProject project = createJavaProject("BinaryProject", new String[0], new String[] {"JCL15_LIB"}, "", "1.5");
			addLibrary(project, "lib.jar", "src.zip", new String[] {
				"/BinaryProject/p/X.java",
				"package p;\n" +
				"public class X<K, V> {\n" +
				"  public class Y<K1, V1> {\n" +
				"  }\n" +
				"}"
			}, "1.5");
			ITypeBinding[] bindings = createTypeBindings(new String[0], new String[] {
				"Lp/X$Y<Lp/X<TK;TV;>;:TK;Lp/X<TK;TV;>;:TV;>;^1"
			}, project);
			assertBindingsEqual(
				"Lp/X$Y<Lp/X<TK;TV;>;:TK;Lp/X<TK;TV;>;:TV;>;^1",
				bindings);
		} finally {
			deleteProject("BinaryProject");
		}
	}

	/*
	 * Ensures that creating a missing binary member type binding doesn't throw a NPE
	 * (regression test for bug 86967 [1.5][dom] NPE in BindingKeyResolver for multi-level parameterized type binding)
	 */
	public void test057() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Lp/Missing$Member;"});
		assertBindingsEqual(
			"<null>",
			bindings);
	}
	
	/*
	 * Ensures that a type parameter binding can be created using its key in batch creation.
	 * (regression test for bug 87050 ASTParser#createASTs(..) cannot resolve method type parameter binding from key)
	 */
	public void test058() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  </*start*/T/*end*/> void foo(T t) {\n" +
				"  }" +
				"}",
			}, 
			"Lp1/X;.foo<T:Ljava/lang/Object;>(TT;)V:TT;");
	}
	
	public void test059() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" + 
				"    Object foo(X<?> list) {\n" + 
				"       return /*start*/list.get()/*end*/;\n" + 
				"    }\n" + 
				"    T get() {\n" + 
				"    	return null;\n" + 
				"    }\n" + 
				"}",
			}, 
			"Lp1/X<TT;>;!*77;");
	}



}
