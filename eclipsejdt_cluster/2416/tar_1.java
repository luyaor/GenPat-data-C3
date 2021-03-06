/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;

public class ASTConverterJavadocTest extends ConverterTestSetup {

	// List of comments read from source of test
	List comments = new ArrayList();
	// List of tags contained in each comment read from test source.
	List allTags = new ArrayList();

	/**
	 * @param name
	 */
	public ASTConverterJavadocTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterJavadocTest.class.getName());		

		if (true) {
			Class c = ASTConverterJavadocTest.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterJavadocTest(methods[i].getName()));
				}
			}
			return suite;
		}
		suite.addTest(new ASTConverterJavadocTest("testJavadoc012"));			
		return suite;
	}

	/*
	 * Convert Javadoc source to match Javadoc.toString().
	 * Store converted comments and their corresponding tags respectively
	 * in this.comments and this.allTags fields
	 */
	void setSourceComment(char[] source) {
		this.comments = new ArrayList();
		this.allTags = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		int comment = 0;
		boolean end = false;
		String tag = null;
		List tags = new ArrayList();
		for (int i=0; i<source.length; i++) {
			if (comment == 0) {
				if (source[i] == '/') {
					switch (source[i+1]) {
						case '/':
							comment = 1; // line comment
							break;
						case '*':
							if (source[i+2] == '*') {
								comment = 3; // javadoc
							} else {
								comment = 2; // block comment
							}
							break;
					}
				}
			}
			switch (comment) {
				case 1: // line comment
					if (source[i] == '\r' || source[i] == '\n') {
						comment = 0;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
						buffer = new StringBuffer();
					} else {
						buffer.append(source[i]);
					}
					break;
				case 3: // javadoc comment
					if (tag != null) {
						if (source[i] >= 'a' && source[i] <= 'z') {
							tag += source[i];
						} else {
							tags.add(tag);
							tag = null;
						}
					}
					if (source[i] == '@') {
						tag = "";
					}
				case 2: // block comment
					if (comment == 2 || source[i] != '\r') {
						buffer.append(source[i]);
					}
					if (end && source[i] == '/') {
						comment = 0;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
						buffer = new StringBuffer();
						tags = new ArrayList();
					}
					end = source[i] == '*';
					break;
				default:
					// do nothing
					break;
			}
		}
	}
	
	/*
	 * Return all tags number for a given Javadoc
	 */
	int allTags(Javadoc docComment) {
		int all = 0;
		// Count main tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			TagElement tagElement = (TagElement) tags.next();
			if (tagElement.getTagName() != null) {
				all++;
			}
			Iterator fragments = tagElement.fragments().listIterator();
			while (fragments.hasNext()) {
				ASTNode node = (ASTNode) fragments.next();
				if (node.getNodeType() == ASTNode.TAG_ELEMENT) {
					all++;
				}
			}
		}
		return all;
	}

	/*
	 * Compare a string a subset of a char array.
	 */
	protected boolean compare(String str, char[] source, int start, int length) {
		if (str.length() != length) return false;
		for (int i=0; i<length; i++) {
			if (str.charAt(i) != source[start+i]) return false;
		}
		return true;
	}

	/*
	 * Verify that objects of Javadoc comment structure match their source positions
	 *
	private void verifyFragmentsPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		assertTrue("Misplaced javadoc start", source[start] == '/' && source[start+1] == '*' && source[start+2] == '*');
		int end = start+docComment.getLength()-1;
		assertTrue("Wrong javadoc length", end<source.length);
		assertTrue("Misplaced javadoc end", source[end-1] == '*' && source[end] == '/');
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyFragmentsPositions((TagElement) tags.next(), source);
		}
	}

	/*
	 * Verify that objects of Javadoc tag structure match their source positions
	 *
	private void verifyFragmentsPositions(TagElement tagElement, char[] source) {
		// Verify tag name
		String tagName = tagElement.getTagName();
		if (tagName != null) {
			assertEquals("Misplaced tag name at "+tagElement.getStartPosition(), tagName, new String(source, tagElement.getStartPosition(), tagName.length()));
		}
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			String text = null;
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				text = new String(source, fragment.getStartPosition(), fragment.getLength());
				assertEquals("Misplaced or wrong text element at "+fragment.getStartPosition(), text, ((TextElement) fragment).getText());
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				verifyFragmentsPositions((TagElement) fragment, source);
			} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
				MemberRef memberRef = (MemberRef) fragment;
				ASTNode node = memberRef.getQualifier();
				if (node != null) {
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong member ref container at "+node.getStartPosition(), text, node.toString());
				}
				if (memberRef.getName() != null) {
					node = memberRef.getName();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong member ref name at "+node.getStartPosition(), text, node.toString());
				}
			} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
				MethodRef methodRef = (MethodRef) fragment;
				ASTNode node = methodRef.getQualifier();
				if (node != null) {
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref container at "+node.getStartPosition(), text, node.toString());
				}
				node = methodRef.getName();
				text = new String(source, node.getStartPosition(), node.getLength());
				assertEquals("Misplaced or wrong method ref name at "+node.getStartPosition(), text, node.toString());
				Iterator parameters = methodRef.parameters().listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					node = param.getName();
					if (node != null) {
						text = new String(source, node.getStartPosition(), node.getLength());
						assertEquals("Misplaced or wrong method ref parameter name at "+node.getStartPosition(), text, node.toString());
					}
					node = param.getType();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref parameter type at "+node.getStartPosition(), text, node.toString());
				}
			}
		}
	}

	/*
	 * Verify positions of tags in source
	 */
	private void verifyPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		int end = start+docComment.getLength()-1;
		assertTrue("Misplaced javadoc start", source[start++] == '/' && source[start++] == '*' && source[start++] == '*');
		// Get first meaningful character
		int tagStart = start;
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
				tagStart++; // purge non-stored characters
			}
			TagElement tagElement = (TagElement) tags.next();
			assertEquals("Tag element has wrong start position", tagStart, tagElement.getStartPosition());
