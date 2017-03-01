package com.graphics.lib.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.swing.JPanel;

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
import com.graphics.lib.shader.IShader;
import com.graphics.lib.zbuffer.ZBufferItem;

/**
 * Responsible for output to the screen
 * 
 * @author Paul Brandon
 *
 */
public class Canvas3D extends JPanel{

	private static final long serialVersionUID = 1L;
	private static Canvas3D cnv = null;
	
	private Map<ICanvasObject, IShader> shapes = Collections.synchronizedMap(new HashMap<ICanvasObject, IShader>());
	
	private Set<ILightSource> lightSources = new HashSet<ILightSource>(); 
	private Set<ILightSource> lightSourcesToAdd = Collections.synchronizedSet(new HashSet<ILightSource>()); 
	private Camera camera;
	private IZBuffer zBuffer;
	private double horizon = 8000;
	private Set<ICanvasUpdateListener> slaves = new HashSet<ICanvasUpdateListener>();
	private List<BiConsumer<Canvas3D,Graphics>> drawPlugins = new ArrayList<BiConsumer<Canvas3D,Graphics>>();
	private boolean drawShadows = false;
	private Facet floor = null;
	private boolean okToPaint = false;

	public static Canvas3D get(Camera camera){
		if (cnv == null) cnv = new Canvas3D(camera);
		else cnv.setCamera(camera);
		return cnv;
	}
	
	public static Canvas3D get(){
		return cnv;
	}
	
	protected Canvas3D(Camera camera)
	{
		this.camera = camera;	
	}
	
	/**
	 * Allows anonymous functions to be registered in order the customise the display
	 * 
	 * @param operation A paint operation
	 */
	public void addDrawOperation(BiConsumer<Canvas3D,Graphics> operation){
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
		if (lightSource != null) this.lightSourcesToAdd.add(lightSource);
	}
	
	public Set<ICanvasObject> getShapes() {
		return shapes.keySet();
	}
	
	public IShader getShader(ICanvasObject obj){
		return shapes.get(obj);
	}
	
