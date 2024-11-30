package com.graphics.lib.shader;

import java.awt.Color;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LineEquation;
import com.graphics.lib.Utils;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IShaderSelector;
import com.graphics.lib.lightsource.ILightSource;
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

    private final LinkedBlockingQueue<ScanlineShader> pool;
    private final IShaderSelector defaultSelector = (o, c) -> this;
    
    @Property(name="zbuffer.skip", defaultValue="2")
    private Integer skip;
    
    ScanlineShaderFactory(Supplier<ScanlineShader> shader, int poolSize) {
        pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0 ; i < poolSize ; i++) {
            ScanlineShader shaderImpl = shader.get();
            shaderImpl.setCloseAction(pool::add);
            pool.add(shaderImpl);
        }
    }
    
    @Override
    public void add(ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater, Collection<ILightSource> lightSources)
    {
        parent.getFacetList().parallelStream()
                             .filter(f -> parent.isProcessBackfaces() || GeneralPredicates.isFrontface(c).test(f))
                             .filter(f -> !isOffScreen(f,c,screen))
                             .forEach(f -> add(f, parent, c, screen, zBufferItemUpdater, lightSources));

    }

    public IShaderSelector getDefaultSelector() {
        return defaultSelector;
    }

    public ScanlineShader getShader() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    
    private void add(Facet facet, ICanvasObject parent, Camera c, Dimension screen, ZBufferItemUpdater zBufferItemUpdater, Collection<ILightSource> lightSources)
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
                localShader.init(parent, facet, c, lightSources);
            }
            
            var lines = List.of(new LineEquation(points.get(0), points.get(1), c),
                                new LineEquation(points.get(1), points.get(2), c),
                                new LineEquation(points.get(2), points.get(0), c));
            
            for (int x = (int)Math.floor(minX) ; x <= Math.ceil(maxX) ; x++)
            {
                final int xVal = x;
                getScanline(x, lines).ifPresent(sl -> processScanline(sl, xVal, localShader, screen, zBufferItemUpdater));
            }
        } catch (Exception ignored) { }
    }
    
    private Optional<ScanLine> getScanline(int xVal, List<LineEquation> lines)
    {
        ScanLine.Builder builder = ScanLine.builder();
        
        List<LineEquation> activeLines = lines.stream().filter(line -> xVal >= line.getMinX() && xVal <= line.getMaxX())
                                                       .filter(line -> {
                                                           Double y = line.getYAtX(xVal);
                                                           return Objects.nonNull(y) && y <= line.getMaxY() && y >= line.getMinY();
                                                       })
                                                       .toList();
        
        
        if (activeLines.size() < 2) {
            return Optional.empty();
        }
        
        if (activeLines.size() == 3) {
            return Optional.empty();
//largely useless hardly ever triggered
//            double dif1 = Math.abs(activeLines.get(0).getYAtX(xVal) - activeLines.get(1).getYAtX(xVal));
//            double dif2 = Math.abs(activeLines.get(1).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal));
//            double dif3 = Math.abs(activeLines.get(0).getYAtX(xVal) - activeLines.get(2).getYAtX(xVal));
//
//            if ((dif1 < dif2 && dif1 < dif3) || (dif3 < dif2 && dif3 < dif1)) {
//                activeLines.remove(0);
//            } else {
//                 activeLines.remove(2);
//            }
        }
        
        for(LineEquation line : activeLines)
        {
            Double y = line.getYAtX(xVal);
            Double z = line.getZValue(xVal, y);
            
            if (builder.startY == null) {
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
        int ceilEnd = (int)Math.round(scanLine.getEndY());
        int floorStart = (int)Math.round(scanLine.getStartY());
        int scanLineLength = ceilEnd - floorStart + 1;
        double percentDistCovered;
        AtomicReference<Color> colour = new AtomicReference<>();
        
        IntFunction<Color> colourSupplier = yVal -> {
            Color c = (skip <= 1 || yVal % skip == 0 || colour.get() == null) && shader != null ? shader.getColour(scanLine, x, yVal) : colour.get();
            colour.set(c);
            return c;
        };

        int positionOnScanLine = 0;
        if (floorStart < 0) {
            positionOnScanLine = Math.abs(floorStart);
        }

        while (positionOnScanLine < scanLineLength) {
            int y = floorStart + positionOnScanLine;
            if (y > screen.getHeight()) break;

            percentDistCovered = (double)positionOnScanLine / scanLineLength;

            double z = LineEquation.interpolateZ(scanLine.getStartZ(), scanLine.getEndZ(), percentDistCovered);
            zBufferItemUpdater.update(x, y, z, colourSupplier);

            positionOnScanLine++;
        }
    }
    
    private boolean isOffScreen(Facet facet, Camera c, Dimension dimension) {
        facet.setFrontFace(GeneralPredicates.isFrontface(c).test(facet));
        return facet.getTransformedNormal(c).z() == 0 ||
                Utils.allMatchAny(facet.getAsList().stream().map(p -> p.getTransformed(c)).toList(),
                        List.of(p -> p.z <= 1,
                                p -> p.x < 0,
                                p -> p.x > dimension.getWidth(),
                                p -> p.y < 0,
                                p -> p.y > dimension.getHeight()));
    }
}
