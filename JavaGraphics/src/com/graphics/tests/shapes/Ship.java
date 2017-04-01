package com.graphics.tests.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;

public class Ship extends CanvasObject {
	
	private Facet wingFlashLeft;
	private Facet wingFlashRight;
	private int cnt = 0;
	private double acceleration = 0.2;
	private double panRate = 4;
	private List<IEffector> weapons = new ArrayList<>();
	
	public Ship(int width, int depth, int height)
	{
		super();
		PlugableCanvasObject ship = new PlugableCanvasObject();
		ship.getVertexList().add(new WorldCoord(0, 0, 0));
		ship.getVertexList().add(new WorldCoord(width/2, 0, depth));
		ship.getVertexList().add(new WorldCoord(-width/2, 0, depth));
		ship.getVertexList().add(new WorldCoord(0, height/2, depth * 0.8));
		ship.getVertexList().add(new WorldCoord(0, -height/2, depth * 0.8));
		
		ship.getVertexList().add(new WorldCoord(0, 0, depth/2)); //centre point
		
		ship.getVertexList().add(new WorldCoord(width/2, 5, depth - 5));
		ship.getVertexList().add(new WorldCoord(width/2, -5, depth - 5)); //wing flash left
		
		ship.getVertexList().add(new WorldCoord(-width/2, 5, depth - 5));
		ship.getVertexList().add(new WorldCoord(-width/2, -5, depth - 5)); //wing flash left
		
		ship.getFacetList().add(new Facet(ship.getVertexList().get(0), ship.getVertexList().get(1), ship.getVertexList().get(4)));
		ship.getFacetList().add(new Facet(ship.getVertexList().get(0), ship.getVertexList().get(3), ship.getVertexList().get(1)));
		ship.getFacetList().add(new Facet(ship.getVertexList().get(0), ship.getVertexList().get(4), ship.getVertexList().get(2)));
		ship.getFacetList().add(new Facet(ship.getVertexList().get(0), ship.getVertexList().get(2), ship.getVertexList().get(3)));
		
		ship.getFacetList().add(new Facet(ship.getVertexList().get(1), ship.getVertexList().get(3), ship.getVertexList().get(4)));
		ship.getFacetList().add(new Facet(ship.getVertexList().get(2), ship.getVertexList().get(4), ship.getVertexList().get(3)));
		
		wingFlashLeft = new Facet(ship.getVertexList().get(1), ship.getVertexList().get(7), ship.getVertexList().get(6));
		wingFlashLeft.setColour(Color.MAGENTA);
		wingFlashLeft.setBaseIntensity(1);
		ship.getFacetList().add(wingFlashLeft);
		
		wingFlashRight = new Facet(ship.getVertexList().get(2), ship.getVertexList().get(8), ship.getVertexList().get(9));
		wingFlashRight.setColour(Color.MAGENTA);
		wingFlashRight.setBaseIntensity(1);
		ship.getFacetList().add(wingFlashRight);
		
		ship.getFacetList().get(4).setColour(Color.YELLOW);
		ship.getFacetList().get(5).setColour(Color.YELLOW);
		
		//self registering - based on trail plugin from plugin library
		ship.registerPlugin("TRAIL", 

					(obj) -> {
						Optional<MovementTransform> movement = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
						if (!movement.isPresent() || movement.get().getAcceleration() == 0) return null; //no point if it isn't moving
						Vector baseVector = movement.get().getVector();
						if (baseVector == null) return null; 
						
						for (int i = 0 ; i < 50 ; i++)
						{

							int index = new Random().nextInt(5);
							if (index == 0) index++;
							Point p = obj.getVertexList().get(index);
							CanvasObject fragment = new CanvasObject();
							fragment.getVertexList().add(new WorldCoord(p.x , p.y, p.z));
							fragment.getVertexList().add(new WorldCoord(p.x + 9 , p.y, p.z));
							fragment.getVertexList().add(new WorldCoord(p.x, p.y + 9, p.z));
							fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
							int colour = new Random().nextInt(3);
							if (colour == 0) fragment.setColour(Color.PINK);
							if (colour == 1) fragment.setColour(Color.ORANGE);
							if (colour == 2) fragment.setColour(Color.YELLOW);
							fragment.setProcessBackfaces(true);
							double xVector = baseVector.x + (Math.random()/2) - 0.25;
							double yVector = baseVector.y + (Math.random()/2) - 0.25;
							double zVector = baseVector.z + (Math.random()/2) - 0.25;
							Transform rot1 = new RepeatingTransform<Rotation>(new Rotation(Axis.Y, Math.random() * 10), 15);
							MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), movement.get().getAcceleration() > 0 ? -20 : 20);
							move.moveUntil(t -> rot1.isCompleteSpecific());
							Transform rot2 = new RepeatingTransform<Rotation>(new Rotation(Axis.X, Math.random() * 10), t -> rot1.isCompleteSpecific());				
							CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(fragment, rot1, rot2);
							fragment.addTransform(move);
							fragment.deleteAfterTransforms();	
							fragment.addFlag(Events.NO_SHADE);
							obj.getChildren().add(fragment);
						}
						return null;
					}

				, true);
		this.setWrappedObject(ship);

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
}
