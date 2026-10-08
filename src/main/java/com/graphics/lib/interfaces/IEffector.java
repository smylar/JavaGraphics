package com.graphics.lib.interfaces;

import java.util.Optional;

public interface IEffector {
	void activate();
	
	default void deActivate() {}
	
	ICanvasObject getParent();
	
	String getId();
	
	default Optional<Class<?>> getEffectClass() { return Optional.empty(); }
}
