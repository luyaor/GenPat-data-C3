/*BEGIN_COPYRIGHT_BLOCK
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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;


import java.io.File;
import java.awt.event.InputEvent;
  import java.awt.event.KeyEvent;
  
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.DummyInteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.CompletionMonitor;
import java.util.Date;

/** Test functions of InteractionsPane.
 *  @version $Id$
 */
public final class InteractionsPaneTest extends DrJavaTestCase {
  
  private static final char UNDEFINED = KeyEvent.CHAR_UNDEFINED;
  private static final int PRESSED = KeyEvent.KEY_PRESSED;
  private static final int RELEASED = KeyEvent.KEY_RELEASED;
  private static final int SHIFT = InputEvent.SHIFT_MASK;
  private static final int TYPED = KeyEvent.KEY_TYPED;
  private static final int VK_UNDEF = KeyEvent.VK_UNDEFINED;

  protected volatile InteractionsDJDocument _adapter;
  protected volatile InteractionsModel _model;
  protected volatile InteractionsDocument _doc;
  protected volatile InteractionsPane _pane;
  protected volatile InteractionsController _controller;

  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    _adapter = new InteractionsDJDocument();
    _model = new TestInteractionsModel(_adapter);
    _doc = _model.getDocument();
    _pane = new InteractionsPane(_adapter) {
      public int getPromptPos() { return _model.getDocument().getPromptPos(); }
    };
    // Make tests silent
    _pane.setBeep(new TestBeep());
    _controller = new InteractionsController(_model, _adapter, _pane);
    _controller.setCachedCaretPos(_pane.getCaretPosition());
    _controller.setCachedPromptPos(_doc.getPromptPos());
//    System.err.println("_controller = " + _controller);
  }

  public void tearDown() throws Exception {
//    _controller = null;
//    _doc = null;
//    _model = null;
//    _pane = null;
//    _adapter = null;
    super.tearDown();
  }

  /** Tests that this.setUp() puts the caret in the correct position. */
  public void testInitialPosition() {
    assertEquals("Initial caret not in the correct position.", _pane.getCaretPosition(), _doc.getPromptPos());
  }

  /** Tests that moving the caret left when it's already at the prompt will cycle it to the end of the line. */
  public void testCaretMovementCyclesWhenAtPrompt() throws EditDocumentException {
    _doc.append("test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        _controller.moveToPrompt();
        _controller.moveLeftAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved left at the prompt.", _doc.getLength(), _pane.getCaretPosition());
  }

  /** Tests that moving the caret right when it's already at the end will cycle it to the prompt. */
  public void testCaretMovementCyclesWhenAtEnd() throws EditDocumentException {
    _doc.append("test text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _controller.moveToEnd();
        _controller.moveRightAction.actionPerformed(null);
      }
    });
    assertEquals("Caret was not cycled when moved right at the end.", _doc.getPromptPos(), _pane.getCaretPosition());
  }

  /** Tests that moving the caret left when it's before the prompt will cycle it to the prompt. */
  public void testLeftBeforePromptMovesToPrompt() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.moveLeftAction.actionPerformed(null);
      }
    });
    assertEquals("Left arrow doesn't move to prompt when caret is before prompt.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /** Tests that moving the caret right when it's before the prompt will cycle it to the end of the document. */
  public void testRightBeforePromptMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.moveRightAction.actionPerformed(null);
      }
    });
    assertEquals("Right arrow doesn't move to end when caret is before prompt.",
                 _doc.getLength(),
                 _pane.getCaretPosition());
  }

  /** Tests that moving the caret up (recalling the previous command from history) will move the caret to the end
   *  of the document.
   */
  public void testHistoryRecallPrevMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() {  
      public void run() {
      _pane.setCaretPosition(1);
      _controller.historyPrevAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on up arrow.", _doc.getLength(), _pane.getCaretPosition());
  }

  /** Tests that moving the caret down (recalling the next command from history) will move the caret to the end of
   *  the document.
   */
  public void testHistoryRecallNextMovesToEnd() {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _pane.setCaretPosition(1);
        _controller.historyNextAction.actionPerformed(null);
      }
    });
    assertEquals("Caret not moved to end on down arrow.", _doc.getLength(), _pane.getCaretPosition());
  }

  public void testCaretStaysAtEndDuringInteraction() throws EditDocumentException {
//    System.err.println("start caret pos = " + _pane.getCaretPosition());
//    System.err.println("start prompt pos = " + _doc.getPromptPos());
    _doc.setInProgress(true);
//    System.err.println(_pane.getCaretPosition());
    _doc.append("simulated output", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    _doc.setInProgress(false);
//    System.err.println("caret pos = " + _pane.getCaretPosition());
//    System.err.println("prompt pos = " + _doc.getPromptPos());
//    System.err.println("Document = |" + _doc.getDocText(0, _doc.getLength()) + "|");
    assertEquals("Caret is at the end after output while in progress.", _doc.getLength(), _pane.getCaretPosition());
  }

  /** Tests that the caret catches up to the prompt if it is before it and output is displayed. */
  public void testCaretMovesUpToPromptAfterInsert() throws EditDocumentException {
    _doc.append("typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(1); } });
    _controller.setCachedCaretPos(1);
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("Caret is at the prompt after output inserted.", _doc.getPromptPos(), _pane.getCaretPosition());
    
    _doc.insertPrompt();
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(1); } });
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("Caret is at the end after output inserted.", _doc.getPromptPos(), _pane.getCaretPosition());
  }

  /** Tests that the caret is moved properly when the current interaction is cleared. */
  public void testClearCurrentInteraction() throws EditDocumentException {
    _doc.append("typed text", InteractionsDocument.DEFAULT_STYLE);
    Utilities.invokeAndWait(new Runnable() { public void run() { _controller.moveToEnd(); } });

    _doc.clearCurrentInteraction();
    Utilities.clearEventQueue();
    assertEquals("Caret is at the prompt after output cleared.", _doc.getPromptPos(), _pane.getCaretPosition());
    assertEquals("Prompt is at the end after output cleared.", _doc.getLength(), _doc.getPromptPos());
  }

  /** Tests that the InteractionsPane cannot be edited before the prompt. */
  public void testCannotEditBeforePrompt() throws EditDocumentException {
    _doc.acquireWriteLock();
    int origLength = 0;
    try {
      origLength = _doc.getLength();
      _doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    }
    finally { _doc.releaseWriteLock(); }
    assertEquals("Document should not have changed.", origLength, _doc.getLength());
  }

  /** Tests that the caret is put in the correct position after an insert. */
  public void testCaretUpdatedOnInsert() throws EditDocumentException {
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        
        // Type 'T'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_T, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'T'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_T, UNDEFINED));
        
        // Type 'Y'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_Y, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'Y'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_Y, UNDEFINED));
        
         // Type 'P'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_P, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'P'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_P, UNDEFINED));
        
         // Type 'E'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_E, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'E'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_E, UNDEFINED));
        
         // Type 'D'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'D'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
      }
    });
