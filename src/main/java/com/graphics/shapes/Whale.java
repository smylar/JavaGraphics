package com.graphics.shapes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctionsImpl;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.skeleton.PivotSkeletonNode;
import com.graphics.lib.traits.AnimatedTrait;
import com.graphics.lib.traits.TraitHandler;

public final class Whale extends CanvasObject {
	public static final String FIN_TAG = "fin";
	public static final String FLUKE_TAG = "fluke";
	
	public Whale(){
	    super(Whale::init);
	    FunctionHandler.register(this, getFunctionsImpl());
		
		this.useAveragedNormals(90);
		
		PivotSkeletonNode rootNode = new PivotSkeletonNode(new WorldCoord(this.getCentre()));
		
		Set<WorldCoord> tailMeshCoords = getVertexList().stream().filter(v -> FLUKE_TAG.equals(v.getGroup())).collect(Collectors.toSet());
		PivotSkeletonNode tailNode = new PivotSkeletonNode(new WorldCoord(this.getVertexList().get(26)), tailMeshCoords);
		
		rootNode.addNode(tailNode);
		rootNode.setMax(Axis.X,20);
		rootNode.setMin(Axis.X,-10);
		
		rootNode.getAnimations().put("TAIL",  PivotSkeletonNode.getDefaultPivotAction(Axis.X, 1, 1));
		
		tailNode.setMax(Axis.Y,10);
		tailNode.setMin(Axis.Y,-10);
		tailNode.getAnimations().put("TAIL",  PivotSkeletonNode.getDefaultPivotAction(Axis.Y, 0.5, 0.5));
		
		AnimatedTrait animated = TraitHandler.INSTANCE.registerTrait(this, AnimatedTrait.class);
		
		animated.setOrientation(new SimpleOrientation(AnimatedTrait.ORIENTATION_TAG));
		animated.getOrientation().getRepresentation().getVertexList().get(0).z = -1;
		animated.setSkeletonRootNode(rootNode);
		animated.startAnimation("TAIL");
	}
	
	private static CanvasObjectFunctionsImpl getFunctionsImpl() {
		return new CanvasObjectFunctionsImpl() {
			@Override
			public boolean isPointInside(ICanvasObject obj, Point p)
			{	
				return obj.getFacetList().parallelStream()
						.filter(f -> !(FIN_TAG.equals(f.getTag()) || FLUKE_TAG.equals(f.getTag())))
						.allMatch(f -> {
							Vector vecPointToFacet = p.vectorToPoint(f.getAsList().get(0)).getUnitVector();
							return Math.toDegrees(Math.acos(vecPointToFacet.dotProduct(f.getNormal()))) < 90;
						});
			}
		};
	}
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init() {
		ImmutableList<WorldCoord> vertexList = generateVertexList();
		return Pair.of(vertexList, generateFacetList(vertexList));
	}
	
