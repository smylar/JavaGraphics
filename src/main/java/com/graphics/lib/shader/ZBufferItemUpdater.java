package com.graphics.lib.shader;

import java.awt.Color;
import java.util.function.IntFunction;

@FunctionalInterface
public interface ZBufferItemUpdater {
    void update(Integer x, Integer y, double z, IntFunction<Color> colour);
}
