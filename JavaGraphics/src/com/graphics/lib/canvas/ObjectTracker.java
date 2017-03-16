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
    
    private static final Map<ICanvasObject, ICanvasObject> proxyMap = Collections.synchronizedMap(new HashMap<>());

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
        ICanvasObject actual = getActual(target);

        Optional<ICanvasObject> ret = Optional.empty();
        if (!trackerMap.containsKey(actual)) {
            trackerMap.put(actual, new HashSet<>());
            
            ICanvasObject proxy = (ICanvasObject) Proxy.newProxyInstance(ICanvasObject.class.getClassLoader(),
                                    new Class[] { ICanvasObject.class },
                                    new ObjectTracker(actual));
            
            proxyMap.put(proxy, actual);
            ret = Optional.of(proxy);
        }
        
        trackerMap.get(actual).add(tracker);
        tracker.addFlag(TRACKING_TAG);
        
        return ret;
    }
    
    public static void stop(ICanvasObject target, ICanvasObject tracker) {
        ICanvasObject actual = getActual(target);
        if (trackerMap.containsKey(actual)) {
            trackerMap.get(actual).remove(tracker);
        }
        tracker.removeFlag(TRACKING_TAG);
        //note currently object will stay proxied even if we remove the last tracker
    }
    
    private static ICanvasObject getActual(ICanvasObject target) {
        return proxyMap.containsKey(target) ? proxyMap.get(target) : target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object results = method.invoke(proxiedObject, args);
        if ("applyTransform".equals(method.getName())) {
            trackerMap.get(this).forEach(tracker -> {
                try {
                    method.invoke(tracker, args);
                } catch (Exception e) {}
            });
        }
        
        return results;
    }

    
}
