package com.graphics.lib.transform;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.orientation.OrientationData;

public class ReapplyOrientationTransform extends Transform {

	private final OrientationData data;
	private List<Rotation> rotations;
	
	public ReapplyOrientationTransform(OrientationData data) {
		this.data = data;
	}
	
	@Override
	public void beforeTransform() {
		this.rotations = Lists.newArrayList(Axis.Z.getRotation(data.getzRot()),
											Axis.X.getRotation(data.getxRot()),
											Axis.Y.getRotation(data.getyRot()));
	}
	
	@Override
	public Consumer<Point> doTransformSpecific() {
		return p -> rotations.forEach(r -> r.doTransformSpecific().accept(p));
	}

	@Override
	public boolean isCompleteSpecific() {
		return false;
	}

}
