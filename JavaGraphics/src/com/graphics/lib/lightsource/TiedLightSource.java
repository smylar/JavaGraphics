package com.graphics.lib.lightsource;

import java.util.Observable;
import java.util.Observer;

/**
 * Abstract for tying a light source to another object. That object must be Observable and notify when something has changed
 * e.g. Object has moved, so so must the light source
 * <br/>
 * Implementing classes must then override the update method to do something with the light source in response to a notification from the tied object
 * 
 * @author Paul Brandon
 *
 * @param <T> - Type of object being tied to
 */
public abstract class TiedLightSource<T extends Observable> extends LightSource implements Observer {
	
	private T tiedTo;
	
	public TiedLightSource(double x, double y, double z) {
		super(x, y, z);
	}

	public void tieTo(T obj){
		this.tiedTo = obj;
		obj.addObserver(this);
	}

	public T getTiedTo() {
		return tiedTo;
	}
	
}
