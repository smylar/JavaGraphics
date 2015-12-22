package com.graphics.lib.texture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;


import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class BmpTexture implements Texture{
	
	private BufferedImage bmpImage;
	private boolean whiteIsTransparent = true;
	private Map<Integer, Color> colourMap = new HashMap<Integer, Color>(); //so we don't get a tonne of colour objects to clear up
	private boolean applyLighting = true;
	
	public BmpTexture(String bmpFileResource, boolean whiteIsTransparent){
		this.whiteIsTransparent = whiteIsTransparent;
		try {
			bmpImage = ImageIO.read(getClass().getResource(bmpFileResource + ".bmp"));
		} catch (IOException e) {}
	}
	
	@Override
	public Color getColour(int x, int y){
		if (x < 0 || y < 0) return null;
		x = x % getWidth();
		y = y % getHeight();
		
		int rgb = bmpImage.getRGB(x, y);
		
		if (whiteIsTransparent && rgb == Color.WHITE.getRGB()) return null;
		
		Color c = null;
		if (colourMap.containsKey(rgb)){
			c = colourMap.get(rgb);
		}else{
			c = new Color(rgb);
			colourMap.put(rgb, c);
		}
		return c;
	}
	
	@Override
	public int getHeight(){
		return bmpImage.getHeight();
	}
	
	@Override
	public int getWidth(){
		return bmpImage.getWidth();
	}

	@Override
	public boolean applyLighting() {
		return applyLighting;
	}

	public void setApplyLighting(boolean applyLighting) {
		this.applyLighting = applyLighting;
	}
	
}
