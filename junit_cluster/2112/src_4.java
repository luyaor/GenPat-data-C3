package org.junit.experimental.theories.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * Supplies Theory parameters based on all public members of the target class.
 */
public class AllMembersSupplier extends ParameterSupplier {
    
    private final TestClass fClass;

    /**
     * Constructs a new supplier for {@code type}
     */
    public AllMembersSupplier(TestClass type) {
        fClass = type;
    }

    @Override
    public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();

        addSinglePointFields(sig, list);
        addMultiPointFields(sig, list);
        addSinglePointMethods(sig, list);
        addMultiPointMethods(sig, list);

        return list;
    }

    private void addMultiPointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
        for (FrameworkMethod dataPointsMethod : getDataPointsMethods(sig)) {
            try {
                addArrayValues(sig, dataPointsMethod.getName(), list, dataPointsMethod.invokeExplosively(null));
            } catch (Throwable e) {
                // ignore and move on
            }
        }
    }

    private void addSinglePointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
        for (FrameworkMethod dataPointMethod : getSingleDataPointMethods(sig)) {
            Object value;
            
            try {
                value = dataPointMethod.invokeExplosively(null);
            } catch (Throwable e) {
                // ignore and move on
                continue;
            }
                
            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue(dataPointMethod.getName(), value));
            }
        }
    }
    
    private void addMultiPointFields(ParameterSignature sig, List<PotentialAssignment> list) {
        for (final Field field : getDataPointsFields(sig)) {
            addArrayValues(sig, field.getName(), list, getStaticFieldValue(field));
        }
    }

    private void addSinglePointFields(ParameterSignature sig, List<PotentialAssignment> list) {
        for (final Field field : getSingleDataPointFields(sig)) {
            Object value = getStaticFieldValue(field);
            
            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue(field.getName(), value));
            }
        }
    }

    private void addArrayValues(ParameterSignature sig, String name, List<PotentialAssignment> list, Object array) {
        for (int i = 0; i < Array.getLength(array); i++) {
            Object value = Array.get(array, i);
            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue(name + "[" + i + "]", value));
            }
        }
    }

    private Object getStaticFieldValue(final Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "unexpected: field from getClass doesn't exist on object");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "unexpected: getFields returned an inaccessible field");
        }
    }

    protected Collection<FrameworkMethod> getDataPointsMethods(ParameterSignature sig) {
        return fClass.getAnnotatedMethods(DataPoints.class);        
    }
    
    protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        List<FrameworkField> fields = fClass.getAnnotatedFields(DataPoint.class);
        Collection<Field> validFields = new ArrayList<Field>();

        for (FrameworkField frameworkField : fields) {
            validFields.add(frameworkField.getField());
        }

        return validFields;
    }
    
    protected Collection<Field> getDataPointsFields(ParameterSignature sig) {
        List<FrameworkField> fields = fClass.getAnnotatedFields(DataPoints.class);
        Collection<Field> validFields = new ArrayList<Field>();

        for (FrameworkField frameworkField : fields) {
            validFields.add(frameworkField.getField());
        }

        return validFields;
    }
    
    protected Collection<FrameworkMethod> getSingleDataPointMethods(ParameterSignature sig) {
        return fClass.getAnnotatedMethods(DataPoint.class);
    }

}