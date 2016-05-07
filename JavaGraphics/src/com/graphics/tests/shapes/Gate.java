package com.graphics.tests.shapes;

import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.shapes.Torus;

/**
 * Extension of Torus that tracks a point to see if it has passed through the Torus, and calls the supplied plugin when it does
 * 
 * @author Paul Brandon
 *
 */
public class Gate extends Torus {
	
	private IPointFinder trackingPoint; //could have a set of these to track multiple points
	private double currentDotProduct;
	private IPlugin<Gate,Void> passThroughPlugin;
	
	public Gate(double tubeRadius, double holeRadius, int arcProgression, IPointFinder trackingPoint) {
		super(tubeRadius, holeRadius, arcProgression);
		this.trackingPoint = trackingPoint;
		this.currentDotProduct = this.getCurrentDotProduct();
	}
	
	public void setPassThroughPlugin(IPlugin<Gate, Void> passThroughPlugin) {
		this.passThroughPlugin = passThroughPlugin;
	}

	private double getCurrentDotProduct()
	{
		return this.trackingPoint != null ? this.trackingPoint.find().vectorToPoint(this.getCentre()).getUnitVector().dotProduct(this.getHolePlane().getNormal()) : 0;
	}
	
	@Override
	public void onDrawComplete(){
		super.onDrawComplete();
		if (this.trackingPoint == null) return;
		
		double dot = getCurrentDotProduct();
		if (this.passThroughPlugin != null && this.trackingPoint.find().distanceTo(this.getCentre()) < this.getActualHoleRadius()){
			if ((dot > 0 && this.currentDotProduct <= 0)||
					(dot < 0 && this.currentDotProduct >= 0)){
				this.passThroughPlugin.execute(this);
			}
		}
		this.currentDotProduct = dot;
	}

}
