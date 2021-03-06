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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.LinkedList;
import java.util.List;

// Uses JSR-14 v2.0/JDK 5.0 compiler classes
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;
//import com.sun.tools.javac.util.List; Clashes with java.util.List
import com.sun.tools.javac.util.Log;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.swing.Utilities;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * JSR-14 v2.0, 2.2, 2.3, 2.4, 2.5, javac 1.5.0 beta, and javac 1.5.0. This class can only be 
 * compiled with one of the 2.2+ versions because of the change in the syntax of a variable 
 * number of arguments to a method. It was (Object[] args ...), now it's (Object ... args). 
 * JSR14v2.3+ differs from JSR14v2.2 only in that the "make" method in JavaCompiler is now called 
 * "instance".  Reflection is used to dynamically get the appropriate method to use once the correct 
 * version is detected.
 *
 * @version $Id$
 */
public class Javac150Compiler implements CompilerInterface {
  
  private boolean _isJSR14v2_4;
  private boolean _isJSR14v2_5;
    
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new Javac150Compiler();

  public static final String COMPILER_CLASS_NAME = "com.sun.tools.javac.main.JavaCompiler";
  
  /** A writer that discards its input. */
  private static final Writer NULL_WRITER = new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  };

  /** A no-op printwriter to pass to the compiler to print error messages. */
  private static final PrintWriter NULL_PRINT_WRITER = new PrintWriter(NULL_WRITER);


  /** Constructor for Javac150Compiler will throw a RuntimeException if an invalid version of the JDK is in use.  */ 
  protected Javac150Compiler() {
    if (!_isValidVersion()) {
      throw new RuntimeException("Invalid version of Java compiler.");
    }
    _isJSR14v2_5 = false;
    _isJSR14v2_4 = _isJSR14v2_4();
  }
  
  /** Uses reflection on the Log object to deduce which JDK is being used. If the constructor for Log in this JDK 
   *  does not match that of JSR-14 v2.0, then the version is not supported.
   */
  protected boolean _isValidVersion() {
    
    Class log = com.sun.tools.javac.util.Log.class;

    // The JSR14 1.2 version of the Log instance method
    Class[] validArgs1 = { Context.class };
    
    try { 
      log.getMethod("instance", validArgs1);  // found in Java 5.0 compilers (and JSR14 prototypes >= 1.2)
      try {
        log.getMethod("hasDiagnosticListener");  // only found in Java 6.0 (1.6.0) and later
        return false; // supports Java 6.0 method; hence not a Java 5.0 compiler
      }
      catch(NoSuchMethodException e) {
        return true; // supports Java 5.0 method but does not support Java 6.0 method
      }
    }
    catch (NoSuchMethodException e) {
      return false;  // does not support Java 5.0 Log functionality (added in JSR14 1.2 prototype)
    }
  }
  
