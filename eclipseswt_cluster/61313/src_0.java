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


import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import junit.framework.*;
import junit.textui.*;

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.Shell
 *
 * @see org.eclipse.swt.widgets.Shell
 */
public class Test_org_eclipse_swt_widgets_Shell extends Test_org_eclipse_swt_widgets_Decorations {

Shell testShell;

public Test_org_eclipse_swt_widgets_Shell(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
	super.setUp();
	testShell = new Shell(shell, SWT.NULL);
	setWidget(shell);
	assertTrue(testShell.getParent() == shell);
}

protected void tearDown() {
	super.tearDown();
}

public void test_Constructor() {
	warnUnimpl("Test test_Constructor not written");
}

public void test_ConstructorI(){
	/* this should test various combinations of STYLE bits, for now just test individual bits */
	int[] cases = {SWT.NO_TRIM, SWT.RESIZE, SWT.TITLE, SWT.CLOSE, SWT.MENU, SWT.MIN, SWT.BORDER, 
				   SWT.CLIP_CHILDREN, SWT.CLIP_SIBLINGS, SWT.ON_TOP, SWT.FLAT, SWT.SMOOTH};
	Shell newShell;
	for (int i = 0; i < cases.length; i++) {
		newShell = new Shell(cases[i]);
		assertTrue("a " +i, newShell.getDisplay() == shell.getDisplay());
		newShell.dispose();
	}
}

public void test_ConstructorLorg_eclipse_swt_widgets_Display(){
	Display display = shell.getDisplay();
	Shell newShell = new Shell(display);
	assertTrue("a: ", newShell.getDisplay() == display);
	newShell.dispose();
}

public void test_ConstructorLorg_eclipse_swt_widgets_DisplayI(){
	int[] cases = {SWT.NO_TRIM, SWT.RESIZE, SWT.TITLE, SWT.CLOSE, SWT.MENU, SWT.MIN, SWT.BORDER, 
				   SWT.CLIP_CHILDREN, SWT.CLIP_SIBLINGS, SWT.ON_TOP, SWT.FLAT, SWT.SMOOTH};
	Shell newShell;
	Display display = shell.getDisplay();
	for (int i = 0; i < cases.length; i++) {
		newShell = new Shell(display, cases[i]);
		assertTrue("a " +i, newShell.getDisplay() == shell.getDisplay());
		newShell.dispose();
	}
}

public void test_ConstructorLorg_eclipse_swt_widgets_Shell(){
	Shell newShell = new Shell(shell);
	assertTrue("a: ", newShell.getParent() == shell);
	newShell.dispose();
}

public void test_ConstructorLorg_eclipse_swt_widgets_ShellI(){
	/* this should test various combinations of STYLE bits, for now just test individual bits */
	int[] cases = {SWT.NO_TRIM, SWT.RESIZE, SWT.TITLE, SWT.CLOSE, SWT.MENU, SWT.MIN, SWT.BORDER, 
				   SWT.CLIP_CHILDREN, SWT.CLIP_SIBLINGS, SWT.ON_TOP, SWT.FLAT, SWT.SMOOTH};
	Shell newShell;
	for (int i = 0; i < cases.length; i++) {
		newShell = new Shell(shell, cases[i]);
		assertTrue("a " +i, newShell.getParent() == shell);
		newShell.dispose();
	}
}

public void test_win32_newLorg_eclipse_swt_widgets_DisplayI() {
	// do not test - Windows only
}

public void test_addShellListenerLorg_eclipse_swt_events_ShellListener() {
	warnUnimpl("Test test_addShellListenerLorg_eclipse_swt_events_ShellListener not written");
}

public void test_close(){

	// bogus line that 'enabled' gpfs
	//	Shell newShell = new Shell();
	testShell.setBounds(20,30,200, 200);
	testShell.open();
	testShell.close();
	shell.setBounds(20,30,200, 200);
	shell.open();
}

public void test_dispose(){
	Shell newShell = new Shell();
	newShell.dispose();
}

public void test_getBounds() {
	// tested in test_setBoundsIIII and test_setBoundsLorg_eclipse_swt_graphics_Rectangle
}

public void test_getDisplay(){
	assertTrue( shell.getDisplay() == testShell.getDisplay());
}

public void test_getEnabled(){
	assertTrue(":a0:", shell.getEnabled());
	shell.setEnabled(false);
	assertTrue(":a:", !shell.getEnabled());
	shell.setEnabled(true);
	assertTrue(":b:", shell.getEnabled());
}

public void test_getImeInputMode() {
	warnUnimpl("Test test_getImeInputMode not written");
}

public void test_getLocation() {
	warnUnimpl("Test test_getLocation not written");
}

public void test_getParent () {
	// overriding Control.test_getParent
	assertTrue(shell.getParent()==null);
	assertTrue(testShell.getParent() == shell);
}

public void test_getShell () {
	assertTrue(":a:", shell.getShell()==shell);
	Shell shell_1 = new Shell(shell);
	assertTrue(":b:", shell_1.getShell()== shell_1);
	shell_1.dispose();
}

public void test_getShells() {
	warnUnimpl("Test test_getShells not written");
}

public void test_getStyle() {
	// overriding Widget.test_getStyle
	assertTrue("testShell not modeless", (testShell.getStyle () & SWT.MODELESS) == SWT.MODELESS);
	int[] cases = {SWT.MODELESS, SWT.PRIMARY_MODAL, SWT.APPLICATION_MODAL, SWT.SYSTEM_MODAL};
	for (int i = 0; i < cases.length; i++) {
		Shell testShell2 = new Shell(shell, cases[i]);
		assertTrue("shell " + i, (testShell2.getStyle () & cases[i]) == cases[i]);
		testShell2.dispose();
	}
}

public void test_isEnabled(){
	assertTrue(":a:", shell.isEnabled());
	shell.setEnabled(false);
	assertTrue(":b:", !shell.isEnabled());
	if (fCheckBogusTestCases)
		assertTrue(":b1:", !testShell.isEnabled());
	shell.setEnabled(true);
	assertTrue(":c:", shell.isEnabled());
	assertTrue(":a:", testShell.isEnabled());
	testShell.setEnabled(false);
	assertTrue(":b:", !testShell.isEnabled());
	testShell.setEnabled(true);
	assertTrue(":c:", testShell.isEnabled());
}

public void test_isVisible() {
	// overriding Control.test_isVisible
	testShell.setVisible(true);
	assertTrue(testShell.isVisible());
	shell.setVisible(true);
	assertTrue(shell.isVisible());

	testShell.setVisible(true);
	shell.setVisible(true);
	assertTrue("shell.isVisible() a:", shell.isVisible());
	shell.setVisible(false);
	assertTrue("shell.isVisible() b:", !shell.isVisible());
	if (fCheckBogusTestCases)
		assertTrue("testShell.isVisible() c:", !testShell.isVisible());
}

public void test_open(){
	shell.open();
}

public void test_removeShellListenerLorg_eclipse_swt_events_ShellListener() {
	warnUnimpl("Test test_removeShellListenerLorg_eclipse_swt_events_ShellListener not written");
}

public void test_setBoundsIIII() {
	// overridden from Control because Shells have a minimum size
}

public void test_setBoundsLorg_eclipse_swt_graphics_Rectangle() {
	// overridden from Control because Shells have a minimum size
//	/* windows */
//	/* note that there is a minimum size for a shell, this test will fail if p1.x < 112 or p1.y < 27 */
//	/* note that there is a maximum size for a shell, this test will fail if p1.x > 1292 or p1.y > 1036 */
//	if (SwtJunit.isWindows) {
//		Point p1 = new Point(112, 27);
//		Rectangle r1 = new Rectangle(20, 30, p1.x, p1.y);
//		Rectangle r2;
//		for (int i = 0; i < 11; i++) {
//			testShell.setBounds(r1);
//			r2 = testShell.getBounds();
//			assert("child shell iteration " + i + " set=" + r1 + " get=" + r2, r1.equals(r2));
//			r1.width += 100;
//			r1.height += 100;
//		}
//		r1 = new Rectangle(20, 30, p1.x, p1.y);
//		for (int i = 0; i < 11; i++) {
//			shell.setBounds(r1);
//			r2 = shell.getBounds();
//			assert("parent shell iteration " + i + " set=" + r1 + " get=" + r2, r1.equals(r2));
//			r1.width += 100;
//			r1.height += 100;
//		}
//	}
//	/* motif */
//	/* note that there is a minimum size for a shell, this test will fail if p1.x < 112 or p1.y < 27 */
//	/* note that there is a maximum size for a shell, this test will fail if p1.x > 1292 or p1.y > 1036 */
//	if (SwtJunit.isMotif) {
//		Point p1 = new Point(15,35);
//		Rectangle r1 = new Rectangle(20, 30, p1.x, p1.y);
//		Rectangle r2;
//		
//		for (int i = 0; i < 15; i++) {
//			testShell.setBounds(r1);
//			r2 = testShell.getBounds();
//			assert("child shell iteration " + i + " set=" + r1 + " get=" + r2, r1.equals(r2));
//			r1.width += 100;
//			r1.height += 100;
//		}
//		r1 = new Rectangle(50, 50, p1.x, p1.y);
//		for (int i = 0; i < 11; i++) {
//			shell.setBounds(r1);
//			r2 = shell.getBounds();
//			assert("parent shell iteration " + i + " set=" + r1 + " get=" + r2, r1.equals(r2));
//			r1.width += 100;
//			r1.height += 100;
//		}
//	}
}

public void test_setEnabledZ() {
	warnUnimpl("Test test_setEnabledZ not written");
}

public void test_setImeInputModeI() {
	warnUnimpl("Test test_setImeInputModeI not written");
}

public void test_setSizeII() {
	/* windows */
	/* note that there is a minimum size for a shell, this test will fail if p1.x < 112 or p1.y < 27 */
	/* note that there is a maximum size for a shell, this test will fail if p1.x > 1292 or p1.y > 1036 */
	if (SwtJunit.isWindows) {
		Point newSize = new Point(112, 27);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize.x, newSize.y);
			assertEquals(newSize, testShell.getSize());
			newSize.x += 100;
			newSize.y += 100;
		}
		newSize = new Point(1292, 1036);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize.x, newSize.y);
			assertEquals(newSize, testShell.getSize());
			newSize.x -= 100;
			newSize.y -= 100;
		}
	}
	
	/* motif */
	/* note that there is a minimum size for a shell, this test will fail if p1.x < ?? or p1.y < ?? */
	/* note that there is a maximum size for a shell, this test will fail if p1.x > ?? or p1.y > ?? */
	if (SwtJunit.isMotif) {
		Point newSize = new Point(2, 2);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize.x, newSize.y);
			assertEquals(newSize, testShell.getSize());
			newSize.x += 100;
			newSize.y += 100;
		}
		newSize = new Point(1600, 1600);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize.x, newSize.y);
			assertEquals(newSize, testShell.getSize());
			newSize.x -= 100;
			newSize.y -= 100;
		}
	}
}

