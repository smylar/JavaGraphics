package com.graphics.lib.interfaces;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface ITrait {
    default Map<String, Method> getInterceptors() {
        return new HashMap<>();
    }
    
    void setParent(ICanvasObject parent);
}
