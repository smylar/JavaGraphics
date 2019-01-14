package com.graphics.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.texture.Texture;

@Deprecated
public class TexturedWorldCoord extends WorldCoord {
	private Map<Texture, Double> textureX;
	private Map<Texture, Double> textureY;
	
	public TexturedWorldCoord(Point p) {
		super(p.x, p.y, p.z);
	}
	
	public double getTextureX(Texture t) {
		return (textureX != null && textureX.containsKey(t)) ? textureX.get(t) : 0;
	}

	public void setTextureX(Texture t, double textureX) {
		if (this.textureX == null) this.textureX = new HashMap<Texture, Double>();
		this.textureX.put(t, textureX);
	}

	public double  getTextureY(Texture t) {
		return (textureY != null && textureY.containsKey(t)) ? textureY.get(t) : 0;
	}

	public void setTextureY(Texture t, double textureY) {
		if (this.textureY == null) this.textureY = new HashMap<Texture, Double>();
		this.textureY.put(t, textureY);
	}
	
	public Set<Texture> getTextures(){
		Set<Texture> textures = new HashSet<Texture>();
		if (textureX != null) textures.addAll(textureX.keySet());
		if (textureY != null) textures.addAll(textureY.keySet());
		return textures;
	}
}
