package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

/**
 * This class provides API for StyledText to implement bidirectional text
 * functions.
 * Objects of this class are created for a single line of text.
 */
class StyledTextBidi {
	private GC gc;
	private int[] bidiSegments;		// bidi text segments, each segment will be rendered separately
	private int[] renderPositions;	// x position at which characters of the line are rendered, in visual order
	private int[] order;			// reordering indices in logical order, iV=order[iL] (iV=visual index, iL=logical index),
									// if no character in a line needs reordering all iV and iL are the same.
	private int[] dx;				// distance between character cells. in visual order. renderPositions[iV + 1] = renderPositions[iV] + dx[iV]
	private byte[] classBuffer;		// the character types in logical order, see BidiUtil for the possible types
	private char[] glyphBuffer;		// the glyphs in visual order as they will be rendered on screen.

	/** 
	 * This class describes a text segment of a single direction, either 
	 * left-to-right (L2R) or right-to-left (R2L). 
	 * Objects of this class are used by StyledTextBidi rendering methods 
	 * to render logically contiguous text segments that may be visually 
	 * discontiguous if they consist of different directions.
	 */
	class DirectionRun {
		int logicalStart;
		int logicalEnd;

		DirectionRun(int logicalStart, int logicalEnd) {
			this.logicalStart = logicalStart;
			this.logicalEnd = logicalEnd;
		}		
		int getVisualStart() {
			int visualStart = order[logicalStart];
			int visualEnd = order[logicalEnd];
			// the visualStart of a R2L direction run is actually
			// at the run's logicalEnd, answered as such since rendering 
			// always occurs from L2R regardless of the text run's
			// direction
			if (visualEnd < visualStart) {
				visualStart = visualEnd;
			}
			return visualStart;
		}
		int getVisualEnd() {
			int visualStart = order[logicalStart];
			int visualEnd = order[logicalEnd];
			// the visualEnd of a R2L direction run is actually
			// at the run's logicalStart, answered as such since rendering 
			// always occurs from L2R regardless of the text run's
			// direction
			if (visualEnd < visualStart) {
				visualEnd = visualStart;
			}
			return visualEnd;
		}
		int getRenderStartX() {
			return renderPositions[getVisualStart()];
		}
		int getRenderStopX() {
			int visualEnd = getVisualEnd();
			
			return renderPositions[visualEnd] + dx[visualEnd];
		}
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("vStart,Stop:" + getVisualStart()  + "," + getVisualEnd() + " lStart,Stop:" + logicalStart  + "," + logicalEnd + " renderStart,Stop: " + getRenderStartX() + "," + getRenderStopX());
			return buf.toString();
		}
	}

/**
 * Constructs an instance of this class for a line of text. The text 
 * is reordered to reflect how it will be displayed.
 * <p>
 * 
 * @param gc the GC to use for rendering and measuring of this line.
 * @param tabWidth tab width in number of spaces, used to calculate 
 * 	tab stops
 * @param text line that bidi data should be calculated for
 * @param boldRanges bold text segments in the line, specified as 
 * 	i=bold start,i+1=bold length
 * @param boldFont font that bold text will be rendered in, needed for 
 * 	proper measuring of bold text segments.
 * @param offset text segments that should be measured and reordered 
 * 	separately, may be needed to preserve the order of separate R2L 
 * 	segments to each other
 */
public StyledTextBidi(GC gc, int tabWidth, String text, int[] boldRanges, Font boldFont, int[] offsets) {
	int length = text.length();
		
	this.gc = gc;
	bidiSegments = offsets;
	renderPositions = new int[length];
	order = new int[length];
	dx = new int[length];
	classBuffer = new byte[length];
	if (length == 0) {
		glyphBuffer = new char[0];
	}
	else {
		glyphBuffer = BidiUtil.getRenderInfo(gc, text, order, classBuffer, dx, 0, offsets);
		if (boldRanges != null) {
			Font normalFont = gc.getFont();
			gc.setFont(boldFont);
			// If the font supports characters shaping, break up the bold ranges based on 
			// the specified bidi segments.  Each bidi segment will be treated separately 
			// for bold purposes.
			int[] segmentedBoldRanges;
			if (isCharacterShaped(gc)) segmentedBoldRanges = segmentedRangesFor(boldRanges);
			else segmentedBoldRanges = boldRanges;
			for (int i = 0; i < segmentedBoldRanges.length; i += 2) {
				int rangeStart = segmentedBoldRanges[i];
				int rangeLength = segmentedBoldRanges[i + 1];
				// Bold text needs to be processed so that the dx array reflects the bold
				// font.
				prepareBoldText(text, rangeStart, rangeLength);
			}
			gc.setFont(normalFont);
		}
		calculateTabStops(text, tabWidth);
		calculateRenderPositions();
	}
}
/**
 * Constructs an instance of this class for a line of text. This constructor
 * should be used when only ordering (not rendering) information is needed.  
 * Only the class and order arrays will be filled during this call.
 * <p>
 * 
 * @param gc the GC to use for rendering and measuring of this line.
 * @param text line that bidi data should be calculated for
 * @param offset text segments that should be measured and reordered 
 * 	separately, may be needed to preserve the order of separate R2L 
 * 	segments to each other
 */
