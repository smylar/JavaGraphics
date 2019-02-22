package com.graphics.tests.weapons;

import java.util.function.Supplier;

import com.graphics.lib.interfaces.IOrientation;

@WithAmmo(max=15)
public class ExtendedMagazineWeapon extends ProjectileWeapon {

    public ExtendedMagazineWeapon(String id, Projectile proj, Supplier<IOrientation> effectVector, IWeaponised parent) {
        super(id, proj, effectVector, parent);
    }

}
