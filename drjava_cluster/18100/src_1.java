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
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

import junit.framework.TestCase;

/**
 * Do the TypeChecking appropriate to the context of a class body.  Common to all Language Levels.
 */
public class ClassBodyTypeChecker extends Bob {
  
  /**The SymbolData corresponding to this class.*/
  private SymbolData _symbolData;
  
  /**True if we encounter a ConstructorDef while visiting the body.*/
  protected boolean hasConstructor;
  
  /*
   * Constructor for ClassBodyTypeChecker.
   * Adds all the variables in the symbol data to the list of what can be seen from this context,
   * since class fields can always be seen.
   * 
   * @param sd  The SymbolData of the class we are type checking.
   * @param file  The File corresponding to the source file we are checking.
   * @param packageName  The package of the source file.
   * @param importedFiles  A list of the names of the classes that are specifically imported in the source file
   * @param importedPackages  A list of the names of the packages that are imported in the source file.
   * @param vars  A list of the variable datas that can be seen and have been given a value before this context
   * @param thrown  The exceptions that are thrown
   */
  public ClassBodyTypeChecker(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(sd, file, packageName, importedFiles, importedPackages, vars, thrown);
    _symbolData = sd;
    hasConstructor = false;
    
    _vars.addAll(sd.getVars());
    
    LinkedList<VariableData> superVars = sd.getAllSuperVars();
    for (int i = 0; i<superVars.size(); i++) {
      VariableData tempVD = superVars.get(i);
      if (tempVD.isFinal()) {
        if (tempVD.gotValue()) {
          thingsThatHaveBeenAssigned.addLast(tempVD);
        }
      }
    }
    _vars.addAll(superVars);

  }
  
  /*@return the symbol data corresponding to the class we are visiting*/
  protected Data _getData() { return _symbolData; }

  
  /**
   * We need to do this so that expressions (which should only occur in variable initializers and
   * initializer blocks) can know which fields have already been declared
   */
  public TypeData forUninitializedVariableDeclaratorOnly(UninitializedVariableDeclarator that, 
                                                            TypeData type_result, 
                                                            TypeData name_result) {
    Word name = that.getName();
    String text = that.getName().getText();
    VariableData vd = getFieldOrVariable(text, _symbolData, _symbolData, name);
    if (vd == null) {
      throw new RuntimeException("The field " + text + " was not found in " + _symbolData.getName() + ".");
    }
    _vars.addLast(vd);
    return null;
  }


  /**Make sure the method is not missing a return type.  
   * If expected and actual are both not null, then their relationship (i.e.
   * is actual a subclass of expected) is checked when the value return statement that returns
   * actual is processed.
   */
  private void _checkReturnType(SymbolData expected, SymbolData actual, ConcreteMethodDef that) {
    // If the return type is void, the BodyTypeChecker will make sure there are no return statements with expressions.
    if (expected == SymbolData.VOID_TYPE) {
      if (actual == null || actual == SymbolData.VOID_TYPE) {
        // correct, do nothing
      }
    }
    else {
      if (actual == null) {
        _addError("This method is missing a return statement.", that);
      }
    }
  }
  
