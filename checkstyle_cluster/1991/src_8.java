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
package com.puppycrawl.tools.checkstyle.checks.indentation;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Handler for method calls.
 *
 * @author jrichard
 */
public class MethodCallHandler extends ExpressionHandler
{
    /**
     * Construct an instance of this handler with the given indentation check,
     * abstract syntax tree, and parent handler.
     *
     * @param aIndentCheck   the indentation check
     * @param aAst           the abstract syntax tree
     * @param aParent        the parent handler
     */
    public MethodCallHandler(IndentationCheck aIndentCheck,
        DetailAST aAst, ExpressionHandler aParent)
    {
        super(aIndentCheck,
            aAst.getType() == TokenTypes.CTOR_CALL
                ? "ctor call" : "method call",
            aAst,
            aParent);
    }

    /**
     * Check the indentation of the left parenthesis.
     */
    private void checkLParen()
    {
        DetailAST lparen = getMainAst();
        int columnNo = expandedTabsColumnNo(lparen);

        if (columnNo == getLevel()) {
            return;
        }

        if (!startsLine(lparen)) {
            return;
        }

        logError(lparen, "lparen", columnNo);
    }

    /**
     * Check the indentation of the right parenthesis.
     */
    private void checkRParen()
    {
        // the rparen can either be at the correct indentation, or on
        // the same line as the lparen
        DetailAST rparen = getMainAst().findFirstToken(TokenTypes.RPAREN);
        int columnNo = expandedTabsColumnNo(rparen);

        if (columnNo == getLevel()) {
            return;
        }

        if (!startsLine(rparen)) {
            return;
        }
        logError(rparen, "rparen", columnNo);
    }

    /**
     * Compute the indentation amount for this handler.
     *
     * @return the expected indentation amount
     */
    public int getLevelImpl()
    {
        // if inside a method call's params, this could be part of
        // an expression, so get the previous line's start
        if (getParent() instanceof MethodCallHandler) {
            MethodCallHandler container = ((MethodCallHandler) getParent());
            if (container != null) {
                if (areOnSameLine(container.getMainAst(), getMainAst())) {
                    return container.getLevel();
                }
                else {
                    return container.getLevel()
                        + getIndentCheck().getBasicOffset();
                }
            }

            // if we get here, we are the child of the left hand side (name
            //  side) of a method call with no "containing" call, use
            //  the first non-method call parent

            ExpressionHandler p = getParent();
            while (p instanceof MethodCallHandler) {
                p = p.getParent();
            }
            return p.suggestedChildLevel(this);
        }

        // if our expression isn't first on the line, just use the start
        // of the line
        LineSet lines = new LineSet();
        findSubtreeLines(lines, (DetailAST) getMainAst().getFirstChild(), true);
        int firstCol = lines.firstLineCol();
        int lineStart = getLineStart(getFirstAst(getMainAst()));
        if (lineStart != firstCol) {
            return lineStart;
        }
        else {
            return super.getLevelImpl();
        }
    }

    /**
     * Get the first AST of the specified method call.
     *
     * @param aAst   the method call
     *
     * @return the first AST of the specified method call
     */
    private DetailAST getFirstAst(DetailAST aAst)
    {
        // walk down the first child part of the dots that make up a method
        // call name

        DetailAST ast = (DetailAST) aAst.getFirstChild();
        while (ast != null && ast.getType() == TokenTypes.DOT) {
            ast = (DetailAST) ast.getFirstChild();
        }

        if (ast == null) {
            ast = aAst;
        }

        return ast;
    }

    /**
     * Indentation level suggested for a child element. Children don't have
     * to respect this, but most do.
     *
     * @param aChild  child AST (so suggestion level can differ based on child
     *                  type)
     *
     * @return suggested indentation for child
     */
    public int suggestedChildLevel(ExpressionHandler aChild)
    {
        // for whatever reason a method that crosses lines, like asList
        // here:
        //            System.out.println("methods are: " + Arrays.asList(
        //                new String[] {"method"}).toString());
        // will not have the right line num, so just get the child name

        DetailAST first = (DetailAST) getMainAst().getFirstChild();
        int indentLevel = getLineStart(first);
        if (aChild instanceof MethodCallHandler) {
            if (!areOnSameLine((DetailAST) aChild.getMainAst().getFirstChild(),
                (DetailAST) getMainAst().getFirstChild()))
            {
                indentLevel += getIndentCheck().getBasicOffset();
            }
        }
        return indentLevel;
    }

    /**
     * Check the indentation of the expression we are handling.
     */
    public void checkIndentation()
    {
        DetailAST methodName = (DetailAST) getMainAst().getFirstChild();
        checkExpressionSubtree(methodName, getLevel(), false, false);

        checkLParen();
        DetailAST rparen = getMainAst().findFirstToken(TokenTypes.RPAREN);
        DetailAST lparen = getMainAst();

        if (rparen.getLineNo() != lparen.getLineNo()) {

            // if this method name is on the same line as a containing
            // method, don't indent, this allows expressions like:
            //    method("my str" + method2(
            //        "my str2"));
            // as well as
            //    method("my str" +
            //        method2(
            //            "my str2"));
            //

            checkExpressionSubtree(
                getMainAst().findFirstToken(TokenTypes.ELIST),
                getLevel() + getIndentCheck().getBasicOffset(),
                false, true);

            checkRParen();
        }
    }

    /**
     * @return true if indentation should be increased after
     *              fisrt line in checkLinesIndent()
     *         false otherwise
     */
    protected boolean shouldIncreaseIndent()
    {
        return false;
    }
}
