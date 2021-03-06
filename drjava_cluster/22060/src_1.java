/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.io.*;

import java.rmi.*;


// NOTE: Do NOT import/use the config framework in this class!
//  (This class runs in a different JVM, and will not share the config object)


import edu.rice.cs.util.Log;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.*;

// For Windows focus fix
import javax.swing.JDialog;

import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.parser.*;

/** This is the main class for the interpreter JVM.  All public methods except those involving remote calls (callbacks) 
 *  synchronized (unless synchronization has no effect).  This class is loaded in the Interpreter JVM, not the Main JVM. 
 *  (Do not use DrJava's config framework here.)
 *  <p>
 *  Note that this class is specific to DynamicJava. It must be refactored to accommodate other interpreters.
 *  @version $Id$
 */
public class InterpreterJVM extends AbstractSlaveJVM implements InterpreterJVMRemoteI, JUnitModelCallback {

  /** Singleton instance of this class. */
  public static final InterpreterJVM ONLY = newInterpreterJVM(); 
  
  private static final Log _log = new Log("MasterSlave.txt", false);
  private static final boolean printMessages = false;
  
  /** String to append to error messages when no stack trace is available. */
  public static final String EMPTY_TRACE_TEXT = "";
    
  /** Metadata encapsulating the default interpreter. */
  private final InterpreterData _defaultInterpreter;
  
  /** Maps names to interpreters with metadata. */
  private final Hashtable<String,InterpreterData> _interpreters;
 
  /** The currently accumulated classpath for all Java interpreters.  List contains unqiue entries. */
  private final Set<File> _classPath;
  
  /** Responsible for running JUnit tests in this JVM. */
  private final JUnitTestManager _junitTestManager;
  
  /** manages the classpath for all of DrJava */
  private final ClassPathManager _classPathManager;
  
  /** Remote reference to the MainJVM class in DrJava's primary JVM.  Assigned ONLY once. */
  private volatile MainJVMRemoteI _mainJVM;

  /** The current interpreter. */
  private volatile InterpreterData _activeInterpreter;
  
//  /** Busy flag.  Used to prevent multiple interpretations from running simultaneously. */
//  private volatile boolean interpretationInProgress = false;
  
  /** Interactions processor, currently a pre-processor **/
  //  private InteractionsProcessorI _interactionsProcessor;
  
  /** Whether to display an error message if a reset fails. */
  private volatile boolean _messageOnResetFailure;
  
