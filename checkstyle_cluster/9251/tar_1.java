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
package com.puppycrawl.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.ScopeUtils;

/**
 * Checks for redundant modifiers in interface definitions.
 *
 * <p>
 * Rationale: The Java Language Specification strongly discourages the usage
 * of "public" and "abstract" for method declarations in interface definitions
 * as a matter of style.
 * </p>
 * <p>
 * Variables in interfaces are automatically public, static and final, so these
 * modifiers are redundant as well.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="RedundantModifier"/&gt;
 * </pre>
 * @author lkuehne
 */
public class RedundantModifierCheck
    extends Check
{
    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.METHOD_DEF,
            TokenTypes.VARIABLE_DEF,
        };
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getRequiredTokens()
    {
        return new int[] {};
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public void visitToken(DetailAST aAST)
    {
        if (ScopeUtils.inInterfaceBlock(aAST)) {
            final DetailAST modifiers =
                aAST.findFirstToken(TokenTypes.MODIFIERS);

            DetailAST modifier = (DetailAST) modifiers.getFirstChild();
            while (modifier != null) {

                // javac does not allow final or static in interface methods
                // hence no need to check that this is not a method

                final int type = modifier.getType();
                if (type == TokenTypes.LITERAL_PUBLIC
                    || type == TokenTypes.ABSTRACT
                    || type == TokenTypes.LITERAL_STATIC
                    || type == TokenTypes.FINAL)
                {
                    log(modifier.getLineNo(),
                        modifier.getColumnNo(),
                        "redundantModifier",
                        new String[] {modifier.getText()});
                    break;
                }

                modifier = (DetailAST) modifier.getNextSibling();
            }
        }
        else if (aAST.getType() == TokenTypes.METHOD_DEF) {
            final DetailAST modifiers =
                            aAST.findFirstToken(TokenTypes.MODIFIERS);
            // private method?
            boolean checkFinal =
                modifiers.branchContains(TokenTypes.LITERAL_PRIVATE);
            // declared in a final class?
            DetailAST parent = aAST.getParent();
            while (parent != null) {
                if (parent.getType() == TokenTypes.CLASS_DEF) {
                    final DetailAST classModifiers =
                        parent.findFirstToken(TokenTypes.MODIFIERS);
                    checkFinal |=
                        classModifiers.branchContains(TokenTypes.FINAL);
                    break;
                }
                parent = parent.getParent();
            }
            if (checkFinal) {
                DetailAST modifier = (DetailAST) modifiers.getFirstChild();
                while (modifier != null) {
                    final int type = modifier.getType();
                    if (type == TokenTypes.FINAL) {
                        log(modifier.getLineNo(),
                            modifier.getColumnNo(),
                            "redundantModifier",
                            new String[] {modifier.getText()});
                        break;
                    }
                    modifier = (DetailAST) modifier.getNextSibling();
                }
            }
        }
    }

}
