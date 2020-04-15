package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

/**
 * Instances of this class are selectable user interface
 * objects that allow the user to enter and modify text.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>MULTI, SINGLE, READ_ONLY, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Verify</dd>
 * </dl>
 * </p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 */

public class Text extends Scrollable {
	int textLimit = 0xFFFF;
	boolean visibility = true;
	public final static int LIMIT;
	public final static String DELIMITER;
	/*
	* These values can be different on different platforms.
	* Therefore they are not initialized in the declaration
	* to stop the compiler from inlining.
	*/
	static {
		LIMIT = 0x7FFFFFFF;
		DELIMITER = "\n";
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
public Text (Composite parent, int style) {
	super (parent, checkStyle (style));
}

void createHandle (int index) {
	state |= HANDLE;
	int parentHandle = parent.parentingHandle ();
	if ((style & SWT.SINGLE) != 0) {
		handle = OS.gtk_entry_new ();
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.gtk_container_add (parentHandle, handle);
		OS.gtk_entry_set_editable (handle, (style & SWT.READ_ONLY) == 0);
	} else {
		fixedHandle = OS.gtk_fixed_new ();
		if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.gtk_fixed_set_has_window (fixedHandle, true);
		scrolledHandle = OS.gtk_scrolled_window_new (0, 0);
		if (scrolledHandle == 0) error (SWT.ERROR_NO_HANDLES);
		handle = OS.gtk_text_new (0, 0);
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.gtk_container_add (parentHandle, fixedHandle);
		OS.gtk_container_add (fixedHandle, scrolledHandle);
		OS.gtk_container_add (scrolledHandle, handle);
		OS.gtk_widget_show (fixedHandle);
		OS.gtk_widget_show (scrolledHandle);
		OS.gtk_text_set_editable (handle, (style & SWT.READ_ONLY) == 0);
		OS.gtk_text_set_word_wrap (handle, (style & SWT.WRAP) != 0 ? 1 : 0);
		int hsp = (style & SWT.H_SCROLL) == 0 ? OS.GTK_POLICY_NEVER : OS.GTK_POLICY_AUTOMATIC;
		int vsp = (style & SWT.V_SCROLL) == 0 ? OS.GTK_POLICY_NEVER : OS.GTK_POLICY_AUTOMATIC;
		OS.gtk_scrolled_window_set_policy (scrolledHandle, hsp, vsp);
	}
	OS.gtk_widget_show (handle);
}

void hookEvents () {
	//TO DO - get rid of enter/exit for mouse crossing border
	super.hookEvents();
/*	signal_connect_after (handle, "changed", SWT.Modify, 2);
	signal_connect (handle, "insert-text", SWT.Verify, 5);
	signal_connect (handle, "delete-text", SWT.Verify, 4);
	signal_connect (handle, "activate", SWT.Selection, 2);*/
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's text is modified, by sending
 * it one of the messages defined in the <code>ModifyListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ModifyListener
 * @see #removeModifyListener
 */
public void addModifyListener (ModifyListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Modify, typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is selected, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * <code>widgetSelected</code> is not called for texts.
 * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed in a single-line text.
 * </p>
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #removeSelectionListener
 * @see SelectionEvent
 */
public void addSelectionListener(SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener(listener);
	addListener(SWT.Selection,typedListener);
	addListener(SWT.DefaultSelection,typedListener);
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's text is verified, by sending
 * it one of the messages defined in the <code>VerifyListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see VerifyListener
 * @see #removeVerifyListener
 */
public void addVerifyListener (VerifyListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Verify, typedListener);
}

/**
 * Appends a string.
 * <p>
 * The new text is appended to the text at
 * the end of the widget.
 * </p>
 *
 * @param string the string to be appended
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void append (String string) {
	checkWidget ();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	byte [] buffer = Converter.wcsToMbcs (null, string);	
	if ((style & SWT.SINGLE) != 0) {
		OS.gtk_entry_append_text(handle, buffer);
	} else {
		int length = getCharCount();
		int [] position = new int [] {length};
		OS.gtk_editable_insert_text (handle, buffer, buffer.length, position);
		OS.gtk_editable_set_position (handle, position [0]);
	}
}

/**
 * Clears the selection.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void clearSelection () {
	checkWidget ();
	int position = OS.gtk_editable_get_position (handle);
	OS.gtk_editable_delete_selection(handle);
	OS.gtk_editable_set_position (handle, position);
}

/**
 * Copies the selected text.
 * <p>
 * The current selection is copied to the clipboard.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void copy () {
	checkWidget ();
	OS.gtk_editable_copy_clipboard(handle);
}
/**
 * Cuts the selected text.
 * <p>
 * The current selection is first copied to the
 * clipboard and then deleted from the widget.
 * </p>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void cut () {
	checkWidget ();
	OS.gtk_editable_cut_clipboard(handle);
}

/**
 * Gets the line number of the caret.
 * <p>
 * The line number of the caret is returned.
 * </p>
 *
 * @return the line number
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getCaretLineNumber () {
	checkWidget ();
	int addr_index=getCaretPosition();
	String tmpString= new String(getText(0,addr_index));
	return getLineNumberInString(tmpString,'\n');
}

/**
 * Gets the location the caret.
 * <p>
 * The location of the caret is returned.
 * </p>
 *
 * @return a point, the location of the caret
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getCaretLocation () {
	checkWidget ();
	/*GtkText gtktext = new GtkText ();
	OS.memmove (gtktext, handle, GtkText.sizeof);
	return new Point (gtktext.cursor_pos_x, gtktext.cursor_pos_y);*/
	return new Point (0,0);
}

/**
 * Gets the position of the caret.
 * <p>
 * The character position of the caret is returned.
 * </p>
 *
 * @return the position of the caret
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getCaretPosition () {
	checkWidget ();
	return OS.gtk_editable_get_position (handle);
}


/**
 * Gets the number of characters.
 *
 * @return number of characters in the widget
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getCharCount () {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) {
		int address = OS.gtk_editable_get_chars (handle, 0, -1);
		int length = OS.strlen (address);
		OS.g_free (address);
		return length;
	}
	return OS.gtk_text_get_length (handle);
}

/**
 * Gets the line delimiter.
 *
 * @return a string that is the line delimiter
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getLineDelimiter () {
	checkWidget ();
	return "\n";
}
/**
 * Gets the double click enabled flag.
 * <p>
 * The double click flag enables or disables the
 * default action of the text widget when the user
 * double clicks.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getDoubleClickEnabled () {
	checkWidget ();
	return true;
}
/**
 * Gets the echo character.
 * <p>
 * The echo character is the character that is
 * displayed when the user enters text or the
 * text is changed by the programmer.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public char getEchoChar () {
	checkWidget ();
	return visibility ? '\0' : '*';
}

/**
 * Gets the editable state.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getEditable () {
	checkWidget ();
	return OS.gtk_editable_get_editable(handle) != 0;
}

/**
 * Gets the number of lines.
 *
 * @return the number of lines in the widget
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getLineCount () {
	checkWidget ();
	return getLineNumberInString(new String(getText()),'\n') + 1;
}
/**
 * Gets the height of a line.
 *
 * @return the height of a row of text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getLineHeight () {
	checkWidget ();/*
	Font font = getFont();
	GdkFont gdkfont = new GdkFont();
	int fontHandle = font.handle;
	OS.memmove(gdkfont, fontHandle, GdkFont.sizeof);
	return gdkfont.ascent + gdkfont.descent;*/
	return 10;
	
}
/**
 * Gets the position of the selected text.
 * <p>
 * Indexing is zero based.  The range of
 * a selection is from 0..N where N is
 * the number of characters in the widget.
 * </p>
 * 
 * @return the start and end of the selection
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getSelection () {
	checkWidget ();
	/*GtkEditable widget = new GtkEditable ();
	OS.memmove (widget, handle, GtkEditable.sizeof);
	return new Point (widget.selection_start_pos, widget.selection_end_pos);*/
	return new Point (OS.gtk_editable_get_selection_start(handle),
	                  OS.gtk_editable_get_selection_end(handle));
}

/**
 * Gets the number of selected characters.
 *
 * @return the number of selected characters.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelectionCount () {
	checkWidget ();
	Point sel = getSelection();
	return  Math.abs(sel.y - sel.x);
}
/**
 * Gets the selected text.
 *
 * @return the selected text
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getSelectionText () {
	checkWidget ();
	/*GtkEditable widget = new GtkEditable ();
	OS.memmove (widget, handle, GtkEditable.sizeof);
	int start = Math.min(widget.selection_start_pos,widget.selection_end_pos);
	int end = Math.max(widget.selection_start_pos,widget.selection_end_pos);
	int address = OS.gtk_editable_get_chars (handle, start, end);
	int length = OS.strlen (address);
	byte [] buffer1 = new byte [length];
	OS.memmove (buffer1, address, length);
	OS.g_free (address);
	char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
	return new String (buffer2, 0, buffer2.length);*/
	return "";
}

