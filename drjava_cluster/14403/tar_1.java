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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.lang.ref.WeakReference;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;

import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.lambda.Thunk;

/** Class for a document region that moves with changes in the document; it also includes a lazy tool-tip and line
  * boundaries.
  * @version $Id$Region
  */
public class MovingDocumentRegion extends DocumentRegion {
  protected volatile Position _lineStartPos;
  protected volatile Position _lineEndPos;
  protected final Thunk<String> _stringSuspension;
  
  /** Update _lineStartPos and _lineEndPos */
  public void updateLines() {
    try {  // _doc is inherited from DocumentRegion
      _lineStartPos = _doc.createPosition(_doc._getLineStartPos(getStartOffset()));
      _lineEndPos  = _doc.createPosition(_doc._getLineEndPos(getEndOffset()));
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }  // should never happen
  }
    
  /** Create a new moving document region. */
  public MovingDocumentRegion(final OpenDefinitionsDocument doc, int start, int end, int lineStart, int lineEnd) {

    super(doc, start, end);
    try {
      _lineStartPos = doc.createPosition(lineStart);
      _lineEndPos  = doc.createPosition(lineEnd);
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }  // should never happen
    
    assert doc != null;
    _stringSuspension = new Thunk<String>() {
      public String value() {
        try {
          int endSel = getEndOffset();
          int startSel = getStartOffset();
          int selLength = endSel - startSel;
          
          int excerptEnd = _lineEndPos.getOffset();
          int excerptStart = _lineStartPos.getOffset();
          int exceptLength = excerptEnd - excerptStart;
          
          // the offsets within the excerpted string of the selection (figuratively in "Red")
          int startRed = startSel - excerptStart;
          int endRed = endSel - excerptStart;
          
          int excerptLength = Math.min(120, excerptEnd - excerptStart);
          String text = doc.getText(excerptStart, excerptLength);
          
          // Construct the matching string and compressed selection prefix and suffix strings within text
          String prefix = StringOps.compress(text.substring(0, startRed));
          String match, suffix;
          if (excerptLength < startRed + selLength) { // selection extends beyond excerpt
            match = text.substring(startRed) + " ...";
            suffix = "";
          }
          else {
            match = text.substring(startRed, endRed);
            suffix = StringOps.compress(text.substring(endRed, excerptLength));
          }
          
          // COMMENT: We need a global invariant concerning non-displayable characters.  
          
          // create the excerpt string
          StringBuilder sb = new StringBuilder(edu.rice.cs.plt.text.TextUtil.htmlEscape(prefix));
          sb.append("<font color=#ff0000>");
//                sb.append(LEFT);
          sb.append(edu.rice.cs.plt.text.TextUtil.htmlEscape(match));
          sb.append("</font>");
//                sb.append(RIGHT);
          sb.append(edu.rice.cs.plt.text.TextUtil.htmlEscape(suffix));
//                sb.append("</html>");
//                sb.append(StringOps.getBlankString(120 - sLength));  // move getBank to StringOps
          return sb.toString();
        }
        catch(BadLocationException e) { return "";  /* Ignore the exception. */ }
      }
    };
  }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }
  
  /** @return line start */
  public int getLineStart() { return _lineStartPos.getOffset(); }
  
  /** @return line end */
  public int getLineEnd() { return _lineEndPos.getOffset(); }
  
  /** @return the string it was assigned */
  public String getString() { return _stringSuspension.value(); }
  
  /** @return true if objects a and b are equal; null values are handled correctly. */
  public static boolean equals(Object a, Object b) {
    if (a == null) return (b == null);
    return a.equals(b);
  }
}
