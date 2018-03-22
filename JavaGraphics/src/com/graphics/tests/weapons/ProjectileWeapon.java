package com.graphics.tests.weapons;

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
		generateProjectile().ifPresent(proj -> {
    		Point pos = origin.find();
    		Canvas3D.get().registerObject(proj, pos);
    		if (lightProjectile) {
    		    lightProjectile(proj, pos);
    		}
    		
		});
	}

	public Projectile getProjectile() {
		return projectile;
	}
    
    public void setLightProjectile(boolean lightProjectile) {
        this.lightProjectile = lightProjectile;
    }

    @Override
    public ICanvasObject getParent() {
        return parent;
    }
    
    @Override
    public Optional<Class<?>> getEffectClass() {
        return Optional.of(projectile.getClass());
    }

    private Optional<CanvasObject> generateProjectile() {
        double parentSpeed = parent.getTransformsOfType(MovementTransform.class)
                .stream()
                .filter(m -> m.getName().equals(ObjectInputController.FORWARD) )
                .findFirst() //using dropWhile would be better than filter here (in Java 9)
                .map(MovementTransform::getSpeed)
                .orElse(0d);
        
       return Optional.ofNullable(projectile.get(effectVector.get(), parentSpeed));
    }
    
    private void lightProjectile(CanvasObject proj, Point pos) {
        ObjectTiedLightSource<LightSource> l = new ObjectTiedLightSource<>(LightSource.class, pos.x, pos.y, pos.z);
        l.tieTo(proj);
        l.setColour(proj.getColour());
        l.getLightSource().setRange(600);
        Canvas3D.get().addLightSource(l.getLightSource());
    }
}
