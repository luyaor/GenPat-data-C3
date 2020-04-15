package org.eclipse.swt.examples.controlexample;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

public class CustomControlExample extends ControlExample {

	/**
	 * Creates an instance of a CustomControlExample embedded
	 * inside the supplied parent Composite.
	 * 
	 * @param parent the container of the example
	 */
	public CustomControlExample(Composite parent) {
		super (parent);
	}
	
	/**
	 * Answers the set of example Tabs
	 */
	Tab[] createTabs() {
		return new Tab [] {
			new CComboTab (this),
			new CTabFolderTab (this),
			new CLabelTab (this),
			new SashFormTab (this),
			new TableTreeTab (this),
		};
	}
	
	/**
	 * Invokes as a standalone program.
	 */
	public static void main(String[] args) {
		standAlone = true;
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		CustomControlExample instance = new CustomControlExample(shell);
		shell.setText(getResourceString("custom.window.title"));
		shell.open();
		while (! shell.isDisposed()) {
			if (! display.readAndDispatch()) display.sleep();
		}
		instance.dispose();
	}
}
