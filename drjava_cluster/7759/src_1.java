/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.interpreter;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.parser.wrapper.*;

import junit.framework.TestCase;

import edu.rice.cs.drjava.model.repl.*;

/**
 * This test class tests only those methods that were modified in order to ensure 
 * that the wrapper classes involved in autoboxing/unboxing are allowed.&nbsp; The 
 * methods that were changed pertained to those sections of the JLS that were 
 * modified by Sun when introducing this new feature.
 * <P>Involved Wrapper Classes:</P>
 * <UL>
 *   <LI>Boolean
 *   <LI>Byte
 *   <LI>Character
 *   <LI>Short
 *   <LI>Integer
 *   <LI>Long
 *   <LI>Float
 *   <LI>Double</LI></UL>
 * Involved Operations
 * <UL>
 *   <LI>Assignment
 *   <LI>Method Invocation
 *   <LI>Casting
 *   <LI>Numeric Promotions (Unary and Binary)
 *   <LI>The <CODE>if</CODE> Statement (<CODE>if-then</CODE> and <CODE>if-then-else</CODE>)
 *   <LI>The <CODE>switch</CODE> Statement
 *   <LI>The <CODE>while</CODE> Statement
 *   <LI>The <CODE>do</CODE> Statement
 *   <LI>The <CODE>for</CODE> Statement
 *   <LI>Array Creation
 *   <LI>Unary Operations:</LI>
 *   <UL>
 *     <LI>Postfix Decrement Operator <CODE>--</CODE>
 *     <LI>Postfix Decrement Operator <CODE>--</CODE>
 *     <LI>Prefix Increment Operator <CODE>++</CODE>
 *     <LI>Prefix Decrement Operator <CODE>--</CODE>
 *     <LI>Plus Operator <CODE>+</CODE>
 *     <LI>Minus Operator <CODE>-</CODE>
 *     <LI>Bitwise Complement Operator <CODE>~</CODE>
 *     <LI>Logical Complement Operator <CODE>!</CODE></LI></UL>
 *   <LI>Binary Operators</LI>
 *   <UL>
 *     <LI>Multiplicative Operators <CODE>*, /, %</CODE>
 *     <LI>Additive Operators <CODE>+, -</CODE>
 *     <LI>Shift Operators <CODE>&lt;&lt;, &gt;&gt;, &gt;&gt;&gt;</CODE>
 *     <LI>Numerical Comparison Operators <CODE>&lt;, &lt;=, &gt;, and &gt;=</CODE>
 *     <LI>Integer Bitwise Operators <CODE>&amp;, ^, and |</CODE>
 *     <LI>Boolean Logical Operators <CODE>&amp;, ^, and |</CODE>
 *     <LI>Conditional Operators <CODE>&amp;&amp;, ||</CODE>
 *     <LI>Conditional Operator <CODE>? :</CODE></LI></UL>
 * </UL>
 * NOTE: Though not explicitly stated in the changed sections of the JLS, the methods 
 * associated with the assignment operators (<CODE>+=, -=, *=, /=, %=, &lt;&lt;=, &gt;&gt;&gt;=, 
 * &gt;&gt;&gt;=, &amp;=, ^=, |=</CODE>) must also be modified and thus tested
 */
public class TypeCheckerTest extends TestCase {
  
  ////// Internal Initialization ////////////////////////
  
  /**
   * The global context we are using.
   */
  private GlobalContext _globalContext;
  
  /**
   * The type checker we are testing.
   */
  private TypeChecker _typeChecker;
  
  /**
   * The interpreter we are using to test our modifications of the ASTs.
   */
  private JavaInterpreter _interpreter;
  
  /**
   * Sets up the tests for execution.
   */
  public void setUp() {
    _globalContext = new GlobalContext(new TreeInterpreter(new JavaCCParserFactory()));
    _globalContext.define("x", int.class);
    _globalContext.define("X", Integer.class);
    _globalContext.define("B", Boolean.class);
    _globalContext.define("b", boolean.class);
    _typeChecker = new TypeChecker(_globalContext);
    _interpreter = new DynamicJavaAdapter();
  }
  
  /**
   * Parses the given string and returns the list of Nodes.
   * @param code the code to parse
   * @return the list of Nodes
   */
  private List<Node> _parseCode(String code) {
    JavaCCParserFactory parserFactory = new JavaCCParserFactory();
    SourceCodeParser parser = parserFactory.createParser(new java.io.StringReader(code), "");
    return parser.parseStream();
  }
  
