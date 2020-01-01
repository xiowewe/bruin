package com.bruin.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyEditorSupport;
import java.util.stream.Stream;

public class Beans {

    public static void main(String[] args) throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(Person.class, Object.class);

        Stream.of(info.getPropertyDescriptors())
                .forEach(propertyDescriptor -> {

                    System.out.println(propertyDescriptor.toString());

                    Class<?> propertytype = propertyDescriptor.getPropertyType();
                    if("age".endsWith(propertyDescriptor.getName())){
                        propertyDescriptor.setPropertyEditorClass(StringToIntegerPropertyEditor.class);
                    }
                });

    }

    static class StringToIntegerPropertyEditor extends PropertyEditorSupport{
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            Integer value = Integer.valueOf(text);

            setValue(value * value);
        }

    }
}
