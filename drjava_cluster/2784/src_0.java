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

/*TypeChecks the context of a body, such as a method body.  Common to all Language Levels.*/
public class BodyTypeChecker extends Bob {
  /**
   * The MethodData of this method.
   */
  protected BodyData _bodyData;
 
  /* Constructor for BodyTypeChecker.  Calls the super constructor for everything except bodyData, which we store a copy of
   * in this visitor in order to have the proper type at compile time.  (Bob stores it as a Data).
   * @param bodyData  The enclosing BodyData for the context we are type checking.
   * @param file  The File corresponding to the source file.
   * @param packageName  The package name from the source file.
   * @param importedFiles  The names of the files that are specifically imported (through a class import statement) in the source file.
   * @param importedPackages  The names of all the packages that are imported through a package import statement in the source file.
   * @param vars  The list of VariableData that have already been defined (used so we can make sure we don't use a variable before it has been defined).
   * @param thrown  The list of exceptions thrown in this body
   */
  public BodyTypeChecker(BodyData bodyData, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(bodyData, file, packageName, importedFiles, importedPackages, vars, thrown);
    _bodyData = bodyData;
  }
  
   /*@return the bodyData (enclosing data) for this context.*/
  protected Data _getData() {
    return _bodyData;
  }
  
  /*@return the instance data of the class/interface enclosing this body data*/
  public TypeData forSimpleThisReferenceOnly(SimpleThisReference that) {
    return _bodyData.getSymbolData().getInstanceData();
  }

  /*@return the instance data of the super class of the class enclosing this body data*/
  public TypeData forSimpleSuperReferenceOnly(SimpleSuperReference that) {
    return _bodyData.getSymbolData().getSuperClass().getInstanceData();
  }
  
  /**
   * Create a new instance of this class for visiting inner bodies.
   */
  protected BodyTypeChecker createANewInstanceOfMe(BodyData bodyData, File file, String pakage, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    return new BodyTypeChecker(bodyData, file, pakage, importedFiles, importedPackages, vars, thrown);
  }

  

  
  /* There is currently no way to differentiate between a block statement and
   * an instance initializer in a braced body given the general nature of a 
   * braced body.  Whenever an instance initialization is visited in a method
   * body, we must assume that it is a block statement.
   */
  public TypeData forInstanceInitializer(InstanceInitializer that) {
    return forBlock(that.getCode());
  }
  
  /**
   * We need to do this so that expressions (which should only occur in variable initializers and
   * initializer blocks) can know which fields have already been declared.  Add all the variable
   * datas that are declared in this declarator to the list of variables that are visibile from where we are.
   */
  public TypeData forUninitializedVariableDeclaratorOnly(UninitializedVariableDeclarator that, 
                                                            TypeData type_result, 
                                                            TypeData name_result) {
      _vars.addLast(_bodyData.getVar(that.getName().getText()));
    return null;
  }


  /** Look at the result of each item in the body.  If one is not null and does not correspond to an Expression
   * Statement, then that means that that statement returns a value.  Check to make sure that there are no
   * statements following it.  If there are, then those statements are unreachable so give an error.
   * @param that  The Body we were type checking
   * @param items_result  Array of results for each item in the body that was visited.
   */
  public TypeData forBodyOnly(Body that, TypeData[] items_result) {
    for (int i = 0; i < items_result.length; i++) {
      if (items_result[i] != null && !(that.getStatements()[i] instanceof ExpressionStatement)) {

        // Found a statement that returns a value.
        if (i < items_result.length - 1) {
        
          // there must be unreachable statements
          _addError("Unreachable statement.", (JExpression)that.getStatements()[i+1]);
        }
       // either way, return the result to keep on type-checking.
        return items_result[i];
      }
    }
    return null;
  }
  
  /*Delegate to forBodyOnly*/
  public TypeData forBracedBodyOnly(BracedBody that, TypeData[] items_result) {
    return forBodyOnly(that, items_result);
  }
  
  /*Delegate to forBodyOnly*/
  public TypeData forUnbracedBodyOnly(UnbracedBody that, TypeData[] items_result) {
    return forBodyOnly(that, items_result);
  }

  
  /*Make sure the enclosing method data is declared to return void.  If it is not, give an error.
   *@return the type the method is declared to return.
   */
  public TypeData forVoidReturnStatementOnly(VoidReturnStatement that) {
    MethodData md = _bodyData.getMethodData();
    if (md.getReturnType() != SymbolData.VOID_TYPE) {
      _addError("Cannot return void when the method's expected return type is not void.", that);

      // Return the correct type to allow type-checking to continue.
      return md.getReturnType().getInstanceData();
    }
    return SymbolData.VOID_TYPE.getInstanceData();
  }

  /*
   * Visit the value being returned to determine its type.  Do the necessary bookkeeping and
   * then delegate to forValueReturnStatementOnly.
   */
  public TypeData forValueReturnStatement(ValueReturnStatement that) {
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    TypeData value_result = that.getValue().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    return forValueReturnStatementOnly(that, value_result);
  }

  /*
   * Make sure that the enclosing method is declared to throw the same type as the return statement
   * is trying to return.  Also make sure that what is being returned is an instance of the type,
   * not the type itself.
   */
  public TypeData forValueReturnStatementOnly(ValueReturnStatement that, TypeData value_result) {
    MethodData md = _bodyData.getMethodData();
    SymbolData expected = md.getReturnType();
    
    if (expected == null) {
      // There was an error processing the method's return type; return the result type
      return value_result;
    }
    
    if (value_result == null || !assertFound(value_result, that)) { 
      // There was an error parsing the return type, return the expected type.
      return expected.getInstanceData(); 
    }
    
    if (value_result != null && !value_result.isInstanceType()) {
     _addError("You cannot return a class or interface name.  Perhaps you meant to say " + value_result.getName() + ".class or to create an instance", that);
     return value_result.getInstanceData();
    }
    
    if (expected == SymbolData.VOID_TYPE) {
      _addError("Cannot return a value when the method's expected return type is void.", that);
      // Return the correc type to allow type-checking to continue.
      return SymbolData.VOID_TYPE.getInstanceData();
    }
    else if (!_isAssignableFrom(expected, value_result.getSymbolData())) {
      _addError("This method expected to return type: \"" + 
                expected.getName() + "\" but here returned type: \"" + value_result.getName() + "\".", that);
    }
    return value_result;
  }
  
  
  /*
   * First, visit the condition expression of the for statement with a special visitor that
   * makes sure no assignment is done.
   * Then, visit the condition expression with the ExpressionTypeChecker which will do all the
   * normal expression stuff.
   * Then, visit the update and and code (block) of the for statement with this visitor.
   * Be very careful about maintaing the various scopes here.
   */
  public TypeData forForStatement(ForStatement that) {
    Boolean expOk = Boolean.TRUE;
    if (that.getCondition() instanceof Expression) {
      Expression exp = (Expression) that.getCondition();
      // Assignment cannot be used in this expression
      expOk = exp.visit(new NoAssignmentAllowedInExpression("the conditional expression of a for-statement"));
    }

    BodyTypeChecker btc = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData init_result = that.getInit().visit(btc);
    
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, btc._vars, _thrown);
    final TypeData condition_result = that.getCondition().visit(etc);
    btc.thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    final TypeData update_result = that.getUpdate().visit(btc);
    final TypeData code_result = that.getCode().visit(btc);
    
