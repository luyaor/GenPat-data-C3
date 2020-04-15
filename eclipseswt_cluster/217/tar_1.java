/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class TableItem extends Item {
	Table parent;
	int index = -1;
	boolean checked, grayed, cached;

	String[] texts;
	int[] textWidths = new int [1];		/* cached string measurements */
	int fontHeight;						/* cached item font height */
	int[] fontHeights;
	int imageIndent;
	Image[] images;
	Color foreground, background;
	String[] displayTexts;
	Color[] cellForegrounds, cellBackgrounds;
	Font font;
	Font[] cellFonts;
	
	static final int MARGIN_TEXT = 3;			/* the left and right margins within the text's space */

public TableItem (Table parent, int style) {
	this (parent, style, checkNull (parent).items.length);
}
public TableItem (Table parent, int style, int index) {
	this (parent, style, index, true);
}
TableItem (Table parent, int style, int index, boolean notifyParent) {
	super (parent, style);
	int validItemIndex = parent.items.length;
	if (!(0 <= index && index <= validItemIndex)) error (SWT.ERROR_INVALID_RANGE);
	this.parent = parent;
	this.index = index;
	int columnCount = parent.columns.length;
	if (columnCount > 0) {
		displayTexts = new String [columnCount];
		if (columnCount > 1) {
			texts = new String [columnCount];
			textWidths = new int [columnCount];
			images = new Image [columnCount];
		}
	}
	if (notifyParent) parent.createItem (this);
}
/*
 * Updates internal structures in the receiver and its child items to handle the creation of a new column.
 */
void addColumn (TableColumn column) {
	int index = column.getIndex ();
	int columnCount = parent.columns.length;

	if (columnCount > 1) {
		if (columnCount == 2) {
			texts = new String [2];
		} else {
			String[] newTexts = new String [columnCount];
			System.arraycopy (texts, 0, newTexts, 0, index);
			System.arraycopy (texts, index, newTexts, index + 1, columnCount - index - 1);
			texts = newTexts;
		}
		if (index == 0) {
			texts [1] = text;
			text = "";	//$NON-NLS-1$
		}

		if (columnCount == 2) {
			images = new Image [2];
		} else {
			Image[] newImages = new Image [columnCount];
			System.arraycopy (images, 0, newImages, 0, index);
			System.arraycopy (images, index, newImages, index + 1, columnCount - index - 1);
			images = newImages;
		}
		if (index == 0) {
			images [1] = image;
			image = null;
		}
		
		int[] newTextWidths = new int [columnCount];
		System.arraycopy (textWidths, 0, newTextWidths, 0, index);
		System.arraycopy (textWidths, index, newTextWidths, index + 1, columnCount - index - 1);
		textWidths = newTextWidths;
	}

	/*
	 * The length of displayTexts always matches the parent's column count, unless this
	 * count is zero, in which case displayTexts is null.  
	 */
	String[] newDisplayTexts = new String [columnCount];
	if (columnCount > 1) {
		System.arraycopy (displayTexts, 0, newDisplayTexts, 0, index);
		System.arraycopy (displayTexts, index, newDisplayTexts, index + 1, columnCount - index - 1);
	}
	displayTexts = newDisplayTexts;

	if (cellBackgrounds != null) {
		Color[] newCellBackgrounds = new Color [columnCount];
		System.arraycopy (cellBackgrounds, 0, newCellBackgrounds, 0, index);
		System.arraycopy (cellBackgrounds, index, newCellBackgrounds, index + 1, columnCount - index - 1);
		cellBackgrounds = newCellBackgrounds;
	}
	if (cellForegrounds != null) {
		Color[] newCellForegrounds = new Color [columnCount];
		System.arraycopy (cellForegrounds, 0, newCellForegrounds, 0, index);
		System.arraycopy (cellForegrounds, index, newCellForegrounds, index + 1, columnCount - index - 1);
		cellForegrounds = newCellForegrounds;
	}
	if (cellFonts != null) {
		Font[] newCellFonts = new Font [columnCount];
		System.arraycopy (cellFonts, 0, newCellFonts, 0, index);
		System.arraycopy (cellFonts, index, newCellFonts, index + 1, columnCount - index - 1);
		cellFonts = newCellFonts;

		int[] newFontHeights = new int [columnCount];
		System.arraycopy (fontHeights, 0, newFontHeights, 0, index);
		System.arraycopy (fontHeights, index, newFontHeights, index + 1, columnCount - index - 1);
		fontHeights = newFontHeights;
	}

	if (index == 0 && columnCount > 1) {
		/*
		 * The new second column now has more space available to it than it did while it
		 * was the first column since it no longer has to show hierarchy decorations, so
		 * recompute its displayText.
		 */
		GC gc = new GC (parent);
		gc.setFont (getFont (1));
		computeDisplayText (1, gc);
		gc.dispose ();
	}
}
static Table checkNull (Table table) {
	if (table == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	return table;
}
void clear () {
	checked = grayed = false;
	texts = null;
	int[] textWidths = new int [1];
	fontHeight = 0;
	fontHeights = null;
	images = null;
	foreground = background = null;
	displayTexts = null;
	cellForegrounds = cellBackgrounds = null;
	font = null;
	cellFonts = null;
	cached = false;

	int columnCount = parent.columns.length;
	if (columnCount > 0) {
		displayTexts = new String [columnCount];
		if (columnCount > 1) {
			texts = new String [columnCount];
			textWidths = new int [columnCount];
			images = new Image [columnCount];
		}
	}
}
void computeDisplayText (int columnIndex, GC gc) {
	int columnCount = parent.columns.length;
	if (columnCount == 0) return;
	
	TableColumn column = parent.columns [columnIndex];
	int availableWidth = column.width - 2 * parent.getCellPadding () - 2 * MARGIN_TEXT;
	Image image = getImage (columnIndex);
	if (image != null) {
		availableWidth -= image.getBounds ().width;
		availableWidth -= Table.MARGIN_IMAGE;
	}

	String text = getText (columnIndex);
	int textWidth = gc.textExtent (text).x;
	if (textWidth <= availableWidth) {
		displayTexts [columnIndex] = text;
		return;
	}
	
	/* Ellipsis will be needed, so subtract their width from the available text width */
	int ellipsisWidth = gc.textExtent (Table.ELLIPSIS).x;
	availableWidth -= ellipsisWidth;
	if (availableWidth <= 0) {
		displayTexts [columnIndex] = Table.ELLIPSIS;
		return;
	}
	
	/* Make initial guess. */
	int index = availableWidth / gc.getFontMetrics ().getAverageCharWidth ();
	textWidth = gc.textExtent (text.substring (0, index)).x;

	/* Initial guess is correct. */
	if (availableWidth == textWidth) {
		displayTexts [columnIndex] = text.substring (0, index) + Table.ELLIPSIS;
		return;
	}

	/* Initial guess is too high, so reduce until fit is found. */
	if (availableWidth < textWidth) {
		do {
			index--;
			if (index < 0) {
				displayTexts [columnIndex] = Table.ELLIPSIS;
				return;
			}
			text = text.substring (0, index);
			textWidth = gc.textExtent (text).x;
		} while (availableWidth < textWidth);
		displayTexts [columnIndex] = text + Table.ELLIPSIS;
		return;
	}
	
	/* Initial guess is too low, so increase until overrun is found. */
	while (textWidth < availableWidth) {
		index++;
		textWidth = gc.textExtent (text.substring (0, index)).x;
	}
	displayTexts [columnIndex] = text.substring (0, index - 1) + Table.ELLIPSIS;
}
void computeDisplayTexts (GC gc) {
	int columnCount = parent.columns.length;
	if (columnCount == 0) return;
	
	Font oldFont = gc.getFont ();
	for (int i = 0; i < columnCount; i++) {
		boolean fontChanged = false;
		Font font = getFont (i);
		if (!font.equals (oldFont)) {
			gc.setFont (font);
			fontChanged = true;
		}
		computeDisplayText (i, gc);
		if (fontChanged) gc.setFont (oldFont);
	}
}
public void dispose () {
	if (isDisposed ()) return;
	Table parent = this.parent;
	int startIndex = index;
	int endIndex = parent.items.length - 1;
	dispose (true);
	parent.redrawItems (startIndex, endIndex, false);
}
void dispose (boolean notifyParent) {
	if (isDisposed ()) return;
	if (notifyParent) parent.destroyItem (this);
	super.dispose ();	/* super is intentional here */
	background = foreground = null;
	cellBackgrounds = cellForegrounds = null;
	font = null;
	cellFonts = null;
	images = null;
	texts = displayTexts = null;
	textWidths = fontHeights = null;
	parent = null;
}
public Color getBackground () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	if (background != null) return background;
	return parent.getBackground ();
}
public Color getBackground (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return getBackground ();
	if (cellBackgrounds == null || cellBackgrounds [columnIndex] == null) return getBackground ();
	return cellBackgrounds [columnIndex];
}
Rectangle getBounds () {
	// TODO is this method needed?
	checkWidget ();
	int textPaintWidth = textWidths [0] + 2 * MARGIN_TEXT;
	return new Rectangle (getTextX (0), parent.getItemY (this), textPaintWidth, parent.itemHeight - 1);
}
public Rectangle getBounds (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	TableColumn[] columns = parent.columns;
	int columnCount = columns.length;
	int validColumnCount = Math.max (1, columnCount);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) {
		return new Rectangle (0, 0, 0, 0);
	}
	/*
	 * If there are no columns then this is the bounds of the receiver's content.
	 */
	if (columnCount == 0) {
		int width = getContentWidth (0);
		return new Rectangle (
			getContentX (0),
			parent.getItemY (this),
			width,
			parent.itemHeight - 1);
	}
	
	TableColumn column = columns [columnIndex];
	if (columnIndex == 0) {
		/* 
		 * For column 0 this is bounds from the beginning of the content to the
		 * end of the column.
		 */
		int x = getContentX (0);
		int offset = x - column.getX ();
		int width = Math.max (0, column.width - offset);		/* max is for columns with small widths */
		return new Rectangle (x, parent.getItemY (this), width, parent.itemHeight - 1);
	}
	/*
	 * For columns > 0 this is the bounds of the table cell.
	 */
	return new Rectangle (column.getX (), parent.getItemY (this), column.width, parent.itemHeight - 1);
}
/*
 * Returns the full bounds of a cell in a table, regardless of its content.
 */
