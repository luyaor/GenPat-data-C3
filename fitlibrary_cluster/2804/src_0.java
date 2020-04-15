/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fitlibrary.specify.eg.MyPoint;

public class AutoWrap {
	public int[] getAnArrayOfInt() {
		return new int[]{1,2,3};
	}
	public Integer[] getAnArrayOfInteger() {
		return new Integer[]{new Integer(1),new Integer(2),new Integer(3)};
	}
	public Object[] getAnArrayOfPoint() {
	    return new Object[] { new MyPoint(0,0), new MyPoint(5,5) };
	}
	@SuppressWarnings("unchecked")
	public List getAListOfPoint() {
	    List list = new ArrayList();
	    list.add(new MyPoint(0,0));
	    list.add(new MyPoint(5,5));
	    return list;
	}
	@SuppressWarnings("unchecked")
	public Iterator getAnIteratorOfPoint() {
	    return getAListOfPoint().iterator();
	}
	@SuppressWarnings("unchecked")
	public Set getASetOfPoint() {
	    return new HashSet(getAListOfPoint());
	}
}