	public void replaceShader(ICanvasObject obj, IShader shader){
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

	public boolean isOkToPaint() {
		return okToPaint;
	}

	public void setOkToPaint(boolean okToPaint) {
		this.okToPaint = okToPaint;
	}
	
	public ICanvasObject getObjectAt(int x, int y){
		if (zBuffer == null) return null;
		
		ZBufferItem item = zBuffer.getItemAt(x, y);
		
		if (item == null) return null;
		
		return item.getTopMostObject();
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
	public void registerObject(CanvasObject obj, Point position){
		this.registerObject(obj, position, null);
	}
	
	/**
	 * Register an object with this canvas so that it will draw it at a specified (world) location
	 * 
	 * @param obj		Object to register
	 * @param position	Position to draw the centre of the object at
	 * @param shader	Shader to draw the object with
	 */
	public void registerObject(CanvasObject obj, Point position, IShader shader)
	{
		if (this.shapes.containsKey(obj)) return;
		CanvasObjectFunctions.DEFAULT.get().moveTo(obj, position);
		this.shapes.put(obj, shader);
	}
	
	/**
	 * Trigger a draw cycle
	 */
	public void doDraw()
	{
		if (lightSourcesToAdd.size() > 0){
			lightSources.addAll(lightSourcesToAdd);
			lightSourcesToAdd.clear();
		}
		
		while(this.zBuffer != null && this.isOkToPaint()){
			//while isOkToPaint() is true then repaint has not yet happened since the last draw cycle
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		if (this.zBuffer == null)
			this.zBuffer = ZBufferEnum.DEFAULT.get();
		
		this.zBuffer.setDimensions(this.getWidth(), this.getHeight());
		this.camera.setViewport(this.getWidth(), this.getHeight());
		
		this.lightSources.removeIf(l -> l.isDeleted());
		
		Set<ICanvasObject> processShapes = new HashSet<>(this.getShapes());
		processShapes.stream().filter(s -> s.isDeleted()).forEach(s -> this.shapes.remove(s));
		processShapes.removeIf(s -> s.isDeleted() || s.isObserving());

		processShapes.parallelStream().forEach(s -> {
			this.processShape(s);
		});
		
		this.camera.doTransforms();
		
		if (this.drawShadows && this.floor != null){
			Set<CanvasObject> shadows = Collections.synchronizedSet(new HashSet<CanvasObject>()); 
			processShapes.parallelStream().forEach(s -> {
				shadows.addAll(getShadowOnFloor(s));
			});
			processShapes.addAll(shadows);
		}
		
		processShapes.parallelStream().forEach(s -> {
			s.onDrawComplete();
		});
		
		processShapes.parallelStream().forEach(s -> {
			this.processShape(s, this.zBuffer, getShader(s));
			this.slaves.forEach(sl -> sl.update(this, s));
		});
		
		
		
		this.setOkToPaint(true);
		this.repaint(); 

		this.slaves.forEach(sl -> sl.update(this, null));		
		
	}
	
	/**
	 * Process a shape into z buffer entries using the given shader
	 * 
	 * @param obj	Object to process
	 * @param zBuf	Z Buffer to update
	 * @param shader	Shader to draw pixels with
	 */
	private void processShape(ICanvasObject obj, IZBuffer zBuf, IShader shader)
	{
		if (obj.isVisible() && !obj.isDeleted())
		{
			this.camera.getView(obj);
			if (shader != null) shader.setLightsources(lightSources);
			zBuf.Add(obj, shader, this.camera, this.horizon);
		}
		
		for (ICanvasObject child : new ArrayList<CanvasObject>(obj.getChildren()))
		{
			this.processShape(child, zBuf, shapes.containsKey(child) ? getShader(child) : shader);
		}
	}
	
	private void processShape(ICanvasObject obj)
	{
		obj.applyTransforms();
		
		for (ICanvasObject child : new ArrayList<CanvasObject>(obj.getChildren()))
		{
			this.processShape(child);
		}
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
		Set<CanvasObject> shadows = new HashSet<CanvasObject>();
		if (floor == null || !obj.getCastsShadow()) return shadows;
		
		for (ILightSource ls : lightSources){
			if (!ls.isOn()) continue;
			
			CanvasObject shadow = new CanvasObject();
			shadow.setColour(new Color(50,50,50));
			shadow.setProcessBackfaces(true);
			shadow.setVisible(true);
			for (Facet f : obj.getFacetList()){
				//TODO this will work out the intersection for a point several times depending on the facets and creates more points than necessary, will want rewrite for better efficiency
				if (!obj.isVisible() || !GeneralPredicates.isLit(ls).test(f)) continue;
				
				List<WorldCoord> points = f.getAsList();
				
				WorldCoord p1 = getShadowPoint(ls.getPosition(), points.get(0));
				if (p1 == null) continue;
				WorldCoord p2 = getShadowPoint(ls.getPosition(), points.get(1));
				if (p2 == null) continue;
				WorldCoord p3 = getShadowPoint(ls.getPosition(), points.get(2));
				if (p3 == null) continue;
				
				shadow.getVertexList().add(p1);
				shadow.getVertexList().add(p2);
				shadow.getVertexList().add(p3);
				shadow.getFacetList().add(new Facet(p1,p2,p3));
			}
			if (shadow.getFacetList().size() > 0) shadows.add(shadow);
		}
		return shadows;
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
		if (intersect == null) return null;
		
		WorldCoord planePoint = new WorldCoord(intersect);
		return planePoint;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		//TODO maybe find a way of not dumping all the ZBufferItem objects each cycle, there is a noticeable slow down with larger buffer
		//every few seconds, presumably when the GC clears them up, but running GC each cycle hurts more, 
		//ZBufferItems now reused and not dumped, think it has improved, however, the TreeMap in that item is still being cleared each cycle
		//may also want to look at all the temporary Vector objects etc that get created too, though don't think that is as much an issue
		
		//did have paint synchronised as can't be sure when paint triggered, 
		//but that takes resources, so trying to remove as many as possible - using an ok to paint flag here
		if (this.zBuffer == null || !this.isOkToPaint()) return;
		super.paintComponent(g);
		//java.util.Date then = new java.util.Date();
		for (List<ZBufferItem> x : this.zBuffer.getBuffer()){
			x.stream().filter(item -> item != null && item.isActive()).forEach(item -> {
				g.setColor(item.getColour());
				g.drawLine(item.getX(), item.getY(), item.getX(), item.getY());
			});
		}
		//System.out.println(new java.util.Date().getTime() - then.getTime());
		for(BiConsumer<Canvas3D,Graphics> op : drawPlugins){
			op.accept(this,g);
		}
		
		this.zBuffer.clear();
		
		this.setOkToPaint(false);
	}

}
