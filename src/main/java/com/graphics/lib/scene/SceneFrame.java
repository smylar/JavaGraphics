package com.graphics.lib.scene;

import java.awt.event.KeyEvent;
import java.util.List;

import com.graphics.lib.lightsource.ILightSource;
import com.graphics.shapes.Surface;

public interface SceneFrame {
    void buildFrame();
    void destroyFrame();
    List<ILightSource> getFrameLightsources();
    List<SceneObject> getFrameObjects();
    Surface getFloor();
    SceneExtents getSceneExtents();
    boolean isLoaded();
    void processInput(KeyEvent key);
}
