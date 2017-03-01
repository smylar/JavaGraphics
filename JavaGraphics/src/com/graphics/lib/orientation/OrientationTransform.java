package com.graphics.lib.orientation;

import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;

/**
 * Contains information on the transforms to be applied to match an orientation
 * 
 * @author Paul Brandon
 *
 */
public class OrientationTransform {
	private double xRot = 0;
	private double yRot = 0;
	private double zRot = 0;
	
	/**
	 * Gets the angle in degrees to rotate by around the X axis
	 * 
	 * @return X Rotation angle
	 */
	public double getxRot() {
		return xRot;
	}

	/**
	 * Gets the angle in degrees to rotate by around the Y axis
	 * 
	 * @return Y Rotation angle
	 */
	public double getyRot() {
		return yRot;
	}

	/**
	 * Gets the angle in degrees to rotate by around the Z axis
	 * 
	 * @return Z Rotation angle
	 */
	public double getzRot() {
		return zRot;
	}

	/**
	 * Generates and stores the rotations needed to match the given orientation from a starting point Forward(0,0,1), Right(1,0,0), Up(0,-1,0)
	 * 
	 * @param orientation
	 */
	public void saveCurrentTransforms(IOrientation orientation)
	{
		Vector forwardv = orientation.getForward();
		Vector upv = orientation.getUp();
		Vector rightv = orientation.getRight();
		WorldCoord forward = new WorldCoord(forwardv.x, forwardv.y, forwardv.z);
		WorldCoord up = new WorldCoord(upv.x, upv.y, upv.z);
		WorldCoord right = new WorldCoord(rightv.x, rightv.y, rightv.z);
			
		ICanvasObject temp = new CanvasObject();
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
	
	/**
	 * Apply the stored rotations to a given object
	 * 
	 * @param obj
	 */
	public void addRotation(ICanvasObject obj)
	{
		obj.applyTransform(new Rotation<ZRotation>(ZRotation.class, zRot));
		obj.applyTransform(new Rotation<XRotation>(XRotation.class, xRot));
		obj.applyTransform(new Rotation<YRotation>(YRotation.class, yRot));
	}
	
	/**
	 * Remove the stored rotation from a given object
	 * 
	 * @param obj
	 */
	public void removeRotation(ICanvasObject obj)
	{	
		obj.applyTransform(new Rotation<YRotation>(YRotation.class, -yRot));
		obj.applyTransform(new Rotation<XRotation>(XRotation.class, -xRot));
		obj.applyTransform(new Rotation<ZRotation>(ZRotation.class, -zRot));
	}
	
	public static List<Rotation<?>> getRotationsForVector(Vector v){
		List<Rotation<?>> rots = new ArrayList<Rotation<?>>();
		Vector unit = v.getUnitVector();
		WorldCoord wc = new WorldCoord(unit.x, unit.y, unit.z);
			
		ICanvasObject temp = new CanvasObject();
		temp.getVertexList().add(wc);

		double xRot = 0;
		double yRot = 0;
		
		if (wc.z < 0) 
			yRot = 180;
		
		if (wc.z != 0)
			yRot += Math.toDegrees(Math.atan(wc.x / wc.z));
		else if (wc.x > 0)
			yRot = 90;
		else if (wc.x < 0)
			yRot = -90;
			
		Rotation<?> r = new Rotation<YRotation>(YRotation.class, -yRot);
		temp.applyTransform(r);
		
		Rotation<?> r0 = new Rotation<YRotation>(YRotation.class, yRot);
		
		if (wc.z != 0)
			xRot += Math.toDegrees(Math.atan(wc.y / wc.z)) * -1;
		else if (wc.y > 0)
			xRot = -90;
		else if (wc.y < 0)
			xRot = 90;
			
		Rotation<?> r1 = new Rotation<XRotation>(XRotation.class, xRot);
		rots.add(r1);
		rots.add(r0);
		
		return rots;
	}
}
