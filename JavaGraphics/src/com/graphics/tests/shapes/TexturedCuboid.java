package com.graphics.tests.shapes;

import java.awt.Color;

import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.SimpleTextureMapper;
import com.graphics.lib.texture.TestTexture;
import com.graphics.lib.traits.TexturableTrait;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends Cuboid {
	
	public TexturedCuboid(int height, int width, int depth) {
		super(height, width, depth);
		
		this.addTrait(new TexturableTrait())
        		.addTexture(new TestTexture())
                .addTexture(new BmpTexture("texture1", Color.white).setApplyLighting(false).setOrder(1))
                .mapTexture(new SimpleTextureMapper());
	}

}
