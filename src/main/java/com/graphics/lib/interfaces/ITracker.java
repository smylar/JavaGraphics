package com.graphics.lib.interfaces;

import java.util.Set;

import com.graphics.lib.transform.Transform;

public interface ITracker extends ITrait {

    /**
     * Observe another object and match its movements
     * <br/>
     * Adds object's vertex list to the parent, avoids issues such as double counting when reusing a transform
     * <br/>
     * <br/>
     * Note this object will be made a child of that it is observing so that it is always processed after the observed item
     * 
     * @param o
     */
    
    void observeAndMatch(ICanvasObject target, Set<Class<? extends Transform>> transformType);

    void stopObserving();

}
