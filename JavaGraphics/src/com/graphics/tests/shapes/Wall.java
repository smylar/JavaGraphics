package com.graphics.tests.shapes;

import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

public class Wall extends CanvasObject {
	private int blockSize = 10;
	
	public Wall(int rows, int cols){
		super();
		
		WorldCoord[][] coords = new WorldCoord[rows + 1][cols + 1]; 
		for (int c = 0 ; c < cols + 1 ; c++){
			WorldCoord coord = new WorldCoord(c * blockSize, 0, 0);
			this.getVertexList().add(coord);
			coords[0][c] = coord;
		}
		
		for (int r = 1 ; r < rows + 1 ; r++){
			for (int c = 0 ; c < cols + 1 ; c++){
				WorldCoord coord = new WorldCoord(c * blockSize, r * blockSize, 0);
				this.getVertexList().add(coord);
				coords[r][c] = coord;
				if (c > 0){					
					this.getFacetList().add(new Facet(coords[r-1][c-1], coords[r][c-1], coords[r-1][c]));
					this.getFacetList().add(new Facet(coords[r-1][c], coords[r][c-1], coords[r][c]));
				}
			}
		}
		
		coords = null;
	}
}
