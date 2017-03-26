package com.graphics.lib.canvas;

import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITracker;

public class TrackingCanvasObject extends CanvasObject implements ITracker {
	public static final String TRACKING_TAG = "tracking_tag";
	
    private ICanvasObject target;
    
    private ICanvasObject pending;
    
    public TrackingCanvasObject() {
        super();
    }
    
    public TrackingCanvasObject(ICanvasObject obj) {
        super(obj);
    }
    
    @Override
    public void setDeleted(boolean isDeleted){
    	if (isDeleted) {
    		pending = null;
    		doStop(false);
    	}
    	super.setDeleted(isDeleted);
    }
    
    @Override
    public void observeAndMatch (ICanvasObject target) {
        pending = target;
        synchronized(pending.getChildren()) {
        	this.addFlag(TRACKING_TAG);
        	pending.getChildren().add(this);
        }
    }
    
    @Override
    public void stopObserving() {
        pending = null;
    }
    
    @Override
    public void onDrawComplete() {
        //do in draw complete phase to minimise any conflicts (may be better in a before stage which doesn't currently exist)
    	
        doStop(true);
        doObserve();
        super.onDrawComplete();
    }
    
    private void doStop(boolean remove) {
        if (target != null && pending == null) {
        	if (remove) {
        		target.getChildren().remove(this); //if delete parent will remove it anyway
        	}
        	target.getVertexList().removeIf(v -> v.hasTag(this.getObjectTag()));
			this.getVertexList().forEach(v -> v.removeTag(this.getObjectTag()));
            target = null;
            this.removeFlag(TRACKING_TAG);
        }
    }
    
    private void doObserve() {
        if (pending == null || (target != null && target.getObjectTag().equals(pending.getObjectTag() ))) {
            return;
        }
        
        target = pending;
    	
		this.getVertexList().forEach(v -> v.addTag(this.getObjectTag()));
		target.getVertexList().addAll(this.getVertexList());
    }
}