public StyledTextBidi(GC gc, String text, int[] offsets) {
	int length = text.length();		
	this.gc = gc;
	bidiSegments = offsets;
	order = new int[length];
	classBuffer = new byte[length];
	BidiUtil.getOrderInfo(gc, text, order, classBuffer, 0, offsets);
	// initialize the unused arrays
	dx = new int[0];
	renderPositions = new int[0];
	glyphBuffer = new char[0];

}
/**
 * Adds a listener that should be called when the user changes the 
 * keyboard layout for the specified window.
 * <p>
 * 
 * @param control Control to add the keyboard language listener for.
 * 	Each window has its own keyboard language setting.
 * @param runnable the listener that should be called when the user 
 * 	changes the keyboard layout.
 */
static void addLanguageListener(Control control, Runnable runnable) {
	BidiUtil.addLanguageListener(control.handle, runnable);
}
/**
 * Answers the direction of the active keyboard language - either 
 * L2R or R2L.  The active keyboard language determines the direction 
 * of the caret and can be changed by the user (e.g., via Alt-Shift on
 * Win32 platforms).
 * <p>
 * 
 * @return the direction of the active keyboard language. SWT.LEFT (for L2R
 *  language) or SWT.RIGHT (for R2L language).
 */
static int getKeyboardLanguageDirection() {
	int language = BidiUtil.getKeyboardLanguage();
	if (language == BidiUtil.KEYBOARD_HEBREW) {
		return SWT.RIGHT;
	}
	if (language == BidiUtil.KEYBOARD_ARABIC) {
		return SWT.RIGHT;
	}
	return SWT.LEFT;
}
/**
 * Removes the keyboard language listener for the specified window.
 * <p>
 * 
 * @param control window to remove the keyboard language listener from.
 */
static void removeLanguageListener(Control control) {
	BidiUtil.removeLanguageListener(control.handle);
}
/**
 * Calculates render positions using the glyph distance values in the dx array.
 */
private void calculateRenderPositions() {
	renderPositions = new int[dx.length];	
	renderPositions[0] = StyledText.XINSET;
	for (int i = 0; i < dx.length - 1; i++) {
		renderPositions[i + 1] = renderPositions[i] + dx[i];
	}
}
/**
 * Calculate the line's tab stops and adjust the dx array to 
 * reflect the width of tab characters.
 * <p>
 * 
 * @param text the original line text (not reordered) containing 
 * 	tab characters.
 * @param tabWidth number of pixels that one tab character represents
 */
private void calculateTabStops(String text, int tabWidth) {
	int tabIndex = text.indexOf('\t', 0);
	int logicalIndex = 0;
	int x = StyledText.XINSET;
	int spaceWidth = gc.stringExtent(" ").x;
				
	while (tabIndex != -1) {
		for (; logicalIndex < tabIndex; logicalIndex++) {
			x += dx[order[logicalIndex]];
		}	
		int tabStop = x + tabWidth;
		// make sure tab stop is at least one space width apart 
		// from the last character. fixes 4844.
		if (tabWidth - tabStop % tabWidth < spaceWidth) {
			tabStop += tabWidth;
		}
		tabStop -= tabStop % tabWidth;
		dx[order[tabIndex]] = tabStop - x;
		tabIndex = text.indexOf('\t', tabIndex + 1);
	}
}
/** 
 * Renders the specified text segment.  All text is rendered L2R
 * regardless of the direction of the text.  The rendered text may 
 * be visually discontiguous if the text segment is bidirectional.
 * <p>
 * 
 * @param logicalStart start offset in the logical text
 * @param length number of logical characters to render
 * @param xOffset x location of the line start
 * @param yOffset y location of the line start 
 */