Rectangle getCellBounds (int columnIndex) {
	int y = parent.getItemY (this);
	if (parent.columns.length == 0) {
		int textPaintWidth = textWidths [0] + 2 * MARGIN_TEXT;
		int width = getTextX (0) + textPaintWidth + parent.horizontalOffset;
		return new Rectangle (-parent.horizontalOffset, y, width, parent.itemHeight);
	}
	TableColumn column = parent.columns [columnIndex];
	return new Rectangle (column.getX (), y, column.width, parent.itemHeight);
}
/*
 * Returns the bounds of the receiver's checkbox, or null if the parent's style does not
 * include SWT.CHECK.
 */
Rectangle getCheckboxBounds () {
	if ((parent.getStyle () & SWT.CHECK) == 0) return null;
	Rectangle result = parent.checkboxBounds;
	result.x = parent.getCellPadding () - parent.horizontalOffset;
	result.y = parent.getItemY (this) + (parent.itemHeight - result.height) / 2;
	return result;
}
public boolean getChecked () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	return checked;
}
int getContentWidth (int columnIndex) {
	int width = textWidths [columnIndex] + 2 * MARGIN_TEXT;
	if (columnIndex == 0) {
		width += parent.col0ImageWidth;
		if (parent.col0ImageWidth > 0) width += Table.MARGIN_IMAGE;
	} else {
		Image image = getImage (columnIndex);
		if (image != null) {
			width += image.getBounds ().width + Table.MARGIN_IMAGE;
		}
	}
	return width;
}
/*
 * Returns the x value where the receiver's content (ie.- its image or text) begins
 * for the specified column.
 */
