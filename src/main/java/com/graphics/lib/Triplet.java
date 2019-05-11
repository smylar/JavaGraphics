package com.graphics.lib;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Triplet<T> {
    private final List<T> values;
    
    public Triplet(T first, T second, T third) {
        values = ImmutableList.of(first, second, third);
    }
    
    public T first() {
        return values.get(0);
    }
    
    public T second() {
        return values.get(1);
    }
    
    public T third() {
        return values.get(2);
    }
    
    public List<T> getAsList() {
        return values;
    }
}
