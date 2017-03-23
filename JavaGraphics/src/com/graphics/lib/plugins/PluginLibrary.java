package com.graphics.lib.plugins;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;

public class PluginLibrary {
	
	private static final String IN_COLLISION = "IN_COLLISION";
	
	public static IPlugin<IPlugable, Void> generateTrailParticles(Color colour, int density, double exhaustVelocity, double particleSize)
	{
		return obj -> {
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.isEmpty()) {
				return null;
			}
			
			Vector baseVector = new Vector(0,0,0);
			for (MovementTransform t : mTrans){
				baseVector.x += t.getVector().x * t.getSpeed();
				baseVector.y += t.getVector().y * t.getSpeed();
				baseVector.z += t.getVector().z * t.getSpeed();
			}
			double speed = baseVector.getSpeed();
			baseVector = baseVector.getUnitVector();
			
			List<WorldCoord> vertices = obj.getVertexList().stream().filter(GeneralPredicates.untagged(obj)).collect(Collectors.toList());//only get untagged points (tagged points usually hidden and have special purpose)
			for (int i = 0 ; i < density ; i++)
			{
				int index = new Random().nextInt(vertices.size() - 1);
				Point p = vertices.get(index);
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
				Transform rot1 = new RepeatingTransform<Rotation>(Axis.Y.getRotation(Math.random() * 5), 45);
				MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), speed - exhaustVelocity);
				move.moveUntil(t -> rot1.isCompleteSpecific());
				Transform rot2 = new RepeatingTransform<Rotation>(Axis.X.getRotation(Math.random() * 5), t -> rot1.isCompleteSpecific());				
				CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, rot1, rot2);
				fragment.addTransform(move);
				
				fragment.deleteAfterTransforms();	
				obj.getChildren().add(fragment);
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Set<PlugableCanvasObject>> explode() 
	{
		return obj -> {
		Set<PlugableCanvasObject> children = new HashSet<>();
		for (Facet f : obj.getFacetList())
		{
			PlugableCanvasObject fragment = new PlugableCanvasObject(new CanvasObject());
			for (WorldCoord p : f.getAsList()){
				fragment.getVertexList().add(new WorldCoord(p.x, p.y, p.z));
			}
			fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
			fragment.setProcessBackfaces(true);
			fragment.setColour(f.getColour() != null ? f.getColour() : obj.getColour());
			Vector baseVector = fragment.getFacetList().get(0).getNormal();
			double xVector = baseVector.x + (Math.random()/2) - 0.25;
			double yVector = baseVector.y + (Math.random()/2) - 0.25;
			double zVector = baseVector.z + (Math.random()/2) - 0.25;
			MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), Math.random() * 15 + 1 );
			fragment.addTransform(move);
			CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, 
																		new RepeatingTransform<Rotation>(Axis.Y.getRotation(Math.random() * 10), 
																		t -> move.isComplete()), new RepeatingTransform<Rotation>(Axis.X.getRotation(Math.random() * 10), 
																		t -> move.isComplete()));
			if (!obj.hasFlag(Events.EXPLODE_PERSIST)){
				move.moveUntil(t -> t.getDistanceMoved() > 100);
				fragment.deleteAfterTransforms();
			}
			children.add(fragment);
			obj.getChildren().add(fragment);
		}
		
		obj.setVisible(false);
		obj.cancelTransforms();
		obj.removePlugins();
		
		return children;
		};
	}
	
	public static IPlugin<IPlugable,Void> flash(Collection<ILightSource> lightSources)
	{
		return obj -> {
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
	
	public static IPlugin<IPlugable,ICanvasObject> hasCollided(ICanvasObjectList objects, String impactorPlugin, String impacteePlugin)
	{
		return obj -> {
			ICanvasObject inCollision = (ICanvasObject)obj.executePlugin(IN_COLLISION); //may need to be a list - may hit more than one!
			
			for (ICanvasObject impactee : objects.get()){
				if (impactee == obj) continue;
			
				if (obj.getVertexList().stream().anyMatch(p -> impactee.getFunctions().isPointInside(impactee, p)))
				{
					if (inCollision == impactee) { return null;}
					if (impactorPlugin != null) obj.executePlugin(impactorPlugin);
					if (impacteePlugin != null){
						impactee.getObjectAs(IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
					}

					obj.registerPlugin(IN_COLLISION, o -> impactee, false);
					return impactee;
				}else if (inCollision == impactee){
					obj.removePlugin(IN_COLLISION);
				}
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable,ICanvasObject> hasCollidedNew(ICanvasObjectList objects, String impactorPlugin, String impacteePlugin)
	{
		return obj -> {
			ICanvasObject inCollision = (ICanvasObject)obj.executePlugin(IN_COLLISION); //may need to be a list - may hit more than one!
			
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.isEmpty()) {
				return null;
			}
			
			Vector baseVector = new Vector(0,0,0);
			for (MovementTransform t : mTrans){
				baseVector.x += t.getVector().x * t.getSpeed();
				baseVector.y += t.getVector().y * t.getSpeed();
				baseVector.z += t.getVector().z * t.getSpeed();
			}
			
			for (ICanvasObject impactee : objects.get()){
				if (impactee == obj) continue;
				//Tries to factor in possibility object was drawn completely on the other side of an object (after moving) and thus not detected as a collision by point inside check
				for(Point p : obj.getVertexList()){
					Point prevPoint = new Point(p);
					prevPoint.x -= baseVector.x;
					prevPoint.y -= baseVector.y;
					prevPoint.z -= baseVector.z;
				
					for(Facet f : impactee.getFacetList())
					{
						Point in = f.getIntersectionPointWithFacetPlane(prevPoint, prevPoint.vectorToPoint(p).getUnitVector(), false);
						if (f.isPointWithin(in) && prevPoint.distanceTo(in) <= prevPoint.distanceTo(p)){
							if (inCollision == impactee) { return null;}
							if (impactorPlugin != null) obj.executePlugin(impactorPlugin);
							if (impacteePlugin != null){
								impactee.getObjectAs(IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
							}

							obj.registerPlugin(IN_COLLISION, o -> impactee, false);
							return impactee;
						}
					}
				}
				if (inCollision == impactee){
					obj.removePlugin(IN_COLLISION);
				} //is slow if lots of objects using this method, might split so things like explosion fragments use the simpler point is inside method
				
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> stop()
	{
		return obj -> {
			obj.cancelTransforms();
			obj.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> stop2()
	{
		return obj -> {
			for (MovementTransform t : obj.getTransformsOfType(MovementTransform.class)){
				t.setSpeed(0);
			}
			obj.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> bounce(ICanvasObject impactee)
	{
		return obj -> {
			//find impacted points and use the one that was nearest the impactee before hitting it, then find the facet it hit and reflect
			//the movement vector using the normal of that facet.
			//It's not entirely perfect, because we don't have infinite normals, you can see this if you hit a sphere dead on with another sphere,
			//it won't come straight back as it would in reality, as we don't necessarily have a normal that is pointing straight back to use.
			//However, we could add another plugin for spheres, the normal is then the vector from centre to impact point
			
			Set<WorldCoord> impactPoints = obj.getVertexList().stream().filter(p -> impactee.getFunctions().isPointInside(impactee, p)).collect(Collectors.toSet());
			if (impactPoints.size() == 0) return null;
			
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.size() == 0) return null;
			
			Vector move = mTrans.get(0).getVector(); //just getting the first one for now (if there is more than one) 

			double speed = mTrans.get(0).getSpeed(); 
			
			Facet curFacet = null;
			double curDist = -1;
			
			for(WorldCoord impactPoint : impactPoints){
				Point prevPoint = new Point(impactPoint.x - (move.x * speed), impactPoint.y - (move.y * speed), impactPoint.z - (move.z * speed));

				for(Facet f : impactee.getFacetList().stream().filter(f -> f.getDistanceFromFacetPlane(impactPoint) < speed + 1).collect(Collectors.toList()))
				{
					Point intersect = f.getIntersectionPointWithFacetPlane(prevPoint, move);
	
					//if intersected with plane of facet check we are within the bounds of the facet
					if (intersect != null && (prevPoint.distanceTo(intersect) < curDist || curDist == -1 ) && f.isPointWithin(intersect)){
						curFacet = f;
						curDist = prevPoint.distanceTo(intersect);
					}
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
	
	
	public static IPlugin<IPlugable, Void> track(ICanvasObject objectToTrack, double rotationRate){
		return obj -> {
			if (objectToTrack == null) return null;

			Optional<MovementTransform> move = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
			if (!move.isPresent()) return null;
			
			//plot deflection (if target moving) and aim for that
			Vector vTrackee = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(objectToTrack, obj.getCentre(), move.get().getSpeed());

			Vector vMove = move.get().getVector();
			double xAngleDif = Math.toDegrees(Math.acos(vTrackee.y) - Math.acos(vMove.getUnitVector().y));
			double xAngleMod = xAngleDif > 0 ? rotationRate : -rotationRate;
			
			double yAngleDif = Math.toDegrees(Math.acos(vTrackee.x) - Math.acos(vMove.getUnitVector().x));
			double yAngleMod = yAngleDif > 0 ? -rotationRate : rotationRate;
			
			ICanvasObject temp = new CanvasObject();
			WorldCoord tempCoord = new WorldCoord(vMove.x, vMove.y, vMove.z);
			temp.getVertexList().add(tempCoord);
			
			temp.applyTransform(Axis.X.getRotation(xAngleMod));
			temp.applyTransform(Axis.Y.getRotation(yAngleMod));
			
			vMove.x = tempCoord.x;
			vMove.y = tempCoord.y;
			vMove.z = tempCoord.z;
			
			return null;
		};
	}

}
