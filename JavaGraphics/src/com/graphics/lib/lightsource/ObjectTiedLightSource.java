package com.graphics.lib.lightsource;

import com.graphics.lib.ObjectStatus;
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
	public void tieTo(final CanvasObject obj) {
	    super.tieTo(obj);
	    obj.observeTransforms()
              .takeWhile(t -> obj == getTiedTo())
              .subscribe(this::matchTransform); 
	    
	    obj.observeStatus()
            .takeWhile(s -> obj == getTiedTo())
            .filter(s -> s == ObjectStatus.VISIBLE)
            .doFinally(() -> getLightSource().setDeleted(true))
            .subscribe(s -> checkStatus()); 
	}
	
	private void matchTransform(Transform trans) {
	    WorldCoord pos = new WorldCoord(this.getLightSource().getPosition());
        
        ((Transform) trans).doTransformSpecific().accept(pos);
        
        this.getLightSource().setPosition(pos);
	}
	
	private void checkStatus() {

	    if (getTiedTo().isVisible())
        {
            getLightSource().turnOn();
        }
        else
        {
            getLightSource().turnOff();
        }
	}

}
