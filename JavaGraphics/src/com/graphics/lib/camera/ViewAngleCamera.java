package com.graphics.lib.camera;

import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;
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
	public void getViewSpecific(ICanvasObject obj) {
		
		//synchronized(obj.getVertexList()){
			obj.getVertexList().parallelStream().forEach(p -> {
				p.resetTransformed(this);
			});
			
			this.alignShapeToCamera(obj);
			
			Point position = this.getPosition();
			
			obj.getVertexList().parallelStream().filter(v -> v.getTransformed(this).z < position.z-1).forEach(p -> {
				this.doPointBehind(obj, p, p.getTransformed(this), position);
			});
			
			obj.getVertexList().parallelStream().forEach(p -> {
				
				Point trans = p.getTransformed(this);
				double zed = trans.z - position.z;
	
				if (zed < 1 && zed > -1) zed = 1;
				
				if (zed<0){
					getCameraCoords(trans, position);
				}
				else doPointInFront(trans, zed);
			});
		//}
	}
	
	private void getCameraCoords(Point p, Point position){
		
		double zed = p.z - position.z;
		 if (zed < 1 && zed >= 0) zed = 1;
		 
		 if (zed < 0){
			 //point behind camera 
			 WorldCoord zAt1 = new WorldCoord(p.x,p.y,position.z);
			 WorldCoord invZ = new WorldCoord(p.x,p.y,position.z - zed);
			 getCameraCoords(zAt1, position);
			 getCameraCoords(invZ, position);
			 p.x = zAt1.x + zAt1.x - invZ.x;
			 p.y = zAt1.y + zAt1.y - invZ.y;
			 p.z = zed;
			 
			 //now with the clipping plane in place if we reach here the whole facet should be not visible (in theory)
			 /*p.x = position.x -10000;
			 p.y = position.y;
			 p.z = zed;*/
		}
		else{
			doPointInFront(p, zed);
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
	
	private void doPointBehind(ICanvasObject obj, WorldCoord wc, Point trans, Point position)
	{
		//attempt to do camera plane clipping
		//moves points behind camera towards nearest attached point in front of camera - will not work for everything - weird effects likely (especially passing close by with larger facets - though configuration of laser means its fine)
		//TODO will squash any texture, would need to update texture coordinates too
		//if (!(obj instanceof Laser)) return; //for testing initially
		
		//if (obj.getObjectAs(Ship.class) != null) return; 
		//may be something up with the ordering of things, mess up on move forward (squishes the rear of the ship just in front of camera), may be best to not do this for the object you are tied to, your view of it shouldn't change anyway
		if (obj == this.getTiedTo()) return; 
		
		Point nearest = null;
		double nearestdist = 0;
		for(Facet f : obj.getFacetList().stream().filter(f -> f.contains(wc) ).collect(Collectors.toList()))
		{	
			for (WorldCoord w : f.getAsList()){
				if (wc == w) continue;
				Point ptrans = w.getTransformed(this);
				if (ptrans.z > position.z){
					double dist = trans.distanceTo(ptrans);
					if (nearest == null || dist < nearestdist){
						nearest = ptrans;
						nearestdist = dist;
					}
				}
			}
		}
		if (nearest != null){
			Vector v = nearest.vectorToPoint(trans);
			Facet camPlane = new Facet(new WorldCoord(10, 0, position.z), new WorldCoord(10, 10, position.z), new WorldCoord(0, 10, position.z));
			Point intersect = camPlane.getIntersectionPointWithFacetPlane(nearest, v.getUnitVector());
			if (intersect != null){
				trans.x = intersect.x;
				trans.y = intersect.y;
				trans.z = position.z - 0.5;
			}
		}
	}
	
	private void doPointInFront(Point p, double zed){
		Point position = this.getPosition();
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
