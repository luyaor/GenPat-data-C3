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

package edu.rice.cs.drjava.config;

import java.io.File;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import edu.rice.cs.drjava.platform.PlatformFactory;

import edu.rice.cs.util.FileOps;


/** Defines the commonly used Option constants in DrJava config and project profiles.
  * @version $Id$
  */
public interface OptionConstants {
  
  // STATIC VARIABLES
  
  /* ---------- Resource Location and Classpath Options ---------- */
  
  /** A file path to a user's preferred browser. */
  public static final FileOption BROWSER_FILE = new FileOption("browser.file", FileOps.NULL_FILE);
  
  /** A String used to launch a user's preferred browser. It is tokenized and appended to the file path. */
  public static final StringOption BROWSER_STRING = new StringOption("browser.string", "");
  
  /** The extension for an old DrJava project file */
  public static final String OLD_PROJECT_FILE_EXTENSION = ".pjt";

  /** The extension for a DrJava project file */
  public static final String PROJECT_FILE_EXTENSION = ".xml";
  
  public static final FileOption JAVAC_LOCATION = new FileOption("javac.location", FileOps.NULL_FILE);
  
  public static final VectorOption<File> EXTRA_CLASSPATH = new ClassPathOption().evaluate("extra.classpath");
  
  public static final VectorOption<String> EXTRA_COMPILERS =
    new VectorOption<String>("extra.compilers", new StringOption("",""), new Vector<String>());
  
  /* ---------- Color Options ---------- */
  
  public static final ColorOption DEFINITIONS_NORMAL_COLOR = new ColorOption("definitions.normal.color", Color.black);
  public static final ColorOption DEFINITIONS_KEYWORD_COLOR = new ColorOption("definitions.keyword.color", Color.blue);
  public static final ColorOption DEFINITIONS_TYPE_COLOR =
    new ColorOption("definitions.type.color", Color.blue.darker().darker());
  public static final ColorOption DEFINITIONS_COMMENT_COLOR =
    new ColorOption("definitions.comment.color", Color.green.darker().darker());
  public static final ColorOption DEFINITIONS_DOUBLE_QUOTED_COLOR =
    new ColorOption("definitions.double.quoted.color", Color.red.darker());
  public static final ColorOption DEFINITIONS_SINGLE_QUOTED_COLOR =
    new ColorOption("definitions.single.quoted.color", Color.magenta);
  public static final ColorOption DEFINITIONS_NUMBER_COLOR =
    new ColorOption("definitions.number.color", Color.cyan.darker());
  public static final ColorOption SYSTEM_OUT_COLOR = new ColorOption("system.out.color", Color.green.darker().darker());
  public static final ColorOption SYSTEM_ERR_COLOR = new ColorOption("system.err.color", Color.red);
  public static final ColorOption SYSTEM_IN_COLOR = new ColorOption("system.in.color", Color.magenta.darker().darker());
  public static final ColorOption INTERACTIONS_ERROR_COLOR =
    new ColorOption("interactions.error.color", Color.red.darker());
  public static final ColorOption DEBUG_MESSAGE_COLOR = new ColorOption("debug.message.color", Color.blue.darker());
  
  /** Color for background of definitions pane. */
  public static final ColorOption DEFINITIONS_BACKGROUND_COLOR =
    new ColorOption("definitions.background.color", Color.white);
  
  /** Color for highlighting brace-matching. */
  public static final ColorOption DEFINITIONS_MATCH_COLOR =
    new ColorOption("definitions.match.color", new Color(190, 255, 230));
  
  /** Color for highlighting errors and test failures. */
  public static final ColorOption COMPILER_ERROR_COLOR = new ColorOption("compiler.error.color", Color.yellow);
  
  /** Color for highlighting bookmarks. */
  public static final ColorOption BOOKMARK_COLOR = new ColorOption("bookmark.color", Color.green);
  
  /** Color for highlighting find results. */
  public static final ColorOption FIND_RESULTS_COLOR1 = 
    new ColorOption("find.results.color1", new Color(0xFF, 0x99, 0x33));
  public static final ColorOption FIND_RESULTS_COLOR2 = 
    new ColorOption("find.results.color2", new Color(0x30, 0xC9, 0x96));
  public static final ColorOption FIND_RESULTS_COLOR3 = 
    new ColorOption("find.results.color3", Color.ORANGE);
  public static final ColorOption FIND_RESULTS_COLOR4 = 
    new ColorOption("find.results.color4", Color.MAGENTA);
  public static final ColorOption FIND_RESULTS_COLOR5 = 
    new ColorOption("find.results.color5", new Color(0xCD, 0x5C, 0x5C));
  public static final ColorOption FIND_RESULTS_COLOR6 = 
    new ColorOption("find.results.color6", Color.DARK_GRAY);
  public static final ColorOption FIND_RESULTS_COLOR7 = 
    new ColorOption("find.results.color7", Color.GREEN);
  public static final ColorOption FIND_RESULTS_COLOR8 = 
    new ColorOption("find.results.color8", Color.BLUE);
  
  public static final ColorOption[] FIND_RESULTS_COLORS = new ColorOption[] {
    FIND_RESULTS_COLOR1,
      FIND_RESULTS_COLOR2,
      FIND_RESULTS_COLOR3,
      FIND_RESULTS_COLOR4,
      FIND_RESULTS_COLOR5,
      FIND_RESULTS_COLOR6,
      FIND_RESULTS_COLOR7,
      FIND_RESULTS_COLOR8
  };
  
  /** Color for highlighting breakpoints. */
  public static final ColorOption DEBUG_BREAKPOINT_COLOR = new ColorOption("debug.breakpoint.color", Color.red);
  
  /** Color for highlighting disabled breakpoints. */
  public static final ColorOption DEBUG_BREAKPOINT_DISABLED_COLOR = 
    new ColorOption("debug.breakpoint.disabled.color", new Color(128,0,0));
  
  /** Color for highlighting thread locations. */
  public static final ColorOption DEBUG_THREAD_COLOR = new ColorOption("debug.thread.color", new Color(100,255,255));
  
  /** Color for the background of the "DrJava Errors" button. */
  public static final ColorOption DRJAVA_ERRORS_BUTTON_COLOR = new ColorOption("drjava.errors.button.color", Color.red);
  
  /* ---------- Font Options ---------- */
  
  /** Main (definitions document, tab contents) */
  public static final FontOption FONT_MAIN = new FontOption("font.main", DefaultFont.getDefaultMainFont());
  
