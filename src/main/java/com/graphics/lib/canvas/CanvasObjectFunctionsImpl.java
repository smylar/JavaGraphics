package com.graphics.lib.canvas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.Translation;

/**
 * Extension functions for canvas objects, different objects may implement their own versions
 * 
 * @author paul.brandon
 *
 */
public class CanvasObjectFunctionsImpl {
	
	/**
	 * Check if hit sphere centred on centre point with diameter of largest dimension
	 * 
	 * @param obj	- Object to process
	 * @param start - Point vector originates from
	 * @param v		- The vector
	 * @return		- <code>True</code> if vector intersects sphere, <code>False</code> otherwise
	 */
	public boolean vectorIntersectsRoughly(ICanvasObject obj, Point start, Vector v)
	{
		double dCentre = start.distanceTo(obj.getCentre());
		double angle = Math.atan(getMaxExtent(obj)/dCentre);
		Vector vCentre = start.vectorToPoint(obj.getCentre()).getUnitVector();
		
		return Math.acos(v.getUnitVector().dotProduct(vCentre)) < angle; 
	}
	
	/**
	 * 
	 * @param target
	 * @param startPoint
	 * @param projSpeed
	 * @return Pair of the Vector to take and target point
	 */
	public Pair<Vector, Point> plotDeflectionShot(ICanvasObject target, Point startPoint, double projSpeed){
		//deflection shot based on constant speeds (no acceleration)
		//The following is based on the Law of Cosines: A*A + B*B - 2*A*B*cos(theta) = C*C
		//A is distance from shot start point to target 
		//B is distance travelled by target until impact (speed * time)
		//C is distance travelled by projectile until impact (speed * time)
		//cos(theta) is also the dot product of the vectors start -> target position and the targets movement vector
		
	    Pair<Vector, Point> defaultVector = Pair.of(startPoint.vectorToPoint(target.getCentre()).getUnitVector(), new Point(target.getCentre()));
		
		List<MovementTransform> mTrans = target.getTransformsOfType(MovementTransform.class);
		if (mTrans.isEmpty()) 
		    return defaultVector;
		
		
		Vector targetVec = mTrans.stream().map(transform -> new Vector(transform.getVector().x() * transform.getSpeed(),
			  	   													   transform.getVector().y() * transform.getSpeed(),
		  	   														   transform.getVector().z() * transform.getSpeed()))
										  .reduce(Vector.ZERO_VECTOR, (left, right) -> left.combine(right));
		
		double speed = targetVec.getSpeed();
		targetVec = targetVec.getUnitVector();
		
		double cosTheta = target.getCentre().vectorToPoint(startPoint).getUnitVector().dotProduct(targetVec);
		double distStartToTarget = startPoint.distanceTo(target.getCentre());
		double time;
		
		if (projSpeed - speed == 0){
			//target and projectile will travel the same distance, therefore C = B and equation boils down to A / (2 * cos(theta)) = B 
			if (cosTheta > 0){
				time = (distStartToTarget / (2 * cosTheta)) / speed;
			}else{
				return defaultVector;
			}
		}
		else{
			// via many substitutions I won't get into here we end up with the quadratic equation  a*t^2 + b*t + c = 0  or t = [ -b +- Sqrt( b^2 - 4*a*c ) ] / (2*a) by completing the square
			//where
			// a = proj speed ^ 2 - target speed ^ 2
			// b = 2A * target speed * cos(theta)
			// c = -A^2
			
			double a = Math.pow(projSpeed,2) - Math.pow(speed,2);
			double b = 2 * distStartToTarget * speed * cosTheta;
			double c = -Math.pow(distStartToTarget,2);
			double discriminant = Math.pow(b,2) - (4 * a * c); 
			
			if (discriminant < 0) 
			    return defaultVector;
			
			double sqrtDisc = Math.sqrt(discriminant);
			double result1 = (-b + sqrtDisc) / (2*a);
			double result2 = (-b - sqrtDisc) / (2*a);
			
			time = Math.min(result1, result2);
			if (time < 0){
				time = Math.max(result1, result2);
			}
			
			if (time < 0) 
			    return defaultVector;
		}
		
		//finalProjectilePosition = finalTargetPosition so:
		// Start + (Vector of Proj * time) = TargetPos +  (Vector of target * time), which becomes
		//Vector of Proj = Vector of target + [(TargetPos - Start) / time ]
		return Pair.of(new Vector (
            				(targetVec.x() * speed) + ((target.getCentre().x - startPoint.x) / time),
            				(targetVec.y() * speed) + ((target.getCentre().y - startPoint.y) / time),
            				(targetVec.z() * speed) + ((target.getCentre().z - startPoint.z) / time)
            				).getUnitVector()
    		        ,new Point(
    		                target.getCentre().x + (targetVec.x() * time),
    		                target.getCentre().y + (targetVec.y() * time),
    		                target.getCentre().z + (targetVec.z() * time)
    		               )
    		        );
		
	}
	
	public void moveTo(ICanvasObject obj, Point p){
		Point anchor = obj.getAnchorPoint();
		obj.applyTransform(new Translation(p.x - anchor.x, p.y - anchor.y, p.z - anchor.z));
	}
	

