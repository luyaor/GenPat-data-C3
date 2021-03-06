/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.myrmidon.components.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.aut.converter.Converter;
import org.apache.aut.converter.ConverterException;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.myrmidon.interfaces.converter.ConverterRegistry;
import org.apache.myrmidon.interfaces.type.TypeFactory;
import org.apache.myrmidon.interfaces.type.TypeManager;

/**
 * Converter engine to handle converting between types.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision$ $Date$
 */
public class DefaultMasterConverter
    implements Converter, Serviceable
{
    private final static Resources REZ =
        ResourceManager.getPackageResources( DefaultMasterConverter.class );

    private ConverterRegistry m_registry;
    private TypeManager m_typeManager;

    /** Map from converter name to Converter. */
    private Map m_converters = new HashMap();

    /**
     * Retrieve relevent services needed to deploy.
     *
     * @param serviceManager the ServiceManager
     * @exception ServiceException if an error occurs
     */
    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_registry = (ConverterRegistry)serviceManager.lookup( ConverterRegistry.ROLE );
        m_typeManager = (TypeManager)serviceManager.lookup( TypeManager.ROLE );
    }

    /**
     * Convert object to destination type.
     *
     * @param destination the destination type
     * @param original the original object
     * @param context the context in which to convert
     * @return the converted object
     * @exception ConverterException if an error occurs
     */
    public Object convert( final Class destination,
                           final Object original,
                           final Object context )
        throws ConverterException
    {
        final Class originalClass = original.getClass();

        if( destination.isAssignableFrom( originalClass ) )
        {
            return original;
        }

        try
        {
            // Search inheritance hierarchy for converter
            final String name = getConverterName( originalClass, destination );

            // Create the converter
            Converter converter = (Converter)m_converters.get( name );
            if( converter == null )
            {
                converter = createConverter( name );
                m_converters.put( name, converter );
            }

            // Convert
            final Object object = converter.convert( destination, original, context );
            if( destination.isInstance( object ) )
            {
                return object;
            }

            final String message =
                REZ.getString( "bad-return-type.error",
                               object.getClass().getName(),
                               destination.getName() );
            throw new ConverterException( message );
        }
        catch( final Exception e )
        {
            final String message = REZ.getString( "convert.error",
                                                  originalClass.getName(),
                                                  destination.getName() );
            throw new ConverterException( message, e );
        }
    }

    /**
     * Create an instance of converter with specified name.
     *
     * @param name the name of converter
     * @return the created converter instance
     * @throws Exception if converter can not be created.
     */
    private Converter createConverter( final String name )
        throws Exception
    {
        final TypeFactory factory = m_typeManager.getFactory( Converter.ROLE );
        return (Converter)factory.create( name );
    }

    /**
     * Determine the name of the converter to use to convert between
     * original and destination classes.
     */
    private String getConverterName( final Class originalClass,
                                     final Class destination )
        throws ConverterException
    {
        //TODO: Maybe we should search the destination classes hierarchy as well

        // Recursively iterate over the super-types of the original class,
        // looking for a converter from source type -> destination type.
        // If more than one is found, choose the most specialised.

        Class match = null;
        String converterName = null;
        ArrayList queue = new ArrayList();
        queue.add( originalClass );

        while( !queue.isEmpty() )
        {
            Class clazz = (Class)queue.remove( 0 );

            // Add superclass and all interfaces
            if( clazz.getSuperclass() != null )
            {
                queue.add( clazz.getSuperclass() );
            }
            final Class[] interfaces = clazz.getInterfaces();
            for( int i = 0; i < interfaces.length; i++ )
            {
                queue.add( interfaces[ i ] );
            }

            // Check if we can convert from current class to destination
            final String name = m_registry.getConverterName( clazz.getName(),
                                                             destination.getName() );
            if( name == null )
            {
                continue;
            }

            // Choose the more specialised source class
            if( match == null || match.isAssignableFrom( clazz ) )
            {
                match = clazz;
                converterName = name;
            }
            else if( clazz.isAssignableFrom( clazz ) )
            {
                continue;
            }
            else
            {
                // Duplicate
                final String message = REZ.getString( "ambiguous-converter.error" );
                throw new ConverterException( message );
            }
        }

        // TODO - should cache the (src, dest) -> converter mapping
        if( match != null )
        {
            return converterName;
        }

        // Could not find a converter
        final String message = REZ.getString( "no-converter.error" );
        throw new ConverterException( message );
    }
}
