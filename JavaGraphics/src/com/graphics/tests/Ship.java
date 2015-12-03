package com.graphics.tests;

import java.awt.Color;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;

public class Ship extends CanvasObject {
	
	public Ship(int width, int depth, int height)
	{
		this.getVertexList().add(new WorldCoord(0, 0, 0));
		this.getVertexList().add(new WorldCoord(width/2, 0, depth));
		this.getVertexList().add(new WorldCoord(-width/2, 0, depth));
		this.getVertexList().add(new WorldCoord(0, height/2, depth * 0.8));
		this.getVertexList().add(new WorldCoord(0, -height/2, depth * 0.8));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(4), this.getVertexList().get(2)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(3)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(3), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(4), this.getVertexList().get(3)));
		
		this.getFacetList().get(4).setColour(Color.YELLOW);
		this.getFacetList().get(5).setColour(Color.YELLOW);
	}
}
