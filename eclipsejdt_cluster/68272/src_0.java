/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class FileSystem implements INameEnvironment, SuffixConstants {
	Classpath[] classpaths;
	String[] knownFileNames;

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
		String normalizedPath();
	}
/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/

public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
	this(classpathNames, initialFileNames, encoding, null);
}
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding, int[] classpathDirectoryModes) {
	int classpathSize = classpathNames.length;
	this.classpaths = new Classpath[classpathSize];
	int problemsOccured = 0;
	for (int i = 0; i < classpathSize; i++) {
		this.classpaths[i] = getClasspath(classpathNames[i], encoding,
					classpathDirectoryModes == null ? 0
							: classpathDirectoryModes[i], null);
		if (this.classpaths[i] == null)
			problemsOccured++;
	}
	if (problemsOccured > 0) {
		Classpath[] newPaths = new Classpath[classpathSize - problemsOccured];
		for (int i = 0, current = 0; i < classpathSize; i++)
			if (this.classpaths[i] != null) {
				newPaths[current] = this.classpaths[i];
			}
		classpathSize = newPaths.length;
		this.classpaths = newPaths;
	}
	initializeKnownFileNames(initialFileNames);
}
FileSystem(Classpath[] classpaths, String[] initialFileNames) {
	this.classpaths = classpaths;
	initializeKnownFileNames(initialFileNames);
}
static Classpath getClasspath(String classpathName, String encoding,
		int classpathDirectoryMode, AccessRuleSet accessRuleSet) {
	Classpath result = null;
	try {
			File file = new File(convertPathSeparators(classpathName));
			if (file.isDirectory()) {
				if (file.exists()) {
					result = new ClasspathDirectory(file, encoding,
							classpathDirectoryMode, accessRuleSet);
				}
			} else {
				String lowercaseClasspathName = classpathName.toLowerCase();
				if (lowercaseClasspathName.endsWith(SUFFIX_STRING_jar)
						|| lowercaseClasspathName.endsWith(SUFFIX_STRING_zip)) {
					result = new ClasspathJar(new ZipFile(file), true,
							accessRuleSet);
					// will throw an IOException if file does not exist
				}
			}
		} catch (IOException e) {
			// result = null; -- this is already the case
		}
	return result;
}
private void initializeKnownFileNames(String[] initialFileNames) {
	this.knownFileNames = new String[initialFileNames.length];
	for (int i = initialFileNames.length; --i >= 0;) {
		String fileName = initialFileNames[i];
		String matchingPathName = null;
		if (fileName.lastIndexOf(".") != -1) //$NON-NLS-1$
			fileName = fileName.substring(0, fileName.lastIndexOf('.')); // remove trailing ".java"

		fileName = convertPathSeparators(fileName);
		for (int j = 0; j < classpaths.length; j++){
			String matchCandidate = this.classpaths[j].normalizedPath();
			if (this.classpaths[j] instanceof  ClasspathDirectory && 
					fileName.startsWith(matchCandidate) && 
					(matchingPathName == null || 
							matchCandidate.length() < matchingPathName.length()))
				matchingPathName = matchCandidate;
		}
		if (matchingPathName == null)
			this.knownFileNames[i] = fileName; // leave as is...
		else
			this.knownFileNames[i] = fileName.substring(matchingPathName.length());
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
	for (int i = 0, length = this.knownFileNames.length; i < length; i++)
		if (qualifiedTypeName.equals(this.knownFileNames[i]))
			return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? "" //$NON-NLS-1$
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			NameEnvironmentAnswer answer = this.classpaths[i].findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName);
			if (answer != null) return answer;
		}
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			NameEnvironmentAnswer answer = (p instanceof ClasspathJar)
				? p.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName)
				: p.findClass(typeName, qp2, qb2);
			if (answer != null) return answer;
		}
	}
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
public ClasspathJar getClasspathJar(File file) throws IOException {
	return new ClasspathJar(new ZipFile(file), true, null);
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
