package com.graphics.lib.traits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.plugins.IPlugin;

/**
 * Wrapper for a canvas object providing plugin functionality, i.e. Anonymous functions can be registered with the object 
 * to be triggered at the end of each draw cycle, or in response to other events
 * 
 * @author Paul Brandon
 *
 * @param <T> Type of the CanvasObject being wrapped
 */
public class PlugableTrait implements IPlugable {
    private static final Map<String, Method> interceptors = new HashMap<>();
	private Map<String,IPlugin<IPlugable,?>> plugins = new HashMap<>();
	private List<String> afterDrawPlugins = new ArrayList<>();
	private List<String> singleAfterDrawPlugins = new ArrayList<>();
	private ICanvasObject parent;
	    
    static {
        try {
            interceptors.put("ondrawcomplete", PlugableTrait.class.getMethod("onDrawComplete"));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
	
	public void onDrawComplete()
	{	
		List<String> pluginList = new ArrayList<>(this.afterDrawPlugins);
		for (String key : pluginList)
		{
			this.executePlugin(key);
		}
		
		pluginList = new ArrayList<>(this.singleAfterDrawPlugins);
		for (String key : pluginList)
		{
			this.executePlugin(key);
		}
		this.singleAfterDrawPlugins.clear();
	}
	
	@Override
	public IPlugable registerSingleAfterDrawPlugin(String key, IPlugin<IPlugable,?> plugin)
	{
		plugins.put(key, plugin);
		singleAfterDrawPlugins.add(key);
		return this;
	}
	
	@Override
	public IPlugable registerPlugin(String key, IPlugin<IPlugable,?> plugin, boolean doAfterDraw)
	{
		plugins.put(key, plugin);
		if (doAfterDraw) {
		    afterDrawPlugins.add(key);
		}
		
		return this;
	}
	
	@Override
	public void removePlugin(String key)
	{
		this.plugins.remove(key);
		this.afterDrawPlugins.remove(key);
		this.singleAfterDrawPlugins.remove(key);
	}
	
	@Override
	public void removePlugins()
	{
		this.plugins.clear();
		this.afterDrawPlugins.clear();
		this.singleAfterDrawPlugins.clear();
	}
	
	@Override
	public Object executePlugin(String key)
	{
		if (this.plugins.containsKey(key) && !parent.isDeleted())
		{
			return this.plugins.get(key).execute(this);
		}
		return null;
	}

    @Override
    public void setParent(ICanvasObject parent) {
        this.parent = parent;
    }

    @Override
    public ICanvasObject getParent() {
        return this.parent;
    }
    
    @Override
    public Map<String, Method> getInterceptors() {
        return interceptors;
    }
}