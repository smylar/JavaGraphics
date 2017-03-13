package com.graphics.lib.canvas;

import com.graphics.lib.interfaces.ICanvasObject;

public class CanvasObjectWrapper<T extends ICanvasObject> extends CanvasObject {
	private T wrappedObject;
	
	public CanvasObjectWrapper()
	{
		this.setData(getData());
	}
	
	public CanvasObjectWrapper(T obj)
	{
		this.setWrappedObject(obj);
	}
	
	@Override
	protected T getWrappedObject()
	{
		return wrappedObject;
	}
	
	protected void setWrappedObject(T obj){
		wrappedObject = obj;
		this.setData(obj.getData());
	}

}
