/* BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 *
 *END_COPYRIGHT_BLOCK */

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;


import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;

/** Default implementation of the CompilerModel interface. This implementation is used for normal DrJava execution
 *  (as opposed to testing DrJava).
 *  @version $Id$
 */
public class DefaultCompilerModel implements CompilerModel {
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();

  /** The global model to which this compiler model belongs. */
  private final GlobalModel _model;

  /** The error model containing all current compiler errors. */
  private CompilerErrorModel _compilerErrorModel;
  
  /** The working directory corresponding to the last compilation */
  private File _workDir;
  
  /** The lock providing mutual exclustion between compilation and unit testing */
  private Object _compilerLock = new Object();

  /** Main constructor.  
    * @param m the GlobalModel that is the source of documents for this CompilerModel
    */
  public DefaultCompilerModel(GlobalModel m) {
    _model = m;
    _compilerErrorModel = new CompilerErrorModel(new CompilerError[0], _model);
    _workDir = _model.getWorkingDirectory();
  }
  
  //--------------------------------- Locking -------------------------------//
  
  /** Returns the lock used to prevent simultaneous compilation and JUnit testing */
  public Object getCompilerLock() { return _compilerLock; }

  //-------------------------- Listener Management --------------------------//

  /** Add a CompilerListener to the model.
   *  @param listener a listener that reacts to compiler events
   * 
   *  This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
   */
  public void addListener(CompilerListener listener) { _notifier.addListener(listener); }

  /** Remove a CompilerListener from the model.  If the listener is not currently
   *  listening to this model, this method has no effect.
   *  @param listener a listener that reacts to compiler events
   * 
   *  This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
   */
  public void removeListener(CompilerListener listener) { _notifier.removeListener(listener); }

  /** Removes all CompilerListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }

  //-------------------------------- Triggers --------------------------------//


  /** Compile all open documents.
   *
   *  <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
   *  step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
   *  the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
   *  (which includes drjava.jar, containing JUnit classes).</p>
   *  
   *  This method formerly only compiled documents which were out of sync with their class file, as a performance 
   *  optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
   *  case this command would not recompile a file in some situations when it should.  Since we value correctness over
   *  performance, we now always compile all open documents.</p>
   *
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compileAll() throws IOException {
    if (_prepareForCompile()) {
      _doCompile(_model.getOpenDefinitionsDocuments());
    }
  }
  
   /** Compiles all documents in the project source tree.  Assumes DrJava currently contains an active project.
    *
    *  <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    *  step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    *  the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    *  (which includes drjava.jar, containing JUnit classes).</p>
    *  
    *  This method formerly only compiled documents which were out of sync with their class file, as a performance 
    *  optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    *  case this command would not recompile a file in some situations when it should.  Since we value correctness over
    *  performance, we now always compile all open documents.</p>
    *
    *  @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileProject() throws IOException {
    if (! _model.isProjectActive()) 
      throw new UnexpectedException("compileProject invoked when DrJava is not in project mode");
    
    if (_prepareForCompile()) {
      _doCompile(_model.getProjectDocuments());
    }
  }
  
  /** Compiles all of the given files.
   *
   *  <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
   *  step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
   *  the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
   *  (which includes drjava.jar, containing JUnit classes).</p>
   *  
   *  This method formerly only compiled documents which were out of sync with their class file, as a performance 
   *  optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
   *  case this command would not recompile a file in some situations when it should.  Since we value correctness over
   *  performance, we now always compile all open documents.</p>
   *
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
    if (_prepareForCompile()) {
      _doCompile(defDocs);
    }
  }
  
  /** Compiles the given file.
    *
    *  <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    *  step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    *  the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    *  (which includes drjava.jar, containing JUnit classes).</p>
    *  
    *  This method formerly only compiled documents which were out of sync with their class file, as a performance 
    *  optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    *  case this command would not recompile a file in some situations when it should.  Since we value correctness over
    *  performance, we now always compile all open documents.</p>
    *
    *  @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(OpenDefinitionsDocument doc) throws IOException {
    if (_prepareForCompile()) {
      _doCompile(Arrays.asList(doc));
    }
  }
  
  /** Check that there are no unsaved or untitled files currently open.
    * @return  @code{true} iff compilation should continue
    */
  private boolean _prepareForCompile() {
    if (_model.hasModifiedDocuments()) _notifier.saveBeforeCompile();
    // If user cancelled save, abort compilation
    return !_model.hasModifiedDocuments();
  }
  
