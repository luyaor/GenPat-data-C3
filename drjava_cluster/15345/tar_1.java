/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.ListModel;

import gj.util.Enumeration;
import gj.util.Hashtable;
import gj.util.Vector;

// DrJava stuff
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.config.OptionConstants;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

/**
 * An integrated debugger which attaches to the Interactions JVM using
 * Sun's Java Platform Debugger Architecture (JPDA/JDI) interface.
 * 
 * @version $Id$
 */
public class DebugManager {
  public static final int STEP_INTO = StepRequest.STEP_INTO;
  public static final int STEP_OVER = StepRequest.STEP_OVER;
  public static final int STEP_OUT = StepRequest.STEP_OUT;
  
  /**
   * Reference to DrJava's model.
   */
  private GlobalModel _model;
  
  /**
   * VirtualMachine of the interactions JVM.
   */
  private VirtualMachine _vm;
  
  /**
   * Manages all event requests in JDI.
   */
  private EventRequestManager _eventManager;

  /**
   * Vector of all current Breakpoints, with and without EventRequests.
   */
  private Vector<Breakpoint> _breakpoints;

  /**
   * Vector of all current Watches
   */
  private Vector<WatchpointData> _watchpoints;
  
  /**
   * Keeps track of any DebugActions whose classes have not yet been
   * loaded, so that EventRequests can be created when the correct
   * ClassPrepareEvent occurs.
   */
  private PendingRequestManager _pendingRequestManager;
  
  /**
   * Provides a way for the DebugManager to communicate with the view.
   */
  private LinkedList _listeners;
  
  /**
   * The Thread that the DebugManager is currently analyzing.
   */
  private ThreadReference _thread;
  
  /**
   * Builds a new DebugManager to debug code in the Interactions JVM,
   * using the JPDA/JDI interfaces.
   * Does not actually connect to the InteractionsJVM until startup().
   */
  public DebugManager(GlobalModel model) {
    _model = model;
    _vm = null;
    _eventManager = null;
    _thread = null;
    _listeners = new LinkedList();
    _breakpoints = new Vector<Breakpoint>();
    _watchpoints = new Vector<WatchpointData>();
    _pendingRequestManager = new PendingRequestManager(this);
  }

  /**
   * Attaches the debugger to the Interactions JVM to prepare for debugging.
   */
  public synchronized void startup() throws DebugException {
    if (!isReady()) {
      //DrJava.consoleOut().println("Starting up...");
      _attachToVM();
      //DrJava.consoleOut().println("Attached. VM = " +_vm);
    
      //DrJava.consoleOut().println("EventHandler started...");
      ThreadDeathRequest tdr = _eventManager.createThreadDeathRequest();
      tdr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      tdr.enable();
      EventHandler eventHandler = new EventHandler(this, _vm);
      eventHandler.start();
     // notifyListeners(new EventNotifier() {
       // public void notifyListener(DebugListener l) {
        //  l.debuggerStarted();
       // }
      //});
    }
  }
  
  /**
   * Handles the details of attaching to the InteractionsJVM.
   */
  private void _attachToVM() throws DebugException {
    // Get the connector
    VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
    List connectors = vmm.attachingConnectors();
    AttachingConnector connector = null;
    java.util.Iterator iter = connectors.iterator();
    while (iter.hasNext()) {
      AttachingConnector conn = (AttachingConnector)iter.next();
      if (conn.name().equals("com.sun.jdi.SocketAttach")) {
        connector = conn;
      }
    }
    if (connector == null) {
      throw new DebugException("Could not find an AttachingConnector!");
    }
    
    // Try to connect on our debug port
    Map args = connector.defaultArguments();
    Connector.Argument port = (Connector.Argument) args.get("port");
    try {
      int debugPort = _model.getDebugPort();
      //System.out.println("using port: " + debugPort);
      port.setValue("" + debugPort);
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
      //DrJava.consoleOut().println("Connected to VM @ port "+debugPort);
    }
    catch (IOException ioe) {
      throw new DebugException("Could not connect to VM: " + ioe);
    }
    catch (IllegalConnectorArgumentsException icae) {
      throw new DebugException("Could not connect to VM: " + icae);
    }
  }
  
