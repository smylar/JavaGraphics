package com.graphics.lib.interfaces;

import java.util.Collection;

@FunctionalInterface
public interface ICanvasObjectList {
	Collection<ICanvasObject> get();
}
