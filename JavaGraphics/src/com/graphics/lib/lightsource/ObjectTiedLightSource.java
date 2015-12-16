package com.graphics.lib.lightsource;

import java.util.Observable;

import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.transform.Transform;


public class ObjectTiedLightSource extends TiedLightSource<CanvasObject> {
	
	public ObjectTiedLightSource(double x, double y, double z) {
		super(x, y, z);
	}

	
	@Override
	public void update(Observable o, Object trans) {
		if (o != this.getTiedTo()) return;
		if (trans instanceof Transform)
		{
			CanvasObject temp = new CanvasObject();
			WorldCoord pos = new WorldCoord(this.getPosition());
			temp.getVertexList().add(pos);
			
			temp.getVertexList().forEach(p -> {
				((Transform) trans).doTransformSpecific().accept(p);
			});
			
			this.setPosition(pos);
		}
		else
		{
			if (this.getTiedTo().isDeleted())
			{
				this.setDeleted(true);
			}
			else if (!this.getTiedTo().isVisible())
			{
				this.turnOff();
			}
			else
			{
				this.turnOn();
			}
		}

	}

}
