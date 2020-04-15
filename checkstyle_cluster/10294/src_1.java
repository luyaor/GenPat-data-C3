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
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * <p>
 * Checks that a token is followed by whitespace.
 * </p>
 * <p> By default the check will check the following tokens:
 *  {@link TokenTypes#COMMA COMMA},
 *  {@link TokenTypes#SEMI SEMI},
 *  {@link TokenTypes#TYPECAST TYPECAST}.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="WhitespaceAfter"/&gt;
 * </pre> 
 * <p> An example of how to configure the check for whitespace only after
 * {@link TokenTypes#COMMA COMMA} and {@link TokenTypes#SEMI SEMI} tokens is:
 * </p>
 * <pre>
 * &lt;module name="WhitespaceAfter"&gt;
 *     &lt;property name="tokens" value="COMMA, SEMI"/&gt;
 * &lt;/module&gt;
 * </pre>
 * @author <a href="mailto:checkstyle@puppycrawl.com">Oliver Burn</a>
 * @author Rick Giles
 * @version 1.0
 */
public class WhitespaceAfterCheck
    extends Check
{
    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.COMMA,
            TokenTypes.SEMI,
            TokenTypes.TYPECAST,
        };
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public void visitToken(DetailAST aAST)
    {
        final String[] lines = getLines();
        final Object[] message;
        final DetailAST targetAST;
        if (aAST.getType() == TokenTypes.TYPECAST) {
            targetAST = aAST.findFirstToken(TokenTypes.RPAREN);
            // TODO: i18n
            message = new Object[]{"cast"};
        }
        else {
            targetAST = aAST;
            message = new Object[]{aAST.getText()};
        }
        final String line = lines[targetAST.getLineNo() - 1];
        final int after =
            targetAST.getColumnNo() + targetAST.getText().length();

        if (after < line.length()) {
            
            final char charAfter = line.charAt(after);
            if ((targetAST.getType() == TokenTypes.SEMI)
                && ((charAfter == ';') || (charAfter == ')')))
            {
                return;
            }
            if (!Character.isWhitespace(charAfter)) {
                log(targetAST.getLineNo(),
                    targetAST.getColumnNo() + targetAST.getText().length(),
                    "ws.notFollowed",
                    message);
            }
        }
    }
}
