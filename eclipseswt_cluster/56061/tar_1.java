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
package org.eclipse.swt.examples.texteditor;


import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 */
public class TextEditor {  
	Shell shell;
	ToolBar toolBar;
	StyledText text;

	Images images = new Images();
	Vector cachedStyles = new Vector();
	Color RED = null; 
	Color BLUE = null; 
	Color GREEN = null; 
	Font font = null;
	
	boolean isBold = false;
	
	ExtendedModifyListener extendedModifyListener;
	static ResourceBundle resources = ResourceBundle.getBundle("examples_texteditor");

Menu createEditMenu() {
	Menu bar = shell.getMenuBar ();
	Menu menu = new Menu (bar);
	
	MenuItem item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Cut_menuitem"));
	item.setAccelerator(SWT.MOD1 + 'X');
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleCutCopy();
			text.cut();
		}
	});

	item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Copy_menuitem"));
	item.setAccelerator(SWT.MOD1 + 'C');
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleCutCopy();
			text.copy();
		}
	});

	item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Paste_menuitem"));
	item.setAccelerator(SWT.MOD1 + 'V');
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			text.paste();
		}
	});

	new MenuItem (menu, SWT.SEPARATOR);
	
	item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Font_menuitem"));
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			setFont();
		}
	});
	return menu;
}

Menu createFileMenu() {
	Menu bar = shell.getMenuBar ();
	Menu menu = new Menu (bar);
	
	MenuItem item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Exit_menuitem"));
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			shell.close ();
		}
	});

	return menu;
}

/*
 * Set the text state to bold.
 */
void bold(boolean bold) {
	isBold = bold;
	Point sel = text.getSelectionRange();
	if ((sel != null) && (sel.y != 0)) {
		StyleRange style;
		int fontStyle = SWT.NORMAL;
		if (isBold) fontStyle = SWT.BOLD;
		style = new StyleRange(sel.x, sel.y, null, null, fontStyle);
		text.setStyleRange(style);
	}
	text.setSelectionRange(sel.x + sel.y, 0);
}
/*
 * Clear all style data for the selected text.
 */
void clear() {
	Point sel = text.getSelectionRange();
	if ((sel != null) && (sel.y != 0)) {
		StyleRange style;
		style = new StyleRange(sel.x, sel.y, null, null, SWT.NORMAL);
		text.setStyleRange(style);
	}
	text.setSelectionRange(sel.x + sel.y, 0);
}
/*
 * Set the foreground color for the selected text.
 */
void fgColor(int color) {
	Point sel = text.getSelectionRange();
	if ((sel == null) || (sel.y == 0)) return;
	Color fg;
	if (color == SWT.COLOR_RED) {
		fg = RED;
	} else if (color == SWT.COLOR_GREEN) {
		fg = GREEN;
	} else if (color == SWT.COLOR_BLUE) {
		fg = BLUE;
	} else {
		fg = null;
	}
	StyleRange style;
	for (int i = sel.x; i<sel.x+sel.y; i++) {
		StyleRange range = text.getStyleRangeAtOffset(i);
		if (range == null) {style = new StyleRange(i, 1, fg, null, SWT.NORMAL);}
		else {style = new StyleRange(i, 1, fg, null, range.fontStyle);};
		text.setStyleRange(style);
	}
	text.setSelectionRange(sel.x + sel.y, 0);
}
void createMenuBar () {
	Menu bar = new Menu (shell, SWT.BAR);
	shell.setMenuBar (bar);

	MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
	fileItem.setText (resources.getString("File_menuitem"));
	fileItem.setMenu (createFileMenu ());

	MenuItem editItem = new MenuItem (bar, SWT.CASCADE);
	editItem.setText (resources.getString("Edit_menuitem"));
	editItem.setMenu (createEditMenu ());
}

