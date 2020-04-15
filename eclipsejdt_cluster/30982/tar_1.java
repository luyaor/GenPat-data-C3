/*******************************************************************************
 * Copyright (c) 2013 GK Software AG and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * Performs matching of null type annotations.
 * Instances are used to encode result from this analysis.
 * @since 3.9 BETA_JAVA8
 */
public class NullAnnotationMatching {
	
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(0, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_UNCHECKED = new NullAnnotationMatching(1, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(2, null);

	/** 0 = OK, 1 = unchecked, 2 = definite mismatch */
	public final int severity;
	
	/** If non-null this field holds the supertype of the provided type which was used for direct matching. */
	public final TypeBinding superTypeHint;
	
	public NullAnnotationMatching(int severity, TypeBinding superTypeHint) {
		this.severity = severity;
		this.superTypeHint = superTypeHint;
	}

	public boolean isAnyMismatch()      { return this.severity != 0; }
	public boolean isUnchecked()        { return this.severity == 1; }
	public boolean isDefiniteMismatch() { return this.severity == 2; }
	
	public String superTypeHintName(CompilerOptions options, boolean shortNames) {
		return String.valueOf(this.superTypeHint.nullAnnotatedReadableName(options, shortNames));
	}
	
	/** Check null-ness of 'var' against a possible null annotation */
	public static int checkAssignment(BlockScope currentScope, FlowContext flowContext,
									   VariableBinding var, int nullStatus, Expression expression, TypeBinding providedType)
	{
		long lhsTagBits = 0L;
		boolean hasReported = false;
		if (currentScope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_8) {
			lhsTagBits = var.tagBits & TagBits.AnnotationNullMASK;
		} else {
			lhsTagBits = var.type.tagBits & TagBits.AnnotationNullMASK;
			NullAnnotationMatching annotationStatus = analyse(var.type, providedType, nullStatus);
			if (annotationStatus.isDefiniteMismatch()) {
				currentScope.problemReporter().nullityMismatchingTypeAnnotation(expression, providedType, var.type, annotationStatus);
				hasReported = true;
			} else if (annotationStatus.isUnchecked()) {
				flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, nullStatus);
				hasReported = true;
			}
		}
		if (lhsTagBits == TagBits.AnnotationNonNull && nullStatus != FlowInfo.NON_NULL) {
			if (!hasReported)
				flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, nullStatus);
			return FlowInfo.NON_NULL;
		} else if (lhsTagBits == TagBits.AnnotationNullable && nullStatus == FlowInfo.UNKNOWN) {	// provided a legacy type?
			return FlowInfo.POTENTIALLY_NULL;			// -> use more specific info from the annotation
		}
		return nullStatus;
	}

