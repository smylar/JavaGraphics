package com.graphics.lib.shader;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.Facet;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.IntensityComponents.ColourComponent;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.util.TriangleAreaCalculator;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader that works out the colour at each vertex and then interpolates the colours for the pixels in between
 * 
 * @author Paul Brandon
 *
 */
public class GoraudShader extends DefaultScanlineShader {
    protected static final Color DEFAULT = new Color(255,255,255);
    
	protected Color colour;
	protected Facet facet;
	//protected ScanLine curScanline;
	//protected IntensityComponents startIntensity;
	//protected IntensityComponents endIntensity;
	
	//protected double lineLength = 0;
	protected Map<Point, IntensityComponents> pointLight = new HashMap<>();
	
	@Override
	public void init(ICanvasObject parent, Facet facet, Camera c) {
	    pointLight.clear();
	    //lineLength = 0;
	    this.facet = facet;
		//curScanline = null;
		//startIntensity = null;
		//endIntensity = null;
	    
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
			pointLight.put(p.getTransformed(c), parent.getLightIntensityFinder().getLightIntensity(Canvas3D.get().getLightSources(), parent, p, n, facet));
		}
	}

	@Override
	public Color getColour(ScanLine scanLine, int x, int y) {
		if (facet == null || scanLine == null) 
		    return colour;
		
//		if (scanLine != this.curScanline){
//			startIntensity = this.getIntensities(x, scanLine.getStartY(), scanLine.getStartLine());
//			endIntensity = this.getIntensities(x, scanLine.getEndY(), scanLine.getEndLine());
//			curScanline = scanLine;
//			lineLength = scanLine.getEndY() - scanLine.getStartY();
//		}
//
//		if (lineLength <= 0) 
//		    return colour;
//			
		
		//double percentDistCovered = (y - scanLine.getStartY()) / lineLength;
		
		//TODO something is off, mesh lines appear brighter
		//think this is actually to do with points on the edge not factoring the 3rd point with a normal being quite different
		//for how they have been done for curved surfaces
		//return generateColour(colour, percentDistCovered);
		
		if (scanLine.getEndY() - scanLine.getStartY() <= 0) 
            return colour;
		
		return generateColour(colour, new Point(x,y,0));
	}
	
//	protected IntensityComponents getIntensities(double xVal, double yVal, LineEquation line)
//	{
// 
//		//double dx = xVal - line.getStart().x;
//		//double dy = yVal - line.getStart().y;
//		//double len = Math.hypot(dx, dy);
//		
//		//double percentLength = len / line.getLength();
//	    
//	    
//	    //below is simpler but still hasn't changed much
//	    double len = Math.abs(xVal - line.getStart().x);
//	    
//	    double xlen = Math.abs(line.getEnd().x - line.getStart().x);
//	    double percentLength = 0;
//	    
//	    if (xlen > 0) {
//	        percentLength = len/xlen;
//	    }
//	    
//		
//		IntensityComponents startComponents = pointLight.get(line.getStart());
//		IntensityComponents endComponents = pointLight.get(line.getEnd());
//		
//		return interpolateIntensityComponent(startComponents, endComponents, percentLength);
//	}
	
//	protected IntensityComponents interpolateIntensityComponent(IntensityComponents start, IntensityComponents end, double percentFromStart) {
//	    IntensityComponents iComponents = new IntensityComponents();
//	    IntensityComponents.forEach(comp -> {
//	        double val = start.get(comp) + ((end.get(comp) - start.get(comp)) * percentFromStart);
//	        iComponents.set(comp, val);
//	    });
//	    return iComponents;
//	}
	
//	protected Color generateColour(final Color colour, final double percentDistCovered) {
//	    return Optional.of(interpolateIntensityComponent(startIntensity, endIntensity, percentDistCovered))
//                .map(facet::checkIntensity)
//                .map(i -> i.apply(colour))
//                .orElse(DEFAULT);
//	}
	
	protected Color generateColour(final Color colour, final Point p) {
	  //TEST cleanup if successful
        //calculates percentage of intensity to use from each point, 
        //based on what percentage of the facet area is covered by the triangle formed between
        //the 2 other points and the pixel to draw
        List<Point> points = List.copyOf(pointLight.keySet());
        List<Double> areas = List.of(
                TriangleAreaCalculator.getArea(p, points.get(1), points.get(2)),
                TriangleAreaCalculator.getArea(p, points.get(2), points.get(0)),
                TriangleAreaCalculator.getArea(p, points.get(0), points.get(1)));
        
        double total = areas.stream().reduce((a,b) -> a+b).orElseGet(() -> 0D);
        
        double red = 0;
        double green = 0;
        double blue = 0;
        
        for (int i = 0 ; i < areas.size() ; i++) {
            double pc = areas.get(i)/total;
            
            var intensity = pointLight.get(points.get(i));
            red += (intensity.get(ColourComponent.RED) * pc);
            green += (intensity.get(ColourComponent.GREEN) * pc);
            blue += (intensity.get(ColourComponent.BLUE) * pc);
        }
        
        var i = new IntensityComponents();
        i.set(ColourComponent.RED, red);
        i.set(ColourComponent.GREEN, green);
        i.set(ColourComponent.BLUE, blue);
        
        return i.apply(colour);
        //end test - pretty much as good as the other attempts!
    }

}
