/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.junit;

import junit.framework.*;
import junit.textui.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;

/**
 * Automated Test Suite for class org.eclipse.swt.browser.VisibilityWindowListener
 *
 * @see org.eclipse.swt.browser.VisibilityWindowListener
 */
public class Test_org_eclipse_swt_browser_VisibilityWindowListener extends SwtTestCase {

public Test_org_eclipse_swt_browser_VisibilityWindowListener(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
}

protected void tearDown() {
}

public void test_hideLorg_eclipse_swt_browser_VisibilityWindowEvent() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addVisibilityWindowListener(new VisibilityWindowListener() {
		public void hide(VisibilityWindowEvent event) {
		}
		public void show(VisibilityWindowEvent event) {
		}
	});
	shell.close();
}

public void test_showLorg_eclipse_swt_browser_VisibilityWindowEvent() {
	// tested in test_hideLorg_eclipse_swt_browser_VisibilityWindowEvent
}


public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_browser_VisibilityWindowListener((String)e.nextElement()));
	}
	return suite;
}

public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_hideLorg_eclipse_swt_browser_VisibilityWindowEvent");
	methodNames.addElement("test_showLorg_eclipse_swt_browser_VisibilityWindowEvent");
	return methodNames;
}

protected void runTest() throws Throwable {
	if (getName().equals("test_hideLorg_eclipse_swt_browser_VisibilityWindowEvent")) test_hideLorg_eclipse_swt_browser_VisibilityWindowEvent();
	else if (getName().equals("test_showLorg_eclipse_swt_browser_VisibilityWindowEvent")) test_showLorg_eclipse_swt_browser_VisibilityWindowEvent();
}
}
