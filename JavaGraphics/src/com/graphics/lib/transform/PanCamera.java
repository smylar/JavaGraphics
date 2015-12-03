package com.graphics.lib.transform;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.IOrientation;

public class PanCamera<T extends Matrix> extends Rotation<T> implements CameraTransform {

	public PanCamera(Class<T> matrix, double angleProgression) {
		super(matrix, angleProgression);
	}

	@Override
	public void doTransform(Camera c) {
		try{
			IOrientation o = c.getOrientation().getClass().newInstance();
			this.beforeTransform();
			o.getRepresentation().getVertexList().stream().forEach(p -> {
				this.doTransformSpecific().accept(p);
			});
			this.afterTransform();
			c.addCameraRotation(o.getRepresentation());
			
			c.setOrientation(o);
		}
		catch(Exception ex){}
	}

}
