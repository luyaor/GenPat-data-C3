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

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.accessibility.*;

/**
 * Automated Test Suite for class org.eclipse.swt.accessibility.AccessibleEvent
 *
 * @see org.eclipse.swt.accessibility.AccessibleEvent
 */
public class Test_org_eclipse_swt_accessibility_AccessibleEvent extends SwtTestCase {

public Test_org_eclipse_swt_accessibility_AccessibleEvent(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

@Override
protected void setUp() {
	shell = new Shell();
}

@Override
protected void tearDown() {
	shell.dispose();
}

public void test_ConstructorLjava_lang_Object() {
	// Object will typically be a widget.
	AccessibleEvent event = new AccessibleEvent(shell);
	assertNotNull(event);
	
	// Test with some other object also.
	event = new AccessibleEvent(new Integer(5));
	assertNotNull(event);
}

public void test_toString() {
	AccessibleEvent event = new AccessibleEvent(shell);
	assertNotNull(event.toString());
	assertTrue(event.toString().length() > 0);
}


public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector<String> methodNames = methodNames();
	java.util.Enumeration<String> e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_accessibility_AccessibleEvent(e.nextElement()));
	}
	return suite;
}

public static java.util.Vector<String> methodNames() {
	java.util.Vector<String> methodNames = new java.util.Vector<String>();
	methodNames.addElement("test_ConstructorLjava_lang_Object");
	methodNames.addElement("test_toString");
	return methodNames;
}

@Override
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLjava_lang_Object")) test_ConstructorLjava_lang_Object();
	else if (getName().equals("test_toString")) test_toString();
}

/* custom */
public Shell shell;
}