//			int tagEnd = tagStart+tagElement.getLength()-1;
			verifyPositions(tagElement , source);
			tagStart += tagElement.getLength();
		}
		while (Character.isWhitespace(source[tagStart])) {
			tagStart++; // purge non-stored characters
		}
		assertTrue("Misplaced javadoc end", source[tagStart++] == '*' && source[tagStart] == '/');
		assertEquals("Wrong javadoc length", tagStart, end);
	}

	/*
	 * Verify positions of fragments in source
	 */
	private void verifyPositions(TagElement tagElement, char[] source) {
		String text = null;
		// Verify tag name
		String tagName = tagElement.getTagName();
		int tagStart = tagElement.getStartPosition();
		if (tagElement.isNested()) {
			assertEquals("Wrong start position for "+tagElement, '{', source[tagStart++]);
		}
		if (tagName != null) {
			text= new String(source, tagStart, tagName.length());
			assertEquals("Misplaced tag name at "+tagStart, tagName, text);
			tagStart += tagName.length();
		}
		// Verify each fragment
		ASTNode previousFragment = null;
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				if (previousFragment != null && previousFragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
					assertTrue("Wrong length for text element "+previousFragment, source[tagStart] == '\r' || source[tagStart] == '\n');
					while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
						tagStart++; // purge non-stored characters
					}
				}
				text = new String(source, tagStart, fragment.getLength());
				assertEquals("Misplaced or wrong text element at "+tagStart, text, ((TextElement) fragment).getText());
			} else {
				while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
					tagStart++; // purge non-stored characters
				}
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME || fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					verifyNamePositions(tagStart, (Name) fragment, source);
				} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
					TagElement inlineTag = (TagElement) fragment;
					assertEquals("Tag element has wrong start position", tagStart, inlineTag.getStartPosition());
					verifyPositions(inlineTag, source);
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = memberRef.getQualifier();
					if (qualifier != null) {
						text = new String(source, start, qualifier.getLength());
						assertEquals("Misplaced or wrong member ref qualifier at "+start, text, qualifier.toString());
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assertEquals("Misplace # separator for member ref"+memberRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = memberRef.getName();
					text = new String(source, start, name.getLength());
					assertEquals("Misplaced or wrong member ref name at "+start, text, name.toString());
					verifyNamePositions(start, name, source);
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = methodRef.getQualifier();
					if (qualifier != null) {
						text = new String(source, start, qualifier.getLength());
						assertEquals("Misplaced or wrong member ref qualifier at "+start, text, qualifier.toString());
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assertEquals("Misplace # separator for member ref"+methodRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = methodRef.getName();
					text = new String(source, start, name.getLength());
					assertEquals("Misplaced or wrong member ref name at "+start, text, name.toString());
					verifyNamePositions(start, name, source);
					start += name.getLength();
					while (source[start] == '(' || source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify parameters
					Iterator parameters = methodRef.parameters().listIterator();
					while (parameters.hasNext()) {
						MethodRefParameter param = (MethodRefParameter) parameters.next();
						// Verify parameter type positions
						Type type = param.getType();
						text = new String(source, start, type.getLength());
						assertEquals("Misplaced or wrong method ref parameter type at "+start, text, type.toString());
						if (type.isSimpleType()) {
							verifyNamePositions(start, ((SimpleType)type).getName(), source);
						}
						start += type.getLength();
						while (Character.isWhitespace(source[start])) {
							 start++; // purge non-stored characters
						}
						// Verify parameter name positions
						name = param.getName();
						if (name != null) {
							text = new String(source, start, name.getLength());
							assertEquals("Misplaced or wrong method ref parameter name at "+start, text, name.toString());
							start += name.getLength();
						}
						while (source[start] == ',' || source[start] == ')' || source[start] == '*' || Character.isWhitespace(source[start])) {
							char ch = source[start++];
							 if (ch == ',' || ch == ')') {
							 	break;
							 }
						}
					}
				}
			}
			tagStart += fragment.getLength();
//			if (source[tagStart] == '\r' || source[tagStart] == '\n') {
//				while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
//					tagStart++; // purge non-stored characters
//				}
//			}
			previousFragment = fragment;
		}
		if (tagElement.isNested()) {
			assertEquals("Wrong end character for "+tagElement, '}', source[tagStart++]);
		}
	}

	/*
	 * Verify each name component positions.
	 */
	private void verifyNamePositions(int nameStart, Name name, char[] source) {
		if (name.isQualifiedName()) {
			QualifiedName qualified = (QualifiedName) name;
			String str = new String(source, qualified.getName().getStartPosition(), qualified.getName().getLength());
			assertEquals("Misplaced or wrong name component "+name, str, qualified.getName().toString());
			verifyNamePositions(nameStart, ((QualifiedName) name).getQualifier(), source);
		}
		String str = new String(source, nameStart, name.getLength());
		assertEquals("Misplaced or wrong name component at "+nameStart, str, name.toString());
	}

	/*
	 * Verify that bindings of Javadoc comment structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	private void verifyBindings(Javadoc docComment) {
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyBindings((TagElement) tags.next());
		}
	}

	/*
	 * Verify that bindings of Javadoc tag structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	private void verifyBindings(TagElement tagElement) {
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		IBinding previousBinding = null;
		boolean resolvedBinding = false;
		ASTNode previousFragment = null;
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				TextElement text = (TextElement) fragment;
				if (resolvedBinding) {
					if (previousBinding == null) {
						assertTrue("Reference in '"+previousFragment+"' should be bound!", text.getText().trim().startsWith("Unknown"));
					} else {
						assertFalse("Unknown reference in'"+previousFragment+"' should NOT be bound!", text.getText().trim().startsWith("Unknown"));
					}
				}
				previousBinding = null;
				resolvedBinding = false;
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				verifyBindings((TagElement) fragment);
				previousBinding = null;
				resolvedBinding = false;
			} else {
				resolvedBinding = true;
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME) {
					previousBinding = ((Name)fragment).resolveBinding();
				} else if (fragment.getNodeType() == ASTNode.SIMPLE_NAME || fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					QualifiedName name = (QualifiedName) fragment;
					previousBinding = name.resolveBinding();
					verifyNameBindings(name.getQualifier());
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					previousBinding = memberRef.resolveBinding();
					previousFragment = fragment;
					if (previousBinding != null) {
						assertNotNull(memberRef.getName()+" binding was not found!", memberRef.getName().resolveBinding());
						verifyNameBindings(memberRef.getQualifier());
					}
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					previousBinding = methodRef.resolveBinding();
					if (previousBinding != null) {
						assertNotNull(methodRef.getName()+" binding was not found!", methodRef.getName().resolveBinding());
						verifyNameBindings(methodRef.getQualifier());
						Iterator parameters = methodRef.parameters().listIterator();
						while (parameters.hasNext()) {
							MethodRefParameter param = (MethodRefParameter) parameters.next();
							assertNotNull(param.getType()+" binding was not found!", param.getType().resolveBinding());
							if (param.getType().isSimpleType()) {
								verifyNameBindings(((SimpleType)param.getType()).getName());
							}
							//	Do not verify parameter name as no binding is expected for them
						}
					}
				}
				previousFragment = fragment;
			}
		}
		assertTrue("Reference in '"+previousFragment+"' should be bound!", (!resolvedBinding || previousBinding != null));
	}

	/*
	 * Verify each name component binding.
	 */
	private void verifyNameBindings(Name name) {
		if (name != null) {
			assertNotNull(name+" binding was not found!", name.resolveBinding());
			SimpleName simpleName = null;
			while (name.isQualifiedName()) {
				simpleName = ((QualifiedName) name).getName();
				assertNotNull(simpleName+" binding was not found!", simpleName.resolveBinding());
				name = ((QualifiedName) name).getQualifier();
				assertNotNull(name+" binding was not found!", name.resolveBinding());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	private void verifyComments(String testNbre, boolean flattened) throws JavaModelException {
		// Get test file
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.test"+testNbre, "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull("Test file Converter/src/javadoc/test"+testNbre+"/Test.java was not found!", sourceUnit);

		// Create DOM AST nodes hierarchy
		String sourceStr = sourceUnit.getSource();
		CompilationUnit compilUnit = (CompilationUnit) runConversion(sourceUnit, true); // resolve bindings
		Comment[] unitComments = compilUnit.getCommentTable();

		// Get comments infos from test file
		char[] source = sourceStr.toCharArray();
		setSourceComment(source);
		
		// Basic comments verification
		assertEquals("Wrong number of comments", this.comments.size(), unitComments.length);
		
		// Verify comments positions and bindings
		for (int i=0; i<unitComments.length; i++) {
			String comment = (String) this.comments.get(i);
			List tags = (List) allTags.get(i);
			if (unitComments[i].isDocComment()) {
				Javadoc docComment = (Javadoc)unitComments[i];
				assertEquals("Invalid tags number! ", tags.size(), allTags(docComment));
				if (flattened) {
					assertEquals("Flattened javadoc does NOT match source!", comment, docComment.toString());
				}
				verifyPositions(docComment, source);
				verifyBindings(docComment);
			}
		}
		
		/* Verify each javadoc: not implemented yet
		Iterator types = compilUnit.types().listIterator();
		while (types.hasNext()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.next();
			verifyJavadoc(typeDeclaration.getJavadoc());
		}
		*/
	}

	/* 
	 * Verify each javadoc
	 * Not implented yet
	private void verifyJavadoc(Javadoc docComment) {
	}
	*/

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void testJavadoc000() throws JavaModelException {
		verifyComments("000", true);
	}

	/**
	 * Check javadoc for invalid syntax
	 */
	public void testJavadoc001() throws JavaModelException {
		verifyComments("001", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50781
	 */
	public void testJavadoc002() throws JavaModelException {
		verifyComments("002", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50784
	 */
	public void testJavadoc003() throws JavaModelException {
		verifyComments("003", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50785
	 */
	public void testJavadoc004() throws JavaModelException {
		verifyComments("004", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50838
	 */
	public void testJavadoc005() throws JavaModelException {
		verifyComments("005", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc006() throws JavaModelException {
		verifyComments("006", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc007() throws JavaModelException {
		verifyComments("007", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc008() throws JavaModelException {
		verifyComments("008", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc009() throws JavaModelException {
		verifyComments("009", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50880
	 */
	public void testJavadoc010() throws JavaModelException {
		verifyComments("010", true);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void testJavadoc011() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.test011", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("No compilation unit", result);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50938
	 */
	public void testJavadoc012() throws JavaModelException {
		verifyComments("012", false);
	}
}