void drawBidiText(int logicalStart, int length, int xOffset, int yOffset) {
	Enumeration directionRuns = getDirectionRuns(logicalStart, length).elements();
	int endOffset = logicalStart + length;

	if (endOffset > getTextLength()) {
		return;
	}
	while (directionRuns.hasMoreElements()) {
		DirectionRun run = (DirectionRun) directionRuns.nextElement();
		int visualStart = run.getVisualStart();
		int visualEnd = run.getVisualEnd();
		int x = xOffset + run.getRenderStartX();
		drawGlyphs(visualStart, visualEnd - visualStart + 1, x, yOffset);				
	}		
}
/**
 * Renders a segment of glyphs. Glyphs are visual objects so the
 * start and length are visual as well.  Glyphs are always rendered L2R.
 * <p>
 * 
 * @param visualStart start offset of the glyphs to render relative to the 
 * 	line start.
 * @param length number of glyphs to render
 * @param x x location to render at
 * @param y y location to render at
 */
private void drawGlyphs(int visualStart, int length, int x, int y) {
	char[] renderBuffer = new char[length];
	int[] renderDx = new int[length];
	if (length == 0) {
		return;
	}	
	System.arraycopy(glyphBuffer, visualStart, renderBuffer, 0, length);
	// copy the distance values for the desired rendering range
	System.arraycopy(dx, visualStart, renderDx, 0, length);	
	BidiUtil.drawGlyphs(gc, renderBuffer, renderDx, x, y);
}
/** 
 * Fills a rectangle spanning the given logical range.
 * The rectangle may be visually discontiguous if the text segment 
 * is bidirectional.
 * <p>
 * 
 * @param logicalStart logcial start offset of the rectangle
 * @param length number of logical characters the rectangle should span
 * @param xOffset x location of the line start
 * @param yOffset y location of the line start
 * @param height height of the rectangle
 */
void fillBackground(int logicalStart, int length, int xOffset, int yOffset, int height) {
	Enumeration directionRuns = getDirectionRuns(logicalStart, length).elements();

	if (logicalStart + length > getTextLength()) {
		return;
	}
	while (directionRuns.hasMoreElements()) {
		DirectionRun run = (DirectionRun) directionRuns.nextElement();
		int startX = run.getRenderStartX();
		gc.fillRectangle(xOffset + startX, yOffset, run.getRenderStopX() - startX, height);	
	}				
}
/**
 * Returns the offset and direction that will be used to position the caret for 
 * the given x location.  The caret will be placed in front of or behind the
 * character at location x depending on what type of character (i.e., R2L or L2R)
 * is at location x.  This method is used for positioning the caret when a mouse
 * click occurs within the widget.
 * <p>
 * 
 * @param x the x location of the character in the line.
 * @return array containing the caret offset and direction for the x location.
 * 	index 0: offset relative to the start of the line
 * 	index 1: direction, either ST.COLUMN_NEXT or ST.COLUMN_PREVIOUS.
 *	The direction is used to control the caret position at direction 
 * 	boundaries. The semantics follow the behavior for keyboard cursor 
 * 	navigation. 
 * 	Example: RRRLLL
 * 	Pressing cursor left (COLUMN_PREVIOUS) in the L2R segment places the cursor 
 * 	in front of the first character of the L2R segment. Pressing cursor right 
 * 	(COLUMN_NEXT) in a R2L segment places the cursor behind the last character 
 * 	of the R2L segment. However, both are the same logical offset.
 */
int[] getCaretOffsetAndDirectionAtX(int x) {
	int lineLength = getTextLength();
	int offset;
	int direction;
	
	if (lineLength == 0) {
		return new int[] {0, 0};
	}		
	int eol = renderPositions[renderPositions.length - 1] + dx[dx.length - 1];
	if (x >= eol) {
		return new int[] {lineLength, ST.COLUMN_NEXT};
	}
	// get the visual offset of the clicked character
	int visualOffset = getVisualOffsetAtX(x);
	// figure out if the character was clicked on the right or left
	int halfway = renderPositions[visualOffset] + dx[visualOffset] / 2;
	boolean visualLeft = (x <= halfway);
	offset = getLogicalOffset(visualOffset);

	if (isRightToLeft(offset)) {
		if (visualLeft) {
			if (isLigated(gc)) {
				// the caret should be positioned after the last
				// character of the ligature
				offset = getLigatureEndOffset(offset);
			}
			offset++;
			// position the caret as if the caret is to the right
			// of the character at location x and the NEXT key is
			// pressed
			direction = ST.COLUMN_NEXT;
		}
		else {
			// position the caret as if the caret is to the left
			// of the character at location x and the PREVIOUS key is
			// pressed
			direction = ST.COLUMN_PREVIOUS;
		}
	}
	else {
		if (visualLeft) {
			// position the caret as if the caret is to the right
			// of the character at location x and the PREVIOUS key is
			// pressed
			direction = ST.COLUMN_PREVIOUS;
		}
		else {
			// position the caret as if the caret is to the left
			// of the character at location x and the NEXT key is
			// pressed
			offset++;
			direction = ST.COLUMN_NEXT;
		}
	}
	return new int[] {offset, direction};		
}
/**
 * Returns the caret position at the specified offset in the line.
 * <p>
 * @param logicalOffset offset of the character in the line
 * @return the caret position at the specified offset in the line.
 */
