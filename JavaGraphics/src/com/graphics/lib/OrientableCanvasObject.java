package com.graphics.lib;

import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;

public class OrientableCanvasObject<T extends CanvasObject> extends CanvasObject implements IOrientable {
	public static final String ORIENTATION_TAG = "Orientation";
	private T wrappedObject;
	private IOrientation orientation;
	private OrientationTransform oTrans = new OrientationTransform();
	
	public OrientableCanvasObject(T obj)
	{
		wrappedObject = obj;
		this.setData(obj.getData());
	}
	
	@Override
	protected T getWrappedObject()
	{
		return wrappedObject;
	}
	
	@Override
	protected CanvasObject getBaseObject()
	{
		return wrappedObject.getBaseObject();
	}
	
	@Override
	public IOrientation getOrientation() {		
		return this.orientation;
	}

	@Override
	public void setOrientation(IOrientation orientation) {
		if (this.orientation != null){
			this.getVertexList().removeIf(v -> v.getTag().equals(ORIENTATION_TAG));
		}
		
		this.orientation = orientation;
		if (this.orientation != null){
			this.getVertexList().addAll(this.orientation.getRepresentation().getVertexList());
		}
	}
	
	public void toBaseOrientation(){
		oTrans.saveCurrentTransforms(orientation);
		oTrans.removeRotation(this);
	}
	
	public void reapplyOrientation(){
		oTrans.addRotation(this);
	}
	
	public void applyOrientation(OrientationTransform oTrans){
		oTrans.addRotation(this);
	}

}