  /**
   * Disconnects the debugger from the Interactions JVM and cleans up
   * any state.
   */
  public synchronized void shutdown() {
    if (isReady()) {
      //DrJava.consoleOut().println("Shutting down...");
      
      removeAllBreakpoints();
      try {
        _vm.dispose();
      }
      catch (VMDisconnectedException vmde) {
        //VM was shutdown prematurely
      }
      finally {
        _vm = null;
        _eventManager = null;
        
        //notifyListeners(new EventNotifier() {
        //  public void notifyListener(DebugListener l) {
        //    l.debuggerShutdown();
        //  }
        //});
      }
    }
  }
  
  /**
   * Returns the status of the debugger
   */
  public synchronized boolean isReady() {
    return _vm != null;
  }
  
  
  /**
   * Returns the current EventRequestManager from JDI, or null if 
   * startup() has not been called.
   */
  synchronized EventRequestManager getEventRequestManager() {
    return _eventManager;
  }
  
  /**
   * Returns the pending request manager used by the debugger.
   */
  synchronized PendingRequestManager getPendingRequestManager() {
    return _pendingRequestManager;
  }
  
  /**
   * Sets the debugger's currently active thread.
   */
  synchronized void setCurrentThread(ThreadReference thread) {
    _thread = thread;
  }
  
  /**
   * Returns the debugger's currently active thread.
   */
  synchronized ThreadReference getCurrentThread() {
    return _thread;
  }
  
  /**
   * Returns the loaded ReferenceType for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.
   */
  synchronized ReferenceType getReferenceType(String className) {
    return getReferenceType(className, -1);
  }
  
  /**
   * Returns the loaded ReferenceType for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.  If the lineNumber is greater than -1,
   * ensures that the returned ReferenceType contains the given lineNumber,
   * searching through inner classes if necessary.  If no inner classes
   * contain the line number, null is returned.
   */
  synchronized ReferenceType getReferenceType(String className, int lineNumber) {
    //System.out.println("Looking for RefType for class: " + className);
    //System.out.println("on line number: " + lineNumber);
    
    // Get all classes that match this name
    List classes = _vm.classesByName(className);
    //System.out.println("Num of classes found: " + classes.size());
    ReferenceType ref = null;
    
    // Assume first one is correct, for now
    if (classes.size() > 0) {
      ref = (ReferenceType) classes.get(0);
      
      if (lineNumber > DebugAction.ANY_LINE) {
        List lines = new LinkedList();
        try {
          lines = ref.locationsOfLine(lineNumber);
        }
        catch (AbsentInformationException aie) {
          // try looking in inner classes
        }
        if (lines.size() == 0) {
          // The ReferenceType might be in an inner class
          List innerRefs = ref.nestedTypes();
          ref = null;
          for (int i = 0; i < innerRefs.size(); i++) {
            try {
              ReferenceType currRef = (ReferenceType) innerRefs.get(i);
              lines = currRef.locationsOfLine(lineNumber);
              if (lines.size() > 0) {
                ref =currRef;
                break;                
              }
            }
            catch (AbsentInformationException aie) {
              // skipping this inner class, look in another
            }
          }
        }
      }
      if (ref != null && !ref.isPrepared()) {
         return null;
      }
    }
    return ref;
    //else throw new DebugException ("Couldn't find the class corresponding to '" + className +"'.");
  }
  
  
  /**
   * Suspends execution of the currently running document.
   */
  public synchronized void suspend() {
    if (_thread == null)
      DrJava.consoleOut().println("Suspend called while _thread was null");
    _thread.suspend();
    currThreadSuspended();
  }
  
  /**
   * Resumes execution of the currently loaded document.
   */
  public synchronized void resume() {
    if (_thread == null)
      DrJava.consoleOut().println("Resume called while _thread was null");
    _thread.resume();
    currThreadResumed();
  }
    
