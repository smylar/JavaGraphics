package com.graphics.lib.orientation;


import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientation;

public class SimpleOrientation implements IOrientation {

	private ICanvasObject orientation;
	
	public SimpleOrientation() {
		this("");
	}

	public SimpleOrientation(String tag) {
		orientation = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(0,0,1), //forward
		                                                              new WorldCoord(0,-1,0), //up
		                                                              new WorldCoord(1,0,0), //right
		                                                              new WorldCoord(0,0,0) //anchor
		                                                              ), 
		                                             ImmutableList.of()));
		
		for (Point p : orientation.getVertexList()) {
			p.addTag(tag);
		}
	}
	
	@Override
	public Vector getForward() {
		Point forward = orientation.getVertexList().get(0);
		return moveToOrigin(new Vector(forward.x, forward.y, forward.z));
	}
	
	@Override
	public Vector getUp() {
		Point up = orientation.getVertexList().get(1);
		return moveToOrigin(new Vector(up.x, up.y, up.z));
	}
	
	@Override
	public Vector getRight() {
		Point right = orientation.getVertexList().get(2);
		return moveToOrigin(new Vector(right.x, right.y, right.z));
	}
	
	@Override
	public Vector getBack() {
		Point forward = orientation.getVertexList().get(0);
		return moveToOriginInverted(new Vector(-forward.x, -forward.y, -forward.z));
	}
	
	@Override
	public Vector getDown() {
		Point up = orientation.getVertexList().get(1);
		return moveToOriginInverted(new Vector(-up.x, -up.y, -up.z));
	}
	
	@Override
	public Vector getLeft() {
		Point right = orientation.getVertexList().get(2);
		return moveToOriginInverted(new Vector(-right.x, -right.y, -right.z));
	}
	
	@Override
	public Point getAnchor() {
		return orientation.getVertexList().get(3);
	}

	@Override
	public ICanvasObject getRepresentation() {
		return orientation;
	}
	
	@Override
	public Facet getPlane(){
		return new Facet(orientation.getVertexList().get(1), orientation.getVertexList().get(2), orientation.getVertexList().get(3));
	}
	
	private Vector moveToOrigin(Vector v)
	{
		Point anchor = getAnchor();
		return Vector.builder().x(v.getX() - anchor.x)
							   .y(v.getY() - anchor.y)
							   .z(v.getZ() - anchor.z)
							   .build();
	}
	
	private Vector moveToOriginInverted(Vector v)
	{
		Point anchor = getAnchor();
		return Vector.builder().x(v.getX() + anchor.x)
							   .y(v.getY() + anchor.y)
							   .z(v.getZ() + anchor.z)
							   .build();
	}

	@Override
	public IOrientation copy() {
		SimpleOrientation copy = new SimpleOrientation();
		ICanvasObject source = this.getRepresentation();
		ICanvasObject dest = copy.getRepresentation();
		IntStream.range(0,4)
		         .forEach(i -> dest.getVertexList().get(i).copyFrom(source.getVertexList().get(i)));

		return copy;
	}
}
