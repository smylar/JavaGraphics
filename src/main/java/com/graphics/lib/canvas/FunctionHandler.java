package com.graphics.lib.canvas;

import java.util.HashMap;
import java.util.Map;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Singleton that tracks which objects use which extension functions.
 * <br/>
 * Probably not as quick handling functions here, but keeps them completely separate from CanvasObject
 * 
 * @author Paul Brandon
 *
 */
public class FunctionHandler {
    private static final FunctionHandler INSTANCE = new FunctionHandler();
    
    private final Map<ICanvasObject, CanvasObjectFunctionsImpl> functions = new HashMap<>();
    
    private FunctionHandler() { }
    
    public static void register(final ICanvasObject obj, final CanvasObjectFunctionsImpl impl) {
        INSTANCE.doRegister(obj, impl);
    }
    
    public static void register(final ICanvasObject obj, final CanvasObjectFunctions impl) {
        INSTANCE.doRegister(obj, impl.get());
    }
    
    /**
     * Gets the registered function object for this object or a default if it doesn't have one
     * @param obj
     * @return
     */
    public static CanvasObjectFunctionsImpl getFunctions(final ICanvasObject obj) {
        return INSTANCE.doGetFunctions(obj);
    }
    
    public void doRegister(final ICanvasObject obj, final CanvasObjectFunctionsImpl impl) {
        functions.put(obj, impl);
        obj.observeDeath()
            .subscribe(d -> functions.remove(obj));
    }

    public CanvasObjectFunctionsImpl doGetFunctions(final ICanvasObject obj) {
        return functions.getOrDefault(obj, CanvasObjectFunctions.DEFAULT.get());
    }
}
