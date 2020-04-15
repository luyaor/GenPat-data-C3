////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2005  Oliver Burn
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

import java.util.Stack;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
/**
 * Check for ensuring that for loop control variables are not modified
 * inside the for block.
 *
 * @author Daniel Grenner
 */
public final class ModifiedControlVariableCheck extends Check
{
    /** Current set of parameters. */
    private Stack mCurrentVariables = new Stack();
    /** Stack of block parameters. */
    private final Stack mVariableStack = new Stack();

    /** {@inheritDoc} */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.OBJBLOCK,
            TokenTypes.LITERAL_FOR,
            TokenTypes.FOR_ITERATOR,
            TokenTypes.ASSIGN,
            TokenTypes.PLUS_ASSIGN,
            TokenTypes.MINUS_ASSIGN,
            TokenTypes.STAR_ASSIGN,
            TokenTypes.DIV_ASSIGN,
            TokenTypes.MOD_ASSIGN,
            TokenTypes.SR_ASSIGN,
            TokenTypes.BSR_ASSIGN,
            TokenTypes.SL_ASSIGN,
            TokenTypes.BAND_ASSIGN,
            TokenTypes.BXOR_ASSIGN,
            TokenTypes.BOR_ASSIGN,
            TokenTypes.INC,
            TokenTypes.POST_INC,
            TokenTypes.DEC,
            TokenTypes.POST_DEC,
        };
    }

    /** {@inheritDoc} */
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    /** {@inheritDoc} */
    public void beginTree(DetailAST aRootAST)
    {
        // clear data
        mCurrentVariables.clear();
        mVariableStack.clear();
    }

    /** {@inheritDoc} */
    public void visitToken(DetailAST aAST)
    {
        switch (aAST.getType()) {
        case TokenTypes.OBJBLOCK:
            enterBlock();
            break;
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.FOR_ITERATOR:
            break;
        case TokenTypes.ASSIGN:
        case TokenTypes.PLUS_ASSIGN:
        case TokenTypes.MINUS_ASSIGN:
        case TokenTypes.STAR_ASSIGN:
        case TokenTypes.DIV_ASSIGN:
        case TokenTypes.MOD_ASSIGN:
        case TokenTypes.SR_ASSIGN:
        case TokenTypes.BSR_ASSIGN:
        case TokenTypes.SL_ASSIGN:
        case TokenTypes.BAND_ASSIGN:
        case TokenTypes.BXOR_ASSIGN:
        case TokenTypes.BOR_ASSIGN:
        case TokenTypes.INC:
        case TokenTypes.POST_INC:
        case TokenTypes.DEC:
        case TokenTypes.POST_DEC:
            checkIdent(aAST);
            break;
        default:
            throw new IllegalStateException(aAST.toString());
        }
    }


    /** {@inheritDoc} */
    public void leaveToken(DetailAST aAST)
    {
        switch (aAST.getType()) {
        case TokenTypes.FOR_ITERATOR:
            visitForDef(aAST.getParent());
            break;
        case TokenTypes.LITERAL_FOR:
            leaveForDef(aAST);
            break;
        case TokenTypes.OBJBLOCK:
            exitBlock();
            break;
        case TokenTypes.ASSIGN:
        case TokenTypes.PLUS_ASSIGN:
        case TokenTypes.MINUS_ASSIGN:
        case TokenTypes.STAR_ASSIGN:
        case TokenTypes.DIV_ASSIGN:
        case TokenTypes.MOD_ASSIGN:
        case TokenTypes.SR_ASSIGN:
        case TokenTypes.BSR_ASSIGN:
        case TokenTypes.SL_ASSIGN:
        case TokenTypes.BAND_ASSIGN:
        case TokenTypes.BXOR_ASSIGN:
        case TokenTypes.BOR_ASSIGN:
        case TokenTypes.INC:
        case TokenTypes.POST_INC:
        case TokenTypes.DEC:
        case TokenTypes.POST_DEC:
            // Do nothing
            break;
        default:
            throw new IllegalStateException(aAST.toString());
        }
    }

    /**
     * Enters an inner class, which requires a new variable set.
     */
    private void enterBlock()
    {
        mVariableStack.push(mCurrentVariables);
        mCurrentVariables = new Stack();

    }
    /**
     * Leave an inner class, so restore variable set.
     */
    private void exitBlock()
    {
        mCurrentVariables = (Stack) mVariableStack.pop();
    }

    /**
     * Check if ident is parameter.
     * @param aAST ident to check.
     */
    private void checkIdent(DetailAST aAST)
    {
        if (mCurrentVariables != null && !mCurrentVariables.isEmpty()) {
            final DetailAST identAST = (DetailAST) aAST.getFirstChild();

            if (identAST != null
                && identAST.getType() == TokenTypes.IDENT
                && mCurrentVariables.contains(identAST.getText()))
            {
                log(aAST.getLineNo(), aAST.getColumnNo(),
                    "modified.control.variable", identAST.getText());
            }
        }
    }

    /**
     * Push current variables to the stack.
     * @param aAST a for definition.
     */
    private void visitForDef(DetailAST aAST)
    {
        final DetailAST forInitAST = aAST.findFirstToken(TokenTypes.FOR_INIT);
        DetailAST parameterDefAST =
            forInitAST.findFirstToken(TokenTypes.VARIABLE_DEF);

        for (; parameterDefAST != null;
             parameterDefAST = (DetailAST) parameterDefAST.getNextSibling())
        {
            if (parameterDefAST.getType() == TokenTypes.VARIABLE_DEF) {
                final DetailAST param =
                    parameterDefAST.findFirstToken(TokenTypes.IDENT);
                mCurrentVariables.push(param.getText());
            }
        }
    }

    /**
     * Pops the variables from the stack.
     * @param aAST a for definition.
     */
    private void leaveForDef(DetailAST aAST)
    {
        final DetailAST forInitAST = aAST.findFirstToken(TokenTypes.FOR_INIT);
        DetailAST parameterDefAST =
            forInitAST.findFirstToken(TokenTypes.VARIABLE_DEF);

        for (; parameterDefAST != null;
             parameterDefAST = (DetailAST) parameterDefAST.getNextSibling())
        {
            if (parameterDefAST.getType() == TokenTypes.VARIABLE_DEF) {
                mCurrentVariables.pop();
            }
        }
    }
}
