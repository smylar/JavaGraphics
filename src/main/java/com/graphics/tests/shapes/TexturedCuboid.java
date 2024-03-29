package com.graphics.tests.shapes;

import java.awt.Color;

import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.SimpleTextureMapper;
import com.graphics.lib.texture.TestTexture;
import com.graphics.lib.traits.TexturableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.shapes.Cuboid;

public class TexturedCuboid extends Cuboid {
	
	public TexturedCuboid(int height, int width, int depth) {
		super(height, width, depth);
		
		TraitHandler.INSTANCE.registerTrait(this, co -> new TexturableTrait(co, new SimpleTextureMapper()))
        		.addTexture(new TestTexture())
                .addTexture(new BmpTexture("texture1", Color.white).setApplyLighting(false).setOrder(1));
	}

}
