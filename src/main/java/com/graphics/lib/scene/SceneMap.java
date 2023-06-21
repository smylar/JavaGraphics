package com.graphics.lib.scene;

import com.graphics.lib.Point;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SceneMap {

    public static final int sceneSize = 3600; //let's assume each scene is a fixed size square for ease with this length
    //scenes we generate will need to match
    private final Map<String, SceneFrame> frames = new HashMap<>();

    public SceneMap add(int xAddr, int zAddr, SceneFrame scene) { //may want to add default too
        //idea here that as we move around we can load in new content and drop what we don't need
        frames.put(toFrameAddress(xAddr, zAddr), scene);
        //TODO will need to validate coords in frame fall within the frame size
        return this;
    }

    public SceneWithOffset getFrameFromPoint(Point point) {
        //given how frames currently registered, the centre point of the first from is at 0,0

        int xAddr = toFrameCoord(point.x);
        int zAddr = toFrameCoord(point.z);

        String frameAddress = toFrameAddress(xAddr, zAddr);
        SceneFrame frame = frames.computeIfAbsent(frameAddress, k -> new FlooredFrame(Color.LIGHT_GRAY, 700)); //arbitrary default for the moment

        return new SceneWithOffset(frame, xAddr*sceneSize, zAddr*sceneSize);
    }

    private String toFrameAddress(int x, int z) {
        return String.format("%d_%d", x, z);
    }

    private int toFrameCoord (double pointCoord) {
        return (int) ((pointCoord + (Math.copySign(sceneSize, pointCoord)/2)) / sceneSize);
    }
}
