package com.graphics.lib.transform;

import com.graphics.lib.Axis;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.IOrientation;

public class PanCamera implements CameraTransform {

	private Rotation rot;
	
	public PanCamera(Axis axis, double angleProgression) {
		rot = axis.getRotation(angleProgression);
	}

	@Override
	public void doTransform(Camera c) {
		try{
			IOrientation o = c.getOrientation().getClass().newInstance();
			rot.beforeTransform();
			o.getRepresentation().getVertexList().forEach(rot.doTransformSpecific()::accept);
			rot.afterTransform();
			c.addCameraRotation(o.getRepresentation());
			
			c.setOrientation(o);
		}
		catch(Exception ex){}
	}

}
