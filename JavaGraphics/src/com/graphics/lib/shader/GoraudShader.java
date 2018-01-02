package com.graphics.lib.shader;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader that works out the colour at each vertex and then interpolates the colours for the pixels in between
 * 
 * @author Paul Brandon
 *
 */
public class GoraudShader extends DefaultShader {
    protected static final Color DEFAULT = new Color(255,255,255);
    
	protected Color colour;
	protected Facet facet;
	protected ScanLine curScanline;
	protected IntensityComponents startIntensity;
	protected IntensityComponents endIntensity;
	
	protected double lineLength = 0;
	protected Map<Point, IntensityComponents> pointLight = new HashMap<>();
	
	@Override
	public void init(ICanvasObject parent, Facet facet, Camera c) {
	    pointLight.clear();
	    lineLength = 0;
	    this.facet = facet;
		curScanline = null;
		startIntensity = null;
		endIntensity = null;
	    
		colour = facet.getColour() == null ? parent.getColour() : facet.getColour();
		if (colour == null) {
		    colour = DEFAULT;
		}
		
		if (parent.hasFlag(Events.NO_SHADE)) {
			this.facet = null;
		    return;
		}

		for (WorldCoord p : facet.getAsList())
		{
			Vector n = parent.getVertexNormalFinder().getVertexNormal(parent, p, facet);
			pointLight.put(p.getTransformed(c), parent.getLightIntensityFinder().getLightIntensity(Canvas3D.get().getLightSources(), parent, p, n, !facet.isFrontFace()));
		}
	}

	@Override
	public Color getColour(ScanLine scanLine, int x, int y) {
		if (facet == null || scanLine == null) 
		    return colour;
		
		if (scanLine != this.curScanline){
			startIntensity = this.getIntensities(x, scanLine.startY, scanLine.startLine);
			endIntensity = this.getIntensities(x, scanLine.endY, scanLine.endLine);
			curScanline = scanLine;
			lineLength = Math.ceil(scanLine.endY) - Math.floor(scanLine.startY);
		}
		
		if (lineLength <= 0) 
		    return colour;

		double percentDistCovered = (y - Math.floor(scanLine.startY)) / lineLength;
		
		//TODO something is off, mesh lines appear brighter
		
		return generateColour(colour, percentDistCovered);
	}
	
	protected IntensityComponents getIntensities(double xVal, double yVal, LineEquation line)
	{
		double dx = xVal - line.getStart().x;
		double dy = yVal - line.getStart().y;
		double len = Math.sqrt((dx*dx)+(dy*dy));
		
		double percentLength = len / line.getLength();
		
		IntensityComponents startComponents = pointLight.get(line.getStart());
		IntensityComponents endComponents = pointLight.get(line.getEnd());
		
		return interpolateIntensityComponent(startComponents, endComponents, percentLength);
	}
	
	protected IntensityComponents interpolateIntensityComponent(IntensityComponents start, IntensityComponents end, double percentFromStart) {
	    IntensityComponents iComponents = new IntensityComponents();
	    IntensityComponents.forEach(comp -> {
	        double val = start.get(comp) + ((end.get(comp) - start.get(comp)) * percentFromStart);
	        iComponents.set(comp, val);
	    });
	    return iComponents;
	}
	
	protected Color generateColour(final Color colour, final double percentDistCovered) {
	    return Optional.of(interpolateIntensityComponent(startIntensity, endIntensity, percentDistCovered))
                .map(facet::checkIntensity)
                .map(i -> i.apply(colour))
                .orElse(DEFAULT);
	}

}
