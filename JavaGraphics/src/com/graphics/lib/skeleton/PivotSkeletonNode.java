package com.graphics.lib.skeleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.WorldCoord;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Translation;

public class PivotSkeletonNode extends SkeletonNode {
	private double xMax = 0;
	private double xMin = 0;
	private double yMax = 0;
	private double yMin = 0;
	private double zMax = 0;
	private double zMin = 0; //rotation limits
	
	private double xCur = 0;
	private double yCur = 0;
	private double zCur = 0; //current rotations
	
	private boolean xTravelPositive = true;
	private boolean yTravelPositive = true;
	private boolean zTravelPositive = true; //current direction of rotation
	
	//TODO rotation speed limits?
	
	private Map<String, PivotAction> animations = new HashMap<String, PivotAction>();
	
	
	public double getxMax() {
		return xMax;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	public double getxMin() {
		return xMin;
	}

	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public double getyMax() {
		return yMax;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}

	public double getyMin() {
		return yMin;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	public double getzMax() {
		return zMax;
	}

	public void setzMax(double zMax) {
		this.zMax = zMax;
	}

	public double getzMin() {
		return zMin;
	}

	public void setzMin(double zMin) {
		this.zMin = zMin;
	}

	public double getxCur() {
		return xCur;
	}

	public void setxCur(double xCur) {
		this.xCur = xCur;
	}

	public double getyCur() {
		return yCur;
	}

	public void setyCur(double yCur) {
		this.yCur = yCur;
	}

	public double getzCur() {
		return zCur;
	}

	public void setzCur(double zCur) {
		this.zCur = zCur;
	}

	public boolean isxTravelPositive() {
		return xTravelPositive;
	}

	public void setxTravelPositive(boolean xTravelPositive) {
		this.xTravelPositive = xTravelPositive;
	}

	public boolean isyTravelPositive() {
		return yTravelPositive;
	}

	public void setyTravelPositive(boolean yTravelPositive) {
		this.yTravelPositive = yTravelPositive;
	}

	public boolean iszTravelPositive() {
		return zTravelPositive;
	}

	public void setzTravelPositive(boolean zTravelPositive) {
		this.zTravelPositive = zTravelPositive;
	}

	public Map<String, PivotAction> getAnimations() {
		return animations;
	}


	@Override
	public void playAnimation(String id)
	{
		//Note canvas object will need to realign itself to base first
		
		//TODO find matching animation an play it, do I leave it to the animation or propagate the movement down sub nodes here
		PivotAction action = animations.get(id);
		if (action != null){
			Collection<PivotDetail> details = action.get(this);
			//TODO check movement within constraints
			
			Set<WorldCoord> coords = this.getAllSubCoords();
			Translation to = new Translation(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
			Translation away = new Translation(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			
			to.doTransform(coords);
			
			for(PivotDetail detail : details){
				Rotation<?> rot = Rotation.getRotation(detail.getDirection(), detail.getAmount());
				
				if (rot != null)
				{
					rot.doTransform(coords);
				}
				//update current position etc.
				if (detail.getDirection() == 'x'){
					this.xCur += detail.getAmount();
					this.xTravelPositive = detail.getAmount() >= 0;
				}else if (detail.getDirection() == 'y'){
					this.yCur += detail.getAmount();
					this.yTravelPositive = detail.getAmount() >= 0;
				}else if (detail.getDirection() == 'z'){
					this.zCur += detail.getAmount();
					this.zTravelPositive = detail.getAmount() >= 0;
				}
			}
			
			away.doTransform(coords);
		}
		
		super.playAnimation(id);
	}
}