    //Now, change any VariableDatas that were given a value in the ForStatement back to having been unassigned since its code is not
    //necessarily executed.
    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    if (expOk.booleanValue()) {return forForStatementOnly(that, init_result, condition_result, update_result, code_result);}
    else {return null;}
  }
  
  
  /* Make sure that the conditional expression has the right type. */
  public TypeData forForStatementOnly(ForStatement that, TypeData init_result, TypeData condition_result, TypeData update_result, TypeData code_result) {
    if (condition_result != null && assertFound(condition_result, that)) { 
      if (!condition_result.isInstanceType()) {
        _addError("This for-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", that);
      }
      else if (!condition_result.getSymbolData().isBooleanType(LanguageLevelConverter.OPT.javaVersion())) {
        _addError("This for-statement's conditional expression must be a boolean value. Instead, its type is " + condition_result.getName(), that);
      }
    }
    return null;
  }
  

  /*
   * First, visit the condition expression of the if statement with a special visitor that
   * makes sure no assignment is done.
   * Then, visit the condition expression with the ExpressionTypeChecker which will do all the
   * normal expression stuff.
   * Then, visit the body of the if statement with this visitor.
   * Be very careful about maintaing the various scopes here.
   */
  public TypeData forIfThenStatement(IfThenStatement that) {
    Boolean expOk = Boolean.TRUE;
    if (that.getTestExpression() instanceof Expression) {
      Expression exp = (Expression) that.getTestExpression();
      // Assignment cannot be used in this expression
      expOk = exp.visit(new NoAssignmentAllowedInExpression("the conditional expression of an if-then statement"));
    }

    //Update what has been assigned here with results from test expression, because any variables it sees or assigns will be visible
    //in the rest of the body.
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData testExpression_result = that.getTestExpression().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);

    //Use a new visitor for the body of the then statement--it is a different lexical scope, so we want to track the
    //variables seperately.
    BodyTypeChecker btc = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData thenStatement_result = that.getThenStatement().visit(btc);
    
    //Now, change any VariableDatas that were given a value in the ThenStatement back to having been unassigned
    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    
    if (expOk.booleanValue()) {return forIfThenStatementOnly(that, testExpression_result, thenStatement_result);}
    return null;
  }
  
  /* Make sure that the conditional expression has the right type. */
  public TypeData forIfThenStatementOnly(IfThenStatement that, TypeData testExpression_result, TypeData thenStatement_result) {
    if (testExpression_result != null && assertFound(testExpression_result, that.getTestExpression())) {
      if (!testExpression_result.isInstanceType()) {
        _addError("This if-then-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", that);
      }
      else if (!testExpression_result.getSymbolData().isBooleanType(LanguageLevelConverter.OPT.javaVersion())) {
        _addError("This if-then-statement's conditional expression must be a boolean value. Instead, its type is " + testExpression_result.getName(), that.getTestExpression());
      }
    }
    return null;
  }
  

  /*
   * First, visit the condition expression of the if-then-else statement with a special visitor that
   * makes sure no assignment is done.
   * Then, visit the condition expression with the ExpressionTypeChecker which will do all the
   * normal expression stuff.
   * Then, visit the body of the if statement and the else with this visitor.
   * Be very careful about maintaing the various scopes here.
   */
  public TypeData forIfThenElseStatement(IfThenElseStatement that) {
    Boolean expOk = Boolean.TRUE;
    if (that.getTestExpression() instanceof Expression) {
      Expression exp = (Expression) that.getTestExpression();
      // Assignment cannot be used in this expression
      expOk = exp.visit(new NoAssignmentAllowedInExpression("the conditional expression of an if-then-else statement"));
    }
    
    //Update list of what has been assigned with one from test expression, because any variables it sees or assigns will be visible
    //in the rest of the body.
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData testExpression_result = that.getTestExpression().visit(etc);
    thingsThatHaveBeenAssigned = etc.thingsThatHaveBeenAssigned;
    
    //Use a new visitor for the body of the then statement--it is a different lexical scope, so we want to track the
    //variables seperately.
    BodyTypeChecker btcThen = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData thenStatement_result = that.getThenStatement().visit(btcThen);
    //Now, change any VariableDatas that were given a value in the ThenStatement back to having been unassigned
    unassignVariableDatas(btcThen.thingsThatHaveBeenAssigned);


    //Use a new visitor for the body of the else statement--it is a different lexical scope, so we want to track the
    //variables seperately.
    BodyTypeChecker btcElse = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData elseStatement_result = that.getElseStatement().visit(btcElse);
    //Now, change any VariableDatas that were given a value in the ElseStatement back to having been unassigned
    unassignVariableDatas(btcElse.thingsThatHaveBeenAssigned);

    //Now compare the two lists of VariableDatas, and reassign those that were assigned in both branches.
    reassignVariableDatas(btcThen.thingsThatHaveBeenAssigned, btcElse.thingsThatHaveBeenAssigned);
    
    if (expOk.booleanValue()) {return forIfThenElseStatementOnly(that, testExpression_result, thenStatement_result, elseStatement_result);}
    return null;
  }
    
  /* Make sure that the conditional expression has the right type, and if both branches of the
   * if/else return, return a value the common super type of the two return types.
   *   We assume that thenStatement_result and elseStatement_result are InstanceDatas
   */
  public TypeData forIfThenElseStatementOnly(IfThenElseStatement that, TypeData testExpression_result, TypeData thenStatement_result, TypeData elseStatement_result) {
    if (testExpression_result != null && assertFound(testExpression_result, that.getTestExpression())) {
      if (!testExpression_result.isInstanceType()) {
        _addError("This if-then-else statement's conditional expression must be a boolean value. Instead, it is a class or interface name", that);
      }
      else if (!testExpression_result.getSymbolData().isBooleanType(LanguageLevelConverter.OPT.javaVersion())) {
        _addError("This if-then-else statement's conditional expression must be a boolean value. Instead, its type is " + testExpression_result.getName(), that.getTestExpression());
      }
    }

    if (testExpression_result == null ||
        thenStatement_result == null || 
        elseStatement_result == null) { return null; }

     
    //     We don't throw an error here because if the then and else branches return incompatible types,
    //     there must have already been an error thrown in forValueReturnStatementOnly
    //     that indicates that one of the return statements is returning the wrong type.
    SymbolData result = getCommonSuperType(thenStatement_result.getSymbolData(), elseStatement_result.getSymbolData());
    if (result==null) {return null;}
    return result.getInstanceData();
  }
  

  /*
   * First, visit the condition expression of the while statement with a special visitor that
   * makes sure no assignment is done.
   * Then, visit the condition expression with the ExpressionTypeChecker which will do all the
   * normal expression stuff.
   * Then, visit the body with this visitor.
   * Be very careful about maintaing the various scopes here.
   */
  public TypeData forWhileStatement(WhileStatement that) {
    Boolean expOk = Boolean.TRUE;
    if (that.getCondition() instanceof Expression) {
      Expression exp = (Expression) that.getCondition();
      // Assignment cannot be used in this expression
      expOk = exp.visit(new NoAssignmentAllowedInExpression("the condition expression of a while statement"));
    }
    
    //Visit the condition expression with an expression type checker and
    //then update list of what was assigned, because it will always be visited so it is in the same scope as the enclosing body.
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData condition_result = that.getCondition().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);

    //Use a new visitor for the body of the while statement--it is a different lexical scope, so we want to track the
    //variables seperately.
    BodyTypeChecker btc = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData code_result = that.getCode().visit(btc);
    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    if (expOk.booleanValue()) {return forWhileStatementOnly(that, condition_result, code_result);}
    return null;
  }
  
  
  /**Make sure that the condition statement of the while returns type boolean. */
  public TypeData forWhileStatementOnly(WhileStatement that, TypeData condition_result, TypeData code_result) {
    if (condition_result != null && assertFound(condition_result, that.getCondition())) {
      if (!condition_result.isInstanceType()) {
        _addError("This while-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", that);
      }
      else if (!condition_result.getSymbolData().isBooleanType(LanguageLevelConverter.OPT.javaVersion())) {
        _addError("This while-statement's conditional expression must be a boolean value. Instead, its type is " + condition_result.getName(), that.getCondition());
      }
    }
    return null;
  }
  
  /*
   * First, visit the body of the do statement with a body type checker.
   * Then, visit the condition expression of the do statement with a special visitor that
   * makes sure no assignment is done.
   * Then, visit the condition expression with the ExpressionTypeChecker which will do all the
   * normal expression stuff.
   * Be very careful about maintaing the various scopes here.
   */
  public TypeData forDoStatement(DoStatement that) {
    //Use a new visitor for the body of the do statement--it is a different lexical scope, so we want to track the
    //variables seperately.
    BodyTypeChecker btc = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    final TypeData code_result = that.getCode().visit(btc);
    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    
    Boolean expOk = Boolean.TRUE;
    if (that.getCondition() instanceof Expression) {
      Expression exp = (Expression) that.getCondition();
      // Assignment cannot be used in this expression
      expOk = exp.visit(new NoAssignmentAllowedInExpression("the condition expression of a do statement"));
    }

    //Visit the condition statement with a new ExpressionTypeChecker, then update the thingsThatHaveBeenAssigned list, since it is in the same scope as the body.
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData condition_result = that.getCondition().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);

    if (expOk.booleanValue()) {return forDoStatementOnly(that, code_result, condition_result);}
    return null;
    
  }


  /**Make sure that the condition statement of the while returns type boolean. */
  public TypeData forDoStatementOnly(DoStatement that, TypeData code_result, TypeData condition_result) {
    if (condition_result != null && assertFound(condition_result, that.getCondition())) {
      if (!condition_result.isInstanceType()) {
        _addError("This do-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", that.getCondition());
      }
      else if (!condition_result.getSymbolData().isBooleanType(LanguageLevelConverter.OPT.javaVersion())) {
        _addError("This do-statement's conditional expression must be a boolean value. Instead, its type is " + condition_result.getName(), that.getCondition());
      }
    }
    if (code_result == null) {return null;}
    return code_result.getInstanceData();
  }
  
  /*Handle the switch statement(explained within the method)*/
  public TypeData forSwitchStatement(SwitchStatement that) {
    Expression exp = that.getTest();
    //make sure no assignments are made in the switch stmt expression
    exp.visit(new NoAssignmentAllowedInExpression("the switch expression of a switch statement"));

    //Visit the test with this visitor, because it is in the scope of the method
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData test_result = that.getTest().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    
    //The test result of a switch statement must be either an int or a char, according to the JLS.
    if (test_result == null || !assertFound(test_result, exp)) {return null;}
    if (!(_isAssignableFrom(SymbolData.INT_TYPE, test_result.getSymbolData()) || _isAssignableFrom(SymbolData.CHAR_TYPE, test_result.getSymbolData()))) {
      _addError("The switch expression must be either an int or a char.  You have used a " + test_result.getSymbolData().getName(), that.getTest());
    }
    
    final TypeData[] cases_result = makeArrayOfRetType(that.getCases().length);
    BodyTypeChecker[] btcs = new BodyTypeChecker[that.getCases().length];
    HashSet<Integer> labels = new HashSet<Integer>();
    LinkedList<VariableData> variablesAssigned = new LinkedList<VariableData>();
    boolean seenDefault = false;
    boolean hadCaseReturn = false;
    
    /**
     * Loop over all the cases, type-checking them and then checking that no labels are duplicated
     * and only one default statement is present.
     */
    for (int i = 0; i < that.getCases().length; i++) {
      SwitchCase sc = that.getCases()[i];
      btcs[i] = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
      cases_result[i] = sc.visit(btcs[i]);
      if (sc instanceof LabeledCase) {
        LabeledCase lc = (LabeledCase) sc;
        //Get the label, make sure it is an int or char, and then put it in our hash set.
          Integer toCheck = null;
          if (lc.getLabel() instanceof CharLiteral)  { 
            toCheck = (int) ((CharLiteral) lc.getLabel()).getValue();
          }
          if (lc.getLabel() instanceof IntegerLiteral) {
            toCheck = ((IntegerLiteral) lc.getLabel()).getValue();
          }
          if (toCheck != null) {
            if (labels.contains(toCheck)) {
              _addError("You cannot have two switch cases with the same label " + toCheck, lc.getLabel());
            }
            else {
              labels.add(toCheck);
            }
          }
      }
      else {
        if (seenDefault) {
          _addError("A switch statement can only have one default case", sc);
        }
        seenDefault = true;
      }
      
      //compare the variables assigned, and let variablesAssigned store the variables that have been assigned in every case 
      // that returns or breaks up to this one and are also assigned in this one
      // if we're in the last case, we treat it as a break even though that may not happen explicitly

      if (cases_result[i] != null || (i == cases_result.length - 1)) { 
        if (!hadCaseReturn) {
          variablesAssigned = btcs[i].thingsThatHaveBeenAssigned; 
          hadCaseReturn = true;
        }
        else {
          Iterator<VariableData> iter = variablesAssigned.iterator();
          while (iter.hasNext()) {
            if (!btcs[i].thingsThatHaveBeenAssigned.contains(iter.next())) {iter.remove();}
          }
        }

      }
      
      //Now, change any VariableDatas that were given a value in the case statement back to having been unassigned
      unassignVariableDatas(btcs[i].thingsThatHaveBeenAssigned);

    }

    //Now assign a value to all variables that were assigned in each case branch, if there was a default case
    if (seenDefault) {
      for (VariableData vd : variablesAssigned) {
        vd.gotValue();
      }
    }
    return forSwitchStatementOnly(that, test_result, cases_result, seenDefault);
}

  /**
   * Here, we follow the following rules for determining what to return:
   * If there is not a default block, the statement does not return.
   * If the result from any of the blocks is KEEP_GOING, the statement does not return.  (KEEP_GOING signifies that a break statement was seen)
   * If the last block does not return, then the statement does not return.
   */
   public TypeData forSwitchStatementOnly(SwitchStatement that, TypeData test_result, TypeData[] cases_result, boolean sawDefault) {
     
     /**If we did not see a default block, this statement cannot be guaranteed to return*/
     if (!sawDefault) {return null;}
     
     /**If any of the blocks are KEEP_GOING, then the statement does not return.*/
     for (int i = 0; i<cases_result.length; i++) {
       if (cases_result[i] != null && cases_result[i].getSymbolData() == SymbolData.KEEP_GOING) {return null;}
     }
     
     /**If the last block does not return, then the statement also does not return. */
     if (cases_result[cases_result.length-1] == null) { return null; }
     
     return _bodyData.getMethodData().getReturnType().getInstanceData();
  }
  
  
  /**
   * Make sure that the label for this LabeledCase is correct.  The label must be a constant expression of type int or char.
   * Then delegate to the super class to handle the braced body of the switch case.
   */
   public TypeData forLabeledCase(LabeledCase that) {
     ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
    final TypeData label_result = that.getLabel().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    Expression exp = that.getLabel();
    
    if (label_result == null || !assertFound(label_result, exp)) {
      return null;
    }
    
    //we allow constant expressions of the form -5 or 5, but nothing else.
    if (!(exp instanceof LexicalLiteral || exp instanceof NumericUnaryExpression && ((NumericUnaryExpression) exp).getValue() instanceof LexicalLiteral)) {
      _addError("The labels of a switch statement must be constants.  You are using a more complicated expression of type " + label_result.getSymbolData().getName(), that.getLabel());
    }
    else if (!_isAssignableFrom(SymbolData.INT_TYPE, label_result.getSymbolData())) {
      _addError("The labels of a switch statement must be constants of int or char type.  You specified a constant of type " + label_result.getSymbolData().getName(), that.getLabel());
    }
    
    return forSwitchCase(that);
  }
  
  /**
   * Delegate handling this default case to its superclass.
   */
  public TypeData forDefaultCase(DefaultCase that) {
    return forSwitchCase(that);
  }
  
  /**
   * Visit the Braced Body of this SwitchCase, and return the result.
   */
  public TypeData forSwitchCase(SwitchCase that) {
    final TypeData code_result = that.getCode().visit(this);
    
    //If the case falls through (i.e. returns null) but has some statements in it, then there is an error.
    //We only want to allow fall through for multiple labels, of the form:
    /** 'a':
     *  'b':
     *   return 5;
     */
    
    if (code_result == null && that.getCode().getStatements().length > 0) {
      _addError("You must end a non-empty switch case with a break or return statement at the Advanced level", that);
    }
    return code_result;
  }
  
 
  /*Visit the block, and update with what it assigns.  Return the result of visiting the block.*/
  public TypeData forBlock(Block that) {
    BlockData bd = _bodyData.getNextBlock();
    if (bd == null) { throw new RuntimeException("Internal Program Error: Enclosing body does not contain this block.  Please report this bug"); }
    
    BodyTypeChecker btc = createANewInstanceOfMe(bd, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    TypeData statements_result = that.getStatements().visit(btc);
    thingsThatHaveBeenAssigned.addAll(btc.thingsThatHaveBeenAssigned);
    return statements_result;
  }
  
  
  /*Try to resolve the type and make sure it can be referenced from this context (i.e. is accessible).*/
  public TypeData forTypeOnly(Type that) {
    Data sd = getSymbolData(that.getName(), _data, that);
    while (sd != null && !LanguageLevelVisitor.isJavaLibraryClass(sd.getSymbolData().getName())) {
      if (!checkAccessibility(that, sd.getMav(), sd.getName(), sd.getSymbolData(), _bodyData.getSymbolData(), "class")) {
        return null;
      }
      sd = sd.getOuterData();
    }
    return null;
  }
  
  /**
   * Makes sure that no super class of any exception is caught before the current exception's catch block.
   */
  protected void checkDuplicateExceptions(TryCatchStatement that) {
    // Make sure that the user isn't throwing duplicate exceptions
    LinkedList<SymbolData> catchBlockExceptions = new LinkedList<SymbolData>();
    CatchBlock[] catchBlocks = that.getCatchBlocks();
    for (int i = 0; i < catchBlocks.length; i++) {
      catchBlockExceptions.addLast(getSymbolData(catchBlocks[i].getException().getDeclarator().getType().getName(), _data, catchBlocks[i].getException()));
    }
    for (int i = 0; i < catchBlockExceptions.size(); i++) {
      for (int j = i+1; j < catchBlockExceptions.size(); j++) {
        if (catchBlockExceptions.get(j) != null && catchBlockExceptions.get(j).isSubClassOf(catchBlockExceptions.get(i))) {
          _addError("Exception " + catchBlockExceptions.get(j).getName() + " has already been caught", catchBlocks[j].getException());
        }
      }
    }
  }
  
  /**
   * Check if the two given SymbolDatas have a common super type.  If so, return it, else return null.
   */
  protected SymbolData getCommonSuperType(SymbolData s1, SymbolData s2) {
    if ((s1 == null) && (s2 == null)) {
      return null;
    }
    
    if (s1 == SymbolData.KEEP_GOING && s2 != null) {return SymbolData.KEEP_GOING;}
    if (s2 == SymbolData.KEEP_GOING && s1 != null) {return SymbolData.KEEP_GOING;}
    
    if (s1 == null && s1 != SymbolData.KEEP_GOING) { return s2; }
    if (s2 == null && s1 != SymbolData.KEEP_GOING) {return s1;}
    if (s1==null || s2==null) {return null;}
    if (s1 == SymbolData.EXCEPTION) { return s2; }
    if (s2 == SymbolData.EXCEPTION) { return s1; }
    
    // See if s1 and s2 have a common super class.
    SymbolData sd = getCommonSuperTypeBaseCase(s1, s2);
    if (sd != null ) { return sd; }
    sd = getCommonSuperTypeBaseCase(s2, s1);
    if (sd != null) { return sd; }
    
    //If s1's superClass is null, then we have gone all the way through the superclass hierarchy without finding a matching class.
    if (s1.getSuperClass() == null) {
      //return null;
      //since we know that Object should be the super class of everything, return Object.
      return getSymbolData("java.lang.Object", _data, new NullLiteral(JExprParser.NO_SOURCE_INFO));
    }
    
    // Recur on the super class chain.   
    sd = getCommonSuperType(s1.getSuperClass(), s2);
    if (sd != null) {
      return sd;
    }
    
    // Recur on each interface.
    for (SymbolData currSd : s1.getInterfaces()) {
      sd = getCommonSuperType(currSd, s2);
      if (sd != null) {
        return sd;
      }
    }
    return null;
  }


  /*@return true if the symbol data is the generic SymbolData.EXCEPTIOn class or if it extends java.lang.Throwable*/
  protected boolean isException(SymbolData sd) {
    return sd==SymbolData.EXCEPTION || sd.isSubClassOf(getSymbolData("java.lang.Throwable", new NullLiteral(JExprParser.NO_SOURCE_INFO), false, false));
  }
  
  /**
   * Returns the least restrictive type returned by the try block and catch blocks or the finally block.  Returns null if this
   * try-catch statement doesn't necessarily return a value.
   */
  protected InstanceData tryCatchLeastRestrictiveType(InstanceData tryBlock_result, InstanceData[] catchBlocks_result, InstanceData finallyBlock_result) {// Return the common superclass or null if there exists a block that doesn't return no nothing (except the finally block) 
    if (tryBlock_result == null || tryBlock_result == SymbolData.KEEP_GOING.getInstanceData()) {return finallyBlock_result;}
    TypeData leastRestrictiveType = tryBlock_result;
    for (int i = 0; i < catchBlocks_result.length; i++) {
      if (catchBlocks_result[i] == null) {return finallyBlock_result;}
      if (catchBlocks_result[i] != SymbolData.KEEP_GOING.getInstanceData() && _isAssignableFrom(catchBlocks_result[i].getSymbolData(), leastRestrictiveType.getSymbolData())) {
        leastRestrictiveType = catchBlocks_result[i];
      }
    }

    SymbolData result;
    if (leastRestrictiveType == null && finallyBlock_result == null) {return null;}
    else if (leastRestrictiveType == null) {result = getCommonSuperType(null, finallyBlock_result.getSymbolData());}
    else if (finallyBlock_result == null) {result = getCommonSuperType(leastRestrictiveType.getSymbolData(), null);}
    else { result = getCommonSuperType(leastRestrictiveType.getSymbolData(), finallyBlock_result.getSymbolData());} 
   
    if (result != null) {return result.getInstanceData();}
    return null;
  }
  
  /**
   * Return true if the Exception is unchecked, and false otherwise.
   * An exception is unchecked if it does not extend either java.lang.RuntimeException or java.lang.Error,
   * and is not declared to be thrown by the enclosing method.
   * @param sd  The SymbolData of the Exception we are checking.
   * @param that  The JExpression passed to getSymbolData for error purposes.
   */
  public boolean isUncaughtCheckedException(SymbolData sd, JExpression that) {
    if (isCheckedException(sd, that)) {
      MethodData md = _bodyData.getMethodData();
      for (int i = 0; i<md.getThrown().length; i++) {
        //If the Exception matches an exception declared to be thrown by the enclosing method, it is not unchecked.
        if (sd.isSubClassOf(getSymbolData(md.getThrown()[i], _data, that))) {return false;}
      }
      return true;
    }
    return false;
  }

  /**
   * Make sure that every exception that is caught could have been thrown in the try statement
   */
  protected void makeSureCaughtStuffWasThrown(TryCatchStatement that, SymbolData[] caught_array, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    // Make sure every Exception that is caught could actually be thrown
    for (int i = 0; i < caught_array.length; i++) {
      SymbolData currCaughtSD = caught_array[i];
      boolean foundThrownException = false;
      if (isCheckedException(currCaughtSD, that) && 
          ! currCaughtSD.getName().equals("java.lang.Exception") &&
          ! currCaughtSD.getName().equals("java.lang.Throwable")) {
        for (Pair<SymbolData, JExpression> p : thrown) {
          SymbolData sd = p.getFirst();
          if (sd.isSubClassOf(currCaughtSD)) {
            foundThrownException = true;
          }
        }
        if (!foundThrownException) {
          _addError("The exception " + currCaughtSD.getName() + " is never thrown in the body of the corresponding try block", that.getCatchBlocks()[i]);
        }
      } 
    }
  }
  
  /**
   * Make sure that every Exception in thrown is either in caught or in the list of what can be thrown from where we are.
   * Also make sure that every Exception that is declared to be thrown or caught is actually thrown.
   * @param that  The TryCatchStatement we are currently working with
   * @param caught_array  The SymbolData[] of exceptions that are explicitely caught.
   * @param thrown  The LinkedList of SymbolData of exceptions that are thrown.  This will be modified.
   */
  protected void compareThrownAndCaught(TryCatchStatement that, SymbolData[] caught_array, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    LinkedList<SymbolData> caught = new LinkedList<SymbolData>();
    for (int i = 0; i<caught_array.length; i++) {
      caught.addLast(caught_array[i]);
    }

    //Make sure that every Exception in thrown is either caught or in the list of what can be thrown
    for (Pair<SymbolData, JExpression> p : thrown) {
      SymbolData sd = p.getFirst();
      JExpression j = p.getSecond();
      if (isUncaughtCheckedException(sd, j)) {
        boolean foundCatchBlock = false;
        for (SymbolData currCaughtSD : caught) {
          if (sd.isSubClassOf(currCaughtSD)) {
            foundCatchBlock = true;
          }
        }
        // Check if this exception is a checked exception and is not declared to be thrown by the enclosing method
        //This is a checked exception.  It should have been caught.
        if (!foundCatchBlock) {
          handleUncheckedException(sd, j);
        }
      }
    }
    
    makeSureCaughtStuffWasThrown(that, caught_array, thrown);
  }

/**
 * Assumes that tryBlock_result, catchBlocks_result, and finallyBlock_result are InstanceDatas
 */
  public TypeData forTryCatchFinallyStatementOnly(TryCatchFinallyStatement that, TypeData tryBlock_result, TypeData[] catchBlocks_result, TypeData finallyBlock_result) {
    checkDuplicateExceptions(that);
    
    //we know ids are instance datas, but we have to do this to cast them properly.
    InstanceData[] ids = new InstanceData[catchBlocks_result.length];
    for (int i = 0; i<ids.length; i++) {
      if (catchBlocks_result[i] != null) {
        ids[i]=catchBlocks_result[i].getInstanceData();
      }
      else {ids[i]=null;}
    }
    
    /**Make sure null pointer exceptions don't happen.*/
    if (tryBlock_result == null && finallyBlock_result==null) {return tryCatchLeastRestrictiveType(null, ids, null);}
    if (tryBlock_result == null) {return tryCatchLeastRestrictiveType(null, ids, finallyBlock_result.getInstanceData());}
    if (finallyBlock_result == null) {return tryCatchLeastRestrictiveType(tryBlock_result.getInstanceData(), ids, null);}

    return tryCatchLeastRestrictiveType(tryBlock_result.getInstanceData(), ids, finallyBlock_result.getInstanceData());
  }
 
  /*Visit the try block, catch blocks, and finally block.  Add any exceptions that are not caught to thrown.*/ 
  public TypeData forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    
    BodyTypeChecker btc = new TryCatchBodyTypeChecker(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), new LinkedList<Pair<SymbolData, JExpression>>());
    final TypeData tryBlock_result = that.getTryBlock().visit(btc);

    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);
    
    BodyTypeChecker[] catchTCs = new BodyTypeChecker[that.getCatchBlocks().length];
    LinkedList<LinkedList<VariableData>> catchVars = new LinkedList<LinkedList<VariableData>>();
    CatchBlock[] catchBlocks = that.getCatchBlocks();
    final TypeData[] catchBlocks_result = makeArrayOfRetType(catchBlocks.length);
    final SymbolData[] caughtExceptions = new SymbolData[catchBlocks.length];

    for (int i = 0; i < catchBlocks.length; i++) {
      catchTCs[i] = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
      catchBlocks_result[i] = catchBlocks[i].visit(catchTCs[i]);
      unassignVariableDatas(catchTCs[i].thingsThatHaveBeenAssigned);
      catchVars.addLast(catchTCs[i].thingsThatHaveBeenAssigned);
      caughtExceptions[i] = getSymbolData(catchBlocks[i].getException().getDeclarator().getType().getName(), _data, catchBlocks[i]);
    }

    BodyTypeChecker btcFinally = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
    TypeData finallyBlock_result = that.getFinallyBlock().visit(btcFinally);
    //leave anything that was assigned in the finally block assigned.  It is always visited.
    
    //Give values to anything assigned in the try and catch blocks.
    reassignLotsaVariableDatas(btc.thingsThatHaveBeenAssigned, catchVars);

    //If the finallyBlock didn't end abruptly, need to check thrown and caught
    
    if (finallyBlock_result == null) {
      compareThrownAndCaught(that, caughtExceptions, btc._thrown);
    }
    
    else { /**It is like thrown is empty--any exceptions in it don't need to be caught.*/
      _thrown = new LinkedList<Pair<SymbolData, JExpression>>();
    }
    
    if (finallyBlock_result == SymbolData.KEEP_GOING.getInstanceData()) {
      finallyBlock_result = null;
    }
    
    // Add the exceptions thrown by btc to our thrown list.
    if (this instanceof TryCatchBodyTypeChecker) {
      _thrown.addAll(btc._thrown);
    }
    
    return forTryCatchFinallyStatementOnly(that, tryBlock_result, catchBlocks_result, finallyBlock_result);
  }
  
  
  /*
   * Resolve the type of the exception, and visit the body, making sure the exception variable
   * is in scope.
   */
  public TypeData forCatchBlock(CatchBlock that) {
    VariableDeclarator dec = that.getException().getDeclarator();
    SymbolData exception_result = getSymbolData(dec.getType().getName(), _data, dec.getType());
    
    BlockData bd = _bodyData.getNextBlock();
    if (bd == null) { throw new RuntimeException("Internal Program Error: Enclosing body does not contain this block.  Please report this bug"); }
    VariableData vd = bd.getVar(dec.getName().getText());
    if (vd == null) { throw new RuntimeException("Internal Program Error: Catch block does not contain its exception variable.  Please report this bug"); }
    LinkedList<VariableData> newVars = cloneVariableDataList(_vars);
    newVars.addLast(vd);
    BodyTypeChecker btc = createANewInstanceOfMe(bd, _file, _package, _importedFiles, _importedPackages, newVars, _thrown);
    TypeData block_result = that.getBlock().getStatements().visit(btc);
    thingsThatHaveBeenAssigned.addAll(btc.thingsThatHaveBeenAssigned);
    return forCatchBlockOnly(that, exception_result, block_result);
  }
  
  /*Return the result of visiting the body*/
  public TypeData forCatchBlockOnly(CatchBlock that, SymbolData exception_result, TypeData block_result) {
    return block_result;
  }
  
  /**Assumes that tryBlock_result, catchBlocks_result, and finallyBlock_result are InstanceDatas*/
  public TypeData forNormalTryCatchStatementOnly(NormalTryCatchStatement that, TypeData tryBlock_result, TypeData[] catchBlocks_result) {
    checkDuplicateExceptions(that);
    InstanceData[] ids = new InstanceData[catchBlocks_result.length];
    for (int i = 0; i<catchBlocks_result.length; i++) {
      ids[i]=(InstanceData) catchBlocks_result[i];

    }
      
    return tryCatchLeastRestrictiveType((InstanceData) tryBlock_result, ids, null);
  }
  
  /*no finally block*/
  public TypeData forNormalTryCatchStatement(NormalTryCatchStatement that) {
    BodyTypeChecker btc = new TryCatchBodyTypeChecker(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), new LinkedList<Pair<SymbolData, JExpression>>());
    final TypeData tryBlock_result = that.getTryBlock().visit(btc);
    unassignVariableDatas(btc.thingsThatHaveBeenAssigned);

    LinkedList<LinkedList<VariableData>> catchVars = new LinkedList<LinkedList<VariableData>>();
    BodyTypeChecker[] catchTCs = new BodyTypeChecker[that.getCatchBlocks().length];

    CatchBlock[] catchBlocks = that.getCatchBlocks();
    final TypeData[] catchBlocks_result = makeArrayOfRetType(catchBlocks.length);
    final SymbolData[] caughtExceptions = new SymbolData[catchBlocks.length];
    for (int i = 0; i < catchBlocks.length; i++) {
      catchTCs[i] = createANewInstanceOfMe(_bodyData, _file, _package, _importedFiles, _importedPackages, cloneVariableDataList(_vars), _thrown);
      catchBlocks_result[i] = catchBlocks[i].visit(catchTCs[i]);
      unassignVariableDatas(catchTCs[i].thingsThatHaveBeenAssigned);
      catchVars.addLast(catchTCs[i].thingsThatHaveBeenAssigned);
      caughtExceptions[i] = getSymbolData(catchBlocks[i].getException().getDeclarator().getType().getName(), _data, catchBlocks[i]);
    }
    
    //Give values to anything assigned in the try and catch blocks.
    reassignLotsaVariableDatas(btc.thingsThatHaveBeenAssigned, catchVars);
    
    compareThrownAndCaught(that, caughtExceptions, btc._thrown);    

    // Add the exceptions thrown by btc to our thrown list.
    if (this instanceof TryCatchBodyTypeChecker) {
      _thrown.addAll(btc._thrown);
    }   
    return forNormalTryCatchStatementOnly(that, tryBlock_result, catchBlocks_result);
  }
  
  
   /*A special visitor that does not allow assignment in any expressions*/
  private class NoAssignmentAllowedInExpression extends JExpressionIFAbstractVisitor<Boolean> {
    String _location;
    private NoAssignmentAllowedInExpression(String location) {
      _location = location;
    }
  
    /**
     * Most expressions do not involve assignment
     */
    public Boolean defaultCase(JExpressionIF that) {
      return Boolean.TRUE;
    }
  
    /*Throw an appropriate error*/
    public Boolean forIncrementExpression(IncrementExpression that) {
      _addError("You cannot use an increment or decrement expression in " + _location +  " at any language level", that);
      return Boolean.FALSE;
    }
  
    /*Throw an appropriate error*/
    public Boolean forAssignmentExpression(AssignmentExpression that) {
      _addError("You cannot use an assignment expression in " + _location +  " at any language level", that);
      return Boolean.FALSE;
    }
    
    /*Throw an appropriate error*/
    public Boolean forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
      _addError("You cannot use an assignment expression in " + _location +  " at any language level" + ".  Perhaps you meant to compare two values with '=='", that);
      return Boolean.FALSE;
    }

  }
  
  
  
   /**
    * Test the methods in the above class.
    */
  public static class BodyTypeCheckerTest extends TestCase {
    
    private BodyTypeChecker _bbtc;
    
    private BodyData _bd1;
    private BodyData _bd2;
    
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
    
    
    public BodyTypeCheckerTest() {
      this("");
    }
    public BodyTypeCheckerTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");

      _bd1 = new MethodData("methodName1", 
                            _packageMav, 
                            new TypeParameter[0], 
                            SymbolData.INT_TYPE, 
                            new VariableData[] { new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, null), new VariableData(SymbolData.BOOLEAN_TYPE) },
                            new String[0],
                            _sd1,
                            null); // no SourceInfo
      ((MethodData)_bd1).getParams()[0].setEnclosingData(_bd1);                      
      ((MethodData)_bd1).getParams()[1].setEnclosingData(_bd1);                      

      _bd2 = new MethodData("methodName2", 
                            _packageMav, 
                            new TypeParameter[0], 
                            SymbolData.VOID_TYPE, 
                            new VariableData[] { new VariableData(SymbolData.INT_TYPE) },
                            new String[0],
                            _sd1,
                            null); // no SourceInfo);
       ((MethodData)_bd2).getParams()[0].setEnclosingData(_bd2);
                            
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      _bd1.addEnclosingData(_sd1);
      _bd1.addVars(((MethodData)_bd1).getParams());
      _bd2.addVars(((MethodData)_bd2).getParams());
      _bbtc = new BodyTypeChecker(_bd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData,JExpression>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.<File>empty());
      _bbtc._importedPackages.addFirst("java.lang");
    }
    
    public void testForUninitializedVariableDeclaratorOnly() {
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, false, _bd1);
      _bd1.addVar(vd1);
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                                new Word(JExprParser.NO_SOURCE_INFO, "Mojo"));
      uvd.visit(_bbtc);
      assertTrue("_vars should contain Mojo.", _bbtc._vars.contains(vd1));
    }
    
    public void testForInitializedVariableDeclaratorOnly() {
      _bbtc.symbolTable.put("int", SymbolData.INT_TYPE);
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, true, _bd1);
      _bd1.addVar(vd1);
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                            new Word(JExprParser.NO_SOURCE_INFO, "Mojo"), 
                                                                            new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      ivd.visit(_bbtc);
      assertEquals("There should be no errors.", 0, errors.size());
      assertTrue("_vars should contain Mojo.", _bbtc._vars.contains(vd1));
      ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                              new Word(JExprParser.NO_SOURCE_INFO, "Santa's Little Helper"), 
                                              new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1));
      try {
        ivd.visit(_bbtc);
        fail("Should have thrown a RuntimeException because there's no field named Santa's Little Helper.");
      }
      catch (RuntimeException re) {
        assertEquals("The error message should be correct.", "Internal Program Error: The field or variable Santa's Little Helper was not found in this block.  Please report this bug.", re.getMessage());
      }
    }
    
    public void testForBracedBodyOnly() {
      // Test one that works.
      BracedBody bb1 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1))});
      TypeData sd = bb1.visit(_bbtc);
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), sd);
      BracedBody bb2 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new CharLiteral(JExprParser.NO_SOURCE_INFO, 'e'))});
     //test another one that works.
      sd = bb2.visit(_bbtc);
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("Should return char type", SymbolData.CHAR_TYPE.getInstanceData(), sd);
      BracedBody bb3 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1)),
                                                        new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new CharLiteral(JExprParser.NO_SOURCE_INFO, 'e'))});
      //test one that should throw an error: unreachable return statement.                                                                                          
      sd = bb3.visit(_bbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Unreachable statement.", errors.get(0).getFirst());
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), sd);
      
      BracedBody bb4 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[0]);
      //test empty body.  should return null.
      sd = bb4.visit(_bbtc);
      assertEquals("There should still be one error", 1, errors.size());
      assertEquals("The error message should still be be correct", "Unreachable statement.", errors.get(0).getFirst());
      assertEquals("Should return null", null, sd);
    }
    
    public void testForVoidReturnStatementOnly() {
      _bbtc._bodyData = _bd2; //this body data has a void return type

      //test one that works
      BracedBody bb1 = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                      new BodyItemI[] { new VoidReturnStatement(JExprParser.NO_SOURCE_INFO)});

      TypeData sd = bb1.visit(_bbtc);

      assertEquals("There should be no errors.", 0, errors.size());
      assertEquals("Should return void type.", SymbolData.VOID_TYPE.getInstanceData(), sd);

      //test with a method that doesn't return void.
      _bbtc._bodyData = _bd1;
      sd = bb1.visit(_bbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), sd);
      assertEquals("Error message should be correct", "Cannot return void when the method's expected return type is not void.", errors.get(0).getFirst());

    }
   
    public void testforValueReturnStatementOnly() {
      //value return statement returns something that is not a subclass the method return type
      BracedBody bb1 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true))});
      TypeData sd = bb1.visit(_bbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Should return boolean type", SymbolData.BOOLEAN_TYPE.getInstanceData(), sd);
      assertEquals("Error message should be correct", "This method expected to return type: \"int\" but here returned type: \"boolean\".", errors.get(0).getFirst());
      
      //value return statement returns something that is assignable from the method return type
      BracedBody bb2 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new CharLiteral(JExprParser.NO_SOURCE_INFO, 'c'))});
      sd = bb2.visit(_bbtc);
      assertEquals("There should be still be one error", 1, errors.size());
      assertEquals("Should return char type", SymbolData.CHAR_TYPE.getInstanceData(), sd);
      assertEquals("Error message should still be correct", "This method expected to return type: \"int\" but here returned type: \"boolean\".", errors.get(0).getFirst());
      
      
      //method returns void
      _bbtc._bodyData = _bd2; //this body data has a void return type
      
      BracedBody bb3 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1))});

      sd = bb3.visit(_bbtc);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("Should return void type", SymbolData.VOID_TYPE.getInstanceData(), sd);
      assertEquals("Error message should be correct", "Cannot return a value when the method's expected return type is void.", errors.get(1).getFirst());

      // Test where the return value is a class name.
      BracedBody bb4 = new BracedBody(JExprParser.NO_SOURCE_INFO,
                                      new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                 new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "int")))});

      sd = bb4.visit(_bbtc);
      assertEquals("There should be 3 errors", 3, errors.size());
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), sd);
      assertEquals("Error message should be correct", "You cannot return a class or interface name.  Perhaps you meant to say int.class or to create an instance", errors.getLast().getFirst());

    }
    
    public void testForIfThenElseStatementOnly() {
      //test if the expression is not of boolean type
      IfThenElseStatement ites1 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new CharLiteral(JExprParser.NO_SOURCE_INFO, 'j')));

      TypeData sd = ites1.visit(_bbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "This if-then-else statement's conditional expression must be a boolean value. Instead, its type is int", errors.get(0).getFirst());

      assertEquals("Should return integer type", SymbolData.INT_TYPE.getInstanceData(), sd);
                                                    
      
      //test if the branches do not return subtypes of each other
      IfThenElseStatement ites2 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true)));

      sd = ites2.visit(_bbtc);
      assertEquals("There should be two errors", 2, errors.size());
      
      assertEquals("Should return Object type", "java.lang.Object", sd.getName());
      assertEquals("Error message should be correct", 
                   "This method expected to return type: \"int\" but here returned type: \"boolean\".", 
                   errors.get(1).getFirst());                                                          

      //test if they do return subtypes of each other
      IfThenElseStatement ites3 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new CharLiteral(JExprParser.NO_SOURCE_INFO, 'f')));

      sd = ites3.visit(_bbtc);
      assertEquals("There should still be two errors", 2, errors.size());
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), sd);
      
      //test if neither branch returns
      IfThenElseStatement ites4 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new EmptyStatement(JExprParser.NO_SOURCE_INFO),
                                                          new EmptyStatement(JExprParser.NO_SOURCE_INFO));

      sd = ites4.visit(_bbtc);
      assertEquals("There should still be two errors", 2, errors.size());
      assertEquals("Should return null type", null, sd);
      
      //test if only one branch returns      
      IfThenElseStatement ites5 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new EmptyStatement(JExprParser.NO_SOURCE_INFO),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 3)));

      sd = ites5.visit(_bbtc);
      assertEquals("There should still be two errors", 2, errors.size());
      assertEquals("Should return null type", null, sd);      

      
      // Test for the word "boolean" as the condition.
      IfThenElseStatement ites6 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "boolean")),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)));
                                                          
      sd = ites6.visit(_bbtc);
      assertEquals("There should be 3 errors", 3, errors.size());
      
      assertEquals("Should return Integer type", SymbolData.INT_TYPE.getInstanceData(), sd);
      assertEquals("Error message should be correct", 
                   "This if-then-else statement's conditional expression must be a boolean value. Instead, it is a class or interface name", 
                   errors.get(2).getFirst());                                                          
}
    
    public void testForBlock() {
      //Check that a block can reference fields in its enclosing method.
      Block b = new Block(JExprParser.NO_SOURCE_INFO, 
                          new BracedBody(JExprParser.NO_SOURCE_INFO,
                                         new BodyItemI[] { new ValueReturnStatement(JExprParser.NO_SOURCE_INFO,
                                                                                    new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")))}));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, _bd1));
      _bbtc._vars = vars;
      TypeData sd = b.visit(_bbtc);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Should return int type.", SymbolData.INT_TYPE.getInstanceData(), sd);
    }
    
    public void testForIfThenStatementOnly() {
      SymbolData sd1 = SymbolData.BOOLEAN_TYPE;
      SymbolData sd2 = SymbolData.INT_TYPE;

      IfThenStatement its = new IfThenStatement(JExprParser.NO_SOURCE_INFO, 
                                                new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                                new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      

      //test a correct condition type
      assertEquals("sd1 is boolean type, so should not add error. Returns null.", null, 
                   _bbtc.forIfThenStatementOnly(its, sd1.getInstanceData(), null));
      assertEquals("No errors should have been added", 0, errors.size());
      
      //test an incorrect condition type
      assertEquals("sd2 is not boolean type, so should add error. Returns null.", null, 
                   _bbtc.forIfThenStatementOnly(its, sd2.getInstanceData(), null));
      assertEquals("Should now be one error.", 1, errors.size());
      assertEquals("Error message should be correct.", "This if-then-statement's conditional expression must be a boolean value. Instead, its type is int", errors.getLast().getFirst());
      
      //test "bool" as a condition
      assertEquals("sd1 is not an instance, so should add error. Returns null.", null, 
                   _bbtc.forIfThenStatementOnly(its, sd1, null));
      assertEquals("Should now be 2 errors.", 2, errors.size());
      assertEquals("Error message should be correct.", "This if-then-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", errors.getLast().getFirst());
    }
   
    public void testForIfThenStatement() {
      //Test that the proper variable assignment happens.
      //here, a variable is only assigned in the then branch, so it should not be set after it returns.
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      IfThenStatement ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);

      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);
      md1.addVar(vd1);
      md1.addVar(vd2);
      
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      
      
      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());

      
      //Here, a variable is assigned before the if, so it should still have a value after the if.

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);

      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);
      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;

      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should also be assigned", vd2.hasValue());
 
      
      //test that if a variable is assigned in a branch of the if, and then returned, it is okay.
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      Statement returnStatement = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement, returnStatement}));
      ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);
      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      
      // Test that an assignment in the if-expression throws an error
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      
      te = new PlusAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      ift = new IfThenStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      ift.visit(_bbtc);
      assertEquals("There should now be one error", 1, errors.size());
      assertEquals("Error message should be correct", "You cannot use an assignment expression in the conditional expression of an if-then statement at any language level", errors.get(0).getFirst());
      
      
    }
    
    public void testForIfThenElseStatement() {
      
      //Test that the proper variable assignment happens.
      //here, a variable is only assigned in the then branch, so it should not be set after it returns.
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));

      IfThenElseStatement ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      
      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);

      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;      
      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      
      //test that if a variable is only assigned in the else case that it is not assigned afterwards
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, new EmptyStatement(JExprParser.NO_SOURCE_INFO), ts);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());
      
      //Here, a variable is assigned before the if, so it should still have a value after the if.

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should also be assigned", vd2.hasValue());
 
      
      //test that if a variable is assigned in a branch of the if, and then returned, it is okay.
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      Statement returnStatement = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement, returnStatement}));
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());
      errors = new LinkedList<Pair<String, JExpressionIF>>();

      
      //test that if a variable is assigned in the then case that it cannot be used in the else case.
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new EqualsExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word (JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 32))));
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
       
     
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot use i because it may not have been given a value", errors.get(0).getFirst());
      
      //test that if a variable is assigned in both cases that it is assigned afterwards
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, ts);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ift.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should be assigned", vd2.hasValue());
      assertEquals("There should be one error", 1, errors.size());
      
      
      //Test that if assignment is used in the conditional expression, an error is thrown
      te = new PlusAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));      
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, new EmptyStatement(JExprParser.NO_SOURCE_INFO), ts);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ift.visit(_bbtc);
      assertEquals("There should now be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "You cannot use an assignment expression in the conditional expression of an if-then-else statement at any language level", errors.get(1).getFirst());
      
      //test that if one branch returns a value but the other is a break or continue that SymbolData.KEEP_GOING is returned.
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      returnStatement = new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")));
      ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {returnStatement}));
      BreakStatement bs = new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO);
      ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, bs);
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), ift});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      assertEquals("Should return SymbolData.KEEP_GOING", SymbolData.KEEP_GOING.getInstanceData(), ift.visit(_bbtc));
      
      assertEquals("There should still be two errors", 2, errors.size());      
    }
    
    public void testForForStatement() {
      //Test that the proper variable assignment happens.
      //here, a variable is only assigned in the for init, so it should not be set after it returns.
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));

      UnparenthesizedExpressionList sel = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      ForStatement fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, te, new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      //      IfThenElseStatement ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      
      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), fs});
      
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                                     intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);
                                                     
      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                      new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;      

      fs.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());
      
      //test that if a variable is testForForStdeclared in the for init that it has a value in the scope of the for statement, but not afterwards
      te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      VariableDeclaration vd = new VariableDeclaration (JExprParser.NO_SOURCE_INFO, _publicMav, new VariableDeclarator[] { new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i"), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      UnparenthesizedExpressionList sel2 = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {te});
      fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, te, sel2, new ExpressionStatement(JExprParser.NO_SOURCE_INFO, te));
            
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {fs});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      
      fs.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());
      
      //here, a variable is only assigned in the for init and the for body, so it should not be set after it returns.
      Statement ts = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));
