package com.graphics.lib.camera;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Point;
import com.graphics.lib.interfaces.IOrientation;

public class FocusPointCamera extends Camera {

	public FocusPointCamera(IOrientation orientation) {
		super(orientation);
	}

	private Point focusPoint = new Point(0,0,1000);
	
	@Override
	public void getViewSpecific(CanvasObject obj) {

		obj.getVertexList().stream().forEach(p -> {
			p.resetTransformed();
			double percent = p.z / focusPoint.z;
			
			double dx = p.x - focusPoint.x;
			double dy = p.y - focusPoint.y;
			p.getTransformed().x -= dx * percent;
			p.getTransformed().y -= dy * percent;
		});
	}

	public Point getFocusPoint() {
		return focusPoint;
	}

	public void setFocusPoint(Point focusPoint) {
		this.focusPoint = focusPoint;
	}

}