  /** Compile the given documents. */
  private void _doCompile(List<OpenDefinitionsDocument> docs) throws IOException {
    ArrayList<File> filesToCompile = new ArrayList<File>();
    ArrayList<File> excludedFiles = new ArrayList<File>();
    for (OpenDefinitionsDocument doc : docs) {
      if (doc.isSourceFile()) {
        File f = doc.getFile();
        // Check for null in case the file is untitled (not sure this is the correct check)
        if (f != null) { filesToCompile.add(f); }
        doc.setCachedClassFile(null); // clear cached class file
      }
      else excludedFiles.add(doc.getFile());
    } 
    
    File buildDir = _model.getBuildDirectory();
    if ((buildDir!=null) && !buildDir.exists() && !buildDir.mkdirs()) {
      throw new IOException("Could not create build directory: "+buildDir);
    }

    File workDir = _model.getWorkingDirectory(); 
    if ((workDir != null) && ! workDir.exists() && ! workDir.mkdirs()) {
      throw new IOException("Could not create working directory: "+workDir);
    }
     
    _notifier.compileStarted();
    try { _compileFiles(filesToCompile, buildDir); }
    catch (Throwable t) {
      CompilerError err = new CompilerError(t.toString(), false);
      _distributeErrors(Arrays.asList(err));
    }
    finally { _notifier.compileEnded(workDir, excludedFiles); }
  }
  

  //-------------------------------- Helpers --------------------------------//

