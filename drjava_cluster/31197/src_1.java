package edu.rice.cs.drjava.model.compiler;

import  java.io.File;
import  java.util.LinkedList;
import  gjc.v6.JavaCompiler;
import  gjc.v6.util.Name;
import  gjc.v6.util.Position;
import  gjc.v6.util.Hashtable;
import  gjc.v6.util.List;
import  gjc.v6.util.Log;


/**
 * The GJ compiler used by DrJava.
 * @version $Id$
 */
public class GJv6Compiler implements CompilerInterface {
  private JavaCompiler _compiler;
  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */
  private OurLog _compilerLog;

  /**
   * Create the compiler.
   */
  public GJv6Compiler () {
  }

  /**
   * Set up new instance of the GJ compiler.
   */
  private void _initCompiler (File sourceRoot) {
    _compilerLog = new OurLog();
    // To use the GJ compiler, we build up the GJ options hashtable.
    Hashtable<String, String> options = Hashtable.make();
    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");
    _compiler = JavaCompiler.make(_compilerLog, options);

    // Build up classpath attribute
    String oldclasspath = System.getProperty("java.class.path");
    String newclasspath = sourceRoot.getAbsolutePath();
    if (oldclasspath.length() > 0) {
      newclasspath += File.pathSeparator;
      newclasspath += oldclasspath;
    }

    options.put("-classpath", newclasspath);
  }

  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    _initCompiler(sourceRoot);
    List<String> filesToCompile = new List<String>();
    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }
    try {
      _compiler.compile(filesToCompile);
    } catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      System.err.println("Compile error: " + t);
      return  new CompilerError[] {
        new CompilerError("", -1, -1, "Compile exception: " + t, false)
      };
    }
    return  _compilerLog.getErrors();
  }

  /**
   * put your documentation comment here
   */
  private class OurLog extends Log {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    /**
     * put your documentation comment here
     * @param source
     * @return 
     */
    public Name useSource(Name source) {
      _sourceName = source.toString();
      return  super.useSource(source);
    }

    /**
     * Overrides Log.print, making it a no-op.
     * This prevents extraneous prints of compiler error messages
     * to the console.
     */
    public void print(String s) {}

    /**
     * put your documentation comment here
     * @param pos
     * @param msg
     */
    public void warning(int pos, String msg) {
      super.warning(pos, msg);
      _errors.addLast(new CompilerError(_sourceName, Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1, msg, true));
    }

    /**
     * put your documentation comment here
     * @param pos
     * @param msg
     */
    public void error(int pos, String msg) {
      super.error(pos, msg);
      _errors.addLast(new CompilerError(_sourceName, Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1, msg, false));
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public CompilerError[] getErrors() {
      return  (CompilerError[])_errors.toArray(new CompilerError[0]);
    }
  }
}



