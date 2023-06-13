package com.graphics.lib.scene;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.texture.Texture;
import com.graphics.shapes.Surface;

public class FlooredFrame implements SceneFrame {

    private final Vector up = new Vector(0,-1,0);
    private final Color floorColour;
    private final double floorLevel;
    private final Set<Supplier<Texture>> textureSuppliers = new HashSet<>();

    private final List<SceneObject> frameObjects = new ArrayList<>();
    private final List<LightSource> lightSources = new ArrayList<>();
    private Surface floor;
    private boolean isLoaded = false;

    public FlooredFrame(Color floorColour, double floorLevel) {
        this.floorColour = floorColour;
        this.floorLevel = floorLevel;
    }
    
    @Override
    public void buildFrame() {
        if (isLoaded) return;
        
        floor = new Surface(20, 20, 180);
        floor.applyTransform(Axis.X.getRotation(-90));
        floor.setColour(floorColour);
        floor.setBaseIntensity(0.5);
        floor.setVertexNormalFinder((obj, p, f) -> up);
        textureSuppliers.forEach(s -> floor.addTexture(s.get()));
        ScanlineShaderFactory shader = textureSuppliers.isEmpty() ? ScanlineShaderFactory.GORAUD : ScanlineShaderFactory.TEXGORAUD;
        frameObjects.add(new SceneObject(floor, new Point(0, floorLevel, 0), shader.getDefaultSelector()));
        isLoaded = true;
    }

    @Override
    public List<LightSource> getFrameLightsources() {
        return List.copyOf(lightSources);
    }

    @Override
    public List<SceneObject> getFrameObjects() {
        return List.copyOf(frameObjects);
    }

    @Override
    public Surface getFloor() {
        return floor;
    }

    @Override
    public SceneExtents getSceneExtents() {
        List<WorldCoord> corners = floor.getCorners();
        return new SceneExtents(corners.get(1).x,
                corners.get(0).x,
                corners.get(0).z,
                corners.get(2).z); //on the basis that we don't move the world grid
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void destroyFrame() {
        frameObjects.forEach(o -> o.object().setDeleted(true));
        lightSources.forEach(ls -> ls.setDeleted(true));
        floor = null;
        frameObjects.clear();
        lightSources.clear();
        isLoaded = false;
    }

    @Override
    public void processInput(KeyEvent key) {
        // TODO Auto-generated method stub
        
    }

    public void addFloorTexture(final Supplier<Texture> textureSupplier) { //only load bmps as required not upfront
        textureSuppliers.add(textureSupplier);
    }

    public void addSceneObject(SceneObject obj) {
        frameObjects.add(obj);
    }

    public void addSceneLightSource(LightSource ls) {
        lightSources.add(ls);
    }

}