/** Compile the given files.
  *  @param files  Source files to compile.
  *  @param classPath  Support jars or directories that should be on the classpath.  If @code{null}, the default is used.
  *  @param sourcePath  Location of additional sources to be compiled on-demand.  If @code{null}, the default is used.
  *  @param destination  Location (directory) for compiled classes.  If @code{null}, the default in-place location is used.
  *  @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with @code{sourceVersion} 
  *                        If @code{null}, the default is used.
  *  @param sourceVersion  The language version of the sources.  Should be consistent with @code{bootClassPath}.  If @code{null},
  *                        the default is used.
  *  @param showWarnings  Whether compiler warnings should be shown or ignored.
  *  @return Errors that occurred. If no errors, should be zero length (not null).
  */
  public List<? extends CompilerError> compile(List<? extends File> files, List<? extends File> classPath, 
                                               List<? extends File> sourcePath, File destination, 
                                               List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
    Context context = _createContext(classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    OurLog log = new OurLog(context);
    JavaCompiler compiler = _makeCompiler(context);
    
    com.sun.tools.javac.util.List<String> filesToCompile = _emptyStringList();
    for (File f : files) {
      // TODO: Can we assume the files are canonical/absolute?  If not, we should use the util methods here.
      filesToCompile = filesToCompile.prepend(f.getAbsolutePath());
    }
    
    try { compiler.compile(filesToCompile); }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      //Added to account for error in javac whereby a variable that was not declared will
      //cause an out of memory error. This change allows us to output both errors and not
      //just the out of memory error
      
      LinkedList<CompilerError> errors = log.getErrors();
      errors.addFirst(new CompilerError("Compile exception: " + t, false));
      return errors;
    }
    
    return log.getErrors();
  }
  
  public boolean isAvailable() {
    try {
      Class.forName(COMPILER_CLASS_NAME);
      try { Class.forName("java.lang.Enum"); }
      catch (Exception e) {
        // If java.lang.Enum is not found, there's a chance the user specified JSR14v2.5 
        // For some reason, java.lang.Enum got moved to collect.jar which we can't put on the
        // bootclasspath.  Look for something 2.5 specific.
        Class.forName("com.sun.tools.javac.main.Main$14");
        _isJSR14v2_5 = true;
      }
      return _isValidVersion();
    }
    catch (Exception e) { return false; }
  }

  public String getName() {
    if (_isJSR14v2_5) return "JSR-14 v2.5";
    else if (_isJSR14v2_4) return "JSR-14 v2.4";
    else return "JSR-14 v2.0/2.2";// + com.sun.tools.javac.Main.class.getResource("Main.class");
  }

  public String toString() { return getName(); }


  protected Context _createContext(List<? extends File> classPath, List<? extends File> sourcePath, File destination, 
                                   List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
    Context context = new Context();
    Options options = Options.instance(context);
    options.putAll(CompilerOptions.getOptions(showWarnings));
    
    //Should be setable some day?
    options.put("-g", "");

    if (classPath != null) { options.put("-classpath", _pathToString(classPath)); }
    if (sourcePath != null) { options.put("-sourcepath", _pathToString(sourcePath)); }
    if (destination != null) { options.put("-d", destination.getPath()); }
    if (bootClassPath != null) { options.put("-bootclasspath", _pathToString(bootClassPath)); }
    if (sourceVersion != null) { options.put("-source", sourceVersion); }
    if (!showWarnings) { options.put("-nowarn", ""); }
    
    return context;
  }

  protected static String _pathToString(List<? extends File> path) {
    StringBuffer result = new StringBuffer();
    boolean first = true;
    for (File f : path) {
      if (!first) { result.append(File.pathSeparatorChar); }
      first = false;
      result.append(f.getPath());
    }
    return result.toString();
  }
  
  
  protected JavaCompiler _makeCompiler(Context context) {
    // Using reflection to allow for JSR14v2.3 since the "make"
    // method was changed to "instance".
    Class javaCompilerClass = JavaCompiler.class;
//    Utilities.show("Compiler Class is: " + javaCompilerClass);
    Class[] validArgs1 = {
      Context.class
    };
    Method m;    
    if (_isJSR14v2_4) {    
      try { 
        m = javaCompilerClass.getMethod("instance", validArgs1);
        return (JavaCompiler)m.invoke(null, new Object[] {context});
      }
      catch (NoSuchMethodException e) { throw new UnexpectedException(e); }
      catch (IllegalAccessException e) { throw new UnexpectedException(e); }
      catch (InvocationTargetException e) {
        e.printStackTrace();
        throw new UnexpectedException(e);
      }      
    }
    else {
      try { 
        m = javaCompilerClass.getMethod("make", validArgs1);
        return (JavaCompiler)m.invoke(null, new Object[] {context});
      }
      catch (NoSuchMethodException e) { throw new UnexpectedException(e); }
      catch (IllegalAccessException e) { throw new UnexpectedException(e); }
      catch (InvocationTargetException e) { throw new UnexpectedException(e); }
//      compiler = JavaCompiler.make(context);
    }
  }
  
  /** Check if we're using JSR14v2.4 or 2.5 (skipping version 2.3 because it will never be officially released). */
  private boolean _isJSR14v2_4() {
    try {
      Class.forName("com.sun.tools.javac.main.Main$14");
      return true;
    }
    catch (Exception e) {
      try {
        Class.forName("com.sun.tools.javac.main.Main+1");
        return true;
      }
      catch (Exception e2) { return false; }
    }
  }

  /** Get an empty List using reflection, since the method to do so changed  with version 1.5.0_04. */
  private com.sun.tools.javac.util.List<String> _emptyStringList() {
    try {
      Method nil = List.class.getMethod("nil");
      @SuppressWarnings("unchecked") com.sun.tools.javac.util.List<String> result = 
        (com.sun.tools.javac.util.List<String>) nil.invoke(null);
      return result;
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException("Exception occured when invoking com.sun.tools.javac.util.List.nil()", e);
    }
    catch (Exception e) {
      try { 
        @SuppressWarnings("unchecked") com.sun.tools.javac.util.List<String> result = 
          (com.sun.tools.javac.util.List<String>) com.sun.tools.javac.util.List.class.newInstance();
        return result;
      }
      catch (Exception e2) {
        throw new RuntimeException("Unable to create an instance of com.sun.tools.javac.util.List", e);
      }
    }
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4.1+
   * or JSR14 v1.2+.
   */
  private static class OurLog extends Log {
    // List of CompilerError
    private LinkedList<CompilerError> _errors = new LinkedList<CompilerError>();
    private String _sourceName = "";

    public OurLog(Context context) { super(context, NULL_PRINT_WRITER, NULL_PRINT_WRITER, NULL_PRINT_WRITER); }

    /** JSR14 uses this crazy signature on warning method because it localizes the warning message. */
    public void warning(int pos, String key, Object ... args) {
      super.warning(pos, key, args);
      //System.out.println("warning: pos = " + pos);

      String msg = getText("compiler.warn." + key, args);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        true));
    }

    /** JSR14 uses this crazy signature on error method because it localizes the error message. */
    public void error(int pos, String key, Object ... args) {
      super.error(pos, key, args);
      //System.out.println("error: pos = " + pos);

      String msg = getText("compiler.err." + key, args);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        false));
    }

    public LinkedList<CompilerError> getErrors() { return _errors; }
  }
}
