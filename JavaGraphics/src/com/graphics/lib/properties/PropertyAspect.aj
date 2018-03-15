package com.graphics.lib.properties;

import java.util.Arrays;

/**
 * An aspect for adding values from properties into Object fields on construction
 * Could use Spring for it but don't really want to bring all that in for this project
 * and besides it's far more fun figuring things out for yourself
 * 
 * @author paul
 *
 */
public aspect PropertyAspect {

    after() returning(Object obj) : call(@PropertyInject *.new(..)) {
        //gets object after construction if class annotated with @PropertyInject
        insertProperties(obj);
    }
    
    after() returning(PropertyInjected obj) : call(*.new(..)) {
        //gets object after construction if it implements the PropertyInjected interface
        //annoyingly flags all constructors on cross-reference even though it only works on classes with the correct interface
        insertProperties(obj);
        obj.afterPropertiesSet();
    }
    
    private void insertProperties(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredFields())
              .filter(f -> f.isAnnotationPresent(Property.class))
              .forEach(f -> {
                  Property prop = f.getAnnotation(Property.class);
                  //TODO create property file and attempt retrieval - for now get the default
                  //will need converters for different types
                  try {
                      f.setAccessible(true);
                      f.set(obj, prop.defaultValue());
                      f.setAccessible(false);
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
              });
    }

}
