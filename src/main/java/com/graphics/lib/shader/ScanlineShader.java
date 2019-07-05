package com.graphics.lib.shader;

import java.awt.Color;
import java.util.function.Consumer;

import com.graphics.lib.Facet;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.zbuffer.ScanLine;

public interface ScanlineShader extends AutoCloseable {

    public void setCloseAction(Consumer<ScanlineShader> onClose);

    public void init(ICanvasObject obj, Facet f, Camera c);

    public Color getColour(ScanLine sl, int x, int y);
}
