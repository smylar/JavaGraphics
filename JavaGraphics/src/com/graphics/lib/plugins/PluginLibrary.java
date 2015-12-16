package com.graphics.lib.plugins;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;

public class PluginLibrary {
	
	public static IPlugin<PlugableCanvasObject<?>, Void> generateTrailParticles(Color colour, int density, double exhaustVelocity, double particleSize)
	{
		return (obj) -> {
			Optional<MovementTransform> movement = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
			if (!movement.isPresent()) return null; //no point if it isn't moving
			Vector baseVector = movement.get().getVector();
			
			for (int i = 0 ; i < density ; i++)
			{
				int index = (int)Math.round(Math.random() * (obj.getVertexList().size() - 1)); //TODO only get untagged points (tagged points usually hidden and have special purpose)
				Point p = obj.getVertexList().get(index);
				CanvasObject fragment = new CanvasObject();
				fragment.getVertexList().add(new WorldCoord(p.x , p.y, p.z));
				fragment.getVertexList().add(new WorldCoord(p.x + particleSize , p.y, p.z));
				fragment.getVertexList().add(new WorldCoord(p.x, p.y + particleSize, p.z));
				fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
				fragment.setColour(colour);
				fragment.setProcessBackfaces(true);
				double xVector = baseVector.x + (Math.random()/2) - 0.25;
				double yVector = baseVector.y + (Math.random()/2) - 0.25;
				double zVector = baseVector.z + (Math.random()/2) - 0.25;
				Transform rot1 = new RepeatingTransform<Rotation<?>>(new Rotation<YRotation>(YRotation.class, Math.random() * 5), 45);
				MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), movement.get().getVelocity() - exhaustVelocity);
				move.moveUntil(t -> rot1.isCompleteSpecific());
				Transform rot2 = new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, Math.random() * 5), t -> rot1.isCompleteSpecific());				
				fragment.addTransformAboutCentre(rot1, rot2);
				fragment.addTransform(move);
				
				fragment.deleteAfterTransforms();	
				obj.getChildren().add(fragment);
			}
			return null;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>, Set<CanvasObject>> explode(Collection<LightSource> lightSources) 
	{
		return (obj) -> {
		Set<CanvasObject> children = new HashSet<CanvasObject>();
		for (Facet f : obj.getFacetList())
		{
			CanvasObject fragment = new CanvasObject();
			fragment.getVertexList().add(new WorldCoord(f.point1.x, f.point1.y, f.point1.z));
			fragment.getVertexList().add(new WorldCoord(f.point2.x, f.point2.y, f.point2.z));
			fragment.getVertexList().add(new WorldCoord(f.point3.x, f.point3.y, f.point3.z));
			fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
			fragment.setProcessBackfaces(true);
			fragment.setColour(f.getColour() != null ? f.getColour() : obj.getColour());
			Vector baseVector = fragment.getFacetList().get(0).getNormal();
			double xVector = baseVector.x + (Math.random()/2) - 0.25;
			double yVector = baseVector.y + (Math.random()/2) - 0.25;
			double zVector = baseVector.z + (Math.random()/2) - 0.25;
			MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), Math.random() * 15 + 1 );
			fragment.addTransform(move);
			fragment.addTransformAboutCentre(new RepeatingTransform<Rotation<?>>(new Rotation<YRotation>(YRotation.class, Math.random() * 10), t -> move.isComplete()), new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, Math.random() * 10), t -> move.isComplete()));
			if (!obj.hasFlag(Events.EXPLODE_PERSIST)){
				move.moveUntil(t -> t.getDistanceMoved() > 100);
				fragment.deleteAfterTransforms();
			}
			children.add(fragment);
		}
		
		obj.setVisible(false);
		obj.cancelTransforms();
		obj.removePlugins();
		
		return children;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>,Void> flash(Collection<LightSource> lightSources)
	{
		return (obj)-> {
			LightSource flash = new LightSource(obj.getCentre().x,  obj.getCentre().y, obj.getCentre().z);
			flash.setRange(-1);
			lightSources.add(flash);
			
			new Thread(() -> {
				try {
					for (int i = 0 ; i < 15 ; i++)
					{
						Thread.sleep(200);
						flash.setIntensity(flash.getIntensity() - 0.075);
					}
				} catch (Exception e) {}
				flash.setDeleted(true);
			}).start();
			return null;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>,PlugableCanvasObject<?>> hasCollided(IPlugin<?,Set<PlugableCanvasObject<?>>> objects, String impactorPlugin, String impacteePlugin)
	{
		return (obj) -> {
			PlugableCanvasObject<?> inCollision = (PlugableCanvasObject<?>)obj.executePlugin("IN_COLLISION"); //may need to be a list - may hit more than one!
			
			for (PlugableCanvasObject<?> impactee : objects.execute(null)){
				if (impactee == obj) continue;
				//TODO will need to factor in possibility object was drawn completely on the other side of an object and thus not detected as a collision
				if (obj.getVertexList().stream().anyMatch(p -> impactee.isPointInside(p)))
				{
					if (inCollision == impactee) { return null;}
					if (impactorPlugin != null) obj.executePlugin(impactorPlugin);
					if (impacteePlugin != null) impactee.executePlugin(impacteePlugin);

					obj.registerPlugin("IN_COLLISION", (o) -> {return impactee;}, false);
					return impactee;
				}else if (inCollision == impactee){
					obj.removePlugin("IN_COLLISION");
				}
			}
			return null;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>, Void> stop()
	{
		return (obj) -> {
			obj.cancelTransforms();
			obj.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>, Void> stop2()
	{
		return (obj) -> {
			for (MovementTransform t : obj.getTransformsOfType(MovementTransform.class)){
				t.setVelocity(0);
			}
			obj.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<PlugableCanvasObject<?>, Void> bounce(PlugableCanvasObject<?> impactee)
	{
		return (obj) -> {
			Optional<WorldCoord> impactPoint = obj.getVertexList().stream().filter(p -> impactee.isPointInside(p)).findAny();
			if (!impactPoint.isPresent()) return null;
			
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.size() == 0) return null;
			
			Vector move = mTrans.get(0).getVector(); //just getting the first one for now (if there is more than one) 

			double velocity = mTrans.get(0).getVelocity(); 
			Point prevPoint = new Point(impactPoint.get().x - (move.x * velocity), impactPoint.get().y - (move.y * velocity), impactPoint.get().z - (move.z * velocity));
			Facet curFacet = null;
			double curDist = -1;
			for(Facet f : impactee.getFacetList().stream().filter(f -> f.getDistanceFromFacetPlane(impactPoint.get()) < velocity + 1).collect(Collectors.toList()))
			{
				Point intersect = f.getIntersectionPointWithFacetPlane(prevPoint, move);

				//if intersected with plane of facet check we are within the bounds of the facet
				if (intersect != null && (prevPoint.distanceTo(intersect) < curDist || curDist == -1 ) && f.isPointWithin(intersect)){
					curFacet = f;
					curDist = prevPoint.distanceTo(intersect);
				}
			}
			
			if (curFacet == null) return null;
			Vector impactedNormal = curFacet.getNormal();
			
			//I know r=d-2(d.n)n is reflection vector in 2 dimension (hopefully it'll work on 3)

			double multiplier = move.dotProduct(impactedNormal) * -2;
			move.x += (impactedNormal.x * multiplier);
			move.y += (impactedNormal.y * multiplier);
			move.z += (impactedNormal.z * multiplier);
			
			return null;
		};
	}

}
