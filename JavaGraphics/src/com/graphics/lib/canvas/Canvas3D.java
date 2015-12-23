package com.graphics.lib.canvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.swing.JPanel;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.shader.IShader;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.zbuffer.ZBufferItem;

public class Canvas3D extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private Map<CanvasObject, IShader> shapes = Collections.synchronizedMap(new HashMap<CanvasObject, IShader>());
	
	private Set<LightSource> lightSources = new HashSet<LightSource>(); 
	private Set<LightSource> lightSourcesToAdd = Collections.synchronizedSet(new HashSet<LightSource>()); 
	private Camera camera;
	private IZBuffer zBuffer;
	private double horizon = 8000;
	private Set<ICanvasUpdateListener> slaves = new HashSet<ICanvasUpdateListener>();
	private List<BiConsumer<Canvas3D,Graphics>> drawPlugins = new ArrayList<BiConsumer<Canvas3D,Graphics>>();
	private boolean okToPaint = false;

	public Canvas3D(Camera camera)
	{
		this.camera = camera;	
	}
	
	public void addDrawOperation(BiConsumer<Canvas3D,Graphics> operation){
		drawPlugins.add(operation);
	}
	
	public double getHorizon() {
		return horizon;
	}

	public void setHorizon(double horizon) {
		this.horizon = horizon;
	}

	public void addLightSource(LightSource lightSource) {
		this.lightSourcesToAdd.add(lightSource);
	}

	public void registerObject(CanvasObject obj, Point position)
	{
		this.registerObject(obj, position, false);
	}
	
	public Set<CanvasObject> getShapes() {
		return shapes.keySet();
	}
	
	public IShader getShader(CanvasObject obj){
		return shapes.get(obj);
	}
	
	public void replaceShader(CanvasObject obj, IShader shader){
		this.shapes.replace(obj, shader);
	}

	public Set<LightSource> getLightSources() {
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

	@Override
	public void setVisible(boolean flag)
	{
		super.setVisible(flag);
		this.camera.setViewport(this.getWidth(), this.getHeight());
	}

	public void registerObject(CanvasObject obj, Point position, boolean drawNow){
		this.registerObject(obj, position, drawNow, null);
	}
	
	public void registerObject(CanvasObject obj, Point position, boolean drawNow, IShader shader)
	{
		if (this.shapes.containsKey(obj)) return;

		obj.applyTransform(new Translation(position));
		this.shapes.put(obj, shader);
		
		if (drawNow)
		{
			this.doDraw();
		}
	}
	
	public void doDraw()
	{
		if (lightSourcesToAdd.size() > 0){
			lightSources.addAll(lightSourcesToAdd);
			lightSourcesToAdd.clear();
		}
		
		while(this.zBuffer != null && this.isOkToPaint()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		if (this.zBuffer == null)
			this.zBuffer = Utils.getDefaultZBuffer();
		
		this.zBuffer.setDimensions(new Dimension(this.getWidth(), this.getHeight()));
		this.camera.setViewport(this.getWidth(), this.getHeight());
		
		this.lightSources.removeIf(l -> l.isDeleted());
		
		Set<CanvasObject> processShapes = new HashSet<CanvasObject>(this.getShapes());
		processShapes.stream().filter(s -> s.isDeleted()).forEach(s -> this.shapes.remove(s));
		processShapes.removeIf(s -> s.isDeleted() || s.isObserving());
		
		this.camera.doTransforms();
		
		processShapes.parallelStream().forEach(s -> {
			this.processShape(s, this.zBuffer, getShader(s));
		});
		
		this.setOkToPaint(true);
		this.repaint(); 
		
		this.slaves.forEach(s -> s.update(this));
			
		processShapes.parallelStream().forEach(s -> {
			s.onDrawComplete();
		});
	}
	
	private void processShape(CanvasObject obj, IZBuffer zBuf, IShader shader)
	{
		obj.applyTransforms();
		if (obj.isVisible())
		{
			this.camera.getView(obj);
			if (shader != null) shader.setLightsources(lightSources);
			Stream<Facet> facetStream = obj.getFacetList().parallelStream();
			if (!obj.isProcessBackfaces()){
				facetStream = facetStream.filter(GeneralPredicates.isFrontface(this.camera));
			}
			facetStream.filter(GeneralPredicates.isOverHorizon(this.camera, this.horizon).negate()).forEach(f ->{
				f.setFrontFace(GeneralPredicates.isFrontface(this.camera).test(f));
				zBuf.Add(f, obj, shader);
			});
		}
		
		for (CanvasObject child : new ArrayList<CanvasObject>(obj.getChildren()))
		{
			this.processShape(child, zBuf, shader);
		};
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
		
		this.zBuffer.getBuffer().values().stream().forEach(m -> {
			for (ZBufferItem item : m.values()){
				if (item == null || !item.active) continue;
				g.setColor(item.getColour());
				g.drawLine(item.getX(), item.getY(), item.getX(), item.getY());
			}
		});
		
		this.setOkToPaint(false);
		
		for(BiConsumer<Canvas3D,Graphics> op : drawPlugins){
			op.accept(this,g);
		}
	}

}