//    System.err.println("Document = '" + _doc.getText() + "'");
    Utilities.clearEventQueue();
    Utilities.clearEventQueue();
    assertEquals("caret should be at end of document", _doc.getLength(), _pane.getCaretPosition());
       
    final int pos = _doc.getLength() - 5;
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(pos); } });
    _controller.setCachedCaretPos(pos);
//    System.err.println("docLength = " +  _doc.getLength() + " caretPos = " + _pane.getCaretPosition());
    
    // Insert text before the prompt
    _doc.insertBeforeLastPrompt("aa", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
//    System.err.println("Document = '" + _doc.getText() + "'");
    assertEquals("caret should be in correct position", pos + 2, _pane.getCaretPosition());

    // Move caret to prompt and insert more text
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(_doc.getPromptPos()); } });
    _doc.insertBeforeLastPrompt("b", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());

    // Move caret before prompt and insert more text
    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(0); } });
    _doc.insertBeforeLastPrompt("ccc", InteractionsDocument.DEFAULT_STYLE);
    Utilities.clearEventQueue();
    assertEquals("caret should be at prompt", _doc.getPromptPos(), _pane.getCaretPosition());

    // Move caret after prompt and insert more text
    final int newPos = _doc.getPromptPos();
    // simulate a keystroke by putting caret just *after* pos of insert
    _pane.setCaretPosition(newPos + 1);
    _controller.setCachedCaretPos(newPos + 1);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        // Type 'D'
        _pane.processKeyEvent(new KeyEvent(_pane, PRESSED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
        _pane.processKeyEvent(new KeyEvent(_pane, TYPED, (new Date()).getTime(), 0, VK_UNDEF, 'D'));
        _pane.processKeyEvent(new KeyEvent(_pane, RELEASED, (new Date()).getTime(), SHIFT, KeyEvent.VK_D, UNDEFINED));
      } 
    });
    Utilities.clearEventQueue();
    assertEquals("caret should be one char after the inserted D", newPos + 2, _pane.getCaretPosition());
  }
  
  public void testSystemIn() {
    final Object bufLock = new Object();
    final StringBuilder buf = new StringBuilder();
    
    final CompletionMonitor completionMonitor = new CompletionMonitor();
    
    _controller.addConsoleStateListener(new InteractionsController.ConsoleStateListener() {
      public void consoleInputStarted(InteractionsController c) {
        completionMonitor.set();
      }     
      public void consoleInputCompleted(String text, InteractionsController c) {
        // do not assert the text here since it won't be called from the testing thread.
        // It is called on the following thread that calls getConsoleInput()
      }
    });
    
    new Thread("Testing System.in") {
      public void run() {
        synchronized(bufLock) {
          String s = _controller.getInputListener().getConsoleInput();
          buf.append(s);
        }
      }
    }.start();
    
    // Wait for console input to begin
    completionMonitor.waitOne();
        
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        _controller.insertConsoleText("test-text"); 
        _controller.interruptConsoleInput();
      }
    });
    
    // Make sure the buffer 'buf' is updated
    synchronized(bufLock) {
      assertEquals("Should have returned the correct text.", "test-text\n", buf.toString());
    }
  }
  
  // NOT USED
