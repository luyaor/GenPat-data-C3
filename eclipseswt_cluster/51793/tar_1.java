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


import org.eclipse.swt.*;
import org.eclipse.swt.tests.junit.SwtJunit;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.test.performance.PerformanceMeter;

import junit.framework.*;
import junit.textui.*;

/**
 * Automated Performance Test Suite for class org.eclipse.swt.graphics.Font
 *
 * @see org.eclipse.swt.graphics.Font
 */
public class Test_org_eclipse_swt_graphics_Font extends SwtPerformanceTestCase {
	Display display;

public Test_org_eclipse_swt_graphics_Font(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() throws Exception {
	super.setUp();
	display = Display.getDefault();
}

public void test_ConstructorLorg_eclipse_swt_graphics_Device$Lorg_eclipse_swt_graphics_FontData() {
	final int COUNT = 10000;	// 20000 causes No More Handles
	
	FontData[] data = new FontData [1];
	data[0] = new FontData (SwtJunit.testFontName, 10, SWT.BOLD | SWT.ITALIC); 
	Font[] fonts = new Font[COUNT];
	
	PerformanceMeter meter = createMeter();
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		fonts[i] = new Font(display, data);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
}

public void test_ConstructorLorg_eclipse_swt_graphics_DeviceLorg_eclipse_swt_graphics_FontData() {
	final int COUNT = 10000;	// 20000 causes No More Handles
	
	FontData data = new FontData (SwtJunit.testFontName, 10, SWT.BOLD | SWT.ITALIC);
	Font[] fonts = new Font[COUNT];
	
	PerformanceMeter meter = createMeter();
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		fonts[i] = new Font(display, data);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
}

public void test_ConstructorLorg_eclipse_swt_graphics_DeviceLjava_lang_StringII() {
	final int COUNT = 10000;	// 20000 causes No More Handles
	
	Font[] fonts = new Font[COUNT];
	
	PerformanceMeter meter = createMeter("no name");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid font with no name
		fonts[i] = new Font(display, "", 10, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
		
	meter = createMeter("unknown name");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid font with unknown name
		fonts[i] = new Font(display, "bad-font", 10, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
	
	meter = createMeter("0 height");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid font with 0 height
		fonts[i] = new Font(display, SwtJunit.testFontName, 0, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);

	meter = createMeter("10 pt");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid normal 10-point font
		fonts[i] = new Font(display, SwtJunit.testFontName, 10, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);

	meter = createMeter("100 pt");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid normal 100-point font
		fonts[i] = new Font(display, SwtJunit.testFontName, 100, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
	
	meter = createMeter("bold italic");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// valid bold italic 10-point font
		fonts[i] = new Font(display, SwtJunit.testFontName, 10, SWT.BOLD | SWT.ITALIC);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);

	meter = createMeter("null device");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		// device == null (valid)
		fonts[i] = new Font(null, SwtJunit.testFontName, 10, SWT.NORMAL);
	}
	meter.stop();

	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();
	}

	disposeMeter(meter);
}

public void test_dispose() {
	final int COUNT = 10000;	// 20000 causes No More Handles
	
	Font[] fonts = new Font [COUNT];
	for (int i = 0; i < COUNT; i++) {
		fonts[i] = new Font(display, SwtJunit.testFontName, 10, SWT.NORMAL);
	}
	
	PerformanceMeter meter = createMeter("not disposed");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();	// dispose
	}
	meter.stop();
	
    disposeMeter(meter);
    
	meter = createMeter("disposed");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		fonts[i].dispose();	// dispose disposed
	}
	meter.stop();
	
    disposeMeter(meter);
}

public void test_equalsLjava_lang_Object() {
	final int COUNT = 60000000;
	
	// Fonts are only equal if their handles are the same
	Font font = new Font(display, SwtJunit.testFontName, 10, SWT.NORMAL);

	PerformanceMeter meter = createMeter("equal");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.equals(font);	// same
	}
	meter.stop();

	font.dispose();
	
	disposeMeter(meter);
	
	font = new Font(display, SwtJunit.testFontName, 10, SWT.NORMAL);
	Font otherFont = new Font(display, SwtJunit.testFontName, 20, SWT.NORMAL);

	meter = createMeter("not equal");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.equals(otherFont);	// different
	}
	meter.stop();

	font.dispose();
	otherFont.dispose();
	
	disposeMeter(meter);
}

public void test_getFontData() {
	final int COUNT = 300000;
	
	Font font = new Font(display, SwtJunit.testFontName, 40, SWT.BOLD | SWT.ITALIC);
	
	PerformanceMeter meter = createMeter();
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.getFontData();
	}
	meter.stop();
	
	font.dispose();
	
	disposeMeter(meter);
}

public void test_hashCode() {
	final int COUNT = 600000000;
	
	Font font = new Font(display, SwtJunit.testFontName, 40, SWT.BOLD | SWT.ITALIC);
	
	PerformanceMeter meter = createMeter();
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.hashCode();
	}
	meter.stop();
	
	font.dispose();
	
	disposeMeter(meter);
}

public void test_isDisposed() {
	final int COUNT = 500000000;
	
	Font font = new Font(display, SwtJunit.testFontName, 10, SWT.NORMAL);
	
	PerformanceMeter meter = createMeter("not disposed");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.isDisposed();	// not disposed
	}
	meter.stop();
	
	font.dispose();
	
	disposeMeter(meter);
	
	meter = createMeter("disposed");
	meter.start();
	for (int i = 0; i < COUNT; i++) {
		font.isDisposed();	// disposed
	}
	meter.stop();
	
	disposeMeter(meter);
}

public void test_win32_newLorg_eclipse_swt_graphics_DeviceI() {
	// do not test - Windows only
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_graphics_Font((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_graphics_Device$Lorg_eclipse_swt_graphics_FontData");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_graphics_DeviceLorg_eclipse_swt_graphics_FontData");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_graphics_DeviceLjava_lang_StringII");
	methodNames.addElement("test_dispose");
	methodNames.addElement("test_equalsLjava_lang_Object");
	methodNames.addElement("test_getFontData");
	methodNames.addElement("test_hashCode");
	methodNames.addElement("test_isDisposed");
	methodNames.addElement("test_win32_newLorg_eclipse_swt_graphics_DeviceI");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLorg_eclipse_swt_graphics_Device$Lorg_eclipse_swt_graphics_FontData")) test_ConstructorLorg_eclipse_swt_graphics_Device$Lorg_eclipse_swt_graphics_FontData();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_graphics_DeviceLorg_eclipse_swt_graphics_FontData")) test_ConstructorLorg_eclipse_swt_graphics_DeviceLorg_eclipse_swt_graphics_FontData();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_graphics_DeviceLjava_lang_StringII")) test_ConstructorLorg_eclipse_swt_graphics_DeviceLjava_lang_StringII();
	else if (getName().equals("test_dispose")) test_dispose();
	else if (getName().equals("test_equalsLjava_lang_Object")) test_equalsLjava_lang_Object();
	else if (getName().equals("test_getFontData")) test_getFontData();
	else if (getName().equals("test_hashCode")) test_hashCode();
	else if (getName().equals("test_isDisposed")) test_isDisposed();
	else if (getName().equals("test_win32_newLorg_eclipse_swt_graphics_DeviceI")) test_win32_newLorg_eclipse_swt_graphics_DeviceI();
}
}
