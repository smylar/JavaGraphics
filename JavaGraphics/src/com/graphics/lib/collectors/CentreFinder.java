package com.graphics.lib.collectors;

import java.util.function.Consumer;

import com.graphics.lib.Point;

public class CentreFinder implements Consumer<Point>{
	
	private Double maxX = null;
	private Double maxY = null;
	private Double maxZ = null;
	private Double minX = null;
	private Double minY = null;
	private Double minZ = null;

	@Override
	public void accept(Point p) {
		if (maxX == null || p.x > maxX) maxX = p.x;
		if (minX == null || p.x < minX) minX = p.x;
		if (maxY == null || p.y > maxY) maxY = p.y;
		if (minY == null || p.y < minY) minY = p.y;
		if (maxZ == null || p.z > maxZ) maxZ = p.z;
		if (minZ == null || p.z < minZ) minZ = p.z;
	}
	
	public void combine(CentreFinder other){
		if (maxX == null || other.maxX > maxX) maxX = other.maxX;
		if (minX == null || other.minX < minX) minX = other.minX;
		if (maxY == null || other.maxY > maxY) maxY = other.maxY;
		if (minY == null || other.minY < minY) minY = other.minY;
		if (maxZ == null || other.maxZ > maxZ) maxZ = other.maxZ;
		if (minZ == null || other.minZ < minZ) minZ = other.minZ;
	}
	
	public Point result(){
		//NULLS ??
		return new Point(minX + ((maxX - minX)/2), minY + ((maxY - minY)/2), minZ + ((maxZ - minZ)/2));
	}

}