/**
 * Gets the number of tabs.
 * <p>
 * Tab stop spacing is specified in terms of the
 * space (' ') character.  The width of a single
 * tab stop is the pixel width of the spaces.
 * </p>
 *
 * @return the number of tab characters
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTabs () {
	checkWidget ();
	/*GtkText widget= new GtkText();
	OS.memmove(widget, handle, GtkText.sizeof);
	return widget.default_tab_width;*/
	return 8;
}

/**
 * Gets the widget text.
 * <p>
 * The text for a text widget is the characters in the widget.
 * </p>
 *
 * @return the widget text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getText () {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) {
		/*
		 * The GTK documentation explicitly states
		 * that this address should not be freed.
		 */
		int address = OS.gtk_entry_get_text (handle);
		int length = OS.strlen (address);
		byte [] buffer1 = new byte [length];
		OS.memmove (buffer1, address, length);
		char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
		return new String (buffer2, 0, buffer2.length);
	}
	return "";
}

/**
 * Gets a range of text.
 * <p>
 * Indexing is zero based.  The range of
 * a selection is from 0..N-1 where N is
 * the number of characters in the widget.
 * </p>
 *
 * @param start the start of the range
 * @param end the end of the range
 * @return the range of text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getText (int start, int end) {
	checkWidget ();
	int address = OS.gtk_editable_get_chars (handle, start, end);
	if (address == 0) return null;
	int length = OS.strlen (address);
	byte [] buffer1 = new byte [length];
	OS.memmove (buffer1, address, length);
	OS.g_free (address);
	char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
	return new String (buffer2, 0, buffer2.length);
}

/**
 * Returns the maximum number of characters that the receiver is capable of holding. 
 * <p>
 * If this has not been changed by <code>setTextLimit()</code>,
 * it will be the constant <code>Text.LIMIT</code>.
 * </p>
 * 
 * @return the text limit
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTextLimit () {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) return textLimit;
	return LIMIT;
}

/**
 * Returns the zero-relative index of the line which is currently
 * at the top of the receiver.
 * <p>
 * This index can change when lines are scrolled or new lines are added or removed.
 * </p>
 *
 * @return the index of the top line
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTopIndex () {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) return 0;
	/*GtkText widget = new GtkText ();
	OS.memmove (widget, handle, GtkText.sizeof);
	int topCharIndex=widget.first_line_start_index;
	return (getLineNumberInString(getText(0,topCharIndex), '\n'));
	//Since getText uses substring (start, end + 1),so topCharIndex-1*/
	return 0;
}

