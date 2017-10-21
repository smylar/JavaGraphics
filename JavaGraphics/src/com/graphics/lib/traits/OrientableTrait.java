package com.graphics.lib.traits;

import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.transform.BaseOrientationTransform;
import com.graphics.lib.transform.ReapplyOrientationTransform;
import com.graphics.lib.transform.Transform;

/**
 * Wrapper for a canvas object providing functionality to allow the object to be aware of its orientation, 
 * should it need to know that for transforms that might be applied to it
 * 
 * @author Paul Brandon
 *
 * @param <T> Type of the CanvasObject being wrapped
 */
public class OrientableTrait implements IOrientable {
	//TODO refactor not to add to object vertex list, intercept and apply separately, once mechanism worked out in Trackable
	public static final String ORIENTATION_TAG = "Orientation";
	private IOrientation orientation;
	private OrientationData oTrans = new OrientationData();
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
	
	@Override
	public Transform toBaseOrientationTransform() {
		return new BaseOrientationTransform(oTrans, orientation);
	}
	
	@Override
	public Transform reapplyOrientationTransform() {
		return new ReapplyOrientationTransform(oTrans);
	}
	
	/**
	 * Revert object to the orientation it started in
	 */
	@Override
	@Deprecated
	public IOrientable toBaseOrientation(){
//		oTrans.saveCurrentTransforms(orientation);
//		oTrans.removeRotation(parent);
		parent.applyTransform(toBaseOrientationTransform());
		return this;
	}
	
	@Override
	@Deprecated
	public IOrientable reapplyOrientation(){
//		oTrans.addRotation(parent);
		parent.applyTransform(reapplyOrientationTransform());
		return this;
	}

	@Override
	public void setParent(ICanvasObject parent) {
		this.parent = parent;
	}

}
