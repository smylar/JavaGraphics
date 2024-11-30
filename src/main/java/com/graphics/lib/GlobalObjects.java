package com.graphics.lib;

import com.graphics.lib.interfaces.ICanvasObject;

import java.util.HashMap;
import java.util.Map;

public class GlobalObjects {
    private static final Map<String, ICanvasObject> globals = new HashMap<>();

    private GlobalObjects() {}

    public static void add(String name, ICanvasObject object) {
        globals.put(name, object);
    }

    public static ICanvasObject get(String name) {
        return globals.get(name);
    }
}