int getContentX (int columnIndex) {
	int minX = parent.getCellPadding ();
	if (columnIndex == 0) {
		Rectangle checkboxBounds = getCheckboxBounds ();
		if (checkboxBounds != null) {
			minX += checkboxBounds.width + Table.MARGIN_IMAGE;
		}
	}

	if (parent.columns.length == 0) return minX - parent.horizontalOffset;	/* free first column */
	
	TableColumn column = parent.columns [columnIndex];
	int columnX = column.getX ();
	if ((column.style & SWT.LEFT) != 0) return columnX + minX;
	
	/* column is not left-aligned */
	int contentWidth = getContentWidth (columnIndex);
	int contentX = 0;
	if ((column.style & SWT.RIGHT) != 0) {
		contentX = column.width - parent.getCellPadding () - contentWidth;	
	} else {	/* SWT.CENTER */
		contentX = (column.width - contentWidth) / 2;
	}
	return Math.max (columnX + minX, columnX + contentX);
}
String getDisplayText (int columnIndex) {
	if (parent.columns.length == 0) return getText (0);
	String result = displayTexts [columnIndex];
	return result != null ? result : "";	//$NON-NLS-1$
}
/*
 * Returns the bounds that should be used for drawing a focus rectangle on the receiver
 */
Rectangle getFocusBounds () {
	int x = getTextX (0);
	int width;
	TableColumn[] columns = parent.columns;
	if (columns.length == 0) {
		width = textWidths [0] + 2 * MARGIN_TEXT;
	} else {
		TableColumn column;
		if ((parent.style & SWT.FULL_SELECTION) != 0) {
			column = columns [columns.length - 1];
		} else {
			column = columns [0];
		}
		width = column.getX () + column.width - x - 1;
	}
	return new Rectangle (x, parent.getItemY (this) + 1, width, parent.itemHeight - 1);
}
public Font getFont () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	if (font != null) return font;
	return parent.getFont ();
}
public Font getFont (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return getFont ();
	if (cellFonts == null || cellFonts [columnIndex] == null) return getFont ();
	return cellFonts [columnIndex];
}
int getFontHeight () {
	if (fontHeight != 0) return fontHeight;
	return parent.fontHeight;
}
int getFontHeight (int columnIndex) {
	if (fontHeights == null || fontHeights [columnIndex] == 0) return getFontHeight ();
	return fontHeights [columnIndex];
}
public Color getForeground () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	if (foreground != null) return foreground;
	return parent.getForeground ();
}
public Color getForeground (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return getForeground ();
	if (cellForegrounds == null || cellForegrounds [columnIndex] == null) return getForeground ();
	return cellForegrounds [columnIndex];
}
public boolean getGrayed () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	return grayed;
}
/*
 * Returns the bounds representing the clickable region that should select the receiver.
 */
