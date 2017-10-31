package com.graphics.lib.traits;

import java.util.Observable;
import java.util.Observer;

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
public class OrientableTrait implements IOrientable, Observer {
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
		
		this.orientation = orientation;
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
		parent.applyTransform(toBaseOrientationTransform());
		return this;
	}
	
	@Override
	@Deprecated
	public IOrientable reapplyOrientation(){
		parent.applyTransform(reapplyOrientationTransform());
		return this;
	}

	@Override
	public void setParent(ICanvasObject parent) {
		if (this.parent != null) {
			this.parent.deleteObserver(this);
		}
		if (parent != null) {
			this.parent = parent;
			parent.addObserver(this);
		}
	}
	
	public void applyTransform(Transform transform) {
		this.orientation.getRepresentation().replayTransform(transform);
	}

	@Override
	public void update(Observable obs, Object args) {
		if (args instanceof Transform) {
			this.orientation.getRepresentation().replayTransform((Transform)args);
		}
		
	}

}
