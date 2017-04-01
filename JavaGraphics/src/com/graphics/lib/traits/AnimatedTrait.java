package com.graphics.lib.traits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.interfaces.IAnimatable;
import com.graphics.lib.skeleton.SkeletonNode;

public class AnimatedTrait extends OrientableTrait implements IAnimatable {
	private static final Map<String, Method> interceptors = new HashMap<>();
	
	static {
	    try{
	        interceptors.put("applytransforms", AnimatedTrait.class.getMethod("applyTransforms"));
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage());
	    }
	}
	
	private SkeletonNode skeletonRootNode;
	
	private List<String> activeAnimations = new ArrayList<>();
	
	public SkeletonNode getSkeletonRootNode() {
		return skeletonRootNode;
	}

	public void setSkeletonRootNode(SkeletonNode skeletonRootNode) {
		//include skeleton in vertex list so they get transformed along with the object mesh
		parent.getVertexList().removeIf(v -> v.hasTag(SkeletonNode.POS_TAG));
		
		if (skeletonRootNode != null){
			parent.getVertexList().addAll(skeletonRootNode.getAllNodePositions());
		}
		
		this.skeletonRootNode = skeletonRootNode;
	}

	@Override
	public void startAnimation(String key) {
		activeAnimations.add(key);
	}

	@Override
	public void stopAnimation(String key) {
		activeAnimations.remove(key);
	}
	
	@Override
    public Map<String, Method> getInterceptors() {
        return interceptors;
    }
	
	public void applyTransforms()
	{
		if (!activeAnimations.isEmpty() && skeletonRootNode != null && !parent.isDeleted() && parent.isVisible()){		
			this.toBaseOrientation();
			
			for(String animation : activeAnimations){
				skeletonRootNode.playAnimation(animation);
			}
			
			this.reapplyOrientation();
		}
	}

}
