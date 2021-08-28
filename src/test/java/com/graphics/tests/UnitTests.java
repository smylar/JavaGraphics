package com.graphics.tests;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.util.TriangleAreaCalculator;

public class UnitTests {

	@Test
	public void testPointDistanceCalculation() {
		Point p = new Point(1,1,1);
		assertTrue(p.distanceTo(new Point(5,1,4)) == 5); //3,4,5 triangle
	}
	
	@Test
	public void testPointVectorCalculation() {
		Point p = new Point(1,1,1);
		Vector v = p.vectorToPoint(new Point(5,1,4));
		assertTrue(v.getX() == 4 && v.getY() == 0 && v.getZ() == 3); 
	}
	
	@Test
	public void testUnitVector() {
		Vector v1 = new Vector(0,0,1).getUnitVector(); //will be unchanged
		assertTrue(v1.getX() == 0 && v1.getY() == 0 && v1.getZ() == 1);
		
		v1 = new Vector(0,1,1).getUnitVector(); 
		DecimalFormat rounded = new DecimalFormat("#.###");
		assertTrue(v1.getX() == 0 && rounded.format(v1.getY()).equals("0.707") && rounded.format(v1.getZ()).equals("0.707"));
	}
	
	@Test
	public void testVectorDotProduct() {
		//the dot product between 2 normalised(unit) vectors gives the cosine of the angle between them
		Vector v1 = new Vector(0,0,1);
		assertTrue(Math.toDegrees(Math.acos(v1.dotProduct(v1))) == 0);
		
		assertTrue(Math.toDegrees(Math.acos(v1.dotProduct(new Vector(0,0,-1)))) == 180);
		
		assertTrue(Math.toDegrees(Math.acos(v1.dotProduct(new Vector(0,1,0)))) == 90);
		
		double value = Math.toDegrees(Math.acos(v1.dotProduct(new Vector(0,1,1).getUnitVector())));
		String roundedVal = new DecimalFormat("#.###").format(value); //due to double precision loss in calculating unit vector it's not exactly 45, maybe consider using BigDecimal - but we generally don't need that level of precision
		assertTrue(roundedVal.equals("45"));
	}
	
	@Test
	public void testVectorCrossProduct() {
		//gets perpendicular vector
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(0,1,0);
		
		Vector cross = v1.crossProduct(v2); 
		assertTrue(cross.getX() == -1 && cross.getY() == 0 && cross.getZ() == 0);
		
		cross = v2.crossProduct(v1);
		assertTrue(cross.getX() == 1 && cross.getY() == 0 && cross.getZ() == 0);
	}
	
	@Test
	public void testPointWithin() {
		Facet f = new Facet(new WorldCoord(0,0,0), new WorldCoord(0,5,0), new WorldCoord(5,0,0));
		
		assertTrue(f.isPointWithin(new Point(1,1,1)));
		assertTrue(f.isPointWithin(new Point(0,0,0)));
		assertFalse(f.isPointWithin(new Point(6,0,0)));
		assertFalse(f.isPointWithin(new Point(1,-1,1)));
	}
	
	@Test
	public void testYRotation() {
	    WorldCoord coord = new WorldCoord(1,2,3);
		ICanvasObject obj = new CanvasObject(() -> Pair.of(ImmutableList.of(coord), ImmutableList.of()));
		
		Rotation rot = new Rotation(Axis.Y, 90);
		obj.applyTransform(rot);
		DecimalFormat round = new DecimalFormat("#.###");
		
		assertTrue(round.format(coord.x).equals("3") && round.format(coord.y).equals("2") && round.format(coord.z).equals("-1"));
	}
	
	@Test
	public void testXRotation() {
	    WorldCoord coord = new WorldCoord(1,2,3);
		ICanvasObject obj = new CanvasObject(() -> Pair.of(ImmutableList.of(coord), ImmutableList.of()));
		
		Rotation rot = new Rotation(Axis.X, 90);
		obj.applyTransform(rot);
		DecimalFormat round = new DecimalFormat("#.###");
		assertTrue(round.format(coord.x).equals("1") && round.format(coord.y).equals("-3") && round.format(coord.z).equals("2"));
	}
	
	@Test
	public void testSameSpeedInterception() {
		CanvasObject target = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(2,2,0)), ImmutableList.of()));
		
		ICanvasObject proj = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(4,0,0)), ImmutableList.of()));
		
		MovementTransform move = new MovementTransform(new Vector(1,0,0), 1);
		target.addTransform(move);
		
		Vector intercept = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(target, proj.getCentre(), 1).getLeft();
		//assertEquals(new Vector(0,1,0),intercept); //damn precision loss!!
		DecimalFormat roundedFormat = new DecimalFormat("#.####");
		assertEquals(roundedFormat.format(intercept.getX()), "0"); 
		assertEquals(roundedFormat.format(intercept.getY()), "1"); 
		assertEquals(roundedFormat.format(intercept.getZ()), "0"); 
		
	}
	
	@Test
	public void testDifferentSpeedInterception() {
		CanvasObject target = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(0,0,0)), ImmutableList.of()));
		
		ICanvasObject proj = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(2,4,0)), ImmutableList.of()));
		
		MovementTransform move = new MovementTransform(new Vector(1,0,0), 1);
		target.addTransform(move);
		
		Vector intercept = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(target, proj.getCentre(), 2).getLeft();
		assertEquals(new Vector(0,-1,0),intercept);
		
	}
	
	@Test
	public void testCannotIntercept() {
		CanvasObject target = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(0,0,0)), ImmutableList.of()));
		
		ICanvasObject proj = new CanvasObject(() -> Pair.of(ImmutableList.of(new WorldCoord(4,0,0)), ImmutableList.of()));
		
		MovementTransform move = new MovementTransform(new Vector(1,0,0), 1);
		target.addTransform(move);
		
		Vector intercept = CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(target, proj.getCentre(), 1).getLeft();
		assertEquals(new Vector(-1,0,0),intercept);
		
	}
	
	@Test
	public void testIteratorRecursion() {
	    assertEquals(120, Utils.recurse(Lists.newArrayList(2,3,4,5), (next,prev) -> next*prev, 1), 0);
	}
	
	@Test
	public void testTriangleArea() {
	    assertEquals(6, TriangleAreaCalculator.getArea(new Point(0,0,0), new Point(0,3,0), new Point(4,0,0)), 0); //345 triangle
	}
}
