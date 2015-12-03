package com.graphics.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.graphics.lib.Canvas3D;
import com.graphics.lib.CanvasObject;
import com.graphics.lib.ObjectControls;
import com.graphics.lib.OrientableCanvasObject;
import com.graphics.lib.PlugableCanvasObject;
import com.graphics.lib.Point;
import com.graphics.lib.SimpleOrientation;
import com.graphics.lib.SlaveCanvas3D;
import com.graphics.lib.Vector;
import com.graphics.shapes.Cuboid;
import com.graphics.shapes.Ovoid;
import com.graphics.shapes.Sphere;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.lightsource.CameraTiedLightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.shader.ShaderFactory;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.PanCamera;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.ScaleTransform;
import com.graphics.lib.transform.SequenceTransform;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;
import com.graphics.shapes.Torus;

public class GraphicsTest extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private boolean go = true;
	private JFrame slave;
	private Canvas3D canvas;
	private static GraphicsTest gt;

	public static void main (String[] args){
		try {
			SwingUtilities.invokeAndWait(() -> gt = new GraphicsTest());
			gt.drawCycle();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public GraphicsTest(){
		super("Graphics Test");
		this.setLayout(new BorderLayout());
		this.setSize(700, 700);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
		ViewAngleCamera cam = new ViewAngleCamera(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		cam.setPosition(new Point(350, 280, -200));
		//FocusPointCamera cam = new FocusPointCamera();
		//cam.setFocusPoint(new Point(300, 300, 1000));
		Canvas3D cnv = new Canvas3D(cam);
		
		cnv.addDrawOperation(Utils.showMarkers());
		
		LightSource l1 = new LightSource(0,0,-500);
		l1.setColour(new Color(255, 0, 0));
		cnv.addLightSource(l1);
		LightSource l2 = new LightSource(600,500,-100);
		l2.setColour(new Color(0, 255, 0));
		cnv.addLightSource(l2);
		LightSource l3 = new LightSource(400,100,100);
		l3.setColour(new Color(0, 0, 255));
		cnv.addLightSource(l3);
		

		this.add(cnv, BorderLayout.CENTER);
		
		slave = new JFrame("Slave");
		slave.setLayout(new BorderLayout());
		slave.setSize(300, 300);
		slave.setLocation(750, 50);
		ViewAngleCamera slaveCam = new ViewAngleCamera(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		slaveCam.setPosition(new Point(1500, 350, 400));
		slaveCam.addTransform("INIT", new PanCamera<YRotation>(YRotation.class, -90));
		slaveCam.doTransforms();
		SlaveCanvas3D scnv = new SlaveCanvas3D(slaveCam, cnv);
		slave.add(scnv);
		slave.setVisible(true);
		this.setVisible(true);
		
		OrientableCanvasObject<Ship> ship = new OrientableCanvasObject<Ship>(new Ship (100, 100, 50));
		ship.setColour(new Color(50, 50, 50));
		ship.applyTransform(new Rotation<YRotation>(YRotation.class, 180));
		ship.setOrientation(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		cnv.registerObject(ship, new Point(350, 350, -50), false, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		PlugableCanvasObject<Torus> torus = new PlugableCanvasObject<Torus>(new Torus(50,50,20));
		torus.setColour(new Color(250, 250, 250));
		cnv.registerObject(torus, new Point(200,200,400), false, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		Transform torust1 = new RepeatingTransform<Rotation<?>>(new Rotation<YRotation>(YRotation.class, 3), 60);
		Transform torust2 = new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, 3), 60);
		SequenceTransform torust = new SequenceTransform();
		torust.addTransform(torust1);
		torust.addTransform(torust2);
		torus.addTransformAboutCentre(torust);
		
		
		PlugableCanvasObject<Cuboid> cube = new PlugableCanvasObject<Cuboid>(new Cuboid(200,200,200));
		cnv.registerObject(cube, new Point(500,500,450), false, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		Transform cubet2 = new RepeatingTransform<Rotation<?>>(new Rotation<ZRotation>(ZRotation.class, 3), 30);
		cube.addTransformAboutCentre(cubet2);
		
		Transform cubet = new RepeatingTransform<Translation>(new Translation(-5,0,0), (t) -> {
			return cube.getCentre().x <= 200; 
		});
		
		cube.addTransform(cubet);
		
		PlugableCanvasObject<Sphere> sphere = new PlugableCanvasObject<Sphere>(new Sphere(100,15));
		sphere.setColour(new Color(255, 255, 0));
		cnv.registerObject(sphere, new Point(500,200,400), false, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		for (int i = 0; i < sphere.getFacetList().size() ; i++)
		{
			if (i % (sphere.getObjectAs(Sphere.class).getPointsPerCircle()/3) == 1 || i % (sphere.getObjectAs(Sphere.class).getPointsPerCircle()/3) == 0)
			{
				sphere.getFacetList().get(i).setColour(new Color(150,0,150));
			}
		}
		
		IPlugin<Void,Set<PlugableCanvasObject<?>>> getObjects = new IPlugin<Void,Set<PlugableCanvasObject<?>>>(){
			@Override
			public Set<PlugableCanvasObject<?>> execute(Void v) {
				Set<PlugableCanvasObject<?>> objects = new HashSet<PlugableCanvasObject<?>>();
				cnv.getShapes().stream().filter(s -> s.isVisible() && !s.isDeleted()).forEach(s -> {
					PlugableCanvasObject<?> obj = s.getObjectAs(PlugableCanvasObject.class);
					if (obj != null) objects.add(obj);
				});
				return objects;
			}
		};
		
		IPlugin<PlugableCanvasObject<?>,Void> explode = new IPlugin<PlugableCanvasObject<?>,Void>(){
			@Override
			public Void execute(PlugableCanvasObject<?> obj) {
				PluginLibrary.explode(cnv.getLightSources()).execute(obj).forEach(c -> {
					PlugableCanvasObject<?> p = new PlugableCanvasObject<CanvasObject>(c);
					obj.getChildren().add(p);
					cnv.replaceShader(obj, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.FLAT));
					p.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(getObjects, PluginLibrary.onCollision(), null), true);
				});
				obj.registerSingleAfterDrawPlugin(Events.FLASH, PluginLibrary.flash(cnv.getLightSources()));
				return null;
			}			
		};
		
		IPlugin<PlugableCanvasObject<?>,Void> bounce = new IPlugin<PlugableCanvasObject<?>,Void>(){
			@Override
			public Void execute(PlugableCanvasObject<?> obj) {	
				PlugableCanvasObject<?> impactee = PluginLibrary.hasCollided(getObjects,null, null).execute(obj);
				if (impactee != null){
					PluginLibrary.bounce(impactee).execute(obj);
				}
				return null;
			}			
		};
		
		ScaleTransform st = new ScaleTransform(0.95);
		RepeatingTransform<ScaleTransform> rpt = new RepeatingTransform<ScaleTransform>(st,15){
			@Override
			public void onComplete(){
				st.setScaling(1.05);
			}
		};
		rpt.setResetAfterComplete(true);
		RepeatingTransform<RepeatingTransform<?>> spheret = new RepeatingTransform<RepeatingTransform<?>>(rpt,30);
			
		sphere.addTransformAboutCentre(spheret);
		sphere.registerPlugin(Events.EXPLODE, explode, false);
		torus.registerPlugin(Events.EXPLODE, explode, false);
		cube.registerPlugin(Events.EXPLODE, explode, false);
		
		CameraTiedLightSource l4 = new CameraTiedLightSource(cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
		l4.tieTo(cam);
		l4.setColour(new Color(255, 255, 255));
		cnv.addLightSource(l4);
		
		this.addKeyListener(new ObjectControls(ship){
			@Override
			public void keyTyped(KeyEvent key) {
				super.keyTyped(key);
				if (key.getKeyChar() == 'l') l4.toggle();
				
				if (key.getKeyChar() == 'f'){
					PlugableCanvasObject<OrientableCanvasObject<?>> proj = new PlugableCanvasObject<OrientableCanvasObject<?>>(new OrientableCanvasObject<Ovoid>(new Ovoid(20,0.3,30)));
					proj.applyTransform(new Rotation<XRotation>(XRotation.class, -90));
					proj.getObjectAs(OrientableCanvasObject.class).setOrientation(new SimpleOrientation());
					proj.setBaseIntensity(1);
					proj.setColour(new Color(0, 255, 0, 80));
					
					proj.registerPlugin(Events.EXPLODE, explode, false);
					for (int i = 0; i < proj.getFacetList().size() ; i++)
					{
						if (i % (proj.getObjectAs(Ovoid.class).getPointsPerCircle()/3) == 1 || i % (proj.getObjectAs(Ovoid.class).getPointsPerCircle()/3) == 0)
						{
							proj.getFacetList().get(i).setColour(new Color(0,225,100));
						}
					}
					cam.addCameraRotation(proj);				

					Optional<MovementTransform> shipMove = ship.getTransformsOfType(MovementTransform.class).stream().filter(m -> m.getName().equals(ObjectControls.FORWARD) || m.getName().equals(ObjectControls.BACKWARD) ).findFirst();
					double baseSpeed = 0;
					if (shipMove.isPresent()) baseSpeed = shipMove.get().getName().equals(ObjectControls.FORWARD) ? shipMove.get().getVelocity() : -shipMove.get().getVelocity();
					MovementTransform move = new MovementTransform(new Vector(cam.getOrientation().getForward().x, cam.getOrientation().getForward().y, cam.getOrientation().getForward().z), 18 + baseSpeed){
						@Override
						public void onComplete(){
							proj.executePlugin(Events.EXPLODE); //TODO may need a life on exploded fragments with projectiles
						}
					};
					move.moveUntil(t -> t.getDistanceMoved() >= 1200);
					proj.addTransform(move);
					
					Rotation<?> rot = new Rotation<ZRotation>(ZRotation.class, 20)
					{
						@Override
						public void beforeTransform(){
							super.beforeTransform();
							proj.getObjectAs(OrientableCanvasObject.class).toBaseOrientation();
						}
						
						@Override
						public void afterTransform(){
							super.afterTransform();	
							proj.getObjectAs(OrientableCanvasObject.class).reapplyOrientation();
						}
					}
					;
					
					Transform projt = new RepeatingTransform<Rotation<?>>(rot, t -> move.isCompleteSpecific());

					proj.addTransformAboutCentre(projt);
					proj.deleteAfterTransforms();
					proj.setProcessBackfaces(true);
					
					proj.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(getObjects, explode, explode), true);
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the right of the camera
					pos.x += right.x * 25;
					pos.y += right.y * 25;
					pos.z += right.z * 25;
					cnv.registerObject(proj, pos, false);
					ObjectTiedLightSource l5 = new ObjectTiedLightSource(pos.x, pos.y, pos.z);
					l5.tieTo(proj);
					l5.setColour(new Color(0, 255, 0));
					l5.setRange(400);
					cnv.addLightSource(l5);
				}
				
				if (key.getKeyChar() == 'b'){
					PlugableCanvasObject<Sphere> proj = new PlugableCanvasObject<Sphere>(new Sphere(18,20));
					proj.setBaseIntensity(1);
					proj.setColour(new Color(0, 255, 255, 80));
					
					proj.deleteAfterTransforms();
					proj.setProcessBackfaces(true);
					Optional<MovementTransform> shipMove = ship.getTransformsOfType(MovementTransform.class).stream().filter(m -> m.getName().equals(ObjectControls.FORWARD) || m.getName().equals(ObjectControls.BACKWARD) ).findFirst();
					double baseSpeed = 0;
					if (shipMove.isPresent()) baseSpeed = shipMove.get().getName().equals(ObjectControls.FORWARD) ? shipMove.get().getVelocity() : -shipMove.get().getVelocity();
					MovementTransform move = new MovementTransform(new Vector(cam.getOrientation().getForward().x, cam.getOrientation().getForward().y, cam.getOrientation().getForward().z), 15 + baseSpeed);
					move.moveUntil(t -> t.getDistanceMoved() >= 1000);
					proj.addTransform(move);

					proj.registerPlugin(Events.CHECK_COLLISION, bounce, true);
					proj.registerPlugin("Trail", PluginLibrary.generateTrailParticles(Color.LIGHT_GRAY, 20, 13), true);
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the left of the camera
					pos.x -= right.x * 25;
					pos.y -= right.y * 25;
					pos.z -= right.z * 25;
					cnv.registerObject(proj, pos, false);
					ObjectTiedLightSource l5 = new ObjectTiedLightSource(pos.x, pos.y, pos.z);
					l5.tieTo(proj);
					l5.setColour(new Color(0, 255, 255));
					l5.setRange(400);
					cnv.addLightSource(l5);
				}
			}
			
		});
		cam.setTiedTo(ship, (o, c) -> {
			Point p = o.getVertexList().get(0);
			c.setPosition(new Point(p.x, p.y, p.z));
		});
		
		scnv.setVisible(true);
		cnv.setVisible(true);
		this.requestFocus();
		this.canvas = cnv;

	}
	
	public void drawCycle()
	{
		long sleep = 75;
		while (go){

			try {
				//System.out.println(sleep);
				if (sleep > 0) Thread.sleep(sleep);
				else Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long cycleStart = new Date().getTime();
			canvas.doDraw();
			
			sleep = 75 - (new Date().getTime() - cycleStart);
		}
		
		System.exit(0);
	}
	
	@Override
	public void dispose(){
		super.dispose();
		slave.dispose();
		go = false;	
	}
}
