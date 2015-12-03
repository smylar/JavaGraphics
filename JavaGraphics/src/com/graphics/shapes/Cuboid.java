package com.graphics.shapes;


import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.transform.Translation;

public class Cuboid extends CanvasObject{
	
	public Cuboid (int height, int width, int depth)
	{
		this.getVertexList().add(new WorldCoord(0, 0, 0));
		this.getVertexList().add(new WorldCoord(0, height, 0));
		this.getVertexList().add(new WorldCoord(width, height, 0));
		this.getVertexList().add(new WorldCoord(width, 0, 0));
		this.getVertexList().add(new WorldCoord(0, 0, depth));
		this.getVertexList().add(new WorldCoord(0, height, depth));
		this.getVertexList().add(new WorldCoord(width, height, depth));
		this.getVertexList().add(new WorldCoord(width, 0, depth));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(2), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(3), this.getVertexList().get(7), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(4), this.getVertexList().get(5), this.getVertexList().get(0)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(1), this.getVertexList().get(0)));
		this.getFacetList().add(new Facet(this.getVertexList().get(7), this.getVertexList().get(6), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(6), this.getVertexList().get(5), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(3), this.getVertexList().get(2), this.getVertexList().get(7)));
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(6), this.getVertexList().get(7)));
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(1), this.getVertexList().get(6)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(5), this.getVertexList().get(6)));
		
		this.applyTransform(new Translation(-width/2,-height/2,-depth/2));
		
		this.getVertexList().add(new WorldCoord(0, 0, 0));
	}
	
	@Override
	public Point getCentre()
	{
		return this.getVertexList().get(this.getVertexList().size() - 1);
	}
}