int getCaretPosition(int logicalOffset) {
	return getCaretPosition(logicalOffset, ST.COLUMN_NEXT);
}
/**
 * Returns the caret position at the specified offset in the line.
 * The direction parameter is used to determine the caret position 
 * at direction boundaries.  If the logical offset is between a R2L 
 * and a L2R segment, pressing cursor left in the L2R segment places
 * the cursor in front of the first character of the L2R segment; whereas
 * pressing cursor right in the R2L segment places the cursor behind 
 * the last character of the R2L segment. However, both caret positions
 * are at the same logical offset.
 * <p>
 * 
 * @param logicalOffset offset of the character in the line
 * @param direction direction the caret moved to the specified location.
 * 	 either ST.COLUMN_NEXT (right cursor key) or ST.COLUMN_PREVIOUS (left cursor key) .
 * @return the caret position at the specified offset in the line, 
 * 	taking the direction into account as described above.
 */
int getCaretPosition(int logicalOffset, int direction) {
	int caretX;
	
	if (getTextLength() == 0) {
		return StyledText.XINSET;
	}
	// at or past end of line?
	if (logicalOffset >= order.length) {
		logicalOffset = Math.min(logicalOffset, order.length - 1);
		int visualOffset = order[logicalOffset];
		if (isRightToLeft(logicalOffset)) {
			caretX = renderPositions[visualOffset];
		}
		else {
			caretX = renderPositions[visualOffset] + dx[visualOffset];
		}
	}
	else
	// at beginning of line?
	if (logicalOffset == 0) {
		int visualOffset = order[logicalOffset];
		if (isRightToLeft(logicalOffset)) {
			caretX = renderPositions[visualOffset] + dx[visualOffset];
		}
		else {
			caretX = renderPositions[visualOffset];
		}
	}
	else
	// consider local numbers as R2L in determining direction boundaries.
	// fixes 1GK9API.
	// treat user specified direction segments like real direction changes.
	if (direction == ST.COLUMN_NEXT &&
		(isRightToLeftInput(logicalOffset) != isRightToLeftInput(logicalOffset - 1) ||
		 isStartOfBidiSegment(logicalOffset))) {
		int visualOffset = order[logicalOffset-1];
		// moving between segments.
		// do not consider local numbers as R2L here, to determine position,
		// because local numbers are navigated L2R and we want the caret to 
		// be to the right of the number. see 1GK9API
		if (isRightToLeft(logicalOffset - 1)) {
			// moving from RtoL to LtoR
			caretX = renderPositions[visualOffset];
		}
		else {
			// moving from LtoR to RtoL
			caretX = renderPositions[visualOffset] + dx[visualOffset];
		}
	}
	else
	// consider local numbers as R2L in determining direction boundaries.
	// fixes 1GK9API.
	if (direction == ST.COLUMN_PREVIOUS &&
		isRightToLeftInput(logicalOffset) != isRightToLeftInput(logicalOffset - 1)) {
		int visualOffset = order[logicalOffset];
		// moving between segments.
		// consider local numbers as R2L here, to determine position, because
		// we want to stay in L2R segment and place the cursor to the left of
		// first L2R character. see 1GK9API
		if (isRightToLeftInput(logicalOffset - 1)) {
			// moving from LtoR to RtoL
			caretX = renderPositions[visualOffset];
		}
		else {
			// moving from RtoL to LtoR
			caretX = renderPositions[visualOffset] + dx[visualOffset];
		}
	}
	else
	if (isRightToLeft(logicalOffset)) {
		int visualOffset = order[logicalOffset];
		caretX = renderPositions[visualOffset] + dx[visualOffset];
	}
	else {
		caretX = renderPositions[order[logicalOffset]];
	}
	return caretX;
}
/**
 * Returns the direction segments that are in the specified text
 * range. The text range may be visually discontiguous if the 
 * text is bidirectional. Each returned direction run has a single 
 * direction and the runs all go from left to right, regardless of
 * the direction of the text in the segment.  User specified segments 
 * (via BidiSegmentListener) are taken into account and result in 
 * separate direction runs.
 * <p>
 * 
 * @param logicalStart offset of the logcial start of the first 
 * 	direction segment
 * @param length length of the text included in the direction 
 * 	segments
 * @return the direction segments that are in the specified 
 *  text range, each segment has a single direction.
 */
