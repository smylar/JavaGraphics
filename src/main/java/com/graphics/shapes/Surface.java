package com.graphics.shapes;

import com.graphics.lib.Point;
import com.graphics.lib.texture.*;
import com.graphics.lib.traits.TexturableTrait;
import com.graphics.lib.traits.TraitHandler;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Surface extends CanvasObject {

	private final int rows;
	private final int cols;

	public Surface(int rows, int cols) {
		this(rows,cols,10);
	}
	
	public Surface(int rows, int cols, int blockSize) {
		super(() -> init(rows,cols,blockSize));
		this.rows = rows;
		this.cols = cols;
    }

	public void addTexture(final Texture texture) {
		TraitHandler.INSTANCE.getTrait(this, TexturableTrait.class)
						.ifPresentOrElse(t -> t.addTexture(texture),
								() -> TraitHandler.INSTANCE.registerTrait(this, co -> new TexturableTrait(co, getTextureMapper()))
										.addTexture(texture));
	}

	private TextureMapper<Surface> getTextureMapper() {
		return new TextureMapper<>(Surface.class) {
			@Override
			protected Map<WorldCoord, Point> mapImpl(Surface obj, Texture texture) {
				//may want to revisit texture mapping, currently requires all coords in  object to be mapped to a texture coord
				//feels like there will be a better way
				double xprog = (double) texture.getWidth() / rows;
				double yprog = (double) texture.getHeight() / cols;

				Map<WorldCoord, Point> tMap = IntStream.range(0, getVertexList().size())
						.collect(
								HashMap::new,
								(a,i) -> {
									int pointRow = i / (cols + 1);
									int pointCol = i % (cols + 1);
									a.put(getVertexList().get(i), new Point(pointCol * xprog,pointRow * yprog ,0));
								},
								HashMap::putAll);

				return Map.copyOf(tMap);
			}
		};
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