	private static ImmutableList<WorldCoord> generateVertexList() {
		Builder<WorldCoord> vertexList = ImmutableList.builder();
		
		vertexList.add(new WorldCoord(62, 40, 0));
		vertexList.add(new WorldCoord(98, 40, 0));
		
		vertexList.add(new WorldCoord(50, 0, 90));
		vertexList.add(new WorldCoord(110, 0, 90));
		vertexList.add(new WorldCoord(110, 100, 90));
		vertexList.add(new WorldCoord(50, 100, 90));
		
		vertexList.add(new WorldCoord(21,90, 98));
		vertexList.add(new WorldCoord(2,65, 106));
		vertexList.add(new WorldCoord(2,35, 106));
		vertexList.add(new WorldCoord(21,10, 98));
		
		vertexList.add(new WorldCoord(139,90, 98));
		vertexList.add(new WorldCoord(158,65, 106));
		vertexList.add(new WorldCoord(158,35, 106));
		vertexList.add(new WorldCoord(139,10, 98));
		
		vertexList.add(new WorldCoord(50, 0, 170));
		vertexList.add(new WorldCoord(110, 0, 170));
		vertexList.add(new WorldCoord(110, 90, 185));
		vertexList.add(new WorldCoord(50, 90, 185));
		
		vertexList.add(new WorldCoord(21,90, 170));
		vertexList.add(new WorldCoord(2,65, 170));
		vertexList.add(new WorldCoord(2,35, 170));
		vertexList.add(new WorldCoord(21,10, 170));
		
		vertexList.add(new WorldCoord(139,90, 170));
		vertexList.add(new WorldCoord(158,65, 170));
		vertexList.add(new WorldCoord(158,35, 170));
		vertexList.add(new WorldCoord(139,10, 170));
		
		vertexList.add(new WorldCoord(75, 40, 330, FLUKE_TAG));
		vertexList.add(new WorldCoord(85, 40, 330, FLUKE_TAG));
		
		vertexList.add(new WorldCoord(150, 40, 350, FLUKE_TAG));
		vertexList.add(new WorldCoord(170, 39, 385, FLUKE_TAG));
		vertexList.add(new WorldCoord(80, 39, 365, FLUKE_TAG));
		vertexList.add(new WorldCoord(-10, 39, 385, FLUKE_TAG));
		vertexList.add(new WorldCoord(10, 40, 350, FLUKE_TAG));
		
		vertexList.add(new WorldCoord(80, 41, 365, FLUKE_TAG));
		
		vertexList.add(new WorldCoord(158, 50, 120));
		vertexList.add(new WorldCoord(158, 50, 170));
		vertexList.add(new WorldCoord(240, 65, 180));
		
		vertexList.add(new WorldCoord(2, 50, 120));
		vertexList.add(new WorldCoord(2, 50, 170));
		vertexList.add(new WorldCoord(-80, 65, 180));
		
		return vertexList.build();
	}
	
	private static ImmutableList<Facet> generateFacetList(List<WorldCoord> vertexList) {
		
		Builder<Facet> facetList = ImmutableList.builder();
		//snout ----------------------------------------
		facetList.add(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(3)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(2)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(4), vertexList.get(1)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(5), vertexList.get(4)));
		
		facetList.add(new Facet(vertexList.get(0), vertexList.get(6), vertexList.get(5)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(7), vertexList.get(6)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(8), vertexList.get(7)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(9), vertexList.get(8)));
		facetList.add(new Facet(vertexList.get(0), vertexList.get(2), vertexList.get(9)));
		
		facetList.add(new Facet(vertexList.get(1), vertexList.get(4), vertexList.get(10)));
		facetList.add(new Facet(vertexList.get(1), vertexList.get(10), vertexList.get(11)));
		facetList.add(new Facet(vertexList.get(1), vertexList.get(11), vertexList.get(12)));
		facetList.add(new Facet(vertexList.get(1), vertexList.get(12), vertexList.get(13)));
		facetList.add(new Facet(vertexList.get(1), vertexList.get(13), vertexList.get(3)));
		
		//body -------------------------------------------
		
