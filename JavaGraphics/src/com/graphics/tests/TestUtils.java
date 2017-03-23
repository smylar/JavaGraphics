package com.graphics.tests;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.graphics.lib.Utils;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.shader.ShaderFactory;
import com.sound.ClipLibrary;

public class TestUtils {
	public static final String SILENT_EXPLODE = "sexpl";
	
	public static BiConsumer<Canvas3D,Graphics> showMarkers(){
		return (c, g) -> {
			double pixelsPerDegree = (double)c.getHeight() / 180;
			int middleHeight = c.getHeight() / 2;
			g.setColor(new Color(0,0,0,150));
			g.fillRect(0, 0, 35, c.getHeight());
			g.setColor(Color.WHITE);
			double pitch = c.getCamera().getPitch();
			for (int i = 0 ; i < 181 ; i+=10){
				double marker = i - 90;
				double offset = marker + pitch;
				if (offset > 90 || offset < -90) continue;
				int pos = middleHeight - (int)Math.round((offset*pixelsPerDegree)); 
				g.drawLine(5,pos,30,pos);
				g.drawString(Double.toString(marker), 10, pos-1);
			}
			g.setColor(Color.RED);
			g.drawLine(5,middleHeight,30,middleHeight);
			
			
			pixelsPerDegree = (double)c.getWidth() / 180;
			int middleWidth = c.getWidth() / 2;
			g.setColor(new Color(0,0,0,150));
			g.fillRect(0, 0, c.getWidth(), 35);
			g.setColor(Color.WHITE);
			double bearing = c.getCamera().getBearing();
			int start = (int)((bearing - 90)/10) * 10;
			for (int i = 0 ; i < 181 ; i+=10){
				double marker = start + i;
				double offset = marker - bearing;
				int pos = middleWidth + (int)Math.round((offset*pixelsPerDegree)); 
				if (marker < -179){
					marker = 180 + (180 + marker);
				}
				else if (marker > 180){
					marker = -180 + (marker - 180);
				}
				g.drawLine(pos,5,pos,30);
				g.drawString(Double.toString(marker), pos+1, 20);
			}
			g.setColor(Color.RED);
			g.drawLine(middleWidth,5,middleWidth,30);
			
			g.setColor(Color.black);

			Utils.drawCircle(middleWidth, middleHeight, 20, new Color(255,0,0), new Color(0,255,0,0)).accept(c, g);
			
			if (c.getCamera() instanceof ViewAngleCamera){	
				g.drawString(""+((ViewAngleCamera)c.getCamera()).getViewAngle() , c.getWidth() - 30, c.getHeight() - 5);
			}
		};
	}
	
	public static ICanvasObjectList getFilteredObjectList(){
		return () -> {
			if (Canvas3D.get() == null) return new ArrayList<ICanvasObject>();
			return Canvas3D.get().getShapes().stream().filter(s -> s.isVisible() && !s.isDeleted() && !s.hasFlag("PHASED")).collect(Collectors.toList());
			};
	}
	
	public static IPlugin<IPlugable,Void> getExplodePlugin(ClipLibrary clipLibrary)
	{
		return new IPlugin<IPlugable,Void>(){
			@Override
			public Void execute(IPlugable obj) {
				
				//could be 2 hits at the same time
				synchronized(obj.getData()) {
					if (obj.isVisible()) {
						obj.setVisible(false);
					} else {
						return null;
					}
				}
				
				PluginLibrary.explode().execute(obj).forEach(c -> {
					Canvas3D.get().replaceShader(obj, ShaderFactory.FLAT);
					c.registerPlugin(Events.CHECK_COLLISION, getBouncePlugin(), true);
					if (!obj.hasFlag(SILENT_EXPLODE) && clipLibrary != null) clipLibrary.playSound("EXPLODE", -20f);
				});
				obj.registerSingleAfterDrawPlugin(Events.FLASH, PluginLibrary.flash(Canvas3D.get().getLightSources()));
				return null;
			}			
		};
	}
	
	public static IPlugin<IPlugable,Void> getBouncePlugin(){
		return new IPlugin<IPlugable,Void>(){
			@Override
			public Void execute(IPlugable obj) {	
				ICanvasObject impactee = PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(),null, null).execute(obj);
				if (impactee != null){
					if (impactee.hasFlag(Events.STICKY)){ 
						obj.cancelTransforms();
						//obj.observeAndMatch(impactee); 
						//TODO if already a child, object needs to stop being a child of that object, as observe and match also makes this fragment a child of impactee, get weird artifacts with the double processing
					}
					else {
						PluginLibrary.bounce(impactee).execute(obj);
					}
				}
				return null;
			}			
		};
	}
	
}
