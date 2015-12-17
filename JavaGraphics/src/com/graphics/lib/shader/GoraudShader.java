package com.graphics.lib.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.zbuffer.ScanLine;

public class GoraudShader implements IShader{

	private Color colour;
	private Facet facet;
	private ScanLine curScanline;
	private IntensityComponents startIntensity;
	private IntensityComponents endIntensity;
	private Point startTexture;
	private Point endTexture;
	private double lineLength = 0;
	private Set<LightSource> ls = new HashSet<LightSource>();
	private Map<Point, IntensityComponents> pointLight = new HashMap<Point, IntensityComponents>();
	
	@Override
	public void init(CanvasObject parent, Facet facet) {
		colour = facet.getColour() == null ? parent.getColour() : facet.getColour();
		if (colour == null) colour = new Color(255,255,255);
		
		if (parent.hasFlag(Events.NO_SHADE)) return;
		
		List<WorldCoord> points = new ArrayList<WorldCoord>();
		points.add(facet.point1);
		points.add(facet.point2);
		points.add(facet.point3);
		for (WorldCoord p : points)
		{
			Vector n = parent.getVertexNormalFinder().getVertexNormal(parent, p, facet);
			pointLight.put(p.getTransformed(), parent.getLightIntensityFinder().getLightIntensity(ls, parent, p, n, !facet.isFrontFace()));
		}

		this.facet = facet;
		curScanline = null;
		startIntensity = null;
		endIntensity = null;
		startTexture = null;
		endTexture = null;
	}

	@Override
	public Color getColour(ScanLine scanLine, int x, int y, double z) {
		if (facet == null || scanLine == null) return colour;
		
		if (scanLine != this.curScanline){
			startIntensity = this.getIntensities(x, scanLine.startY, scanLine.startLine);
			endIntensity = this.getIntensities(x, scanLine.endY, scanLine.endLine);
			if (facet.getTexture() != null){
				startTexture = this.getTexturePosition(x, scanLine.startY, scanLine.startLine, z);
				endTexture = this.getTexturePosition(x, scanLine.endY, scanLine.endLine, z);
			}
			curScanline = scanLine;
			lineLength = Math.ceil(scanLine.endY) - Math.floor(scanLine.startY);
		}
		
		if (lineLength == 0) return colour;

		double percentDistCovered = (y - Math.floor(scanLine.startY)) / lineLength;
		
		if (facet.getTexture() != null){
	
			int tx = (int)Math.round(startTexture.x + ((endTexture.x - startTexture.x) * percentDistCovered));
			int ty = (int)Math.round(startTexture.y + ((endTexture.y - startTexture.y) * percentDistCovered));
			Color c = facet.getTexture().getColour(tx, ty);
			if (c != null) return c;
		}
		
		double redIntensity = startIntensity.getRed() + ((endIntensity.getRed() - startIntensity.getRed()) * percentDistCovered);
		if (redIntensity < facet.getBaseIntensity()) redIntensity = facet.getBaseIntensity();
		double greenIntensity = startIntensity.getGreen() + ((endIntensity.getGreen() - startIntensity.getGreen()) * percentDistCovered);
		if (greenIntensity < facet.getBaseIntensity()) greenIntensity = facet.getBaseIntensity();
		double blueIntensity = startIntensity.getBlue() + ((endIntensity.getBlue() - startIntensity.getBlue()) * percentDistCovered);
		if (blueIntensity < facet.getBaseIntensity()) blueIntensity = facet.getBaseIntensity();
		
		return new Color((int)((double)colour.getRed() * redIntensity), 
				(int)((double)colour.getGreen() * greenIntensity), 
				(int)((double)colour.getBlue() * blueIntensity),
				colour.getAlpha());
	}
	
	private IntensityComponents getIntensities(double xVal, double yVal, LineEquation line)
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
	
	private Point getTexturePosition(double xVal, double yVal, LineEquation line, double z)
	{
		double dx = xVal - line.getStart().x;
		double dy = yVal - line.getStart().y;
		double len = Math.sqrt((dx*dx)+(dy*dy));
		
		double percentLength = len / line.getLength();
		
		double x = line.getWorldStart().getTextureX() + ((line.getWorldEnd().getTextureX() - line.getWorldStart().getTextureX() ) * percentLength);
		double y = line.getWorldStart().getTextureY() + ((line.getWorldEnd().getTextureY() - line.getWorldStart().getTextureY() ) * percentLength);
	
		return new Point (x, y, 0);
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
