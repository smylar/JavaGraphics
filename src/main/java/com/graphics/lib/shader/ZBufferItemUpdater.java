package com.graphics.lib.shader;

import java.awt.Color;
import java.util.function.Function;

@FunctionalInterface
public interface ZBufferItemUpdater {
    void update(Integer x, Integer y, double z, Function<Integer,Color> colour);
}
