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

package koala.dynamicjava.interpreter.modifier;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This interface represents the objets that modify an object field
 *
 * @author Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class ObjectFieldModifier extends LeftHandSideModifier {
  /**
   * The field
   */
  protected Field field;
  
  /**
   * The node
   */
  protected ObjectFieldAccess node;
  
  /**
   * The object in which field is accessed
   */
  protected Object fieldObject;
  
  /**
   * The list used to manage recursive callsl it is a stack of pending
   * fieldObjects
   */
  protected List<Object> fields = new LinkedList<Object>();
  
  /**
   * Creates a new field modifier
   * @param f the field to modify
   * @param n the field access node
   */
  public ObjectFieldModifier(Field f, ObjectFieldAccess n) {
    field = f;
    node  = n;
  }
  
  /**
   * Prepares the modifier for modification
   */
  public Object prepare(Visitor<Object> v, Context ctx) {
    fields.add(0, fieldObject);
    
    fieldObject = node.getExpression().acceptVisitor(v);
    try {
      return field.get(fieldObject);
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }
  
  /**
   * Sets the value of the underlying left hand side expression
   */
  public void modify(Context ctx, Object value) {
    try {
      field.set(fieldObject, value);
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    } finally {
      fieldObject = fields.remove(0);
    }
  }
}
