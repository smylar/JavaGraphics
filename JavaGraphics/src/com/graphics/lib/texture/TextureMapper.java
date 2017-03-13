package com.graphics.lib.texture;

import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;

public abstract class TextureMapper<T extends ICanvasObject> {

	private Class<T> clazz;
	
	public TextureMapper(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public final void map(ICanvasObject obj, Map<WorldCoord, Point> textureMap, Texture texture) {
		obj.getObjectAs(clazz).ifPresent(o -> mapImpl(o, textureMap, texture));
	}
	
	protected abstract void mapImpl(T obj, Map<WorldCoord, Point> textureMap, Texture texture);

}
