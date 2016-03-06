package com.graphics.lib.camera;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IOrientation;

/**
 * Camera that takes a view angle to work out the screen coordinates of everything that is to be displayed.
 * 
 * @author Paul Brandon
 *
 */
public class ViewAngleCamera extends Camera {

	private static final double cos45 = Math.cos(Math.toRadians(45));
	private double viewAngle = 45;
	private double tanViewAngle = Math.tan(Math.toRadians(45));
	
	public ViewAngleCamera(IOrientation orientation) {
		super(orientation);
	}
	
	@Override
	public void getViewSpecific(CanvasObject obj) {
		
		obj.getVertexList().parallelStream().forEach(p -> {
			p.resetTransformed(this);
		});
		
		this.alignShapeToCamera(obj);
		
		obj.getVertexList().parallelStream().forEach(p -> {
			this.getCameraCoords(p.getTransformed(this));
		});
	}
	
	private void getCameraCoords(Point p){
		Point position = this.getPosition();
		double zed = p.z - position.z;
		 if (zed < 1 && zed >= 0) zed = 1;
		 
		 if (zed < 0){
			 //point behind camera 
			 //TODO needs fixing - or needs entirely new approach - probably along lines of a clipping plane - which will be fun
			 //finally did good test of this with laser - big difference in vertices, gets worse as point further behind camera at an angle
			 //a cheat would be to add intermediate points of a suitable length so this wouldn't show up
			 Point zAt1 = new Point(p.x,p.y,position.z);
			 Point invZ = new Point(p.x,p.y,position.z - zed);
			 getCameraCoords(zAt1);
			 getCameraCoords(invZ);
			 p.x = zAt1.x + zAt1.x - invZ.x;
			 p.y = zAt1.y + zAt1.y - invZ.y;
			 p.z = zed;		 
		 }
		 else{
			 double dx = p.x - position.x;
			 double dy = p.y - position.y;
			 double angletopointx = Math.atan(dx / zed);
			 double angletopointy = Math.atan(dy / zed);
			 ViewPort vp = this.getViewport(zed);
			 double viewPortx = (vp.width/2) + (Math.tan(angletopointx) * zed);
			 double viewPorty = (vp.height/2) + (Math.tan(angletopointy) * zed);
			 
			 double scaling = this.dispwidth / vp.width;
			 
			 p.x = viewPortx * scaling;
			 p.y = viewPorty * scaling;
			 p.z = zed;
		 }
	}

	public double getViewAngle() {
		return viewAngle;
	}

	public void setViewAngle(double viewAngle) {
		if (viewAngle <= 0) return;
		if (viewAngle >= 90) viewAngle = 89;
		this.viewAngle = viewAngle;
		this.tanViewAngle = Math.tan(Math.toRadians(this.viewAngle));
	}
	
	private ViewPort getViewport(double z){
		double radius = this.tanViewAngle * z;
		double boxWidth = (cos45 * radius) * 2;
		double boxHeight = boxWidth;
		if (this.dispwidth > this.dispheight){
			boxHeight = boxHeight * (this.dispheight / this.dispwidth);
		}
		else if (this.dispheight > this.dispwidth){
			boxWidth = boxWidth * (this.dispwidth / this.dispheight);
		}
		return new ViewPort(boxWidth, boxHeight);
	}
	
	private class ViewPort
	{
		double width = 0;
		double height = 0;
		
		public ViewPort(double width, double height){
			this.height = height;
			this.width = width;
		}
	}

}
