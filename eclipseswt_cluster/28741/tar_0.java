package org.eclipse.swt.dnd;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.internal.*;

/**
 *
 * Class <code>DropTarget</code> defines the target object for a drag and drop transfer.
 *
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 *
 * <p>This class identifies the <code>Control</code> over which the user must position the cursor
 * in order to drop the data being transferred.  It also specifies what data types can be dropped on 
 * this control and what operations can be performed.  You may have several DropTragets in an 
 * application but there can only be a one to one mapping between a <code>Control</code> and a <code>DropTarget</code>.
 * The DropTarget can receive data from within the same application or from other applications 
 * (such as text dragged from a text editor like Word).</p>
 *
 * <code><pre>
 *	int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
 *	Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 *	DropTarget target = new DropTarget(label, operations);
 *	target.setTransfer(types);
 * </code></pre>
 *
 * <p>The application is notified of data being dragged over this control and of when a drop occurs by 
 * implementing the interface <code>DropTargetListener</code> which uses the class 
 * <code>DropTargetEvent</code>.  The application can modify the type of drag being performed 
 * on this Control at any stage of the drag by modifying the <code>event.detail</code> field or the 
 * <code>event.currentDataType</code> field.  When the data is dropped, it is the responsibility of 
 * the application to copy this data for its own purposes.
 *
 * <code><pre>
 *	target.addDropListener (new DropTargetListener() {
 *		public void dragEnter(DropTargetEvent event) {};
 *		public void dragOver(DropTargetEvent event) {};
 *		public void dragLeave(DropTargetEvent event) {};
 *		public void dragOperationChanged(DropTargetEvent event) {};
 *		public void dropAccept(DropTargetEvent event) {}
 *		public void drop(DropTargetEvent event) {
 *			// A drop has occurred, copy over the data
 *			if (event.data == null) { // no data to copy, indicate failure in event.detail
 *				event.detail = DND.DROP_NONE;
 *				return;
 *			}
 *			label.setText ((String) event.data); // data copied to label text
 *		}
 * 	});
 * </pre></code>
 *
 * <dl>
 *	<dt><b>Styles</b> <dd>DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK 
 *	<dt><b>Events</b> <dd>DND.DragEnter, DND.DragLeave, DND.DragOver, DND.DragOperationChanged, 
 *                        DND.Drop, DND.DropAccept
 * </dl>
 */
public final class DropTarget extends Widget {
	
/**
 * Creates a new <code>DropTarget</code> to handle dropping on the specified <code>Control</code>.
 * 
 * @param control the <code>Control</code> over which the user positions the cursor to drop data
 *
 * @param style the bitwise OR'ing of allowed operations; this may be a combination of any of 
 *					DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK
 *
 */
public DropTarget(Control control, int style) {
	super(control, style);
}

/**	 
 * Adds the listener to receive events.
 *
 * @param listener the listener
 *
 * @exception SWTError 
 *	<ul><li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
 * 		<li>ERROR_WIDGET_DISPOSED  when the widget has been disposed</li>
 * 		<li>ERROR_NULL_ARGUMENT when listener is null</li></ul>
 */
public void addDropListener(DropTargetListener listener) {	
}

/**
 * Returns the Control which is registered for this DropTarget.  This is the control over which the 
 * user positions the cursor to drop the data.
 *
 * @return the Control which is registered for this DropTarget
 *
 */
public Control getControl () {
	return null;
}
public Display getDisplay () {
	return null;
}
/**
 * Returns the list of data types that can be transferred to this DropTarget.
 *
 * @return the list of data types that can be transferred to this DropTarget
 *
 */ 
public Transfer[] getTransfer() { return null; }
public void notifyListener (int eventType, Event event) {}
/**	 
 * Removes the listener.
 *
 * @param listener the listener
 *
 * @exception SWTError
 *	<ul><li>ERROR_THREAD_INVALID_ACCESS	when called from the wrong thread</li>
 * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
 * 		<li>ERROR_NULL_ARGUMENT when listener is null</li></ul>
 */
public void removeDropListener(DropTargetListener listener) {}
/**
 * Specifies the list of data types that can be transferred to this DropTarget.
 *
 * @param transferAgents a list of Transfer objects which define the types of data that can be
 *						 dropped on this target
 */
public void setTransfer(Transfer[] transferAgents){}

}
