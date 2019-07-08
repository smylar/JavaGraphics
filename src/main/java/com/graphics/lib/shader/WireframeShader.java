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
        double startx = Math.round(l.getMinX() < 0 ? 0 : l.getMinX());
        double endx = Math.round(l.getMaxX() > screen.getWidth() ? screen.getWidth() : l.getMaxX());
        if (startx != endx) {
            for (double x = startx ; x < endx + 1 ; x++) {
                double y = l.getYAtX(x);
                double z = l.getZValue(x, y);
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
                double z = l.getZValue(x, y);
                if (isOnScreen.test(new Point(x,y,z))) {
                    zBufferItemUpdater.update((int)Math.round(x), (int)y, z, () -> Color.BLACK);
                }
            }
        }
    }
}
