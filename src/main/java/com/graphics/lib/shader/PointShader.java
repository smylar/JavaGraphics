package com.graphics.lib.shader;

import java.awt.Color;
import java.awt.Dimension;

import com.graphics.lib.GeneralPredicates;
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
              .filter(GeneralPredicates.isOnScreen(screen))
              .forEach(p -> addMarkerToBuffer(p, parent, zBufferItemUpdater));

    }
    
    private void addMarkerToBuffer(final Point p, final ICanvasObject parent, final ZBufferItemUpdater zBufferItemUpdater) {
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y), p.z, y -> Color.BLACK);
        zBufferItemUpdater.update((int)Math.round(p.x+1), (int)Math.round(p.y), p.z, y -> parent.getColour());
        zBufferItemUpdater.update((int)Math.round(p.x-1), (int)Math.round(p.y), p.z ,y -> parent.getColour());
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y+1), p.z, y -> parent.getColour());
        zBufferItemUpdater.update((int)Math.round(p.x), (int)Math.round(p.y-1), p.z, y -> parent.getColour());
    }

}
