package org.eclipse.swt.internal;
/*
 * (c) Copyright IBM Corp. 2001, 2002.
 * All Rights Reserved
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.internal.win32.GCP_RESULTS;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.RECT;
import org.eclipse.swt.internal.win32.TCHAR;
import java.util.Hashtable;
/*
 * Wraps Win32 API used to bidi enable the StyledText widget.
 */
public class BidiUtil {

	// Keyboard language ids
	public static final int KEYBOARD_LATIN = 0;
	public static final int KEYBOARD_HEBREW = 1;
	public static final int KEYBOARD_ARABIC = 2;

	// getRenderInfo flag values
	public static final int CLASSIN = 1;
	public static final int LINKBEFORE = 2;
	public static final int LINKAFTER = 4;

	// variables used for providing a listener mechanism for keyboard language 
	// switching 
	static Hashtable map = new Hashtable ();
	static Hashtable oldProcMap = new Hashtable ();
	static Callback callback = new Callback (BidiUtil.class, "windowProc", 4);

	// GetCharacterPlacement constants
	static final int GCP_REORDER = 0x0002;
	static final int GCP_GLYPHSHAPE = 0x0010;
	static final int GCP_LIGATE = 0x0020;
	static final int GCP_CLASSIN = 0x00080000;
	static final byte GCPCLASS_ARABIC = 2;
	static final byte GCPCLASS_HEBREW = 2;	
	static final byte GCPCLASS_LOCALNUMBER = 4;
	static final int GCPGLYPH_LINKBEFORE = 0x8000;
	static final int GCPGLYPH_LINKAFTER = 0x4000;
	// ExtTextOut constants
	static final int ETO_GLYPH_INDEX = 0x0010;
	// Windows primary language identifiers
	static final int LANG_ARABIC = 0x01;
	static final int LANG_HEBREW = 0x0d;
	// ActivateKeyboard constants
	static final int HKL_NEXT = 1;
	static final int HKL_PREV = 0;

