package com.graphics.lib.canvas.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.graphics.lib.GeneralPredicates;
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
    
    public BoundingBox(IObjectFinder targetSupplier) {
        this.targetSupplier = targetSupplier;
    }
    
    @Override
    public void accept(AbstractCanvas cnv, Graphics g) {
        Camera cam = cnv.getCamera();
        ICanvasObject selectedObject = targetSupplier.find();
        if (selectedObject == null || !selectedObject.isVisible()) 
            return;
        
        CentreFinder bounds = selectedObject.getVertexList().stream()
                                            .filter(GeneralPredicates.untagged())
                                            .map(coord -> coord.getTransformed(cam))
                                            .collect(CentreFinder::new, CentreFinder::accept, CentreFinder::combine);
        
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
        
        //might be nice to add an indicator in which way to turn if the target is off screen
    }

}
