package com.graphics.lib.canvas;

import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.interfaces.ICanvasObject;

public class TrackingCanvasObject extends CanvasObjectWrapper implements ITracker, Observer {
    public static final String TRACKING_TAG = "tracking";
    
    private ICanvasObject target;
    
    private ICanvasObject pending;
    
    public TrackingCanvasObject() {
        super();
    }
    
    public TrackingCanvasObject(ICanvasObject obj) {
        super(obj);
    }
    
    @Override
    public void setDeleted(boolean isDeleted) {
        stopObserving();
        doStop();
        super.setDeleted(isDeleted);
    }
    
    @Override
    public void observeAndMatch (ICanvasObject target) {
        pending = target;
    }
    
    @Override
    public void stopObserving() {
        pending = null;
    }
    
    @Override
    public void onDrawComplete() {
        //do in draw complete phase to minimise any conflicts (may be better in a before stage which doesn't currently exist)
        doStop();
        doObserve();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (target != null && target.isDeleted()) {
            pending = null;
            doStop();
        }
    }
    
    private void doStop() {
        if (target != null && pending == null) {
            target.getChildren().remove(this);
            synchronized(target.getVertexList()){
                target.getVertexList().removeIf(v -> v.hasTag(this.getObjectTag()));
                this.getVertexList().forEach(v -> v.removeTag(this.getObjectTag()));
            }
            target.deleteObserver(this);
            target = null;
            this.removeFlag(TRACKING_TAG);
        }
    }
    
    private void doObserve() {
        if (pending == null || (target != null && target.getObjectTag().equals(pending.getObjectTag() ))) {
            return;
        }
        
        target = pending;
        synchronized(target.getVertexList()){
            target.getChildren().add(this);
            this.getVertexList().forEach(v -> v.addTag(this.getObjectTag()));
            target.getVertexList().addAll(this.getVertexList());
        }
        target.addObserver(this);
        this.addFlag(TRACKING_TAG);
    }
}
