package com.graphics.tests;

import java.awt.Color;
import com.graphics.lib.TexturedWorldCoord;
import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.TestTexture;
import com.graphics.lib.texture.Texture;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends Cuboid {

	private Texture texture;
	private BmpTexture bmptexture;
	
	public TexturedCuboid(int height, int width, int depth) {
		super(height, width, depth);
		bmptexture.setApplyLighting(false);
		bmptexture.setOrder(1);
	}
	
	@Override
	protected void generateVertexList(int height, int width, int depth){
		bmptexture = new BmpTexture("texture1", Color.white);
		texture = new TestTexture();
		super.generateVertexList(height, width, depth);
		TexturedWorldCoord tCoord = new TexturedWorldCoord(this.getVertexList().get(0));
		tCoord.setTextureY(bmptexture, 0);
		tCoord.setTextureY(texture, 0);
		this.getVertexList().set(0, tCoord);
		
		tCoord = new TexturedWorldCoord(this.getVertexList().get(1));
		tCoord.setTextureY(bmptexture, bmptexture.getHeight());
		tCoord.setTextureY(texture, texture.getHeight());
		this.getVertexList().set(1, tCoord);
		
		tCoord = new TexturedWorldCoord(this.getVertexList().get(2));
		tCoord.setTextureY(bmptexture, bmptexture.getHeight());
		tCoord.setTextureX(bmptexture, bmptexture.getWidth());
		tCoord.setTextureY(texture, texture.getHeight());
		tCoord.setTextureX(texture, texture.getWidth());
		this.getVertexList().set(2, tCoord);
		
		tCoord = new TexturedWorldCoord(this.getVertexList().get(3));
		tCoord.setTextureX(bmptexture, bmptexture.getWidth());
		tCoord.setTextureX(texture, texture.getWidth());
		this.getVertexList().set(3, tCoord);
	}

}
