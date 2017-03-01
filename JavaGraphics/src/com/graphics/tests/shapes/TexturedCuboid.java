package com.graphics.tests.shapes;

import java.awt.Color;

import com.graphics.lib.canvas.TexturableCanvasObject;
import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.SimpleTextureMapper;
import com.graphics.lib.texture.TestTexture;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends TexturableCanvasObject<Cuboid> {
	
	public TexturedCuboid(int height, int width, int depth) {
		super(new Cuboid(height, width, depth));
		
		this.addTexture(new TestTexture())
			.addTexture(new BmpTexture("texture1", Color.white).setApplyLighting(false).setOrder(1))
			.mapTexture(new SimpleTextureMapper());
	}

}
