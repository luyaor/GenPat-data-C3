package org.eclipse.swt.printing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class allow the user to select
 * a printer and various print-related parameters
 * prior to starting a print job.
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */

public class PrintDialog extends Dialog {
	int scope = PrinterData.ALL_PAGES;
	int startPage = -1, endPage = -1;
	boolean printToFile = false;
	
/**
 * Constructs a new instance of this class given only its parent.
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
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
public PrintDialog (Shell parent) {
	this (parent, SWT.PRIMARY_MODAL);
}

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
public PrintDialog (Shell parent, int style) {
	super (parent, style);
}

/**
 * Returns the print job scope that the user selected
 * before pressing OK in the dialog. This will be one
 * of the following values:
 * <dl>
 * <dt><code>ALL_PAGES</code></dt>
 * <dd>Print all pages in the current document</dd>
 * <dt><code>PAGE_RANGE</code></dt>
 * <dd>Print the range of pages specified by startPage and endPage</dd>
 * <dt><code>SELECTION</code></dt>
 * <dd>Print the current selection</dd>
 * </dl>
 *
 * @return the scope setting that the user selected
 */
public int getScope() {
	return scope;
}

/**
 * Sets the scope of the print job. The user will see this
 * setting when the dialog is opened. This can have one of
 * the following values:
 * <dl>
 * <dt><code>ALL_PAGES</code></dt>
 * <dd>Print all pages in the current document</dd>
 * <dt><code>PAGE_RANGE</code></dt>
 * <dd>Print the range of pages specified by startPage and endPage</dd>
 * <dt><code>SELECTION</code></dt>
 * <dd>Print the current selection</dd>
 * </dl>
 *
 * @param int the scope setting when the dialog is opened
 */
public void setScope(int scope) {
	this.scope = scope;
}

/**
 * Returns the start page setting that the user selected
 * before pressing OK in the dialog.
 * <p>
 * Note that this value is only valid if the scope is <code>PAGE_RANGE</code>.
 * </p>
 *
 * @return the start page setting that the user selected
 */
public int getStartPage() {
	return startPage;
}

/**
 * Sets the start page that the user will see when the dialog
 * is opened.
 *
 * @param int the startPage setting when the dialog is opened
 */
public void setStartPage(int startPage) {
	this.startPage = startPage;
}

/**
 * Returns the end page setting that the user selected
 * before pressing OK in the dialog.
 * <p>
 * Note that this value is only valid if the scope is <code>PAGE_RANGE</code>.
 * </p>
 *
 * @return the end page setting that the user selected
 */
public int getEndPage() {
	return endPage;
}

/**
 * Sets the end page that the user will see when the dialog
 * is opened.
 *
 * @param int the end page setting when the dialog is opened
 */
public void setEndPage(int endPage) {
	this.endPage = endPage;
}

/**
 * Returns the 'Print to file' setting that the user selected
 * before pressing OK in the dialog.
 *
 * @return the 'Print to file' setting that the user selected
 */
public boolean getPrintToFile() {
	return printToFile;
}

/**
 * Sets the 'Print to file' setting that the user will see
 * when the dialog is opened.
 *
 * @param boolean the 'Print to file' setting when the dialog is opened
 */
public void setPrintToFile(boolean printToFile) {
	this.printToFile = printToFile;
}

protected void checkSubclass() {
}

/**
 * Makes the receiver visible and brings it to the front
 * of the display.
 *
 * @return a printer data object describing the desired print job parameters
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public PrinterData open() {
	PRINTDLG pd = new PRINTDLG();
	pd.lStructSize = PRINTDLG.sizeof;
	pd.Flags = OS.PD_USEDEVMODECOPIESANDCOLLATE;
	if (printToFile) pd.Flags |= OS.PD_PRINTTOFILE;
	switch (scope) {
		case PrinterData.PAGE_RANGE: pd.Flags |= OS.PD_PAGENUMS; break;
		case PrinterData.SELECTION: pd.Flags |= OS.PD_SELECTION; break;
		default: pd.Flags |= OS.PD_ALLPAGES;
	}
	pd.nMinPage = 1;
	pd.nMaxPage = -1;
	pd.nFromPage = (short) startPage;
	pd.nToPage = (short) endPage;
	if (OS.PrintDlg(pd)) {
		/* Get driver and device from the DEVNAMES struct */
		int ptr = OS.GlobalLock(pd.hDevNames);
		int size = OS.GlobalSize(ptr);
		byte [] DEVNAMES = new byte[size];
		OS.MoveMemory(DEVNAMES, ptr, size);
		OS.GlobalUnlock(ptr);
		int driverOffset = (DEVNAMES[0] & 0xFF) | ((DEVNAMES[1] & 0xFF) << 8);
		int i = 0;
		while (driverOffset + i < size) {
			if (DEVNAMES[driverOffset + i] == 0) break;
			else i++;
		}
		String driver = new String(DEVNAMES, driverOffset, i);
		int deviceOffset = (DEVNAMES[2] & 0xFF) | ((DEVNAMES[3] & 0xFF) << 8);
		i = 0;
		while (deviceOffset + i < size) {
			if (DEVNAMES[deviceOffset + i] == 0) break;
			else i++;
		}
		String device = new String(DEVNAMES, deviceOffset, i);
	
		/* Create PrinterData object and set fields from PRINTDLG */
		PrinterData data = new PrinterData(driver, device);
		if ((pd.Flags & OS.PD_PAGENUMS) != 0) {
			data.scope = PrinterData.PAGE_RANGE;
			data.startPage = pd.nFromPage;
			data.endPage = pd.nToPage;
		} else if ((pd.Flags & OS.PD_SELECTION) != 0) {
			data.scope = PrinterData.SELECTION;
		}
		data.printToFile = (pd.Flags & OS.PD_PRINTTOFILE) != 0;
		data.copyCount = pd.nCopies;
		data.collate = (pd.Flags & OS.PD_COLLATE) != 0;

		/* Bulk-save the printer-specific settings in the DEVMODE struct */
		ptr = OS.GlobalLock(pd.hDevMode);
		size = OS.GlobalSize(ptr);
		data.otherData = new byte[size];
		OS.MoveMemory(data.otherData, ptr, size);
		OS.GlobalUnlock(ptr);

		return data;
	}
	return null;
}
}
