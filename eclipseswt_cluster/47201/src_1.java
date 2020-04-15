package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.motif.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class represent a non-selectable
 * user interface object that displays a string or image.
 * When SEPARATOR is specified, displays a single
 * vertical or horizontal line.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, HORIZONTAL, SHADOW_IN, SHADOW_OUT, VERTICAL</dd>
 * <dd>CENTER, LEFT, RIGHT, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public /*final*/ class Label extends Control {
	String text = "";
	Image image, bitmap, disabled;

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Label (Composite parent, int style) {
	super (parent, checkStyle (style));
}
static int checkStyle (int style) {
	if ((style & SWT.SEPARATOR) != 0) return style;
	return checkBits (style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0);
}
public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget();
	int border = getBorderWidth ();
	int width = border * 2, height = border * 2;
	if ((style & SWT.SEPARATOR) != 0) {
		if ((style & SWT.HORIZONTAL) != 0) {
			width += DEFAULT_WIDTH;  height += 3;
		} else {
			width += 3; height += DEFAULT_HEIGHT;
		}
		if (wHint != SWT.DEFAULT) width = wHint + (border * 2);
		if (hHint != SWT.DEFAULT) height = hHint + (border * 2);
		return new Point (width, height);
	}
	int [] argList = {OS.XmNlabelType, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int labelType = argList [1];
	if (labelType == OS.XmSTRING && (style & SWT.WRAP) != 0 && wHint != SWT.DEFAULT) {
		/* If we are wrapping text, calculate the height based on wHint. */
		int [] argList4 = {
			OS.XmNfontList, 0,      /* 1 */
			OS.XmNmarginTop, 0,     /* 3 */
			OS.XmNmarginBottom, 0,  /* 5 */
			OS.XmNmarginHeight, 0,  /* 7 */
		};
		OS.XtGetValues (handle, argList4, argList4.length / 2);
		Display display = getDisplay ();
		String string = display.wrapText (text, argList4 [1], wHint);
		GC gc = new GC(this);
		Point extent = gc.textExtent(string);
		gc.dispose();
		height = extent.y + argList4 [3] + argList4 [5] + argList4 [7] * 2 + border * 2;
	} else {
		/* If we are not wrapping, ask the widget for its geometry. */
		XtWidgetGeometry result = new XtWidgetGeometry ();
		result.request_mode = OS.CWWidth | OS.CWHeight;
		int [] argList2 = {OS.XmNrecomputeSize, 1};
		OS.XtSetValues(handle, argList2, argList2.length / 2);
		OS.XtQueryGeometry (handle, null, result);
		int [] argList3 = {OS.XmNrecomputeSize, 0};
		OS.XtSetValues(handle, argList3, argList3.length / 2);
		width += result.width;
		height += result.height;
	}

	/**
	 * Feature in Motif. If a label's labelType is XmSTRING but it
	 * has no label set into it yet, recomputing the size will
	 * not take into account the height of the font, as we would
	 * like it to. Take care of this case.
	 */
	if (labelType == OS.XmSTRING && text.length () == 0) {
		height += getFontHeight ();
		width = 0;
	}
	if (wHint != SWT.DEFAULT) width = wHint + (border * 2);
	if (hHint != SWT.DEFAULT) height = hHint + (border * 2);
	return new Point (width, height);
}
void createHandle (int index) {
	state |= HANDLE;
	int parentHandle = parent.handle;
	int borderWidth = (style & SWT.BORDER) != 0 ? 1 : 0;
	if ((style & SWT.SEPARATOR) != 0) {
		int separatorType = separatorType ();
		int orientation = (style & SWT.HORIZONTAL) != 0 ? OS.XmHORIZONTAL : OS.XmVERTICAL;
		int [] argList = {
			OS.XmNancestorSensitive, 1,
			OS.XmNborderWidth, borderWidth,
			OS.XmNorientation, orientation,
			OS.XmNseparatorType, separatorType,
		};
		handle = OS.XmCreateSeparator (parentHandle, null, argList, argList.length / 2);
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		return;
	}
	int alignment = OS.XmALIGNMENT_BEGINNING;
	if ((style & SWT.CENTER) != 0) alignment = OS.XmALIGNMENT_CENTER;
	if ((style & SWT.RIGHT) != 0) alignment = OS.XmALIGNMENT_END;
	int [] argList = {
		OS.XmNancestorSensitive, 1,
		OS.XmNrecomputeSize, 0,
		OS.XmNalignment, alignment,
		OS.XmNborderWidth, borderWidth,
	};
	handle = OS.XmCreateLabel (parentHandle, null, argList, argList.length / 2);
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
}
int defaultBackground () {
	return getDisplay ().labelBackground;
}
int defaultFont () {
	return getDisplay ().labelFont;
}
int defaultForeground () {
	return getDisplay ().labelForeground;
}
/**
 * Returns a value which describes the position of the
 * text or image in the receiver. The value will be one of
 * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
 * unless the receiver is a <code>SEPARATOR</code> label, in 
 * which case, <code>NONE</code> is returned.
 *
 * @return the alignment 
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getAlignment () {
	checkWidget();
	if ((style & SWT.SEPARATOR) != 0) return SWT.LEFT;
	int [] argList = {OS.XmNalignment, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int alignment = argList [1];
	if (alignment == OS.XmALIGNMENT_BEGINNING) return SWT.LEFT;
	if (alignment == OS.XmALIGNMENT_CENTER) return SWT.CENTER;
	if (alignment == OS.XmALIGNMENT_END)return SWT.RIGHT;
	return SWT.LEFT;
}
/**
 * Returns the receiver's image if it has one, or null
 * if it does not.
 *
 * @return the receiver's image
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Image getImage () {
	checkWidget();
	return image;
}
String getNameText () {
	return getText ();
}
/**
 * Returns the receiver's text, which will be an empty
 * string if it has never been set or if the receiver is
 * a <code>SEPARATOR</code> label.
 *
 * @return the receiver's text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getText () {
	checkWidget();
	if ((style & SWT.SEPARATOR) != 0) return "";
	return text;
}
boolean mnemonicHit () {
	Composite control = this.parent;
	while (control != null) {
		Control [] children = control._getChildren ();
		int index = 0;
		while (index < children.length) {
			if (children [index] == this) break;
			index++;
		}
		index++;
		if (index < children.length) {
			if (children [index].setFocus ()) return true;
		}
		control = control.parent;
	}
	return false;
}
boolean mnemonicMatch (char key) {
	char mnemonic = findMnemonic (getText ());
	if (mnemonic == '\0') return false;
	return Character.toUpperCase (key) == Character.toUpperCase (mnemonic);
}
void propagateWidget (boolean enabled) {
	super.propagateWidget (enabled);
	/*
	* Labels never participate in focus traversal when
	* either enabled or disabled.
	*/
	if (enabled) {
		int [] argList = {OS.XmNtraversalOn, 0};
		OS.XtSetValues (handle, argList, argList.length / 2);
	}
}
void releaseWidget () {
	super.releaseWidget ();
	int [] argList = {
		OS.XmNlabelPixmap, OS.XmUNSPECIFIED_PIXMAP,
		OS.XmNlabelInsensitivePixmap, OS.XmUNSPECIFIED_PIXMAP,
	};
	OS.XtSetValues (handle, argList, argList.length / 2);
	if (bitmap != null) bitmap.dispose ();
	if (disabled != null) disabled.dispose ();
	image = bitmap = disabled = null; 
}
int separatorType () {
	if ((style & (SWT.SHADOW_IN)) != 0) return OS.XmSHADOW_ETCHED_IN;
	if ((style & (SWT.SHADOW_OUT)) != 0) return OS.XmSHADOW_ETCHED_OUT;
	return OS.XmSHADOW_ETCHED_IN;
}
/**
 * Controls how text and images will be displayed in the receiver.
 * The argument should be one of <code>LEFT</code>, <code>RIGHT</code>
 * or <code>CENTER</code>.  If the receiver is a <code>SEPARATOR</code>
 * label, the argument is ignored and the alignment is not changed.
 *
 * @param alignment the new alignment 
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setAlignment (int alignment) {
	checkWidget();
	if ((style & SWT.SEPARATOR) != 0) return;
	int [] argList = {OS.XmNalignment, OS.XmALIGNMENT_BEGINNING};
	if ((alignment & SWT.CENTER) != 0) argList [1] = OS.XmALIGNMENT_CENTER;
	if ((alignment & SWT.RIGHT) != 0) argList [1] = OS.XmALIGNMENT_END;
	OS.XtSetValues (handle, argList, argList.length / 2);
}
void setBackgroundPixel (int pixel) {
	super.setBackgroundPixel (pixel);
	int [] argList = {OS.XmNlabelType, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (argList [1] == OS.XmPIXMAP) setBitmap (image);
}
void setBitmap (Image image) {
	int labelPixmap = OS.XmUNSPECIFIED_PIXMAP;
	int labelInsensitivePixmap = OS.XmUNSPECIFIED_PIXMAP;
	if (bitmap != null) bitmap.dispose ();
	if (disabled != null) disabled.dispose ();
	bitmap = disabled = null;
	if (image != null) {
		if (image.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
		Display display = getDisplay ();
		switch (image.type) {
			case SWT.BITMAP:
				labelPixmap = image.pixmap;
				disabled = new Image (display, image, SWT.IMAGE_DISABLE);
				labelInsensitivePixmap = disabled.pixmap;
				break;
			case SWT.ICON:
				Rectangle rect = image.getBounds ();
				bitmap = new Image (display, rect.width, rect.height);
				GC gc = new GC (bitmap);
				gc.setBackground (getBackground ());
				gc.fillRectangle (rect);
				gc.drawImage (image, 0, 0);
				gc.dispose ();
				labelPixmap = bitmap.pixmap;
				disabled = new Image (display, bitmap, SWT.IMAGE_DISABLE);
				labelInsensitivePixmap = disabled.pixmap;
				break;
			default:
				error (SWT.ERROR_NOT_IMPLEMENTED);
		}
	}
	int [] argList = {
		OS.XmNlabelType, OS.XmPIXMAP,
		OS.XmNlabelPixmap, labelPixmap,
		OS.XmNlabelInsensitivePixmap, labelInsensitivePixmap,
	};
	OS.XtSetValues (handle, argList, argList.length / 2);
}
public void setBounds (int x, int y, int width, int height) {
	super.setBounds (x, y, width, height);
	if ((style & SWT.WRAP) != 0) setText (text);
}
public void setFont (Font font) {
	super.setFont (font);
	if ((style & SWT.WRAP) != 0) setText (text);
}
/**
 * Sets the receiver's image to the argument, which may be
 * null indicating that no image should be displayed.
 *
 * @param image the image to display on the receiver (may be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setImage (Image image) {
	checkWidget();
	setBitmap (this.image = image);
}
public void setSize (int width, int height) {
	super.setSize (width, height);
	if ((style & SWT.WRAP) != 0) setText (text);
}
/**
 * Sets the receiver's text.
 * <p>
 * This method sets the widget label.  The label may include
 * the mnemonic characters and line delimiters.
 * </p>
 * 
 * @param string the new text
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setText (String string) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if ((style & SWT.SEPARATOR) != 0) return;
	text = string;
	
	/* Strip out mnemonic marker symbols, and remember the mnemonic. */
	char [] unicode = new char [string.length ()];
	string.getChars (0, unicode.length, unicode, 0);
	int i=0, j=0, mnemonic=0;
	while (i < unicode.length) {
		if ((unicode [j++] = unicode [i++]) == Mnemonic) {
			if (i == unicode.length) {continue;}
			if (unicode [i] == Mnemonic) {i++; continue;}
			if (mnemonic == 0) mnemonic = unicode [i];
			j--;
		}
	}
	while (j < unicode.length) unicode [j++] = 0;
	
	/* Wrap the text if necessary, and convert to mbcs. */
	byte [] buffer;
	if ((style & SWT.WRAP) != 0) {
		int [] argList = {
			OS.XmNfontList, 0,     /* 1 */
			OS.XmNwidth, 0,        /* 3 */
			OS.XmNmarginLeft, 0,   /* 5 */
			OS.XmNmarginRight, 0,  /* 7 */
			OS.XmNborderWidth, 0,  /* 9 */
			OS.XmNmarginWidth, 0,  /* 11 */
		};
		OS.XtGetValues (handle, argList, argList.length / 2);
		int width = argList [3] - argList [5] - argList [7] - argList [9] * 2 - argList [11] * 2;
		Display display = getDisplay ();
		if (mnemonic != 0) string = new String (unicode);
		string = display.wrapText (string, argList [1], width);
		buffer = Converter.wcsToMbcs (getCodePage (), string, true);
	} else {
		buffer = Converter.wcsToMbcs (getCodePage (), unicode, true);
	}
	
	int [] parseTable = getDisplay ().parseTable;
	int xmString = OS.XmStringParseText (
		buffer,
		0,
		OS.XmFONTLIST_DEFAULT_TAG, 
		OS.XmCHARSET_TEXT, 
		parseTable,
		parseTable.length,
		0);
	if (xmString == 0) error (SWT.ERROR_CANNOT_SET_TEXT);
		
	/*
	* Bug in Solaris.  If a mnemonic is defined to be a character
	* that appears in a string in a position that follows a '\n',
	* Solaris segment faults.  For example, a label with text
	* "Hello\nthe&re" would GP since "r" appears after '\n'.
	*
	* The fix is to remove mnemonics from labels that contain
	* '\n', which is fine since such labels generally just act
	* as descriptive texts anyways.
	*/ 
	if (mnemonic == 0 || string.indexOf ('\n') != -1) {
		mnemonic = OS.XK_VoidSymbol;
	}
	int [] argList = {
		OS.XmNlabelType, OS.XmSTRING,
		OS.XmNlabelString, xmString,
		OS.XmNmnemonic, mnemonic,
	};
	OS.XtSetValues (handle, argList, argList.length / 2);
	if (xmString != 0) OS.XmStringFree (xmString);
}
}
