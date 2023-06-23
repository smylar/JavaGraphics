package com.graphics.shapes;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Mountain extends CanvasObject {

    public Mountain(int levels, double height, double width) {
        super(self -> init(levels, height, width), Mountain.class);
    }

    private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(int levels, double height, double width) {
        //use a fractal method, repeatedly divide triangles moving the height by a random amount each time

        double z = Math.sin(Math.toRadians(60))*width;
        double halfWidth = width/2;
        //these form equilateral triangles in the x-z plane with length of each side equal to width
        WorldCoord coord1 = new WorldCoord(0,-height,0);
        WorldCoord coord2 = new WorldCoord(width,0,0);
        WorldCoord coord3 = new WorldCoord(halfWidth,0,z);
        WorldCoord coord4 = new WorldCoord(-halfWidth,0,z);
        WorldCoord coord5 = new WorldCoord(-width,0,0);
        WorldCoord coord6 = new WorldCoord(-halfWidth,0,-z);
        WorldCoord coord7 = new WorldCoord(halfWidth,0,-z);

        final Set<SharedPoint> sharedPoints = new HashSet<>();
        List<Facet> facets = Stream.of( //forms 6 triangles around central point coord1
            new Facet(coord1, coord2, coord3),
            new Facet(coord1, coord3, coord4),
            new Facet(coord1, coord4, coord5),
            new Facet(coord1, coord5, coord6),
            new Facet(coord1, coord6, coord7),
            new Facet(coord1, coord7, coord2))
                .flatMap(f -> divideAndRaise(f, 1, levels, sharedPoints))
                .toList();

        //TODO transform generated shape to match given height either squishing or elongating
        //get highest point work out a multiplier and apply to all y values

        //extract unique vertices
        List<WorldCoord> vl = facets.stream()
                .flatMap(f -> f.getAsList().stream())
                .distinct()
                .toList();

        //TODO colour top of mountain a different colour e.g. for snow capped effect
        //really should refactor away from Guava on CanvasObject lists now! copying immutable list to immutable list!!!
        return Pair.of(ImmutableList.copyOf(vl), ImmutableList.copyOf(facets));
        // need to make transition into floor a lot nicer there are gaps around the base,
        // may be able to use further triangles with different variation settings to smooth things out around the sides
    }

    private static Stream<Facet> divideAndRaise(Facet facet, final int level, final int maxLevel, final Set<SharedPoint> sharedPoints) {
        List<WorldCoord> fCoords = facet.getAsList();
        WorldCoord mid1 = getRaisedMidpoint(fCoords.get(0), fCoords.get(1), level, sharedPoints);
        WorldCoord mid2 = getRaisedMidpoint(fCoords.get(1), fCoords.get(2), level, sharedPoints);
        WorldCoord mid3 = getRaisedMidpoint(fCoords.get(2), fCoords.get(0), level, sharedPoints);

        Stream<Facet> facets = Stream.of(
                new Facet(fCoords.get(0), mid1, mid3),
                new Facet(fCoords.get(1), mid2, mid1),
                new Facet(fCoords.get(2), mid3, mid2),
                new Facet(mid1, mid2, mid3));

        if (level == maxLevel) {
            return facets;
        }

        return facets.flatMap(f -> divideAndRaise(f, level+1, maxLevel, sharedPoints));
    }

    private static WorldCoord getRaisedMidpoint(WorldCoord wc1, WorldCoord wc2, int level, Set<SharedPoint> sharedPoints) {
        return sharedPoints.stream()
                .map(sp -> sp.getSharedMidPoint(wc1, wc2))//may want some better addressing rather than filtering, but isn't run after object initialised
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(() -> {
                    Vector v = wc1.vectorToPoint(wc2);
                    double raiseAmount = (-100d/level) + Math.random() * (200d/level); //probably should be some function of height
                    WorldCoord midPoint = new WorldCoord(wc1.x + v.x()/2, wc1.y + v.y()/2 - raiseAmount, wc1.z + v.z()/2); //n.b. world coords still flipped so decreasing y values are the upwards direction
                    sharedPoints.add(new SharedPoint(wc1, wc2, midPoint));
                    return midPoint;
                });
    }

    private record SharedPoint(WorldCoord a, WorldCoord b, WorldCoord midPoint) {
        Optional<WorldCoord> getSharedMidPoint(WorldCoord c, WorldCoord d) {
            return (a == c || a == d) && (b == c || b == d) ? Optional.of(midPoint) : Optional.empty();
        }
    }
}