	/*
	 * Public character class constants are the same as Windows 
	 * platform constants. 
	 * Saves conversion of class array in getRenderInfo to arbitrary 
	 * constants for now.
	 */
	public static final int CLASS_HEBREW = GCPCLASS_ARABIC;
	public static final int CLASS_ARABIC = GCPCLASS_HEBREW;
	public static final int CLASS_LOCALNUMBER = GCPCLASS_LOCALNUMBER;
	public static final int REORDER = GCP_REORDER;				
	public static final int LIGATE = GCP_LIGATE;
	public static final int GLYPHSHAPE = GCP_GLYPHSHAPE;

/**
 * Adds a language listener. The listener will get notified when the language of
 * the keyboard changes (via Alt-Shift on Win platforms).  Do this by creating a 
 * window proc for the Control so that the window messages for the Control can be
 * monitored.
 * <p>
 *
 * @param int the handle of the Control that is listening for keyboard language 
 *  changes
 * @param runnable the code that should be executed when a keyboard language change
 *  occurs
 */
public static void addLanguageListener (int hwnd, Runnable runnable) {
	map.put (new Integer (hwnd), runnable);
	int oldProc = OS.GetWindowLong (hwnd, OS.GWL_WNDPROC);
	oldProcMap.put (new Integer(hwnd), new Integer(oldProc));
	OS.SetWindowLong (hwnd, OS.GWL_WNDPROC, callback.getAddress ());
}
/**
 * Wraps the ExtTextOut function.
 * <p>
 *
 * @param gc the gc to use for rendering
 * @param renderBuffer the glyphs to render as an array of characters
 * @param renderDx the width of each glyph in renderBuffer
 * @param x x position to start rendering
 * @param y y position to start rendering
 */
public static void drawGlyphs(GC gc, char[] renderBuffer, int[] renderDx, int x, int y) {
	RECT rect = null;
	OS.ExtTextOutW(gc.handle, x, y, ETO_GLYPH_INDEX, rect, renderBuffer, renderBuffer.length, renderDx);
}
/**
 * Return ordering and rendering information for the given text.  Wraps the GetFontLanguageInfo
 * and GetCharacterPlacement functions.
 * <p>
 * 
 * @param gc the GC to use for measuring of this line, input parameter
 * @param text text that bidi data should be calculated for, input parameter
 * @param order an array of integers representing the visual position of each character in
 *  the text array, output parameter
 * @param classBuffer an array of integers representing the type (e.g., ARABIC, HEBREW, 
 *  LOCALNUMBER) of each character in the text array, input/output parameter
 * @param dx an array of integers representing the pixel width of each glyph in the returned
 *  glyph buffer, output paramteter
 * @param flags an integer representing rendering flag information, input parameter
 * @param offsets text segments that should be measured and reordered separately, input 
 *  parameter. See org.eclipse.swt.custom.BidiSegmentEvent for details.
 * @return buffer with the glyphs that should be rendered for the given text
 */
public static char[] getRenderInfo(GC gc, String text, int[] order, byte[] classBuffer, int[] dx, int flags, int [] offsets) {
	int fontLanguageInfo = OS.GetFontLanguageInfo(gc.handle);
	int hHeap = OS.GetProcessHeap();
	int[] lpCs = new int[8];
	int cs = OS.GetTextCharset(gc.handle);
	OS.TranslateCharsetInfo(cs, lpCs, OS.TCI_SRCCHARSET);
	TCHAR textBuffer = new TCHAR(lpCs[1], text, false);
	int byteCount = textBuffer.length();
	boolean linkBefore = (flags & LINKBEFORE) == LINKBEFORE;
	boolean linkAfter = (flags & LINKAFTER) == LINKAFTER;

	GCP_RESULTS result = new GCP_RESULTS();
	result.lStructSize = GCP_RESULTS.sizeof;
	result.nGlyphs = byteCount;
	int lpOrder = result.lpOrder = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount * 4);
	int lpDx = result.lpDx = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount * 4);
	int lpClass = result.lpClass = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
	int lpGlyphs = result.lpGlyphs = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount * 2);

	// set required dwFlags
	int dwFlags = 0;
	int glyphFlags = 0;
	if (((fontLanguageInfo & GCP_REORDER) == GCP_REORDER)) {
		dwFlags |= GCP_REORDER;
	}
	if ((fontLanguageInfo & GCP_LIGATE) == GCP_LIGATE) {
		dwFlags |= GCP_LIGATE;
		glyphFlags |= 0;
	}
	if ((fontLanguageInfo & GCP_GLYPHSHAPE) == GCP_GLYPHSHAPE) {
		dwFlags |= GCP_GLYPHSHAPE;
		if (linkBefore) {
			glyphFlags |= GCPGLYPH_LINKBEFORE;
		}
		if (linkAfter) {
			glyphFlags |= GCPGLYPH_LINKAFTER;
		}
	}
	byte[] lpGlyphs2;
	if (linkBefore || linkAfter) {
		lpGlyphs2 = new byte[2];
		lpGlyphs2[0]=(byte)glyphFlags;
		lpGlyphs2[1]=(byte)(glyphFlags >> 8);
	}
	else {
		lpGlyphs2 = new byte[] {(byte) glyphFlags};
	}
	OS.MoveMemory(result.lpGlyphs, lpGlyphs2, lpGlyphs2.length);

	if ((flags & CLASSIN) == CLASSIN) {
		// set classification values for the substring
		dwFlags |= GCP_CLASSIN;
		OS.MoveMemory(result.lpClass, classBuffer, classBuffer.length);
	}

	char[] glyphBuffer = new char[result.nGlyphs];
	int glyphCount = 0;
	for (int i=0; i<offsets.length-1; i++) {
		int offset = offsets [i];
		int length = offsets [i+1] - offsets [i];

		// The number of glyphs expected is <= length (segment length);
		// the actual number returned may be less in case of Arabic ligatures.
		result.nGlyphs = length;
		TCHAR textBuffer2 = new TCHAR(lpCs[1], text.substring(offset, offset + length), false);
		OS.GetCharacterPlacement(gc.handle, textBuffer2, textBuffer2.length(), 0, result, dwFlags);

		if (dx != null) {
			int [] dx2 = new int [result.nGlyphs];
			OS.MoveMemory(dx2, result.lpDx, dx2.length * 4);
			System.arraycopy (dx2, 0, dx, glyphCount, dx2.length);
		}
		if (order != null) {
			int [] order2 = new int [length];
			OS.MoveMemory(order2, result.lpOrder, order2.length * 4);
			for (int j=0; j<length; j++) {
				order2 [j] += glyphCount;
			}
			System.arraycopy (order2, 0, order, offset, length);
		}
		if (classBuffer != null) {
			byte [] classBuffer2 = new byte [length];
			OS.MoveMemory(classBuffer2, result.lpClass, classBuffer2.length);
			System.arraycopy (classBuffer2, 0, classBuffer, offset, length);
		}
		char[] glyphBuffer2 = new char[result.nGlyphs];
		OS.MoveMemory(glyphBuffer2, result.lpGlyphs, glyphBuffer2.length * 2);
		System.arraycopy (glyphBuffer2, 0, glyphBuffer, glyphCount, glyphBuffer2.length);
		glyphCount += glyphBuffer2.length;

		// We concatenate successive results of calls to GCP.
		// For Arabic, it is the only good method since the number of output
		// glyphs might be less than the number of input characters.
		// This assumes that the whole line is built by successive adjacent
		// segments without overlapping.
		result.lpOrder += length * 4;
		result.lpDx += length * 4;
		result.lpClass += length;
		result.lpGlyphs += glyphBuffer2.length * 2;
	}

	/* Free the memory that was allocated. */
	OS.HeapFree(hHeap, 0, lpGlyphs);
	OS.HeapFree(hHeap, 0, lpClass);
	OS.HeapFree(hHeap, 0, lpDx);
	OS.HeapFree(hHeap, 0, lpOrder);
	return glyphBuffer;
}
/**
 * Return bidi ordering information for the given text.  Does not return rendering 
 * information (e.g., glyphs, glyph distances).  Use this method when you only need 
 * ordering information.  Doing so will improve performance.  Wraps the 
 * GetFontLanguageInfo and GetCharacterPlacement functions.
 * <p>
 * 
 * @param gc the GC to use for measuring of this line, input parameter
 * @param text text that bidi data should be calculated for, input parameter
 * @param order an array of integers representing the visual position of each character in
 *  the text array, output parameter
 * @param classBuffer an array of integers representing the type (e.g., ARABIC, HEBREW, 
 *  LOCALNUMBER) of each character in the text array, input/output parameter
 * @param flags an integer representing rendering flag information, input parameter
 * @param offsets text segments that should be measured and reordered separately, input 
 *  parameter. See org.eclipse.swt.custom.BidiSegmentEvent for details.
 */
