package com.graphics.lib.texture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

public class BmpTexture implements Texture{
	
	private BufferedImage bmpImage;
	private Color transparentColour = null;
	private Map<Integer, Color> colourMap = new HashMap<>(); //so we don't get a tonne of colour objects to clear up
	private boolean applyLighting = true;
	private int order = 0;
	
	public BmpTexture(String bmpFileResource){
		try {
			bmpImage = ImageIO.read(getClass().getResource(bmpFileResource + ".bmp"));
		} catch (IOException e) {}
	}
	
	public BmpTexture(String bmpFileResource, Color transparentColour){
		this(bmpFileResource);
		this.transparentColour = transparentColour;
	}
	
	@Override
	public Optional<Color> getColour(int x, int y){
		if (x < 0 || y < 0 || bmpImage == null) {
		    return Optional.empty(); 
		}
		
		int xval = x % getWidth();
		int yval = y % getHeight();
		
		int rgb = bmpImage.getRGB(xval, yval);
		
		if (transparentColour != null && rgb == transparentColour.getRGB()) {
		    return Optional.empty();
		}
		
		Color c = colourMap.computeIfAbsent(rgb, Color::new);

		return Optional.ofNullable(c);
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

	public BmpTexture setApplyLighting(boolean applyLighting) {
		this.applyLighting = applyLighting;
		return this;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public BmpTexture setOrder(int order) {
		this.order = order;
		return this;
	}

	@Override
	public void setColour(int x, int y, Color colour) {
		if (x < 0 || y < 0 || x > getWidth()-1 || y > getHeight()-1 || bmpImage == null) return;
		
		bmpImage.setRGB(x, y, colour.getRGB());
	}
	
}
