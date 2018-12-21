package com.graphics.lib.zbuffer;

import static com.graphics.lib.util.NumberUtils.NUMBERS;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Utils;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.properties.Property;
import com.graphics.lib.properties.PropertyInject;
import com.graphics.lib.shader.IShader;
import com.graphics.lib.shader.IShaderFactory;

/**
 * A 2-Dimensional construct that stores the Z value and colour of pixels to be drawn on the screen
 * This allows us to determine which objects are in front of other objects etc.
 * 
 * @author paul.brandon
 *
 */
@PropertyInject
public class ZBuffer implements IZBuffer {
	private List<List<ZBufferItem>> buffer = new ArrayList<>();
	private BufferedImage imageBuf = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private int dispWidth;
	private int dispHeight;
	
	@Property(name="zbuffer.skip", defaultValue="2")
	private Integer skip; //controls how often we do a colour calculation, a value of 1 checks all lines, whereas 3 calculates every 3rd line and intervening points use the same as the last calculated point

	@Override
	public ZBufferItem getItemAt(int x, int y) {
	    return buffer.get(x).get(y);
	}
	
	/**
	 * {@inheritDoc}
	 * <br/>
	 * Override notes: A null shader here will mean that the facets specified colour will be used
	 */
	@Override
	public void add(final ICanvasObject obj, final IShaderFactory shader, final Camera c, final double horizon)
	{
		obj.getFacetList().parallelStream()
		                  .filter(f -> obj.isProcessBackfaces() || GeneralPredicates.isFrontface(c).test(f))
		                  .filter(f -> !GeneralPredicates.isOverHorizon(c, horizon).test(f))
		                  .filter(f -> !isOffScreen(f,c))
		                  .forEach(f -> add(f, obj, shader, c));
	}
	
	@Override
	public synchronized BufferedImage getBuffer() {
		return imageBuf;
	}
	
	@Override
	public synchronized void refreshBuffer() {

	    buffer.parallelStream()
	          .flatMap(List::parallelStream)
	          .forEach(item -> {
	                if (item.isActive()) {
	                    imageBuf.setRGB(item.getX(), item.getY(), item.getColour().getRGB());
	                } else {
	                    imageBuf.setRGB(item.getX(), item.getY(), Color.WHITE.getRGB());
	                }
	           });
	}

	private void add(Facet facet, ICanvasObject parent, IShaderFactory shader, Camera c)
    {   
        List<WorldCoord> points = facet.getAsList();
        
        Comparator<WorldCoord> xComp = (o1, o2) -> (int)(o1.getTransformed(c).x - o2.getTransformed(c).x);

        double minX = points.stream().min(xComp).get().getTransformed(c).x;
        double maxX = points.stream().max(xComp).get().getTransformed(c).x;

        
        if (minX < 0) 
            minX = 0;
        if (maxX > this.dispWidth) 
            maxX = this.dispWidth;          
        
        try (IShader localShader = shader.getShader()) {
            localShader.init(parent, facet, c);
        
            List<LineEquation> lines = new ArrayList<>();
            
            lines.add(new LineEquation(points.get(0), points.get(1), c));
            lines.add(new LineEquation(points.get(1), points.get(2), c));
            lines.add(new LineEquation(points.get(2), points.get(0), c));
            
            for (int x = (int)Math.floor(minX) ; x <= Math.ceil(maxX) ; x++)
            {
                final int xVal = x;
                getScanline(x, lines).ifPresent(sl -> processScanline(sl, parent, xVal, localShader));
            }
        } catch (Exception e) { }
    }
	
	private boolean isOffScreen(Facet facet, Camera c) {
	    facet.setFrontFace(GeneralPredicates.isFrontface(c).test(facet));
	    return facet.getTransformedNormal(c).getZ() == 0 ||
	            Utils.allMatchAny(facet.getAsList().stream().map(p -> p.getTransformed(c)).collect(Collectors.toList()), 
                        Lists.newArrayList(p -> p.z <= 1,
                                           p -> p.x < 0,
                                           p -> p.x > dispWidth,
                                           p -> p.y < 0,
                                           p -> p.y > dispHeight));
	}
	
