package com.graphics.tests.weapons;

import java.util.Optional;

import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;

public interface IWeaponised extends ICanvasObject {

    Optional<IEffector> getWeapon(final String id);
    
    public Optional<WorldCoord> getWeaponLocation(final String id);
}
