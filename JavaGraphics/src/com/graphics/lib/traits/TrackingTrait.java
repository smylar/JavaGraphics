package com.graphics.lib.traits;

import java.util.Observable;
import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITracker;

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
    
    public TrackingTrait(ICanvasObject parent) {
        this.parent = parent;
    }
    
    public void setDeleted() {
        System.out.println("TrackingTrait::setDeleted");
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
        System.out.println("TrackingTrait::onDrawComplete");
        doStop(true);
        doObserve();
    }
    
//    @Override
//    public void setParent(ICanvasObject parent) {
//        this.parent = parent;
//        
//    }
    
    private void doStop(boolean remove) {
        if (target != null && pending != target) {
        	if (remove) {
        		target.getChildren().remove(this); //if delete parent will remove it anyway
        	}

        	//target.deleteObserver(this);
            target = null;
            parent.removeFlag(TRACKING_TAG);
        }
    }
    
    private void doObserve() {
        if (pending == null || (target != null && target == pending)) {
            return;
        }
        
        target = pending;
        
        //target.addObserver(this);
        
        final ICanvasObject thisTarget = target;


        target.observeTransforms()
                  .takeUntil(t -> thisTarget != target || parent.isDeleted())
                  .subscribe(parent::replayTransform); 
        //this can leave the subscription in place if the target doesn't move as the condition is checked after an emission
        //though it will complete as soon as it does move
        //may need to combine with an existence stream on the parent
        
        target.observeStatus()
                .takeUntil(s -> thisTarget != target || parent.isDeleted())
                .filter(s -> s == ObjectStatus.DRAW_COMPLETE)
                .doFinally(this::setDeleted)
                .subscribe(s -> onDrawComplete());
              
    }

	@Override
	public void update(Observable obs, Object payload) {
	    //will be removed once Observer interface removed from ITrait 
//		if (payload instanceof Transform) {
//			parent.replayTransform((Transform)payload);
//		} else if (payload == ObjectStatus.DELETED) {
//		    setDeleted();
//		} else if (payload == ObjectStatus.DRAW_COMPLETE) {
//		    onDrawComplete();
//		}
//		
	}

}
