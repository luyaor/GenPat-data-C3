/*******************************************************************************
 * Copyright (c) 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

@SuppressWarnings({"rawtypes", "unchecked"})
public class InnerInferenceHelper {

	/** For each candidate method store here the array of argument types if inner inference has improved any during Invocation Type Inference. */
	private Map/*<MethodBinding,TypeBinding[]>*/ argTypesPerCandidate = new HashMap();

	public void registerInnerResult(MethodBinding method, TypeBinding resolvedType, int argCount, int argIdx) {
		TypeBinding[] argTypes = (TypeBinding[]) this.argTypesPerCandidate.get(method);
		if (argTypes == null)
			this.argTypesPerCandidate.put(method, argTypes = new TypeBinding[argCount]);
		argTypes[argIdx] = resolvedType;
	}
	
	public TypeBinding[] getArgumentTypesForCandidate(MethodBinding candidate, TypeBinding[] plainArgTypes) {
		TypeBinding[] argTypes = (TypeBinding[]) this.argTypesPerCandidate.get(candidate);
		return argTypes != null ? argTypes : plainArgTypes;
	}
}
