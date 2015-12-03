package com.graphics.lib;

import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;

public class OrientationTransform {
	private double xRot = 0;
	private double yRot = 0;
	private double zRot = 0;
	
	public double getxRot() {
		return xRot;
	}

	public double getyRot() {
		return yRot;
	}

	public double getzRot() {
		return zRot;
	}

	public void saveCurrentTransforms(IOrientation orientation)
	{
		Vector forwardv = orientation.getForward();
		Vector upv = orientation.getUp();
		Vector rightv = orientation.getRight();
		WorldCoord forward = new WorldCoord(forwardv.x, forwardv.y, forwardv.z);
		WorldCoord up = new WorldCoord(upv.x, upv.y, upv.z);
		WorldCoord right = new WorldCoord(rightv.x, rightv.y, rightv.z);
			
		CanvasObject temp = new CanvasObject();
		temp.getVertexList().add(forward);
		temp.getVertexList().add(up);
		temp.getVertexList().add(right);
		
		this.xRot = 0;
		this.yRot = 0;
		this.zRot = 0;
		
		if (forward.z < 0) 
			this.yRot = 180;
		
		if (forward.z != 0)
			this.yRot += Math.toDegrees(Math.atan(forward.x / forward.z));
		else if (forward.x > 0)
			this.yRot = 90;
		else if (forward.x < 0)
			this.yRot = -90;
			
		temp.applyTransform(new Rotation<YRotation>(YRotation.class, -this.yRot));
		
		if (forward.z != 0)
			this.xRot += Math.toDegrees(Math.atan(forward.y / forward.z)) * -1;
		else if (forward.y > 0)
			this.xRot = -90;
		else if (forward.y < 0)
			this.xRot = 90;
			
		temp.applyTransform(new Rotation<XRotation>(XRotation.class, -xRot));
		
		if (up.y > 0) 
			this.zRot = 180;
		
		if (right.x != 0)
			this.zRot += Math.toDegrees(Math.atan(right.y / right.x));
		else if (right.y > 0)
			this.zRot = 90;
		else if (right.y < 0)
			this.zRot = -90;
		
	}
	
	public void addRotation(CanvasObject obj)
	{
		obj.applyTransform(new Rotation<ZRotation>(ZRotation.class, zRot));
		obj.applyTransform(new Rotation<XRotation>(XRotation.class, xRot));
		obj.applyTransform(new Rotation<YRotation>(YRotation.class, yRot));
	}
	
	public void removeRotation(CanvasObject obj)
	{	
		obj.applyTransform(new Rotation<YRotation>(YRotation.class, -yRot));
		obj.applyTransform(new Rotation<XRotation>(XRotation.class, -xRot));
		obj.applyTransform(new Rotation<ZRotation>(ZRotation.class, -zRot));
	}
}
