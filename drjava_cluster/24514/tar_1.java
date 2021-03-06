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

package edu.rice.cs.drjava.model;


import java.awt.EventQueue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.text.BadLocationException;
import javax.swing.SwingUtilities;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.FileOps;

import edu.rice.cs.util.NullFile;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.newjvm.AbstractMasterJVM;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.FileOption;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.debug.DebugListener;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.DebugThreadData;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.javadoc.NoJavadocAvailable;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.DummyInteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.DefaultCompilerModel;
import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.model.junit.DefaultJUnitModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.ui.MainFrame;

import java.io.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Handles the bulk of DrJava's program logic. The UI components interface with the GlobalModel through its public
  * methods, and the GlobalModel responds via the GlobalModelListener interface. This removes the dependency on the 
  * UI for the logical flow of the program's features.  With the current implementation, we can finally test the compile
  * functionality of DrJava, along with many other things. <p>
  * @version $Id$
  */
public class DefaultGlobalModel extends AbstractGlobalModel {
  
  /* FIELDS */
  
  /* static Log _log inherited from AbstractGlobalModel */
  
  /* Interpreter fields */
  
  /** The document used in the Interactions model. */
  protected final InteractionsDJDocument _interactionsDocument;
  
  /** RMI interface to the Interactions JVM. */
  final MainJVM _jvm; 
  
  /** Interface between the InteractionsDocument and the JavaInterpreter, which runs in a separate JVM. */
  protected final DefaultInteractionsModel _interactionsModel;
  
  /** Core listener attached to interactions model */
  protected InteractionsListener _interactionsListener = new InteractionsListener() {
    public void interactionStarted() { }
    
    public void interactionEnded() { }
    
    public void interactionErrorOccurred(int offset, int length) { }
    
    public void interpreterResetting() { }
    
    public void interpreterReady(File wd) {
      File buildDir = _state.getBuildDirectory();
      if (buildDir != null) {
        //        System.out.println("adding for reset: " + _state.getBuildDirectory().getAbsolutePath());
        _jvm.addBuildDirectoryClassPath(IOUtil.attemptAbsoluteFile(buildDir));
      }
    }
    
    public void interpreterResetFailed(Throwable t) { }
    
    public void interpreterExited(int status) { }
    
    public void interpreterChanged(boolean inProgress) { }
    
    public void interactionIncomplete() { }
    
    public void slaveJVMUsed() { }
  };
  
  private CompilerListener _clearInteractionsListener =
    new CompilerListener() {
    public void compileStarted() { }
    
    public void compileEnded(File workDir, List<? extends File> excludedFiles) {
      // Only clear interactions if there were no errors and unit testing is not in progress
      if ( ((_compilerModel.getNumErrors() == 0) || (_compilerModel.getCompilerErrorModel().hasOnlyWarnings()))
            && ! _junitModel.isTestInProgress() && _resetAfterCompile) {
        resetInteractions(workDir);  // use same working directory as current interpreter
      }
    }
    public void saveBeforeCompile() { }
    public void saveUntitled() { }
    public void activeCompilerChanged() { }
  };
  
  // ---- Compiler Fields ----
  
  /** CompilerModel manages all compiler functionality. */
  private final CompilerModel _compilerModel;
  
  /** Whether or not to reset the interactions JVM after compiling.  Should only be false in test cases. */
  private volatile boolean _resetAfterCompile = true;
  
  /** Number of errors in last compilation.  compilerModel._numErrors is trashed when the compile model is reset. */
  private volatile int _numCompErrors = 0;
  
  /* JUnit Fields */
  
  /** JUnitModel manages all JUnit functionality. */
  private final DefaultJUnitModel _junitModel;
  
  /* Javadoc Fields */
  
  /** Manages all Javadoc functionality. */
  protected volatile JavadocModel _javadocModel;
  
  /* Debugger Fields */
  
  /** Interface to the integrated debugger.  If unavailable, set NoDebuggerAvailable.ONLY. */
  private volatile Debugger _debugger;
  
  /* CONSTRUCTORS */
  
