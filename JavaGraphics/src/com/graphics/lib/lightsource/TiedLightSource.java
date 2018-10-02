package com.graphics.lib.lightsource;

import java.awt.Color;

/**
 * Abstract for tying a light source to another object. That object must be Observable and notify when something has changed
 * e.g. Object has moved, so so must the light source
 * <br/>
 * Implementing classes must then override the update method to do something with the light source in response to a notification from the tied object
 * <br/><br/>
 * Changed from extending LightSource to using a LightSource generic so we can use this with any kind of light source.
 * 
 * @author Paul Brandon
 *
 * @param <L> - Type of light source
 * @param <T> - Type of object being tied to
 */
public abstract class TiedLightSource<L extends LightSource, T> {
	
	private T tiedTo;
	private L lightSource;
	
	public TiedLightSource(Class<L> ls, double x, double y, double z) {
		try {
			this.lightSource = ls.getConstructor(double.class, double.class, double.class).newInstance(x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void tieTo(T obj){
		this.tiedTo = obj;
	}

	public T getTiedTo() {
		return tiedTo;
	}

	public L getLightSource() {
		return lightSource;
	}
	
	public void setColour(Color colour) {
		if (this.lightSource != null){
			this.lightSource.setColour(colour);
		}
		
	}
	
	public void toggle() {
		if (this.lightSource != null){
			this.lightSource.toggle();
		}
		
	}
}
