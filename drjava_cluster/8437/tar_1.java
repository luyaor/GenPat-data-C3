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

package koala.dynamicjava.tree.tiger;

import koala.dynamicjava.tree.*;

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

public class PolymorphicSuperMethodCall extends SuperMethodCall {
  /**
   * The type arguments on which this method call applies
   */
  private List<TypeName> _typeArgs;

  /**
   * Creates a new node
   * @param mn    the method name
   * @param args  the arguments. null if no arguments.
   * @param targs the type arguments
   * @exception IllegalArgumentException if mn is null
   */
  public PolymorphicSuperMethodCall(String mn, List<? extends Expression> args, List<TypeName> targs) {
    this(mn, args, targs, SourceInfo.NONE);
  }
  
  /**
   * Creates a new node
   * @param mn    the method name
   * @param args  the arguments. null if no arguments.
   * @param targs the type arguments
   * @exception IllegalArgumentException if mn is null
   */
  public PolymorphicSuperMethodCall(String mn, List<? extends Expression> args, List<TypeName> targs,
                                    SourceInfo si) {
    super(mn, args, si);
    _typeArgs = targs;
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
  
  /**
   * Implementation of toStringHelper (note that type
   * arguments share in deciding equality, because the
   * default equals() in class Node calls toString())
   */

  public List<TypeName> getTypeArguments(){ return _typeArgs; }

  public String toStringHelper() {
//    List<TypeName> tp = getTypeArguments();
//    String typeArgsStr = "";
//    if(tp.size()>0)
//      typeArgsStr = ""+tp.get(0);
//    for(int i = 1; i < tp.size(); i++)
//      typeArgsStr = typeArgsStr + " " + tp.get(i);

    return ""+getTypeArguments()+" "+super.toStringHelper();
  }
}
