package com.graphics.tests.weapons;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.control.ObjectInputController;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.transform.MovementTransform;

/**
 * A weapon that produces a {@link Projectile} object when activated
 * 
 * @author paul.brandon
 *
 */
public class ProjectileWeapon implements IEffector {
	
	private int ammoCount = 10;
	private int ticksBetweenShots = 20;
	private long lastShotTick = -1000;
	private boolean lightProjectile = true;
	private final Projectile projectile;
	private final IPointFinder origin;
	private final Supplier<IOrientation> effectVector;
	private final ICanvasObject parent;
	
	public ProjectileWeapon(Projectile proj, IPointFinder origin, Supplier<IOrientation> effectVector, ICanvasObject parent) {
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
    		
    		generateProjectile().ifPresent(proj -> {
	    		Point pos = origin.find();
	    		Canvas3D.get().registerObject(proj, pos);
	    		if (lightProjectile) {
	    		    lightProjectile(proj, pos);
	    		}
	    		
    		});
		}
	}

	public int getAmmoCount() {
		return ammoCount;
	}

	public ProjectileWeapon setAmmoCount(int ammoCount) {
		this.ammoCount = ammoCount;
		return this;
	}

	public Projectile getProjectile() {
		return projectile;
	}

    public ProjectileWeapon setTicksBetweenShots(int ticksBetweenShots) {
        this.ticksBetweenShots = ticksBetweenShots;
        return this;
    }
    
    public void setLightProjectile(boolean lightProjectile) {
        this.lightProjectile = lightProjectile;
    }

    protected ICanvasObject getParent() {
        return parent;
    }

    private Optional<CanvasObject> generateProjectile() {
        double parentSpeed = 0;
        Optional<MovementTransform> move = parent.getTransformsOfType(MovementTransform.class)
                .stream()
                .filter(m -> m.getName().equals(ObjectInputController.FORWARD) )
                .findFirst(); //using dropWhile would be better than filter here (in Java 9)
        
        if (move.isPresent()) {
            parentSpeed = move.get().getSpeed();
        }
        
        return Optional.ofNullable(projectile.get(effectVector.get(), parentSpeed));
    }
    
    private void lightProjectile(CanvasObject proj, Point pos) {
        ObjectTiedLightSource<LightSource> l = new ObjectTiedLightSource<>(LightSource.class, pos.x, pos.y, pos.z);
        l.tieTo(proj);
        l.setColour(proj.getColour());
        l.getLightSource().setRange(600);
        Canvas3D.get().addLightSource(l.getLightSource());
    }
    
    private boolean canFire() {
        return ammoCount > 0 
                && Objects.nonNull(Canvas3D.get()) 
                && Canvas3D.get().getTicks() - lastShotTick > ticksBetweenShots;
    }
}