	private void processScanline(ScanLine scanLine, ICanvasObject parent, int x, IShader shader) {
		double scanLineLength = Math.floor(scanLine.getEndY()) - Math.floor(scanLine.getStartY());
		double percentDistCovered = 0;
		Color colour = null;
		for (int y = (int)Math.floor(scanLine.getStartY() < 0 ? 0 : scanLine.getStartY()) ; y < Math.floor(scanLine.getEndY() > this.dispHeight ? this.dispHeight : scanLine.getEndY() ) ; y++)
		{
			
			if (scanLineLength != 0)
			{
				percentDistCovered = (y - Math.floor(scanLine.getStartY())) / scanLineLength;
			}
			
			double z = this.interpolateZ(scanLine.getStartZ(), scanLine.getEndZ(), percentDistCovered);
			final int yVal = y;
			Supplier<Color> colourSupplier = () -> skip == 1 || yVal % skip == 0 || colour == null ? shader.getColour(scanLine, x, yVal) : colour;
			addToBuffer(parent, x, y, z, colourSupplier);
		}
	}
	
	private void addToBuffer(ICanvasObject parent, Integer x, Integer y, double z, Supplier<Color> colour)
	{	
	    //use a supplier for colour so we don't lose performance executing a shader when this pixel is actually behind another
		try {
		    if (z >= 0) {
    			ZBufferItem bufferItem = getItemAt(x,y);
    			bufferItem.add(parent, z, colour);
		    }
		} catch(Exception e) {
			//think it sometimes initially sets buffer of wrong size - JComponents taking their time to report their height etc.
			//is nearly always sorted out in the second cycle though as canvas3d does check if the dimensions have changed
		}
	}
		

	private Optional<ScanLine> getScanline(int xVal, List<LineEquation> lines)
	{
		ScanLine.Builder builder = ScanLine.builder();
		
		List<LineEquation> activeLines = lines.stream().filter(line -> xVal >= line.getMinX() && xVal <= line.getMaxX())
                                		               .filter(line -> {
                                		                   Double y = line.getYAtX(xVal);
                                		                   return Objects.nonNull(y) && y <= line.getMaxY() && y >= line.getMinY();
                                		               })
                                		               .collect(Collectors.toList());
		
		
		if (activeLines.size() < 2) 
		    return Optional.empty();
		
		if (activeLines.size() == 3) {
			double dif1 = NUMBERS.toPostive(activeLines.get(0).getYAtX(xVal) - activeLines.get(1).getYAtX(xVal));
	    	double dif2 = NUMBERS.toPostive(activeLines.get(1).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal));
	    	double dif3 = NUMBERS.toPostive(activeLines.get(0).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal));
	    	 		
			if ((dif1 < dif2 && dif1 < dif3) || (dif3 < dif2 && dif3 < dif1)) {
			 	activeLines.remove(0);
			} else {
				 activeLines.remove(2);
			}
		}	
		
		for(LineEquation line : activeLines)
		{
			Double y = line.getYAtX(xVal);
			Double z = this.getZValue(xVal, y, line);
			
			if (builder.startY == null){
			    builder.startY = y;
			    builder.startLine = line;
			    builder.startZ = z;
			}
			else if (y < builder.startY)
			{
			    builder.endY = builder.startY ;
			    builder.endLine = builder.startLine;
			    builder.endZ = builder.startZ ;
			    builder.startY = y;
			    builder.startLine = line;
			    builder.startZ = z;
			}
			else
			{
			    builder.endY = y;
			    builder.endLine = line;
			    builder.endZ = z;
			}
		}
		
		return Optional.ofNullable(builder.build());
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

			buffer = new ArrayList<>();
			
			for (int x = 0 ; x < width + 1 ; x++) {
				ArrayList<ZBufferItem> list = new ArrayList<>();
					for (int y = 0 ; y < height + 1 ; y++) {
						list.add(new ZBufferItem(x, y));
					}
				buffer.add(list);
			}
			
			imageBuf = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_ARGB);
			imageBuf.setAccelerationPriority(0.75f);
		}
	}

	@Override
	public void clear() {
	    buffer.parallelStream()
	          .flatMap(List::parallelStream)
	          .filter(ZBufferItem::isActive)
	          .forEach(ZBufferItem::clear);
	}
}