void createShell (Display display) {
	shell = new Shell (display);
	shell.setText (resources.getString("Window_title"));	
	images.loadAll (display);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	shell.setLayout(layout);
	shell.addDisposeListener (new DisposeListener () {
		public void widgetDisposed (DisposeEvent e) {
			if (font != null) font.dispose();
			images.freeAll ();
			RED.dispose();
			GREEN.dispose();
			BLUE.dispose();
		}
	});
}
void createStyledText() {
	initializeColors();
	text = new StyledText (shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	GridData spec = new GridData();
	spec.horizontalAlignment = GridData.FILL;
	spec.grabExcessHorizontalSpace = true;
	spec.verticalAlignment = GridData.FILL;
	spec.grabExcessVerticalSpace = true;
	text.setLayoutData(spec);
	extendedModifyListener = new ExtendedModifyListener() {
		public void modifyText(ExtendedModifyEvent e) {
			handleExtendedModify(e);
		}
	};
	text.addExtendedModifyListener(extendedModifyListener);
}

void createToolBar() {
	toolBar = new ToolBar(shell, SWT.NULL);
	
	ToolItem item = new ToolItem(toolBar, SWT.CHECK);
	item.setImage(images.Bold);
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			bold(((ToolItem)event.widget).getSelection());
		}
	});
	
	item = new ToolItem(toolBar, SWT.SEPARATOR);

	item = new ToolItem(toolBar, SWT.PUSH);
	item.setImage(images.Red);
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			fgColor(SWT.COLOR_RED);
		}
	});
	item = new ToolItem(toolBar, SWT.PUSH);
	item.setImage(images.Green);
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			fgColor(SWT.COLOR_GREEN);
		}
	});
	item = new ToolItem(toolBar, SWT.PUSH);
	item.setImage(images.Blue);
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			fgColor(SWT.COLOR_BLUE);
		}
	});
	
	item = new ToolItem(toolBar, SWT.SEPARATOR);

	item = new ToolItem(toolBar, SWT.PUSH);
	item.setImage(images.Erase);
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			clear();
		}
	});
}
void displayError(String msg) {
	MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
	box.setMessage(msg);
	box.open();
}
/*
 * Cache the style information for text that has been cut or copied.
 */
void handleCutCopy() {
	// Save the cut/copied style info so that during paste we will maintain
	// the style information.  Cut/copied text is put in the clipboard in
	// RTF format, but is not pasted in RTF format.  The other way to 
	// handle the pasting of styles would be to access the Clipboard directly and 
	// parse the RTF text.
	cachedStyles = new Vector();
	Point sel = text.getSelectionRange();
	int startX = sel.x;
	for (int i=sel.x; i<=sel.x+sel.y-1; i++) {
		StyleRange style = text.getStyleRangeAtOffset(i);
		if (style != null) {
			style.start = style.start - startX;
			if (!cachedStyles.isEmpty()) {
				StyleRange lastStyle = (StyleRange)cachedStyles.lastElement();
				if (lastStyle.similarTo(style) && lastStyle.start + lastStyle.length == style.start) {
					lastStyle.length++;
				} else {
					cachedStyles.addElement(style);
				}
			} else {
				cachedStyles.addElement(style);
			}
		}
	}
}
void handleExtendedModify(ExtendedModifyEvent event) {
	if (event.length == 0) return;
	StyleRange style;
	if (event.length == 1 || text.getTextRange(event.start, event.length).equals(text.getLineDelimiter())) {
		// Have the new text take on the style of the text to its right (during
		// typing) if no style information is active.
		int caretOffset = text.getCaretOffset();
		style = null;
		if (caretOffset < text.getCharCount()) style = text.getStyleRangeAtOffset(caretOffset);
		if (style != null) {
			style.start = event.start;
			style.length = event.length;
			int fontStyle = SWT.NORMAL;
			if (isBold) fontStyle = SWT.BOLD;
			style.fontStyle = fontStyle;
			text.setStyleRange(style);
		} else if (isBold) {
			StyleRange newStyle = new StyleRange(event.start, event.length, null, null, SWT.BOLD);
			text.setStyleRange(newStyle);
		}
	} else {
		// paste occurring, have text take on the styles it had when it was
		// cut/copied
		for (int i=0; i<cachedStyles.size(); i++) {
			style = (StyleRange)cachedStyles.elementAt(i);
			StyleRange newStyle = (StyleRange)style.clone();
			newStyle.start = style.start + event.start;
			text.setStyleRange(newStyle);
		}
	}
}

public static void main (String [] args) {
	Display display = new Display ();
	TextEditor example = new TextEditor ();
	Shell shell = example.open (display);
	while (!shell.isDisposed ())
		if (!display.readAndDispatch ()) display.sleep ();
	display.dispose ();
}

public Shell open (Display display) {
	createShell (display);
	createMenuBar ();
	createToolBar ();
	createStyledText ();
	shell.setSize(500, 300);
	shell.open ();
	return shell;
}

void setFont() {
	FontDialog fontDialog = new FontDialog(shell);
	fontDialog.setFontList((text.getFont()).getFontData());
	FontData fontData = fontDialog.open();
	if(fontData != null) {
		if(font != null)
			font.dispose();
		font = new Font(shell.getDisplay(), fontData);
		text.setFont(font);
	}
}

void initializeColors() {
	Display display = Display.getDefault();
	RED = new Color (display, new RGB(255,0,0));
	BLUE = new Color (display, new RGB(0,0,255));
	GREEN = new Color (display, new RGB(0,255,0));
}
}
