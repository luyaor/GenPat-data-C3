/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;

import junit.framework.TestCase;

/**Do the TypeChecking appropriate to the context of a class body.  Common to all Language Levels.*/
public class InterfaceBodyTypeChecker extends Bob {
  
  /**The SymbolData corresponding to this interface.*/
  private SymbolData _symbolData;
  
  
    /*
   * Constructor for InterfaceBodyTypeChecker.
   * Adds all the variables in the symbol data to the list of what can be seen from this context,
   * since interface fields can always be seen.
   * 
   * @param sd  The SymbolData of the interface we are type checking.
   * @param file  The File corresponding to the source file we are checking.
   * @param packageName  The package of the source file.
   * @param importedFiles  A list of the names of the classes that are specifically imported in the source file
   * @param importedPackages  A list of the names of the packages that are imported in the source file.
   * @param vars  A list of the variable datas that can be seen and have been given a value before this context
   * @param thrown  The exceptions that are thrown
   */
  public InterfaceBodyTypeChecker(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(sd, file, packageName, importedFiles, importedPackages, vars, thrown);
    _vars.addAll(sd.getVars());
    _symbolData = sd;
  }

  /**@return the SymbolData corresponding to the enclosing interface*/
  protected Data _getData() {
    return _symbolData;
  }

  
  /** All fields in interfaces must be initialized where they are declared, so throw an error.*/
  public TypeData forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    _addError("All fields in interfaces must be initialized", that);
    return null;
  }

  /**
   * Resolve all the stuff that is stored in the AbstractMethodDef.  Find the method data
   * that was created on the first pass to correspond to this method, and make sure that this 
   * method doesn't resolve another method with a different return type.
   */
  public TypeData forAbstractMethodDef(AbstractMethodDef that) {
    final TypeData mav_result = that.getMav().visit(this);
    final TypeData[] typeParams_result = makeArrayOfRetType(that.getTypeParams().length);
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = that.getTypeParams()[i].visit(this);
    }
    final TypeData result_result = getSymbolData(that.getResult().getName(), _symbolData, that);//that.getResult().visit(this);
    final TypeData name_result = that.getName().visit(this);
    final TypeData[] params_result = makeArrayOfRetType(that.getParams().length);
    for (int i = 0; i<params_result.length; i++) {
      params_result[i] = getSymbolData(that.getParams()[i].getDeclarator().getType().getName(), _symbolData, that.getParams()[i]);
    }
    final TypeData[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = getSymbolData(that.getThrows()[i].getName(), _symbolData, that.getThrows()[i]);//that.getThrows()[i].visit(this);
    }
    // Ensure that this method doesn't override another method with a different return type.
    MethodData md = _symbolData.getMethod(that.getName().getText(), params_result);
    if (md == null) {
      throw new RuntimeException("Internal Program Error: Could not find the method " + that.getName().getText() + " in interface " + _symbolData.getName() + ".  Please report this bug.");
    }
    SymbolData.checkDifferentReturnTypes(md, _symbolData, _targetVersion);
    return result_result;
  }
  

  /**This error should be thrown in the first pass, but throw it again here, just in case*/
   public TypeData forConcreteMethodDef(ConcreteMethodDef that) {
     _addError("Concrete method definitions cannot appear in interfaces", that);
     return null;
   }
  
   /**
    * Try to resolve the type that is referenced.  Check to see if it is accessible from this context.
    */
    public TypeData forTypeOnly(Type that) {
    Data sd = getSymbolData(that.getName(), _symbolData, that);
    if (sd != null) {sd = sd.getOuterData();}
    while (sd != null && !LanguageLevelVisitor.isJavaLibraryClass(sd.getSymbolData().getName())) {
      if (!checkAccessibility(that, sd.getMav(), sd.getName(), sd.getSymbolData(), _symbolData, "class or interface")) {
        return null;
      }
      sd = sd.getOuterData();
    }
    return null;
  }
    
  
   /**
    * Test the methods declared in the above class.
    */
  public static class InterfaceBodyTypeCheckerTest extends TestCase {
    
    private InterfaceBodyTypeChecker _ibbtc;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    
    
    public InterfaceBodyTypeCheckerTest() {
      this("");
    }
    public InterfaceBodyTypeCheckerTest(String name) {
      super(name);
//      _file = File.createTempFile("DrJava-test", ".java");
//      _bv = new BeginnerVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
//                                 new LinkedList<String>());
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      _ibbtc = new InterfaceBodyTypeChecker(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      _ibbtc._targetVersion = JavaVersion.JAVA_5;
      _ibbtc._importedPackages.addFirst("java.lang");
    }
    
    public void testForUninitializedVariableDeclaratorOnly() {
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, false, _sd1);
      _sd1.addVar(vd1);
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                                new Word(JExprParser.NO_SOURCE_INFO, "Mojo"));
      uvd.visit(_ibbtc);
      _ibbtc.forUninitializedVariableDeclaratorOnly(uvd, SymbolData.INT_TYPE, null);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "All fields in interfaces must be initialized", errors.get(0).getFirst());
    }
    
    public void testForInitializedVariableDeclaratorOnly() {
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, false, _sd1);
      _sd1.addVar(vd1);
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                            new Word(JExprParser.NO_SOURCE_INFO, "Mojo"), 
                                                                            new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      ivd.visit(_ibbtc);
      assertEquals("There should be no errors.", 0, errors.size());
      assertTrue("_vars should contain Mojo.", _ibbtc._vars.contains(vd1));
      ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                              new Word(JExprParser.NO_SOURCE_INFO, "Santa's Little Helper"), 
                                              new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      try {
        ivd.visit(_ibbtc);
        fail("Should have thrown a RuntimeException because there's no field named Santa's Little Helper.");
      }
      catch (RuntimeException re) {
        assertEquals("The error message should be correct.", "Internal Program Error: The field or variable Santa's Little Helper was not found in this block.  Please report this bug.", re.getMessage());
      }
    }
    
    public void testForConcreteMethodDef() {
      FormalParameter[] fps = new FormalParameter[] {
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                              new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                            false),
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                                                              new Word (JExprParser.NO_SOURCE_INFO, "field2")),
                            false)};
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    fps,
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {
        new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, 
                                 new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))}));
      MethodData md = new MethodData("methodName", 
                                     _packageMav, 
                                     new TypeParameter[0], 
                                     SymbolData.INT_TYPE, 
                                     new VariableData[] { new VariableData(SymbolData.DOUBLE_TYPE), new VariableData(SymbolData.BOOLEAN_TYPE) },
                                     new String[0],
                                     _sd1,
                                     null); // no SourceInfo
                                     
      md.getParams()[0].setEnclosingData(md);                               
      md.getParams()[1].setEnclosingData(md);                               

      _sd1.addMethod(md);
