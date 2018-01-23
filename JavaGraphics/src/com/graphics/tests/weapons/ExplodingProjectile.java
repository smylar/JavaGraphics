package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Axis;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.shapes.Ovoid;
import com.graphics.tests.TestUtils;

public class ExplodingProjectile extends Projectile {

	@Override
	public CanvasObject get(IOrientation orientation, double parentSpeed) {
	    Ovoid proj = new Ovoid(20,0.3,30);
		proj.applyTransform(new Rotation(Axis.X, -90));
		TraitHandler.INSTANCE.registerTrait(proj, new OrientableTrait()).setOrientation(new SimpleOrientation());
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 0, 0, 80));
		proj.setCastsShadow(false);
		TraitHandler.INSTANCE.registerTrait(proj, new PlugableTrait())
		                     .registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(getClipLibrary()), false)
		                     .registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollidedNew(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE), true);
		proj.addFlag(TestUtils.SILENT_EXPLODE);

		final Vector forward = orientation.getForward();
		for (int i = 0; i < proj.getFacetList().size() ; i++)
		{
			if (i % (proj.getPointsPerCircle()/3) == 1 || i % (proj.getPointsPerCircle()/3) == 0)
			{
				proj.getFacetList().get(i).setColour(new Color(225,0,0));
			}
		}

		for (Rotation r : OrientationData.getRotationsForVector(forward)){
			proj.applyTransform(r);
		}

		MovementTransform move = new MovementTransform(forward, this.getSpeed() + parentSpeed) {
			@Override
			public void onComplete(){
			    TraitHandler.INSTANCE.getTrait(proj, IPlugable.class).ifPresent(p -> p.executePlugin(Events.EXPLODE));
			}
		};
		
		//TODO range should be affected by the speed of the projectile
		move.moveUntil(t -> t.getDistanceMoved() >= this.getRange());
		proj.addTransform(move);
		
		Transform projt = new RepeatingTransform<Rotation>(new Rotation(Axis.Z, 20), t -> move.isCompleteSpecific());

		TraitHandler.INSTANCE.getTrait(proj, IOrientable.class).ifPresent(o -> proj.addTransform(o.toBaseOrientationTransform()));
		
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(proj, projt);
		
		TraitHandler.INSTANCE.getTrait(proj, IOrientable.class).ifPresent(o -> proj.addTransform(o.reapplyOrientationTransform()));
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);
		
		return proj;
	}

}
