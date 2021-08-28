package com.graphics.lib.zbuffer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.List;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.shader.IShaderFactory;

/**
 * A 2-Dimensional construct that stores the Z value and colour of pixels to be drawn on the screen
 * This allows us to determine which objects are in front of other objects etc.
 * 
 * @author paul.brandon
 *
 */
public class ZBuffer implements IZBuffer {
	private List<List<ZBufferItem>> buffer = new ArrayList<>();
	private BufferedImage imageBuf = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private Dimension dimension = new Dimension();

	@Override
	public ZBufferItem getItemAt(int x, int y) {
	    return buffer.get(x).get(y);
	}
	
	@Override
	public void add(final ICanvasObject obj, final IShaderFactory shader, final Camera c, final double horizon)
	{
	    if (c.getPosition().distanceTo(obj.getCentre()) < horizon) {

	        shader.add(obj, c, dimension, (x,y,z,cs) -> addToBuffer(obj, x, y, z, cs));
	    }
	}
	
	@Override
	public BufferedImage getBuffer() {
		return imageBuf;
	}
	
	@Override
	public void refreshBuffer() {

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
	
	private void addToBuffer(ICanvasObject parent, Integer x, Integer y, double z, Function<Integer,Color> colour)
	{	
	    //use a supplier for colour so we don't lose performance executing a shader when this pixel is actually behind another
		try {
		    if (z >= 0) {
    			ZBufferItem bufferItem = getItemAt(x,y);
    			bufferItem.add(parent, z, colour.apply(y));
		    }
		} catch(Exception e) {
			//think it sometimes initially sets buffer of wrong size - JComponents taking their time to report their height etc.
			//is nearly always sorted out in the second cycle though as canvas3d does check if the dimensions have changed
		}
	}
	
	@Override
	public void setDimensions(int width, int height) {
		//setting up all zbuffer item objects does slow it down, a bit but should mean it doesn't slowly slow down as items are added, performance should stay constant
		//except for the actual drawing of pixels
		//Also means we don't have the possibility of 2 threads trying to add the same item
		if (dimension.getHeight() != height || dimension.getWidth() != width)
		{
			dimension = new Dimension(width, height);

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
