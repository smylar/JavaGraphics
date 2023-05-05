package com.graphics.lib.texture;

import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;

public abstract class TextureMapper<T extends ICanvasObject> {

	private final Class<T> clazz;
	
	public TextureMapper(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public final Map<WorldCoord, Point> map(ICanvasObject obj, Texture texture) {
	    return obj.getObjectAs(clazz).map(o -> mapImpl(o, texture)).orElseGet(HashMap::new);
	}
	
	protected abstract Map<WorldCoord, Point> mapImpl(T obj, Texture texture);

}
