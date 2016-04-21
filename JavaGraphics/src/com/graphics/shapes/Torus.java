package com.graphics.shapes;

import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.transform.YRotation;

public class Torus extends CanvasObject{
	private int points = 0;
	private WorldCoord centre;

	public Torus (double tubeRadius, double holeRadius)
	{
		this(tubeRadius, holeRadius, 18);
	}
	
	public Torus (double tubeRadius, double holeRadius, int arcProgression)
	{
		List<Point> circle = new ArrayList<Point>();
		if (360 % arcProgression != 0) arcProgression = 18;
		int points = 360/arcProgression;
		Point circleCentre = new Point(tubeRadius,holeRadius+(tubeRadius*2),tubeRadius);
		
		//make an initial circle 2d circle in x-z plane (y doesn't change)
		for (int i = 0 ; i < points ; i++){
			double angle = Math.toRadians(i*arcProgression);
			double x = tubeRadius - (tubeRadius*Math.sin(angle));
			double z = tubeRadius - (tubeRadius*Math.cos(angle));
			circle.add(new Point(x, holeRadius+(tubeRadius*2),z));
		}
		//note, this is a clockwise circle
		//now move circle in a circle in y plane around centre point of torus
		
		List<WorldCoord> circleCentres = new ArrayList<WorldCoord>();
		for (int i = 0 ; i < points ; i++){
			double angle = Math.toRadians(i*arcProgression);
			for (Point p : circle)
			{
				double radiustopoint = (holeRadius+(tubeRadius*2)) - p.z;
				double y = radiustopoint*Math.sin(angle);
				double z = radiustopoint - (radiustopoint*Math.cos(angle));
				this.getVertexList().add(new WorldCoord(p.x, p.y-y, p.z+z));
			}
			
			//circle centre points - used for easier shading
			double radiustopoint = holeRadius + tubeRadius;
			double y = radiustopoint*Math.sin(angle);
			double z = radiustopoint - (radiustopoint*Math.cos(angle));
			circleCentres.add(new WorldCoord(circleCentre.x,circleCentre.y - y, circleCentre.z + z));			
		}
		this.getVertexList().addAll(circleCentres);
		
		//join circles together (working out the triangles)
		for (int i = 0 ; i < points-1 ; i++){
		
			int a = i*points;
			int b = (points*i)+1;
			int c = points*(i+1);
			for (int j = 1 ; j < (points*2)-1 ; j++){
				if (j%2 == 0){
					a++;
					b += points;
				}else if (j>1){
					c++;
					b -= (points-1);
				}
				this.getFacetList().add(new Facet(this.getVertexList().get(c), this.getVertexList().get(b), this.getVertexList().get(a)));
			}
			//tie last points in circle to first points
			this.getFacetList().add(new Facet(this.getVertexList().get(points*(i+2)-1), this.getVertexList().get(points*i), this.getVertexList().get((points*(i+1))-1)));
			this.getFacetList().add(new Facet(this.getVertexList().get(points*(i+2)-1), this.getVertexList().get(points*(i+1)), this.getVertexList().get(points*i)));
		
		}
		//tie last circle to first
		int a = points -1;
		int b = (points*points) - 2;
		int c = (points*points) - 1;
		for (int j = 1 ; j < (points*2)-1 ; j++){
			if (j%2 == 0){
				a--;
				c = points - (int)(j*0.5);
			}else if (j>1){
				b--;
				c = (points*points)-(int)((j+1)*0.5);
			}

			this.getFacetList().add(new Facet(this.getVertexList().get(c), this.getVertexList().get(b), this.getVertexList().get(a)));
		}

		int len = points*points;
		this.getFacetList().add(new Facet(this.getVertexList().get(points-1), this.getVertexList().get(0), this.getVertexList().get(len-1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(len-points), this.getVertexList().get(len-1)));
		
		this.addTransform(new Translation(-tubeRadius,-(holeRadius + tubeRadius*2),-(holeRadius + tubeRadius*2)));
		
		this.addTransform(new Rotation<YRotation>(YRotation.class, 90));
		this.applyTransforms();
		
		this.points = points;
		
		this.centre = new WorldCoord(0,0,0);
		this.getVertexList().add(centre);//centre point
	}
	
	public double getActualTubeRadius(){
		//scaling transforms may have been applied
		int index = points * points; //start of tube centre points
		WorldCoord tc = this.getVertexList().get(index);
		
		return tc.distanceTo(this.getVertexList().get(0));
	}
	
	public double getActualHoleRadius(){
		//scaling transforms may have been applied
		return this.centre.distanceTo(this.getVertexList().get(0)) - (getActualTubeRadius()*2);
	}
	
	public double getActualRadius(){
		//scaling transforms may have been applied
		return this.centre.distanceTo(this.getVertexList().get(0));
	}
	
	public Facet getHolePlane(){
		int startIndex = points * points;
		int incr = points / 3;
		return new Facet(this.getVertexList().get(startIndex), this.getVertexList().get(startIndex+incr), this.getVertexList().get(startIndex+incr+incr));
	}
	
	@Override
	public Point getCentre()
	{
		return this.centre;
	}
	
	@Override
	public IVertexNormalFinder getVertexNormalFinder()
	{
		return (obj, p, f) -> {
			if (!obj.getVertexList().contains(p)) return new Vector(0,0,-1);
			
			int index = (points * points) + Math.floorDiv(obj.getVertexList().indexOf(p), this.points);
			
			Point centre = obj.getVertexList().get(index);
			
			return centre.vectorToPoint(p).getUnitVector();
		};
	}
	
	@Override
	public boolean isPointInside(Point p)
	{
		/*for (int i = points*points ; i < this.getVertexList().size() - 1 ; i++)
		{
			//TODO this isn't quite right - this actually checks bubbles within the tube (centred on the tube centre points) and may not overlap enough
			if (this.getVertexList().get(i).distanceTo(p) < this.getActualTubeRadius()) return true;
		}
		
		return false;*/
		
		double distFromCentre = this.centre.distanceTo(p);
		if (distFromCentre > this.getActualRadius() || this.centre.distanceTo(p) < this.getActualHoleRadius()) return false;
		
		double distFromHolePlane = this.getHolePlane().getDistanceFromFacetPlane(p);
		if (distFromHolePlane > this.getActualTubeRadius()) return false;
		
		//now just to check within the circular area of the tube
		double pX = Math.sqrt(Math.pow(distFromCentre, 2) - Math.pow(distFromHolePlane, 2));
		double circleX = this.getActualRadius() - this.getActualTubeRadius();
		//pY would be distFromHolePlane and circleY = 0
		
		return Math.pow(pX - circleX, 2) + Math.pow(distFromHolePlane, 2) < Math.pow(this.getActualTubeRadius(), 2);
	}
	
}
