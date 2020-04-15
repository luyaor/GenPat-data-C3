/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.junit.extension;

import junit.framework.ComparisonFailure;

public class TestCase extends junit.framework.TestCase {
	public TestCase(String name) {
		super(name);
	}
public static void assertEquals(String expected, String actual) {
    assertEquals(null, expected, actual);
}
public static void assertEquals(String message, String expected, String actual) {
	if (expected == null && actual == null)
		return;
	if (expected != null && expected.equals(actual))
		return;
	final String formatted;
	if (message != null) {
		formatted = message+"."; //$NON-NLS-1$
	} else {
		formatted = ""; //$NON-NLS-1$
	}
	final String expectedWithLineSeparators = expected == null ? null : showLineSeparators(expected);
	final String actualWithLineSeparators = actual == null ? null : showLineSeparators(actual);
	
	throw new ComparisonFailure(null, expectedWithLineSeparators, actualWithLineSeparators) {
		String fExpected = expectedWithLineSeparators; // used only for the result compare utility
		String fActual = actualWithLineSeparators; // used only for the result compare utility
		public String getMessage() {
			return 
				formatted
					+ "\n----------- Expected ------------\n" //$NON-NLS-1$
					+ expectedWithLineSeparators
					+ "\n------------ but was ------------\n" //$NON-NLS-1$
					+ actualWithLineSeparators
					+ "\n--------- Difference is ----------\n" //$NON-NLS-1$
					+ super.getMessage();
		}
	};
}
/*
 * Shows the line separators in the given String.
 */
protected static String showLineSeparators(String string) {
	StringBuffer buffer = new StringBuffer();
	int length = string.length();
	for (int i = 0; i < length; i++) {
		char car = string.charAt(i);
		switch (car) {
			case '\n': 
				buffer.append("\\n\n"); //$NON-NLS-1$
				break;
			case '\r':
				if (i < length-1 && string.charAt(i+1) == '\n') {
					buffer.append("\\r\\n\n"); //$NON-NLS-1$
					i++;
				} else {
					buffer.append("\\r\n"); //$NON-NLS-1$
				}
				break;
			default:
				buffer.append(car);
				break;
		}
	}
	return buffer.toString();
}
}
