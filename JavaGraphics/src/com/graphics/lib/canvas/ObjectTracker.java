package com.graphics.lib.canvas;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.graphics.lib.interfaces.ICanvasObject;

public class ObjectTracker implements InvocationHandler {
    
    public static final String TRACKING_TAG = "tracking";
    
    private static final Map<ICanvasObject, Set<ICanvasObject>> trackerMap = Collections.synchronizedMap(new HashMap<>());

    private final ICanvasObject proxiedObject;
    
    private ObjectTracker (ICanvasObject proxiedObject) {
        this.proxiedObject = proxiedObject;
    }
    
    /**
     * Set up a tracker to track a target
     * 
     * @param target The object to track 
     * @param tracker The object doing the tracking
     * @return An optional containing the proxy instance IF A NEW ONE HAS BEEN CREATED, an empty optional otherwise
     */
    public static Optional<ICanvasObject> track (ICanvasObject target, ICanvasObject tracker) {
        //possible improvement - may want to check for cyclic tracking - which will stop everything while two objects call invoke on each other forever
        Optional<ICanvasObject> ret = Optional.empty();
        if (!trackerMap.containsKey(target)) {
            trackerMap.put(target, new HashSet<>());
            
            ICanvasObject proxy = (ICanvasObject) Proxy.newProxyInstance(ICanvasObject.class.getClassLoader(),
                                    new Class[] { ICanvasObject.class },
                                    new ObjectTracker(target));
            
            ret = Optional.of(proxy);
        }
        
        tracker.addFlag(TRACKING_TAG);
        
        return ret;
    }
    
    public static void stop(ICanvasObject target, ICanvasObject tracker) {
        if (trackerMap.containsKey(target)) {
            trackerMap.get(target).remove(tracker);
        }
        tracker.removeFlag(TRACKING_TAG);
        //note currently object will stay proxied even if we remove the last tracker
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object results = method.invoke(proxiedObject, args);
        /*if ("applyTransform".equals(method.getName())) {
            trackerMap.get(this).forEach(tracker -> {
                try {
                    method.invoke(tracker, args);
                } catch (Exception e) {}
            });
        }*/
        //this won't really work for forwarding transforms, as transforms could be object specific or will change internal data
        //so a transfom would have to be applied to a single merged object
        
        return results;
    }

    
}
