package com.graphics.lib.shader;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;

public class WireframeShader implements IShaderFactory {

    private static final WireframeShader INSTANCE = new WireframeShader();
    
    private WireframeShader() {}
    
    public static IShaderFactory getShader() {
        return INSTANCE;
    }
    
    @Override
    public void add(ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater) {
        parent.getFacetList()
              .parallelStream()
              .forEach(f -> add(f, c, screen, zBufferItemUpdater));

    }
    
    private void add(Facet facet, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater)
    {   
        List<WorldCoord> points = facet.getAsList();
        
        if (points.isEmpty() || points.stream().map(p -> p.getTransformed(c)).noneMatch(GeneralPredicates.isOnScreen(screen))) {
            return;
        }        
        
        Predicate<Point> isOnScreen = GeneralPredicates.isOnScreen(screen);
        Stream.of(new LineEquation(points.get(0), points.get(1), c),
                  new LineEquation(points.get(1), points.get(2), c),
                  new LineEquation(points.get(2), points.get(0), c))
              .forEach(l -> {
                  processX(l, screen, zBufferItemUpdater, isOnScreen);
                  processY(l, screen, zBufferItemUpdater, isOnScreen);
              });
    }
    
    private void processX(final LineEquation l, final Dimension screen, final ZBufferItemUpdater zBufferItemUpdater, final Predicate<Point> isOnScreen) {
        double startx = Math.round(l.getMinX() < 0 ? 0 : l.getMinX());
        double endx = Math.round(l.getMaxX() > screen.getWidth() ? screen.getWidth() : l.getMaxX());
        if (startx != endx) {
            for (double x = startx ; x < endx + 1 ; x++) {
                double y = l.getYAtX(x);
                double z = getZValue(x, y, l);
                if (isOnScreen.test(new Point(x,y,z))) {
                    zBufferItemUpdater.update((int)x, (int)Math.round(y), z, () -> Color.BLACK);
                }
            }
        }
    }
    
    private void processY(final LineEquation l, final Dimension screen, final ZBufferItemUpdater zBufferItemUpdater, final Predicate<Point> isOnScreen) {
        double starty = Math.round(l.getMinY() < 0 ? 0 : l.getMinY());
        double endy = Math.round(l.getMaxY() > screen.getHeight() ? screen.getHeight() : l.getMaxY());
        if (starty != endy) {
            for (double y = starty ; y < endy + 1 ; y++) {
                double x = l.getXAtY(y);
                double z = getZValue(x, y, l);
                if (isOnScreen.test(new Point(x,y,z))) {
                    zBufferItemUpdater.update((int)Math.round(x), (int)y, z, () -> Color.BLACK);
                }
            }
        }
    }
    
    //copied from scanline shader - so might want somewhere common
    private double getZValue(double xVal, double yVal, LineEquation line)
    {
        double dx = xVal - line.getStart().x;
        double dy = yVal - line.getStart().y;
        double len = Math.sqrt((dx*dx)+(dy*dy));
        
        double percentLength = len / line.getLength();
        double startZ = line.getStart().z;
        double endZ = line.getEnd().z;
        
        return this.interpolateZ(startZ, endZ, percentLength); 
    }
    
    private double interpolateZ(double startZ, double endZ, double percentLength)
    {
        return 1d / ((1d/startZ) + (percentLength * ((1d/endZ) - (1d/startZ))));
    }

}
