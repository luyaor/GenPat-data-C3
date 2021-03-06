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

package edu.rice.cs.drjava.model.junit;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.DummyCompilerListener;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

//import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.text.SwingDocument;
import edu.rice.cs.util.swing.Utilities;

import org.apache.bcel.classfile.*;

import static edu.rice.cs.drjava.config.OptionConstants.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Manages unit testing via JUnit.
  * @version $Id$
  */
public class DefaultJUnitModel implements JUnitModel, JUnitModelCallback {
  
  /** Manages listeners to this model. */
  private final JUnitEventNotifier _notifier = new JUnitEventNotifier();
  
  /** RMI interface to a secondary JVM for running tests.  Using a second JVM prevents interactions and tests from 
    * corrupting the state of DrJava.
    */
  private final MainJVM _jvm;
  
  /** The compiler model.  It contains a lock used to prevent simultaneous test and compile.  It also tracks the number
    * errors in the last compilation, which is required information if junit forces compilation.  
    */
  private final CompilerModel _compilerModel;
  
  /** The global model to which the JUnitModel belongs */
  private final GlobalModel _model;
  
  /** The error model containing all current JUnit errors. */
  private volatile JUnitErrorModel _junitErrorModel;
  
  /** State flag to prevent starting new tests on top of old ones */
  private volatile boolean _testInProgress = false;
  
  /** State flag to record if test classes in projects must end in "Test" */
  private boolean _forceTestSuffix = false;
  
  /** The document used to display JUnit test results.  Used only for testing. */
  private final SwingDocument _junitDoc = new SwingDocument();
  
