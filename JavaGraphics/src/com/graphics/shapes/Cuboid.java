package com.graphics.shapes;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.transform.Translation;

public class Cuboid extends CanvasObject{
	
	public Cuboid (int height, int width, int depth)
	{
	    super(() -> init(height, width, depth), c -> {
		    										c.applyTransform(new Translation(-width/2,-height/2,-depth/2));
		    										c.fixCentre();
	    										});
	}
	
	private static Pair<List<WorldCoord>, List<Facet>> init(int height, int width, int depth) {
		List<WorldCoord> vertexList = generateVertexList(height, width, depth);
		return Pair.of(vertexList, generateFacetList(vertexList));
	}
	
	private static List<WorldCoord> generateVertexList(int height, int width, int depth)
	{
		return Lists.newArrayList(new WorldCoord(0, 0, 0),
								  new WorldCoord(0, height, 0),
								  new WorldCoord(width, height, 0),
								  new WorldCoord(width, 0, 0),
								  new WorldCoord(0, 0, depth),
								  new WorldCoord(0, height, depth),
								  new WorldCoord(width, height, depth),
								  new WorldCoord(width, 0, depth));
	}
	
	private static List<Facet> generateFacetList(List<WorldCoord> vertexList)
	{
		return Lists.newArrayList(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(3)),
								  new Facet(vertexList.get(1), vertexList.get(2), vertexList.get(3)),
								  new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(4)),
								  new Facet(vertexList.get(3), vertexList.get(7), vertexList.get(4)),
								  new Facet(vertexList.get(4), vertexList.get(5), vertexList.get(0)),
								  new Facet(vertexList.get(5), vertexList.get(1), vertexList.get(0)),
								  new Facet(vertexList.get(7), vertexList.get(6), vertexList.get(4)),
								  new Facet(vertexList.get(6), vertexList.get(5), vertexList.get(4)),
								  new Facet(vertexList.get(3), vertexList.get(2), vertexList.get(7)),
								  new Facet(vertexList.get(2), vertexList.get(6), vertexList.get(7)),
								  new Facet(vertexList.get(2), vertexList.get(1), vertexList.get(6)),
								  new Facet(vertexList.get(1), vertexList.get(5), vertexList.get(6)));
	}
}
