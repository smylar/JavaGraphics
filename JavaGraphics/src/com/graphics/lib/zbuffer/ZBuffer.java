package com.graphics.lib.zbuffer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.shader.IShader;

public class ZBuffer implements IZBuffer{
	private List<List<ZBufferItem>> zBuffer;
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
		
		//HashMap<Integer,ZBufferItem> xCol = zBuffer.get(x);
		List<ZBufferItem> xCol = zBuffer.get(x);
		if (xCol == null) return null;
		
		return xCol.get(y);
	}
	
	/**
	 * {@inheritDoc}
	 * <br/>
	 * Override notes: A null shader here will mean that the facets specified colour will be used
	 */
	@Override
	public void Add(CanvasObject obj, IShader shader, Camera c, double horizon)
	{
		Stream<Facet> facetStream = obj.getFacetList().parallelStream();
		if (!obj.isProcessBackfaces()){
			facetStream = facetStream.filter(GeneralPredicates.isFrontface(c));
		}
		facetStream.filter(GeneralPredicates.isOverHorizon(c, horizon).negate()).forEach(f ->{
			f.setFrontFace(GeneralPredicates.isFrontface(c).test(f));
			Add(f, obj, shader, c);
		});
	}
	
	
	private void Add(Facet facet, CanvasObject parent, IShader shader, Camera c)
	{
		if (zBuffer == null) return;
		
		List<WorldCoord> points = facet.getAsList();
		if (points.stream().allMatch(p -> p.getTransformed(c).z <= 0)) return;
		
		//not sure about these though - it is possible to have points out of range either side of the screen so something should be drawn - but performance likely to be better (when significant objects are off screen)
		//if (points.stream().allMatch(p -> p.getTransformed(c).x < 0 || p.getTransformed(c).x > this.dispWidth)) return;
		//if (points.stream().allMatch(p -> p.getTransformed(c).y < 0 || p.getTransformed(c).y > this.dispHeight)) return;
		//do separately? so returns if all points off one side
		if (points.stream().allMatch(p -> p.getTransformed(c).x < 0)) return;
		if (points.stream().allMatch(p -> p.getTransformed(c).x > this.dispWidth)) return;
		if (points.stream().allMatch(p -> p.getTransformed(c).y < 0)) return;
		if (points.stream().allMatch(p -> p.getTransformed(c).y > this.dispHeight)) return;
		
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
		
		List<LineEquation> lines = new ArrayList<LineEquation>();
		
		lines.add(new LineEquation(points.get(0), points.get(1), c));
		lines.add(new LineEquation(points.get(1), points.get(2), c));
		lines.add(new LineEquation(points.get(2), points.get(0), c));
		
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
				
				if (skip == 1 || y % skip == 0 || colour == null){	
					colour = localShader == null ? (facet.getColour() == null ? parent.getColour() : facet.getColour()) : localShader.getColour(scanLine, x, y);
				}
				this.addToBuffer(parent, x, y, z, colour);
			}
		}
	}
	
	@Override
	//public Map<Integer, HashMap<Integer, ZBufferItem>> getBuffer() {
	public List<List<ZBufferItem>> getBuffer() {
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
			//zBuffer = new HashMap<Integer, HashMap<Integer,ZBufferItem>>();
			zBuffer = new ArrayList<List<ZBufferItem>>();
			
			for (int x = 0 ; x < width + 1 ; x++){
				//HashMap<Integer,ZBufferItem> map = new HashMap<Integer,ZBufferItem>();
				ArrayList<ZBufferItem> list = new ArrayList<ZBufferItem>();
				//zBuffer.put(x, map);
				//for (int y = 0 ; y < height + 1 ; y++){
				//	map.put(y, new ZBufferItem(x, y));
				//}
				for (int y = 0 ; y < height + 1 ; y++){
					list.add(new ZBufferItem(x, y));
				}
				zBuffer.add(list);
			}
		}
	}

	@Override
	public void clear() {
		/*this.zBuffer.values().stream().forEach(m -> {
			for (ZBufferItem item : m.values()){
				if (item.isActive()) item.clear();
			}
		});*/
		this.zBuffer.parallelStream().forEach(m -> {
			for (ZBufferItem item : m){
				if (item.isActive()) item.clear();
			}
		});
		
	}
}
