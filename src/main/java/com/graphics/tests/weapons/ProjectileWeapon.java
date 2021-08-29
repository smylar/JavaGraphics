package com.graphics.tests.weapons;

import java.util.Optional;
import java.util.function.Supplier;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.control.ObjectInputController;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.transform.MovementTransform;

/**
 * A weapon that produces a {@link Projectile} object when activated
 * 
 * @author paul.brandon
 *
 */
@WithAmmo
public class ProjectileWeapon implements IEffector {
	
	private boolean lightProjectile = true;
	private final Projectile projectile;
	private final Supplier<IOrientation> effectVector;
	private final IWeaponised parent;
	private final String id;
	
	public ProjectileWeapon(String id, Projectile proj, Supplier<IOrientation> effectVector, IWeaponised parent) {
		this.effectVector = effectVector;
		this.parent = parent;
		this.projectile = proj;
		this.id = id;
	}
	
	@Override
	public void activate() {
	    parent.getWeaponLocation(id).ifPresent(origin -> {
	    
    		generateProjectile(origin).ifPresent(proj -> {
        		Canvas3D.get().registerObject(proj, origin);
        		if (lightProjectile) {
        		    lightProjectile(proj, origin);
        		}
        		
    		});
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
    
    @Override
    public String getId() {
        return this.id;
    }

    private Optional<CanvasObject> generateProjectile(WorldCoord origin) {
        double parentSpeed = parent.getTransformsOfType(MovementTransform.class)
                .stream()
                .dropWhile(m -> !m.getName().equals(ObjectInputController.FORWARD) )
                .findFirst()
                .map(MovementTransform::getSpeed)
                .orElse(0d);
        
       return Optional.ofNullable(projectile.get(effectVector.get(), origin, parentSpeed));
    }
    
    private void lightProjectile(CanvasObject proj, Point pos) {
        ObjectTiedLightSource<LightSource> l = new ObjectTiedLightSource<>(LightSource.class, pos.x, pos.y, pos.z);
        l.tieTo(proj);
        l.setColour(proj.getColour());
        l.getLightSource().setRange(600);
        Canvas3D.get().addLightSource(l.getLightSource());
    }
}
