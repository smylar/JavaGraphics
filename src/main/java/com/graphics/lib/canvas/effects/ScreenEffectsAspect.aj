package com.graphics.lib.canvas.effects;

import java.awt.Graphics;
import java.util.List;

import com.google.common.collect.Lists;
import com.graphics.lib.canvas.Canvas3D;

/**
 * Draws screen overlay elements once the scene has been rendered
 * 
 * @author paul.brandon
 *
 */
public aspect ScreenEffectsAspect {

    private static List<DrawAction> actions = Lists.newArrayList();
    
    public static void addAction(DrawAction action) {
        actions.add(action);
    }
    
    after(Canvas3D cnv, Graphics g): target(cnv) && args(g) && execution(void paintComponent(Graphics)) {
        actions.forEach(op -> op.accept(cnv, g));
    }
}
