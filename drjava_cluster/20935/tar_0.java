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

package edu.rice.cs.util.classloader;

import junit.framework.*;
import java.net.*;

/**
 * Test cases for {@link StrictURLClassLoader}.
 *
 * @version $Id$
 */
public class StrictURLClassLoaderTest extends TestCase {
  /**
   * Make sure this loader doesn't load classes from the system
   * classloader.
   */
  public void testWontLoadFromSystem() throws Throwable {
    StrictURLClassLoader loader = new StrictURLClassLoader(new URL[0]);
    String myName = getClass().getName();

    try {
      loader.loadClass(myName);
      fail("should not have loaded class");
    }
    catch (ClassNotFoundException e) {
      // yep, we expected it to fail
    }
  }

  /**
   * Make sure this loader doesn't load resources from the bootclasspath.
   */
  public void testWontLoadResourceFromBootClassPath() throws Throwable {
    StrictURLClassLoader loader = new StrictURLClassLoader(new URL[0]);
    String compiler = "com/sun/tools/javac/util/Log.class";

    URL resource = loader.getResource(compiler);
    assertTrue("should not have found resource", resource == null);
  }

  /**
   * Make sure this loader can load from the given URLs.
   */
  public void testWillLoadClassFromGivenURLs() throws Throwable {
    String logResource = "com/sun/tools/javac/Main.class";
    String compilerClass = "com.sun.tools.javac.Main";
    URL[] urls = ToolsJarClassLoader.getToolsJarURLs();

    if (urls.length > 0) {
      //System.out.println("testing urls");
      StrictURLClassLoader loader = new StrictURLClassLoader(urls);

      Class c = loader.loadClass(compilerClass);
      assertEquals("loaded class", compilerClass, c.getName());

    
      URL resource = loader.getResource(logResource);
      assertTrue("resource found", resource != null);
    }
  }
}
