package com.graphics.lib.lightsource;

import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Describes a light source that mirrors all movements of the camera
 * 
 * @author paul.brandon
 *
 * @param <L> The type of the specific light source to tie to the camera
 */
public class CameraTiedLightSource<L extends LightSource> extends TiedLightSource<L,Camera> implements Observer {

	private Point startPosition;
	private Point startCamPosition;
	
	public CameraTiedLightSource(Class<L> ls, double x, double y, double z) {
		super(ls, x, y, z);
		this.startPosition = new Point(x, y, z);
	}

	@Override
	public void tieTo(Camera cam){
		super.tieTo(cam);
		cam.addObserver(this);
		this.startCamPosition = new Point(cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (startCamPosition == null || !arg.toString().equals(Camera.CAMERA_MOVED)) {
		    return;
		}
		
		Camera c = this.getTiedTo();
		
		WorldCoord p = new WorldCoord(this.startPosition);
		
		p.x = startPosition.x + (c.getPosition().x - startCamPosition.x);
		p.y = startPosition.y + (c.getPosition().y - startCamPosition.y);
		p.z = startPosition.z + (c.getPosition().z - startCamPosition.z);
		
		ICanvasObject temp = Utils.getCoordAsCanvasObject(p);
		c.matchCameraRotation(temp);
		this.getLightSource().setPosition(p);
	}

}
