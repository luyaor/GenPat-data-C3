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
 * Automated Test Suite for class org.eclipse.swt.dnd.Clipboard
 *
 * @see org.eclipse.swt.dnd.Clipboard
 */
public class Test_org_eclipse_swt_dnd_Clipboard extends SwtTestCase {

public Test_org_eclipse_swt_dnd_Clipboard(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
}

protected void tearDown() {
}

public void test_ConstructorLorg_eclipse_swt_widgets_Display() {
	warnUnimpl("Test test_ConstructorLorg_eclipse_swt_widgets_Display not written");
}

public void test_checkSubclass() {
	warnUnimpl("Test test_checkSubclass not written");
}

public void test_dispose() {
	warnUnimpl("Test test_dispose not written");
}

public void test_getContentsLorg_eclipse_swt_dnd_Transfer() {
	warnUnimpl("Test test_getContentsLorg_eclipse_swt_dnd_Transfer not written");
}

public void test_setContents$Ljava_lang_Object$Lorg_eclipse_swt_dnd_Transfer() {
	warnUnimpl("Test test_setContents$Ljava_lang_Object$Lorg_eclipse_swt_dnd_Transfer not written");
}

public void test_getAvailableTypeNames() {
	warnUnimpl("Test test_getAvailableTypeNames not written");
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_dnd_Clipboard((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_Display");
	methodNames.addElement("test_checkSubclass");
	methodNames.addElement("test_dispose");
	methodNames.addElement("test_getContentsLorg_eclipse_swt_dnd_Transfer");
	methodNames.addElement("test_setContents$Ljava_lang_Object$Lorg_eclipse_swt_dnd_Transfer");
	methodNames.addElement("test_getAvailableTypeNames");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_Display")) test_ConstructorLorg_eclipse_swt_widgets_Display();
	else if (getName().equals("test_checkSubclass")) test_checkSubclass();
	else if (getName().equals("test_dispose")) test_dispose();
	else if (getName().equals("test_getContentsLorg_eclipse_swt_dnd_Transfer")) test_getContentsLorg_eclipse_swt_dnd_Transfer();
	else if (getName().equals("test_setContents$Ljava_lang_Object$Lorg_eclipse_swt_dnd_Transfer")) test_setContents$Ljava_lang_Object$Lorg_eclipse_swt_dnd_Transfer();
	else if (getName().equals("test_getAvailableTypeNames")) test_getAvailableTypeNames();
}
}
