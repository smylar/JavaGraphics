package com.graphics.lib.shader;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Consumer;

import com.graphics.lib.Facet;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.zbuffer.ScanLine;

public interface ScanlineShader extends AutoCloseable {

    void setCloseAction(Consumer<ScanlineShader> onClose);

    void init(ICanvasObject obj, Facet f, Camera c, Collection<ILightSource> lightSources);

    Color getColour(ScanLine sl, int x, int y);
}
