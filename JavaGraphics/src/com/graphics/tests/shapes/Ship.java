package com.graphics.tests.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;

public final class Ship extends CanvasObject {
	
	private final Facet wingFlashLeft;
	private final Facet wingFlashRight;
	private int cnt = 0;
	private double acceleration = 0.2;
	private double panRate = 4;
	private List<IEffector> weapons = new ArrayList<>();
	
	public Ship(final int width, final int depth, final int height)
	{
		super(() -> init(width,depth,height));
		
		wingFlashLeft = this.getFacetList().get(6);
		wingFlashLeft.setColour(Color.MAGENTA);
		wingFlashLeft.setBaseIntensity(1);
		
		wingFlashRight = this.getFacetList().get(7);
		wingFlashRight.setColour(Color.MAGENTA);
		wingFlashRight.setBaseIntensity(1);
		
		getFacetList().get(4).setColour(Color.YELLOW);
		getFacetList().get(5).setColour(Color.YELLOW);
		
		//self registering - based on trail plugin from plugin library
		TraitHandler.INSTANCE.registerTrait(this, new PlugableTrait()).registerPlugin("TRAIL", 

					plugable -> {
					    ICanvasObject obj = plugable.getParent();
						Optional<MovementTransform> movement = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
						if (!movement.isPresent() || movement.get().getAcceleration() == 0) return null; //no point if it isn't moving
						Vector baseVector = movement.get().getVector();
						if (baseVector == null) return null; 
						
						for (int i = 0 ; i < 50 ; i++)
						{

							int index = new Random().nextInt(5);
							if (index == 0) index++;
							Point p = obj.getVertexList().get(index);
							CanvasObject fragment = Utils.getParticle(p, 9);

							int colour = new Random().nextInt(3);
							if (colour == 0) fragment.setColour(Color.PINK);
							if (colour == 1) fragment.setColour(Color.ORANGE);
							if (colour == 2) fragment.setColour(Color.YELLOW);
							fragment.setProcessBackfaces(true);
							double xVector = baseVector.getX() + (Math.random()/2) - 0.25;
							double yVector = baseVector.getY() + (Math.random()/2) - 0.25;
							double zVector = baseVector.getZ() + (Math.random()/2) - 0.25;
							Transform rot1 = new RepeatingTransform<Rotation>(new Rotation(Axis.Y, Math.random() * 10), 15);
							MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), movement.get().getAcceleration() > 0 ? -20 : 20);
							move.moveUntil(t -> rot1.isCompleteSpecific());
							Transform rot2 = new RepeatingTransform<Rotation>(new Rotation(Axis.X, Math.random() * 10), t -> rot1.isCompleteSpecific());				
							CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, rot1, rot2);
							fragment.addTransform(move);
							fragment.deleteAfterTransforms();	
							fragment.addFlag(Events.PHASED);
							fragment.addFlag(Events.NO_SHADE);
							obj.getChildren().add(fragment);
						}
						return null;
					}
				, true);
	}
	
	@Override
	public Point getCentre(){
		return this.getVertexList().get(5);
	}
	
	@Override
	public void onDrawComplete(){
		super.onDrawComplete();
		if (++cnt > 10){
			cnt = 0;
			if (wingFlashRight.getColour().equals(Color.MAGENTA)){
				wingFlashRight.setColour(Color.GREEN);
				wingFlashLeft.setColour(Color.GREEN);
			}else{
				wingFlashRight.setColour(Color.MAGENTA);
				wingFlashLeft.setColour(Color.MAGENTA);
			}
		}
	}

	public double getAcceleration() {
		return this.acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public double getPanRate() {
		return panRate;
	}

	public void setPanRate(double panRate) {
		this.panRate = panRate;
	}

	public List<IEffector> getWeapons() {
		return weapons;
	}
	
	public void addWeapon(IEffector weapon){
		weapons.add(weapon);
	}
	
	private static Pair<ImmutableList<WorldCoord>,ImmutableList<Facet>> init(final int width, final int depth, final int height) {
	    ImmutableList<WorldCoord> vertexList = generateVertexList(width, depth, height);
	    return Pair.of(vertexList, generateFacetList(vertexList));
	}
	
	private static ImmutableList<WorldCoord> generateVertexList(final int width, final int depth, final int height) {
	    return ImmutableList.of(new WorldCoord(0, 0, 0),
	                            new WorldCoord(width/2, 0, depth),
	                            new WorldCoord(-width/2, 0, depth),
	                            new WorldCoord(0, height/2, depth * 0.8),
	                            new WorldCoord(0, -height/2, depth * 0.8),
        
	                            new WorldCoord(0, 0, depth/2),//centre point
        
	                            new WorldCoord(width/2, 5, depth - 5),
	                            new WorldCoord(width/2, -5, depth - 5), //wing flash left
        
	                            new WorldCoord(-width/2, 5, depth - 5),
	                            new WorldCoord(-width/2, -5, depth - 5)); //wing flash left
	}
	
	private static ImmutableList<Facet> generateFacetList(List<WorldCoord> vertexList) {
	    return ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(4)),
                                   new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(1)),
                                   new Facet(vertexList.get(0), vertexList.get(4), vertexList.get(2)),
                                   new Facet(vertexList.get(0), vertexList.get(2), vertexList.get(3)),
                                   new Facet(vertexList.get(1), vertexList.get(3), vertexList.get(4)),
                                   new Facet(vertexList.get(2), vertexList.get(4), vertexList.get(3)),
                                   new Facet(vertexList.get(1), vertexList.get(7), vertexList.get(6)),
                                   new Facet(vertexList.get(2), vertexList.get(8), vertexList.get(9)));
	}
}
