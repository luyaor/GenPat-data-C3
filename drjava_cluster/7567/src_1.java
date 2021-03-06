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

import edu.rice.cs.drjava.DrJava;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
//import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public class EventHandler extends Thread {
  
  private DebugManager _debugManager;
  private VirtualMachine _vm;
  private boolean _connected = true;  // Connected to VM
  
  EventHandler (DebugManager debugManager, VirtualMachine vm) {
    _debugManager = debugManager;
    _vm = vm;
  }
  
  public void run() {
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        //              System.err.println("Got Event Set, policy = " + eventSet.suspendPolicy());
        //boolean resumeStoppedApp = false;
        EventIterator it = eventSet.eventIterator();
        while (it.hasNext()) {
          //resumeStoppedApp |= !handleEvent(it.nextEvent());
          handleEvent(it.nextEvent());
        }
        
        /*if (resumeStoppedApp) {
          eventSet.resume();
        } else if (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL) {
          setCurrentThread(eventSet);
          notifier.vmInterrupted();
        }*/
      } catch (InterruptedException exc) {
        // Do nothing. Any changes will be seen at top of loop.
      } catch (VMDisconnectedException discExc) {
        System.out.println("Got disc exception");
        handleDisconnectedException();
        break;
      }
    }
  }
  
  public void handleEvent(Event e) {
    System.out.println("handleEvent: "+e);
    if (e instanceof BreakpointEvent) {
      _handleBreakpointEvent((BreakpointEvent) e);
    }
    else if (e instanceof ClassPrepareEvent) {
      _handleClassPrepareEvent((ClassPrepareEvent) e);
    }
    else if (e instanceof VMDeathEvent) {
      _handleVMDeathEvent((VMDeathEvent) e);
    }
    else if (e instanceof VMDisconnectEvent) {
      _handleVMDisconnectEvent((VMDisconnectEvent) e);
    }
    else 
      throw new Error("Unexpected event type");
  }
  
  private void _handleBreakpointEvent(BreakpointEvent e) {
    System.out.println("Breakpoint reached");
    _debugManager.hitBreakpoint((BreakpointRequest)e.request());
    //((LocatableEvent) e).thread().suspend();
  }
  
  private void _handleClassPrepareEvent(ClassPrepareEvent e) {
    DrJava.consoleOut().println("ClassPrepareEvent occured");
    DrJava.consoleOut().println("In " + e.referenceType().name());
    try {
      DrJava.consoleOut().println("sourcename " + e.referenceType().sourceName());
    }
    catch(AbsentInformationException aie) {
      DrJava.consoleOut().println("no info");
    }
    try {
      _debugManager.getPendingRequestManager().classPrepared(e);
    }
    catch(DebugException de) {
      System.err.println("Error preparing action: " + de);
    }
    // resumes this thread which was suspended because its 
    // suspend policy was SUSPEND_EVENT_THREAD
    e.thread().resume();
    DrJava.consoleOut().println("resumed thread");
  }
  
  private void _handleVMDeathEvent(VMDeathEvent e) {
    System.out.println("VM died");    
  }
  
  private void _handleVMDisconnectEvent(VMDisconnectEvent e) {
    System.out.println("VM disconnected");
    DrJava.consoleOut().println("event: "+e);
    _connected = false;
    _debugManager.shutdown();
  }
  
  /**
   * A VMDisconnectedException has happened while dealing with
   * another event. We need to flush the event queue, dealing only
   * with exit events (VMDeath, VMDisconnect) so that we terminate
   * correctly.
   */
  synchronized void handleDisconnectedException() {
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        EventIterator iter = eventSet.eventIterator();
        while (iter.hasNext()) {
          Event event = iter.nextEvent();
          if (event instanceof VMDeathEvent) {
            _handleVMDeathEvent((VMDeathEvent)event);
          } else if (event instanceof VMDisconnectEvent) {
            _handleVMDisconnectEvent((VMDisconnectEvent)event);
          } 
        }
        eventSet.resume(); // Resume the VM
      } catch (InterruptedException exc) {
        // ignore
      }
    }
  }
}
