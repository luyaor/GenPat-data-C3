/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * Tool tip snippet: show a Label's tool tip iff it's not fully visible
 * 
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet278 {

public static void main (String [] args) {
	Display display = new Display ();
	Shell shell = new Shell (display);
	shell.setBounds (10, 10, 300, 100);
	shell.setLayout (new FillLayout ());
	final Label label = new Label (shell, SWT.NONE);
	label.setText ("resize the Shell then hover over this Label");
	label.addListener (SWT.MouseEnter, new Listener () {
		public void handleEvent (Event event) {
			int desiredLabelWidth = label.computeSize (SWT.DEFAULT, SWT.DEFAULT).x;
			int desiredLabelRightX = label.getLocation ().x + desiredLabelWidth;
			int availableRightX = label.getParent ().getClientArea ().width;
			boolean isFullyVisible = desiredLabelRightX <= availableRightX;
			System.out.println ("Label is fully visible: " + isFullyVisible);
			label.setToolTipText (isFullyVisible ? null : label.getText ());
		}
	});
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}

}
