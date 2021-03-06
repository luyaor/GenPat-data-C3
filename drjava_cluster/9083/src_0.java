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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.io.*;

import javax.swing.text.BadLocationException;
import javax.swing.event.*;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.CodeStatus;

/**
 * A test on the GlobalModel that does deals with everything outside of
 * simple file operations, e.g., compile, quit.
 *
 * @version $Id$
 */
public final class GlobalModelOtherTest extends GlobalModelTestCase
  implements OptionConstants
{
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelOtherTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(GlobalModelOtherTest.class);
  }
  
  /**
   * Tests that the undoableEditHappened event is fired if the undo manager
   * is in use.
   */
  public void testUndoEventsOccur() throws BadLocationException {
    final OpenDefinitionsDocument doc = _model.newFile();
    
    // Have to add an undoable edit listener for Undo to work
    doc.getDocument().addUndoableEditListener(new UndoableEditListener() {
      public void undoableEditHappened(UndoableEditEvent e) {
        doc.getDocument().getUndoManager().addEdit(e.getEdit());
      }
    });
    
    TestListener listener = new TestListener() {
      public void undoableEditHappened() {
        undoableEditCount++;
      }
    };
    _model.addListener(listener);
    changeDocumentText("test", doc);
    _model.removeListener(listener);
    listener.assertUndoableEditCount(1);
  }
  
  /**
   * Checks that System.exit is handled appropriately from
   * interactions frame.
   */
  public void testInteractionPreventedFromExit()
    throws DocumentAdapterException, InterruptedException
  {
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        interactionStartCount++;
      }

      public void interpreterExited(int status) {
        assertInteractionStartCount(1);
        assertInteractionsResettingCount(1);
        interpreterExitedCount++;
        lastExitStatus = status;
      }
      
      public void interpreterResetting() {
        assertInteractionStartCount(1);
        assertInteractionsExitedCount(0);
        assertInteractionsResetCount(0);
        interpreterResettingCount++;
      }

      public void interpreterReady() {
        synchronized(this) {
          assertInteractionStartCount(1);
          assertInteractionsExitedCount(1);
          assertInteractionsResettingCount(1);
          interpreterReadyCount++;
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    synchronized(listener) {
      interpretIgnoreResult("System.exit(23);");
      listener.wait();
    }
    _model.removeListener(listener);

    listener.assertInteractionStartCount(1);
    listener.assertInteractionsResettingCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertInteractionsExitedCount(1);
    assertEquals("exit status", 23, listener.lastExitStatus);
  }
  
  /**
   * Checks that System.exit is handled appropriately from
   * interactions frame when there is a security manager in
   * the interpreter JVM.
   */
  public void testInteractionResetFailed()
    throws DocumentAdapterException, InterruptedException
  {
    TestListener listener = new TestListener() {
      
      public void interpreterResetting() {
        assertInteractionsResetFailedCount(0);
        interpreterResettingCount++;
      }
      
      public void interpreterResetFailed() {
        synchronized(this) {
          assertInteractionsResettingCount(1);
          interpreterResetFailedCount++;
          this.notify();
        }
      }
    };

    // Prevent the Interactions JVM from quitting
    interpret("edu.rice.cs.drjava.DrJava.enableSecurityManager();");
    
    // Don't show the pop-up message
    _model._interpreterControl.setShowMessageOnResetFailure(false);
    
    _model.addListener(listener);
    synchronized(listener) {
      _model.resetInteractions();
      listener.wait();
    }
    _model.removeListener(listener);
    interpretIgnoreResult("edu.rice.cs.drjava.DrJava.disableSecurityManager();");

    listener.assertInteractionsResettingCount(1);
    listener.assertInteractionsResetFailedCount(1);
    listener.assertInteractionsResetCount(0);
    listener.assertInteractionsExitedCount(0);
  }

  /**
   * Checks that the interpreter can be aborted and then work
   * correctly later.
   * Part of what we check here is that the interactions classpath
   * is correctly reset after aborting interactions. That is, we ensure
   * that the compiled class is still visible after aborting. This was
   * broken in drjava-20020108-0958 -- or so I thought. I can't consistently
   * reproduce the problem in the UI (seems to show up using IBM's JDK only),
   * and I can never reproduce it in the test case. Grr.
   *
   * OK, now I found the explanation: We were in some cases running two new JVMs
   * on an abort. I fixed the problem in {@link MainJVM#restartInterpreterJVM}.
   */
  public void testInteractionAbort()
    throws BadLocationException, DocumentAdapterException, 
    InterruptedException, IOException
  {
    doCompile(setupDocument(FOO_TEXT), tempFile());
    final String beforeAbort = interpret("DrJavaTestFoo.class.getName()");
    assertEquals("\"DrJavaTestFoo\"", beforeAbort);
    
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        interactionStartCount++;
      }

      public void interactionEnded() {
        // this can only happen on the second interpretation!
        assertInteractionStartCount(2);
        interactionEndCount++;
      }

      public void interpreterExited(int status) {
        try {
          Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
        }
        assertInteractionStartCount(1);
        interpreterExitedCount++;
      }

      public void interpreterResetting() {
        assertInteractionStartCount(1);
        assertInteractionsExitedCount(0);
        assertInteractionsResetCount(0);
        interpreterResettingCount++;
      }
      
      public void interpreterReady() {
        synchronized(this) {
          assertInteractionStartCount(1);
          assertInteractionsExitedCount(0);
          assertInteractionsResettingCount(1);
          interpreterReadyCount++;
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    synchronized(listener) {
      interpretIgnoreResult("while (true) {}");
      listener.assertInteractionStartCount(1);
      _model.resetInteractions();
      listener.wait();
    }
    listener.assertInteractionsResettingCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertInteractionsExitedCount(0);

    // now make sure it still works!
    assertEquals("5", interpret("5"));
    _model.removeListener(listener);

    // make sure we can still see class foo
    final String afterAbort = interpret("DrJavaTestFoo.class.getName()");
    assertEquals("\"DrJavaTestFoo\"", afterAbort);
  }

  /**
   * Checks that reset console works.
   */
  public void testResetConsole()
    throws BadLocationException, DocumentAdapterException, InterruptedException
  {
    //System.err.println("Entering testResetConsole");
    TestListener listener = new TestListener() {
      public void interactionStarted() {}
      public void interactionEnded() {
        synchronized(this) {
          interactionEndCount++;
          this.notify();
        }
      }

      public void consoleReset() {
        consoleResetCount++;
      }
    };

    _model.addListener(listener);

    _model.resetConsole();
    assertEquals("Length of console text",
                 0,
                 _model.getConsoleDocument().getDocLength());

    listener.assertConsoleResetCount(1);

    synchronized(listener) {
      interpretIgnoreResult("System.out.print(\"a\");");
      listener.wait();  // notified on interactionEnded
    }

    /*
    // alas, there's no very good way to know when it's done
    // so we just wait some time hoping the println will have happened
    int i;
    for (i = 0; i < 50; i++) {
      if (_model.getConsoleDocument().getLength() == 1) {
        break;
      }

      Thread.currentThread().sleep(100);
    }
    //System.err.println("wait i=" + i);
    */

    assertEquals("Length of console text",
                 1,
                 _model.getConsoleDocument().getDocLength());


    _model.resetConsole();
    assertEquals("Length of console text",
                 0,
                 _model.getConsoleDocument().getDocLength());

    listener.assertConsoleResetCount(2);
  }

  /**
   * Creates a new class, compiles it and then checks that the REPL
   * can see it.  Then checks that a compiled class file in another
   * directory can be both accessed and extended if it is on the
   * "extra.classpath" config option.
   */
  public void testInteractionsCanSeeCompiledClasses()
    throws BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    // Compile Foo
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    File dir1 = new File(_tempDir, "dir1");
    dir1.mkdir();
    File file1 = new File(dir1, "TestFile1.java");
    doCompile(doc1, file1);

    assertEquals("interactions result",
                 "\"DrJavaTestFoo\"",
                 interpret("new DrJavaTestFoo().getClass().getName()"));
    
    // Add directory 1 to extra classpath and close doc1
    Vector<File> cp = new Vector<File>();
    cp.addElement(dir1);
    DrJava.getConfig().setSetting(EXTRA_CLASSPATH, cp);
    _model.closeFile(doc1);
    
    // Compile Baz which extends Foo in another directory.
    OpenDefinitionsDocument doc2 = setupDocument(BAZ_TEXT);
    File dir2 = new File(_tempDir, "dir2");
    dir2.mkdir();
    File file2 = new File(dir2, "TestFile1.java");
    doCompile(doc2, file2);
    
    // Ensure that Baz can use the Foo class from extra classpath
    assertEquals("interactions result",
                 "\"DrJavaTestBaz\"",
                 interpret("new DrJavaTestBaz().getClass().getName()"));
    
    // Ensure that static fields can be seen
    // IN PROGRESS: BUG #702733
//    assertEquals("result of static field",
//                 "3",
//                 interpret("DrJavaTestBaz.x"));
    
    // Also ensure that Foo can be used directly
    assertEquals("interactions result",
                 "\"DrJavaTestFoo\"",
                 interpret("new DrJavaTestFoo().getClass().getName()"));
  }
  
  /**
   * Compiles a new class in the default package with a mixed case name,
   * and ensures that it can be instantiated on a variable with an
   * identical name (but a lowercase first letter).
   * Catches SF bug #689026 ("DynamicJava can't handle certain variable names")
   */
  public void testInteractionsVariableWithLowercaseClassName()
    throws BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    // Compile a test file
    OpenDefinitionsDocument doc1 = 
      setupDocument("public class DrJavaTestClass {}");
    File file1 = new File(_tempDir, "DrJavaTestClass.java");
    doCompile(doc1, file1);

    // This shouldn't cause an error (no output should be displayed)
    assertEquals("interactions result", "",
                 interpret("drJavaTestClass = new DrJavaTestClass();"));
  }

  /**
   * Checks that updating a class and recompiling it is visible from
   * the REPL.
   */
  public void testInteractionsCanSeeChangedClass()
    throws BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    final String text_before = "class DrJavaTestFoo { public int m() { return ";
    final String text_after = "; } }";
    final int num_iterations = 3;
    File file;
    OpenDefinitionsDocument doc;

    for (int i = 0; i < num_iterations; i++) {
      doc = setupDocument(text_before + i + text_after);
      file = tempFile(i);
      doCompile(doc, file);
      
      assertEquals("interactions result, i=" + i,
          String.valueOf(i),
          interpret("new DrJavaTestFoo().m()"));
    }
  }

  /**
   * Checks that an anonymous inner class can be defined in the repl!
   */
  public void testInteractionsDefineAnonymousInnerClass()
    throws BadLocationException, DocumentAdapterException, 
    IOException, InterruptedException
  {
    final String interface_text = "public interface I { int getValue(); }";
    final File file = createFile("I.java");

    OpenDefinitionsDocument doc;

    doc = setupDocument(interface_text);
    doCompile(doc, file);

    for (int i = 0; i < 3; i++) {
      String s = "new I() { public int getValue() { return " + i + "; } }.getValue()";

      assertEquals("interactions result, i=" + i,
                   String.valueOf(i),
                   interpret(s));
    }
  }

  public void testGetSourceRootDefaultPackage()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Get current working directory
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
        
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    
    // Get source root (current directory only)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 0, roots.length);
    /*assertEquals("source root (current directory)", 
                 workDir,
                 roots[0]);
                 */
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source roots
    roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", subdir, roots[0]);
  }

  public void testGetSourceRootPackageThreeDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", baseTempDir, roots[0]);

  }

  public void testGetSourceRootPackageThreeDeepInvalid()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Get current working directory
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
        
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    // Now make subdirectory a/b/d
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "d");
    subdir.mkdirs();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // The package name is wrong so this should return only currDir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 0, roots.length);
    /*assertEquals("source root (current directory)", 
                 workDir,
                 roots[0]);*/
  }

  public void testGetSourceRootPackageOneDeepValid()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a
    File subdir = new File(baseTempDir, "a");
    subdir.mkdir();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    OpenDefinitionsDocument doc = setupDocument("package a;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", baseTempDir, roots[0]);

  }


  public void testGetMultipleSourceRootsDefaultPackage()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectories a, b
    File subdir1 = new File(baseTempDir, "a");
    subdir1.mkdir();
    File subdir2 = new File(baseTempDir, "b");
    subdir2.mkdir();

    // Save the footext to DrJavaTestFoo.java in subdirectory 1
    File file1 = new File(subdir1, "DrJavaTestFoo.java");
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    doc1.saveFileAs(new FileSelector(file1));

    // Save the bartext to Bar.java in subdirectory 1
    File file2 = new File(subdir1, "Bar.java");
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    doc2.saveFileAs(new FileSelector(file2));

    // Save the bartext to Bar.java in subdirectory 2
    File file3 = new File(subdir2, "Bar.java");
    OpenDefinitionsDocument doc3 = setupDocument(BAR_TEXT);
    doc3.saveFileAs(new FileSelector(file3));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source roots (should be 2: no duplicates)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, roots.length);
    File root1 = roots[0];
    File root2 = roots[1];

    // Make sure both source roots are in set
    // But we don't care about the order
    if (!( (root1.equals(subdir1) && root2.equals(subdir2)) ||
           (root1.equals(subdir2) && root2.equals(subdir1)) ))
    {
      fail("source roots did not match");
    }
  }

  
  /**
   * Creates a new class, compiles it and then checks that the REPL
   * can see it.
   */
  public void testInteractionsLiveUpdateClasspath()
    throws BadLocationException, DocumentAdapterException, 
    IOException, InterruptedException
  {
      
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    File f = tempFile();
    
    doCompile(doc, f);

    // Rename the directory so it's not on the classpath anymore
    String tempPath = f.getParent();
    File tempDir = new File(tempPath);
    tempDir.renameTo(new File(tempPath+"a"));

    String result = interpret("new DrJavaTestFoo().getClass().getName()");
    
    // Should cause a NoClassDefFound, but we shouldn't check exact syntax.
    //  Instead, make sure it isn't "DrJavaTestFoo", as if the class was found.
    assertTrue("interactions should have an error, not the correct answer",
               !"\"DrJavaTestFoo\"".equals(result));

    // Add new directory to classpath through Config
    Vector<File> cp = new Vector<File>();
    cp.addElement(new File(tempPath + "a"));
    DrJava.getConfig().setSetting(EXTRA_CLASSPATH, cp);
    
    result = interpret("new DrJavaTestFoo().getClass().getName()");
    
    // Now it should be on the classpath
    assertEquals("interactions result",
                 "\"DrJavaTestFoo\"",
                 result);
    
    
    // Rename directory back to clean up
    tempDir = new File(tempPath + "a");
    tempDir.renameTo( new File(tempPath));

  }
  
  /**
   * Tests that the appropriate event is fired when the model's interpreter changes.
   */
  public void testSwitchInterpreters() {
    TestListener listener = new TestListener() {
      public void interpreterChanged(boolean inProgress) {
        assertTrue("should not be in progress", !inProgress);
        interpreterChangedCount++;
      }
    };
    _model.addListener(listener);
    
    // Create a new Java interpreter
    ((RMIInteractionsModel)_model.getInteractionsModel()).
      addJavaInterpreter("testInterpreter");
    
    // Set it to be active
    ((RMIInteractionsModel)_model.getInteractionsModel()).
      setActiveInterpreter("testInterpreter", "myPrompt>");
    
    listener.assertInterpreterChangedCount(1);
    _model.removeListener(listener);
  }
  
  /**
   * Tests that setting and changing an input listener works correctly.
   */
  public void testSetChangeInputListener() {
    InputListener listener1 = new InputListener() {
      public String getConsoleInput() {
        return "input1";
      }
    };
    
    InputListener listener2 = new InputListener() {
      public String getConsoleInput() {
        return "input2";
      }
    };
    
    try {
      _model.getConsoleInput();
      fail("Should not have allowed getting input before a listener is installed!");
    }
    catch (IllegalStateException ise) {
      assertEquals("Should have thrown the correct exception.",
                   "No input listener installed!", ise.getMessage());
    }
    
    _model.setInputListener(listener1);
    assertEquals("First input listener should return correct input", "input1", _model.getConsoleInput());
    _model.changeInputListener(listener1, listener2);
    assertEquals("Second input listener should return correct input", "input2", _model.getConsoleInput());
  }
   
}
