package com.graphics.lib.interfaces;

import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasEvent;
import com.graphics.lib.scene.SceneMap;
import com.graphics.lib.scene.SceneWithOffset;

public interface ISecondaryCamera {
	void update(Canvas3D source, CanvasEvent event, ICanvasObject obj);

	SceneWithOffset getRelevantFrame(SceneMap sceneMap);
}
