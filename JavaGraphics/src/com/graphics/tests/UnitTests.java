package com.graphics.tests;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.junit.Test;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;

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
		assertTrue(v.x == 4 && v.y == 0 && v.z == 3); 
	}
	
	@Test
	public void testUnitVector() {
		Vector v1 = new Vector(0,0,1).getUnitVector(); //will be unchanged
		assertTrue(v1.x == 0 && v1.y == 0 && v1.z == 1);
		
		v1 = new Vector(0,1,1).getUnitVector(); 
		DecimalFormat rounded = new DecimalFormat("#.###");
		assertTrue(v1.x == 0 && rounded.format(v1.y).equals("0.707") && rounded.format(v1.z).equals("0.707"));
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
		assertTrue(cross.x == -1 && cross.y == 0 && cross.z == 0);
		
		cross = v2.crossProduct(v1);
		assertTrue(cross.x == 1 && cross.y == 0 && cross.z == 0);
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
		CanvasObject obj = new CanvasObject();
		WorldCoord coord = new WorldCoord(1,2,3);
		obj.getVertexList().add(coord);
		
		Rotation<YRotation> rot = new Rotation<YRotation>(YRotation.class, 90);
		obj.applyTransform(rot);
		DecimalFormat round = new DecimalFormat("#.###");
		
		assertTrue(round.format(coord.x).equals("3") && round.format(coord.y).equals("2") && round.format(coord.z).equals("-1"));
	}
	
	@Test
	public void testXRotation() {
		CanvasObject obj = new CanvasObject();
		WorldCoord coord = new WorldCoord(1,2,3);
		obj.getVertexList().add(coord);
		
		Rotation<XRotation> rot = new Rotation<XRotation>(XRotation.class, 90);
		obj.applyTransform(rot);
		DecimalFormat round = new DecimalFormat("#.###");
		assertTrue(round.format(coord.x).equals("1") && round.format(coord.y).equals("-3") && round.format(coord.z).equals("2"));
	}
}
