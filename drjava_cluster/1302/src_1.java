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

import edu.rice.cs.util.FileOps;
import edu.rice.cs.plt.lambda.Thunk;

/** Class for a document region that moves with changes in the document; it also includes a lazy tool-tip
 * @version $Id$Region
 */
public class MovingDocumentRegion extends DocumentRegion {
  protected final Thunk<String> _stringSuspension;
  
  /** Create a new moving document region. */
  public MovingDocumentRegion(OpenDefinitionsDocument doc, File file, Position sp, Position ep, Thunk<String> ss) {
    super(doc, sp, ep);
    assert doc != null;
    _stringSuspension = ss;
  }
  
  /** @return the document, or null if it hasn't been established yet */
  public OpenDefinitionsDocument getDocument() { return _doc; }

  /** @return the file */
  public File getFile() { return _file; }
  
  /** @return the string it was assigned */
  public String getString() { 
    StringBuilder result = new StringBuilder(120);
    result.append(_stringSuspension.value()); 
    return result.toString();
  }
  
  /** @return true if objects a and b are equal; null values are handled correctly. */
  public static boolean equals(Object a, Object b) {
    if (a == null) return (b == null);
    return a.equals(b);
  }
}
