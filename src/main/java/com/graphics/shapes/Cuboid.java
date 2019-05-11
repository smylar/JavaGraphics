package com.graphics.shapes;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IFacet;
import com.graphics.lib.transform.Translation;

public class Cuboid extends CanvasObject{
	
	public Cuboid (int height, int width, int depth)
	{
	    super(() -> init(height, width, depth));
	    
	    applyTransform(new Translation(-width/2d,-height/2d,-depth/2d));
        fixCentre();
	}
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(int height, int width, int depth) {
		ImmutableList<WorldCoord> vertexList = generateVertexList(height, width, depth);
		return Pair.of(vertexList, generateFacetList(vertexList));
	}
	
	private static ImmutableList<WorldCoord> generateVertexList(int height, int width, int depth)
	{
		return ImmutableList.of(new WorldCoord(0, 0, 0),
								  new WorldCoord(0, height, 0),
								  new WorldCoord(width, height, 0),
								  new WorldCoord(width, 0, 0),
								  new WorldCoord(0, 0, depth),
								  new WorldCoord(0, height, depth),
								  new WorldCoord(width, height, depth),
								  new WorldCoord(width, 0, depth)
								);
	}
	
	private static ImmutableList<Facet> generateFacetList(List<WorldCoord> vertexList)
	{
//		return ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(3)),
//								  new Facet(vertexList.get(1), vertexList.get(2), vertexList.get(3)),
//								  new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(4)),
//								  new Facet(vertexList.get(3), vertexList.get(7), vertexList.get(4)),
//								  new Facet(vertexList.get(4), vertexList.get(5), vertexList.get(0)),
//								  new Facet(vertexList.get(5), vertexList.get(1), vertexList.get(0)),
//								  new Facet(vertexList.get(7), vertexList.get(6), vertexList.get(4)),
//								  new Facet(vertexList.get(6), vertexList.get(5), vertexList.get(4)),
//								  new Facet(vertexList.get(3), vertexList.get(2), vertexList.get(7)),
//								  new Facet(vertexList.get(2), vertexList.get(6), vertexList.get(7)),
//								  new Facet(vertexList.get(2), vertexList.get(1), vertexList.get(6)),
//								  new Facet(vertexList.get(1), vertexList.get(5), vertexList.get(6))
//								  );
	    IFacet f = (a,b,c) -> new Facet(vertexList.get(a), vertexList.get(b), vertexList.get(c));
		return ImmutableList.of(f.generate(0, 1, 3), //slightly cleaner?
		                        f.generate(1, 2, 3),
	                            f.generate(0, 3, 4),
	                            f.generate(3, 7, 4),
	                            f.generate(4, 5, 0),
	                            f.generate(5, 1, 0),
	                            f.generate(7, 6, 4),
	                            f.generate(6, 5, 4),
	                            f.generate(3, 2, 7),
	                            f.generate(2, 6, 7),
	                            f.generate(2, 1, 6),
	                            f.generate(1, 5, 6)
                                );
	}
}
