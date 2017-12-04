package com.graphics.tests.weapons;

import java.util.Observable;
import java.util.Observer;
import java.util.function.Supplier;

import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.interfaces.IPointFinder;

/**
 * A weapon that will repeatedly generate {@link Projectile} objects when activated until deactivated
 * 
 * @author paul.brandon
 *
 */
public final class AutoProjectileWeapon extends ProjectileWeapon implements Observer {
    
    public AutoProjectileWeapon(Projectile proj, IPointFinder origin, Supplier<IOrientation> effectVector, ICanvasObject parent) {
        super(proj, origin, effectVector, parent);
    }

    @Override
    public void activate() {
        getParent().addObserver(this);
    }
    
    @Override
    public void deActivate() {
        getParent().deleteObserver(this);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (o == getParent()) {
            if (arg == ObjectStatus.DRAW_COMPLETE) {
                super.activate();
            } else if (arg == ObjectStatus.DELETED) {
                super.deActivate();
            }
        }
    }

}
