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
package org.eclipse.swt.accessibility;


import java.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.widgets.*;

public class AccessibleObject {
	int handle, parentType, index = -1, id = ACC.CHILDID_SELF;
	Accessible accessible;
	AccessibleObject parent;
	Hashtable children = new Hashtable (9);
	boolean isLightweight = false;
	int actionNamePtr = -1;
	int descriptionPtr = -1;
	int keybindingPtr = -1;
	int namePtr = -1;
	int textPtr = -1;
	int valuePtr = -1;
	static boolean DEBUG = Display.DEBUG;

	AccessibleObject (int type, int widget, Accessible accessible, int parentType) {
		this (type, widget, accessible, parentType, false);
	}
	
	AccessibleObject (int type, int widget, Accessible accessible, int parentType, boolean isLightweight) {
		super ();
		handle = OS.g_object_new (type, 0);
		ATK.atk_object_initialize (handle, widget);
		this.accessible = accessible;
		this.isLightweight = isLightweight;
		this.parentType = parentType;
		if (DEBUG) System.out.println("new AccessibleObject: " + handle);
	}

	void addChild (AccessibleObject child) {
		children.put (new Integer (child.handle), child);
		child.setParent (this);
	}
	
	int atkAction_get_keybinding (int index) {
		if (accessible.getAccessibleListeners ().length != 0) {
			AccessibleListener[] listeners = accessible.getAccessibleListeners ();
			AccessibleEvent event = new AccessibleEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getKeyboardShortcut (event);	
			} 
			if (event.result != null) {
				if (keybindingPtr != -1) OS.g_free (keybindingPtr);
				byte[] name = Converter.wcsToMbcs (null, event.result, true);
				keybindingPtr = OS.g_malloc (name.length);
				OS.memmove (keybindingPtr, name, name.length);
				return keybindingPtr; 	
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_ACTION_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkActionIface actionIface = new AtkActionIface ();
			ATK.memmove (actionIface, superType);
			if (actionIface.get_keybinding != 0) {
				return OS.call (actionIface.get_keybinding, handle, index);
			}
		}
		return 0;
	}