//      te = new Expression(JExprParser.NO_SOURCE_INFO, new ExpressionPiece[] { new Word(JExprParser.NO_SOURCE_INFO, "j"),
//        new Operator(JExprParser.NO_SOURCE_INFO, "<"), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)});
      sel = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, new EmptyForCondition(JExprParser.NO_SOURCE_INFO), new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {ts})));
      //      IfThenElseStatement ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), fs});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                                     intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);
                                                     
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                      new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;      
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      fs.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());
      assertEquals("There should be no errors", 0, errors.size());

      //here, a variable is assigned before the for init, so it should still be set after it returns.
//      te = new Expression(JExprParser.NO_SOURCE_INFO, new ExpressionPiece[] { new Word(JExprParser.NO_SOURCE_INFO, "j"),
//        new Operator(JExprParser.NO_SOURCE_INFO, "<"), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)});
      sel = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, new EmptyForCondition(JExprParser.NO_SOURCE_INFO), new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      //      IfThenElseStatement ift = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO, te, ts, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
      
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VariableDeclaration(JExprParser.NO_SOURCE_INFO,  _packageMav, new UninitializedVariableDeclarator[]{uvd}), fs});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                                     intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                                     new ReferenceType[0], bb);
                                                     
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                      new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;      
      
      fs.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should be assigned", vd2.hasValue());
      assertEquals("Should be 0 errors", 0, errors.size());
      
