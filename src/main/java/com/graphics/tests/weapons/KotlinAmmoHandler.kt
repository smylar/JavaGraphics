package com.graphics.tests.weapons

import com.graphics.lib.canvas.Canvas3D

//Kotlin straight copy of DefaultAmmoHandler, my first go in anger
class KotlinAmmoHandler(private val maxCount: Int,
                        private var ammoCount: Int,
                        private val ticksBetweenShots: Int = 20,
                        private var lastShotTick: Long = -1000) : AmmoHandler {
    
    constructor(maxCount: Int, ticksBetweenShots: Int) : this(maxCount, maxCount, ticksBetweenShots) {}
    
     fun setAmmoCount(ammoCount: Int): KotlinAmmoHandler {
        this.ammoCount = if (ammoCount > maxCount) maxCount else ammoCount;
        return this;
    }
    
    override fun getAmmoCount(): Int {
        return ammoCount;
    }
    
    override fun canFire(): Boolean {
        println("Kotlin!");
        return Canvas3D.get()
            ?.let{c -> c.getTicks()}
            ?.takeIf{ticks -> ammoCount > 0 && ticks - lastShotTick > ticksBetweenShots}
            ?.let{
                lastShotTick = it;
                ammoCount--;
                true;
            }?: false;

    }
    
    override fun addAmmo(count: Int) {
        ammoCount = if (ammoCount + count > maxCount) maxCount else ammoCount + count;
    }
}