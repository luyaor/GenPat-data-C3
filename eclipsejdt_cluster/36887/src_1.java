/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.apt.core.internal.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Stores annotation processor factories, and handles mapping from projects
 * to them.
 */
public class AnnotationProcessorFactoryLoader {
	
	/** List of jar file entries that specify autoloadable service providers */
    private static final String[] AUTOLOAD_SERVICES = {
        "META-INF/services/com.sun.mirror.apt.AnnotationProcessorFactory"
    };
	
    /** All plugin factories available from this install of eclipse */
	private static List<AnnotationProcessorFactory> PLUGIN_FACTORIES;
	
	/** map of plugin names -> factories */
	private static final HashMap<String, AnnotationProcessorFactory> PLUGIN_FACTORY_MAP = new HashMap<String, AnnotationProcessorFactory>();
	
	/** Loader instance -- holds all workspace and project data */
	private static AnnotationProcessorFactoryLoader LOADER;
	
	private static boolean VERBOSE_LOAD = false;
	
	// Members -- workspace and project data	
	
	private final Map<IJavaProject, List<AnnotationProcessorFactory>> _project2Factories = new HashMap<IJavaProject, List<AnnotationProcessorFactory>>();
	private final Set<IJavaProject> _projectsLoaded = new HashSet<IJavaProject>();

	/** 
	 * Singleton
	 */
    public static synchronized AnnotationProcessorFactoryLoader getLoader() {
    	if ( LOADER == null )
    		LOADER = new AnnotationProcessorFactoryLoader();
    	return LOADER;
    }
    
    private AnnotationProcessorFactoryLoader() {
    	loadPluginFactoryMap();
    	List<PluginFactoryContainer> pluginContainers = FactoryPathUtil.getAllPluginFactoryContainers();
    	setPluginAnnotationProcessorFactories( pluginContainers );
    }
    
    /**
     * Called when underlying preferences change
     */
    public synchronized void reset() {
    	_project2Factories.clear();
    }
    
    public synchronized List<AnnotationProcessorFactory> getFactoriesForProject( IJavaProject jproj ) {
    	
    	List<AnnotationProcessorFactory> factories = null;
    	
		if (_projectsLoaded.contains(jproj)) {
    		factories = _project2Factories.get(jproj);
    		if (factories != null) {
    			return factories;
    		}
		}
		// Load the project
		List<FactoryContainer> containers = AptConfig.getEnabledContainers(jproj);
		factories = loadFactories(containers);
		_projectsLoaded.add(jproj);
		_project2Factories.put(jproj, factories);
		return factories;
    	
    }
    
    
	private static void setPluginAnnotationProcessorFactories( List<PluginFactoryContainer> containers )
	{
		PLUGIN_FACTORIES = loadFactories( containers );
	}
    
	private static List<AnnotationProcessorFactory> loadFactories( List<? extends FactoryContainer> containers )
	{
		List<AnnotationProcessorFactory> factories = new ArrayList(containers.size());
		ClassLoader classLoader = _createClassLoader( containers );
		for ( FactoryContainer fc : containers )
		{
			List<AnnotationProcessorFactory> f = loadFactoryClasses( fc, classLoader );
			for ( AnnotationProcessorFactory apf : f )
				factories.add( apf  );
		}
		return factories;
	}
	
	private static List<AnnotationProcessorFactory> loadFactoryClasses( FactoryContainer fc, ClassLoader classLoader )
	{
		List<String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( factoryNames.size() ); 
		for ( String factoryName : factoryNames )
		{
			AnnotationProcessorFactory factory;
			if ( fc.getType() == FactoryType.PLUGIN )
				factory = loadFactoryFromPlugin( factoryName );
			else
				factory = loadFactoryFromClassLoader( factoryName, classLoader );
			
			if ( factory != null )
				factories.add( factory );
		}
		return factories;
	}
	
	private static AnnotationProcessorFactory loadFactoryFromPlugin( String factoryName )
	{
		AnnotationProcessorFactory apf = PLUGIN_FACTORY_MAP.get( factoryName );
		if ( apf == null ) 
		{
			// TODO:  log error somewhere
			System.err.println("could not find AnnotationProcessorFactory " + 
					factoryName + " from available factories defined by plugins" );
		}
		return apf;
	}

	private static AnnotationProcessorFactory loadFactoryFromClassLoader( String factoryName, ClassLoader cl )
	{
		AnnotationProcessorFactory f = null;
		try
		{
			Class c = cl.loadClass( factoryName );
			f = (AnnotationProcessorFactory)c.newInstance();
		}
		catch( Exception e )
		{
			// TODO:  log this stack trace
			e.printStackTrace();
		}
		catch ( NoClassDefFoundError ncdfe )
		{
			// **DO NOT REMOVE THIS CATCH BLOCK***
			// This error indicates a problem with the factory path specified 
			// by the project, and it needs to be caught and reported!
			
			// TODO:  log this error
			ncdfe.printStackTrace();
		}
		return f;
	}
	
