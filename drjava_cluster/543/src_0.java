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

package koala.dynamicjava.tree;

import java.util.*;

/**
 * This class represents a type declaration
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/10
 */

public abstract class TypeDeclaration extends Declaration {

  /**
   * The name property name
   */
  public final static String NAME = "name";

  /**
   * The interfaces property name
   */
  public final static String INTERFACES = "interfaces";

  /**
   * The members property name
   */
  public final static String MEMBERS = "members";

  /**
   * The name of this class
   */
  private String name;

  /**
   * The implemented interfaces
   */
  private List<? extends ReferenceTypeName> interfaces;

  /**
   * The members
   */
  private List<Node> members;

  /**
   * Creates a new class declaration
   * @param mods  the modifiers
   * @param name  the name of the class to declare
   * @param impl  the list of implemented interfaces (List of List of Token). Can be null.
   * @param body  the list of fields declarations
   * @exception IllegalArgumentException if name is null or body is null
   */
  protected TypeDeclaration(ModifierSet mods, String name, List<? extends ReferenceTypeName> impl, List<Node> body,
                            SourceInfo si) {
    super(mods, si);
    if (name == null) throw new IllegalArgumentException("name == null");
    if (body == null) throw new IllegalArgumentException("body == null");
    this.name = name;
    interfaces = impl;
    members = body;
  }

  /**
   * Returns the name of this class
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the type's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    firePropertyChange(NAME, name, name = s);
  }

  /**
   * Returns a list that contains the names (String) of the implemented interfaces.
   * Can be null.
   */
  public List<? extends ReferenceTypeName> getInterfaces() {
    return interfaces;
  }

  /**
   * Sets the interfaces (a list of strings)
   */
  public void setInterfaces(List<? extends ReferenceTypeName> l) {
    firePropertyChange(INTERFACES, interfaces, interfaces = l);
  }

  /**
   * Returns the list of the declared members
   */
  public List<Node> getMembers() {
    return members;
  }

  /**
   * Sets the members
   * @exception IllegalArgumentException if l is null
   */
  public void setMembers(List<Node> l) {
    if (l == null) throw new IllegalArgumentException("l == null");

    firePropertyChange(MEMBERS, members, members = l);
  }
}
