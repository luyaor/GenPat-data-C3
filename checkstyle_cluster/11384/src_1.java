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

package com.google.checkstyle.test.chapter4formatting.rule4861blockcommentstyle;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.checkstyle.test.base.BaseCheckTestSupport;
import com.google.checkstyle.test.base.ConfigurationBuilder;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck;

public class CommentsIndentationTest extends BaseCheckTestSupport {

    private static ConfigurationBuilder builder;

    @BeforeClass
    public static void setConfigurationBuilder() {
        builder = new ConfigurationBuilder(new File("src/it/"));
    }

    @Test
    public void commentsIndentationTest() throws Exception {

        final String[] expected = {
            "1: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.single", 2, 1, 0),
            "13: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.single", 14, 8, 6),
            "23: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.block", 24, 8, 4),
            "25: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.block", 27, 8, 4),
            "28: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.block", 31, 8, 4),
            "50: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.single", 51, 23, 19),
            "51: " + getCheckMessage(CommentsIndentationCheck.class, "comments.indentation.block", 53, 19, 32),
        };

        Configuration checkConfig = builder.getCheckConfig("CommentsIndentation");
        String filePath = builder.getFilePath("CommentsIndentationInput");

        Integer[] warnList = builder.getLinesWithWarn(filePath);
        verify(checkConfig, filePath, expected, warnList);
    }
}
