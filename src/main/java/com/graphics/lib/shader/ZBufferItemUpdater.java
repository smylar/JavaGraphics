package com.graphics.lib.shader;

import java.awt.Color;
import java.util.function.Supplier;

@FunctionalInterface
public interface ZBufferItemUpdater {
    void update(Integer x, Integer y, double z, Supplier<Color> colour);
}
