package com.graphics.shapes;

import com.graphics.lib.Facet;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MountainUtils {
    private MountainUtils(){}

    public static Pair<Set<WorldCoord>, List<Facet>> generateFractals(int levels, double variation, Stream<Facet> startOutline) {
        final Set<SharedPoint> sharedPoints = new HashSet<>();

        List<Facet> facets = startOutline
                .flatMap(f -> divideAndRaise(f, 1, levels, variation, sharedPoints))
                .toList();

        Map<WorldCoord, Long> coordCounts = facets.stream()
                .flatMap(f -> f.getAsList().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        coordCounts.entrySet()
                .stream()
                .filter(e -> e.getValue() < 6)
                .forEach(e -> e.getKey().y = 0); //set all edge coords to 0 so they join the floor

        return Pair.of(coordCounts.keySet(), facets);
    }

    public static Mountain setSnowLine(Mountain mountain, int above, Color colour) {
        List<WorldCoord> pointsInRange = mountain.getVertexList().stream().filter(v -> v.y < -above).toList(); //inverted y

        mountain.getFacetList().stream()
                .filter(f -> pointsInRange.contains(f.first()) || pointsInRange.contains(f.second()) || pointsInRange.contains(f.third()))
                .forEach(f -> f.setColour(colour));

        return mountain;
    }

    private static Stream<Facet> divideAndRaise(Facet facet, final int level, final int maxLevel, final double variation, final Set<SharedPoint> sharedPoints) {
        List<WorldCoord> fCoords = facet.getAsList();
        double levelVariation = variation/level;
        WorldCoord mid1 = getRaisedMidpoint(fCoords.get(0), fCoords.get(1), levelVariation, sharedPoints);
        WorldCoord mid2 = getRaisedMidpoint(fCoords.get(1), fCoords.get(2), levelVariation, sharedPoints);
        WorldCoord mid3 = getRaisedMidpoint(fCoords.get(2), fCoords.get(0), levelVariation, sharedPoints);

        Stream<Facet> facets = Stream.of(
                new Facet(fCoords.get(0), mid1, mid3),
                new Facet(fCoords.get(1), mid2, mid1),
                new Facet(fCoords.get(2), mid3, mid2),
                new Facet(mid1, mid2, mid3));

        if (level == maxLevel) {
            return facets;
        }

        return facets.flatMap(f -> divideAndRaise(f, level+1, maxLevel, variation, sharedPoints));
    }

    private static WorldCoord getRaisedMidpoint(WorldCoord wc1, WorldCoord wc2, double variation, Set<SharedPoint> sharedPoints) {
        return sharedPoints.stream()
                .flatMap(sp -> sp.getSharedMidPoint(wc1, wc2).stream())//may want some better addressing rather than filtering, but isn't run after object initialised
                .findFirst()
                .orElseGet(() -> {
                    Vector v = wc1.vectorToPoint(wc2);
                    double raiseAmount = Math.random() * (variation*2) - variation;
                    double y = wc1.y + v.y()/2 - raiseAmount;
                    WorldCoord midPoint = new WorldCoord(wc1.x + v.x()/2, y > 0 ? 0 : y, wc1.z + v.z()/2); //n.b. world coords still flipped so decreasing y values are the upwards direction
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
