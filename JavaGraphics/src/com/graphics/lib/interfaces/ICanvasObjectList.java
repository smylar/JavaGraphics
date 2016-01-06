package com.graphics.lib.interfaces;

import java.util.Collection;

import com.graphics.lib.canvas.CanvasObject;

@FunctionalInterface
public interface ICanvasObjectList {
	public Collection<CanvasObject> get();
}
