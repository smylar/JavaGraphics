package com.graphics.tests.weapons;

public interface AmmoHandler {
    public boolean canFire();
    
    public int getAmmoCount();
    
    public void addAmmo(int count);
}
