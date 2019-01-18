package com.graphics.lib.canvas.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.AbstractCanvas;
import com.graphics.lib.orientation.OrientationData;

/**
 * Add roll marker (artificial horizon)
 * 
 * @author paul.brandon
 *
 */
public class RollMarker implements DrawAction {
    
    private static final Color lineColour = new Color(125,125,125,150);

    @Override
    public void accept(AbstractCanvas cnv, Graphics g) {
        //may also want to move this up and down depending on if we are looking down or up
        Camera cam = cnv.getCamera();
        OrientationData data = new OrientationData(cam.getOrientation());
        final int centrex = (int)Math.round(cnv.getWidth() /2);
        final int centrey = (int)Math.round(cnv.getHeight() /2);
        final Polygon indicator = new Polygon();
        indicator.addPoint(centrex - 75, centrey -1);
        indicator.addPoint(centrex + 60, centrey -1);
        indicator.addPoint(centrex + 60, centrey - 5);
        indicator.addPoint(centrex + 75, centrey);
        indicator.addPoint(centrex + 60, centrey + 5);
        indicator.addPoint(centrex + 60, centrey +1);
        indicator.addPoint(centrex - 75, centrey +1);
        
        AffineTransform tx = new AffineTransform();
        tx.rotate(-Math.toRadians(data.getzRot()), centrex, centrey);
        
        g.setColor(lineColour);
        ((Graphics2D)g).fill(tx.createTransformedShape(indicator));
        
    }

}
