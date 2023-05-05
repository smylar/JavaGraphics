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
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.texture.Texture;
import com.graphics.shapes.Surface;

public class FlooredFrame implements SceneFrame {

    private final Color floorColour;
    private final double floorLevel;
    private final Set<Supplier<Texture>> textureSuppliers = new HashSet<>();
    
    protected Surface floor;
    protected List<SceneObject> frameObjects = new ArrayList<>();
    protected List<LightSource> lightSources = new ArrayList<>();

    public FlooredFrame(Color floorColour, double floorLevel) {
        this.floorColour = floorColour;
        this.floorLevel = floorLevel;
    }
    
    @Override
    public void buildFrame() {
        if (!frameObjects.isEmpty()) return;
        
        floor = new Surface(20, 20, 180);
        floor.applyTransform(Axis.X.getRotation(-90));
        floor.setColour(floorColour);
        floor.setBaseIntensity(0.5);
        textureSuppliers.forEach(s -> floor.addTexture(s.get()));
        ScanlineShaderFactory shader = textureSuppliers.isEmpty() ? ScanlineShaderFactory.GORAUD : ScanlineShaderFactory.TEXGORAUD;
        frameObjects.add(new SceneObject(floor, new Point(0, floorLevel, 0), shader));
    }

    @Override
    public List<LightSource> getFrameLightsources() {
        return lightSources == null ? List.of() : List.copyOf(lightSources);
    }

    @Override
    public List<SceneObject> getFrameObjects() {
        return frameObjects == null ? List.of() : List.copyOf(frameObjects);
    }

    @Override
    public Surface getFloor() {
        return floor;
    }

    @Override
    public void destroyFrame() {
        frameObjects.forEach(o -> o.object().setDeleted(true));
        lightSources.forEach(ls -> ls.setDeleted(true));
        floor = null;
        frameObjects.clear();
        lightSources.clear();
    }

    @Override
    public void processInput(KeyEvent key) {
        // TODO Auto-generated method stub
        
    }

    public void addFloorTexture(final Supplier<Texture> textureSupplier) { //only load bmps as required not upfront
        textureSuppliers.add(textureSupplier);
    }

}
