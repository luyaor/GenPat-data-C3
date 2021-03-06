/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.generics;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * Processor that tests our handling of parameterized types
 * by exploring the parameterized types in resources/targets.
 */
@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GenericsProc extends BaseProcessor
{
	// Initialized in collectElements()
	private TypeElement _elementAC;
	private TypeElement _elementString;
	private TypeElement _elementIterator;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) {
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		
		if (!collectElements()) {
			return false;
		}
		
		if (!examineACNames()) {
			return false;
		}
		
		if (!examineACTypeParams()) {
			return false;
		}
		
		reportSuccess();
		return false;
	}

	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if all tests passed
	 */
	private boolean collectElements() {
		_elementAC = _elementUtils.getTypeElement("targets.model.pb.AC");
		if (_elementAC == null) {
			reportError("element AC was not found");
			return false;
		}
		if (_elementAC.getKind() != ElementKind.CLASS) {
			reportError("AC claims to not be a class");
			return false;
		}
		_elementString = _elementUtils.getTypeElement("java.lang.String");
		_elementIterator = _elementUtils.getTypeElement("java.util.Iterator");
		return true;
	}

	/**
	 * Examine the qualified and simple names of element AC and subelements
	 * @return true if all tests passed
	 */
	private boolean examineACNames()
	{
		String qnameAC = _elementAC.getQualifiedName().toString();
		if (!"targets.model.pb.AC".equals(qnameAC)) {
			reportError("AC's qualified name is unexpected: " + qnameAC);
			return false;
		}
		String snameAC = _elementAC.getSimpleName().toString();
		if (!"AC".equals(snameAC)) {
			reportError("AC's simple name is unexpected: " + snameAC);
			return false;
		}
		List<TypeElement> childElements = ElementFilter.typesIn(_elementAC.getEnclosedElements());
		if (childElements == null || childElements.size() != 1) {
			reportError("AC should contain one child type");
			return false;
		}
		TypeElement elementACInner = childElements.iterator().next();
		String qnameInner = elementACInner.getQualifiedName().toString();
		if (!"targets.model.pb.AC.ACInner".equals(qnameInner)) {
			reportError("AC.ACInner's qualified name is unexpected: " + qnameInner);
			return false;
		}
		String snameInner = elementACInner.getSimpleName().toString();
		if (!"ACInner".equals(snameInner)) {
			reportError("AC.ACInner's simple name is unexpected: " + snameInner);
			return false;
		}
		return true;
	}

	/**
	 * Examine the type parameters of element AC
	 * @return true if all tests passed
	 */
	private boolean examineACTypeParams()
	{
		List<? extends TypeParameterElement> params = _elementAC.getTypeParameters();
		if (null == params || params.size() != 2) {
			reportError("element AC does not report 2 type parameters");
			return false;
		}
		Iterator<? extends TypeParameterElement> iter = params.iterator();
		TypeParameterElement t1 = iter.next();
		TypeParameterElement t2 = iter.next();
		if (!"T1".equals(t1.getSimpleName().toString()) || 
				!"T2".equals(t2.getSimpleName().toString())) {
			reportError("Type parameters of element AC are not named T1 and T2");
			return false;
		}
		if (t1.getKind() != ElementKind.TYPE_PARAMETER) {
			reportError("Type parameter T1 of element AC claims not to be ElementKind.TYPE_PARAMTER");
			return false;
		}
		if (!_elementAC.equals(t2.getGenericElement())) {
			reportError("Type parameter T2 of element AC does not return AC from getGenericElement()");
			return false;
		}
		List<? extends TypeMirror> boundsT1 = t1.getBounds();
		if (null == boundsT1 || boundsT1.size() != 2) {
			reportError("Type parameter T1 of element AC has wrong number of bounds");
			return false;
		}
		TypeMirror boundT1_0 = boundsT1.get(0);
		if (!(boundT1_0 instanceof DeclaredType) || !_elementString.equals(((DeclaredType)boundT1_0).asElement())) {
			reportError("Bound[0] of type parameter T1 of element AC is not String");
			return false;
		}
		TypeMirror boundT1_1 = boundsT1.get(1);
		if (!(boundT1_1 instanceof DeclaredType) || !_elementIterator.equals(((DeclaredType)boundT1_1).asElement())) {
			reportError("Bound[1] of type parameter T1 of element AC is not Iterator");
			return false;
		}
		return true;
	}

}
