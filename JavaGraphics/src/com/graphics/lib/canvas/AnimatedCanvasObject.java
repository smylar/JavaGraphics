package com.graphics.lib.canvas;

import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.interfaces.IAnimatable;
import com.graphics.lib.skeleton.SkeletonNode;

public class AnimatedCanvasObject<T extends CanvasObject> extends OrientableCanvasObject<T> implements IAnimatable{
	private SkeletonNode skeletonRootNode;
	
	private List<String> activeAnimations = new ArrayList<String>();
	
	public AnimatedCanvasObject()
	{
		super();
	}
	
	public AnimatedCanvasObject(T obj)
	{
		super(obj);
	}
	
	public SkeletonNode getSkeletonRootNode() {
		return skeletonRootNode;
	}

	public void setSkeletonRootNode(SkeletonNode skeletonRootNode) {
		//include skeleton in vertex list so they get transformed along with the object mesh
		this.getVertexList().removeIf(v -> v.hasTag(SkeletonNode.POS_TAG));
		
		if (skeletonRootNode != null){
			this.getVertexList().addAll(skeletonRootNode.getAllNodePositions());
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
	public void afterTransforms()
	{
		if (activeAnimations.size() > 0 && skeletonRootNode != null && !isDeleted() && isVisible()){		
			this.toBaseOrientation();
			
			for(String animation : activeAnimations){
				skeletonRootNode.playAnimation(animation);
			}
			
			this.reapplyOrientation();
		}
		
		if (getWrappedObject() != null) getWrappedObject().afterTransforms();
		else 
			super.afterTransforms();
	}

}
