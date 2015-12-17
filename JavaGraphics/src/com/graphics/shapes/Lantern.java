package com.graphics.shapes;

import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

public class Lantern extends CanvasObject {
	public Lantern(){
		this.getVertexList().add(new WorldCoord(0, -10, 0)); //0
		this.getVertexList().add(new WorldCoord(5, 0, 5)); //1
		this.getVertexList().add(new WorldCoord(5, 0, -5)); //2
		this.getVertexList().add(new WorldCoord(-5, 0, -5)); //3
		this.getVertexList().add(new WorldCoord(-5, 0, 5)); //4
		this.getVertexList().add(new WorldCoord(0, 10, 0)); //5
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(2)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(2), this.getVertexList().get(3)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(1), this.getVertexList().get(2)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(4), this.getVertexList().get(1)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(4), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(3), this.getVertexList().get(4)));
	}
}
