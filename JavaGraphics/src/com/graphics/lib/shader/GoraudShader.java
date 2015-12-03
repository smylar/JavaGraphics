package com.graphics.lib.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.ScanLine;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.lightsource.LightSource;

public class GoraudShader implements IShader{

	private Color colour;
	private Facet facet;
	private ScanLine curScanline;
	private IntensityComponents startIntensity;
	private IntensityComponents endIntensity;
	private List<LightSource> ls = new ArrayList<LightSource>();
	
	@Override
	public void init(CanvasObject parent, Facet facet) {
		colour = facet.getColour() == null ? parent.getColour() : facet.getColour();
		
		List<WorldCoord> points = new ArrayList<WorldCoord>();
		points.add(facet.point1);
		points.add(facet.point2);
		points.add(facet.point3);
		for (Point p : points)
		{
			p.setNormal(parent.getVertexNormalFinder(), facet, parent);
			p.setLightIntensity(parent.getLightIntensityFinder(ls), !facet.isFrontFace(), parent);
		}
		this.facet = facet;
		curScanline = null;
		startIntensity = null;
		endIntensity = null;
	}

	@Override
	public Color getColour(ScanLine scanLine, int x, int y, double percentDistCovered) {
		if (facet == null || scanLine == null) return new Color(255,255,255);
		
		if (scanLine != this.curScanline){
			startIntensity = this.getIntensities(x, scanLine.startY, scanLine.startLine);
			endIntensity = this.getIntensities(x, scanLine.endY, scanLine.endLine);
			curScanline = scanLine;
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
		IntensityComponents startComponents = line.getStart().getLightIntensity();
		IntensityComponents endComponents = line.getEnd().getLightIntensity();
		
		IntensityComponents pointComponents = new IntensityComponents();
		
		pointComponents.setRed(startComponents.getRed() + ((endComponents.getRed() - startComponents.getRed()) * percentLength));
		pointComponents.setGreen(startComponents.getGreen() + ((endComponents.getGreen() - startComponents.getGreen()) * percentLength));
		pointComponents.setBlue(startComponents.getBlue() + ((endComponents.getBlue() - startComponents.getBlue()) * percentLength));
		
		return pointComponents;
	}
	
	@Override
	public void setLightsources(List<LightSource> ls) {
		this.ls = ls;
		
	}

}
