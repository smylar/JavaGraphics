package com.graphics.lib.skeleton;

import java.util.HashSet;
import java.util.Set;

import com.graphics.lib.WorldCoord;

public class SkeletonNode {
	public static final String POS_TAG = "SkeletonNodePosition";
	
	private WorldCoord position;
	
	private Set<WorldCoord> attachedMeshCoords = new HashSet<WorldCoord>();
	
	private Set<SkeletonNode> attachedNodes = new HashSet<SkeletonNode>();

	public WorldCoord getPosition() {
		return position;
	}

	public void setPosition(WorldCoord position) {
		position.setTag(POS_TAG);
		this.position = position;
	}

	public Set<WorldCoord> getAttachedMeshCoords() {
		return attachedMeshCoords;
	}

	public void setAttachedMeshCoords(Set<WorldCoord> attachedMeshCoords) {
		this.attachedMeshCoords = attachedMeshCoords;
	}

	public Set<SkeletonNode> getAttachedNodes() {
		return attachedNodes;
	}

	public void setAttachedNodes(Set<SkeletonNode> attachedNodes) {
		this.attachedNodes = attachedNodes;
	}
	
	public void addNode(SkeletonNode node){
		this.attachedNodes.add(node);
	}
	
	public Set<WorldCoord> getAllNodePositions(){
		Set<WorldCoord> positions = new HashSet<WorldCoord>();
		this.getAllNodePositions(positions);
		return positions;
	}
	
	protected void getAllNodePositions(Set<WorldCoord> positions){
		positions.add(getPosition());
		for (SkeletonNode node : attachedNodes)
		{
			node.getAllNodePositions(positions);
		}
	}
	
	/**
	 * Get all mesh coordinates attached to this node and all sub nodes, and the sub node positions themselves
	 * <br/>
	 * N.B. does not include the top level node itself
	 * 
	 * @return
	 */
	protected Set<WorldCoord> getAllSubCoords()
	{
		Set<WorldCoord> coords = new HashSet<WorldCoord>();
		getAllSubCoords(coords);
		return coords;
	}
	
	protected void getAllSubCoords(Set<WorldCoord> coords)
	{
		coords.addAll(this.attachedMeshCoords);
		for (SkeletonNode node : attachedNodes)
		{
			coords.add(node.getPosition());
			node.getAllSubCoords(coords);
		}
	}
	
	public void playAnimation(String id)
	{
		for (SkeletonNode node : attachedNodes)
		{
			node.playAnimation(id);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		return result;
	}	
}
