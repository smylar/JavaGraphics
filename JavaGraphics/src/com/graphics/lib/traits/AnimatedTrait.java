package com.graphics.lib.traits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.interfaces.IAnimatable;
import com.graphics.lib.skeleton.SkeletonNode;
import com.graphics.lib.transform.Transform;

/**
 * Handles animation of an object by intercepting the applyTransforms call to the parent object
 * and applying further transforms.<br/>
 * Animation adds a skeleton, a skeleton consists of points (nodes) that can be attached to other nodes (forming a bone)
 * and to points belonging to the parent object, so that when that node moves, the attached nodes and mesh moves with it.<br/>
 * To play an animation, the node tree will be traversed and adjustments made as per the animation definition of that node
 * 
 * @author paul.brandon
 *
 */
public class AnimatedTrait extends OrientableTrait implements IAnimatable, Observer {
	private static final Map<String, Method> interceptors = new HashMap<>();
	
	static {
	    try{
	        interceptors.put("applytransforms", AnimatedTrait.class.getMethod("applyTransforms"));
	    } catch (Exception ex) {}
	}
	
	private SkeletonNode skeletonRootNode;
	
	private List<String> activeAnimations = new ArrayList<>();
	
	public SkeletonNode getSkeletonRootNode() {
		return skeletonRootNode;
	}

	/**
	 * Set the skeleton root node (start of the node tree), this add the Skeleton nodes to the object vertex list
	 * so that transforms applied to the object also apply to skeleton. Any existing skeleton nodes in the vertex list 
	 * will be removed first
	 * 
	 * @param skeletonRootNode
	 */
	public void setSkeletonRootNode(SkeletonNode skeletonRootNode) {
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
	
	/**
	 * Apply animation transforms
	 */
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
	
	@Override
	public void update(Observable obs, Object args) {
		super.update(obs, args);
		if (Objects.nonNull(skeletonRootNode) && args instanceof Transform) {
			((Transform)args).replay(skeletonRootNode.getAllNodePositions());
		}
		
	}

}
