package com.graphics.lib.canvas;

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
public class OrientableCanvasObject<T extends CanvasObject> extends CanvasObjectWrapper<T> implements IOrientable {
	public static final String ORIENTATION_TAG = "Orientation";
	private IOrientation orientation;
	private OrientationTransform oTrans = new OrientationTransform();
	
	public OrientableCanvasObject()
	{
		super();
	}
	
	public OrientableCanvasObject(T obj)
	{
		super(obj);
	}
	
	
	@Override
	public IOrientation getOrientation() {		
		return this.orientation;
	}

	@Override
	public void setOrientation(IOrientation orientation) {
		if (this.orientation != null){
			this.getVertexList().removeIf(v -> v.hasTag(ORIENTATION_TAG));
		}
		
		this.orientation = orientation;
		if (this.orientation != null){
			this.getVertexList().addAll(this.orientation.getRepresentation().getVertexList());
		}
	}
	
	/**
	 * Revert object to the orientation it started in
	 */
	@Override
	public void toBaseOrientation(){
		oTrans.saveCurrentTransforms(orientation);
		oTrans.removeRotation(this);
	}
	
	@Override
	public void reapplyOrientation(){
		oTrans.addRotation(this);
	}
	
	@Override
	public void applyOrientation(OrientationTransform oTrans){
		oTrans.addRotation(this);
	}

}
