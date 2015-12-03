package com.graphics.lib.lightsource;

import java.util.Observable;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;

public class CameraTiedLightSource extends TiedLightSource<Camera> {

	private Point startPosition;
	private Point startCamPosition;
	
	public CameraTiedLightSource(double x, double y, double z) {
		super(x, y, z);
		this.startPosition = new Point(x, y, z);
	}

	public void tieTo(Camera cam){
		super.tieTo(cam);
		this.startCamPosition = new Point (cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (startCamPosition == null || !arg.toString().equals(Camera.CAMERA_MOVED)) return;
		
		Camera c = this.getTiedTo();
		CanvasObject temp = new CanvasObject();
		WorldCoord p = new WorldCoord(this.startPosition);
		
		p.x = startPosition.x + (c.getPosition().x - startCamPosition.x);
		p.y = startPosition.y + (c.getPosition().y - startCamPosition.y);
		p.z = startPosition.z + (c.getPosition().z - startCamPosition.z);
		temp.getVertexList().add(p);
		c.matchCameraRotation(temp);
		this.setPosition(p);
	}

}
