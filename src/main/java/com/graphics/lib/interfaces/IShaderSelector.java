package com.graphics.lib.interfaces;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.shader.IShaderFactory;

import java.util.function.BiFunction;

public interface IShaderSelector extends BiFunction<ICanvasObject, Camera, IShaderFactory> {
}
