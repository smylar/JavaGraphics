package com.graphics.tests;

import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.TestTexture;
import com.graphics.lib.texture.Texture;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends Cuboid {

	public TexturedCuboid(int height, int width, int depth) {
		super(height, width, depth);
		Texture texture = new TestTexture();
		BmpTexture bmptexture = new BmpTexture("texture1", true);
		bmptexture.setApplyLighting(false);

		this.getVertexList().get(1).setTextureY(bmptexture, bmptexture.getHeight());
		this.getVertexList().get(2).setTextureY(bmptexture, bmptexture.getHeight());
		this.getVertexList().get(2).setTextureX(bmptexture, bmptexture.getWidth());
		this.getVertexList().get(3).setTextureX(bmptexture, bmptexture.getWidth());
		
		this.getVertexList().get(1).setTextureY(texture, texture.getHeight());
		this.getVertexList().get(2).setTextureY(texture, texture.getHeight());
		this.getVertexList().get(2).setTextureX(texture, texture.getWidth());
		this.getVertexList().get(3).setTextureX(texture, texture.getWidth());
		
		this.getFacetList().get(0).addTexture(bmptexture);
		this.getFacetList().get(1).addTexture(bmptexture);
		
		this.getFacetList().get(0).addTexture(texture);
		this.getFacetList().get(1).addTexture(texture);
	}

}
