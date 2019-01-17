package com.graphics.lib.canvas.effects;

import java.awt.Color;
import java.awt.Graphics;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.AbstractCanvas;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
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
        final double centrex = cnv.getWidth() /2;
        final double centrey = cnv.getHeight() /2;
        final WorldCoord start = new WorldCoord(centrex - 75, centrey, 0);
        final WorldCoord end = new WorldCoord(centrex + 75, centrey, 0);
        final WorldCoord a1 = new WorldCoord(centrex + 60, centrey - 5, 0);
        final WorldCoord a2 = new WorldCoord(centrex + 60, centrey + 5, 0);
        //problem with this is that it will create this object every cycle instead of manipulating an existing one
        //may want to redo to avoid that
        CanvasObject marker = new CanvasObject(() -> Pair.of(ImmutableList.of(start, end, a1, a2), 
                                                             ImmutableList.of()));
        
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutPoint(marker, new Point(centrex, centrey, 0), Axis.Z.getRotation(-data.getzRot()));
        marker.applyTransforms();
        g.setColor(lineColour);
        g.drawLine((int)Math.round(start.x), 
                   (int)Math.round(start.y), 
                   (int)Math.round(end.x), 
                   (int)Math.round(end.y));
        g.fillPolygon(new int[] {(int)Math.round(end.x), (int)Math.round(a1.x), (int)Math.round(a2.x)}, 
                      new int[] {(int)Math.round(end.y), (int)Math.round(a1.y), (int)Math.round(a2.y)}, 
                      3);
        
    }

}
