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
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * <p>
 * Checks for imports from a set of illegal packages.
 * By default, the check rejects all <code>sun.*</code> packages. 
 * To reject other packages, set property illegalPkgs to a comma-separated
 * list of the illegal packages.  
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * TODO: re-visit after configuration format settled.
 * <pre>
 * &lt;check
 *    classname="com.puppycrawl.tools.checkstyle.checks.IllegalImportCheck"/&gt;
 * </pre>
 * <p>
 * An example of how to configure the check so that it rejects packages
 * <code>java.io.*</code> and <code>java.sql.*</code> is
 * </p>
 * <pre>
 * &lt;check
 *    classname="com.puppycrawl.tools.checkstyle.checks.IllegalImportCheck"&gt;
 *    &lt;property name="illegalPkgs" value="java.io, java.sql"/&gt;
 * &lt;/check&gt;
 * </pre>
 * @author <a href="mailto:checkstyle@puppycrawl.com">Oliver Burn</a>
 * @author Lars K�hne
 * @version 1.0
 */
public class IllegalImportCheck
    extends AbstractImportCheck
{
    /** set of illegal packages */
    private final Set mIllegalPkgs = new TreeSet();

    /**
     * Creates a new <code>IllegalImportCheck</code> instance.
     */
    public IllegalImportCheck()
    {
        setIllegalPkgs(new String[] {"sun"});
    }

    /**
     * Set the list of illegal packages.
     * @param aFrom array of illegal packages
     */
    public void setIllegalPkgs(String[] aFrom)
    {
        mIllegalPkgs.clear();
        for (int i = 0; i < aFrom.length; i++) {
            final String s = aFrom[i];
            mIllegalPkgs.add(s);
        }
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.IMPORT};
    }

    /** @see com.puppycrawl.tools.checkstyle.api.Check */
    public void visitToken(DetailAST aAST)
    {
        final FullIdent imp = getImportText(aAST);
        if (isIllegalImport(imp.getText())) {
            log(aAST.getLineNo(),
                aAST.getColumnNo(),
                "import.illegal",
                imp.getText());
        }
    }

    /**
     * Checks if an import is from a package that must not be used.
     * @param aImportText the argument of the import keyword
     * @return if <code>aImportText</code> contains an illegal package prefix
     */
    private boolean isIllegalImport(String aImportText)
    {
        for (Iterator it = mIllegalPkgs.iterator(); it.hasNext();) {
            final String illegalPkg = (String) it.next();
            if (aImportText.startsWith(illegalPkg + ".")) {
                return true;
            }
        }
        return false;
    }
}
