package com.graphics.shapes;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

public class Ovoid extends CanvasObject{
	private double radius = 0;
	private int angleProgression = 0;
	
	public Ovoid(double radius, double radiusMod, int angleProgression)
	{
	    super(() -> init(radius, radiusMod, angleProgression), c -> c.fixCentre());
		this.angleProgression = checkAngleProgression(angleProgression);
		this.radius = radius;
	}
	
	public double getRadius() {
		return radius;
	}

	public int getPointsPerCircle() {
		return 360 / this.angleProgression;
	}

	public int getAngleProgression() {
		return angleProgression;
	}
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(double radius, double radiusMod, int angleProgression) {
		
		List<WorldCoord> vertexList = Lists.newArrayList();
		ImmutableList.Builder<Facet> facets = ImmutableList.builder();
		
		angleProgression = checkAngleProgression(angleProgression);
		
		int points = 360 / angleProgression;
		int pointsarc = 180 / angleProgression;
		
		for (int i = 0 ; i < pointsarc-1 ; i++){
			double circleRad = Math.sin(Math.toRadians((i+1)*angleProgression)) * radius;
			if (circleRad < 0) circleRad = circleRad * -1 ;
			
			double y = Math.cos(Math.toRadians((i+1) * angleProgression)) * radius;
			
			for (int j = 0 ; j < points ; j++){
				double x = circleRad * Math.sin(Math.toRadians(j * angleProgression));
				double z = circleRad * Math.cos(Math.toRadians(j * angleProgression));
				vertexList.add(new WorldCoord(x * radiusMod, y, z * radiusMod));
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
				facets.add(new Facet(vertexList.get(c), vertexList.get(b), vertexList.get(a)));
			}
			//tie last points in circle to first points
			facets.add(new Facet(vertexList.get(points*(i+2)-1), vertexList.get(points*i), vertexList.get((points*(i+1))-1)));
			facets.add(new Facet(vertexList.get(points*(i+2)-1), vertexList.get(points*(i+1)), vertexList.get(points*i)));
		
		}
		//add top and bottom
		vertexList.add(new WorldCoord(0,radius,0));
		vertexList.add(new WorldCoord(0,-radius,0));
		
		for (int i = 0 ; i < points ; i++){
			int addr = vertexList.size() -2;
			int j = i+1;
			if (i == points-1) j = 0;
			
			facets.add(new Facet(vertexList.get(addr), vertexList.get(i), vertexList.get(j)));
			facets.add(new Facet(vertexList.get(++addr), vertexList.get(j+(points*(pointsarc-2))), vertexList.get(i+(points*(pointsarc-2)))));
		}
		
		return Pair.of(ImmutableList.copyOf(vertexList), facets.build());
	}
	
	private static int checkAngleProgression(int angleProgression) {
		return 360 % angleProgression == 0 ? angleProgression : 10;
	}
}
