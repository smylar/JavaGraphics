package com.graphics.lib.scene;

import com.graphics.lib.Point;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IShaderSelector;

public record SceneObject(ICanvasObject object, Point framePosition, IShaderSelector shaderSelector) {
    
}
