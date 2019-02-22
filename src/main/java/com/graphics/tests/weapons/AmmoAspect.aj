package com.graphics.tests.weapons;

import static com.graphics.tests.shapes.Ship.Hardpoints;
import java.lang.reflect.Proxy;

import com.graphics.lib.interfaces.IEffector;
import com.graphics.tests.shapes.Ship;

public aspect AmmoAspect {
    
//    IEffector around (IEffector ef) : execution((@WithAmmo IEffector)+.new(..)) && this(ef) { 
//        IEffector effector = proceed(ef);
//        if (Proxy.isProxyClass(effector.getClass())) {
//            return effector;
//        }
//        
//        WithAmmo ammoAnnotation = effector.getClass().getAnnotation(WithAmmo.class);
//        DefaultAmmoHandler handler = new DefaultAmmoHandler();
//        if (ammoAnnotation.count() > 0) {
//            handler.setAmmoCount(ammoAnnotation.count());
//        }
//        if (ammoAnnotation.ticks() > 0) {
//            handler.setTicksBetweenShots(ammoAnnotation.ticks());
//        }
//        return AmmoProxy.weaponWithAmmo(effector, handler);
//    }  
//    trying to proxy on instantiation but doesn't work as I can't return an interface type instead of the constructor type here
//    
    
    void around(Ship s, Hardpoints h, IEffector ef) : target(s) && args(h,ef) && execution(void addWeapon(Hardpoints,IEffector)) {

        if (!Proxy.isProxyClass(ef.getClass()) && ef.getClass().isAnnotationPresent(WithAmmo.class)) {
          //return effector;
            WithAmmo ammoAnnotation = ef.getClass().getAnnotation(WithAmmo.class);
            DefaultAmmoHandler handler = new DefaultAmmoHandler(ammoAnnotation.max());
            if (ammoAnnotation.ticks() > 0) {
                handler.setTicksBetweenShots(ammoAnnotation.ticks());
            }
            proceed(s, h, AmmoProxy.weaponWithAmmo(ef, handler));
        } else {
            proceed(s, h, ef);
        }
        
    }
}
