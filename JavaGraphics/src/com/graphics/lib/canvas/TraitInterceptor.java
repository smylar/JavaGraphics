package com.graphics.lib.canvas;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITrait;

public class TraitInterceptor implements InvocationHandler {
    
    private final ICanvasObject proxiedObject;
    
    private TraitInterceptor (ICanvasObject proxiedObject) {
        this.proxiedObject = proxiedObject;
    }
    
    public static ICanvasObject intercept (ICanvasObject target) {        
        return (ICanvasObject) Proxy.newProxyInstance(ICanvasObject.class.getClassLoader(),
                                new Class[] { ICanvasObject.class },
                                new TraitInterceptor(target));
    }
    
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object results = method.invoke(proxiedObject, args);
        
        
        proxiedObject.getTraits().stream().filter(trait -> trait.getInterceptors().containsKey(method.getName().toLowerCase()))
                                          .forEach(trait -> invokeTrait(trait, trait.getInterceptors().get(method.getName().toLowerCase()), args));
        
        return results;
    }
    
    private void invokeTrait(ITrait trait, Method method, Object[] args)
    {
        try {
            method.invoke(trait, args);
        } catch (Exception e){}
    }

    
}
