/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.BadLocationException;
import junit.framework.*;
import gj.util.Vector;
import junit.extensions.*;

public class DefinitionsDocumentTest extends TestCase {
	protected DefinitionsDocument defModel;
		
	public DefinitionsDocumentTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			defModel = new DefinitionsDocument();
		}
	
	public static Test suite()
		{
			return new TestSuite(DefinitionsDocumentTest.class);
		}

	public void testInsertToDoc()
		{
			try {
				defModel.insertString(0,"a/*bc */\"\\{}()",null);
				assertEquals("#0.0",defModel.getText(0,8),"a/*bc */");
				assertEquals("#0.1",14,defModel._currentLocation);
				defModel.insertString(0,"Start:",null);
				assertEquals("#1.0",defModel.getText(0,14),"Start:a/*bc */");
				assertEquals("#1.1",6,defModel._currentLocation);

				// document is:
				// Start:=>a/*bc */"\\{}()

				BraceReduction rm = defModel._reduced;
				assertEquals("2.1",0,rm.getStateAtCurrent());
				rm.move(2);
				// document is:
				// Start:a/=>*bc */"\\{}()
				assertEquals("2.3","/*",rm.currentToken().getType());
				rm.move(2);
				// document is:
				// Start:a/*b=>c */"\\{}()
				assertEquals("2.4",true,rm.currentToken().isGap());
				assertEquals("2.5",ReducedToken.INSIDE_BLOCK_COMMENT,
										 rm.currentToken().getState());
				rm.move(2);
				// document is:
				// Start:a/*bc =>*/"\{}()
				assertEquals("2.6","*/",rm.currentToken().getType());
				rm.move(2);
				// document is:
				// Start:a/*bc */=>"\{}()
				assertEquals("2.7","\"",rm.currentToken().getType());
				rm.move(1);
				// document is:
				// Start:a/*bc */"=>\{}()
				assertEquals("2.8","\\",rm.currentToken().getType());
				rm.move(1);
				// document is:
				// Start:a/*bc */"\=>{}()
				assertEquals("2.9","{",rm.currentToken().getType());
				rm.move(1);
				// document is:
				// Start:a/*bc */"\{=>}()
				assertEquals("2.91","}",rm.currentToken().getType());
				rm.move(1);
				// document is:
				// Start:a/*bc */"\{}=>()
				assertEquals("2.92","(",rm.currentToken().getType());
				rm.move(1);
				// document is:
				// Start:a/*bc */"\\{}(=>)
				assertEquals("2.93",")",rm.currentToken().getType());
			}
			catch( javax.swing.text.BadLocationException e) {
				System.out.println("EXCEPTION");
			}
		}

	public void testDeleteDoc()
		{
			try {
				defModel.insertString(0,"a/*bc */",null);
				defModel.remove(3,3);
				assertEquals("#0.0","a/**/",defModel.getText(0,5));
				assertEquals("#0.1",3,defModel._currentLocation);
					
				BraceReduction rm = defModel._reduced;
				assertEquals("1.0","*/",rm.currentToken().getType());
				// no longer support getBlockOffset
				//				assertEquals("1.1",0,rm.getBlockOffset());
				rm.move(-2);
				assertEquals("1.2","/*",rm.currentToken().getType());
				rm.move(2);
				assertEquals("1.3",ReducedToken.INSIDE_BLOCK_COMMENT,
										 rm.getStateAtCurrent());
			}
			catch( javax.swing.text.BadLocationException e)
				{
					System.out.println(e.toString());
				}
		}

	public void testStateChangeDoc()
		{
			try {
				Vector<StateBlock> actual;
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"a/*bc */\"\\{}()",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 1,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(1, 7,
																					 StateBlock.BLOCK_COMMENT_COLOR));
				expected.addElement(new StateBlock(8, 6,
																					 StateBlock.QUOTE_COLOR));
				
				assertEquals("#0.1",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.12",expected.elementAt(1),actual.elementAt(1));
				assertEquals("#0.13",expected.elementAt(2),actual.elementAt(2));

				assertEquals("#0.2",3,actual.size());

			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
		}

	public void testStateChangeDoc2()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"/*bc */ //ad}()",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 7,
																					 StateBlock.BLOCK_COMMENT_COLOR));
				expected.addElement(new StateBlock(7, 1,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(8, 7,
																					 StateBlock.LINE_COMMENT_COLOR));
			
				assertTrue("#0.1",actual.elementAt(0).equals(expected.elementAt(0)));
				assertTrue("#0.2",actual.elementAt(1).equals(expected.elementAt(1)));
				assertTrue("#0.2.2",actual.elementAt(2).equals(expected.elementAt(2)));
				assertEquals("#0.3",3,actual.size());
			
				expected = new Vector<StateBlock>();
			
				defModel.insertString(1,"\"hehe\"",null);
				// /"hehe"*bc */ //ad}()
							
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 1,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(1, 6,
																					 StateBlock.QUOTE_COLOR));
				expected.addElement(new StateBlock(7, 7,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(14, 7,
																					 StateBlock.LINE_COMMENT_COLOR));
				assertEquals("#0.4",true, defModel.hasHighlightChanged());
				assertEquals("#0.5",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.6",expected.elementAt(1),actual.elementAt(1));
				assertEquals("#0.7",expected.elementAt(2),actual.elementAt(2));
				assertEquals("#0.8",expected.elementAt(3),actual.elementAt(3));
				assertEquals("#0.9",4,actual.size());
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}

		}

	
	public void testStateChangeOnRemove()
		{
			try {
				Vector<StateBlock> actual;
				Vector<StateBlock> expected = new Vector<StateBlock>();
				defModel.insertString(0,"/*bc */ //ad}()",null);
				defModel.remove(13,2);

				// was commented out
				//assertEquals("#0.1(was commented out)",false,
				//					 	 defModel.hasHighlightChanged());
				defModel.remove(4,1);
				// /*bc*/ //ad}
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(2, 4,
																					 StateBlock.BLOCK_COMMENT_COLOR));
				expected.addElement(new StateBlock(6, 1,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(7, 5,
																					 StateBlock.LINE_COMMENT_COLOR));
				
				assertEquals("#0.2",3,actual.size());
				assertEquals("#0.3",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.4",expected.elementAt(1),actual.elementAt(1));
				assertEquals("#0.5",expected.elementAt(2),actual.elementAt(2));
				
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
		}

	
	public void testInsertBetween()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"{}\"\"/*bcnrqu */jl/}()",null);
				// {}""/*bcnrqu */jl/}()
				assertEquals("#0.0",true, defModel.hasHighlightChanged());
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 2,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(2, 2,
																					 StateBlock.QUOTE_COLOR));
				expected.addElement(new StateBlock(4, 11,
																					 StateBlock.BLOCK_COMMENT_COLOR));
				expected.addElement(new StateBlock(15, 6,
																					 StateBlock.DEFAULT_COLOR));

				assertEquals("#0.01",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.02",expected.elementAt(1),actual.elementAt(1));
				assertEquals("#0.03",expected.elementAt(2),actual.elementAt(2));
				assertEquals("#0.04",expected.elementAt(3),actual.elementAt(3));

				// {}""/*1bcnrqu */jl/}()
				defModel.insertString(6,"1",null);
				assertEquals("0.1",true,defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(0, 2,
																					 StateBlock.DEFAULT_COLOR));
				expected.addElement(new StateBlock(2, 2,
																					 StateBlock.QUOTE_COLOR));
				expected.addElement(new StateBlock(4, 12,
																					 StateBlock.BLOCK_COMMENT_COLOR));
				expected.addElement(new StateBlock(16, 6,
																					 StateBlock.DEFAULT_COLOR));

				defModel.insertString(9,"2",null);
				assertEquals("0.1",true,defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(4, 13,
																					 StateBlock.BLOCK_COMMENT_COLOR));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}

		}


	public void testInsertBetweenFormNewBrace()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"abcd/ddd",null);
				// was commented out
				//assertEquals("#0.0(was commented out)",false,
				//						 defModel.hasHighlightChanged());

				defModel.insertString(5,"/",null);
				assertEquals("#0.1",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(4, 5,
																					 StateBlock.LINE_COMMENT_COLOR));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
			
		}

	public void testInsertBetweenFormNewBrace2()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"\"",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(0, 1,
																					 StateBlock.QUOTE_COLOR));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}			
		}
}







