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

// DrJava stuff
import edu.rice.cs.drjava.model.GlobalModel;

// JSwat stuff
import com.bluemarsh.jswat.*;
import com.bluemarsh.jswat.ui.*;
import com.sun.jdi.Bootstrap;

/**
 * Interface between DrJava and JSwat, a Java debugger.
 * 
 * @version $Id$
 */
public class DebugManager {
  private boolean _isReady; // status of debugger

  private Session _session;
  private JSwat _swat;
  
  private GlobalModel _model;
  
  /**
   * Builds a new DebugManager which interfaces to JSwat.
   * Does not instantiate JSwat until init is called.
   */
  public DebugManager(GlobalModel model) {
    _isReady = false;
    _session = null;
    _swat = null;
    _model = model;
  }
  
  /**
   * Returns the status of the debugger
   */
  public boolean isReady() {
    return _isReady;
  }
  
  /**
   * Prepares an instantiation of the debugger.
   */
  public void init(UIAdapter adapter) {
    _session = new Session();
    
    // Test for the JDI package before we continue.
    Bootstrap.virtualMachineManager();
    
    _swat = JSwat.instanceOf();
   
    // Load the jswat settings file.
    File dir = new File(System.getProperty("user.home") + File.separator + ".jswat");
    File f = new File(dir, "settings");
    AppSettings props = AppSettings.instanceOf();
    props.load(f);
    
    adapter.buildInterface();
    _session.init(adapter);

    _session.addListener(new DebugOutputAdapter());

     _isReady = true;
   }
  
  /**
   * 
   */
  public void endSession() {
    Main.endSession(_session);
  }
  
  /**
   * When the debugger is no longer needed, this method 
   * removes it from memory.
   */
  public void cleanUp() {
    
    _session = null;
    _swat = null;
    
    _isReady = false;
  }
    
  /**
   * Sends a command directly to JSwat.
   * This method is specific to the JSwat debugger.
   * @param command JSwat command to perform
   */
  public void performCommand(String command) {
    Manager manager = _session.getManager(CommandManager.class);
    ((CommandManager)manager).handleInput(command);
  }
  
  /**
   * Attaches the given Writer directly to JSwat's status Log.
   * This method is specific to the JSwat debugger.
   */
  public void attachLogWriter(Writer w) {
    Log log = _session.getStatusLog();
    log.attach(w);
    log.start();
  }
  
  /*
  public void start();
  
  public void resume();
  
  public void stop();
  
  public void removeAllBreakpoints();
  
  public void getBreakpoints();
  
  public void setBreakpoint();
    
  public void removeBreakpoint();
  
  public void addWatch();
  
  public void removeWatch();
  */
  
  /*
   * Class DebugOutputAdapter is responsible for displaying the output
   * of a debuggee process to the Log. It reads both the standard output
   * and standard error streams from the debuggee VM. For it to operate
   * correctly it must be added as a session listener.
   *
   * @author  Nathan Fiedler, with modifications
   */
  class DebugOutputAdapter implements SessionListener {
    /** When this reaches 2, the output streams are finished. */
    protected int outputCompleteCount;
    
    
    /**
     * Constructs a DebugOutputAdapter to output to the given Log.
     *
     * @param  log  Log to output to.
     */
    public DebugOutputAdapter() {

    } // DebugOutputAdapter
    
    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
      // Attach to the stderr and stdout input streams of the passed
      // VirtualMachine and begin reading from them. Everything read
      // will be displayed in the text area.
      com.sun.jdi.VirtualMachine vm = session.getVM();
      if (vm.process() == null) {
        // Must be a remote process, which can't provide us
        // with an input and error streams.
        // We're automatically finished reading output.
        outputCompleteCount = 2;
      } else {
        // Assume output reading is not complete.
        outputCompleteCount = 0;
        // Create readers for the input and error streams.
        displayOutput(vm.process().getErrorStream(),false);
        displayOutput(vm.process().getInputStream(),true);
      }
    } // activate
    
    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
    } // close
    
    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public synchronized void deactivate(Session session) {
      // Wait for the output readers to finish.
      while (outputCompleteCount < 2) {
        try {
          wait();
        } catch (InterruptedException ie) {
          break;
        }
      }
    } // deactivate
    
    /** 
     * Create a thread that will retrieve and display any output
     * from the given input stream.
     *
     * @param  is  InputStream to read from.
     */

    protected void displayOutput(final InputStream is, final boolean isOut) {
      Thread thr = new Thread("output reader") { 
        public void run() {
          try {
            BufferedReader br =
              new BufferedReader(new InputStreamReader(is));
            String line;
            // Dump until there's nothing left.
            while ((line = br.readLine()) != null) {
              line += "\n";
              if (isOut)
                _model.debugSystemOutPrint(line);
              else
                _model.debugSystemErrPrint(line);
            }
          } catch (IOException ioe) {
            _model.debugSystemErrPrint("Error reading from streams.\n");
          } finally {
            notifyOutputComplete();
          }
        }
      };
      thr.setPriority(Thread.MIN_PRIORITY);
      thr.start();
    } // displayOutput
    
    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     */
        public void init(Session session) {
        } // init
    
    /**
     * Notify any waiters that one of the reader threads has
     * finished reading its output. This must be a separate
     * method in order to be synchronized on 'this' object.
     */
    protected synchronized void notifyOutputComplete() {
      outputCompleteCount++;
      notifyAll();
    } // notifyOutputComplete
  } // DebugOutputAdapter
}
