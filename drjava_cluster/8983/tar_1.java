/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

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
import java.lang.reflect.Type;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This abstract tree visitor checks the typing rules and loads
 * the classes, fields and methods. The nature of the methods in its child classes is dependent upon 
 * which runtime environment version is being used.
 * 
 * NOTE: This class replaces the class TypeChecker. The static method makeTypeChecker should be used
 * whenever a new TypeChecker is instantiated.
 *
 */

public abstract class AbstractTypeChecker extends VisitorObject<Type> {
  
  /** The context */
  protected Context<Type> context;
  
  /** Creates a new name visitor
   *  @param ctx the context
   */
  public AbstractTypeChecker(Context<Type> ctx) { context = ctx; }
  
  /** Builds an appropriate type checker object. */
  public static AbstractTypeChecker makeTypeChecker(Context<Type> ctx) {
    if (!TigerUtilities.isTigerEnabled()) return new TypeChecker14(ctx);
    return new TypeChecker15(ctx);
  }
  
  /**
   * Visits a PackageDeclaration
   * @param node the node to visit
   * @return null
   */
  public Type visit(PackageDeclaration node) {
    context.setCurrentPackage(node.getName());
    return null;
  }
  
  //ImportDeclaration does not currently support static import /**/
  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   */
  public Type visit(ImportDeclaration node) {
    if(node.isStatic()) {
      staticImportHandler(node);
    }
    else {
      // Declare the package or class importation
      if (node.isPackage()) {
        context.declarePackageImport(node.getName());
      } else {
        try {
          context.declareClassImport(node.getName());
        } catch (ClassNotFoundException e) {
          throw new CatchedExceptionError(e, node);
        }
      }
    }
    return null;
  }
  
  /**
   * Checks if the ImportDeclaration node is a static import and throws an exception if static import is not supported in the current version of the runtime environment
   * @param node the ImportDeclaration node being checked
   */
  protected abstract void staticImportHandler(ImportDeclaration node);
  
  /**
   * Visits a WhileStatement first by checking the 
   * condition type and unboxing if necessary then by 
   * recurring to its body.
   * @param node the node to visit
   */
  public Type visit(WhileStatement whileStmt) {
    // Check the condition
    Expression exp = whileStmt.getCondition();
    
    Type type = exp.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", whileStmt);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "exp.booleanValue();"
      whileStmt.setCondition(_unbox(exp, type));
    }
    
