package com.graphics.lib.shader;

import java.awt.Color;
import java.util.List;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.ScanLine;
import com.graphics.lib.lightsource.LightSource;

public interface IShader {
	public void init(CanvasObject obj, Facet f);
	public Color getColour (ScanLine sl, int x, int y, double percentDistCovered);
	public void setLightsources(List<LightSource> ls);
}
