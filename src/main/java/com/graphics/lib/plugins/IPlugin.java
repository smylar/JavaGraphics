package com.graphics.lib.plugins;

@FunctionalInterface
public interface IPlugin<T,R> {
	public R execute(T obj);
}
