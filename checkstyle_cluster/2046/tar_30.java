package com.puppycrawl.tools.checkstyle;

import com.puppycrawl.tools.checkstyle.checks.OtherLeftCurlyCheck;
import com.puppycrawl.tools.checkstyle.checks.LeftCurlyOption;

public class OtherLeftCurlyCheckTest
    extends BaseCheckTestCase
{
    public void testDefault()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OtherLeftCurlyCheck.class);
        final Checker c = createChecker(checkConfig);
        final String fname = getPath("InputLeftCurlyOther.java");
        final String[] expected = {
            "19:9: '{' should be on the previous line.",
            "21:13: '{' should be on the previous line.",
            "23:17: '{' should be on the previous line.",
            "30:17: '{' should be on the previous line.",
            "34:17: '{' should be on the previous line.",
            "42:13: '{' should be on the previous line.",
            "46:13: '{' should be on the previous line.",
            "52:9: '{' should be on the previous line.",
            "54:13: '{' should be on the previous line.",
            "63:9: '{' should be on the previous line.",
        };
        verify(c, fname, expected);
    }

    public void testNL()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OtherLeftCurlyCheck.class);
        checkConfig.addAttribute("option", LeftCurlyOption.NL.toString());
        final Checker c = createChecker(checkConfig);
        final String fname = getPath("InputLeftCurlyOther.java");
        final String[] expected = {
            "26:33: '{' should be on a new line."
        };
        verify(c, fname, expected);
    }

    public void testMissingBraces()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(OtherLeftCurlyCheck.class);
        final Checker c = createChecker(checkConfig);
        final String fname = getPath("InputBraces.java");
        final String[] expected = {
        };
        verify(c, fname, expected);
    }
}
