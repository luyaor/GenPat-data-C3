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

package edu.rice.cs.drjava.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.beans.*;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.util.swing.FindResult;
import edu.rice.cs.util.swing.ContinueCommand;
import edu.rice.cs.util.UnexpectedException;

/**
 * The dialog box that handles requests for finding and replacing text.
 * @version $Id$
 */
 class FindReplaceDialog extends JDialog {
  private JOptionPane _optionPane;
  private JButton _findNextButton;
  private JButton _replaceButton;
  private JButton _replaceFindButton;
  private JButton _replaceAllButton;
  private JButton _closeButton;
  private JTextField _findField = new JTextField(20);
  private JTextField _replaceField = new JTextField(20);
  private JLabel _message;
  private FindReplaceMachine _machine;
  private SingleDisplayModel _model;
  private MainFrame _frame;
  private DefinitionsPane _defPane = null;
  private boolean _caretChanged;

   /** 
    * Listens for changes to the cursor position in order
    * to reset the start position
    */
  private CaretListener _caretListener = new CaretListener() {
    public void caretUpdate(CaretEvent e) {
      _replaceAction.setEnabled(false);
      _replaceFindAction.setEnabled(false);
      _caretChanged = true;
    }
  };
  
   /**
    * Called from MainFrame upon opening this Dialog or
    * changes in the active document
    */
  public void beginListeningTo(DefinitionsPane defPane) {
    if(_defPane==null) {
      _defPane = defPane;
      _defPane.addCaretListener(_caretListener);
      _caretChanged = true;
      _updateMachine();
      if (!_machine.isOnMatch()) {
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
      }
      else {
        _replaceAction.setEnabled(true);
        _replaceFindAction.setEnabled(true);
      }
      _message.setText("");
    } else {
      throw new UnexpectedException(new RuntimeException("FindReplaceDialog should not be listening to anything"));
    }
    
  }

   /**
    * Called from MainFrame upon closing this Dialog or
    * changes in the active document
    */
  public void stopListening() {
    if(_defPane!=null) {
      _defPane.removeCaretListener(_caretListener);
      _defPane = null;
    } else {
      throw new UnexpectedException(new RuntimeException("FindReplaceDialog should be listening to something"));
    }

  }

  /** How the dialog responds to window events. */
  private WindowListener _dialogListener = new WindowAdapter() {
    public void windowClosing(WindowEvent ev) {
      hide();
    }
  };

  private Action _findNextAction = new AbstractAction("Find Next") {
      public void actionPerformed(ActionEvent e) {
        _doFind();
        _findNextButton.requestFocus();
      }
    };
   
   /**
    * Abstracted out since this is called from find and replace/find
    */
  private void _doFind() {
    _updateMachine();
    _machine.setFindWord(_findField.getText());
    _machine.setReplaceWord(_replaceField.getText());
    _message.setText("");
    
    // FindResult contains the offset to the next occurence of the string
    // and a flag indicating whether the end of the document was wrapped
    // around while searching for the string
    FindResult fr = _machine.findNext();
    if (fr.getWrapped()) {
      Toolkit.getDefaultToolkit().beep();
      _message.setText("Reached the end of the document, continuing from the beginning.");
    }
    int pos = fr.getFoundOffset();
    if (pos >= 0) {
      _selectFoundItem(pos - _machine.getFindWord().length(), pos);
      _replaceAction.setEnabled(true);
      _replaceFindAction.setEnabled(true);
    } 
    // else the entire document was searched and no instance of the string
    // was found
    else if (pos == -1) {
      Toolkit.getDefaultToolkit().beep();
      _message.setText("Search text \"" + _machine.getFindWord() +
                       "\" not found.");
    }
  }

  private Action _replaceAction = new AbstractAction("Replace") {  
    public void actionPerformed(ActionEvent e) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      String replaceWord = _replaceField.getText();
      _machine.setReplaceWord(replaceWord);
      _message.setText("");
      
      // replaces the occurance at the current position
      boolean replaced = _machine.replaceCurrent();
      if (replaced) {
      int pos =  _machine.getCurrentOffset();
      _selectFoundItem(pos-replaceWord.length(),pos);
      }
      _replaceAction.setEnabled(false);
      _replaceFindAction.setEnabled(false);
      _replaceButton.requestFocus();
    }
  };


  private Action _replaceFindAction = new AbstractAction("Replace/Find Next") {
    public void actionPerformed(ActionEvent e) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      String replaceWord = _replaceField.getText();
      _machine.setReplaceWord(replaceWord);
      _message.setText("");
      // replaces the occurance at the current position
      boolean replaced = _machine.replaceCurrent();
      // and finds the next word
      if (replaced) {
        int pos =  _machine.getCurrentOffset();
        _selectFoundItem(pos-replaceWord.length(),pos);
        _doFind();
        _replaceFindButton.requestFocus();
      }
      else {
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
        Toolkit.getDefaultToolkit().beep();
        _message.setText("Replace failed.");
      }
    }
  };

   // Replaces all occurences of the findfield text with that
   // of the replacefield text both before and after the cursor
   // without prompting for wrapping around the end of the
   // document
  private Action _replaceAllAction = new AbstractAction("Replace All") {
    public void actionPerformed(ActionEvent e) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _message.setText("");
      int count = _machine.replaceAll();
      Toolkit.getDefaultToolkit().beep();
      _message.setText("Replaced " + count + " occurrence" + ((count == 1) ? "" :
                                                              "s") + ".");
      _replaceAction.setEnabled(false);
      _replaceFindAction.setEnabled(false);
    }
  };


  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {
      hide();
    }
  };
  
  /**
   * Constructor.
   * @param   Frame frame the overall enclosing window
   * @param   DefinitionsPane defPane the definitions pane which contains the
   * document text being searched over
   */
  public FindReplaceDialog(MainFrame frame, SingleDisplayModel model) {
    super(frame,"Find / Replace", false) ; // not modal.
    _frame = frame;
    _model = model;
    
    addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          hide();
        }
      }
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          hide();
        }
      }
      public void keyTyped(KeyEvent e) {
      }      
    });
    
    addWindowListener(_dialogListener);
    
    setTitle("Find/Replace");

    _findNextButton = new JButton(_findNextAction);
    _replaceButton = new JButton(_replaceAction);
    _replaceFindButton = new JButton(_replaceFindAction);
    _replaceAllButton = new JButton(_replaceAllAction);
    _closeButton = new JButton(_closeAction);
    _message = new JLabel();

    _replaceAction.setEnabled(false);
    _replaceFindAction.setEnabled(false);

    Font font = _findField.getFont().deriveFont(16f);
    _findField.setFont(font);
    _replaceField.setFont(font);

    // set up the layout
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

    buttons.add(Box.createGlue());
    buttons.add(_findNextButton);
    buttons.add(_replaceButton);
    buttons.add(_replaceFindButton);
    buttons.add(_replaceAllButton);
    buttons.add(_closeButton);
    buttons.add(Box.createGlue());

    JLabel findLabel = new JLabel("Find:", SwingConstants.LEFT);
    findLabel.setLabelFor(_findField);
    findLabel.setHorizontalAlignment(SwingConstants.LEFT);

    JLabel replaceLabel = new JLabel("Replace:", SwingConstants.LEFT);
    replaceLabel.setLabelFor(_replaceField);
    replaceLabel.setHorizontalAlignment(SwingConstants.LEFT);

    Container main = getContentPane();
    // arrange everything in 1 column, with all rows being equally high.
    main.setLayout(new GridLayout(0,1)); // .setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

    main.add(findLabel);
    main.add(_findField);
    main.add(replaceLabel);
    main.add(_replaceField);
    main.add(buttons);
    main.add(_message);
    
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    _machine = new FindReplaceMachine();
    
    _findField.addActionListener(_findNextAction);
    
    // DocumentListener that keeps track of changes in the find field.
    _findField.getDocument().addDocumentListener(new DocumentListener() {

      /**
       * If attributes in the find field have changed, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
      }

      /**
       * If text has been inserted into the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
      }

      /**
       * If text has been deleted from the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void removeUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
      }
    });
        
    // let the dialog size itself correctly.
    pack();
    // centers the dialog in the middle of MainFrame
    setLocationRelativeTo(_frame);
  }

   // sets appropriate variables in the FindReplaceMachine if the 
   // caret has been changed
  private void _updateMachine() {
    if (_caretChanged) {
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      _machine.setDocument(doc.getDocument());
      _machine.setStart(doc.getDocument().getCurrentLocation());
      _machine.setPosition(doc.getDocument().getCurrentLocation());
      _caretChanged = false;
    }
  }
  
  /**
   * Shows the dialog and sets the focus appropriately.
   */
  public void show() {
    super.show();
    _frame.installFindReplaceDialog(this);
    _updateMachine();
    _findField.requestFocus();
    _findField.selectAll();
  }
      
   /**
    * Will select the searched-for text.
    * Originally highlighted the text, but we ran into problems
    * with the document remove method changing the view to where
    * the cursor was located, resulting in replace constantly jumping 
    * from the replaced text back to the cursor.
    * There was a removePreviousHighlight method which was removed
    * since selections are removed automatically upon a caret 
    * change. 
    */ 
  private void _selectFoundItem(int from, int to) {
    try {
      _defPane.select(from, to);
      
      JViewport v = _frame.getDefViewport();
      int viewHeight = (int)v.getSize().getHeight();
      // Scroll to make sure this item is visible
      // Centers the selection in the viewport
      Rectangle startRect = _defPane.modelToView(from);
      int startRectY = (int)startRect.getY();
      startRect.setLocation(0, startRectY-viewHeight/2);
      //Rectangle endRect = _defPane.modelToView(to - 1);
      Point endPoint = new Point(0, startRectY+viewHeight/2-1);
      
      // Add the end rect onto the start rect to make a rectangle
      // that encompasses the entire selection
      startRect.add(endPoint);      
      
      _defPane.scrollRectToVisible(startRect);
      _defPane.requestFocus();
      
    } 
    catch (BadLocationException badBadLocation) {}
  }
 
   /*private void _close() {
    hide();
    }*/

  public void hide() {
    
    /*if (_machine.isOnMatch()) {
      _defPane.select(_machine.getCurrentOffset() - 
      _machine.getFindWord().length(),
      _machine.getCurrentOffset());
      }
      else {
      _defPane.setCaretPosition(_machine.getCurrentOffset());
      }*/
    
    //_defPane.requestFocus();
    _frame.uninstallFindReplaceDialog(this);
    super.hide();
  }  
  
   /*private ContinueCommand CONFIRM_CONTINUE = new ContinueCommand() {
    public boolean shouldContinue() {
      String text = "The search has reached the end of the document.\n" +
        "Continue searching from the start?";
      int rc = JOptionPane.showConfirmDialog(FindReplaceDialog.this,
          text,
          "Continue search?",
          JOptionPane.YES_NO_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          return true;
        case JOptionPane.NO_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }

    }
    };*/
  
}

