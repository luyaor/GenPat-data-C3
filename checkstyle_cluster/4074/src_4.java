////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2003  Oliver Burn
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
package com.puppycrawl.tools.checkstyle.checks.j2ee;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that a Bean implementation satisfies Bean
 * requirements:
 * <ul>
 * <li>The class is defined as <code>public</code>.</li>
 * <li>The class cannot be defined as <code>abstract</code> or
 * <code>final</code>.</li>
 * <li>It contains a <code>public</code> constructor with no parameters.</li>
 * <li>It must not define the <code>finalize</code> method.</li>
</ul>
 * @author Rick Giles
 */
public class AbstractBeanCheck
    extends Check
{
    /**
     * @see com.puppycrawl.tools.checkstyle.api.Check
     */
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF};
    }

    /**
     * @see com.puppycrawl.tools.checkstyle.api.Check
     */
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

/**
 * Checks a bean class requirements:
 * <ul>
 * <li>The class is defined as <code>public</code>.</li>
 * <li>The class cannot be defined as <code>abstract</code> or
 * <code>final</code>.</li>
 * <li>It contains a <code>public</code> constructor with no parameters.</li>
 * <li>It must not define the <code>finalize</code> method.</li>
 * </ul>
 * @param aAST CLASS_DEF node for class definition to check.
 * @param aBeanType bean type for error messages.
 */
    protected void checkBean(DetailAST aAST, String aBeanType)
    {
        final DetailAST nameAST = aAST.findFirstToken(TokenTypes.IDENT);
        if (!Utils.isPublic(aAST)) {
            log(nameAST.getLineNo(), nameAST.getColumnNo(),
                "nonpublic.bean", aBeanType);
        }
        if (Utils.isFinal(aAST)) {
            log(nameAST.getLineNo(), nameAST.getColumnNo(),
                "illegalmodifier.bean",
                new Object[] {aBeanType, "final"});
        }
        if (!Utils.hasPublicConstructor(aAST, 0)) {
            log(nameAST.getLineNo(), nameAST.getColumnNo(),
                "nonpublicconstructor.bean", aBeanType);
        }
        if (Utils.hasPublicMethod(aAST, "finalize", true, 0)) {
            log(
                nameAST.getLineNo(),
                nameAST.getColumnNo(),
                "hasfinalize.bean",
                aBeanType);
        }
    }
}
