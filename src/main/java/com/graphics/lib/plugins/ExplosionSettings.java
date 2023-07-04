package com.graphics.lib.plugins;

import com.graphics.lib.transform.Acceleration;

public record ExplosionSettings(Acceleration fragementAcceleration, int minForce, int maxForce) {

    public static ExplosionSettings getDefault() {
        return new ExplosionSettings(null, 2, 16);
    }
}