Rectangle getHitBounds () {
	int contentX = getContentX (0);
	int width = 0;
	TableColumn[] columns = parent.columns;
	if (columns.length == 0) {
		width = getContentWidth (0); 
	} else {
		/* 
		 * If there are columns then this spans from the beginning of the receiver's column 0
		 * image or text to the end of either column 0 or the last column (FULL_SELECTION).
		 */
		TableColumn column;
		if ((parent.style & SWT.FULL_SELECTION) != 0) {
			column = columns [columns.length - 1];
		} else {
			column = columns [0];
		}
		width = column.getX () + column.width - contentX;
	}
	return new Rectangle (contentX, parent.getItemY (this), width, parent.itemHeight);
}
public Image getImage () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	return super.getImage ();
}
public Image getImage (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return null;
	if (columnIndex == 0) return getImage ();
	return images [columnIndex];
}
public Rectangle getImageBounds (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return new Rectangle (0,0,0,0);

	int padding = parent.getCellPadding ();
	int startX = getContentX (columnIndex);
	int itemHeight = parent.itemHeight;
	int imageSpaceY = itemHeight - 2 * padding;
	int y = parent.getItemY (this);
	Image image = getImage (columnIndex); 
	if (image == null) {
		return new Rectangle (startX, y + padding, 0, imageSpaceY);
	}
	
	Rectangle imageBounds = image.getBounds ();
	/* 
	 * For column 0 all images have the same width, which may be larger or smaller
	 * than the image to be drawn here.  Therefore the image bounds to draw must be
	 * specified.
	 */
	int drawWidth;
	if (columnIndex == 0) {
		int imageSpaceX = parent.col0ImageWidth;
		drawWidth = Math.min (imageSpaceX, imageBounds.width);
	} else {
		drawWidth = imageBounds.width;
	}
	int drawHeight = Math.min (imageSpaceY, imageBounds.height);
	return new Rectangle (
		startX, y + (itemHeight - drawHeight) / 2,
		drawWidth, drawHeight);
}
public int getImageIndent () {
	checkWidget();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	return imageIndent;	// TODO
}
public Table getParent () {
	checkWidget ();
	return parent;
}
/*
 * Returns the receiver's ideal width for the specified columnIndex.
 */
int getPreferredWidth (int columnIndex) {
	GC gc = new GC (parent);
	gc.setFont (getFont (columnIndex));
	int textPaintWidth = gc.textExtent (getText (columnIndex)).x + 2 * MARGIN_TEXT;
	gc.dispose ();
	int result = getTextX (columnIndex) + textPaintWidth + parent.getCellPadding ();	/* right side cell pad */
	result -= parent.columns [columnIndex].getX ();
	return result;
}
public String getText () {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	return super.getText ();
}
public String getText (int columnIndex) {
	checkWidget ();
	if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return "";	//$NON-NLS-1$
	if (columnIndex == 0) return getText ();
	if (texts [columnIndex] == null) return "";	//$NON-NLS-1$
	return texts [columnIndex];
}
/*
 * Returns the x value where the receiver's text begins.
 */
int getTextX (int columnIndex) {
	int textX = getContentX (columnIndex);
	if (columnIndex == 0) {
		textX += parent.col0ImageWidth;
		if (parent.col0ImageWidth > 0) textX += Table.MARGIN_IMAGE;
	} else {
		Image image = getImage (columnIndex);
		if (image != null) {
			textX += image.getBounds ().width + Table.MARGIN_IMAGE;	
		}
	}
	return textX;
}
boolean isSelected () {
	return parent.getSelectionIndex (this) != -1;
}
/*
 * The paintCellContent argument indicates whether the item should paint
 * its cell contents (ie.- its text, image and check) in addition
 * to its item-level attributes (ie.- background color and selection).
 */
