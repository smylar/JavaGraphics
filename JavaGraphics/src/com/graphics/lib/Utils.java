package com.graphics.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.ILightIntensityFinder;

/**
 * Random orphaned utilities that need a proper home
 * 
 * @author paul
 *
 */
public class Utils {	
	public static ILightIntensityFinder getShadowLightIntensityFinder(ICanvasObjectList objectsToCheck)
	{
		//EXPERIMENTAL
		return (ls, obj, p, v, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				Set<ICanvasObject> checkList = new HashSet<>(objectsToCheck.get());
				for(ICanvasObject checkObj : checkList){
					if (checkObj == obj || !checkObj.getCastsShadow() || !checkObj.isVisible()) continue;
					Vector v1 = l.getPosition().vectorToPoint(checkObj.getCentre()).getUnitVector();
					Vector v2 = p.vectorToPoint(checkObj.getCentre()).getUnitVector();
					if (v1.dotProduct(v2) >= 0) continue; //not pointing towards each other so test object not between lightsource and obj
					
					if (FunctionHandler.getFunctions(checkObj).vectorIntersects(checkObj, l.getPosition(), lightVector)) return; //an object is blocking light

				}
				
				double percent = 0;
				
				double answer = v.dotProduct(lightVector);
				
				double deg = Math.toDegrees(Math.acos(answer));
				
				if (deg > 90 && !bf)
				{			
					percent = (deg-90) / 90;
				}
				else if (bf)
				{
					//light on the rear of the facet for if we are processing backfaces - which implies the rear may be visible
					percent = (90-deg) / 90;
				}
				
				double intensity = intComps.getRed() * percent;
				if (intensity > maxIntensity.getRed()) maxIntensity.setRed(intensity);
				intensity = intComps.getGreen() * percent;
				if (intensity > maxIntensity.getGreen()) maxIntensity.setGreen(intensity);
				intensity = intComps.getBlue() * percent;
				if (intensity > maxIntensity.getBlue()) maxIntensity.setBlue(intensity);
			});
			return maxIntensity;
		};
	}
	
	public static BiConsumer<Canvas3D, Graphics> drawCircle(int x, int y, int radius, Color startColour, Color endColour){
		//drawing a circle without using drawCircle, just because :)
		return (c,g) ->{
			double rs = Math.pow(radius,2);
			int redDiff = endColour.getRed() - startColour.getRed();
			int blueDiff = endColour.getBlue() - startColour.getBlue();
			int greenDiff = endColour.getGreen() - startColour.getGreen();
			int alphaDiff = endColour.getAlpha() - startColour.getAlpha();
			for(int i = 0 ; i <= radius ; i++)
			{
				int length = (int)Math.round(Math.sqrt(rs - Math.pow(i,2))) ;
				for (int j = 0 ; j <= length ; j++ ){
					double pc = Math.sqrt(Math.pow(j, 2) + Math.pow(i, 2)) / (double)radius;
					if (pc > 1) pc = 1;
					int cRed = (int)Math.floor(startColour.getRed() + ((double)redDiff * pc));
					int cGreen = (int)Math.floor(startColour.getGreen() + ((double)greenDiff * pc));
					int cBlue = (int)Math.floor(startColour.getBlue() + ((double)blueDiff * pc));
					int cAlpha = (int)Math.floor(startColour.getAlpha() + ((double)alphaDiff * pc));
			
					g.setColor(new Color(cRed, cGreen, cBlue, cAlpha));
			
					g.drawLine(x+j, y-i, x+j, y-i); 
					g.drawLine(x+j, y+i, x+j, y+i);
					g.drawLine(x-j, y-i, x-j, y-i); 
					g.drawLine(x-j, y+i, x-j, y+i); 
					//this will form a crosshair when using different alpha values, as we'll be drawing twice in some cases (unless we check for j = 0 etc)
					//is a nice effect so keeping for now
				}
			}
		};
	}
	
	/**
	 * Generate a point based on the distance to 2 known points
	 * (intersecting circles problem) currently applies 2D only
	 * 
	 * @param p1 First known point
	 * @param p2 Second known point
	 * @param r1 Distance to first known point (radius of first circle)
	 * @param r2 Distance to second known point (radius of first circle)
	 * @return
	 */
	public static Optional<Pair<Point, Point>> getPointFromKnownPoints(Point p1, Point p2, double r1, double r2) {
		double dist = p1.distanceTo(p2);
		
		if (r1 + r2 < dist || dist == 0) {
			//cannot overlap
			return Optional.empty();
		}
		
		if (r1 + r2 == dist) {
			//may need some tolerance
			//circles touch at one point
			Vector v = p1.vectorToPoint(p2).getUnitVector();
			Point p3 = new Point(p1.x + (v.x*r1) , p1.y + (v.y*r1), 0);
			return Optional.of(new Pair<>(p3, p3));
		}
		
		double a = (r1*r1 - r2*r2 + dist*dist)/(2*dist);
		double h = Math.sqrt(r1*r1 - a*a);
		double p3x = p1.x + (p2.x - p1.x)*(a/dist);
		double p3y = p1.y + (p2.y - p1.y)*(a/dist);
		Point p3 = new Point(p3x, p3y, 0);
		
		return Optional.of(new Pair<>(
				new Point(p3.x + h*(p2.y - p1.y)/dist, p3.y - h*(p2.x - p1.x)/dist, 0),
				new Point(p3.x - h*(p2.y - p1.y)/dist, p3.y + h*(p2.x - p1.x)/dist, 0)
		));
	}

}
