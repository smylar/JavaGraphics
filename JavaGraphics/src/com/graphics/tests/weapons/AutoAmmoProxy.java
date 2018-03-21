package com.graphics.tests.weapons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.IEffector;

public class AutoAmmoProxy implements InvocationHandler, Observer {

    private final AmmoHandler ammoHandler;
    private final IEffector weapon;
    
    public AutoAmmoProxy (IEffector weapon, AmmoHandler ammoHandler) {
        this.ammoHandler = ammoHandler;
        this.weapon = weapon;
    }
    
    public static IEffector weaponWithAmmo(IEffector weapon, AmmoHandler ammoHandler) {
        return (IEffector)Proxy.newProxyInstance(IEffector.class.getClassLoader(),
                            new Class[] {IEffector.class },
                            new AutoAmmoProxy(weapon, ammoHandler));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("activate")) {
            activate();
        } else if (method.getName().equals("deActivate")) {
            deActivate();
        } else {
            method.invoke(weapon, args);
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == weapon.getParent()) {
            if (arg == ObjectStatus.DRAW_COMPLETE) {
                weapon.activate();
            } else if (arg == ObjectStatus.DELETED) {
                deActivate();
            }
        }
    }
    
    private void activate() {
        if (ammoHandler.canFire()) {
            weapon.getParent().addObserver(this);
            weapon.activate();
        }
    }
    
    private void deActivate() {
        weapon.getParent().deleteObserver(this);
        weapon.deActivate();
    }

}
