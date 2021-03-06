/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a method declaration.
 */
public class JavadocMethodCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocMethodCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_NUMBERS = new int[] { 49 };
//	TESTS_RANGE = new int[] { 90, -1 };
}
public static Test suite() {
	return buildTestSuite(JavadocMethodCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * @category Tests for tag names completion
 */
public void test001() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on impossible tag name:\n" +
		"	 * 	@aut\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@aut");
	assertResults("");
}

public void test003() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on one letter:\n" +
		"	 * 	@r\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@r");
	assertResults(
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 * 	@ser\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@ser");
	assertResults(
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on full tag name:\n" +
		"	 * 	@inheritDoc\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@inheritDoc");
	assertResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_3);
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test007() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods;\n" + 
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"link[JAVADOC_INLINE_TAG]{{@link }, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot }, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc }, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain }, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"code[JAVADOC_INLINE_TAG]{{@code }, null, null, code, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"literal[JAVADOC_INLINE_TAG]{{@literal }, null, null, literal, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for types completion
 */
public void test010() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethodsE\n" + 
		"	 */\n" + 
		"	public void foo() {}\n" + 
		"}\n" + 
		"class BasicTestMethodsException1 extends Exception{}\n" + 
		"class BasicTestMethodsException2 extends Exception{}\n" + 
		"class BasicTestMethodsExample {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+"21}\n" + 
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+"21}\n" + 
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+"21}"
	);
}

public void test011() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see \n" + 
		"	 */\n" + 
		"	public void foo() {}\n" + 
		"}\n" + 
		"class BasicTestMethodsException1 extends Exception{}\n" + 
		"class BasicTestMethodsException2 extends Exception{}\n" + 
		"class BasicTestMethodsExample {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@see ", 0); // completion on empty token
	assertResults("");
}

public void test012() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@throws BasicTestMethodsE\n" + 
		"	 */\n" + 
		"	public void foo() {}\n" + 
		"}\n" + 
		"class BasicTestMethodsException1 extends Exception{}\n" + 
		"class BasicTestMethodsException2 extends Exception{}\n" + 
		"class BasicTestMethodsExample {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+"41}\n" + 
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+"41}\n" + 
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+"21}"
	);
}

public void test013() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@throws BasicTestMethodsE\n" + 
		"	 */\n" + 
		"	public void foo() throws BasicTestMethodsException2 {}\n" + 
		"}\n" + 
		"class BasicTestMethodsException1 extends Exception{}\n" + 
		"class BasicTestMethodsException2 extends Exception{}\n" + 
		"class BasicTestMethodsExample {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+"71}\n" + 
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+"41}\n" + 
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+"21}"
	);
}

public void test014() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@throws \n" + 
		"	 */\n" + 
		"	public void foo() throws BasicTestMethodsException {}\n" + 
		"}\n" + 
		"class BasicTestMethodsException extends Exception{}\n" + 
		"class BasicTestMethodsExample {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@throws ", 0); // completion on empty token
	assertResults(
		"BasicTestMethodsException[TYPE_REF]{BasicTestMethodsException, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException;, null, null, "+this.positions+"51}"
	);
}

public void test015() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@throws I\n" + 
		"	 * 		Note: there should be NO base types in proposals." + 
		"	 */\n" + 
		"	public void foo() {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "I");
	assertResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"41}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"41}"
	);
}

public void test016() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@throws java.lang.I\n" + 
		"	 */\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.I");
	assertResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"38}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"68}"
	);
}

/**
 * @category Tests for fields completion
 */
public void test020() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"	void foo() {}\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}"
	);
}

public void test021() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethods#fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"	void foo() {}\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}"
	);
}

public void test022() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"	void foo() {}\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}"
	);
}

public void test023() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see OtherFields#fo\n" + 
			"	 */\n" + 
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherFields.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherFields {\n" + 
			"	int foo;\n" + 
			"	void foo() {}\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.OtherFields;, I, foo, null, "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.OtherFields;, ()V, foo, null, "+this.positions+"29}"
	);
}

/**
 * @category Tests for methods completion
 */
public void test030() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see fo\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults("");
}

public void test031() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #fo\n" + 
		"	 */\n" + 
		"	<T> void foo() {}\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, foo, null, "+this.positions+"29}"
	);
}

public void test032() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #ba\n" + 
		"	 * \n" + 
		"	 * Note that argument names are put in proposals although there are not while completing\n" + 
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" + 
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}"
	);
}

