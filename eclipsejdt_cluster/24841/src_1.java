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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;

public class ASTStructuralPropertyTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase { 

	public static Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTStructuralPropertyTest.class.getName());
		
		Class c = ASTStructuralPropertyTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTStructuralPropertyTest(methods[i].getName(), AST.LEVEL_2_0));
				suite.addTest(new ASTStructuralPropertyTest(methods[i].getName(), AST.LEVEL_3_0));
			}
		}
		return suite;
	}	
	
	AST ast;
	ASTParser parser;
	int API_LEVEL;

	public ASTStructuralPropertyTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}
	
	protected void setUp() {
		ast = AST.newAST(this.API_LEVEL);
		parser = ASTParser.newParser(this.API_LEVEL);
	}
	
	protected void tearDown() {
		ast = null;
	}
	
	public void testLocationInParent() {
		final ASTNode root = SampleASTs.oneOfEach(ast);
		ASTVisitor v = new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				StructuralPropertyDescriptor me = node.getLocationInParent();
				assertTrue(me != null || (node == root));
			}
		};
		root.accept(v);
	}
		
	public void testStructuralProperties() {
		final ASTNode root = SampleASTs.oneOfEach(ast);
		
		final Set simpleProperties = new HashSet(400);
		final Set childProperties = new HashSet(400);
		final Set childListProperties = new HashSet(400);
		final Set visitedProperties = new HashSet(400);
		final Set nodeClasses = new HashSet(100);
		
		ASTVisitor v = new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				StructuralPropertyDescriptor me = node.getLocationInParent();
				if (me != null) {
					visitedProperties.add(me);
				}
				visitedProperties.add(me);
				nodeClasses.add(node.getClass());
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					Object o = node.getStructuralProperty(p);
					if (p.isSimpleProperty()) {
						simpleProperties.add(p);
						// slam simple properties
						node.setStructuralProperty(p, o);
					} else if (p.isChildProperty()) {
						childProperties.add(p);
						// replace child with a copy
						ASTNode copy = ASTNode.copySubtree(ast, (ASTNode) o);
						node.setStructuralProperty(p, copy);
					} else if (p.isChildListProperty()) {
						childListProperties.add(p);
						// replace child list with copies
						List list = (List) o;
						List copy = ASTNode.copySubtrees(ast, list);
						list.clear();
						list.addAll(copy);
					}
				}
			}
		};
		root.accept(v);
		System.out.println("Total visited node classes: " + nodeClasses.size());
		System.out.println("Total visited properties: " + visitedProperties.size());
		System.out.println("Total properties hit: "
				+ simpleProperties.size() + " simple, "
				+ childProperties.size() + " child, "
				+ childListProperties.size() + " child list");
		
		// visit should rebuild tree
		ASTNode newRoot = SampleASTs.oneOfEach(ast);
		assertTrue(root.subtreeMatch(new ASTMatcher(), newRoot));
	}
	
	public void testProtect() {
		final ASTNode root = SampleASTs.oneOfEach(ast);
		
		// check that all properties are again modifiable
		class Slammer extends ASTVisitor {
			boolean shouldBeProtected;
			Slammer(boolean shouldBeProtected){
				super(true); // visit doc
				this.shouldBeProtected = shouldBeProtected;
			}
			public void postVisit(ASTNode node) {
				try {
					node.setSourceRange(1, 1);
					assertTrue(!shouldBeProtected);
				} catch (RuntimeException e) {
					assertTrue(shouldBeProtected);
				}
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					Object o = node.getStructuralProperty(p);
					if (p.isSimpleProperty()) {
						// slam simple properties
						try {
							node.setStructuralProperty(p, o);
							assertTrue(!shouldBeProtected);
						} catch (RuntimeException e) {
							assertTrue(shouldBeProtected);
						}
					} else if (p.isChildProperty()) {
						// replace child with a copy
						ASTNode copy = ASTNode.copySubtree(ast, (ASTNode) o);
						try {
							node.setStructuralProperty(p, copy);
							assertTrue(!shouldBeProtected);
						} catch (RuntimeException e) {
							assertTrue(shouldBeProtected);
						}
					} else if (p.isChildListProperty()) {
						// replace child list with copies
						List list = (List) o;
						List copy = ASTNode.copySubtrees(ast, list);
						if (!list.isEmpty()) {
							try {
								list.clear();
								assertTrue(!shouldBeProtected);
							} catch (RuntimeException e) {
								assertTrue(shouldBeProtected);
							}
							try {
								list.addAll(copy);
								assertTrue(!shouldBeProtected);
							} catch (RuntimeException e) {
								assertTrue(shouldBeProtected);
							}
						}
					}
				}
			}
		}
		
		class Protector extends ASTVisitor {
			boolean shouldBeProtected;
			Protector(boolean shouldBeProtected){
				super(true); // visit doc
				this.shouldBeProtected = shouldBeProtected;
			}
			public void preVisit(ASTNode node) {
				int f = node.getFlags();
				if (shouldBeProtected) {
					f |= ASTNode.PROTECT;
				} else {
					f &= ~ASTNode.PROTECT;
				}
				node.setFlags(f);
			}
		}


		// mark all nodes as protected
		root.accept(new Protector(true));
		root.accept(new Slammer(true));
		
		// mark all nodes as unprotected
		root.accept(new Protector(false));
		root.accept(new Slammer(false));
	}
	
	public void testDelete() {
		final ASTNode root = SampleASTs.oneOfEach(ast);
		
		// check that nodes can be deleted unless mandatory
		root.accept(new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					if (p.isChildProperty()) {
						ChildPropertyDescriptor c = (ChildPropertyDescriptor) p;
						ASTNode child = (ASTNode) node.getStructuralProperty(c);
						if (!c.isMandatory() && child != null) {
							try {
								child.delete(); 
								assertTrue(node.getStructuralProperty(c) == null);
						    } catch (RuntimeException e) {
							    assertTrue(false);
						    }
						}
					} else if (p.isChildListProperty()) {
						// replace child list with copies
						List list = (List) node.getStructuralProperty(p);
						// iterate over a copy and try removing all members
						List copy = new ArrayList();
						copy.addAll(list);
						for (Iterator it2 = copy.iterator(); it2.hasNext(); ) {
							ASTNode n = (ASTNode) it2.next();
							try {
								n.delete();
								assertTrue(!list.contains(n));
						    } catch (RuntimeException e) {
							    assertTrue(false);
						    }
						}
					}
				}
			}
		});
	}
	
	public void testCreateInstance() {
		for (int nodeType = 0; nodeType < 100; nodeType++) {
			Class nodeClass = null;
			try {
				nodeClass = ASTNode.nodeClassForType(nodeType);
			} catch (RuntimeException e) {
				// oops - guess that's not valid
			}
			if (nodeClass != null) {
				try {
					ASTNode node = ast.createInstance(nodeClass);
					if (ast.apiLevel() == AST.LEVEL_2_0) {
						assertTrue((nodeType >= 1) && (nodeType <= 69));
					} else {
						assertTrue((nodeType >= 1) && (nodeType <= 83));
					}
					assertTrue(node.getNodeType() == nodeType);
					//ASTNode node2 = ast.createInstance(nodeType);
					//assertTrue(node2.getNodeType() == nodeType);
				} catch (RuntimeException e) {
					if (ast.apiLevel() == AST.LEVEL_2_0) {
						assertTrue((nodeType < 1) || (nodeType > 69));
					} else {
						assertTrue((nodeType < 1) || (nodeType > 83));
					}
				}
			}
		}
	}
	
	public void testNodeClassForType() {
		Set classes = new HashSet(100);
		// make sure node types are contiguous starting at 0
		int hi = 0;
		for (int nodeType = 1; nodeType < 100; nodeType++) {
			try {
				Class nodeClass = ASTNode.nodeClassForType(nodeType);
				assertTrue(ASTNode.class.isAssignableFrom(nodeClass));
				classes.add(nodeClass);
				if (nodeType > 1) {
					assertTrue(hi == nodeType - 1);
				}
				hi = nodeType;
			} catch (RuntimeException e) {
				// oops - guess that's not valid
			}
		}
		assertTrue(hi == 83); // last known one
		assertTrue(classes.size() == hi); // all classes are distinct
	}
}
