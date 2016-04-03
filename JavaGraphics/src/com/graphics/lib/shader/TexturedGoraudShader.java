package com.graphics.lib.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Point;
import com.graphics.lib.TexturedWorldCoord;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.texture.Texture;
import com.graphics.lib.zbuffer.ScanLine;

public class TexturedGoraudShader extends GoraudShader {
	
	private List<Texture> textures = new ArrayList<Texture>();
	
	@Override
	public void init(CanvasObject parent, Facet facet, Camera c) {
		super.init(parent, facet, c);
		this.textures.clear();
		Set<Texture> tx = new HashSet<Texture>();
		if (facet.getAsList().stream().allMatch(p -> p instanceof TexturedWorldCoord)){
			for (WorldCoord wc : facet.getAsList()){
				tx.addAll(((TexturedWorldCoord)wc).getTextures());
			}
			tx.removeIf(t -> !facet.getAsList().stream().allMatch(p -> ((TexturedWorldCoord)p).getTextures().contains(t)));
			this.textures = tx.stream().sorted((a,b) -> b.getOrder() - a.getOrder()).collect(Collectors.toList());
		}
	}
	
	@Override
	public Color getColour(ScanLine scanLine, int x, int y) {
		if (textures.size() == 0) return super.getColour(scanLine, x, y);
		if (scanLine == null) return colour;
		
		if (scanLine != this.curScanline){
			startIntensity = this.getIntensities(x, scanLine.startY, scanLine.startLine);
			endIntensity = this.getIntensities(x, scanLine.endY, scanLine.endLine);
			curScanline = scanLine;
			lineLength = Math.ceil(scanLine.endY) - Math.floor(scanLine.startY);
		}
		
		if (lineLength == 0) return colour;

		double percentDistCovered = (y - Math.floor(scanLine.startY)) / lineLength;
		
		Color pointColour = colour;
		
		for (Texture t : textures){
			startTexture = this.getTexturePosition(x, scanLine.startY, scanLine.startLine, t);
			endTexture = this.getTexturePosition(x, scanLine.endY, scanLine.endLine, t);
			double ux = (1 - percentDistCovered) * (startTexture.x/startTexture.z) + (percentDistCovered * (endTexture.x/endTexture.z));
			double uy = (1 - percentDistCovered) * (startTexture.y/startTexture.z) + (percentDistCovered * (endTexture.y/endTexture.z));
			double r = (1 - percentDistCovered) * (1/startTexture.z) + (percentDistCovered * (1/endTexture.z));
			
			int tx = (int)Math.round(ux/r);
			int ty = (int)Math.round(uy/r);
			
			Color c = t.getColour(tx, ty);

			if (c != null){
				if (!t.applyLighting()) return c;
					
				pointColour = c;
				break;
			}
		}
		
		pointIntensity.setRed(startIntensity.getRed() + ((endIntensity.getRed() - startIntensity.getRed()) * percentDistCovered));
		pointIntensity.setGreen(startIntensity.getGreen() + ((endIntensity.getGreen() - startIntensity.getGreen()) * percentDistCovered));
		pointIntensity.setBlue(startIntensity.getBlue() + ((endIntensity.getBlue() - startIntensity.getBlue()) * percentDistCovered));
		facet.checkIntensity(pointIntensity);
		
		return pointIntensity.applyIntensities(pointColour);
	}
	
	
	private Point getTexturePosition(double xVal, double yVal, LineEquation line, Texture t)
	{
		double dx = xVal - line.getStart().x;
		double dy = yVal - line.getStart().y;
		double len = Math.sqrt((dx*dx)+(dy*dy));
		
		double percentLength = len / line.getLength();
		
		////ua = (1-a)(u0/z0) + a(u1/z1) / (1-a)(1/z0) + a(1/z1)
		
		TexturedWorldCoord start = (TexturedWorldCoord)line.getWorldStart();
		TexturedWorldCoord end = (TexturedWorldCoord)line.getWorldEnd();
		double x = (1 - percentLength) * (start.getTextureX(t)/line.getStart().z) + (percentLength * (end.getTextureX(t)/line.getEnd().z));
		double y = (1 - percentLength) * (start.getTextureY(t)/line.getStart().z) + (percentLength * (end.getTextureY(t)/line.getEnd().z));
		double r = (1 - percentLength) * (1/line.getStart().z) + (percentLength * (1/line.getEnd().z));
		x = x/r;
		y = y/r;
		
		double z = line.getStart().z + ((line.getEnd().z - line.getStart().z ) * percentLength);
	
		return new Point (x, y, z);
	}

}
