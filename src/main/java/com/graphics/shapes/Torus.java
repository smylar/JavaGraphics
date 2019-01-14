package com.graphics.shapes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Translation;

public class Torus extends CanvasObject {
    private static final int DEFAULT_PROGRESSION = 18;
	private int points;

	public Torus (double tubeRadius, double holeRadius)
	{
		this(tubeRadius, holeRadius, DEFAULT_PROGRESSION);
	}
	
	public Torus (double tubeRadius, double holeRadius, int arcProgression)
	{
	    super(self -> {
	        int progression = 360 % arcProgression != 0 ? DEFAULT_PROGRESSION : arcProgression;
	        self.setPoints(360/progression);
	        return init(tubeRadius, holeRadius, progression, self.getPoints());
	    }, Torus.class);
	    
	    FunctionHandler.register(this, CanvasObjectFunctions.TORUS);
		
		this.addTransform(new Translation(-tubeRadius,-(holeRadius + tubeRadius*2),-(holeRadius + tubeRadius*2)));
		
		this.addTransform(Axis.Y.getRotation(90));
		this.applyTransforms();
		
		this.fixCentre();
		
		this.setVertexNormalFinder(getvnFinder());
	}
	
	public double getActualTubeRadius() {
		//scaling transforms may have been applied
		int index = points * points; //start of tube centre points
		WorldCoord tc = this.getVertexList().get(index);
		
		return tc.distanceTo(this.getVertexList().get(0));
	}
	
	public double getActualHoleRadius(){
		//scaling transforms may have been applied
		return this.getCentre().distanceTo(this.getVertexList().get(0)) - (getActualTubeRadius()*2);
	}
	
	public double getActualRadius(){
		//scaling transforms may have been applied
		return this.getCentre().distanceTo(this.getVertexList().get(0));
	}
	
	public Facet getHolePlane(){
		int startIndex = points * points;
		int incr = points / 3;
		return new Facet(this.getVertexList().get(startIndex), this.getVertexList().get(startIndex+incr), this.getVertexList().get(startIndex+incr+incr));
	}
	
	protected int getPoints() {
        return points;
    }

    protected void setPoints(int points) {
        this.points = points;
    }

    private IVertexNormalFinder getvnFinder()
	{
		return (obj, p, f) -> {
			if (!obj.getVertexList().contains(p)) return new Vector(0,0,-1);
			
			int index = (points * points) + Math.floorDiv(obj.getVertexList().indexOf(p), this.points);
			
			Point centre = obj.getVertexList().get(index);
			
			return centre.vectorToPoint(p).getUnitVector();
		};
	}
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init(double tubeRadius, double holeRadius, int arcProgression, int points) {
        
        List<WorldCoord> vertexList = Lists.newArrayList();
        ImmutableList.Builder<Facet> facets = ImmutableList.builder();
        
        List<Point> circle = new ArrayList<>();

        Point circleCentre = new Point(tubeRadius,holeRadius+(tubeRadius*2),tubeRadius);
        
        //make an initial circle 2d circle in x-z plane (y doesn't change)
        for (int i = 0 ; i < points ; i++){
            double angle = Math.toRadians(i*arcProgression);
            double x = tubeRadius - (tubeRadius*Math.sin(angle));
            double z = tubeRadius - (tubeRadius*Math.cos(angle));
            circle.add(new Point(x, holeRadius+(tubeRadius*2),z));
        }
        //note, this is a clockwise circle
        //now move circle in a circle in y plane around centre point of torus
        
        List<WorldCoord> circleCentres = new ArrayList<>();
        for (int i = 0 ; i < points ; i++){
            double angle = Math.toRadians(i*arcProgression);
            for (Point p : circle)
            {
                double radiustopoint = (holeRadius+(tubeRadius*2)) - p.z;
                double y = radiustopoint*Math.sin(angle);
                double z = radiustopoint - (radiustopoint*Math.cos(angle));
                vertexList.add(new WorldCoord(p.x, p.y-y, p.z+z));
            }
            
            //circle centre points - used for easier shading
            double radiustopoint = holeRadius + tubeRadius;
            double y = radiustopoint*Math.sin(angle);
            double z = radiustopoint - (radiustopoint*Math.cos(angle));
            circleCentres.add(new WorldCoord(circleCentre.x,circleCentre.y - y, circleCentre.z + z));           
        }
        vertexList.addAll(circleCentres);
        
        //join circles together (working out the triangles)
        for (int i = 0 ; i < points-1 ; i++){
        
            int a = i*points;
            int b = (points*i)+1;
            int c = points*(i+1);
            for (int j = 1 ; j < (points*2)-1 ; j++){
                if (j%2 == 0){
                    a++;
                    b += points;
                }else if (j>1){
                    c++;
                    b -= (points-1);
                }
                facets.add(new Facet(vertexList.get(c), vertexList.get(b), vertexList.get(a)));
            }
            //tie last points in circle to first points
            facets.add(new Facet(vertexList.get(points*(i+2)-1), vertexList.get(points*i), vertexList.get((points*(i+1))-1)));
            facets.add(new Facet(vertexList.get(points*(i+2)-1), vertexList.get(points*(i+1)), vertexList.get(points*i)));
        
        }
        //tie last circle to first
        int a = points -1;
        int b = (points*points) - 2;
        int c = (points*points) - 1;
        for (int j = 1 ; j < (points*2)-1 ; j++){
            if (j%2 == 0){
                a--;
                c = points - (int)(j*0.5);
            }else if (j>1){
                b--;
                c = (points*points)-(int)((j+1)*0.5);
            }

            facets.add(new Facet(vertexList.get(c), vertexList.get(b), vertexList.get(a)));
        }

        int len = points*points;
        facets.add(new Facet(vertexList.get(points-1), vertexList.get(0), vertexList.get(len-1)));
        facets.add(new Facet(vertexList.get(0), vertexList.get(len-points), vertexList.get(len-1)));
        
        return Pair.of(ImmutableList.copyOf(vertexList), facets.build());
	}
	
}