  /** Constructs a new GlobalModel. Creates a new MainJVM and starts its Interpreter JVM. */
  public DefaultGlobalModel() {
    Iterable<? extends JDKToolsLibrary> tools = findLibraries();
    List<CompilerInterface> compilers = new LinkedList<CompilerInterface>();
    _debugger = null;
    _javadocModel = null;
    for (JDKToolsLibrary t : tools) {
      if (t.compiler().isAvailable()) { compilers.add(t.compiler()); }
      if (_debugger == null && t.debugger().isAvailable()) { _debugger = t.debugger(); }
      if (_javadocModel == null && t.javadoc().isAvailable()) { _javadocModel = t.javadoc(); }
    }
    if (_debugger == null) { _debugger = NoDebuggerAvailable.ONLY; }
    if (_javadocModel == null) { _javadocModel = new NoJavadocAvailable(this); }
    
    File workDir = Utilities.TEST_MODE ? new File(System.getProperty("user.home")) : getWorkingDirectory();
    try { _jvm = new MainJVM(workDir); }
    catch(java.rmi.server.ExportException e) {
      if (e.getMessage().equals("Listen failed on port: 0")) throw new RuntimeException(e);
      else throw new UnexpectedException(e);
    }
    catch(RemoteException e) { throw new UnexpectedException(e); }
//    AbstractMasterJVM._log.log(this + " has created a new MainJVM");
    _compilerModel = new DefaultCompilerModel(this, compilers);
    _junitModel = new DefaultJUnitModel(_jvm, _compilerModel, this);
    _interactionsDocument = new InteractionsDJDocument();
    
    _interactionsModel = new DefaultInteractionsModel(this, _jvm, _interactionsDocument, workDir);
    _interactionsModel.addListener(_interactionsListener);
    _jvm.setInteractionsModel(_interactionsModel);
    _jvm.setJUnitModel(_junitModel);
    
    StringBuilder sb = new StringBuilder();
    if ((!("".equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX)))) &&
        (!(OptionConstants.heapSizeChoices.get(0).equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX))))) {
      sb.append("-Xmx");
      sb.append(DrJava.getConfig().getSetting(SLAVE_JVM_XMX));
      sb.append("M ");
    }
    sb.append(DrJava.getConfig().getSetting(SLAVE_JVM_ARGS));
    _jvm.setOptionArgs(sb.toString());
    
    OptionListener<String> updateListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oe) {
        StringBuilder sb = new StringBuilder();
        if ((!("".equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX)))) &&
            (!(OptionConstants.heapSizeChoices.get(0).equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX))))) { 
          sb.append("-Xmx");
          sb.append(DrJava.getConfig().getSetting(SLAVE_JVM_XMX));
          sb.append("M ");
        }
        sb.append(DrJava.getConfig().getSetting(SLAVE_JVM_ARGS));
        _jvm.setOptionArgs(sb.toString());
      }
    };
    DrJava.getConfig().addOptionListener(SLAVE_JVM_ARGS, updateListener); 
    DrJava.getConfig().addOptionListener(SLAVE_JVM_XMX, updateListener); 
    
    _setupDebugger();
    
    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _javadocModel.addListener(_notifier);
    
    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);
    
    // Note: starting the JVM in another thread does not appear to improve performance
//    AbstractMasterJVM._log.log("Starting the interpreter in " + this);
    _jvm.startInterpreterJVM();
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.    
//    _parsingControl = new DefaultLightWeightParsingControl(this);
  }
  
  
  private Iterable<JDKToolsLibrary> findLibraries() {
    // Order to return: config setting, runtime (if different version), from search (if different versions)
    
    // We could give priority to libraries that have both available compilers and debuggers, but since this will 
    // almost always be true, it seems like more trouble than it is worth
    
    // map is sorted by version, lowest-to-highest
    Map<JavaVersion, JDKToolsLibrary> results = new TreeMap<JavaVersion, JDKToolsLibrary>();
    
    File configTools = DrJava.getConfig().getSetting(JAVAC_LOCATION);
    if (configTools != FileOps.NULL_FILE) {
      JDKToolsLibrary fromConfig = JarJDKToolsLibrary.makeFromFile(configTools, this);
      if (fromConfig.isValid()) { results.put(fromConfig.version().majorVersion(), fromConfig); }
    }
    
    JDKToolsLibrary fromRuntime = JDKToolsLibrary.makeFromRuntime(this);
    JavaVersion runtimeVersion = fromRuntime.version().majorVersion();
    if (fromRuntime.isValid() && !results.containsKey(runtimeVersion)) { results.put(runtimeVersion, fromRuntime); }
    
    Iterable<JarJDKToolsLibrary> fromSearch = JarJDKToolsLibrary.search(this);
    for (JDKToolsLibrary t : fromSearch) {
      JavaVersion tVersion = t.version().majorVersion();
      // guaranteed to be valid
      if (!results.containsKey(tVersion)) { results.put(tVersion, t); }
    }
    
    return IterUtil.reverse(results.values());
  }
  
  