	/**
	 * Find any mismatches between the two given types, which are caused by null type annotations.
	 * @param requiredType
	 * @param providedType
	 * @param nullStatus we are only interested in NULL or NON_NULL, -1 indicates that we are in a recursion, where flow info is ignored
	 * @return a status object representing the severity of mismatching plus optionally a supertype hint
	 */
	public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType, int nullStatus) {
		int severity = 0;
		TypeBinding superTypeHint = null;
		if (requiredType instanceof ArrayBinding) {
			long[] requiredDimsTagBits = ((ArrayBinding)requiredType).nullTagBitsPerDimension;
			if (requiredDimsTagBits != null) {
				int dims = requiredType.dimensions();
				if (requiredType.dimensions() == providedType.dimensions()) {
					long[] providedDimsTagBits = ((ArrayBinding)providedType).nullTagBitsPerDimension;
					if (providedDimsTagBits == null) {
						severity = 1; // required is annotated, provided not, need unchecked conversion
					} else {
						for (int i=0; i<=dims; i++) {
							long requiredBits = validNullTagBits(requiredDimsTagBits[i]);
							long providedBits = validNullTagBits(providedDimsTagBits[i]);
							if (i > 0)
								nullStatus = -1; // don't use beyond the outermost dimension
							severity = Math.max(severity, computeNullProblemSeverity(requiredBits, providedBits, nullStatus));
							if (severity == 2)
								return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
						}
					}
				} else if (providedType.id == TypeIds.T_null) {
					if (dims > 0 && requiredDimsTagBits[0] == TagBits.AnnotationNonNull)
						return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
				}
			}
		} else if (requiredType.hasNullTypeAnnotations() || providedType.hasNullTypeAnnotations()) {
			long requiredBits = validNullTagBits(requiredType.tagBits);
			if (requiredBits != TagBits.AnnotationNullable // nullable lhs accepts everything, ...
					|| nullStatus == -1) // only at detail/recursion even nullable must be matched exactly
			{
				long providedBits = validNullTagBits(providedType.tagBits);
				severity = computeNullProblemSeverity(requiredBits, providedBits, nullStatus);
			}
			if (severity < 2) {
				TypeBinding providedSuper = providedType.findSuperTypeOriginatingFrom(requiredType);
				if (providedSuper != providedType)
					superTypeHint = providedSuper;
				if (requiredType.isParameterizedType()  && providedSuper instanceof ParameterizedTypeBinding) { // TODO(stephan): handle providedType.isRaw()
					TypeBinding[] requiredArguments = ((ParameterizedTypeBinding) requiredType).arguments;
					TypeBinding[] providedArguments = ((ParameterizedTypeBinding) providedSuper).arguments;
					if (requiredArguments != null && providedArguments != null && requiredArguments.length == providedArguments.length) {
						for (int i = 0; i < requiredArguments.length; i++) {
							NullAnnotationMatching status = analyse(requiredArguments[i], providedArguments[i], -1);
							severity = Math.max(severity, status.severity);
							if (severity == 2)
								return new NullAnnotationMatching(severity, superTypeHint);
						}
					}
				} else 	if (requiredType instanceof WildcardBinding) {
					WildcardBinding wildcardBinding = (WildcardBinding) requiredType;
					if (wildcardBinding.bound != null) {
						NullAnnotationMatching status = analyse(wildcardBinding.bound, providedType, nullStatus);
						severity = Math.max(severity, status.severity);
					}
					// TODO(stephan): what about otherBounds? Do we accept "? extends @NonNull I1 & @Nullable I2" in the first place??
				}
				TypeBinding requiredEnclosing = requiredType.enclosingType();
				TypeBinding providedEnclosing = providedType.enclosingType();
				if (requiredEnclosing != null && providedEnclosing != null) {
					NullAnnotationMatching status = analyse(requiredEnclosing, providedEnclosing, -1);
					severity = Math.max(severity, status.severity);
				}
			}
		}
		if (severity == 0)
			return NullAnnotationMatching.NULL_ANNOTATIONS_OK;
		return new NullAnnotationMatching(severity, superTypeHint);
	}

	public static long validNullTagBits(long bits) {
		bits &= TagBits.AnnotationNullMASK;
		return bits == TagBits.AnnotationNullMASK ? 0 : bits;
	}
	
	/** Provided that both types are {@link TypeBinding#equalsEquals}, return the one that is more likely to show null at runtime. */
	public static TypeBinding moreDangerousType(TypeBinding one, TypeBinding two) {
		if (one == null) return null;
		long oneNullBits = validNullTagBits(one.tagBits);
		long twoNullBits = validNullTagBits(two.tagBits);
		if (oneNullBits != twoNullBits) {
			if (oneNullBits == TagBits.AnnotationNullable)
				return one;			// nullable is dangerous
			if (twoNullBits == TagBits.AnnotationNullable)
				return two;			// nullable is dangerous
			// below this point we have unknown vs. nonnull, which is which?
			if (oneNullBits == 0)
				return one;			// unknown is more dangerous than nonnull
			return two;				// unknown is more dangerous than nonnull
		} else if (one != two) {
			if (analyse(one, two, -1).isAnyMismatch())
				return two;			// two doesn't snugly fit into one, so it must be more dangerous
		}
		return one;
	}

	private static int computeNullProblemSeverity(long requiredBits, long providedBits, int nullStatus) {
		if (requiredBits != 0 && requiredBits != providedBits) {
			if (requiredBits == TagBits.AnnotationNonNull && nullStatus == FlowInfo.NON_NULL) {
				return 0; // OK by flow analysis
			}
			if (providedBits != 0) {
				return 2; // mismatching annotations
			} else {
				return 1; // need unchecked conversion regarding type detail
			}
		}
		return 0; // OK by tagBits
	}
}