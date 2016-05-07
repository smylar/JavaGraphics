package com.graphics.lib.interfaces;

import com.graphics.lib.canvas.CanvasObject;

@FunctionalInterface
public interface IObjectFinder {
	public CanvasObject find();
}
