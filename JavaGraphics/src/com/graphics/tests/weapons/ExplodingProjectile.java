package com.graphics.tests.weapons;

import static com.graphics.lib.traits.TraitManager.TRAITS;

import java.awt.Color;

import com.graphics.lib.Axis;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.shapes.Ovoid;
import com.graphics.tests.TestUtils;

public class ExplodingProjectile extends Projectile{

	@Override
	public CanvasObject get(Vector initialVector, double parentSpeed) {
	    Ovoid proj = new Ovoid(20,0.3,30);
		proj.applyTransform(new Rotation(Axis.X, -90));
		TRAITS.addTrait(proj, new OrientableTrait()).setOrientation(new SimpleOrientation());
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 0, 0, 80));
		proj.setCastsShadow(false);
		TRAITS.addTrait(proj, new PlugableTrait()).registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(getClipLibrary()), false)
		                                  .registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollidedNew(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE), true);
		proj.addFlag(TestUtils.SILENT_EXPLODE);

		for (int i = 0; i < proj.getFacetList().size() ; i++)
		{
			if (i % (proj.getPointsPerCircle()/3) == 1 || i % (proj.getPointsPerCircle()/3) == 0)
			{
				proj.getFacetList().get(i).setColour(new Color(225,0,0));
			}
		}

		for (Rotation r : OrientationData.getRotationsForVector(initialVector)){
			proj.applyTransform(r);
		}

		MovementTransform move = new MovementTransform(initialVector, this.getSpeed() + parentSpeed){
			@Override
			public void onComplete(){
				TRAITS.getTrait(proj, PlugableTrait.class).ifPresent(p -> p.executePlugin(Events.EXPLODE));
			}
		};
		
		//TODO range should be affected by the speed of the projectile
		move.moveUntil(t -> t.getDistanceMoved() >= this.getRange());
		proj.addTransform(move);
		
		Rotation rot = new Rotation(Axis.Z, 20)
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				TRAITS.getTrait(proj, IOrientable.class).ifPresent(o -> o.toBaseOrientation());
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				TRAITS.getTrait(proj, IOrientable.class).ifPresent(o -> o.reapplyOrientation());
			}
		}
		;
		
		Transform projt = new RepeatingTransform<Rotation>(rot, t -> move.isCompleteSpecific());

		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(proj, projt);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);
		
		return proj;
	}

}
