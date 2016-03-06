package com.graphics.shapes;

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
		if (360 % arcProgression != 0) arcProgression = 18;
		int points = 360/arcProgression;
		
		for (int i = 0 ; i < points ; i++){
			double angle = Math.toRadians(i*arcProgression);
			double x = radius - (radius*Math.sin(angle));
			double z = radius - (radius*Math.cos(angle));
			
			this.getVertexList().add(new WorldCoord(x,0,z));
			this.getVertexList().add(new WorldCoord(x,height,z));
			if (i > 0){
				int last = this.getVertexList().size() - 1;
				this.getFacetList().add(new Facet(this.getVertexList().get(last - 2), this.getVertexList().get(last - 1), this.getVertexList().get(last)));
				this.getFacetList().add(new Facet(this.getVertexList().get(last - 2), this.getVertexList().get(last - 3), this.getVertexList().get(last - 1)));
			}
		}
		
		int last = this.getVertexList().size() - 1;
		this.getFacetList().add(new Facet(this.getVertexList().get(last), this.getVertexList().get(0), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(last), this.getVertexList().get(last - 1), this.getVertexList().get(0)));
		
		Point centre = this.getCentre();
		this.addTransform(new Translation(-centre.x, 0, -centre.z));
		this.applyTransforms();
	}

}
