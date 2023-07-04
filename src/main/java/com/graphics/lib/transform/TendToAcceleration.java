package com.graphics.lib.transform;

import com.graphics.lib.Vector;
import org.apache.commons.lang3.tuple.Pair;

public record TendToAcceleration (Vector modVector, Vector targetVelocity) implements Acceleration {

    //modVector is almost but not quite an acceleration vector
    //as we are tending towards a vector it represents the maximum number of units a vector can change regardless of direction

    //attempt to reach a target vector by applying an acceleration in the required direction
    public Pair<Vector,Double> modify(Vector vector, double lastSpeed) {
        Vector velocity = vector.generateVelocity(lastSpeed); //TODO may need to spot zero vector
        //mind you we did already generate this in MovementTransform, may be able to pass

        Vector newVector = new Vector(modifyComponent(velocity.x(), targetVelocity.x(), modVector.x()),
                modifyComponent(velocity.y(), targetVelocity.y(), modVector.y()),
                modifyComponent(velocity.z(), targetVelocity.z(), modVector.z()));

        return Pair.of(newVector, newVector.getSpeed());
    }

    private double modifyComponent(double source, double target, double maxChange) {
        double dif = target - source;
        double modified = target;
        if (Math.abs(dif) > Math.abs(maxChange)) {
            modified = source + Math.copySign(maxChange, dif);
        }
        return modified;
    }

    @Override
    public boolean changesDirection() {
        return true;
    }
}
