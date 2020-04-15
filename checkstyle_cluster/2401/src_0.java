////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2002  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Check that reports empty blocks.
 *
 * @author Lars K�hne
 */
public class EmptyBlockCheck extends AbstractOptionCheck
{
    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.LITERAL_FINALLY,
            TokenTypes.LITERAL_SYNCHRONIZED,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_ELSE,
            TokenTypes.LITERAL_FOR,
            TokenTypes.STATIC_INIT,
            // TODO: need to handle....
            //TokenTypes.LITERAL_SWITCH,      
        };
    }
        
    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public void visitToken(DetailAST aAST)
    {
        final DetailAST slistAST = aAST.findFirstToken(TokenTypes.SLIST);
        if (slistAST != null) {
            if (mOption == BlockOption.STMT) {
                if (slistAST.getChildCount() <= 1) {      
                    log(slistAST.getLineNo(),
                        slistAST.getColumnNo(),
                        "block.noStmt",
                        aAST.getText());
                }   
            }
            else if (mOption == BlockOption.TEXT) {
                if (!hasText(slistAST)) {
                        log(slistAST.getLineNo(),
                        slistAST.getColumnNo(),
                        "block.empty",
                        aAST.getText());
                }
            }
        }
    }

    private boolean hasText(final DetailAST slistAST)
    {
        boolean retVal = false;
        
        final DetailAST rcurlyAST = slistAST.findFirstToken(TokenTypes.RCURLY);
        if (rcurlyAST != null) {
            final int slistLineNo = slistAST.getLineNo();
            final int slistColNo = slistAST.getColumnNo();
            final int rcurlyLineNo = rcurlyAST.getLineNo();
            final int rcurlyColNo = rcurlyAST.getColumnNo();
            final String[] lines = getLines();
            if (slistLineNo == rcurlyLineNo) {
                // Handle braces on the same line
                final String txt = lines[slistLineNo - 1]
                    .substring(slistColNo + 1, rcurlyColNo);
                if (txt.trim().length() != 0) {
                     retVal = true;
                }
            }
            else {
                // check only whitespace of first & last lines
                if ((lines[slistLineNo - 1]
                     .substring(slistColNo + 1).trim().length() != 0)
                    || (lines[rcurlyLineNo - 1]
                        .substring(0, rcurlyColNo).trim().length() != 0))
                {
                    retVal = true;
                }
                else {
                    // check if all lines are also only whitespace
                    for (int i = slistLineNo; i < (rcurlyLineNo - 1); i++)
                    {
                        if (lines[i].trim().length() > 0) {
                            retVal = true;
                            break;
                        }
                    }
                }
            }
        }               
        return retVal;
    }
}