private Vector getDirectionRuns(int logicalStart, int length) {
	Vector directionRuns = new Vector();
	int logicalEnd = logicalStart + length - 1;
	int segmentLogicalStart = logicalStart;
	int segmentLogicalEnd = segmentLogicalStart;
	 
	if (logicalEnd < getTextLength()) {
		int bidiSegmentIndex = 0;
		int bidiSegmentEnd = bidiSegments[bidiSegmentIndex + 1];			

		// Find the bidi segment that the direction runs start in.
		// There will always be at least on bidi segment (for the entire line).
		while (bidiSegmentIndex < bidiSegments.length - 2 && bidiSegmentEnd <= logicalStart) {
			bidiSegmentIndex++;
			bidiSegmentEnd = bidiSegments[bidiSegmentIndex + 1];
		}
		while (segmentLogicalEnd <= logicalEnd) {
			int segType = classBuffer[segmentLogicalStart];
			// Search for the end of the direction segment. Each segment needs to 
			// be rendered separately. 
			// E.g., 11211 (1=R2L, 2=L2R), rendering from logical index 0 to 5 
			// would be visual 1 to 4 and would thus miss visual 0. Rendering the 
			// segments separately would render from visual 1 to 0, then 2, then 
			// 4 to 3.
			while (segmentLogicalEnd < logicalEnd &&
					segType == classBuffer[segmentLogicalEnd + 1] &&
					segmentLogicalEnd + 1 < bidiSegmentEnd) {
				segmentLogicalEnd++;
			}
			directionRuns.addElement(new DirectionRun(segmentLogicalStart, segmentLogicalEnd));
			segmentLogicalStart = ++segmentLogicalEnd;
			// The current direction run ends at a bidi segment end. Get the next bidi segment.
			if (segmentLogicalEnd == bidiSegmentEnd && bidiSegmentIndex < bidiSegments.length - 2) {
				bidiSegmentIndex++;
				bidiSegmentEnd = bidiSegments[bidiSegmentIndex + 1];
			}
		}
	}
	return directionRuns;
}
/**
 * Returns the offset of the last character comprising a ligature.
 * <p>
 * 
 * @return the offset of the last character comprising a ligature.
 */
int getLigatureEndOffset(int offset) {
	// assume only bidi languages support ligatures
	if (!isRightToLeft(offset)) return offset;	
	int newOffset = offset;
	int i = offset + 1;
	// a ligature is a visual character that is comprised of 
	// multiple logical characters, thus each logical part of
	// a ligature will have the same order value
	while (i<order.length && (order[i] == order[offset])) {
		newOffset = i;
		i++;
	}
	return newOffset;
}
/**
 * Returns the offset of the first character comprising a ligature.
 * <p>
 * 
 * @return the offset of the first character comprising a ligature.
 */
int getLigatureStartOffset(int offset) {
	// assume only bidi languages support ligatures
	if (!isRightToLeft(offset)) return offset;	
	int newOffset = offset;
	int i = offset - 1;
	// a ligature is a visual character that is comprised of 
	// multiple logical characters, thus each logical part of
	// a ligature will have the same order value
	while (i>=0 && (order[i] == order[offset])) {
		newOffset = i;
		i--;
	}
	return newOffset;
}
/**
 * Returns the logical offset of the character at the specified 
 * visual offset.
 * <p>
 * 
 * @param visualOffset the visual offset
 * @return the logical offset of the character at <code>visualOffset</code>.
 */
private int getLogicalOffset(int visualOffset) {
	int logicalOffset = 0;
	
	while (logicalOffset < order.length && order[logicalOffset] != visualOffset) {
		logicalOffset++;
	}
	return logicalOffset;
}
/**
 * Returns the offset of the character at the specified x location.
 * <p>
 * 
 * @param x the location of the character
 * @return the logical offset of the character at the specified x 
 * 	location.
 */
