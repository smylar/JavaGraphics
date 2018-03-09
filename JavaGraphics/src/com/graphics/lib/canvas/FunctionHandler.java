package com.graphics.lib.canvas;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.Utils;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Singleton that tracks which objects use which extension functions.
 * <br/>
 * Probably not as quick handling functions here, but keeps them completely separate from CanvasObject
 * 
 * @author Paul Brandon
 *
 */
public class FunctionHandler implements Observer {
    private static final FunctionHandler INSTANCE = new FunctionHandler();
    
    private final Map<ICanvasObject, CanvasObjectFunctionsImpl> functions = new HashMap<>();
    
    private FunctionHandler() { }
    
    public static void register(ICanvasObject obj, CanvasObjectFunctionsImpl impl) {
        INSTANCE.doRegister(obj, impl);
    }
    
    public static void register(ICanvasObject obj, CanvasObjectFunctions impl) {
        INSTANCE.doRegister(obj, impl.get());
    }
    
    /**
     * Gets the registered function object for this object or a default if it doesn't have one
     * @param obj
     * @return
     */
    public static CanvasObjectFunctionsImpl getFunctions(ICanvasObject obj) {
        return INSTANCE.doGetFunctions(obj);
    }
    
    public void doRegister(ICanvasObject obj, CanvasObjectFunctionsImpl impl) {
        obj.addObserver(this);
        functions.put(obj, impl);
    }

    public CanvasObjectFunctionsImpl doGetFunctions(ICanvasObject obj) {
        return functions.getOrDefault(obj, CanvasObjectFunctions.DEFAULT.get());
    }
    
    @Override
    public void update(Observable obs, Object arg) {
        Utils.cast(obs, ICanvasObject.class)
             .filter(ICanvasObject::isDeleted)
             .ifPresent(o -> {
                functions.remove(o);
                o.deleteObserver(this);
             });
    }
}