public static void getOrderInfo(GC gc, String text, int[] order, byte[] classBuffer, int flags, int [] offsets) {
	int fontLanguageInfo = OS.GetFontLanguageInfo(gc.handle);
	int hHeap = OS.GetProcessHeap();
	int[] lpCs = new int[8];
	int cs = OS.GetTextCharset(gc.handle);
	OS.TranslateCharsetInfo(cs, lpCs, OS.TCI_SRCCHARSET);
	TCHAR textBuffer = new TCHAR(lpCs[1], text, false);
	int byteCount = textBuffer.length();

	GCP_RESULTS result = new GCP_RESULTS();
	result.lStructSize = GCP_RESULTS.sizeof;
	result.nGlyphs = byteCount;
	int lpOrder = result.lpOrder = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount * 4);
	int lpClass = result.lpClass = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);

	// set required dwFlags, these values will affect how the text gets rendered and
	// ordered
	int dwFlags = 0;
	if (((fontLanguageInfo & GCP_REORDER) == GCP_REORDER)) {
		dwFlags |= GCP_REORDER;
	}
	if ((fontLanguageInfo & GCP_LIGATE) == GCP_LIGATE) {
		dwFlags |= GCP_LIGATE;
	}
	if ((fontLanguageInfo & GCP_GLYPHSHAPE) == GCP_GLYPHSHAPE) {
		dwFlags |= GCP_GLYPHSHAPE;
	}
	if ((flags & CLASSIN) == CLASSIN) {
		// set classification values for the substring, classification values
		// can be specified on input
		dwFlags |= GCP_CLASSIN;
		OS.MoveMemory(result.lpClass, classBuffer, classBuffer.length);
	}

	int glyphCount = 0;
	for (int i=0; i<offsets.length-1; i++) {
		int offset = offsets [i];
		int length = offsets [i+1] - offsets [i];
		// The number of glyphs expected is <= length (segment length);
		// the actual number returned may be less in case of Arabic ligatures.
		result.nGlyphs = length;
		TCHAR textBuffer2 = new TCHAR(lpCs[1], text.substring(offset, offset + length), false);
		OS.GetCharacterPlacement(gc.handle, textBuffer2, textBuffer2.length(), 0, result, dwFlags);

		if (order != null) {
			int [] order2 = new int [length];
			OS.MoveMemory(order2, result.lpOrder, order2.length * 4);
			for (int j=0; j<length; j++) {
				order2 [j] += glyphCount;
			}
			System.arraycopy (order2, 0, order, offset, length);
		}
		if (classBuffer != null) {
			byte [] classBuffer2 = new byte [length];
			OS.MoveMemory(classBuffer2, result.lpClass, classBuffer2.length);
			System.arraycopy (classBuffer2, 0, classBuffer, offset, length);
		}
		glyphCount += result.nGlyphs;

		// We concatenate successive results of calls to GCP.
		// For Arabic, it is the only good method since the number of output
		// glyphs might be less than the number of input characters.
		// This assumes that the whole line is built by successive adjacent
		// segments without overlapping.
		result.lpOrder += length * 4;
		result.lpClass += length;
	}

	/* Free the memory that was allocated. */
	OS.HeapFree(hHeap, 0, lpClass);
	OS.HeapFree(hHeap, 0, lpOrder);
}
/**
 * Return bidi attribute information for the font in the specified gc.  
 * <p>
 *
 * @param gc the gc to query
 * @return bitwise OR of the REORDER, LIGATE and GLYPHSHAPE flags
 * 	defined by this class.
 */
