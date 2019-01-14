package com.graphics.lib.interfaces;

import com.graphics.lib.transform.Transform;

public interface IOrientable extends ITrait {
	IOrientation getOrientation();
	
	IOrientable setOrientation(IOrientation orientation);
	
	Transform toBaseOrientationTransform();

	Transform reapplyOrientationTransform();
}
