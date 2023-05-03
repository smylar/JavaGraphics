package com.graphics.lib.scene;

import java.awt.event.KeyEvent;
import java.util.List;

import com.graphics.lib.lightsource.LightSource;
import com.graphics.shapes.Surface;

public interface SceneFrame {
    void buildFrame();
    void destroyFrame();
    List<LightSource> getFrameLightsources();
    List<SceneObject> getFrameObjects();
    Surface getFloor();
    void processInput(KeyEvent key);
}
