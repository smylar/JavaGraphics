package com.graphics.lib.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.graphics.lib.Point;

public class SequenceTransform extends Transform {

	private List<Transform> sequence = new ArrayList<Transform>();
	private int cnt = 0;
	private Transform applied;
	
	public void addTransform(Transform t)
	{
		sequence.add(t);
	}

	@Override
	public boolean isCompleteSpecific() {
		return cnt >= sequence.size();
	}

	@Override
	public void afterTransform() {
		applied.afterTransform();
		if (this.cnt < this.sequence.size() && applied.isCompleteSpecific()) this.cnt++;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		if (this.cnt >= this.sequence.size() && this.resetAfterComplete)
		{
			this.cnt = 0;
			this.setCompleted(false);
		}
		this.applied = null;
		
		if (this.cnt < this.sequence.size())
		{
			applied = this.sequence.get(cnt);
			
			applied.beforeTransform();
			return applied.doTransformSpecific();
		}
		return (p) -> {return;};
	}
	
	@Override
	public Transform actualTransformsApplied(){
		return applied == null ? null : applied.actualTransformsApplied();
	}
	
}
