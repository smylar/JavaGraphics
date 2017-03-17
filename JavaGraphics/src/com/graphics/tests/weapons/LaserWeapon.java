package com.graphics.tests.weapons;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;

import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.ITexturable;
import com.graphics.lib.interfaces.IVectorFinder;
import com.graphics.lib.orientation.OrientationTransform;
import com.graphics.lib.texture.Texture;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.collectors.NearestIntersectedFacetFinder;
import com.graphics.lib.collectors.IntersectionData;
import com.graphics.tests.TestUtils;

public class LaserWeapon implements IEffector {

	private int duration = 15;
	private int range = 1000;
	private IPointFinder origin;
	private IVectorFinder effectVector;
	private ICanvasObject parent;
	private LaserEffect laserEffect;
	
	public LaserWeapon(IPointFinder origin, IVectorFinder effectVector, ICanvasObject parent){
		this.origin = origin;
		this.effectVector = effectVector;
		this.parent = parent;
	}
	
	@Override
	public void activate() {
		if (Canvas3D.get() == null || effectVector == null) return;
		
		LaserEffect lsr = new LaserEffect(range);
		lsr.setTickLife(this.duration);
		PlugableCanvasObject<LaserEffect> laser = new PlugableCanvasObject<LaserEffect>(lsr);
		laser.addFlag("PHASED");
		Point pos = origin.find();
		
		for (Rotation r : OrientationTransform.getRotationsForVector(effectVector.getVector())){
			lsr.applyTransform(r);
		}
		
		laser.registerPlugin("LASER", 
				(obj) -> {
						Vector v = effectVector.getVector();
						obj.getObjectAs(LaserEffect.class).ifPresent(l -> {
							Entry<Double, IntersectionData<ICanvasObject>> f = TestUtils.getFilteredObjectList().get().stream().collect(new NearestIntersectedFacetFinder<>(v,l.getAnchorPoint(),l.getLength()));
							if (f != null)
							{
								if (!markTexture(f.getValue())) {
									f.getValue().getFacet().setMaxIntensity(f.getValue().getFacet().getMaxIntensity() - 0.15);
								}
								l.setCurLength(f.getKey());
							}else{
								l.resetLength();
							}
						});
						//as an aspiration - create dynamic texture map for laser 'holes'
						return null;
		},true);
		
		Canvas3D.get().registerObject(laser, pos);
		laser.observeAndMatch(parent);
		this.laserEffect = lsr;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	@Override
	public void deActivate() {
		laserEffect.setTickLife(0);
		
	}
	
	private boolean markTexture(IntersectionData<ICanvasObject> intersectionData) {
		Optional<ITexturable> texturable = intersectionData.getParent().getObjectAs(ITexturable.class);
		if (!texturable.isPresent()) return false;
		
		List<WorldCoord> coords = intersectionData.getFacet().getAsList();
		Set<Texture> textures = new HashSet<>(texturable.get().getTextures());
		for (WorldCoord coord : coords) {
			textures.retainAll(texturable.get().getTextures(coord));
		}
		
		if (!textures.isEmpty()) {
			//get top most texture
			Texture active = textures.stream().sorted((a,b) -> b.getOrder() - a.getOrder()).collect(Collectors.toList()).get(0);

			//get texture coord
			Point tp1 = texturable.get().getTextureCoord(active, coords.get(0)).get();
			Point tp2 = texturable.get().getTextureCoord(active, coords.get(1)).get();
			
			double distWorld = coords.get(0).distanceTo(coords.get(1));
			double distTexture = tp1.distanceTo(tp2);
			double ratio = distTexture/distWorld;
			
			double d1 = coords.get(0).distanceTo(intersectionData.getIntersection());
			double d2 = coords.get(1).distanceTo(intersectionData.getIntersection());
			
			Consumer<Point> process = p -> {
				int x = (int)Math.round(p.x);
				int y = (int)Math.round(p.y);
				Color current = active.getColour(x, y).orElse(intersectionData.getParent().getColour());
				
				if (current != null) {
					current = new Color((int)(current.getRed() * 0.8), (int)(current.getBlue() * 0.8), (int)(current.getGreen() * 0.8));
				} else {
					current = Color.BLACK;
				}
				active.setColour(x, y, current);
			};
			
			
			Utils.getPointFromKnownPoints(tp1, tp2, d1 * ratio, d2 * ratio).ifPresent(points -> {	
				Utils.getPointFromKnownPoints(coords.get(0), coords.get(1), d1, d2).ifPresent(p -> {	
					if (intersectionData.getFacet().isPointWithin(p.getFirst())) {
						process.accept(points.getFirst());
					} else {
						process.accept(points.getSecond());
					}
				});
			});
			
			return true;
		}
		
		return false;
	}

}