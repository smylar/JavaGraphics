package com.graphics.lib.camera;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IOrientation;

public class ViewAngleCamera extends Camera {
	
	public ViewAngleCamera(IOrientation orientation) {
		super(orientation);
	}

	private double viewAngle = 90;
	
	@Override
	public void getViewSpecific(CanvasObject obj) {
		
		obj.getVertexList().parallelStream().forEach(p -> {
			p.resetTransformed();
		});
		
		this.alignShapeToCamera(obj);
		
		obj.getVertexList().parallelStream().forEach(p -> {
			this.getCameraCoords(p.getTransformed());
		});
	}
	
	private void getCameraCoords(Point p){
		Point position = this.getPosition();
		double zed = p.z - position.z;
		 if (zed < 1 && zed >= 0) zed = 1;
		 
		 if (zed < 0){
			 //point behind camera 
			 Point zAt1 = new Point(p.x,p.y,position.z);
			 Point invZ = new Point(p.x,p.y,position.z + (zed*-1));
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
		if (viewAngle >= 180) viewAngle = 179;
		this.viewAngle = viewAngle;
	}
	
	private ViewPort getViewport(double z){
		double radius = Math.tan(Math.toRadians(this.viewAngle/2)) * z;
		double boxWidth = (Math.cos(Math.toRadians(45)) * radius) * 2;
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
