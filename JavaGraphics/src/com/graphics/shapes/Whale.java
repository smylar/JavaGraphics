package com.graphics.shapes;

import java.util.stream.Collectors;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.AnimatedCanvasObject;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.skeleton.PivotSkeletonNode;

public class Whale extends AnimatedCanvasObject<Whale> {
	public static final String FIN_TAG = "fin";
	public static final String FLUKE_TAG = "fluke";
	
	//private int multiplier = 1;
	
	public Whale(){
		this.getVertexList().add(new WorldCoord(62, 40, 0));
		this.getVertexList().add(new WorldCoord(98, 40, 0));
		
		this.getVertexList().add(new WorldCoord(50, 0, 90));
		this.getVertexList().add(new WorldCoord(110, 0, 90));
		this.getVertexList().add(new WorldCoord(110, 100, 90));
		this.getVertexList().add(new WorldCoord(50, 100, 90));
		
		this.getVertexList().add(new WorldCoord(21,90, 98));
		this.getVertexList().add(new WorldCoord(2,65, 106));
		this.getVertexList().add(new WorldCoord(2,35, 106));
		this.getVertexList().add(new WorldCoord(21,10, 98));
		
		this.getVertexList().add(new WorldCoord(139,90, 98));
		this.getVertexList().add(new WorldCoord(158,65, 106));
		this.getVertexList().add(new WorldCoord(158,35, 106));
		this.getVertexList().add(new WorldCoord(139,10, 98));
		
		this.getVertexList().add(new WorldCoord(50, 0, 170));
		this.getVertexList().add(new WorldCoord(110, 0, 170));
		this.getVertexList().add(new WorldCoord(110, 90, 185));
		this.getVertexList().add(new WorldCoord(50, 90, 185));
		
		this.getVertexList().add(new WorldCoord(21,90, 170));
		this.getVertexList().add(new WorldCoord(2,65, 170));
		this.getVertexList().add(new WorldCoord(2,35, 170));
		this.getVertexList().add(new WorldCoord(21,10, 170));
		
		this.getVertexList().add(new WorldCoord(139,90, 170));
		this.getVertexList().add(new WorldCoord(158,65, 170));
		this.getVertexList().add(new WorldCoord(158,35, 170));
		this.getVertexList().add(new WorldCoord(139,10, 170));
		
		this.getVertexList().add(new WorldCoord(75, 40, 330, FLUKE_TAG));
		this.getVertexList().add(new WorldCoord(85, 40, 330, FLUKE_TAG));
		
		this.getVertexList().add(new WorldCoord(150, 40, 350, FLUKE_TAG));
		this.getVertexList().add(new WorldCoord(170, 39, 385, FLUKE_TAG));
		this.getVertexList().add(new WorldCoord(80, 39, 365, FLUKE_TAG));
		this.getVertexList().add(new WorldCoord(-10, 39, 385, FLUKE_TAG));
		this.getVertexList().add(new WorldCoord(10, 40, 350, FLUKE_TAG));
		
		this.getVertexList().add(new WorldCoord(80, 41, 365, FLUKE_TAG));
		
		this.getVertexList().add(new WorldCoord(158, 50, 120));
		this.getVertexList().add(new WorldCoord(158, 50, 170));
		this.getVertexList().add(new WorldCoord(240, 65, 180));
		
		this.getVertexList().add(new WorldCoord(2, 50, 120));
		this.getVertexList().add(new WorldCoord(2, 50, 170));
		this.getVertexList().add(new WorldCoord(-80, 65, 180));
		
		
		//snout ----------------------------------------
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(2)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(4), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(5), this.getVertexList().get(4)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(6), this.getVertexList().get(5)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(7), this.getVertexList().get(6)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(8), this.getVertexList().get(7)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(9), this.getVertexList().get(8)));
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(9)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(4), this.getVertexList().get(10)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(10), this.getVertexList().get(11)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(11), this.getVertexList().get(12)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(12), this.getVertexList().get(13)));
		this.getFacetList().add(new Facet(this.getVertexList().get(1), this.getVertexList().get(13), this.getVertexList().get(3)));
		
		//body -------------------------------------------
		
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(3), this.getVertexList().get(15)));
		this.getFacetList().add(new Facet(this.getVertexList().get(2), this.getVertexList().get(15), this.getVertexList().get(14)));
		this.getFacetList().add(new Facet(this.getVertexList().get(3), this.getVertexList().get(13), this.getVertexList().get(25)));
		this.getFacetList().add(new Facet(this.getVertexList().get(3), this.getVertexList().get(25), this.getVertexList().get(15)));
		this.getFacetList().add(new Facet(this.getVertexList().get(13), this.getVertexList().get(12), this.getVertexList().get(24)));
		this.getFacetList().add(new Facet(this.getVertexList().get(13), this.getVertexList().get(24), this.getVertexList().get(25)));
		this.getFacetList().add(new Facet(this.getVertexList().get(12), this.getVertexList().get(11), this.getVertexList().get(23)));
		this.getFacetList().add(new Facet(this.getVertexList().get(12), this.getVertexList().get(23), this.getVertexList().get(24)));
		this.getFacetList().add(new Facet(this.getVertexList().get(11), this.getVertexList().get(10), this.getVertexList().get(22)));
		this.getFacetList().add(new Facet(this.getVertexList().get(11), this.getVertexList().get(22), this.getVertexList().get(23)));
		this.getFacetList().add(new Facet(this.getVertexList().get(10), this.getVertexList().get(4), this.getVertexList().get(22)));
		this.getFacetList().add(new Facet(this.getVertexList().get(4), this.getVertexList().get(16), this.getVertexList().get(22)));
		this.getFacetList().add(new Facet(this.getVertexList().get(4), this.getVertexList().get(5), this.getVertexList().get(17)));
		this.getFacetList().add(new Facet(this.getVertexList().get(4), this.getVertexList().get(17), this.getVertexList().get(16)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(6), this.getVertexList().get(18)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(18), this.getVertexList().get(17)));
		this.getFacetList().add(new Facet(this.getVertexList().get(6), this.getVertexList().get(7), this.getVertexList().get(19)));
		this.getFacetList().add(new Facet(this.getVertexList().get(6), this.getVertexList().get(19), this.getVertexList().get(18)));
		this.getFacetList().add(new Facet(this.getVertexList().get(7), this.getVertexList().get(8), this.getVertexList().get(20)));
		this.getFacetList().add(new Facet(this.getVertexList().get(7), this.getVertexList().get(20), this.getVertexList().get(19)));
		this.getFacetList().add(new Facet(this.getVertexList().get(8), this.getVertexList().get(9), this.getVertexList().get(21)));
		this.getFacetList().add(new Facet(this.getVertexList().get(8), this.getVertexList().get(21), this.getVertexList().get(20)));
		this.getFacetList().add(new Facet(this.getVertexList().get(9), this.getVertexList().get(2), this.getVertexList().get(14)));
		this.getFacetList().add(new Facet(this.getVertexList().get(9), this.getVertexList().get(14), this.getVertexList().get(21)));
		
		//tail ----------------------------------------------
		this.getFacetList().add(new Facet(this.getVertexList().get(14), this.getVertexList().get(15), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(14), this.getVertexList().get(27), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(15), this.getVertexList().get(25), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(25), this.getVertexList().get(24), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(24), this.getVertexList().get(23), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(23), this.getVertexList().get(22), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(22), this.getVertexList().get(16), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(16), this.getVertexList().get(17), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(16), this.getVertexList().get(26), this.getVertexList().get(27)));
		this.getFacetList().add(new Facet(this.getVertexList().get(17), this.getVertexList().get(18), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(18), this.getVertexList().get(19), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(19), this.getVertexList().get(20), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(20), this.getVertexList().get(21), this.getVertexList().get(26)));
		this.getFacetList().add(new Facet(this.getVertexList().get(21), this.getVertexList().get(14), this.getVertexList().get(26)));
		
		// tailfin (fluke) -------------------------------------
		
			//top
		this.getFacetList().add(new Facet(this.getVertexList().get(27), this.getVertexList().get(28), this.getVertexList().get(29), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(27), this.getVertexList().get(29), this.getVertexList().get(30), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(32), this.getVertexList().get(26), this.getVertexList().get(30), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(31), this.getVertexList().get(32), this.getVertexList().get(30), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(26), this.getVertexList().get(27), this.getVertexList().get(30), FLUKE_TAG));
			//bottom
		this.getFacetList().add(new Facet(this.getVertexList().get(33), this.getVertexList().get(28), this.getVertexList().get(27), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(33), this.getVertexList().get(29), this.getVertexList().get(28), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(26), this.getVertexList().get(33), this.getVertexList().get(27), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(32), this.getVertexList().get(33), this.getVertexList().get(26), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(31), this.getVertexList().get(33), this.getVertexList().get(32), FLUKE_TAG));
			//infill
		this.getFacetList().add(new Facet(this.getVertexList().get(30), this.getVertexList().get(33), this.getVertexList().get(31), FLUKE_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(30), this.getVertexList().get(29), this.getVertexList().get(33), FLUKE_TAG));
		
		//fins -------------------------------------------------
		this.getFacetList().add(new Facet(this.getVertexList().get(34), this.getVertexList().get(36), this.getVertexList().get(35), FIN_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(34), this.getVertexList().get(35), this.getVertexList().get(36), FIN_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(37), this.getVertexList().get(38), this.getVertexList().get(39), FIN_TAG));
		this.getFacetList().add(new Facet(this.getVertexList().get(37), this.getVertexList().get(39), this.getVertexList().get(38), FIN_TAG));
		
		//TODO - an interface for building all this?!?!?
		
		this.UseAveragedNormals(90);
		this.setOrientation(new SimpleOrientation(ORIENTATION_TAG));
		this.getOrientation().getRepresentation().getVertexList().get(0).z = -1;
		
		PivotSkeletonNode rootNode = new PivotSkeletonNode();
		rootNode.setPosition(new WorldCoord(this.getCentre()));
		
		PivotSkeletonNode tailNode = new PivotSkeletonNode();
		tailNode.setPosition(new WorldCoord(this.getVertexList().get(26)));
		tailNode.getAttachedMeshCoords().addAll(this.getVertexList().stream().filter(v -> FLUKE_TAG.equals(v.getGroup())).collect(Collectors.toSet()));
		
		rootNode.addNode(tailNode);
		rootNode.setMax(Axis.X,20);
		rootNode.setMin(Axis.X,-10);
		
		rootNode.getAnimations().put("TAIL",  PivotSkeletonNode.getDefaultPivotAction(Axis.X, 1, 1));
		
		tailNode.setMax(Axis.Y,10);
		tailNode.setMin(Axis.Y,-10);
		tailNode.getAnimations().put("TAIL",  PivotSkeletonNode.getDefaultPivotAction(Axis.Y, 0.5, 0.5));
		
		this.setSkeletonRootNode(rootNode);
		this.startAnimation("TAIL");
	}
	
	@Override
	public boolean isPointInside(Point p)
	{
		//is as overridden method, but we need to ignore the fins and fluke as it messes it up
		//TODO also there is an issue with this if we hit the whale low down in front of the tail and the tail is in or near the fully down position
		
		if (this.getBaseObject() != this) return this.getBaseObject().isPointInside(p);
		for (Facet f : getFacetList())
		{
			if (FIN_TAG.equals(f.getTag()) || FLUKE_TAG.equals(f.getTag())) continue;
			
			Vector vecPointToFacet = p.vectorToPoint(f.point1).getUnitVector();
			double deg = Math.toDegrees(Math.acos(vecPointToFacet.dotProduct(f.getNormal())));
			if (deg >= 90) return false;
		}
		return true;
	}
}
