package org.eclipse.swt.tests.junit;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import junit.framework.*;
import junit.textui.*;

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.Composite
 *
 * @see org.eclipse.swt.widgets.Composite
 */
public class Test_org_eclipse_swt_widgets_Composite extends Test_org_eclipse_swt_widgets_Scrollable {

Composite composite;

public Test_org_eclipse_swt_widgets_Composite(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
	super.setUp();
	composite = new Composite(shell, 0);
	super.setWidget(composite);
}

protected void tearDown() {
	super.tearDown();
}

protected void setWidget(Widget w) {
	if (composite != null)
		composite.dispose();
	composite = (Composite)w;
	super.setWidget(w);
}

public void test_ConstructorLorg_eclipse_swt_widgets_CompositeI() {
	try {
		composite = new Composite(null, 0);
		fail("No exception thrown");
	}
	catch (IllegalArgumentException e) {
	}

	int[] cases = {SWT.H_SCROLL, SWT.V_SCROLL, SWT.H_SCROLL | SWT.V_SCROLL};
	for (int i = 0; i < cases.length; i++)
		composite = new Composite(shell, cases[i]);
}

public void test_getChildren() {
	assertEquals(":a:", new Control[]{}, composite.getChildren());
	Composite c1 = new Composite(composite, 0);
	assertEquals(":b:", new Control[]{c1}, composite.getChildren());

	List c2 = new List(composite, 0);
	assertEquals(":c:", new Control[]{c1, c2}, composite.getChildren());

	Scale c3 = new Scale(composite, 0);
	assertEquals(":d:", new Control[]{c1, c2, c3}, composite.getChildren());

	c2.dispose();
	assertEquals(":e:", new Control[]{c1, c3}, composite.getChildren());
	
	Control[] children = composite.getChildren();
	for (int i = 0; i < children.length; i++)
		children[i].dispose();

	assertEquals(":f:", new Control[]{}, composite.getChildren());
}

public void test_getLayout() {
	// tested in test_setLayoutLorg_eclipse_swt_widgets_Layout
}

public void test_getTabList() {
	// tested in test_setTabList$Lorg_eclipse_swt_widgets_Control
}

public void test_layout() {
	// tested in test_layoutZ
}

public void test_layoutZ() {
	Layout[] layouts = {null, new FillLayout(), new RowLayout(), new GridLayout()};
	for (int i = 0; i < layouts.length; i++) {
		composite.setLayout(layouts[i]);
		composite.layout(false);
		composite.layout(true);
		composite.layout();
	}
}

public void test_setLayoutLorg_eclipse_swt_widgets_Layout() {
	Layout[] layouts = {null, new FillLayout(), new RowLayout(), new GridLayout()};
	for (int i = 0; i < layouts.length; i++) {
		composite.setLayout(layouts[i]);
		assertEquals(layouts[i], composite.getLayout());
	}
}

public void test_setTabList$Lorg_eclipse_swt_widgets_Control() {
	Button button1 = new Button(composite, SWT.PUSH);
	Button button2 = new Button(composite, SWT.PUSH);
	Control[] tablist = new Control[] {button1, button2};
	composite.setTabList(tablist);
	assertEquals(tablist, composite.getTabList());
	button1.dispose();
	button2.dispose();
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_widgets_Composite((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_CompositeI");
	methodNames.addElement("test_getChildren");
	methodNames.addElement("test_getLayout");
	methodNames.addElement("test_getTabList");
	methodNames.addElement("test_layout");
	methodNames.addElement("test_layoutZ");
	methodNames.addElement("test_setLayoutLorg_eclipse_swt_widgets_Layout");
	methodNames.addElement("test_setTabList$Lorg_eclipse_swt_widgets_Control");
	methodNames.addAll(Test_org_eclipse_swt_widgets_Scrollable.methodNames()); // add superclass method names
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_CompositeI")) test_ConstructorLorg_eclipse_swt_widgets_CompositeI();
	else if (getName().equals("test_getChildren")) test_getChildren();
	else if (getName().equals("test_getLayout")) test_getLayout();
	else if (getName().equals("test_getTabList")) test_getTabList();
	else if (getName().equals("test_layout")) test_layout();
	else if (getName().equals("test_layoutZ")) test_layoutZ();
	else if (getName().equals("test_setLayoutLorg_eclipse_swt_widgets_Layout")) test_setLayoutLorg_eclipse_swt_widgets_Layout();
	else if (getName().equals("test_setTabList$Lorg_eclipse_swt_widgets_Control")) test_setTabList$Lorg_eclipse_swt_widgets_Control();
	else super.runTest();
}
}
