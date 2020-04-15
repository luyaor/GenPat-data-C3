package org.eclipse.swt.examples.controlexample;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class EventConsole {
	Shell shell;
	Text text;
	boolean isActive = true;
	static boolean [] eventsFilter = new boolean [35];
	static {
		int [] defaultFilter = {
			SWT.KeyDown, SWT.KeyUp, SWT.MouseDown,
			SWT.MouseUp, SWT.MouseDoubleClick, SWT.Selection,
			SWT.DefaultSelection, SWT.Expand, SWT.Collapse,
			SWT.Iconify, SWT.Deiconify, SWT.Close,
			SWT.Show, SWT.Hide, SWT.Modify,
			SWT.Verify, SWT.Activate, SWT.Deactivate,
			SWT.Help, SWT.DragDetect, SWT.Arm,
			SWT.Traverse, SWT.HardKeyDown, SWT.HardKeyUp
		};
		for (int i = 0; i < defaultFilter.length; i++) {
			eventsFilter [defaultFilter [i]] = true;
		}
	}
	
	public EventConsole (Shell parent) {
		super ();
		shell = new Shell (parent);
		shell.setText ("ControlExample Events");
		shell.setLayout (new FillLayout ());
		Point parentSize = parent.getSize();
		shell.setBounds(10, parentSize.y - 100, parentSize.x / 2, 200);
		text = new Text (shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		
		Menu bar = new Menu (shell, SWT.BAR);
		MenuItem consoleItem = new MenuItem (bar, SWT.CASCADE);
		consoleItem.setText ("Console");
		shell.setMenuBar (bar);
		Menu dropDown = new Menu (bar);
		consoleItem.setMenu (dropDown);

		MenuItem clear = new MenuItem (dropDown, SWT.NONE);
		clear.setText ("Clear");
		clear.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				text.setText("");
			}
		});
		
		MenuItem close = new MenuItem (dropDown, SWT.NONE);
		close.setText ("Dismiss");
		close.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				shell.dispose ();
				isActive = false;
			}
		});
		shell.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				isActive = false;
			}
		});
	}
	
	public void close () {
		shell.dispose ();
		isActive = false;
	}
	
	public void log (Event event) {
		if (isActive && eventsFilter [event.type]) {
			StringBuffer output = new StringBuffer ();
			output.append ("Control: ");
			output.append (event.widget.toString ());
			output.append (" Type: ");
			output.append (event.type);
			output.append (" X: ");
			output.append (event.x);
			output.append (" Y: ");
			output.append (event.y);
			output.append ("\n");
			text.append (output.toString ());
		}
	}

	public void open () {
		shell.open ();
	}
}