public static int getFontBidiAttributes(GC gc) {
	int fontStyle = 0;
	int fontLanguageInfo = OS.GetFontLanguageInfo(gc.handle);
	if (((fontLanguageInfo & GCP_REORDER) != 0)) {
		fontStyle |= REORDER;
	}
	if (((fontLanguageInfo & GCP_LIGATE) != 0)) {
		fontStyle |= LIGATE;
	}
	if (((fontLanguageInfo & GCP_GLYPHSHAPE) != 0)) {
		fontStyle |= GLYPHSHAPE;
	}
	return fontStyle;	
}
/**
 * Return the active keyboard language.  
 * <p>
 *
 * @return an integer representing the active keyboard language (KEYBOARD_HEBREW,
 *  KEYBOARD_ARABIC, KEYBOARD_LATIN)
 */
public static int getKeyboardLanguage() {
	int layout = OS.GetKeyboardLayout(0);
	// only interested in low 2 bytes, which is the primary
	// language identifier
	layout = layout & 0x000000FF;
	if (layout == LANG_HEBREW) return KEYBOARD_HEBREW;
	if (layout == LANG_ARABIC) return KEYBOARD_ARABIC;
	// return LATIN for all non-bidi languages
	return KEYBOARD_LATIN;
}
/**
 * Return the languages that are installed for the keyboard.  
 * <p>
 *
 * @return integer array with an entry for each installed language
 */
