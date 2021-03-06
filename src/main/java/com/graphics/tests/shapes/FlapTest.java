package com.graphics.tests.shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.graphics.lib.Axis;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.skeleton.PivotDetail;
import com.graphics.lib.skeleton.PivotSkeletonNode;
import com.graphics.lib.skeleton.SkeletonNode;
import com.graphics.lib.traits.AnimatedTrait;
import com.graphics.lib.traits.TraitHandler;

public class FlapTest extends Bird {
    private static final String TIP_RETRACT_ACTION = "TIP_RETRACT";
    private static final String TIP_EXTEND_ACTION = "TIP_EXTEND";
    private static final String FLAP_ACTION = "FLAP";
	private String tipAnimation = "";
	
	public FlapTest() {
		super();
		AnimatedTrait animatable = TraitHandler.INSTANCE.registerTrait(this, AnimatedTrait.class);
		animatable.setOrientation(new SimpleOrientation(AnimatedTrait.ORIENTATION_TAG));
		
		SkeletonNode rootNode = new SkeletonNode(new WorldCoord(0, 0, 30));
		
		PivotSkeletonNode rightWingPivot = new PivotSkeletonNode(new WorldCoord(1, 0, 30));
		rightWingPivot.setMin(Axis.Z, -75);
		rightWingPivot.setMax(Axis.Z, 10);
		rightWingPivot.setMin(Axis.X, -8);
		rightWingPivot.setMax(Axis.X, 12);
		
		Set<WorldCoord> meshCoords = Sets.newHashSet(getVertexList().get(2), 
                                                     getVertexList().get(3),
                                                     getVertexList().get(4));
		
		PivotSkeletonNode rightWingJointPivot = new PivotSkeletonNode(new WorldCoord(50, 0, 30), meshCoords);
		rightWingJointPivot.setMax(Axis.Z, 100);
		
		rootNode.addNode(rightWingPivot);
		rightWingPivot.addNode(rightWingJointPivot);
		
		rightWingJointPivot.getAnimations().put(TIP_RETRACT_ACTION, PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, true));
		rightWingJointPivot.getAnimations().put(TIP_EXTEND_ACTION, PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, false));
		
		PivotDetail wingPivot = new PivotDetail();
		wingPivot.setDirection(Axis.Z);

		PivotDetail xPivot = new PivotDetail();
		xPivot.setDirection(Axis.X);
		rightWingPivot.getAnimations().put(FLAP_ACTION, n ->{
			List<PivotDetail> p = new ArrayList<>();
			double xPivotMod = 0;
			double amt = 0;
			if (n.getCur(Axis.Z) >= n.getMax(Axis.Z) || !n.isTravelPositive(Axis.Z)){
				amt = -3;
				xPivotMod = 3;
				n.setTravelPositive(Axis.Z, false);
				if (n.getCur(Axis.Z) > n.getMin(Axis.Z) + 20){
					tipAnimation = TIP_RETRACT_ACTION;
				}
			}
			
			if (n.getCur(Axis.Z) > n.getMin(Axis.Z) && n.getCur(Axis.Z) < n.getMin(Axis.Z) + 20 && !n.isTravelPositive(Axis.Z)){
				tipAnimation = TIP_EXTEND_ACTION;
			}
			else if (n.getCur(Axis.Z) <= n.getMin(Axis.Z) || n.isTravelPositive(Axis.Z))
			{
				n.setTravelPositive(Axis.Z, true);
				if (rightWingJointPivot.getCur(Axis.Z) > rightWingJointPivot.getMin(Axis.Z))
				{
					tipAnimation = TIP_EXTEND_ACTION;
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
		
		PivotSkeletonNode leftWingPivot = new PivotSkeletonNode(new WorldCoord(-1, 0, 30));
		leftWingPivot.setMax(Axis.Z, 75);
		leftWingPivot.setMin(Axis.Z, -10);
		leftWingPivot.setMin(Axis.X, -12);
		leftWingPivot.setMax(Axis.X, 12);
		leftWingPivot.setTravelPositive(Axis.Z, false);
		
		Set<WorldCoord> leftMeshCoords = Sets.newHashSet(getVertexList().get(5), 
		                                             getVertexList().get(6),
		                                             getVertexList().get(7));
		        
		PivotSkeletonNode leftWingJointPivot = new PivotSkeletonNode(new WorldCoord(-50, 0, 30), leftMeshCoords);
		leftWingJointPivot.setMin(Axis.Z, -100);
		
		rootNode.addNode(leftWingPivot);
		leftWingPivot.addNode(leftWingJointPivot);
		
		leftWingJointPivot.getAnimations().put(TIP_RETRACT_ACTION, PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, false));
		leftWingJointPivot.getAnimations().put(TIP_EXTEND_ACTION, PivotSkeletonNode.getUniDirectionalPivotAction(Axis.Z, 6, true));

		leftWingPivot.getAnimations().put(FLAP_ACTION, n -> {
			List<PivotDetail> p = new ArrayList<>();
			
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
		
		animatable.setSkeletonRootNode(rootNode);
		animatable.startAnimation(FLAP_ACTION);
	}
}
