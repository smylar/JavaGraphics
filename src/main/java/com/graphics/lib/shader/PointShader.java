package com.graphics.lib.shader;

import java.awt.Color;
import java.awt.Dimension;

import com.graphics.lib.Point;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Draws a marker at point in the vertex list
 */
public class PointShader implements IShaderFactory {
    
    private static final PointShader INSTANCE = new PointShader();
    
    private PointShader() {}
    
    public static PointShader getShader() {
        return INSTANCE;
    }

    @Override
    public void add(final ICanvasObject parent, final Camera c, final Dimension screen, final ZBufferItemUpdater zBufferItemUpdater) {
        parent.getVertexList()
              .stream()
              .filter(v -> !v.hasTags())
              .map(v -> v.getTransformed(c))
              .filter(p -> isOnScreen(p, screen))
              .forEach(p -> addMarkerToBuffer(p, parent, zBufferItemUpdater));

    }
    
    private boolean isOnScreen(final Point p, final Dimension screen) {
        return p.z > 0 &&
               p.x > 0 && p.x < screen.getWidth() &&
               p.y > 0 && p.y < screen.getHeight();
    }
    
    private void addMarkerToBuffer(final Point p, final ICanvasObject parent, final ZBufferItemUpdater zBufferItemUpdater) {
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y), p.z, () -> Color.BLACK);
        zBufferItemUpdater.update((int)Math.round(p.x+1), (int)Math.round(p.y), p.z, parent::getColour);
        zBufferItemUpdater.update((int)Math.round(p.x-1), (int)Math.round(p.y), p.z, parent::getColour);
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y+1), p.z, parent::getColour);
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y-1), p.z, parent::getColour);
    }

}