//      _ibbtc._vars.addLast(new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE));
      cmd.visit(_ibbtc);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "Concrete method definitions cannot appear in interfaces", errors.get(0).getFirst());
    }
    

    
    public void testForTypeOnly() {
      Type t = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double");
      t.visit(_ibbtc);
      assertEquals("There should be no errors", 0, errors.size());
      
      SymbolData sd = new SymbolData("Adam");
      sd.setIsContinuation(false);
      symbolTable.put("Adam", sd);
      sd.setMav(_publicMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam", new Type[0]);
      t.visit(_ibbtc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      SymbolData innerSd = new SymbolData("Adam$Wulf");
      innerSd.setIsContinuation(false);
      sd.addInnerClass(innerSd);
      innerSd.setOuterData(sd);
      innerSd.setMav(_publicMav);
      _ibbtc.symbolTable.put("USaigehgihdsgslghdlighs", innerSd);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_ibbtc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      innerSd.setMav(_privateMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_ibbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The class or interface Adam.Wulf is private and cannot be accessed from " + _ibbtc._symbolData.getName(),
                   errors.get(0).getFirst());
      
      sd.setMav(_privateMav);
      innerSd.setMav(_publicMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_ibbtc);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "The class or interface Adam is private and cannot be accessed from " + _ibbtc._symbolData.getName(),
                   errors.get(1).getFirst());
    }
  }
}