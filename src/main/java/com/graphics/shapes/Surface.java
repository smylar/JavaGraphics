package com.graphics.shapes;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

public class Surface extends CanvasObject {

	public Surface(int rows, int cols) {
		super(() -> init(rows,cols,10));
	}
	
	public Surface(int rows, int cols, int blockSize) {
        super(() -> init(rows,cols,blockSize));
    }
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(int rows, int cols, int blockSize) {
		WorldCoord[][] coords = new WorldCoord[rows + 1][cols + 1]; 
		ImmutableList.Builder<WorldCoord> vertexList = ImmutableList.builder();
		ImmutableList.Builder<Facet> facets = ImmutableList.builder();
		
		for (int c = 0 ; c < cols + 1 ; c++){
			WorldCoord coord = new WorldCoord(c * blockSize, 0, 0);
			vertexList.add(coord);
			coords[0][c] = coord;
		}
		
		for (int r = 1 ; r < rows + 1 ; r++){
			for (int c = 0 ; c < cols + 1 ; c++){
				WorldCoord coord = new WorldCoord(c * blockSize, r * blockSize, 0);
				vertexList.add(coord);
				coords[r][c] = coord;
				if (c > 0){					
					facets.add(new Facet(coords[r-1][c-1], coords[r][c-1], coords[r-1][c]));
					facets.add(new Facet(coords[r-1][c], coords[r][c-1], coords[r][c]));
				}
			}
		}
		return Pair.of(vertexList.build(), facets.build());		
	}
}
