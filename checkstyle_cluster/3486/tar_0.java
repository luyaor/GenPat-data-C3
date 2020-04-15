////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
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
package com.puppycrawl.tools.checkstyle.checks.design;

import java.io.File;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import static com.puppycrawl.tools.checkstyle.checks.design.OneTopLevelClassCheck.MSG_KEY;

public class OneTopLevelClassCheckTest extends BaseCheckTestSupport
{

    @Test
    public void testFileWithOneTopLevelClass() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OneTopLevelClassCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("design" + File.separator + "InputOneTopLevelClass.java"), expected);
    }

    @Test
    public void testFileWithFewTopLevelClasses() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OneTopLevelClassCheck.class);
        final String[] expected = {
            "25: " + getCheckMessage(MSG_KEY, "NoSuperClone"),
            "33: " + getCheckMessage(MSG_KEY, "InnerClone"),
            "50: " + getCheckMessage(MSG_KEY, "CloneWithTypeArguments"),
            "58: " + getCheckMessage(MSG_KEY, "CloneWithTypeArgumentsAndNoSuper"),
            "67: " + getCheckMessage(MSG_KEY, "MyClassWithGenericSuperMethod"),
            "84: " + getCheckMessage(MSG_KEY, "AnotherClass"),
        };
        verify(checkConfig, getPath("coding" + File.separator + "InputClone.java"), expected);
    }

    @Test
    public void testFileWithSecondEnumTopLevelClass() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OneTopLevelClassCheck.class);
        final String[] expected = {
            "83: " + getCheckMessage(MSG_KEY, "InputDeclarationOrderEnum"),
        };
        verify(checkConfig, getPath("coding" + File.separator + "InputDeclarationOrder.java"), expected);
    }
}
