package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.BaseCheckTestCase;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class RedundantThrowsCheckTest
    extends BaseCheckTestCase
{
    public void testDefaults() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {
            "7:37: Redundant throws: 'java.io.FileNotFoundException' is subclass of 'java.io.IOException'.",
            "13:16: Redundant throws: 'RuntimeException' is unchecked exception.",
            "19:29: Redundant throws: 'java.io.IOException' listed more then one time.",
            "39:27: Redundant throws: 'NullPointerException' is subclass of 'RuntimeException'.",
            "39:27: Redundant throws: 'NullPointerException' is unchecked exception.",
            "39:49: Redundant throws: 'RuntimeException' is unchecked exception.",
        };
        verify(checkConfig, getPath("InputRedundantThrows.java"), expected);
    }

    public void testAllowUnchecked() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        checkConfig.addAttribute("allowUnchecked", "true");
        final String[] expected = {
            "7:37: Redundant throws: 'java.io.FileNotFoundException' is subclass of 'java.io.IOException'.",
            "19:29: Redundant throws: 'java.io.IOException' listed more then one time.",
//             "35:27: Unable to get class information for WrongException.",
            "39:27: Redundant throws: 'NullPointerException' is subclass of 'RuntimeException'.",
        };
        verify(checkConfig, getPath("InputRedundantThrows.java"), expected);
    }

    public void testAllowSubclasses() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        checkConfig.addAttribute("allowSubclasses", "true");
        final String[] expected = {
            "13:16: Redundant throws: 'RuntimeException' is unchecked exception.",
            "19:29: Redundant throws: 'java.io.IOException' listed more then one time.",
            "39:27: Redundant throws: 'NullPointerException' is unchecked exception.",
            "39:49: Redundant throws: 'RuntimeException' is unchecked exception.",
        };
        verify(checkConfig, getPath("InputRedundantThrows.java"), expected);
    }

    public void testRejectDuplicatesOnly() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        checkConfig.addAttribute("allowSubclasses", "true");
        checkConfig.addAttribute("allowUnchecked", "true");
        final String[] expected = {
            "19:29: Redundant throws: 'java.io.IOException' listed more then one time.",
        };
        verify(checkConfig, getPath("InputRedundantThrows.java"), expected);
    }

    public void test_1168408_1() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("javadoc/Test1.java"), expected);
    }

    public void test_1168408_2() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("javadoc/Test2.java"), expected);
    }

    public void test_1168408_3() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("javadoc/Test3.java"), expected);
    }

    public void test_1220726() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("javadoc/BadCls.java"), expected);
    }

    public void test_generics_params() throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(RedundantThrowsCheck.class);
        final String[] expected = {
            "15:34: Redundant throws: 'RE' is unchecked exception.",
            "23:37: Redundant throws: 'RE' is subclass of 'E'.",
            "23:37: Redundant throws: 'RE' is unchecked exception.",
            "31:69: Redundant throws: 'NPE' is subclass of 'RE'.",
            "31:69: Redundant throws: 'NPE' is unchecked exception.",
            "31:74: Redundant throws: 'RE' is unchecked exception.",
            "41:38: Redundant throws: 'RuntimeException' is subclass of 'RE'.",
            "41:38: Redundant throws: 'RuntimeException' is unchecked exception.",
            "41:56: Redundant throws: 'RE' is subclass of 'java.lang.RuntimeException'.",
            "41:56: Redundant throws: 'RE' is unchecked exception.",
            "42:13: Redundant throws: 'java.lang.RuntimeException' is unchecked exception.",
        };
        verify(checkConfig, getPath("javadoc/TestGenerics.java"), expected);
    }
}
