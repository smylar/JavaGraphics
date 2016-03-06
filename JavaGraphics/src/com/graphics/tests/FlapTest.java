package com.graphics.tests;

import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.Axis;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.AnimatedCanvasObject;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.skeleton.PivotDetail;
import com.graphics.lib.skeleton.PivotSkeletonNode;
import com.graphics.lib.skeleton.SkeletonNode;

public class FlapTest extends AnimatedCanvasObject<Bird> {
	private String tipAnimation = "";
	
	public FlapTest(){
		super(new Bird());
		this.setOrientation(new SimpleOrientation(ORIENTATION_TAG));
		
		SkeletonNode rootNode = new SkeletonNode();
		rootNode.setPosition(new WorldCoord(0, 0, 30));
		
		PivotSkeletonNode rightWingPivot = new PivotSkeletonNode();
		rightWingPivot.setPosition(new WorldCoord(1, 0, 30));
		rightWingPivot.setMin(Axis.Z, -75);
		rightWingPivot.setMax(Axis.Z, 10);
		rightWingPivot.setMin(Axis.X, -8);
		rightWingPivot.setMax(Axis.X, 12);
		
		PivotSkeletonNode rightWingJointPivot = new PivotSkeletonNode();
		rightWingJointPivot.setPosition(new WorldCoord(50, 0, 30));
		rightWingJointPivot.setMax(Axis.Z, 100);
		
		rightWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(2));
		rightWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(3));
		rightWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(4));
		
		rootNode.addNode(rightWingPivot);
		rightWingPivot.addNode(rightWingJointPivot);
		
		rightWingJointPivot.getAnimations().put("TIP_RETRACT", PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, true));
		rightWingJointPivot.getAnimations().put("TIP_EXTEND", PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, false));
		
		PivotDetail wingPivot = new PivotDetail();
		wingPivot.setDirection(Axis.Z);

		PivotDetail xPivot = new PivotDetail();
		xPivot.setDirection(Axis.X);
		rightWingPivot.getAnimations().put("FLAP", (n) ->{
			List<PivotDetail> p = new ArrayList<PivotDetail>();
			double xPivotMod = 0;
			double amt = 0;
			if (n.getCur(Axis.Z) >= n.getMax(Axis.Z) || !n.isTravelPositive(Axis.Z)){
				amt = -3;
				xPivotMod = 3;
				n.setTravelPositive(Axis.Z, false);
				if (n.getCur(Axis.Z) > n.getMin(Axis.Z) + 20){
					tipAnimation = "TIP_RETRACT";
				}
			}
			
			if (n.getCur(Axis.Z) > n.getMin(Axis.Z) && n.getCur(Axis.Z) < n.getMin(Axis.Z) + 20 && !n.isTravelPositive(Axis.Z)){
				tipAnimation = "TIP_EXTEND";
			}
			else if (n.getCur(Axis.Z) <= n.getMin(Axis.Z) || n.isTravelPositive(Axis.Z))
			{
				n.setTravelPositive(Axis.Z, true);
				if (rightWingJointPivot.getCur(Axis.Z) > rightWingJointPivot.getMin(Axis.Z))
				{
					tipAnimation = "TIP_EXTEND";
				}else{
					amt = 2;
					xPivotMod = -3;
				}
			}
			
			double xCur = n.getCur(Axis.X);
			xPivot.setAmount(-xCur);
			n.doMotionNow(xPivot); //reset wing to straight
			xPivot.setAmount(xCur + xPivotMod);
			
			n.playAnimation(tipAnimation);
			wingPivot.setAmount(amt);
			p.add(wingPivot);
			p.add(xPivot); //reapply x rotation - may need better system for complex/composite movement
			return p;
		});
		
		PivotSkeletonNode leftWingPivot = new PivotSkeletonNode();
		leftWingPivot.setPosition(new WorldCoord(-1, 0, 30));
		leftWingPivot.setMax(Axis.Z, 75);
		leftWingPivot.setMin(Axis.Z, -10);
		leftWingPivot.setMin(Axis.X, -12);
		leftWingPivot.setMax(Axis.X, 12);
		leftWingPivot.setTravelPositive(Axis.Z, false);
		
		PivotSkeletonNode leftWingJointPivot = new PivotSkeletonNode();
		leftWingJointPivot.setPosition(new WorldCoord(-50, 0, 30));
		leftWingJointPivot.setMin(Axis.Z, -100);
		
		leftWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(5));
		leftWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(6));
		leftWingJointPivot.getAttachedMeshCoords().add(this.getVertexList().get(7));
		
		rootNode.addNode(leftWingPivot);
		leftWingPivot.addNode(leftWingJointPivot);
		
		leftWingJointPivot.getAnimations().put("TIP_RETRACT", PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, false));
		leftWingJointPivot.getAnimations().put("TIP_EXTEND", PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, true));

		leftWingPivot.getAnimations().put("FLAP", (n) ->{
			List<PivotDetail> p = new ArrayList<PivotDetail>();
			
			double xCur = n.getCur(Axis.X);
			double xP = xPivot.getAmount();
			xPivot.setAmount(-xCur);
			n.doMotionNow(xPivot); //reset wing to straight
			xPivot.setAmount(xP);
			
			wingPivot.setAmount(-wingPivot.getAmount());
			n.playAnimation(tipAnimation);
			p.add(wingPivot);
			p.add(xPivot);
			return p;
		});
		
		this.setSkeletonRootNode(rootNode);
		this.startAnimation("FLAP");
	}
}