  /** 
   * Steps into the execution of the currently loaded document.
   * The flag denotes what kind of step to take. The following mark valid options:
   * StepRequest.STEP_INTO
   * StepRequest.STEP_OVER
   * StepRequest.STEP_OUT
   */
  public synchronized void step(int flag) throws DebugException {
    if (_thread == null) {
      throw new DebugException("Must have a thread to step.");
    }
    //if (!_thread.isSuspended()) 
      //DrJava.consoleOut().println("Current thread is not suspended while trying to create a step request!");    
    // don't allow the creation of a new StepRequest if there's already one on
    // the current thread
    List steps = _eventManager.stepRequests();    
    for (int i = 0; i < steps.size(); i++) {
      //DrJava.consoleOut().println("creating a new step: event thread: " + 
      //                            ((StepRequest)steps.get(i)).thread() + 
      //                            " current thread: " +
      //                            _thread);
      if (((StepRequest)steps.get(i)).thread().equals(_thread)) {
        DrJava.consoleOut().println("There's already a StepRequest on the current thread");
        DrJava.consoleOut().println("suspendCount: " + _thread.suspendCount());
        return;
      }
    }
        
    Step step = new Step(this, StepRequest.STEP_LINE, flag);
    resume();
    //System.out.println("_thread resumed");
  }
  

  /**
   * Adds a watchpoint to the given field at the given line in the given
   * document.
   * @param field the name of the field we will watch
   */
  public synchronized void addWatchpoint(String field) {   
    _watchpoints.addElement(new WatchpointData(field));//new Watchpoint (doc, field, lineNum, this));
  }
  

  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param offset Start offset on the line to set the breakpoint
   * @param lineNumber Line on which to set or remove the breakpoint
   */
  public synchronized void toggleBreakpoint(OpenDefinitionsDocument doc, 
                                            int offset, int lineNum)
    throws DebugException {  
    
    Breakpoint breakpoint = doc.getBreakpointAt(offset);
    if (breakpoint == null) {
      setBreakpoint(new Breakpoint (doc, offset, lineNum, this));
    }
    else {
      removeBreakpoint(breakpoint);
    }
  }
  
