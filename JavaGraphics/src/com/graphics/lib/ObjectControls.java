package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.graphics.lib.canvas.OrientableCanvasObject;
import com.graphics.lib.orientation.SimpleOrientation;
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
				if (cTransform.getSpeed() > 4) cTransform.setSpeed(cTransform.getSpeed() - 4);
				else transform.cancel();
				return;
			}
			
			transform = this.obj.getTransform(FORWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getSpeed() < 50) cTransform.setSpeed(cTransform.getSpeed() + 4);
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
				if (cTransform.getSpeed() > 4) cTransform.setSpeed(cTransform.getSpeed() - 4);
				else transform.cancel();
				return;
			}			
			
			transform = this.obj.getTransform(BACKWARD);
			if (transform != null && transform instanceof MovementTransform){
				MovementTransform cTransform = (MovementTransform)transform;
				if (cTransform.getSpeed() < 50) cTransform.setSpeed(cTransform.getSpeed() + 4);
			}
			else{
				Transform move = new MovementTransform(() -> obj.getOrientation().getBack(), 4);
				move.setName(BACKWARD);
				this.obj.addTransform(move);
			}
		}

		else if (!this.obj.hasNamedTransform(PAN_RIGHT) && (key.getKeyChar() == 'd' || key.getKeyChar() == 'D'))
		{
			Rotation<?> r = new Rotation<YRotation>(YRotation.class, 4)
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
		}
		else if (!this.obj.hasNamedTransform(PAN_LEFT) && (key.getKeyChar() == 'a' || key.getKeyChar() == 'A'))
		{
			Rotation<?> r = new Rotation<YRotation>(YRotation.class, -4)
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
		}
		else if (!this.obj.hasNamedTransform(PAN_DOWN) && (key.getKeyChar() == 'w' || key.getKeyChar() == 'W'))
		{
			Rotation<?> r = new Rotation<XRotation>(XRotation.class, -4)
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
		}
		else if (!this.obj.hasNamedTransform(PAN_UP) && (key.getKeyChar() == 's' || key.getKeyChar() == 'S'))
		{
			Rotation<?> r = new Rotation<XRotation>(XRotation.class, 4)
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
		}
		else if (!this.obj.hasNamedTransform(ROLL_RIGHT) && (key.getKeyChar() == 'e' || key.getKeyChar() == 'E'))
		{
			Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, 4)
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
		}
		else if (!this.obj.hasNamedTransform(ROLL_LEFT) && (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q'))
		{
			Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, -4)
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
		}
		else if (!this.obj.hasNamedTransform(UP) && key.getKeyCode() == 38)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getUp(), 4);
			move.setName(UP);
			this.obj.addTransform(move);
		}
		else if (!this.obj.hasNamedTransform(RIGHT) && key.getKeyCode() == 39)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getRight(), 4);
			move.setName(RIGHT);
			this.obj.addTransform(move);
		}
		else if (!this.obj.hasNamedTransform(DOWN) && key.getKeyCode() == 40)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getDown(), 4);
			move.setName(DOWN);
			this.obj.addTransform(move);
		}
		else if (!this.obj.hasNamedTransform(LEFT) && key.getKeyCode() == 37)
		{
			Transform move = new MovementTransform(() -> obj.getOrientation().getLeft(), 4);
			move.setName(LEFT);
			this.obj.addTransform(move);
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		if (key.getKeyChar() == 'w' || key.getKeyChar() == 'W')
		{
			this.obj.cancelNamedTransform(PAN_DOWN);
		}
		else if (key.getKeyChar() == 's' || key.getKeyChar() == 'S')
		{
			this.obj.cancelNamedTransform(PAN_UP);
		}
		else if (key.getKeyChar() == 'd' || key.getKeyChar() == 'D')
		{
			this.obj.cancelNamedTransform(PAN_RIGHT);
		}
		else if (key.getKeyChar() == 'a' || key.getKeyChar() == 'A')
		{
			this.obj.cancelNamedTransform(PAN_LEFT);
		}
		else if (key.getKeyChar() == 'e' || key.getKeyChar() == 'E')
		{
			this.obj.cancelNamedTransform(ROLL_RIGHT);
		}
		else if (key.getKeyChar() == 'q' || key.getKeyChar() == 'Q')
		{
			this.obj.cancelNamedTransform(ROLL_LEFT);
		}
		else if (key.getKeyCode() == 38)
		{
			this.obj.cancelNamedTransform(UP);
		}
		else if (key.getKeyCode() == 39)
		{
			this.obj.cancelNamedTransform(RIGHT);
		}
		else if (key.getKeyCode() == 40)
		{
			this.obj.cancelNamedTransform(DOWN);
		}
		else if (key.getKeyCode() == 37)
		{
			this.obj.cancelNamedTransform(LEFT);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