public void test033() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #ba\n" + 
		"	 * \n" + 
		"	 * Note that argument names are put in proposals although there are not while completing\n" + 
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" + 
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" + 
		"	 */\n" + 
		"	<T, U> void bar(String str, Class<T> clt, Class<U> clu) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, Class, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;U:Ljava.lang.Object;>(Ljava.lang.String;Ljava.lang.Class<TT;>;Ljava.lang.Class<TU;>;)V, bar, (str, clt, clu), "+this.positions+"29}"
	);
}

public void test034() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethods#ba\n" + 
		"	 * \n" + 
		"	 * Note that argument names are put in proposals although there are not while completing\n" + 
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" + 
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}"
	);
}

public void test035() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#ba\n" + 
		"	 * \n" + 
		"	 * Note that argument names are put in proposals although there are not while completing\n" + 
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" + 
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}"
	);
}

public void test036() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see OtherTypes#fo\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void foo() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.OtherTypes;, ()V, foo, null, "+this.positions+"29}"
	);
}

public void test037() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test038() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #\n" + 
		"	 */\n" + 
		"	<T> void foo() {}\n" + 
		"	<TParam1, TParam2> void bar(TParam1 tp1, TParam2 tp2) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"bar[METHOD_REF]{bar(Object, Object), Ljavadoc.methods.tags.BasicTestMethods;, <TParam1:Ljava.lang.Object;TParam2:Ljava.lang.Object;>(TTParam1;TTParam2;)V, bar, (tp1, tp2), "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, foo, null, "+this.positions+"29}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test039() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethods#\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test040() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"29}\n" + 
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+"29}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test041() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test042() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(Str\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Str");
	assertResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"21}"
	);
}

public void test043() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(java.lang.\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.");
	assertSortedResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"18}\n" + 
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+"18}\n" + 
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+"18}\n" + 
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+"18}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"18}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"18}\n" + 
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+"18}\n" + 
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+"18}\n" + 
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"18}\n" + 
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+"18}"
	);
}

public void test044() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(java.lang.St\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.St");
	assertResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"18}"
	);
}

public void test045() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String s\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String s");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test046() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String str, \n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test047() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String,\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test048() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String str, bool\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bool");
	assertResults(
		"boolean[KEYWORD]{boolean, null, null, boolean, null, "+this.positions+"18}"
	);
}

/*
 * Specific case where we can complete but we don't want to as the prefix is not syntaxically correct
 */
public void test049() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String str, boolean,\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean,");
	assertResults("");
}

public void test050() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String str, boolean flag,\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean flag,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test051() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String,boolean,\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String,boolean,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test052() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String,boolean,Object\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Object");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+"25}"
	);
}

/*
 * Specific case where we can complete but we don't want to as the prefix is not syntaxically correct
 */
public void test053() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String, boolean, Object o\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String, boolean, Object o");
	assertResults("");
}

public void test054() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #bar(String str, boolean flag, Object o\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	void bar(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean flag, Object o");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+"25}"
	);
}

public void test055() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see OtherTypes#foo(\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void foo(String str) {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "foo(");
	assertResults(
		"foo[METHOD_REF]{foo(String), Ljavadoc.methods.tags.OtherTypes;, (Ljava.lang.String;)V, foo, (str), "+this.positions+"25}"
	);
}

public void test056() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see javadoc.methods.tags.OtherTypes#foo(\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void foo(String str) {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "foo(");
	assertResults(
		"foo[METHOD_REF]{foo(String), Ljavadoc.methods.tags.OtherTypes;, (Ljava.lang.String;)V, foo, (str), "+this.positions+"25}"
	);
}

/**
 * @category Tests for method parameters completion
 */
public void test060() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param \n" + 
		"	 */\n" + 
		"	public String foo(String str) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"str[JAVADOC_PARAM_REF]{str, null, null, str, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test061() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param x\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "x");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test062() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test063() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "x");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test064() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xx\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xx");
	assertResults("");
}

public void test065() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(	"");
}

public void test066() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults("");
}

public void test067() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", -1); // last position
	assertResults("");
}

public void test068() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test069() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test070() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr2\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test071() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr2\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test072() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr2\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults("");
}

public void test073() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr2\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults("");
}

public void test074() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param xstr2\n" + 
		"	 */\n" + 
		"	public String foo(String xstr, String xstr2) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 3); // 3rd position
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test075() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param \n" + 
		"	 */\n" + 
		"	public String foo(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+2)+"}\n" + 
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" + 
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test076() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xstr\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	public String methodMultipleParam2(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" + 
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test077() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param \n" + 
		"	 * 	@param flag\n" + 
		"	 */\n" + 
		"	public String methodMultipleParam3(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" + 
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test078() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param obj\n" + 
		"	 * 	@param xstr\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	public String methodMultipleParam4(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test079() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param \n" + 
		"	 * 	@param obj\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param flag\n" + 
		"	 */\n" + 
		"	public String methodMultipleParam5(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults("");
}

