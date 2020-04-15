////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2004  Oliver Burn
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
package com.puppycrawl.tools.checkstyle.checks.coding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * <p>
 * Ensures that local variables that never get their values changed,
 * must be declared final.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="FinalLocalVariable"&gt;
 *     &lt;property name="token" value="VARIABLE_DEF"/&gt;
 * &lt;/module&gt;
 * </pre>
 * @author k_gibbs, r_auckenthaler
 */
public class FinalLocalVariableCheck extends Check
{
    /** Scope Stack */
    private Stack mScopeStack = new Stack();

    /**
     * @see Check#getDefaultTokens()
     */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.IDENT,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.STATIC_INIT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.SLIST,
            TokenTypes.OBJBLOCK,
        };
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getAcceptableTokens()
    {
        return new int[] {
            TokenTypes.VARIABLE_DEF,
            TokenTypes.PARAMETER_DEF,
        };
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getRequiredTokens()
    {
        return new int[] {
            TokenTypes.IDENT,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.STATIC_INIT,
            TokenTypes.LITERAL_FOR,
            TokenTypes.SLIST,
            TokenTypes.OBJBLOCK,
        };
    }

    /**
     * @see Check#visitToken(DetailAST)
     */
    public void visitToken(DetailAST aAST)
    {
        switch(aAST.getType()) {
        case TokenTypes.OBJBLOCK:
        case TokenTypes.SLIST:
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.METHOD_DEF:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.STATIC_INIT:
        case TokenTypes.INSTANCE_INIT:
            mScopeStack.push(new HashMap());
            break;

        case TokenTypes.PARAMETER_DEF:
        case TokenTypes.VARIABLE_DEF:
            if (aAST.getParent().getType() != TokenTypes.OBJBLOCK) {
                insertVariable(aAST);
            }
            break;

        case TokenTypes.IDENT:
            final int parentType = aAST.getParent().getType();
            if ((TokenTypes.POST_DEC        == parentType)
                || (TokenTypes.DEC          == parentType)
                || (TokenTypes.POST_INC     == parentType)
                || (TokenTypes.INC          == parentType)
                || (TokenTypes.ASSIGN       == parentType)
                || (TokenTypes.PLUS_ASSIGN  == parentType)
                || (TokenTypes.MINUS_ASSIGN == parentType)
                || (TokenTypes.DIV_ASSIGN   == parentType)
                || (TokenTypes.STAR_ASSIGN  == parentType)
                || (TokenTypes.MOD_ASSIGN   == parentType)
                || (TokenTypes.SR_ASSIGN    == parentType)
                || (TokenTypes.BSR_ASSIGN   == parentType)
                || (TokenTypes.SL_ASSIGN    == parentType)
                || (TokenTypes.BXOR_ASSIGN  == parentType)
                || (TokenTypes.BOR_ASSIGN   == parentType)
                || (TokenTypes.BAND_ASSIGN  == parentType))
            {
                removeVariable(aAST);
            }
            break;

        default:
        }
    }

    /**
     * Inserts a variable at the topmost scope stack
     * @param aAST the variable to insert
     */
    private void insertVariable(DetailAST aAST)
    {
        if (!aAST.branchContains(TokenTypes.FINAL)) {
            final HashMap state = (HashMap) mScopeStack.peek();
            final DetailAST ast = aAST.findFirstToken(TokenTypes.IDENT);
            state.put(ast.getText(), ast);
        }
    }

    /**
     * Removes the variable from the Stacks
     * @param aAST Variable to remove
     */
    private void removeVariable(DetailAST aAST)
    {
        for (int i = mScopeStack.size() - 1; i >= 0; i--) {
            final HashMap state = (HashMap) mScopeStack.get(i);
            final Object obj = state.remove(aAST.getText());
            if (obj != null) {
                break;
            }
        }
    }

    /**
     * @see Check#leaveToken(DetailAST)
     */
    public void leaveToken(DetailAST aAST)
    {
        super.leaveToken(aAST);

        switch(aAST.getType()) {
        case TokenTypes.OBJBLOCK:
        case TokenTypes.SLIST:
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.STATIC_INIT:
        case TokenTypes.INSTANCE_INIT:
        case TokenTypes.METHOD_DEF:
            final HashMap state = (HashMap) mScopeStack.pop();
            final Iterator finalVars = state.values().iterator();

            while (finalVars.hasNext()) {
                final DetailAST var = (DetailAST) finalVars.next();
                log(var.getLineNo(), var.getColumnNo(),
                    "final.variable", var.getText());
            }
            break;

        default:
        }
    }
}