  /** Converts JExprParseExceptions thrown by the JExprParser in language levels to CompilerErrors. */
  private LinkedList<CompilerError> _parseExceptions2CompilerErrors(LinkedList<JExprParseException> pes) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<JExprParseException> iter = pes.iterator();
    while (iter.hasNext()) {
      JExprParseException pe = iter.next();
      errors.addLast(new CompilerError(pe.getFile(), pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, pe.getMessage(), false));
    }
    return errors;
  }
  
  /** Converts errors thrown by the language level visitors to CompilerErrors. */
  private LinkedList<CompilerError> _visitorErrors2CompilerErrors(LinkedList<Pair<String, JExpressionIF>> visitorErrors) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<Pair<String, JExpressionIF>> iter = visitorErrors.iterator();
    while (iter.hasNext()) {
      Pair<String, JExpressionIF> pair = iter.next();
      String message = pair.getFirst();      
//      System.out.println("Got error message: " + message);
      JExpressionIF jexpr = pair.getSecond();
      
      SourceInfo si;
      if (jexpr == null) si = JExprParser.NO_SOURCE_INFO;
      else si = pair.getSecond().getSourceInfo();
      
      errors.addLast(new CompilerError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  
  /** Compile the given files and update the model with any errors that result.  Does not notify listeners.  
   *  All public compile methods delegate to this one so this method is the only one that uses synchronization to 
   *  prevent compiling and unit testing at the same time.
   * 
   * @param files The files to be compiled
   * @param buildDir The output directory for all the .class files; @code{null} means output to the same 
   *                 directory as the source file
   * 
   */
  private void _compileFiles(List<? extends File> files, File buildDir) throws IOException {
    if (!files.isEmpty()) {
      /* Canonicalize buildDir */
      if (buildDir != null) buildDir = FileOps.getCanonicalFile(buildDir);
      
      List<File> classPath = _model.getClassPath().asFileVector();
      
      // Temporary hack to allow a boot class path to be specified
      List<File> bootClassPath = null;
      if (System.getProperty("drjava.bootclasspath") != null) {
        bootClassPath = new LinkedList<File>();
        StringTokenizer st = new StringTokenizer(System.getProperty("drjava.bootclasspath"), File.pathSeparator);
        while (st.hasMoreTokens()) {
          bootClassPath.add(new File(st.nextToken()));
        }
      }
      
      List<CompilerError> errors = new LinkedList<CompilerError>();
      
      List<? extends File> preprocessedFiles = _compileLanguageLevelsFiles(files, errors);
      
      if (errors.isEmpty()) {
        CompilerInterface compiler = CompilerRegistry.ONLY.getActiveCompiler();
        
        synchronized(_compilerLock) {
          if (preprocessedFiles == null) {
            errors.addAll(compiler.compile(files, classPath, null, buildDir, bootClassPath, null, true));
          }
          else {
            /** If compiling a language level file, do not show warnings, as these are not caught by the language level parser */
            errors.addAll(compiler.compile(preprocessedFiles, classPath, null, buildDir, bootClassPath, null, false));
          }
        }
      }
      _distributeErrors(errors);
    }
    else { 
      // TODO: Is this necessary?
      _distributeErrors(Collections.<CompilerError>emptyList());
    }
  }
  
  
  /** Compiles the language levels files in the list.  Adds any errors to the given error list.
    * @return  An updated list for compilation containing no Language Levels files, or @code{null}
    *          if there were no Language Levels files to process.
    */
  private List<? extends File> _compileLanguageLevelsFiles(List<? extends File> files, List<? super CompilerError> errors) {
    // TODO: The classpath (and sourcepath, bootclasspath) should be an argument passed to Language Levels.
    LanguageLevelConverter llc = new LanguageLevelConverter(getActiveCompiler().getName());
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> llErrors = 
      llc.convert(files.toArray(new File[0]));
    
    /* Rename any .dj0 files in files to be .java files, so the correct thing is compiled.  The hashset is used to 
     * make sure we never send in duplicate files. This can happen if the java file was sent in along with the 
     * corresponding .dj* file. The dj* file is renamed to a .java file and thus we have two of the same file in 
     * the list.  By adding the renamed file to the hashset, the hashset efficiently removes duplicates.
     */
    HashSet<File> javaFileSet = new HashSet<File>();
    boolean containsLanguageLevels = false;
    for (File f : files) {
      File canonicalFile = FileOps.getCanonicalFile(f);
      String fileName = canonicalFile.getPath();
      int lastIndex = fileName.lastIndexOf(".dj");
      if (lastIndex != -1) {
        containsLanguageLevels = true;
        javaFileSet.add(new File(fileName.substring(0, lastIndex) + ".java"));
      }
      else { javaFileSet.add(canonicalFile); }
    }
    files = new LinkedList<File>(javaFileSet);
    
    errors.addAll(_parseExceptions2CompilerErrors(llErrors.getFirst()));
    errors.addAll(_visitorErrors2CompilerErrors(llErrors.getSecond()));
    if (containsLanguageLevels) { return files; }
    else { return null; }
  }
  
  /** Sorts the given array of CompilerErrors and divides it into groups based on the file, giving each group to the
    * appropriate OpenDefinitionsDocument, opening files if necessary.  Called immediately after compilations finishes.
    */
  private void _distributeErrors(List<? extends CompilerError> errors) throws IOException {
//    resetCompilerErrors();  // Why is this done?
    _compilerErrorModel = new CompilerErrorModel(errors.toArray(new CompilerError[0]), _model);
    _model.setNumCompErrors(_compilerErrorModel.getNumCompErrors());  // cache number of compiler errors in global model
  }

  //----------------------------- Error Results -----------------------------//

  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel() { return _compilerErrorModel; }

  /** Gets the total number of errors in this compiler model. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }
  
  /** Gets the total number of current compiler errors. */
  public int getNumCompErrors() { return getCompilerErrorModel().getNumCompErrors(); }
  
  /** Gets the total number of current warnings. */  
  public int getNumWarnings() { return getCompilerErrorModel().getNumWarnings(); }

  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel(new CompilerError[0], _model);
  }

  //-------------------------- Compiler Management --------------------------//

  /**
   * Returns all registered compilers that are actually available.
   * That is, for all elements in the returned array, .isAvailable()
   * is true.
   * This method will never return null or a zero-length array.
   * Instead, if no compiler is registered and available, this will return
   * a one-element array containing an instance of
   * {@link NoCompilerAvailable}.
   *
   * @see CompilerRegistry#getAvailableCompilers
   */
  public CompilerInterface[] getAvailableCompilers() {
    return CompilerRegistry.ONLY.getAvailableCompilers();
  }

  /**
   * Gets the compiler that is the "active" compiler.
   *
   * @see #setActiveCompiler
   * @see CompilerRegistry#getActiveCompiler
   */
  public CompilerInterface getActiveCompiler() {
    return CompilerRegistry.ONLY.getActiveCompiler();
  }

  /**
   * Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   *
   * @see #getActiveCompiler
   * @see CompilerRegistry#setActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    CompilerRegistry.ONLY.setActiveCompiler(compiler);
  }
}
