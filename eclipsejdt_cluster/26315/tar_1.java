/*******************************************************************************
 * Copyright (c) 2000, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Provides methods for computing Java-specific names.
 * <p>
 * The bevavior of the methods is dependent of several JavaCore options.<br>
 * The possible options are :
 * <ul>
 * <li>CODEASSIST_FIELD_PREFIXES : Define the Prefixes for Field Name.</li>
 * <li>CODEASSIST_STATIC_FIELD_PREFIXES : Define the Prefixes for Static Field Name.</li>
 * <li>CODEASSIST_LOCAL_PREFIXES : Define the Prefixes for Local Variable Name.</li>
 * <li>CODEASSIST_ARGUMENT_PREFIXES : Define the Prefixes for Argument Name.</li>
 * <li>CODEASSIST_FIELD_SUFFIXES : Define the Suffixes for Field Name.</li>
 * <li>CODEASSIST_STATIC_FIELD_SUFFIXES : Define the Suffixes for Static Field Name.</li>
 * <li>CODEASSIST_LOCAL_SUFFIXES : Define the Suffixes for Local Variable Name.</li>
 * <li>CODEASSIST_ARGUMENT_SUFFIXES : Define the Suffixes for Argument Name.</li>
 * </ul>
 * <p>
 * 
 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
 * 
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @see JavaCore#setOptions
 * @see JavaCore#getDefaultOptions
 * @since 2.1
 */
public final class NamingConventions {
	private static final char[] DEFAULT_NAME = "name".toCharArray(); //$NON-NLS-1$
	
	private static final char[] GETTER_BOOL_NAME = "is".toCharArray(); //$NON-NLS-1$
	private static final char[] GETTER_NAME = "get".toCharArray(); //$NON-NLS-1$
	private static final char[] SETTER_NAME = "set".toCharArray(); //$NON-NLS-1$
	
	/**
	 * Not instantiable.
	 */
	private NamingConventions() {}
	
	private static char[] computeBaseNames(char firstName, char[][] prefixes, char[][] excludedNames){
		char[] name = new char[]{firstName};
		
		for(int i = 0 ; i < excludedNames.length ; i++){
			if(CharOperation.equals(name, excludedNames[i], false)) {
				name[0]++;
				if(name[0] > 'z')
					name[0] = 'a';
				if(name[0] == firstName)
					return null;
				i = 0;
			}	
		}
		
		return name;
	}
	

	private static char[][] computeNames(char[] sourceName){
		char[][] names = new char[5][];
		int nameCount = 0;
		boolean previousIsUpperCase = false;
		boolean previousIsLetter = true;
		for(int i = sourceName.length - 1 ; i >= 0 ; i--){
			boolean isUpperCase = Character.isUpperCase(sourceName[i]);
			boolean isLetter = Character.isLetter(sourceName[i]);
			if(isUpperCase && !previousIsUpperCase && previousIsLetter){
				char[] name = CharOperation.subarray(sourceName,i,sourceName.length);
				if(name.length > 1){
					if(nameCount == names.length) {
						System.arraycopy(names, 0, names = new char[nameCount * 2][], 0, nameCount);
					}
					name[0] = Character.toLowerCase(name[0]);
					names[nameCount++] = name;
				}
			}
			previousIsUpperCase = isUpperCase;
			previousIsLetter = isLetter;
		}
		if(nameCount == 0){
			names[nameCount++] = CharOperation.toLowerCase(sourceName);				
		}
		System.arraycopy(names, 0, names = new char[nameCount][], 0, nameCount);
		return names;
	}
	

	private static Scanner getNameScanner(CompilerOptions compilerOptions) {
		return
			new Scanner(
				false /*comment*/, 
				false /*whitespace*/, 
				false /*nls*/, 
				compilerOptions.sourceLevel >= CompilerOptions.JDK1_4 /*assert*/, 
				compilerOptions.complianceLevel >= CompilerOptions.JDK1_4 /*strict comment*/,
				null /*taskTags*/, 
				null/*taskPriorities*/);
	}
	
