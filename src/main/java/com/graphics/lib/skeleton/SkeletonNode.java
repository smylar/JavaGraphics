package com.graphics.lib.skeleton;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.graphics.lib.WorldCoord;

public class SkeletonNode {
	public static final String POS_TAG = "SkeletonNodePosition";
	
	private final WorldCoord position;
	
	private Set<WorldCoord> attachedMeshCoords = ImmutableSet.of();
	
	private Set<SkeletonNode> attachedNodes = new HashSet<>();

	public SkeletonNode(WorldCoord position) {
        position.addTag(POS_TAG);
        this.position = position;
    }
	
	public SkeletonNode(WorldCoord position, Collection<WorldCoord> meshCoords) {
        this(position);
        attachedMeshCoords = ImmutableSet.copyOf(meshCoords);
    }
	
	public WorldCoord getPosition() {
		return position;
	}

	public Set<WorldCoord> getAttachedMeshCoords() {
		return attachedMeshCoords;
	}

	public Set<SkeletonNode> getAttachedNodes() {
		return attachedNodes;
	}
	
	public void addNode(SkeletonNode node) {
		this.attachedNodes.add(node);
	}
	
	public Set<WorldCoord> getAllNodePositions() {
		Set<WorldCoord> positions = new HashSet<>();
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
		Set<WorldCoord> coords = new HashSet<>();
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
}
