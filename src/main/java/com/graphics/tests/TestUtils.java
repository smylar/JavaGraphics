package com.graphics.tests;

import java.awt.Color;
import java.util.Optional;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.effects.DrawAction;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.ExplosionSettings;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.sound.ClipLibrary;

public class TestUtils {
	public static final String SILENT_EXPLODE = "sexpl";
	
	public static DrawAction showMarkers() {
		return (c, g) -> {
			double pixelsPerDegree = (double)c.getHeight() / 180;
			int middleHeight = c.getHeight() / 2;
			g.setColor(new Color(0,0,0,150));
			g.fillRect(0, 0, 35, c.getHeight());
			g.setColor(Color.WHITE);
			double pitch = c.getCamera().getPitch();
			for (int i = 0 ; i < 181 ; i+=10) {
				double marker = i - 90;
				double offset = marker + pitch;
				if (offset > 90 || offset < -90) continue;
				int pos = middleHeight - (int)Math.round(offset*pixelsPerDegree); 
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
			for (int i = 0 ; i < 181 ; i+=10) {
				double marker = start + i;
				double offset = marker - bearing;
				int pos = middleWidth + (int)Math.round(offset*pixelsPerDegree); 
				if (marker < -179){
					marker = 180 + (180 + marker);
				}
				else if (marker > 180){
					marker = -180 + (marker - 180);
				}
				g.drawLine(pos,5,pos,30);
				g.drawString(Double.toString(marker), pos+1, 20);
			}
		};
	}
	
	public static ICanvasObjectList getFilteredObjectList(){
		return () -> Canvas3D.get().getShapes(s -> s.isVisible() && !s.isDeleted() && !s.hasFlag(Events.PHASED));
	}
	
	public static IPlugin<IPlugable,Void> getExplodePlugin(Optional<ClipLibrary> clipLibrary, ExplosionSettings explosionSettings)
	{
		return plugable -> {
			ICanvasObject obj = plugable.getParent();
			//could be 2 hits at the same time
			synchronized(obj) {
				if (obj.isVisible()) {
					obj.setVisible(false);
				} else {
					return null;
				}
			}

			PluginLibrary.explode(explosionSettings).execute(plugable).forEach(c -> {
				Canvas3D.get().replaceShader(obj, ScanlineShaderFactory.FLAT.getDefaultSelector());
				TraitHandler.INSTANCE.registerTrait(c, PlugableTrait.class).registerPlugin(Events.CHECK_COLLISION, getBouncePlugin(0.6), true);
				//may want to get the bounceFactor from elsewhere
				if (!obj.hasFlag(SILENT_EXPLODE)) {
					clipLibrary.ifPresent(cl -> cl.playSound("EXPLODE", -20f));
				}
			});
			plugable.registerSingleAfterDrawPlugin(Events.FLASH, PluginLibrary.flash());
			return null;
		};
	}
	
	public static IPlugin<IPlugable,Void> getBouncePlugin(double bounceSpeedFactor){
		return plugable -> {
			ICanvasObject obj = plugable.getParent();
			ICanvasObject impactee = PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(),null, null, false).execute(plugable);
			if (impactee != null){
				if (impactee.hasFlag(Events.STICKY)){
					obj.cancelTransforms();
					//obj.observeAndMatch(impactee);
					//TODO if already a child, object needs to stop being a child of that object, as observe and match also makes this fragment a child of impactee, get weird artifacts with the double processing
				}
				else {
					PluginLibrary.bounce(impactee, bounceSpeedFactor).execute(plugable);
				}
			}
			return null;
		};
	}
	
}
