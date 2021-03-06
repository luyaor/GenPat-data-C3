/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

/**
 * Javadoc formatter test suite with following options changes from the Eclipse
 * default settings:
 * <ul>
 * 	<li>'Remove blank lines' set to <code>true</code></li>
 * 	<li>'Enable header comment formatting' set to <code>true</code></li>
 * </ul>
 * options activated.
 */
public class FormatterCommentsClearBlankLinesTests extends FormatterCommentsTests {

public static Test suite() {
	// Get all superclass tests
	TestSuite suite = new Suite(FormatterCommentsClearBlankLinesTests.class.getName());
	List tests = buildTestsList(FormatterCommentsClearBlankLinesTests.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;

}

public FormatterCommentsClearBlankLinesTests(String name) {
	super(name);
}

protected void setUp() throws Exception {
    super.setUp();
}

DefaultCodeFormatter codeFormatter() {
	DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	preferences.comment_clear_blank_lines_in_block_comment = true;
	preferences.comment_clear_blank_lines_in_javadoc_comment = true;
	preferences.comment_format_header = true;
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	return codeFormatter;
}

String getOutputFolder() {
	return "clear_blank_lines";
}
}
