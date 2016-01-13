package com.graphics.lib.camera;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.graphics.lib.transform.CameraMovementTransform;
import com.graphics.lib.transform.CameraTransform;
import com.graphics.lib.transform.PanCamera;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;

public class CameraControls implements KeyListener{
	private static final String FORWARD = "FORWARD";
	private static final String BACKWARD = "BACKWARD";
	private static final String UP = "UP";
	private static final String DOWN = "DOWN";
	private static final String LEFT = "LEFT";
	private static final String RIGHT = "RIGHT";
	private static final String PAN_LEFT = "PAN_LEFT";
	private static final String PAN_RIGHT = "PAN_RIGHT";
	private static final String PAN_UP = "PAN_UP";
	private static final String PAN_DOWN = "PAN_DOWN";
	private static final String ROLL_LEFT = "ROLL_LEFT";
	private static final String ROLL_RIGHT = "ROLL_RIGHT";

	private boolean panLeftKeyDown = false;
	private boolean panRightKeyDown = false;
	private boolean panUpKeyDown = false;
	private boolean panDownKeyDown = false;
	private boolean rollLeftKeyDown = false;
	private boolean rollRightKeyDown = false;
	private boolean upKeyDown = false;
	private boolean downKeyDown = false;
	private boolean rightKeyDown = false;
	private boolean leftKeyDown = false;

	private Camera camera;
	
	public CameraControls(Camera camera)
	{
		this.camera = camera;
		//will need some kind of configuration object
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		
		if (key.getKeyChar() == '+' || key.getKeyChar() == '=')
		{
			CameraTransform transform = this.camera.getTransform(BACKWARD);
			if (transform != null && transform instanceof CameraMovementTransform){
				CameraMovementTransform cTransform = (CameraMovementTransform)transform;
				if (cTransform.getSpeed() > 4) cTransform.setSpeed(cTransform.getSpeed() - 4);
				else this.camera.removeTransform(BACKWARD);
				return;
			}
			
			transform = this.camera.getTransform(FORWARD);
			if (transform != null && transform instanceof CameraMovementTransform){
				CameraMovementTransform cTransform = (CameraMovementTransform)transform;
				if (cTransform.getSpeed() < 50) cTransform.setSpeed(cTransform.getSpeed() + 4);
			}
			else{
				this.camera.addTransform(FORWARD, new CameraMovementTransform(() -> camera.getOrientation().getForward(), 4));
			}
		}
		else if (key.getKeyChar() == '-' || key.getKeyChar() == '_')
		{
			CameraTransform transform = this.camera.getTransform(FORWARD);
			if (transform != null && transform instanceof CameraMovementTransform){
				CameraMovementTransform cTransform = (CameraMovementTransform)transform;
				if (cTransform.getSpeed() > 4) cTransform.setSpeed(cTransform.getSpeed() - 4);
				else this.camera.removeTransform(FORWARD);
				return;
			}			
			
			transform = this.camera.getTransform(BACKWARD);
			if (transform != null && transform instanceof CameraMovementTransform){
				CameraMovementTransform cTransform = (CameraMovementTransform)transform;
				if (cTransform.getSpeed() < 50) cTransform.setSpeed(cTransform.getSpeed() + 4);
			}
			else{
				this.camera.addTransform(BACKWARD, new CameraMovementTransform(() -> camera.getOrientation().getBack(), 4));
			}
		}
		else if (!this.panRightKeyDown && (key.getKeyChar() == 'd' || key.getKeyChar() == 'D'))
		{
			this.camera.addTransform(PAN_RIGHT, new PanCamera<YRotation>(YRotation.class, 5));
			this.panRightKeyDown = true;
		}
		else if (!this.panLeftKeyDown && (key.getKeyChar() == 'a' || key.getKeyChar() == 'A'))
		{
			this.camera.addTransform(PAN_LEFT, new PanCamera<YRotation>(YRotation.class, -5));
			this.panLeftKeyDown = true;
		}
		else if (!this.panDownKeyDown && (key.getKeyChar() == 'w' || key.getKeyChar() == 'W'))
		{
			this.camera.addTransform(PAN_DOWN, new PanCamera<XRotation>(XRotation.class, -5));
			this.panDownKeyDown = true;
		}
		else if (!this.panUpKeyDown && (key.getKeyChar() == 's' || key.getKeyChar() == 'S'))
		{
			this.camera.addTransform(PAN_UP, new PanCamera<XRotation>(XRotation.class, 5));
			this.panUpKeyDown = true;
		}
		else if (!this.rollRightKeyDown && (key.getKeyChar() == 'e' || key.getKeyChar() == 'E'))
		{
			this.camera.addTransform(ROLL_RIGHT, new PanCamera<ZRotation>(ZRotation.class, 5));
			this.rollRightKeyDown = true;
		}
		else if (!this.rollLeftKeyDown && (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q'))
		{
			this.camera.addTransform(ROLL_LEFT, new PanCamera<ZRotation>(ZRotation.class, -5));
			this.rollLeftKeyDown = true;
		}
		else if (!this.upKeyDown && key.getKeyCode() == 38)
		{
			this.camera.addTransform(UP, new CameraMovementTransform(() -> camera.getOrientation().getUp(), 5));
			this.upKeyDown = true;
		}
		else if (!this.rightKeyDown && key.getKeyCode() == 39)
		{
			this.camera.addTransform(RIGHT, new CameraMovementTransform(() -> camera.getOrientation().getRight(), 5));
			this.rightKeyDown = true;
		}
		else if (!this.downKeyDown && key.getKeyCode() == 40)
		{
			this.camera.addTransform(DOWN, new CameraMovementTransform(() -> camera.getOrientation().getDown(), 5));
			this.downKeyDown = true;
		}
		else if (!this.leftKeyDown && key.getKeyCode() == 37)
		{
			this.camera.addTransform(LEFT, new CameraMovementTransform(() -> camera.getOrientation().getLeft(), 5));
			this.leftKeyDown = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		if (key.getKeyChar() == 'w' || key.getKeyChar() == 'W')
		{
			this.camera.removeTransform(PAN_DOWN);
			this.panDownKeyDown = false;
		}
		else if (key.getKeyChar() == 's' || key.getKeyChar() == 'S')
		{
			this.camera.removeTransform(PAN_UP);
			this.panUpKeyDown = false;
		}
		else if (key.getKeyChar() == 'd' || key.getKeyChar() == 'D')
		{
			this.camera.removeTransform(PAN_RIGHT);
			this.panRightKeyDown = false;
		}
		else if (key.getKeyChar() == 'a' || key.getKeyChar() == 'A')
		{
			this.camera.removeTransform(PAN_LEFT);
			this.panLeftKeyDown = false;
		}
		else if (key.getKeyChar() == 'e' || key.getKeyChar() == 'E')
		{
			this.camera.removeTransform(ROLL_RIGHT);
			this.rollRightKeyDown = false;
		}
		else if (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q')
		{
			this.camera.removeTransform(ROLL_LEFT);
			this.rollLeftKeyDown = false;
		}
		else if (key.getKeyCode() == 38)
		{
			this.camera.removeTransform(UP);
			this.upKeyDown = false;
		}
		else if (key.getKeyCode() == 39)
		{
			this.camera.removeTransform(RIGHT);
			this.rightKeyDown = false;
		}
		else if (key.getKeyCode() == 40)
		{
			this.camera.removeTransform(DOWN);
			this.downKeyDown = false;
		}
		else if (key.getKeyCode() == 37)
		{
			this.camera.removeTransform(LEFT);
			this.leftKeyDown = false;
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