public void test080() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param obj\n" + 
		"	 * 	@param xstr\n" + 
		"	 * 	@param flag\n" + 
		"	 */\n" + 
		"	public String methodMultipleParam5(String xstr, boolean flag, Object obj) {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ob");
	assertResults(
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

/**
 * @category Tests for type parameters completion
 */
public void test090() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param \n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test091() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TM>\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test092() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xtc\n" + 
		"	 * 	@param <TM>\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test093() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xtc\n" + 
		"	 ** 	@param \n" + 
		"	 * 	@param xtc\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test094() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 ** 	@param \n" + 
		"	 * 	@param xtc\n" + 
		"	 * 	@param xtm\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test095() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param xtc\n" + 
		"	 * 	@param xtm\n" + 
		"	 * 	@param <TM>\n" + 
		"	 ** 	@param \n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults("");
}

public void test096() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<", 2); // 2nd occurence
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test097() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <T\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<T", 2); // 2nd occurence
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test098() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TC\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TC", 2); // 2nd occurence
	assertSortedResults("");
}

public void test099() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TM>\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM");
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test100() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TM>\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM>");
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test101() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TM>\n" + 
		"	 * 	@param <TM>\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM");
	assertSortedResults("");
}

public void test102() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param <TM>\n" + 
		"	 * 	@param <TM>\n" + 
		"	 */\n" + 
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM>", 2); // 2nd occurence
	assertSortedResults("");
}

public void test103() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" +
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@param ab\n" + 
		"	 */\n" + 
		"	void foo(Object ab1, Object ab2) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0);
	assertSortedResults(
		"ab1[JAVADOC_PARAM_REF]{ab1, null, null, ab1, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" + 
		"ab2[JAVADOC_PARAM_REF]{ab2, null, null, ab2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

/**
 * @category Tests for constructors completion
 */
public void test110() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 2); // 2nd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test111() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	<T> BasicTestMethods(int xxx, float real, Class<T> clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 2); // 2nd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>(IFLjava.lang.Class<TT;>;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test112() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethods#BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 3); // 3rd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test113() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 3); // 3rd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test114() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see OtherTypes#O\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	OtherTypes() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[METHOD_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.methods.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test115() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" + 
			"public class BasicTestMethods {\n" + 
			"	/**\n" + 
			"	 * Completion after:\n" + 
			"	 * 	@see OtherTypes#O implicit default constructor\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" + 
			"public class OtherTypes {\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[METHOD_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.methods.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test116() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test117() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #\n" + 
		"	 */\n" + 
		"	<T> BasicTestMethods() {}\n" + 
		"	<T, U> BasicTestMethods(int xxx, Class<T> cl1, Class<U> cl2) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, Class, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;U:Ljava.lang.Object;>(ILjava.lang.Class<TT;>;Ljava.lang.Class<TU;>;)V, BasicTestMethods, (xxx, cl1, cl2), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test118() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see #\n" + 
		" */\n" + 
		"public class BasicTestMethods {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test119() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test120() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see BasicTestMethods#BasicTestMethods(\n" + 
		"	 */\n" + 
		"	BasicTestMethods() {}\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test121() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#BasicTestMethods(\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: int
public void test122() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(in\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "in");
	assertResults(
		"int[KEYWORD]{int, null, null, int, null, "+this.positions+"18}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"11}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: int
public void test123() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "int");
	assertResults(
		"int[KEYWORD]{int, null, null, int, null, "+this.positions+"22}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"11}"
	);
}

public void test124() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int aaa, fl\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fl");
	assertResults(
		"float[KEYWORD]{float, null, null, float, null, "+this.positions+"18}"
	);
}

public void test125() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int aaa, float\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "float");
	assertResults(
		"float[KEYWORD]{float, null, null, float, null, "+this.positions+"22}"
	);
}

public void test126() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int, float, Cla\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Cla");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"21}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: Class
public void test127() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int, float, java.lang.\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.");
	assertSortedResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"18}\n" + 
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+"18}\n" + 
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+"18}\n" + 
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+"18}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"18}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"18}\n" + 
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+"18}\n" + 
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+"18}\n" + 
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"18}\n" + 
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+"18}"
	);
}

public void test128() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int, float, java.lang.Cla\n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.Cla");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"18}"
	);
}

public void test129() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" + 
		"public class BasicTestMethods {\n" + 
		"	void foo() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	@see #BasicTestMethods(int, float, Class\n" + 
		"	 * \n" + 
		"	 */\n" + 
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Class");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"25}"
	);
}
}
