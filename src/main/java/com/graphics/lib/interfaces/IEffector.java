package com.graphics.lib.interfaces;

import java.util.Optional;

public interface IEffector {
	public void activate();
	
	default void deActivate() {}
	
	public ICanvasObject getParent();
	
	public String getId();
	
	default Optional<Class<?>> getEffectClass() { return Optional.empty(); }
}
