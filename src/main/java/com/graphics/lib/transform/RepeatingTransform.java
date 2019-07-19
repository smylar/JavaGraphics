package com.graphics.lib.transform;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.graphics.lib.Point;

public class RepeatingTransform<T extends Transform> extends Transform{
	T transform;
	int repeat = 0;
	int cnt = 0;
	Predicate<T> until;
	
	
	public RepeatingTransform(T transform, Predicate<T> until)
	{
		this.transform = transform;
		this.until = until;
	}
	
	public RepeatingTransform(T transform, int repeatFor)
	{
		this.transform = transform;
		this.repeat = repeatFor;
	}
	
	public T getTransform() {
		return transform;
	}

	@Override
	public boolean isCompleteSpecific() {
		if (until != null){
			return until.test(transform);
		}
		return cnt >=repeat && repeat > 0; 
	}

	@Override
	public void afterTransform() {
		transform.afterTransform();
		if (repeat > 0) cnt++;
	}
	
	@Override
	public void beforeTransform() {
		transform.beforeTransform();
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		if (this.isComplete() && this.resetAfterComplete){
			cnt = 0;
			this.setCompleted(false);
		}
		
		if (!this.isComplete()){		
			return transform.doTransformSpecific();
		}
		return p -> {};
	}
	
	@Override
	public Transform actualTransformsApplied(){
		return transform.actualTransformsApplied();
	}
}
