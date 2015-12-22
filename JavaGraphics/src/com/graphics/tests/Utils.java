package com.graphics.tests;

import java.awt.Color;
import java.awt.Graphics;
import java.util.function.BiConsumer;

import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.Canvas3D;

public class Utils {
	
	
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
				g.drawString(""+(int)marker, 10, pos-1);
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
				g.drawString(""+(int)marker, pos+1, 20);
			}
			g.setColor(Color.RED);
			g.drawLine(middleWidth,5,middleWidth,30);
			
			g.setColor(Color.black);
			//System.out.println(middleWidth + ":" + middleHeight);
			g.drawLine(middleWidth - 5, middleHeight, middleWidth + 5, middleHeight);
			g.drawLine(middleWidth, middleHeight - 5, middleWidth, middleHeight + 5);
			
			if (c.getCamera() instanceof ViewAngleCamera){	
				g.drawString(""+((ViewAngleCamera)c.getCamera()).getViewAngle() , c.getWidth() - 30, c.getHeight() - 10);
			}
		};
	}
	
}
