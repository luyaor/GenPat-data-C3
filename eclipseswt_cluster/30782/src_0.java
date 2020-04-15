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
package org.eclipse.swt.tests.junit.performance;

import junit.framework.*;
import junit.textui.*;
import org.eclipse.swt.graphics.*;

/**
 * Automated Performance Test Suite for class org.eclipse.swt.graphics.RGB
 *
 * @see org.eclipse.swt.graphics.RGB
 */
public class Test_org_eclipse_swt_graphics_RGB extends SwtPerformanceTestCase {
	static final int COUNT = 10000;

public Test_org_eclipse_swt_graphics_RGB(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

public void test_ConstructorIII() {
	startMeasuring();
	for (int i = 0; i < COUNT; i++) {
		new RGB(50,150,250);
	}
	stopMeasuring();
	
	commitMeasurements();
	assertPerformance();
}

public void test_equalsLjava_lang_Object() {
	RGB rgb1 = new RGB (0, 128, 255);
	RGB rgb2 = new RGB (0, 128, 255);
	
	startMeasuring();
	for (int i = 0; i < COUNT; i++) {
		rgb1.equals(rgb2);	// same
	}
	stopMeasuring();
	
	commitMeasurements();
	assertPerformance();
	
	rgb2 = new RGB (128, 255, 0);
	
	startMeasuring();
	for (int i = 0; i < COUNT; i++) {
		rgb1.equals(rgb2);	// different
	}
	stopMeasuring();
	
	commitMeasurements();
	assertPerformance();
}

public void test_hashCode() {
	RGB rgb = new RGB (0, 128, 255);
	
	startMeasuring();
	for (int i = 0; i < COUNT; i++) {
		rgb.hashCode();
	}
	stopMeasuring();
	
	commitMeasurements();
	assertPerformance();
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_graphics_RGB((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorIII");
	methodNames.addElement("test_equalsLjava_lang_Object");
	methodNames.addElement("test_hashCode");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorIII")) test_ConstructorIII();
	else if (getName().equals("test_equalsLjava_lang_Object")) test_equalsLjava_lang_Object();
	else if (getName().equals("test_hashCode")) test_hashCode();
}
}
