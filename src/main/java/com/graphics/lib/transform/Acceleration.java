package com.graphics.lib.transform;

import com.graphics.lib.Vector;
import org.apache.commons.lang3.tuple.Pair;

public interface Acceleration {
    Pair<Vector,Double> modify(Vector velocity, double currentSpeed);

    default boolean changesDirection()  { return false;}
}
