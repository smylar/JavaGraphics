package com.graphics.lib.skeleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.Axis;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Translation;

/**
 * Represents a movable joint in a skeleton (e.g. a knee joint)
 * <br/><br/>
 * Skeleton nodes attached to this node will be moved when this joint is rotated along with mesh vertices attached to this node and those sub nodes
 * 
 * @author Paul Brandon
 *
 */
public class PivotSkeletonNode extends SkeletonNode {

	private Map<String, PivotAction> animations = new HashMap<String, PivotAction>();
	
	private Map<Axis, PivotInfo> pivotInfo = new HashMap<Axis, PivotInfo>();
	
	/**
	 * Gets a default pivot action that pivots back and forth between the pivot limits
	 * 
	 * @param direction - Direction of pivot (x, y or z)
	 * @param amount - Angle in degrees to pivot each draw cycle
	 * @return	A pivot action
	 */
	public static PivotAction getDefaultPivotAction(Axis direction, double amount)
	{
		return (n) ->{
			List<PivotDetail> p = new ArrayList<PivotDetail>();
			PivotDetail d = new PivotDetail();
			d.setDirection(direction);
			
			double amt = n.isTravelPositive(direction) ? amount : -amount;
			if (n.getCur(direction) >= n.getMax(direction) || n.getCur(direction) <= n.getMin(direction))
			{
				amt = -amt;
			}
			d.setAmount(amt);
			p.add(d);
			return p;
		};
	}
	
	public PivotSkeletonNode(){
		super();
		for (Axis a : Axis.values()){
			pivotInfo.put(a, new PivotInfo());
		}
	}
	
	public double getMax(Axis direction){
		return this.pivotInfo.get(direction).getMax();
	}
	
	public double getMin(Axis direction){
		return this.pivotInfo.get(direction).getMin();
	}
	
	public double getCur(Axis direction){
		return this.pivotInfo.get(direction).getCur();
	}

	public boolean isTravelPositive(Axis direction){
		return this.pivotInfo.get(direction).isTravelPositive();
	}
	
	public void setMax(Axis direction, double max){
		this.pivotInfo.get(direction).setMax(max);
	}
	
	public void setMin(Axis direction, double min){
		this.pivotInfo.get(direction).setMin(min);
	}
	
	public void setCur(Axis direction, double cur){
		this.pivotInfo.get(direction).setCur(cur);
	}
	
	public void setTravelPositive(Axis direction, boolean tp){
		this.pivotInfo.get(direction).setTravelPositive(tp);
	}

	public Map<String, PivotAction> getAnimations() {
		return animations;
	}


	@Override
	public void playAnimation(String id)
	{
		//Note canvas object will need to realign itself to base first
		
		PivotAction action = animations.get(id);
		if (action != null){
			Collection<PivotDetail> details = action.get(this);

			Set<WorldCoord> coords = this.getAllSubCoords();
			Translation to = new Translation(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
			Translation away = new Translation(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			
			to.doTransform(coords);
			
			for(PivotDetail detail : details){
				Axis direction = detail.getDirection();
				
				//keep within pivot limits
				if (getCur(direction) + detail.getAmount() > getMax(direction)) detail.setAmount(getMax(direction) - getCur(direction));
				else if (getCur(direction) + detail.getAmount() < getMin(direction)) detail.setAmount(getMin(direction) - getCur(direction));
				
				if (detail.getAmount() == 0) continue;
				
				Rotation<?> rot = Rotation.getRotation(detail.getDirection(), detail.getAmount());
				
				if (rot != null)
				{
					rot.doTransform(coords);
				}
				//update current position etc.
				double curAmount = this.getCur(direction);
				this.setCur(direction, curAmount + detail.getAmount());
				this.setTravelPositive(direction, detail.getAmount() >= 0);
			}
			
			away.doTransform(coords);
		}
		
		super.playAnimation(id);
	}
	
	private class PivotInfo
	{
		private double max = 0;
		private double min = 0; //rotation limits in degrees
		
		private double cur = 0; //current rotations in degrees
		
		private boolean travelPositive = true; //current direction of rotation
		
		//TODO rotation speed limits?

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
			if (this.cur > this.max) this.cur = this.max;
		}

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
			if (this.cur < this.min) this.cur = this.min;
		}

		public double getCur() {
			return cur;
		}

		public void setCur(double cur) {
			if (cur < min) this.cur = min;
			else if (cur > max) this.cur = max;
			else this.cur = cur;
		}

		public boolean isTravelPositive() {
			return travelPositive;
		}

		public void setTravelPositive(boolean travelPositive) {
			this.travelPositive = travelPositive;
		}
		
	}
}
