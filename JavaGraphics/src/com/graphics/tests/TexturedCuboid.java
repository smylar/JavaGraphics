package com.graphics.tests;

import com.graphics.lib.texture.TestTexture;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends Cuboid {

	public TexturedCuboid(int height, int width, int depth) {
		super(height, width, depth);
		TestTexture texture = new TestTexture();

		this.getVertexList().get(1).setTextureY(texture.getHeight());
		this.getVertexList().get(2).setTextureY(texture.getHeight());
		this.getVertexList().get(2).setTextureX(texture.getWidth());
		this.getVertexList().get(3).setTextureX(texture.getWidth());
		
		this.getFacetList().get(0).setTexture(texture);
		this.getFacetList().get(1).setTexture(texture);
	}

}
