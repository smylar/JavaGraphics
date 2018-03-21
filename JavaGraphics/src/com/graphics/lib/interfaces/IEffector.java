package com.graphics.lib.interfaces;

public interface IEffector {
	public void activate();
	
	default void deActivate() {}
	
	public ICanvasObject getParent();
}