	int atkAction_get_name (int index) {
		if (accessible.getAccessibleListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getDefaultAction (event);				
			} 
			if (event.result != null) {
				if (actionNamePtr != -1) OS.g_free (actionNamePtr);
				byte[] name = Converter.wcsToMbcs (null, event.result, true);
				actionNamePtr = OS.g_malloc (name.length);
				OS.memmove (actionNamePtr, name, name.length);
				return actionNamePtr;
			}
		}	
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_ACTION_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkActionIface actionIface = new AtkActionIface ();
			ATK.memmove (actionIface, superType);
			if (actionIface.get_name != 0) {
				return OS.call (actionIface.get_name, handle, index);
			}
		}
		return 0;
	}	

	int atkComponent_get_extents (int x, int y, int width, int height, int coord_type) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.x = event.y = event.width = event.height = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getLocation (event);
			}
			if (event.x != -1 && event.y != -1 && event.width != -1 && event.height != -1) { 
				if (coord_type == ATK.ATK_XY_WINDOW) {
					// translate display -> control 
					int gtkAccessibleHandle = OS.GTK_ACCESSIBLE (handle);
					GtkAccessible gtkAccessible = new GtkAccessible ();
					OS.memmove (gtkAccessible, gtkAccessibleHandle);
					int topLevel = OS.gtk_widget_get_toplevel (gtkAccessible.widget);
					int window = OS.GTK_WIDGET_WINDOW (topLevel);
					int[] topWindowX = new int [1], topWindowY = new int [1];
					OS.gdk_window_get_origin (window, topWindowX, topWindowY);
					event.x -= topWindowX [0];
					event.y -= topWindowY [0];
				}
				OS.memmove (x, new int[] {event.x}, 4);
				OS.memmove (y, new int[] {event.y}, 4);
				OS.memmove (width, new int[] {event.width}, 4);
				OS.memmove (height, new int[] {event.height}, 4);
				return 0;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_COMPONENT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkComponentIface componentIface = new AtkComponentIface ();
			ATK.memmove (componentIface, superType);
			if (componentIface.get_extents != 0) {
				return OS.call (componentIface.get_extents, x, y, width, height, coord_type);
			}
		}
		return 0;
	}

	int atkComponent_get_position (int x, int y, int coord_type) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.x = event.y = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getLocation (event);
			}
			if (event.x != -1 && event.y != -1) { 
				if (coord_type == ATK.ATK_XY_WINDOW) {
					// translate display -> control 
					int gtkAccessibleHandle = OS.GTK_ACCESSIBLE (handle);
					GtkAccessible gtkAccessible = new GtkAccessible ();
					OS.memmove (gtkAccessible, gtkAccessibleHandle);
					int topLevel = OS.gtk_widget_get_toplevel (gtkAccessible.widget);
					int window = OS.GTK_WIDGET_WINDOW (topLevel);
					int[] topWindowX = new int [1], topWindowY = new int [1];
					OS.gdk_window_get_origin (window, topWindowX, topWindowY);
					event.x -= topWindowX [0];
					event.y -= topWindowY [0];
				}
				OS.memmove (x, new int[] {event.x}, 4);
				OS.memmove (y, new int[] {event.y}, 4);
				return 0;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_COMPONENT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkComponentIface componentIface = new AtkComponentIface ();
			ATK.memmove (componentIface, superType);
			if (componentIface.get_position != 0) {
				return OS.call (componentIface.get_position, handle, x, y, coord_type);
			}
		}
		return 0;
	}

	int atkComponent_get_size (int width, int height, int coord_type) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.width = event.height = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getLocation (event);
			}
			if (event.width != -1 && event.height != -1) { 
				OS.memmove (width, new int[] {event.width}, 4);
				OS.memmove (height, new int[] {event.height}, 4);
				return 0;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_COMPONENT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkComponentIface componentIface = new AtkComponentIface ();
			ATK.memmove (componentIface, superType);
			if (componentIface.get_size != 0) {
				return OS.call (componentIface.get_size, handle, width, height, coord_type);
			}
		}
		return 0;
	}

	int atkComponent_ref_accessible_at_point (int x, int y, int coord_type) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();				
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.x = x; event.y = y;
			if (coord_type == ATK.ATK_XY_WINDOW) {
				// translate control -> display
				int gtkAccessibleHandle = OS.GTK_ACCESSIBLE (handle);
				GtkAccessible gtkAccessible = new GtkAccessible ();
				OS.memmove (gtkAccessible, gtkAccessibleHandle);
				int topLevel = OS.gtk_widget_get_toplevel (gtkAccessible.widget);
				int window = OS.GTK_WIDGET_WINDOW (topLevel);				
				int[] topWindowX = new int [1], topWindowY = new int [1];
				OS.gdk_window_get_origin (window, topWindowX, topWindowY);
				event.x += topWindowX [0];
				event.y += topWindowY [0]; 
				Rectangle rect = accessible.control.getBounds ();				
				event.x -= rect.x;
				event.y -= rect.y;
			}
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getChildAtPoint (event);				
			}
			if (event.childID == id) event.childID = ACC.CHILDID_SELF;
			AccessibleObject accObj = getChildByID (event.childID);
			if (accObj != null) {
				OS.g_object_ref (accObj.handle);	
				return accObj.handle;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_COMPONENT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkComponentIface componentIface = new AtkComponentIface ();
			ATK.memmove (componentIface, superType);
			if (componentIface.ref_accessible_at_point != 0) {
				return OS.call (componentIface.ref_accessible_at_point, handle, x, y, coord_type);
			}
		}
		return 0;
	}	

	int atkObject_get_description () {
		if (accessible.getAccessibleListeners ().length != 0) {
			AccessibleListener[] listeners = accessible.getAccessibleListeners ();
			AccessibleEvent event = new AccessibleEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getDescription (event);				
			} 
			if (event.result != null) {
				if (descriptionPtr != -1) OS.g_free (descriptionPtr);
				byte[] name = Converter.wcsToMbcs (null, event.result, true);
				descriptionPtr = OS.g_malloc (name.length);
				OS.memmove (descriptionPtr, name, name.length);
				return descriptionPtr; 
			}
		} 
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_description == 0) return 0;
		return OS.call (objectClass.get_description, handle);
	}

	int atkObject_get_name () {
		if (accessible.getAccessibleListeners ().length != 0) {
			AccessibleListener[] listeners = accessible.getAccessibleListeners ();
			AccessibleEvent event = new AccessibleEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getName (event);				
			} 
			if (event.result != null) {
				if (namePtr != -1) OS.g_free (namePtr);
				byte[] name = Converter.wcsToMbcs (null, event.result, true);
				namePtr = OS.g_malloc (name.length);
				OS.memmove (namePtr, name, name.length);
				return namePtr; 
			}
		} 
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_name == 0) return 0;
		return OS.call (objectClass.get_name, handle);
	}	

	int atkObject_get_n_children () {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.detail = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getChildCount (event);
			} 
			if (event.detail != -1) return event.detail;
		}
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_n_children == 0) return 0;
		return OS.call (objectClass.get_n_children, handle);
	}

	int atkObject_get_index_in_parent () {
		if (index != -1) return index;
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_index_in_parent == 0) return 0;
		return OS.call (objectClass.get_index_in_parent, handle);
	}

	int atkObject_get_parent () {
		if (parent != null) return parent.handle;
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_parent == 0) return 0;
		return OS.call (objectClass.get_parent, handle);
	}

	int atkObject_get_role () {
		if (accessible.getAccessibleListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.detail = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getRole (event);				
			} 
			if (event.detail != -1) {
				switch (event.detail) {
					// Convert from win32 role values to atk role values
					case ACC.ROLE_CHECKBUTTON: return ATK.ATK_ROLE_CHECK_BOX;
					case ACC.ROLE_CLIENT_AREA: return ATK.ATK_ROLE_DRAWING_AREA;
					case ACC.ROLE_COMBOBOX: return ATK.ATK_ROLE_COMBO_BOX;
					case ACC.ROLE_DIALOG: return ATK.ATK_ROLE_DIALOG;
					case ACC.ROLE_LABEL: return ATK.ATK_ROLE_LABEL;
					case ACC.ROLE_LIST: return ATK.ATK_ROLE_LIST;
					case ACC.ROLE_LISTITEM: return ATK.ATK_ROLE_LIST_ITEM;
					case ACC.ROLE_MENU: return ATK.ATK_ROLE_MENU;
					case ACC.ROLE_MENUBAR: return ATK.ATK_ROLE_MENU_BAR;
					case ACC.ROLE_MENUITEM: return ATK.ATK_ROLE_MENU_ITEM;
					case ACC.ROLE_PROGRESSBAR: return ATK.ATK_ROLE_PROGRESS_BAR;
					case ACC.ROLE_PUSHBUTTON: return ATK.ATK_ROLE_PUSH_BUTTON;
					case ACC.ROLE_SCROLLBAR: return ATK.ATK_ROLE_SCROLL_BAR;
					case ACC.ROLE_SEPARATOR: return ATK.ATK_ROLE_SEPARATOR;
					case ACC.ROLE_SLIDER: return ATK.ATK_ROLE_SLIDER;
					case ACC.ROLE_TABLE: return ATK.ATK_ROLE_TABLE;
					case ACC.ROLE_TABFOLDER: return ATK.ATK_ROLE_PAGE_TAB_LIST;
					case ACC.ROLE_TABLECOLUMN: return ATK.ATK_ROLE_TABLE_COLUMN_HEADER; // closest match
					case ACC.ROLE_TABITEM: return ATK.ATK_ROLE_PAGE_TAB;
					case ACC.ROLE_TEXT: return ATK.ATK_ROLE_TEXT;
					case ACC.ROLE_TOOLBAR: return ATK.ATK_ROLE_TOOL_BAR;
					case ACC.ROLE_TOOLTIP: return ATK.ATK_ROLE_TOOL_TIP;
					case ACC.ROLE_TREE: return ATK.ATK_ROLE_TREE;
					case ACC.ROLE_RADIOBUTTON: return ATK.ATK_ROLE_RADIO_BUTTON;
					case ACC.ROLE_WINDOW: return ATK.ATK_ROLE_WINDOW;
				}
			}
		} 
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.get_role == 0) return 0;
		return OS.call (objectClass.get_role, handle);
	}

	int atkObject_ref_child (int index) {
		updateChildren ();
		AccessibleObject accObject = getChildByIndex (index);	
		if (accObject != null) {
			OS.g_object_ref (accObject.handle);	
			return accObject.handle;
		}
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.ref_child == 0) return 0;
		return OS.call (objectClass.ref_child, handle, index);
	}

	int atkObject_ref_state_set  () {
		if (accessible.getControlListeners ().length != 0) {
			int set = ATK.atk_state_set_new ();
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			event.detail = -1;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getState (event);
			} 
			if (event.detail != -1) {
				//	Convert from win32 state values to atk state values
				int state = event.detail;
				if ((state & ACC.STATE_BUSY) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_BUSY);
				if ((state & ACC.STATE_CHECKED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_CHECKED);
				if ((state & ACC.STATE_EXPANDED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_EXPANDED);
				if ((state & ACC.STATE_FOCUSABLE) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_FOCUSABLE);
				if ((state & ACC.STATE_FOCUSED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_FOCUSED);
				if ((state & ACC.STATE_HOTTRACKED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_ARMED); ;
				if ((state & ACC.STATE_INVISIBLE) == 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_VISIBLE);
				if ((state & ACC.STATE_MULTISELECTABLE) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_MULTISELECTABLE);
				if ((state & ACC.STATE_OFFSCREEN) == 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_SHOWING);												
				if ((state & ACC.STATE_PRESSED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_PRESSED);
				if ((state & ACC.STATE_READONLY) == 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_EDITABLE);
				if ((state & ACC.STATE_SELECTABLE) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_SELECTABLE);
				if ((state & ACC.STATE_SELECTED) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_SELECTED);
				if ((state & ACC.STATE_SIZEABLE) != 0) ATK.atk_state_set_add_state (set, ATK.ATK_STATE_RESIZABLE);
				// Note: STATE_COLLAPSED and STATE_NORMAL have no ATK equivalents
				return set;
			}	
		}
		int superType = OS.g_type_class_peek (parentType);
		AtkObjectClass objectClass = new AtkObjectClass ();
		ATK.memmove (objectClass, superType);
		if (objectClass.ref_state_set == 0) return 0;
		return OS.call (objectClass.ref_state_set, handle);
	}

	int atkSelection_is_child_selected (int index) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getSelection (event);
			}
			AccessibleObject accessibleObject = getChildByID (event.childID);
			if (accessibleObject != null) { 
				return accessibleObject.index == index ? 1 : 0;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_SELECTION_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkSelectionIface selectionIface = new AtkSelectionIface ();
			ATK.memmove (selectionIface, superType);
			if (selectionIface.is_child_selected != 0) {
				return OS.call (selectionIface.is_child_selected, handle, index);
			}
		}
		return 0;
	}

	int atkSelection_ref_selection (int index) {
		if (accessible.getControlListeners ().length != 0) {
			AccessibleControlListener[] listeners = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			event.childID = id;
			for (int i = 0; i < listeners.length; i++) {
				listeners [i].getSelection (event);
			} 
			AccessibleObject accObj = getChildByID (event.childID);
			if (accObj != null) {
				OS.g_object_ref (accObj.handle);	
				return accObj.handle;
			}
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_SELECTION_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkSelectionIface selectionIface = new AtkSelectionIface ();
			ATK.memmove (selectionIface, superType);
			if (selectionIface.ref_selection != 0) {
				return OS.call (selectionIface.ref_selection, handle, index);
			}
		}
		return 0;
	}

	int atkText_get_character_at_offset (int offset) {
		String text = getText ();
		if (text != null) return (int)text.charAt (offset); // TODO bogus!
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_character_at_offset != 0) {
				return OS.call (textIface.get_character_at_offset, handle, offset);
			}
		}
		return 0;
	}

	int atkText_get_character_count () {
		String text = getText ();
		if (text != null) return text.length ();
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_character_count != 0) {
				return OS.call (textIface.get_character_count, handle);
			}
		}
		return 0;
	}

	int atkText_get_text (int start_offset, int end_offset) {
		String text = getText ();
		if (text != null) {
			if (end_offset == -1) {
				end_offset = text.length ();
			} else {
				end_offset = Math.min (end_offset + 1, text.length ());	
			}
			text = text.substring (start_offset, end_offset);
			byte[] bytes = Converter.wcsToMbcs (null, text, true);
//			TODO gnopernicus bug? freeing previous string can cause gp
//			if (textPtr != -1) OS.g_free (textPtr);
			textPtr = OS.g_malloc (bytes.length);
			OS.memmove (textPtr, bytes, bytes.length);
			return textPtr;
		}
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_text != 0) {
				return OS.call (textIface.get_text, handle, start_offset, end_offset);
			}
		}
		return 0;
	}

	int atkText_get_text_after_offset (int offset, int boundary_type, int start_offset, int end_offset) {
		String text = getText ();
		if (text != null) {
			int length = text.length ();
			int startBounds = offset;
			int endBounds = offset;
			switch (boundary_type) {
				case ATK.ATK_TEXT_BOUNDARY_CHAR: {
					if (text.length () > offset) endBounds++;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_WORD_START: {
					int wordStart1 = getIndexOfChar (text, " !?.\n", offset);
					if (wordStart1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					while (wordStart1 < length) {
						char current = text.charAt (wordStart1); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						wordStart1++;
					}
					if (wordStart1 == length) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = wordStart1;
					int wordStart2 = getIndexOfChar (text, " !?.\n", wordStart1);
					if (wordStart2 == -1) {
						endBounds = length;
						break;
					}
					while (wordStart2 < length) {
						char current = text.charAt (wordStart2); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						wordStart2++;
					}
					endBounds = wordStart2;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_WORD_END: {
					int wordEnd1 = getIndexOfChar (text, " !?.\n", offset);
					if (wordEnd1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = wordEnd1;
					int wordEnd2 = wordEnd1;
					while (wordEnd2 < length) {
						char current = text.charAt (wordEnd2); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						wordEnd2++;
					}
					if (wordEnd2 == length) {
						endBounds = length;
						break;
					}
					wordEnd2 = getIndexOfChar (text, " !?.\n", wordEnd2);
					if (wordEnd2 == -1) {
						endBounds = length;
					} else {
						endBounds = wordEnd2;
					}
					break;
				}

				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_START: {
					int sentenceStart1 = getIndexOfChar (text, "!?.", offset);
					if (sentenceStart1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					while (sentenceStart1 < length) {
						char current = text.charAt (sentenceStart1); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						sentenceStart1++;
					}
					if (sentenceStart1 == length) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = sentenceStart1;
					int sentenceStart2 = getIndexOfChar (text, "!?.", sentenceStart1);
					if (sentenceStart2 == -1) {
						endBounds = length;
						break;
					}
					while (sentenceStart2 < length) {
						char current = text.charAt (sentenceStart2); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						sentenceStart2++;
					}
					endBounds = sentenceStart2;
					break;
				}

				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_END: {
					int sentenceEnd1 = getIndexOfChar (text, "!?.", offset);
					if (sentenceEnd1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = sentenceEnd1;
					int sentenceEnd2 = sentenceEnd1;
					while (sentenceEnd2 < length) {
						char current = text.charAt (sentenceEnd2); 
						if (current != ' ' && current != '!' && current != '?' && current != '.' && current != '\n') break;
						sentenceEnd2++;
					}
					if (sentenceEnd2 == length) {
						endBounds = length;
						break;
					}
					sentenceEnd2 = getIndexOfChar (text, "!?.", sentenceEnd2);
					if (sentenceEnd2 == -1) {
						endBounds = length;
					} else {
						endBounds = sentenceEnd2;
					}
					break;
				}

				case ATK.ATK_TEXT_BOUNDARY_LINE_START: {
					int lineStart1 = text.indexOf ('\n', offset);
					if (lineStart1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					while (lineStart1 < length) {
						if (text.charAt (lineStart1) != '\n') break;
						lineStart1++;
					}
					if (lineStart1 == length) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = lineStart1;
					int lineStart2 = text.indexOf ('\n', lineStart1);
					if (lineStart2 == -1) {
						endBounds = length;
						break;
					}
					while (lineStart2 < length) {
						if (text.charAt (lineStart2) != '\n') break;
						lineStart2++;
					}
					endBounds = lineStart2;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_LINE_END: {
					int lineEnd1 = text.indexOf ('\n', offset);
					if (lineEnd1 == -1) {
						startBounds = endBounds = length;
						break;
					}
					startBounds = lineEnd1;
					int lineEnd2 = lineEnd1;
					while (lineEnd2 < length) {
						if (text.charAt (lineEnd2) != '\n') break;
						lineEnd2++;
					}
					if (lineEnd2 == length) {
						endBounds = length;
						break;
					}
					lineEnd2 = text.indexOf ('\n', lineEnd2);
					if (lineEnd2 == -1) {
						endBounds = length;
					} else {
						endBounds = lineEnd2;
					}
					break;
				}
			}
//			OS.memmove (start_offset, new int[] {startBounds}, 4);
//			OS.memmove (end_offset, new int[] {endBounds}, 4);
			text = text.substring (startBounds, endBounds);
System.out.println("result: \"" + text + "\"");
			byte[] bytes = Converter.wcsToMbcs (null, text, true);
			// TODO gnopernicus bug? freeing previous string can cause gp
//			if (textPtr != -1) OS.g_free (textPtr);
			textPtr = OS.g_malloc (bytes.length);
			OS.memmove (textPtr, bytes, bytes.length);
			return textPtr;
		} 
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_text_after_offset != 0) {
				return OS.call (textIface.get_text_after_offset, handle, offset, boundary_type, start_offset, end_offset);
			}
		}
		return 0;
	}

	int atkText_get_text_at_offset (int offset, int boundary_type, int start_offset, int end_offset) {
		// TODO according to new gnome doc this determined text wrong
		String text = getText ();
		if (text != null) {
			int startBounds = offset;
			String beforeText = text.substring (0, offset);
			int endBounds = offset;
			String afterText = text.substring (offset);
			switch (boundary_type) {
				case ATK.ATK_TEXT_BOUNDARY_CHAR: {
					if (afterText.length () > 0) endBounds++;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_LINE_START:
				case ATK.ATK_TEXT_BOUNDARY_LINE_END: {
					startBounds = beforeText.lastIndexOf ('\n') + 1; // TODO use platform line delimiter?
					int newlineIndex = afterText.indexOf ('\n');	//TODO use platform line delimiter?
					if (newlineIndex == -1) newlineIndex = afterText.length ();
					endBounds += newlineIndex;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_START:
				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_END: {
					// TODO ask the client for eligible separators?
					int separatorIndex = 0;
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('.') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('?') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('!') + 1);
					startBounds = separatorIndex;
					separatorIndex = afterText.length ();
					int periodIndex = afterText.indexOf ('.');
					if (periodIndex != -1) separatorIndex = Math.min (separatorIndex, periodIndex + 1);
					int questionIndex = afterText.indexOf ('?');
					if (questionIndex != -1) separatorIndex = Math.min (separatorIndex, questionIndex + 1);
					int exclaimationIndex = afterText.indexOf ('!');
					if (exclaimationIndex != -1) separatorIndex = Math.min (separatorIndex, exclaimationIndex + 1);
					endBounds += separatorIndex;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_WORD_START:
				case ATK.ATK_TEXT_BOUNDARY_WORD_END: {
					// TODO ask the client for eligible separators?
					int separatorIndex = 0;
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf (' ') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('\n') + 1);
					startBounds = separatorIndex;
					separatorIndex = afterText.length ();
					int spaceIndex = afterText.indexOf (' ');
					if (spaceIndex != -1) separatorIndex = Math.min (separatorIndex, spaceIndex);
					int newlineIndex = afterText.indexOf ('\n');
					if (newlineIndex != -1) separatorIndex = Math.min (separatorIndex, newlineIndex);
					endBounds += separatorIndex;
					break;
				}
			}
			OS.memmove (start_offset, new int[] {startBounds}, 4);
			OS.memmove (end_offset, new int[] {endBounds}, 4);
			text = text.substring (startBounds, endBounds);
			byte[] bytes = Converter.wcsToMbcs (null, text, true);
//			TODO gnopernicus bug? freeing previous string can cause gp
//			if (textPtr != -1) OS.g_free (textPtr);
			textPtr = OS.g_malloc (bytes.length);
			OS.memmove (textPtr, bytes, bytes.length);
			return textPtr;
		} 
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_text_at_offset != 0) {
				return OS.call (textIface.get_text_at_offset, handle, offset, boundary_type, start_offset, end_offset);
			}
		}
		return 0;
	}

	int atkText_get_text_before_offset (int offset, int boundary_type, int start_offset, int end_offset) {
		//	TODO according to new gnome doc this determined text wrong
		String text = getText ();
		if (text != null) {
			int startBounds = offset;
			String beforeText = text.substring (0, offset);
			switch (boundary_type) {
				case ATK.ATK_TEXT_BOUNDARY_CHAR: {
					if (beforeText.length () > 0) startBounds--;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_LINE_START:
				case ATK.ATK_TEXT_BOUNDARY_LINE_END: {
					startBounds = beforeText.lastIndexOf ('\n') + 1; // TODO use platform line delimiter?
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_START:
				case ATK.ATK_TEXT_BOUNDARY_SENTENCE_END: {
					// TODO ask the client for eligible separators?
					int separatorIndex = 0;
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('.') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('?') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('!') + 1);
					startBounds = separatorIndex;
					break;
				}
				case ATK.ATK_TEXT_BOUNDARY_WORD_START:
				case ATK.ATK_TEXT_BOUNDARY_WORD_END: {
					// TODO ask the client for eligible separators?
					int separatorIndex = 0;
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf (' ') + 1);
					separatorIndex = Math.max (separatorIndex, beforeText.lastIndexOf ('\n') + 1);
					startBounds = separatorIndex;
					break;
				}
			}
			OS.memmove (start_offset, new int[] {startBounds}, 4);
			OS.memmove (end_offset, new int[] {offset}, 4);
//			text = text.substring (start_offset, end_offset);
			byte[] bytes = Converter.wcsToMbcs (null, text, true);
//			TODO gnopernicus bug? freeing previous string can cause gp
//			if (textPtr != -1) OS.g_free (textPtr);
			textPtr = OS.g_malloc (bytes.length);
			OS.memmove (textPtr, bytes, bytes.length);
			return textPtr;
		} 
		if (OS.g_type_is_a (parentType, AccessibleType.ATK_TEXT_TYPE)) {
			int superType = OS.g_type_class_peek (parentType);
			AtkTextIface textIface = new AtkTextIface ();
			ATK.memmove (textIface, superType);
			if (textIface.get_text_before_offset != 0) {
				return OS.call (textIface.get_text_before_offset, handle, offset, boundary_type, start_offset, end_offset);
			}
		}
		return 0;
	}

	void dispose () {
		if (DEBUG) System.out.println("AccessibleObject.dispose: " + handle);
		Enumeration elements = children.elements ();
		while (elements.hasMoreElements ()) {
			AccessibleObject child = (AccessibleObject) elements.nextElement ();
			if (child.isLightweight) OS.g_object_unref (child.handle);
		}
		if (parent != null) parent.removeChild (this, false);
		if (namePtr != -1) OS.g_free (namePtr);
		namePtr = -1;
		if (descriptionPtr != -1) OS.g_free (descriptionPtr);
		descriptionPtr = -1;
		if (keybindingPtr != -1) OS.g_free (keybindingPtr);
		keybindingPtr = -1;
		if (actionNamePtr != -1) OS.g_free (actionNamePtr);
		actionNamePtr = -1;
		if (textPtr != -1) OS.g_free (textPtr);
		textPtr = -1;
		if (valuePtr != -1) OS.g_free (valuePtr);
		valuePtr = -1;
	}

	AccessibleObject getChildByHandle (int handle) {
		return (AccessibleObject) children.get (new Integer (handle));	
	}	

	AccessibleObject getChildByID (int childId) {
		if (childId == ACC.CHILDID_SELF) return this;
		Enumeration elements = children.elements ();
		while (elements.hasMoreElements ()) {
			AccessibleObject object = (AccessibleObject) elements.nextElement ();
			if (object.id == childId) return object;
		}
		return null;
	}
	
	AccessibleObject getChildByIndex (int childIndex) {
		Enumeration elements = children.elements ();
		while (elements.hasMoreElements ()) {
			AccessibleObject object = (AccessibleObject) elements.nextElement ();
			if (object.index == childIndex) return object;
		}
		return null;
	}
	
	int getIndexOfChar (String string, String searchChars, int startIndex) {
		int result = string.length ();
		for (int i = 0; i < searchChars.length (); i++) {
			char current = searchChars.charAt (i);
			int index = string.indexOf (current, startIndex);
			if (index != -1) result = Math.min (result, index);
		}
		return result;
	}
	
	String getText () {
		if (accessible.getAccessibleListeners ().length == 0) return null;
		AccessibleControlListener[] listeners = accessible.getControlListeners();
		AccessibleControlEvent event = new AccessibleControlEvent (this);
		event.childID = id;
		for (int i = 0; i < listeners.length; i++) {
			listeners [i].getValue(event);				
		} 
		return event.result;
	}

	void removeChild (AccessibleObject child, boolean unref) {
		children.remove (new Integer (child.handle));
		if (unref && child.isLightweight) OS.g_object_unref (child.handle);
	}

	void setFocusToChild (int childId) {
		updateChildren ();
		AccessibleObject accObject = getChildByID (childId);
		if (accObject != null) {
			ATK.atk_focus_tracker_notify (accObject.handle);
		}
	}
	
	void setParent (AccessibleObject parent) {
		this.parent = parent;
	}
	
	void updateChildren () {
		if (accessible.getControlListeners ().length > 0) {
			AccessibleControlListener[] listener = accessible.getControlListeners ();
			AccessibleControlEvent event = new AccessibleControlEvent (this);
			for (int i = 0; i < listener.length; i++) {
				listener [i].getChildren (event);
			}
			if (event.children != null && event.children.length > 0) {
				Hashtable childrenCopy = (Hashtable)children.clone ();
				if (event.children [0] instanceof Integer) {
					//	an array of child id's (Integers) was answered
					AccessibleType childType = AccessibleType.getInstance ();
					for (int i = 0; i < event.children.length; i++) {
						AccessibleObject object = getChildByIndex (i);
						if (object == null) {
							object = new AccessibleObject (childType.handle, 0, accessible, childType.handle, true);
							childType.addInstance (object);
							addChild (object);
							object.index = i;
						}
						try {
							object.id = ((Integer)event.children [i]).intValue ();
						} catch (ClassCastException e) {
							// a non-ID value was given so don't set the ID
						}
						childrenCopy.remove (new Integer (object.handle));
					}
				} else {
					// an array of Accessible children was answered
					for (int i = 0; i < event.children.length; i++) {
						AccessibleObject object = null;
						try {
							object = ((Accessible)event.children [i]).accessibleObject;
						} catch (ClassCastException e) {
							// a non-Accessible value was given so nothing to do here 
						}
						object.index = i;
						childrenCopy.remove (new Integer (object.handle));
					}
				}
				// remove previous children of self which were not answered
				Enumeration childrenToRemove = childrenCopy.elements ();
				while (childrenToRemove.hasMoreElements ()) {
					AccessibleObject object = (AccessibleObject) childrenToRemove.nextElement (); 
					removeChild (object, true);
				}
			}
		}
	}
}
