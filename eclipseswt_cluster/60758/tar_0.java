/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.junit;

import junit.framework.*;
import junit.textui.*;

/**
 * Automated Test Suite for class org.eclipse.swt.events.ShellAdapter
 *
 * @see org.eclipse.swt.events.ShellAdapter
 */
public class Test_org_eclipse_swt_events_ShellAdapter extends SwtTestCase {

public Test_org_eclipse_swt_events_ShellAdapter(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

public void test_Constructor() {
	warnUnimpl("Test test_Constructor not written");
}

public void test_shellActivatedLorg_eclipse_swt_events_ShellEvent() {
	warnUnimpl("Test test_shellActivatedLorg_eclipse_swt_events_ShellEvent not written");
}

public void test_shellClosedLorg_eclipse_swt_events_ShellEvent() {
	warnUnimpl("Test test_shellClosedLorg_eclipse_swt_events_ShellEvent not written");
}

public void test_shellDeactivatedLorg_eclipse_swt_events_ShellEvent() {
	warnUnimpl("Test test_shellDeactivatedLorg_eclipse_swt_events_ShellEvent not written");
}

public void test_shellDeiconifiedLorg_eclipse_swt_events_ShellEvent() {
	warnUnimpl("Test test_shellDeiconifiedLorg_eclipse_swt_events_ShellEvent not written");
}

public void test_shellIconifiedLorg_eclipse_swt_events_ShellEvent() {
	warnUnimpl("Test test_shellIconifiedLorg_eclipse_swt_events_ShellEvent not written");
}


public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector<String> methodNames = methodNames();
	java.util.Enumeration<String> e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_events_ShellAdapter(e.nextElement()));
	}
	return suite;
}

public static java.util.Vector<String> methodNames() {
	java.util.Vector<String> methodNames = new java.util.Vector<String>();
	methodNames.addElement("test_Constructor");
	methodNames.addElement("test_shellActivatedLorg_eclipse_swt_events_ShellEvent");
	methodNames.addElement("test_shellClosedLorg_eclipse_swt_events_ShellEvent");
	methodNames.addElement("test_shellDeactivatedLorg_eclipse_swt_events_ShellEvent");
	methodNames.addElement("test_shellDeiconifiedLorg_eclipse_swt_events_ShellEvent");
	methodNames.addElement("test_shellIconifiedLorg_eclipse_swt_events_ShellEvent");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_Constructor")) test_Constructor();
	else if (getName().equals("test_shellActivatedLorg_eclipse_swt_events_ShellEvent")) test_shellActivatedLorg_eclipse_swt_events_ShellEvent();
	else if (getName().equals("test_shellClosedLorg_eclipse_swt_events_ShellEvent")) test_shellClosedLorg_eclipse_swt_events_ShellEvent();
	else if (getName().equals("test_shellDeactivatedLorg_eclipse_swt_events_ShellEvent")) test_shellDeactivatedLorg_eclipse_swt_events_ShellEvent();
	else if (getName().equals("test_shellDeiconifiedLorg_eclipse_swt_events_ShellEvent")) test_shellDeiconifiedLorg_eclipse_swt_events_ShellEvent();
	else if (getName().equals("test_shellIconifiedLorg_eclipse_swt_events_ShellEvent")) test_shellIconifiedLorg_eclipse_swt_events_ShellEvent();
}
}
