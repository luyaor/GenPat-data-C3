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

package edu.rice.cs.drjava.ui;


import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.io.Serializable;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;

/** Abstract class that hooks a Swing console/interactions document with its Swing pane.
  * TODO: move interactions specific functionality to InteractionsController by creating ConsoleDJDocument class
  * @version $Id$
  */
public abstract class AbstractConsoleController /* implements Serializable */ {
  
  /** Adapter for the Swing document used by the model.*/
  protected final InteractionsDJDocument _swingConsoleDocument;

  /** Pane from the view. */
  protected final InteractionsPane _pane;

  /** Style to use for default text. */
  protected final SimpleAttributeSet _defaultStyle;

  /** Style to use for System.out. */
  protected final SimpleAttributeSet _systemOutStyle;

  /** Style to use for System.err. */
  protected final SimpleAttributeSet _systemErrStyle;

  /** Action to change focus to previous pane.  Package private for testing purposes. */
  volatile Action switchToPrevPaneAction;

  /** Action to change focus to next pane. */
  volatile Action switchToNextPaneAction;

  /** Initializes the Swing console document and Swing interactions pane. Subclasses *must* call _init() at the end 
   *  of their constructors.
   */
  protected AbstractConsoleController(InteractionsDJDocument doc, InteractionsPane pane) {
    _swingConsoleDocument = doc;
    _pane = pane;
    _defaultStyle = new SimpleAttributeSet();
    _systemOutStyle = new SimpleAttributeSet();
    _systemErrStyle = new SimpleAttributeSet();
  }

  /** Gets the console document for this console.*/
  public abstract ConsoleDocument getConsoleDoc();

  /** Initialization method.  *Must* be called in constructor by all subclasses. */
  protected void _init() {
    _addDocumentStyles();
    _setupModel();
    _setupView();
  }

