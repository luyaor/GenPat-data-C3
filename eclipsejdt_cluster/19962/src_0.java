/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal.util;

import java.io.IOException;
import java.util.List;

/**
 * An entity that contains annotation processor factories.
 */
public abstract class FactoryContainer
{
	public enum FactoryType {
		PLUGIN,  // Eclipse plugin 
		EXTJAR,  // external jar file (not in workspace)
		WKSPJAR, // jar file within workspace
		VARJAR;  // external jar file referenced by classpath variable
	}
	
	/**	
	 * Returns an ID that is guaranteed to be sufficiently unique for this container --
	 * that is, all necessary state can be reconstructed from just the id and FactoryType.
	 * For plugins, it's the plugin id, for jar files, the path to the jar, etc.
	 */
	public abstract String getId();
	
	/**
	 * This method is used to display the container in the UI.
	 * If this default implementation is not adequate for a particular
	 * container, that container should provide an override.
	 */
	@Override
	public String toString() {
		return getId();
	}
	
	public abstract FactoryType getType();
	
	/**
	 * Test whether the resource that backs this container exists,
	 * can be located, and is (at least in principle) accessible for 
	 * factories to be loaded from.  For instance, a plugin exists if 
	 * the plugin is loaded in Eclipse; a jar exists if the jar file 
	 * can be found on disk.  The test is not required to be perfect:
	 * for instance, a jar file might exist but be corrupted and
	 * therefore not really readable, but this method would still return
	 * true.
	 * @return true if the resource backing the container exists.
	 */
	public abstract boolean exists();
	
	protected abstract List<String> loadFactoryNames() throws IOException;
	
	protected List<String> _factoryNames;
	
	public List<String> getFactoryNames() throws IOException
	{ 
		if ( _factoryNames == null )
			_factoryNames = loadFactoryNames();
		return _factoryNames;
	}
	
	@Override
	public int hashCode() {
		return getType().hashCode() ^ getId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FactoryContainer)) {
			return false;
		}

		FactoryContainer other = (FactoryContainer) o;
		return other.getType() == getType() && other.getId().equals(getId());
	}
}

