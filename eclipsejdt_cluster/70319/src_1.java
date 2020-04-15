/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;

/**
 * Implementation of PackageElement, which represents a package
 */
public class PackageElementImpl extends ElementImpl implements PackageElement {

	PackageElementImpl(PackageBinding binding) {
		super(binding);
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitPackage(this, p);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		//PackageBinding binding = (PackageBinding)_binding;
		throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
	}

	@Override
	public Element getEnclosingElement() {
		// packages have no enclosing element
		return null;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.PACKAGE;
	}

	@Override
	PackageElement getPackage()
	{
		return this;
	}

	@Override
	public Name getQualifiedName() {
		return new NameImpl(CharOperation.concatWith(((PackageBinding)_binding).compoundName, '.'));
	}

	@Override
	public boolean isUnnamed() {
		PackageBinding binding = (PackageBinding)_binding;
		return binding.compoundName == CharOperation.NO_CHAR_CHAR;
	}

}