  /**
   * Finds the corresponding MethodData for this constructor first.  Then visits the body, making sure that
   * all final fields are assigned a value and that no final fields are reassigned.
   */
  public TypeData forConstructorDef(ConstructorDef that) {
    hasConstructor = true;
    final TypeData mav_result = that.getMav().visit(this);
    final TypeData[] parameters_result = makeArrayOfRetType(that.getParameters().length);
    final TypeData[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = getSymbolData(that.getThrows()[i].getName(), _symbolData, that);     //that.getThrows()[i].visit(this);
    }

    // We need to match the name and params.
    // First find the correct MethodData.
    MethodData md = null;
    FormalParameter[] fParams = that.getParameters();
    String[] paramTypes = new String[fParams.length];
    for (int i = 0; i < fParams.length; i++) {
      paramTypes[i] = fParams[i].getDeclarator().getType().getName();
    }
    LinkedList<MethodData> mds = _symbolData.getMethods();
    Iterator<MethodData> iter = mds.iterator();
    while (iter.hasNext()) {
      boolean match = true;
      MethodData tempMd = iter.next();
      if (tempMd.getName().equals(LanguageLevelVisitor.getUnqualifiedClassName(_symbolData.getName()))) {
        // Check the params.
        VariableData[] vds = tempMd.getParams();
        if (paramTypes.length == vds.length) {
          for (int i = 0; i < paramTypes.length; i++) {
            // The parameters should be in order.  Must also check the unqualified form of the VariableData's type.
            if(!vds[i].getType().getName().equals(paramTypes[i]) &&
               !LanguageLevelVisitor.getUnqualifiedClassName(vds[i].getType().getName()).equals(paramTypes[i])) {
              match = false;
              break;
            }
          }
          if (match) {
            md = tempMd;            
            break;
          }
        }
      }
    }
    if (md == null) {
      throw new RuntimeException("The constructor " + LanguageLevelVisitor.getUnqualifiedClassName(_symbolData.getName()) + " was not in the class " + _symbolData.getName() + ".");
    }

    LinkedList<VariableData> ll = new LinkedList<VariableData>();// = cloneVariableDataList(_vars);
    VariableData[] vds = md.getParams();
    for (int i = 0; i<vds.length; i++) {
      ll.addLast(vds[i]);
    }
    ll.addAll(cloneVariableDataList(_vars));

    ConstructorBodyTypeChecker btc = new ConstructorBodyTypeChecker(md, _file, _package, _importedFiles, _importedPackages, ll, new LinkedList<Pair<SymbolData, JExpression>>());
    final TypeData body_result = that.getStatements().visit(btc);
    
    //make sure the constructor assigns a value to any uninitialized fields (i.e. final fields)
    LinkedList<VariableData> sdVars = _symbolData.getVars();
    for (int i = 0; i<sdVars.size(); i++) { //Since non-final fields are automatically given a value, this in effect just checks the final fields.
      if (!sdVars.get(i).hasValue()) {
        _addError("The final field " + sdVars.get(i).getName() + " has not been initialized.  Make sure you give it a value in this constructor", that);
        return null;
      }
    }
    
    _symbolData.decrementConstructorCount();
    
   
    if (_symbolData.getConstructorCount() > 0) {
      unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    }
    
    return forConstructorDefOnly(that, mav_result, parameters_result, throws_result, body_result);
  }
  

  /*Basically, a no-op*/
  public TypeData forConstructorDefOnly(ConstructorDef that, TypeData mav_result, TypeData[] parameters_result, TypeData[] throws_result, TypeData body_result) {
    return forJExpressionOnly(that);
  }
  

  /*
   * Visit all of the fields of the ConcreteMethodDef, and resolve everything.  Then, find
   * the corresponding MethodData in the SymbolData's list of methods.  It must match both in name and parameter
   * types.  Keep track of what has been assigned before we visit the method body, and then visit the
   * method body.  Then, make sure the return type of the method is okay, and unassign any variables
   * for which the assignment should not be visible outside the scope of the method.
   */
  public TypeData forConcreteMethodDef(ConcreteMethodDef that) {
    final TypeData mav_result = that.getMav().visit(this);
    final TypeData[] typeParams_result = makeArrayOfRetType(that.getTypeParams().length);
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = that.getTypeParams()[i].visit(this);
    }
    final SymbolData result_result = getSymbolData(that.getResult().getName(), _symbolData, that);
    final TypeData name_result = that.getName().visit(this);
    final TypeData[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = getSymbolData(that.getThrows()[i].getName(), _symbolData, that.getThrows()[i]);
    }
    // We need to match the name and params.
    // First find the correct MethodData.
    MethodData md = null;
    FormalParameter[] fParams = that.getParams();
    String[] paramTypes = new String[fParams.length];
    for (int i = 0; i < fParams.length; i++) {
      paramTypes[i] = fParams[i].getDeclarator().getType().getName();
    }
    LinkedList<MethodData> mds = _symbolData.getMethods();
    Iterator<MethodData> iter = mds.iterator();
    while (iter.hasNext()) {
      boolean match = false;
      MethodData tempMd = iter.next();
      if (tempMd.getName().equals(that.getName().getText())) {
        match = true;

        // Check the params.
        VariableData[] vds = tempMd.getParams();
        if (paramTypes.length == vds.length) {
          for (int i = 0; i < paramTypes.length; i++) {
            // The parameters should be in order.  Must also check the unqualified form of the VariableData's type.
            if(!vds[i].getType().getName().equals(paramTypes[i]) &&
               !LanguageLevelVisitor.getUnqualifiedClassName(vds[i].getType().getName()).equals(paramTypes[i])) {
              match = false;
              break;
            }
          }
          if (match) {
            md = tempMd;            
            break;
          }
        }
      }
    }
    if (md == null) {
      throw new RuntimeException("Internal Program Error: The method " + that.getName().getText() + " was not in the class " + _symbolData.getName() + ".  Please report this bug.");
    }
    
