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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.*;

import junit.framework.Test;

public class SignatureTests extends AbstractJavaModelTests {
public SignatureTests(String name) {
	super(name);
}
/**
 * Ensures that creating an invalid type signature throws an IllegalArgumentException or return the expected signature.
 */
protected void assertInvalidTypeSignature(String typeName, boolean isResolved, String expected) {
	String actual;
	try {
		actual = Signature.createTypeSignature(typeName, isResolved);
	} catch (IllegalArgumentException e) {
		return;
	}
	assertEquals(expected, actual);
}
public static Test suite() {
	return buildTestSuite(SignatureTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Prefix for tests names to run
//	TESTS_PREFIX =  "testGetTypeErasure";
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//	TESTS_NAMES = new String[] { "testGetTypeErasure5", "testGetTypeErasure9", "testGetTypeErasure10" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//	TESTS_NUMBERS = new int[] { 8 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//	testsRange = new int[] { -1, -1 };
}
/**
 * @see Signature
 */
public void testCreateArraySignature() {
	assertEquals(
		"Signature#createArraySignature not correct", 
		"[[[[QString",
		Signature.createArraySignature("QString", 4));
		
	// tests with 1.5-specific elements
	assertEquals(
		"Signature#createArraySignature not correct", 
		"[[[[Qlist<Qstring;>;",
		Signature.createArraySignature("Qlist<Qstring;>;", 4));
		
}
/**
 * @see Signature
 */
public void testCreateMethodSignature() {
	assertEquals(
		"Signature#createMethodSignature is not correct 1", 
		"(QString;QObject;I)I", 
		Signature.createMethodSignature(new String[] {"QString;", "QObject;", "I"}, "I"));
	assertEquals(
		"Signature#createMethodSignature is not correct 2", 
		"()Ljava.lang.String;", 
		Signature.createMethodSignature(new String[] {}, "Ljava.lang.String;"));
}
/**
 * @see Signature
 */
public void testCreateTypeSignature() {
	assertEquals("Signature#createTypeSignature is not correct1", "I",
			Signature.createTypeSignature("int".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct2", "Ljava.lang.String;",
			Signature.createTypeSignature("java.lang.String".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct3", "QString;", 
			Signature.createTypeSignature("String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct4", "Qjava.lang.String;", 
			Signature.createTypeSignature("java.lang.String".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct5", "[I",
			Signature.createTypeSignature("int []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct6", "[QString;",
			Signature.createTypeSignature("String []".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct7", "[Ljava.util.Vector;",
			Signature.createTypeSignature("java.util.Vector []".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct8", "[[Ljava.util.Vector;",
			Signature.createTypeSignature("java .\n util  .  Vector[  ][]".toCharArray(), true));
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=41019
	assertEquals("Signature#createTypeSignature is not correct9", "Linteration.test.MyData;",
			Signature.createTypeSignature("interation.test.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct10", "Llongtest.MyData;",
			Signature.createTypeSignature("longtest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct11", "Lbooleantest.MyData;",
			Signature.createTypeSignature("booleantest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct12", "Lbytetest.MyData;",
			Signature.createTypeSignature("bytetest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct13", "Lchartest.MyData;",
			Signature.createTypeSignature("chartest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct14", "Lshorttest.MyData;",
			Signature.createTypeSignature("shorttest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct15", "Ldoubletest.MyData;",
			Signature.createTypeSignature("doubletest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct16", "Lfloattest.MyData;",
			Signature.createTypeSignature("floattest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct17", "Lvoidtest.MyData;",
			Signature.createTypeSignature("voidtest.MyData".toCharArray(), true));
	assertEquals("Signature#createTypeSignature is not correct18", "QList<QList<QString;>;>;",
			Signature.createTypeSignature("List<List<String>>".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct19", "QList<QList<I>;>;",
			Signature.createTypeSignature("List<List<int>>".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct20", "[QList<QList<[I>;>;",
			Signature.createTypeSignature("List<List<int[]>>[]".toCharArray(), false));
	assertEquals("Signature#createTypeSignature is not correct21", "Qjava.y.Map<[QObject;QString;>.MapEntry<[Qp.K<QT;>;[Qq.r.V2;>;",
			Signature.createTypeSignature("java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>".toCharArray(), false));	
}
/**
 * Ensures that creating an invalid type signature throws an IllegalArgumentException.
 */
public void testCreateInvalidTypeSignature() {
	assertInvalidTypeSignature(null, false, null);
	assertInvalidTypeSignature("", false, "");
	assertInvalidTypeSignature("int.Y", false, "I");
	assertInvalidTypeSignature("Y [].X", false, "[QY;");
	assertInvalidTypeSignature("X[[]", true, "[[LX;");
}
/**
 * @see Signature
 */
public void testGetArrayCount() {
	assertEquals("Signature#getArrayCount is not correct", 4,
			Signature.getArrayCount("[[[[QString;"));
	try {
		Signature.getArrayCount("");
		assertTrue("Signature#getArrayCount is not correct, exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// tests with 1.5-specific elements
	assertEquals(
		"Signature#getArrayCount not correct", 4,
		Signature.getArrayCount("[[[[Qlist<Qstring;>;"));
}

/**
 * @see Signature
 */
public void testGetElementType() {
	assertEquals("Signature#getElementType is not correct1", "QString;",
			Signature.getElementType("[[[[QString;"));
	assertEquals("Signature#getElementType is not correct2", "QString;",
			Signature.getElementType("QString;"));
	assertEquals("Signature#getElementType is not correct2", "I",
			Signature.getElementType("[[I"));
	try {
		Signature.getElementType("");
		assertTrue("Signature#getArrayCount is not correct, exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
	
	// tests with 1.5-specific elements
	assertEquals(
		"Signature#getElementType not correct", "Qlist<Qstring;>;",
		Signature.getElementType("[[[[Qlist<Qstring;>;"));
}
/**
 * @see Signature
 */
public void testGetParameterCount() {
	String methodSig = "(QString;QObject;I)I";
	assertEquals("Signature#getParameterCount is not correct1", 3,
			Signature.getParameterCount(methodSig));
	try {
		Signature.getParameterCount("");
		assertTrue("Signature#getParameterCount is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// tests with 1.5-specific elements
	methodSig = "<X:Qlist<Qstring;>;>(IQlist;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
	assertEquals("Signature#getParameterCount is not correct3", 3,
			Signature.getParameterCount(methodSig));
	methodSig = "<X:Qlist<Qstring;>;>(IQlist<Qstring;>;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
	assertEquals("Signature#getParameterCount is not correct4", 3,
			Signature.getParameterCount(methodSig));
}
/**
 * @see Signature
 */
public void testGetParameterTypes() {
	String methodSig = "(QString;QObject;I)I";
	String[] types= Signature.getParameterTypes(methodSig);
	assertEquals("Signature#getParameterTypes is not correct1", 3, types.length);
	assertEquals("Signature#getParameterTypes is not correct2", "QObject;", types[1]);
	try {
		Signature.getParameterTypes("");
		assertTrue("Signature#getParameterTypes is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}

	// primitive types
	methodSig = "(BCDFIJSVZ)V";
	assertEquals("Signature#getParameterTypes 4", 9,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 4", "B",
			Signature.getParameterTypes(methodSig)[0]);
	assertEquals("Signature#getParameterTypes 4", "C",
			Signature.getParameterTypes(methodSig)[1]);
	assertEquals("Signature#getParameterTypes 4", "D",
			Signature.getParameterTypes(methodSig)[2]);
	assertEquals("Signature#getParameterTypes 4", "F",
			Signature.getParameterTypes(methodSig)[3]);
	assertEquals("Signature#getParameterTypes 4", "I",
			Signature.getParameterTypes(methodSig)[4]);
	assertEquals("Signature#getParameterTypes 4", "J",
			Signature.getParameterTypes(methodSig)[5]);
	assertEquals("Signature#getParameterTypes 4", "S",
			Signature.getParameterTypes(methodSig)[6]);
	assertEquals("Signature#getParameterTypes 4", "V",
			Signature.getParameterTypes(methodSig)[7]);
	assertEquals("Signature#getParameterTypes 4", "Z",
			Signature.getParameterTypes(methodSig)[8]);

	// array types
	methodSig = "([I[[J[[[B[Qstring;[Tv;[Lstring;)V";
	assertEquals("Signature#getParameterTypes 5", 6,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 5", "[I",
			Signature.getParameterTypes(methodSig)[0]);
	assertEquals("Signature#getParameterTypes 5", "[[J",
			Signature.getParameterTypes(methodSig)[1]);
	assertEquals("Signature#getParameterTypes 5", "[[[B",
			Signature.getParameterTypes(methodSig)[2]);
	assertEquals("Signature#getParameterTypes 5", "[Qstring;",
			Signature.getParameterTypes(methodSig)[3]);
	assertEquals("Signature#getParameterTypes 5", "[Tv;",
			Signature.getParameterTypes(methodSig)[4]);
	assertEquals("Signature#getParameterTypes 5", "[Lstring;",
			Signature.getParameterTypes(methodSig)[5]);
	
	// resolved types
	methodSig = "(La;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La;",
			Signature.getParameterTypes(methodSig)[0]);
	methodSig = "(La<TE;>;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La<TE;>;",
			Signature.getParameterTypes(methodSig)[0]);
	methodSig = "(La/b/c<TE;>.d<TF;>;)V";
	assertEquals("Signature#getParameterTypes 6", 1,
			Signature.getParameterTypes(methodSig).length);
	assertEquals("Signature#getParameterTypes 6", "La/b/c<TE;>.d<TF;>;",
			Signature.getParameterTypes(methodSig)[0]);
}
/**
 * @see Signature
 */
public void testGetTypeParameters1() {
	String sig = "<X:TF;Y::Ljava.lang.Cloneable;>";
	assertStringsEqual(
			"Unexpected type parameters", 
			"X:TF;\n" + 
			"Y::Ljava.lang.Cloneable;\n",
			Signature.getTypeParameters(sig));
}
/**
 * @see Signature
 */
public void testGetTypeParameters2() {
	String sig = "<X:TF;Y::Ljava.lang.Cloneable;>()V";
	assertStringsEqual(
			"Unexpected type parameters", 
			"X:TF;\n" + 
			"Y::Ljava.lang.Cloneable;\n",
			Signature.getTypeParameters(sig));
}

/**
 * @see Signature
 */
public void testGetQualifier1() {
	assertEquals(
		"java.lang",
		Signature.getQualifier("java.lang.Object"));
}
public void testGetQualifier2() {
	assertEquals(
		"",
		Signature.getQualifier(""));
}
public void testGetQualifier3() {
	assertEquals(
		"java.util",
		Signature.getQualifier("java.util.List<java.lang.Object>"));
}
/**
 * @see Signature
 */
public void testGetReturnType() {
	String methodSig = "(QString;QObject;I)I";
	assertEquals("Signature#getReturnType is not correct1", "I",
			Signature.getReturnType(methodSig));
	try {
		Signature.getReturnType("");
		assertTrue("Signature#getReturnType is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
	
	// tests with 1.5-specific elements
	methodSig = "<X:Qlist<Qstring;>;>(Qstring;Qobject;I)IQexception;Qerror;";
	assertEquals("Signature#getReturnType is not correct2", "I",
			Signature.getReturnType(methodSig));
	methodSig = "<X:Qlist<Qstring;>;>(Qlist<Qstring;>;)Qlist<Qxxx;>;Qexception;Qerror;";
	assertEquals("Signature#getReturnType is not correct3", "Qlist<Qxxx;>;",
			Signature.getReturnType(methodSig));
}

/**
 * @see Signature
 */
public void testGetThrownExceptionTypes() {
	String methodSig = "(QString;QObject;I)I";
	assertStringsEqual("Signature#getThrownExceptionTypes is not correct1", "",
			Signature.getThrownExceptionTypes(methodSig));
	try {
		Signature.getThrownExceptionTypes("");
		assertTrue("Signature#getThrownExceptionTypes is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
	
	// tests with 1.5-specific elements
	methodSig = "<X:Qlist<Qstring;>;>(Qstring;Qobject;I)IQexception;Qerror;";
	assertStringsEqual("Signature#getThrownExceptionTypes is not correct2", "Qexception;\nQerror;\n",
			Signature.getThrownExceptionTypes(methodSig));
	methodSig = "<X:Qlist<Qstring;>;>(Qlist<Qstring;>;)Qlist<Qxxx;>;Qexception<TT;>;Qerror;";
	assertStringsEqual("Signature#getThrownExceptionTypes is not correct3", "Qexception<TT;>;\nQerror;\n",
			Signature.getThrownExceptionTypes(methodSig));
}
/**
 * @see Signature
 * @since 3.0
 */
public void testGetTypeVariable() {
	// tests with 1.5-specific elements
	String formalTypeParameterSignature = "Hello:";
	assertEquals("Signature#getTypeVariable is not correct1", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
	assertEquals("Signature#getTypeVariable is not correct2", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
	assertEquals("Signature#getTypeVariable is not correct3", "Hello",
			Signature.getTypeVariable(formalTypeParameterSignature));
	try {
		Signature.getTypeVariable("");
		assertTrue("Signature#getTypeVariable is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
}

/**
 * @see Signature
 * @since 3.0
 */
public void testGetTypeParameterBounds() {
	// tests with 1.5-specific elements
	String formalTypeParameterSignature = "Hello:";
	assertEquals("Signature#getTypeParameterBounds is not correct1", 0,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
	assertEquals("Signature#getTypeParameterBounds is not correct2", 2,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct2a", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	assertEquals("Signature#getTypeParameterBounds is not correct2b", "Qi2;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
	formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
	assertEquals("Signature#getTypeParameterBounds is not correct3", 3,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct3a", "Qlist<Qstring;>;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	assertEquals("Signature#getTypeParameterBounds is not correct3b", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
	assertEquals("Signature#getTypeParameterBounds is not correct3c", "Qi2;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[2]);
	formalTypeParameterSignature = "Hello:Qi1;";
	assertEquals("Signature#getTypeParameterBounds is not correct4", 1,
			Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
	assertEquals("Signature#getTypeParameterBounds is not correct4a", "Qi1;",
			Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
	try {
		Signature.getTypeParameterBounds("");
		assertTrue("Signature#getTypeParameterBounds is not correct: exception", false);
	} catch (IllegalArgumentException iae) {
		// do nothing
	}
}

/**
 * @see Signature
 */
public void testGetTypeArguments1() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QT;\n",
		Signature.getTypeArguments("QList<QT;>;")
	);
}

public void testGetTypeArguments2() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QT;\n" +
		"QU;\n",
		Signature.getTypeArguments("QX<QT;QU;>;")
	);
}

public void testGetTypeArguments3() {
	assertStringsEqual(
		"Unexpected type arguments",
		"*\n",
		Signature.getTypeArguments("QX<*>;")
	);
}

public void testGetTypeArguments4() {
	assertStringsEqual(
		"Unexpected type arguments",
		"+QE;\n" +
		"-QS;\n",
		Signature.getTypeArguments("QX<+QE;-QS;>;")
	);
}

public void testGetTypeArguments5() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QList<QT;>;\n" +
		"QMap<QU;QABC<QT;>;>;\n",
		Signature.getTypeArguments("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;")
	);
}

/*
 * getTypeArguments() on a raw type
 * (regression test for bug 73671 [1.5] Signature.getTypeArguments should also tolerate normal types)
 */
public void testGetTypeArguments6() {
	assertStringsEqual(
		"Unexpected type arguments",
		"",
		Signature.getTypeArguments("QList;")
	);
}

public void testGetTypeArguments7() {
	assertStringsEqual(
		"Unexpected type arguments",
		"",
		Signature.getTypeArguments("QX<QObject;>.Member;")
	);
}

public void testGetTypeArguments8() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QObject;\n",
		Signature.getTypeArguments("QX<QObject;>.Member<QObject;>;")
	);
}

public void testGetTypeArguments9() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QObject;\n",
		Signature.getTypeArguments("QX.Member<QObject;>;")
	);
}

public void testGetTypeArguments10() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QList<QT;>;\n" +
		"QMap<QU;QABC<QT;>;>;\n",
		Signature.getTypeArguments("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;")
	);
}

public void testGetTypeArguments11() {
	assertStringsEqual(
		"Unexpected type arguments",
		"QObject;\n",
		Signature.getTypeArguments("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;")
	);
}

/**
 * @see Signature
 */
public void testGetTypeErasure1() {
	assertEquals(
		"QList;",
		Signature.getTypeErasure("QList<QT;>;")
	);
}

public void testGetTypeErasure2() {
	assertEquals(
		"QList;",
		Signature.getTypeErasure("QList;")
	);
}

public void testGetTypeErasure3() {
	assertEquals(
		"QX;",
		Signature.getTypeErasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;")
	);
}

public void testGetTypeErasure4() {
	assertEquals(
		"QX.Member;",
		Signature.getTypeErasure("QX<QObject;>.Member;")
	);
}

public void testGetTypeErasure5() {
	assertEquals(
		"QX.Member;",
		Signature.getTypeErasure("QX<QObject;>.Member<QObject;>;")
	);
}

public void testGetTypeErasure6() {
	assertEquals(
		"QX.Member;",
		Signature.getTypeErasure("QX.Member<QObject;>;")
	);
}

public void testGetTypeErasure7() {
	assertEquals(
		"QX.Member;",
		Signature.getTypeErasure("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;")
	);
}

public void testGetTypeErasure8() {
	assertEquals(
		"QX.Member;",
		Signature.getTypeErasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;")
	);
}

/**
 * @see Signature
 */
public void testGetSimpleName() {
	assertEquals("Signature#getSimpleName is not correct 1", "Object",
			Signature.getSimpleName("java.lang.Object"));
	assertEquals("Signature#getSimpleName is not correct 2", "",
			Signature.getSimpleName(""));
	assertEquals("Signature#getSimpleName is not correct 3", 
			"MapEntry<K<T>[],V2[]>",
			Signature.getSimpleName("java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>"));
	assertEquals("Signature#getSimpleName is not correct 4", 
			"MapEntry<K<T>[],? extends V2>",
			Signature.getSimpleName("java.y.Map<Object[],String>.MapEntry<p.K<T>[],? extends q.r.V2>"));	
}
/**
 * @see Signature
 */
public void testGetSimpleNames1() {
	assertStringsEqual(
		"Unexpected simple names",
		"java\n" + 
		"lang\n" + 
		"Object\n",
		Signature.getSimpleNames("java.lang.Object"));
}
public void testGetSimpleNames2() {
	assertStringsEqual(
		"Unexpected simple names",
		"",
		Signature.getSimpleNames(""));
}
public void testGetSimpleNames3() {
	assertStringsEqual(
		"Unexpected simple names",
		"Object\n",
		Signature.getSimpleNames("Object"));
}
public void testGetSimpleNames4() {
	assertStringsEqual(
		"Unexpected simple names",
		"java\n" + 
		"util\n" + 
		"List<java.lang.String>\n",
		Signature.getSimpleNames("java.util.List<java.lang.String>"));
}
/**
 * @see Signature
 */
public void testToQualifiedName() {
	assertEquals("Signature#toQualifiedName is not correct1", "java.lang.Object",
			Signature.toQualifiedName(new String[] {"java", "lang", "Object"}));
	assertEquals("Signature#toQualifiedName is not correct2", "Object",
			Signature.toQualifiedName(new String[] {"Object"}));
	assertEquals("Signature#toQualifiedName is not correct3", "",
			Signature.toQualifiedName(new String[0]));
}
/**
 * @see Signature.toString(String)
 */
public void testToStringType01() {
	assertEquals(
		"java/lang/String",
		Signature.toString("Ljava/lang/String;"));
}
public void testToStringType02() {
	assertEquals(
		"java.lang.String",
		Signature.toString("Ljava.lang.String;"));
}
public void testToStringType03() {
	assertEquals(
		"java.lang.String[]",
		Signature.toString("[Ljava.lang.String;"));
}
public void testToStringType04() {
	assertEquals(
		"String",
		Signature.toString("QString;"));
}
public void testToStringType05() {
	assertEquals(
		"String[][]",
		Signature.toString("[[QString;"));
}
public void testToStringType06() {
	assertEquals(
		"boolean",
		Signature.toString("Z"));
}
public void testToStringType07() {
	assertEquals(
		"byte",
		Signature.toString("B"));
}
public void testToStringType08() {
	assertEquals(
		"char",
		Signature.toString("C"));
}
public void testToStringType09() {
	assertEquals(
		"double",
		Signature.toString("D"));
}
public void testToStringType10() {
	assertEquals(
		"float",
		Signature.toString("F"));
}
public void testToStringType11() {
	assertEquals(
		"int",
		Signature.toString("I"));
}
public void testToStringType12() {
	assertEquals(
		"long",
		Signature.toString("J"));
}
public void testToStringType13() {
	assertEquals(
		"short",
		Signature.toString("S"));
}
public void testToStringType14() {
	assertEquals(
		"void",
		Signature.toString("V"));
}
public void testToStringType15() {
	assertEquals(
		"int[][][]",
		Signature.toString("[[[I"));
}

// signatures with 1.5 elements

public void testToStringType16() {
	assertEquals(
		"VAR",
		Signature.toString("TVAR;"));
}
public void testToStringType17() {
	assertEquals(
		"A<B>",
		Signature.toString("QA<QB;>;"));
}
public void testToStringType18() {
	assertEquals(
		"A<?>",
		Signature.toString("QA<*>;"));
}
public void testToStringType19() {
	assertEquals(
		"A<? extends B>",
		Signature.toString("QA<+QB;>;"));
}
public void testToStringType20() {
	assertEquals(
		"A<? super B>",
		Signature.toString("QA<-QB;>;"));
}
public void testToStringType21() {
	assertEquals(
		"A<?,?,?,?,?>",
		Signature.toString("LA<*****>;"));
}
public void testToStringType22() {
	assertEquals(
		"a<V>.b<W>.c<X>",
		Signature.toString("La<TV;>.b<QW;>.c<LX;>;"));
}
public void testToStringType23() {
	assertEquals(
		"java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>",
		Signature.toString("Qjava.y.Map<[QObject;QString;>.MapEntry<[Qp.K<QT;>;[Qq.r.V2;>;"));
}
public void testToStringType24() {
	assertEquals(
		"Stack<List<Object>>",
		Signature.toString("QStack<QList<QObject;>;>;"));
}
public void testToStringType25() {
	assertEquals(
		"?",
		Signature.toString("*"));
}
public void testToStringType26() {
	assertEquals(
		"? extends Object",
		Signature.toString("+QObject;"));
}
public void testToStringType27() {
	assertEquals(
		"? super InputStream",
		Signature.toString("-QInputStream;"));
}
/**
 * @see Signature.toString(String, String, String[], boolean, boolean)
 */
public void testToStringMethod01() {
	assertEquals(
		"void main(String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true));
}
public void testToStringMethod02() {
	assertEquals(
		"main(String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, false));
}
public void testToStringMethod03() {
	assertEquals(
		"main(java.lang.String[] args)",
		Signature.toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, true, false));
}
public void testToStringMethod04() {
	assertEquals(
		"(java.lang.String[])",
		Signature.toString("([Ljava.lang.String;)V", null, null, true, false));
}
public void testToStringMethod05() {
	assertEquals(
		"String main(String[] args)",
		Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, false, true));
}
public void testToStringMethod06() {
	assertEquals(
		"java.lang.String main(java.lang.String[] args)",
		Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, true, true));
}
public void testToStringMethod07() {
	assertEquals(
		"java.lang.String main(java.lang.String[] args)",
		Signature.toString("main([Ljava.lang.String;)Ljava.lang.String;", "main", new String[] {"args"}, true, true));
}
public void testToStringMethod08() {
	assertEquals(
		"java.lang.String[] foo()",
		Signature.toString("()[Ljava.lang.String;", "foo", null, true, true));
}
public void testToStringMethod09() {
	assertEquals(
		"I foo(C, L)",
		Signature.toString("(LC;LL;)LI;", "foo", null, true, true));
}
public void testToStringMethod10() {
	assertEquals(
		"char[][] foo()",
		Signature.toString("()[[C", "foo", null, true, true));
}
public void testToStringMethod11() {
	assertEquals(
		"void foo(java.lang.Object, String[][], boolean, byte, char, double, float, int, long, short)",
		Signature.toString("(Ljava.lang.Object;[[QString;ZBCDFIJS)V", "foo", null, true, true));
}
public void testToStringMethod12() {
	try {
		Signature.toString("([Ljava.lang.String;V", null, null, true, false);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should get an exception", false);
}

/**
 * Test the toString() signature of an inner type.
 */
public void testToStringInnerType() {
	assertEquals(
		"Signature#toString is not correct", 
		"x.y.A.Inner",
		Signature.toString("Lx.y.A$Inner;"));
}

/**
 * @see Signature.getTypeSignatureKind(String)
 */
public void testGetTypeSignatureKind() {
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 1", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Ljava.lang.String;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 2", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[Ljava.lang.String;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 3", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QString;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 4", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[QString;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 5", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Z"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 6", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("B"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 7", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("C"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 8", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("D"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 9", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("F"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 10", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("I"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 11", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("J"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 12", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("S"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 13", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("V"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 14", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[[I"));
	
	// signatures with 1.5 elements
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 15", 
		Signature.TYPE_VARIABLE_SIGNATURE,
		Signature.getTypeSignatureKind("TVAR;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 16", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 17", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<*>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 18", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<+QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 19", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<-QB;>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 20", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("LA<*****>;"));
	assertEquals(
		"Signature#getTypeSignatureKind(String) is not correct 21", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;"));
}

/**
 * @see Signature.getTypeSignatureKind(char[])
 */
public void testGetTypeSignatureKind2() {
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 1", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Ljava.lang.String;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 2", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[Ljava.lang.String;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 3", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QString;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 4", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[QString;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 5", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("Z".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 6", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("B".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 7", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("C".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 8", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("D".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 9", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("F".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 10", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("I".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 11", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("J".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 12", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("S".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 13", 
		Signature.BASE_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("V".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 14", 
		Signature.ARRAY_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("[[[I".toCharArray()));
	
	// signatures with 1.5 elements
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 15", 
		Signature.TYPE_VARIABLE_SIGNATURE,
		Signature.getTypeSignatureKind("TVAR;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 16", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 17", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<*>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 18", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<+QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 19", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("QA<-QB;>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 20", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("LA<*****>;".toCharArray()));
	assertEquals(
		"Signature#getTypeSignatureKind(char[]) is not correct 21", 
		Signature.CLASS_TYPE_SIGNATURE,
		Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;".toCharArray()));
}
public void testGetTypeFragment01() {
	assertEquals(
		"C.D.E",
		Signature.getSignatureSimpleName("La.b.C$D$E;"));
}
public void testGetTypeFragment02() {
	assertEquals(
		"C.D.E",
		Signature.getSignatureSimpleName("LC$D$E;"));
}
public void testGetTypeFragment03() {
	assertEquals(
		"C<X>.D.E",
		Signature.getSignatureSimpleName("La.b.C<LX;>.D$E;"));
}
public void testGetPackageFragment01() {
	assertEquals(
		"a.b",
		Signature.getSignatureQualifier("La.b.C$D$E;"));
}
public void testGetPackageFragment02() {
	assertEquals(
		"",
		Signature.getSignatureQualifier("LC$D$E;"));
}
public void testGetPackageFragment03() {
	assertEquals(
		"a.b",
		Signature.getSignatureQualifier("La.b.C<LX;>.D$E;"));
}
public void testGetPackageFragment04() {
	assertEquals(
		"",
		Signature.getSignatureQualifier("LC<LX;>.D$E;"));
}
}
