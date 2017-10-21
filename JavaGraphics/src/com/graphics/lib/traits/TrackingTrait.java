package com.graphics.lib.traits;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.canvas.TraitInterceptor;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITracker;
import com.graphics.lib.transform.Transform;

/**
 * Trait whereby an object will completely mirror the transforms applied to another object<br/>
 * Note this will be achieved by adding the trackers vertex list to the target's list at the draw complete phase
 * 
 * @author paul.brandon
 *
 */
public class TrackingTrait implements ITracker, Observer  {
    private static final Map<String, Method> interceptors = new HashMap<>();
	public static final String TRACKING_TAG = "tracking_tag";
	
	static {
	    try {
	        //could make this attribute based, can then ensure correct spellings for the target and lowercase for the key
	        interceptors.put("setdeleted", TrackingTrait.class.getMethod("setDeleted", boolean.class));
	        interceptors.put("ondrawcomplete", TrackingTrait.class.getMethod("onDrawComplete"));
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage());
	    }
	}
	
	private ICanvasObject parent; //the object this trait belongs to
	
    private ICanvasObject target;
    
    private ICanvasObject pending;
    
    public void setDeleted(boolean isDeleted){
    	if (isDeleted) {
    		pending = null;
    		doStop(false);
    	}
    }
    
    @Override
    public void observeAndMatch (ICanvasObject target) {
        pending = target;
        synchronized(pending.getChildren()) {
        	parent.addFlag(TRACKING_TAG);
        	pending.getChildren().add(Proxy.isProxyClass(parent.getClass()) ? parent : TraitInterceptor.intercept(parent)); 
        	//may need to check proxy is the trait interceptor, but that is the only proxy we have at the moment
        }
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
    
    @Override
    public Map<String, Method> getInterceptors() {
        return interceptors;
    }
    
    private void doStop(boolean remove) {
        if (target != null && pending == null) {
        	if (remove) {
        		target.getChildren().remove(this); //if delete parent will remove it anyway
        	}
//        	target.getVertexList().removeIf(v -> v.hasTag(parent.getObjectTag()));
//			parent.getVertexList().forEach(v -> v.removeTag(parent.getObjectTag()));
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
    	
//		parent.getVertexList().forEach(v -> v.addTag(parent.getObjectTag()));
//		target.getVertexList().addAll(parent.getVertexList());
        target.addObserver(this);
    }

	@Override
	public void update(Observable obs, Object payload) {
		if (payload instanceof Transform) {
			parent.replayTransform((Transform)payload);
		}
		
	}

}