int getOffsetAtX(int x) {
	int visualOffset;

	if (getTextLength() == 0) {
		return 0;
	}
	if (x >= renderPositions[renderPositions.length - 1] + dx[dx.length - 1]) {
		// Return when x is past the end of the line. Fixes 1GLADBK.
		return -1;
	}
	visualOffset = getVisualOffsetAtX(x);
	return getLogicalOffset(visualOffset);
}
/**
 * Returns the visual offset of the character at the specified x 
 * location.
 * <p>
 * 
 * @param x the location of the character
 * @return the visual offset of the character at the specified x 
 * 	location.
 */
private int getVisualOffsetAtX(int x) {
	int lineLength = getTextLength();
	int low = -1;
	int high = lineLength;

	while (high - low > 1) {
		int offset = (high + low) / 2;
		int visualX = renderPositions[offset];

		// visualX + dx is the start of the next character. Restrict right/high
		// search boundary only if x is before next character. Fixes 1GL4ZVE.
		if (x < visualX + dx[offset]) {
			high = offset;			
		}
		else 
		if (high == lineLength && high - offset == 1) {
			// requested x location is past end of line
			high = -1;
		}
		else {
			low = offset;
		}
	}
	return high;
}
/**
 * Returns the reordering indices that map between logical and 
 * visual index of characters in the specified range.
 * <p>
 * 
 * @param start start offset of the reordering indices
 * @param length number of reordering indices to return
 * @return the reordering indices that map between logical and 
 * 	visual index of characters in the specified range. Relative 
 * 	to the start of the range.
 */
private int[] getRenderIndexesFor(int start, int length) {
	int[] positions = new int[length];
	int end = start + length;
	
	for (int i = start; i < end; i++) {
		positions[i-start] = order[i];
	}		
	return positions;
}
/**
 * Break up the given ranges such that each range is fully contained within a bidi
 * segment.
 */
private int[] segmentedRangesFor(int[] ranges) {
	if ((bidiSegments == null) || (bidiSegments.length == 0)) return ranges;
	Vector newRanges = new Vector();
	int j=0;
	int startSegment;
	int endSegment;
	for (int i=0; i<ranges.length; i+=2) {
		int start = ranges[i];
		int end = start+ranges[i+1];
		startSegment=-1;
		endSegment=-1;
		boolean done = false;
		while (j<bidiSegments.length && !done) {
			if (bidiSegments[j]<=start) {
				startSegment=j;
			}
			if (bidiSegments[j]>=end) {
				endSegment=j-1;
				j--;
			}
			done = (startSegment != -1) && (endSegment != -1);
			if (!done) j++;
		}
		if (startSegment == endSegment) {
			// range is within one segment
			newRanges.addElement(new Integer(start));
			newRanges.addElement(new Integer(end-start));
		} else if (startSegment > endSegment) {
			// range is within no segment (i.e., it's empty)
		} else {
			// range spans multiple segments
			newRanges.addElement(new Integer(start));
			newRanges.addElement(new Integer(bidiSegments[startSegment+1]-start));
			startSegment++;
			for (int k=startSegment; k<endSegment; k++) {
				newRanges.addElement(new Integer(bidiSegments[k]));
				newRanges.addElement(new Integer(bidiSegments[k+1]-bidiSegments[k]));
			}
			newRanges.addElement(new Integer(bidiSegments[endSegment]));
			newRanges.addElement(new Integer(end-bidiSegments[endSegment]));
		}	
	}
	int[] intArray = new int[newRanges.size()];
	for (int i=0; i<newRanges.size(); i++) {
		intArray[i]=((Integer)newRanges.elementAt(i)).intValue();
	}
	return intArray;
}
/**
 * Returns the number of characters in the line.
 * <p>
 * 
 * @return the number of characters in the line.
 */
private int getTextLength() {
	return dx.length;
}
/**
 * Returns the width in pixels of the line.
 * <p>
 * 
 * @return the width in pixels of the line.
 */
int getTextWidth() {
	int width = 0;
	
	if (getTextLength() > 0) {
		width = renderPositions[renderPositions.length - 1] + dx[dx.length - 1];
	}
	return width;
}
/**
 * Returns whether the current platform supports a bidi language.
 * <p>
 * 
 * @return true=bidi is supported, false otherwise. 
 */
