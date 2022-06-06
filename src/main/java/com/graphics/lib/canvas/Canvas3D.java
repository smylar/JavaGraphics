package com.graphics.lib.canvas;

import static com.graphics.lib.canvas.CanvasEvent.PAINT;
import static com.graphics.lib.canvas.CanvasEvent.PREPARE_BUFFER;
import static com.graphics.lib.canvas.CanvasEvent.PROCESS;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.properties.Property;
import com.graphics.lib.properties.PropertyInject;
import com.graphics.lib.shader.IShaderFactory;
import com.graphics.lib.shader.PointShader;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.shader.WireframeShader;
import com.graphics.lib.traits.TrackingTrait;
import com.graphics.lib.util.DrawMode;
import com.graphics.lib.zbuffer.IZBuffer;
import com.graphics.lib.zbuffer.ZBufferItem;

import io.reactivex.Observable;

/**
 * Responsible for output to the screen
 * 
 * @author Paul Brandon
 *
 */
@PropertyInject
public class Canvas3D extends AbstractCanvas {

	private static final long serialVersionUID = 1L;
	private static Canvas3D cnv = null;
	
	private Map<ICanvasObject, IShaderFactory> shapes = Collections.synchronizedMap(new HashMap<ICanvasObject, IShaderFactory>());
	
	private Set<ILightSource> lightSources = new HashSet<>(); 
	private Set<ILightSource> lightSourcesToAdd = Collections.synchronizedSet(new HashSet<ILightSource>()); 
	private double horizon = 8000;
	private Set<ICanvasUpdateListener> slaves = new HashSet<>();
	private boolean drawShadows = false;
	private Facet floor = null;
	private long tickCount = 0;
	
	@Property(name="canvas.drawMode", defaultValue="NORMAL")
    private DrawMode drawMode;

	protected Canvas3D(Camera camera)
    {
        super(camera);   
    }
	
	public static Canvas3D get(Camera camera){
		if (cnv == null) {
		    cnv = new Canvas3D(camera);
		} else {
		    cnv.setCamera(camera);
		}
		return cnv;
	}
	
	public static Canvas3D get() {
		return cnv;
	}
	
	public double getHorizon() {
		return horizon;
	}

	public void setHorizon(double horizon) {
		this.horizon = horizon;
	}
	
	public boolean isDrawShadows() {
		return drawShadows;
	}

	public void setDrawShadows(boolean drawShadows) {
		this.drawShadows = drawShadows;
	}
	
	public Facet getFloor() {
		return floor;
	}

	public void setFloor(Facet floor) {
		this.floor = floor;
	}

	public void addLightSource(LightSource lightSource) {
		if (lightSource != null) {
		    this.lightSourcesToAdd.add(lightSource);
		}
	}
	
	public Set<ICanvasObject> getShapes(Predicate<ICanvasObject> predicate) {
	    synchronized(shapes) {
	        return shapes.keySet().stream().filter(predicate).collect(Collectors.toSet());
	    }
	}
	
	public Set<ICanvasObject> getShapes() {
		return shapes.keySet();
	}
	
	public IShaderFactory getShader(ICanvasObject obj) {
		return shapes.get(obj) == null ? ScanlineShaderFactory.NONE : shapes.get(obj);
	}
	
	public void replaceShader(ICanvasObject obj, IShaderFactory shader) {
	    
	    if (!drawMode.fixedShader()) {
	        this.shapes.replace(obj, shader);
	    }
	}

	public Set<ILightSource> getLightSources() {
		return lightSources;
	}
	
	public void addObserver(ICanvasUpdateListener l){
		this.slaves.add(l);
	}
	
	public long getTicks() {
	    return tickCount;
	}
	
	public Optional<ICanvasObject> getObjectAt(final int x, final int y) {
	    
	    return Optional.ofNullable(getzBuffer())
	                   .map(zBuf -> zBuf.getItemAt(x, y))
	                   .map(ZBufferItem::getTopMostObject);
	}

	/**
	 * Register an object with this canvas so that it will draw it at a specified (world) location with a default shader
	 * 
	 * @param obj		Object to register
	 * @param position	Position to draw the centre of the object at
	 */
	public void registerObject(ICanvasObject obj, Point position){
		this.registerObject(obj, position, ScanlineShaderFactory.NONE);
	}
	
	/**
	 * Register an object with this canvas so that it will draw it at a specified (world) location
	 * 
	 * @param obj		Object to register
	 * @param position	Position to draw the centre of the object at
	 * @param shader	Shader to draw the object with
	 */
	public void registerObject(ICanvasObject obj, Point position, IShaderFactory shaderFactory)
	{
		if (Objects.nonNull(obj) && !this.shapes.containsKey(obj)) {
		    CanvasObjectFunctions.DEFAULT.get().moveTo(obj, position);
		    var shader = switch (drawMode) {
		        case POINT -> PointShader.getShader();
		        case WIRE -> WireframeShader.getShader();
		        default -> shaderFactory;
		    }; //could just put these in the enum

		    this.shapes.put(obj, shader);

		}
	}

	/**
	 * Trigger a draw cycle
	 */
	public Observable<ICanvasObject> doDraw()
	{
	    tickCount++;
		addPendingLightsources();
		
		getCamera().setViewport(this.getWidth(), this.getHeight());
		
		this.lightSources.removeIf(ILightSource::isDeleted);
		
		Set<ICanvasObject> processShapes = Set.copyOf(this.getShapes())
		                                       .parallelStream()
		                                       .filter(this::filterShapes)
		                                       .map(ICanvasObject::applyTransforms)
		                                       .collect(Collectors.toSet());
		
		getCamera().doTransforms();
		
		if (this.drawShadows && Objects.nonNull(this.floor)) {

		    processShapes.addAll(processShapes.stream() 
		                                      .flatMap(s -> getShadowOnFloor(s).parallelStream())
		                                      .collect(Collectors.toSet()));
		}

		return renderShapes(processShapes);
	}
	
