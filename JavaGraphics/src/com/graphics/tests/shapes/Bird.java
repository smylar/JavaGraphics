package com.graphics.tests.shapes;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctionsImpl;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;

public class Bird extends CanvasObject {
	public Bird() {
	    super();
	    FunctionHandler.register(this, getFunctionsImpl());
		
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
	
	
	private CanvasObjectFunctionsImpl getFunctionsImpl() {
		return new CanvasObjectFunctionsImpl() {
			@Override
			public boolean isPointInside(ICanvasObject obj, Point p)
			{		
				//as this is essentially a 2D object this won't work, well it would report true if just underneath the object
				return false;
			}
			
			@Override
			public double getMaxExtent(ICanvasObject obj)
			{
				return obj.getCentre().distanceTo(obj.getVertexList().get(4));
			}
		};
	}
}
