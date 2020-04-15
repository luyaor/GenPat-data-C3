package com.puppycrawl.tools.checkstyle.checks.metrics;

import com.puppycrawl.tools.checkstyle.BaseCheckTestCase;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import java.io.File;

public class ClassDataAbstractionCouplingCheckTest extends BaseCheckTestCase
{
    public void test() throws Exception {
        DefaultConfiguration checkConfig =
            createCheckConfig(ClassDataAbstractionCouplingCheck.class);

        checkConfig.addAttribute("max", "0");

        String[] expected = {
            "6:1: Class Data Abstraction Coupling is 4 (max allowed is 0) classes [AnotherInnerClass, InnerClass, HashSet, HashMap].",
            "7:5: Class Data Abstraction Coupling is 1 (max allowed is 0) classes [ArrayList].",
        };

        verify(checkConfig,
               getPath("metrics" + File.separator + "ClassCouplingCheckTestInput.java"),
               expected);
    }
}
