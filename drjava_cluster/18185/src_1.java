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

package edu.rice.cs.util.newjvm;

import edu.rice.cs.drjava.DrJavaTestCase;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

import java.rmi.RemoteException;

/** Test cases for the master/slave jvm control framework.
  * @version $Id$
  */
public class IntegratedMasterSlaveTest extends DrJavaTestCase {
  
  private static Log _log = new Log("MasterSlave.txt", false);
  
  volatile TestMasterJVM _testMaster = _newTestMasterJVM(); // JUnit ensures separate copy for each test
  
  private TestMasterJVM _newTestMasterJVM()  { 
    try { return new TestMasterJVM(); }
    catch(RemoteException e) { throw new UnexpectedException(e); }
  }
  
  public void tearDown() throws Exception {
    _testMaster.dispose();
    super.tearDown();
  }
  
  public void testItAll() throws Exception {
    // run a couple of times. each one forks its own jvm so not
    // too many! we run multiple times to prove that the master
    // can invoke multiple slaves (only one active at a time though)
    for (int i = 0; i < 2; i++) _testMaster.runTestSequence();
    _log.log("testItAll completed");
  }
  
  public void testImmediateQuit() throws Exception {
    for (int i = 0; i < 5; i++)  _testMaster.runImmediateQuitTest();
    _log.log("testImmediateQuit completed");
  }
  
  
  private class TestMasterJVM extends AbstractMasterJVM implements TestMasterRemote {
    
    /** Field and lock used to signal slave quit events. */
    private volatile boolean _justQuit;                     // true after slave quits
    private final Object _quitLock = new Object();
    
    /** Field and lock used to signal slave connected events. */
    private volatile boolean _connected;                    // true when slave is connected
    private final Object _connectedLock = new Object();
    
    /** Field and lock used to signal letter change events. */
    private volatile char _letter;
    private final Object _letterLock = new Object();
    
    private volatile String _currentTest = "";
    
    public TestMasterJVM() throws RemoteException { super(CounterSlave.class.getName()); }
    
    /** In util-20020414-0647, if quitSlave were called between the time the slave was invoked and the time it 
      * registered, an IllegalStateException was thrown. The correct behavior, which we test for here, is for the
      * slave to quit as soon as it is started up.
      */
    public void runImmediateQuitTest() throws Exception {
      
//      Utilities.show("ImmediateQuitTest started");
      
      _currentTest = "runImmediateQuitTest";
      _justQuit = false; 
      _connected = false;
      _letter = 'a';  // this needs to be reset because the slave is going to check it!
      
      invokeSlave(new String[0], FileOps.NULL_FILE);
      
//      Utilities.show("slave invoked");
      
      // Immediately call quit, which should not throw an exception. It should return without waiting.
      quitSlave();
      
//      Utilities.show("slave quit");     
      
      // now we just wait for the quit to process
      synchronized(_quitLock) { while (! _justQuit) _quitLock.wait(); }
      
      _currentTest = "";  // If we get here, it worked as expected.
      
      
//      Utilities.show("ImmediateQuitTest finished");
      _log.log("Ran immediateQuitTest");
      
      // (All of the post-quit invariants are checked in handleSlaveQuit.
    }
    
    public void runTestSequence() throws Exception {
      
      _currentTest = "runTestSequence";
      _justQuit = false;
      _connected = false;
      _letter = 'a';
      
      invokeSlave(new String[0], FileOps.NULL_FILE);           
      
      synchronized(_connectedLock) { while (! _connected) _connectedLock.wait();  }
      
      ((TestSlaveRemote)getSlave()).startLetterTest();
      
      _log.log("letter test started");
      
      // now, wait until five getletter calls passed; after fifth call letter is 'f' due to the ++
      synchronized(_letterLock) { while (_letter != 'f') { _letterLock.wait(); } }
      
      _log.log("letter test finished");
      
      for (int i = 0; i < 7; i++) {
        int value = ((TestSlaveRemote) getSlave()).getNumber();
        assertEquals("value returned by slave", i, value);
      }
      
      _log.log("number test finished");
      
      quitSlave();
      synchronized(_quitLock) { while (! _justQuit) _quitLock.wait(); } // for quit to finish
      _currentTest = "";
      _log.log("Ran runTestSequence");
    }
    
