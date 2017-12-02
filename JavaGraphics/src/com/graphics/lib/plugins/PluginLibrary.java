package com.graphics.lib.plugins;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.Translation;

public class PluginLibrary {
	
	private static final String IN_COLLISION = "IN_COLLISION";
	
	private PluginLibrary() {}
	
	public static IPlugin<IPlugable, Void> generateTrailParticles(Color colour, int density, double exhaustVelocity, double particleSize)
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.isEmpty()) {
				return null;
			}
			
			Vector baseVector = mTrans.stream().map(trans -> Vector.builder().from(trans.getVelocity()))
												.reduce(Vector.builder(), (left, right) -> left.combine(right))
												.build(); 
			
			
			double speed = baseVector.getSpeed();
			baseVector = baseVector.getUnitVector();
			
			List<WorldCoord> vertices = obj.getVertexList().stream().filter(GeneralPredicates.untagged(obj)).collect(Collectors.toList());//only get untagged points (tagged points usually hidden and have special purpose)
			for (int i = 0 ; i < density ; i++)
			{
				int index = new Random().nextInt(vertices.size() - 1);
				CanvasObject fragment = Utils.getParticle(vertices.get(index), particleSize);
				fragment.setColour(colour);
				fragment.setProcessBackfaces(true);
				double xVector = baseVector.getX() + (Math.random()/2) - 0.25;
				double yVector = baseVector.getY() + (Math.random()/2) - 0.25;
				double zVector = baseVector.getZ() + (Math.random()/2) - 0.25;
				Transform rot1 = new RepeatingTransform<Rotation>(Axis.Y.getRotation(Math.random() * 5), 45);
				MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), speed - exhaustVelocity);
				move.moveUntil(t -> rot1.isCompleteSpecific());
				Transform rot2 = new RepeatingTransform<Rotation>(Axis.X.getRotation(Math.random() * 5), t -> rot1.isCompleteSpecific());				
				CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, rot1, rot2);
				fragment.addTransform(move);

				fragment.addFlag(Events.PHASED);
				fragment.deleteAfterTransforms();	
				obj.getChildren().add(fragment);
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Set<ICanvasObject>> explode() 
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
    		Set<ICanvasObject> children = new HashSet<>();
    		for (Facet f : obj.getFacetList())
    		{
    			ICanvasObject fragment = Utils.getFragment(f);
    			fragment.setProcessBackfaces(true);
    			fragment.setColour(f.getColour() != null ? f.getColour() : obj.getColour());
    			Vector baseVector = fragment.getFacetList().get(0).getNormal();
    			double xVector = baseVector.getX() + (Math.random()/2) - 0.25;
    			double yVector = baseVector.getY() + (Math.random()/2) - 0.25;
    			double zVector = baseVector.getZ() + (Math.random()/2) - 0.25;
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
    		plugable.removePlugins();
    		
    		return children;
		};
	}
	
	public static IPlugin<IPlugable,Void> flash(Collection<ILightSource> lightSources)
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
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
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			ICanvasObject inCollision = (ICanvasObject)plugable.executePlugin(IN_COLLISION); //may need to be a list - may hit more than one!
			
			for (ICanvasObject impactee : objects.get()){
				if (impactee.equals(obj)) continue;
			
				if (obj.getVertexList().stream().anyMatch(p -> FunctionHandler.getFunctions(impactee).isPointInside(impactee, p)))
				{
					if (impactee.equals(inCollision)) { return null;}
					if (impactorPlugin != null) {
						plugable.executePlugin(impactorPlugin);
					}
					if (impacteePlugin != null) {
						impactee.getTrait(IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
					}

					plugable.registerPlugin(IN_COLLISION, o -> impactee, false);
					return impactee;
				}else if (impactee.equals(inCollision)) {
					plugable.removePlugin(IN_COLLISION);
				}
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable,ICanvasObject> hasCollidedNew(ICanvasObjectList objects, String impactorPlugin, String impacteePlugin)
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			ICanvasObject inCollision = (ICanvasObject)plugable.executePlugin(IN_COLLISION); //may need to be a list - may hit more than one!
			
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.isEmpty()) {
				return null;
			}
			
			Vector baseVector = mTrans.stream().map(trans -> Vector.builder().from(trans.getVelocity()))
												.reduce(Vector.builder(), (left, right) -> left.combine(right))
												.build();
			
			for (ICanvasObject impactee : objects.get()){
				if (impactee.equals(obj)) continue;
				//Tries to factor in possibility object was drawn completely on the other side of an object (after moving) and thus not detected as a collision by point inside check
				for(Point p : obj.getVertexList()){
					Point prevPoint = new Point(p);
					prevPoint.x -= baseVector.getX();
					prevPoint.y -= baseVector.getY();
					prevPoint.z -= baseVector.getZ();
				
					for(Facet f : impactee.getFacetList())
					{
						Point in = f.getIntersectionPointWithFacetPlane(prevPoint, prevPoint.vectorToPoint(p).getUnitVector(), false);
						if (f.isPointWithin(in) && prevPoint.distanceTo(in) <= prevPoint.distanceTo(p)) {
							if (impactee.equals(inCollision)) { return null;}
							if (impactorPlugin != null) {
								plugable.executePlugin(impactorPlugin);
							}
							if (impacteePlugin != null){
								impactee.getTrait(IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
							}

							plugable.registerPlugin(IN_COLLISION, o -> impactee, false);
							return impactee;
						}
					}
				}
				if (impactee.equals(inCollision)) {
					plugable.removePlugin(IN_COLLISION);
				} //is slow if lots of objects using this method, might split so things like explosion fragments use the simpler point is inside method
				
			}
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> delete()
	{
		return plugable -> {
			plugable.getParent().setDeleted(true);
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> stop()
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			obj.cancelTransforms();
			plugable.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> stop2()
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			for (MovementTransform t : obj.getTransformsOfType(MovementTransform.class)){
				t.setSpeed(0);
			}
			plugable.removePlugins();
			return null;
		};
	}
	
	public static IPlugin<IPlugable, Void> bounce(ICanvasObject impactee)
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			//find impacted points and use the one that was nearest the impactee before hitting it, then find the facet it hit and reflect
			//the movement vector using the normal of that facet.
			//It's not entirely perfect, because we don't have infinite normals, you can see this if you hit a sphere dead on with another sphere,
			//it won't come straight back as it would in reality, as we don't necessarily have a normal that is pointing straight back to use.
			//However, we could add another plugin for spheres, the normal is then the vector from centre to impact point
			
			Set<WorldCoord> impactPoints = obj.getVertexList().stream().filter(p -> FunctionHandler.getFunctions(impactee).isPointInside(impactee, p)).collect(Collectors.toSet());
			if (impactPoints.isEmpty()) 
			    return null;
			
			List<MovementTransform> mTrans = obj.getTransformsOfType(MovementTransform.class);
			if (mTrans.isEmpty()) return null;
			
			MovementTransform move = mTrans.get(0); //just getting the first one for now (if there is more than one) 
			Vector velocity = move.getVelocity();
			
			Optional<Facet> curFacet = Optional.empty();
			double curDist = -1;
			
			for(WorldCoord impactPoint : impactPoints) {
				Point prevPoint = new Point(impactPoint.x - velocity.getX(), impactPoint.y - velocity.getY(), impactPoint.z - velocity.getZ());

				for(Facet f : impactee.getFacetList().stream().filter(f -> f.getDistanceFromFacetPlane(impactPoint) < move.getSpeed() + 1).collect(Collectors.toList()))
				{
					Point intersect = f.getIntersectionPointWithFacetPlane(prevPoint, move.getVector());
	
					//if intersected with plane of facet check we are within the bounds of the facet
					if (intersect != null && (prevPoint.distanceTo(intersect) < curDist || curDist == -1 ) && f.isPointWithin(intersect)){
						curFacet = Optional.of(f);
						curDist = prevPoint.distanceTo(intersect);
					}
				}
			}
			curFacet.ifPresent(facet -> Utils.reflect(move, facet.getNormal()));
			
			return null;
		};
	}
	
	
	public static IPlugin<IPlugable, Void> track(ICanvasObject objectToTrack, double rotationRate){
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			if (objectToTrack == null) return null;

			Optional<MovementTransform> move = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
			if (!move.isPresent()) return null;
			
			//plot deflection (if target moving) and aim for that
			Pair<Vector,Point> target = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(objectToTrack, obj.getCentre(), move.get().getSpeed());
			Point centre = obj.getCentre();
			new Translation(-centre.x, -centre.y, -centre.z).doTransformSpecific().accept(target.getRight());
			Vector vMove = move.get().getVector();
			
			OrientationData.getRotationsForVector(vMove).stream().map(transform -> new Rotation(transform.getAxis(), -transform.getAngleProgression()))
			                                                                .forEach(transform -> transform.doTransformSpecific().accept(target.getRight()));
				
			double xAngleMod = 0;
			double yAngleMod = 0;
			if (target.getRight().z < 0) {
			    //TODO still no worky, now tends to spiral away from the target, need to do a turn like ship does, 
			    //transforms below need to take account of current orientation
			    yAngleMod = rotationRate;
			} else {
			    if (target.getRight().x < 0) yAngleMod = -rotationRate;
			    if (target.getRight().x > 0) yAngleMod = rotationRate;
			    if (target.getRight().y < 0) xAngleMod = rotationRate;
                if (target.getRight().y > 0) xAngleMod = -rotationRate;
			}			

			Point tempCoord = new Point(vMove.getX(), vMove.getY(), vMove.getZ());
			Axis.X.getRotation(xAngleMod).doTransformSpecific().accept(tempCoord);
			Axis.Y.getRotation(yAngleMod).doTransformSpecific().accept(tempCoord);
			
			move.get().setVector(Vector.builder().x(tempCoord.x).y(tempCoord.y).z(tempCoord.z).build());
			
			return null;
		};
	}

}
