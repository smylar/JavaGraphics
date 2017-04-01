package com.graphics.lib.interfaces;

import com.graphics.lib.orientation.OrientationTransform;

public interface IOrientable extends ITrait {
	IOrientation getOrientation();
	
	IOrientable setOrientation(IOrientation orientation);
	
	/**
	 * Revert object to the orientation it started in
	 */
	IOrientable toBaseOrientation();
	
	public IOrientable reapplyOrientation();
	
	public IOrientable applyOrientation(OrientationTransform oTrans);
}
