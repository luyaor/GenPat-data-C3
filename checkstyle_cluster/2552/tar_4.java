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
package com.puppycrawl.tools.checkstyle.checks.imports;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import java.io.File;
import org.junit.Test;

import static com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck.MSG_LEX;
import static com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck.MSG_LINE_SEPARATOR;
import static com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck.MSG_NONGROUP_IMPORT;
import static com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck.MSG_ORDER;

public class CustomImportOrderCheckTest extends BaseCheckTestSupport
{
    /**
     * @throws Exception
     */
    @Test
    public void testCustom() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("standardPackageRegExp", "java|javax");
        checkConfig.addAttribute("thirdPartyPackageRegExp", "com|org");
        checkConfig
                .addAttribute("customImportOrderRules",
                        "STATIC###SAME_PACKAGE(3)###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_LEX, "java.awt.Button.ABORT"),
            "7: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "8: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "9: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "10: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "11: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "12: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "13: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "14: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "15: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "16: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrder.java"), expected);
    }

    /**
     * Checks different group orderings and imports which are out of those ones
     * specified in the configuration.
     * @throws Exception
     */
    @Test
    public void testDefaultPackage() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", "com.|org.");
        checkConfig.addAttribute("customImportOrderRules",
                "STATIC###STANDARD_JAVA_PACKAGE###THIRD_PARTY_PACKAGE");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_LEX, "java.awt.Button.ABORT"),
            "9: " + getCheckMessage(MSG_LEX, "java.awt.Dialog"),
            "13: " + getCheckMessage(MSG_LEX, "java.io.File"),
            "15: " + getCheckMessage(MSG_LEX, "java.io.InputStream"),
            "20: " + getCheckMessage(MSG_LEX, "com.google.common.collect.*"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrder.java"), expected);
    }

    /**
     * Checks different combinations for same_package group.
     * @throws Exception
     */
    @Test
    public void testNonSpecifiedImports() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", "org.");
        checkConfig
                .addAttribute("customImportOrderRules",
                        "STATIC###STANDARD_JAVA_PACKAGE###THIRD_PARTY_PACKAGE###SAME_PACKAGE(3)");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_LEX, "java.awt.Button.ABORT"),
            "9: " + getCheckMessage(MSG_LEX, "java.awt.Dialog"),
            "13: " + getCheckMessage(MSG_LEX, "java.io.File"),
            "15: " + getCheckMessage(MSG_LEX, "java.io.InputStream"),
            "18: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "20: " + getCheckMessage(MSG_NONGROUP_IMPORT),
            "21: " + getCheckMessage(MSG_LINE_SEPARATOR, "org.junit.*"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrder.java"), expected);
    }

    @Test
    public void testOrderRuleWithOneGroup() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", "org.");
        checkConfig.addAttribute("customImportOrderRules",
                "STANDARD_JAVA_PACKAGE");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "7: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "8: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "9: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "10: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "11: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrder2.java"), expected);
    }

    @Test
    public void testSamePackageAndStatic() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", "org.");
        checkConfig.addAttribute("customImportOrderRules",
                "STATIC###SAME_PACKAGE(3)");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "5: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "6: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "7: " + getCheckMessage(MSG_ORDER, "STATIC"),
            "8: " + getCheckMessage(MSG_ORDER, "STATIC"),
            "10: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "11: " + getCheckMessage(MSG_ORDER, "STATIC"),
        };

        verify(checkConfig, new File("src/test/resources-noncompilable/com/puppycrawl/tools/"
                + "checkstyle/imports/"
                + "InputCustomImportOrderSamePackage.java").getCanonicalPath(), expected);
    }

    @Test
    public void testOnlySamePackage() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("customImportOrderRules", "SAME_PACKAGE(3)");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "6: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "7: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "8: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "9: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
        };

        verify(checkConfig, new File("src/test/resources-noncompilable/com/puppycrawl/tools/"
                + "checkstyle/imports/"
                + "InputCustomImportOrderSamePackage2.java").getCanonicalPath(), expected);
    }

    @Test
    public void testWithoutLineSeparator() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", "org.");
        checkConfig.addAttribute("separateLineBetweenGroups", "false");
        checkConfig.addAttribute("customImportOrderRules",
                "STATIC###SAME_PACKAGE(3)");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "5: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "6: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "7: " + getCheckMessage(MSG_ORDER, "STATIC"),
            "8: " + getCheckMessage(MSG_ORDER, "STATIC"),
            "10: " + getCheckMessage(MSG_ORDER, "SAME_PACKAGE"),
            "11: " + getCheckMessage(MSG_ORDER, "STATIC"),
        };

        verify(checkConfig, new File("src/test/resources-noncompilable/com/puppycrawl/tools/"
                + "checkstyle/imports/"
                + "InputCustomImportOrderSamePackage.java").getCanonicalPath(), expected);
    }

    @Test
    public void testNoValid() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", ".*");
        checkConfig.addAttribute("specialImportsRegExp", "com.google");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        checkConfig.addAttribute("customImportOrderRules",
                "STATIC###SPECIAL_IMPORTS###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE");
        final String[] expected = {};

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrderNoValid.java"), expected);
    }

    @Test
    public void testPossibleIndexOutOfBoundsException() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("thirdPartyPackageRegExp", ".*");
        checkConfig.addAttribute("specialImportsRegExp", "com.google");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        checkConfig.addAttribute("customImportOrderRules",
                "STATIC###SPECIAL_IMPORTS###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE");
        final String[] expected = {
            "5: " + getCheckMessage(MSG_ORDER, "THIRD_PARTY_PACKAGE"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "DOMSource.java"), expected);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testDefaultPackage2() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("standardPackageRegExp", "java|javax");
        checkConfig.addAttribute("thirdPartyPackageRegExp", "com|org");
        checkConfig.addAttribute("customImportOrderRules",
            "STATIC###SAME_PACKAGE(3)###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE");
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");

        final String[] expected = {
            "7: " + getCheckMessage(MSG_LEX, "java.awt.Button.ABORT"),
            "10: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "11: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "12: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "13: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "14: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "15: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "16: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "17: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "18: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "19: " + getCheckMessage(MSG_ORDER, "STANDARD_JAVA_PACKAGE"),
            "23: " + getCheckMessage(MSG_LEX, "com.google.common.*"),
        };

        verify(checkConfig, new File("src/test/resources-noncompilable/com/puppycrawl/tools/"
                + "checkstyle/imports/InputDefaultPackage.java").getCanonicalPath(), expected);
    }

    @Test
    public void testWithoutThirdPartyPackage() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("sortImportsInGroupAlphabetically", "true");
        checkConfig.addAttribute("separateLineBetweenGroups", "true");
        checkConfig.addAttribute("customImportOrderRules",
                "SAME_PACKAGE(3)###THIRD_PARTY_PACKAGE###STANDARD_JAVA_PACKAGE###STATIC");
        final String[] expected = {

        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrderThirdPartyPackage.java"), expected);
    }

    @Test
    public void testThirdPartyAndSpecialImports() throws Exception
    {
        final DefaultConfiguration checkConfig =
                createCheckConfig(CustomImportOrderCheck.class);
        checkConfig.addAttribute("specialImportsRegExp", "antlr.*");
        checkConfig.addAttribute("customImportOrderRules",
                "SAME_PACKAGE(3)###THIRD_PARTY_PACKAGE###STATIC###"
                + "SPECIAL_IMPORTS");
        final String[] expected = {
            "11: " + getCheckMessage(MSG_ORDER, "THIRD_PARTY_PACKAGE"),
        };

        verify(checkConfig, getPath("imports" + File.separator
                + "InputCustomImportOrderThirdPartyAndSpecial.java"), expected);
    }
}
