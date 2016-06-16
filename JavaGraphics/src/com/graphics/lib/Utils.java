package com.graphics.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.zbuffer.ZBuffer;


public class Utils {
	//stuff in here should be separated to own factories

	public static enum VertexNormalFinderEnum{
		DEFAULT, CENTRE_TO_POINT
	}
	
	public static enum LightIntensityFinderEnum{
		DEFAULT
	}

	public static IZBuffer getDefaultZBuffer()
	{
		return new ZBuffer();
	}
	
	
	public static ILightIntensityFinder getLightIntensityFinder(LightIntensityFinderEnum type)
	{
		switch(type){
			case DEFAULT: return getDefaultLightIntensityFinder();
			default: return getDefaultLightIntensityFinder();
		}
	}
	
	private static ILightIntensityFinder getDefaultLightIntensityFinder()
	{
		return (ls, obj, p, v, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				double percent = 0;
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				
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
	
	public static ILightIntensityFinder getShadowLightIntensityFinder(ICanvasObjectList objectsToCheck)
	{
		return (ls, obj, p, v, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				Set<CanvasObject> checkList = new HashSet<CanvasObject>(objectsToCheck.get());
				for(CanvasObject checkObj : checkList){
					if (checkObj == obj || !checkObj.getCastsShadow() || !checkObj.isVisible()) continue;
					Vector v1 = l.getPosition().vectorToPoint(checkObj.getCentre()).getUnitVector();
					Vector v2 = p.vectorToPoint(checkObj.getCentre()).getUnitVector();
					if (v1.dotProduct(v2) >= 0) continue; //not pointing towards each other so test object not between lightsource and obj
					
					if (checkObj.vectorIntersects(l.getPosition(), lightVector)) return; //an object is blocking light

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
	
	public static IVertexNormalFinder getVertexNormalFinder(VertexNormalFinderEnum type)
	{
		switch(type){
			case DEFAULT: return getDefaultVertexNormalFinder();
			case CENTRE_TO_POINT: return getCentreToPointVertexNormalFinder();
			default: return getDefaultVertexNormalFinder();
		}
	}
	
	
	private static IVertexNormalFinder getDefaultVertexNormalFinder()
	{
		return (obj, p, f) -> {
			if (obj.getVertexFacetMap() != null){
				List<Facet> facetList = obj.getVertexFacetMap().get(p);
				if (facetList != null && facetList.size() > 0)
				{	
					Vector normal = new Vector(0,0,0);
					for(Facet facet : facetList){
						normal.addVector(facet.getNormal());
					}
					return normal.getUnitVector(); //could possibly store for reuse between transforms
				}
			}
			
			return f.getNormal();
		};
	}
	
	private static IVertexNormalFinder getCentreToPointVertexNormalFinder()
	{
		return (obj, p, f) -> {
			Point centre = obj.getCentre();
			return new Vector(p.x - centre.x, p.y - centre.y, p.z - centre.z).getUnitVector();
		};
	}
	
	public static Vector plotDeflectionShot(CanvasObject target, Point startPoint, double projSpeed){
		//deflection shot based on constant speeds (no acceleration)
		//The following is based on the Law of Cosines: A*A + B*B - 2*A*B*cos(theta) = C*C
		//A is distance from shot start point to target 
		//B is distance travelled by target until impact (speed * time)
		//C is distance travelled by projectile until impact (speed * time)
		//cos(theta) is also the dot product of the vectors start -> target position and the targets movement vector
		
		Vector defaultVector = startPoint.vectorToPoint(target.getCentre()).getUnitVector();
		
		List<MovementTransform> mTrans = target.getTransformsOfType(MovementTransform.class);
		if (mTrans.size() == 0) return defaultVector;
		Vector targetVec = new Vector(0,0,0);
		for (MovementTransform t : mTrans){
			targetVec.x += t.getVector().x * t.getSpeed();
			targetVec.y += t.getVector().y * t.getSpeed();
			targetVec.z += t.getVector().z * t.getSpeed();
		}
		double speed = targetVec.getSpeed();
		targetVec = targetVec.getUnitVector();
		
		double cosTheta = target.getCentre().vectorToPoint(startPoint).getUnitVector().dotProduct(targetVec);
		double distStartToTarget = startPoint.distanceTo(target.getCentre());
		double time = 0;
		
		if (projSpeed - speed == 0){
			//target and projectile will travel the same distance, therefore C = B and equation boils down to A / (2 * cos(theta)) = B 
			if (cosTheta > 0){
				time = (distStartToTarget / (2 * cosTheta)) / speed;
			}else{
				return defaultVector;
			}
		}
		else{
			// via many substitutions I won't get into here we end up with the quadratic equation  a*t^2 + b*t + c = 0  or t = [ -b ± Sqrt( b^2 - 4*a*c ) ] / (2*a) by completing the square
			//where
			// a = proj speed ^ 2 - target speed ^ 2
			// b = 2A * target speed * cos(theta)
			// c = -A^2
			
			double a = Math.pow(projSpeed,2) - Math.pow(speed,2);
			double b = 2 * distStartToTarget * speed * cosTheta;
			double c = -Math.pow(distStartToTarget,2);
			double discriminant = Math.pow(b,2) - (4 * a * c); 
			
			if (discriminant < 0) return defaultVector;
			
			double sqrtDisc = Math.sqrt(discriminant);
			double result1 = (-b + sqrtDisc) / (2*a);
			double result2 = (-b - sqrtDisc) / (2*a);
			
			time = Math.min(result1, result2);
			if (time < 0){
				time = Math.max(result1, result2);
			}
			
			if (time < 0) return defaultVector;
		}
		
		//finalProjectilePosition = finalTargetPosition so:
		// Start + (Vector of Proj * time) = TargetPos +  (Vector of target * time), which becomes
		//Vector of Proj = Vector of target + [(TargetPos - Start) / time ]
		return new Vector (
				(targetVec.x * speed) + ((target.getCentre().x - startPoint.x) / time),
				(targetVec.y * speed) + ((target.getCentre().y - startPoint.y) / time),
				(targetVec.z * speed) + ((target.getCentre().z - startPoint.z) / time)
				).getUnitVector();
		
	}
	
	public static void moveTo(CanvasObject obj, Point p){
		Point anchor = obj.getAnchorPoint();
		obj.applyTransform(new Translation(p.x - anchor.x, p.y - anchor.y, p.z - anchor.z));
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

}
