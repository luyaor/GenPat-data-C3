/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class FileSystem implements INameEnvironment, SuffixConstants {
	Classpath[] classpaths;
	Set knownFileNames;

	interface Classpath {
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName);
		boolean isPackage(String qualifiedPackageName); 
		/**
		 * This method resets the environment. The resulting state is equivalent to
		 * a new name environment without creating a new object.
		 */
		void reset();
		/**
		 * Return a normalized path for file based classpath entries. This is an absolute path
		 * ending with a file separator for directories, an absolute path deprived from the '.jar'
		 * (resp. '.zip') extension for jar (resp. zip) files.
		 * @return a normalized path for file based classpath entries
		 */
		char[] normalizedPath();
		/**
		 * Return the path for file based classpath entries. This is an absolute path
		 * ending with a file separator for directories, an absolute path including the '.jar'
		 * (resp. '.zip') extension for jar (resp. zip) files.
		 * @return the path for file based classpath entries
		 */
		String getPath();
		/**
		 * Initialize the entry
		 */
		void initialize() throws IOException;
	}
/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
	final int classpathSize = classpathNames.length;
	this.classpaths = new Classpath[classpathSize];
	int counter = 0;
	for (int i = 0; i < classpathSize; i++) {
		Classpath classpath = getClasspath(classpathNames[i], encoding, null);
		try {
			classpath.initialize();
			this.classpaths[counter++] = classpath;
		} catch (IOException e) {
			// ignore
		}
	}
	if (counter != classpathSize) {
		System.arraycopy(this.classpaths, 0, (this.classpaths = new Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
}
FileSystem(Classpath[] paths, String[] initialFileNames) {
	final int length = paths.length;
	int counter = 0;
	this.classpaths = new FileSystem.Classpath[length];
	for (int i = 0; i < length; i++) {
		final Classpath classpath = paths[i];
		try {
			classpath.initialize();
			this.classpaths[counter++] = classpath;
		} catch(IOException exception) {
			// ignore
		}
	}
	if (counter != length) {
		// should not happen
		System.arraycopy(this.classpaths, 0, (this.classpaths = new FileSystem.Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
}
static Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet) {
	return getClasspath(classpathName, encoding, ClasspathLocation.SOURCE | ClasspathLocation.BINARY, accessRuleSet);
}
static Classpath getClasspath(String classpathName, String encoding, int mode, AccessRuleSet accessRuleSet) {
	Classpath result = null;
	File file = new File(convertPathSeparators(classpathName));
	if (file.isDirectory()) {
		if (file.exists()) {
			result = new ClasspathDirectory(file, encoding, mode, accessRuleSet);
		}
	} else {
		String lowercaseClasspathName = classpathName.toLowerCase();
		if (lowercaseClasspathName.endsWith(SUFFIX_STRING_jar)
				|| lowercaseClasspathName.endsWith(SUFFIX_STRING_zip)) {
			if (mode == ClasspathLocation.SOURCE) {
				// will throw an IOException if file does not exist
				result = new ClasspathSourceJar(file, true, accessRuleSet, encoding);			
			} else {
				// will throw an IOException if file does not exist
				result = new ClasspathJar(file, true, accessRuleSet);
			}
		}
	}
	return result;
}
private void initializeKnownFileNames(String[] initialFileNames) {
	this.knownFileNames = new HashSet(initialFileNames.length * 2);
	for (int i = initialFileNames.length; --i >= 0;) {
		char[] fileName = initialFileNames[i].toCharArray();
		char[] matchingPathName = null;
		final int lastIndexOf = CharOperation.lastIndexOf('.', fileName);
		if (lastIndexOf != -1) {
			fileName = CharOperation.subarray(fileName, 0, lastIndexOf);
		}
		CharOperation.replace(fileName, '\\', '/');
		for (int j = 0; j < classpaths.length; j++){
			char[] matchCandidate = this.classpaths[j].normalizedPath();
			if (this.classpaths[j] instanceof  ClasspathDirectory && 
					CharOperation.prefixEquals(matchCandidate, fileName) && 
					(matchingPathName == null ||
							matchCandidate.length < matchingPathName.length))
				matchingPathName = matchCandidate;
		}
		if (matchingPathName == null) {
			this.knownFileNames.add(new String(fileName)); // leave as is...
		} else {
			this.knownFileNames.add(new String(CharOperation.subarray(fileName, matchingPathName.length, fileName.length)));
		}
		matchingPathName = null;
	}
}
public void cleanup() {
	for (int i = 0, max = this.classpaths.length; i < max; i++)
		this.classpaths[i].reset();
}
private static String convertPathSeparators(String path) {
	return File.separatorChar == '/'
		? path.replace('\\', '/')
		 : path.replace('/', '\\');
}
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName){
	if (this.knownFileNames.contains(qualifiedTypeName)) return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? "" //$NON-NLS-1$
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	NameEnvironmentAnswer suggestedAnswer = null;
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			NameEnvironmentAnswer answer = this.classpaths[i].findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			NameEnvironmentAnswer answer = (p instanceof ClasspathJar)
				? p.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName)
				: p.findClass(typeName, qp2, qb2);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}
public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}
public boolean isPackage(char[][] compoundName, char[] packageName) {
	String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++)
			if (this.classpaths[i].isPackage(qualifiedPackageName))
				return true;
	} else {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			if ((p instanceof ClasspathJar) ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2))
				return true;
		}
	}
	return false;
}
}
