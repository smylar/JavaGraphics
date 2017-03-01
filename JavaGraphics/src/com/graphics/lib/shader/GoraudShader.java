package com.graphics.lib.shader;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader that works out the colour at each vertex and then interpolates the colours for the pixels in between
 * 
 * @author Paul Brandon
 *
 */
public class GoraudShader implements IShader{

	protected Color colour; //TODO as private with getters etc
	protected Facet facet;
	protected ScanLine curScanline;
	protected IntensityComponents startIntensity;
	protected IntensityComponents endIntensity;
	protected IntensityComponents pointIntensity = new IntensityComponents();
	protected Point startTexture;
	protected Point endTexture;
	protected double lineLength = 0;
	protected Set<ILightSource> ls = new HashSet<ILightSource>();
	protected Map<Point, IntensityComponents> pointLight = new HashMap<Point, IntensityComponents>();
	
	@Override
	public void init(ICanvasObject parent, Facet facet, Camera c) {
		colour = facet.getColour() == null ? parent.getColour() : facet.getColour();
		if (colour == null) colour = new Color(255,255,255);
		
		if (parent.hasFlag(Events.NO_SHADE)) return;

		for (WorldCoord p : facet.getAsList())
		{
			Vector n = parent.getVertexNormalFinder().getVertexNormal(parent, p, facet);
			pointLight.put(p.getTransformed(c), parent.getLightIntensityFinder().getLightIntensity(ls, parent, p, n, !facet.isFrontFace()));
		}

		this.facet = facet;
		curScanline = null;
		startIntensity = null;
		endIntensity = null;
		startTexture = null;
		endTexture = null;
	}

	@Override
	public Color getColour(ScanLine scanLine, int x, int y) {
		if (facet == null || scanLine == null) return colour;
		
		if (scanLine != this.curScanline){
			startIntensity = this.getIntensities(x, scanLine.startY, scanLine.startLine);
			endIntensity = this.getIntensities(x, scanLine.endY, scanLine.endLine);
			curScanline = scanLine;
			lineLength = Math.ceil(scanLine.endY) - Math.floor(scanLine.startY);
		}
		
		if (lineLength == 0) return colour;

		double percentDistCovered = (y - Math.floor(scanLine.startY)) / lineLength;
		
		Color pointColour = colour;
		
		//TODO something is off, mesh lines appear brighter
		pointIntensity.setRed(startIntensity.getRed() + ((endIntensity.getRed() - startIntensity.getRed()) * percentDistCovered));
		pointIntensity.setGreen(startIntensity.getGreen() + ((endIntensity.getGreen() - startIntensity.getGreen()) * percentDistCovered));
		pointIntensity.setBlue(startIntensity.getBlue() + ((endIntensity.getBlue() - startIntensity.getBlue()) * percentDistCovered));
		facet.checkIntensity(pointIntensity);
		
		return pointIntensity.applyIntensities(pointColour);
	}
	
	protected IntensityComponents getIntensities(double xVal, double yVal, LineEquation line)
	{
		double dx = xVal - line.getStart().x;
		double dy = yVal - line.getStart().y;
		double len = Math.sqrt((dx*dx)+(dy*dy));
		
		double percentLength = len / line.getLength();
		
		IntensityComponents startComponents = pointLight.get(line.getStart());
		IntensityComponents endComponents = pointLight.get(line.getEnd());
		
		IntensityComponents pointComponents = new IntensityComponents();
		
		pointComponents.setRed(startComponents.getRed() + ((endComponents.getRed() - startComponents.getRed()) * percentLength));
		pointComponents.setGreen(startComponents.getGreen() + ((endComponents.getGreen() - startComponents.getGreen()) * percentLength));
		pointComponents.setBlue(startComponents.getBlue() + ((endComponents.getBlue() - startComponents.getBlue()) * percentLength));
		
		return pointComponents;
	}
	
	@Override
	public void setLightsources(Set<ILightSource> ls) {
		this.ls = ls;
		
	}

	@Override
	public Set<ILightSource> getLightsources() {
		return ls;
	}

}
