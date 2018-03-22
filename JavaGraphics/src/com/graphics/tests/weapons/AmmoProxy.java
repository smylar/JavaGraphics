package com.graphics.tests.weapons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.graphics.lib.interfaces.IEffector;

/**
 * Proxy class that can be put in front of an {@link IEffector} instance
 * to modify the firing logic of the weapon via ammunition logic provided by a {@link AmmoHandler} instance
 * 
 * @author paul
 *
 */
public class AmmoProxy implements InvocationHandler {

    private final AmmoHandler ammoHandler;
    private final IEffector weapon;
    
    public AmmoProxy (IEffector weapon, AmmoHandler ammoHandler) {
        this.ammoHandler = ammoHandler;
        this.weapon = weapon;
    }
    
    public static IEffector weaponWithAmmo(IEffector weapon, AmmoHandler ammoHandler) {
        AmmoTracker.INSTANCE.add(weapon, ammoHandler);
        return (IEffector)Proxy.newProxyInstance(IEffector.class.getClassLoader(),
                            new Class[] {IEffector.class },
                            new AmmoProxy(weapon, ammoHandler));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("activate")) {
            if (ammoHandler.canFire()) {
                weapon.activate();
            }
        } else {
            method.invoke(weapon, args);
        }
        return null;
    }

}
