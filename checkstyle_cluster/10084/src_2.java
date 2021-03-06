package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.BaseCheckTestCase;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import java.io.File;

public class MultipleStringLiteralsCheckTest extends BaseCheckTestCase
{
    public void testIt() throws Exception
    {
        DefaultConfiguration checkConfig =
            createCheckConfig(MultipleStringLiteralsCheck.class);
        checkConfig.addAttribute("allowedDuplicates", "2");

        final String[] expected = {
            "5:16: The String \"StringContents\" appears 3 times in the file.",
        };

        verify(checkConfig,
               getPath("coding" + File.separator + "InputMultipleStringLiterals.java"),
               expected);
    }
}
