package com.graphics.shapes;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.transform.Translation;


public class Cylinder extends CanvasObject {
	
	public Cylinder(double radius, double height)
	{
		this(radius, height, 18);
	}
	
	public Cylinder(double radius, double height, int arcProgression)
	{
	    super(() -> init(radius, height, 360 % arcProgression != 0 ? 18 : arcProgression));
		//N.B. this is currently an open cylinder, i.e. ends are not closed like a sealed can
		//may like option to specify if an end is open or not	
		
		Point centre = this.getCentre();
		this.addTransform(new Translation(-centre.x, 0, -centre.z));
		this.applyTransforms();
	}
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(double radius, double height, int arcProgression) {
		
		List<WorldCoord> vertexList = Lists.newArrayList();
		ImmutableList.Builder<Facet> facets = ImmutableList.builder();
		
		int points = 360/arcProgression;
		
		for (int i = 0 ; i < points ; i++){
			double angle = Math.toRadians(i*arcProgression);
			double x = radius - (radius*Math.sin(angle));
			double z = radius - (radius*Math.cos(angle));
			
			vertexList.add(new WorldCoord(x,0,z));
			vertexList.add(new WorldCoord(x,height,z));
			if (i > 0){
				int last = vertexList.size() - 1;
				facets.add(new Facet(vertexList.get(last - 2), vertexList.get(last - 1), vertexList.get(last)));
				facets.add(new Facet(vertexList.get(last - 2), vertexList.get(last - 3), vertexList.get(last - 1)));
			}
		}
		
		int last = vertexList.size() - 1;
		facets.add(new Facet(vertexList.get(last), vertexList.get(0), vertexList.get(1)));
		facets.add(new Facet(vertexList.get(last), vertexList.get(last - 1), vertexList.get(0)));
		
		return Pair.of(ImmutableList.copyOf(vertexList), facets.build());
	}

}
