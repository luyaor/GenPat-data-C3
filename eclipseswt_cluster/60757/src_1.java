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
package org.eclipse.swt.tests.junit;

import junit.framework.*;
import junit.textui.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.ToolBar
 *
 * @see org.eclipse.swt.widgets.ToolBar
 */
public class Test_org_eclipse_swt_widgets_ToolBar extends Test_org_eclipse_swt_widgets_Composite {

public Test_org_eclipse_swt_widgets_ToolBar(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
	super.setUp();
	toolBar = new ToolBar(shell, 0);
	setWidget(toolBar);
}

protected void tearDown() {
	super.tearDown();
}

public void test_ConstructorLorg_eclipse_swt_widgets_CompositeI() {
	try {
		new ToolBar(null, 0);
		fail("No exception thrown for parent == null");
	}
	catch (IllegalArgumentException e) {
	}
}

public void test_computeSizeIIZ() {
	warnUnimpl("Test test_computeSizeIIZ not written");
}

public void test_computeTrimIIII() {
	warnUnimpl("Test test_computeTrimIIII not written");
}

public void test_getItemCount() {
	int number = 10;
	ToolItem ti;
	for (int i = 0; i<number ; i++){
		assertTrue(":a:" + i, toolBar.getItemCount()==i);
	  	ti = new ToolItem(toolBar, 0);
	}
}

public void test_getItemI() {
	int number = 5;
	ToolItem[] items = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	items[i] = new ToolItem(toolBar, 0);
	}
	for (int i = 0; i<number ; i++){
		assertTrue(":a:", toolBar.getItem(i)==items[i]);
	}

	toolBar = new ToolBar(shell, 0);
	number = 5;
	items = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	items[i] = new ToolItem(toolBar, 0);
	}
	try {
		toolBar.getItem(number);
		fail("No exception thrown for illegal index argument");
	}
	catch (IllegalArgumentException e) {
	}
}

public void test_getItemLorg_eclipse_swt_graphics_Point() {
	warnUnimpl("Test test_getItemLorg_eclipse_swt_graphics_Point not written");
}

public void test_getItems() {
	int number = 5;
	ToolItem[] items = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	items[i] = new ToolItem(toolBar, 0);
	}
	assertEquals(items, toolBar.getItems());
	
	toolBar.getItems()[0].dispose();
	assertEquals(new ToolItem[]{items[1], items[2], items[3], items[4]}, toolBar.getItems());

	toolBar.getItems()[3].dispose();
	assertEquals(new ToolItem[]{items[1], items[2], items[3]}, toolBar.getItems());

	toolBar.getItems()[1].dispose();
	assertEquals(new ToolItem[]{items[1], items[3]}, toolBar.getItems());
}

public void test_getRowCount() {
	toolBar = new ToolBar(shell, SWT.WRAP);
	int number = 5;
	ToolItem[] items = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	items[i] = new ToolItem(toolBar, 0);
	}
	assertTrue(":a:" + toolBar.getRowCount(), toolBar.getRowCount()==number);  //????  because of Size(0, 0)

	toolBar = new ToolBar(shell, 0);
	number = 5;
	items = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	items[i] = new ToolItem(toolBar, 0);
	}
	assertTrue(":a:", toolBar.getRowCount()==1);
}

public void test_indexOfLorg_eclipse_swt_widgets_ToolItem() {
	int number = 10;
	ToolItem[] tis = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	tis[i] = new ToolItem(toolBar, 0);
	}
	for (int i = 0; i<number ; i++){
		assertTrue(":a:" + i, toolBar.indexOf(tis[i])==i);
	}

	number = 10;
	tis = new ToolItem[number];
	for (int i = 0; i<number ; i++){
	  	tis[i] = new ToolItem(toolBar, 0);
	}
	for (int i = 0; i<number ; i++){
		try {
			toolBar.indexOf(null);		
			fail("No exception thrown for toolItem == null");
		}
		catch (IllegalArgumentException e) {
		}
	}
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_widgets_ToolBar((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_CompositeI");
	methodNames.addElement("test_computeSizeIIZ");
	methodNames.addElement("test_computeTrimIIII");
	methodNames.addElement("test_getItemCount");
	methodNames.addElement("test_getItemI");
	methodNames.addElement("test_getItemLorg_eclipse_swt_graphics_Point");
	methodNames.addElement("test_getItems");
	methodNames.addElement("test_getRowCount");
	methodNames.addElement("test_indexOfLorg_eclipse_swt_widgets_ToolItem");
	methodNames.addAll(Test_org_eclipse_swt_widgets_Composite.methodNames()); // add superclass method names
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_CompositeI")) test_ConstructorLorg_eclipse_swt_widgets_CompositeI();
	else if (getName().equals("test_computeSizeIIZ")) test_computeSizeIIZ();
	else if (getName().equals("test_computeTrimIIII")) test_computeTrimIIII();
	else if (getName().equals("test_getItemCount")) test_getItemCount();
	else if (getName().equals("test_getItemI")) test_getItemI();
	else if (getName().equals("test_getItemLorg_eclipse_swt_graphics_Point")) test_getItemLorg_eclipse_swt_graphics_Point();
	else if (getName().equals("test_getItems")) test_getItems();
	else if (getName().equals("test_getRowCount")) test_getRowCount();
	else if (getName().equals("test_indexOfLorg_eclipse_swt_widgets_ToolItem")) test_indexOfLorg_eclipse_swt_widgets_ToolItem();
	else super.runTest();
}

/* custom */
protected ToolBar toolBar;
}
