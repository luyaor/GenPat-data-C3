/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.myrmidon.components.configurer;

import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.myrmidon.framework.DataType;
import org.apache.myrmidon.interfaces.configurer.Configurer;
import org.apache.myrmidon.interfaces.converter.MasterConverter;
import org.apache.myrmidon.interfaces.role.RoleManager;
import org.apache.myrmidon.interfaces.type.TypeException;
import org.apache.myrmidon.interfaces.type.TypeFactory;
import org.apache.myrmidon.interfaces.type.TypeManager;

/**
 * Class used to configure tasks.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision$ $Date$
 */
public class DefaultConfigurer
    extends AbstractLogEnabled
    implements Configurer, Composable, LogEnabled
{
    private final static Resources REZ =
        ResourceManager.getPackageResources( DefaultConfigurer.class );

    ///Converter to use for converting between values
    private MasterConverter m_converter;

    //TypeManager to use to create types in typed adders
    private TypeManager m_typeManager;

    //RoleManager to use to map from type names -> role shorthand
    private RoleManager m_roleManager;

    ///Cached object configurers.  This is a map from Class to the
    ///ObjectConfigurer for that class.
    private Map m_configurerCache = new HashMap();

    public void compose( final ComponentManager componentManager )
        throws ComponentException
    {
        m_converter = (MasterConverter)componentManager.lookup( MasterConverter.ROLE );
        m_typeManager = (TypeManager)componentManager.lookup( TypeManager.ROLE );
        m_roleManager = (RoleManager)componentManager.lookup( RoleManager.ROLE );
    }

    /**
     * Configure a task based on a configuration in a particular context.
     * This configuring can be done in different ways for different
     * configurers.
     * This one does it by first checking if object implements Configurable
     * and if it does will pass the task the configuration - else it will use
     * mapping rules to map configuration to types
     *
     * @param object the object
     * @param configuration the configuration
     * @param context the Context
     * @exception ConfigurationException if an error occurs
     */
    public void configure( final Object object,
                           final Configuration configuration,
                           final Context context )
        throws ConfigurationException
    {
        configureObject( object, configuration, context );
    }

    /**
     * Does the work of configuring an object.
     */
    private void configureObject( final Object object,
                                  final Configuration configuration,
                                  final Context context )
        throws ConfigurationException
    {
        if( object instanceof Configurable )
        {
            // Let the object configure itself
            ( (Configurable)object ).configure( configuration );
        }
        else
        {
            final String elemName = configuration.getName();

            // Locate the configurer for this object
            final ObjectConfigurer configurer = getConfigurer( object.getClass() );

            // Start configuring this object
            final ConfigurationState state = configurer.startConfiguration( object );

            // Set each of the attributes
            final String[] attributes = configuration.getAttributeNames();
            for( int i = 0; i < attributes.length; i++ )
            {
                final String name = attributes[ i ];
                try
                {
                    // Set the attribute
                    final String value = configuration.getAttribute( name );
                    setAttribute( state, name, value, context );
                }
                catch( final NoSuchPropertyException nspe )
                {
                    final String message =
                        REZ.getString( "no-such-attribute.error", elemName, name );
                    throw new ConfigurationException( message, nspe );
                }
                catch( final ConfigurationException ce )
                {
                    throw ce;
                }
                catch( final Exception ce )
                {
                    final String message =
                        REZ.getString( "bad-set-attribute.error", elemName, name );
                    throw new ConfigurationException( message, ce );
                }
            }

            // Set the text content
            final String content = configuration.getValue( null );
            if( null != content && content.length() > 0 )
            {
                try
                {
                    // Set the content
                    final PropertyConfigurer contentConfigurer = state.getConfigurer().getContentConfigurer();
                    setValue( contentConfigurer, state, content, context );
                }
                catch( final NoSuchPropertyException nspe )
                {
                    final String message =
                        REZ.getString( "no-content.error", elemName );
                    throw new ConfigurationException( message, nspe );
                }
                catch( final ConfigurationException ce )
                {
                    throw ce;
                }
                catch( final Exception ce )
                {
                    final String message =
                        REZ.getString( "bad-set-content.error", elemName );
                    throw new ConfigurationException( message, ce );
                }
            }

            // Create and configure each of the child elements
            final Configuration[] children = configuration.getChildren();
            for( int i = 0; i < children.length; i++ )
            {
                final Configuration childConfig = children[ i ];
                final String name = childConfig.getName();
                try
                {
                    configureElement( state, childConfig, context );
                }
                catch( final NoSuchPropertyException nspe )
                {
                    final String message =
                        REZ.getString( "no-such-element.error", elemName, name );
                    throw new ConfigurationException( message, nspe );
                }
                catch( final ConfigurationException ce )
                {
                    throw ce;
                }
                catch( final Exception ce )
                {
                    final String message =
                        REZ.getString( "bad-set-element.error", elemName, name );
                    throw new ConfigurationException( message, ce );
                }
            }

            // Finish configuring the object
            configurer.finishConfiguration( state );
        }
    }

    /**
     * Configure named attribute of object in a particular context.
     * This configuring can be done in different ways for different
     * configurers.
     *
     * @param object the object
     * @param name the attribute name
     * @param value the attribute value
     * @param context the Context
     * @exception ConfigurationException if an error occurs
     */
    public void configure( final Object object,
                           final String name,
                           final String value,
                           final Context context )
        throws ConfigurationException
    {
        // Locate the configurer for this object
        final ObjectConfigurer configurer = getConfigurer( object.getClass() );

        // TODO - this ain't right, the validation is going to be screwed up
        final ConfigurationState state = configurer.startConfiguration( object );

        // Set the attribute value
        try
        {
            setAttribute( state, name, value, context );
        }
        catch( final Exception ce )
        {
            final String message =
                REZ.getString( "bad-set-class-attribute.error",
                               name,
                               object.getClass().getName() );
            throw new ConfigurationException( message, ce );
        }

        // Finish up
        configurer.finishConfiguration( state );
    }

    /**
     * Configures a property from a nested element.
     */
    private void configureElement( final ConfigurationState state,
                                   final Configuration element,
                                   final Context context )
        throws Exception
    {
        final String elementName = element.getName();
        if( elementName.toLowerCase().endsWith( "-ref" ) )
        {
            // A reference
            configureReference( state, element, context );
        }
        else
        {
            // An inline object
            configureInline( state, element, context );
        }
    }

    /**
     * Configure a property from an inline object.
     */
    private void configureInline( final ConfigurationState state,
                                  final Configuration element,
                                  final Context context )
        throws Exception
    {
        final String name = element.getName();

        // Locate the configurer for the child element
        final PropertyConfigurer childConfigurer
            = getConfigurerFromName( state.getConfigurer(), name, true );

        // Create & configure the child element
        final Object child =
            setupChild( state, element, context, childConfigurer );

        // Set the child element
        childConfigurer.addValue( state, child );
    }

    /**
     * Configures a property from a reference.
     */
    private void configureReference( final ConfigurationState state,
                                     final Configuration element,
                                     final Context context )
        throws Exception
    {

        // Extract the id
        final String id = element.getAttribute( "id" );
        if( 1 != element.getAttributeNames().length ||
            0 != element.getChildren().length )
        {
            final String message = REZ.getString( "extra-config-for-ref.error" );
            throw new ConfigurationException( message );
        }

        // Set the property
        final String name = element.getName();
        setReference( state, name, id, context );
    }

    /**
     * Sets a property using a reference.
     */
    private void setReference( final ConfigurationState state,
                               final String refName,
                               final String unresolvedId,
                               final Context context )
        throws Exception
    {
        // Adjust the name
        final String name = refName.substring( 0, refName.length() - 4 );

        // Locate the configurer for the property
        final PropertyConfigurer childConfigurer
            = getConfigurerFromName( state.getConfigurer(), name, false );

        // Resolve any props in the id
        Object id = resolveProperty( unresolvedId, context );

        // Locate the referenced object
        Object ref = null;
        try
        {
            ref = context.get( id );
        }
        catch( final ContextException exc )
        {
            final String message = REZ.getString( "get-ref.error", id );
            throw new ConfigurationException( message, exc );
        }

        // Check the types
        final Class type = childConfigurer.getType();
        if( !type.isInstance( ref ) )
        {
            final String message = REZ.getString( "mismatch-ref-types.error", id, type.getName(), ref.getClass().getName() );
            throw new ConfigurationException( message );
        }

        // Set the child element
        childConfigurer.addValue( state, ref );
    }

    private Object resolveProperty( final String unresolvedId,
                                    final Context context )
        throws Exception
    {
        return PropertyUtil.resolveProperty( unresolvedId, context, false );
    }

    /**
     * Sets an attribute value.
     */
    private void setAttribute( final ConfigurationState state,
                               final String name,
                               final String value,
                               final Context context )
        throws Exception
    {
        if( name.toLowerCase().endsWith( "-ref" ) )
        {
            // A reference
            setReference( state, name, value, context );
        }
        else
        {
            // Set the value
            PropertyConfigurer propConfigurer
                = getConfigurerFromName( state.getConfigurer(), name, false );
            setValue( propConfigurer, state, value, context );
        }
    }

    /**
     * Sets an attribute value, or an element's text content.
     */
    private void setValue( final PropertyConfigurer setter,
                           final ConfigurationState state,
                           final String value,
                           final Context context )
        throws Exception
    {
        // Resolve property references in the attribute value
        Object objValue = PropertyUtil.resolveProperty( value, context, false );

        // Convert the value to the appropriate type
        final Class clazz = setter.getType();
        objValue = m_converter.convert( clazz, objValue, context );

        // Set the value
        setter.addValue( state, objValue );
    }

    /**
     * Locates the configurer for a particular class.
     */
    private ObjectConfigurer getConfigurer( final Class clazz )
        throws ConfigurationException
    {
        ObjectConfigurer configurer =
            (ObjectConfigurer)m_configurerCache.get( clazz );
        if( null == configurer )
        {
            configurer = DefaultObjectConfigurer.getConfigurer( clazz );
            m_configurerCache.put( clazz, configurer );
        }
        return configurer;
    }

    /**
     * Creates and configures an inline object.
     */
    private Object setupChild( final ConfigurationState state,
                               final Configuration element,
                               final Context context,
                               final PropertyConfigurer childConfigurer )
        throws ConfigurationException
    {
        final String name = element.getName();
        final Class type = childConfigurer.getType();
        Object child = childConfigurer.createValue( state );

        if( null == child && Configuration.class == type )
        {
            //special case where you have add...(Configuration)
            return element;
        }
        else if( null == child )
        {
            // Create an instance
            if( type.isInterface() )
            {
                child = createdTypedObject( name, type );
            }
            else
            {
                child = createObject( type );
            }
        }

        configureObject( child, element, context );
        return child;
    }

    /**
     * Determines the property configurer to use for a particular element
     * or attribute.  If the supplied name matches a property of the
     * class being configured, that property configurer is returned.  If
     * the supplied name matches the role shorthand for the class' typed
     * property, then the typed property configurer is used.
     *
     * @param configurer The configurer for the class being configured.
     * @param name The attribute/element name.
     */
    private PropertyConfigurer getConfigurerFromName( final ObjectConfigurer configurer,
                                                      final String name,
                                                      boolean ignoreRoleName )
        throws NoSuchPropertyException
    {
        // Try a named property
        final NoSuchPropertyException exc;
        try
        {
            return configurer.getProperty( name );
        }
        catch( NoSuchPropertyException e )
        {
            // Keep for later
            exc = e;
        }

        // Try a typed property
        final PropertyConfigurer propertyConfigurer = configurer.getTypedProperty();
        if( !ignoreRoleName )
        {
            final String roleShorthand = m_roleManager.getNameForRole( propertyConfigurer.getType().getName() );
            if( !name.equalsIgnoreCase( roleShorthand ) )
            {
                // Rethrow the original exception
                throw exc;
            }
        }

        return propertyConfigurer;
    }

    /**
     * Utility method to create an instance of the
     * specified type that satisfies supplied interface.
     */
    private Object createdTypedObject( final String name,
                                       final Class type )
        throws ConfigurationException
    {
        // Attempt to create the object
        final Object obj;
        try
        {
            final TypeFactory factory = getTypeFactory( DataType.class );
            obj = factory.create( name );
        }
        catch( final Exception e )
        {
            final String message =
                REZ.getString( "create-typed-object.error",
                               name,
                               type.getName() );
            throw new ConfigurationException( message, e );
        }

        // Check the types
        if( !type.isInstance( obj ) )
        {
            final String message =
                REZ.getString( "mismatched-typed-object.error", name, type.getName() );
            throw new ConfigurationException( message );
        }

        return obj;
    }

    /**
     * Utility method to instantiate an instance of the specified class.
     */
    private Object createObject( final Class type )
        throws ConfigurationException
    {
        try
        {
            return type.newInstance();
        }
        catch( final Exception e )
        {
            final String message =
                REZ.getString( "create-object.error",
                               type.getName() );
            throw new ConfigurationException( message, e );
        }
    }

    /**
     * Locates a type factory.
     */
    protected final TypeFactory getTypeFactory( final Class role )
        throws ConfigurationException
    {
        try
        {
            return m_typeManager.getFactory( role );
        }
        catch( final TypeException te )
        {
            final String message = REZ.getString( "no-factory-for-role.error", role.getName() );
            throw new ConfigurationException( message, te );
        }
    }
}