  /** Main constructor.
    * @param jvm RMI interface to a secondary JVM for running tests
    * @param compilerModel the CompilerModel, used only as a lock to prevent simultaneous test and compile
    * @param model used only for getSourceFile
    */
  public DefaultJUnitModel(MainJVM jvm, CompilerModel compilerModel, GlobalModel model) {
    _jvm = jvm;
    _compilerModel = compilerModel;
    _model = model;
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], _model, false);
  }
  
  //-------------------------- Field Setters --------------------------------//
  
  public void setForceTestSuffix(boolean b) { _forceTestSuffix = b; }
  
  //------------------------ Simple Predicates ------------------------------//
  
  public boolean isTestInProgress() { return _testInProgress;  }

  //------------------------Listener Management -----------------------------//
  
  /** Add a JUnitListener to the model.
   *  @param listener a listener that reacts to JUnit events
   */
  public void addListener(JUnitListener listener) { _notifier.addListener(listener); }
  
  /** Remove a JUnitListener from the model.  If the listener is not currently listening to this model, this method 
   *  has no effect.
   *  @param listener a listener that reacts to JUnit events
   */
  public void removeListener(JUnitListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all JUnitListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  

  
  //-------------------------------- Triggers --------------------------------//
  
  /** Used only for testing. */
  public SwingDocument getJUnitDocument() { return _junitDoc; }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If the class file 
   *  associated with a file is not a test case, it is ignored.  
   */
  public void junitAll() { junitDocs(_model.getOpenDefinitionsDocuments()); }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If a class file associated with a 
   *  source file is not a test case, it will be ignored.  Synchronized against the compiler model to prevent 
   *  testing and compiling at the same time, which would create invalid results.
   */
  public void junitProject() {
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    
    for (OpenDefinitionsDocument doc : _model.getOpenDefinitionsDocuments()) { 
      if (doc.inProjectPath())  lod.add(doc);
    }
    junitOpenDefDocs(lod, true);
  }
  
  /** Forwards the classnames and files to the test manager to test all of them; does not notify 
   *  since we don't have ODD's to send out with the notification of junit start.
   *  @param qualifiedClassnames a list of all the qualified class names to test.
   *  @param files a list of their source files in the same order as qualified class names.
   */
  public void junitClasses(List<String> qualifiedClassnames, List<File> files) {
    Utilities.showDebug("junitClasses(" + qualifiedClassnames + ", " + files);
    synchronized(_compilerModel.getCompilerLock()) {
      
      // Check _testInProgress 
      if (_testInProgress) return;
      
      List<String> testClasses;
      try { testClasses = _jvm.findTestClasses(qualifiedClassnames, files); }
      catch(IOException e) { throw new UnexpectedException(e); }
      
//      System.err.println("Found test classes: " + testClasses);
      
      if (testClasses.isEmpty()) {
        nonTestCase(true);
        return;
      }
      _notifier.junitClassesStarted(); 
      try { _jvm.runTestSuite(); } 
      catch(Exception e) {
//        System.err.println("Threw exception " + e);
        _notifier.junitEnded();
        _testInProgress = false;
        throw new UnexpectedException(e); 
      }
    }
  }
  
  public void junitDocs(List<OpenDefinitionsDocument> lod) { junitOpenDefDocs(lod, true); }
  
  /** Runs JUnit on the current document.  Forces the user to compile all open source documents before proceeding. */
  public void junit(OpenDefinitionsDocument doc) throws ClassNotFoundException, IOException {
    debug.logStart("junit(doc)");
//    new ScrollableDialog(null, "junit(" + doc + ") called in DefaultJunitModel", "", "").show();
    File testFile;
    try { 
      testFile = doc.getFile(); 
      if (testFile == null) {  // document is untitiled: abort unit testing and return
        nonTestCase(false);
        debug.logEnd("junit(doc): no corresponding file");
        return;
      }
    } 
    catch(FileMovedException fme) { /* do nothing */ }
    
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    lod.add(doc);
    junitOpenDefDocs(lod, false);
    debug.logEnd("junit(doc)");
  }
  
  /** Ensures that all documents have been compiled since their last modification and then delegates the actual testing
   *  to _rawJUnitOpenTestDocs. */
  private void junitOpenDefDocs(final List<OpenDefinitionsDocument> lod, final boolean allTests) {
    // If a test is running, don't start another one.
    
//    System.err.println("junitOpenDefDocs(" + lod + "," + allTests + ")");
    
    // Check_testInProgress flag
    if (_testInProgress) return; 

    // Reset the JUnitErrorModel, fixes bug #907211 "Test Failures Not Cleared Properly".
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
    
    if (_model.hasOutOfSyncDocuments(lod) || _model.hasModifiedDocuments(lod)) {
      /* hasOutOfSyncDocments(lod) can return false when some documents have not been successfully compiled; the 
       * granularity of time-stamping and the presence of multiple classes in a file (some of which compile 
       * successfully) can produce false reports.  */
//      System.err.println("Out of sync documents exist");
        
        CompilerListener testAfterCompile = new DummyCompilerListener() {
          @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
            final CompilerListener listenerThis = this;
            try {
              if (_model.hasOutOfSyncDocuments(lod) || _model.getNumCompErrors() > 0) {
                if (! Utilities.TEST_MODE) 
                  JOptionPane.showMessageDialog(null, "All open files must be compiled before running a unit test", 
                                              "Must Compile All Before Testing", JOptionPane.ERROR_MESSAGE); 
                nonTestCase(allTests);
                return;
              }
              _rawJUnitOpenDefDocs(lod, allTests);
            }
            finally {  // always remove this listener after its first execution
              SwingUtilities.invokeLater(new Runnable() { 
                public void run() { _compilerModel.removeListener(listenerThis); }
              });
            }
          }
        };
        
//        Utilities.show("Notifying JUnitModelListener");
        _notifier.compileBeforeJUnit(testAfterCompile);
      }
      
      else _rawJUnitOpenDefDocs(lod, allTests);
  }
  
  /** Runs all TestCases in the document list lod; assumes all documents have been compiled. It finds the TestCase 
   *  classes by searching the build directories for the documents. */
  private void _rawJUnitOpenDefDocs(List<OpenDefinitionsDocument> lod, boolean allTests) {
    File buildDir = _model.getBuildDirectory();
//    System.err.println("Build directory is " + buildDir);
    
    /** Open java source files */
    HashSet<String> openDocFiles = new HashSet<String>();
    
    /** A map whose keys are directories containing class files corresponding to open java source files.
     *  Their values are the corresponding source roots. 
     */
    HashMap<File, File> classDirsAndRoots = new HashMap<File, File>();
    
    // Initialize openDocFiles and classDirsAndRoots
    // All packageNames should be valid because all source files are compiled
    
    for (OpenDefinitionsDocument doc: lod) /* for all nonEmpty documents in lod */ {
      if (doc.isSourceFile())  { // excludes Untitled documents and open non-source files
        try {
          File sourceRoot = doc.getSourceRoot(); // may throw an InvalidPackageException
          
          // doc has valid package name; add it to list of open java source doc files
          openDocFiles.add(doc.getCanonicalPath());
        
          String packagePath = doc.getPackageName().replace('.', File.separatorChar);
          
          // Add (canonical path name for) build directory for doc to classDirs
          
          File buildRoot = (buildDir == null) ? sourceRoot: buildDir;
          
          File classFileDir = new File(IOUtil.attemptCanonicalFile(buildRoot), packagePath);
          
          File sourceDir = 
            (buildDir == null) ? classFileDir : new File(IOUtil.attemptCanonicalFile(sourceRoot), packagePath);
          
          if (! classDirsAndRoots.containsKey(classFileDir)) {
            classDirsAndRoots.put(classFileDir, sourceDir);
//          System.err.println("Adding " + classFileDir + " with source root " + sourceRoot + 
//          " to list of class directories");
          }
        }
        catch (InvalidPackageException e) { /* Skip the file, since it doesn't have a valid package */ }
      }
    }
    
//    System.err.println("classDirs = " + classDirsAndRoots.keySet());
    
    /** set of dirs potentially containing test classes */
    Set<File> classDirs = classDirsAndRoots.keySet();
    
//    System.err.println("openDocFiles = " + openDocFiles);
        
    /* Names of test classes. */
    ArrayList<String> classNames = new ArrayList<String>();
    
    /* Source files corresonding to potential test class files */
    ArrayList<File> files = new ArrayList<File>();
    
    /* Flag indicating if project is open */
    boolean isProject = _model.isProjectActive();

    try {
      for (File dir: classDirs) { // foreach class file directory
//        System.err.println("Examining directory " + dir);
        
        File[] listing = dir.listFiles();
        
//        System.err.println("Directory contains the files: " + Arrays.asList(listing));
        
        if (listing != null) { // listFiles may return null if there's an IO error
          for (File entry : listing) { /* for each class file in the build directory */        
            
//            System.err.println("Examining file " + entry);
            
            /* ignore non-class files */
            String name = entry.getName();
            if (! name.endsWith(".class")) continue;
            
            /* In projects, ignore class names that do not end in "Test" if FORCE_TEST_SUFFIX option is set */
            if (_forceTestSuffix) {
              String noExtName = name.substring(0, name.length() - 6);  // remove ".class" from name
              int indexOfLastDot = noExtName.lastIndexOf('.');
              String simpleClassName = noExtName.substring(indexOfLastDot + 1);
//            System.err.println("Simple class name is " + simpleClassName);
              if (isProject && ! simpleClassName.endsWith("Test")) continue;
            }
            
//            System.err.println("Found test class: " + noExtName);
            
            /* ignore entries that do not correspond to files?  Can this happen? */
            if (! entry.isFile()) continue;
            
            // Add this class and the corrresponding source file to classNames and files, respectively.
            // Finding the source file is non-trivial because it may be a language-levels file
            
            try {
              JavaClass clazz = new ClassParser(entry.getCanonicalPath()).parse();
              String className = clazz.getClassName(); // get classfile name
//              System.err.println("looking for source file for: " + className);
              int indexOfDot = className.lastIndexOf('.');
              
              File rootDir = classDirsAndRoots.get(dir);
              
              /** The canonical pathname for the file (including the file name) */
              String javaSourceFileName = rootDir.getCanonicalPath() + File.separator + clazz.getSourceFileName();
//              System.err.println("Full java source fileName = " + javaSourceFileName);
              
              /* The index in fileName of the dot preceding the extension ".java", ".dj0*, ".dj1", or ".dj2" */
              int indexOfExtDot = javaSourceFileName.lastIndexOf('.');
//              System.err.println("indexOfExtDot = " + indexOfExtDot);
              if (indexOfExtDot == -1) continue;  // RMI stub class files return source file names without extensions
//              System.err.println("File found in openDocFiles = "  + openDocFiles.contains(sourceFileName));
              
              /* Determine if this java source file was generated from a language levels file. */
              String strippedName = javaSourceFileName.substring(0, indexOfExtDot);
//              System.err.println("Stripped name = " + strippedName);
              
              String sourceFileName;
              
              if (openDocFiles.contains(javaSourceFileName)) sourceFileName = javaSourceFileName;
              else if (openDocFiles.contains(strippedName + ".dj0")) sourceFileName = strippedName + ".dj0";
              else if (openDocFiles.contains(strippedName + ".dj1")) sourceFileName = strippedName + ".dj1";
              else if (openDocFiles.contains(strippedName + ".dj2")) sourceFileName = strippedName + ".dj2";
              else continue; // no matching source file is open
              
              File sourceFile = new File(sourceFileName);
              classNames.add(className);
              files.add(sourceFile);
//              System.err.println("Class " + className + "added to classNames.   File " + sourceFileName + " added to files.");
            }
            catch(IOException e) { /* ignore it; can't read class file */ }
            catch(ClassFormatException e) { /* ignore it; class file is bad */ }
          }
        }
      }
    }
    catch(Exception e) {
//      new ScrollableDialog(null, "UnexceptedExceptionThrown", e.toString(), "").show();
      throw new UnexpectedException(e); 
    }
//    finally { 
//      new ScrollableDialog(null, "junit setup loop terminated", classNames.toString(), "").show();
//    }
    
    // synchronized over _compilerModel to ensure that compilation and junit testing are mutually exclusive.
    // TODO: should we disable compile commands while testing?  Should we use protected flag instead of lock?
   
    synchronized(_compilerModel.getCompilerLock()) {
      /** Set up junit test suite on slave JVM; get TestCase classes forming that suite */
      List<String> tests;
      try { tests = _jvm.findTestClasses(classNames, files); }
      catch(IOException e) { throw new UnexpectedException(e); }
      
      if (tests == null || tests.isEmpty()) {
        nonTestCase(allTests);
        return;
      }
      
      try {
        /** Run the junit test suite that has already been set up on the slave JVM */
        _notifier.junitStarted(); // notify listeners that JUnit testing has finally started!
        //          new ScrollableDialog(null, "junitStarted executed in DefaultJunitModel", "", "").show();
        _jvm.runTestSuite();
        
      }
      catch(Exception e) {
        // Probably a java.rmi.UnmarshalException caused by the interruption of unit testing.
        // Swallow the exception and proceed.
        _notifier.junitEnded();  // balances junitStarted()
        _testInProgress = false;
        throw new UnexpectedException(e);
      }
    }
  }
  
  //-------------------------------- Helpers --------------------------------//
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the JUnitErrorModel, which contains error info for the last test run. */
  public JUnitErrorModel getJUnitErrorModel() { return _junitErrorModel; }
  
  /** Resets the junit error state to have no errors. */
  public void resetJUnitErrors() {
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], _model, false);
  }
  
  //---------------------------- Model Callbacks ----------------------------//
  
  /** Called from the JUnitTestManager if its given className is not a test case.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(final boolean isTestAll) {
    // NOTE: junitStarted is called in a different thread from the testing thread.  The _testInProgress flag
    //       is used to prevent a new test from being started and overrunning the existing one.
//      Utilities.show("DefaultJUnitModel.nonTestCase(" + isTestAll + ") called");
      _notifier.nonTestCase(isTestAll);
      _testInProgress = false;
  }
  
  /** Called to indicate that an illegal class file was encountered
   *  @param e the ClassFileObject describing the error.
   */
  public void classFileError(ClassFileError e) { _notifier.classFileError(e); }
  
  /** Called to indicate that a suite of tests has started running.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(final int numTests) { _notifier.junitSuiteStarted(numTests); }
  
  /** Called when a particular test is started.
   *  @param testName The name of the test being started.
   */
  public void testStarted(final String testName) { _notifier.junitTestStarted(testName); }
  
  /** Called when a particular test has ended.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(final String testName, final boolean wasSuccessful, final boolean causedError) {
     _notifier.junitTestEnded(testName, wasSuccessful, causedError);
  }
  
  /** Called when a full suite of tests has finished running.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) called", "", "").show();
    _junitErrorModel = new JUnitErrorModel(errors, _model, true);
    _notifier.junitEnded();
    _testInProgress = false;
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) finished", "", "").show();
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
   * @param className the name of the class for which we want to find the file
   * @return the file associated with the given class
   */
  public File getFileForClassName(String className) { return _model.getSourceFile(className + ".java"); }
  
  /** Returns the current classpath in use by the JUnit JVM, in the form of a path-separator delimited string. */
  public Iterable<File> getClassPath() {  return _jvm.getClassPath(); }
  
  /** Called when the JVM used for unit tests has registered. */
  public void junitJVMReady() {
   
    if (! _testInProgress) return; 
    JUnitError[] errors = new JUnitError[1];
    errors[0] = new JUnitError("Previous test suite was interrupted", true, "");
    _junitErrorModel = new JUnitErrorModel(errors, _model, true);
    _notifier.junitEnded();
    _testInProgress = false; 
  }
}
