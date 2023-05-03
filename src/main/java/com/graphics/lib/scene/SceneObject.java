package com.graphics.lib.scene;

import com.graphics.lib.Point;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.shader.IShaderFactory;

public record SceneObject(ICanvasObject object, Point framePosition, IShaderFactory shaderFactory) {
    
}