    LinkedList<VariableData> ll = new LinkedList<VariableData>();
    VariableData[] vds = md.getParams();
    for (int i = 0; i<vds.length; i++) {
      ll.addLast(vds[i]);
    }
    ll.addAll(cloneVariableDataList(_vars));
    
    LinkedList<VariableData> thingsWeAssigned = new LinkedList<VariableData>();
    for (int i = 0; i<_symbolData.getVars().size(); i++) {
      VariableData tempVd = _symbolData.getVars().get(i);
      if (tempVd.gotValue()) { //then this variable did not have a value previously.
        thingsWeAssigned.addLast(tempVd);
      }
    }
    
    BodyTypeChecker btc = new BodyTypeChecker(md, _file, _package, _importedFiles, _importedPackages, ll, new LinkedList<Pair<SymbolData, JExpression>>());
    
    TypeData body_result = that.getBody().visit(btc); // We assume that this will return an InstanceData -- the return type of the body
    
    // This checks to see that the method returns the correct type.  It throws its own errors.
    if (body_result != null) {body_result = body_result.getSymbolData();}
    _checkReturnType(md.getReturnType(), (SymbolData) body_result, that);
    if (md.getReturnType() != null) {
      // Ensure that this method doesn't override another method with a different return type.
      SymbolData.checkDifferentReturnTypes(md, _symbolData, LanguageLevelConverter.OPT.javaVersion());
    }
    
    // This is not used because this call eventually invokes the forUninitializedVariableDeclarator method above.
    final TypeData[] params_result = makeArrayOfRetType(that.getParams().length);

    for (int i = 0; i<thingsWeAssigned.size(); i++) {
      thingsWeAssigned.get(i).lostValue();
    }
    
