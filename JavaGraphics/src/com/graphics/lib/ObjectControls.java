package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;

public class ObjectControls implements KeyListener{
	public static final String FORWARD = "FORWARD";
	public static final String BACKWARD = "BACKWARD";
	public static final String UP = "UP";
	public static final String DOWN = "DOWN";
	public static final String LEFT = "LEFT";
	public static final String RIGHT = "RIGHT";
	public static final String PAN_LEFT = "PAN_LEFT";
	public static final String PAN_RIGHT = "PAN_RIGHT";
	public static final String PAN_UP = "PAN_UP";
	public static final String PAN_DOWN = "PAN_DOWN";
	public static final String ROLL_LEFT = "ROLL_LEFT";
	public static final String ROLL_RIGHT = "ROLL_RIGHT";

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

	private OrientableCanvasObject<?> obj;
	
	public ObjectControls(OrientableCanvasObject<?> obj)
	{
		this.obj = obj;
		if (this.obj.getOrientation() == null)
		{
			this.obj.setOrientation(new SimpleOrientation());
		}
		//will need some kind of configuration object
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		
		if (key.getKeyChar() == '+' || key.getKeyChar() == '=')
		{
			Transform transform = this.obj.getTransform(BACKWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getVelocity() > 4) cTransform.setVelocity(cTransform.getVelocity() - 4);
				else transform.cancel();
				return;
			}
			
			transform = this.obj.getTransform(FORWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getVelocity() < 50) cTransform.setVelocity(cTransform.getVelocity() + 4);
			}
			else{
				Transform move = new MovementTransform(() -> obj.getOrientation().getForward(), 4);
				move.setName(FORWARD);
				this.obj.addTransform(move);
			}
		}
		else if (key.getKeyChar() == '-' || key.getKeyChar() == '_')
		{
			Transform transform = this.obj.getTransform(FORWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getVelocity() > 4) cTransform.setVelocity(cTransform.getVelocity() - 4);
				else transform.cancel();
				return;
			}			
			
			transform = this.obj.getTransform(BACKWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getVelocity() < 50) cTransform.setVelocity(cTransform.getVelocity() + 4);
			}
			else{
				Transform move = new MovementTransform(() -> obj.getOrientation().getBack(), 4);
				move.setName(BACKWARD);
				this.obj.addTransform(move);
			}
		}

		else if (!this.panRightKeyDown && (key.getKeyChar() == 'd' || key.getKeyChar() == 'D'))
		{
			Rotation<?> r = new Rotation<YRotation>(YRotation.class, 5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(PAN_RIGHT);
			this.obj.addTransformAboutCentre(rot);
			this.panRightKeyDown = true;
		}
		else if (!this.panLeftKeyDown && (key.getKeyChar() == 'a' || key.getKeyChar() == 'A'))
		{
			Rotation<?> r = new Rotation<YRotation>(YRotation.class, -5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(PAN_LEFT);
			this.obj.addTransformAboutCentre(rot);
			this.panLeftKeyDown = true;
		}
		else if (!this.panDownKeyDown && (key.getKeyChar() == 'w' || key.getKeyChar() == 'W'))
		{
			Rotation<?> r = new Rotation<XRotation>(XRotation.class, -5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(PAN_DOWN);
			this.obj.addTransformAboutCentre(rot);
			this.panDownKeyDown = true;
		}
		else if (!this.panUpKeyDown && (key.getKeyChar() == 's' || key.getKeyChar() == 'S'))
		{
			Rotation<?> r = new Rotation<XRotation>(XRotation.class, 5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(PAN_UP);
			this.obj.addTransformAboutCentre(rot);
			this.panUpKeyDown = true;
		}
		else if (!this.rollRightKeyDown && (key.getKeyChar() == 'e' || key.getKeyChar() == 'E'))
		{
			Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, 5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(ROLL_RIGHT);
			this.obj.addTransformAboutCentre(rot);
			this.rollRightKeyDown = true;
		}
		else if (!this.rollLeftKeyDown && (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q'))
		{
			Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, -5)
			{
				@Override
				public void beforeTransform(){
					super.beforeTransform();
					obj.toBaseOrientation();
				}
				
				@Override
				public void afterTransform(){
					super.afterTransform();	
					obj.reapplyOrientation();
				}
			};
			Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
			rot.setName(ROLL_LEFT);
			this.obj.addTransformAboutCentre(rot);
			this.rollLeftKeyDown = true;
		}
		else if (!this.upKeyDown && key.getKeyCode() == 38)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getUp(), 5);
			move.setName(UP);
			this.obj.addTransform(move);
			this.upKeyDown = true;
		}
		else if (!this.rightKeyDown && key.getKeyCode() == 39)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getRight(), 5);
			move.setName(RIGHT);
			this.obj.addTransform(move);
			this.rightKeyDown = true;
		}
		else if (!this.downKeyDown && key.getKeyCode() == 40)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getDown(), 5);
			move.setName(DOWN);
			this.obj.addTransform(move);
			this.downKeyDown = true;
		}
		else if (!this.leftKeyDown && key.getKeyCode() == 37)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getLeft(), 5);
			move.setName(LEFT);
			this.obj.addTransform(move);
			this.leftKeyDown = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		if (key.getKeyChar() == 'w' || key.getKeyChar() == 'W')
		{
			this.obj.getTransform(PAN_DOWN).cancel();
			this.panDownKeyDown = false;
		}
		else if (key.getKeyChar() == 's' || key.getKeyChar() == 'S')
		{
			this.obj.getTransform(PAN_UP).cancel();
			this.panUpKeyDown = false;
		}
		else if (key.getKeyChar() == 'd' || key.getKeyChar() == 'D')
		{
			this.obj.getTransform(PAN_RIGHT).cancel();
			this.panRightKeyDown = false;
		}
		else if (key.getKeyChar() == 'a' || key.getKeyChar() == 'A')
		{
			this.obj.getTransform(PAN_LEFT).cancel();
			this.panLeftKeyDown = false;
		}
		else if (key.getKeyChar() == 'e' || key.getKeyChar() == 'E')
		{
			this.obj.getTransform(ROLL_RIGHT).cancel();
			this.rollRightKeyDown = false;
		}
		else if (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q')
		{
			this.obj.getTransform(ROLL_LEFT).cancel();
			this.rollLeftKeyDown = false;
		}
		else if (key.getKeyCode() == 38)
		{
			this.obj.getTransform(UP).cancel();
			this.upKeyDown = false;
		}
		else if (key.getKeyCode() == 39)
		{
			this.obj.getTransform(RIGHT).cancel();
			this.rightKeyDown = false;
		}
		else if (key.getKeyCode() == 40)
		{
			this.obj.getTransform(DOWN).cancel();
			this.downKeyDown = false;
		}
		else if (key.getKeyCode() == 37)
		{
			this.obj.getTransform(LEFT).cancel();
			this.leftKeyDown = false;
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
