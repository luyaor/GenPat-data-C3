/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 12/11/2006
 */

package fitlibrary.parser;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.parser.lookup.ParserFactory;
import fitlibrary.table.ICell;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;
import fitlibraryGeneric.typed.GenericTyped;

public class EnumParser implements Parser {
	private GenericTyped typed;
	private Evaluator evaluator;

	public EnumParser(GenericTyped typed, Evaluator evaluator) {
		this.typed = typed;
		this.evaluator = evaluator;
	}
	@SuppressWarnings("unchecked")
	public TypedObject parseTyped(ICell cell, TestResults testResults) throws Exception {
		String text = cell.text(evaluator);
		if (text.equals(""))
			return  typed.typedObject(null);
		Class asClass = typed.asClass();
		try {
			return typed.typedObject(Enum.valueOf(asClass, ExtendedCamelCase.camel(text.replaceAll("\\s+","")).toUpperCase()));
		} catch (IllegalArgumentException e) {
			try {
				return typed.typedObject(Enum.valueOf(asClass, ExtendedCamelCase.camel(text.replaceAll("\\s+","_")).toUpperCase()));
			} catch (IllegalArgumentException e2) {
				throw new FitLibraryException("Unknown");
			}
		}
	}
	public boolean matches(ICell cell, Object result, TestResults testResults) throws Exception {
		if (cell.hasEmbeddedTable()) {
			cell.unexpected(testResults,"collection");
			return false;
		}
		if (cell.text(evaluator).equals(""))
			return result == null;
		Object parsed = parseTyped(cell,testResults).getSubject();
		return parsed.equals(result);
	}
	public String show(Object result) throws Exception {
		return result.toString();
	}
    public static ParserFactory parserFactory() {
    	return new ParserFactory() {
    		public Parser parser(Evaluator evaluator, Typed typed) {
    			return new EnumParser((GenericTyped) typed,evaluator);
    		}
    	};
    }
    @SuppressWarnings("unused")
    public Evaluator traverse(TypedObject typedObject) {
		throw new RuntimeException("No Traverse available");
	}
}