	/**
	 * Get the facet that a vector from a given point intersects.
	 * 
	 * @param start	- Start point of the vector
	 * @param v		- The vector
	 * @param getAny	- True if any intersected facet should be returned, false if the nearest should be returned
	 * @return		- The intersected facet or null if none is intersected
	 */
	public Facet getIntersectedFacet(ICanvasObject obj, Point start, Vector v, boolean getAny)
	{
		//get quick estimate for those that are nowhere near - check if hit sphere centred on centre point with diameter of largest dimension	
		if (!this.vectorIntersectsRoughly(obj, start, v)) 
		    return null;
		
		Facet closest = null;
		double closestDist = 0;
		//the brute force generalised approach, check each front facing (to point) facet for intersection - will be horrible for something with a high facet count
		for (Facet f : obj.getFacetList().parallelStream().filter(GeneralPredicates.isFrontface(start)).toList())
		{
			if (f.isPointWithin(f.getIntersectionPointWithFacetPlane(start, v))){
				if (getAny) 
				    return f;
			
				double dist = f.getAsList().stream().mapToDouble(start::distanceTo).average().getAsDouble(); 
				if (closest == null || dist < closestDist){
					closest = f;
					closestDist = dist;
				}
			}
		}
		return closest;
	}
	
	/**
	 * Get map of all intersected facets that a vector from a given point intersects, and the point on the facet at which it intersects.
	 * 
	 * @param start	- Start point of the vector
	 * @param v		- The vector
	 * @return		- List of intersected facets
	 */
	public Map<Facet, Point> getIntersectedFacets(ICanvasObject obj, Point start, Vector v)
	{
		Map<Facet,Point> list = new HashMap<>();

		if (!this.vectorIntersectsRoughly(obj, start, v)) 
		    return list;
		
		for (Facet f : obj.getFacetList())
		{
			Point intersect = f.getIntersectionPointWithFacetPlane(start, v, true);
			if (f.isPointWithin(intersect)){
				list.put(f, intersect);
			}
		}
		return list;
	}
	
	/**
	 * Check if a vector intersects this object
	 * 
	 * @param start - Point vector originates from
	 * @param v		- The vector
	 * @return		- <code>True</code> if vector intersects sphere, <code>False</code> otherwise
	 */
	public boolean vectorIntersects(ICanvasObject obj, Point start, Vector v){
		return this.getIntersectedFacet(obj, start, v, true) != null;
	}
	
	/**
	 * Tests whether a point is inside this object
	 * 
	 * @param p - Point to test
	 * @return <code>True</code> if point is inside object, <code>False</code> otherwise
	 */
	public boolean isPointInside(ICanvasObject obj, Point p)
	{
		//should do for most simple objects - can override it for something shape specific - e.g. it doesn't work for the whale, and shapes such as spheres can can work this out much quicker
		//checks if all facets appears as backfaces to the tested point
		
		return obj.getFacetList().parallelStream().allMatch(f -> {
			Vector vecPointToFacet = p.vectorToPoint(f.getAsList().get(0)).getUnitVector();
			return Math.toDegrees(Math.acos(vecPointToFacet.dotProduct(f.getNormal()))) < 90;
		});

		//Sub shapes???
	}
	
	/**
	 * Gets the maximum distance from the centre of an object to a vertex
	 *	<br/>
	 * This general implementation will do for most shapes, though specific shapes may want to override to improve speed
	 * 
	 * @return
	 */
	public double getMaxExtent(ICanvasObject obj)
	{
		Point centre = obj.getCentre();
		return obj.getVertexList().stream().filter(GeneralPredicates.untagged()).map(v -> v.distanceTo(centre))
				.reduce(0d, (a,b) -> b > a ? b : a); //playing with reduce, could equally just use max() instead of reduce
	}
	
	public void addTransformAboutCentre(ICanvasObject obj, Transform...t)
	{
		this.addTransformAboutPoint(obj, obj::getCentre, t);
	}
	
	public void addTransformAboutPoint(ICanvasObject obj, Point p, Transform...transform)
	{
		this.addTransformAboutPoint(obj, () -> p, transform);
	}
	
	/**
	 * Add a transform sequence where the given point is moved to the origin, the required transforms applied, and then moved back to the original point
	 * 
	 * @param pFinder - Anonymous method that generates the point to transform about
	 * @param transform - List of transforms to apply
	 */
	public void addTransformAboutPoint(ICanvasObject obj, IPointFinder pFinder, Transform...transform)
	{
		Translation temp = new Translation(){
			@Override
			public void beforeTransform(){
				Point p = pFinder.find();
				setTransX(-p.x);
				setTransY(-p.y);
				setTransZ(-p.z);
			}
		};
		
		Transform temp2 = new Translation(){
			@Override
			public void beforeTransform(){
			    setTransX(-temp.getTransX());
			    setTransY(-temp.getTransY());
			    setTransZ(-temp.getTransZ());
			}
		};
		obj.addTransform(temp);
		for (Transform t : transform)
		{
			temp2.addDependency(t);
			temp.addDependency(t);
			obj.addTransform(t);
		}
		obj.addTransform(temp2);
	}
}