	private static char[] removePrefixAndSuffix(char[] name, char[][] prefixes, char[][] suffixes) {
		// remove longer prefix
		char[] withoutPrefixName = name;
		if (prefixes != null) {
			int bestLength = 0;
			for (int i= 0; i < prefixes.length; i++) {
				char[] prefix = prefixes[i];
				if (CharOperation.startsWith(name, prefix)) {
					int currLen = prefix.length;
					boolean lastCharIsLetter = Character.isLetter(prefix[currLen - 1]);
					if(!lastCharIsLetter || (lastCharIsLetter && name.length > currLen && Character.isUpperCase(name[currLen]))) {
						if (bestLength < currLen && name.length != currLen) {
							withoutPrefixName = CharOperation.subarray(name, currLen, name.length);
							bestLength = currLen;
						}
					}
				}
			}
		}
		
		// remove longer suffix
		char[] withoutSuffixName = withoutPrefixName;
		if(suffixes != null) {
			int bestLength = 0;
			for (int i = 0; i < suffixes.length; i++) {
				char[] suffix = suffixes[i];
				if(CharOperation.endsWith(withoutPrefixName, suffix)) {
					int currLen = suffix.length;
					if(bestLength < currLen && withoutPrefixName.length != currLen) {
						withoutSuffixName = CharOperation.subarray(withoutPrefixName, 0, withoutPrefixName.length - currLen);
						bestLength = currLen;
					}
				}
			}
		}
		
		withoutSuffixName[0] = Character.toLowerCase(withoutSuffixName[0]);
		return withoutSuffixName;
	}

