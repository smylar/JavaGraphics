package com.graphics.lib.shader;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
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
        Predicate<Point> isOnScreen = GeneralPredicates.isOnScreen(screen);
        parent.getFacetList()
              .parallelStream()
              .flatMap(f -> add(f, c, isOnScreen))
              .unordered()
              .distinct()
              .forEach(l -> {
                  processX(l, screen, zBufferItemUpdater, isOnScreen);
                  processY(l, screen, zBufferItemUpdater, isOnScreen);
              });

    }
    
    private Stream<LineEquation> add(final Facet facet, final Camera c, final Predicate<Point> isOnScreen)
    {   
        List<WorldCoord> points = facet.getAsList();
        
        if (points.isEmpty() || points.stream().map(p -> p.getTransformed(c)).noneMatch(isOnScreen)) {
            return Stream.empty();
        }        
        
        return Stream.of(new LineEquation(points.get(0), points.get(1), c),
                         new LineEquation(points.get(1), points.get(2), c),
                         new LineEquation(points.get(2), points.get(0), c));
    }
    
    private void processX(final LineEquation l, final Dimension screen, final ZBufferItemUpdater zBufferItemUpdater, final Predicate<Point> isOnScreen) {
        final double startx = l.getMinX() < 0 ? 0 : l.getMinX();
        final double endx = l.getMaxX() > screen.getWidth() ? screen.getWidth() : l.getMaxX();

        if (startx < endx) {
            DoubleStream.iterate(startx, x -> x < endx, x -> x+1)
                        .forEach(x -> addToBuffer(x, l.getYAtX(x), l, zBufferItemUpdater, isOnScreen));
        }
    }
    
    private void processY(final LineEquation l, final Dimension screen, final ZBufferItemUpdater zBufferItemUpdater, final Predicate<Point> isOnScreen) {
        final double starty = l.getMinY() < 0 ? 0 : l.getMinY();
        final double endy = l.getMaxY() > screen.getHeight() ? screen.getHeight() : l.getMaxY();
        if (starty < endy) {
            DoubleStream.iterate(starty, y -> y <= endy, y -> y+1)
                        .forEach(y -> addToBuffer(l.getXAtY(y), y, l, zBufferItemUpdater, isOnScreen));
        }
    }
    
    private void addToBuffer(final double x, final double y, final LineEquation l, final ZBufferItemUpdater zBufferItemUpdater, final Predicate<Point> isOnScreen) {
        double z = l.getZValue(x, y);
        if (isOnScreen.test(new Point(x,y,z))) {
            zBufferItemUpdater.update((int)Math.round(x), (int)Math.round(y), z, () -> Color.BLACK);
        }
    }
}
