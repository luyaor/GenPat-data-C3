package com.puppycrawl.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.BaseCheckTestCase;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class MagicNumberCheckTest
    extends BaseCheckTestCase
{
    public void testDefault()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(MagicNumberCheck.class);
        final String[] expected = {
            "41:26: '3' is a magic number.",
            "42:32: '1.5' is a magic number.",
            "43:27: '3' is a magic number.",
            "43:31: '4' is a magic number.",
            "45:29: '3' is a magic number.",
            "47:23: '3' is a magic number.",
            "48:26: '1.5' is a magic number.",
            "50:22: '3' is a magic number.",
            "50:29: '5' is a magic number.",
            "50:37: '3' is a magic number.",
            "54:26: '3' is a magic number.",
            "55:39: '3' is a magic number.",
            "60:25: '010' is a magic number.",
            "61:25: '011' is a magic number.",
            "63:30: '010L' is a magic number.",
            "64:30: '011l' is a magic number.",
            "68:24: '0x10' is a magic number.",
            "69:24: '0X011' is a magic number.",
            "71:29: '0x10L' is a magic number.",
            "72:29: '0X11l' is a magic number.",
            "85:28: '3' is a magic number.",
            "91:14: '0xffffffff' is a magic number.",
            
        };
        verify(checkConfig, getPath("InputMagicNumber.java"), expected);
    }
    
    public void testIgnoreSome()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(MagicNumberCheck.class);
        checkConfig.addAttribute("ignoreNumbers", "0, 1, 3.0, 8, 16");
        final String[] expected = {
            "22:25: '2' is a magic number.",
            "28:35: '2' is a magic number.",
            "30:24: '2' is a magic number.",
            "32:29: '2.0' is a magic number.",
            "34:29: '2' is a magic number.",
            "36:17: '2' is a magic number.",
            "38:19: '2.0' is a magic number.",
            "42:32: '1.5' is a magic number.",
            "43:31: '4' is a magic number.",
            "48:26: '1.5' is a magic number.",
            "50:29: '5' is a magic number.",
            "61:25: '011' is a magic number.",
            "64:30: '011l' is a magic number.",
            "69:24: '0X011' is a magic number.",
            "72:29: '0X11l' is a magic number.",
            "91:14: '0xffffffff' is a magic number.",
        };
        verify(checkConfig, getPath("InputMagicNumber.java"), expected);
    }
    
    public void testIgnoreNone()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(MagicNumberCheck.class);
        checkConfig.addAttribute("ignoreNumbers", "");
        final String[] expected = {
            "21:24: '1' is a magic number.",
            "22:25: '2' is a magic number.",
            "23:26: '0L' is a magic number.",
            "24:26: '0l' is a magic number.",
            "25:30: '0D' is a magic number.",
            "26:30: '0d' is a magic number.",
            "28:35: '2' is a magic number.",
            "30:20: '1' is a magic number.",
            "30:24: '2' is a magic number.",
            "31:21: '1' is a magic number.",
            "32:23: '1.0' is a magic number.",
            "32:29: '2.0' is a magic number.",
            "34:22: '0' is a magic number.",
            "34:29: '2' is a magic number.",
            "36:13: '1' is a magic number.",
            "36:17: '2' is a magic number.",
            "38:13: '1.0' is a magic number.",
            "38:19: '2.0' is a magic number.",
            "41:26: '3' is a magic number.",
            "42:32: '1.5' is a magic number.",
            "43:27: '3' is a magic number.",
            "43:31: '4' is a magic number.",
            "45:29: '3' is a magic number.",
            "47:23: '3' is a magic number.",
            "48:26: '1.5' is a magic number.",
            "50:22: '3' is a magic number.",
            "50:29: '5' is a magic number.",
            "50:37: '3' is a magic number.",
            "54:26: '3' is a magic number.",
            "55:39: '3' is a magic number.",
            "59:25: '00' is a magic number.",
            "60:25: '010' is a magic number.",
            "61:25: '011' is a magic number.",
            "63:30: '010L' is a magic number.",
            "64:30: '011l' is a magic number.",
            "67:23: '0x0' is a magic number.",
            "68:24: '0x10' is a magic number.",
            "69:24: '0X011' is a magic number.",
            "70:28: '0x0L' is a magic number.",
            "71:29: '0x10L' is a magic number.",
            "72:29: '0X11l' is a magic number.",
            "85:28: '3' is a magic number.",
            "91:14: '0xffffffff' is a magic number.",
        };
        verify(checkConfig, getPath("InputMagicNumber.java"), expected);
    }
    
    public void testIntegersOnly()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(MagicNumberCheck.class);
        checkConfig.addAttribute("tokens", "NUM_INT, NUM_LONG");
        final String[] expected = {
            "41:26: '3' is a magic number.",
            "43:27: '3' is a magic number.",
            "43:31: '4' is a magic number.",
            "45:29: '3' is a magic number.",
            "47:23: '3' is a magic number.",
            "50:22: '3' is a magic number.",
            "50:29: '5' is a magic number.",
            "50:37: '3' is a magic number.",
            "54:26: '3' is a magic number.",
            "55:39: '3' is a magic number.",
            "60:25: '010' is a magic number.",
            "61:25: '011' is a magic number.",
            "63:30: '010L' is a magic number.",
            "64:30: '011l' is a magic number.",
            "68:24: '0x10' is a magic number.",
            "69:24: '0X011' is a magic number.",
            "71:29: '0x10L' is a magic number.",
            "72:29: '0X11l' is a magic number.",
            "85:28: '3' is a magic number.",
            "91:14: '0xffffffff' is a magic number.",
        };
        verify(checkConfig, getPath("InputMagicNumber.java"), expected);
    }
}

