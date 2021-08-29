package com.graphics.tests.weapons;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.interfaces.ITexturable;
import com.graphics.lib.interfaces.ITracker;
import com.graphics.lib.interfaces.IVectorFinder;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.texture.Texture;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TrackingTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.collectors.IntersectedFacetFinder;
import com.graphics.lib.collectors.IntersectionData;
import com.graphics.tests.TestUtils;

/**
 * Defines parameters for a laser weapon, i.e. How/when to generate a {@link LaserEffect}
 * 
 * @author paul.brandon
 *
 */
public class LaserWeapon implements IEffector {

	private int duration = 15;
	private int range = 1000;
	private final IVectorFinder effectVector;
	private final IWeaponised parent;
	private final String id;
	private LaserEffect laserEffect;
	
	public LaserWeapon(String id, IVectorFinder effectVector, IWeaponised parent){
		this.effectVector = effectVector;
		this.parent = parent;
		this.id = id;
	}
	
	@Override
	public void activate() {
		if (Canvas3D.get() == null || effectVector == null
		    || laserEffect != null && !laserEffect.isDeleted()) { 
		    return;
		}
		
		LaserEffect lsr = new LaserEffect(range);
		lsr.setTickLife(this.duration);
		lsr.addFlag("PHASED");

		TraitHandler.INSTANCE.registerTrait(lsr, TrackingTrait.class);
		
		TraitHandler.INSTANCE.registerTrait(lsr, PlugableTrait.class).registerPlugin("LASER", 
				obj -> {
						if (lsr.getTickLife() <= 0) {
							lsr.setDeleted(true);
							return null;
						}
					
						lsr.setTickLife(lsr.getTickLife() - 1);
						obj.getParent().getObjectAs(LaserEffect.class).ifPresent(l -> {
							TreeSet<IntersectionData<ICanvasObject>> f = TestUtils.getFilteredObjectList().get().parallelStream()
							                                                      .collect(new IntersectedFacetFinder<>(effectVector.getVector(),l.getAnchorPoint(),l.getLength()));		
							l.resetLength();								
							if (!f.isEmpty()) {
							    if (!markTexture(f.first())) {
                                    f.first().getFacet().setMaxIntensity(f.first().getFacet().getMaxIntensity() - 0.15);
                                }
                                l.setCurLength(f.first().getDistanceAway());
							}
						});
						return null;
		},true);
		
		lsr.addFlag(Events.NO_SHADE);
		
		TraitHandler.INSTANCE.getTrait(parent, IPlugable.class).ifPresent(p -> 
			p.registerSingleAfterDrawPlugin("ADD_LASER", obj -> {
				for (Rotation r : OrientationData.getRotationsForVector(effectVector.getVector())) {
					lsr.applyTransform(r);
				}
				CanvasObjectFunctions.DEFAULT.get().moveTo(lsr, parent.getWeaponLocation(id).get());
				TraitHandler.INSTANCE.getTrait(lsr, ITracker.class).ifPresent(trait -> trait.observeAndMatch(parent, Set.of()));
				return null;
			})
		);
		
		
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
	
	@Override
    public ICanvasObject getParent() {
        return this.parent;
    }
	
	@Override
    public String getId() {
        return this.id;
    }
	
	private boolean markTexture(IntersectionData<ICanvasObject> intersectionData) {
		Optional<ITexturable> texturable = TraitHandler.INSTANCE.getTrait(intersectionData.getParent(), ITexturable.class);
		if (!texturable.isPresent()) 
		    return false;
		
		List<WorldCoord> coords = intersectionData.getFacet().getAsList();
		Set<Texture> textures = new HashSet<>(texturable.get().getTextures());
		for (WorldCoord coord : coords) {
			textures.retainAll(texturable.get().getTextures(coord));
		}
		
		if (!textures.isEmpty()) {
			//get top most texture
			Texture active = textures.stream().sorted((a,b) -> b.getOrder() - a.getOrder()).toList().get(0);

			//get texture coord
			Point tp1 = texturable.get().getTextureCoord(active, coords.get(0)).get();
			Point tp2 = texturable.get().getTextureCoord(active, coords.get(1)).get();
			
			double distWorld = coords.get(0).distanceTo(coords.get(1));
			double distTexture = tp1.distanceTo(tp2);
			double ratio = distTexture/distWorld;
			
			double d1 = coords.get(0).distanceTo(intersectionData.getIntersection());
			double d2 = coords.get(1).distanceTo(intersectionData.getIntersection());
			
			
			Utils.getPointFromKnownPoints(tp1, tp2, d1 * ratio, d2 * ratio).ifPresent(points -> 
				Utils.getPointFromKnownPoints(coords.get(0), coords.get(1), d1, d2)
				    .map(p -> intersectionData.getFacet().isPointWithin(p.getLeft()) ? points.getLeft() : points.getRight() )
				    .ifPresent(processTextureMark(active, intersectionData)::accept)
			);
			
			return true;
		}
		
		return false;
	}
	
	private Consumer<Point> processTextureMark(Texture active, IntersectionData<ICanvasObject> intersectionData) {
	    return p -> {
            int x = (int)Math.round(p.x);
            int y = (int)Math.round(p.y);
            
            Color current = Optional.ofNullable(active.getColour(x, y).orElse(intersectionData.getParent().getColour()))
                                    .map(c -> new Color((int)(c.getRed() * 0.8), (int)(c.getBlue() * 0.8), (int)(c.getGreen() * 0.8)))
                                    .orElse(Color.BLACK);
            
            active.setColour(x, y, current);
        };
	}

}
