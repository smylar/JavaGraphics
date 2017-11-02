package com.graphics.lib.lightsource;

import java.util.Observable;

import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.transform.Transform;

/**
 * Describes a light source that mirrors all movements of the object it is tied to
 * 
 * @author paul.brandon
 *
 * @param <L> The type of the specific light source to tie to an object
 */
public class ObjectTiedLightSource<L extends LightSource> extends TiedLightSource<L, CanvasObject> {
	
	public ObjectTiedLightSource(Class<L> ls, double x, double y, double z) {
		super(ls, x, y, z);
	}

	
	@Override
	public void update(Observable o, Object trans) {
		if (o != this.getTiedTo()) {
		    return;
		}
		
		if (trans instanceof Transform)
		{
			WorldCoord pos = new WorldCoord(this.getLightSource().getPosition());
			
			((Transform) trans).doTransformSpecific().accept(pos);
			
			this.getLightSource().setPosition(pos);
		}
		else
		{
			if (this.getTiedTo().isDeleted())
			{
				this.getLightSource().setDeleted(true);
			}
			else if (!this.getTiedTo().isVisible())
			{
				this.getLightSource().turnOff();
			}
			else
			{
				this.getLightSource().turnOn();
			}
		}

	}

}
