package com.graphics.lib.shader;

/**
 * Interface for retrieving shaders (allows custom shaders to be added)
 * 
 * @author paul.brandon
 *
 */
@FunctionalInterface
public interface IShaderFactory {
    /**
     * Retrieve a shader instance
     * 
     * @return
     */
    public IShader getShader();

}