/**
 * Gets the top pixel.
 * <p>
 * The top pixel is the pixel position of the line
 * that is currently at the top of the widget.  On
 * some platforms, a text widget can be scrolled by
 * pixels instead of lines so that a partial line
 * is displayed at the top of the widget.
 * </p><p>
 * The top pixel changes when the widget is scrolled.
 * The top pixel does not include the widget trimming.
 * </p>
 *
 * @return the pixel position of the top line
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTopPixel () {
	checkWidget ();
	/*if ((style & SWT.SINGLE) != 0) return 0;
	GtkText widget= new GtkText();
	OS.memmove(widget, handle, GtkText.sizeof);
	return widget.first_onscreen_ver_pixel;*/
	return 0;
}

boolean getWrap () {
	checkWidget ();
	return false;
}

/**
 * Inserts a string.
 * <p>
 * The old selection is replaced with the new text.
 * </p>
 *
 * @param string the string
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void insert (String string) {
	checkWidget ();
	/*if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	byte [] buffer = Converter.wcsToMbcs (null, string);
	GtkEditable widget = new GtkEditable ();
	OS.memmove (widget, handle, GtkEditable.sizeof);
	int start = Math.min(widget.selection_start_pos, widget.selection_end_pos);
	int end = Math.max(widget.selection_start_pos, widget.selection_end_pos);	
	OS.gtk_editable_delete_text (handle, start, end);
	int [] position = new int [] {start};
	OS.gtk_editable_insert_text (handle, buffer, buffer.length, position);*/
}

