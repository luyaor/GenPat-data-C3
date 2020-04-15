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
import com.puppycrawl.tools.checkstyle.api.Utils;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RE;
import org.apache.commons.beanutils.ConversionException;

/**
 * Abstract class for checks that verify strings using a regular
 * expression. It provides support for setting the regular expression using
 * the property name <code>format</code>.
 *
 * @author <a href="mailto:checkstyle@puppycrawl.com">Oliver Burn</a>
 * @version 1.0
 */
public abstract class AbstractFormatCheck
    extends Check
{
    /** the regexp to match against */
    private RE mRegexp;
    /** the format string of the regexp */
    private String mFormat;

    /**
     * Creates a new <code>AbstractFormatCheck</code> instance.
     * @param aDefaultFormat default format
     * @throws ConversionException unable to parse aDefaultFormat
     */
    public AbstractFormatCheck(String aDefaultFormat)
    {
        setFormat(aDefaultFormat);
    }

    /**
     * Set the format to the specified regular expression.
     * @param aFormat a <code>String</code> value
     * @throws ConversionException unable to parse aFormat
     */
    public void setFormat(String aFormat)
    {
        try {
            mRegexp = Utils.getRE(aFormat);
            mFormat = aFormat;
        }
        catch (RESyntaxException e) {
            throw new ConversionException("unable to parse " + aFormat, e);
        }
    }

    /** @return the regexp to match against */
    public RE getRegexp()
    {
        return mRegexp;
    }

    /** @return the regexp format */
    public String getFormat()
    {
        return mFormat;
    }
}