public void test_setSizeLorg_eclipse_swt_graphics_Point() {
	/* windows */
	/* note that there is a minimum size for a shell, this test will fail if p1.x < 112 or p1.y < 27 */
	/* note that there is a maximum size for a shell, this test will fail if p1.x > 1292 or p1.y > 1036 */
	if (SwtJunit.isWindows) {
		Point newSize = new Point(112, 27);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize);
			assertEquals(newSize, testShell.getSize());
			newSize.x += 100;
			newSize.y += 100;
		}
		newSize = new Point(1292, 1036);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize);
			assertEquals(newSize, testShell.getSize());
			newSize.x -= 100;
			newSize.y -= 100;
		}
	}
	
	/* motif */
	/* note that there is a minimum size for a shell, this test will fail if p1.x < ?? or p1.y < ?? */
	/* note that there is a maximum size for a shell, this test will fail if p1.x > ?? or p1.y > ?? */
	if (SwtJunit.isMotif) {
		Point newSize = new Point(2, 2);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize);
			assertEquals(newSize, testShell.getSize());
			newSize.x += 100;
			newSize.y += 100;
		}
		newSize = new Point(1600, 1600);
		for (int i = 0; i < 10; i++) {
			testShell.setSize(newSize);
			assertEquals(newSize, testShell.getSize());
			newSize.x -= 100;
			newSize.y -= 100;
		}
	}
}

