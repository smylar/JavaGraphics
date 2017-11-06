package com.graphics.tests.shapes;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctionsImpl;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;

public class Bird extends CanvasObject {
	public Bird() {
	    super(Bird::init);
	    FunctionHandler.register(this, getFunctionsImpl());
		
		this.setProcessBackfaces(true);
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
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init() {
		ImmutableList<WorldCoord> vertexList = generateVertexList();
		return Pair.of(vertexList, generateFacetList(vertexList));
	}
	
	private static ImmutableList<WorldCoord> generateVertexList() {
		return ImmutableList.of(new WorldCoord(0, 0, 0),
								new WorldCoord(0, 0, 60),
								//right wing
								new WorldCoord(50, 0, 10),
								new WorldCoord(50, 0, 50),
								new WorldCoord(100, 0, 30),
								//left wing
								new WorldCoord(-50, 0, 10),
								new WorldCoord(-50, 0, 50),
								new WorldCoord(-100, 0, 30));
	}
	
	private static ImmutableList<Facet> generateFacetList(ImmutableList<WorldCoord> vertexList) {
		return ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(2), vertexList.get(1)),
								new Facet(vertexList.get(1), vertexList.get(2), vertexList.get(3)),
								new Facet(vertexList.get(3), vertexList.get(2), vertexList.get(4)),
								new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(5)),
								new Facet(vertexList.get(1), vertexList.get(6), vertexList.get(5)),
								new Facet(vertexList.get(5), vertexList.get(6), vertexList.get(7)));
	}
}
