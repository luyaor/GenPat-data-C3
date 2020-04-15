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

import java.util.*;
import junit.framework.*;

import koala.dynamicjava.tree.*;
//import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;

import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;

import koala.dynamicjava.interpreter.throwable.WrongVersionException;

/**
 * 
 * Tests to ensure the type checker behaves 
 * 
 */
public class Distinction1415Test extends TestCase {
  private TreeInterpreter astInterpreter;
  private TreeInterpreter strInterpreter;
  
  private ParserFactory parserFactory;
  private String testString;
  
  public Distinction1415Test(String name) {
    super(name);
  }
  
  public void setUp(){
    parserFactory = new JavaCCParserFactory();
    astInterpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
    strInterpreter = new TreeInterpreter(parserFactory); // ParserFactory is needed to interpret a string
  }
    
  public Object interpret(String testString) throws InterpreterException {
    return strInterpreter.interpret(new StringReader(testString), "Unit Test");
  }
  
  /**
   * Test that the use of generic reference types fails when the runtime environment version is set to 1.4
   */
  public void testGenericReferenceTypes14(){
    String currentversion = System.getProperty("java.specification.version");
    System.setProperty("java.specification.version","1.4");
    
    try{
      testString =
        "import java.util.LinkedList;\n"+
        "LinkedList<String> l = new LinkedList<String>();\n"+
        "l.add(\"Str1Str2Str3\");\n"+
        "l.get(0);\n";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    System.setProperty("java.specification.version",currentversion); 
  }
  
  /**
   * Test that the use of generic reference types does not fail when the runtime environment version is set to 1.5
   */
  public void testGenericReferenceTypes15(){
    String currentversion = System.getProperty("java.specification.version");
    System.setProperty("java.specification.version","1.5");
    try{
      testString =
        "import java.util.LinkedList;\n"+
        "LinkedList<String> l = new LinkedList<String>();\n"+
        "l.add(\"Str1Str2Str3\");\n"+
        "l.get(0);\n";
      interpret(testString);
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    System.setProperty("java.specification.version",currentversion); 
  }
  
  /**
   * Test that the use of autoboxing and auto-unboxing fails when the runtime environment version is set to 1.4
   */
  public void testAutoboxing14() {
    String currentversion = System.getProperty("java.specification.version");
    System.setProperty("java.specification.version","1.4");
    
    //Auto box
    try{
      testString =
        "int i = 5;" +
        "Integer j;" +
        "j = i;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto unbox
    try{
      testString =
        "Character c = new Character('c');" +
        "char d;" +
        "d = c;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto box in declaration
    try{
      testString =
        "Integer k = 5;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto unbox in declaration
    try{
      testString =
        "int l = new Integer(5);";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    System.setProperty("java.specification.version",currentversion);    
  }
  
  /**
   * Test that the use of autoboxing and auto-unboxing does not fail when the runtime environment version is set to 1.5
   */
  public void testAutoboxing15() {
    String currentversion = System.getProperty("java.specification.version");
    System.setProperty("java.specification.version","1.5");
    try{
      testString =
        "Character c = new Character('c');" +
        "char d;" +
        "d = c;" +
        "Integer i = 5;" +
        "int j = new Integer(6);"+
        "i = j;"+
        "j = i;";
      interpret(testString);
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    System.setProperty("java.specification.version",currentversion); 
  }
  
 
  
  
}

