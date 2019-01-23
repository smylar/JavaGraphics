package com.graphics.tests.weapons;

import com.graphics.lib.interfaces.IEffector;
import com.sound.ClipLibrary;

/**
 * Tries to match a weapon to a sound resource in order to play it when the weapon is activated.
 * If nothing is found it will try a default resource, or do nothing if that isn't found
 * 
 * @author paul.brandon
 *
 */
public aspect WeaponSoundAspect {
    
    private final ClipLibrary clipLibrary = ClipLibrary.getInstance();
    
    after(ProjectileWeapon weapon) : call(void IEffector.activate()) && target(weapon) {
        System.out.println("Fired " + weapon.getProjectile().getClass().getName());
        makeSound(weapon.getProjectile().getClass().getName());
    }
    
    after() returning(LaserEffect effect) : call(LaserEffect.new(..)) {
        System.out.println("Fired laser");
        makeSound("laser");
                
    }
    
    private void makeSound(String key) {
        clipLibrary.playSound(key)
                   .orElse(clipLibrary.playSound("DEFAULTWEAPON").orElse(null));
    }
}
