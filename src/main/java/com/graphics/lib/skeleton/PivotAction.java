package com.graphics.lib.skeleton;

import java.util.Collection;

@FunctionalInterface
public interface PivotAction {
	public Collection<PivotDetail> get(PivotSkeletonNode node);
}
