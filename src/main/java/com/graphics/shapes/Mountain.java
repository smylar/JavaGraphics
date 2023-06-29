package com.graphics.shapes;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mountain extends CanvasObject {

    public Mountain(int levels, double height, double width) {
        super(self -> init(levels, height, width), Mountain.class);
        fixCentre();
    }

    private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(int levels, double height, double radius) {
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


        Map<WorldCoord, Long> coordCounts = facets.stream()
                .flatMap(f -> f.getAsList().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        coordCounts.entrySet()
                .stream()
                .filter(e -> e.getValue() < 6)
                .forEach(e -> e.getKey().y = 0); //set all edge coords to 0 so they join the floor
        //though further triangles around the central 6 could help smooth this out

        //really should refactor away from Guava on CanvasObject lists now! copying immutable list to immutable list!!!
        //just immutableList makes it clear what is required without the parent having to test for it
        return Pair.of(ImmutableList.copyOf(coordCounts.keySet()), ImmutableList.copyOf(facets));
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
                    double raiseAmount = (-100d/level) + Math.random() * (200d/level); //probably should be some function of height or configurable
                    double y = wc1.y + v.y()/2 - raiseAmount;
                    WorldCoord midPoint = new WorldCoord(wc1.x + v.x()/2, y > 0 ? 0 : y, wc1.z + v.z()/2); //n.b. world coords still flipped so decreasing y values are the upwards direction
                    sharedPoints.add(new SharedPoint(wc1, wc2, midPoint));
                    return midPoint;
                });
    }

    public Mountain setSnowLine(int above, Color colour) {
        List<WorldCoord> pointsInRange = getVertexList().stream().filter(v -> v.y < -above).toList(); //inverted y

        getFacetList().stream()
                .filter(f -> pointsInRange.contains(f.first()) || pointsInRange.contains(f.second()) || pointsInRange.contains(f.third()))
                .forEach(f -> f.setColour(colour));

        return this;
    }

    private record SharedPoint(WorldCoord a, WorldCoord b, WorldCoord midPoint) {
        Optional<WorldCoord> getSharedMidPoint(WorldCoord c, WorldCoord d) {
            return (a == c || a == d) && (b == c || b == d) ? Optional.of(midPoint) : Optional.empty();
        }
    }
}
