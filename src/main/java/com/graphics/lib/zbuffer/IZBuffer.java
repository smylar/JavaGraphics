package com.graphics.lib.zbuffer;

import java.awt.image.BufferedImage;
import java.util.Collection;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.shader.IShaderFactory;

/**
 * Interface for a Z Buffer.
 * <br/>
 * A Z Buffer contains the information required to draw the entire scene on the screen.
 * Generally we calculate the x, y and position and colour of every pixel in an object,
 * as more than one object might occupy the same screen x and y coordinates, the one with the smallest z value remains in the buffer
 * 
 * @author Paul Brandon
 *
 */
public sealed interface IZBuffer permits ZBuffer {
    /**
     * Add a facet to the Z Buffer
     * 
     * @param obj - The CanvasObject the facet belongs to
     * @param shader - The shader object that will handle colouring of the facet
     * @param c- The camera being processed
     */
    void add(ICanvasObject obj, IShaderFactory shader, Camera c, double horizon, Collection<ILightSource> lightSources);
    
    /**
     * Get the Z buffer, the form is (in an attempt for some performance when finding a specific entry):
     * <br/>
     * <code>Map{xValue, Map{yValue, item}}</code>
     * @return The Z Buffer map
     */
    BufferedImage getBuffer();
    
    /**
     * Refresh the image buffer with the current data
     */
    void refreshBuffer();
    
    
    /**
     * Set the width of the buffer in pixels
     */
    void setDimensions(int width, int height);
    
    /**
     * Get the item at the given x, y coordinates
     */
    ZBufferItem getItemAt(int x, int y);
    
    void clear();
}