	private static ClassLoader _createClassLoader( Collection<? extends FactoryContainer> containers )
	{
		ArrayList<URL> urlList = new ArrayList<URL>( containers.size() );
		for ( FactoryContainer fc : containers ) 
		{
			if ( fc.getType() == FactoryType.JAR  )
			{
				JarFactoryContainer jfc = (JarFactoryContainer) fc;
				try
				{
					URL u = jfc.getJarFileURL();
					urlList.add( u );
				}
				catch ( MalformedURLException mue )
				{
					// TODO:  log this exception
					mue.printStackTrace();
				}
			}
		}
		
		ClassLoader cl = null;
		if ( urlList.size() > 0 )
		{
			URL[] urls = urlList.toArray(new URL[urlList.size()]);
			cl = new URLClassLoader( urls, AnnotationProcessorFactoryLoader.class.getClassLoader() );
		}
		return cl;
	}
	
	/**
	 * Discover and instantiate annotation processor factories by searching for plugins
	 * which contribute to org.eclipse.jdt.apt.core.annotationProcessorFactory.
	 * This method is used when running within the Eclipse framework.  When running
	 * standalone at the command line, use {@link #LoadFactoriesFromJars}.
	 * This method can be called repeatedly, but each time it will erase the previous
	 * contents of the set of known AnnotationProcessorFactoriesDefined by plugin and 
	 * do a full rediscovery.
	 */
	private void loadPluginFactoryMap() {
		assert PLUGIN_FACTORY_MAP.size() == 0 : "loadPluginFactoryMap() called more than once";

		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - namecls of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id
		IExtension[] extensions =  extension.getExtensions();
		// for all extensions of this point...
		for(int i = 0; i < extensions.length; i++){
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			// for all config elements named "factory"
			for(int j = 0; j < configElements.length; j++){
				String elementName = configElements[j].getName();
				if (!("factory".equals(elementName))) { //$NON-NLS-1$ - name of configElement
					continue;
				}
				try {
					Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$ - attribute name
					if (execExt instanceof AnnotationProcessorFactory){
						PLUGIN_FACTORY_MAP.put( execExt.getClass().getName(), (AnnotationProcessorFactory)execExt );
					}
				} catch(CoreException e) {
						e.printStackTrace();
				}
			}
		}
	}
	
	private List<FactoryContainer> getPluginFactoryContainers()
	{
		List<FactoryContainer> factories = new ArrayList<FactoryContainer>();
	
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - name of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id

		IExtension[] extensions =  extension.getExtensions();
		for(int i = 0; i < extensions.length; i++) 
		{
			PluginFactoryContainer container = null;
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			for(int j = 0; j < configElements.length; j++)
			{
				String elementName = configElements[j].getName();
				if ( "factory".equals( elementName ) ) //$NON-NLS-1$ - name of configElement 
				{ 
					if ( container == null )
					{
						container = new PluginFactoryContainer(extensions[i].getNamespace());
						factories.add( container );
					}
					container.addFactoryName( configElements[j].getAttribute("class") );
				}
			}
		}
		return factories;
	}
  
    /**
     * Given a jar file, get the names of any AnnotationProcessorFactory
     * implementations it offers.  The information is based on the Sun
     * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">
     * Jar Service Provider spec</a>: the jar file contains a META-INF/services
     * directory; that directory contains text files named according to the desired
     * interfaces; and each file contains the names of the classes implementing
     * the specified service.  The files may also contain whitespace (which is to
     * be ignored).  The '#' character indicates the beginning of a line comment,
     * also to be ignored.  Implied but not stated in the spec is that this routine
     * also ignores anything after the first nonwhitespace token on a line.
     * @param jar the jar file.
     * @return a list, possibly empty, of fully qualified classnames to be instantiated.
     */
    private static List<String> _getServiceClassnamesFromJar(File jar)
    {
        List<String> classNames = new ArrayList<String>();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);

            for (String providerName : AUTOLOAD_SERVICES) {
                JarEntry provider = jarFile.getJarEntry(providerName);
                if (provider == null) {
                    continue;
                }
                // Extract classnames from this text file.
                InputStream is = jarFile.getInputStream(provider);
                BufferedReader rd;
                rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                for (String line = rd.readLine(); line != null; line = rd.readLine()) {
                    // hack off any comments
                    int iComment = line.indexOf('#');
                    if (iComment >= 0) {
                        line = line.substring(0, iComment);
                    }
                    // add the first non-whitespace token to the list
                    final String[] tokens = line.split("\\s", 2);
                    if (tokens[0].length() > 0) {
                        if (VERBOSE_LOAD) {
                            System.err.println("Found provider classname: " + tokens[0]);
                        }
                        classNames.add(tokens[0]);
                    }
                }
                rd.close();
            }
        }
        catch (IOException e) {
            if (VERBOSE_LOAD) {
                System.err.println("\tUnable to extract provider names from \"" + jar + "\"; skipping because of: " + e);
            }
            return classNames;
        }
        finally {
        	if (jarFile != null) {try {jarFile.close();} catch (IOException ioe) {}}
        }
        return classNames;
    }    
}
