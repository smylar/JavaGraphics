package com.graphics.lib.interfaces;

import com.graphics.lib.orientation.OrientationTransform;

public interface IOrientable extends ICanvasObject {
	IOrientation getOrientation();
	
	void setOrientation(IOrientation orientation);
	
	/**
	 * Revert object to the orientation it started in
	 */
	void toBaseOrientation();
	
	public void reapplyOrientation();
	
	public void applyOrientation(OrientationTransform oTrans);
}
