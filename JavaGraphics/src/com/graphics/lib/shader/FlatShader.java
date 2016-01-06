package com.graphics.lib.shader;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader that takes the light intensity at the average of the 3 vertices and applies that colour across the whole facet
 * 
 * @author Paul Brandon
 *
 */
public class FlatShader implements IShader{

	private Color colour = new Color(255,255,255);
	private Set<LightSource> ls = new HashSet<LightSource>();
	
	@Override
	public void init(CanvasObject obj, Facet f) {
		Color colour = f.getColour() == null ? obj.getColour() : f.getColour();
		
		Point p = new Point((f.point1.x + f.point2.x + f.point3.x)/3, (f.point1.y + f.point2.y + f.point3.y)/3, (f.point1.z + f.point2.z + f.point3.z)/3);
		
		IntensityComponents pointLight = obj.getLightIntensityFinder().getLightIntensity(ls, obj, p, f.getNormal(), !f.isFrontFace());
		
		this.colour = new Color((int)((double)colour.getRed() * pointLight.getRed()), 
				(int)((double)colour.getGreen() * pointLight.getGreen()), 
				(int)((double)colour.getBlue() * pointLight.getBlue()),
				colour.getAlpha());
		
	}

	@Override
	public Color getColour(ScanLine sl, int x, int y) {
		return this.colour;
	}

	@Override
	public void setLightsources(Set<LightSource> ls) {
		this.ls = ls;
		
	}

	@Override
	public Set<LightSource> getLightsources() {
		return ls;
	}

}