void paint (GC gc, TableColumn column, boolean paintCellContent) {
	if (!parent.checkData (this, true)) return;
	int columnIndex = 0, x = 0;
	if (column != null) {
		columnIndex = column.getIndex ();
		x = column.getX ();
	}
	/* if this cell is completely to the right of the client area then there's no need to paint it */
	Rectangle clientArea = parent.getClientArea ();
	if (clientArea.x + clientArea.width < x) return;

	Rectangle cellBounds = getCellBounds (columnIndex);
	int cellRightX = 0;
	if (column != null) {
		cellRightX = column.getX () + column.width;
	} else {
		cellRightX = cellBounds.x + cellBounds.width;
	}

	/* if this cell is completely to the left of the client area then there's no need to paint it */
	if (cellRightX < 0) return;

	/* restrict the clipping region to the cell */
	gc.setClipping (x, cellBounds.y, cellRightX - x, cellBounds.height);
	
	int y = parent.getItemY (this);
	int padding = parent.getCellPadding ();
	int itemHeight = parent.itemHeight;

	/* draw the background color if this item has a custom color set */
	Color background = getBackground (columnIndex);
	if (!background.equals (parent.getBackground ())) {
		Color oldBackground = gc.getBackground ();
		gc.setBackground (background);
		if (columnIndex == 0) {
			Rectangle focusBounds = getFocusBounds ();
			int fillWidth = 0;
			if (column == null) {
				fillWidth = focusBounds.width;
			} else {
				fillWidth = column.width - focusBounds.x;
				if (parent.linesVisible) fillWidth--;
			}
			gc.fillRectangle (focusBounds.x, focusBounds.y, fillWidth, focusBounds.height);
		} else {
			int fillWidth = cellBounds.width;
			if (parent.linesVisible) fillWidth--;
			gc.fillRectangle (cellBounds.x, cellBounds.y + 1, fillWidth, cellBounds.height - 1);
		}
		gc.setBackground (oldBackground);
	}

	/* draw the selection bar if the receiver is selected */
	if (isSelected () && (columnIndex == 0 || (parent.style & SWT.FULL_SELECTION) != 0)) {
		if (parent.hasFocus () || (parent.style & SWT.HIDE_SELECTION) == 0) {
			Color oldBackground = gc.getBackground ();
			gc.setBackground (display.getSystemColor (SWT.COLOR_LIST_SELECTION));
			if (columnIndex == 0) {
				Rectangle focusBounds = getFocusBounds ();
				int fillWidth = focusBounds.width;
				if (parent.columns.length < 2 || (parent.style & SWT.FULL_SELECTION) == 0) {
					fillWidth -= 2;	/* space for right bound of focus rect */
				}
				gc.fillRectangle (focusBounds.x + 1, focusBounds.y + 1, fillWidth, focusBounds.height - 2);
			} else {
				int fillWidth = column.width;
				if (columnIndex == parent.columns.length - 1) {
					fillWidth -= 2;		/* space for right bound of focus rect */
				}
				gc.fillRectangle (column.getX (), y + 2, fillWidth, itemHeight - 3);
			}
			gc.setBackground (oldBackground);
		}
	}

	if (!paintCellContent) return;

	/* Draw checkbox if drawing column 0 and parent has style SWT.CHECK */
	if (columnIndex == 0 && (parent.style & SWT.CHECK) != 0) {
		Image baseImage = grayed ? parent.getGrayUncheckedImage () : parent.getUncheckedImage ();
		Rectangle checkboxBounds = getCheckboxBounds ();
		gc.drawImage (baseImage, checkboxBounds.x, checkboxBounds.y);
		/* Draw checkmark if item is checked */
		if (checked) {
			Image checkmarkImage = parent.getCheckmarkImage ();
			Rectangle checkmarkBounds = checkmarkImage.getBounds ();
			int xInset = (checkboxBounds.width - checkmarkBounds.width) / 2;
			int yInset = (checkboxBounds.height - checkmarkBounds.height) / 2;
			gc.drawImage (checkmarkImage, checkboxBounds.x + xInset, checkboxBounds.y + yInset);
		}
	}

	Image image = getImage (columnIndex);
	String text = getDisplayText (columnIndex);
	Rectangle imageArea = getImageBounds (columnIndex);
	int startX = imageArea.x;
	
	/* while painting the cell's content restrict the clipping region */
	gc.setClipping (
		startX,
		cellBounds.y + padding,
		cellRightX - startX - padding,
		cellBounds.height - (2 * padding));

	/* draw the image */
	if (image != null) {
		Rectangle imageBounds = image.getBounds ();
		gc.drawImage (
			image,
			0, 0,									/* source x, y */
			imageBounds.width, imageBounds.height,	/* source width, height */
			imageArea.x, imageArea.y,				/* dest x, y */
			imageArea.width, imageArea.height);		/* dest width, height */
	}
	
	/* draw the text */
	if (text.length () > 0) {
		boolean fontChanged = false, foregroundChanged = false;
		Font oldFont = gc.getFont ();
		Font font = getFont (columnIndex);
		if (!font.equals (oldFont)) {
			gc.setFont (font);
			fontChanged = true;
		}
		int fontHeight = getFontHeight (columnIndex);
		Color oldForeground = gc.getForeground ();
		if (isSelected () && (columnIndex == 0 || (parent.style & SWT.FULL_SELECTION) != 0)) {
			if (parent.hasFocus () || (parent.style & SWT.HIDE_SELECTION) == 0) {
				gc.setForeground (display.getSystemColor (SWT.COLOR_LIST_SELECTION_TEXT));
				foregroundChanged = true;
			}
		} else {
			Color foreground = getForeground (columnIndex);
			if (!foreground.equals (oldForeground)) {
				gc.setForeground (foreground);
				foregroundChanged = true;
			}
		}
		x = getTextX (columnIndex) + MARGIN_TEXT;
		gc.drawString (text, x, y + (itemHeight - fontHeight) / 2, true);
		if (foregroundChanged) gc.setForeground (oldForeground);
		if (fontChanged) gc.setFont (oldFont);
	}
}
/*
 * Recomputes the cached text widths.
 */
