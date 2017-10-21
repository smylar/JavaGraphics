package com.graphics.lib.interfaces;

import com.graphics.lib.transform.Transform;

public interface IOrientable extends ITrait {
	IOrientation getOrientation();
	
	IOrientable setOrientation(IOrientation orientation);
	
	/**
	 * Revert object to the orientation it started in
	 */
	@Deprecated
	IOrientable toBaseOrientation();
	
	@Deprecated
	public IOrientable reapplyOrientation();

	
	Transform toBaseOrientationTransform();

	Transform reapplyOrientationTransform();
}
