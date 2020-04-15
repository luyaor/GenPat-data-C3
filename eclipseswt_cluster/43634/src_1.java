/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.junit.performance;

import junit.framework.*;
import junit.textui.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * Automated Performance Test Suite for class org.eclipse.swt.graphics.Color
 *
 * @see org.eclipse.swt.graphics.Color
 */
public class Test_situational extends SwtPerformanceTestCase {

public Test_situational(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() throws Exception {
	super.setUp();
	display = Display.getDefault();
}


/**
 * Situations:
 * 
 * - Widget creation
 * - syncExec/asyncExec performance
 * - Image creation
 * - Drawing operations
 * - String measuring
 * - String drawing
 * - Region operations
 * - Fonts
 * - Image loading
 * - Layouts
 */
public void test_createComposites() {
	PerformanceMeter meter = createMeter("Create 1000 composites");
	int samples;
	
	// Warm up.
	for(samples = 0; samples < 2; samples++) {
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		for (int i = 0; i < 100; i++) {
			Composite c = new Composite(shell, SWT.NONE);
			c.setLayout(new FillLayout());
			for (int j = 0; j < 10; j++) {
				Composite c2 = new Composite(c, SWT.NONE);
			}
		}
		shell.dispose();
		while(display.readAndDispatch());
	}

	for(samples = 0; samples < 10; samples++) {
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		meter.start();
		for (int i = 0; i < 100; i++) {
			Composite c = new Composite(shell, SWT.NONE);
			c.setLayout(new FillLayout());
			for (int j = 0; j < 10; j++) {
				Composite c2 = new Composite(c, SWT.NONE);
			}
		}
		meter.stop();
		shell.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_createWidgets() {
	PerformanceMeter meter = createMeter("Create 10 composites of 130 widgets");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		meter.start();
		for (int i = 0; i < 10; i++) {
			Composite c = new Composite(shell, SWT.NONE);
			c.setLayout(new RowLayout());
			for (int j = 0; j < 10; j++) {
				Button b = new Button(c, SWT.PUSH);
				Label label = new Label(c, SWT.NONE);
				Scale scale = new Scale(c, SWT.NONE);
				ProgressBar bar = new ProgressBar(c, SWT.NONE);
				List list = new List(c, SWT.NONE);
				Text text = new Text(c, SWT.SINGLE);
				Text multitext = new Text(c, SWT.MULTI);
				Slider slider = new Slider(c, SWT.NONE);
				Tree tree = new Tree(c, SWT.NONE);
				Table table = new Table(c, SWT.NONE);
				TabFolder tabFolder = new TabFolder(c, SWT.NONE);
				Group g = new Group(c, SWT.BORDER);				
				Composite c2 = new Composite(c, SWT.NONE);
			}
		}
		meter.stop();
		shell.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_layout() {
	PerformanceMeter meter = createMeter("layout 1155 widgets");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		for (int i = 0; i < 5; i++) {
			Composite c = new Composite(shell, SWT.NONE);
			c.setLayout(new RowLayout());
			for (int j = 0; j < 5; j++) {
				Composite c2 = new Composite(c, SWT.NONE);
				c2.setLayout(new GridLayout(5, false));
				for (int k = 0; k < 5; k++) {
					Composite c3 = new Composite(c2, SWT.NONE);
					c3.setLayout(new GridLayout(4, true));
					Button b = new Button(c3, SWT.PUSH);
					Label label = new Label(c3, SWT.NONE);
					b = new Button(c3, SWT.PUSH);
					label = new Label(c3, SWT.NONE);
					b = new Button(c3, SWT.PUSH);
					label = new Label(c3, SWT.NONE);
					b = new Button(c3, SWT.PUSH);
					label = new Label(c3, SWT.NONE);
				}
			}
		}
		shell.open();
		while(display.readAndDispatch());
		meter.start();
		for(int numlayouts = 0; numlayouts < 100; numlayouts++) {
			shell.layout(true, true);
		}
		meter.stop();
		shell.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_imageDrawing() {
	PerformanceMeter meter = createMeter("Draw on an image");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		int width = 640;
		int height = 480;
		Image image = new Image(display, width, height);
		Color color1 = new Color(display, 0xff, 0, 0xff);
		Color color2 = new Color(display, 0, 0xff, 0xff);
		int x1 = 0, y1 = height/2, x2 = width/2, y2 = 0;
		meter.start();
		GC gc = new GC(image);
		for(int i = 0; i < 10000; i++) {
			x1 = (x1 + 5) % width; y1 = (y1 + 5) % height; x2 = (x2 + 5) % width; y2 = (y2 + 5) % height;
			gc.drawLine(x1, y1, x2, y2);
			gc.setForeground((i & 1) == 0 ? color1 : color2);
			gc.setBackground((i & 1) == 0 ? color1 : color2);
			gc.fillRectangle(x1, y1, 200, 200);
			gc.drawRoundRectangle(x2, y2, 200, 200, 50, 50);
			gc.setLineStyle(SWT.LINE_DASHDOT);
			gc.drawLine(x2, y1, x1, y2);
		}
		gc.dispose();
		meter.stop();
		image.dispose();
		color1.dispose();
		color2.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_windowDrawing() {
	PerformanceMeter meter = createMeter("Draw on a window");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		int width = 640;
		int height = 480;
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		Canvas c = new Canvas(shell, SWT.NONE);
		c.setLayoutData(new GridData(width, height));
		shell.pack();
		shell.open();
		while(display.readAndDispatch());
		Color color1 = new Color(display, 0xff, 0, 0xff);
		Color color2 = new Color(display, 0, 0xff, 0xff);
		int x1 = 0, y1 = height/2, x2 = width/2, y2 = 0;
		meter.start();
		GC gc = new GC(c);
		for(int i = 0; i < 2000; i++) {
			x1 = (x1 + 5) % width; y1 = (y1 + 5) % height; x2 = (x2 + 5) % width; y2 = (y2 + 5) % height;
			gc.drawLine(x1, y1, x2, y2);
			gc.setForeground((i & 1) == 0 ? color1 : color2);
			gc.setBackground((i & 1) == 0 ? color1 : color2);
			gc.fillRectangle(x1, y1, 200, 200);
			gc.drawRoundRectangle(x2, y2, 200, 200, 50, 50);
			gc.setLineStyle(SWT.LINE_DASHDOT);
			gc.drawLine(x2, y1, x1, y2);
		}
		gc.dispose();
		meter.stop();
		shell.dispose();
		color1.dispose();
		color2.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_stringDrawing() {
	PerformanceMeter meter = createMeter("300 strings by GC.drawText");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		int width = 640;
		int height = 480;
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		Canvas c = new Canvas(shell, SWT.NONE);
		c.setLayoutData(new GridData(width, height));
		shell.pack();
		shell.open();
		while(display.readAndDispatch());
		Color color1 = new Color(display, 0xff, 0, 0xff);
		Color color2 = new Color(display, 0, 0xff, 0xff);
		Font font1 = new Font(display, "Helvetica", 20, SWT.NONE);
		Font font2 = new Font(display, "Helvetica", 10, SWT.BOLD);
		String testString = "The quick \tbr&own SWT jum&ped foxily o\nver the lazy dog.";
		int x1 = 0, y1 = height/2, x2 = width/2, y2 = 0;
		meter.start();
		GC gc = new GC(c);
		for(int i = 0; i < 100; i++) {
			x1 = (x1 + 5) % width; y1 = (y1 + 5) % height; x2 = (x2 + 5) % width; y2 = (y2 + 5) % height;
			gc.setFont((i & 1) == 0 ? font1 : font2);
			gc.setForeground((i & 1) == 0 ? color1 : color2);
			gc.textExtent(testString);
			gc.drawText(testString, x2, y1);
			gc.drawText(testString, x2, y1/2, SWT.DRAW_MNEMONIC | SWT.DRAW_TRANSPARENT);
			gc.drawText(testString, x2, y2, true);
		}
		gc.dispose();
		meter.stop();
		shell.dispose();
		color1.dispose();
		color2.dispose();
		font1.dispose();
		font2.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public void test_fastStringDrawing() {
	PerformanceMeter meter = createMeter("2000 strings by GC.drawString()");
	int samples;
	
	for(samples = 0; samples < 10; samples++) {
		int width = 640;
		int height = 480;
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		Canvas c = new Canvas(shell, SWT.NONE);
		c.setLayoutData(new GridData(width, height));
		shell.pack();
		shell.open();
		while(display.readAndDispatch());
		Color color1 = new Color(display, 0xff, 0, 0xff);
		Color color2 = new Color(display, 0, 0xff, 0xff);
		Font font1 = new Font(display, "Helvetica", 20, SWT.NONE);
		Font font2 = new Font(display, "Helvetica", 10, SWT.BOLD);
		String testString = "The quick brown SWT jumped foxily over the lazy dog.";
		int x1 = 0, y1 = height/2, x2 = width/2, y2 = 0;
		meter.start();
		GC gc = new GC(c);
		for(int i = 0; i < 1000; i++) {
			x1 = (x1 + 5) % width; y1 = (y1 + 5) % height; x2 = (x2 + 5) % width; y2 = (y2 + 5) % height;
			gc.setFont((i & 1) == 0 ? font1 : font2);
			gc.setForeground((i & 1) == 0 ? color1 : color2);
			gc.stringExtent(testString);
			gc.drawString(testString, x1, y2);
			gc.drawString(testString, x1, y1, true);
		}
		gc.dispose();
		meter.stop();
		shell.dispose();
		color1.dispose();
		color2.dispose();
		font1.dispose();
		font2.dispose();
		while(display.readAndDispatch());
	}	
	disposeMeter(meter);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_situational((String)e.nextElement()));
	}
	return suite;
}
public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_createComposites");
	methodNames.addElement("test_createWidgets");
	methodNames.addElement("test_imageDrawing");
	methodNames.addElement("test_windowDrawing");
	methodNames.addElement("test_stringDrawing");
	methodNames.addElement("test_fastStringDrawing");
	methodNames.addElement("test_layout");
	return methodNames;
}
protected void runTest() throws Throwable {
	if (getName().equals("test_createComposites")) test_createComposites();
	else if (getName().equals("test_createWidgets")) test_createWidgets();
	else if (getName().equals("test_layout")) test_layout();
	else if (getName().equals("test_imageDrawing")) test_imageDrawing();
	else if (getName().equals("test_windowDrawing")) test_windowDrawing();
	else if (getName().equals("test_stringDrawing")) test_stringDrawing();
	else if (getName().equals("test_fastStringDrawing")) test_fastStringDrawing();
}

/* custom */
Display display;
}
