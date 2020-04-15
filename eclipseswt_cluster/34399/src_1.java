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

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.TypedListener
 *
 * @see org.eclipse.swt.widgets.TypedListener
 */
public class Test_org_eclipse_swt_widgets_TypedListener extends SwtTestCase {

public Test_org_eclipse_swt_widgets_TypedListener(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
}

protected void tearDown() {
}

public void test_ConstructorLorg_eclipse_swt_internal_SWTEventListener() {
	warnUnimpl("Test test_ConstructorLorg_eclipse_swt_internal_SWTEventListener not written");
}

public void test_getEventListener() {
	warnUnimpl("Test test_getEventListener not written");
}

public void test_handleEventLorg_eclipse_swt_widgets_Event() {
	warnUnimpl("Test test_handleEventLorg_eclipse_swt_widgets_Event not written");
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_widgets_TypedListener((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_internal_SWTEventListener");
	methodNames.addElement("test_getEventListener");
	methodNames.addElement("test_handleEventLorg_eclipse_swt_widgets_Event");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLorg_eclipse_swt_internal_SWTEventListener")) test_ConstructorLorg_eclipse_swt_internal_SWTEventListener();
	else if (getName().equals("test_getEventListener")) test_getEventListener();
	else if (getName().equals("test_handleEventLorg_eclipse_swt_widgets_Event")) test_handleEventLorg_eclipse_swt_widgets_Event();
}
}
