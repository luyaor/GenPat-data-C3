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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.ScopeUtils;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that constant names conform to a specified format.
 *
 * @author Rick Giles
 * @version 1.0
 */
public class ConstantNameCheck
    extends AbstractNameCheck
{
    /** Creates a new <code>ConstantNameCheck</code> instance. */
    public ConstantNameCheck()
    {
        super("^[A-Z](_?[A-Z0-9]+)*$");
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.VARIABLE_DEF};
    }

    /** @see com.puppycrawl.tools.checkstyle.checks.AbstractNameCheck */
    protected final boolean mustCheckName(DetailAST aAST)
    {
        boolean retVal = false;

        DetailAST modifiersAST = aAST.findFirstToken(TokenTypes.MODIFIERS);
        final boolean isStatic = modifiersAST != null
            && modifiersAST.branchContains(TokenTypes.LITERAL_STATIC);
        final boolean isFinal = modifiersAST != null
            && modifiersAST.branchContains(TokenTypes.FINAL);

        if ((isStatic  && isFinal) || ScopeUtils.inInterfaceBlock(aAST)) {
            // Handle the serialVersionUID constant which is used for
            // Serialization. Cannot enforce rules on it. :-)
            final DetailAST nameAST = aAST.findFirstToken(TokenTypes.IDENT);
            if ((nameAST != null)
                    && !("serialVersionUID".equals(nameAST.getText()))) {
                retVal = true;
            }
        }

        return retVal;
    }
}
