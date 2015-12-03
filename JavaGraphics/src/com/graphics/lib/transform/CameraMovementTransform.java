package com.graphics.lib.transform;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.IVectorFinder;

public class CameraMovementTransform extends MovementTransform implements CameraTransform {

	public CameraMovementTransform(IVectorFinder vectorFinder, double velocity) {
		super(vectorFinder, velocity);
	}

	@Override
	public void doTransform(Camera c) {
		this.beforeTransform();
		this.doTransformSpecific().accept(c.getPosition());
		this.afterTransform();
	}
	

}

