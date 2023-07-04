package com.graphics.lib.transform;

import com.graphics.lib.Vector;
import org.apache.commons.lang3.tuple.Pair;

public record InlineAcceleration(double accelerationValue, double maxSpeed) implements Acceleration {
    //apply acceleration to a Vector in the direction it is moving up to a max speed
    //if max speed is zero accelerate forever!

    @Override
    public Pair<Vector,Double> modify(final Vector unit, final double currentSpeed) {
        //using separate speed so minus values can be used (for reversing), speed on vectors is always positive

        double newSpeed = currentSpeed + accelerationValue;
        if (maxSpeed != 0 && newSpeed > maxSpeed) {
            newSpeed = maxSpeed;
        }
        return Pair.of(unit.generateVelocity(newSpeed), newSpeed);
    }
}
