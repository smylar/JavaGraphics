package com.graphics.lib;

import java.util.ArrayList;
import java.util.List;

public class Triplet<T> {
    final List<T> triplet = new ArrayList<>(3);
    
    public Triplet(T first, T second, T third) {
        triplet.add(first);
        triplet.add(second);
        triplet.add(third);
    }
    
    public T first() {
        return triplet.get(0);
    }
    
    public T second() {
        return triplet.get(1);
    }
    
    public T third() {
        return triplet.get(2);
    }
    
    public List<T> getAsList() {
        return triplet;
    }
}
