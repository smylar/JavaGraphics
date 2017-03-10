package com.graphics.tests.weapons;

import java.util.Optional;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.control.ObjectInputController;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.IVectorFinder;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.transform.MovementTransform;

public class ProjectileWeapon implements IEffector {
	
	private int ammoCount = 10;
	private Projectile projectile;
	private IPointFinder origin;
	private IVectorFinder effectVector;
	private ICanvasObject parent;
	
	public ProjectileWeapon(Projectile proj, IPointFinder origin, IVectorFinder effectVector, ICanvasObject parent){
		this.origin = origin;
		this.effectVector = effectVector;
		this.parent = parent;
		this.projectile = proj;
		proj.setStartPoint(origin);
	}
	
	@Override
	public void activate() {
		if (ammoCount == 0 || Canvas3D.get() == null) return;
		ammoCount--;
		double parentSpeed = 0;
		Optional<MovementTransform> move = parent.getTransformsOfType(MovementTransform.class)
				.stream()
				.filter(m -> m.getName().equals(ObjectInputController.FORWARD) )
				.findFirst();
		
		if (move.isPresent()) parentSpeed = move.get().getSpeed();
		
		CanvasObject proj = projectile.get(effectVector.getVector(), parentSpeed);
		
		if (proj == null) return;
		
		Point pos = origin.find();
		Canvas3D.get().registerObject(proj, pos);
		ObjectTiedLightSource<LightSource> l = new ObjectTiedLightSource<LightSource>(LightSource.class, pos.x, pos.y, pos.z);
		l.tieTo(proj);
		l.setColour(proj.getColour());
		l.getLightSource().setRange(400);
		Canvas3D.get().addLightSource(l.getLightSource());
	}

	public int getAmmoCount() {
		return ammoCount;
	}

	public void setAmmoCount(int ammoCount) {
		this.ammoCount = ammoCount;
	}

	public Projectile getProjectile() {
		return projectile;
	}

	public void setProjectile(Projectile projectile) {
		this.projectile = projectile;
	}

	@Override
	public void deActivate() {
		// TODO Auto-generated method stub
		
	}
}