void recomputeTextWidths (GC gc) {
	int validColumnCount = Math.max (1, parent.columns.length);
	textWidths = new int [validColumnCount];
	Font oldFont = gc.getFont ();
	for (int i = 0; i < textWidths.length; i++) {
		String value = getDisplayText (i);
		if (value != null) {
			boolean fontChanged = false;
			Font font = getFont (i);
			if (!font.equals (oldFont)) {
				gc.setFont (font);
				fontChanged = true;
			}
			textWidths [i] = gc.textExtent (value).x;
			if (fontChanged) gc.setFont (oldFont);
		}
	}
}
void redrawItem () {
	parent.redraw (0, parent.getItemY (this), parent.getClientArea ().width, parent.itemHeight, false);
}
/*
 * Updates internal structures in the receiver and its child items to handle the removal of a column.
 */
void removeColumn (TableColumn column, int index) {
	int columnCount = parent.columns.length;

	if (columnCount == 0) {
		/* reverts to normal table when last column disposed */
		cellBackgrounds = cellForegrounds = null;
		displayTexts = null;
		cellFonts = null;
		fontHeights = null;
		GC gc = new GC (parent);
		gc.setFont (getFont ());
		recomputeTextWidths (gc);
		gc.dispose ();
		return;
	}

	String[] newTexts = new String [columnCount];
	System.arraycopy (texts, 0, newTexts, 0, index);
	System.arraycopy (texts, index + 1, newTexts, index, columnCount - index);
	texts = newTexts;
	
	Image[] newImages = new Image [columnCount];
	System.arraycopy (images, 0, newImages, 0, index);
	System.arraycopy (images, index + 1, newImages, index, columnCount - index);
	images = newImages;

	int[] newTextWidths = new int [columnCount];
	System.arraycopy (textWidths, 0, newTextWidths, 0, index);
	System.arraycopy (textWidths, index + 1, newTextWidths, index, columnCount - index);
	textWidths = newTextWidths;

	String[] newDisplayTexts = new String [columnCount];
	System.arraycopy (displayTexts, 0, newDisplayTexts, 0, index);
	System.arraycopy (displayTexts, index + 1, newDisplayTexts, index, columnCount - index);
	displayTexts = newDisplayTexts;

	if (cellBackgrounds != null) {
		Color[] newCellBackgrounds = new Color [columnCount];
		System.arraycopy (cellBackgrounds, 0, newCellBackgrounds, 0, index);
		System.arraycopy (cellBackgrounds, index + 1, newCellBackgrounds, index, columnCount - index);
		cellBackgrounds = newCellBackgrounds;
	}
	if (cellForegrounds != null) {
		Color[] newCellForegrounds = new Color [columnCount];
		System.arraycopy (cellForegrounds, 0, newCellForegrounds, 0, index);
		System.arraycopy (cellForegrounds, index + 1, newCellForegrounds, index, columnCount - index);
		cellForegrounds = newCellForegrounds;
	}
	if (cellFonts != null) {
		Font[] newCellFonts = new Font [columnCount];
		System.arraycopy (cellFonts, 0, newCellFonts, 0, index);
		System.arraycopy (cellFonts, index + 1, newCellFonts, index, columnCount - index);
		cellFonts = newCellFonts;

		int[] newFontHeights = new int [columnCount];
		System.arraycopy (fontHeights, 0, newFontHeights, 0, index);
		System.arraycopy (fontHeights, index + 1, newFontHeights, index, columnCount - index);
		fontHeights = newFontHeights;
	}

	if (index == 0) {
		text = texts [0] != null ? texts [0] : "";	//$NON-NLS-1$
		texts [0] = null;
		image = images [0];
		images [0] = null;
		/* 
		 * The new first column will not have as much width available to it as it did when it was
		 * the second column since it now has to show hierarchy decorations as well, so recompute
		 * its displayText. 
		 */
		GC gc = new GC (parent);
		gc.setFont (getFont (0));
		computeDisplayText (0, gc);
		gc.dispose ();
	}
	if (columnCount < 2) {
		texts = null;
		images = null;
	}
}
public void setBackground (Color value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	if (background == value) return;
	if (background != null && background.equals (value)) return;
	background = value;
	redrawItem ();
}
public void setBackground (int columnIndex, Color value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return;
	if (cellBackgrounds == null) {
		cellBackgrounds = new Color [validColumnCount];
	}
	if (cellBackgrounds [columnIndex] == value) return;
	if (cellBackgrounds [columnIndex] != null && cellBackgrounds [columnIndex].equals (value)) return;
	cellBackgrounds [columnIndex] = value;

	Rectangle bounds = getCellBounds (columnIndex);
	parent.redraw (bounds.x, bounds.y, bounds.width, bounds.height, false);
}
public void setChecked (boolean value) {
	checkWidget ();
	if ((parent.getStyle () & SWT.CHECK) == 0) return;
	if (checked == value) return;
	checked = value;

	Rectangle bounds = getCheckboxBounds ();
	parent.redraw (bounds.x, bounds.y, bounds.width, bounds.height, false);
}
public void setFont (Font value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	if (font == value) return;
	if (value != null && value.equals (font)) return;
	
	Rectangle bounds = getBounds ();
	int oldRightX = bounds.x + bounds.width;
	font = value;
	
	/* recompute cached values for string measurements */
	GC gc = new GC (parent);
	gc.setFont (getFont ());
	computeDisplayTexts (gc);
	recomputeTextWidths (gc);
	fontHeight = gc.getFontMetrics ().getHeight ();
	gc.dispose ();
	
	/* horizontal bar could be affected if table has no columns */
	if (parent.columns.length == 0) {
		bounds = getBounds ();
		int newRightX = bounds.x + bounds.width;
		parent.updateHorizontalBar (newRightX, newRightX - oldRightX);
	}
	redrawItem ();
}
public void setFont (int columnIndex, Font value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}

	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return;
	if (cellFonts == null) cellFonts = new Font [validColumnCount];
	if (cellFonts [columnIndex] == value) return;
	if (cellFonts [columnIndex] != null && cellFonts [columnIndex].equals (value)) return;
	cellFonts [columnIndex] = value;
	
	/* recompute cached values for string measurements */
	GC gc = new GC (parent);
	gc.setFont (getFont (columnIndex));
	if (fontHeights == null) fontHeights = new int [validColumnCount];
	fontHeights [columnIndex] = gc.getFontMetrics ().getHeight ();
	computeDisplayText (columnIndex, gc);
	textWidths [columnIndex] = gc.textExtent (getDisplayText (columnIndex)).x;
	gc.dispose ();

	Rectangle bounds = getCellBounds (columnIndex);
	parent.redraw (bounds.x, bounds.y, bounds.width, bounds.height, false);
}
public void setForeground (Color value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	if (foreground == value) return;
	if (foreground != null && foreground.equals (value)) return;
	foreground = value;
	redrawItem ();
}
public void setForeground (int columnIndex, Color value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return;
	if (cellForegrounds == null) {
		cellForegrounds = new Color [validColumnCount];
	}
	if (cellForegrounds [columnIndex] == value) return;
	if (cellForegrounds [columnIndex] != null && cellForegrounds [columnIndex].equals (value)) return;
	cellForegrounds [columnIndex] = value;

	parent.redraw (
		getTextX (columnIndex),
		parent.getItemY (this),
		textWidths [columnIndex] + 2 * MARGIN_TEXT,
		parent.itemHeight,
		false);
}
public void setGrayed (boolean value) {
	checkWidget ();
	if ((parent.getStyle () & SWT.CHECK) == 0) return;
	if (grayed == value) return;
	grayed = value;

	Rectangle bounds = getCheckboxBounds ();
	parent.redraw (bounds.x, bounds.y, bounds.width, bounds.height, false);
}
public void setImage (Image value) {
	checkWidget ();
	setImage (0, value);
}
public void setImage (Image[] value) {
	checkWidget ();
	if (value == null) error (SWT.ERROR_NULL_ARGUMENT);
	
	// TODO make a smarter implementation of this
	for (int i = 0; i < value.length; i++) {
		if (value [i] != null) setImage (i, value [i]);
	}
}
public void setImage (int columnIndex, Image value) {
	checkWidget ();
	if (value != null && value.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);

	TableColumn[] columns = parent.columns;
	int validColumnCount = Math.max (1, columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return;
	Image image = getImage (columnIndex);
	if (value == image) return;
	if (value != null && value.equals (image)) return;
	if (columnIndex == 0) {
		super.setImage (value);
	} else {
		images [columnIndex] = value;
	}
	
	/* 
	 * An image width change may affect the space available for the item text, so
	 * recompute the displayText if there are columns.
	 */
	if (columns.length > 0) {
		GC gc = new GC (parent);
		gc.setFont (getFont (columnIndex));
		computeDisplayText (columnIndex, gc);
		textWidths [columnIndex] = gc.textExtent (getDisplayText (columnIndex)).x;
		gc.dispose ();
	}
	
	if (value == null) {
		redrawItem ();
		return;
	}

	/*
	 * If this is the first image being put into the table then its item height
	 * may be adjusted, in which case a full redraw is needed.
	 */
	if (parent.imageHeight == 0) {
		int oldItemHeight = parent.itemHeight;
		parent.setImageHeight (value.getBounds ().height);
		if (oldItemHeight != parent.itemHeight) {
			if (columnIndex == 0) {
				parent.col0ImageWidth = value.getBounds ().width;
				if (columns.length > 0) {
					/* 
					 * All column 0 cells will now have less room available for their texts,
					 * so all items must now recompute their column 0 displayTexts.
					 */
					GC gc = new GC (parent);
					TableItem[] rootItems = parent.items;
					for (int i = 0; i < rootItems.length; i++) {
						rootItems [i].updateColumnWidth (columns [0], gc);
					}
					gc.dispose ();
				}
			}
			parent.redraw ();
			return;
		}
	}

	/* 
	 * If this is the first image being put into column 0 then all cells
	 * in the column should also indent accordingly. 
	 */
	if (columnIndex == 0 && parent.col0ImageWidth == 0) {
		parent.col0ImageWidth = value.getBounds ().width;
		/* redraw the column */
		if (columns.length == 0) {
			parent.redraw ();
		} else {
			/* 
			 * All column 0 cells will now have less room available for their texts,
			 * so all items must now recompute their column 0 displayTexts.
			 */
			GC gc = new GC (parent);
			TableItem[] rootItems = parent.items;
			for (int i = 0; i < rootItems.length; i++) {
				rootItems [i].updateColumnWidth (columns [0], gc);
			}
			gc.dispose ();
			parent.redraw (
				0, 0,
				columns [0].width,
				parent.getClientArea ().height,
				true);
		}
		return;
	}
	redrawItem ();
}
public void setImageIndent (int indent) {
	checkWidget();
	if (indent < 0) return;
	if (imageIndent == indent) return;
	imageIndent = indent;
}
public void setText (String value) {
	checkWidget ();
	Rectangle bounds = getBounds ();
	int oldRightX = bounds.x + bounds.width;
	setText (0, value);
	/* horizontal bar could be affected if table has no columns */
	if (parent.columns.length == 0) {
		bounds = getBounds ();
		int newRightX = bounds.x + bounds.width;
		parent.updateHorizontalBar (newRightX, newRightX - oldRightX);
	}
}
public void setText (String[] value) {
	checkWidget ();
	if (value == null) error (SWT.ERROR_NULL_ARGUMENT);
	Rectangle bounds = getBounds ();
	int oldRightX = bounds.x + bounds.width;
	// TODO make a smarter implementation of this
	for (int i = 0; i < value.length; i++) {
		if (value [i] != null) setText (i, value [i]);
	}
	/* horizontal bar could be affected if table has no columns */
	if (parent.columns.length == 0) {
		bounds = getBounds ();
		int newRightX = bounds.x + bounds.width;
		parent.updateHorizontalBar (newRightX, newRightX - oldRightX);
	}
}
public void setText (int columnIndex, String value) {
	checkWidget ();
	if (value == null) error (SWT.ERROR_NULL_ARGUMENT);
	int validColumnCount = Math.max (1, parent.columns.length);
	if (!(0 <= columnIndex && columnIndex < validColumnCount)) return;
	if (value.equals (getText (columnIndex))) return;
	if (columnIndex == 0) {
		super.setText (value);
	} else {
		texts [columnIndex] = value;		
	}
	
	int oldWidth = textWidths [columnIndex];
	GC gc = new GC (parent);
	gc.setFont (getFont (columnIndex));
	computeDisplayText (columnIndex, gc);
	textWidths [columnIndex] = gc.textExtent (getDisplayText (columnIndex)).x;
	gc.dispose ();

	if (parent.columns.length == 0) {
		Rectangle bounds = getBounds ();
		int rightX = bounds.x + bounds.width;
		parent.updateHorizontalBar (rightX, textWidths [columnIndex] - oldWidth);
	}
	parent.redraw (
		getTextX (columnIndex),
		parent.getItemY (this),
		Math.max (oldWidth, textWidths [columnIndex]) + 2 * MARGIN_TEXT,
		parent.itemHeight,
		false);
}
void updateColumnWidth (TableColumn column, GC gc) {
	int columnIndex = column.getIndex ();
	boolean fontChanged = false;
	Font oldFont = gc.getFont ();
	Font font = getFont (columnIndex);
	if (!font.equals(oldFont)) {
		gc.setFont (font);
		fontChanged = true;
	}
	computeDisplayText (columnIndex, gc);
	textWidths [columnIndex] = gc.textExtent (getDisplayText (columnIndex)).x;
	if (fontChanged) gc.setFont (oldFont);
}
/*
 * The parent's font has changed, so if this font was being used by the receiver then
 * recompute its cached text sizes using the gc argument.
 */
void updateFont (GC gc) {
	if (font == null) {		/* receiver is using the Table's font */
		computeDisplayTexts (gc);
		recomputeTextWidths (gc);
	}
}
}