    whileStmt.getBody().acceptVisitor(this);
    return null;
  }
  
  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   */
  public abstract Type visit(ForEachStatement node);
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Type visit(ForStatement node) {
    // Enter a new scope
    context.enterScope();
    
    List<Node> l;
    // Check the statements
    if ((l = node.getInitialization()) != null) {
      checkList(l);
    }
    
    Expression cond = node.getCondition();
    if (cond != null) {
      
      Type type = cond.acceptVisitor(this);
      if (type != boolean.class && type != Boolean.class) {
        throw new ExecutionError("condition.type", node);
      }
      
      // Auto unbox: Boolean->boolean
      if (type == Boolean.class) {
        // add method call on expression:
        //   "cond.booleanValue();"
        node.setCondition(_unbox(cond, type));
      }
    }
    
    if ((l = node.getUpdate()) != null) {
      checkList(l);
    }
    
    node.getBody().acceptVisitor(this);
    
    // Leave the current scope and store the defined variables
    // (a map of String-Class<?> mappings) in the "variables" property
    node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
    return null;
  }
  
  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Type visit(DoStatement node) {
    node.getBody().acceptVisitor(this);
    
    Expression exp = node.getCondition();
    Type type = exp.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "exp.booleanValue();"
      node.setCondition(_unbox(exp, type));
    }
    return null;
  }
  
  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Type visit(SwitchStatement node) {
    // Visits the components of this node
    Expression exp = node.getSelector();
    Type c1 = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    
    if (c != char.class      && c != byte.class && c != short.class && c != int.class  && 
        c != Character.class && c != Byte.class && c != Short.class && c != Integer.class &&
        // (TigerUtilities.isTigerEnabled() && (c.getSuperclass() != Class.forName("java.lang.Enum")))) {
        !TigerUtilities.isEnum(c)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("selector.type", node);
    }
    // unbox it if needed
    if (c == Character.class || c == Byte.class || c == Short.class || c == Integer.class) {
      node.setSelector(_unbox(exp, c));
    }
    
    // Check the type of the case labels
    Iterator<SwitchBlock> it = node.getBindings().iterator();
    while (it.hasNext()) {
      SwitchBlock sb = it.next();
      sb.acceptVisitor(this);
      exp = sb.getExpression();
      if (exp != null) {
        Type lc1 = NodeProperties.getType(exp);
        Class<?> lc = (Class<?>)lc1;
        if (lc != char.class &&  lc != byte.class &&
            lc != short.class && lc != int.class &&
            //  (TigerUtilities.isTigerEnabled() && (lc.getSuperclass() != Class.forName("java.lang.Enum")))) {
            (TigerUtilities.isTigerEnabled() && !TigerUtilities.isEnum(lc))) {
          node.setProperty(NodeProperties.ERROR_STRINGS,
                           new String[] { lc.getName() });
          throw new ExecutionError("switch.label.type", node);
        }
        if (c != lc) {
          Number n = null;
          if (exp.hasProperty(NodeProperties.VALUE)) {
            Object cst = exp.getProperty(NodeProperties.VALUE);
            if (lc == char.class) {
              n = new Integer(((Character)cst).charValue());
            } else {
              n = (Number)cst;
            }
          }
          if (c == byte.class) {
            if (exp.hasProperty(NodeProperties.VALUE)) {
              if (n.byteValue() != n.intValue()) {
                node.setProperty(NodeProperties.ERROR_STRINGS,
                                 new String[] { c.getName() });
                throw new ExecutionError
                  ("switch.label.type", node);
              }
            } else {
              throw new ExecutionError("switch.label.type", node);
            }
          } else if (c == short.class || c == char.class) {
            if (exp.hasProperty(NodeProperties.VALUE)) {
              if (n.shortValue() != n.intValue()) {
                node.setProperty(NodeProperties.ERROR_STRINGS,
                                 new String[] { c.getName() });
                throw new ExecutionError
                  ("switch.label.type", node);
              }
            } else if (lc == int.class) {
              node.setProperty(NodeProperties.ERROR_STRINGS,
                               new String[] { c.getName() });
              throw new ExecutionError("switch.label.type", node);
            }
          }
        }
      }
    }
    
    return null;
  }
  
  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public Type visit(SwitchBlock node) {
    Expression exp = node.getExpression();
    if (exp != null) {
      exp.acceptVisitor(this);
    }
    List<Node> l = node.getStatements();
    if (l != null) {
      checkList(l);
    }
    return null;
  }
  
  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Type visit(LabeledStatement node) {
    node.getStatement().acceptVisitor(this);
    return null;
  }
  
  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Type visit(TryStatement node) {
    node.getTryBlock().acceptVisitor(this);
    Iterator<CatchStatement> it = node.getCatchStatements().iterator();
    while (it.hasNext()) {
      it.next().acceptVisitor(this);
    }
    Node n = node.getFinallyBlock();
    if (n != null) {
      n.acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public Type visit(CatchStatement node) {
    // Enter a new scope
    context.enterScope();
    
    Type c1 = node.getException().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    if (!Throwable.class.isAssignableFrom(c)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("catch.type", node);
    }
    
    node.getBlock().acceptVisitor(this);
    
    // Leave the current scope
    context.leaveScope();
    node.setProperty(NodeProperties.TYPE, c);
    return null;
  }
  
  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Type visit(ThrowStatement node) {
    Type c1 = node.getExpression().acceptVisitor(this);
    Class<?> c = (Class<?>)c1; 
    if (!Throwable.class.isAssignableFrom(c)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("throw.type", node);
    }
    return null;
  }
  
  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Type visit(ReturnStatement node) {
    Expression e = node.getExpression();
    if (e != null) {
      e.acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits an IfThenStatement
   * @param node the node to visit
   */
  public Type visit(IfThenStatement node) {
    Expression cond = node.getCondition();
    
    // Check the condition
    Type type = cond.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "cond.booleanValue();"
      node.setCondition(_unbox(cond, type));
    }
    
    node.getThenStatement().acceptVisitor(this);
    return null;
  }
  
  /**
   * Visits an IfThenElseStatement
   * @param node the node to visit
   */
  public Type visit(IfThenElseStatement node) {
    Expression cond = node.getCondition();
    
    // Check the condition
    Type type = cond.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "cond.booleanValue();"
      node.setCondition(_unbox(cond, type));
    }
    
    node.getThenStatement().acceptVisitor(this);
    node.getElseStatement().acceptVisitor(this);
    return null;
  }
  
  /**
   * Visits an AssertStatement
   * @param node the node to visit
   */
  public Type visit(AssertStatement node) {
    Expression cond = node.getCondition();
    
    //Check the condition
    Type type = cond.acceptVisitor(this);
    if(type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox; Boolean->boolean
    if(type == Boolean.class) {
      //add method call on expression;
      // "cond.booleanValue();"
      node.setCondition(_unbox(cond, type));
    }
    
    //Check the failure string
    Expression failString = node.getFailString();
    if(failString != null) {
      Type type2 = failString.acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Type visit(SynchronizedStatement node) {
    // Check the lock
    Type c1 = node.getLock().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    if (c.isPrimitive()) {
      throw new ExecutionError("lock.type", node);
    }
    
    node.getBody().acceptVisitor(this);
    return null;
  }
  
  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public Type visit(Literal node) {
    // Set the properties of the node
    Type c = node.getType();
    node.setProperty(NodeProperties.VALUE, node.getValue());
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /** Visits a VariableDeclaration
   *  @param node the node to visit
   */
  public Type visit(VariableDeclaration node) {
    // Define the variable
    Type lc = node.getType().acceptVisitor(this);
    if (node.isFinal()) context.defineConstant(node.getName(), lc);
    else context.define(node.getName(), lc);
    
    // Get the type of the initializer
    Expression init = node.getInitializer();
    if (init != null) {
      Type rc = init.acceptVisitor(this);
      Expression exp = checkAssignmentStaticRules(lc, rc, node, init);
      node.setInitializer(exp);
    }
    
    return null;
  }
  
//  /**
//   * This method visits a variable declaration without actually defining
//   * anything in the context.  This method was added to allow other objects
//   * to type check a variable declaration without actually creating a new
//   * binding in the context.  This behavior was added so that when a
//   * variable declaration has some error in it, the declaration would not
//   * make it into the context. (this method should be run on an individual 
//   * variable declaration before the type checker visitor is formally 
//   * executed on the entire AST)
//   * @param node the variable declaration to check
//   */
//  public void preCheckVariableDeclaration(VariableDeclaration node) {
//    Class<?> lc = node.getType().acceptVisitor(this);
//    Expression init = node.getInitializer();
//    if (init != null) {
//      Class<?> rc = init.acceptVisitor(this);
//      // this call to checkAssignmentStaticRules is not
//      // intended to mutate the AST for autoboxing/unboxing
//      checkAssignmentStaticRules(lc, rc, node, init);
//    }
//  }
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public Type visit(BlockStatement node) {
    // Enter a new scope
    context.enterScope();
    
    // Do the type checking of the nested statements
    checkList(node.getStatements());
    
    // Leave the current scope and store the defined variables
    // (a map of String-Class<?> mappings) in the "variables" property
    node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
    
    return null;
  }
  
  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  public Type visit(ObjectFieldAccess node) {
    // Visit the expression
    Type c1 = node.getExpression().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    
    // Load the field object
    if (!c.isArray()) {
      Field f = null;
      try {
        f = context.getField(c, node.getFieldName());
      } catch (Exception e) { throw new CatchedExceptionError(e, node); }
      
      // Set the node properties
      c = f.getType();
      node.setProperty(NodeProperties.FIELD, f);
      node.setProperty(NodeProperties.TYPE, c);
      node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
      return c;
      
    } else {
      if (!node.getFieldName().equals("length")) {
        String s0 = "length";
        String s1 = c.getComponentType().getName() + " array";
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
        throw new ExecutionError("no.such.field", node);
      }
      node.setProperty(NodeProperties.TYPE,  int.class);
      node.setProperty(NodeProperties.MODIFIER, new InvalidModifier(node));
      return int.class;
    }
  }
  
  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public Type visit(SuperFieldAccess node) {
    Field f = null;
    try {
      f = context.getSuperField(node, node.getFieldName());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
    
    // Set the node properties
    node.setProperty(NodeProperties.FIELD, f);
    Type c;
    node.setProperty(NodeProperties.TYPE,  c = f.getType());
    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
  }
  
  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public Type visit(StaticFieldAccess node) {
    // Visit the field type
    Type c1 = node.getFieldType().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // Load the field object
    Field f = null;
    try {
      f = context.getField(c, node.getFieldName());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
    
    // Set the node properties
    node.setProperty(NodeProperties.FIELD, f);
    node.setProperty(NodeProperties.TYPE,  c = f.getType());
    
    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
  }
  
  /**
   * Visits an ObjectMethodCall
   * @param node the node to visit
   */
  public Type visit(ObjectMethodCall node) {
    // Check the receiver
    Expression exp = node.getExpression();
    Type      c1   = exp.acceptVisitor(this);
    Class<?>   c   = (Class<?>)c1;
    String    mn   = node.getMethodName();
    
    if (!c.isArray() || c.isArray() && !mn.equals("clone")) {
      // Do the type checking of the arguments
      List<Expression> args = node.getArguments();
      Class<?>[] cargs = Constants.EMPTY_CLASS_ARRAY;        
      if (args != null) {
        cargs = new Class[args.size()];
        Iterator<Expression> it = args.iterator();
        int i  = 0;
        while (it.hasNext()) {
          cargs[i++] = (Class<?>)it.next().acceptVisitor(this);
        }
        
        
      }
      Method m = null;
      try {
        m = context.lookupMethod(exp, mn, cargs);
      }
      catch (NoSuchMethodException e) {
        String s = c.getName();
        String sargs = "";
        for (int i = 0; i < cargs.length-1; i++) {
          sargs += cargs[i].getName() + ", ";
        }
        if (cargs.length > 0) {
          sargs += cargs[cargs.length-1].getName();
        }
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { mn, s, sargs });
        throw new ExecutionError("no.such.method.with.args", node);
      }
      catch (MethodModificationError e) {
        Expression expr = e.getExpression();
        expr.acceptVisitor(this);
        node.setExpression(expr);
        m = e.getMethod();
      }
      
      // Set the node properties
      node.setProperty(NodeProperties.METHOD, m);
      node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
      return c;
    } 
    else {
      if (!mn.equals("clone") || node.getArguments() != null) {
        String s0 = "clone";
        String s1 = c.getComponentType().getName() + " array";
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
        throw new ExecutionError("no.such.method", node);
      }
      node.setProperty(NodeProperties.TYPE, c = Object.class);
      return c;
    }
  }
  
  /**
   * Visits a MethodDeclaration
   * Note: the checkVarArgs here may or may not actually work. The test case in Distinction1415 failed when tiger features
   * were disabled, and when tracing through, the visitor that actually acted upon the method declaration node was the ClassInfoCompiler, and
   * there is another statement throwing a WrongVersionException in this file.
   * @param node the node to visit
   */
  public Type visit(MethodDeclaration node) {
    checkVarArgs(node);
    context.defineFunction(node);
    
    node.setProperty(NodeProperties.TYPE, node.getReturnType().acceptVisitor(this));
    node.setProperty(NodeProperties.FUNCTIONS, context.getFunctions());
    node.setProperty(NodeProperties.IMPORTATION_MANAGER, context.getImportationManager().clone());
    
    context.enterScope();
    checkList(node.getParameters());
    context.leaveScope();
    return null;
  }
  
  /**
   * Checks to see if the MethodDeclaration has variable arguments and whether or not they are allowed
   * @param node - the MethodDeclaration which may or may not contain variable arguments
   */
  protected abstract void checkVarArgs(MethodDeclaration node);
  
  
  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  public Type visit(FunctionCall node) {
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    Class<?>[] cargs = Constants.EMPTY_CLASS_ARRAY;       //LOOK HERE!!
    if (args != null) {
      cargs = new Class[args.size()];
      Iterator<Expression> it = args.iterator();
      int i  = 0;
      while (it.hasNext()) {
        cargs[i++] = (Class<?>)it.next().acceptVisitor(this);
      }
    }
    
    MethodDeclaration f = null;
    try { f = context.lookupFunction(node.getMethodName(), cargs); } 
    catch (NoSuchFunctionException e) {
      String s = node.getMethodName();
      node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s });
      throw new ExecutionError("no.such.function", node);
    }
    
    // Set the node properties
    Type c;
    node.setProperty(NodeProperties.FUNCTION, f);
    node.setProperty(NodeProperties.TYPE,  c = NodeProperties.getType(f));
    return c;
  }
  
  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public Type visit(SuperMethodCall node) {
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    Class<?>[] pt = Constants.EMPTY_CLASS_ARRAY;
    if (args != null) {
      pt = new Class[args.size()];
      Iterator<Expression> it = args.iterator();
      int i = 0;
      while (it.hasNext()) {
        pt[i++] = (Class<?>)it.next().acceptVisitor(this);
      }
    }
    
    Method m = null;
    try { m = context.lookupSuperMethod(node, node.getMethodName(), pt); } 
    catch (Exception e) { throw new CatchedExceptionError(e, node); }
    
    // Set the node properties
    Type c;
    node.setProperty(NodeProperties.METHOD, m);
    node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
    return c;
  }
  
  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public Type visit(StaticMethodCall node) {
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    Class<?>[] cargs = Constants.EMPTY_CLASS_ARRAY;
    if (args != null) {
      cargs = new Class[args.size()];
      Iterator<Expression> it = args.iterator();
      int      i  = 0;
      while (it.hasNext()) {
        cargs[i++] = (Class<?>)it.next().acceptVisitor(this);
      }
    }
    Method m = null;
    Node   n = node.getMethodType();
    Type  c1 = n.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    
    try { m = context.lookupMethod(n, node.getMethodName(), cargs); } 
    catch (NoSuchMethodException e) {
      String s0 = node.getMethodName();
      String s1 = c.getName();
      String sargs = "";
      for (int i = 0; i < cargs.length-1; i++) {
        sargs += cargs[i].getName() + ", ";
      }
      if (cargs.length > 0) sargs += cargs[cargs.length-1].getName();
      
      node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1, sargs });
      throw new ExecutionError("no.such.method.with.args", node);
    }
    
    // Set the node properties
    node.setProperty(NodeProperties.METHOD, m);
    node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
    return c;
  }
  
  /**
   * Visits a SimpleAssignExpression
   * @param node the node to visit
   */
  public Type visit(SimpleAssignExpression node) {
    Expression left  = node.getLeftExpression();
    Expression right = node.getRightExpression();
    Type rc = right.acceptVisitor(this);
    
    // Perhaps is this assignment a variable declaration
    if (left instanceof QualifiedName) {
      String var = ((QualifiedName)left).getRepresentation();
      if (!context.exists(var)) {
        context.define(var, (rc == null) ? Object.class : rc);
      }
    }
    
    // Get the type of the left hand side
    Type lc = left.acceptVisitor(this);
    
    // The left subexpression must be a variable
    if (!left.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Check the validity of the assignment
    Expression exp = checkAssignmentStaticRules(lc, rc, node, right);
    node.setRightExpression(exp);
    
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
  /**
   * Visits a QualifiedName
   * @param node the node to visit
   */
  public Type visit(QualifiedName node) {
    String var = node.getRepresentation();
    
    // Set the modifier
    Type c = context.get(var);    //cast to Class<?>???
    node.setProperty(NodeProperties.TYPE, c);
    
    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
    
  }
  
  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  public Type visit(SimpleAllocation node) {
    // Check the type to declare
    Node type = node.getCreationType();
    Type c1  = type.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    Class<?>[] cargs = Constants.EMPTY_CLASS_ARRAY;
    
    if (args != null) {
      cargs = new Class[args.size()];
      
      ListIterator<Expression> it = args.listIterator();
      int i  = 0;
      while (it.hasNext()) {
        cargs[i++] = (Class<?>)it.next().acceptVisitor(this);
      }
    }
    return context.setProperties(node, c, cargs);
  }
  
  /**
   * Visits a InnerAllocation
   * @param node the node to visit
   */
  public Type visit(InnerAllocation node) {
    // Visit the expression
    Type ec1 = node.getExpression().acceptVisitor(this);
    Class<?> ec = (Class<?>)ec1;
    // Check the type to declare
    Node type = node.getCreationType();
    if (type instanceof ReferenceType) {
      ReferenceType rt = (ReferenceType)type;
      rt.setRepresentation(ec.getName() + "$" + rt.getRepresentation());
    } else {
      throw new ExecutionError("allocation.type", node);
    }
    Type c1 = type.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    Type dc1 = InterpreterUtilities.getDeclaringClass(c);
    Class<?> dc = (Class<?>)dc1;
    
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    Class<?>[] cargs = null;
    
    
    if (dc != null && dc.isAssignableFrom(ec)) {
      // Adds an argument if the class to build is an innerclass
      if (args != null) {
        cargs = new Class<?>[args.size() + 1];
        
        cargs[0] = ec;
        ListIterator<Expression> it = args.listIterator();
        int i  = 1;
        while (it.hasNext()) {
          cargs[i++] = (Class<?>)it.next().acceptVisitor(this);
        }
      } else {
        cargs = new Class<?>[] { ec };
      }
    } else {
      throw new ExecutionError("allocation.type", node);
    }
    Constructor cons = null;
    try { cons = context.lookupConstructor(c, cargs); } 
    catch (Exception e) { throw new CatchedExceptionError(e, node); }
    
    // Set the properties of this node
    node.setProperty(NodeProperties.TYPE,        c);
    node.setProperty(NodeProperties.CONSTRUCTOR, cons);
    
    return c;
  }
  
  /**
   * Visits a ClassAllocation
   * @param node the node to visit
   */
  public Type visit(ClassAllocation node) {
    // If the class allocation is the initializer of a field,
    // it is possible that it has already been visited
    if (node.hasProperty(NodeProperties.TYPE)) {
      return NodeProperties.getType(node);
    } else {
      // Get the class to allocate
      Node   ctn   = node.getCreationType();
      Type   ct1   = ctn.acceptVisitor(this);
      Class<?> ct = (Class<?>)ct1;
      List<Expression>   largs = node.getArguments();
      Class<?>[] args = Constants.EMPTY_CLASS_ARRAY;
      
      if (largs != null) {
        args = new Class[largs.size()];
        Iterator<Expression> it = largs.iterator();
        int i = 0;
        while (it.hasNext()) {
          args[i++] = (Class<?>)it.next().acceptVisitor(this);
        }
      }
      return context.setProperties(node, ct, args, node.getMembers());
    }
  }
  
  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public Type visit(ArrayAllocation node) {
    // Do the checking of the size expressions
    ListIterator<Expression> it = node.getSizes().listIterator();
    
    while (it.hasNext()) {
      Expression exp = it.next();
      Type c1 = exp.acceptVisitor(this);
      Class<?> c = (Class<?>)c1;
      
      // Dimension expression must be of an integral type, but not long
      if (c != char.class      && c != byte.class && c != short.class && c != int.class &&
          c != Character.class && c != Byte.class && c != Short.class && c != Integer.class) {
        throw new ExecutionError("array.dimension.type", node);
      }
      
      // un-box the size of the array, if necessary
      if (TigerUtilities.isBoxingType(c)) {
        it.set(_unbox(exp, c));
      }
    }
    
    // Type-check the type of the array
    Type c1 = node.getCreationType().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // Visits the initializer if one
    if (node.getInitialization() != null) {
      node.getInitialization().acceptVisitor(this);
    }
    
    // Set the type properties of this node
    Type ac = Array.newInstance(c, new int[node.getDimension()]).getClass();
    node.setProperty(NodeProperties.TYPE, ac);
    node.setProperty(NodeProperties.COMPONENT_TYPE, c);
    return ac;
  }
  
  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  public Type visit(ArrayInitializer node) {
    node.getElementType().acceptVisitor(this);
    
    checkList(node.getCells());
    return null;
  }
  
  /**
   * Visits an ArrayAccess
   * @param node the node to visit
   */
  public Type visit(ArrayAccess node) {
    // Visits the expression on which this array access applies
    Type c1 = node.getExpression().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // Make sure this is an array
    if (!c.isArray()) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("array.required", node);
    }
    
    // Sets the properties of this node
    Type result = c.getComponentType();
    node.setProperty(NodeProperties.TYPE, result);
    node.setProperty(NodeProperties.MODIFIER, new ArrayModifier(node));
    
    // Visits the cell number expression
    Type c2 = node.getCellNumber().acceptVisitor(this);
    c = (Class<?>)c2;
    
    // The index must be of an integral type, but not a long
    if (c != char.class      && c != byte.class && c != short.class && c != int.class &&
        c != Character.class && c != Byte.class && c != Short.class && c != Integer.class) {
      throw new ExecutionError("array.index.type", node);
    }
    
    // un-box the index into the array, if necessary
    if (TigerUtilities.isBoxingType(c)) {
      node.setCellNumber(_unbox(node.getCellNumber(), c));
    }
    
    return result;
  }
  
  /**
   * Visits a PrimitiveType
   * @param node the node to visit
   */
  public Type visit(PrimitiveType node) {
    Type c = node.getValue();
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits a ReferenceType
   * @param node the node to visit
   */
  public Type visit(ReferenceType node) {
    //System.out.println(node)
    checkGenericReferenceType(node);
    Type c = null;
    try {
      c = context.lookupClass(node.getRepresentation());
    } 
    catch (ClassNotFoundException e) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { node.getRepresentation() });
      throw new ExecutionError("undefined.class", node);
    }
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * If java earlier than 1.5, checks if the node is a 
   * GenericReferenceType
   * @param node unused
   */  
  protected abstract void checkGenericReferenceType(ReferenceType node);
  
  
  /**
   * Visits an ArrayType
   * @param node the node to visit
   */
  public Type visit(ArrayType node) {
    Node eType = node.getElementType();
    Type c1 = eType.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    Type ac = Array.newInstance(c, 0).getClass();
    
    // Set the type property of this node
    node.setProperty(NodeProperties.TYPE, ac);
    return ac;
  }
  
  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  public Type visit(TypeExpression node) {
    Type c = node.getType().acceptVisitor(this);
    node.setProperty(NodeProperties.TYPE, Class.class);
    node.setProperty(NodeProperties.VALUE, c);
    return Class.class;
  }
  
  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public Type visit(NotExpression node) {
    // Check the type
    Expression exp = node.getExpression();
    Type c1 = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    
    if (c != boolean.class && c != Boolean.class) {
      throw new ExecutionError("not.expression.type", node);
    }
    node.setProperty(NodeProperties.TYPE, boolean.class);
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(c)) {
      node.setExpression(_unbox(exp, c));
    }
    
    // Compute the expression if it is constant
    if (exp.hasProperty(NodeProperties.VALUE)) {
      if (((Boolean) exp.getProperty(NodeProperties.VALUE)).booleanValue()) {
        node.setProperty(NodeProperties.VALUE, Boolean.FALSE);
      } 
      else {
        node.setProperty(NodeProperties.VALUE, Boolean.TRUE);
      }
    }
    return boolean.class;
  }
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public Type visit(ComplementExpression node) {
    // Check the type
    Expression e = node.getExpression();
    Type c1 = e.acceptVisitor(this);
    Type returnType = c1;
    Class<?> c = (Class<?>)c1;
    if (c == char.class      || c == byte.class || c == short.class ||
        c == Character.class || c == Byte.class || c == Short.class) {
      node.setProperty(NodeProperties.TYPE, int.class);
    } 
    else if (c == int.class     || c == long.class ||
             c == Integer.class || c == Long.class) {
      if (c == Integer.class) {
        returnType = int.class;
      }
      else if (c == Long.class) {
        returnType = long.class;
      }
      node.setProperty(NodeProperties.TYPE, returnType);
    } 
    else {
      throw new ExecutionError("complement.expression.type", node);
    }
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(c)) {
      node.setExpression(_unbox(e, c));
    }
    
    // Compute the expression if it is constant
    if (e.hasProperty(NodeProperties.VALUE)) {
      Object o = e.getProperty(NodeProperties.VALUE);
      
      if (o instanceof Character) {
        o = new Integer(((Character)o).charValue());
      }
      if (c == int.class) {
        o = new Integer(~((Number)o).intValue());
      } 
      else {
        o = new Long(~((Number)o).longValue());
      }
      node.setProperty(NodeProperties.VALUE, o);
    }
    return returnType;
  }
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public Type visit(PlusExpression node) {
    Type c1 = visitUnaryOperation(node, "plus.expression.type");
    Class<?> c = (Class<?>)c1;
    // Compute the expression if it is constant
    Node  n = node.getExpression();
    if (n.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.plus(c, n.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public Type visit(MinusExpression node) {
    Type c1 = visitUnaryOperation(node, "minus.expression.type");
    Class<?> c = (Class<?>)c1;
    // Compute the expression if it is constant
    Node  n = node.getExpression();
    if (n.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.minus(c, n.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits an AddExpression
   * @param node the node to visit
   */
  public Type visit(AddExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    Type lc = ln.acceptVisitor(this);
    Type rc = rn.acceptVisitor(this);
    Type c  = String.class;
    
    if (lc != String.class && rc != String.class) {
      c = visitNumericExpression(node, "addition.type");
    } 
    else {
      node.setProperty(NodeProperties.TYPE, c);
    }
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) && 
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.add((Class<?>)c, ln.getProperty(NodeProperties.VALUE),
                                                rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public Type visit(AddAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Do some error checking for null, void, etc.
    if (lc != String.class) {
      if (lc == null          || rc == null          ||
          lc == void.class    || rc == void.class    ||
          lc == boolean.class || rc == boolean.class || 
          lc == Boolean.class || rc == Boolean.class ||
          !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
          !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc)) ) {
        throw new ExecutionError("addition.type", node);
      }
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public Type visit(SubtractExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Type c1 = visitNumericExpression(node, "subtraction.type");
    Class<?> c = (Class<?>)c1;
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.subtract(c,
                                       ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public Type visit(SubtractAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Do some error checking for null, void, etc.
    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        lc == Boolean.class || rc == Boolean.class ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc)) ) {
      throw new ExecutionError("subtraction.type", node);
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public Type visit(MultiplyExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Type c1 = visitNumericExpression(node, "multiplication.type");
    Class<?> c = (Class<?>)c1;
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.multiply(c,
                                       ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public Type visit(MultiplyAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Do some error checking for null, void, etc.
    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        lc == Boolean.class || rc == Boolean.class ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc)) ) {
      throw new ExecutionError("multiplication.type", node);
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
//  /**
//   * Visits a DivideExpression
//   * @param node the node to visit
//   */
//  public Class<?> visit(DivideExpression node) {
//    Node  ln = node.getLeftExpression();
//    Node  rn = node.getRightExpression();
//    ln.acceptVisitor(this);
//    rn.acceptVisitor(this);
//    Class<?> c = visitNumericExpression(node, "division.type");
//
//    // Compute the expression if it is constant
//    if (ln.hasProperty(NodeProperties.VALUE) &&
//        rn.hasProperty(NodeProperties.VALUE)) {
//      node.setProperty
//        (NodeProperties.VALUE,
//         InterpreterUtilities.divide(c,
//                                     ln.getProperty(NodeProperties.VALUE),
//                                     rn.getProperty(NodeProperties.VALUE)));
//    }
//    return c;
//  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Type visit(DivideExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Type c = visitNumericExpression(node, "division.type");
    return c;
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Type visit(RemainderExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Type c = visitNumericExpression(node, "remainder.type");
    return c;
  }
  
  
  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public Type visit(DivideAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;

    // Do some error checking for null, void, etc.
    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        lc == Boolean.class || rc == Boolean.class ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc))) {
      throw new ExecutionError("division.type", node);
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
//  /**
//   * Visits a RemainderExpression
//   * @param node the node to visit
//   */
//  public Class<?> visit(RemainderExpression node) {
//    Node  ln = node.getLeftExpression();
//    Node  rn = node.getRightExpression();
//    ln.acceptVisitor(this);
//    rn.acceptVisitor(this);
//    Class<?> c = visitNumericExpression(node, "remainder.type");
//
//    // Compute the expression if it is constant
//    if (ln.hasProperty(NodeProperties.VALUE) &&
//        rn.hasProperty(NodeProperties.VALUE)) {
//      node.setProperty
//        (NodeProperties.VALUE,
//         InterpreterUtilities.remainder(c,
//                                        ln.getProperty(NodeProperties.VALUE),
//                                        rn.getProperty(NodeProperties.VALUE)));
//    }
//    return c;
//  }
  
  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  public Type visit(RemainderAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Do some error checking for null, void, etc.
    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        lc == Boolean.class || rc == Boolean.class ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc)) ) {
      throw new ExecutionError("remainder.type", node);
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
  /**
   * Visits an EqualExpression
   * @param node the node to visit
   */
  public Type visit(EqualExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = rn.acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Check the equality rules, and un-box
    checkEqualityStaticRules(lc, rc, node);
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.equalTo(lc, rc,
                                                    ln.getProperty(NodeProperties.VALUE),
                                                    rn.getProperty(NodeProperties.VALUE)));
    }
    
    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    
    // Return the type of the expression (always boolean)
    return boolean.class;
  }
  
  /**
   * Visits an NotEqualExpression
   * @param node the node to visit
   */
  public Type visit(NotEqualExpression node) {
    // Check the types
    Node ln = node.getLeftExpression();
    Node rn = node.getRightExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = rn.acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    // Check the equality rules, and un-box
    checkEqualityStaticRules(lc, rc, node);
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.notEqualTo(lc, rc,
                                                       ln.getProperty(NodeProperties.VALUE),
                                                       rn.getProperty(NodeProperties.VALUE)));
    }
    
    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    
    // Return boolean type
    return boolean.class;
  }
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public Type visit(LessExpression node) {
    Type c = visitRelationalExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.lessThan(ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public Type visit(LessOrEqualExpression node) {
    Type c = visitRelationalExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.lessOrEqual(ln.getProperty(NodeProperties.VALUE),
                                          rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public Type visit(GreaterExpression node) {
    Type c = visitRelationalExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.greaterThan(ln.getProperty(NodeProperties.VALUE),
                                          rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public Type visit(GreaterOrEqualExpression node) {
    Type c = visitRelationalExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.greaterOrEqual(ln.getProperty(NodeProperties.VALUE),
                                             rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public Type visit(BitAndExpression node) {
    Type c1 = visitBitwiseExpression(node);
    Class<?> c = (Class<?>)c1;
    Node ln = node.getLeftExpression();
    Node rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.bitAnd(c,
                                                   ln.getProperty(NodeProperties.VALUE),
                                                   rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public Type visit(BitAndAssignExpression node) {
    return visitBitwiseAssign(node);
  }
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public Type visit(BitOrExpression node) {
    Type c1 = visitBitwiseExpression(node);
    Class<?> c = (Class<?>)c1;
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.bitOr(c, ln.getProperty(NodeProperties.VALUE), rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public Type visit(BitOrAssignExpression node) {
    return visitBitwiseAssign(node);
  }
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public Type visit(ExclusiveOrExpression node) {
    Type c1 = visitBitwiseExpression(node);
    Class<?> c = (Class<?>)c1;
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.xOr
           (c,
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public Type visit(ExclusiveOrAssignExpression node) {
    return visitBitwiseAssign(node);
  }
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public Type visit(ShiftLeftExpression node) {
    Type c = visitShiftExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.shiftLeft
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public Type visit(ShiftLeftAssignExpression node) {
    Type c = visitShiftExpression(node);
    
    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("shift.left.type", node);
    }
    
    return c;
  }
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public Type visit(ShiftRightExpression node) {
    Type c = visitShiftExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.shiftRight
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a ShiftRightAssignExpression
   * @param node the node to visit
   */
  public Type visit(ShiftRightAssignExpression node) {
    Type c = visitShiftExpression(node);
    
    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("shift.right.type", node);
    }
    
    return c;
  }
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public Type visit(UnsignedShiftRightExpression node) {
    Type c = visitShiftExpression(node);
    
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.unsignedShiftRight
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }
  
  /**
   * Visits a UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public Type visit(UnsignedShiftRightAssignExpression node) {
    Type c = visitShiftExpression(node);
    
    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("unsigned.shift.right.type", node);
    }
    
    return c;
  }
  
  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  public Type visit(AndExpression node) {
    Expression le = node.getLeftExpression();
    Expression re = node.getRightExpression();
    Type lc = le.acceptVisitor(this);
    Type rc = re.acceptVisitor(this);
    
    // Check the types of the operands
    if (!(lc == boolean.class || lc == Boolean.class) || 
        !(rc == boolean.class || rc == Boolean.class) ) {
      throw new ExecutionError("and.type", node);
    }
    
    // Auto-unbox, if necessary
    if (lc == Boolean.class) {
      node.setLeftExpression(_unbox(le, lc));
    }
    if (rc == Boolean.class) {
      node.setRightExpression(_unbox(re, rc));
    }
    
    // Compute the expression if it is constant
    if (le.hasProperty(NodeProperties.VALUE) &&
        re.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         (((Boolean)le.getProperty(NodeProperties.VALUE)).booleanValue() &&
          ((Boolean)re.getProperty(NodeProperties.VALUE)).booleanValue())
           ? Boolean.TRUE : Boolean.FALSE);
    }
    
    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }
  
  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  public Type visit(OrExpression node) {
    Expression ln = node.getLeftExpression();
    Expression rn = node.getRightExpression();
    Type lc = ln.acceptVisitor(this);
    Type rc = rn.acceptVisitor(this);
    
    // Check the types of the operands
    if (!(lc == boolean.class || lc == Boolean.class) || 
        !(rc == boolean.class || rc == Boolean.class) ) {
      throw new ExecutionError("or.type", node);
    }
    
    // Auto-unbox, if necessary
    if (lc == Boolean.class) {
      node.setLeftExpression(_unbox(ln, lc));
    }
    if (rc == Boolean.class) {
      node.setRightExpression(_unbox(rn, rc));
    }
    
    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         (((Boolean)ln.getProperty(NodeProperties.VALUE)).booleanValue() ||
          ((Boolean)rn.getProperty(NodeProperties.VALUE)).booleanValue())
           ? Boolean.TRUE : Boolean.FALSE);
    }
    
    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }
  
//  /**
//   * Visits a InstanceOfExpression
//   * @param node the node to visit
//   */
//  public Class<?> visit(InstanceOfExpression node) {
//    node.getReferenceType().acceptVisitor(this);
//
//    // The expression must not have a primitive type
//    if ((node.getExpression().acceptVisitor(this)).isPrimitive()) {
//      throw new ExecutionError("left.expression", node);
//    }
//
//    // Set the type property
//    node.setProperty(NodeProperties.TYPE, boolean.class);
//    return boolean.class;
//  }
  
  
  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  public Type visit(InstanceOfExpression node) {
    node.getReferenceType().acceptVisitor(this);
    
    // The expression must not have a primitive type
    Type c1 = node.getExpression().acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    if ((c != null) && c.isPrimitive()) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public Type visit(ConditionalExpression node) {
    // Get the type of the conidition expression
    Expression condExp = node.getConditionExpression();
    Type type = condExp.acceptVisitor(this);
    
    // Check the condition
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto-unbox, if necessary
    if (type == Boolean.class) {
      node.setConditionExpression(_unbox(condExp, type));
    }
    
    // Determine the type of the expression
    Expression exp1 = node.getIfTrueExpression();
    Expression exp2 = node.getIfFalseExpression();
    Type c1A = exp1.acceptVisitor(this);
    Type c2B = exp2.acceptVisitor(this);
    Type ec = null;
    
    Class<?> c1 = (Class<?>)c1A;
    Class<?> c2 = (Class<?>)c2B;
    
    // unbox a boxing type, except when the boxing type is 
    // Boolean and the other is not boolean
    if (TigerUtilities.isBoxingType(c1) && c2.isPrimitive()) {
      if (!(c1 == Boolean.class && c2 != boolean.class)) {
        exp1 = _unbox(exp1, c1);
        c1 = _correspondingPrimType(c1).getValue();
        node.setIfTrueExpression(exp1);
      }
    }
    else if (TigerUtilities.isBoxingType(c2) && c1.isPrimitive()) {
      if (!(c2 == Boolean.class && c1 != boolean.class)) {
        exp2 = _unbox(exp2, c2);
        c2 = _correspondingPrimType(c2).getValue();
        node.setIfFalseExpression(exp2);
      }
    }
    
    // See if the expression is typable
    if (c1 == c2) {
      ec = c1;
    } 
    else if (c1 == null) {
      ec = c2;
    }
    else if (c2 == null) {
      ec = c1;
    }
    else if (!c1.isPrimitive() && !c2.isPrimitive()) {
      if (c1.isAssignableFrom(c2)) {
        ec = c1;
      }
      else if (c2.isAssignableFrom(c1)) {
        ec = c2;
      }
      else {
        // originally set to throw an error, this case now 
        // simply returns Object, the lowest common type
        ec = Object.class;
      }
    }
    else if (c1 == void.class || c2 == void.class) {
      throw new ExecutionError("incompatible.types", node);
    }
    else if (c1 == boolean.class || c2 == boolean.class ||
             c1 == Boolean.class || c2 == Boolean.class) {
      if ((c1 == boolean.class && c2.isPrimitive()) || 
          (c2 == boolean.class && c1.isPrimitive())) {
        // box both
        node.setIfTrueExpression (_box(exp1, TigerUtilities.correspondingBoxingType(c1)));
        node.setIfFalseExpression(_box(exp2, TigerUtilities.correspondingBoxingType(c2)));
        ec = Object.class;
      }
      else if (c1 == Boolean.class && c2.isPrimitive()) {
        // box c2
        node.setIfFalseExpression(_box(exp2, TigerUtilities.correspondingBoxingType(c2)));
        ec = Object.class;
      }
      else if (c2 == Boolean.class && c1.isPrimitive()) {
        // box c1
        node.setIfTrueExpression(_box(exp1, TigerUtilities.correspondingBoxingType(c1)));
        ec = Object.class;
      }
      else if (c1 == boolean.class && TigerUtilities.isBoxingType(c2)) {
        // box c1
        node.setIfTrueExpression(_box(exp1, Boolean.class));
        ec = Object.class;
      }
      else if (c2 == boolean.class && TigerUtilities.isBoxingType(c1)) {
        // box c2
        node.setIfFalseExpression(_box(exp2, Boolean.class));
        ec = Object.class;
      }
      else {
        throw new ExecutionError("incompatible.types", node);
      }
    }
    else if ((c1 == short.class && c2 == byte.class) ||
             (c1 == byte.class  && c2 == short.class)) {
      ec = short.class;
    }
    else if ((c2 == byte.class || c2 == short.class || c2 == char.class) &&
             exp1.hasProperty(NodeProperties.VALUE) && c1 == int.class) {
      Number n = (Number) exp1.getProperty(NodeProperties.VALUE);
      if (c2 == byte.class) {
        if (n.intValue() == n.byteValue()) {
          ec = byte.class;
        }
        else {
          ec = int.class;
        }
      } 
      else if (n.intValue() == n.shortValue()) {
        ec = (c2 == char.class) ? char.class : short.class;
      }
      else {
        ec = int.class;
      }
    } 
    else if ((c1 == byte.class || c1 == short.class || c1 == char.class) &&
             exp2.hasProperty(NodeProperties.VALUE) && c2 == int.class) {
      Number n = (Number)exp2.getProperty(NodeProperties.VALUE);
      if (c1 == byte.class) {
        if (n.intValue() == n.byteValue()) {
          ec = byte.class;
        } 
        else {
          ec = int.class;
        }
      } 
      else if (n.intValue() == n.shortValue()) {
        ec = (c1 == char.class) ? char.class : short.class;
      }
      else {
        ec = int.class;
      }
    } 
    else if (c1 == double.class || c2 == double.class) {
      ec = double.class;
    }
    else if (c1 == float.class || c2 == float.class) {
      ec = float.class;
    } 
    else if (c1 == long.class || c2 == long.class) {
      ec = long.class;
    }
    else {
      ec = int.class;
    }
    node.setProperty(NodeProperties.TYPE, ec);
    
    return ec;
  }
  
  /**
   * Visits a FormalParameter
   * @param node the node to visit
   * @return the class of the parameter
   */
  public Type visit(FormalParameter node) {
    Type c = node.getType().acceptVisitor(this);
    
    if (node.isFinal()) {
      context.defineConstant(node.getName(), c);
    } else {
      context.define(node.getName(), c);
    }
    node.setProperty(NodeProperties.TYPE, c);
    
    return c;
  }
  
  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public Type visit(PostIncrement node) {
    
    Node exp = node.getExpression();
    Type c1  = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // The type of the subexpression must be numeric
    if (!(c.isPrimitive() || TigerUtilities.isBoxingType(c)) ||
        c == void.class     ||
        c == boolean.class  ||
        c == Boolean.class) {
      throw new ExecutionError("post.increment.type", node);
    }
    
    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("post.increment.type", node);
    }
    
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public Type visit(PreIncrement node) {
    Node exp = node.getExpression();
    Type c1  = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // The type of the subexpression must be numeric
    if (!(c.isPrimitive() || TigerUtilities.isBoxingType(c)) ||
        c == void.class     ||
        c == boolean.class  ||
        c == Boolean.class) {
      throw new ExecutionError("pre.increment.type", node);
    }
    
    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("pre.increment.type", node);
    }
    
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public Type visit(PostDecrement node) {
    Node exp = node.getExpression();
    Type c1  = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // The type of the subexpression must be numeric
    if (!(c.isPrimitive() || TigerUtilities.isBoxingType(c)) ||
        c == void.class     ||
        c == boolean.class  ||
        c == Boolean.class) {
      throw new ExecutionError("post.decrement.type", node);
    }
    
    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("post.decrement.type", node);
    }
    
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public Type visit(PreDecrement node) {
    Node exp = node.getExpression();
    Type c1  = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    // The type of the subexpression must be numeric
    if (!(c.isPrimitive() || TigerUtilities.isBoxingType(c)) ||
        c == void.class     ||
        c == boolean.class  ||
        c == Boolean.class) {
      throw new ExecutionError("pre.decrement.type", node);
    }
    
    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("pre.decrement.type", node);
    }
    
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public Type visit(CastExpression node) {
    Type c = node.getTargetType().acceptVisitor(this);
    checkCastStaticRules(c, node.getExpression().acceptVisitor(this), node);
    
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }
  
  /**
   * Visits an unary operation.
   */
  private Type visitUnaryOperation(UnaryExpression node, String s) {
    Expression exp = node.getExpression();
    Type c1 = exp.acceptVisitor(this);
    Class<?> c = (Class<?>)c1;
    Type returnType = c;
    
    if (c == char.class      || c == byte.class || c == short.class || c == int.class ||
        c == Character.class || c == Byte.class || c == Short.class || c == Integer.class) {
      node.setProperty(NodeProperties.TYPE, int.class);
    }
    else if (c == long.class || c == float.class || c == double.class ||
             c == Long.class || c == Float.class || c == Double.class) {
      if (c == Long.class) {
        returnType = long.class;
      }
      else if (c == Float.class) {
        returnType = float.class;
      }
      else if (c == Double.class) {
        returnType = double.class;
      }
      node.setProperty(NodeProperties.TYPE, returnType);
    }
    else {
      throw new ExecutionError(s, node);
    }
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(c)) {
      node.setExpression(_unbox(exp, c));
    }
    
    return returnType;
  }
  
  /**
   * Visits a numeric expression.
   * This method checks the types on any numeric binary operator
   * performing any needed unboxing or numeric promotion on either
   * of the operands.
   * @param node the Binary expression to type check
   * @param s The key to use when throwing an ExecutionError
   * @return the type of the expression after any numeric promotion or 
   * unboxing has been performed
   */
  protected Type visitNumericExpression(BinaryExpression node, String s) {
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    
    // Set the type property of the given node
    Type lc1 = NodeProperties.getType(leftExp);
    Type rc1 = NodeProperties.getType(rightExp);
    Type c  = null;
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    
    // Check to make sure the left and right types are valid
    if (lc == null           || rc == null          ||
        lc == boolean.class  || rc == boolean.class ||
        !(lc.isPrimitive()   || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()   || TigerUtilities.isBoxingType(rc))  ||
        lc == void.class     || rc == void.class) {
      throw new ExecutionError(s, node);
    } 
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (TigerUtilities.isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    // Set the type of the node to be the binary promotion
    if (lc == double.class || lc == Double.class || 
        rc == double.class || rc == Double.class) {
      c = double.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else if (lc == float.class || lc == Float.class ||
             rc == float.class || rc == Float.class) {
      c = float.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else if (lc == long.class || lc == Long.class ||
             rc == long.class || rc == Long.class) {
      c = long.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else {
      c = int.class;
      node.setProperty(NodeProperties.TYPE, c);
    }
    
    // Return the type of the node
    return c;
  }
  
  
  /**
   * Checks the typing rules for an assignment
   * @param lc   the class of the left part of an assignment
   * @param rc   the class of the right part of an assignment
   * @param node the entire assignment node
   * @param v    The right-hand side of the expression
   * @return The right-hand side of the assignment.  This expression will
   * be the unboxing/boxing of the RHS if necessary.
   */
  public Expression checkAssignmentStaticRules(Type lc1, Type rc1,
                                               Node node, Expression v) {
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    
    if (lc != null) {
      if (lc.isPrimitive()) {
        if (lc == boolean.class && rc != boolean.class) {
          if (rc == Boolean.class) {
            return _unbox(v,Boolean.class);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == byte.class && rc != byte.class) {
          if (rc == Byte.class) {
            return _unbox(v,Byte.class);
          }
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.byteValue()) {
              return v;
            }
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if ((lc == short.class || lc == char.class) &&
                 (rc != byte.class && rc != short.class && rc != char.class)) {
          if (lc == short.class && rc == Short.class) {
            return _unbox(v, Short.class);
          }
          if (lc == char.class && rc == Character.class) {
            return _unbox(v, Character.class);
          }
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.shortValue()) {
              return v;
            }
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == int.class    &&
                 (rc != byte.class  &&
                  rc != short.class &&
                  rc != char.class  &&
                  rc != int.class)) {
          if (rc == Byte.class      || rc == Short.class ||
              rc == Character.class || rc == Integer.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == long.class   &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == float.class   ||
                  rc == double.class)) {
          if (TigerUtilities.isBoxingType(rc) && TigerUtilities.isIntegralType(rc)) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == float.class  &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == double.class)) {
          if (TigerUtilities.isBoxingType(rc) && rc != Boolean.class && rc != Double.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == double.class &&
                 (rc == null        ||
                  !rc.isPrimitive() ||
                  rc == void.class  ||
                  rc == boolean.class)) {
          if (TigerUtilities.isBoxingType(rc) && rc != Boolean.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
      }
      else /* lc is not Primitive */
        if (rc != null) {
        if (rc.isPrimitive()) {
          Type boxedRc1 = _correspondingRefClass(rc);
          Class<?> boxedRc = (Class<?>)boxedRc1;
          if (lc.isAssignableFrom(boxedRc)) return _box(v,boxedRc);
          if (TigerUtilities.boxesTo(rc, lc)) return _box(v, lc); /* I think this statement is unnecessary.  Corky 6/19/04 */
          throw new ExecutionError("assignment.types", node);
        }
        if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) { /* I don't know why the second clause appears in this test.  Corky 6/19/04 */
          throw new ExecutionError("assignment.types", node);
        }
      }
    }
    return v;
  }
  
  /**
   * Checks the typing rules in an equality operation
   * @param lc the class of the left operand
   * @param rc the class of the right operand
   * @param s  the error message
   * @param n  the current node
   */
  private void checkEqualityStaticRules(Class<?> lc, Class<?> rc, BinaryExpression n) {
    Expression leftExp = n.getLeftExpression();
    Expression rightExp = n.getRightExpression();
    
    // Auto-unbox, if necessary
    /**
     * We have not decided what the correct semantics
     * for the == operator is for boxed/primitive types
     */
    if (lc != null && rc != null) {
      if (TigerUtilities.isBoxingType(lc) && rc.isPrimitive()) {
        ObjectMethodCall methodCall = _unbox(leftExp, lc);
        n.setLeftExpression(methodCall);
        lc = (Class) methodCall.getProperty(NodeProperties.TYPE);
      }
      if (TigerUtilities.isBoxingType(rc) && lc.isPrimitive()) {
        ObjectMethodCall methodCall = _unbox(rightExp, rc);
        n.setRightExpression(methodCall);
        rc = (Class) methodCall.getProperty(NodeProperties.TYPE);
      }
    }
    
    if (lc != rc || lc == void.class) {
      if (lc == void.class    || rc == void.class ||
          lc == boolean.class || rc == boolean.class) {
        throw new ExecutionError("compare.type", n);
      } 
      else if ((lc == null && rc.isPrimitive()) ||
               (rc == null && lc.isPrimitive())) {
        throw new ExecutionError("compare.type", n);
      } 
      else if (lc != null && rc != null) {
        if (lc.isPrimitive() ^ rc.isPrimitive()) {
          throw new ExecutionError("compare.type", n);
        }
        else if (!lc.isPrimitive() && !rc.isPrimitive()) {  
          if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) {
            throw new ExecutionError("compare.type", n);
          }
        }
      }
    }
  }
  
  /**
   * Visits a relational expression.  This simply makes sure
   * the types are numerical primitives/boxing types, 
   * performs the unboxing if necessary, and sets the type
   * of the overall expression to the boolean primitive type
   * @param node the relational expression: (> < >= <=)
   * @return the type of the expression: boolean
   */
  private Type visitRelationalExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Type lc1 = leftExp.acceptVisitor(this);
    Type rc1 = rightExp.acceptVisitor(this);
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    if (lc == null          || rc == null           ||
        lc == void.class    || rc == void.class     ||
        lc == boolean.class || rc == boolean.class  ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))   || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc))) {
      throw new ExecutionError("relational.expression.type", node);
    }
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (TigerUtilities.isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    // The type of the expression is always boolean
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }
  
  /**
   * Visits a bitwise expression.
   * If either the left or right expression is an integral
   * boxing type, this method performs the unboxing procedure
   * and uses the primitive type of the ObjectMethodCall to 
   * perform the any numeric promotion necessary.
   * @param node the bitwise expression to be type checked.
   * @return the type of the expression after unboxing and 
   *         promotion has been executed
   */
  private Type visitBitwiseExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Type lc1 = leftExp.acceptVisitor(this);
    Type rc1 = rightExp.acceptVisitor(this);
    Type c = null;
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    
    boolean intLeft   = TigerUtilities.isIntegralType(lc);
    boolean intRight  = TigerUtilities.isIntegralType(rc);
    boolean boolLeft  = (lc == boolean.class  || lc == Boolean.class);
    boolean boolRight = (rc == boolean.class  || rc == Boolean.class);
    
//    if (lc == null           || rc == null           ||
//        lc == void.class     || rc == void.class     ||
//        lc == float.class    || rc == float.class    ||
//        lc == Float.class    || rc == Float.class    ||
//        lc == double.class   || rc == double.class   ||
//        lc == Double.class   || rc == Double.class   ||
//        (lc == boolean.class ^ rc == boolean.class)  ||
//        !lc.isPrimitive()    || !rc.isPrimitive()) {
    if (!(  intLeft &&  intRight ) && 
        !( boolLeft && boolRight ) ){
      throw new ExecutionError("bitwise.expression.type", node);
    } 
    
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (TigerUtilities.isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    if (lc == long.class || rc == long.class ||
        lc == Long.class || rc == Long.class) {
      node.setProperty(NodeProperties.TYPE, c = long.class);
    }
    else if ( boolLeft ) {
      node.setProperty(NodeProperties.TYPE, c = boolean.class);
    }
    else {
      node.setProperty(NodeProperties.TYPE, c = int.class);
    }
    return c;
  }
  
  /**
   * Checks a bitwise expression
   */
  private Type visitBitwiseAssign(BinaryExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Type lc1 = ln.acceptVisitor(this);
    Type rc1 = node.getRightExpression().acceptVisitor(this);
    
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    if (lc == null           || rc == null             ||
        lc == void.class     || rc == void.class       ||
        lc == float.class    || rc == float.class      ||
        lc == double.class   || rc == double.class     ||
        ((lc == boolean.class || lc == Boolean.class) ^ (rc == boolean.class || rc == Boolean.class)) ||
        !(lc.isPrimitive() || TigerUtilities.isBoxingType(lc))       || 
        !(rc.isPrimitive() || TigerUtilities.isBoxingType(rc))) {
      throw new ExecutionError("bitwise.expression.type", node);
    }
    
    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }
    
    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }
  
  /**
   * Visits a shift expression.
   * This method is responsible for unboxing the two operands
   * as necessary, performing any numeric promotions needed,
   * and checking that the types of the two operands are compatible
   * with the shift operations.
   * @param node the shifting expression to be type checked
   * @return the type of the expression after any unboxing or numeric
   * promotion has been successfully executed.
   */
  private Type visitShiftExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Type lc1 = leftExp.acceptVisitor(this);
    Type rc1 = rightExp.acceptVisitor(this);
    Type c  = null;
    Class<?> lc = (Class<?>)lc1;
    Class<?> rc = (Class<?>)rc1;
    
    if (lc == null          || rc == null          ||
        lc == boolean.class || rc == boolean.class ||
        lc == void.class    || rc == void.class    ||
        lc == float.class   || rc == float.class   ||
        lc == Float.class   || rc == Float.class   ||
        lc == double.class  || rc == double.class  ||
        lc == Double.class  || rc == Double.class  ||
        !(lc.isPrimitive()  || TigerUtilities.isBoxingType(lc))  || 
        !(rc.isPrimitive()  || TigerUtilities.isBoxingType(rc)) ) {
      throw new ExecutionError("shift.expression.type", node);
    } 
    
    // Auto-unbox, if necessary
    if (TigerUtilities.isBoxingType(lc) && !leftExp.hasProperty(NodeProperties.MODIFIER)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (TigerUtilities.isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    if (lc == long.class || lc == Long.class) {
      node.setProperty(NodeProperties.TYPE, c = long.class);
    } 
    else {
      node.setProperty(NodeProperties.TYPE, c = int.class);
    }
    return c;
  }
  
  /**
   * Checks the typing rules in a cast expression
   * @param tc the target class
   * @param ec the expression class
   * @param castExp the entire cast expression that is being type-checked
   */
  private void checkCastStaticRules(Type tc1, Type ec1, CastExpression castExp) {
    
    Class<?> tc = (Class<?>)tc1;
    Class<?> ec = (Class<?>)ec1;
    
    if (tc != ec) {
      if (tc.isPrimitive()) {
        boolean isBoxingType = TigerUtilities.isBoxingType(ec);
        if (ec == null          || 
            ec == boolean.class || 
            (tc == boolean.class && ec != Boolean.class) ||
            !(ec.isPrimitive()  || isBoxingType) ||
            ec == void.class) {
          throw new ExecutionError("cast", castExp);
        }
        
        if (isBoxingType) {
          castExp.setExpression(_unbox(castExp.getExpression(), ec));
        }        
      } 
      else if (ec != null) {
        if (ec.isArray()) {
          if (tc.isArray()) {
            Type tec1 = tc.getComponentType();
            Type eec1 = ec.getComponentType();
            
            Class<?> tec = (Class<?>)tec1;
            Class<?> eec = (Class<?>)eec1;
            
            if (tec.isPrimitive() && eec.isPrimitive()) {
              if (tec != eec) {
                throw new ExecutionError("cast", castExp);
              }
            } 
            else {
              checkCastStaticRules(tec, eec, castExp);
            }
          } 
          else if (tc.isInterface() && tc != Cloneable.class) {
            throw new ExecutionError("cast", castExp);
          }
          else if (tc != Object.class) {
            throw new ExecutionError("cast", castExp);
          }
        }
        else if (ec.isInterface()) {
          if (tc.isArray()) {
            if (!Cloneable.class.isAssignableFrom(ec)) {
              throw new ExecutionError("cast", castExp);
            }
          }
          else if (!tc.isInterface()) { // ec is an interface, tc is not
            if (isFinal(tc) && !ec.isAssignableFrom(tc)) {
              throw new ExecutionError("cast", castExp);
            }
          }
        } // ec is not an interface type
        else if (tc.isInterface()) { // tc is an interface, ec is not
          if (isFinal(ec) && !tc.isAssignableFrom(ec)) {
            throw new ExecutionError("cast", castExp);
          }
        } // both ec and tc are classes or primitives
        else if (ec.isPrimitive() && TigerUtilities.isBoxingType(tc) && ec != boolean.class) {
          castExp.setExpression(_box(castExp.getExpression(), tc));
        }
        else if (!ec.isAssignableFrom(tc) && !tc.isAssignableFrom(ec)) {
          throw new ExecutionError("cast", castExp);
        }
      }
    }
  }
  
  /**
   * Tests if the class/interface c is final
   */
  private static boolean isFinal(Class<?> c) { 
    return Modifier.isFinal(c.getModifiers());
  }
  
  /**
   * Check a list of node by running this type checker
   * on each node in the list.
   * @param the list of nodes to type check
   */
  private void checkList(List<? extends Node> l) {
    ListIterator<? extends Node> it = l.listIterator();
    while (it.hasNext()) {
      it.next().acceptVisitor(this);
    }
  }
  
  
  /**
   * Returns the primitive type that corresponds to the given reference class.
   * @param refType the reference class
   * @return the corresponding primitive type
   */
  protected static PrimitiveType _correspondingPrimType(Class<?> refClass) {
    if (refClass == Boolean.class) {
      return new BooleanType();
    }
    else if (refClass == Byte.class) {
      return new ByteType();
    }
    else if (refClass == Character.class) {
      return new CharType();
    }
    else if (refClass == Short.class) {
      return new ShortType();
    }
    else if (refClass == Integer.class) {
      return new IntType();
    }
    else if (refClass == Long.class) {
      return new LongType();
    }
    else if (refClass == Float.class) {
      return new FloatType();
    }
    else if (refClass == Double.class) {
      return new DoubleType();
    }
    else {
      throw new RuntimeException("No corresponding primitive type for reference class " + 
                                 refClass + ".");
    }
  }
  
  /**
   * Returns the reference class that corresponds to the given primitive class.
   * @param primClass the primtive class
   * @return the corresponding reference class
   */
  protected static Type _correspondingRefClass(Class<?> primClass) {
    if (primClass == boolean.class) {
      return Boolean.class;
    }
    else if (primClass == byte.class) {
      return Byte.class;
    }
    else if (primClass == char.class) {
      return Character.class;
    }
    else if (primClass == short.class) {
      return Short.class;
    }
    else if (primClass == int.class) {
      return Integer.class;
    }
    else if (primClass == long.class) {
      return Long.class;
    }
    else if (primClass == float.class) {
      return Float.class;
    }
    else if (primClass == double.class) {
      return Double.class;
    }
    else {
      throw new RuntimeException("No corresponding reference class for primitive class (?) " + 
                                 primClass + ".");
    }
  }
  
  
  /**
   * If autoboxing is supported
   * Boxes the given expression by returning the correct
   * <code>SimpleAllocation</code> corresponding to the given
   * primitive type. Throws an exception if 
   * @param exp the expression to box
   * @param refType the reference type to box the primitive type to
   * @return the <code>SimpleAllocation</code> that boxes the expression
   */
  protected abstract SimpleAllocation _box(Expression exp, Type refType);
  
  /**
   * If Unboxing is supported, unboxes the given expression by returning the correct
   * <code>ObjectMethodCall</code> corresponding to the given type
   * Throws an exception if unboxing is not supported
   * @param child The expression to unbox
   * @param type The type of the evaluated expression
   * @return The <code>ObjectMethodCall</code> that unboxes the expression
   */
  protected abstract ObjectMethodCall _unbox(Expression child, Type type);
}
