package com.graphics.lib.traits;

import java.util.Set;

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
public class TrackingTrait implements ITracker {
	public static final String TRACKING_TAG = "tracking_tag";
	
	private final ICanvasObject parent; //the object this trait belongs to
	
    private ICanvasObject target;
    
    private ICanvasObject pending;
    
    private Set<Class<? extends Transform>> matchTypes;
    
    public TrackingTrait(ICanvasObject parent) {
        this.parent = parent;
    }
    
    public void setDeleted() {
		pending = null;
		doStop(false);
    }
    
    @Override
    public void observeAndMatch (ICanvasObject target, Set<Class<? extends Transform>> transformTypes) {
        pending = target;
        matchTypes = transformTypes;
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
    
    private void doStop(boolean remove) {
        if (target != null && pending != target) {
        	if (remove) {
        		target.getChildren().remove(parent); //if delete parent will remove it anyway
        	}

            target = null;
            parent.removeFlag(TRACKING_TAG);
        }
    }
    
    private void doObserve() {
        if (pending == null || (target != null && target == pending)) {
            return;
        }
        
        target = pending;
        
        final ICanvasObject thisTarget = target;


        target.observeTransforms()
                  .takeWhile(t -> thisTarget == target)
                  .filter(t -> matchTypes.isEmpty() || matchTypes.contains(t.getClass()))
                  .subscribe(parent::replayTransform); 
        
        target.observeStatus()
                .takeWhile(t -> thisTarget == target)
                .filter(s -> s == ObjectStatus.DRAW_COMPLETE)
                .doFinally(this::setDeleted)
                .subscribe(s -> onDrawComplete());
              
    }

}
