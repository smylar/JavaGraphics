package com.graphics.lib.plugins;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphics.lib.canvas.Canvas3D;
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
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.traits.TraitHandler;
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
			
			Vector baseVector = mTrans.stream().map(trans -> new Vector(trans.getVelocity()))
												.reduce(Vector.ZERO_VECTOR, Vector::combine);
			
			
			double speed = baseVector.getSpeed();
			baseVector = baseVector.getUnitVector();
			
			List<WorldCoord> vertices = obj.getVertexList().stream().filter(GeneralPredicates.untagged()).toList();//only get untagged points (tagged points usually hidden and have special purpose)
			for (int i = 0 ; i < density ; i++)
			{
				int index = new Random().nextInt(vertices.size() - 1);
				CanvasObject fragment = Utils.getParticle(vertices.get(index), particleSize);
				fragment.setColour(colour);
				fragment.setProcessBackfaces(true);
				double xVector = baseVector.x() + (Math.random()/2) - 0.25;
				double yVector = baseVector.y() + (Math.random()/2) - 0.25;
				double zVector = baseVector.z() + (Math.random()/2) - 0.25;
				Transform rot1 = new RepeatingTransform<>(Axis.Y.getRotation(Math.random() * 5), 45);
				MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), speed - exhaustVelocity);
				move.moveUntil(t -> rot1.isCompleteSpecific());
				Transform rot2 = new RepeatingTransform<>(Axis.X.getRotation(Math.random() * 5), t -> rot1.isCompleteSpecific());
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
    			double xVector = baseVector.x() + (Math.random()/2) - 0.25;
    			double yVector = baseVector.y() + (Math.random()/2) - 0.25;
    			double zVector = baseVector.z() + (Math.random()/2) - 0.25;
    			MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), Math.random() * 15 + 1 );
    			fragment.addTransform(move);
    			CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, 
    																		new RepeatingTransform<>(Axis.Y.getRotation(Math.random() * 10),
    																		t -> move.isComplete()), new RepeatingTransform<>(Axis.X.getRotation(Math.random() * 10),
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
	
	public static IPlugin<IPlugable,Void> flash()
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			LightSource flash = new LightSource(obj.getCentre().x,  obj.getCentre().y, obj.getCentre().z);
			flash.setRange(-1);
			Canvas3D.get().addLightSource(flash);
			
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
					    TraitHandler.INSTANCE.getTrait(impactee, IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
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
			
			Vector baseVector = mTrans.stream().map(trans -> new Vector(trans.getVelocity()))
												.reduce(Vector.ZERO_VECTOR, Vector::combine);
			
			for (ICanvasObject impactee : objects.get()) {
				if (impactee.equals(obj)) continue;
				//Tries to factor in possibility object was drawn completely on the other side of an object (after moving) and thus not detected as a collision by point inside check
				for(Point p : obj.getVertexList()){
					Point prevPoint = new Point(p);
					prevPoint.x -= baseVector.x();
					prevPoint.y -= baseVector.y();
					prevPoint.z -= baseVector.z();
				
					for(Facet f : impactee.getFacetList())
					{
						Point in = f.getIntersectionPointWithFacetPlane(prevPoint, prevPoint.vectorToPoint(p).getUnitVector(), false);
						if (f.isPointWithin(in) && prevPoint.distanceTo(in) <= prevPoint.distanceTo(p)) {
							if (impactee.equals(inCollision)) { return null;}
							if (impactorPlugin != null) {
								plugable.executePlugin(impactorPlugin);
							}
							if (impacteePlugin != null){
							    TraitHandler.INSTANCE.getTrait(impactee, IPlugable.class).ifPresent(impacteePlugable -> impacteePlugable.executePlugin(impacteePlugin));
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
	
	public static IPlugin<IPlugable, Void> stop(boolean removePlugins)
	{
		return plugable -> {
		    ICanvasObject obj = plugable.getParent();
			obj.cancelTransforms();
			if (removePlugins) {
			    plugable.removePlugins();
			}
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
				Point prevPoint = new Point(impactPoint.x - velocity.x(), impactPoint.y - velocity.y(), impactPoint.z - velocity.z());

				for(Facet f : impactee.getFacetList().stream().filter(f -> f.getDistanceFromFacetPlane(impactPoint) < move.getSpeed() + 1).toList())
				{
					Point intersect = f.getIntersectionPointWithFacetPlane(prevPoint, move.getVector());
	
					//if intersected with plane of facet check we are within the bounds of the facet
					if (intersect != null && (prevPoint.distanceTo(intersect) < curDist || curDist == -1 ) && f.isPointWithin(intersect)) {
						curFacet = Optional.of(f);
						curDist = prevPoint.distanceTo(intersect);
					}
				}
			}
			curFacet.ifPresent(facet -> Utils.reflect(move, facet.getNormal()));
			
			return null;
		};
	}
	
	
	public static IPlugin<IPlugable, Void> track(final ICanvasObject objectToTrack, final double rotationRate) {
		return plugable -> {
		    Optional.ofNullable(plugable.getParent()).ifPresent(obj -> 
		        obj.getTransformsOfType(MovementTransform.class).stream()
		                                                        .findFirst()
		                                                        .flatMap(m -> TraitHandler.INSTANCE.getTrait(obj, IOrientable.class))
		                                                        .ifPresent(o -> track(o, obj, objectToTrack, rotationRate))
		    );
			
			return null;
		};
	}
	
	private static void track(IOrientable orientable, ICanvasObject parent, ICanvasObject objectToTrack, double rotationRate) {
	  //plot deflection (if target moving) and aim for that
	    Point centre = parent.getCentre();
        Pair<Vector,Point> target = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(objectToTrack, centre, orientable.getOrientation().getForward().getSpeed());
        
        new Translation(-centre.x, -centre.y, -centre.z).doTransformSpecific().accept(target.getRight());
        ICanvasObject rep = orientable.getOrientation().getRepresentation();

        Transform toBase = orientable.toBaseOrientationTransform();
        rep.applyTransform(toBase);
        toBase.replay(target.getRight());
            
        double xAngleMod = 0;
        double yAngleMod = 0;
        if (target.getRight().z < 0) {
            yAngleMod = rotationRate;
        } else {
            if (target.getRight().x < 0) yAngleMod = -rotationRate;
            if (target.getRight().x > 0) yAngleMod = rotationRate;
            
        }   
        
        if (target.getRight().y < 0) xAngleMod = rotationRate;
        if (target.getRight().y > 0) xAngleMod = -rotationRate;

        rep.applyTransform(Axis.X.getRotation(xAngleMod), Axis.Y.getRotation(yAngleMod), orientable.reapplyOrientationTransform());

	}

}