public void test_setVisibleZ() {
	warnUnimpl("Test test_setVisibleZ not written");
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_widgets_Shell((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_Constructor");
	methodNames.addElement("test_ConstructorI");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_Display");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_DisplayI");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_Shell");
	methodNames.addElement("test_ConstructorLorg_eclipse_swt_widgets_ShellI");
	methodNames.addElement("test_win32_newLorg_eclipse_swt_widgets_DisplayI");
	methodNames.addElement("test_addShellListenerLorg_eclipse_swt_events_ShellListener");
	methodNames.addElement("test_close");
	methodNames.addElement("test_dispose");
	methodNames.addElement("test_getBounds");
	methodNames.addElement("test_getDisplay");
	methodNames.addElement("test_getEnabled");
	methodNames.addElement("test_getImeInputMode");
	methodNames.addElement("test_getLocation");
	methodNames.addElement("test_getShell");
	methodNames.addElement("test_getShells");
	methodNames.addElement("test_isEnabled");
	methodNames.addElement("test_open");
	methodNames.addElement("test_removeShellListenerLorg_eclipse_swt_events_ShellListener");
	methodNames.addElement("test_setEnabledZ");
	methodNames.addElement("test_setImeInputModeI");
	methodNames.addElement("test_setVisibleZ");
	methodNames.addAll(Test_org_eclipse_swt_widgets_Decorations.methodNames()); // add superclass method names
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_Constructor")) test_Constructor();
	else if (getName().equals("test_ConstructorI")) test_ConstructorI();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_Display")) test_ConstructorLorg_eclipse_swt_widgets_Display();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_DisplayI")) test_ConstructorLorg_eclipse_swt_widgets_DisplayI();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_Shell")) test_ConstructorLorg_eclipse_swt_widgets_Shell();
	else if (getName().equals("test_ConstructorLorg_eclipse_swt_widgets_ShellI")) test_ConstructorLorg_eclipse_swt_widgets_ShellI();
	else if (getName().equals("test_win32_newLorg_eclipse_swt_widgets_DisplayI")) test_win32_newLorg_eclipse_swt_widgets_DisplayI();
	else if (getName().equals("test_addShellListenerLorg_eclipse_swt_events_ShellListener")) test_addShellListenerLorg_eclipse_swt_events_ShellListener();
	else if (getName().equals("test_close")) test_close();
	else if (getName().equals("test_dispose")) test_dispose();
	else if (getName().equals("test_getBounds")) test_getBounds();
	else if (getName().equals("test_getDisplay")) test_getDisplay();
	else if (getName().equals("test_getEnabled")) test_getEnabled();
	else if (getName().equals("test_getImeInputMode")) test_getImeInputMode();
	else if (getName().equals("test_getLocation")) test_getLocation();
	else if (getName().equals("test_getShell")) test_getShell();
	else if (getName().equals("test_getShells")) test_getShells();
	else if (getName().equals("test_isEnabled")) test_isEnabled();
	else if (getName().equals("test_open")) test_open();
	else if (getName().equals("test_removeShellListenerLorg_eclipse_swt_events_ShellListener")) test_removeShellListenerLorg_eclipse_swt_events_ShellListener();
	else if (getName().equals("test_setEnabledZ")) test_setEnabledZ();
	else if (getName().equals("test_setImeInputModeI")) test_setImeInputModeI();
	else if (getName().equals("test_setVisibleZ")) test_setVisibleZ();
	else super.runTest();
}
}
