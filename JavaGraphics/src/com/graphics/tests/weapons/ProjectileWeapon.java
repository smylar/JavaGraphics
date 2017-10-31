package com.graphics.tests.weapons;

import java.util.Objects;
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
	private int ticksBetweenShots = 20;
	private long lastShotTick = -1000;
	private final Projectile projectile;
	private final IPointFinder origin;
	private final IVectorFinder effectVector;
	private final ICanvasObject parent;
	
	public ProjectileWeapon(Projectile proj, IPointFinder origin, IVectorFinder effectVector, ICanvasObject parent) {
		this.origin = origin;
		this.effectVector = effectVector;
		this.parent = parent;
		this.projectile = proj;
		proj.setStartPoint(origin);
	}
	
	@Override
	public void activate() {
		if (canFire()) {
    		ammoCount--;
    		lastShotTick = Canvas3D.get().getTicks();
    		
    		CanvasObject proj = generateProjectile();
    		
    		Point pos = origin.find();
    		Canvas3D.get().registerObject(proj, pos);
    		ObjectTiedLightSource<LightSource> l = new ObjectTiedLightSource<>(LightSource.class, pos.x, pos.y, pos.z);
    		l.tieTo(proj);
    		l.setColour(proj.getColour());
    		l.getLightSource().setRange(400);
    		Canvas3D.get().addLightSource(l.getLightSource());
		}
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

    public void setTicksBetweenShots(int millisecondsBetweenShots) {
        this.ticksBetweenShots = millisecondsBetweenShots;
    }
    
    protected ICanvasObject getParent() {
        return parent;
    }

    private CanvasObject generateProjectile() {
        double parentSpeed = 0;
        Optional<MovementTransform> move = parent.getTransformsOfType(MovementTransform.class)
                .stream()
                .filter(m -> m.getName().equals(ObjectInputController.FORWARD) )
                .findFirst();
        
        if (move.isPresent()) {
            parentSpeed = move.get().getSpeed();
        }
        
        return projectile.get(effectVector.getVector(), parentSpeed);
    }
    
    
    private boolean canFire() {
        return ammoCount > 0 
                && Objects.nonNull(Canvas3D.get()) 
                && Canvas3D.get().getTicks() - lastShotTick > ticksBetweenShots;
    }
}
