package com.graphics.tests.shapes;

import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

public class Bird extends CanvasObject{
	public Bird(){
		this.getVertexList().add(new WorldCoord(0, 0, 0));
		this.getVertexList().add(new WorldCoord(0, 0, 60));
		
		//right wing
		this.getVertexList().add(new WorldCoord(50, 0, 10));
		this.getVertexList().add(new WorldCoord(50, 0, 50));
		this.getVertexList().add(new WorldCoord(100, 0, 30));
		
		//left wing
		this.getVertexList().add(new WorldCoord(-50, 0, 10));
		this.getVertexList().add(new WorldCoord(-50, 0, 50));
		this.getVertexList().add(new WorldCoord(-100, 0, 30));
		
		this.setProcessBackfaces(true);
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(2), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(3), this.getVertexList().get(2), this.getVertexList().get(4)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(5)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(6), this.getVertexList().get(5)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(6), this.getVertexList().get(7)));
	}
	
	@Override
	public double getMaxExtent()
	{
		return this.getCentre().distanceTo(this.getVertexList().get(4));
	}
}