  /** Adds AttributeSets as named styles to the Swing console document. */
  protected void _addDocumentStyles() {
    // Default
    _swingConsoleDocument.setDocStyle(ConsoleDocument.DEFAULT_STYLE, _defaultStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        setDefaultFont(oe.value);
      }
    });

    // System.out
    _systemOutStyle.addAttributes(_defaultStyle);
    _systemOutStyle.addAttribute(StyleConstants.Foreground,
                                 DrJava.getConfig().getSetting(OptionConstants.SYSTEM_OUT_COLOR));
    _swingConsoleDocument.setDocStyle(ConsoleDocument.SYSTEM_OUT_STYLE, _systemOutStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_OUT_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _systemOutStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });

    // System.err
    _systemErrStyle.addAttributes(_defaultStyle);
    _systemErrStyle.addAttribute(StyleConstants.Foreground,
                                 DrJava.getConfig().getSetting(OptionConstants.SYSTEM_ERR_COLOR));
    _swingConsoleDocument.setDocStyle(ConsoleDocument.SYSTEM_ERR_STYLE, _systemErrStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_ERR_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _systemErrStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
  }

  /** Sets the font for the document, updating all existing text.  This behavior is only necessary in Mac OS X, since
    * setFont() works fine on JTextPane on all other tested platforms.  This glitch in the Mac JVM still exists as of
    * 11-28-06 in beta Java 6.0 build 88.
    * @param f New font to use.
    */
  public void setDefaultFont(Font f) {
    Color c = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    setDefaultFont(f, c);
  }

  /** Sets the color for the document, updating all existing text.  This behavior is only necessary in Mac OS X, since
    * changing the main font works on all other tested platforms.
    * @param c New color to use.
    */
  public void setDefaultFont(Color c) {
    Font f = DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN);
    setDefaultFont(f, c);
  }

  /** Sets the font and color for the document, updating all existing text.  This behavior is only necessary in Mac OS
    * X, since setFont() and changing the main font works on all other tested platforms.
    * @param f New font to use.
    * @param c New color to use.
    */
  public void setDefaultFont(Font f, Color c) {
    if (PlatformFactory.ONLY.isMacPlatform()) {
      SimpleAttributeSet fontSet = new SimpleAttributeSet();
      StyleConstants.setFontFamily(fontSet, f.getFamily());
      StyleConstants.setFontSize(fontSet, f.getSize());
      StyleConstants.setBold(fontSet, f.isBold());
      StyleConstants.setItalic(fontSet, f.isItalic());
      if (c != null) {
        StyleConstants.setForeground(fontSet, c);
      }
      _swingConsoleDocument.setCharacterAttributes(0, _swingConsoleDocument.getLength()+1, fontSet, false);
      _pane.setCharacterAttributes(fontSet, false);
      _updateStyles(fontSet);
    }
  }

  /** Updates all document styles with the attributes contained in newSet.
    * @param newSet Style containing new attributes to use.
    */
  protected void _updateStyles(AttributeSet newSet) {
    _defaultStyle.addAttributes(newSet);
    _systemOutStyle.addAttributes(newSet);
    _systemErrStyle.addAttributes(newSet);
  }

  /** Sets up the model.*/
  protected abstract void _setupModel();
  
  /** Sets up the view. */
  protected void _setupView() {
    KeyStroke beginLineKey = DrJava.getConfig().getSetting(OptionConstants.KEY_BEGIN_LINE);
    _pane.addActionForKeyStroke(beginLineKey, gotoPromptPosAction);
    _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(beginLineKey),
                                selectToPromptPosAction);
    KeyStroke endLineKey = DrJava.getConfig().getSetting(OptionConstants.KEY_END_LINE);
    _pane.addActionForKeyStroke(endLineKey, gotoEndAction);
    _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(endLineKey),
                                selectToEndAction);

    DrJava.getConfig().addOptionListener(OptionConstants.KEY_BEGIN_LINE,
                                         new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoPromptPosAction);
        _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(oe.value),
                                    selectToPromptPosAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_END_LINE,
                                         new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoEndAction);
        _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(oe.value),
                                    selectToEndAction);
     }
    });
    
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_CUT, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_COPY, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
     }
    });
  }
  
  /** Clears and resets the view (other than features derived from the model. */
  public void resetView() {
//    _pane.resetPrompts();  // NOT USED
//    System.err.println("Prompts.reset" + "Prompts for pane " + _pane.hashCode() + " is " + _pane.getPromptList());
  }
  
  /** Default cut action. */
  Action cutAction = new DefaultEditorKit.CutAction() {
    public void actionPerformed(ActionEvent e) {
      
      if (_pane.getSelectedText() != null) {
        super.actionPerformed(e);
        String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(_pane);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
    }
  };
  
  /** Default copy action. */
  Action copyAction = new DefaultEditorKit.CopyAction() {
    public void actionPerformed(ActionEvent e) {
      if (_pane.getSelectedText() != null) {
        super.actionPerformed(e);
        String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(_pane);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
    }
  };

  /** Accessor method for the InteractionsDJDocument. */
  public InteractionsDJDocument getDocumentAdapter() { return _swingConsoleDocument; }

  /** Accessor method for the InteractionsPane. */
  public InteractionsPane getPane() { return _pane; }

  /** Determines if the associated console pane is currently computing.
   *  @return true iff the console is busy
   */
  protected boolean _busy() { return ! getConsoleDoc().hasPrompt(); }

  /** Inserts a new line at the caret position. */
  AbstractAction newLineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { 
      ConsoleDocument doc = getConsoleDoc();
      doc.acquireWriteLock();
      try { doc.insertNewline(_pane.getCaretPosition()); }
      finally { doc.releaseWriteLock(); }
    }
  };

  /** Removes all text after the prompt. */
  AbstractAction clearCurrentAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { getConsoleDoc().clearCurrentInput(); }
  };

  /** Goes to the end of the current input line. */
  AbstractAction gotoEndAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { moveToEnd(); }
  };

  /** Selects to the end of the current input line. */
  AbstractAction selectToEndAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { 
      ConsoleDocument doc = getConsoleDoc();
      doc.acquireReadLock();
      try { _pane.moveCaretPosition(doc.getLength()); }
      finally { doc.releaseReadLock(); }
    }
  };

  /** Moves the caret to the prompt. */
  AbstractAction gotoPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { moveToPrompt(); }
  };

  /** Selects to the current prompt. */
  AbstractAction selectToPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      assert EventQueue.isDispatchThread();
      ConsoleDocument doc = getConsoleDoc();
      // Selects the text between the old pos and the prompt
      _pane.moveCaretPosition(doc.getPromptPos());
    }
  };

  /** Moves the pane's caret to the end of the document. Only affects reduced_model not the document model.  */
  void moveToEnd() { 
    assert EventQueue.isDispatchThread();
    int len = getConsoleDoc().getLength();
    _pane.setCaretPosition(len);
//    setCachedCaretPos(len);
  }
  
  /** Moves the pane's caret to the document's prompt. Only affects reduced_model not the document model. */
  void moveToPrompt() { 
    assert EventQueue.isDispatchThread();
    int pos = getConsoleDoc().getPromptPos();
    _pane.setCaretPosition(pos);
  }

  public void setPrevPaneAction(Action a) {
    switchToPrevPaneAction = a;

    // We do this here since switchToPrevPaneAction is set after the constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE),
                                switchToPrevPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_PANE, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE),
                                    switchToPrevPaneAction);
      }
    });
  }

  public void setNextPaneAction(Action a) {
    switchToNextPaneAction = a;

    // We do this here since switchToNextPaneAction is set after the
    // constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE),
                                switchToNextPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_PANE, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE),
                                    switchToNextPaneAction);
      }
    });
  }
}
