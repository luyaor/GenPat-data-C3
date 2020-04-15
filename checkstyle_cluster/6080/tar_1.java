////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2004  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.api;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.AbstractArrayConverter;
import org.apache.commons.beanutils.converters.BooleanArrayConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteArrayConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CharacterArrayConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.DoubleArrayConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatArrayConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerArrayConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongArrayConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortArrayConverter;
import org.apache.commons.beanutils.converters.ShortConverter;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.beans.PropertyDescriptor;


/**
 * A Java Bean that implements the component lifecycle interfaces by
 * calling the bean's setters for all configration attributes.
 * @author lkuehne
 */
public class AutomaticBean
    implements Configurable, Contextualizable
{
    static {
        initConverters();
    }

    /**
     * Setup the jakarta-commons-beanutils type converters so they throw
     * a ConversionException instead of using the default value.
     */
    private static void initConverters()
    {
        // TODO: is there a smarter way to tell beanutils not to use defaults?

        // If any runtime environment like ANT or an IDE would use beanutils
        // with different converters we would really be stuck here.
        // Having to configure a static utility class in this way is really
        // strange, it seems like a design problem in BeanUtils
        final boolean[] booleanArray = new boolean[0];
        final byte[] byteArray = new byte[0];
        final char[] charArray = new char[0];
        final double[] doubleArray = new double[0];
        final float[] floatArray = new float[0];
        final int[] intArray = new int[0];
        final long[] longArray = new long[0];
        final short[] shortArray = new short[0];

        ConvertUtils.register(new BooleanConverter(), Boolean.TYPE);
        ConvertUtils.register(new BooleanConverter(), Boolean.class);
        ConvertUtils.register(
            new BooleanArrayConverter(), booleanArray.getClass());
        ConvertUtils.register(new ByteConverter(), Byte.TYPE);
        ConvertUtils.register(new ByteConverter(), Byte.class);
        ConvertUtils.register(
            new ByteArrayConverter(byteArray), byteArray.getClass());
        ConvertUtils.register(new CharacterConverter(), Character.TYPE);
        ConvertUtils.register(new CharacterConverter(), Character.class);
        ConvertUtils.register(
            new CharacterArrayConverter(), charArray.getClass());
        ConvertUtils.register(new DoubleConverter(), Double.TYPE);
        ConvertUtils.register(new DoubleConverter(), Double.class);
        ConvertUtils.register(
            new DoubleArrayConverter(doubleArray), doubleArray.getClass());
        ConvertUtils.register(new FloatConverter(), Float.TYPE);
        ConvertUtils.register(new FloatConverter(), Float.class);
        ConvertUtils.register(new FloatArrayConverter(), floatArray.getClass());
        ConvertUtils.register(new IntegerConverter(), Integer.TYPE);
        ConvertUtils.register(new IntegerConverter(), Integer.class);
        ConvertUtils.register(new IntegerArrayConverter(), intArray.getClass());
        ConvertUtils.register(new LongConverter(), Long.TYPE);
        ConvertUtils.register(new LongConverter(), Long.class);
        ConvertUtils.register(new LongArrayConverter(), longArray.getClass());
        ConvertUtils.register(new ShortConverter(), Short.TYPE);
        ConvertUtils.register(new ShortConverter(), Short.class);
        ConvertUtils.register(new ShortArrayConverter(), shortArray.getClass());
        // TODO: investigate:
        // StringArrayConverter doesn't properly convert an array of tokens with
        // elements containing an underscore, "_".
        // Hacked a replacement class :(
        //        ConvertUtils.register(new StringArrayConverter(),
        //                        String[].class);
        ConvertUtils.register(new StrArrayConverter(), String[].class);
        ConvertUtils.register(new IntegerArrayConverter(), Integer[].class);

        // BigDecimal, BigInteger, Class, Date, String, Time, TimeStamp
        // do not use defaults in the default configuration of ConvertUtils
    }

    /** the configuration of this bean */
    private Configuration mConfiguration;

    /**
     * Implements the Configurable interface using bean introspection.
     *
     * Subclasses are allowed to add behaviour. After the bean
     * based setup has completed first the method
     * {@link #finishLocalSetup finishLocalSetup}
     * is called to allow completion of the bean's local setup,
     * after that the method {@link #setupChild setupChild}
     * is called for each {@link Configuration#getChildren child Configuration}
     * of <code>aConfiguration</code>.
     *
     * @see Configurable
     */
    public final void configure(Configuration aConfiguration)
        throws CheckstyleException
    {
        mConfiguration = aConfiguration;

        // TODO: debug log messages
        final String[] attributes = aConfiguration.getAttributeNames();

        for (int i = 0; i < attributes.length; i++) {
            final String key = attributes[i];
            final String value = aConfiguration.getAttribute(key);

            try {
                // BeanUtils.copyProperties silently ignores missing setters
                // for key, so we have to go through great lengths here to
                // figure out if the bean property really exists.
                PropertyDescriptor pd =
                        PropertyUtils.getPropertyDescriptor(this, key);
                if (pd == null || pd.getWriteMethod() == null) {
                    throw new CheckstyleException(
                        "Property '" + key + "' in module "
                        + aConfiguration.getName()
                        + " does not exist, please check the documentation");
                }

                // finally we can set the bean property
                BeanUtils.copyProperty(this, key, value);
            }
            catch (InvocationTargetException e) {
                throw new CheckstyleException(
                    "Cannot set property '" + key + "' in module "
                    + aConfiguration.getName() + " to '" + value
                    + "': " + e.getTargetException().getMessage(), e);
            }
            catch (IllegalAccessException e) {
                throw new CheckstyleException(
                    "cannot access " + key + " in "
                    + this.getClass().getName(), e);
            }
            catch (NoSuchMethodException e) {
                throw new CheckstyleException(
                    "cannot access " + key + " in "
                    + this.getClass().getName(), e);
            }
            catch (IllegalArgumentException e) {
                throw new CheckstyleException(
                    "illegal value '" + value + "' for property '" + key
                    + "' of module " + aConfiguration.getName(), e);
            }
            catch (ConversionException e) {
                throw new CheckstyleException(
                    "illegal value '" + value + "' for property '" + key
                    + "' of module " + aConfiguration.getName(), e);
            }

        }

        finishLocalSetup();

        Configuration[] childConfigs = aConfiguration.getChildren();
        for (int i = 0; i < childConfigs.length; i++) {
            final Configuration childConfig = childConfigs[i];
            setupChild(childConfig);
        }
    }

    /**
     * Implements the Contextualizable interface using bean introspection.
     * @see Contextualizable
     */
    public final void contextualize(Context aContext)
        throws CheckstyleException
    {
        // TODO: debug log messages
        final String[] attributes = aContext.getAttributeNames();

        for (int i = 0; i < attributes.length; i++) {
            final String key = attributes[i];
            final Object value = aContext.get(key);

            try {
                BeanUtils.copyProperty(this, key, value);
            }
            catch (InvocationTargetException e) {
                // TODO: log.debug("The bean " + this.getClass()
                // + " is not interested in " + value)
                throw new CheckstyleException("cannot set property "
                    + key + " to value " + value + " in bean "
                    + this.getClass().getName(), e);
            }
            catch (IllegalAccessException e) {
                throw new CheckstyleException(
                    "cannot access " + key + " in "
                    + this.getClass().getName(), e);
            }
            catch (IllegalArgumentException e) {
                throw new CheckstyleException(
                    "illegal value '" + value + "' for property '" + key
                    + "' of bean " + this.getClass().getName(), e);
            }
            catch (ConversionException e) {
                throw new CheckstyleException(
                    "illegal value '" + value + "' for property '" + key
                    + "' of bean " + this.getClass().getName(), e);
            }
        }
    }

    /**
     * Returns the configuration that was used to configure this component.
     * @return the configuration that was used to configure this component.
     */
    protected final Configuration getConfiguration()
    {
        return mConfiguration;
    }

    /**
     * Provides a hook to finish the part of this compoent's setup that
     * was not handled by the bean introspection.
     * <p>
     * The default implementation does nothing.
     * </p>
     * @throws CheckstyleException if there is a configuration error.
     */
    protected void finishLocalSetup() throws CheckstyleException
    {
    }

    /**
     * Called by configure() for every child of this component's Configuration.
     * <p>
     * The default implementation does nothing.
     * </p>
     * @param aChildConf a child of this component's Configuration
     * @throws CheckstyleException if there is a configuration error.
     * @see Configuration#getChildren
     */
    protected void setupChild(Configuration aChildConf)
        throws CheckstyleException
    {
    }
}

