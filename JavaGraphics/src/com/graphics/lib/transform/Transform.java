package com.graphics.lib.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.graphics.lib.Point;

/**
 * Base class for performing transformations (rotation, translation etc.) on a set of coordinates.
 * 
 * @author Paul Brandon
 *
 */
public abstract class Transform {
	
	private List<Transform> dependencyList = new ArrayList<Transform>();
	private boolean completed = false;
	private boolean cancelled = false;
	private String name = "";
	protected boolean resetAfterComplete = false;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setResetAfterComplete(boolean resetAfterComplete) {
		this.resetAfterComplete = resetAfterComplete;
	}
	
	public void addDependency(Transform t)
	{
		//if transform has a dependency it will not be removed from the transform list if the dependency is still in the list
		dependencyList.add(t);
	}
	
	public List<Transform> getDependencyList() {
		return dependencyList;
	}
	
	public final void doTransform(List<? extends Point> points){
		this.beforeTransform();
		points.stream().forEach(p -> {
			this.doTransformSpecific().accept(p);
		});
		this.afterTransform();
	}
	
	public final boolean isComplete(){
		if (this.cancelled) return true;
		
		if (!this.completed)
		{
			this.completed = this.isCompleteSpecific();
			if (this.completed) this.onComplete();
		}
		return this.completed;
	}
	
	public boolean isCancelled()
	{
		return this.cancelled;
	}
	
	public void cancel()
	{
		this.completed = true;
		this.cancelled = true;
		this.resetAfterComplete = false;
	}
	
	protected void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public abstract Consumer<Point> doTransformSpecific();
	
	public abstract boolean isCompleteSpecific();
	
	public void beforeTransform() {}
	
	public void afterTransform() {}
	
	public void onComplete() {}
	
	public Transform actualTransformsApplied(){
		return this;
	}
}
