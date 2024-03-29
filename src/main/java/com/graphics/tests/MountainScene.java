package com.graphics.tests;

import com.graphics.lib.Point;
import com.graphics.lib.scene.FlooredFrame;
import com.graphics.lib.scene.SceneObject;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.shapes.Mountain;
import com.graphics.shapes.MountainUtils;

import java.awt.Color;

public class MountainScene extends FlooredFrame {

    public MountainScene(Color floorColour, double floorLevel) {
        super(floorColour, floorLevel);
    }

    @Override
    public void buildFrame() {
        if (isLoaded()) return;

        super.buildFrame();

        Mountain mountain = new Mountain(5, 500, 300, 100);
        MountainUtils.setSnowLine(mountain, 350, Color.WHITE)
                .setColour(new Color(217, 128, 69));
        mountain.useAveragedNormals(60);
        addSceneObject(new SceneObject(mountain, new Point(0, floorLevel - 250,0), ScanlineShaderFactory.GORAUD.getDefaultSelector()));
    }
}
