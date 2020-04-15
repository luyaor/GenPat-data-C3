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
package org.eclipse.jdt.core.tests.compiler.parser;

public class CompletionParserTest2 extends AbstractCompletionTest {
public CompletionParserTest2(String testName) {
	super(testName);
}
public void test0001(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = <CompleteOnName:zzz>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
		
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0003(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
		
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0004(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
		
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o = <CompleteOnName:zzz>;\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0006(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0007(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = new zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new <CompleteOnType:zzz>();\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}
public void test0008(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = new <CompleteOnType:zzz>();\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0009(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new <CompleteOnType:zzz>();\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0010(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = new zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new <CompleteOnType:zzz>();\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}
public void test0011(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" +
		"    {\n" +
		"      Object o = new <CompleteOnType:zzz>();\n" +
		"    }\n" +
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0012(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new <CompleteOnType:zzz>();\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0013(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = yyy;\n" + 
		"  zzz\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0014(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0015(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0016(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = yyy;\n" + 
		"  zzz\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}

public void test0017(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o;\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0018(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0019(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = bar(zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = bar(<CompleteOnName:zzz>);\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}

public void test0020(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = bar(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = bar(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0021(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = bar(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = bar(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0022(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = bar(zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = bar(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}

public void test0023(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = bar(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o = bar(<CompleteOnName:zzz>);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0024(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = bar(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = bar(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0025(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = new X(zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new X(<CompleteOnName:zzz>);\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}


public void test0026(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = new X(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = new X(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0027(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = new X(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new X(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0028(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = new X(zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new X(<CompleteOnName:zzz>);\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}


public void test0029(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = new X(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o = new X(<CompleteOnName:zzz>);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0030(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = new X(zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new X(<CompleteOnName:zzz>);\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0031(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = {zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0032(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0033(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0034(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = {zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0035(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o;\n" +
		"      {\n" +
		"        <CompleteOnName:zzz>;\n" + 
		"      }\n" +
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0036(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0037(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object[] o = {zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = {<CompleteOnName:zzz>,};\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0038(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object[] o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object[] o = {<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0039(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object[] o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = {<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0040(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object[] o = {zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = {<CompleteOnName:zzz>,};\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0041(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object[] o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object[] o = {<CompleteOnName:zzz>,};\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0042(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object[] o = {zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = {<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0043(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object[] o = new X[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = new X[<CompleteOnName:zzz>];\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0044(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object[] o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" +
		"    Object[] o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0045(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object[] o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}



public void test0046(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object[] o = new X[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0047(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object[] o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" +
		"    {\n" +
		"      Object[] o = new X[<CompleteOnName:zzz>];\n" + 
		"    }\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0048(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object[] o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0049(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object[] o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0050(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object[] o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0051(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object[] o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0052(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object[] o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0053(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0054(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object[] o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0055(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object[] o = zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = <CompleteOnName:zzz>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0056(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object[] o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object[] o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0057(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object[] o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0058(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object[] o = zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object[] o = <CompleteOnName:zzz>;\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0059(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object[] o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" +
		"    {\n" +
		"      Object[] o = <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0060(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object[] o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object[] o = <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0061(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = new X[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new X[<CompleteOnName:zzz>];\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0062(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0063(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0064(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = new X[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o = new X[<CompleteOnName:zzz>];\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0065(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o = new X[<CompleteOnName:zzz>];\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0066(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = new X[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new X[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0067(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
		
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0068(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0069(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}

public void test0070(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0071(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o;\n" +
		"      {\n" +
		"        <CompleteOnName:zzz>;\n" +
		"      }\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0072(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    Object o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
		
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0073(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  int o = new int[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o = new int[<CompleteOnName:zzz>];\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0074(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    int o = new int[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    int o = new int[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0075(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int o = new int[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int o = new int[<CompleteOnName:zzz>];\n" +  
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0076(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  int o = new int[zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o = new int[<CompleteOnName:zzz>];\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0077(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    int o = new int[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      int o = new int[<CompleteOnName:zzz>];\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0078(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    int o = new int[zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int o = new int[<CompleteOnName:zzz>];\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0079(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  int o = new int[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o;\n" +
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
		
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
		
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0080(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    int o = new int[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    int o = new int[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0081(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int o = new int[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" +  
		"    {\n" +
		"      new int[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int o = new int[]{<CompleteOnName:zzz>,};\n" +  
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0082(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  int o = new int[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o;\n" +
		"  {\n" +
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  int o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0083(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    int o = new int[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
		
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      int o;\n" +
		"      {\n" +
		"        <CompleteOnName:zzz>;\n" +
		"      }\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0084(){


	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    int o = new int[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new int[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0085(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  X o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  X o;\n" +
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  X o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0086(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    X o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    X o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0087(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    X o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    X o = new X[]{<CompleteOnName:zzz>,};\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0088(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  X o = new X[]{zzz;\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  X o;\n" +
		"  {\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  X o;\n" +
		"  {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0089(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    X o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      X o;\n" +
		"      {\n" +
		"        <CompleteOnName:zzz>;\n" +
		"      }\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0090(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    #\n" +
		"    X o = new X[]{zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>,}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    {\n" +
		"      new X[]{<CompleteOnName:zzz>,};\n" + 
		"    }\n" +
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    X o;\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0091(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  Object o = \"yyy;\n" + 
		"  zzz\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0092(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    Object o = \"yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0093(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o = \"yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0094(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  #\n" + 
		"  Object o = \"yyy;\n" + 
		"  zzz\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  Object o;\n" +
		"  <CompleteOnType:zzz>;\n" +
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast"); 
}


public void test0095(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = \"yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" + 
		"      Object o;\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}


public void test0096(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  {\n" + 
		"    #\n" +
		"    Object o = \"yyy;\n" + 
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"  }\n" +
		"  public X() {\n" + 
		"  }\n" +  
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  {\n" + 
		"    {\n" +
		"      Object o;\n" +
		"      <CompleteOnName:zzz>;\n" + 
		"    }\n" +
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast"); 
}
public void test0097(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    o = <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0098(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    o = <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0099(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    o = new <CompleteOnType:zzz>();\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0100(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = new zzz;\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    o = new <CompleteOnType:zzz>();\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0101(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0102(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = yyy;\n" +
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0103(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0104(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    Object o;\n" +
		"    o = \"yyy;\n" +
		"    zzz\n" + 
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    Object o;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0105(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = 1 + zzz\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x = (1 + <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0106(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = 1 + (zzz\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x = (1 + <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0107(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = 0;\n" +
		"    int y = 1 + x;\n" +
		"    zzz;\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x;\n" +
		"    int y;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0108(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = -zzz;\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x = (- <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0109(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = -(zzz;\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x = (- <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0110(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    int x = 0;\n" +
		"    int y = -x;\n" +
		"    zzz;\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    int x;\n" +
		"    int y;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0111(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    if(1 == zzz) {}\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    (1 == <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0112(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(){\n" + 
		"    if(1 == (zzz)) {}\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    (1 == <CompleteOnName:zzz>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0113(){
	String str = 
		"package p;\n" +  
		"public class X {\n" +
		"  void foo(Object x){\n" + 
		"    if(x instanceof ZZZ) {}\n" +
		"  }\n" + 
		"}\n";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo(Object x) {\n" + 
		"  }\n" + 
		"}\n";
	
	checkDietParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	expectedParentNodeToString = "(x instanceof <CompleteOnType:ZZZ>)";
	completionIdentifier = "ZZZ";
	expectedReplacedSource = "ZZZ";
	expectedUnitDisplayString =
		"package p;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo(Object x) {\n" + 
		"    (x instanceof <CompleteOnType:ZZZ>);\n" +
		"  }\n" + 
		"}\n";
	
	checkMethodParse(
		str.toCharArray(), 
		cursorLocation, 
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0114(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    boolean a;\n" +
		"    boolean b;\n" +
		"    boolean c;\n" +
		"    c = <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0115(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b;\n" +
		"    a == b ? zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    boolean a;\n" +
		"    boolean b;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0116(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "c = ((a == b) ? a : <CompleteOnName:zzz>)";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    boolean a;\n" +
		"    boolean b;\n" +
		"    boolean c;\n" +
		"    c = ((a == b) ? a : <CompleteOnName:zzz>);\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0117(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a == b ? a : (zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    boolean a;\n" +
		"    boolean b;\n" +
		"    boolean c;\n" +
		"    c = <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0118(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    boolean a, b, c;\n" +
		"    c = a# == b ? a : zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    boolean a;\n" +
		"    boolean b;\n" +
		"    boolean c;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0119(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0120(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(1) {\n" +
		"      case Something :\n" +
		"      case zzz\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:zzz>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0121(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "tab[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    tab[<CompleteOnName:zzz>];\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0122(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:tab[].zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "tab[].zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnClassLiteralAccess:tab[].zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0123(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    tab[0].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnMemberAccess:tab[0].zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:tab[0].zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0124(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[zzz]\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "foo()[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    foo()[<CompleteOnName:zzz>];\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0125(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0126(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    foo()[1].zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnMemberAccess:foo()[1].zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnMemberAccess:foo()[1].zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0127(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    if (zzz() == null) bar = null;\n" +
		"  }\n" +
		"  Object o = new O();\n" +
		"}\n";

	String completeBehind = "O";
	int cursorLocation = str.lastIndexOf("O") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:O>()";
	String completionIdentifier = "O";
	String expectedReplacedSource = "O";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  Object o = new <CompleteOnType:O>();\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0128(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    new Object() {\n" +
		"      void bar() {\n" +
		"        a[zzz\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "a[<CompleteOnName:zzz>]";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Object() {\n" +
		"      () {\n" +
		"      }\n" +
		"      void bar() {\n" +
		"        a[<CompleteOnName:zzz>];\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0129(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    Object local;\n" +
		"    double bar;\n" +
		"    for(;;) {\n" +
		"      bar = (double)0;\n" +
		"    }\n" +
		"    zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "zzz";
	expectedReplacedSource = "zzz";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object local;\n" +
		"    double bar;\n" +
		"    <CompleteOnName:zzz>;\n" +
		"  }\n" +
		"}\n";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
}