/**
 * Pastes text from clipboard.
 * <p>
 * The selected text is deleted from the widget
 * and new text inserted from the clipboard.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void paste () {
	checkWidget ();
	OS.gtk_editable_paste_clipboard(handle);
}

int processModify (int arg0, int arg1, int int2) {
	sendEvent (SWT.Modify);
	return 0;
}

int processVerify (int int0, int int1, int int2) {
	/*if (!hooks (SWT.Verify)) return 0;
	if (int2 != 0) {
		// Insert 
		if (int0 == 0 || int1==0){
			 return 0;
		}
//		int length = OS.strlen (int0);
		byte [] buffer1 = new byte [int1];
		OS.memmove (buffer1, int0, buffer1.length);
		char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
		String oldText = new String (buffer2, 0, buffer2.length);
		int [] position = new int [1];
		OS.memmove (position, int2, 4);
		if (position [0] == -1) position [0] = OS.gtk_text_get_length (handle);
		String newText = verifyText (oldText, position [0], position [0]); //WRONG POSITION
		if (newText == null) {
			byte [] insert_text = Converter.wcsToMbcs (null, "insert-text", true);
			OS.gtk_signal_emit_stop_by_name (handle, insert_text);
			return 0;
		}
		if (newText != oldText) {
			int windowProc5 = getDisplay ().windowProc5;
			byte [] buffer3 = Converter.wcsToMbcs (null, newText);
			OS.gtk_signal_handler_block_by_func (handle, windowProc5, SWT.Verify);
			OS.gtk_editable_insert_text (handle, buffer3, buffer3.length, position);
			OS.gtk_signal_handler_unblock_by_func (handle, windowProc5, SWT.Verify);
			byte [] insert_text = Converter.wcsToMbcs (null, "insert-text", true);
			OS.gtk_signal_emit_stop_by_name (handle, insert_text);
			return 0;
		}
	} else {
		// Delete 
		int address = OS.gtk_editable_get_chars (handle, int0, int1);
		int length = OS.strlen (address);
		byte [] buffer1 = new byte [length];
		OS.memmove (buffer1, address, length);
		OS.g_free (address);
		char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
		String oldText = new String (buffer2, 0, buffer2.length);
		String newText = verifyText (oldText, int0, int1);
		if (newText == null) {
			byte [] delete_text = Converter.wcsToMbcs (null, "delete-text", true);
			OS.gtk_signal_emit_stop_by_name (handle, delete_text);
			return 0;
		}
	}*/
	return 0;
}

int processSelection (int int0, int int1, int int2) {
	postEvent (SWT.DefaultSelection);
	return 0;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver's text is modified.
 *
 * @param listener the listener which should no longer be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ModifyListener
 * @see #addModifyListener
 */
public void removeModifyListener (ModifyListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Modify, listener);	
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is selected.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #addSelectionListener
 */
public void removeSelectionListener(SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.Selection, listener);
	eventTable.unhook(SWT.DefaultSelection,listener);	
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is verified.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see VerifyListener
 * @see #addVerifyListener
 */
public void removeVerifyListener (VerifyListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Verify, listener);	
}

/**
 * Selects all the text in the receiver.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void selectAll () {
	checkWidget ();
	/*OS.gtk_editable_select_region (handle, 0, -1);*/
}

