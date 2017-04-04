package com.graphics.tests.shapes;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Torus;
import com.graphics.tests.TestUtils;

/**
 * Extension of Torus that tracks a point to see if it has passed through the Torus, and calls the supplied plugin when it does
 * 
 * @author Paul Brandon
 *
 */
public class Gate extends Torus {
	
	private Map<ICanvasObject, Double> currentDotProducts = new HashMap<>();
	private IPlugin<Gate,Void> passThroughPluginForGate;
	private IPlugin<ICanvasObject,Void> passThroughPluginForObject;
	private List<WorldCoord> innerPoints;
	private int offset = 0;
	private int interval = 4;
	private int cnt = 0;
	private double holeRadius;
	private boolean passThrough = false;
	
	public Gate(double tubeRadius, double holeRadius, int arcProgression) {
		super(tubeRadius, holeRadius, arcProgression);
		this.holeRadius = holeRadius;
		innerPoints = this.getVertexList().stream().filter(v -> v.distanceTo(getCentre()) <= holeRadius + 0.01).collect(Collectors.toList());
	}
	
	public void setPassThroughPluginForGate(IPlugin<Gate, Void> passThroughPlugin) {
		this.passThroughPluginForGate = passThroughPlugin;
	}
	
	public void setPassThroughPluginForObject(IPlugin<ICanvasObject, Void> passThroughPlugin) {
        this.passThroughPluginForObject = passThroughPlugin;
    }

	private double getCurrentDotProduct(ICanvasObject obj)
	{
		return obj.getCentre().vectorToPoint(this.getCentre()).getUnitVector().dotProduct(this.getHolePlane().getNormal());
	}
	
	@Override
	public void onDrawComplete(){
		super.onDrawComplete();
		Map<ICanvasObject, Double> dotProducts = new HashMap<>();
		TestUtils.getFilteredObjectList().get().parallelStream().forEach(obj -> {
		    double dot = getCurrentDotProduct(obj);
		    dotProducts.put(obj, dot);
		    if (obj.getCentre().distanceTo(this.getCentre()) < this.getActualHoleRadius() && isOpposite(obj, dot)) {
		        passThrough = true;
		        if (this.passThroughPluginForObject != null) {
		            this.passThroughPluginForObject.execute(obj);
		        }
		    }
		});
		
		if (passThrough && this.passThroughPluginForGate != null) {
		    this.passThroughPluginForGate.execute(this);
		    passThrough = false;
		}
		
		this.currentDotProducts = dotProducts;
		if (++cnt == interval) {
		    addParticleEffect();
		    cnt = 0;
		}
	}
	
	private boolean isOpposite(ICanvasObject obj, double dotProduct) {
	    Double objDotProduct = this.currentDotProducts.getOrDefault(obj, 0D);
	    return (dotProduct > 0 && objDotProduct <= 0) || (dotProduct < 0 && objDotProduct >= 0);
	}
	
	private void addParticleEffect() {
	    for (int i = offset++ ; i < innerPoints.size() ; i+=interval) {
	        Point p = innerPoints.get(i);
            CanvasObject fragment = new CanvasObject();
            fragment.getVertexList().add(new WorldCoord(p.x , p.y, p.z));
            fragment.getVertexList().add(new WorldCoord(p.x + 2, p.y, p.z));
            fragment.getVertexList().add(new WorldCoord(p.x, p.y + 2, p.z));
            fragment.getFacetList().add(new Facet(fragment.getVertexList().get(0), fragment.getVertexList().get(1), fragment.getVertexList().get(2)));
            fragment.setColour(Color.PINK);
            fragment.setProcessBackfaces(true);
            MovementTransform move = new MovementTransform(p.vectorToPoint(getCentre()).getUnitVector(), 1).moveUntil(t -> t.getDistanceMoved() >= holeRadius-2);        
            fragment.addTransform(move);
            fragment.addFlag(Events.NO_SHADE);
            fragment.addFlag(Events.PHASED);
            fragment.deleteAfterTransforms();   
            getChildren().add(fragment);
	    }
	    
	    if (offset == interval) {
	        offset = 0;
	    }
	}

}