  /** Private constructor; use the singleton ONLY instance. */
  private InterpreterJVM() throws RemoteException {

    _classPath = new LinkedHashSet<File>();
    _classPathManager = new ClassPathManager();
    _defaultInterpreter = new InterpreterData(new DynamicJavaAdapter(_classPathManager));
    _interpreters = new Hashtable<String,InterpreterData>();
    _junitTestManager = new JUnitTestManager(this);
    _messageOnResetFailure = true;
    
    //    _interactionsProcessor = new InteractionsProcessor();
    
    _quitSlaveThreadName = "Reset Interactions Thread";
    _pollMasterThreadName = "Poll DrJava Thread";
    _activeInterpreter = _defaultInterpreter;
    
    try { _activeInterpreter.getInterpreter().interpret("0"); }
    catch (ExceptionReturnedException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  private static InterpreterJVM newInterpreterJVM() {
    try { return new InterpreterJVM(); }
    catch(Exception e) { throw new UnexpectedException(e); }
  }

  private static void _dialog(String s) {
    //javax.swing.JOptionPane.showMessageDialog(null, s);
    _log.log(s);
  }
  
  /** Actions to perform when this JVM is started (through its superclass, AbstractSlaveJVM).  Contract from superclass
   *  mandates that this code does not synchronized on this across a remote call.  This method has no synchronization
   *  because it can only be called once (part of the superclass contract) and _mainJVM is only assigned (once!) here. */
  protected void handleStart(MasterRemote mainJVM) {
    //_dialog("handleStart");
    _mainJVM = (MainJVMRemoteI) mainJVM;
    
    // redirect stdin
    System.setIn(new InputStreamRedirector() {
      protected String _getInput() {  // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try { 
          String s = _mainJVM.getConsoleInput();
//          System.err.println("InterpreterJVM.getConsoleInput() = '" + s + "'");
          return s;
        }
        catch(RemoteException re) {
          // blow up if no MainJVM found
          _log.log("System.in: " + re.toString());
          throw new IllegalStateException("Main JVM can't be reached for input.\n" + re);
        }
      }
    });
    
    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) { // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try {
          //_log.logTime("out.print: " + s);
          _mainJVM.systemOutPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log("System.out: " + re.toString());
        }
      }
    }));
    
    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) { // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try {
          //_log.logTime("err.print: " + s);
          _mainJVM.systemErrPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log("System.err: " + re.toString());
        }
      }
    }));
    
    /* On Windows, any frame or dialog opened from Interactions pane will appear *behind* DrJava's frame, unless a 
     * previous frame or dialog is shown here.  Not sure what the difference is, but this hack seems to work.  (I'd
     * be happy to find a better solution, though.)  Only necessary on Windows, since frames and dialogs on other 
     * platforms appear correctly in front of DrJava. */
    if (PlatformFactory.ONLY.isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.setVisible(true);
      d.setVisible(false);
    }
    //_dialog("interpreter JVM started");
  }

  /** Interprets the given string of source code in the active interpreter. The result is returned to MainJVM via 
   *  the interpretResult method.
   *  @param s Source code to interpret.
   */
  public void interpret(String s) { interpret(s, _activeInterpreter); }
  
  /** Interprets the given string of source code with the given interpreter. The result is returned to MainJVM via 
   *  the interpretResult method.
   *  @param s Source code to interpret.
   *  @param interpreterName Name of the interpreter to use
   *  @throws IllegalArgumentException if the named interpreter does not exist
   */
  public void interpret(String s, String interpreterName) { interpret(s, getInterpreter(interpreterName));  }
  
  /** Interprets the given string of source code with the given interpreter.  The result is returned to MainJVM via
   *  the interpretResult method.  Not synchronized on this!
   *  @param input Source code to interpret.
   *  @param interpreter The interpreter (plus metadata) to use
   */
  public void interpret(final String input, final InterpreterData interpreter) {
    _log.log(this + ".interpret(" + input + ") called");
    try {
      synchronized(interpreter) { 
        if (interpreter.inProgress()) {
            _mainJVM.interpretResult(new InterpreterBusy());
          return;
        }
//      interpretationInProgress = true; 
      interpreter.setInProgress(true);  // records that a given interpreter is in progress (used by debugger?)
      }
      // The following code is NOT synchronized on this. Mutual exclusion is guaranteed by preceding synchronized block.
//        Utilities.showDebug("InterpreterJVM.interpret(" + input + ", ...) called");
      Thread thread = new Thread("interpret thread: " + input) {
        public void run() {
          String s = input;
          try {  // Delimiting a catch for RemoteExceptions that might be thrown in catch clauses of enclosed try
            try {
              _log.log("Interpreter thread for " + input + " has started");
//              _dialog("to interp: " + s);
              
//            Utilities.showDebug("Preparing to invoke interpret method on " + s);
              Object result = interpreter.getInterpreter().interpret(s);
              String resultString = String.valueOf(result);
//            Utilities.showDebug("Result string is: " + resultString);
              
              if (result == Interpreter.NO_RESULT) {
                //return new VoidResult();
                //_dialog("void interp ret: " + resultString);
                _mainJVM.interpretResult(new VoidResult());
              }
              else {
                // we use String.valueOf because it deals with result = null!
                //_dialog("about to tell main result was " + resultString);
                //return new ValueResult(resultString);
                String style = InteractionsDocument.OBJECT_RETURN_STYLE;
                if (result instanceof String) {
                  style = InteractionsDocument.STRING_RETURN_STYLE;
                  //Single quotes have already been added to chars by now, so they are read as strings
                  String possibleChar = (String)result;
                  
                  if (possibleChar.startsWith("\'") && possibleChar.endsWith("\'") && possibleChar.length()==3)
                    style = InteractionsDocument.CHARACTER_RETURN_STYLE;                
                }
                if (result instanceof Number) style = InteractionsDocument.NUMBER_RETURN_STYLE;
                _mainJVM.interpretResult(new ValueResult(resultString, style));
              }
            }
            catch (ExceptionReturnedException e) {
              Throwable t = e.getContainedException();
//            Utilities.showStackTrace(t);
              _dialog("interp exception: " + t);
              // TODO: replace the following if ladder by dynamic dispatch.  Create a visitor for DynamicJava errors?
              if (t instanceof ParseException)
                _mainJVM.interpretResult(new SyntaxErrorResult((ParseException) t, input));
              else if (t instanceof TokenMgrError)
                _mainJVM.interpretResult(new SyntaxErrorResult((TokenMgrError) t, input));
              else if (t instanceof ParseError)
                _mainJVM.interpretResult(new SyntaxErrorResult((ParseError) t, input));
              else {
                //Other exceptions are non lexical/parse related exceptions. These include arithmetic exceptions, 
                //wrong version exceptions, etc.
                
                _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(), t.getMessage(),
                                                             InterpreterJVM.getStackTrace(t), null));
              }                                                                                                                                        
            }
            catch (Throwable t) {
              // A user's toString method might throw anything, so we need to be careful
              _dialog("irregular interp exception: " + t);
//            Utilities.showStackTrace(t);
              String shortMsg = null;
              if ((t instanceof ParseError) &&  ((ParseError) t).getParseException() != null) 
                shortMsg = ((ParseError) t).getMessage(); // in this case, getMessage is equivalent to getShortMessage
              _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(), t.getMessage(),
                                                           InterpreterJVM.getStackTrace(t), shortMsg));
            }
          }
          catch(RemoteException re) { /* MainJVM no longer accessible.  Cannot recover. */  
            _log.log("MainJVM.interpret threw " + re.toString());
          }
        }
      }; // end of Thread definition
      
      thread.setDaemon(true);
      thread.start();
    } // end of interpretation block including synchronized prelude 
    catch(RemoteException re) { /* MainJVM not accessible.  Cannot recover. */  
      _log.log("MainJVM.interpret threw" + re.toString());
    }
    finally { // fields are volatile so no synchronization is necessary
//      interpretationInProgress = false;
      interpreter.setInProgress(false); 
    }
  }
        
  private static String _processReturnValue(Object o) {
    if (o instanceof String) return "\"" + o + "\"";
    if (o instanceof Character) return "'" + o + "'";
    return o.toString();
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   *  @return null if the variable is not defined, "null" if the value is null, or else its string representation
   */
  public synchronized String getVariableToString(String var) throws RemoteException {
    // Add to the default interpreter, if it is a JavaInterpreter
    Interpreter i = _activeInterpreter.getInterpreter();
    if (i instanceof JavaInterpreter) {
      try {
        Object value = ((JavaInterpreter)i).getVariable(var);
        if (value == null)  return "null";
        if (value instanceof koala.dynamicjava.interpreter.UninitializedObject) return null;
        return _processReturnValue(value);
      }
      catch (IllegalStateException e) { return null; }  // variable was not defined
    }
    return null;
  }
  
  /** Gets the class name of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public synchronized String getVariableClassName(String var) throws RemoteException {
    // Add to the default interpreter, if it is a JavaInterpreter
    Interpreter i = _activeInterpreter.getInterpreter();
    if (i instanceof JavaInterpreter) {
      try {
        Class c = ((JavaInterpreter)i).getVariableClass(var);
        if (c == null) return "null";
        else return c.getName();
      }
      catch (IllegalStateException e) {
        // variable was not defined
        return null;
      }
    }
    else return null;
  }
  
  /** Adds a named DynamicJavaAdapter to list of interpreters. Presets it to contain the current accumulated classpath.
   *  @param name the unique name for the interpreter
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addJavaInterpreter(String name) {
    JavaInterpreter interpreter = new DynamicJavaAdapter(_classPathManager);
    // Add each entry on the accumulated classpath
    _updateInterpreterClassPath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addDebugInterpreter(String name, String className) {
    JavaDebugInterpreter interpreter = new JavaDebugInterpreter(name, className);
    interpreter.setPrivateAccessible(true);
    // Add each entry on the accumulated classpath
    _updateInterpreterClassPath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /** Adds a named interpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param interpreter the interpreter to add
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addInterpreter(String name, Interpreter interpreter) {
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    _interpreters.put(name, new InterpreterData(interpreter));
  }
  
  /** Removes the interpreter with the given name, if it exists.  Unsynchronized because _interpreters is immutable
   *  and its methods are thread-safe.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) { _interpreters.remove(name); }
  
  /** Returns the interpreter (with metadata) with the given name
   *  @param name the unique name of the desired interpreter
   *  @throws IllegalArgumentException if no such named interpreter exists
   */
  InterpreterData getInterpreter(String name) {
    InterpreterData interpreter = _interpreters.get(name);
    if (interpreter != null) return interpreter;
    else throw new IllegalArgumentException("Interpreter '" + name + "' does not exist.");
  }
  
  /** Returns the Java interpreter with the given name
   *  @param name the unique name of the desired interpreter
   *  @throws IllegalArgumentException if no such named interpreter exists, or if the named interpreter is not a Java
   *          interpreter
   */
  public synchronized JavaInterpreter getJavaInterpreter(String name) {
    if (printMessages) System.out.println("Getting interpreter data");
    InterpreterData interpreterData = getInterpreter(name);
    if (printMessages) System.out.println("Getting interpreter instance");
    Interpreter interpreter = interpreterData.getInterpreter();
    if (printMessages) System.out.println("returning");
    
    if (interpreter instanceof JavaInterpreter) return (JavaInterpreter) interpreter;
    else {
      throw new IllegalArgumentException("Interpreter '" + name + "' is not a JavaInterpreter.");
    }
  }
  
  
  /** Sets the current interpreter to be the one specified by the given name
   *  @param name the unique name of the interpreter to set active
   *  @return Whether the new interpreter is currently in progress with an interaction
   */
  public synchronized boolean setActiveInterpreter(String name) {
    _activeInterpreter = getInterpreter(name);
    return _activeInterpreter.inProgress();
  }
  
  /** Sets the default interpreter to be active.
   *  @return Whether the new interpreter is currently in progress with an interaction
   */
  public synchronized boolean setToDefaultInterpreter() {
    _activeInterpreter = _defaultInterpreter;
    return _activeInterpreter.inProgress();
  }
  
  /** Gets the hashtable containing the named interpreters.  Package private for testing purposes.
   *  @return said hashtable
   */
  Hashtable<String,InterpreterData> getInterpreters() { return _interpreters; }
  
  /** Returns the current active interpreter.  Package private; for tests only. */
  Interpreter getActiveInterpreter() { return _activeInterpreter.getInterpreter(); }
  
  /** Gets the stack trace from the given exception, stripping off the bottom parts of the trace that are internal 
   *  to the interpreter.  This would be much easier to do in JDK 1.4, since you can get the stack trace frames 
   *  directly, instead of having to parse this!  TODO: revise this code to use the JDK 1.4+ API.
   */
  public static String getStackTrace(Throwable t) {
    //_dialog("before creating reader");
    BufferedReader reader = new BufferedReader(new StringReader(StringOps.getStackTrace(t)));
    
    //_dialog("after creating reader");
    LinkedList<String> traceItems = new LinkedList<String>();
    try {
      // we will generate list of trace items
      // skip the first one since it's just the message
      //_dialog("before first readLine");
      reader.readLine();
      //_dialog("after first readLine");
      
      String s;
      while ((s = reader.readLine()) != null) {
        //_dialog("read: " + s);
        traceItems.add(s);
      }
    }
    catch (IOException ioe) {
      return "Unable to get stack trace";
    }
    
    // OK, now we crop off everything after the first "koala.dynamicjava." or "edu.rice.cs.drjava.", if there is one.
    
    //  First, find the index of an occurrence.
    int index = -1;
    for (int i = 0; i < traceItems.size(); i++) {
      String item = traceItems.get(i);
      item = item.trim();
      if (item.startsWith("at edu.rice.cs.drjava.") || item.startsWith("at koala.dynamicjava.")) {
        index = i;
        break;
      }
    }
    
    // Now crop off the rest
    if (index > -1) {
      while (traceItems.size() > index) traceItems.removeLast();
    }
    
    // Last check: See if there are no items left. If there are none, put one in to say it happened at top-level.
    if (traceItems.isEmpty()) traceItems.add(EMPTY_TRACE_TEXT);
    
    // OK, now rebuild string
    final StringBuilder buf = new StringBuilder();
    final ListIterator itor = traceItems.listIterator();
    final String newLine = StringOps.EOL;  // intended for output to system? (as opposed to Swing text)
    boolean first = true;
    while (itor.hasNext()) {
      if (first) first = false; else buf.append(newLine);

      buf.append("  " + ((String) itor.next()).trim());
    }
    
    return buf.toString();
  }
  
  // ---------- Java-specific methods ----------
  
  /** Sets the package scope for the current active interpreter, if it is a JavaInterpreter. */
  public void setPackageScope(String s) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPackageScope(s);
    }
  }
  
  /** @param show Whether to show a message if a reset operation fails. */
  public void setShowMessageOnResetFailure(boolean show) { _messageOnResetFailure = show; }
  
  /** This method is called if the interpreterJVM cannot be exited (likely because of a modified security manager. */
  protected void quitFailed(Throwable th) {  // NOT synchronized
    if (_messageOnResetFailure) {
      String msg = "The interactions pane could not be reset:\n" + th;
      javax.swing.JOptionPane.showMessageDialog(null, msg);
    }
    
    try { _mainJVM.quitFailed(th); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("quitFailed: " + re.toString());
    }
  }
  
  /** Sets the interpreter to allow access to private members. */
  public synchronized void setPrivateAccessible(boolean allow) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPrivateAccessible(allow);
    }
  } 
  
  // ---------- JUnit methods ----------
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases classes (by 
   *  loading them).  Unsynchronized because it contains a remote call and does not involve mutable local state.
   *  @param classNames the class names to run in a test
   *  @param files the associated file
   *  @return the class names that are actually test cases
   */
  public List<String> findTestClasses(List<String> classNames, List<File> files) throws RemoteException {
    return _junitTestManager.findTestClasses(classNames, files);
  }
  
  /** Runs JUnit test suite already cached in the Interpreter JVM.  Unsynchronized because it contains a remote call
   *  and does not involve mutable local state.
   *  @return false if no test suite is cached; true otherwise
   */
  public boolean runTestSuite() throws RemoteException {
    return _junitTestManager.runTestSuite();
  }
  
  /** Notifies Main JVM that JUnit has been invoked on a non TestCase class.  Unsynchronized because it contains a 
   *  remote call and does not involve mutable local state.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    try { _mainJVM.nonTestCase(isTestAll); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("nonTestCase: " + re.toString());
    }
  }
  
  /** Notifies the main JVM that JUnitTestManager has encountered an illegal class file.  Unsynchronized because it 
   *  contains a remote call and does not involve mutable local state.
   *  @param e the ClassFileError object describing the error on loading the file
   */
  public void classFileError(ClassFileError e) {
    try { _mainJVM.classFileError(e); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("classFileError: " + re.toString());
    }
  }
  
  /** Notifies that a suite of tests has started running.  Unsynchronized because it contains a remote call and does
   *  not involve mutable local state.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) {
    try { _mainJVM.testSuiteStarted(numTests); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testSuiteStarted: " + re.toString());
    }
  }
  
  /** Notifies that a particular test has started.  Unsynchronized because it contains a remote call and does not
   *  involve mutable local state.
   *  @param testName The name of the test being started.
   */
  public void testStarted(String testName) {
    try { _mainJVM.testStarted(testName); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testStarted" + re.toString());
    }
  }
  
  /** Notifies that a particular test has ended.  Unsynchronized because it contains a remote call.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try { _mainJVM.testEnded(testName, wasSuccessful, causedError); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testEnded: " + re.toString());
    }
  }
  
  /** Notifies that a full suite of tests has finished running.  Unsynchronized because it contains a remote call
   *  and does not involve mutable local state.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    try { _mainJVM.testSuiteEnded(errors); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testSuiteFinished: " + re.toString());
    }
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.  Unsynchronized because it 
   *  contains a remote call and does not involve mutable local state.
   *  @param className the name of the class for which we want to find the file
   *  @return the file associated with the given class
   */
  public File getFileForClassName(String className) {
    try { return _mainJVM.getFileForClassName(className); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("getFileForClassName: " + re.toString());
      return null;
    }
  }
  
  public void junitJVMReady() { }
  
  //////////////////////////////////////////////////////////////
  // ALL functions regarding classpath
  //////////////////////////////////////////////////////////////
  
  /** Adds a classpath to the given interpreter.  assumes that lock on this is held.
   *  @param interpreter the interpreter
   */
  protected /* synchronized */ void _updateInterpreterClassPath(JavaInterpreter interpreter) {
    
    for (File f : _classPathManager.getProjectCP())
      interpreter.addProjectClassPath(f);
    
    for (File f : _classPathManager.getBuildDirectoryCP())
      interpreter.addBuildDirectoryClassPath(f);
    
    for (File f : _classPathManager.getProjectFilesCP())
      interpreter.addProjectFilesClassPath(f);
    
    for (File f : _classPathManager.getExternalFilesCP())
      interpreter.addExternalFilesClassPath(f);
    
    for (File f : _classPathManager.getExtraCP())
      interpreter.addExtraClassPath(f);
  }
  
  /** Adds the given path to the classpath shared by ALL Java interpreters.  Only unique paths are added.
   *  @param f  Entry to add to the accumulated classpath
   */
  public synchronized void addExtraClassPath(File f) {
    if (_classPath.contains(f)) return;    // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addExtraClassPath(f);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addExtraClassPath(f);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(f);
  }
 
  /** Adds the given file to the classpath shared by ALL Java interpreters.  Only unique paths are added.
   *  @param f  Entry to add to the accumulated classpath
   */
  public synchronized void addProjectClassPath(File f) {
    if (_classPath.contains(f)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addProjectClassPath(f);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addProjectClassPath(f);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(f);
  }
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   *  @param f  Entry to add to the accumulated classpath
   */
  public synchronized void addBuildDirectoryClassPath(File f) {
    if (_classPath.contains(f)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addBuildDirectoryClassPath(f);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addBuildDirectoryClassPath(f);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(f);
  }
  
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   *  @param f  Entry to add to the accumulated classpath
   */
  public synchronized void addProjectFilesClassPath(File f) {
    if (_classPath.contains(f)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addProjectFilesClassPath(f);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addProjectFilesClassPath(f);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(f);
  }
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   * @param f  Entry to add to the accumulated classpath
   */
  public synchronized void addExternalFilesClassPath(File f) {
    if (_classPath.contains(f)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addExternalFilesClassPath(f);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addExternalFilesClassPath(f);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(f);
  }
  
  public synchronized Iterable<File> getClassPath() {
    Iterable<File> result = IterUtil.empty();
    result = IterUtil.compose(result, _classPathManager.getProjectCP());
    result = IterUtil.compose(result, _classPathManager.getBuildDirectoryCP());
    result = IterUtil.compose(result, _classPathManager.getProjectFilesCP());
    result = IterUtil.compose(result, _classPathManager.getExternalFilesCP());
    result = IterUtil.compose(result, _classPathManager.getExtraCP());
    return result;
  }
  
  public List<File> getAugmentedClassPath() { return IterUtil.asList(getClassPath()); }
  
}


/** Bookkeeping class to maintain information about each interpreter, such as whether it is currently in progress. */
class InterpreterData {
  protected final Interpreter _interpreter;
  protected volatile boolean _inProgress;
  
  InterpreterData(Interpreter interpreter) {
    _interpreter = interpreter;
    _inProgress = false;
  }
  
  // The following methods do not need to be synchronized because they access or set volatile fields.
  
  /** Gets the interpreter. */
  public Interpreter getInterpreter() { return _interpreter; }
  
  /** Returns whether this interpreter is currently in progress with an interaction. */
  public boolean inProgress() { return _inProgress; }
  
  /** Sets whether this interpreter is currently in progress. */
  public void setInProgress(boolean inProgress) { _inProgress = inProgress; }
}
