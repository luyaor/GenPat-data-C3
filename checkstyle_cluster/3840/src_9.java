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
package com.puppycrawl.tools.checkstyle.checks.whitespace;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Before;
import org.junit.Test;

public class NoWhitespaceAfterCheckTest
    extends BaseCheckTestSupport
{
    private DefaultConfiguration checkConfig;

    @Before
    public void setUp()
    {
        checkConfig = createCheckConfig(NoWhitespaceAfterCheck.class);
    }

    @Test
    public void testDefault() throws Exception
    {
        checkConfig.addAttribute("allowLineBreaks", "false");
        final String[] expected = {
            "5:14: '.' is followed by whitespace.",
            "6:12: '.' is followed by whitespace.",
            "29:14: '-' is followed by whitespace.",
            "29:21: '+' is followed by whitespace.",
            "31:15: '++' is followed by whitespace.",
            "31:22: '--' is followed by whitespace.",
            "111:22: '!' is followed by whitespace.",
            "112:23: '~' is followed by whitespace.",
            "129:24: '.' is followed by whitespace.",
            "132:11: '.' is followed by whitespace.",
            "136:12: '.' is followed by whitespace.",
        };
        verify(checkConfig, getPath("InputWhitespace.java"), expected);
    }

    @Test
    public void testDotAllowLineBreaks() throws Exception
    {
        checkConfig.addAttribute("tokens", "DOT");
        final String[] expected = {
            "5:14: '.' is followed by whitespace.",
            "129:24: '.' is followed by whitespace.",
            "136:12: '.' is followed by whitespace.",
        };
        verify(checkConfig, getPath("InputWhitespace.java"), expected);
    }

    @Test
    public void testTypecast() throws Exception
    {
        checkConfig.addAttribute("tokens", "TYPECAST");
        final String[] expected = {
            "87:28: ')' is followed by whitespace.",
            "89:23: ')' is followed by whitespace.",
            "241:22: ')' is followed by whitespace.",
        };
        verify(checkConfig, getPath("InputWhitespace.java"), expected);
    }

    @Test
    public void testArrayDeclarations() throws Exception
    {
        checkConfig.addAttribute("tokens", "ARRAY_DECLARATOR");
        final String[] expected = {
            "6:11: 'Object' is followed by whitespace.",
            "8:22: 'someStuff3' is followed by whitespace.",
            "9:8: 'int' is followed by whitespace.",
            "10:13: 's' is followed by whitespace.",
            "11:13: 'd' is followed by whitespace.",
            "16:14: 'get' is followed by whitespace.",
            "18:8: 'int' is followed by whitespace.",
            "19:34: 'get1' is followed by whitespace.",
            "28:8: 'int' is followed by whitespace.",
            "29:12: 'cba' is followed by whitespace.",
            "31:26: 'String' is followed by whitespace.",
            "32:27: 'String' is followed by whitespace.",
            "39:11: 'ar' is followed by whitespace.",
            "39:24: 'int' is followed by whitespace.",
            "40:16: 'int' is followed by whitespace.",
            "43:63: 'getLongMultArray' is followed by whitespace.",
        };
        verify(checkConfig, getPath("whitespace/InputNoWhitespaceAfterArrayDeclarations.java"), expected);
    }

    @Test
    public void testNpe() throws Exception
    {
        final String[] expected = {

        };
        verify(checkConfig, getPath("whitespace/InputNoWhiteSpaceAfterCheckFormerNpe.java"),
                 expected);
    }
}
