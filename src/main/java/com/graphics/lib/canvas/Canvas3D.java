package com.graphics.lib.canvas;

import static com.graphics.lib.canvas.CanvasEvent.PAINT;
import static com.graphics.lib.canvas.CanvasEvent.PREPARE_BUFFER;
import static com.graphics.lib.canvas.CanvasEvent.PROCESS;

import java.awt.Color;
import java.io.Serial;
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

import com.graphics.lib.interfaces.IShaderSelector;
import com.graphics.lib.scene.SceneMap;
import com.graphics.lib.scene.SceneObject;
import com.graphics.lib.scene.SceneWithOffset;
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
import com.graphics.lib.interfaces.ISecondaryCamera;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.properties.Property;
import com.graphics.lib.properties.PropertyInject;
import com.graphics.lib.scene.SceneFrame;
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

	private static final String UNBOUND = "unbound";
	private static final IShaderSelector pointShaderSelector = (o, c) -> PointShader.getShader();
	private static final IShaderSelector wireShaderSelector = (o,c) -> WireframeShader.getShader();
	@Serial
	private static final long serialVersionUID = 1L;
	private static Canvas3D cnv = null;
	
	private final Map<ICanvasObject, IShaderSelector> shapes = Collections.synchronizedMap(new HashMap<>());
	
	private final Set<ILightSource> lightSources = new HashSet<>();
	private final Set<ILightSource> lightSourcesToAdd = Collections.synchronizedSet(new HashSet<>());
	private double horizon = 8000;
	private final Set<ISecondaryCamera> slaves = new HashSet<>();
	private boolean drawShadows = false;
	private long tickCount = 0;
	private final SceneMap sceneMap;
	private SceneFrame currentFrame;
	private Set<SceneWithOffset> loadedFrames = new HashSet<>();

	
	@Property(name="canvas.drawMode", defaultValue="NORMAL")
    private DrawMode drawMode;

	protected Canvas3D(Camera camera, SceneMap sceneMap) {
		super(camera);
		this.sceneMap = sceneMap;
    }
	
	public static Canvas3D get(Camera camera, SceneMap sceneMap) {
		if (cnv == null) {
		    cnv = new Canvas3D(camera, sceneMap);
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
	
	public IShaderFactory getShader(ICanvasObject obj, Camera cam) {
		return shapes.get(obj) == null ? ScanlineShaderFactory.NONE : shapes.get(obj).apply(obj, cam);
	}
	
	public void replaceShader(ICanvasObject obj, IShaderSelector selector) {
	    
	    if (!drawMode.fixedShader()) {
	        this.shapes.replace(obj, selector);
	    }
	}

	public Set<ILightSource> getLightSources() {
		return lightSources;
	}
	
	public Facet getFloorPlane() {
	    if (currentFrame == null) {
			return null;

	    }
		return currentFrame.getFloor().getFacetList().get(0);
	}
	
	public SceneFrame getCurrentScene() {
		return currentFrame;
	}
	
	public void addObserver(ISecondaryCamera l){
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

	public void registerObject(ICanvasObject obj, Point position) {
		registerObject(obj, position, (o,c) -> ScanlineShaderFactory.NONE, false);
	}

	public void registerObject(ICanvasObject obj, Point position, IShaderSelector shaderSelector){
		registerObject(obj, position, shaderSelector, false);
	}

	/**
	 * Trigger a draw cycle
	 */
	public Observable<ICanvasObject> doDraw()
	{
	    tickCount++;
	    switchFrames();
		addPendingLightsources();
		
		getCamera().setViewport(this.getWidth(), this.getHeight());
		
		this.lightSources.removeIf(ILightSource::isDeleted);
		
		Set<ICanvasObject> processShapes = Set.copyOf(this.getShapes())
		                                       .parallelStream()
		                                       .filter(this::filterShapes)
		                                       .map(ICanvasObject::applyTransforms)
		                                       .collect(Collectors.toSet());
		
		getCamera().doTransforms();
		
		if (this.drawShadows && Objects.nonNull(this.getFloorPlane())) {

		    processShapes.addAll(processShapes.stream() 
		                                      .flatMap(s -> getShadowOnFloor(s).parallelStream())
		                                      .collect(Collectors.toSet()));
		}

		return renderShapes(processShapes);
	}

	private void registerObject(ICanvasObject obj, Point position, IShaderSelector shaderSelector, boolean sceneBound)
	{
		if (Objects.isNull(obj) || shapes.containsKey(obj)) {
			return;
		}

		CanvasObjectFunctions.DEFAULT.get().moveTo(obj, position);
		var shader = switch (drawMode) {
			case POINT -> pointShaderSelector;
			case WIRE -> wireShaderSelector;
			default -> shaderSelector;
		}; //could just put these in the enum

		shapes.put(obj, shader);

		if (!sceneBound) {
			obj.addFlag(UNBOUND); //may want an explicit boolean for this in the interface for slightly better performance
		}

	}
	
	private void switchFrames() {

	    Point camPos = getCamera().getPosition();
		//we'll presume frame floor coords are axis aligned as it makes sense to do so, therefore no transforms required
		//we'll also assume they all have the same orientation (unless we start spinning rooms or something)
		SceneWithOffset sceneWithOffset = sceneMap.getFrameFromPoint(camPos);
		SceneFrame cameraFrame = sceneWithOffset.scene();

		Set<SceneWithOffset> framesToLoad = slaves.stream().map(c -> c.getRelevantFrame(sceneMap)).collect(Collectors.toSet());
		framesToLoad.add(sceneWithOffset);

		// TODO this will load at the border, obviously will want something that loads as you get near while keeping the current one
		// or have walls between them!
		if (currentFrame != cameraFrame) {
			currentFrame = cameraFrame;
		}

		framesToLoad.stream()
				.filter(f -> !loadedFrames.contains(f))
				.forEach(f -> loadScene(cameraFrame, f.xOffset(), f.zOffset()));

		loadedFrames.stream()
				.filter(f -> !framesToLoad.contains(f))
				.forEach(f -> f.scene().destroyFrame());

		loadedFrames = framesToLoad;
	}

	private void loadScene (SceneFrame newFrame, int xOffset, int zOffset) {
		newFrame.buildFrame();

		newFrame.getFrameLightsources().forEach(ls -> {
			Point lsPosition = ls.getPosition();
			ls.setPosition(new Point(lsPosition.x + xOffset, lsPosition.y, lsPosition.z + zOffset));
			addLightSource(ls);
		});

		newFrame.getFrameObjects().forEach(o -> {
			Point position = o.framePosition();
			registerObject(o.object(), new Point(position.x + xOffset, position.y, position.z + zOffset), o.shaderSelector(), true);
		});
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
		Set<ICanvasObject> frameObjects = currentFrame.getFrameObjects().stream().map(SceneObject::object).collect(Collectors.toSet());
	    return Observable.fromIterable(shapes)
	              .doOnSubscribe(d -> prepareZBuffer())
	              .doOnNext(s -> {
	                  s.onDrawComplete();  //cross object updates can happen here safer not to be parallel
	                  notifyEvent(PROCESS, s);
	              })
				  .filter(s -> frameObjects.contains(s) || s.hasFlag(UNBOUND)) //only draw if in current frame, will need to do something with light sources too, to stop cross lighting
				  .doOnNext(s -> processShape(s, getzBuffer(), getShader(s, getCamera())))
	              .doFinally(() -> {
	                  getzBuffer().refreshBuffer();
	                  SwingUtilities.invokeLater(this::repaint);
	                  notifyEvent(PAINT);
	              });
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

	private void processShape(ICanvasObject obj, IZBuffer zBuf, IShaderFactory shader)
	{
		if (obj.isVisible() && !obj.isDeleted())
		{
		    getCamera().getView(obj);
			
			zBuf.add(obj, shader, getCamera(), this.horizon);
		}
		
		obj.getChildren().forEach(child -> this.processShape(child, zBuf, shader));
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

	    Facet floor = getFloorPlane();
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

	private WorldCoord getShadowPoint(Point start, Point end) {
	    Facet floor = getFloorPlane();
		Vector lightVector = start.vectorToPoint(end).getUnitVector();
		Point intersect = floor.getIntersectionPointWithFacetPlane(end, lightVector);
		
		return Optional.ofNullable(intersect).map(WorldCoord::new).orElse(null);
	}

}
