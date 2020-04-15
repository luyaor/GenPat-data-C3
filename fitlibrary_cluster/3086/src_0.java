/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import fit.Fixture;
import fitlibrary.collection.CollectionTraverse;
import fitlibrary.parser.collection.ArrayParser;
import fitlibrary.parser.collection.ListParser;
import fitlibrary.parser.collection.MapParser;
import fitlibrary.parser.collection.SetParser;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.typed.TypedObject;

public class DoAutoWrapper {
	private final Evaluator evaluator;
	
	public DoAutoWrapper(Evaluator evaluator) {
		this.evaluator = evaluator;
	}
	public Object wrapObjectWithTraverse(TypedObject typedResult) {
		if (typedResult == null)
			return null;
		Object result = typedResult.getSubject();
		if (result == null)
			return null;
		if (notToBeAutoWrapped(result))
			return result;
		if (result instanceof Evaluator) {
			Evaluator resultEvaluator = (Evaluator)result;
			if (resultEvaluator != evaluator && resultEvaluator.getNextOuterContext() == null)
				return withOuter(resultEvaluator);
		    return resultEvaluator;
		}
		if (result instanceof Fixture)
		    return result;
		
		Class<?> returnType = result.getClass();
		if (MapParser.applicableType(returnType) || ArrayParser.applicableType(returnType))
			return withOuter(typedResult.traverse(evaluator));
		if (SetParser.applicableType(returnType) || ListParser.applicableType(returnType)) {
			CollectionTraverse traverse = (CollectionTraverse) typedResult.traverse(evaluator);
			traverse.setActualCollection(result);
			return withOuter(traverse);
		}
		if (ParseDelegation.hasParseMethod(returnType))
		    return result;
		return withOuter(new DoTraverse(typedResult));
	}
	public static boolean canAutoWrap(Object result) {
		return !(result instanceof String || result instanceof StringBuffer || isPrimitiveType(result.getClass()));
	}
	private boolean notToBeAutoWrapped(Object result) {
		return result instanceof String || result instanceof StringBuffer || 
			isPrimitiveType(result.getClass());
	}

	private Object withOuter(Evaluator inner) {
		inner.setOuterContext(evaluator);
		inner.setRuntimeContext(evaluator.getRuntimeContext());
		return inner;
	}
	private static boolean isPrimitiveType(Class<?> returnType) {
		return returnType.isPrimitive() ||
			   returnType == Boolean.class ||
			   Number.class.isAssignableFrom(returnType) ||
			   returnType == Character.class;
	}
}