    return result_result;

  }

  
  /*
   * Make sure that this method does not override another method with a different return type,
   * since this is not allowed in java.
   */
  public TypeData forAbstractMethodDef(AbstractMethodDef that) {
    final TypeData mav_result = that.getMav().visit(this);
    final TypeData[] typeParams_result = makeArrayOfRetType(that.getTypeParams().length);
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = that.getTypeParams()[i].visit(this);
    }
    final SymbolData result_result = getSymbolData(that.getResult().getName(), _symbolData, that);
    final TypeData name_result = that.getName().visit(this);

    // This is not used because this call eventually invokes the forUninitializedVariableDeclarator method above.
    final TypeData[] params_result = makeArrayOfRetType(that.getParams().length);
    for (int i = 0; i<params_result.length; i++) {
      params_result[i] = getSymbolData(that.getParams()[i].getDeclarator().getType().getName(), _symbolData, that.getParams()[i]);
    }
    final TypeData[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = getSymbolData(that.getThrows()[i].getName(), _symbolData, that.getThrows()[i]);
    }
    // Ensure that this method doesn't override another method with a different return type.
    MethodData md = _symbolData.getMethod(that.getName().getText(), params_result);
    if (md == null) {
      throw new RuntimeException("Internal Program Error: Could not find the method " + that.getName().getText() + " in class " + _symbolData.getName() +".  Please report this bug.");
    }
    SymbolData.checkDifferentReturnTypes(md, _symbolData, LanguageLevelConverter.OPT.javaVersion());

    return result_result;
  }
  
  /*Try to resolve the type, and then make sure it can be seen from where we are. */
  public TypeData forTypeOnly(Type that) {
    Data sd = getSymbolData(that.getName(), _symbolData, that);
    if (sd != null) {sd = sd.getOuterData();}
    while (sd != null && !LanguageLevelVisitor.isJavaLibraryClass(sd.getSymbolData().getName())) {
      if (!checkAccessibility(that, sd.getMav(), sd.getName(), sd.getSymbolData(), _symbolData, "class or interface")) {
        return null;
      }
      sd = sd.getOuterData();
    }
    return forJExpressionOnly(that);
  }
  
   /**
    * Test the methods that are defined above.
    */
  public static class ClassBodyTypeCheckerTest extends TestCase {
    
    private ClassBodyTypeChecker _cbbtc;
    
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
    
    
    public ClassBodyTypeCheckerTest() {
      this("");
    }
    public ClassBodyTypeCheckerTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable = symbolTable = new Symboltable();
      _cbbtc = new ClassBodyTypeChecker(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.<File>empty());
      _cbbtc._importedPackages.addFirst("java.lang");
    }
    
    public void testForUninitializedVariableDeclaratorOnly() {
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, false, _cbbtc._data);
      _sd1.addVar(vd1);
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                                new Word(JExprParser.NO_SOURCE_INFO, "Mojo"));
//      uvd.visit(_cbbtc);
      _cbbtc.forUninitializedVariableDeclaratorOnly(uvd, SymbolData.INT_TYPE, null);
      assertTrue("_vars should contain Mojo.", _cbbtc._vars.contains(vd1));      
    }
    
    public void testForInitializedVariableDeclaratorOnly() {
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, false, _cbbtc._data);
      _sd1.addVar(vd1);
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                            new Word(JExprParser.NO_SOURCE_INFO, "Mojo"), 
                                                                            new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      ivd.visit(_cbbtc);
      assertEquals("There should be no errors.", 0, errors.size());
      assertTrue("_vars should contain Mojo.", _cbbtc._vars.contains(vd1));
      ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                              new Word(JExprParser.NO_SOURCE_INFO, "Santa's Little Helper"), 
                                              new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      try {
        ivd.visit(_cbbtc);
        fail("Should have thrown a RuntimeException because there's no field named Santa's Little Helper.");
      }
      catch (RuntimeException re) {
        assertEquals("The error message should be correct.", "Internal Program Error: The field or variable Santa's Little Helper was not found in this block.  Please report this bug.", re.getMessage());
      }
    }
    
    public void test_checkReturnType() {
      /* Check tests that should work correctly first. */
      _cbbtc._checkReturnType(SymbolData.VOID_TYPE, null, null);
      _cbbtc._checkReturnType(SymbolData.VOID_TYPE, SymbolData.VOID_TYPE, null);
      _cbbtc._checkReturnType(SymbolData.INT_TYPE, SymbolData.INT_TYPE, null);
      _cbbtc._checkReturnType(SymbolData.DOUBLE_TYPE, SymbolData.INT_TYPE, null);
      
      assertEquals("There should be no errors.", 0, errors.size());
      
      /* Check tests that should each throw an error. */
      _cbbtc._checkReturnType(SymbolData.INT_TYPE, null, null);
      assertEquals("There should now be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "This method is missing a return statement.", errors.get(0).getFirst());
      
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
        new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5) )}));
      MethodData md = new MethodData("methodName", 
                                     _packageMav, 
                                     new TypeParameter[0], 
                                     SymbolData.INT_TYPE, 
                                     new VariableData[] { new VariableData(SymbolData.DOUBLE_TYPE), new VariableData(SymbolData.BOOLEAN_TYPE) },
                                     new String[0],
                                     _sd1,
                                     null); // no SourceInfo
      _sd1.addMethod(md);
      cmd.visit(_cbbtc);
      assertEquals("There should be no errors.", 0, errors.size());
      
      cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "Selma"),
                                                    fps,
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, 
                                                                                   new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))}));
      
      try {
        cmd.visit(_cbbtc);
        fail("Should have thrown a RuntimeException because there's no method named Selma.");
      }
      catch (RuntimeException re) {
        assertEquals("The error message should be correct.", "Internal Program Error: The method Selma was not in the class i.like.monkey.  Please report this bug.", re.getMessage());
      }
      
      
      //Check that an uninitialized variable is caught:
      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));

      Statement s = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      ConcreteMethodDef cmd0 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], intt,
                                                    new Word(JExprParser.NO_SOURCE_INFO, "invalidMethod"), new FormalParameter[0],
                                                    new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), s}));
      VariableData vd = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      MethodData md0 = new MethodData("invalidMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[0], new String[0], _sd1, cmd0);
      _sd1.addMethod(md0);
      vd.setEnclosingData(md0);
      md0.addVar(vd);
      
      _cbbtc = new ClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages, new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      cmd0.visit(_cbbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot use i because it may not have been given a value", errors.get(0).getFirst());


      
      //Check that the lexical scope of an if then statement is handled correctly.
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO,
                                             new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
                                             new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      IfThenStatement ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift,
        new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")))});
      
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);

      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      _sd1.addMethod(md1);
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);
      md1.addVar(vd1);
      md1.addVar(vd2);
      
      _cbbtc = new ClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages, new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      cmd1.visit(_cbbtc);
      
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "You cannot use i because it may not have been given a value", errors.get(1).getFirst());
      
      //Check that a final variable cannot be reassigned to
      s = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      VariableDeclaration i = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd});
      ExpressionStatement se = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 2)));
      ExpressionStatement se2 = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)));
      
      BracedBody b = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {i, se, se2, s});
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], intt,
                                                    new Word(JExprParser.NO_SOURCE_INFO, "doubleAssignmentMethod"), new FormalParameter[0],
                                                    new ReferenceType[0], b);
      
      VariableData vdi = new VariableData("i", _finalMav, SymbolData.INT_TYPE, false, null);
      MethodData md2 = new MethodData("doubleAssignmentMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[0], new String[0], _sd1, cmd2);
      _sd1.addMethod(md2);
      vdi.setEnclosingData(md2);

      md2.addVar(vdi);

      _cbbtc = new ClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages, new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      cmd2.visit(_cbbtc);
      assertEquals("There should be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "You cannot assign a value to i because it is immutable and has already been given a value", errors.get(2).getFirst());
 
       
      //test that if a variable is assigned in a branch of the if, and then returned, it is okay.
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
       new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      Statement returnStatement = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement, returnStatement}));
      ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), 
        ift, 
        new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, 
                                 new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))});
      
      ConcreteMethodDef cmd4 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod3"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod3", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd4);
      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      
      _sd1.addMethod(md1);
      _cbbtc = new ClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages, new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      

      md1.addBlock(new BlockData(md1));
      cmd4.visit(_cbbtc);
      assertEquals("There should be 3 errors", 3, errors.size());
    }
    
    public void testCheckDifferentReturnTypes() {
      SymbolData superSd = new SymbolData("aiya");
      _sd1.setSuperClass(superSd);
      MethodData md3 = new MethodData("methodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.CHAR_TYPE,
                                      new VariableData[0],
                                      new String[0],
                                      null,
                                      null);
      superSd.addMethod(md3);
      MethodData md4 = new MethodData("methodName",
                                     _publicMav,
                                     new TypeParameter[0],
                                     SymbolData.INT_TYPE,
                                     new VariableData[0],
                                     new String[0],
                                     superSd,
                                     null);
      MethodDef mDef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "methodName"), new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 76))}));
      _sd1.addMethod(md4);
      _cbbtc._symbolData = _sd1;
      mDef.visit(_cbbtc);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "methodName() in " + _sd1.getName() + " cannot override methodName() in aiya; attempting to use different return types",
                   errors.get(0).getFirst());
      mDef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                   new Word(JExprParser.NO_SOURCE_INFO, "methodName"), new FormalParameter[0], new ReferenceType[0]);
      
      mDef.visit(_cbbtc);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct", "methodName() in " + _sd1.getName() + " cannot override methodName() in aiya; attempting to use different return types",
                   errors.get(1).getFirst());
    }
    
    public void testForTypeOnly() {
      Type t = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double");
      t.visit(_cbbtc);
      assertEquals("There should be no errors", 0, errors.size());
      
      SymbolData sd = new SymbolData("Adam");
      sd.setIsContinuation(false);
      symbolTable.put("Adam", sd);
      sd.setMav(_publicMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam", new Type[0]);
      t.visit(_cbbtc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      SymbolData innerSd = new SymbolData("Adam$Wulf");
      innerSd.setIsContinuation(false);
      sd.addInnerClass(innerSd);
      innerSd.setOuterData(sd);
      innerSd.setMav(_publicMav);
      _cbbtc.symbolTable.put("USaigehgihdsgslghdlighs", innerSd);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_cbbtc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      innerSd.setMav(_privateMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_cbbtc);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The class or interface Adam.Wulf is private and cannot be accessed from " + _cbbtc._symbolData.getName(),
                   errors.get(0).getFirst());
      
      sd.setMav(_privateMav);
      innerSd.setMav(_publicMav);
      t = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Adam.Wulf", new Type[0]);
      t.visit(_cbbtc);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "The class or interface Adam is private and cannot be accessed from " + _cbbtc._symbolData.getName(),
                   errors.get(1).getFirst());
    }
    
    public void testForConstructorDef() {
      VariableDeclaration vd = new VariableDeclaration(JExprParser.NO_SOURCE_INFO, _finalMav, new VariableDeclarator[] {new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i"))});
      ExpressionStatement se = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1)));      
      BracedBody cbb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {se});
      ConstructorDef cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "Jimes"), _publicMav, new FormalParameter[0], new ReferenceType[0], cbb);
      BracedBody b = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {vd, cd});
      ClassDef classDef = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Jimes"), new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], b);

      SymbolData sd = new SymbolData("Jimes");
      VariableData vData = new VariableData("i", _finalMav, SymbolData.INT_TYPE, false, sd);
      _cbbtc._file = new File("Jimes.dj0");
      sd.setMav(_publicMav);
      sd.setIsContinuation(false);
      sd.addVar(vData);
      SymbolData sd2 = new SymbolData("java.lang.Object");
      sd2.setIsContinuation(false);
      sd2.setMav(_publicMav);
      sd2.setPackage("java.lang");
      sd.setSuperClass(sd2);
      symbolTable.put("Jimes", sd);
      symbolTable.put("java.lang.Object", sd2);
      MethodData md = new MethodData("Jimes", _publicMav, new TypeParameter[0], sd, new VariableData[0], new String[0], sd, cd);
      MethodData objMd = new MethodData("Object", _publicMav, new TypeParameter[0], sd2, new VariableData[0], new String[0], sd2, cd);
      sd.addMethod(md);
      
      
      //assumes an explicit super call with no arguments
      classDef.visit(_cbbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "You must invoke one of java.lang.Object's constructors here.  You can either explicitly invoke one of its exisitng constructors or add a constructor with signature: Object().", errors.getLast().getFirst());

      // test that a constructor can set the value of a final field
      sd2.addMethod(objMd); //give super class constructor
      vData.lostValue();

      classDef.visit(_cbbtc);
      assertEquals("There should still be one error", 1, errors.size());

      
      // test that if the constructor does not assign a value to the final field, then an error is thrown
      vData.lostValue();
      cbb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {});
      cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "Jimes"), _publicMav, new FormalParameter[0], new ReferenceType[0], cbb);
      b = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {vd, cd});
      classDef = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Jimes"), new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], b);
 
      classDef.visit(_cbbtc);
      assertEquals("There should be 2 errors now", 2, errors.size());
      assertEquals("The error message should be correct", "The final field i has not been initialized.  Make sure you give it a value in this constructor", errors.getLast().getFirst());
      
      //test the case of a constructor that makes a call to another constructor
      vData = new VariableData("j", _finalMav, SymbolData.INT_TYPE, false, sd);
      sd.setVars(new LinkedList<VariableData>());
      sd.addVar(vData);

      LinkedList<VariableData> vs = new LinkedList<VariableData>();
      vs.addLast(vData);
      _cbbtc = new ClassBodyTypeChecker(sd, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), vs, new LinkedList<Pair<SymbolData, JExpression>>());
      ExpressionStatement assign = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 45)));
      b = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleThisConstructorInvocation(JExprParser.NO_SOURCE_INFO, new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]))), assign});
      cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "name"), _publicMav, new FormalParameter[0], new ReferenceType[0], b);
      cd.visit(_cbbtc);
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct","You cannot assign a value to j because it is immutable and has already been given a value" , errors.getLast().getFirst());


    }
    
  }
}