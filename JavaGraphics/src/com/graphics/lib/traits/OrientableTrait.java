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
 */
public class OrientableTrait implements IOrientable {
	public static final String ORIENTATION_TAG = "Orientation";
	private IOrientation orientation;
	private OrientationData oTrans = new OrientationData();
	protected final ICanvasObject parent;
	
	public OrientableTrait(ICanvasObject parent) {
            this.parent = parent;
            parent.observeTransforms()
                  .subscribe(this::applyTransform);
    }
	
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
	
	public void applyTransform(Transform transform) {
	    if (orientation != null) {
	        orientation.getRepresentation().replayTransform(transform);
	    }
	}

}
