package com.graphics.lib.transform;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.orientation.OrientationData;

public class BaseOrientationTransform extends Transform {

	private final OrientationData data;
	private final IOrientation orientation;
	private List<Rotation> rotations;
	
	public BaseOrientationTransform(OrientationData data, IOrientation orientation) {
		this.data = data;
		this.orientation = orientation;
	}
	
	@Override
	public void beforeTransform() {
		data.saveCurrentTransforms(orientation);
		this.rotations = Lists.newArrayList(Axis.Y.getRotation(-data.getyRot()),
											Axis.X.getRotation(-data.getxRot()),
											Axis.Z.getRotation(-data.getzRot()));

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
