package com.graphics.tests.weapons;

import java.awt.Color;
import java.util.Date;

import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class BouncyProjectile extends Projectile {
	
	@Override
	public CanvasObject get(Vector initialVector, double parentSpeed) {
		PlugableCanvasObject<Sphere> proj = new PlugableCanvasObject<Sphere>(new Sphere(18,20));
		proj.setBaseIntensity(1);
		proj.setColour(new Color(0, 255, 255, 80));
		proj.setCastsShadow(false);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);

		MovementTransform move = new MovementTransform(initialVector, this.getSpeed() + parentSpeed);
		long delTime = new Date().getTime() + 5000;
		move.moveUntil(t -> t.getDistanceMoved() > this.getRange() || (t.getSpeed() == 0 && new Date().getTime() > delTime));
		proj.addTransform(move);

		proj.registerPlugin(Events.CHECK_COLLISION, getBouncePlugin(), true);
		proj.registerPlugin("Trail", PluginLibrary.generateTrailParticles(Color.LIGHT_GRAY, 20, 13, 0.66), true);
		
		return proj;
	}
	
	private IPlugin<IPlugable,Void> getBouncePlugin(){
		return new IPlugin<IPlugable,Void>(){
			@Override
			public Void execute(IPlugable obj) {	
				ICanvasObject impactee = PluginLibrary.hasCollidedNew(TestUtils.getFilteredObjectList(),null, null).execute(obj);
				if (impactee != null){
					if (impactee.hasFlag(Events.STICKY)){ 
						PluginLibrary.stop2().execute(obj);
						obj.observeAndMatch(impactee);
						if (getClipLibrary() != null) getClipLibrary().playSound("STICK", -20f);
					}
					else {
						PluginLibrary.bounce(impactee).execute(obj);
						if (getClipLibrary() != null)getClipLibrary().playSound("BOUNCE", -20f);
					}
				}
				return null;
			}			
		};
	}

}