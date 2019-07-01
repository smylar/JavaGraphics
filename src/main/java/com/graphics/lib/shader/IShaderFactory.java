package com.graphics.lib.shader;

import java.awt.Dimension;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Interface for retrieving shaders (allows custom shaders to be added)
 * 
 * @author paul.brandon
 *
 */
@FunctionalInterface
public interface IShaderFactory {

    void add(ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater);

}
