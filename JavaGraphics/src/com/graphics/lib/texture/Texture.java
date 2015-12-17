package com.graphics.lib.texture;

import java.awt.Color;

public interface Texture {
	public Color getColour(int x, int y);
	public int getHeight();
	public int getWidth();
}
