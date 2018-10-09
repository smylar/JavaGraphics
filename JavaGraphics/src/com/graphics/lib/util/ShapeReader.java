package com.graphics.lib.util;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.Facet;

public class ShapeReader {
           
    private static final String VL = "shapes/%s.vl";
    private static final String FL = "shapes/%s.fl";
    private static final String SPLITTER = ",";
    
    private ShapeReader() {}
    
    public static ImmutableList<WorldCoord> getWorldCoordsFromResource(String resource) {
        return ArrayReader.getLineAsDoubleArray(String.format(VL, resource.toLowerCase()), SPLITTER)
                           .map(vx -> new WorldCoord(vx.get(0), vx.get(1), vx.get(2)))
                           .collectInto(ImmutableList.<WorldCoord>builder(), (l, v) -> l.add(v))
                           .blockingGet()
                           .build();
                   
    }
    
    public static ImmutableList<Facet> getFacetsFromResource(String resource, List<WorldCoord> vertexList) {
        return ArrayReader.getLineAsIntArray(String.format(FL, resource.toLowerCase()), SPLITTER)
                           .map(f -> new Facet(vertexList.get(f.get(0)), vertexList.get(f.get(1)), vertexList.get(f.get(2))))
                           .collectInto(ImmutableList.<Facet>builder(), (l, f) -> l.add(f))
                           .blockingGet()
                           .build();
                   
    }
}