    public char getLetter() {
      synchronized(_letterLock) {
        char ret = _letter;
        _letter++;
        _letterLock.notify();
        return ret;
      }
    }
    
    protected void handleSlaveConnected() {
      TestSlaveRemote slave = (TestSlaveRemote) getSlave();
      assertTrue("slave is set", slave != null);
      assertTrue("startUp not in progress", ! isStartupInProgress());
      // getLetter should have never been called.
      assertEquals("letter value", 'a', _letter);
      synchronized(_connectedLock) { 
        _connected = true;
        _connectedLock.notify(); 
      }
      _log.log("_handleSlaveConnected() finished");
    }
    
    protected void handleSlaveQuit(int status) {
      assertEquals("slave result code", 0, status);
      if (_currentTest.equals("runTestSequence")) {
        // 5 letter calls must have occurred, so 'f' should be next
        assertEquals("last letter returned", 'f', _letter);
      }
      assertTrue("slave is not set", getSlave() == null);
      assertTrue("startUp not in progress", ! isStartupInProgress());
      
      // alert test method that quit occurred.
      synchronized(_quitLock) {
        _justQuit = true;
        _quitLock.notify();
      }
    }
    
    /** Called if the slave JVM dies before it is able to register.
      * @param cause The Throwable which caused the slave to die.
      */
    public void errorStartingSlave(Throwable cause) throws RemoteException {
      fail("There was an error starting the slave JVM: " + cause);
    }
  }
  
  /** The slave will exit with error codes in the case of problems, since there is no other thing it can do!
    * <DL>
    * <DT>1</DT><DD>MasterRemote class cast exception.</DD>
    * <DT>2</DT><DD>Incorect value from getLetter</DD>
    * <DT>3</DT><DD>RemoteException caught</DD>
    * <DT>4</DT><DD>Timeout waiting for master JVM to call</DD>
    * <DT>5</DT><DD>Interrupted while waiting for master JVM to call</DD>
    * </DL>
    */
  public static class CounterSlave extends AbstractSlaveJVM implements TestSlaveRemote {
    
    public static final CounterSlave ONLY = newCounterSlave();
    
    private volatile int _counter = 0;
    private volatile TestMasterRemote _master = null;
    
    private CounterSlave() throws RemoteException { }
    
    private static CounterSlave newCounterSlave() {
      try { return new CounterSlave(); }
      catch(RemoteException e) { throw new UnexpectedException(e); }
    }
    
    public synchronized int getNumber() { return _counter++; }
    
    protected void handleStart(MasterRemote m) { _master = (TestMasterRemote) m; }
    
    public void startLetterTest() throws RemoteException {
      // Run this part of the test in a new thread, so this call will immediately return
      Thread thread = new Thread() {
        public void run() {
          try {
            for (char c = 'a'; c <= 'e'; c++) {
              char got = _master.getLetter();
              if (c != got) System.exit(2);
            }
            
            // OK, now wait up till 15 seconds for master jvm to call
            Thread.sleep(15000);
            System.exit(4);
          }
          catch (InterruptedException e) { System.exit(5); }
          catch (RemoteException re) {
            javax.swing.JOptionPane.showMessageDialog(null, re.toString());
            System.exit(3);
          }
          catch (ClassCastException cce) { System.exit(1); }
        }
      };
      thread.start();
    }
  }
  
  public interface TestSlaveRemote extends SlaveRemote {
    public int getNumber() throws RemoteException;
    public void startLetterTest() throws RemoteException;
  }
  
  public interface TestMasterRemote/*<SlaveType extends SlaveRemote>*/ extends MasterRemote/*<SlaveType>*/ {
    public char getLetter() throws RemoteException;
  }
}
