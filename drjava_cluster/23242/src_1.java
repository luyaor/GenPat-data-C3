/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.javalanglevels;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import junit.framework.TestCase;
import java.util.*;
import java.io.*;

/**
 * This is a high-level test to make sure that taking an Advanced Level file from
 * source file to augmented file has the correct behavior, does not throw errors when
 * it should not, throws errors when it should, and results in the correct augmented code.
 * Files that should be successfully tested are placed in the testFiles/forAdvancedLevelTest folder 
 * as .dj2 files, and the expected augmented files asre also placed in the testFiles/forAdvancedLevelTest
 * folder with the same name, but a .expected extension.  Files that are expected to generate errors are
 * placed in the testFiles/forAdvancedLevelTest/shouldBreak folder, as .dj2 files.
 * Other subdirectories are used for other tests.
 */public class AdvancedLevelTest extends TestCase {
  File directory;
  
  //Iterable _iterable;
  //Appendable _appendable;
  //EnumConstantNotPresentException _ecnp;
  //ProcessBuilder _pb;
  
  public void setUp() {
    directory = new File("testFiles/forAdvancedLevelTest");
  }
  
  /**
   * Try some example files and make sure they can be converted without errors and that the resulting conversions are correct.
   */
  public void testSuccessful() {
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj2");
      }
    });
    
    
    LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles);
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    
    /**Now make sure that the resulting java files are correct.*/
    for(int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      
      if (correctFile.exists()) {
        try {
          assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                       readFileAsString(correctFile),
                       readFileAsString(resultingFile));
        }
        catch (IOException ioe) {
          fail(ioe.getMessage());
          // let JUnit throw the exception
        }
      }
    }
    
    //test the subdirectory files as well.
    File newDirectory = new File(directory.getAbsolutePath() + System.getProperty("file.separator") + "importedFiles");
    testFiles = newDirectory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        String name = pathName.getAbsolutePath();
        return name.endsWith("IsItPackageAndImport.dj1") || name.endsWith("ToReference.dj1");
      }});
      
      
      /**Now make sure that the resulting java files are correct.*/
      for(int i = 0; i < testFiles.length; i++) {
        File currFile = testFiles[i];
        String fileName = currFile.getAbsolutePath();
        fileName = fileName.substring(0, fileName.length() -4);
        File resultingFile = new File(fileName + ".java");
        File correctFile = new File(fileName + ".expected");
        
        if (correctFile.exists()) {
          try {
            assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                         readFileAsString(correctFile),
                         readFileAsString(resultingFile));
          }
          catch (IOException ioe) {
            fail(ioe.getMessage());
            // let JUnit throw the exception
          }
        }
      }
      
      //And make sure that no java file was generated for ToReference2.dj1
      //(This is testing that we correctly handled what could have been an ambiguous name reference, but wasn't)
      File f = new File(newDirectory, "ToReference2.java");
      assertFalse("ToReference2.java should not exist", f.exists());
      
      newDirectory = new File(directory.getAbsolutePath() + System.getProperty("file.separator") + "importedFiles2");
      testFiles = newDirectory.listFiles(new FileFilter() {
        public boolean accept(File pathName) {
          return pathName.getAbsolutePath().endsWith("AlsoReferenced.dj1");
        }});
        
        
        
        /**Now make sure that the resulting java files are correct.*/
        for(int i = 0; i < testFiles.length; i++) {
          File currFile = testFiles[i];
          String fileName = currFile.getAbsolutePath();
          fileName = fileName.substring(0, fileName.length() -4);
          File resultingFile = new File(fileName + ".java");
          File correctFile = new File(fileName + ".expected");
          
          if (correctFile.exists()) {
            try {
              assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                           readFileAsString(correctFile),
                           readFileAsString(resultingFile));
            }
            catch (IOException ioe) {
              fail(ioe.getMessage());
              // let JUnit throw the exception
            }
          }
        }
        
        //And make sure that no java file was generated for ToReference.dj1
        f = new File(newDirectory, "ToReference.java");
        assertFalse("ToReference.java should not exist", f.exists());
        
  }

  /**
   * Test that if a package and a class have the same name, an error is given.
   */
  public void testPackageError() {
    directory = new File(directory.getAbsolutePath() + "/shouldBreak/noBreak");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj2");
      }});
      LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      for (int i = 0; i<testFiles.length; i++) {
        result = llc.convert(new File[]{testFiles[i]});
        assertTrue("should be parse exceptions or visitor exceptions", !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
    
      
  }

  /**
   * Test a set of files that have various Advanced Level errors.  See the files themselves for a description of the errors.
   */
  public void testShouldBeErrors() { 
    directory = new File(directory.getAbsolutePath() + "/shouldBreak");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj2");
      }});

      LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      for (int i = 0; i<testFiles.length; i++) {
        LanguageLevelVisitor._errorAdded = false;
        result = llc.convert(new File[]{testFiles[i]});
        assertTrue("should be parse exceptions or visitor exceptions", !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
  }
  
  
  /**
   * This file used to have a NullPointer Exception in it because of a bug in the code.  Leave the test in so that the bug
   * never gets reintroduced.
   */
  public void testNoNullPointer() { 
    directory = new File(directory.getAbsolutePath() + "/shouldBreak");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith("SwitchDoesntAssign.dj2");
      }});

      LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      for (int i = 0; i<testFiles.length; i++) {
        result = llc.convert(new File[]{testFiles[i]});
        assertTrue("should be parse exceptions or visitor exceptions", !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
  }  
  
    
  /**make sure that the order packaged files are compiled in does not matter.
   * This is to make sure that a bug that was fixed stays fixed.
   */
  public void testPackagedOrderMatters() {
     File newDirectory = new File(directory.getAbsolutePath() + System.getProperty("file.separator") + "lists-dj2" + System.getProperty("file.separator") + "src" + System.getProperty("file.separator") + "listFW");
     File[] testFiles = new File[]{new File(newDirectory, "NEList.dj2"),
                                   new File(newDirectory, "MTList.dj2"),
                                   new File(newDirectory, "IList.dj2")};

          System.out.flush();

      LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      result = llc.convert(testFiles);
      
      assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
      
      assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());

      //don't worry about checking the .java files for correctness...just make sure there weren't any exceptions
      }
  
  /**
   * Read the entire contents of a file and return them.  Copied from DrJava's FileOps.
   */
  public static String readFileAsString(final File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      String s = reader.readLine();
      buf.append(s);
    }

    reader.close();
    return buf.toString();
  }

}
