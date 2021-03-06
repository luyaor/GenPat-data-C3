/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.*;
import java.util.*;
import java.io.*;

import junit.framework.TestCase;

/*
 * Language Level Visitor that represents the Intermediate Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable within a class body at 
 * the Intermediate Language Level. 
 */
public class ClassBodyIntermediateVisitor extends IntermediateVisitor {
  
  /**The SymbolData corresponding to this class. */
  private SymbolData _classData;
  
   /*
   * Constructor for ClassBodyAdvancedVisitor.
   * @param sd  The SymbolData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
   */
  public ClassBodyIntermediateVisitor(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages, LinkedList<String> classDefsInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);    
    _classData = sd;
  }
  
  /*Add an appropriate error*/
  public Void forStatementDoFirst(Statement that) {
    _addError("Statements cannot appear outside of method bodies", that);
    return null;
  }
  
  
  /* Make sure that this concrete method def is not declared to be abstract or static. */
  public Void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    // Concrete methods can now be public, private, protected at the Intermediate level.  They still cannot be static.
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i].equals("abstract")) {
        _addError("Methods that have a braced body cannot be declared \"abstract\"", that);
        break;
      }
      else if (modifiers[i].equals("static")) {
        _addError("Static methods cannot be used at the Intermediate level", that);
        break;
      }
    }
    return super.forConcreteMethodDefDoFirst(that);
  }
  
  /*Make sure that this abstract method def is declared to be abstract*/
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    if (!_classData.hasModifier("abstract")) {
      _addError("Abstract methods can only be declared in abstract classes", that);
    }
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    // Concrete methods can now be public, private, protected at the Intermediate level.  They still cannot be static.
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i].equals("static")) {
        _addError("Static methods cannot be used at the Intermediate level", that);
        break;
      }
    }
    return super.forAbstractMethodDefDoFirst(that);
  }

  /**Add an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body", that);
    return null;
  }
    
  /* Convert the Variable declartaion to variable datas.  Then, make sure that all
   * static fields are initialized and that no fields are declared to be abstract.
   * Finally, add the variable datas to the symbol data, and give an error if
   * two fields have the same names */
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    VariableData[] vds = _variableDeclaration2VariableData(that, _classData);
    //make sure that none of the static fields are uninitialized:
    LinkedList<VariableData> vdsList = new LinkedList<VariableData>();
    for (int i = 0; i<vds.length; i++) {
      if (vds[i].hasModifier("static") && (that.getDeclarators()[i] instanceof UninitializedVariableDeclarator)) {
        _addAndIgnoreError("All static fields must be initialized", that);
      }
      else if (!vds[i].hasModifier("static") && (that.getDeclarators()[i] instanceof InitializedVariableDeclarator)) {
        _addAndIgnoreError("Only static fields may be initialized outside of a constructor at the Intermediate level", that);
      }
      
      else {
        vdsList.addLast(vds[i]);
      }
    }
    if(!_classData.addFinalVars(vdsList.toArray(new VariableData[vdsList.size()]))) {
      _addAndIgnoreError("You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", that);
    }
    return null;
  }

  /* Create a method data corresponding to this method declaration, and then visit the concrete method def with a new bodybody 
   * visitor, passing it the enclosing method data. Methods are still public by default, but this can be overridden by the user.
   * Make sure the method name is different from the class name.
   */
  public Void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _classData);
    
    // At Intermediate Level, methods are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }

    String className = getUnqualifiedClassName(_classData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                         that);
    }
    else _classData.addMethod(md);

    that.getBody().visit(new BodyBodyIntermediateVisitor(md, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    return forConcreteMethodDefOnly(that);
  }

  /*Create a method data corresponding to this method declaration, and then visit the
   * abstract method def with a new bodybody visitor, passing it the enclosing method data.
   * Make sure the method name is different from the class name.
   * At the Intermediate Level, methods are public by default, but this can be overridden by the user.
   */
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _classData);

    //At Intermediate Level, methods are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }
    
    String className = getUnqualifiedClassName(_classData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                         that);
    }
    else {
      _classData.addMethod(md);
    }
    return null;
  }
  
 
  

  /** Create a constructor corresponding to the specifications in the ConstructorDef.  */
  public Void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    if (prune(that)) return null;

    that.getMav().visit(this);
    String name = getUnqualifiedClassName(that.getName().getText());
    if (!name.equals(getUnqualifiedClassName(_classData.getName()))) {
      _addAndIgnoreError("The constructor return type and class name must match", that);
    }

    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    SymbolData returnType = _classData;
    MethodData md = new MethodData(name, that.getMav(), new TypeParameter[0], returnType, 
                                   new VariableData[0], throwStrings, _classData, that);
    
    // At Intermediate Level, constructors are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }
    
    _checkError(); // reset check flag
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParameters(), md);
    if (! _checkError()) {  //if there was an error converting the formalParameters, don't use them.
      md.setParams(vds);
      if (!md.addFinalVars(vds)) {
        _addAndIgnoreError("You cannot have two method parameters with the same name", that);
      }
    }
    
    _classData.addMethod(md);
    that.getStatements().visit(new BodyBodyIntermediateVisitor(md, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    //note that we have seen a constructor.
    _classData.incrementConstructorCount();
    return forConstructorDefOnly(that);
  }
  
  /** Delegate to method in LLV. */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _classData);
    return null;
  }

  /** Delegate to method in LLV. */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _classData);
    return null;
  }
  
  /** Test the methods in the above (enclosing) class. */
  public static class ClassBodyIntermediateVisitorTest extends TestCase {
    
    private ClassBodyIntermediateVisitor _cbiv;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _publicFinalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public", "final"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static"});
    private ModifiersAndVisibility _abstractStaticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract", "static"});
    private ModifiersAndVisibility _privateAbstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private", "abstract"});
    private ModifiersAndVisibility _finalStaticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static", "final"});
    private ModifiersAndVisibility _finalPrivateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final", "private"});
    
    
    public ClassBodyIntermediateVisitorTest() {
      this("");
    }
    public ClassBodyIntermediateVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _cbiv = new ClassBodyIntermediateVisitor(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      _cbiv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _cbiv._resetNonStaticFields();
      _cbiv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _privateMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      
      
      
      // Check one that doesn't work because it is declared abstract but is actually a concrete method
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd2.visit(_cbiv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Methods that have a braced body cannot be declared \"abstract\"", errors.get(0).getFirst());
      
      // Check one that doesn't work because it is static
      ConcreteMethodDef cmd3 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _staticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd3.visit(_cbiv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "Static methods cannot be used at the Intermediate level", errors.get(1).getFirst());
      
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that doesn't work
      AbstractMethodDef amd = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _abstractMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0]);
      amd.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes", 
                   errors.get(0).getFirst());
      
      // Check one that works
      _cbiv._classData.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbiv);
      assertEquals("There should still be one error", 1, errors.size());
      
      // Check one that doesn't work because it is static
      AbstractMethodDef amd3 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractStaticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_cbiv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "Static methods cannot be used at the Intermediate level", errors.get(1).getFirst());
    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(JExprParser.NO_SOURCE_INFO, 
                                                       new Block(JExprParser.NO_SOURCE_INFO, 
                                                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])));
      ii.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "This open brace must mark the beginning of a method or class body", errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor,
     * perhaps we could factor it out. */
    
    public void testForVariableDeclarationOnly() {
      // Check one that works
      VariableDeclaration vdecl = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field1")),
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field2"))});
      VariableData vd1 = new VariableData("field1", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbiv._classData);
      VariableData vd2 = new VariableData("field2", _finalPrivateMav, SymbolData.BOOLEAN_TYPE, false, _cbiv._classData);
      vdecl.visit(_cbiv);
      
      assertEquals("There should not be any errors.", 0, errors.size());
      assertTrue("field1 was added.", _sd1.getVars().contains(vd1));
      assertTrue("field2 was added.", _sd1.getVars().contains(vd2));
      
      //TODO: Test that static fields are made public
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field3")),
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field3"))});
      VariableData vd3 = new VariableData("field3", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbiv._classData);
      vdecl2.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", errors.get(0).getFirst());
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));
      
      //Check a static field that has not been assigned (won't work)
      VariableDeclaration vdecl3 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _staticMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field4"))});
      VariableData vd4 = new VariableData("field4", _finalStaticMav, SymbolData.DOUBLE_TYPE, false, _cbiv._classData);
          vdecl3.visit(_cbiv);
        assertEquals("There should be two errors", 2, errors.size());
        assertEquals("The error message should be correct", "All static fields must be initialized", errors.get(1).getFirst());
        assertFalse("field4 was not added.", _sd1.getVars().contains(vd4));
        
      
      //Check a non-static field that has been assigned.  (won't work);
      VariableDeclaration vdecl5 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field5"), new DoubleLiteral(JExprParser.NO_SOURCE_INFO, 2.4))});
      vdecl5.visit(_cbiv);
      VariableData vd5 = new VariableData("field5", _publicFinalMav, SymbolData.DOUBLE_TYPE, true, _cbiv._classData);
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("The new error message should be correct", "Only static fields may be initialized outside of a constructor at the Intermediate level", errors.get(2).getFirst());
      assertFalse("Field 5 was not added.", _sd1.getVars().contains(vd5));
      
      //check one that overrides the super class's field
      VariableDeclaration vdecl6 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field6"))});
      
      
      VariableData vd6 = new VariableData("field6", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbiv._classData);
      SymbolData myData = new SymbolData("myData");
      myData.addVar(vd6);
      _cbiv._classData.setSuperClass(myData);
      vdecl6.visit(_cbiv);
      assertEquals("There should be four errors.", 4, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", errors.get(3).getFirst());

    }
    
    public void testFormalParameters2VariableData() {
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
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, _cbiv._classData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, true, _cbiv._classData);
      VariableData[] vds = _cbiv.formalParameters2VariableData(fps, _cbiv._classData);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    

    

    
    public void testForConcreteMethodDef() {
      // Test one that works.
      MethodDef mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _privateMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      mdef.visit(_cbiv);
      assertEquals("There should not be any errors.", 0, errors.size());

      
      //Check one that works but needs to be augmented with public
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO,
                                                    _packageMav,
                                                    new TypeParameter[0],
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                    new Word(JExprParser.NO_SOURCE_INFO, "noMavMethod"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0],
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      cmd1.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      assertEquals("_sd1 should contain 2 methods", 2, _sd1.getMethods().size());
      assertTrue("The second method should be default public", _sd1.getMethods().get(1).hasModifier("public"));

      
      
      // Test one that doesn't work.
      mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      mdef.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    public void testForAbstractMethodDef() {
      // Test one that works but needs to be augmented with public.
      MethodDef mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _cbiv._classData.setMav(_abstractMav);

      mdef.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      assertEquals("_sd1 should contain 1 methods", 1, _sd1.getMethods().size());
      assertTrue("The method should be default public", _sd1.getMethods().getFirst().hasModifier("public"));

      
      
      
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, " +
                   "and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor,
     * perhaps we could factor it out. */
    
//    public void testForOtherExpressionOnly() {
//      // Test that if the OtherExpression contains a Word, that the Word is resolved.
//      assertFalse("java.lang.System should not be in the symbolTable.", symbolTable.containsKey("java.lang.System"));
//      Expression ex = new Expression( JExprParser.NO_SOURCE_INFO,
//                                     new ExpressionPiece[] { new OtherExpression(JExprParser.NO_SOURCE_INFO, 
//                                                                                 new Word(JExprParser.NO_SOURCE_INFO,
//                                                                                                              "System"))});
//      ex.visit(_cbiv);
//      assertEquals("There should not be any errors.", 0, errors.size());
//      assertTrue("java.lang.System should be in the symbolTable.", symbolTable.containsKey("java.lang.System"));
//    }
    
    public void testForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = 
        new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO,
                                          new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                          new Word(JExprParser.NO_SOURCE_INFO, "i"),
                                          new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      
      ivd.visit(_cbiv);
      
      assertEquals("There should be no errors now", 0, errors.size());
//      assertEquals("Error message should be correct",
//                   "Cannot initialize a class's fields at the Intermediate level.  To set the value of a field, when" +
//                   "you instantiate the class, assign the desired value using the class's constructor",
//                   errors.get(0).getFirst());
    }


    public void testForInnerInterfaceDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerInterfaceDef cd1 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cd1}));
      
      SymbolData sd0 = new SymbolData(_cbiv._classData.getName() + "$Lisa", _packageMav, new TypeParameter[0], 
                                      new LinkedList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(_cbiv._classData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
                                      new LinkedList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      symbolTable.put(_cbiv._classData.getName() + "$Lisa", sd0);
      symbolTable.put(_cbiv._classData.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbiv);

      SymbolData sd = _cbiv._classData.getInnerClassOrInterface("Lisa");

      // NOTE: No longer allowing inner interfaces at the intermediate level
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Nested interfaces cannot be used at the Intermediate level", errors.get(0).getFirst());
    }
    
    public void testForConstructorDef() {
      //this is a ConstructorDef with no formal parameters and no throws
      ConstructorDef cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, 
                                             new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, 
                                             new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      //What if constructor name and SymbolData name don't match?  Should throw an error.
      _cbiv._classData = new SymbolData("NotRightName");
      cd.visit(_cbiv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "The constructor return type and class name must match", errors.getLast().getFirst());
      
      //If they are the same, it should work just fine.
      _cbiv._classData = new SymbolData("MyClass");
      
      MethodData constructor = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbiv._classData, 
                                              new VariableData[0], 
                                              new String[0], 
                                              _cbiv._classData,
                                              null);
      
      
      cd.visit(_cbiv);
      
      
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 1 method", 1, _cbiv._classData.getMethods().size());
      assertTrue("SymbolData's constructor should be correct", _cbiv._classData.getMethods().contains(constructor));
      
      //With a ConstructorDef with more throws and variables, should work okay.
      FormalParameter fp = 
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                new Word(JExprParser.NO_SOURCE_INFO, "i")), false);
      ReferenceType rt = new TypeVariable(JExprParser.NO_SOURCE_INFO, "MyMadeUpException");
      ConstructorDef cd2 = new ConstructorDef(JExprParser.NO_SOURCE_INFO, 
                                              new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, 
                                              new FormalParameter[] {fp}, new ReferenceType[] {rt}, 
                                              new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      VariableData vd = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, null);
      MethodData constructor2 = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbiv._classData, 
                                               new VariableData[] {vd}, 
                                               new String[] {"MyMadeUpException"}, 
                                              _cbiv._classData,
                                              null);

                                           
      constructor2.addVar(vd);
      cd2.visit(_cbiv);
      vd.setEnclosingData(_cbiv._classData.getMethods().getLast());
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 2 methods", 2, _cbiv._classData.getMethods().size());
      
      assertTrue("SymbolData should have new constructor", _cbiv._classData.getMethods().contains(constructor2));
      
                                              
      //If two variable names are duplicated, should throw an error.
      FormalParameter fp2 = 
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                                new Word(JExprParser.NO_SOURCE_INFO, "i")), false);
      
      ConstructorDef cd3 = new ConstructorDef(JExprParser.NO_SOURCE_INFO, 
                                              new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, 
                                              new FormalParameter[] {fp, fp2}, new ReferenceType[] {rt}, 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd3.visit(_cbiv);
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "You cannot have two method parameters with the same name", 
                   errors.getLast().getFirst());
    }    
  }
}