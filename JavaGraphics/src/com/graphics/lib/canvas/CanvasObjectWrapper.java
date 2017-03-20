package com.graphics.lib.canvas;

import com.graphics.lib.interfaces.ICanvasObject;

public class CanvasObjectWrapper extends CanvasObject {
	private ICanvasObject wrappedObject;
	
	public CanvasObjectWrapper()
	{
		this.setData(getData());
	}
	
	public CanvasObjectWrapper(ICanvasObject obj)
	{
		this.setWrappedObject(obj);
	}
	
	@Override
	protected ICanvasObject getWrappedObject()
	{
		return wrappedObject;
	}
	
	protected void setWrappedObject(ICanvasObject obj){
		wrappedObject = obj;
		this.setData(obj.getData());
	}

}