static boolean isBidiPlatform() {
	return BidiUtil.isBidiPlatform();
}
/**
 * Returns whether the font set in the specified gc supports 
 * character shaping.
 * <p>
 * 
 * @param gc the GC that should be tested for character shaping.
 * @return 
 * 	true=the font set in the specified gc supports character shaped glyphs
 * 	false=the font set in the specified gc doesn't support character shaped glyphs 
 */
static boolean isCharacterShaped(GC gc) {
	return (BidiUtil.getFontBidiAttributes(gc) & BidiUtil.GLYPHSHAPE) != 0;
}
/**
 * Returns whether the font set in the specified gc contains 
 * ligatured glyphs.
 * <p>
 * 
 * @param gc the GC that should be tested for ligatures.
 * @return 
 * 	true=the font set in the specified gc contains ligatured glyphs. 
 * 	false=the font set in the specified gc doesn't contain ligatured 
 * 	glyphs. 
 */
static boolean isLigated(GC gc) {
	return (BidiUtil.getFontBidiAttributes(gc) & BidiUtil.LIGATE) != 0;
}
/**
 * Returns the direction of the character at the specified index.
 * Used for rendering and caret positioning where local numbers (e.g., 
 * national Arabic, or Hindi, numbers) are considered left-to-right.
 * <p>
 * 
 * @param logicalIndex the index of the character
 * @return 
 * 	true=the character at the specified index is in a right-to-left
 * 	codepage (e.g., Hebrew, Arabic).
 * 	false=the character at the specified index is in a left-to-right/latin
 * 	codepage.
 */
boolean isRightToLeft(int logicalIndex) {
	boolean isRightToLeft = false;
	
	if (logicalIndex < classBuffer.length) {
		isRightToLeft = (classBuffer[logicalIndex] == BidiUtil.CLASS_ARABIC) || 
					    (classBuffer[logicalIndex] == BidiUtil.CLASS_HEBREW);
	}
	return isRightToLeft;
}
/**
 * Returns the direction of the character at the specified index.
 * Used for setting the keyboard language where local numbers (e.g., 
 * national Arabic, or Hindi, numbers) are considered right-to-left.
 * <p>
 * 
 * @param logicalIndex the index of the character
 * @return 
 * 	true=the character at the specified index is in a right-to-left
 * 	codepage (e.g., Hebrew, Arabic).
 * 	false=the character at the specified index is in a left-to-right/latin
 * 	codepage.
 */
boolean isRightToLeftInput(int logicalIndex) {
	boolean isRightToLeft = false;
	
	if (logicalIndex < classBuffer.length) {
		isRightToLeft = (classBuffer[logicalIndex] == BidiUtil.CLASS_ARABIC) || 
					    (classBuffer[logicalIndex] == BidiUtil.CLASS_HEBREW) ||
					    (classBuffer[logicalIndex] == BidiUtil.CLASS_LOCALNUMBER);
	}
	return isRightToLeft;
}
/**
 * Returns whether the specified index is the start of a user 
 * specified direction segment.
 * <p>
 * 
 * @param logicalIndex the index to test
 * @return true=the specified index is the start of a user specified 
 * 	direction segment, false otherwise
 */
private boolean isStartOfBidiSegment(int logicalIndex) {
	for (int i = 0; i < bidiSegments.length; i++) {
		if (bidiSegments[i] == logicalIndex) return true;
	}
	return false;	
}
/**
 * Reorders and calculates render positions for the specified sub-line 
 * of text. The results will be merged with the data for the rest of 
 * the line .
 * <p>
 * 
 * @param textline the entire line of text that this object represents.
 * @param logicalStart the start offset of the first character to 
 * 	reorder.
 * @param length the number of characters to reorder
 */