		facetList.add(new Facet(vertexList.get(2), vertexList.get(3), vertexList.get(15)));
		facetList.add(new Facet(vertexList.get(2), vertexList.get(15), vertexList.get(14)));
		facetList.add(new Facet(vertexList.get(3), vertexList.get(13), vertexList.get(25)));
		facetList.add(new Facet(vertexList.get(3), vertexList.get(25), vertexList.get(15)));
		facetList.add(new Facet(vertexList.get(13), vertexList.get(12), vertexList.get(24)));
		facetList.add(new Facet(vertexList.get(13), vertexList.get(24), vertexList.get(25)));
		facetList.add(new Facet(vertexList.get(12), vertexList.get(11), vertexList.get(23)));
		facetList.add(new Facet(vertexList.get(12), vertexList.get(23), vertexList.get(24)));
		facetList.add(new Facet(vertexList.get(11), vertexList.get(10), vertexList.get(22)));
		facetList.add(new Facet(vertexList.get(11), vertexList.get(22), vertexList.get(23)));
		facetList.add(new Facet(vertexList.get(10), vertexList.get(4), vertexList.get(22)));
		facetList.add(new Facet(vertexList.get(4), vertexList.get(16), vertexList.get(22)));
		facetList.add(new Facet(vertexList.get(4), vertexList.get(5), vertexList.get(17)));
		facetList.add(new Facet(vertexList.get(4), vertexList.get(17), vertexList.get(16)));
		facetList.add(new Facet(vertexList.get(5), vertexList.get(6), vertexList.get(18)));
		facetList.add(new Facet(vertexList.get(5), vertexList.get(18), vertexList.get(17)));
		facetList.add(new Facet(vertexList.get(6), vertexList.get(7), vertexList.get(19)));
		facetList.add(new Facet(vertexList.get(6), vertexList.get(19), vertexList.get(18)));
		facetList.add(new Facet(vertexList.get(7), vertexList.get(8), vertexList.get(20)));
		facetList.add(new Facet(vertexList.get(7), vertexList.get(20), vertexList.get(19)));
		facetList.add(new Facet(vertexList.get(8), vertexList.get(9), vertexList.get(21)));
		facetList.add(new Facet(vertexList.get(8), vertexList.get(21), vertexList.get(20)));
		facetList.add(new Facet(vertexList.get(9), vertexList.get(2), vertexList.get(14)));
		facetList.add(new Facet(vertexList.get(9), vertexList.get(14), vertexList.get(21)));
		
		//tail ----------------------------------------------
		facetList.add(new Facet(vertexList.get(14), vertexList.get(15), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(14), vertexList.get(27), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(15), vertexList.get(25), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(25), vertexList.get(24), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(24), vertexList.get(23), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(23), vertexList.get(22), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(22), vertexList.get(16), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(16), vertexList.get(17), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(16), vertexList.get(26), vertexList.get(27)));
		facetList.add(new Facet(vertexList.get(17), vertexList.get(18), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(18), vertexList.get(19), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(19), vertexList.get(20), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(20), vertexList.get(21), vertexList.get(26)));
		facetList.add(new Facet(vertexList.get(21), vertexList.get(14), vertexList.get(26)));
		
		// tailfin (fluke) -------------------------------------
		
			//top
		facetList.add(new Facet(vertexList.get(27), vertexList.get(28), vertexList.get(29), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(27), vertexList.get(29), vertexList.get(30), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(32), vertexList.get(26), vertexList.get(30), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(31), vertexList.get(32), vertexList.get(30), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(26), vertexList.get(27), vertexList.get(30), FLUKE_TAG));
			//bottom
		facetList.add(new Facet(vertexList.get(33), vertexList.get(28), vertexList.get(27), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(33), vertexList.get(29), vertexList.get(28), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(26), vertexList.get(33), vertexList.get(27), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(32), vertexList.get(33), vertexList.get(26), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(31), vertexList.get(33), vertexList.get(32), FLUKE_TAG));
			//infill
		facetList.add(new Facet(vertexList.get(30), vertexList.get(33), vertexList.get(31), FLUKE_TAG));
		facetList.add(new Facet(vertexList.get(30), vertexList.get(29), vertexList.get(33), FLUKE_TAG));
		
		//fins -------------------------------------------------
		facetList.add(new Facet(vertexList.get(34), vertexList.get(36), vertexList.get(35), FIN_TAG));
		facetList.add(new Facet(vertexList.get(34), vertexList.get(35), vertexList.get(36), FIN_TAG));
		facetList.add(new Facet(vertexList.get(37), vertexList.get(38), vertexList.get(39), FIN_TAG));
		facetList.add(new Facet(vertexList.get(37), vertexList.get(39), vertexList.get(38), FIN_TAG));
		
		//TODO - an interface for building all this?!?!?
		return facetList.build();
	}
}