//  public void compileAll() throws IOException{ 
////    ScrollableDialog sd = new ScrollableDialog(null, "DefaultGlobalModel.compileAll() called", "", "");
////    sd.show();
//    _state.compileAll(); 
//  }
  
  
//  public void junitAll() { _state.junitAll(); }
  
  /** Sets the build directory for a project. */
  public void setBuildDirectory(File f) {
    _state.setBuildDirectory(f);
    if (f != FileOps.NULL_FILE) {
      //      System.out.println("adding: " + f.getAbsolutePath());
      _jvm.addBuildDirectoryClassPath(IOUtil.attemptAbsoluteFile(f));
    }
    
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
    setClassPathChanged(true);
  }
  
  // ----- METHODS -----
  
  /** @return the interactions model. */
  public DefaultInteractionsModel getInteractionsModel() { return _interactionsModel; }
  
  /** @return InteractionsDJDocument in use by the InteractionsDocument. */
  public InteractionsDJDocument getSwingInteractionsDocument() { return _interactionsDocument; }
  
  public InteractionsDocument getInteractionsDocument() { return _interactionsModel.getDocument(); }
  
  /** Gets the CompilerModel, which provides all methods relating to compilers. */
  public CompilerModel getCompilerModel() { return _compilerModel; }
  
  /** Gets the JUnitModel, which provides all methods relating to JUnit testing. */
  public JUnitModel getJUnitModel() { return _junitModel; }
  
  /** Gets the JavadocModel, which provides all methods relating to Javadoc. */
  public JavadocModel getJavadocModel() { return _javadocModel; }
  
  public int getNumCompErrors() { return _numCompErrors; }
  public void setNumCompErrors(int num) { _numCompErrors = num; }
  
  /** Prepares this model to be thrown away.  Never called in practice outside of quit(), except in tests. */
  public void dispose() {
    // Kill the interpreter
    _jvm.killInterpreter(null);
    // Commented out because it invokes UnicastRemoteObject.unexport
//    try { _jvm.dispose(); }
//    catch(RemoteException e) { /* ignore */ }
    _notifier.removeAllListeners();  // removes the global model listeners!
  }
  
  /** Disposes of external resources. Kills the slave JVM. */
  public void disposeExternalResources() {
    // Kill the interpreter
    _jvm.killInterpreter(null);
  }
  
  public void resetInteractions(File wd) { resetInteractions(wd, false); }
  
  /** Clears and resets the slave JVM with working directory wd. Also clears the console if the option is 
    * indicated (on by default).  The reset operation is suppressed if the existing slave JVM has not been
    * used, {@code wd} matches its working directory, and forceReset is false.  {@code wd} may be {@code null}
    * if a valid directory cannot be determined.  In that case, the former working directory is used.
    */
  public void resetInteractions(File wd, boolean forceReset) {
    assert _interactionsModel._pane != null;
    
    debug.logStart();
    File workDir = _interactionsModel.getWorkingDirectory();
    if (wd == null) { wd = workDir; }
    
    if (! forceReset && ! _jvm.slaveJVMUsed() && ! isClassPathChanged() && wd.equals(workDir)) {
      debug.log();
      // Eliminate resetting interpreter (slaveJVM) since it has already been reset appropriately.
      _interactionsModel._notifyInterpreterReady(wd);
      debug.logEnd();
      return; 
    }
    // update the setting
    debug.log();
    DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, wd);
    _interactionsModel.resetInterpreter(wd);
    debug.logEnd();
  }
  
  /** Interprets the current given text at the prompt in the interactions pane. */
  public void interpretCurrentInteraction() { _interactionsModel.interpretCurrentInteraction(); }
  
  /** Interprets file selected in the FileOpenSelector. Assumes strings have no trailing whitespace. Interpretation is
    * aborted after the first error.
    */
  public void loadHistory(FileOpenSelector selector) throws IOException { _interactionsModel.loadHistory(selector); }
  
  /** Loads the history/histories from the given selector. */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    return _interactionsModel.loadHistoryAsScript(selector);
  }
  
  /** Clears the interactions history */
  public void clearHistory() { _interactionsModel.getDocument().clearHistory(); }
  
  /** Saves the unedited version of the current history to a file
    * @param selector File to save to
    */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }
  
  /** Saves the edited version of the current history to a file
    * @param selector File to save to
    * @param editedVersion Edited verison of the history which will be saved to file instead of the lines saved in 
    *        the history. The saved file will still include any tags needed to recognize it as a history file.
    */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector, editedVersion);
  }
  
  /** Returns the entire history as a String with semicolons as needed. */
  public String getHistoryAsStringWithSemicolons() {
    return _interactionsModel.getDocument().getHistoryAsStringWithSemicolons();
  }
  
  /** Returns the entire history as a String. */
  public String getHistoryAsString() {
    return _interactionsModel.getDocument().getHistoryAsString();
  }
  
  /** Called when the debugger wants to print a message.  Inserts a newline. */
  public void printDebugMessage(String s) {
    _interactionsModel.getDocument().
      insertBeforeLastPrompt(s + "\n", InteractionsDocument.DEBUGGER_STYLE);
  }
  
  /** Blocks until the interpreter has registered. */
  public void waitForInterpreter() { _jvm.ensureInterpreterConnected(); }
  
  
  /** Returns the current classpath in use by the Interpreter JVM. */
  public Iterable<File> getInteractionsClassPath() { return _jvm.getClassPath(); }
  
  /** Sets whether or not the Interactions JVM will be reset after a compilation succeeds.  This should ONLY be used 
    * in tests!  This method is not supported by AbstractGlobalModel.
    * @param shouldReset Whether to reset after compiling
    */
  void setResetAfterCompile(boolean shouldReset) { _resetAfterCompile = shouldReset; }
  
  /** Gets the Debugger used by DrJava. */
  public Debugger getDebugger() { return _debugger; }
  
  /** Returns an available port number to use for debugging the interactions JVM.
    * @throws IOException if unable to get a valid port number.
    */
  public int getDebugPort() throws IOException { return _interactionsModel.getDebugPort(); }
  
  // ---------- ConcreteOpenDefDoc inner class ----------
  
  /** Inner class to handle operations on each of the open DefinitionsDocuments by the GlobalModel. <br><br>
    * This was at one time called the <code>DefinitionsDocumentHandler</code>
    * but was renamed (2004-Jun-8) to be more descriptive/intuitive.
    */
  class ConcreteOpenDefDoc extends AbstractGlobalModel.ConcreteOpenDefDoc {
    /** Standard constructor for a document read from a file.  Initializes this ODD's DD.
      * @param f file describing DefinitionsDocument to manage
      */
    ConcreteOpenDefDoc(File f) { super(f); }
    
    /* Standard constructor for a new document (no associated file) */
    ConcreteOpenDefDoc(NullFile f) { super(f); }
    
    /** Starting compiling this document.  Used only for unit testing */
    public void startCompile() throws IOException { _compilerModel.compile(ConcreteOpenDefDoc.this); }
    
    private volatile InteractionsListener _runMain;
    
    /** Runs the main method in this document in the interactions pane after resetting interactions with the source
      * root for this document as the working directory.  Warns the use if the class files for the doucment are not 
      * up to date.  Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    public void runMain() throws ClassNameNotFoundException, IOException {
      assert EventQueue.isDispatchThread();
      
      // Get the class name for this document, the first top level class in the document.
      final String className = getDocument().getQualifiedClassName();
      final InteractionsDocument iDoc = _interactionsModel.getDocument();
      if (! checkIfClassFileInSync()) {
        iDoc.insertBeforeLastPrompt(DOCUMENT_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
        return;
      }
      
      final boolean wasDebuggerEnabled = getDebugger().isReady();
      
      _runMain = new DummyInteractionsListener() {
        private boolean alreadyRun = false;
        public void interpreterReady(File wd) {
          // prevent listener from running twice
          if (alreadyRun) return; else alreadyRun = true;
          // Restart debugger if it was previously enabled and is now off
          if (wasDebuggerEnabled && (! getDebugger().isReady())) {
            try { getDebugger().startUp(); } catch(DebugException de) { /* ignore, continue without debugger */ }
          }
          
          // Load the proper text into the interactions document
          iDoc.clearCurrentInput();
          iDoc.append("java " + className, null);
          
          // Finally, execute the new interaction and record that event
          new Thread("Running main method") {
            public void run() { _interactionsModel.interpretCurrentInteraction(); }
            
          }.start();
          _notifier.runStarted(ConcreteOpenDefDoc.this);
          
          // This used to be called using invokeLater, so that the listener would be removed
          // after the read lock of the notifier had been released, but that was not always
          // safe; the removal could still happen before the read lock was released
          // Now removeListener has been rewritten and can be called even when the lock is
          // held. In that case, the removal will be done as soon as possible.
          _interactionsModel.removeListener(_runMain);
        }
      };
      
      _interactionsModel.addListener(_runMain);
      
      File workDir;
      if (isProjectActive()) workDir = getWorkingDirectory(); // use working directory for project
      else {
        // use source root of current document
        try { workDir = getSourceRoot(); }
        catch (InvalidPackageException e) { workDir = FileOps.NULL_FILE; }
      }
      // Reset interactions to the soure root for this document; class will be executed when new interpreter is ready
      resetInteractions(workDir);  
    }
    
    /** Runs JUnit on the current document.  Requires that all source documents are compiled before proceeding. */
    public void startJUnit() throws ClassNotFoundException, IOException { _junitModel.junit(this); }
    
    /** Generates Javadoc for this document, saving the output to a temporary directory.  The location is provided to 
      * the javadocEnded event on the given listener.
      * java@param saver FileSaveSelector for saving the file if it needs to be saved
      */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _javadocModel.javadocDocument(this, saver);
    }
    
    /** Called to indicate the document is being closed, so to remove all related state from the debug manager. */
    public void removeFromDebugger() { getBreakpointManager().removeRegions(this); }
  } /* End of ConcreteOpenDefDoc */
  
  /** Creates a ConcreteOpenDefDoc for a new DefinitionsDocument.
    * @return OpenDefinitionsDocument object for a new document
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(NullFile f) { return new ConcreteOpenDefDoc(f); }
  
  /** Creates a ConcreteOpenDefDoc for a given file f
    * @return OpenDefinitionsDocument object for f
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(File f) throws IOException { 
    if (! f.exists()) throw new FileNotFoundException("file " + f + " cannot be found");
    return new ConcreteOpenDefDoc(f); 
  }
  
  /** Adds the source root for doc to the interactions classpath; this function is a helper to _openFiles.
    * @param doc the document to add to the classpath
    */
  protected void addDocToClassPath(OpenDefinitionsDocument doc) {
    try {
      File sourceRoot = doc.getSourceRoot();
      if (doc.isAuxiliaryFile()) { _interactionsModel.addProjectFilesClassPath(sourceRoot); }
      else { _interactionsModel.addExternalFilesClassPath(sourceRoot); }
      setClassPathChanged(true);
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
  }
  
  private void _setupDebugger() {
    _jvm.setDebugModel(_debugger.callback());
    
    // add listener to set the project file to "changed" when a breakpoint or watch is added, removed, or changed
    getBreakpointManager().addListener(new RegionManagerListener<Breakpoint>() {
      public void regionAdded(final Breakpoint bp) { setProjectChanged(true); }
      public void regionChanged(final Breakpoint bp) { setProjectChanged(true); }
      public void regionRemoved(final Breakpoint bp) { 
        try {
          getDebugger().removeBreakpoint(bp);
        } catch(DebugException de) {
          /* just ignore it */
          // TODO: should try to pop up dialog to give the user the option of restarting the debugger (mgricken)
//          int result = JOptionPane.showConfirmDialog(null, "Could not remove breakpoint.", "Restart debugger?", JOptionPane.YES_NO_OPTION);
//          if (result==JOptionPane.YES_OPTION) {
//            getDebugger().shutdown();
//            getDebugger().startUp();
//          }
        }
        setProjectChanged(true);
      }
    });
    getBookmarkManager().addListener(new RegionManagerListener<OrderedDocumentRegion>() {
      public void regionAdded(OrderedDocumentRegion r) { setProjectChanged(true); }
      public void regionChanged(OrderedDocumentRegion r) { setProjectChanged(true); }
      public void regionRemoved(OrderedDocumentRegion r) { setProjectChanged(true); }
    });
    
    _debugger.addListener(new DebugListener() {
      public void watchSet(final DebugWatchData w) { setProjectChanged(true); }
      public void watchRemoved(final DebugWatchData w) { setProjectChanged(true); }    
      
      public void regionAdded(final Breakpoint bp) { }
      public void regionChanged(final Breakpoint bp) { }
      public void regionRemoved(final Breakpoint bp) { }
      public void debuggerStarted() { }
      public void debuggerShutdown() { }
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) { }
      public void breakpointReached(final Breakpoint bp) { }
      public void stepRequested() { }
      public void currThreadSuspended() { }
      public void currThreadResumed() { }
      public void threadStarted() { }
      public void currThreadDied() { }
      public void nonCurrThreadDied() {  }
      public void currThreadSet(DebugThreadData thread) { }
    });
  }
  
  /** Get the class path to be used in all class-related operations.
    * TODO: Insure that this is used wherever appropriate.
    */
  public Iterable<File> getClassPath() {
    Iterable<File> result = IterUtil.empty();
    
    if (isProjectActive()) {
      File buildDir = getBuildDirectory();
      if (buildDir != null) { result = IterUtil.compose(result, buildDir); }
      
      /* We prefer to assume the project root is the project's source root, rather than
       * checking *every* file in the project for its source root.  This is a bit problematic,
       * because "Compile Project" won't care if the user has multiple source roots (or even just a
       * single "src" subdirectory), and the user in this situation (assuming the build dir is 
       * null) wouldn't notice a problem until trying to access the compiled classes in the 
       * Interactions.
       */
      File projRoot = getProjectRoot();
      if (projRoot != null) { result = IterUtil.compose(result, projRoot); }
      
      Iterable<File> projectExtras = getExtraClassPath();
      if (projectExtras != null) { result = IterUtil.compose(result, projectExtras); }
    }
    else { result = IterUtil.compose(result, getSourceRootSet()); }
    
    Vector<File> globalExtras = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if (globalExtras != null) { result = IterUtil.compose(result, globalExtras); }
    
    /* We must add JUnit to the class path.  We do so by including the current JVM's class path.
     * This is not ideal, because all other classes on the current class path (including all of DrJava's
     * internal classes) are also included.  But we're probably stuck doing something like this if we
     * want to continue bundling JUnit with DrJava.
     */
    result = IterUtil.compose(result, RUNTIME_CLASS_PATH);
    
    return result;
  }
  
  /** Adds the project root (if a project is open), the source roots for other open documents, the paths in the 
    * "extra classpath" config option, as well as any project-specific classpaths to the interpreter's classpath. 
    * This method is called in DefaultInteractionsModel when the interpreter becomes ready.
    */
  public void resetInteractionsClassPath() {
    Iterable<File> projectExtras = getExtraClassPath();
    //System.out.println("Adding project classpath vector to interactions classpath: " + projectExtras);
    if (projectExtras != null)  for (File cpE : projectExtras) { _interactionsModel.addProjectClassPath(cpE); }
    
    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if (cp != null) {
      for (File f : cp) { _interactionsModel.addExtraClassPath(f); }
    }
    
    for (OpenDefinitionsDocument odd: getAuxiliaryDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try { _interactionsModel.addProjectFilesClassPath(odd.getSourceRoot()); }
      catch(InvalidPackageException e) {  /* ignore it */ }
    }
    
    for (OpenDefinitionsDocument odd: getNonProjectDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try { 
        File sourceRoot = odd.getSourceRoot();
        if (sourceRoot != null) _interactionsModel.addExternalFilesClassPath(sourceRoot); 
      }
      catch(InvalidPackageException e) { /* ignore it */ }
    }
    
    // add project source root to projectFilesClassPath.  All files in project tree have this root.
    
    _interactionsModel.addProjectFilesClassPath(getProjectRoot());
    setClassPathChanged(false);  // reset classPathChanged state
  }
  
//  private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
//    public void optionChanged (OptionEvent<Vector<File>> oce) {
//      Vector<File> cp = oce.value;
//      if (cp != null) {
//        for (File f: cp) {
//          // this forwards directly to InterpreterJVM.addClassPath(String)
//          try { _interactionsModel.addExtraClassPath(f.toURL()); }
//          catch(MalformedURLException murle) { 
//            /* do nothing; findbugs signals a bug unless this catch clause spans more than two lines */ 
//          }
//        }
//      }
//    }
//  }
  
}
