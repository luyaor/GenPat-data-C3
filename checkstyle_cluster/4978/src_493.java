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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.AbstractTypeAwareCheck;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks for redundant exceptions declared in throws clause
 * such as duplicates, unchecked exceptions or subclasses of
 * another declared exception.
 *
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="RedundantThrows"&gt;
 *    &lt;property name=&quot;allowUnchecked&quot; value=&quot;true&quot;/&gt;
 *    &lt;property name=&quot;allowSubclasses&quot; value=&quot;true&quot;/&gt;
 * &lt;/module&gt;
 * </pre>
 * @author o_sukhodolsky
 */
public class RedundantThrowsCheck
    extends AbstractTypeAwareCheck
{
    /**
     * whether unchecked exceptions in throws
     * are allowed or not
     */
    private boolean mAllowUnchecked;

    /**
     * whether subclass of another declared
     * exception is allowed in throws clause
     */
    private boolean mAllowSubclasses;

    /**
     * Getter for allowUnchecked property.
     * @param aAllowUnchecked whether unchecked excpetions in throws
     *                         are allowed or not
     */
    public void setAllowUnchecked(boolean aAllowUnchecked)
    {
        mAllowUnchecked = aAllowUnchecked;
    }

    /**
     * Getter for allowSubclasses property.
     * @param aAllowSubclasses whether subclass of another declared
     *                         exception is allowed in throws clause
     */
    public void setAllowSubclasses(boolean aAllowSubclasses)
    {
        mAllowSubclasses = aAllowSubclasses;
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF,
        };
    }

    /**
     * Checks exceptions declared in throws for a method or constructor.
     * @param aAST the tree node for the method or constructor.
     */
    protected final void processAST(DetailAST aAST)
    {
        final List knownExcs = new LinkedList();
        final DetailAST throwsAST =
            aAST.findFirstToken(TokenTypes.LITERAL_THROWS);
        if (throwsAST != null) {
            DetailAST child = (DetailAST) throwsAST.getFirstChild();
            while (child != null) {
                if ((child.getType() == TokenTypes.IDENT)
                    || (child.getType() == TokenTypes.DOT))
                {
                    final FullIdent fi = FullIdent.createFullIdent(child);
                    checkException(fi, knownExcs);
                }
                child = (DetailAST) child.getNextSibling();
            }
        }
    }

    /**
     * Logs error if unable to load class information.
     * @param aIdent class name for which we can no load class.
     */
    protected final void logLoadError(FullIdent aIdent)
    {
        log(aIdent.getLineNo(), aIdent.getColumnNo(),
            "redundant.throws.classInfo", aIdent.getText());
    }

    /**
     * Checks if an exception is already know (list of known
     * exceptions contains it or its superclass) and it's not
     * a superclass for some known exception and it's not
     * an unchecked exception.
     * If it's unknown then it will be added to ist of known exception.
     * All subclasses of this exception will be deleted from known
     * and the exception  will be added instead.
     *
     * @param aExc <code>FullIdent</code> of exception to check
     * @param aKnownExcs list of already known exception
     */
    private void checkException(FullIdent aExc, List aKnownExcs)
    {
        // Let's try to load class.
        ClassInfo newClassInfo = new ClassInfo(aExc);

        if (!mAllowUnchecked) {
            if (isUnchecked(newClassInfo.getClazz())) {
                log(aExc.getLineNo(), aExc.getColumnNo(),
                    "redundant.throws.unchecked", aExc.getText());
            }
        }

        boolean shouldAdd = true;
        for (Iterator known = aKnownExcs.iterator(); known.hasNext();) {
            final ClassInfo ci = (ClassInfo) known.next();
            final FullIdent fi = ci.getName();

            if (isSameType(fi.getText(), aExc.getText())) {
                shouldAdd = false;
                log(aExc.getLineNo(), aExc.getColumnNo(),
                    "redundant.throws.duplicate", aExc.getText());
            }
            else if (!mAllowSubclasses) {
                if (isSubclass(ci.getClazz(), newClassInfo.getClazz())) {
                    known.remove();
                    log(fi.getLineNo(), fi.getColumnNo(),
                        "redundant.throws.subclass",
                        fi.getText(), aExc.getText());
                }
                else if (isSubclass(newClassInfo.getClazz(), ci.getClazz())) {
                    shouldAdd = false;
                    log(aExc.getLineNo(), aExc.getColumnNo(),
                        "redundant.throws.subclass",
                        aExc.getText(), fi.getText());
                }
            }
        }

        if (shouldAdd) {
            aKnownExcs.add(newClassInfo);
        }
    }
}
