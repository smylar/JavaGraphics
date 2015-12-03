package com.graphics.lib.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.ScanLine;
import com.graphics.lib.lightsource.LightSource;

public class FlatShader implements IShader{

	private Color colour = new Color(255,255,255);
	private List<LightSource> ls = new ArrayList<LightSource>();
	
	@Override
	public void init(CanvasObject obj, Facet f) {
		Color colour = f.getColour() == null ? obj.getColour() : f.getColour();
		
		Point p = new Point((f.point1.x + f.point2.x + f.point3.x)/3, (f.point1.y + f.point2.y + f.point3.y)/3, (f.point1.z + f.point2.z + f.point3.z)/3);
		
		p.setNormal(f.getNormal());
		p.setLightIntensity(obj.getLightIntensityFinder(ls), !f.isFrontFace(), obj);
		
		this.colour = new Color((int)((double)colour.getRed() * p.getLightIntensity().getRed()), 
				(int)((double)colour.getGreen() * p.getLightIntensity().getGreen()), 
				(int)((double)colour.getBlue() * p.getLightIntensity().getBlue()),
				colour.getAlpha());
		
	}

	@Override
	public Color getColour(ScanLine sl, int x, int y, double percentDistCovered) {
		return this.colour;
	}

	@Override
	public void setLightsources(List<LightSource> ls) {
		this.ls = ls;
		
	}

}
