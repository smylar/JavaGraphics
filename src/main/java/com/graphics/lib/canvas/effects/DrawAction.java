package com.graphics.lib.canvas.effects;

import java.awt.Graphics;
import java.util.function.BiConsumer;

import com.graphics.lib.canvas.AbstractCanvas;

public interface DrawAction extends BiConsumer<AbstractCanvas,Graphics> {

}
