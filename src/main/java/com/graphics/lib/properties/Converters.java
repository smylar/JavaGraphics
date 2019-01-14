package com.graphics.lib.properties;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Converters for translating property values into their required type
 * 
 * @author paul.brandon
 *
 */
public enum Converters {
    STRING(String.class, s -> s),
    INT(Integer.class, Integer::valueOf);
    
    private Class<?> clazz;
    private Function<String,?> converter;
    
    private <T> Converters (Class<T> clazz, Function<String,T> converter) {
        this.converter = converter;
        this.clazz = clazz;
    }
    
    @SuppressWarnings("unchecked")
    private <T> Function<String, T> getConverter(Class<T> clazz) {
        if (this.clazz.equals(clazz)) {
            return (Function<String, T>)converter;
        }
        
        throw new RuntimeException(String.format("Could not get converter %s as %s", this.name(), clazz.getName()));
    }
    
    private Class<?> getOutputClass() {
        return clazz;
    }
    
    
    public static <T> T convert(Class<T> clazz, String source) {
        return Arrays.stream(Converters.values())
                      .filter(c -> c.getOutputClass().equals(clazz))
                      .findFirst()
                      .map(c -> c.getConverter(clazz))
                      .map(c -> c.apply(source))
                      .orElseThrow(() -> new RuntimeException(String.format("Could not get converter for %s", clazz.getName())));
    }
}
