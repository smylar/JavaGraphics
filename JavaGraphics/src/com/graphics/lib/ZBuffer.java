package com.graphics.lib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.shader.IShader;

public class ZBuffer implements IZBuffer{
	private Map<Integer, HashMap<Integer,ZBufferItem>> zBuffer = new HashMap<Integer, HashMap<Integer,ZBufferItem>>();
	private int dispWidth;
	private int dispHeight;

	/**
	 * {@inheritDoc}
	 * <br/>
	 * Override notes: A null shader here will mean that the facets specified colour will be used
	 */
	@Override
	public void Add(Facet facet, CanvasObject parent, IShader shader)
	{
		List<LineEquation> lines = new ArrayList<LineEquation>();
		lines.add(new LineEquation(facet.point1.getTransformed(), facet.point2.getTransformed()));
		lines.add(new LineEquation(facet.point2.getTransformed(), facet.point3.getTransformed()));
		lines.add(new LineEquation(facet.point3.getTransformed(), facet.point1.getTransformed()));
		List<WorldCoord> points = new ArrayList<WorldCoord>();
		points.add(facet.point1);
		points.add(facet.point2);
		points.add(facet.point3);
		
		Vector normal = facet.getTransformedNormal();
		
		if (normal.z == 0) return;
		
		Comparator<WorldCoord> xComp = new Comparator<WorldCoord>(){
			@Override
			public int compare(WorldCoord o1, WorldCoord o2) {
				return (int)(o1.getTransformed().x - o2.getTransformed().x);
			}		
		};

		double minX = points.stream().min(xComp).get().getTransformed().x;
		double maxX = points.stream().max(xComp).get().getTransformed().x;

		
		if (minX < 0) minX = 0;
		if (maxX > this.dispWidth) maxX = this.dispWidth;			
		
		if (shader != null) shader.init(parent, facet);
		
		for (int x = (int)Math.floor(minX) ; x <= Math.ceil(maxX) ; x++)
		{
			ScanLine scanLine = this.getScanline(x, lines);
			if (scanLine == null) continue;
			
			double scanLineLength = Math.ceil(scanLine.endY) - Math.floor(scanLine.startY);
			double percentDistCovered = 0;
			for (int y = (int)Math.floor(scanLine.startY < 0 ? 0 : scanLine.startY) ; y < Math.ceil(scanLine.endY > this.dispHeight ? this.dispHeight : scanLine.endY) ; y++)
			{
				if (scanLineLength != 0)
				{
					percentDistCovered = (y - Math.floor(scanLine.startY)) / scanLineLength;
				}
				
				double z = scanLine.startZ + ((scanLine.endZ - scanLine.startZ) * percentDistCovered);
				
				Color colour = shader == null ? (facet.getColour() == null ? parent.getColour() : facet.getColour()) : shader.getColour(scanLine, x, y, percentDistCovered);

				this.addToBuffer(x, y, z, colour);
			}
		}
	}
	
	@Override
	public Map<Integer, HashMap<Integer, ZBufferItem>> getBuffer() {
		return zBuffer;
	}

	@Override
	public void setDispWidth(int dispWidth) {
		this.dispWidth = dispWidth;
	}

	@Override
	public void setDispHeight(int dispHeight) {
		this.dispHeight = dispHeight;
	}

	private synchronized void addToBuffer(Integer x, Integer y, double z, Color colour)
	{	
		if (z < 0) return;
		
		if (!this.zBuffer.containsKey(x))
		{
			this.zBuffer.put(x, new HashMap<Integer, ZBufferItem>());
		}
		
		ZBufferItem bufferItem = this.zBuffer.get(x).get(y);
		
		if (bufferItem == null){
			bufferItem = new ZBufferItem(x, y);
			this.zBuffer.get(x).put(y, bufferItem);
		}
		
		bufferItem.add((int)Math.round(z), colour);	
	}
		

	private ScanLine getScanline(int xVal, List<LineEquation> lines)
	{
		ScanLine scanLine = new ScanLine();
		List<LineEquation> activeLines = new ArrayList<LineEquation>();
		for(LineEquation line : lines)
		{
			if (xVal >= line.getMinX() && xVal <= line.getMaxX())
			{
				Double y = line.getYAtX(xVal);
				if (y == null || (y  > line.getMinY() && y < line.getMinY())) continue;
				
				activeLines.add(line);
			}
		}
		
		if (activeLines.size() < 2) return null;
		
		if (activeLines.size() == 3){
			double dif1 = activeLines.get(0).getYAtX(xVal) - activeLines.get(1).getYAtX(xVal);
	    	double dif2 = activeLines.get(1).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal);
	    	double dif3 = activeLines.get(0).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal);
	    	if (dif1 < 0) dif1 = dif1 * -1 ;
	    	if (dif2 < 0) dif2 = dif2 * -1 ;
	    	if (dif3 < 0) dif3 = dif3 * -1 ;
	    	 		
			if ((dif1 < dif2 && dif1 < dif3) || (dif3 < dif2 && dif3 < dif1)){
			 	activeLines.remove(0);
			}else{
				 activeLines.remove(2);
			}
		}	
		
		for(LineEquation line : activeLines)
		{
			Double y = line.getYAtX(xVal);
			Double z = this.getZValue(xVal, y, line);
			
			if (scanLine.startY == null){
				scanLine.startY = y;
				scanLine.startLine = line;
				scanLine.startZ = z;
			}
			else if (y < scanLine.startY)
			{
				scanLine.endY = scanLine.startY ;
				scanLine.endLine = scanLine.startLine;
				scanLine.endZ = scanLine.startZ ;
				scanLine.startY = y;
				scanLine.startLine = line;
				scanLine.startZ = z;
			}
			else
			{
				scanLine.endY = y;
				scanLine.endLine = line;
				scanLine.endZ = z;
			}
		}
		
		return scanLine;
	}
	
	private double getZValue(double xVal, double yVal, LineEquation line)
	{
		double dx = xVal - line.getStart().x;
		double dy = yVal - line.getStart().y;
		double len = Math.sqrt((dx*dx)+(dy*dy));
		
		double percentLength = len / line.getLength();
		double startZ = line.getStart().z;
		double endZ = line.getEnd().z;
		
		return startZ + ((endZ - startZ) * percentLength);
	}
}
