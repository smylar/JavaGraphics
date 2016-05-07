package com.graphics.tests.weapons;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.IVectorFinder;
import com.graphics.lib.orientation.OrientationTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.tests.TestUtils;

public class LaserWeapon implements IEffector {

	private int duration = 15;
	private int range = 1000;
	private IPointFinder origin;
	private IVectorFinder effectVector;
	private CanvasObject parent;
	private LaserEffect laserEffect;
	
	public LaserWeapon(IPointFinder origin, IVectorFinder effectVector, CanvasObject parent){
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
		
		Vector v = effectVector.getVector();
		for (Rotation<?> r : OrientationTransform.getRotationsForVector(effectVector.getVector())){
			lsr.applyTransform(r);
		}

		Canvas3D.get().registerObject(laser, pos);
		laser.observeAndMatch(parent);
		
		laser.registerPlugin("LASER", 
				(obj) -> {
						LaserEffect l = obj.getObjectAs(LaserEffect.class);
						if (l != null){
							for (CanvasObject screenObjects : TestUtils.getFilteredObjectList().get()){
								for (Facet f : screenObjects.getIntersectedFacets(l.getAnchorPoint(), v))
								{
									if (f != null && f.getAsList().stream().mapToDouble(p -> p.distanceTo(l.getAnchorPoint())).average().getAsDouble() < l.getLength() ){
										f.setMaxIntensity(f.getMaxIntensity() - 0.15);
										//as an aspiration - create dynamic texture map for laser 'holes'
									}
								}
							}
						}
						return null;
		},true);
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

}