	/**
	 * Remove prefix and suffix from an argument name.<br>
	 * If argument name prefix is <code>pre</code> and argument name suffix is <code>suf</code>
	 * then for an argument named <code>preArgsuf</code> the result of this method is <code>arg</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preArgsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_ARGUMENT_PREFIXES and
	 * CODEASSIST_ARGUMENT_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
 	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[] removePrefixAndSuffixForArgumentName(IJavaProject javaProject, char[] argumentName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			argumentName,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from an argument name.<br>
	 * If argument name prefix is <code>pre</code> and argument name suffix is <code>suf</code>
	 * then for an argument named <code>preArgsuf</code> the result of this method is <code>arg</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preArgsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_ARGUMENT_PREFIXES and
	 * CODEASSIST_ARGUMENT_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
 	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String removePrefixAndSuffixForArgumentName(IJavaProject javaProject, String argumentName) {
		return String.valueOf(removePrefixAndSuffixForArgumentName(javaProject, argumentName.toCharArray()));
	}

	/**
	 * Remove prefix and suffix from a field name.<br>
	 * If field name prefix is <code>pre</code> and field name suffix is <code>suf</code>
	 * then for a field named <code>preFieldsuf</code> the result of this method is <code>field</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preFieldsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @return char[] the name without prefix and suffix.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[] removePrefixAndSuffixForFieldName(IJavaProject javaProject, char[] fieldName, int modifiers) {
		boolean isStatic = Flags.isStatic(modifiers);
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			fieldName,
			isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
			isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes);
	}

	/**
	 * Remove prefix and suffix from a field name.<br>
	 * If field name prefix is <code>pre</code> and field name suffix is <code>suf</code>
	 * then for a field named <code>preFieldsuf</code> the result of this method is <code>field</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preFieldsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @return char[] the name without prefix and suffix.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String removePrefixAndSuffixForFieldName(IJavaProject javaProject, String fieldName, int modifiers) {
		return String.valueOf(removePrefixAndSuffixForFieldName(javaProject, fieldName.toCharArray(), modifiers));
	}
	/**
	 * Remove prefix and suffix from a local variable name.<br>
	 * If local variable name prefix is <code>pre</code> and local variable name suffix is <code>suf</code>
	 * then for a local variable named <code>preLocalsuf</code> the result of this method is <code>local</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preLocalsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_LOCAL_PREFIXES and 
	 * CODEASSIST_LOCAL_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[] removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, char[] localName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			localName,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from a local variable name.<br>
	 * If local variable name prefix is <code>pre</code> and local variable name suffix is <code>suf</code>
	 * then for a local variable named <code>preLocalsuf</code> the result of this method is <code>local</code>.
	 * If there is no prefix or suffix defined in JavaCore options the result is the unchanged
	 * name <code>preLocalsuf</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_LOCAL_PREFIXES and 
	 * CODEASSIST_LOCAL_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return char[] the name without prefix and suffix.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, String localName) {
		return String.valueOf(removePrefixAndSuffixForLocalVariableName(javaProject, localName.toCharArray()));
	}

	/**
	 * Suggest names for an argument. The name is computed from argument's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the argument is <code>TypeName</code>, the prefix for argument is <code>pre</code>
	 * and the suffix for argument is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_ARGUMENT_PREFIXES and 
	 * CODEASSIST_ARGUMENT_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[][] suggestArgumentNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				assistOptions.argumentPrefixes,
				assistOptions.argumentSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}
	
	/**
	 * Suggest names for an argument. The name is computed from argument's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the argument is <code>TypeName</code>, the prefix for argument is <code>pre</code>
	 * and the suffix for argument is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_ARGUMENT_PREFIXES and 
	 * CODEASSIST_ARGUMENT_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String[] suggestArgumentNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestArgumentNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	/**
	 * Suggest names for a field. The name is computed from field's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the field is <code>TypeName</code>, the prefix for field is <code>pre</code>
	 * and the suffix for field is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES and for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[][] suggestFieldNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, int modifiers, char[][] excludedNames) {
		boolean isStatic = Flags.isStatic(modifiers);
		
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
				isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}

	/**
	 * Suggest names for a field. The name is computed from field's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the field is <code>TypeName</code>, the prefix for field is <code>pre</code>
	 * and the suffix for field is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES and for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String[] suggestFieldNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, int modifiers, String[] excludedNames) {
		return convertCharsToString(
			suggestFieldNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				modifiers,
				convertStringToChars(excludedNames)));
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the local variable is <code>TypeName</code>, the prefix for local variable is <code>pre</code>
	 * and the suffix for local variable is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_LOCAL_PREFIXES and
	 * CODEASSIST_LOCAL_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[][] suggestLocalVariableNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				assistOptions.localPrefixes,
				assistOptions.localSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type
	 * and possible prefixes or suffixes are added.<br>
	 * If the type of the local variable is <code>TypeName</code>, the prefix for local variable is <code>pre</code>
	 * and the suffix for local variable is <code>suf</code> then the proposed names are <code>preTypeNamesuf</code>
	 * and <code>preNamesuf</code>. If there is no prefix or suffix the proposals are <code>typeName</code>
	 * and <code>name</code>.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_LOCAL_PREFIXES and
	 * CODEASSIST_LOCAL_SUFFIXES.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String[] suggestLocalVariableNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestLocalVariableNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	
	private static char[][] suggestNames(
		char[] packageName,
		char[] qualifiedTypeName,
		int dim,
		char[][] prefixes,
		char[][] suffixes,
		char[][] excludedNames,
		Scanner nameScanner){
			
		if(qualifiedTypeName == null || qualifiedTypeName.length == 0)
			return CharOperation.NO_CHAR_CHAR;
			
		char[] typeName = CharOperation.lastSegment(qualifiedTypeName, '.');
		
		if(prefixes == null || prefixes.length == 0) {
			prefixes = new char[1][0];
		} else {
			int length = prefixes.length;
			System.arraycopy(prefixes, 0, prefixes = new char[length+1][], 1, length);
			prefixes[0] = CharOperation.NO_CHAR;
		}
		
		if(suffixes == null || suffixes.length == 0) {
			suffixes = new char[1][0];
		} else {
			int length = suffixes.length;
			System.arraycopy(suffixes, 0, suffixes = new char[length+1][], 1, length);
			suffixes[0] = CharOperation.NO_CHAR;
		}
		
		char[][] names = new char[5][];
		int namesCount = 0;
		
		char[][] tempNames = null;
		
		// compute variable name for base type
		try{
			nameScanner.setSource(typeName);
			switch (nameScanner.getNextToken()) {
				case TerminalTokens.TokenNameint :
				case TerminalTokens.TokenNamebyte :
				case TerminalTokens.TokenNameshort :
				case TerminalTokens.TokenNamechar :
				case TerminalTokens.TokenNamelong :
				case TerminalTokens.TokenNamefloat :
				case TerminalTokens.TokenNamedouble :
				case TerminalTokens.TokenNameboolean :	
					char[] name = computeBaseNames(typeName[0], prefixes, excludedNames);
					if(name != null) {
						tempNames =  new char[][]{name};
					}
					break;
			}	
		} catch(InvalidInputException e){
		}

		// compute variable name for non base type
		if(tempNames == null) {
			tempNames = computeNames(typeName);
		}
		
		for (int i = 0; i < tempNames.length; i++) {
			char[] tempName = tempNames[i];
			if(dim > 0) {
				int length = tempName.length;
				if (tempName[length-1] == 's'){
					System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
					tempName[length] = 'e';
					tempName[length+1] = 's';
				} else if(tempName[length-1] == 'y') {
					System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
					tempName[length-1] = 'i';
					tempName[length] = 'e';
					tempName[length+1] = 's';
				} else {
					System.arraycopy(tempName, 0, tempName = new char[length + 1], 0, length);
					tempName[length] = 's';
				}
			}
			
			for (int j = 0; j < prefixes.length; j++) {
				if(prefixes[j].length > 0
					&& Character.isLetterOrDigit(prefixes[j][prefixes[j].length - 1])) {
					tempName[0] = Character.toUpperCase(tempName[0]);
				} else {
					tempName[0] = Character.toLowerCase(tempName[0]);
				}
				char[] prefixName = CharOperation.concat(prefixes[j], tempName);
				for (int k = 0; k < suffixes.length; k++) {
					char[] suffixName = CharOperation.concat(prefixName, suffixes[k]);
					suffixName =
						excludeNames(
							suffixName,
							prefixName,
							suffixes[k],
							excludedNames);
					if(JavaConventions.validateFieldName(new String(suffixName)).isOK()) {
						names[namesCount++] = suffixName;
					} else {
						suffixName = CharOperation.concat(
							prefixName,
							String.valueOf(1).toCharArray(),
							suffixes[k]
						);
						suffixName =
							excludeNames(
								suffixName,
								prefixName,
								suffixes[k],
								excludedNames);
						if(JavaConventions.validateFieldName(new String(suffixName)).isOK()) {
							names[namesCount++] = suffixName;
						}
					}
					if(namesCount == names.length) {
						System.arraycopy(names, 0, names = new char[namesCount * 2][], 0, namesCount);
					}
				}
				
			}
		}
		System.arraycopy(names, 0, names = new char[namesCount][], 0, namesCount);
		
		// if no names were found
		if(names.length == 0) {
			names = new char[][]{excludeNames(DEFAULT_NAME, DEFAULT_NAME, CharOperation.NO_CHAR, excludedNames)};
		}
		return names;
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.<br>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>isFieldName</code> for boolean field or
	 * <code>getFieldName</code> for others. If there is no prefix and suffix the proposal is <code>isPreFieldNamesuf</code>
	 * for boolean field or <code>getPreFieldNamesuf</code> for others.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[] suggestGetterName(IJavaProject project, char[] fieldName, int modifiers, boolean isBoolean, char[][] excludedNames) {
		if (isBoolean) {
			char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
			int prefixLen =  GETTER_BOOL_NAME.length;
			if (CharOperation.startsWith(name, GETTER_BOOL_NAME) 
				&& name.length > prefixLen && Character.isUpperCase(name[prefixLen])) {
				return suggestNewName(name, excludedNames);
			} else {
				return suggestNewName(
					CharOperation.concat(GETTER_BOOL_NAME, suggestAccessorName(project, fieldName, modifiers)),
					excludedNames
				);
			}
		} else {
			return suggestNewName(
				CharOperation.concat(GETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
				excludedNames
			);
		}
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.<br>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>isFieldName</code> for boolean field or
	 * <code>getFieldName</code> for others. If there is no prefix and suffix the proposal is <code>isPreFieldNamesuf</code>
	 * for boolean field or <code>getPreFieldNamesuf</code> for others.<br>
	 * 
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String suggestGetterName(IJavaProject project, String fieldName, int modifiers, boolean isBoolean, String[] excludedNames) {
		return String.valueOf(
			suggestGetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				isBoolean,
				convertStringToChars(excludedNames)));
	}

	/**
	 * Suggest name for a setter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.<br>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>setFieldName</code>.
	 * If there is no prefix and suffix the proposal is <code>setPreFieldNamesuf</code>.<br>
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static char[] suggestSetterName(IJavaProject project, char[] fieldName, int modifiers, boolean isBoolean, char[][] excludedNames) {

		if (isBoolean) {
			char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
			int prefixLen =  GETTER_BOOL_NAME.length;
			if (CharOperation.startsWith(name, GETTER_BOOL_NAME) 
				&& name.length > prefixLen && Character.isUpperCase(name[prefixLen])) {
				name = CharOperation.subarray(name, prefixLen, name.length);
				return suggestNewName(
					CharOperation.concat(SETTER_NAME, suggestAccessorName(project, name, modifiers)),
					excludedNames
				);
			} else {
				return suggestNewName(
					CharOperation.concat(SETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
					excludedNames
				);
			}
		} else {
			return suggestNewName(
				CharOperation.concat(SETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
				excludedNames
			);
		}
	}
	
	/**
	 * Suggest name for a setter method. The name is computed from field's name
	 * and possible prefixes or suffixes are removed.<br>
	 * If the field name is <code>preFieldNamesuf</code> and the prefix for field is <code>pre</code> and
	 * the suffix for field is <code>suf</code> then the prosposed name is <code>setFieldName</code>.
	 * If there is no prefix and suffix the proposal is <code>setPreFieldNamesuf</code>.<br>
	 * This method is affected by the following JavaCore options : CODEASSIST_FIELD_PREFIXES, 
	 * CODEASSIST_FIELD_SUFFIXES for instance field and CODEASSIST_STATIC_FIELD_PREFIXES,
	 * CODEASSIST_STATIC_FIELD_SUFFIXES for static field.<br>
	 * For a complete description of these configurable options, see <code>getDefaultOptions</code>.
	 * For programmaticaly change these options, see <code>JavaCore#setOptions()</code>.
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers as defined by the class
	 * <code>Flags</code>.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which cannot be suggested (already used names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 * @see Flags
	 * @see JavaCore#setOptions
	 * @see JavaCore#getDefaultOptions
	 */
	public static String suggestSetterName(IJavaProject project, String fieldName, int modifiers, boolean isBoolean, String[] excludedNames) {
		return String.valueOf(
			suggestSetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				isBoolean,
				convertStringToChars(excludedNames)));
	}
	
	private static char[] suggestAccessorName(IJavaProject project, char[] fieldName, int modifiers) {
		char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
		if (name.length > 0 && Character.isLowerCase(name[0])) {
			name[0] = Character.toUpperCase(name[0]);
		}
		return name;
	}
	
	private static char[] suggestNewName(char[] name, char[][] excludedNames){
		if(excludedNames == null) {
			return name;
		}
		
		char[] newName = name;
		int count = 2;
		int i = 0;
		while (i < excludedNames.length) {
			if(CharOperation.equals(newName, excludedNames[i], false)) {
				newName = CharOperation.concat(name, String.valueOf(count++).toCharArray());
				i = 0;
			} else {
				i++;
			}
		}
		return newName;
	}
	
	private static String[] convertCharsToString(char[][] c) {
		int length = c == null ? 0 : c.length;
		String[] s = new String[length];
		for (int i = 0; i < length; i++) {
			s[i] = String.valueOf(c[i]);
		}
		return s;
	}
	
	private static char[][] convertStringToChars(String[] s) {
		int length = s == null ? 0 : s.length;
		char[][] c = new char[length][];
		for (int i = 0; i < length; i++) {
			if(s[i] == null) {
				c[i] = CharOperation.NO_CHAR;
			} else {
				c[i] = s[i].toCharArray();
			}
		}
		return c;
	}
	
	private static char[] excludeNames(
		char[] suffixName,
		char[] prefixName,
		char[] suffix,
		char[][] excludedNames) {
		int count = 2;
		int m = 0;
		while (m < excludedNames.length) {
			if(CharOperation.equals(suffixName, excludedNames[m], false)) {
				suffixName = CharOperation.concat(
					prefixName,
					String.valueOf(count++).toCharArray(),
					suffix
				);
				m = 0;
			} else {
				m++;
			}
		}
		return suffixName;
	}
}
