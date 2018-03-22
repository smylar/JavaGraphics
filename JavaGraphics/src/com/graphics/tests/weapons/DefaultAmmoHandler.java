package com.graphics.tests.weapons;

import java.util.Optional;

import com.graphics.lib.canvas.Canvas3D;

/**
 * Decides whether a weapon can fire based on rate of fire and available ammo
 * 
 * @author paul
 *
 */
public class DefaultAmmoHandler implements AmmoHandler {

    private int ammoCount = 10;
    private int ticksBetweenShots = 20;
    private long lastShotTick = -1000;
    
    @Override
    public int getAmmoCount() {
        return ammoCount;
    }

    public DefaultAmmoHandler setAmmoCount(int ammoCount) {
        this.ammoCount = ammoCount;
        return this;
    }
    
    public DefaultAmmoHandler setTicksBetweenShots(int ticksBetweenShots) {
        this.ticksBetweenShots = ticksBetweenShots;
        return this;
    }
    
    @Override
    public boolean canFire() {
        int curAmmo = ammoCount;
        Optional.ofNullable(Canvas3D.get())
                .map(Canvas3D::getTicks)
                .filter(ticks -> ammoCount > 0 && ticks - lastShotTick > ticksBetweenShots)
                .ifPresent(ticks -> {
                    lastShotTick = ticks;
                    ammoCount--;
                });
        return ammoCount < curAmmo;
    }

}