	private boolean filterShapes(ICanvasObject shape) {
	    if (shape.isDeleted()) {
	        shapes.remove(shape);
	        return false;
	    }
	    
	    return !shape.hasFlag(TrackingTrait.TRACKING_TAG);
	}
	
	@Override
	protected void prepareZBuffer() {
        super.prepareZBuffer();
        notifyEvent(PREPARE_BUFFER);
    }
	
	private Observable<ICanvasObject> renderShapes(Set<ICanvasObject> shapes) {
	    return Observable.fromIterable(shapes)
	              .doOnSubscribe(d -> prepareZBuffer())
	              .doOnNext(s -> {
	                  s.onDrawComplete();  //cross object updates can happen here safer not to be parallel
	                  processShape(s, getzBuffer(), getShader(s));
	                  notifyEvent(PROCESS, s);
	              })
	              .doFinally(() -> {
	                  getzBuffer().refreshBuffer();
	                  SwingUtilities.invokeLater(this::repaint);
	                  notifyEvent(PAINT);
	              });
	    //may try and get the slave canvases to subscribe to this, instead of a specific notification
	}
	
	private void notifyEvent(CanvasEvent event) {
        notifyEvent(event, null);
    }
	
	private void notifyEvent(CanvasEvent event, ICanvasObject object) {
	    slaves.forEach(sl -> sl.update(this, event, object));
	}
	
	private void addPendingLightsources() {
	    if (!lightSourcesToAdd.isEmpty()) {
            lightSources.addAll(lightSourcesToAdd);
            lightSourcesToAdd.clear();
        }
	}
	
	
	/**
	 * Process a shape into z buffer entries using the given shader
	 * 
	 * @param obj	Object to process
	 * @param zBuf	Z Buffer to update
	 * @param shader	Shader to draw pixels with
	 */
	private void processShape(ICanvasObject obj, IZBuffer zBuf, IShaderFactory shader)
	{
		if (obj.isVisible() && !obj.isDeleted())
		{
		    getCamera().getView(obj);
			
			zBuf.add(obj, shader, getCamera(), this.horizon);
		}
		
		obj.getChildren().forEach(child -> this.processShape(child, zBuf, shapes.containsKey(child) ? getShader(child) : shader));
	}
	
	
	/**
	 * EXPERIMENTAL - Used to project shadows on to a plane (in this case a floor plane - though it could be any plane)
	 * <br/>
	 * It generates a temporary canvas object that describes how the shadow is drawn, just like any other canvas object
	 * <br/>
	 * At the moment, it will just colour shadow areas in grey and does not take account of other light sources, that might mean there should be some lighting in a shaded area 
	 * 
	 * @param obj	Object to project a shadow for
	 * @return		Set of generated shadow objects
	 */
	private Set<CanvasObject> getShadowOnFloor(ICanvasObject obj){	

		if (floor == null || !obj.getCastsShadow()) {
		    return Sets.newHashSet();
		}
		final Color shadowColour = new Color(150,150,150,50);
		return lightSources.stream()
		                   .filter(ILightSource::isOn)
		                   .map(ls -> 
		                       new CanvasObject(() -> {
		                           Builder<WorldCoord> vertexList = ImmutableList.builder();
		                           Builder<Facet> facetList = ImmutableList.builder();
		                           for (Facet f : obj.getFacetList()){
		                               //TODO this will work out the intersection for a point several times depending on the facets and creates more points than necessary, will want rewrite for better efficiency
		                               if (obj.isVisible() && GeneralPredicates.isLit(ls).test(f)) {
		                                   addShadowPoints(ls, f, vertexList, facetList);
		                               }
		                           }
		                           return Pair.of(vertexList.build(), facetList.build());
		                       })
		                   )
		                   .filter(shadow -> !shadow.getFacetList().isEmpty())
		                   .peek(shadow -> {
		                       shadow.setColour(shadowColour);
                               shadow.setProcessBackfaces(true);
                               shadow.setVisible(true);
		                   })
		                   .collect(Collectors.toSet());
	}
	
	private void addShadowPoints(ILightSource ls, Facet f, Builder<WorldCoord> vertexList, Builder<Facet> facetList) {
	    List<WorldCoord> points = f.getAsList();
        
        WorldCoord p1 = getShadowPoint(ls.getPosition(), points.get(0));
        if (p1 == null) 
            return;
        WorldCoord p2 = getShadowPoint(ls.getPosition(), points.get(1));
        if (p2 == null) 
            return;
        WorldCoord p3 = getShadowPoint(ls.getPosition(), points.get(2));
        if (p3 == null) 
            return;
        
        vertexList.add(p1);
        vertexList.add(p2);
        vertexList.add(p3);
        facetList.add(new Facet(p1,p2,p3));
	}
	
	/**
	 * Gets the point on the floor (shadow plane) for the line described by the two given points
	 * 
	 * @param start
	 * @param end
	 * @return World coordinate of intersection with the floor
	 */
	private WorldCoord getShadowPoint(Point start, Point end){
		Vector lightVector = start.vectorToPoint(end).getUnitVector();
		Point intersect = floor.getIntersectionPointWithFacetPlane(end, lightVector);
		
		return Optional.ofNullable(intersect).map(WorldCoord::new).orElse(null);
	}

}
