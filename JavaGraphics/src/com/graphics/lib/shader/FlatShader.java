package com.graphics.lib.shader;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader that takes the light intensity at the average of the 3 vertices and applies that colour across the whole facet
 * 
 * @author Paul Brandon
 *
 */
public class FlatShader implements IShader{

	private Color colour = new Color(255,255,255);
	private Set<ILightSource> ls = Canvas3D.get().getLightSources();
	
	@Override
	public void init(ICanvasObject obj, Facet f, Camera c) {
		Color colour = f.getColour() == null ? obj.getColour() : f.getColour();
		
		List<WorldCoord> points = f.getAsList();
		Point p = new Point((points.get(0).x + points.get(1).x + points.get(2).x)/3, 
				(points.get(0).y + points.get(1).y + points.get(2).y)/3, 
				(points.get(0).z + points.get(1).z + points.get(2).z)/3);
		
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

}
