/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.ant.launcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Basic Loader that is responsible for all the hackery to get classloader to work.
 * Other classes can call AntLoader.getLoader() and add to their own classloader.
 *
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 */
public final class AntLoader
    extends URLClassLoader
{
    protected static AntLoader     c_classLoader;

    public static AntLoader getLoader()
    {
        if( null == c_classLoader ) 
        {
            c_classLoader = new AntLoader( new URL[ 0 ] );
        }

        return c_classLoader;
    }

    /**
     * Magic entry point.
     *
     * @param argsthe CLI arguments
     * @exception Exception if an error occurs
     */
    public final static void main( final String[] args ) 
        throws Exception
    { 
        final URL archive = new URL( "file:lib/myrmidon.jar" );
        c_classLoader = new AntLoader( new URL[] { archive } );
        
        try
        {
            //load class and retrieve appropriate main method.
            final Class clazz = c_classLoader.loadClass( "org.apache.ant.Main" );
            final Method method = clazz.getMethod( "main", new Class[] { args.getClass() } );
            
            //kick the tires and light the fires....
            method.invoke( null, new Object[] { args } );
        }
        catch( final Throwable throwable ) 
        {
            throwable.printStackTrace();
        }
    }

    /**
     * Basic constructor.
     *
     * @param urls the Starting URLS
     */
    public AntLoader( final URL[] urls )
    {
        super( urls );
    }

    /**
     * Add a URL to classloader
     *
     * @param url the url
     */
    public void addURL( final URL url )
    {
        super.addURL( url );
    }
}
