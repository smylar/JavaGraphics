package com.graphics.lib.lightsource;

import java.util.Observable;

import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.transform.Transform;


public class ObjectTiedLightSource<L extends LightSource> extends TiedLightSource<L, CanvasObject> {
	
	public ObjectTiedLightSource(Class<L> ls, double x, double y, double z) {
		super(ls, x, y, z);
	}

	
	@Override
	public void update(Observable o, Object trans) {
		if (o != this.getTiedTo()) return;
		if (trans instanceof Transform)
		{
			ICanvasObject temp = new CanvasObject();
			WorldCoord pos = new WorldCoord(this.getLightSource().getPosition());
			temp.getVertexList().add(pos);
			
			temp.getVertexList().forEach(p -> ((Transform) trans).doTransformSpecific().accept(p));
			
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
