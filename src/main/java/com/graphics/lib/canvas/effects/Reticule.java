package com.graphics.lib.canvas.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;

import com.graphics.lib.Utils;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.AbstractCanvas;

/**
 * Draw reticule in middle of the screen
 * 
 * @author paul.brandon
 *
 */
public class Reticule implements DrawAction {
    
    private final Color startColour;
    private final Color endColour;
    private final BiConsumer<Pair<Integer,Integer>, Graphics> drawer;
    
    public Reticule() {
        this(Color.RED, new Color(0,255,0,0));
    }
    
    public Reticule(Color start, Color end) {
        startColour = start;
        endColour = end;
        drawer = Utils.drawCircle(20, startColour, endColour);
    }

    @Override
    public void accept(final AbstractCanvas cnv, final Graphics g) {
        int middleHeight = cnv.getHeight() / 2;
        int middleWidth = cnv.getWidth() / 2;
        
        g.setColor(Color.RED);
        g.drawLine(middleWidth,5,middleWidth,30);
        
        g.setColor(Color.black);

        drawer.accept(Pair.of(middleWidth, middleHeight), g);
        
        Utils.cast(cnv.getCamera(), ViewAngleCamera.class)
             .ifPresent(vac ->  g.drawString(""+vac.getViewAngle() , cnv.getWidth() - 30, cnv.getHeight() - 5));
    }

}