//      make sure that assignment is not allowed in the conditional of the for statement
      te = new PlusAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      vd = new VariableDeclaration (JExprParser.NO_SOURCE_INFO, _publicMav, new VariableDeclarator[] { new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i"), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      sel2 = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {te});
      fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, te, sel2, new EmptyStatement(JExprParser.NO_SOURCE_INFO));
            
      bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {fs});
      
      cmd1 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], 
                                   intt, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[] {param}, 
                                   new ReferenceType[0], bb);
                                   
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                           new VariableData[] {vd1}, new String[0], _sd1, cmd1);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      
      te = new PositivePrefixIncrementExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")));
      vd = new VariableDeclaration (JExprParser.NO_SOURCE_INFO, _publicMav, new VariableDeclarator[] { new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i"), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      sel2 = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {te});
      fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, te, sel2, new EmptyStatement(JExprParser.NO_SOURCE_INFO));

      fs.visit(_bbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot use an increment or decrement expression in the conditional expression of a for-statement at any language level", errors.get(0).getFirst());
      
      
    }
    
    public void testForWhileStatement() {
      //Test that a variable without a value before the while statement, that is given a value in the body of the while statement, still doesn't have a value afterwards
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));


      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));      

      Statement ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      WhileStatement ws = new WhileStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      
      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ws.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());

      
      //Test that a variable with a value before the while statement, that is given a value in the body of the while statement, still has a value afterwards
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ws.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should be assigned", vd2.hasValue());

     
      //Test that assignment is not allowed in the condition expression of the while
      te = new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      ws = new WhileStatement(JExprParser.NO_SOURCE_INFO, te, ts);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      
      ws.visit(_bbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot use an assignment expression in the condition expression of a while statement at any language level.  Perhaps you meant to compare two values with '=='", errors.get(0).getFirst());
      
      

    }
    
    public void testForWhileStatementOnly() {
      //Test that a boolean condition expression results in no error
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));

      
      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));      

      Statement ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      WhileStatement ws = new WhileStatement(JExprParser.NO_SOURCE_INFO, te, ts);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      assertEquals("Should return null", null, _bbtc.forWhileStatementOnly(ws, SymbolData.BOOLEAN_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());

      //Test that a non-boolean condition expression throws an error
      assertEquals("Should return null", null, _bbtc.forWhileStatementOnly(ws, SymbolData.INT_TYPE.getInstanceData(), SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "This while-statement's conditional expression must be a boolean value. Instead, its type is int", errors.get(0).getFirst());
 
      //Test "boolean" as a condition
      assertEquals("Should return null", null, _bbtc.forWhileStatementOnly(ws, SymbolData.BOOLEAN_TYPE, SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("There should be 2 error", 2, errors.size());
      assertEquals("Error message should be correct", "This while-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", errors.getLast().getFirst());
    }
    
    public void testForForStatementOnly() {
      
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));

      UnparenthesizedExpressionList sel = new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10))});
      ForStatement fs = new ForStatement(JExprParser.NO_SOURCE_INFO, sel, new NullLiteral(JExprParser.NO_SOURCE_INFO), new UnparenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), new EmptyStatement(JExprParser.NO_SOURCE_INFO));
  
      
      //Test that a boolean condition results in no error
      assertEquals("Should return null", null, _bbtc.forForStatementOnly(fs, SymbolData.INT_TYPE, SymbolData.BOOLEAN_TYPE.getInstanceData(), SymbolData.INT_TYPE, SymbolData.INT_TYPE));
      assertEquals("There should be no errors", 0, errors.size());
      
                   
      //Test that a non-boolean condition expression throws an error             
      assertEquals("Should return null", null, _bbtc.forForStatementOnly(fs, SymbolData.INT_TYPE, SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE, SymbolData.CHAR_TYPE));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "This for-statement's conditional expression must be a boolean value. Instead, its type is double", errors.get(0).getFirst());

      //Test "boolean" as the condition             
      assertEquals("Should return null", null, _bbtc.forForStatementOnly(fs, SymbolData.INT_TYPE, SymbolData.BOOLEAN_TYPE, SymbolData.INT_TYPE, SymbolData.CHAR_TYPE));
      assertEquals("Should be 2 error", 2, errors.size());
      assertEquals("The error message should be correct", "This for-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", errors.getLast().getFirst());
    }
    
    public void testForDoStatement() {
      //Test that a variable without a value before the do statement, that is given a value in the body of the while statement, still doesn't have a value afterwards
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));


      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));      

      Statement ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      DoStatement ds = new DoStatement(JExprParser.NO_SOURCE_INFO, ts, te);
      
      VariableData vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, false, null);
      
      MethodData md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      LinkedList<VariableData> vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ds.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertFalse("vd2 should not be assigned", vd2.hasValue());

      
      //Test that a variable with a value before the do statement, that is given a value in the body of the do statement, still has a value afterwards
      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ds.visit(_bbtc);
      assertTrue("vd1 should be assigned", vd1.hasValue());
      assertTrue("vd2 should be assigned", vd2.hasValue());

     
      //Test that assignment is not allowed in the condition expression of the do
      te = new PlusAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      ds = new DoStatement(JExprParser.NO_SOURCE_INFO, ts, te);

      vd1 = new VariableData("j", _packageMav, SymbolData.INT_TYPE, true, null);
      vd2 = new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, null);
      
      md1 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE,
                                     new VariableData[] {vd1}, new String[0], _sd1, null);
      
      vd1.setEnclosingData(md1);
      vd2.setEnclosingData(md1);

      md1.addVar(vd1);
      md1.addVar(vd2);
      
      vars = new LinkedList<VariableData>();
      vars.addLast(vd1);
      vars.addLast(vd2);
      _bbtc = new BodyTypeChecker(md1, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, vars, new LinkedList<Pair<SymbolData, JExpression>>());
      _bbtc._bodyData = md1;
      _bbtc._data = md1;
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      
      ds.visit(_bbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot use an assignment expression in the condition expression of a do statement at any language level", errors.get(0).getFirst());
      
    }
    
    public void testForDoStatementOnly() {
      Expression te = new LessThanExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
        new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));

      
      Statement assignStatement = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 10)));      

      Statement ts = new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignStatement}));
      DoStatement ds = new DoStatement(JExprParser.NO_SOURCE_INFO, ts, te);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));

      //Test that a boolean condition results in no error
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), _bbtc.forDoStatementOnly(ds, SymbolData.INT_TYPE, SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());

      //test that a non-boolean condition expression throws an error
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), _bbtc.forDoStatementOnly(ds, SymbolData.INT_TYPE, SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "This do-statement's conditional expression must be a boolean value. Instead, its type is double", errors.get(0).getFirst());

      //test "bool" as the condition
      assertEquals("Should return double", SymbolData.DOUBLE_TYPE.getInstanceData(), _bbtc.forDoStatementOnly(ds, SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.BOOLEAN_TYPE));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "This do-statement's conditional expression must be a boolean value. Instead, it is a class or interface name", errors.getLast().getFirst());
    }
    

   public void testForSwitchStatementOnly() {
     //if we did not see a default block, should return null
     SwitchStatement ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1), new SwitchCase[0]);
     assertEquals("Should return null--no default block", null, _bbtc.forSwitchStatementOnly(ss, 
                                                                                             SymbolData.CHAR_TYPE.getInstanceData(), 
                                                                                             new TypeData[] {SymbolData.INT_TYPE}, 
                                                                                             false));
     
     //if any of the blocks are KEEP_GOING, should return null
     assertEquals("Should return null--has a keep going block", null, _bbtc.forSwitchStatementOnly(ss, 
                                                                                             SymbolData.CHAR_TYPE.getInstanceData(), 
                                                                                             new TypeData[] {SymbolData.KEEP_GOING, SymbolData.INT_TYPE}, 
                                                                                             true));
     
     assertEquals("Should return null--has a keep going block", null, _bbtc.forSwitchStatementOnly(ss, 
                                                                                             SymbolData.CHAR_TYPE.getInstanceData(), 
                                                                                             new TypeData[] {SymbolData.INT_TYPE, SymbolData.KEEP_GOING}, 
                                                                                             true));
                  
     
     //if last block does not return, statement does not return
     assertEquals("Should return null--last block is null", null, _bbtc.forSwitchStatementOnly(ss, 
                                                                                             SymbolData.CHAR_TYPE.getInstanceData(), 
                                                                                             new TypeData[] {SymbolData.INT_TYPE, SymbolData.CHAR_TYPE, null}, 
                                                                                             true));
                                                                                             
     
     //if all 3 conditions are false, statement does return
     assertEquals("Should NOT return null", SymbolData.INT_TYPE.getInstanceData(), _bbtc.forSwitchStatementOnly(ss, 
                                                                                             SymbolData.CHAR_TYPE.getInstanceData(), 
                                                                                             new TypeData[] {SymbolData.INT_TYPE, SymbolData.CHAR_TYPE, null, SymbolData.CHAR_TYPE}, 
                                                                                             true));
                                                                                             
    }
 
    public void testForSwitchStatement() {
      _bbtc._vars.addLast(new VariableData("dan", _publicMav, SymbolData.INT_TYPE, true, _bbtc._bodyData));
      
      //assignment in switch expression should throw error
      SwitchStatement ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)), new SwitchCase[0]);
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "You cannot use an assignment expression in the switch expression of a switch statement at any language level.  Perhaps you meant to compare two values with '=='", errors.getLast().getFirst());
      
      //non int or char value in switch expression
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new DoubleLiteral(JExprParser.NO_SOURCE_INFO, 4.2), new SwitchCase[0]);
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should be 2 error", 2, errors.size());
      assertEquals("Error message should be correct", "The switch expression must be either an int or a char.  You have used a double", errors.getLast().getFirst());

      //two switch cases with the same label
      UnbracedBody emptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);

      LabeledCase l1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), emptyBody);
      LabeledCase l2 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), emptyBody);
      LabeledCase l3 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 7), emptyBody);

      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {l1, l2, l3});
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "You cannot have two switch cases with the same label 5", errors.getLast().getFirst());
      
      //two default cases
      DefaultCase dc1 = new DefaultCase(JExprParser.NO_SOURCE_INFO, emptyBody);
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {dc1, dc1});
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", "A switch statement can only have one default case", errors.getLast().getFirst());

      //x is assigned
      VariableData xData = new VariableData("x", _publicMav, SymbolData.INT_TYPE, false, _bbtc._bodyData);
      _bbtc._vars.addLast(xData);

      ExpressionStatement assignX = new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "x")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)));
      UnbracedBody returnBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignX, new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))});
      UnbracedBody breakBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignX, new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO)});
      UnbracedBody breakNoAssignBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO)});
      UnbracedBody fallThroughBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {assignX});
      UnbracedBody fallThroughNoAssignBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      
      SwitchCase c1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), returnBody);
      SwitchCase c2 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 6), breakBody);
      SwitchCase c3 = new DefaultCase(JExprParser.NO_SOURCE_INFO, breakBody);
      
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {c1, c2, c3});
      
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertTrue("x has been assigned", xData.hasValue());
      
      //x is assigned -- the first block falls through
      xData.lostValue();
      
      c1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), fallThroughNoAssignBody);
      c2 = new DefaultCase(JExprParser.NO_SOURCE_INFO, breakBody);
      c3 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 6), breakBody);
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {c1, c2, c3});
     
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertTrue("x has been assigned", xData.hasValue());
      
      //x is not assigned -- the second block does not fall through
      xData.lostValue();
      
      c1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), fallThroughNoAssignBody);
      c2 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 6), breakNoAssignBody);
      c3 = new DefaultCase(JExprParser.NO_SOURCE_INFO, breakBody);
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {c1, c2, c3});
     
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertFalse("x has not been assigned", xData.hasValue());
      
      //x is not assigned -- there is no default case
      xData.lostValue();
      
      c1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), fallThroughNoAssignBody);
      c2 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 6), fallThroughNoAssignBody);
      c3 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 7), breakBody);
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {c1, c2, c3});
     
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertFalse("x has not been assigned", xData.hasValue());
      
      //x is assigned -- the last case is always executed--but an error is added, because it falls through.
      xData.lostValue();
      
      c1 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), fallThroughNoAssignBody);
      c2 = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 6), fallThroughNoAssignBody);
      c3 = new DefaultCase(JExprParser.NO_SOURCE_INFO, fallThroughBody);
      ss = new SwitchStatement(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), new SwitchCase[] {c1, c2, c3});
     
      assertEquals("Should return null", null, ss.visit(_bbtc));
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("The error message should be correct", "You must end a non-empty switch case with a break or return statement at the Advanced level", errors.getLast().getFirst());
      assertTrue("x has been assigned", xData.hasValue());
      
    }
    
    public void testForLabeledCase() {
      symbolTable.put("java.lang.String", new SymbolData("java.lang.String"));
      UnbracedBody emptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      //Test a label that is okay
      LabeledCase lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new CharLiteral(JExprParser.NO_SOURCE_INFO, 'e'), emptyBody);
      assertEquals("Should return null", null, lc.visit(_bbtc));
      assertEquals("There should be no errors", 0, errors.size());

      lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 27), emptyBody);
      assertEquals("Should return null", null, lc.visit(_bbtc));
      assertEquals("There should be no errors", 0, errors.size());
      
      //Test that a braced body that returns something is handled correctly
      UnbracedBody nonEmptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))});
      lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 27), nonEmptyBody);
      TypeData result = lc.visit(_bbtc);
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), result);
      assertEquals("There should be no errors", 0, errors.size());
      
      
      //Test some that are not:
      
      //label that is a more complex expression: length greater than 1
      lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new PlusExpression(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 42)), emptyBody);
      assertEquals("Should return null", null, lc.visit(_bbtc));
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The labels of a switch statement must be constants.  You are using a more complicated expression of type int", errors.getLast().getFirst());

      //label that is a more complex expression: something other than a literal of length 1
      _bbtc._vars.addLast(new VariableData("dan", _publicMav, SymbolData.INT_TYPE, true, _bbtc._bodyData));
      lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "dan")), emptyBody);
      assertEquals("Should return null", null, lc.visit(_bbtc));
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "The labels of a switch statement must be constants.  You are using a more complicated expression of type int", errors.getLast().getFirst());
                         
      //and a literal whose type is not int or char
      lc = new LabeledCase(JExprParser.NO_SOURCE_INFO, new StringLiteral(JExprParser.NO_SOURCE_INFO, "hi!"), emptyBody);
      assertEquals("Should return null", null, lc.visit(_bbtc));
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "The labels of a switch statement must be constants of int or char type.  You specified a constant of type java.lang.String", errors.getLast().getFirst());

    }
    
    public void testForDefaultCase() {
      UnbracedBody emptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      UnbracedBody returnBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))});
      UnbracedBody breakBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO)});
      
      //an empty body
      DefaultCase dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, emptyBody);
      assertEquals("Should return null", null, dc.visit(_bbtc));
      assertEquals("There should be no errors", 0, errors.size());

      //a body with a return statement
      dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, returnBody);
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), dc.visit(_bbtc));
      assertEquals("There should be no errors", 0, errors.size());
       
      //a body with a break
      dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, breakBody);
      assertEquals("Should return KEEP_GOING", SymbolData.KEEP_GOING, dc.visit(_bbtc));
      assertEquals("There should be no errors", 0, errors.size());
    }
    
    public void testForSwitchCase() {
      UnbracedBody emptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      UnbracedBody returnBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5))});
      UnbracedBody breakBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO)});
      UnbracedBody nonEmptyBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new EmptyStatement(JExprParser.NO_SOURCE_INFO)});
     
      //empty body
      DefaultCase dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, emptyBody);
      assertEquals("Should return null", null, _bbtc.forSwitchCase(dc));
      assertEquals("There should be no errors", 0, errors.size());

      //return body
      dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, returnBody);
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), _bbtc.forSwitchCase(dc));
      assertEquals("There should be no errors", 0, errors.size());
       
      //break body
      dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, breakBody);
      assertEquals("Should return KEEP_GOING", SymbolData.KEEP_GOING, _bbtc.forSwitchCase(dc));
      assertEquals("There should be no errors", 0, errors.size());
      
      //non-empty body that does not return: fall-through
      dc = new DefaultCase(JExprParser.NO_SOURCE_INFO, nonEmptyBody);
      assertEquals("Should return null", null, _bbtc.forSwitchCase(dc));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "You must end a non-empty switch case with a break or return statement at the Advanced level", errors.getLast().getFirst());

    }

    public void testCreateANewInstanceOfMe() {
      //make sure that the correct type of visitor is returned
      BodyTypeChecker btc = _bbtc.createANewInstanceOfMe(_bbtc._bodyData, _bbtc._file, _bbtc._package, _bbtc._importedFiles, _bbtc._importedPackages, _bbtc._vars, _bbtc._thrown);
      assertTrue("Should be an instance of BodyTypeChecker", btc instanceof BodyTypeChecker);
      assertFalse("Should not be an instance of ConstructorBodyTypeChecker", btc instanceof ConstructorBodyTypeChecker);

    }
    
    public void testCheckDuplicateExceptions() {
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      Block b = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0]);
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], b);
      _bbtc.checkDuplicateExceptions(ntcs);
      _bbtc.checkDuplicateExceptions(tcfs);
      assertEquals("Should be no errors", 0, errors.size());
      
      UninitializedVariableDeclarator uvd1 = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Exception", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "e"));
      UninitializedVariableDeclarator uvd2 = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "RuntimeException", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "e"));
      UninitializedVariableDeclarator uvd3 = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "IOException", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "e"));

      FormalParameter fp1 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd1, false);
      FormalParameter fp2 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd2, false);
      FormalParameter fp3 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd3, false);

      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                      new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = new Symboltable();
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      llv._importedFiles.addLast("java.io.IOException");
      SymbolData e = llv.getSymbolData("java.lang.Exception", JExprParser.NO_SOURCE_INFO, true);
      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      SymbolData ioe = llv.getSymbolData("java.io.IOException", JExprParser.NO_SOURCE_INFO, true);
      
      symbolTable.put("java.lang.Exception", e);
      symbolTable.put("java.lang.RuntimeException", re);
      symbolTable.put("java.io.IOException", ioe);
      

      CatchBlock c1 = new CatchBlock(JExprParser.NO_SOURCE_INFO, fp1, b);
      CatchBlock c2 = new CatchBlock(JExprParser.NO_SOURCE_INFO, fp2, b);
      CatchBlock c3 = new CatchBlock(JExprParser.NO_SOURCE_INFO, fp3, b);
      _bbtc._importedFiles.addLast("java.io.IOException");
      
      //just one exception, no error
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[] {c1});
      _bbtc.checkDuplicateExceptions(ntcs);
      assertEquals("Should be no errors", 0, errors.size());
      
      //2nd exception is subclass of 1st exception: should throw error
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[]{c1, c2});
      _bbtc.checkDuplicateExceptions(ntcs);
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "Exception java.lang.RuntimeException has already been caught", errors.get(0).getFirst());

      //two exceptions, unrelated.  no error
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[]{c2, c3});
      _bbtc.checkDuplicateExceptions(ntcs);
      assertEquals("Should still be one error", 1, errors.size());

      //2nd and 3rd exceptions subclasses of 1st exception: should throw 2 errors
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[]{c1, c2, c3});
      _bbtc.checkDuplicateExceptions(ntcs);
      assertEquals("Should be three errors", 3, errors.size());
      assertEquals("2nd Error message should be correct", "Exception java.lang.RuntimeException has already been caught", errors.get(1).getFirst());
      assertEquals("3rd Error message should be correct", "Exception java.io.IOException has already been caught", errors.get(2).getFirst());
      
      //1st exception subclass of 2nd exception: should be no error
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[]{c2, c1});
      _bbtc.checkDuplicateExceptions(ntcs);
      assertEquals("Should still be three errors", 3, errors.size());
    }
    
    public void testTryCatchLeastRestrictiveType() {

      InstanceData[] sdArray = new InstanceData[] {SymbolData.BYTE_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData(), SymbolData.SHORT_TYPE.getInstanceData()};
      assertEquals("Should return long type", SymbolData.LONG_TYPE.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(SymbolData.LONG_TYPE.getInstanceData(), sdArray, null));
      assertEquals("Should return Object", "java.lang.Object", _bbtc.tryCatchLeastRestrictiveType(SymbolData.SHORT_TYPE.getInstanceData(), sdArray, SymbolData.BOOLEAN_TYPE.getInstanceData()).getName());
      assertEquals("Should return double type", SymbolData.DOUBLE_TYPE.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(SymbolData.SHORT_TYPE.getInstanceData(), sdArray, SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should return null", null, _bbtc.tryCatchLeastRestrictiveType(null, sdArray, null));
      assertEquals("Should return int type", SymbolData.INT_TYPE.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(SymbolData.SHORT_TYPE.getInstanceData(), sdArray, null));
      assertEquals("Should return long type", SymbolData.LONG_TYPE.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(null, sdArray, SymbolData.LONG_TYPE.getInstanceData()));
      
      sdArray = new InstanceData[] {null, SymbolData.INT_TYPE.getInstanceData()};
      assertEquals("Should return null", null, _bbtc.tryCatchLeastRestrictiveType(SymbolData.INT_TYPE.getInstanceData(), sdArray, null));
      assertEquals("Should return short", SymbolData.SHORT_TYPE.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(SymbolData.INT_TYPE.getInstanceData(), sdArray, SymbolData.SHORT_TYPE.getInstanceData()));
      
      SymbolData sd = new SymbolData("java.lang.Object");
      SymbolData sd2 = new SymbolData("java.lang.String");
      sd.setIsContinuation(false);
      sd2.setIsContinuation(false);
      symbolTable.put("java.lang.Object", sd);
      symbolTable.put("java.lang.String", sd2);
      sd2.setSuperClass(sd);
      
      assertEquals("Should return Object", sd.getInstanceData(), _bbtc.tryCatchLeastRestrictiveType(sd2.getInstanceData(), new InstanceData[]{sd.getInstanceData(), sd2.getInstanceData()}, null));
    }
    
    public void testHandleMethodInvocation() {
      //handleMethodInvocation(MethodData md, JExpression jexpr) 
      MethodData md = new MethodData("Fun", _publicMav, new TypeParameter[0], _sd1, 
                                     new VariableData[0], 
                                     new String[0], 
                                     _sd1,
                                     null);
      
      MethodData md2 = new MethodData("InTheSun", _publicMav, new TypeParameter[0], _sd1,
                                      new VariableData[0],
                                      new String[] {"java.lang.RuntimeException", "java.io.IOException"},
                                      _sd1,
                                      null);
                                      
      NullLiteral nl = new NullLiteral(JExprParser.NO_SOURCE_INFO);

      _bbtc._importedFiles.addLast("java.io.IOException");
      
      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                      new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = new Symboltable();
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      llv._importedFiles.addLast("java.io.IOException");


      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      SymbolData ioe = llv.getSymbolData("java.io.IOException", JExprParser.NO_SOURCE_INFO, true);
      
      symbolTable.put("java.lang.RuntimeException", re);
      symbolTable.put("java.io.IOException", ioe);

      
      _bbtc.handleMethodInvocation(md, nl);
      assertEquals("There should be no exceptions in _thrown", 0, _bbtc._thrown.size());
      
      _bbtc.handleMethodInvocation(md2, nl);
      assertEquals("There should be 2 exceptions in _thrown", 2, _bbtc._thrown.size());
      assertEquals("The first exception should be java.lang.RuntimeException", re, _bbtc._thrown.get(0).getFirst());
      assertEquals("The second exception should be java.lang.IOException", ioe, _bbtc._thrown.get(1).getFirst());
    }
    
    public void testForThrowStatement() {
      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                      new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = new Symboltable();
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      symbolTable.put("java.lang.RuntimeException", re);
      
      VariableData vd = new VariableData("myException", _publicMav, re, true, _bbtc._bodyData);
      _bbtc._vars.addLast(vd);
      Expression e = new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "myException"));
      ThrowStatement ts = new ThrowStatement(JExprParser.NO_SOURCE_INFO, e);
      
      assertEquals("Should return EXCEPTION", SymbolData.EXCEPTION.getInstanceData(), ts.visit(_bbtc));
      
      assertEquals("There should be 1 exception in _thrown", 1, _bbtc._thrown.size());
      assertEquals("The exception should be java.lang.RuntimeException", re, _bbtc._thrown.get(0).getFirst());

    }
    
    public void testMakeSureCaughtStuffWasThrown() {
      //TryCatchStatement that, SymbolData[] caught_array, LinkedList<Pair<SymbolData, JExpression>> thrown) {
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      Block b = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);

      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[] {new CatchBlock(JExprParser.NO_SOURCE_INFO,  param, b)});
      SymbolData javaLangThrowable = new SymbolData("java.lang.Throwable");
      _bbtc.symbolTable.put("java.lang.Throwable", javaLangThrowable);
      SymbolData exception = new SymbolData("my.crazy.exception");
      exception.setSuperClass(javaLangThrowable);
      SymbolData exception2 = new SymbolData("A&M.beat.Rice.in.BaseballException");
      exception2.setSuperClass(javaLangThrowable);
      SymbolData exception3 = new SymbolData("aegilha");
      exception3.setSuperClass(javaLangThrowable);
      LinkedList<Pair<SymbolData, JExpression>> thrown = new LinkedList<Pair<SymbolData, JExpression>>();

      
      _bbtc.makeSureCaughtStuffWasThrown(ntcs, new SymbolData[0], thrown);
      assertEquals("There should be no errors", 0, errors.size());
      
      Pair<SymbolData, JExpression> p = new Pair<SymbolData, JExpression>(exception, ntcs);
      thrown.addLast(p);
      _bbtc.makeSureCaughtStuffWasThrown(ntcs, new SymbolData[]{exception}, thrown);
      assertEquals("There should still be no errors", 0, errors.size());
      
      thrown.remove(p);
      
      _bbtc.makeSureCaughtStuffWasThrown(ntcs, new SymbolData[] { exception2}, thrown);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The exception A&M.beat.Rice.in.BaseballException is never thrown in the body of the corresponding try block", errors.get(0).getFirst());
    }
    
    public void testIsCheckedException() {
      SymbolData th = new SymbolData("java.lang.Throwable");
      th.setIsContinuation(false);
      SymbolData r = new SymbolData("java.lang.Error");
      r.setIsContinuation(false);
      r.setSuperClass(th);
      SymbolData ex = new SymbolData("java.lang.Exception");
      ex.setIsContinuation(false);
      ex.setSuperClass(th);
      SymbolData re = new SymbolData("java.lang.RuntimeException");
      re.setIsContinuation(false);
      re.setSuperClass(ex);
      symbolTable.put("java.lang.Throwable", th);
      symbolTable.put("java.lang.RuntimeException", re);
      symbolTable.put("java.lang.Error", r);
      symbolTable.put("java.lang.Exception", ex);
      SymbolData e1 = new SymbolData("exception1");
      e1.setSuperClass(ex);
      SymbolData e2 = new SymbolData("exception2");
      e2.setSuperClass(re);
      SymbolData e3 = new SymbolData("exception3");
      e3.setSuperClass(r);
      
      NullLiteral nl = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      
      assertTrue("Does not subclass RuntimeException or Error", _bbtc.isCheckedException(e1, nl));
      assertFalse("Subclasses java.lang.RuntimeException", _bbtc.isCheckedException(e2, nl));
      assertFalse("Subclasses java.lang.Error", _bbtc.isCheckedException(e3, nl));
      
    }
    
    public void testIsUncheckedException() {
      //Check that extending RuntimeException or Error works as expected
      SymbolData th = new SymbolData("java.lang.Throwable");
      th.setIsContinuation(false);
      SymbolData r = new SymbolData("java.lang.Error");
      r.setIsContinuation(false);
      r.setSuperClass(th);
      SymbolData ex = new SymbolData("java.lang.Exception");
      ex.setIsContinuation(false);
      ex.setSuperClass(th);
      SymbolData re = new SymbolData("java.lang.RuntimeException");
      re.setIsContinuation(false);
      re.setSuperClass(ex);
      symbolTable.put("java.lang.Throwable", th);
      symbolTable.put("java.lang.RuntimeException", re);
      symbolTable.put("java.lang.Error", r);
      symbolTable.put("java.lang.Exception", ex);

      SymbolData e1 = new SymbolData("exception1");
      e1.setIsContinuation(false);
      e1.setSuperClass(ex);
      symbolTable.put("exception1", e1);
      SymbolData e2 = new SymbolData("exception2");
      e2.setSuperClass(re);
      SymbolData e3 = new SymbolData("exception3");
      e3.setSuperClass(r);
      SymbolData e4 = new SymbolData("exception4");
      e4.setSuperClass(e1);
      
      NullLiteral nl = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      
      assertTrue("Does not subclass RuntimeException or Error or anything in the method data", _bbtc.isUncaughtCheckedException(e1, nl));
      assertFalse("Subclasses java.lang.RuntimeException", _bbtc.isUncaughtCheckedException(e2, nl));
      assertFalse("Subclasses java.lang.Error", _bbtc.isUncaughtCheckedException(e3, nl));
      
      //What if you throw something the method data announces that it throws?
      _bbtc._bodyData.getMethodData().setThrown(new String[] {"exception1"});
      assertFalse("Is in method data", _bbtc.isUncaughtCheckedException(e1, nl));
      
      assertFalse("Superclass is in method data", _bbtc.isUncaughtCheckedException(e4, nl));
      
      
    }
    
    public void testHandleUncheckedException() {
      JExpression j = new SimpleMethodInvocation(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i"))}));
      
      _bbtc.handleUncheckedException(new SymbolData("i.have.a.shoe"), j);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The method myMethod is declared to throw the exception i.have.a.shoe which needs to be caught or declared to be thrown", errors.get(0).getFirst()); 
      Expression e = new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "myException"));
      j = new ThrowStatement(JExprParser.NO_SOURCE_INFO, e);
      _bbtc.handleUncheckedException(new SymbolData("you.have.a.pot"), j);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "This statement throws the exception you.have.a.pot which needs to be caught or declared to be thrown", errors.get(1).getFirst());

    }
    
    public void testCompareThrownAndCaught() {
      JExpression j = new SimpleMethodInvocation(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i"))}));
      
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      Block b = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);

      PrimitiveType intt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter param = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, intt, new Word(JExprParser.NO_SOURCE_INFO, "j")), false);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[] {new CatchBlock(JExprParser.NO_SOURCE_INFO,  param, b)});

      SymbolData javaLangThrowable = new SymbolData("java.lang.Throwable");
      _bbtc.symbolTable.put("java.lang.Throwable", javaLangThrowable);
      SymbolData exception = new SymbolData("my.crazy.exception");
      exception.setSuperClass(javaLangThrowable);
      SymbolData exception2 = new SymbolData("A&M.beat.Rice.in.BaseballException");
      exception2.setSuperClass(javaLangThrowable);
      SymbolData exception3 = new SymbolData("aegilha");
      exception3.setSuperClass(exception2);
      SymbolData[] caught_array = new SymbolData[] { exception, exception2 };
      LinkedList<Pair<SymbolData, JExpression>> thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      thrown.addLast(new Pair<SymbolData, JExpression>(exception, j));
      thrown.addLast(new Pair<SymbolData, JExpression>(exception2, ntcs));
      thrown.addLast(new Pair<SymbolData, JExpression>(exception3, ntcs));
      
      _bbtc.compareThrownAndCaught(ntcs, caught_array, thrown);
      assertEquals("There should be no errors", 0, errors.size());
      
      _bbtc.compareThrownAndCaught(ntcs, new SymbolData[] {exception2}, thrown);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The method myMethod is declared to throw the exception my.crazy.exception which needs to be caught or declared to be thrown", errors.get(0).getFirst());

    }
    
    public void testForBracedBody() {
      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                      new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = new Symboltable();
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      SymbolData eb = llv.getSymbolData("java.util.prefs.BackingStoreException", JExprParser.NO_SOURCE_INFO, true);
      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      symbolTable = llv.symbolTable;

      
      //Make sure it is okay to have something else other than an uncaught exception in a braced body.
      BracedBody plainBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO)});
      plainBody.visit(_bbtc);
      assertEquals("There should be no errors", 0, errors.size());

      //Make sure it is okay to throw a Runtime Exception in a braced body, without catching it.
      BracedBody runtimeBB = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
                           new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.lang.RuntimeException", 
                                                                 new Type[0]), 
                                                             new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0])))});
      runtimeBB.visit(_bbtc);
      assertEquals("There should be no errors", 0, errors.size());
      
      
      //Make sure it is okay to have a uncaught exception in a braced body, if the method is declared to throw it.
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
        new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.util.prefs.BackingStoreException", 
                                                                 new Type[0]), 
                                          new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[] {new StringLiteral(JExprParser.NO_SOURCE_INFO, "wee")})))});

      _bbtc._bodyData.getMethodData().setThrown(new String[]{"java.util.prefs.BackingStoreException"});
      _bbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();

      bb.visit(_bbtc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      //make sure it is not okay to have a unchecked exception in a braced body if the method is not declared to throw it.
      _bbtc._bodyData.getMethodData().setThrown(new String[0]);
      _bbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      
      bb.visit(_bbtc);

      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(0).getFirst());
      
      //make sure that it is not okay to invoke a method that throws an exception if the enclosing method is not declared to throw it.
      MethodData badMethod = new MethodData("throwsException", 
                                            _packageMav, 
                                            new TypeParameter[0], 
                                            SymbolData.INT_TYPE, 
                                            new VariableData[0],
                                            new String[] {"java.util.prefs.BackingStoreException"},
                                            _sd1,
                                            null);
      _bbtc._bodyData.getSymbolData().addMethod(badMethod);                                      
      _bbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      BracedBody bbMethod = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleMethodInvocation(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "throwsException"), new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0])))});
      bbMethod.visit(_bbtc);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", "The method throwsException is declared to throw the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.getLast().getFirst());
      
      //if enclosing method is delared to throw it, should be okay:
      _bbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      bbMethod.visit(_bbtc);
      assertEquals("There should still be two errors", 2, errors.size());
      

      //make sure that it is not okay to invoke a constructor that throws an exception if the enclosing method is not declared to throw it
      _bbtc._bodyData.getMethodData().setThrown(new String[0]);
      _sd3.setMav(_publicMav);
      _sd3.setIsContinuation(false);
      _bbtc.symbolTable.put(_sd3.getName(), _sd3);
      MethodData constructor = new MethodData("zebra", _publicMav, new TypeParameter[0], _sd3, new VariableData[0], new String[] {"java.util.prefs.BackingStoreException"}, _sd3, null);
      _sd3.addMethod(constructor);
      BracedBody bbConstr = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[]{new ExpressionStatement(JExprParser.NO_SOURCE_INFO, new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, _sd3.getName(), new Type[0]), new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0])))});
      _bbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      bbConstr.visit(_bbtc);
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("The error message should be correct", "The constructor for the class zebra is declared to throw the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown.", errors.getLast().getFirst());
      

      //if enclosing method is delared to throw it, should be okay:
      _bbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      bbConstr.visit(_bbtc);
      assertEquals("There should still be three errors", 3, errors.size());

      
    }
    
    public void testForTryCatchFinallyStatement() {
      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                      new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = symbolTable;
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      SymbolData eb = llv.getSymbolData("java.util.prefs.BackingStoreException", JExprParser.NO_SOURCE_INFO, true);
      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
        new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.util.prefs.BackingStoreException", 
                                                                 new Type[0]), 
                                          new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[]{new StringLiteral(JExprParser.NO_SOURCE_INFO, "arg")})))});
      
      Block b = new Block(JExprParser.NO_SOURCE_INFO, bb);
      Block b2 = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);
    
      _bbtc._bodyData.getMethodData().setThrown(new String[0]);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));


      
      //Test that an empty finally block behaves as expected
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], b2);
      tcfs.visit(_bbtc);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(0).getFirst());
            
      //Test that a finally block where only one branch ends abruptly acts as expected
      IfThenElseStatement ites1 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new EmptyStatement(JExprParser.NO_SOURCE_INFO));
                                                          
      BracedBody bb2 = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {ites1});
      TryCatchFinallyStatement tcfs2 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], new Block(JExprParser.NO_SOURCE_INFO, bb2));
      _bbtc._bodyData.removeAllBlocks();
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();
      
      TypeData result = tcfs2.visit(_bbtc);
      assertEquals("Should return Exception", SymbolData.EXCEPTION.getInstanceData(), result);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(1).getFirst());
                                      
      //Test that a finally block where both branches end abruptly acts as expected (break)
      IfThenElseStatement ites2 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, false),
                                                          new ValueReturnStatement(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 4)),
                                                          new UnlabeledBreakStatement(JExprParser.NO_SOURCE_INFO));
                                      
      BracedBody bb3 = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {ites2});
      TryCatchFinallyStatement tcfs3 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], new Block(JExprParser.NO_SOURCE_INFO, bb3));

      _bbtc._bodyData.removeAllBlocks();
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      assertEquals("Should return Exception", SymbolData.EXCEPTION.getInstanceData(), tcfs3.visit(_bbtc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      

      //Test that a finally block where both branches end abruptly acts as expected (void return)
      _bbtc._bodyData.getMethodData().setReturnType(SymbolData.VOID_TYPE);
      IfThenElseStatement ites3 = new IfThenElseStatement(JExprParser.NO_SOURCE_INFO,
                                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true),
                                                          new VoidReturnStatement(JExprParser.NO_SOURCE_INFO),
                                                          new VoidReturnStatement(JExprParser.NO_SOURCE_INFO));
                                                          
      BracedBody bb4 = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {ites3});
      TryCatchFinallyStatement tcfs4 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], new Block(JExprParser.NO_SOURCE_INFO, bb4));
      _bbtc._bodyData.removeAllBlocks();
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();
      
      result = tcfs4.visit(_bbtc);
      assertEquals("Should return SymbolData.VOID_TYPE", SymbolData.VOID_TYPE.getInstanceData(), result);
      
      assertEquals("Should still still be 2 errors", 2, errors.size());

      _bbtc._bodyData.getMethodData().setReturnType(SymbolData.INT_TYPE);


      //      try {
      //         try b
      //         finally b2 }
      //      finally b2
      
      //Test that an error is thrown if a try catch statement is nested, an error is thrown but not caught, and finally doesn't return
      TryCatchFinallyStatement inner = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], b2);
      TryCatchFinallyStatement nested = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, 
                                             new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner})), 
                                             new CatchBlock[0], b2);
                                             
      BlockData innerBD = new BlockData(_bbtc._bodyData);
      innerBD.addBlock(new BlockData(innerBD));
      innerBD.addBlock(new BlockData(innerBD));

      _bbtc._bodyData.removeAllBlocks();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      _bbtc._bodyData.resetBlockIterator();
      
      nested.visit(_bbtc);
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(2).getFirst());
                                      
      //Test that no error is thrown if the exception is caught
      UninitializedVariableDeclarator uvd1 = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.util.prefs.BackingStoreException", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "e"));
      FormalParameter fp1 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd1, false);
      BlockData catchBD = new BlockData(_bbtc._bodyData);
      VariableData fpData = new VariableData("e", null, eb, true, catchBD);
      catchBD.addVar(fpData);

      CatchBlock cb = new CatchBlock(JExprParser.NO_SOURCE_INFO, fp1, b2);
      TryCatchFinallyStatement nested2 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner})), new CatchBlock[] {cb}, b2);
      _bbtc._bodyData.removeAllBlocks();
      innerBD.resetBlockIterator();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(catchBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      nested2.visit(_bbtc);
      assertEquals("There should still be 3 errors", 3, errors.size());
      
      //Test that no error is thrown if it is a runtime exception
      BracedBody reb = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
        new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.lang.RuntimeException", 
                                                                 new Type[0]), 
                                         new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0])))});
      
      //      try {
      //        try {
      //          reb
      //        }
      //        finally b2
      //      }
      //      finally b2
      //      }
    
      TryCatchFinallyStatement inner3 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, 
                                                                     new Block(JExprParser.NO_SOURCE_INFO, reb), new CatchBlock[0], b2);
      TryCatchFinallyStatement nested3 = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, 
                                                                      new Block(JExprParser.NO_SOURCE_INFO, 
                                                                                new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner3})), 
                                                                      new CatchBlock[0], b2);
                                                                                
      _bbtc._bodyData.removeAllBlocks();
      innerBD.resetBlockIterator();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      nested3.visit(_bbtc);
      assertEquals("There should still be 3 errors", 3, errors.size());
      
      //Test that no error is thrown if the method is declared to throw it
      _bbtc._bodyData.getMethodData().setThrown(new String[]{"java.util.prefs.BackingStoreException"});
      innerBD.resetBlockIterator();
      _bbtc._bodyData.resetBlockIterator();
      nested.visit(_bbtc);
      assertEquals("There should still be 3 errors!", 3, errors.size());
    }

    public void testForNormalTryCatchStatement() {
      LanguageLevelVisitor llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                                          new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      llv.symbolTable = symbolTable;
      llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();

      SymbolData eb = llv.getSymbolData("java.util.prefs.BackingStoreException", JExprParser.NO_SOURCE_INFO, true);
      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", JExprParser.NO_SOURCE_INFO, true);
      
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
        new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.util.prefs.BackingStoreException", 
                                                                 new Type[0]), 
                                          new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[]{new StringLiteral(JExprParser.NO_SOURCE_INFO, "boo")})))});
      
      Block b = new Block(JExprParser.NO_SOURCE_INFO, bb);
      Block b2 = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);
    
      _bbtc._bodyData.getMethodData().setThrown(new String[0]);
      
      
