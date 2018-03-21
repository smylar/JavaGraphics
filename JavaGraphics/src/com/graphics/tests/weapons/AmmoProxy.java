package com.graphics.tests.weapons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.graphics.lib.interfaces.IEffector;

public class AmmoProxy implements InvocationHandler {

    private final AmmoHandler ammoHandler;
    private final IEffector weapon;
    
    public AmmoProxy (IEffector weapon, AmmoHandler ammoHandler) {
        this.ammoHandler = ammoHandler;
        this.weapon = weapon;
    }
    
    public static IEffector weaponWithAmmo(IEffector weapon, AmmoHandler ammoHandler) {
        return (IEffector)Proxy.newProxyInstance(IEffector.class.getClassLoader(),
                            new Class[] {IEffector.class },
                            new AmmoProxy(weapon, ammoHandler));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("activate") && ammoHandler.canFire()) {
            weapon.activate();
        } else {
            method.invoke(weapon, args);
        }
        return null;
    }

}
