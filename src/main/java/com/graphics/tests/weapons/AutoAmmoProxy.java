package com.graphics.tests.weapons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.IEffector;

/**
 * Modifies firing logic so that it will attempt to fire multiple times while the weapon is active
 * governed by the provided ammunition rules
 * 
 * @author paul
 *
 */
public class AutoAmmoProxy implements InvocationHandler {

    private final IEffector weapon;
    private boolean activated = false;
    
    public AutoAmmoProxy (IEffector weapon, AmmoHandler ammoHandler) {
        this.weapon = weapon;
        weapon.getParent().observeStatus()
                          .doFinally(this::deActivate)
                          .filter(s -> s == ObjectStatus.DRAW_COMPLETE && activated && ammoHandler.canFire())
                          .subscribe(s -> weapon.activate());
    }
    
    public static IEffector weaponWithAmmo(IEffector weapon, AmmoHandler ammoHandler) {
        AmmoTracker.INSTANCE.add(weapon, ammoHandler);
        return (IEffector)Proxy.newProxyInstance(IEffector.class.getClassLoader(),
                            new Class[] {IEffector.class },
                            new AutoAmmoProxy(weapon, ammoHandler));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("activate".equals(method.getName())) {
            activated = true;
        } else if ("deActivate".equals(method.getName())) {
            deActivate();
        } else {
            return method.invoke(weapon, args);
        }
        return null;
    }
    
    private void deActivate() {
        activated = false;
        weapon.deActivate();
    }

}