//       Test that an empty finally block behaves as expected
      NormalTryCatchStatement tcfs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0]);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      tcfs.visit(_bbtc);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(0).getFirst());
            
//      Test that an error is thrown if a try catch statement is nested, an error is thrown but not caught, and finally doesn't return
      NormalTryCatchStatement inner = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0]);
      NormalTryCatchStatement nested = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner})), new CatchBlock[0]);
      
      BlockData innerBD = new BlockData(_bbtc._bodyData);
      innerBD.addBlock(new BlockData(innerBD));
      innerBD.addBlock(new BlockData(innerBD));

      _bbtc._bodyData.removeAllBlocks();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      
      _bbtc._bodyData.resetBlockIterator();

      nested.visit(_bbtc);
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "This statement throws the exception java.util.prefs.BackingStoreException which needs to be caught or declared to be thrown", errors.get(1).getFirst());
                                      
//      Test that no error is thrown if the exception is caught
      UninitializedVariableDeclarator uvd1 = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.util.prefs.BackingStoreException", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "e"));
      FormalParameter fp1 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd1, false);
      BlockData catchBD = new BlockData(_bbtc._bodyData);
      VariableData fpData = new VariableData("e", null, eb, true, catchBD);
      catchBD.addVar(fpData);
     

      CatchBlock cb = new CatchBlock(JExprParser.NO_SOURCE_INFO, fp1, b2);
      NormalTryCatchStatement nested2 = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner})), new CatchBlock[] {cb});

      _bbtc._bodyData.removeAllBlocks();
      innerBD.resetBlockIterator();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(catchBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      nested2.visit(_bbtc);
      assertEquals("There should still be 2 errors", 2, errors.size());
      
//      Test that no error is thrown if it is a runtime exception
      BracedBody reb = new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                     new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
        new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                         new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                 "java.lang.RuntimeException", 
                                                                 new Type[0]), 
                                         new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0])))});
      
      NormalTryCatchStatement inner3 = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, new Block(JExprParser.NO_SOURCE_INFO, reb), new CatchBlock[0]);
      NormalTryCatchStatement nested3 = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, new Block(JExprParser.NO_SOURCE_INFO, new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {inner3})), new CatchBlock[0]);
      
      _bbtc._bodyData.removeAllBlocks();
      innerBD.resetBlockIterator();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      
      nested3.visit(_bbtc);
      assertEquals("There should still be 2 errors", 2, errors.size());
      
      // Test that no error is thrown if the method is declared to throw it
      _bbtc._bodyData.getMethodData().setThrown(new String[]{"java.util.prefs.BackingStoreException"});
      _bbtc._bodyData.removeAllBlocks();
      innerBD.resetBlockIterator();
      _bbtc._bodyData.addBlock(innerBD);
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.addBlock(new BlockData(_bbtc._bodyData));
      _bbtc._bodyData.resetBlockIterator();

      nested.visit(_bbtc);
      assertEquals("There should still be 2 errors!", 2, errors.size());
    }
  }
}