/**
 * Sets the double click enabled flag.
 * <p>
 * The double click flag enables or disables the
 * default action of the text widget when the user
 * double clicks.
 * </p>
 * 
 * @param doubleClick the new double click flag
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setDoubleClickEnabled (boolean doubleClick) {
	checkWidget ();
}

/**
 * Sets the echo character.
 * <p>
 * The echo character is the character that is
 * displayed when the user enters text or the
 * text is changed by the programmer.
 * </p>
 *
 * @param echo the new echo character
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setEchoChar (char echo) {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) {
		OS.gtk_entry_set_visibility (handle, visibility = echo == '\0');
	}
}

/**
 * Sets the editable state.
 *
 * @param editable the new editable state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setEditable (boolean editable) {
	checkWidget ();
	/*OS.gtk_editable_set_editable (handle, editable);*/
}

/**
 * Sets the selection.
 * <p>
 * Indexing is zero based.  The range of
 * a selection is from 0..N where N is
 * the number of characters in the widget.
 * </p><p>
 * Text selections are specified in terms of
 * caret positions.  In a text widget that
 * contains N characters, there are N+1 caret
 * positions, ranging from 0..N.  This differs
 * from other functions that address character
 * position such as getText () that use the
 * regular array indexing rules.
 * </p>
 *
 * @param start new caret position
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (int start) {
	checkWidget ();
	OS.gtk_editable_set_position (handle, start);
}

/**
 * Sets the selection.
 * <p>
 * Indexing is zero based.  The range of
 * a selection is from 0..N where N is
 * the number of characters in the widget.
 * </p><p>
 * Text selections are specified in terms of
 * caret positions.  In a text widget that
 * contains N characters, there are N+1 caret
 * positions, ranging from 0..N.  This differs
 * from other functions that address character
 * position such as getText () that use the
 * usual array indexing rules.
 * </p>
 *
 * @param start the start of the range
 * @param end the end of the range
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (int start, int end) {
	checkWidget ();
	OS.gtk_editable_set_position (handle, start);
	OS.gtk_editable_select_region (handle, start, end);
}

/**
 * Sets the selection.
 * <p>
 * Indexing is zero based.  The range of
 * a selection is from 0..N where N is
 * the number of characters in the widget.
 * </p><p>
 * Text selections are specified in terms of
 * caret positions.  In a text widget that
 * contains N characters, there are N+1 caret
 * positions, ranging from 0..N.  This differs
 * from other functions that address character
 * position such as getText () that use the
 * usual array indexing rules.
 * </p>
 *
 * @param selection the point
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (Point selection) {
	checkWidget ();
	if (selection == null) error (SWT.ERROR_NULL_ARGUMENT);
	OS.gtk_editable_set_position (handle, selection.x);
	OS.gtk_editable_select_region (handle, selection.x, selection.y);
}

 /**
 * Sets the number of tabs.
 * <p>
 * Tab stop spacing is specified in terms of the
 * space (' ') character.  The width of a single
 * tab stop is the pixel width of the spaces.
 * </p>
 *
 * @param tabs the number of tabs
 *
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
*/
public void setTabs (int tabs) {
	checkWidget ();
	/*GtkText widget= new GtkText();
	widget.default_tab_width=tabs;
	OS.memmove(handle, widget, GtkText.sizeof);*/
}