  /** Class that allows the main font to be initialized properly. On Mac OS X, Monaco is the best monospaced font. */
  static class DefaultFont {
    public static Font getDefaultMainFont() {
      if (PlatformFactory.ONLY.isMacPlatform())  return Font.decode("Monaco-12");
      else return Font.decode("Monospaced-12");
    }
    public static Font getDefaultLineNumberFont() {
      if (PlatformFactory.ONLY.isMacPlatform()) return Font.decode("Monaco-12");
      else return Font.decode("Monospaced-12");
    }
    public static Font getDefaultDocListFont() {
      if (PlatformFactory.ONLY.isMacPlatform()) return Font.decode("Monaco-10");
      else return Font.decode("Monospaced-10");
    }
  }
  
  /** Line numbers */
  public static final FontOption FONT_LINE_NUMBERS =
    new FontOption("font.line.numbers", DefaultFont.getDefaultLineNumberFont());
  
  /** List of open documents */
  public static final FontOption FONT_DOCLIST = new FontOption("font.doclist", DefaultFont.getDefaultDocListFont());
  
  /** Toolbar buttons */
  public static final FontOption FONT_TOOLBAR = new FontOption("font.toolbar", Font.decode("dialog-10"));
  
  /** Whether to draw anti-aliased text.  (Slightly slower.) */
  public static final BooleanOption TEXT_ANTIALIAS = new BooleanOption("text.antialias", Boolean.TRUE);
  
  
  /* ---------- Other Display Options ---------- */
  
  /** Whether icons should be displayed on the toolbar buttons. */
  public static final BooleanOption TOOLBAR_ICONS_ENABLED =
    new BooleanOption("toolbar.icons.enabled", Boolean.TRUE);
  
  /** Whether text should be displayed on toolbar buttons. Note: only relevant if toolbar icons are enabled. */
  public static final BooleanOption TOOLBAR_TEXT_ENABLED = new BooleanOption("toolbar.text.enabled", Boolean.TRUE);
  
  /** Whether or not the toolbar should be displayed. */
  public static final BooleanOption TOOLBAR_ENABLED = new BooleanOption("toolbar.enabled", Boolean.TRUE);
  
  /** Whether the line-numbers should be displayed in a row header. */
  public static final BooleanOption LINEENUM_ENABLED = new BooleanOption("lineenum.enabled", Boolean.FALSE);
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption WINDOW_STORE_POSITION = new BooleanOption("window.store.position", Boolean.TRUE);
  
  /** Whether a sample of the source code will be show when fast switching documents. */
  public static final BooleanOption SHOW_SOURCE_WHEN_SWITCHING = 
    new BooleanOption("show.source.for.fast.switch", Boolean.TRUE);
  
  /** The current look and feel. */
  public static final ForcedChoiceOption LOOK_AND_FEEL =
    new ForcedChoiceOption("look.and.feel", LookAndFeels.getDefaultLookAndFeel(), LookAndFeels.getLookAndFeels());
  
  /** Class that allows the look and feels to be initialized properly. */
  static class LookAndFeels {
    
    /** Return the look-and-feel to use by default */
    public static String getDefaultLookAndFeel() {
      if (PlatformFactory.ONLY.isMacPlatform())
        return UIManager.getSystemLookAndFeelClassName();
      else
        return UIManager.getCrossPlatformLookAndFeelClassName();
    }
    
    /** Need to ensure that a look-and-feel can be instantiated and is valid.
      * TODO:  store the LookAndFeel object rather than its classname.  This would be much nicer, as we could display a 
      * useful name, and wouldn't have to reinstantiate it when it's installed.
      * @return the list of available look-and-feel classnames
      */
    public static ArrayList<String> getLookAndFeels() {
      ArrayList<String> lookAndFeels = new ArrayList<String>();
      LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();
      if (lafis != null) {
        for (int i = 0; i < lafis.length; i++) {
          try {
            String currName = lafis[i].getClassName();
            LookAndFeel currLAF = (LookAndFeel) Class.forName(currName).newInstance();
            if (currLAF.isSupportedLookAndFeel()) lookAndFeels.add(currName);
          }
          catch (Exception ex) {
            // failed to load/instantiate class, or it is not supported.
            // It is not a valid choice.
          }
        }
      }
      return lookAndFeels;
    }
  }
  
