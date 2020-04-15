/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.closure;

import java.util.List;

import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.ExtendedCamelCase;

public class ActionSignature {
	public final String name;
	public final int arity;
	
	public ActionSignature(String name, int arity) {
		this.name = ExtendedCamelCase.camel(name);
		this.arity = arity;
	}
	@Override
	public String toString() {
		return name+"/"+arity;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ActionSignature))
			return false;
		ActionSignature other = (ActionSignature) obj;
		return arity == other.arity && name.equals(other.name);
	}
	@Override
	public int hashCode() {
		return name.hashCode()+arity*17;
	}
	
	public static ActionSignature create(Row row, int from, int upTo,
			boolean doStyle, Evaluator evaluator) {
		if (!doStyle)
			return new ActionSignature(ExtendedCamelCase.camel(row.text(from, evaluator)), upTo-from-1);
		StringBuilder name = new StringBuilder(row.text(from, evaluator));
		for (int i = from + 2; i < upTo; i += 2)
			name.append(" ").append(row.text(i, evaluator));
		return new ActionSignature(ExtendedCamelCase.camel(name.toString()), (upTo - from) / 2);
	}
	public static ActionSignature doStyle(List<String> cells) {
		return doStyle(cells.toArray(new String[0]));
	}
	public static ActionSignature doStyle(String... cells) {
		String name = cells[0];
		for (int i = 2; i < cells.length; i += 2)
			name += " "+cells[i];
		return new ActionSignature(ExtendedCamelCase.camel(name), cells.length/2);
	}
	public static ActionSignature seqStyle(List<String> cells) {
		return seqStyle(cells.toArray(new String[0]));
	}
	public static ActionSignature seqStyle(String... cells) {
		return new ActionSignature(cells[0], cells.length-1);
	}
}