/**
 * <p>Standard Converter implementation that converts an incoming
 * String into an array of String.  On a conversion failure, returns
 * a specified default value or throws a ConversionException depending
 * on how this instance is constructed.</p>
 *
 * Hacked from
 * http://cvs.apache.org/viewcvs/jakarta-commons/beanutils/src/java/org/apache/commons/beanutils/converters/StringArrayConverter.java
 * because that implementation fails to convert array of tokens with elements
 * containing an underscore, "_" :(
 *
 * @author Rick Giles
 */


final class StrArrayConverter extends AbstractArrayConverter
{
    /**
     * <p>Model object for type comparisons.</p>
     */
    private static String[] sModel = new String[0];

    /**
     * Creates a new StrArrayConverter object.
     */
    public StrArrayConverter()
    {
        this.defaultValue = null;
        this.useDefault = false;
    }

    /**
     * Create a onverter that will return the specified default value
     * if a conversion error occurs.
     *
     * @param aDefaultValue The default value to be returned
     */
    public StrArrayConverter(Object aDefaultValue)
    {
        this.defaultValue = aDefaultValue;
        this.useDefault = true;
    }

    /**
     * Convert the specified input object into an output object of the
     * specified type.
     *
     * @param aType Data type to which this value should be converted
     * @param aValue The input value to be converted
     *
     * @return the converted object
     *
     * @throws ConversionException if conversion cannot be performed
     *  successfully
     */
    public Object convert(Class aType, Object aValue)
        throws ConversionException
    {
        // Deal with a null value
        if (aValue == null) {
            if (useDefault) {
                return (defaultValue);
            }
            throw new ConversionException("No value specified");
        }

        // Deal with the no-conversion-needed case
        if (sModel.getClass() == aValue.getClass()) {
            return (aValue);
        }

        // Parse the input value as a String into elements
        // and convert to the appropriate type
        try {
            final List list = parseElements(aValue.toString());
            final String[] results = new String[list.size()];

            for (int i = 0; i < results.length; i++) {
                results[i] = (String) list.get(i);
            }
            return (results);
        }
        catch (Exception e) {
            if (useDefault) {
                return (defaultValue);
            }
            throw new ConversionException(aValue.toString(), e);
        }
    }

    /**
     * <p>
     * Parse an incoming String of the form similar to an array initializer in
     * the Java language into a <code>List</code> individual Strings for each
     * element, according to the following rules.
     * </p>
     * <ul>
     * <li>The string must have matching '{' and '}' delimiters around a
     * comma-delimited list of values.</li>
     * <li>Whitespace before and after each element is stripped.
     * <li>If an element is itself delimited by matching single or double
     * quotes, the usual rules for interpreting a quoted String apply.</li>
     * </ul>
     * 
     * @param aValue
     *            String value to be parsed
     * @return the list of Strings parsed from the array
     * @throws NullPointerException
     *             if <code>svalue</code> is <code>null</code>
     */
    protected List parseElements(String aValue)
        throws NullPointerException
    {
        // Validate the passed argument
        if (aValue == null) {
            throw new NullPointerException();
        }

        // Trim any matching '{' and '}' delimiters
        aValue = aValue.trim();

        if (aValue.startsWith("{") && aValue.endsWith("}")) {
            aValue = aValue.substring(1, aValue.length() - 1);
        }

        final StringTokenizer st = new StringTokenizer(aValue, ",");
        final List retVal = new ArrayList();

        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            retVal.add(token.trim());
        }

        return retVal;
    }
}
