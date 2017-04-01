package com.graphics.lib.traits;

import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.orientation.OrientationTransform;

/**
 * Wrapper for a canvas object providing functionality to allow the object to be aware of its orientation, 
 * should it need to know that for transforms that might be applied to it
 * 
 * @author Paul Brandon
 *
 * @param <T> Type of the CanvasObject being wrapped
 */
public class OrientableTrait implements IOrientable {
	public static final String ORIENTATION_TAG = "Orientation";
	private IOrientation orientation;
	private OrientationTransform oTrans = new OrientationTransform();
	protected ICanvasObject parent;
	
	
	@Override
	public IOrientation getOrientation() {		
		return this.orientation;
	}

	@Override
	public IOrientable setOrientation(IOrientation orientation) {
		if (this.orientation != null){
			parent.getVertexList().removeIf(v -> v.hasTag(ORIENTATION_TAG));
		}
		
		this.orientation = orientation;
		if (this.orientation != null){
			parent.getVertexList().addAll(this.orientation.getRepresentation().getVertexList());
		}
		return this;
	}
	
	/**
	 * Revert object to the orientation it started in
	 */
	@Override
	public IOrientable toBaseOrientation(){
		oTrans.saveCurrentTransforms(orientation);
		oTrans.removeRotation(parent);
		return this;
	}
	
	@Override
	public IOrientable reapplyOrientation(){
		oTrans.addRotation(parent);
		return this;
	}
	
	@Override
	public IOrientable applyOrientation(OrientationTransform oTrans){
		oTrans.addRotation(parent);
		return this;
	}

	@Override
	public void setParent(ICanvasObject parent) {
		this.parent = parent;
	}

}