  /**
   * Sets a breakpoint.
   *
   * @param breakpoint The new breakpoint to set
   */
  public synchronized void setBreakpoint(final Breakpoint breakpoint)
  {
    //System.out.println("setting: " + breakpoint);
    /*
    if (breakpoint.getRequest() != null)
      _breakpoints.put(breakpoint.getRequest(), breakpoint);*/
    _breakpoints.addElement(breakpoint);
    breakpoint.getDocument().addBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointSet(breakpoint);
      }
    });
  }
  
 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  *
  * @param breakpoint The breakpoint to remove.
  * @param className the name of the class the BP is being removed from.
  */
  public synchronized void removeBreakpoint(final Breakpoint breakpoint) {
    //System.out.println("unsetting: " + breakpoint);
    _breakpoints.removeElement(breakpoint);
    
    if ( breakpoint.getRequest() != null && _eventManager != null) {
      _eventManager.deleteEventRequest(breakpoint.getRequest());
    }
    else {
      _pendingRequestManager.removePendingRequest(breakpoint);
    }
    breakpoint.getDocument().removeBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointRemoved(breakpoint);
      }
    });
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public synchronized void removeAllBreakpoints() {
    while (_breakpoints.size() > 0) {
      removeBreakpoint( _breakpoints.elementAt(0));
    }
  }

  /**
   * Called when a breakpoint is reached.  The Breakpoint object itself
   * should be stored in the "debugAction" property on the request.
   * @param request The BreakPointRequest reached by the debugger
   */
  synchronized void reachedBreakpoint(BreakpointRequest request) {
    Object property = request.getProperty("debugAction");
    if ( (property!=null) && (property instanceof Breakpoint)) {
      final Breakpoint breakpoint = (Breakpoint)property;
      _model.printDebugMessage("Breakpoint hit in class " + 
                               breakpoint.getClassName() + "  [line " +
                               breakpoint.getLineNumber() + "]");
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.breakpointReached(breakpoint);
        }
      });
    }
    else {
      // A breakpoint we didn't set??
    }
  }
  
  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public synchronized Vector<Breakpoint> getBreakpoints() {
    Vector<Breakpoint> sortedBreakpoints = new Vector<Breakpoint>();
    ListModel docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.getSize(); i++) {
      Vector<Breakpoint> docBreakpoints = 
        ((OpenDefinitionsDocument)docs.getElementAt(i)).getBreakpoints();
      for (int j = 0; j < docBreakpoints.size(); j++) {
        sortedBreakpoints.addElement(docBreakpoints.elementAt(j));
      }      
    }
    return sortedBreakpoints;
  }
  
  /**
   * Prints the list of breakpoints in the current session of DrJava, both pending
   * resolved Breakpoints are listed
   */
  public synchronized void printBreakpoints() {
    Enumeration<Breakpoint> breakpoints = getBreakpoints().elements();
    if (breakpoints.hasMoreElements()) {
      _model.printDebugMessage("Breakpoints: ");
      while (breakpoints.hasMoreElements()) {
        Breakpoint breakpoint = breakpoints.nextElement();
        _model.printDebugMessage("  " + breakpoint.getClassName() +
                                 "  [line " + breakpoint.getLineNumber() + "]");
      }
    }
    else {
      _model.printDebugMessage("No breakpoints set.");
    }
  }
  
  /**
   * Takes the location of event e, opens the document corresponding to its class
   * and centers the definition pane's view on the appropriate line number
   * @param e should be a LocatableEvent
   */
  synchronized void scrollToSource(LocatableEvent e) {
    Location location = e.location();
    OpenDefinitionsDocument doc = null;
    
    // First see if doc is stored
    EventRequest request = e.request();
    Object docProp = request.getProperty("document");
    if ((docProp != null) && (docProp instanceof OpenDefinitionsDocument)) {
      doc = (OpenDefinitionsDocument) docProp;
    }
    else {
      // No stored doc, look on the source root set (later, also the sourcepath)
      ReferenceType rt = location.declaringType();
      String className = rt.name();
      String ps = System.getProperty("file.separator");
      // replace periods with the System's file separator
      className = StringOps.replace(className, ".", ps);
      
      // crop off the $ if there is one and anything after it
      int indexOfDollar = className.indexOf('$');    
      if (indexOfDollar > -1) {
        className = className.substring(0, indexOfDollar);
      }
      
      String filename = className + ".java";
      Vector<File> sourcepath = 
        DrJava.CONFIG.getSetting(OptionConstants.DEBUG_SOURCEPATH);
      File f = _getSourceFileFromPaths(filename, sourcepath);
      //File f = null;
      if (f == null) {
        // If not on sourcepath, check source root set (open files)
        File[] sourceRoots = _model.getSourceRootSet();
        Vector<File> roots = new Vector<File>();
        for (int i=0; i < sourceRoots.length; i++) {
          roots.addElement(sourceRoots[i]);
        }
        f = _getSourceFileFromPaths(filename, roots);
      }
      
      if (f != null) {
        // Get a document for this file, forcing it to open
        //DrJava.consoleOut().println("found file: " + f.getAbsolutePath());
        try {
          //doc = _model.getDocumentForFile(f, location.lineNumber());
          doc = _model.getDocumentForFile(f);
        }
        catch (IOException ioe) {
          // No doc, so don't notify listener
          //DrJava.consoleOut().println("Problem opening file, won't scroll: " + ioe);
        }
        catch (OperationCanceledException oce) {
          // No doc, so don't notify listener
          //DrJava.consoleOut().println("Problem opening file, won't scroll: " + oce);
        }
      }
    }
    
    // Open and scroll if doc was found
    if (doc != null) {
      //DrJava.consoleOut().println("Will scroll to line: " + location.lineNumber());

      final OpenDefinitionsDocument docF = doc;
      final Location locationF = location;
        
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.threadLocationUpdated(docF, locationF.lineNumber());
        }
      });
    }
    else {
      //DrJava.consoleOut().println("couldn't open file to scroll to");
      String className = location.declaringType().name();
      printMessage("  (Source for " + className + " not found.)");
    }
  }
  
  /**
   * Searches for a source file with the given name on the provided paths.
   * Returns null if the file is not found.
   * @param filename Name of the source file to look for
   * @param paths An array of directories to search
   */
  private File _getSourceFileFromPaths(String filename, Vector<File> paths) {
    File f = null;
    for (int i = 0; i < paths.size(); i++) {
      String currRoot = paths.elementAt(i).getAbsolutePath();
      //DrJava.consoleOut().println("Trying to find " + currRoot + ps + className + 
      //                            ".java");
      f = new File(currRoot + System.getProperty("file.separator") + filename);
      if (f.exists()) {
        return f;
      }
    }
    return null;
  }
  
  
  /**
   * Prints a message in the Interactions Pane.
   * @param message Message to display
   */
  synchronized void printMessage(String message) {
    _model.printDebugMessage(message);
  }

  private void _updateWatchpoints() {
    int stackIndex = 0;
    StackFrame currFrame = null;
    List frames = null;
    try {
      frames = _thread.frames();
    }
    catch (IncompatibleThreadStateException itse) {
      DrJava.consoleOut().println("Thread has not been suspended");
    }
    currFrame = (StackFrame) frames.get(stackIndex);
    stackIndex++;
    Location location = currFrame.location();
    ReferenceType rt = location.declaringType();
    //DrJava.consoleOut().println("stack frame: " + frames);
    //DrJava.consoleOut().println("all fields: " + rt.allFields());
    for (int i = 0; i < _watchpoints.size(); i++) {
      WatchpointData currWatchpoint = _watchpoints.elementAt(i);
      String currName = currWatchpoint.getName();
      Object currValue = currWatchpoint.getValue();
      // check for "this"
      if (currName.equals("this")) {
        ObjectReference obj = currFrame.thisObject();
        if (obj != null) {
          currWatchpoint.setValue(obj);
          currWatchpoint.setType(obj.type());
        }
        else {
          currWatchpoint.setValue(null);
          currWatchpoint.setType(null);
        }
        continue;
      }          
      //List frames = null;
      LocalVariable localVar = null;
      try {
        localVar = currFrame.visibleVariableByName(currName);
      }
      catch (AbsentInformationException aie) {
        DrJava.consoleOut().println("No line number information for this method");
      }
      
      // if the variable being watched is not a local variable, check if it's a field
      if (localVar == null) {
        Field field = rt.fieldByName(currName);
        
        // if the variable is not a field either, it's not defined in this 
        // ReferenceType's scope, keep further out in scope.
        String className = rt.name();
        //ReferenceType outerRt;
        while (field == null) {
          
          // crop off the $ if there is one and anything after it
          int indexOfDollar = className.indexOf('$');    
          if (indexOfDollar > -1) {
            className = className.substring(0, indexOfDollar);
          }
          else {
            // There is no $ in the className, we're at the outermost class and the
            // field still was not found
            currWatchpoint.setValue(null);
            currWatchpoint.setType(null);
            break;
          }
          rt = (ReferenceType)_vm.classesByName(className).get(0);
          field = rt.fieldByName(currName);
        }
        if (field != null) {
          // check if the field is static
          if (field.isStatic()) {
            currWatchpoint.setValue(rt.getValue(field));
            try {
              currWatchpoint.setType(field.type());
            }
            catch (ClassNotLoadedException cnle) {
              DrJava.consoleOut().println("Class not loaded");
              currWatchpoint.setType(null);
            }
          }
          else {
            StackFrame outerFrame = currFrame;
            // the field is not static
            // check if the frame represents a native or static method and
            // keep going down the stack frame looking for the frame that
            // has the same ReferenceType that we found the Field in.
            // This is a hack, remove it to improve performance, it will
            // work sometimes, but not always.
            //DrJava.consoleOut().println("checking frames *");
            while (outerFrame.thisObject() != null && 
                   !outerFrame.thisObject().referenceType().equals(rt) &&
                   stackIndex < frames.size()) {
              outerFrame = (StackFrame) frames.get(stackIndex);
              //DrJava.consoleOut().println("outerFrame: " + outerFrame);
              stackIndex++;
            }
            if (stackIndex < frames.size() && outerFrame.thisObject() != null) { 
              // then we found the right stack frame
              currWatchpoint.setValue(outerFrame.thisObject().getValue(field));
              try {
                currWatchpoint.setType(field.type());
              }
              catch (ClassNotLoadedException cnle) {
                DrJava.consoleOut().println("Class not loaded");
                currWatchpoint.setType(null);
              }
            }
            else {
              currWatchpoint.setValue(null);
              currWatchpoint.setType(null);
            }
          }
        }
      }
      else {
        currWatchpoint.setValue(currFrame.getValue(localVar));
        try {
          currWatchpoint.setType(localVar.type());
        }
        catch (ClassNotLoadedException cnle) {
          DrJava.consoleOut().println("Class not loaded");
          currWatchpoint.setType(null);
        }
      }
    }
  }
  
  private void _displayWatchpoints() {
    DrJava.consoleOut().println("Watchpoints:");
    for (int i = 0; i < _watchpoints.size(); i ++) {
      WatchpointData currWatchpoint = _watchpoints.elementAt(i);  
      if (currWatchpoint.getChanged()) {
        DrJava.consoleOut().print("! ");
        DrJava.consoleOut().println(currWatchpoint);
      }
      else {
        DrJava.consoleOut().println(currWatchpoint);
      }
    }
  }
  
  /**
   * Notifies all listeners that the current thread has been suspended.
   */
  synchronized void currThreadSuspended() {
    //DrJava.consoleOut().println("DM: thread suspended (show)");
    _updateWatchpoints();
    _displayWatchpoints();
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadSuspended();
      }
    });
  }
  
  
  /**
   * Notifies all listeners that the current thread has been resumed.
   */
  synchronized void currThreadResumed() {
    //DrJava.consoleOut().println("DM: thread resumed (hide)");
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadResumed();
      }
    });
  }
  
  /**
   * Notifies all listeneres that the current thread has died.
   */
  synchronized void currThreadDied() {
    _model.printDebugMessage("The current thread has finished.");
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadDied();
      }
    });
  }
  
  /**
   * Notifies all listeneres that the debugger has shut down.
   */
  synchronized void notifyDebuggerShutdown() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerShutdown();
      }
    });
  }

 /**
   * Notifies all listeneres that the debugger has started.
   */
  synchronized void notifyDebuggerStarted() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerStarted();
      }
    });
  }
  /**
   * Adds a listener to this DebugManager.
   * @param listener a listener that reacts on events generated by the DebugManager
   */
  public synchronized void addListener(DebugListener listener) {
    _listeners.addLast(listener);
  }

  public synchronized void removeListener(DebugListener listener){
    //System.out.println("size A: "+_listeners.size());
    _listeners.remove(listener);
    //System.out.println("size B: "+_listeners.size());    
  }
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  protected void notifyListeners(EventNotifier n) {
    synchronized(_listeners) {
      ListIterator i = _listeners.listIterator();

      while(i.hasNext()) {
        DebugListener cur = (DebugListener) i.next();
        n.notifyListener(cur);
      }
    }
  }
  
  /**
   * Class model for notifying listeners of an event.
   */
  protected abstract class EventNotifier {
    public abstract void notifyListener(DebugListener l);
  }
  
  private class WatchpointData {
    String _name;
    Object _value;
    Type _type;
    boolean _changed;
    
    public WatchpointData(String name) {
      _name = name;
      _value = null;
      _type = null;
      _changed = false;
    }
    
    public String getName() {
      return _name;
    }
    
    public Object getValue() {
      return _value;
    }
    
    public Type getType() {
      return _type;
    }
    
    public void setName(String name) {
      _name = name;
    }
    
    public void setValue(Object value) {
      if (value != null) {
        if (!value.equals(_value)) {
          _changed = true;
        }
        else
          _changed = false;
      }
      else
        _changed = false;
      _value = value;
    }
    
    public void setType(Type type) {
      _type = type;
    }
    
    public boolean getChanged() {
      return _changed;
    }
    
    public String toString() {
      return _type + " " + _name + ": " + _value;
    }
  }
  
}