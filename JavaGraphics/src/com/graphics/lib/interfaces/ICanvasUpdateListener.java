package com.graphics.lib.interfaces;

import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;

public interface ICanvasUpdateListener {
	public void update(Canvas3D source, CanvasObject obj);
}
