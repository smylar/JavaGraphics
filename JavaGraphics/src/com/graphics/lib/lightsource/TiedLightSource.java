package com.graphics.lib.lightsource;

import java.util.Observable;
import java.util.Observer;

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
