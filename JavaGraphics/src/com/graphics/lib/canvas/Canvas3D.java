package com.graphics.lib.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JPanel;
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
import com.graphics.lib.ZBufferEnum;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.shader.IShaderFactory;
import com.graphics.lib.shader.ShaderFactory;
import com.graphics.lib.traits.TrackingTrait;
import com.graphics.lib.zbuffer.ZBufferItem;

/**
 * Responsible for output to the screen
 * 
 * @author Paul Brandon
 *
 */
public class Canvas3D extends JPanel {

	private static final long serialVersionUID = 1L;
	private static Canvas3D cnv = null;
	
	private Map<ICanvasObject, IShaderFactory> shapes = Collections.synchronizedMap(new HashMap<ICanvasObject, IShaderFactory>());
	
	private Set<ILightSource> lightSources = new HashSet<>(); 
	private Set<ILightSource> lightSourcesToAdd = Collections.synchronizedSet(new HashSet<ILightSource>()); 
	private Camera camera;
	private IZBuffer zBuffer;
	private double horizon = 8000;
	private Set<ICanvasUpdateListener> slaves = new HashSet<>();
	private transient List<BiConsumer<Canvas3D,Graphics>> drawPlugins = new ArrayList<>();
	private boolean drawShadows = false;
	private Facet floor = null;
	private long tickCount = 0;

	protected Canvas3D(Camera camera)
    {
        this.camera = camera;   
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
	
	/**
	 * Allows anonymous functions to be registered in order to customise the display
	 * 
	 * @param operation A paint operation
	 */
	public void addDrawOperation(BiConsumer<Canvas3D,Graphics> operation) {
		drawPlugins.add(operation);
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
		return shapes.get(obj) == null ? ShaderFactory.NONE : shapes.get(obj);
	}
	
	public void replaceShader(ICanvasObject obj, IShaderFactory shader) {
		this.shapes.replace(obj, shader);
	}

	public Set<ILightSource> getLightSources() {
		return lightSources;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void addObserver(ICanvasUpdateListener l){
		this.slaves.add(l);
	}
	
	public void setzBuffer(IZBuffer zBuffer) {
		this.zBuffer = zBuffer;
	}
	
	public IZBuffer getzBuffer() {
		return zBuffer;
	}
	
	public long getTicks() {
	    return tickCount;
	}
	
	public Optional<ICanvasObject> getObjectAt(final int x, final int y) {
	    
	    return Optional.ofNullable(zBuffer)
	                   .map(zBuf -> zBuf.getItemAt(x, y))
	                   .map(ZBufferItem::getTopMostObject);
	}

	@Override
	public void setVisible(boolean flag)
	{
		super.setVisible(flag);
		this.camera.setViewport(this.getWidth(), this.getHeight());
	}

	/**
	 * Register an object with this canvas so that it will draw it at a specified (world) location with a default shader
	 * 
	 * @param obj		Object to register
	 * @param position	Position to draw the centre of the object at
	 */
	public void registerObject(ICanvasObject obj, Point position){
		this.registerObject(obj, position, ShaderFactory.NONE);
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
    		this.shapes.put(obj, shaderFactory);
		}
	}
	
	/**
	 * Trigger a draw cycle
	 */
	public void doDraw()
	{
	    tickCount++;
		addPendingLightsources();
		
		prepareZBuffer();
		
		this.camera.setViewport(this.getWidth(), this.getHeight());
		
		this.lightSources.removeIf(ILightSource::isDeleted);
		
		Set<ICanvasObject> processShapes = new HashSet<>(this.getShapes());
		processShapes.stream().filter(ICanvasObject::isDeleted).forEach(this.shapes::remove);
		processShapes.removeIf(s -> s.isDeleted() || s.hasFlag(TrackingTrait.TRACKING_TAG));

		processShapes.parallelStream().forEach(ICanvasObject::applyTransforms);
		
		this.camera.doTransforms();
		
		if (this.drawShadows && Objects.nonNull(this.floor)) {
//			Set<CanvasObject> shadows = Collections.synchronizedSet(new HashSet<CanvasObject>()); 
//			processShapes.parallelStream().forEach(s -> shadows.addAll(getShadowOnFloor(s)));
//			processShapes.addAll(shadows);
		    processShapes.addAll(processShapes.parallelStream()
		                                      .flatMap(s -> getShadowOnFloor(s).stream())
		                                      .collect(Collectors.toSet()));
		}

		processShapes.forEach(ICanvasObject::onDrawComplete); //cross object updates can happen here safer not to be parallel
		
		processShapes.parallelStream().forEach(s -> {
			this.processShape(s, this.zBuffer, getShader(s));
			this.slaves.forEach(sl -> sl.update(this, s));
		});
		
		
		processShapes.clear();
		this.zBuffer.refreshBuffer();
		SwingUtilities.invokeLater(() -> repaint()); //might squeeze some extra performance, may need to be careful, may need to synchronise the refreshBuffer call
		
		slaves.forEach(sl -> sl.update(this, null));
		
	}
	
	private void addPendingLightsources() {
	    if (!lightSourcesToAdd.isEmpty()) {
            lightSources.addAll(lightSourcesToAdd);
            lightSourcesToAdd.clear();
        }
	}
	
	private void prepareZBuffer() {
	    if (Objects.isNull(this.zBuffer)) {
            this.zBuffer = ZBufferEnum.DEFAULT.get();
        } else {
            this.zBuffer.clear();
        }
        
        this.zBuffer.setDimensions(this.getWidth(), this.getHeight());
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
			this.camera.getView(obj);
			
			zBuf.add(obj, shader, this.camera, this.horizon);
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
		                   .filter(shadow -> shadow.getFacetList().size() > 0)
		                   .peek(shadow -> {
		                       shadow.setColour(new Color(50,50,50));
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
	
	@Override
	public void paintComponent(Graphics g)
	{
		if (Objects.nonNull(this.zBuffer)) {
    		super.paintComponent(g);
    
    		g.drawImage(this.zBuffer.getBuffer(), 0, 0, null);
    
    		drawPlugins.forEach(op -> op.accept(this, g));
		}
	}

}
