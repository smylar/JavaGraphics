package com.graphics.tests;

import com.graphics.lib.Point;
import com.graphics.lib.scene.FlooredFrame;
import com.graphics.lib.scene.SceneObject;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.shapes.Mountain;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MountainScene extends FlooredFrame {

    Mountain mountain; //temp store for testing

    public MountainScene(Color floorColour, double floorLevel) {
        super(floorColour, floorLevel);
    }

    @Override
    public void buildFrame() {
        if (isLoaded()) return;

        super.buildFrame();

        Mountain mountain = new Mountain(5, 500, 300);
        mountain.setSnowLine(350, Color.WHITE)
                .setColour(new Color(217, 128, 69));
        addSceneObject(new SceneObject(mountain, new Point(0, floorLevel - 250,0), ScanlineShaderFactory.GORAUD.getDefaultSelector()));


        this.mountain = mountain;
    }


    public void processInput(KeyEvent key) {
        if (key.getKeyChar() == 'o') {
            System.out.println(mountain.getFacetList().get(0).getAsList().get(0));
        }
    }
}
