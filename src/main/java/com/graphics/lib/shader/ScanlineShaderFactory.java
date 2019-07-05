package com.graphics.lib.shader;

import static com.graphics.lib.util.NumberUtils.NUMBERS;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Utils;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.properties.Property;
import com.graphics.lib.properties.PropertyInject;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * Sets up various shaders that can be used, they are placed on a queue to enable re-use
 * 
 * @author Paul Brandon
 *
 */
@PropertyInject
public enum ScanlineShaderFactory implements IShaderFactory {
		GORAUD(GoraudShader::new, 8), 
		TEXGORAUD(TexturedGoraudShader::new, 8), 
		FLAT(FlatShader::new, 8), 
		NONE(DefaultScanlineShader::new, 8);

	private LinkedBlockingQueue<ScanlineShader> pool;
	
	@Property(name="zbuffer.skip", defaultValue="2")
    private Integer skip;
	
	private ScanlineShaderFactory(Supplier<ScanlineShader> shader, int poolSize) {
        pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0 ; i < poolSize ; i++) {
            ScanlineShader shaderImpl = shader.get();
            shaderImpl.setCloseAction(pool::add);
            pool.add(shaderImpl);
        }
	}
	
	@Override
	public void add(ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater)
    {       
	    parent.getFacetList().parallelStream()
                             .filter(f -> parent.isProcessBackfaces() || GeneralPredicates.isFrontface(c).test(f))
                             .filter(f -> !isOffScreen(f,c,screen))
                             .forEach(f -> add(f, parent, c, screen, zBufferItemUpdater));

    }
	
	private void add(Facet facet, ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater)
    {   
        List<WorldCoord> points = facet.getAsList();
        
        Comparator<WorldCoord> xComp = (o1, o2) -> (int)(o1.getTransformed(c).x - o2.getTransformed(c).x);

        double minX = points.stream().min(xComp).get().getTransformed(c).x;
        double maxX = points.stream().max(xComp).get().getTransformed(c).x;

        
        if (minX < 0) 
            minX = 0;
        if (maxX > screen.getWidth()) 
            maxX = screen.getWidth();          
        
        try (ScanlineShader localShader = getShader()) {
            if (localShader != null) {
                localShader.init(parent, facet, c);
            }
            
            List<LineEquation> lines = new ArrayList<>();
            
            lines.add(new LineEquation(points.get(0), points.get(1), c));
            lines.add(new LineEquation(points.get(1), points.get(2), c));
            lines.add(new LineEquation(points.get(2), points.get(0), c));
            
            for (int x = (int)Math.floor(minX) ; x <= Math.ceil(maxX) ; x++)
            {
                final int xVal = x;
                getScanline(x, lines).ifPresent(sl -> processScanline(sl, xVal, localShader, screen, zBufferItemUpdater));
            }
        } catch (Exception e) { }
    }
	
	private ScanlineShader getShader() {   
        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
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
	
	private void processScanline(ScanLine scanLine, int x, ScanlineShader shader, Dimension screen, ZBufferItemUpdater zBufferItemUpdater) {
        double scanLineLength = Math.floor(scanLine.getEndY()) - Math.floor(scanLine.getStartY());
        double percentDistCovered = 0;
        Color colour = null;
        for (int y = (int)Math.floor(scanLine.getStartY() < 0 ? 0 : scanLine.getStartY()) ; y < Math.floor(scanLine.getEndY() > screen.getHeight() ? screen.getHeight() : scanLine.getEndY() ) ; y++)
        {
            
            if (scanLineLength != 0)
            {
                percentDistCovered = (y - Math.floor(scanLine.getStartY())) / scanLineLength;
            }
            
            double z = this.interpolateZ(scanLine.getStartZ(), scanLine.getEndZ(), percentDistCovered);
            final int yVal = y;
            Supplier<Color> colourSupplier = () -> skip == 1 || yVal % skip == 0 || colour == null || shader == null ? shader.getColour(scanLine, x, yVal) : colour;
            zBufferItemUpdater.update(x, y, z, colourSupplier);
        }
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
	
	private boolean isOffScreen(Facet facet, Camera c, Dimension dimension) {
        facet.setFrontFace(GeneralPredicates.isFrontface(c).test(facet));
        return facet.getTransformedNormal(c).getZ() == 0 ||
                Utils.allMatchAny(facet.getAsList().stream().map(p -> p.getTransformed(c)).collect(Collectors.toList()), 
                        Lists.newArrayList(p -> p.z <= 1,
                                           p -> p.x < 0,
                                           p -> p.x > dimension.getWidth(),
                                           p -> p.y < 0,
                                           p -> p.y > dimension.getHeight()));
    }
}
