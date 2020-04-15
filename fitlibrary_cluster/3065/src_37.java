/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 22/10/2006
*/

package fitlibraryGeneric.typed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import fitlibrary.parser.lookup.FieldParser;
import fitlibrary.parser.lookup.GetterParser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.NonGenericTypedObject;
import fitlibrary.typed.Typed;
import fitlibrary.typed.TypedObject;

public class GenericTypedObject extends NonGenericTypedObject {
	public final static GenericTypedObject NULL = new GenericTypedObject(null);
	private final GenericTyped typed;

	public GenericTypedObject(Object subject, GenericTyped typed) {
		super(subject);
		this.typed = typed;
	}
	public GenericTypedObject(Object subject) {
		super(subject);
		if (subject == null)
			this.typed = new GenericTyped(void.class);
		else
			this.typed = new GenericTyped(subject.getClass());
	}
	@Override
	protected TypedObject asTypedObject(Object sut) {
		return new GenericTypedObject(sut);
	}
	@Override
	public Class<?> getClassType() {
		return typed.asClass();
	}
	@Override
	protected Typed resultTyped(Method method) {
		Type genericReturnType = typed.bind(method.getGenericReturnType(),describe(method));
		return new GenericTyped(genericReturnType,true);
	}
	@Override
	protected Typed resultTyped(Field field) {
		Type genericReturnType = typed.bind(field.getGenericType(),describe(field));
		return new GenericTyped(genericReturnType,true);
	}
	@SuppressWarnings("unchecked")
	@Override
	public GetterParser resultParser(Evaluator evaluator, Method method, Class actualResultType) {
		Typed resultTyped = new GenericTyped(actualResultType,true);
		return new GetterParser(typed.on(evaluator,resultTyped,true),method);
	}
	@SuppressWarnings("unchecked")
	@Override
	public ResultParser resultParser(Evaluator evaluator, Field field, Class actualResultType) {
		Typed resultTyped = new GenericTyped(actualResultType,true);
		return new FieldParser(typed.on(evaluator,resultTyped,true),field);
	}
	@Override
	protected Typed parameterTyped(Method method, int parameterNo) {
		Type givenType = method.getGenericParameterTypes()[parameterNo];
		Type genericParameterType = typed.bind(givenType,describe(method));
		return new GenericTyped(genericParameterType,true);
	}
	@Override
	public TypedObject asReturnTypedObject(Object object, Method method) {
		return new GenericTypedObject(object,
				new GenericTyped(typed.bind(method.getGenericReturnType(),describe(method))));
	}
	@Override
	public TypedObject asReturnTypedObject(Object object, Field field) {
		return new GenericTypedObject(object,
				new GenericTyped(typed.bind(field.getGenericType(),describe(field))));
	}
	@Override
	public String toString() {
		return "GenericTypedObject["+subject+":"+typed+"]";
	}
	@Override
	public Typed getTyped() {
		return typed;
	}
	private String describe(Method method) {
		return "in "+method.toGenericString();
	}
	private String describe(Field field) {
		return "in "+field.getName();
	}

}
