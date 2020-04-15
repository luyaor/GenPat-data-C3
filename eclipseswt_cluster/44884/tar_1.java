package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class allow the user to navigate
 * the file system and select or enter a file name.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SAVE, OPEN, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public class FileDialog extends Dialog {
	String [] filterNames = new String [0];
	String [] filterExtensions = new String [0];
	String [] fileNames = new String [0];
	String filterPath = "", fileName = "";
	String fullPath = "";
	static final String FILTER = "*.*";
	static int BUFFER_SIZE = 1024 * 10;

/**
 * Constructs a new instance of this class given only its
 * parent.
 * <p>
 * Note: Currently, null can be passed in for the parent.
 * This has the effect of creating the dialog on the currently active
 * display if there is one. If there is no current display, the 
 * dialog is created on a "default" display. <b>Passing in null as
 * the parent is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param parent a shell which will be the parent of the new instance
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 */
public FileDialog (Shell parent) {
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
 * for all SWT dialog classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 * Note: Currently, null can be passed in for the parent.
 * This has the effect of creating the dialog on the currently active
 * display if there is one. If there is no current display, the 
 * dialog is created on a "default" display. <b>Passing in null as
 * the parent is not considered to be good coding style,
 * and may not be supported in a future release of SWT.</b>
 * </p>
 *
 * @param parent a shell which will be the parent of the new instance
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 */
public FileDialog (Shell parent, int style) {
	super (parent, style);
	checkSubclass ();
}

/**
 * Returns the path of the first file that was
 * selected in the dialog relative to the filter path,
 * or null if none is available.
 * 
 * @return the relative path of the file
 */
public String getFileName () {
	return fileName;
}

/**
 * Returns the paths of all files that were selected
 * in the dialog relative to the filter path, or null
 * if none are available.
 * 
 * @return the relative paths of the files
 */
public String [] getFileNames () {
	return fileNames;
}

/**
 * Returns the file extensions which the dialog will
 * use to filter the files it shows.
 *
 * @return the file extensions filter
 */
public String [] getFilterExtensions () {
	return filterExtensions;
}

/**
 * Returns the file names which the dialog will
 * use to filter the files it shows.
 *
 * @return the file name filter
 */
public String [] getFilterNames () {
	return filterNames;
}

/**
 * Returns the path which the dialog will use to filter
 * the files it shows.
 *
 * @return the filter path
 */
public String getFilterPath () {
	return filterPath;
}

/**
 * Makes the dialog visible and brings it to the front
 * of the display.
 *
 * @return a string describing the absolute path of the first selected file,
 *         or null if the dialog was cancelled or an error occurred
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
 * </ul>
 */
public String open () {
	int hHeap = OS.GetProcessHeap ();
	
	/* Get the owner HWND for the dialog */
	int hwndOwner = 0;
	if (parent != null) hwndOwner = parent.handle;

	/* Convert the title and copy it into lpstrTitle */
	if (title == null) title = "";	
	/* Use the character encoding for the default locale */
	TCHAR buffer3 = new TCHAR (0, title, true);
	int byteCount3 = buffer3.length () * TCHAR.sizeof;
	int lpstrTitle = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount3);
	OS.MoveMemory (lpstrTitle, buffer3, byteCount3); 

	/* Compute filters and copy into lpstrFilter */
	String strFilter = "";
	if (filterNames == null) filterNames = new String [0];
	if (filterExtensions == null) filterExtensions = new String [0];
	for (int i=0; i<filterExtensions.length; i++) {
		String filterName = filterExtensions [i];
		if (i < filterNames.length) filterName = filterNames [i];
		strFilter = strFilter + filterName + '\0' + filterExtensions [i] + '\0';
	}
	if (filterExtensions.length == 0) {
		strFilter = strFilter + FILTER + '\0' + FILTER + '\0';
	}
	/* Use the character encoding for the default locale */
	TCHAR buffer4 = new TCHAR (0, strFilter, true);
	int byteCount4 = buffer4.length () * TCHAR.sizeof;
	int lpstrFilter = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount4);
	OS.MoveMemory (lpstrFilter, buffer4, byteCount4);
	
	/* Convert the fileName and filterName to C strings */
	if (fileName == null) fileName = "";
	/* Use the character encoding for the default locale */
	TCHAR name = new TCHAR (0, fileName, true);

	/*
	* Bug/Feature in Windows.  Verify that the file name is valid.
	* If an invalid file name is passed to the standard dialog, it
	* does not open and returns an error code.  The fix is to avoid
	* this behavior by verifying the file name before opening the
	* dialog.  If the file name is not valid, use an empty string.
	*/
	if (!OS.IsWinCE) {
		if (OS.GetFileTitle (name, null, (short) 0) < 0) {
			name = new TCHAR (0, "", true);
		}
	}
	
	/*
	* Copy the name into lpstrFile and ensure that the
	* last byte is NULL and the buffer does not overrun.
	* Note that the longest that a single path name can
	* be on Windows is 256.
	*/
	int nMaxFile = 256;
	if ((style & SWT.MULTI) != 0) nMaxFile = Math.max (nMaxFile, BUFFER_SIZE);
	int byteCount = nMaxFile * TCHAR.sizeof;
	int lpstrFile = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
	int byteCountFile = Math.min (name.length () * TCHAR.sizeof, byteCount - TCHAR.sizeof);
	OS.MoveMemory (lpstrFile, name, byteCountFile); 

	/*
	* Copy the path into lpstrInitialDir and ensure that
	* the last byte is NULL and the buffer does not overrun.
	*/
	if (filterPath == null) filterPath = "";
	/* Use the character encoding for the default locale */
	TCHAR path = new TCHAR (0, filterPath, true);
	int lpstrInitialDir = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
	int byteCountDir = Math.min (path.length () * TCHAR.sizeof, byteCount - TCHAR.sizeof);
	OS.MoveMemory (lpstrInitialDir, path, byteCountDir); 

	/* Create the file dialog struct */
	OPENFILENAME struct = new OPENFILENAME ();
	struct.lStructSize = OPENFILENAME.sizeof;
	struct.Flags = OS.OFN_HIDEREADONLY | OS.OFN_NOCHANGEDIR;
	if ((style & SWT.MULTI) != 0) {
		struct.Flags |= OS.OFN_ALLOWMULTISELECT | OS.OFN_EXPLORER;
	}
	struct.hwndOwner = hwndOwner;
	struct.lpstrTitle = lpstrTitle;
	struct.lpstrFile = lpstrFile;
	struct.nMaxFile = nMaxFile;
	struct.lpstrInitialDir = lpstrInitialDir;
	struct.lpstrFilter = lpstrFilter;
	struct.nFilterIndex = 0;
	
	/*
	* Feature in Windows.  The focus window is not saved and
	* and restored automatically by the call to GetOpenFileName ().
	* The fix is to save and restore the focus window.
	*/
	int hwndFocus = OS.GetFocus ();
	
	/*
	* Bug/Feature in Windows.  When Windows opens the standard
	* file dialog, it changes the cursor to the hourglass and
	* does not put it back.  The fix is to save the current
	* cursor and restore it when the dialog closes.
	*/
	int hCursor = OS.GetCursor ();

	/*
	* Open the dialog.  If the open fails due to an invalid
	* file name, use an empty file name and open it again.
	*/
	boolean save = (style & SWT.SAVE) != 0;
	boolean success = (save) ? OS.GetSaveFileName (struct) : OS.GetOpenFileName (struct);
	if (OS.CommDlgExtendedError () == OS.FNERR_INVALIDFILENAME) {
		OS.MoveMemory (lpstrFile, new TCHAR (0, "", true), TCHAR.sizeof);
		success = (save) ? OS.GetSaveFileName (struct) : OS.GetOpenFileName (struct);
	}

	/* Set the new path, file name and filter */
	fullPath = null;
	if (success) {
		
		/* Use the character encoding for the default locale */
		TCHAR buffer = new TCHAR (0, struct.nMaxFile);
		int byteCount1 = buffer.length () * TCHAR.sizeof;
		OS.MoveMemory (buffer, lpstrFile, byteCount1);
		
		/*
		* Bug in WinCE.  For some reason, nFileOffset and nFileExtension
		* are always zero on WinCE HPC. nFileOffset is always zero on
		* WinCE PPC when using GetSaveFileName.  nFileOffset is correctly
		* set on WinCE PPC when using OpenFileName.  The fix is to parse
		* lpstrFile to calculate nFileOffset.
		* 
		* Note: WinCE does not support multi-select file dialogs.
		*/
		int nFileOffset = struct.nFileOffset;
		if (OS.IsWinCE && nFileOffset == 0) {
			int index = 0; 
			while (index < buffer.length ()) {
				int ch = buffer.tcharAt (index);
				if (ch == 0) break;
				if (ch == '\\') nFileOffset = index + 1;
				index++;
			}
		}
		if (nFileOffset != 0) {
		
			/* Use the character encoding for the default locale */
			TCHAR prefix = new TCHAR (0, nFileOffset - 1);
			int byteCount2 = prefix.length () * TCHAR.sizeof;
			OS.MoveMemory (prefix, lpstrFile, byteCount2);
			filterPath = prefix.toString (0, prefix.length ());
			
			/*
			* Get each file from the buffer.  Files are delimited
			* by a NULL character with 2 NULL characters at the end.
			*/
			int count = 0;
			fileNames = new String [(style & SWT.MULTI) != 0 ? 4 : 1];
			int start = nFileOffset;
			do {
				int end = start;
				while (end < buffer.length () && buffer.tcharAt (end) != 0) end++;
				String string = buffer.toString (start, end - start);
				start = end;
				if (count == fileNames.length) {
					String [] newFileNames = new String [fileNames.length + 4];
					System.arraycopy (fileNames, 0, newFileNames, 0, fileNames.length);
					fileNames = newFileNames;
				}
				fileNames [count++] = string;
				if ((style & SWT.MULTI) == 0) break;
				start++;
			} while (start < buffer.length () && buffer.tcharAt (start) != 0);
			
			if (fileNames.length > 0) fileName = fileNames  [0];
			String separator = "";
			int length = filterPath.length ();
			if (length > 0 && filterPath.charAt (length - 1) != '\\') {
				separator = "\\";
			}
			fullPath = filterPath + separator + fileName;
			if (count < fileNames.length) {
				String [] newFileNames = new String [count];
				System.arraycopy (fileNames, 0, newFileNames, 0, count);
				fileNames = newFileNames;
			}
		}
	}
	
	/* Free the memory that was allocated. */
	OS.HeapFree (hHeap, 0, lpstrFile);
	OS.HeapFree (hHeap, 0, lpstrFilter);
	OS.HeapFree (hHeap, 0, lpstrInitialDir);
	OS.HeapFree (hHeap, 0, lpstrTitle);
	
	/* Restore the old cursor */
	OS.SetCursor (hCursor);
	
	/* Restore the old focus */
	OS.SetFocus (hwndFocus);

	/*
	* This code is intentionally commented.  On some
	* platforms, the owner window is repainted right
	* away when a dialog window exits.  This behavior
	* is currently unspecified.
	*/
//	if (hwndOwner != 0) OS.UpdateWindow (hwndOwner);
	
	/* Answer the full path or null */
	return fullPath;
}

/**
 * Set the initial filename which the dialog will
 * select by default when opened to the argument,
 * which may be null.  The name will be prefixed with
 * the filter path when one is supplied.
 * 
 * @param string the file name
 */
public void setFileName (String string) {
	fileName = string;
}

/**
 * Set the file extensions which the dialog will
 * use to filter the files it shows to the argument,
 * which may be null.
 *
 * @param extensions the file extension filter
 */
public void setFilterExtensions (String [] extensions) {
	filterExtensions = extensions;
}

/**
 * Sets the file names which the dialog will
 * use to filter the files it shows to the argument,
 * which may be null.
 *
 * @param names the file name filter
 */
public void setFilterNames (String [] names) {
	filterNames = names;
}

/**
 * Sets the path which the dialog will use to filter
 * the files it shows to the argument, which may be
 * null.
 *
 * @param string the filter path
 */
public void setFilterPath (String string) {
	filterPath = string;
}

}
