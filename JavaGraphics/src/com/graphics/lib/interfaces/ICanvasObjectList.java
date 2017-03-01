package com.graphics.lib.interfaces;

import java.util.Collection;

@FunctionalInterface
public interface ICanvasObjectList {
	public Collection<ICanvasObject> get();
}
