package com.graphics.shapes;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;

public class Ovoid extends CanvasObject{
	private double radius = 0;
	private int pointsPerCircle = 0;
	
	public Ovoid(double radius, double radiusMod, int angleProgression)
	{
		this.radius = radius;
		if (360 % angleProgression != 0) angleProgression = 10;
		
		int points = 360 / angleProgression;
		int pointsarc = 180 / angleProgression;
		this.pointsPerCircle = points;
		
		for (int i = 0 ; i < pointsarc-1 ; i++){
			double circleRad = Math.sin(Math.toRadians((i+1)*angleProgression)) * radius;
			if (circleRad < 0) circleRad = circleRad * -1 ;
			
			double y = Math.cos(Math.toRadians((i+1) * angleProgression)) * radius;
			
			for (int j = 0 ; j < points ; j++){
				double x = circleRad * Math.sin(Math.toRadians(j * angleProgression));
				double z = circleRad * Math.cos(Math.toRadians(j * angleProgression));
				this.getVertexList().add(new WorldCoord(x * radiusMod, y, z * radiusMod));
			}
			
		}
		
		for (int i = 0 ; i < pointsarc-2 ; i++){
		
			int a = i * points;
			int b = (points * i) + 1;
			int c = points * (i + 1);
			for (int j = 1 ; j < (points*2)-1 ; j++){
				if (j % 2 == 0){
					a++;
					b+=points;
				}else if (j > 1){
					c++;
					b-=(points-1);
				}
				this.getFacetList().add(new Facet(this.getVertexList().get(c), this.getVertexList().get(b), this.getVertexList().get(a)));
			}
			//tie last points in circle to first points
			this.getFacetList().add(new Facet(this.getVertexList().get(points*(i+2)-1), this.getVertexList().get(points*i), this.getVertexList().get((points*(i+1))-1)));
			this.getFacetList().add(new Facet(this.getVertexList().get(points*(i+2)-1), this.getVertexList().get(points*(i+1)), this.getVertexList().get(points*i)));
		
		}
		//add top and bottom
		this.getVertexList().add(new WorldCoord(0,radius,0));
		this.getVertexList().add(new WorldCoord(0,-radius,0));
		
		for (int i = 0 ; i < points ; i++){
			int addr = this.getVertexList().size() -2;
			int j = i+1;
			if (i == points-1) j = 0;
			
			this.getFacetList().add(new Facet(this.getVertexList().get(addr), this.getVertexList().get(i), this.getVertexList().get(j)));
			this.getFacetList().add(new Facet(this.getVertexList().get(++addr), this.getVertexList().get(j+(points*(pointsarc-2))), this.getVertexList().get(i+(points*(pointsarc-2)))));
		}

		this.getVertexList().add(new WorldCoord(0,0,0));
		//this.UseAveragedNormals(90); tested as working when not using the CENTRE_TO_POINT vertex finder which is more efficient for this shape
	}
	
	public double getRadius() {
		return radius;
	}

	public int getPointsPerCircle() {
		return pointsPerCircle;
	}

	@Override
	public Point getCentre()
	{
		return this.getVertexList().get(this.getVertexList().size() - 1);
	}
	
	@Override
	public boolean isPointInside(Point p)
	{
		return this.getCentre().distanceTo(p) < this.radius;
	}
}