//  /** Fields used in a closure in testPromptList */
//  private volatile int _firstPrompt, _secondPrompt, _size;
//  private volatile boolean _resetDone;
//  
//  public void testPromptListClearedOnReset() throws Exception {
//    // Can't use the fields declared in setUp; we need a real InteractionsModel
//    final MainFrame _mf = new MainFrame();
//    final Object _resetLock = new Object();
//    
//    Utilities.clearEventQueue();
//    GlobalModel gm = _mf.getModel();
//    _controller = _mf.getInteractionsController();
//    _model = gm.getInteractionsModel();
//    _adapter = gm.getSwingInteractionsDocument();
//    _doc = gm.getInteractionsDocument();
//    _pane = _mf.getInteractionsPane();
//    
//    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.resetPrompts(); } });
//    
//    Utilities.clearEventQueue();
//
////    System.err.println(_pane.getPromptList());
//    assertEquals("PromptList before insert should contain 0 elements", 0, _pane.getPromptList().size());
//        
//    // Insert some text 
//    _doc.append("5", InteractionsDocument.NUMBER_RETURN_STYLE);
//
//    Utilities.invokeAndWait(new Runnable() { public void run() { _pane.setCaretPosition(_doc.getLength()); } });
////    System.err.println(_pane.getPromptList());
//    
//    Utilities.clearEventQueue();
//    
//    assertEquals("PromptList after insert should contain 1 element", 1, _pane.getPromptList().size());    
//    assertEquals("First prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length(),
//                 (int)_pane.getPromptList().get(0)); //needs cast to prevent ambiguity
//    
//    _doc.insertPrompt();
//    Utilities.clearEventQueue();
//    
//    assertEquals("PromptList has length 2", 2, _pane.getPromptList().size());
//    
//    Utilities.invokeAndWait(new Runnable() {
//      public void run() { 
//        _pane.setCaretPosition(_doc.getLength());
//        _firstPrompt = (int) _pane.getPromptList().get(0); // cast prevents ambiguity
//        _secondPrompt = (int) _pane.getPromptList().get(1); // cast prevents ambiguity
//      }
//    });
//    
//    assertEquals("PromptList after insertion of new prompt should contain 2 elements", 2, _pane.getPromptList().size());
//    assertEquals("First prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length(),
//                 _firstPrompt); 
//    assertEquals("Second prompt should be saved as being at position",
//                 _model.getStartUpBanner().length() + InteractionsDocument.DEFAULT_PROMPT.length() * 2 + 1,
//                 _secondPrompt); 
//    
//    synchronized(_resetLock) { _resetDone = false; }
//    _model.addListener(new DummyInteractionsListener() {
//      public void interpreterReady(File wd) {
//        synchronized(_resetLock) {
//          _resetDone = true;
//          _resetLock.notifyAll();
//        }
//      }});
//      
////    System.err.println("Executing reset interpreter");  
//    _model.resetInterpreter(FileOption.NULL_FILE);
//    Utilities.clearEventQueue();
// 
//    /* Wait until reset has finished. Reset is started just before interpreterReady notification. */
//    synchronized(_resetLock) { while (! _resetDone) _resetLock.wait(); }
//    Utilities.clearEventQueue();
// 
//    _doc.acquireWriteLock();
//    try {  // wait until the reset operation (which is queued ahead of us) has grabbed the WriteLock
//      Utilities.invokeAndWait(new Runnable() { public void run() {  _size = _pane.getPromptList().size(); } });
//    }
//    finally { _doc.releaseWriteLock(); }
//      
//    Utilities.clearEventQueue();
////    System.err.println("PromptList for pane " + _pane.hashCode() + " is " + _pane.getPromptList());
//    
//    assertEquals("PromptList after reset should contain one element", 1, _size);
//  }
    
}
