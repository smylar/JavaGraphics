package com.graphics.lib;

import com.graphics.lib.interfaces.IOrientation;

public class SimpleOrientation implements IOrientation {

	private CanvasObject orientation;
	
	public SimpleOrientation(){
		this("");
	}

	public SimpleOrientation(String tag){
		orientation = new CanvasObject();
		orientation.getVertexList().add(new WorldCoord(0,0,1)); //forward
		orientation.getVertexList().add(new WorldCoord(0,-1,0)); //up
		orientation.getVertexList().add(new WorldCoord(1,0,0)); //right
		orientation.getVertexList().add(new WorldCoord(0,0,0)); //anchor
		
		for (Point p : orientation.getVertexList()){
			p.setTag(tag);
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
	public CanvasObject getRepresentation() {
		return orientation;
	}
	
	private Vector moveToOrigin(Vector v)
	{
		Point anchor = getAnchor();
		v.x -= anchor.x;
		v.y -= anchor.y;
		v.z -= anchor.z;
		return v;
	}
	
	private Vector moveToOriginInverted(Vector v)
	{
		Point anchor = orientation.getVertexList().get(3);
		v.x += anchor.x;
		v.y += anchor.y;
		v.z += anchor.z;
		return v;
	}

	@Override
	public IOrientation copy() {
		SimpleOrientation copy = new SimpleOrientation();
		CanvasObject source = this.getRepresentation();
		CanvasObject dest = copy.getRepresentation();
		for (int i = 0 ; i < 4 ; i++)
		{
			dest.getVertexList().get(i).x = source.getVertexList().get(i).x;
			dest.getVertexList().get(i).y = source.getVertexList().get(i).y;
			dest.getVertexList().get(i).z = source.getVertexList().get(i).z;
		}
		return copy;
	}
}
