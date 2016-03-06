package com.graphics.lib.zbuffer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.Facet;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.shader.IShader;

public class ZBuffer implements IZBuffer{
	private Map<Integer, HashMap<Integer,ZBufferItem>> zBuffer;
	private int dispWidth;
	private int dispHeight;
	private int skip = 1; //controls how often we do a colour calculation, a value of 1 checks all lines, whereas 3 calculates every 3rd line and intervening points use the same as the last calculated point

	
	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	@Override
	public ZBufferItem getItemAt(int x, int y){
		if (zBuffer == null) return null;
		
		HashMap<Integer,ZBufferItem> xCol = zBuffer.get(x);
		if (xCol == null) return null;
		
		return xCol.get(y);
	}
	
	/**
	 * {@inheritDoc}
	 * <br/>
	 * Override notes: A null shader here will mean that the facets specified colour will be used
	 */
	@Override
	public void Add(Facet facet, CanvasObject parent, IShader shader, Camera c)
	{
		if (zBuffer == null) return;
		List<LineEquation> lines = new ArrayList<LineEquation>();
		lines.add(new LineEquation(facet.point1, facet.point2, c));
		lines.add(new LineEquation(facet.point2, facet.point3, c));
		lines.add(new LineEquation(facet.point3, facet.point1, c));
		List<WorldCoord> points = new ArrayList<WorldCoord>();
		points.add(facet.point1);
		points.add(facet.point2);
		points.add(facet.point3);
		
		Vector normal = facet.getTransformedNormal(c);
		
		if (normal.z == 0) return;
		
		Comparator<WorldCoord> xComp = new Comparator<WorldCoord>(){
			@Override
			public int compare(WorldCoord o1, WorldCoord o2) {
				return (int)(o1.getTransformed(c).x - o2.getTransformed(c).x);
			}		
		};

		double minX = points.stream().min(xComp).get().getTransformed(c).x;
		double maxX = points.stream().max(xComp).get().getTransformed(c).x;

		
		if (minX < 0) minX = 0;
		if (maxX > this.dispWidth) maxX = this.dispWidth;			
		
		IShader localShader = null;
		if (shader != null){
			try {
				localShader = shader.getClass().newInstance();
				localShader.setLightsources(shader.getLightsources());
			} catch (Exception e) {
				//e.printStackTrace();
			} 
			localShader.init(parent, facet, c);
		}
		
		
		for (int x = (int)Math.floor(minX) ; x <= Math.ceil(maxX) ; x++)
		{
			ScanLine scanLine = this.getScanline(x, lines);
			if (scanLine == null) continue;
			
			double scanLineLength = Math.floor(scanLine.endY) - Math.floor(scanLine.startY);
			double percentDistCovered = 0;
			Color colour = null;
			for (int y = (int)Math.floor(scanLine.startY < 0 ? 0 : scanLine.startY) ; y < Math.floor(scanLine.endY > this.dispHeight ? this.dispHeight : scanLine.endY ) ; y++)
			{
				
				if (scanLineLength != 0)
				{
					percentDistCovered = (y - Math.floor(scanLine.startY)) / scanLineLength;
				}
				
				double z = this.interpolateZ(scanLine.startZ, scanLine.endZ, percentDistCovered);
				if (z < 0) continue;
				
				if (skip == 1 || y % skip == 0 || colour == null){	
					colour = localShader == null ? (facet.getColour() == null ? parent.getColour() : facet.getColour()) : localShader.getColour(scanLine, x, y);
				}
				this.addToBuffer(parent, x, y, z, colour);
			}
		}
	}
	
	@Override
	public Map<Integer, HashMap<Integer, ZBufferItem>> getBuffer() {
		return zBuffer;
	}

	private void addToBuffer(CanvasObject parent, Integer x, Integer y, double z, Color colour)
	//private synchronized void addToBuffer(Integer x, Integer y, double z, Color colour)
	{	
		if (z < 0) return;
		
		/*if (!this.zBuffer.containsKey(x))
		{
			this.zBuffer.put(x, new HashMap<Integer, ZBufferItem>());
		}*/
		
		ZBufferItem bufferItem = this.zBuffer.get(x).get(y);
		
		/*if (bufferItem == null){
			bufferItem = new ZBufferItem(x, y);
			this.zBuffer.get(x).put(y, bufferItem);
		}*/
		
		bufferItem.add(parent, z, colour);
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
		
		return this.interpolateZ(startZ, endZ, percentLength); 
		//some problems still exist with laser as seen from slave when aimed near it (sphere overlap of laser firing behind slave), so still not getting z properly - may also be a problem in the camera transform algorithm
		//as it sometimes seems to put the laser the wrong side of the slave camera when laser travels from in front to behind camera - possibly a general problem crossing this line
	}


	private double interpolateZ(double startZ, double endZ, double percentLength)
	{
		return 1d / ((1d/startZ) + (percentLength * ((1d/endZ) - (1d/startZ))));
	}
	
	@Override
	public void setDimensions(int width, int height) {
		//setting up all zbuffer item objects does slow it down, a bit but should mean it doesn't slowly slow down as items are added, performance should stay constant
		//except for the actual drawing of pixels
		//Also means we don't have the possibility of 2 threads trying to add the same item
		if (this.dispHeight != height || this.dispWidth != width)
		{
			this.dispHeight = height;
			this.dispWidth = width;
			zBuffer = new HashMap<Integer, HashMap<Integer,ZBufferItem>>();
			
			for (int x = 0 ; x < width + 1 ; x++){
				HashMap<Integer,ZBufferItem> map = new HashMap<Integer,ZBufferItem>();
				zBuffer.put(x, map);
				for (int y = 0 ; y < height + 1 ; y++){
					map.put(y, new ZBufferItem(x, y));
				}
			}
		}
	}

	@Override
	public void clear() {
		this.zBuffer.values().stream().forEach(m -> {
			for (ZBufferItem item : m.values()){
				if (item.isActive()) item.clear();
			}
		});
		
	}
}
