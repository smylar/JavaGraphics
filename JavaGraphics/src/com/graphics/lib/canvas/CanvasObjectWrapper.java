package com.graphics.lib.canvas;

public class CanvasObjectWrapper<T extends CanvasObject> extends CanvasObject {
	private T wrappedObject;
	
	public CanvasObjectWrapper()
	{
		this.setData(getData());
	}
	
	public CanvasObjectWrapper(T obj)
	{
		wrappedObject = obj;
		this.setData(obj.getData());
	}
	
	@Override
	protected T getWrappedObject()
	{
		return wrappedObject;
	}

}
