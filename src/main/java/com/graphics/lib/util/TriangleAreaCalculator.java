package com.graphics.lib.util;

import com.graphics.lib.Point;

public class TriangleAreaCalculator {

    public static double getArea(final Point p1, final Point p2, final Point p3) {
        //herons formula
        double l1 = p1.screenDistanceTo(p2);
        double l2 = p2.screenDistanceTo(p3);
        double l3 = p3.screenDistanceTo(p1);

        //semi perimiter
        double s = (l1 + l2 + l3)/2;
        
        return Math.sqrt(s * (s-l1) * (s-l2) * (s-l3));
    }
}
