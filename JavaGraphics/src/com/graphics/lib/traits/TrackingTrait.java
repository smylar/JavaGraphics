package com.graphics.lib.traits;

import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITracker;
import com.graphics.lib.transform.Transform;

/**
 * Trait whereby an object will completely mirror the transforms applied to another object<br/>
 * 
 * @author paul.brandon
 *
 */
public class TrackingTrait implements ITracker, Observer  {
	public static final String TRACKING_TAG = "tracking_tag";
	
	private ICanvasObject parent; //the object this trait belongs to
	
    private ICanvasObject target;
    
    private ICanvasObject pending;
    
    public void setDeleted(){
    		pending = null;
    		doStop(false);
    }
    
    @Override
    public void observeAndMatch (ICanvasObject target) {
        pending = target;
        synchronized(pending.getChildren()) {
        	parent.addFlag(TRACKING_TAG);
        	pending.getChildren().add(parent); 
        }
        doObserve();
    }
    
    @Override
    public void stopObserving() {
        pending = null;
    }
    
    public void onDrawComplete() {
        //do in draw complete phase to minimise any conflicts (may be better in a before stage which doesn't currently exist)
    	
        doStop(true);
        doObserve();
    }
    
    @Override
    public void setParent(ICanvasObject parent) {
        this.parent = parent;
        
    }
    
    private void doStop(boolean remove) {
        if (target != null && pending == null) {
        	if (remove) {
        		target.getChildren().remove(this); //if delete parent will remove it anyway
        	}

        	target.deleteObserver(this);
            target = null;
            parent.removeFlag(TRACKING_TAG);
        }
    }
    
    private void doObserve() {
        if (pending == null || (target != null && target.getObjectTag().equals(pending.getObjectTag() ))) {
            return;
        }
        
        target = pending;
    	
        target.addObserver(this);
    }

	@Override
	public void update(Observable obs, Object payload) {
		if (payload instanceof Transform) {
			parent.replayTransform((Transform)payload);
		} else if (payload == ObjectStatus.DELETED) {
		    setDeleted();
		} else if (payload == ObjectStatus.DRAW_COMPLETE) {
		    onDrawComplete();
		}
		
	}

}
