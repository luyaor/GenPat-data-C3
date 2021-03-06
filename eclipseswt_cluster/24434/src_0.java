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

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.*;

/**
 * Automated Test Suite for class org.eclipse.swt.events.TypedEvent
 *
 * @see org.eclipse.swt.events.TypedEvent
 */
public class Test_org_eclipse_swt_events_TypedEvent extends SwtTestCase {

public Test_org_eclipse_swt_events_TypedEvent(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
	shell = new Shell();
}

protected void tearDown() {
	shell.dispose();
}

public void test_ConstructorLjava_lang_Object() {
	warnUnimpl("Test test_ConstructorLjava_lang_Object not written");
}

public void test_ConstructorLorg_eclipse_swt_widgets_Event() {
	Event event = new Event();
	event.widget = shell;
	TypedEvent typedEvent = new TypedEvent(event);
	assertNotNull(typedEvent);
}

public void test_toString() {
	Event event = new Event();
	event.widget = shell;
	TypedEvent typedEvent = new TypedEvent(event);
	assertNotNull(typedEvent.toString());
	assertTrue(typedEvent.toString().length() > 0);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_events_TypedEvent((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLjava_lang_Object");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_Event");
	methodNames.addElement("test_toString");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLjava_lang_Object")) test_ConstructorLjava_lang_Object();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_Event")) test_ConstructorLorg_eclipse_swt_widgets_Event();
	else if (getName().equals("test_toString")) test_toString();
}

/* custom */
public Shell shell;
}
