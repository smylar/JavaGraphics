package com.graphics.tests;

import java.awt.Color;
import java.util.Optional;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;

public class Ship extends PlugableCanvasObject<Ship> {
	
	private Facet wingFlashLeft;
	private Facet wingFlashRight;
	private int cnt = 0;
	
	public Ship(int width, int depth, int height)
	{
		super();
		this.getVertexList().add(new WorldCoord(0, 0, 0));
		this.getVertexList().add(new WorldCoord(width/2, 0, depth));
		this.getVertexList().add(new WorldCoord(-width/2, 0, depth));
		this.getVertexList().add(new WorldCoord(0, height/2, depth * 0.8));
		this.getVertexList().add(new WorldCoord(0, -height/2, depth * 0.8));
		
		this.getVertexList().add(new WorldCoord(0, 0, depth/2)); //centre point
		
		this.getVertexList().add(new WorldCoord(width/2, 5, depth - 5));
		this.getVertexList().add(new WorldCoord(width/2, -5, depth - 5)); //wing flash left
		
		this.getVertexList().add(new WorldCoord(-width/2, 5, depth - 5));
		this.getVertexList().add(new WorldCoord(-width/2, -5, depth - 5)); //wing flash left
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(4), this.getVertexList().get(2)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(3)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(3), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(4), this.getVertexList().get(3)));
		
		wingFlashLeft = new Facet(this.getVertexList().get(1), this.getVertexList().get(7), this.getVertexList().get(6));
		wingFlashLeft.setColour(Color.MAGENTA);
		wingFlashLeft.setBaseIntensity(1);
		this.getFacetList().add(wingFlashLeft);
		
		wingFlashRight = new Facet(this.getVertexList().get(2), this.getVertexList().get(8), this.getVertexList().get(9));
		wingFlashRight.setColour(Color.MAGENTA);
		wingFlashRight.setBaseIntensity(1);
		this.getFacetList().add(wingFlashRight);
		
		this.getFacetList().get(4).setColour(Color.YELLOW);
		this.getFacetList().get(5).setColour(Color.YELLOW);
		
		//self registering - based on trail plugin from plugin library
		this.registerPlugin("TRAIL", 

					(obj) -> {
						Optional<MovementTransform> movement = obj.getTransformsOfType(MovementTransform.class).stream().findFirst();
						if (!movement.isPresent()) return null; //no point if it isn't moving
						Vector baseVector = movement.get().getVector();
						if (baseVector == null) return null; 
						
						for (int i = 0 ; i < 50 ; i++)
						{

							int index = (int)Math.round(Math.random() * 4);
							if (index == 0) index++;
							Point p = obj.getVertexList().get(index);
							CanvasObject fragment = new CanvasObject();
							fragment.getVertexList().add(new WorldCoord(p.x , p.y, p.z));
							fragment.getVertexList().add(new WorldCoord(p.x + 9 , p.y, p.z));
							fragment.getVertexList().add(new WorldCoord(p.x, p.y + 9, p.z));
							fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
							int colour = (int)Math.round(Math.random() * 2);
							if (colour == 0) fragment.setColour(Color.PINK);
							if (colour == 1) fragment.setColour(Color.ORANGE);
							if (colour == 2) fragment.setColour(Color.YELLOW);
							fragment.setProcessBackfaces(true);
							double xVector = baseVector.x + (Math.random()/2) - 0.25;
							double yVector = baseVector.y + (Math.random()/2) - 0.25;
							double zVector = baseVector.z + (Math.random()/2) - 0.25;
							Transform rot1 = new RepeatingTransform<Rotation<?>>(new Rotation<YRotation>(YRotation.class, Math.random() * 10), 15);
							MovementTransform move = new MovementTransform(new Vector(xVector, yVector, zVector), -20);
							move.moveUntil(t -> rot1.isCompleteSpecific());
							Transform rot2 = new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, Math.random() * 10), t -> rot1.isCompleteSpecific());				
							fragment.addTransformAboutCentre(rot1, rot2);
							fragment.addTransform(move);
							fragment.deleteAfterTransforms();	
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
}
