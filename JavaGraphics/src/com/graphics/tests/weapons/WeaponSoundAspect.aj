package com.graphics.tests.weapons;

import com.graphics.lib.interfaces.IEffector;

public aspect WeaponSoundAspect {
    after(ProjectileWeapon weapon) : call(void IEffector.activate()) && target(weapon) {
        //TODO get sound based on projectile
        //also need to figure what to do if two weapons fire at the same time
        System.out.println("Fired " + weapon.getProjectile().getClass().getName());
    }
    
    after(LaserWeapon weapon) : call(void IEffector.activate()) && target(weapon) {
        //TODO play laser sound
        System.out.println("Fired laser");
    }
}