/**
 * Sets the contents of the receiver to the given string.
 *
 * @param text the new text
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setText (String string) {
	checkWidget ();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	OS.gtk_editable_delete_text (handle, 0, -1);
	int [] position = new int [1];
	byte [] buffer = Converter.wcsToMbcs (null, string);
	OS.gtk_editable_insert_text (handle, buffer, buffer.length, position);
	OS.gtk_editable_set_position (handle, 0);
}

/**
 * Sets the maximum number of characters that the receiver
 * is capable of holding to be the argument.
 *
 * @param limit new text limit
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setTextLimit (int limit) {
	checkWidget ();
	if (limit == 0) error (SWT.ERROR_CANNOT_BE_ZERO);
	if ((style & SWT.SINGLE) != 0) {
		textLimit = (short) limit;
		OS.gtk_entry_set_max_length (handle, (short) limit);
	}
}

/**
 * Sets the zero-relative index of the line which is currently
 * at the top of the receiver. This index can change when lines
 * are scrolled or new lines are added and removed.
 *
 * @param index the index of the top item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setTopIndex (int index) {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) return;
	if (index > getLineCount()) return;
	int adjustmentHandle = OS.gtk_scrolled_window_get_vadjustment(scrolledHandle);
	GtkAdjustment adjustment = new GtkAdjustment(adjustmentHandle);
	int adjust = (int)(index*adjustment.upper/getLineCount());
	if (adjust <= 0) {
		adjust = 0;
		verticalBar.setSelection(0);
	} else {
		verticalBar.setSelection(adjust);
	}
	OS.gtk_adjustment_value_changed(verticalBar.handle);
	int topindex=getTopIndex();
	int lineheight = getLineHeight();
	while( topindex != index) {
		adjust=adjust+lineheight;
		verticalBar.setSelection(adjust+lineheight);
		OS.gtk_adjustment_value_changed(verticalBar.handle);
		topindex=getTopIndex();
	}
}

void setWrap (boolean wrap) {
	checkWidget ();
	OS.gtk_text_set_word_wrap(handle, wrap ? 1 : 0);
}

/**
 * Shows the selection.
 * <p>
 * If the selection is already showing
 * in the receiver, this method simply returns.  Otherwise,
 * lines are scrolled until the selection is visible.
 * </p>
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void showSelection () {
	checkWidget ();
	/*if ((style & SWT.SINGLE) != 0) return;
	int start_pos, end_pos, pos;
	pos = OS.gtk_editable_get_position (handle);
	GtkEditable widget = new GtkEditable ();
	OS.memmove (widget, handle, GtkEditable.sizeof);
	start_pos = Math.min(widget.selection_start_pos, widget.selection_end_pos) ;
	end_pos = Math.max(widget.selection_start_pos, widget.selection_end_pos) ;
	GtkText gtktext = new GtkText ();
	OS.memmove (gtktext, handle, GtkText.sizeof);
	int topCharIndex=gtktext.first_line_start_index;
	if ( (topCharIndex > start_pos && topCharIndex < end_pos) || topCharIndex==start_pos ||
		topCharIndex == end_pos) return;
	if (pos < start_pos || pos > end_pos) {
		int adjustmentHandle = OS.gtk_scrolled_window_get_vadjustment(scrolledHandle);
		GtkAdjustment adjustment = new GtkAdjustment(adjustmentHandle);
		String tmpString= new String(getText(0,start_pos));
		int currentln=getLineNumberInString(tmpString, '\n');
		int adjust = (int)(currentln*adjustment.upper/getLineCount()-adjustment.page_increment);
		if (adjust <= 0) 
			OS.gtk_adjustment_set_value (verticalBar.handle, 0);
		else
			OS.gtk_adjustment_set_value (verticalBar.handle, adjust);
		OS.gtk_adjustment_value_changed(verticalBar.handle);
		OS.gtk_editable_set_position (handle, widget.selection_end_pos);
		OS.gtk_editable_select_region (handle, widget.selection_start_pos, widget.selection_end_pos);
	}
*/
}

String verifyText (String string, int start, int end) {
	Event event = new Event ();
	event.text = string;
	event.start = start;
	event.end = end;
	sendEvent (SWT.Verify, event);
	if (!event.doit) return null;
	return event.text;
}

int getLineNumberInString( String string,char delimiter) {
	int count=0;
	for (int i=0; i<string.length (); i++) {
		if (string.charAt (i) == delimiter) count++;
	}
	return count;			
}

static int checkStyle (int style) {
	if ((style & SWT.SINGLE) != 0) style &= ~(SWT.H_SCROLL | SWT.V_SCROLL);
	if ((style & (SWT.SINGLE | SWT.MULTI)) != 0) return style;
	if ((style & (SWT.H_SCROLL | SWT.V_SCROLL)) != 0) {
		return style | SWT.MULTI;
	}
	return style | SWT.SINGLE;
}
}