static int[] getKeyboardLanguageList() {
	int maxSize = 10;
	int[] tempList = new int[maxSize];
	int size = OS.GetKeyboardLayoutList(maxSize, tempList);
	int[] list = new int[size];
	System.arraycopy(tempList, 0, list, 0, size);
	return list;
}
/**
 * Return whether or not the platform supports a bidi language.  Determine this
 * by looking at the languages that are installed for the keyboard.  
 * <p>
 *
 * @return true if bidi is supported, false otherwise. Always 
 * 	false on Windows CE.
 */
public static boolean isBidiPlatform() {
	if (OS.IsWinCE) return false;
	int[] languages = getKeyboardLanguageList();
	for (int i=0; i<languages.length; i++) {
		int language = languages[i] & 0x000000FF;;
		if ((language == LANG_ARABIC) || (language == LANG_HEBREW)) {
			return true;
		}
	}
	return false;
}
/**
 * Removes the specified language listener.
 * <p>
 *
 * @param hwnd the handle of the Control that is listening for keyboard language changes
 */
public static void removeLanguageListener (int hwnd) {
	map.remove (new Integer (hwnd));
	Integer proc = (Integer)oldProcMap.remove (new Integer (hwnd));
	if (proc == null) return;
	OS.SetWindowLong (hwnd, OS.GWL_WNDPROC, proc.intValue());
}		
/**
 * Switch the keyboard language to the specified language. 
 * <p>
 *
 * @param language integer representing language. One of 
 * 	KEYBOARD_HEBREW, KEYBOARD_ARABIC, KEYBOARD_LATIN.
 */
public static void setKeyboardLanguage(int language) {
	// don't switch the keyboard if it doesn't need to be
	if (language == getKeyboardLanguage()) return;
	
	boolean isBidiLang = (language == KEYBOARD_HEBREW) || (language == KEYBOARD_ARABIC);		
	// get the corresponding WIN language id for the
	// language
	if (isBidiLang) {
		int langId;
		if (language == KEYBOARD_HEBREW) langId = LANG_HEBREW;
		else langId = LANG_ARABIC;		
		// get the list of active languages
		int[] list = getKeyboardLanguageList();
		// set to first language of the given type
		for (int i=0; i<list.length; i++) {
			int id = list[i] & 0x000000FF;
			if (id == langId) {
				OS.ActivateKeyboardLayout(list[i], 0);
				return;
			}
		}
	} else {
		// set to the first "Latin" language (anything not
		// hebrew or arabic)
		int[] list = getKeyboardLanguageList();
		for (int i=0; i<list.length; i++) {
			int id = list[i] & 0x000000FF;
			if ((id != LANG_HEBREW) && (id != LANG_ARABIC)) {
				OS.ActivateKeyboardLayout(list[i], 0);
				return;
			}
		}
	}

}
/**
 * Window proc to intercept keyboard language switch event (WS_INPUTLANGCHANGE).
 * Run the Control's registered runnable when the keyboard language is switched.
 * 
 * @param hwnd handle of the control that is listening for the keyboard language
 *  change event
 * @param msg window message
 */
static int windowProc (int hwnd, int msg, int wParam, int lParam) {
	switch (msg) {
		case 0x51 /*OS.WM_INPUTLANGCHANGE*/:
			Runnable runnable = (Runnable) map.get (new Integer (hwnd));
			if (runnable != null) runnable.run ();
			break;
		}
	Integer oldProc = (Integer)oldProcMap.get(new Integer(hwnd));
	return OS.CallWindowProc (oldProc.intValue(), hwnd, msg, wParam, lParam);
}

}