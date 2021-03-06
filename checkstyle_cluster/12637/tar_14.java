package com.puppycrawl.tools.checkstyle;

import com.puppycrawl.tools.checkstyle.checks.ParenPadCheck;
import com.puppycrawl.tools.checkstyle.checks.PadOption;

public class ParenPadCheckTest
    extends BaseCheckTestCase
{
    public void testDefault()
        throws Exception
    {
        final CheckConfiguration checkConfig = new CheckConfiguration();
        checkConfig.setClassname(ParenPadCheck.class.getName());
        final Checker c = createChecker(checkConfig);
        final String fname = getPath("InputWhitespace.java");
        final String[] expected = {
            "58:12: '(' is followed by whitespace.",
            "58:36: ')' is preceeded with whitespace.",
            "74:13: '(' is followed by whitespace.",
            "74:18: ')' is preceeded with whitespace.",
        };
        verify(c, fname, expected);
    }

    public void testSpace()
        throws Exception
    {
        final CheckConfiguration checkConfig = new CheckConfiguration();
        checkConfig.setClassname(ParenPadCheck.class.getName());
        checkConfig.addProperty("option", PadOption.SPACE.toString());
        final Checker c = createChecker(checkConfig);
        final String fname = getPath("InputWhitespace.java");
        final String[] expected = {
            "29:20: '(' is not followed by whitespace.",
            "29:23: ')' is not preceeded with whitespace.",
            "37:22: '(' is not followed by whitespace.",
            "37:26: ')' is not preceeded with whitespace.",
            "41:15: '(' is not followed by whitespace.",
            "41:33: ')' is not preceeded with whitespace.",
            "76:20: '(' is not followed by whitespace.",
            "76:21: ')' is not preceeded with whitespace.",
            "87:21: '(' is not followed by whitespace.",
            "87:27: ')' is not preceeded with whitespace.",
            "88:14: '(' is not followed by whitespace.",
            "88:20: ')' is not preceeded with whitespace.",
            "89:14: '(' is not followed by whitespace.",
            "89:20: ')' is not preceeded with whitespace.",
            "90:14: '(' is not followed by whitespace.",
            "90:20: ')' is not preceeded with whitespace.",
            "97:22: '(' is not followed by whitespace.",
            "97:28: ')' is not preceeded with whitespace.",
            "98:14: '(' is not followed by whitespace.",
            "98:18: ')' is not preceeded with whitespace.",
            "150:28: '(' is not followed by whitespace.",
            "150:32: ')' is not preceeded with whitespace.",
            "153:16: '(' is not followed by whitespace.",
            "153:20: ')' is not preceeded with whitespace.",
            "162:20: '(' is not followed by whitespace.",
            "165:10: ')' is not preceeded with whitespace.",
        };
        verify(c, fname, expected);
    }
}