  /* ---------- Key Binding Options ----------- */
  public static int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  
  /**
   * The key binding for creating a new file
   */
  public static final KeyStrokeOption KEY_NEW_FILE =
    new KeyStrokeOption("key.new.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_N, MASK));
  /**
   * The key binding for opening an entire project.  I is right next to O, so
   * it seemed logical that ctrl-I would open a project and ctrl-O open a file
   */
  public static final KeyStrokeOption KEY_OPEN_PROJECT =
    new KeyStrokeOption("key.open.project",
                        KeyStroke.getKeyStroke(KeyEvent.VK_I, MASK));
  /**
   * The key binding for creating a new JUnit test case
   */
  public static final KeyStrokeOption KEY_NEW_TEST =
    new KeyStrokeOption("key.new.test",
                        KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for opening a folder
   */
  public static final KeyStrokeOption KEY_OPEN_FOLDER =
    new KeyStrokeOption("key.open.folder",
                        KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK|InputEvent.SHIFT_MASK));
  /**
   * The key binding for opening a file
   */
  public static final KeyStrokeOption KEY_OPEN_FILE =
    new KeyStrokeOption("key.open.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK));
  /**
   * The key binding for saving a file
   */
  public static final KeyStrokeOption KEY_SAVE_FILE =
    new KeyStrokeOption("key.save.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
  /**
   * The key binding for saving a file as
   */
  public static final KeyStrokeOption KEY_SAVE_FILE_AS =
    new KeyStrokeOption("key.save.file.as",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for saving all files
   */
  public static final KeyStrokeOption KEY_SAVE_ALL_FILES =
    new KeyStrokeOption("key.save.all.files",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK |
                                               InputEvent.ALT_MASK));
    /** The key binding for exporting in the old project file format */
  public static final KeyStrokeOption KEY_EXPORT_OLD =
    new KeyStrokeOption("key.export.old", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for renaming a file
   */
  public static final KeyStrokeOption KEY_RENAME_FILE = 
    new KeyStrokeOption("key.rename.file", KeyStroke.getKeyStroke(KeyEvent.VK_R, MASK));
  
  /**
   * The key binding for reverting a file
   */
  public static final KeyStrokeOption KEY_REVERT_FILE =
    new KeyStrokeOption("key.revert.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_R, MASK|InputEvent.SHIFT_MASK));
  /**
   * The key binding for closing a file
   */
  public static final KeyStrokeOption KEY_CLOSE_FILE =
    new KeyStrokeOption("key.close.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK));
  /**
   * The key binding for closing all files
   */
  public static final KeyStrokeOption KEY_CLOSE_ALL_FILES =
    new KeyStrokeOption("key.close.all.files",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK |
                                               InputEvent.ALT_MASK));
  
  public static final KeyStrokeOption KEY_CLOSE_PROJECT =
    new KeyStrokeOption("key.close.project",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for showing the print preview */
  public static final KeyStrokeOption KEY_PAGE_SETUP =
    new KeyStrokeOption("key.page.setup", KeyStrokeOption.NULL_KEYSTROKE);
  
  /** The key binding for showing the print preview. */
  public static final KeyStrokeOption KEY_PRINT_PREVIEW =
    new KeyStrokeOption("key.print.preview", KeyStroke.getKeyStroke(KeyEvent.VK_P, MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for printing a file */
  public static final KeyStrokeOption KEY_PRINT =
    new KeyStrokeOption("key.print", KeyStroke.getKeyStroke(KeyEvent.VK_P, MASK));
  
  /** The key binding for quitting */
  public static final KeyStrokeOption KEY_QUIT =
    new KeyStrokeOption("key.quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, MASK));
  
  /** The key binding for forced quitting */
  public static final KeyStrokeOption KEY_FORCE_QUIT =
    new KeyStrokeOption("key.force.quit", KeyStrokeOption.NULL_KEYSTROKE);
  
  /** The key binding for undo-ing */
  public static final KeyStrokeOption KEY_UNDO =
    new KeyStrokeOption("key.undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK));
  
  /** The key binding for redo-ing */
  public static final KeyStrokeOption KEY_REDO =
    new KeyStrokeOption("key.redo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for cutting */
  public static final KeyStrokeOption KEY_CUT =
    new KeyStrokeOption("key.cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, MASK));
  
  /** The key binding for copying */
  public static final KeyStrokeOption KEY_COPY =
    new KeyStrokeOption("key.copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, MASK));
  
  /** The key binding for pasting */
  public static final KeyStrokeOption KEY_PASTE =
    new KeyStrokeOption("key.paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, MASK));
  
  /** The key binding for pasting from history */
  public static final KeyStrokeOption KEY_PASTE_FROM_HISTORY =
    new KeyStrokeOption("key.paste.from.history", KeyStroke.getKeyStroke(KeyEvent.VK_V , MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for selecting all text */
  public static final KeyStrokeOption KEY_SELECT_ALL =
    new KeyStrokeOption("key.select.all", KeyStroke.getKeyStroke(KeyEvent.VK_A, MASK));
  
  /** The key binding for find and replace */
  public static final KeyStrokeOption KEY_FIND_NEXT =
    new KeyStrokeOption("key.find.next", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
  
  /** The key binding for find previous (opposite direction) */
  public static final KeyStrokeOption KEY_FIND_PREV =
    new KeyStrokeOption("key.find.prev",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F3,  InputEvent.SHIFT_MASK));
  /**
   * The key binding for find and replace
   */
  public static final KeyStrokeOption KEY_FIND_REPLACE =
    new KeyStrokeOption("key.find.replace",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, MASK));
  /**
   * The key binding for goto line
   */
  public static final KeyStrokeOption KEY_GOTO_LINE =
    new KeyStrokeOption("key.goto.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
  
  /**
   * The key binding for goto file.
   */
  public static final KeyStrokeOption KEY_GOTO_FILE =
    new KeyStrokeOption("key.goto.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK|KeyEvent.SHIFT_MASK));
  
  /**
   * The key binding for goto this file.
   */
  public static final KeyStrokeOption KEY_GOTO_FILE_UNDER_CURSOR =
    new KeyStrokeOption("key.goto.file.under.cursor",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
  
  /**
   * The key binding for open Javadoc.
   */
  public static final KeyStrokeOption KEY_OPEN_JAVADOC =
    new KeyStrokeOption("key.open.javadoc",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK));
  
  /**
   * The key binding for open Javadoc under cursor.
   */
  public static final KeyStrokeOption KEY_OPEN_JAVADOC_UNDER_CURSOR =
    new KeyStrokeOption("key.open.javadoc.under.cursor",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F6, MASK));
  
  /**
   * The key binding for complete file.
   */
  public static final KeyStrokeOption KEY_COMPLETE_FILE =
    new KeyStrokeOption("key.complete.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, MASK|KeyEvent.SHIFT_MASK));
  
  /**
   * The key binding for indenting
   *
   public static final KeyStrokeOption KEY_INDENT =
   new KeyStrokeOption("key.indent",
   KeyStroke.getKeyStroke(KeyEvent.VK_TAB, MASK)); */
  
  /**
   * The key binding for commenting out lines
   */
  public static final KeyStrokeOption KEY_COMMENT_LINES =
    new KeyStrokeOption("key.comment.lines",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, MASK));
  
  /**
   * The key binding for un-commenting lines
   */
  public static final KeyStrokeOption KEY_UNCOMMENT_LINES =
    new KeyStrokeOption("key.uncomment.lines",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                                               (MASK | InputEvent.SHIFT_MASK)));
  
  /**
   * The key binding for selecting previous document
   */
  public static final KeyStrokeOption KEY_PREVIOUS_DOCUMENT =
    new KeyStrokeOption("key.previous.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, MASK));
  /**
   * The key binding for selecting next document
   */
  public static final KeyStrokeOption KEY_NEXT_DOCUMENT =
    new KeyStrokeOption("key.next.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, MASK));
  
  /**
   * The key binding for changing the focus to the previous pane
   */
  public static final KeyStrokeOption KEY_PREVIOUS_PANE =
    new KeyStrokeOption("key.previous.pane",
                        KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MASK));
  
  /**
   * The key binding for changing the focus to the next pane
   */
  public static final KeyStrokeOption KEY_NEXT_PANE =
    new KeyStrokeOption("key.next.pane",
                        KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MASK));
  
  /**
   * The key binding for going to the opening brace.
   */
  public static final KeyStrokeOption KEY_OPENING_BRACE =
    new KeyStrokeOption("key.goto.opening.brace",
                        KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MASK|InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for going to the closing brace.
   */
  public static final KeyStrokeOption KEY_CLOSING_BRACE =
    new KeyStrokeOption("key.goto.closing.brace",
                        KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MASK|InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for jumping to the next location in the browser history
   */
  public static final KeyStrokeOption KEY_BROWSE_FORWARD =
    new KeyStrokeOption("key.browse.forward",
                        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK|InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for jumping to the previous location in the browser history
   */
  public static final KeyStrokeOption KEY_BROWSE_BACK =
    new KeyStrokeOption("key.browse.back",
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK|InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for openning the preferences dialog
   */
  public static final KeyStrokeOption KEY_PREFERENCES =
    new KeyStrokeOption("key.preferences",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, MASK));
  
  /**
   * The key binding for compiling current document
   */
  public static final KeyStrokeOption KEY_COMPILE =
    new KeyStrokeOption("key.compile", KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for compiling all
   */
  public static final KeyStrokeOption KEY_COMPILE_ALL =
    new KeyStrokeOption("key.compile.all", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
  
  /**
   * The key binding for running the main method of the current document
   */
  public static final KeyStrokeOption KEY_RUN =
    new KeyStrokeOption("key.run", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
  
  /**
   * The key binding for running the project's main document
   */
  public static final KeyStrokeOption KEY_RUN_MAIN =
    new KeyStrokeOption("key.run.main", KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
  
  /**
   * The key binding for testing the current document
   */
  public static final KeyStrokeOption KEY_TEST =
    new KeyStrokeOption("key.test",
                        KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK | InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for testing all open JUnit test cases.
   */
  public static final KeyStrokeOption KEY_TEST_ALL =
    new KeyStrokeOption("key.test.all", KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK));
  
  /**
   * The key binding for generating javadoc for all documents
   */
  public static final KeyStrokeOption KEY_JAVADOC_ALL =
    new KeyStrokeOption("key.javadoc.all", KeyStroke.getKeyStroke(KeyEvent.VK_J, MASK));
  
  /**
   * The key binding for generating javadoc for the current document
   */
  public static final KeyStrokeOption KEY_JAVADOC_CURRENT =
    new KeyStrokeOption("key.javadoc.current",
                        KeyStroke.getKeyStroke(KeyEvent.VK_J, MASK | InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for executing an interactions history.
   */
  public static final KeyStrokeOption KEY_EXECUTE_HISTORY =
    new KeyStrokeOption("key.execute.history", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for loading an interactions history as a script.
   */
  public static final KeyStrokeOption KEY_LOAD_HISTORY_SCRIPT =
    new KeyStrokeOption("key.load.history.script", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for saving an interactions history.
   */
  public static final KeyStrokeOption KEY_SAVE_HISTORY =
    new KeyStrokeOption("key.save.history", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for clearing the interactions history.
   */
  public static final KeyStrokeOption KEY_CLEAR_HISTORY =
    new KeyStrokeOption("key.clear.history", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for resetting the interactions pane.
   */
  public static final KeyStrokeOption KEY_RESET_INTERACTIONS =
    new KeyStrokeOption("key.reset.interactions", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for viewing the interactions classpath.
   */
  public static final KeyStrokeOption KEY_VIEW_INTERACTIONS_CLASSPATH =
    new KeyStrokeOption("key.view.interactions.classpath", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for printing the interactions.
   */
  public static final KeyStrokeOption KEY_PRINT_INTERACTIONS =
    new KeyStrokeOption("key.view.print.interactions", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for lifting the current interaction to definitions.
   */
  public static final KeyStrokeOption KEY_LIFT_CURRENT_INTERACTION =
    new KeyStrokeOption("key.lift.current.interaction", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding to enter or leave multiline input mode.
   *
   public static final KeyStrokeOption KEY_TOGGLE_MULTILINE_INTERACTION =
   new KeyStrokeOption("key.toggle.multiline.interaction",
   KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK));
   */
  
  /**
   * The key binding for clearing the console.
   */
  public static final KeyStrokeOption KEY_CLEAR_CONSOLE =
    new KeyStrokeOption("key.clear.console", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for printing the console.
   */
  public static final KeyStrokeOption KEY_PRINT_CONSOLE =
    new KeyStrokeOption("key.view.print.console", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for moving the cursor backwards
   */
  public static final KeyStrokeOption KEY_BACKWARD =
    new KeyStrokeOption("key.backward", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
  
  /**
   * The key binding for moving the cursor to the beginning of the document
   */
  public static final KeyStrokeOption KEY_BEGIN_DOCUMENT =
    new KeyStrokeOption("key.begin.document", KeyStroke.getKeyStroke(KeyEvent.VK_HOME, MASK));
  
  /**
   * The key binding for moving the cursor to the beginning of the current line
   */
  public static final KeyStrokeOption KEY_BEGIN_LINE =
    new KeyStrokeOption("key.begin.line", KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
  
  /**
   * The key binding for moving the cursor to the beginning of the current paragraph.
   * (Doesn't seem to do anything useful...)
   *
   public static final KeyStrokeOption KEY_BEGIN_PARAGRAPH =
   new KeyStrokeOption("key.begin.paragraph",
   KeyStroke.getKeyStroke(KeyEvent.VK_UP, MASK));
   */
  
  /**
   * The key binding for moving the cursor to the beginning of the previous word
   */
  public static final KeyStrokeOption KEY_PREVIOUS_WORD =
    new KeyStrokeOption("key.previous.word", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MASK));
  
  /**
   * The key binding for deleting the next character
   */
  public static final KeyStrokeOption KEY_DELETE_NEXT =
    new KeyStrokeOption("key.delete.next",
                        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
  /**
   * The key binding for deleting the previous character (with shift set)
   */
  public static final KeyStrokeOption KEY_DELETE_PREVIOUS =
    new KeyStrokeOption("key.delete.previous", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
  
  /**
   * The key binding for deleting the next character (with shift set)
   */
  public static final KeyStrokeOption KEY_SHIFT_DELETE_NEXT =
    new KeyStrokeOption("key.delete.next",
                        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK));
  /**
   * The key binding for deleting the previous character (with shift set)
   */
  public static final KeyStrokeOption KEY_SHIFT_DELETE_PREVIOUS =
    new KeyStrokeOption("key.delete.previous", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for moving the cursor down
   */
  public static final KeyStrokeOption KEY_DOWN =
    new KeyStrokeOption("key.down", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
  
  /**
   * The key binding for moving the cursor up
   */
  public static final KeyStrokeOption KEY_UP =
    new KeyStrokeOption("key.up", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
  
  /**
   * The key binding for moving the cursor to the end of the document
   */
  public static final KeyStrokeOption KEY_END_DOCUMENT =
    new KeyStrokeOption("key.end.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, MASK));
  /**
   * The key binding for moving the cursor to the end of the current line
   */
  public static final KeyStrokeOption KEY_END_LINE =
    new KeyStrokeOption("key.end.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
  
//  /** The key binding for moving the cursor to the end of the current paragraph. */
//  public static final KeyStrokeOption KEY_END_PARAGRAPH =
//    new KeyStrokeOption("key.end.paragraph", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, MASK));
  
  /** The key binding for moving the cursor to the beginning of the next word. */
  public static final KeyStrokeOption KEY_NEXT_WORD =
    new KeyStrokeOption("key.next.word", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MASK));
  
  /** The key binding for moving the cursor forwards. */
  public static final KeyStrokeOption KEY_FORWARD =
    new KeyStrokeOption("key.forward", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
  
  /** The key binding for page down. */
  public static final KeyStrokeOption KEY_PAGE_DOWN =
    new KeyStrokeOption("key.page.down", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
  
  /** The key binding for page up. */
  public static final KeyStrokeOption KEY_PAGE_UP =
    new KeyStrokeOption("key.page.up", KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
  
  /** The key binding for cutting a line. */
  public static final KeyStrokeOption KEY_CUT_LINE =
    new KeyStrokeOption("key.cut.line", KeyStroke.getKeyStroke(KeyEvent.VK_K, (MASK | InputEvent.ALT_MASK)));
  
  /** The key binding for clearing a line, emacs-style. */
  public static final KeyStrokeOption KEY_CLEAR_LINE =
    new KeyStrokeOption("key.clear.line", KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
  
  /** The key binding for toggling debug mode. */
  public static final KeyStrokeOption KEY_DEBUG_MODE_TOGGLE =
    new KeyStrokeOption("key.debug.mode.toggle", KeyStroke.getKeyStroke(KeyEvent.VK_D, MASK | InputEvent.SHIFT_MASK));
  
//  /** The key binding for suspending the debugger. */
//  public static final KeyStrokeOption KEY_DEBUG_SUSPEND =
//    new KeyStrokeOption("key.debug.suspend", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
  
  /** The key binding for resuming the debugger. */
  public static final KeyStrokeOption KEY_DEBUG_RESUME =
    new KeyStrokeOption("key.debug.resume", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
  
  /** The key binding for stepping into in the debugger */
  public static final KeyStrokeOption KEY_DEBUG_STEP_INTO =
    new KeyStrokeOption("key.debug.step.into", KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
  
  /** The key binding for stepping over in the debugger. */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OVER =
    new KeyStrokeOption("key.debug.step.over", KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
  
  /** The key binding for stepping out in the debugger. */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OUT =
    new KeyStrokeOption("key.debug.step.out", KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_MASK));
  
  /** The key binding for toggling a breakpoint. */
  public static final KeyStrokeOption KEY_DEBUG_BREAKPOINT_TOGGLE =
    new KeyStrokeOption("key.debug.breakpoint.toggle", KeyStroke.getKeyStroke(KeyEvent.VK_B, MASK));
  
  /** The key binding for displaying the breakpoints panel. */
  public static final KeyStrokeOption KEY_DEBUG_BREAKPOINT_PANEL =
    new KeyStrokeOption("key.debug.breakpoint.panel", KeyStroke.getKeyStroke(KeyEvent.VK_B, MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for clearing all breakpoints. */
  public static final KeyStrokeOption KEY_DEBUG_CLEAR_ALL_BREAKPOINTS =
    new KeyStrokeOption("key.debug.clear.all.breakpoints", KeyStrokeOption.NULL_KEYSTROKE);
  
  /** The key binding for toggling a bookmark. */
  public static final KeyStrokeOption KEY_BOOKMARKS_TOGGLE =
    new KeyStrokeOption("key.bookmarks.toggle", KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK));
  
  /** The key binding for displaying the bookmarks panel. */
  public static final KeyStrokeOption KEY_BOOKMARKS_PANEL =
    new KeyStrokeOption("key.bookmarks.panel", KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK | InputEvent.SHIFT_MASK));
  
  /** The key binding for help */
  public static final KeyStrokeOption KEY_HELP =
    new KeyStrokeOption("key.help", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
  
  /** The key binding for quickstart. Currently set to the null keystroke. */
  public static final KeyStrokeOption KEY_QUICKSTART = 
    new KeyStrokeOption("key.quickstart", KeyStrokeOption.NULL_KEYSTROKE);
  
  /** The key binding for the about dialog */
  public static final KeyStrokeOption KEY_ABOUT = 
    new KeyStrokeOption("key.about", KeyStrokeOption.NULL_KEYSTROKE);
  
  /** The key binding for the "DrJava Errors" dialog */
  public static final KeyStrokeOption KEY_DRJAVA_ERRORS = 
    new KeyStrokeOption("key.drjava.errors", KeyStrokeOption.NULL_KEYSTROKE);
  
  /**
   * The key binding for following a file, like using "less" and F.
   */
  public static final KeyStrokeOption KEY_FOLLOW_FILE =
    new KeyStrokeOption("key.follow.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK | InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for executing an external process.
   */
  public static final KeyStrokeOption KEY_EXEC_PROCESS =
    new KeyStrokeOption("key.exec.process",
                        KeyStroke.getKeyStroke(KeyEvent.VK_X, MASK | InputEvent.SHIFT_MASK));
  
  /* ---------- Find Replace Options ---------- */
  
  public static final BooleanOption FIND_MATCH_CASE = 
    new BooleanOption("find.replace.match.case", Boolean.TRUE);
  
  public static final BooleanOption FIND_SEARCH_BACKWARDS = 
    new BooleanOption("find.replace.search.backwards", Boolean.FALSE);
  
  public static final BooleanOption FIND_WHOLE_WORD = 
    new BooleanOption("find.replace.whole.word", Boolean.FALSE);
  
  public static final BooleanOption FIND_ALL_DOCUMENTS = 
    new BooleanOption("find.replace.all.documents", Boolean.FALSE);
  
  public static final BooleanOption FIND_NO_COMMENTS_STRINGS =
    new BooleanOption("find.replace.no.comments.strings", Boolean.FALSE);
  
  public static final BooleanOption FIND_NO_TEST_CASES =
    new BooleanOption("find.replace.no.test.cases", Boolean.FALSE);
  
  /* ---------- Debugger Options ---------- */
  
  /**
   * A classpath-structured vector of all paths to look for source files on
   * while stepping in the debugger.
   */
  public static final VectorOption<File> DEBUG_SOURCEPATH =
    new ClassPathOption().evaluate("debug.sourcepath");
  
  /**
   * Whether stepping should step through Java's source files
   */
  public static final BooleanOption DEBUG_STEP_JAVA =
    new BooleanOption("debug.step.java", Boolean.FALSE);
  
  /**
   * Whether stepping should step through Dynamic Java's source files
   */
  public static final BooleanOption DEBUG_STEP_INTERPRETER =
    new BooleanOption("debug.step.interpreter", Boolean.FALSE);
  
  /**
   * Whether stepping should step through DrJava's source files
   */
  public static final BooleanOption DEBUG_STEP_DRJAVA =
    new BooleanOption("debug.step.drjava", Boolean.FALSE);
  
  /**
   * Which packages to exclude when stepping.
   */
  public static final StringOption DEBUG_STEP_EXCLUDE =
    new StringOption("debug.step.exclude", "");
  
  
  
  /* ---------- Javadoc Options ---------- */
  
  /**
   * Possible options for Javadoc access levels.
   */
  static final ArrayList<String> accessLevelChoices =
    AccessLevelChoices.evaluate();
  static class AccessLevelChoices {
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add("public");
      aList.add("protected");
      aList.add("package");
      aList.add("private");
      return aList;
    }
  }
  
  /** The lowest access level of classes and members to include in the javadoc. */
  public static final ForcedChoiceOption JAVADOC_ACCESS_LEVEL =
    new ForcedChoiceOption("javadoc.access.level", "package", accessLevelChoices);
  
  /** Possible options for Javadoc system class documentation links. */
  static final String JAVADOC_NONE_TEXT = "none";
  static final String JAVADOC_1_3_TEXT = "1.3";
  static final String JAVADOC_1_4_TEXT = "1.4";
  static final String JAVADOC_1_5_TEXT = "1.5";
  
  static final String[] choices = new String[]{JAVADOC_NONE_TEXT, JAVADOC_1_3_TEXT, JAVADOC_1_4_TEXT, JAVADOC_1_5_TEXT};
  
  static final ArrayList<String> linkVersionChoices = new ArrayList<String>(Arrays.asList(choices));
  
  /** Constants for the URLs of Sun's system class documentation for different versions of Java. */
  public static final StringOption JAVADOC_1_3_LINK =
    new StringOption("javadoc.1.3.link", "http://java.sun.com/j2se/1.3/docs/api");
  public static final StringOption JAVADOC_1_4_LINK =
    new StringOption("javadoc.1.4.link", "http://java.sun.com/j2se/1.4/docs/api");
  public static final StringOption JAVADOC_1_5_LINK =
    new StringOption("javadoc.1.5.link", "http://java.sun.com/j2se/1.5/docs/api");
  
  /** The version of Java to use for links to Javadoc for system classes. */
  public static final ForcedChoiceOption JAVADOC_LINK_VERSION =
    new ForcedChoiceOption("javadoc.link.version",
                           (System.getProperty("java.specification.version").equals("1.3") ? JAVADOC_1_3_TEXT :
                              (System.getProperty("java.specification.version").equals("1.4") ? JAVADOC_1_4_TEXT : 
                                 JAVADOC_1_5_TEXT)),
                           linkVersionChoices);
  
  /** Whether to include the entire package heirarchy from the source roots when generating JavaDoc output. */
  public static final BooleanOption JAVADOC_FROM_ROOTS = new BooleanOption("javadoc.from.roots", Boolean.FALSE);
  
  /** A string containing custom options to be passed to Javadoc. This string needs to be tokenized before passing it to 
    * Javadoc.
    */
  public static final StringOption JAVADOC_CUSTOM_PARAMS = 
    new StringOption("javadoc.custom.params", "-author -version");
  
  /** The default destination directory for Javadoc output. */
  public static final FileOption JAVADOC_DESTINATION = new FileOption("javadoc.destination", FileOps.NULL_FILE);
  
  /** Whether to always prompt for a destination directory, whether or not a default has been set. */
  public static final BooleanOption JAVADOC_PROMPT_FOR_DESTINATION =
    new BooleanOption("javadoc.prompt.for.destination", Boolean.TRUE);
  
  /* ---------- NOTIFICATION OPTIONS ---------- */
  
  /** Whether to prompt when the interactions pane is unexpectedly reset. */
  public static final BooleanOption INTERACTIONS_EXIT_PROMPT =
    new BooleanOption("interactions.exit.prompt", Boolean.TRUE);
  
  /** Whether to prompt before quitting DrJava. */
  public static final BooleanOption QUIT_PROMPT = new BooleanOption("quit.prompt", Boolean.TRUE);
  
  /** Whether to prompt before resetting the interactions pane. */
  public static final BooleanOption INTERACTIONS_RESET_PROMPT =
    new BooleanOption("interactions.reset.prompt", Boolean.TRUE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_COMPILE =
    new BooleanOption("save.before.compile", Boolean.FALSE);
  
  /** Whether to prompt to save before running. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_RUN =
    new BooleanOption("save.before.run", Boolean.FALSE);
  
  /** Whether to prompt to save before testing. */
  public static final BooleanOption ALWAYS_COMPILE_BEFORE_JUNIT =
    new BooleanOption("compile.before.junit", Boolean.FALSE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_JAVADOC =
    new BooleanOption("save.before.javadoc", Boolean.FALSE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_DEBUG =
    new BooleanOption("save.before.debug", Boolean.FALSE);
  
  /** Whether to warn if a document has been modified before allowing the user to set a breakpoint in it. */
  public static final BooleanOption WARN_BREAKPOINT_OUT_OF_SYNC =
    new BooleanOption("warn.breakpoint.out.of.sync", Boolean.TRUE);
  
  /** Whether to warn that the user is debugging a file that is out of sync with its class file. */
  public static final BooleanOption WARN_DEBUG_MODIFIED_FILE =
    new BooleanOption("warn.debug.modified.file", Boolean.TRUE);
  
  /** Whether to warn that a restart is necessary before the look and feel will change. */
  public static final BooleanOption WARN_CHANGE_LAF = new BooleanOption("warn.change.laf", Boolean.TRUE);
  
  /** Whether to warn that a file's path contains a "#' symbol. */
  public static final BooleanOption WARN_PATH_CONTAINS_POUND =
    new BooleanOption("warn.path.contains.pound", Boolean.TRUE);
  
  /* ---------- MISC OPTIONS ---------- */
  
  /** Whether to warn when cleaning the build directory */
  public static final BooleanOption PROMPT_BEFORE_CLEAN = new BooleanOption("prompt.before.clean", Boolean.TRUE);
  
  /** Open directory should default to recursive */
  public static final BooleanOption OPEN_FOLDER_RECURSIVE =  new BooleanOption("open.folder.recursive", Boolean.FALSE);
  
  /** How many spaces to use for indenting. */
  public static final NonNegativeIntegerOption INDENT_LEVEL = 
    new NonNegativeIntegerOption("indent.level",new Integer(2));
  
  /** Number of lines to remember in the Interactions History */
  public static final NonNegativeIntegerOption HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("history.max.size", new Integer(500));
  
  /** Number of files to list in the recent file list */
  public static final NonNegativeIntegerOption RECENT_FILES_MAX_SIZE =
    new NonNegativeIntegerOption("recent.files.max.size", new Integer(5));
  
  /** Whether to automatically close comments. */
  public static final BooleanOption AUTO_CLOSE_COMMENTS =
    new BooleanOption("auto.close.comments", Boolean.FALSE);
  
  /** Whether to clear the console when manually resetting the interactions pane. */
  public static final BooleanOption RESET_CLEAR_CONSOLE =
    new BooleanOption("reset.clear.console", Boolean.TRUE);
  
  /** Whether to run assert statements in the interactions pane. */
  public static final BooleanOption RUN_WITH_ASSERT =
    new BooleanOption("run.with.assert", Boolean.TRUE);
  
  /** Whether to make emacs-style backup files. */
  public static final BooleanOption BACKUP_FILES = new BooleanOption("files.backup", Boolean.TRUE);
  
  /** Whether to allow users to access to all members in the Interactions Pane. */
  public static final BooleanOption ALLOW_PRIVATE_ACCESS = new BooleanOption("allow.private.access", Boolean.FALSE);
  
  /** Whether to force test classes in projects to end in "Test". */
  public static final BooleanOption FORCE_TEST_SUFFIX = new BooleanOption("force.test.suffix", Boolean.FALSE);
  
  /** Whether remote control using sockets is enabled. */
  public static final BooleanOption REMOTE_CONTROL_ENABLED = new BooleanOption("remote.control.enabled", Boolean.TRUE);
  
  /** The port where DrJava will listen for remote control requests. */
  public static final IntegerOption REMOTE_CONTROL_PORT = new IntegerOption("remote.control.port", new Integer(4444));
  
  /* ---------- COMPILER OPTIONS ------------- */
  
  /** Whether to show unchecked warnings */
  public static final BooleanOption SHOW_UNCHECKED_WARNINGS = 
    new BooleanOption("show.unchecked.warnings", Boolean.TRUE);
  
  /** Whether to show deprecation warnings */
  public static final BooleanOption SHOW_DEPRECATION_WARNINGS = 
    new BooleanOption("show.deprecation.warnings", Boolean.TRUE);
  
  /** Whether to show finally warnings */
  public static final BooleanOption SHOW_FINALLY_WARNINGS = new BooleanOption("show.finally.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_SERIAL_WARNINGS = 
    new BooleanOption("show.serial.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_FALLTHROUGH_WARNINGS = 
    new BooleanOption("show.fallthrough.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_PATH_WARNINGS = 
    new BooleanOption("show.path.warnings", Boolean.FALSE);
  
  /* ---------- UNDISPLAYED OPTIONS ---------- */
  
  /** The language level to use when starting DrJava.  Stores the most recently used one.  Defaults to full java. */
  public static final IntegerOption LANGUAGE_LEVEL = new IntegerOption("language.level", new Integer(0));
  
  /** A vector containing the most recently used files. */
  public static final VectorOption<File> RECENT_FILES =
    new VectorOption<File>("recent.files",new FileOption("",null),new Vector<File>());
  
  /** A vector containing the most recently used projects. */
  public static final VectorOption<File> RECENT_PROJECTS =
    new VectorOption<File>("recent.projects",new FileOption("",null),new Vector<File>());
  
  /** Whether to enabled the Show Debug Console menu item in the Tools menu. */
  public static final BooleanOption SHOW_DEBUG_CONSOLE = new BooleanOption("show.debug.console", Boolean.FALSE);
  
  /** Height of MainFrame at startUp.  Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption WINDOW_HEIGHT =
    new NonNegativeIntegerOption("window.height",new Integer(700));
  
  /** Width of MainFrame at startUp.  Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption WINDOW_WIDTH =
    new NonNegativeIntegerOption("window.width",new Integer(800));
  
  /** X position of MainFrame at startUp.  Can be overridden if out of bounds. This value can legally be negative in a
    * multi-screen setup.
    */
  public static final IntegerOption WINDOW_X = new IntegerOption("window.x", new Integer(Integer.MAX_VALUE));
  
  /** Y position of MainFrame at startUp.  Can be overridden if out of bounds. This value can legally be negative in a
    * multi-screen setup.
    */
  public static final IntegerOption WINDOW_Y = new IntegerOption("window.y", new Integer(Integer.MAX_VALUE));
  
  /** The window state (maxamized or normal). The current window state
    * is saved on shutdown.
    */
  public static final IntegerOption WINDOW_STATE =
    new IntegerOption("window.state", new Integer(Frame.NORMAL));
  
  /** Width of DocList at startUp.  Must be less than WINDOW_WIDTH. Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption DOC_LIST_WIDTH =
    new NonNegativeIntegerOption("doc.list.width",new Integer(150));
  
  /** Height of tabbed panel at startUp.  Must be less than WINDOW_HEIGHT + DEBUG_PANEL_HEIGHT.  Can be overridden if 
    * out of bounds.
    */
  public static final NonNegativeIntegerOption TABS_HEIGHT =
    new NonNegativeIntegerOption("tabs.height",new Integer(120));
  
  /** Height of debugger panel at startUp.  Must be less than WINDOW_HEIGHT + TABS_HEIGHT.  Can be overridden if out of
    * bounds.
    */
  public static final NonNegativeIntegerOption DEBUG_PANEL_HEIGHT =
    new NonNegativeIntegerOption("debug.panel.height",new Integer(0));
  
  /** The directory in use by the file choosers upon the previous quit. */
  public static final FileOption LAST_DIRECTORY = new FileOption("last.dir", FileOps.NULL_FILE);
  
  /** The directory in use by the Interactions pane upon the previous quit. */
  public static final FileOption LAST_INTERACTIONS_DIRECTORY = new FileOption("last.interactions.dir", FileOps.NULL_FILE);
  
  /** Whether to save and restore Interactions pane directory at startUp/shutdown (sticky=true), or to use
    * "user.home" (sticky=false). */
  public static final BooleanOption STICKY_INTERACTIONS_DIRECTORY =
    new BooleanOption("sticky.interactions.dir", Boolean.TRUE);
  
  /** The command-line arguments to be passed to the Master JVM. */
  public static final StringOption MASTER_JVM_ARGS = new StringOption("master.jvm.args", "");
  
  /** The command-line arguments to be passed to the Slave JVM. */
  public static final StringOption SLAVE_JVM_ARGS = new StringOption("slave.jvm.args", "");
  
  /* Possible maximum heap sizes. */
  public static final ArrayList<String> heapSizeChoices = HeapSizeChoices.evaluate();
  static class HeapSizeChoices {
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add("default");
      aList.add("64");
      aList.add("128");
      aList.add("256");
      aList.add("512");
      aList.add("768");
      aList.add("1024");
      aList.add("1536");
      aList.add("2048");
      aList.add("2560");
      aList.add("3072");
      aList.add("3584");
      aList.add("4096");
      return aList;
    }
  }
  
  /** The command-line arguments for the maximum heap size (-Xmx___) to be passed to the Master JVM. */
  public static final ForcedChoiceOption MASTER_JVM_XMX =
    new ForcedChoiceOption("master.jvm.xmx", "default", heapSizeChoices);
  
  /** The command-line arguments for the maximum heap size (-Xmx___) to be passed to the Slave JVM. */
  public static final ForcedChoiceOption SLAVE_JVM_XMX =
    new ForcedChoiceOption("slave.jvm.xmx", "default", heapSizeChoices);
  
  /** The last state of the "Clipboard History" dialog. */
  public static final StringOption DIALOG_CLIPBOARD_HISTORY_STATE = new StringOption("dialog.clipboard.history.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_CLIPBOARD_HISTORY_STORE_POSITION =
    new BooleanOption("dialog.clipboardhistory.store.position", Boolean.TRUE);
  
  /** How many entries are kept in the clipboard history. */
  public static final NonNegativeIntegerOption CLIPBOARD_HISTORY_SIZE =
    new NonNegativeIntegerOption("clipboardhistory.store.size", 10);
  
  /** The last state of the "Go to File" dialog. */
  public static final StringOption DIALOG_GOTOFILE_STATE = new StringOption("dialog.gotofile.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_GOTOFILE_STORE_POSITION =
    new BooleanOption("dialog.gotofile.store.position", Boolean.TRUE);
  
  /** The last state of the "Open Javadoc" dialog. */
  public static final StringOption DIALOG_OPENJAVADOC_STATE = new StringOption("dialog.openjavadoc.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_OPENJAVADOC_STORE_POSITION =
    new BooleanOption("dialog.openjavadoc.store.position", Boolean.TRUE);
  
  /** The last state of the "Auto Import" dialog. */
  public static final StringOption DIALOG_AUTOIMPORT_STATE = new StringOption("dialog.autoimport.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_AUTOIMPORT_STORE_POSITION =
    new BooleanOption("dialog.autoimport.store.position", Boolean.TRUE);
  
  /** Number of entries in the browser history (0 for unlimited). */
  public static final NonNegativeIntegerOption BROWSER_HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("browser.history.max.size", new Integer(50));
  
  /**
   * Whether to also list files with fully qualified paths.
   */
  public static final BooleanOption DIALOG_GOTOFILE_FULLY_QUALIFIED =
    new BooleanOption("dialog.gotofile.fully.qualified", Boolean.FALSE);
  
  /** The last state of the "Complete File" dialog. */
  public static final StringOption DIALOG_COMPLETE_WORD_STATE = new StringOption("dialog.completeword.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_COMPLETE_WORD_STORE_POSITION =
    new BooleanOption("dialog.completeword.store.position", Boolean.TRUE);
  
  /** Whether to scan class files for auto-completion class names. */
  public static final BooleanOption DIALOG_COMPLETE_SCAN_CLASS_FILES =
    new BooleanOption("dialog.completeword.scan.class.files", Boolean.FALSE);
  
  /** Whether to include Java API classes in auto-completion. */
  public static final BooleanOption DIALOG_COMPLETE_JAVAAPI =
    new BooleanOption("dialog.completeword.javaapi", Boolean.FALSE);
  
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
  /** Whether to perform light-weight parsing. */
  public static final BooleanOption LIGHTWEIGHT_PARSING_ENABLED =
    new BooleanOption("lightweight.parsing.enabled", Boolean.FALSE);
  
  /** Delay for light-weight parsing. */
  public static final NonNegativeIntegerOption DIALOG_LIGHTWEIGHT_PARSING_DELAY =
    new NonNegativeIntegerOption("lightweight.parsing.delay", new Integer(500));
  
  /** The last state of the "Create Jar from Project " dialog. */
  public static final StringOption DIALOG_JAROPTIONS_STATE = new StringOption("dialog.jaroptions.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_JAROPTIONS_STORE_POSITION =
    new BooleanOption("dialog.jaroptions.store.position", Boolean.TRUE);
  
  /** The last state of the "Execute External Process" dialog. */
  public static final StringOption DIALOG_EXTERNALPROCESS_STATE = new StringOption("dialog.externalprocess.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_EXTERNALPROCESS_STORE_POSITION =
    new BooleanOption("dialog.externalprocess.store.position", Boolean.TRUE);
  
  /** The last state of the "Edit External Process" dialog. */
  public static final StringOption DIALOG_EDITEXTERNALPROCESS_STATE = new StringOption("dialog.editexternalprocess.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_EDITEXTERNALPROCESS_STORE_POSITION =
    new BooleanOption("dialog.editexternalprocess.store.position", Boolean.TRUE);
  
  /** Whether to put the focus in the definitions pane after find/replace. */
  public static final BooleanOption FIND_REPLACE_FOCUS_IN_DEFPANE =
    new BooleanOption("find.replace.focus.in.defpane", Boolean.FALSE);
  
  /** Whether to show a notification popup when the first DrJava error occurs. */
  public static final BooleanOption DIALOG_DRJAVA_ERROR_POPUP_ENABLED =
    new BooleanOption("dialog.drjava.error.popup.enabled", Boolean.TRUE);
  
  /** Whether to show the "code preview" popups in the RegionTreePanels (bookmarks, breakpoints, find all). */
  public static final BooleanOption SHOW_CODE_PREVIEW_POPUPS =
    new BooleanOption("show.code.preview.popups", Boolean.TRUE);
  
  /** Whether to use Runtime.halt to quit DrJava (see bugs 1550220 and 1478796). */
  public static final BooleanOption DRJAVA_USE_FORCE_QUIT =
    new BooleanOption("drjava.use.force.quit", Boolean.FALSE);
  
  /** Whether to display the "Auto Import" dialog when an undefined class
    * is encountered in the Interactions Pane. */
  public static final BooleanOption DIALOG_AUTOIMPORT_ENABLED =
    new BooleanOption("dialog.autoimport.enabled", Boolean.TRUE);
  
  /** Delay for following files. */
  public static final NonNegativeIntegerOption FOLLOW_FILE_DELAY =
    new NonNegativeIntegerOption("follow.file.delay", new Integer(300));
  
  /** Maximum lines to keep when following files, or 0 for unlimited. */
  public static final NonNegativeIntegerOption FOLLOW_FILE_LINES =
    new NonNegativeIntegerOption("follow.file.lines", new Integer(1000));
  
  /** Prefix for the "external saved" settings. */
  public static final String EXTERNAL_SAVED_PREFIX = "external.saved.";
  
  /** The number of saved external processes. */
  public static final NonNegativeIntegerOption EXTERNAL_SAVED_COUNT =
    new NonNegativeIntegerOption(EXTERNAL_SAVED_PREFIX+"count", new Integer(0));
  
  /** The names of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_NAMES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX+"names",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The types of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_TYPES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX+"types",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The command lines of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_CMDLINES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX+"cmdlines",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The JVM args of saved external processes (if applicable). */
  public static final VectorOption<String> EXTERNAL_SAVED_JVMARGS =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX+"jvmargs",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The work directories of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_WORKDIRS =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX+"workdirs",
                             new StringOption("",""),
                             new Vector<String>());
}
