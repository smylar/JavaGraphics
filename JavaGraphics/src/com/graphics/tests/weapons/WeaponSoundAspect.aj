package com.graphics.tests.weapons;

import com.graphics.lib.interfaces.IEffector;
import com.sound.ClipLibrary;

public aspect WeaponSoundAspect {
    
    private final ClipLibrary clipLibrary = ClipLibrary.getInstance();
    
    after(ProjectileWeapon weapon) : call(void IEffector.activate()) && target(weapon) {
        System.out.println("Fired " + weapon.getProjectile().getClass().getName());
        makeSound(weapon.getProjectile().getClass().getName());
    }
    
    after(LaserWeapon weapon) : call(void IEffector.activate()) && target(weapon) {
        System.out.println("Fired laser");
        makeSound("laser");
    }
    
    private void makeSound(String key) {
        clipLibrary.playSound(key)
                   .orElse(clipLibrary.playSound("DEFAULTWEAPON").orElse(null));
    }
}
