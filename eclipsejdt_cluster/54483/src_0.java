/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

//TODO: I'm putting these things into HashSets, so I better figure out equality and hash!
public class TypeElementImpl extends ElementImpl implements TypeElement {
	
	public static TypeElement newTypeElementImpl(ReferenceBinding binding)
	{
		//TODO: to get equality, probably want to cache these.  What's the lifecycle of the cache and who owns it?
		return new TypeElementImpl(binding);
	}

	private TypeElementImpl(ReferenceBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getFileName()
	 */
	@Override
	public String getFileName() {
		char[] name = ((ReferenceBinding)_binding).getFileName();
		if (name == null)
			return null;
		return new String(name);
	}
	
	protected ReferenceBinding getReferenceBinding() {
		return (ReferenceBinding)_binding;
	}

	public List<? extends TypeMirror> getInterfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public NestingKind getNestingKind() {
		// TODO Auto-generated method stub
		return null;
	}

	public Name getQualifiedName() {
		//TODO: what is the right way to get this (including member types, parameterized types, ...?
		return new NameImpl(CharOperation.concatWith(getReferenceBinding().compoundName, '.'));
	}

	public TypeMirror getSuperclass() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends TypeParameterElement> getTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return _binding.toString();
	}

}