  private Class _checkBinaryExpression(String text, String leftExpected, String rightExpected) 
    throws ExceptionReturnedException {
    
    BinaryExpression exp = (BinaryExpression)_parseCode(text).get(0);
    
    Class type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getLeftExpression().toString();
    assertEquals("Left should have unboxed correctly.", leftExpected, actual);

    actual = exp.getRightExpression().toString();
    assertEquals("Right should have unboxed correctly.", rightExpected, actual);
    
    _interpreter.interpret(text);
    
    return type;
  }
  
  private Class _checkUnaryExpression(String text, String expected) 
    throws ExceptionReturnedException {
    
    UnaryExpression exp = (UnaryExpression)_parseCode(text).get(0);
    
    Class type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getExpression().toString();
    assertEquals("Expression should have unboxed correctly.", expected, actual);

    _interpreter.interpret(text);
    
    return type;
  }
  
  ////// Control Statements /////////////////////////////
  
  /**
   * Tests the While statement's condition statement
   */
  public void testVisitWhileStatement() throws ExceptionReturnedException {
    String text = "while (B) { }";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((WhileStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret("Boolean B = Boolean.FALSE; " + text);
  }
  
  /**
   * Tests the do-while loop's condition statement
   */
  public void testVisitDoStatement() throws ExceptionReturnedException {
    String text = "do { } while(B);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((DoStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret("Boolean B = Boolean.FALSE; " + text);
  }  
  
  /**
   * Tests the for loop's condition statement
   */
  public void testVisitForStatement() throws ExceptionReturnedException {
    String text = "for(int i=0; new Boolean(i<1); i++);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.LessExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    String actual = ((ForStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);

    _interpreter.interpret(text);
  }
  
  public void testSwitchStatement() throws ExceptionReturnedException {
    String text = "switch (new Integer(1)) { }";
    SwitchStatement stmt = (SwitchStatement)_parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String actual = stmt.getSelector().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }
  

  public void testIfThenStatement() throws ExceptionReturnedException {
    String text = "if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret("Boolean B = Boolean.TRUE;" + text);
  }
  
  /**
   * Tests the if-then-else statement for auto-unboxing.
   */
  public void testIfThenElseStatement() throws ExceptionReturnedException {
    String text = "if (B) { } else if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret("Boolean B = Boolean.TRUE;" + text);
  }
  
  //////////// Addititve Bin Ops ////////////////////////
  /**
   * Tests adding two Integers.
   */
  public void testAddTwoIntegers() throws ExceptionReturnedException {
    
    String text = "new Integer(1) + new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }

  /**
   * Tests substracting two Integers.
   */
  public void testSubtractingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) - new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  ///////////// Additive Assignemt //////////////////////
  
  /**
   * Tests the += operation.
   */
  public void testPlusEquals() {
    Node exp = _parseCode("x += new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  /**
   * Tests the -= operation.
   */
  public void testMinusEquals() {
    Node exp = _parseCode("x -= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  
  //////////// Multiplicitive Bin Ops ///////////////////
  
  /**
   * Tests multiplying two Integers.
   */
  public void testMultiplyingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) * new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testDividingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) / new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
      
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testModingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) % new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  //////////// Multiplicitive Assignments ///////////////
  
  /**
   * Tests the *= operation.
   */
  public void testMultEquals() {
    Node exp = _parseCode("x *= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  /**
   * Tests the /= operation.
   */
  public void testDivideEquals() {
    Node exp = _parseCode("x /= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  /**
   * Tests the %= operation.
   */
  public void testModEquals() {
    Node exp = _parseCode("x %= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  //////////// Shift Bin Ops ////////////////////////////
  /**
   * Tests Shift Right on two Shorts
   */
  public void testShiftRight() throws ExceptionReturnedException {
    String text = "(new Short(\"1\") >> new Short(\"2\"));";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"1\" 1 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Shift Left on two Shorts
   */
  public void testShiftLeft() throws ExceptionReturnedException {
    String text = "new Short(\"-10\") << new Short(\"2\");";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"-10\" -10 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Unsigned Shift on two longs
   */
  public void testUShiftRight() throws ExceptionReturnedException {
    String text = "new Long(-1) >>> new Long(1);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.MinusExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Shift Assignments ////////////////////////
/*
  There's a problem to resolve here.  These tests right now will pass because
  the Shift*Assign cases currently use the same type checking as the binary
  operations.  The binary operations work, but the assigns do not (in the 
  interpreter at least).  These tests would fail if tied into the drjava 
  interpreter.
  */
  
//  /**
//   * Tests the <<= operation.
//   */
//  public void testLeftShiftEquals() {
//    Node exp = _parseCode("x <<= new Integer(2);").get(0);
//    
//    try {
//      exp.acceptVisitor(_typeChecker);
//      fail("Should have thrown an excpetion.");
//    }
//    catch (ExecutionError ee) {
//    }
//  }
//  
//  /**
//   * Tests the >>= operation.
//   */
//  public void testRightShiftEquals() {
//    Node exp = _parseCode("x >>= new Integer(2);").get(0);
//    
//    try {
//      exp.acceptVisitor(_typeChecker);
//      fail("Should have thrown an excpetion.");
//    }
//    catch (ExecutionError ee) {
//    }
//  }
//  
//  /**
//   * Tests the >>>= operation.
//   */
//  public void testUnsignedRightShiftEquals() {
//    Node exp = _parseCode("x >>>= new Integer(2);").get(0);
//    
//    try {
//      exp.acceptVisitor(_typeChecker);
//      fail("Should have thrown an excpetion.");
//    }
//    catch (ExecutionError ee) {
//    }
//  }
//  


  //////////// Bitwise Bin Ops //////////////////////////
  
  /**
   * Tests XORing two Booleans.
   */
  public void testBooleanBitwiseXOr() throws ExceptionReturnedException {
    String text = "new Boolean(true) ^ new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Booleans.
   */
  public void testBooleanBitwiseAnd() throws ExceptionReturnedException {
    String text = "new Boolean(true) & new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Booleans.
   */
  public void testBooleanBitwiseOr() throws ExceptionReturnedException {
    String text = "new Boolean(true) | new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests XORing two Booleans.
   */
  public void testNumericBitwiseXOr() throws ExceptionReturnedException {
    String text = "new Long(0) ^ new Integer(1);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 0 0 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Integers.
   */
  public void testNumericBitwiseAnd() throws ExceptionReturnedException {
    String text = "new Character('a') & new Integer(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: charValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Character) [(koala.dynamicjava.tree.CharacterLiteral: 'a' a char)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";


    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Integers.
   */
  public void testNumericBitwiseOr() throws ExceptionReturnedException {
    String text = "new Short(\"2\") | new Byte(\"2\");";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: byteValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Byte) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Bitwise Assignments //////////////////////
  
  /**
   * Tests the &= operation.
   */
  public void testAndEquals() {
    Node exp = _parseCode("x &= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  /**
   * Tests the ^= operation.
   */
  public void testXorEquals() {
    Node exp = _parseCode("x ^= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  /**
   * Tests the |= operation.
   */
  public void testOrEquals() {
    Node exp = _parseCode("x |= new Integer(2);").get(0);
    
    try {
      exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an excpetion.");
    }
    catch (ExecutionError ee) {
    }
  }
  
  
  //////////// Boolean/Comparative Bin Ops //////////////
  
  /**
   * Tests ANDing two Booleans.
   */
  public void testAndingTwoBooleans() throws ExceptionReturnedException {
    String text = "new Boolean(true) && new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests ORing two Booleans.
   */
  public void testOringTwoBooleans() throws ExceptionReturnedException {
    String text = "new Boolean(true) || new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan with two Doubles
   */
  public void testGreaterThan() throws ExceptionReturnedException {
    String text = "new Double(1) > new Double(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan or Equal to with two Floats
   */
  public void testGreaterThanEqual() throws ExceptionReturnedException {
    String text = "new Float(1) >= new Float(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Float) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Float) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan to with two Longs
   */
  public void testLessThan() throws ExceptionReturnedException {
    String text = "new Long(12) < new Long(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan Or Equal to with two Integers
   */
  public void testLessThanEqual() throws ExceptionReturnedException {
    String text = "new Integer(12) <= new Integer(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the equality operator (==) with an integer and a short.
   */
  public void testEqualsEquals() throws ExceptionReturnedException {
    String text = "new Integer(1) == new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) == 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the inequality operator (!=) with an integer and a short.
   */
  public void testNotEquals() throws ExceptionReturnedException {
    String text = "new Integer(1) != new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) != 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Compliment Unary Op //////////////////////
  
  /**
   * Tests Complimenting an Integer.
   */
  public void testComplimentingOneBoolean() throws ExceptionReturnedException {
    String text = "~new Integer(24);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 24 24 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Plus Operator.
   */
  public void testPlusOperator() throws ExceptionReturnedException {
    String text = "+new Double(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Minus Operator.
   */
  public void testMinusOperator() throws ExceptionReturnedException {
    String text = "-new Integer(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }

  /**
   * Tests Negating a Boolean.
   */
  public void testNegatingOneBoolean() throws ExceptionReturnedException {
    String text = "!new Boolean(false);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkUnaryExpression(text, expected);
  }
  
  
  //////////// Other Operations //////////////////////
  
  public void testSimpleAssignBox() {
    Node exp = _parseCode("B = true;").get(0);
  }
  public void testSimpleAssignUnbox() {  
    Node exp = _parseCode("b = new Boolean(false);").get(0);
  }
  public void testSimpleAssignBoxInt() {
    Node exp = _parseCode("X = 3 + 5;").get(0);
  }
  public void testSimpleAssignBoxAddExp() {
    Node exp = _parseCode("X = new Integer(1) + new Integer(3);").get(0);
  }
  
  // may need more simple assignment tests
  // need some variable assignment tests.
  
//  public void testUnboingCastExpression() throws ExceptionReturnedException{
//    text = "(int)new Integer(1);";
//  
//    Node exp = _parseCode(text).get(0);
//    Class type = exp.acceptVisitor(_typeChecker);
//    assertEquals("Should be the primitive type.", int.class, type);
//
//    _interpreter.interpret(text);
//  }
//  
//  public void testBoxingCastExpression() throws ExceptionReturnedException{
//    text = "(Integer)1;";
//    Node exp = _parseCode(text).get(0);
//    Class type = exp.acceptVisitor(_typeChecker);
//    assertEquals("Should be the refrence type.", Integer.class, type);
//
//    _interpreter.interpret(text);
//  }
//  
//  public void testDoubleCastExpression() throws ExceptionReturnedException{
//    text = "(Byte)(byte)1;";
//    Node exp = _parseCode(text).get(0);
//    Class type = exp.acceptVisitor(_typeChecker);
//    assertEquals("Should be the primitive type.", int.class, type);
//
//    _interpreter.interpret(text);
//  }
//  
//  public void testErroneousUnoxingCastExpression() throws ExceptionReturnedException{
//    try {
//      text = "(int)new Long(3);";
//      _parseCode(text).get(0).acceptVisitor(_typeChecker);
//      fail("Should have thrown an error");
//    }
//    catch (ExecutionError e) { /* expected */ }
//  }
//  
//  /**
//   * Tests the coniditional expression (... ? ... : ...).
//   */
//  public void testConditionalExpression() throws ExceptionReturnedException{
//    String text = "(Boolean.TRUE) ? Boolean.TRUE : 1;";
//    String expected = "";
//    Node stmt = _parseCode(text).get(0);
//    
//    Class type = stmt.acceptVisitor(_typeChecker);
//    assertEquals("the type should have been Object", Object.class, type);
//    
//    String actual = ((ConditionalStatement)stmt).getCondition().toString();
//    assertEquals("Should have autounboxed", expected, actual);
//    
//    _interpreter.interpret(text);
//  }
//  public void testConditionalExpression() throws ExceptionReturnedException{
//    String text = "(Boolean.TRUE) ? new Integer(1) : 1;";
//    String expected = "";
//    Node stmt = _parseCode(text).get(0);
//    
//    Class type = stmt.acceptVisitor(_typeChecker);
//    assertEquals("the type should have been primitive int", int.class, type);
//    
//    String actual = ((ConditionalStatement)stmt).getCondition().toString();
//    assertEquals("Should have autounboxed", expected, actual);
//    
//    _interpreter.interpret(text);
//  }
  
  public void testVariableDeclaration() {
  }
  
  public void testArrayAllocation() {
  }
  
  public void testArrayInitialization() {
  }
  
  public void testArrayAccess() {
  }

  /**
   * Method calls may or may not be a simple project.  We need to look into what
   * needs to be tested, the different types of method calls that must change,
   * what specifications have been added in the jsr language specs...
   */
}
