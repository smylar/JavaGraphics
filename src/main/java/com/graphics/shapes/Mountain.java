package com.graphics.shapes;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Mountain extends CanvasObject {

    public Mountain(int levels, double height, double width, double variation) {
        super(self -> init(levels, height, width, variation), Mountain.class);
        fixCentre();
    }

    private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(int levels, double height, double radius, double variation) {
        //use a fractal method, repeatedly divide triangles moving the height by a random amount each time

        double z = Math.sin(Math.toRadians(60))*radius;
        double halfRadius = radius/2;
        //these form equilateral triangles in the x-z plane with length of each side equal to radius
        WorldCoord coord1 = new WorldCoord(0,-height,0);
        WorldCoord coord2 = new WorldCoord(radius,0,0);
        WorldCoord coord3 = new WorldCoord(halfRadius,0,z);
        WorldCoord coord4 = new WorldCoord(-halfRadius,0,z);
        WorldCoord coord5 = new WorldCoord(-radius,0,0);
        WorldCoord coord6 = new WorldCoord(-halfRadius,0,-z);
        WorldCoord coord7 = new WorldCoord(halfRadius,0,-z);

        Stream<Facet> initial = Stream.of( //forms 6 triangles around central point coord1, forms a single central peak
            new Facet(coord1, coord2, coord3),
            new Facet(coord1, coord3, coord4),
            new Facet(coord1, coord4, coord5),
            new Facet(coord1, coord5, coord6),
            new Facet(coord1, coord6, coord7),
            new Facet(coord1, coord7, coord2));

        Pair<Set<WorldCoord>, List<Facet>> mountain = MountainUtils.generateFractals(levels, variation, initial);

        //really should refactor away from Guava on CanvasObject lists now! copying immutable list to immutable list!!!
        //just immutableList makes it clear what is required without the parent having to test for it
        return Pair.of(ImmutableList.copyOf(mountain.getLeft()), ImmutableList.copyOf(mountain.getRight()));
    }
}