private void prepareBoldText(String textline, int logicalStart, int length) {
	int byteCount = length;
	int flags = 0;
	String text = textline.substring(logicalStart, logicalStart + length);

	// Figure out what is before and after the substring so that the proper character 
	// shaping will occur.  Character shaping will not occur across bidi segments, so
	// if the bold text starts or ends on a bidi segment, do not process the text
	// for character shaping.
	if (logicalStart != 0  
		&& isCharacterShaped(gc) 
		&& !isStartOfBidiSegment(logicalStart)
		&& !Compatibility.isWhitespace(textline.charAt(logicalStart - 1)) 
		&& isRightToLeft(logicalStart - 1)) {
		// if the start of the substring is not the beginning of the 
		// text line, check to see what is before the string
		flags |= BidiUtil.LINKBEFORE;
	}
	if ((logicalStart + byteCount) != dx.length 
		&& isCharacterShaped(gc) 
		&& !isStartOfBidiSegment(logicalStart + length)
		&& !Compatibility.isWhitespace(textline.charAt(logicalStart + byteCount)) 
		&& isRightToLeft(logicalStart + byteCount)) {
		// if the end of the substring is not the end of the text line,
		// check to see what is after the substring
		flags |= BidiUtil.LINKAFTER;
	}		
	// set classification values for the substring
	flags |= BidiUtil.CLASSIN;
	byte[] classArray = new byte[byteCount];
	int[] renderIndexes = getRenderIndexesFor(logicalStart, byteCount);
	for (int i = 0; i < byteCount; i++) {
		classArray[i] = classBuffer[renderIndexes[i]];
	}
	int[] dxArray = new int[byteCount];
	int[] orderArray = new int[byteCount];	
	BidiUtil.getRenderInfo(gc, text, orderArray, classArray, dxArray, flags, new int[] {0, text.length()});
	// update the existing dx array with the new dx values based on the bold font
	for (int i = 0; i < dxArray.length; i++) {
		int dxValue = dxArray[orderArray[i]];
		int visualIndex = renderIndexes[i];
		dx[visualIndex] = dxValue;
	}
}
/** 
 * Redraws a rectangle spanning the given logical range.
 * The rectangle may be visually discontiguous if the text segment 
 * is bidirectional.
 * <p>
 * 
 * @param parent window that should be invalidated
 * @param logicalStart logcial start offset of the rectangle
 * @param length number of logical characters the rectangle should span
 * @param xOffset x location of the line start
 * @param yOffset y location of the line start
 * @param height height of the invalidated rectangle
 */
void redrawRange(Control parent, int logicalStart, int length, int xOffset, int yOffset, int height) {
	Enumeration directionRuns = getDirectionRuns(logicalStart, length).elements();

	if (logicalStart + length > getTextLength()) {
		return;
	}
	while (directionRuns.hasMoreElements()) {
		DirectionRun run = (DirectionRun) directionRuns.nextElement();
		int startX = run.getRenderStartX();

		parent.redraw(xOffset + startX, yOffset, run.getRenderStopX() - startX, height, true);
	}				
}
/**
 * Sets the keyboard language to match the codepage of the character 
 * at the specified offset.
 * Only distinguishes between left-to-right and right-to-left
 * characters and sets the keyboard language to one of Latin, Hebrew 
 * and Arabic.
 * <p>
 * 
 * @param logicalIndex logical offset of the character to use for 
 * 	determining the new keyboard language.
 */
void setKeyboardLanguage(int logicalIndex) {
	int language = BidiUtil.KEYBOARD_LATIN;
	
	if (logicalIndex >= classBuffer.length) {
		return;
	}
	if (isRightToLeftInput(logicalIndex)) {
		String codePage = System.getProperty("file.encoding").toUpperCase();
		if ("CP1255".equals(codePage)) {
			language = BidiUtil.KEYBOARD_HEBREW;
		}
		else
		if ("CP1256".equals(codePage)) {		
			language = BidiUtil.KEYBOARD_ARABIC;
		}
	}
	BidiUtil.setKeyboardLanguage(language);
}
/**
 * Returns a string representation of the receiver.
 * <p>
 * 
 * @return a string representation of the receiver for 
 *	debugging purposes. The output order of the StyledTextbidi values
 *	is as follows: order, render position, dx, character class, glyphs.
 */
public String toString() {
	StringBuffer buf = new StringBuffer();
	
	buf.append("StyledTextBidi {{");
	// order
	for (int i = 0; i < order.length; i++) {
		if (i != 0) {
			buf.append(",");
		}
		buf.append(order[i]);
	}
	buf.append("}, {");
	// render positions
	for (int i = 0; i < renderPositions.length; i++) {
		if (i != 0) {
			buf.append(",");
		}
		buf.append(renderPositions[i]);
	}
	buf.append("}, {");
	// dx
	for (int i = 0; i < dx.length; i++) {
		if (i != 0) {
			buf.append(",");
		}
		buf.append(dx[i]);
	}
	buf.append("}, {");
	// character class
	for (int i = 0; i < classBuffer.length; i++) {
		if (i != 0) {
			buf.append(",");
		}
		buf.append(classBuffer[i]);
	}
	buf.append("}, {");
	// glyphs
	buf.append(glyphBuffer);
	buf.append("}}");
	return buf.toString();
}
}