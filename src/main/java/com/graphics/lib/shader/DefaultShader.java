package com.graphics.lib.shader;

import java.awt.Color;
import java.util.function.Consumer;

import com.graphics.lib.Facet;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.zbuffer.ScanLine;

public class DefaultShader implements IShader {
    
    private Consumer<IShader> onClose;
    private Facet facet;
    private ICanvasObject parent;

    @Override
    public void close() throws Exception {
        if (onClose != null) {
            onClose.accept(this);
        }
    }

    @Override
    public void setCloseAction(Consumer<IShader> onClose) {
        this.onClose = onClose;
        
    }

    @Override
    public void init(ICanvasObject obj, Facet f, Camera c) {
        facet = f;
        parent = obj;
    }

    @Override
    public Color getColour(ScanLine sl, int x, int y) {
        return facet.getColour() == null ? parent.getColour() : facet.getColour();
    }

}
