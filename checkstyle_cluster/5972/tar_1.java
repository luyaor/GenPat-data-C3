////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2008  Oliver Burn
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

package com.puppycrawl.tools.checkstyle.checks.header;

import java.util.Arrays;

import java.io.File;
import java.util.List;

/**
 * Checks the header of the source against a header file that contains a
 * {@link java.util.regex.Pattern regular expression}
 * for each line of the source header.
 *
 * @author Lars K�hne
 * @author o_sukhodolsky
 */
public class RegexpHeaderCheck extends AbstractHeaderCheck
{
    /**
     * Provides typesafe access to the subclass specific HeaderInfo.
     *
     * @return the result of {@link #createHeaderInfo()}
     */
    protected RegexpHeaderInfo getRegexpHeaderInfo()
    {
        return (RegexpHeaderInfo) getHeaderInfo();
    }

    /**
     * Set the lines numbers to repeat in the header check.
     * @param aList comma separated list of line numbers to repeat in header.
     */
    public void setMultiLines(int[] aList)
    {
        getRegexpHeaderInfo().setMultiLines(aList);
    }

    @Override
    protected void processFiltered(File aFile, List<String> aLines)
    {
        final int headerSize = getRegexpHeaderInfo().getHeaderLines().size();
        final int fileSize = aLines.size();

        if (headerSize - getRegexpHeaderInfo().getMultLines().length > fileSize)
        {
            log(1, "header.missing");
        }
        else {
            int headerLineNo = 0;
            int i;
            for (i = 0; (headerLineNo < headerSize) && (i < fileSize); i++) {
                final String line = aLines.get(i);
                boolean isMatch = isMatch(line, headerLineNo);
                while (!isMatch && isMultiLine(headerLineNo)) {
                    headerLineNo++;
                    isMatch = (headerLineNo == headerSize)
                            || isMatch(line, headerLineNo);
                }
                if (!isMatch) {
                    log(i + 1, "header.mismatch", getHeaderLines().get(
                            headerLineNo));
                    break; // stop checking
                }
                if (!isMultiLine(headerLineNo)) {
                    headerLineNo++;
                }
            }
            if (i == fileSize) {
                // if file finished, but we have at least one non-multi-line
                // header isn't completed
                for (; headerLineNo < headerSize; headerLineNo++) {
                    if (!isMultiLine(headerLineNo)) {
                        log(1, "header.missing");
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected HeaderInfo createHeaderInfo()
    {
        return new RegexpHeaderInfo();
    }

    /**
     * Checks if a code line matches the required header line.
     * @param aLine the code line
     * @param aHeaderLineNo the header line number.
     * @return true if and only if the line matches the required header line.
     */
    private boolean isMatch(String aLine, int aHeaderLineNo)
    {
        return getRegexpHeaderInfo().getHeaderRegexps().get(aHeaderLineNo)
                .matcher(aLine).find();
    }

    /**
     * @param aLineNo a line number
     * @return if <code>aLineNo</code> is one of the repeat header lines.
     */
    private boolean isMultiLine(int aLineNo)
    {
        return (Arrays.binarySearch(getRegexpHeaderInfo().getMultLines(),
                aLineNo + 1) >= 0);
    }
}
