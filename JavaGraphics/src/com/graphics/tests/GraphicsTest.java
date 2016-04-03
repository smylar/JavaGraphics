package com.graphics.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BasicStroke;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;




import com.graphics.lib.Facet;
import com.graphics.lib.ObjectControls;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.shapes.Cuboid;
import com.graphics.shapes.Lantern;
import com.graphics.shapes.Ovoid;
import com.graphics.shapes.Sphere;
import com.graphics.shapes.Whale;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.OrientableCanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.canvas.SlaveCanvas3D;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.lightsource.DirectionalLightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.shader.ShaderFactory;
import com.graphics.lib.transform.*;
import com.graphics.lib.zbuffer.ZBuffer;
import com.graphics.shapes.Torus;
import com.sound.ClipLibrary;

public class GraphicsTest extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String SILENT_EXPLODE = "sexpl";
	
	private boolean go = true;
	private JFrame slave;
	private Canvas3D canvas;
	private static GraphicsTest gt;
	private ClipLibrary clipLibrary = new ClipLibrary();
	private CanvasObject selectedObject = null;
	private MouseEvent select = null;

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
		ZBuffer zBuf = new ZBuffer();
		//zBuf.setSkip(3);
		cnv.setzBuffer(zBuf);
		
		cnv.addDrawOperation(TestUtils.showMarkers());
		
		Facet floor = new Facet(new WorldCoord(0,650,0), new WorldCoord(700,650,0), new WorldCoord(0,650,700));
		cnv.setFloor(floor);
		//cnv.setDrawShadows(true);
		
		ObjectTiedLightSource<LightSource> l1 = new ObjectTiedLightSource<LightSource>(LightSource.class, 0,0,-500);
		l1.getLightSource().setColour(new Color(255, 0, 0));
		cnv.addLightSource(l1.getLightSource());
		Lantern lantern1 = new Lantern();
		lantern1.attachLightsource(l1);
		cnv.registerObject(lantern1, new Point(0,0,-500), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.NONE));
		
		ObjectTiedLightSource<LightSource> l2 = new ObjectTiedLightSource<LightSource>(LightSource.class, 500,200,-100);
		l2.getLightSource().setColour(new Color(0, 255, 0));
		cnv.addLightSource(l2.getLightSource());
		Lantern lantern2 = new Lantern();
		lantern2.attachLightsource(l2);
		cnv.registerObject(lantern2, new Point(500,200,-100), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.NONE));
		
		/*ObjectTiedLightSource<LightSource> l3 = new ObjectTiedLightSource<LightSource>(LightSource.class, 400,100,100);
		l3.getLightSource().setColour(new Color(0, 0, 255));
		cnv.addLightSource(l3.getLightSource());
		Lantern lantern3 = new Lantern();
		lantern3.attachLightsource(l3);
		cnv.registerObject(lantern3, new Point(400,100,100), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.NONE));*/
		
		ObjectTiedLightSource<DirectionalLightSource> l3 = new ObjectTiedLightSource<DirectionalLightSource>(DirectionalLightSource.class, 400,100,100);
		l3.getLightSource().setColour(new Color(0, 0, 255));
		cnv.addLightSource(l3.getLightSource());
		Lantern lantern3 = new Lantern();
		lantern3.attachLightsource(l3);
		OrientableCanvasObject<Lantern> ol3 = new OrientableCanvasObject<Lantern>(lantern3);
		ol3.setOrientation(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		cnv.registerObject(ol3, new Point(400,100,100), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.NONE));
		l3.getLightSource().setDirection(() -> {return ol3.getOrientation().getForward();});
		l3.getLightSource().setLightConeAngle(40);
		Transform l3spin = new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, 4), 0);
		ol3.addTransformAboutCentre(l3spin);
		
		this.add(cnv, BorderLayout.CENTER);
		
		slave = new JFrame("Slave");
		slave.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		slave.setLayout(new BorderLayout());
		slave.setSize(300, 300);
		slave.setLocation(750, 50);
		
		CanvasObject camcube = new Cuboid(20,20,20);
		cnv.registerObject(camcube, new Point(1515, 300, 400), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.FLAT));
		
		PlugableCanvasObject<?> whale = new PlugableCanvasObject<Whale>(new Whale()); 
		whale.setColour(Color.cyan);
		//whale.applyTransform(new Rotation<YRotation>(YRotation.class, 45));
		cnv.registerObject(whale, new Point(1515, 300, 400), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		FlapTest flap = new FlapTest(); 
		flap.setColour(Color.ORANGE);
		cnv.registerObject(flap, new Point(1000, 500, 200), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		ViewAngleCamera slaveCam = new ViewAngleCamera(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		slaveCam.setPosition(new Point(1500, 300, 400));
		slaveCam.addTransform("INIT", new PanCamera<YRotation>(YRotation.class, -90));
		slaveCam.doTransforms();
		SlaveCanvas3D scnv = new SlaveCanvas3D(slaveCam);
		slave.add(scnv);
		slave.setVisible(true);
		cnv.addObserver(scnv);
		this.setVisible(true);
		
		OrientableCanvasObject<Ship> ship = new OrientableCanvasObject<Ship>(new Ship (100, 100, 50));
		ship.setColour(new Color(50, 50, 50));
		ship.applyTransform(new Rotation<YRotation>(YRotation.class, 180));
		ship.setOrientation(new SimpleOrientation(OrientableCanvasObject.ORIENTATION_TAG));
		cnv.registerObject(ship, new Point(350, 350, -50), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		PlugableCanvasObject<Torus> torus = new PlugableCanvasObject<Torus>(new Torus(50,50,20));
		torus.setColour(new Color(250, 250, 250));
		//torus.setLightIntensityFinder(Utils.getShadowLightIntensityFinder(() -> { return cnv.getShapes();})); //for testing shadows falling on the torus
		cnv.registerObject(torus, new Point(200,200,400), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		Transform torust1 = new RepeatingTransform<Rotation<?>>(new Rotation<YRotation>(YRotation.class, 3), 60);
		Transform torust2 = new RepeatingTransform<Rotation<?>>(new Rotation<XRotation>(XRotation.class, 3), 60);
		SequenceTransform torust = new SequenceTransform();
		torust.addTransform(torust1);
		torust.addTransform(torust2);
		torus.addTransformAboutCentre(torust);
		torus.addFlag(Events.EXPLODE_PERSIST);
		//torus.setCastsShadow(false);
		
		PlugableCanvasObject<TexturedCuboid> cube = new PlugableCanvasObject<TexturedCuboid>(new TexturedCuboid(200,200,200));
		cnv.registerObject(cube, new Point(500,500,450), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.TEXGORAUD));
		Transform cubet2 = new RepeatingTransform<Rotation<?>>(new Rotation<ZRotation>(ZRotation.class, 3), 30);
		cube.addTransformAboutCentre(cubet2);
		cube.addFlag(Events.STICKY);
		
		//Transform cubet = new RepeatingTransform<Translation>(new Translation(-5,0,0), (t) -> {
		//	return cube.getCentre().x <= 200; 
		//});
		MovementTransform cubet = new MovementTransform(new Vector(-1,0,0), 5);
		cubet.moveUntil(t -> t.getDistanceMoved() >= 350);
		
		cube.addTransform(cubet);
		
		PlugableCanvasObject<Sphere> sphere = new PlugableCanvasObject<Sphere>(new Sphere(100,15));
		sphere.setColour(new Color(255, 255, 0));
		sphere.addFlag(Events.EXPLODE_PERSIST);
		cnv.registerObject(sphere, new Point(500,200,400), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		for (int i = 0; i < sphere.getFacetList().size() ; i++)
		{
			if (i % (sphere.getObjectAs(Sphere.class).getPointsPerCircle()/3) == 1 || i % (sphere.getObjectAs(Sphere.class).getPointsPerCircle()/3) == 0)
			{
				sphere.getFacetList().get(i).setColour(new Color(150,0,150));
			}
		}
		
		Wall wall = new Wall(40,40);
		wall.setColour(new Color(240, 240, 240));
		wall.setLightIntensityFinder(Utils.getShadowLightIntensityFinder(() -> { return cnv.getShapes();}));
		wall.setVisible(false);
		cnv.registerObject(wall, new Point(300,0,700), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.GORAUD));
		
		ICanvasObjectList getObjects = () -> {return cnv.getShapes().stream().filter(s -> s.isVisible() && !s.isDeleted() && !s.hasFlag("PHASED")).collect(Collectors.toList());};
		
		IPlugin<PlugableCanvasObject<?>,Void> explode = new IPlugin<PlugableCanvasObject<?>,Void>(){
			@Override
			public Void execute(PlugableCanvasObject<?> obj) {
				PluginLibrary.explode(cnv.getLightSources()).execute(obj).forEach(c -> {
					cnv.replaceShader(obj, ShaderFactory.GetShader(ShaderFactory.ShaderEnum.FLAT));
					c.registerPlugin(Events.STOP, PluginLibrary.stop(), false);
					c.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(getObjects, Events.STOP, null), true);
					if (!obj.hasFlag(SILENT_EXPLODE)) clipLibrary.playSound("EXPLODE", -20f);
				});
				obj.registerSingleAfterDrawPlugin(Events.FLASH, PluginLibrary.flash(cnv.getLightSources()));
				return null;
			}			
		};
		
		whale.registerPlugin(Events.EXPLODE, explode, false);
		
		IPlugin<PlugableCanvasObject<?>,Void> bounce = new IPlugin<PlugableCanvasObject<?>,Void>(){
			@Override
			public Void execute(PlugableCanvasObject<?> obj) {	
				//CanvasObject impactee = PluginLibrary.hasCollided(getObjects,null, null).execute(obj);
				CanvasObject impactee = PluginLibrary.hasCollidedNew(getObjects,null, null).execute(obj);
				if (impactee != null){
					if (impactee.hasFlag(Events.STICKY)){ 
						PluginLibrary.stop2().execute(obj);
						obj.observeAndMatch(impactee);
						clipLibrary.playSound("STICK", -20f);
					}
					else {
						PluginLibrary.bounce(impactee).execute(obj);
						clipLibrary.playSound("BOUNCE", -20f);
					}
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
		RepeatingTransform<?> spheret = new RepeatingTransform<RepeatingTransform<?>>(rpt,30);
			
		sphere.addTransformAboutCentre(spheret);
		
		//CameraTiedLightSource l4 = new CameraTiedLightSource(cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
		//l4.tieTo(cam);
		DirectionalLightSource l4 = new DirectionalLightSource();
		l4.setPosition(() -> {return cam.getPosition();});
		l4.setDirection(() -> {return cam.getOrientation().getForward().getUnitVector();});
		l4.setLightConeAngle(45);
		l4.setColour(new Color(255, 255, 255));
		cnv.addLightSource(l4);
		
		sphere.registerPlugin(Events.EXPLODE, explode, false);
		torus.registerPlugin(Events.EXPLODE, explode, false);
		
		this.addKeyListener(new ObjectControls(ship){
			@Override
			public void keyTyped(KeyEvent key) {
				super.keyTyped(key);
				if (key.getKeyChar() == 'l') l4.toggle();
				
				if (key.getKeyChar() == 'r'){
					Laser laser = new Laser(1000);
					laser.addFlag("PHASED");
					cam.addCameraRotation(laser);
					Point pos = new Point(cam.getPosition());
					Vector down = cam.getOrientation().getDown();
					pos.x += down.x * 15;
					pos.y += down.y * 15;
					pos.z += down.z * 15;
					cnv.registerObject(laser, pos);
					
					for (CanvasObject obj : getObjects.get()){ //TODO would like an object list for those on screen only here
						//Facet f = obj.getIntersectedFacet(pos, cam.getOrientation().getForward(), false);
						for (Facet f : obj.getIntersectedFacets(pos, cam.getOrientation().getForward()))
						{
							//if (f != null && (f.point1.getTransformed(cam).z + f.point2.getTransformed(cam).z + f.point3.getTransformed(cam).z)/3 < laser.getLength() ){
							if (f != null && f.getAsList().stream().mapToDouble(p -> p.getTransformed(cam).z).average().getAsDouble() < laser.getLength() ){
		
								//f.setColour(Color.DARK_GRAY);
								f.setMaxIntensity(0.15);
								//as an aspiration - create dynamic texture map for laser 'holes'
							}
						}
					}
					/*laser = new Laser();
					cam.addCameraRotation(laser);
					pos = new Point(cam.getPosition());
					pos.x -= down.x * 15;
					pos.y -= down.y * 15;
					pos.z -= down.z * 15;
					cnv.registerObject(laser, pos);*/
				}
				
				if (key.getKeyChar() == 'f'){
					//fire a projectile (that also generates light) that attempts to blow up a target
					PlugableCanvasObject<?> proj = new PlugableCanvasObject<CanvasObject>(new OrientableCanvasObject<Ovoid>(new Ovoid(20,0.3,30)));
					proj.applyTransform(new Rotation<XRotation>(XRotation.class, -90));
					proj.getObjectAs(OrientableCanvasObject.class).setOrientation(new SimpleOrientation());
					proj.setBaseIntensity(1);
					proj.setColour(new Color(255, 0, 0, 80));
					proj.setCastsShadow(false);
					proj.registerPlugin(Events.EXPLODE, explode, false);
					proj.addFlag(SILENT_EXPLODE);
					for (int i = 0; i < proj.getFacetList().size() ; i++)
					{
						if (i % (proj.getObjectAs(Ovoid.class).getPointsPerCircle()/3) == 1 || i % (proj.getObjectAs(Ovoid.class).getPointsPerCircle()/3) == 0)
						{
							proj.getFacetList().get(i).setColour(new Color(225,0,0));
						}
					}
					cam.addCameraRotation(proj);				

					Optional<MovementTransform> shipMove = ship.getTransformsOfType(MovementTransform.class).stream().filter(m -> m.getName().equals(ObjectControls.FORWARD) || m.getName().equals(ObjectControls.BACKWARD) ).findFirst();
					double speed = 18;
					if (shipMove.isPresent()) speed += shipMove.get().getName().equals(ObjectControls.FORWARD) ? shipMove.get().getSpeed() : -shipMove.get().getSpeed();
					MovementTransform move = new MovementTransform(new Vector(cam.getOrientation().getForward().x, cam.getOrientation().getForward().y, cam.getOrientation().getForward().z), speed){
						@Override
						public void onComplete(){
							proj.executePlugin(Events.EXPLODE);
						}
					};
					move.moveUntil(t -> t.getDistanceMoved() >= 1200);
					proj.addTransform(move);
					
					/*MovementTransform gravity = new MovementTransform(new Vector(0,1,0),0);
					gravity.setAcceleration(0.15);
					gravity.moveUntil(t -> move.isComplete());
					proj.addTransform(gravity);*/
					
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
					
					proj.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollidedNew(getObjects, Events.EXPLODE, Events.EXPLODE), true);
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the right of the camera
					pos.x += right.x * 25;
					pos.y += right.y * 25;
					pos.z += right.z * 25;
					cnv.registerObject(proj, pos);
					ObjectTiedLightSource<LightSource> l5 = new ObjectTiedLightSource<LightSource>(LightSource.class, pos.x, pos.y, pos.z);
					l5.tieTo(proj);
					l5.getLightSource().setColour(proj.getColour());
					l5.getLightSource().setRange(400);
					cnv.addLightSource(l5.getLightSource());
				}
				
				if (key.getKeyChar() == 'b'){
					//fires a ball (that also generates light) that tries to bounce off a target
					PlugableCanvasObject<Sphere> proj = new PlugableCanvasObject<Sphere>(new Sphere(18,20));
					proj.setBaseIntensity(1);
					proj.setColour(new Color(0, 255, 255, 80));
					proj.setCastsShadow(false);
					proj.deleteAfterTransforms();
					proj.setProcessBackfaces(true);
					Optional<MovementTransform> shipMove = ship.getTransformsOfType(MovementTransform.class)
							.stream()
							.filter(m -> m.getName().equals(ObjectControls.FORWARD) || m.getName().equals(ObjectControls.BACKWARD) )
							.findFirst();
					
					double baseSpeed = 0;
					if (shipMove.isPresent()) baseSpeed = shipMove.get().getName().equals(ObjectControls.FORWARD) ? shipMove.get().getSpeed() : -shipMove.get().getSpeed();
					MovementTransform move = new MovementTransform(new Vector(cam.getOrientation().getForward().x, cam.getOrientation().getForward().y, cam.getOrientation().getForward().z), 15 + baseSpeed);
					long delTime = new Date().getTime() + 5000;
					move.moveUntil(t -> t.getDistanceMoved() > 1000 || (t.getSpeed() == 0 && new Date().getTime() > delTime));
					proj.addTransform(move);

					proj.registerPlugin(Events.CHECK_COLLISION, bounce, true);
					proj.registerPlugin("Trail", PluginLibrary.generateTrailParticles(Color.LIGHT_GRAY, 20, 13, 0.66), true);
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the left of the camera
					pos.x -= right.x * 25;
					pos.y -= right.y * 25;
					pos.z -= right.z * 25;
					cnv.registerObject(proj, pos);
					ObjectTiedLightSource<LightSource> l5 = new ObjectTiedLightSource<LightSource>(LightSource.class, pos.x, pos.y, pos.z);
					l5.tieTo(proj);
					l5.setColour(new Color(0, 255, 255));
					l5.getLightSource().setRange(400);
					cnv.addLightSource(l5.getLightSource());
				}
				
				if (key.getKeyChar() == 't'){
					//fires a ball that attempts to track a target and blow it up
					PlugableCanvasObject<Sphere> proj = new PlugableCanvasObject<Sphere>(new Sphere(18,20));
					proj.setBaseIntensity(1);
					proj.setColour(new Color(255, 255, 0, 80));
					proj.setCastsShadow(false);
					proj.deleteAfterTransforms();
					proj.setProcessBackfaces(true);

					MovementTransform move = new MovementTransform(new Vector(cam.getOrientation().getForward().x, cam.getOrientation().getForward().y, cam.getOrientation().getForward().z), 20);
					move.moveUntil(t -> t.getDistanceMoved() > 2000);
					proj.addTransform(move);

					proj.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(getObjects, Events.EXPLODE, Events.EXPLODE), true);
					proj.registerPlugin(Events.EXPLODE, explode, false);
					proj.registerPlugin("Track", PluginLibrary.track(selectedObject, 1), true); 
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the left of the camera
					pos.x -= right.x * 25;
					pos.y -= right.y * 25;
					pos.z -= right.z * 25;
					cnv.registerObject(proj, pos);
				}
				
				if (key.getKeyChar() == 'y'){
					PlugableCanvasObject<Cuboid> movingTarget = new PlugableCanvasObject<Cuboid>(new Cuboid(20,20,20));
					//CanvasObject movingTarget = new Cuboid(20,20,20);
					MovementTransform move = new MovementTransform(new Vector(1,1,0).getUnitVector(), 4);
					move.moveUntil(t -> t.getDistanceMoved() > 5000);
					movingTarget.addTransform(move);
					movingTarget.setCastsShadow(false);
					movingTarget.deleteAfterTransforms();
					movingTarget.registerPlugin(Events.EXPLODE, explode, false);
					cnv.registerObject(movingTarget , new Point(0, 0, 0), ShaderFactory.GetShader(ShaderFactory.ShaderEnum.FLAT));
				}
				
				if (key.getKeyChar() == 'u'){
					//fires a ball on an intercept course to a target and attempts to blow it up
					if (selectedObject == null) return;
					
					PlugableCanvasObject<Sphere> proj = new PlugableCanvasObject<Sphere>(new Sphere(18,20));
					proj.setBaseIntensity(1);
					proj.setColour(new Color(255, 0, 255, 80));
					proj.setCastsShadow(false);
					proj.deleteAfterTransforms();
					proj.setProcessBackfaces(true);

					proj.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(getObjects, Events.EXPLODE, Events.EXPLODE), true);
					proj.registerPlugin(Events.EXPLODE, explode, false);
					
					Point pos = new Point(cam.getPosition());
					Vector right = cam.getOrientation().getRight();
					//offsets to the left of the camera
					pos.x -= right.x * 25;
					pos.y -= right.y * 25;
					pos.z -= right.z * 25;
					cnv.registerObject(proj, pos);
					
					Vector vTrackee = Utils.plotDeflectionShot(selectedObject, proj, 20);
					MovementTransform move = new MovementTransform(vTrackee, 20); 
					move.moveUntil(t -> t.getDistanceMoved() > 10000);
					proj.addTransform(move);
				}
				
				if (key.getKeyChar() == 'z'){
					double viewAngle = cam.getViewAngle() - 5;
					cam.setViewAngle(viewAngle < 5 ? 5 : viewAngle);
				}
				
				if (key.getKeyChar() == 'x'){
					double viewAngle = cam.getViewAngle() + 5;
					cam.setViewAngle(viewAngle > 85 ? 85 : viewAngle);
				}
				
				if (key.getKeyChar() == 'c'){
					double angle = l4.getLightConeAngle() - 5;
					l4.setLightConeAngle(angle < 5 ? 5 : angle);
				}
				
				if (key.getKeyChar() == 'v'){
					double angle = l4.getLightConeAngle() + 5;
					l4.setLightConeAngle(angle > 85 ? 85 : angle);
				}
				
				if (key.getKeyChar() == 'n'){
					wall.setVisible(!wall.isVisible());
				}
				
				if (key.getKeyChar() == 'm'){
					cnv.setDrawShadows(!cnv.isDrawShadows());
				}
				
				if (key.getKeyChar() == '.' && l3.getLightSource().getIntensity() <= 0.9 && l3.getLightSource().isOn()){
					l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() + 0.1);
					lantern3.setColour(l3.getLightSource().getActualColour());
				}
				
				if (key.getKeyChar() == ',' && l3.getLightSource().getIntensity() >= 0.1 && l3.getLightSource().isOn()){
					l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() - 0.1);
					lantern3.setColour(l3.getLightSource().getActualColour());
				}
				
				if (key.getKeyChar() == '1'){
					l1.toggle();
				}
				if (key.getKeyChar() == '2'){
					l2.toggle();
				}
				if (key.getKeyChar() == '3'){
					l3.toggle();
				}
				
				checkEngineSound(ship);
			}
			
			@Override
			public void keyPressed(KeyEvent key) {
				super.keyPressed(key);
				checkEngineSound(ship);
			}
			
			@Override
			public void keyReleased(KeyEvent key) {
				super.keyReleased(key);
				checkEngineSound(ship);
			}
			
		});
		
		
		cnv.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				select = e;
			}		
		});
		
		cnv.addDrawOperation((c, g) -> {
			CanvasObject selectedObject = getSelectedObject();
			if (selectedObject == null || !selectedObject.isVisible()) return;
			
			WorldCoord pt = selectedObject.getVertexList().get(0);
			double maxX = pt.getTransformed(cam).x;
			double maxY = pt.getTransformed(cam).y;
			double minX = pt.getTransformed(cam).x;
			double minY = pt.getTransformed(cam).y;
			for (WorldCoord p : selectedObject.getVertexList())
			{
				if (p.getTag().length() > 0) continue;
				if (p.getTransformed(cam).x > maxX) maxX = p.getTransformed(cam).x;
				if (p.getTransformed(cam).x < minX) minX = p.getTransformed(cam).x;
				if (p.getTransformed(cam).y > maxY) maxY = p.getTransformed(cam).y;
				if (p.getTransformed(cam).y < minY) minY = p.getTransformed(cam).y;
			};
			
			g.setColor(Color.DARK_GRAY);
			float dash1[] = {10.0f};
		    BasicStroke dashed = new BasicStroke(1.5f,
		                        BasicStroke.CAP_BUTT,
		                        BasicStroke.JOIN_MITER,
		                        10.0f, dash1, 0.0f);
			((Graphics2D)g).setStroke(dashed);
			g.drawRect((int)minX - 4, (int)minY - 4, (int)maxX - (int)minX + 8, (int)maxY - (int)minY + 8);
		});
		
		/*cnv.addDrawOperation((c, g) -> {
		 	//messing with collectors - puts the count of each object type at the bottom of the screen
			Map<String, Long> objectCounts = c.getShapes().stream().collect(
						Collectors.groupingBy(s -> s.getBaseObject().getClass().getSimpleName(), Collectors.counting())
					);	
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(objectCounts.toString(), 40, c.getHeight()-5);
		});*/
		
		
		cam.setTiedTo(ship, (o, c) -> {
			Point p = o.getVertexList().get(0);
			c.setPosition(new Point(p.x, p.y, p.z));
		});
		
		scnv.setVisible(true);
		cnv.setVisible(true);
		this.requestFocus();
		this.canvas = cnv;

	}
	
	private synchronized CanvasObject getSelectedObject() {
		return selectedObject;
	}

	private synchronized void setSelectedObject(CanvasObject selectedObject) {
		this.selectedObject = selectedObject;
	}

	private void checkEngineSound(CanvasObject source){
		if (source.getTransformsOfType(MovementTransform.class).stream().filter(t -> !t.isCancelled() && !t.isComplete()).count() > 0){
			clipLibrary.loopSound("ENGINE", -30f);
		}else{
			clipLibrary.stopSound("ENGINE");
		}
	}
	
	public void drawCycle()
	{
		long sleep = 50;
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
			if (select != null){
				setSelectedObject(canvas.getObjectAt(select.getX(), select.getY()));
				select = null;
			}
			
			sleep = 50 - (new Date().getTime() - cycleStart);
		}
		try {
			clipLibrary.close();
		} catch (Exception e) {}
		System.out.println("Bye bye");
		System.exit(0);
	}
	
	@Override
	public void dispose(){
		if (go){
			go = false;
		}else{
			super.dispose();
			slave.dispose();
		}
	}
}
