package com.graphics.lib.properties;

/**
 * Indicates a class that has properties that can be injected, and has an action that must be invoked after the properties are set
 * 
 * @author paul.brandon
 *
 */
public interface PropertyInjected {
    public void afterPropertiesSet();
}
