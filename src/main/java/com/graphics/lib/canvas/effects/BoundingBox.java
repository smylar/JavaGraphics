package com.graphics.lib.canvas.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.util.Optional;

import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.AbstractCanvas;
import com.graphics.lib.collectors.CentreFinder;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IObjectFinder;

/**
 * Draw box around a selected object
 * 
 * @author paul.brandon
 *
 */
public class BoundingBox implements DrawAction {

    private final IObjectFinder targetSupplier;
    private static final Vector up = new Vector(0,-1,0);
    
    public BoundingBox(IObjectFinder targetSupplier) {
        this.targetSupplier = targetSupplier;
    }
    
    @Override
    public void accept(final AbstractCanvas cnv, final Graphics g) {
        Camera cam = cnv.getCamera();
        
        Optional.ofNullable(targetSupplier.find())
                .filter(ICanvasObject::isVisible)
                .ifPresent(selectedObject -> {
                    CentreFinder bounds = getBounds(selectedObject, cam);
                    if (isOffScreen(bounds, cnv.getHeight(), cnv.getWidth())) {
                        drawIndicator(g, bounds, cnv);
                    } else { 
                        drawBoundingBox(g, bounds);
                    }
                    
                });
    }
    
    private void drawBoundingBox(final Graphics g, final CentreFinder bounds) {
        g.setColor(Color.DARK_GRAY);
        float[] dash1 = {10.0f};
        BasicStroke dashed = new BasicStroke(1.5f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10.0f, dash1, 0.0f);
        ((Graphics2D)g).setStroke(dashed);
        g.drawRect((int)(bounds.getMinX() - 4), 
                   (int)(bounds.getMinY() - 4), 
                   (int)(bounds.getMaxX() - bounds.getMinX() + 8), 
                   (int)(bounds.getMaxY()  - bounds.getMinY() + 8));
    }
    
    private void drawIndicator(final Graphics g, final CentreFinder bounds, final AbstractCanvas cnv) {
        int centrey = cnv.getHeight() / 2;
        int centrex = cnv.getWidth() / 2;
        
        Point boundsCentre = bounds.result();
        Vector vCentre = new Point(centrex, centrey, 0).vectorToPoint(new Point(boundsCentre.x, boundsCentre.y, 0));
        double angle = up.angleBetweenRad(vCentre);
        if (boundsCentre.x < centrex) {
            angle = -angle;
        }
        final Polygon indicator = new Polygon();
        indicator.addPoint(centrex, centrey - 100);
        indicator.addPoint(centrex + 10, centrey - 90);
        indicator.addPoint(centrex - 10, centrey - 90);
        
        AffineTransform tx = new AffineTransform();
        tx.rotate(angle, centrex, centrey);
        
        g.setColor(Color.RED);
        ((Graphics2D)g).fill(tx.createTransformedShape(indicator));
    }
    
    private CentreFinder getBounds(ICanvasObject selectedObject, Camera cam) {
        return  selectedObject.getVertexList().stream()
                                              .filter(GeneralPredicates.untagged())
                                              .map(coord -> coord.getTransformed(cam))
                                              .collect(CentreFinder::new, CentreFinder::accept, CentreFinder::combine);
    }
    
    private boolean isOffScreen(final CentreFinder bounds, final int height, final int width) {
        return bounds.getMinZ() <= 1 ||
               bounds.getMaxX() < 0 ||
               bounds.getMinX() > width || 
               bounds.getMaxY() < 0 || 
               bounds.getMinY() > height;
    }

}
