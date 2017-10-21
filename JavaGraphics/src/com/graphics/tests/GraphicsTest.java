package com.graphics.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.BasicStroke;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.shapes.Cuboid;
import com.graphics.shapes.Lantern;
import com.graphics.shapes.Sphere;
import com.graphics.shapes.Whale;
import com.graphics.tests.shapes.FlapTest;
import com.graphics.tests.shapes.Gate;
import com.graphics.tests.shapes.Ship;
import com.graphics.tests.shapes.TexturedCuboid;
import com.graphics.tests.shapes.Wall;
import com.graphics.tests.weapons.BouncyProjectile;
import com.graphics.tests.weapons.DeflectionProjectile;
import com.graphics.tests.weapons.ExplodingProjectile;
import com.graphics.tests.weapons.LaserWeapon;
import com.graphics.tests.weapons.Projectile;
import com.graphics.tests.weapons.ProjectileWeapon;
import com.graphics.tests.weapons.TrackingProjectile;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.SlaveCanvas3D;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.lightsource.DirectionalLightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.shader.ShaderFactory;
import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.OvoidTextureMapper;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TexturableTrait;
import com.graphics.lib.transform.*;
import com.graphics.lib.zbuffer.ZBuffer;
import com.sound.ClipLibrary;

public class GraphicsTest extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private boolean go = true;
	private JFrame slave;
	private Canvas3D canvas;
	private static GraphicsTest gt;
	private ClipLibrary clipLibrary = new ClipLibrary("sounds.txt");
	private ICanvasObject selectedObject = null;
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
			
		ViewAngleCamera cam = new ViewAngleCamera(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		cam.setPosition(new Point(350, 280, -200));
		//FocusPointCamera cam = new FocusPointCamera();
		//cam.setFocusPoint(new Point(300, 300, 1000));
		Canvas3D cnv = Canvas3D.get(cam);
		ZBuffer zBuf = new ZBuffer();
		//zBuf.setSkip(3);
		cnv.setzBuffer(zBuf);
		
		cnv.addDrawOperation(TestUtils.showMarkers());
		
		Facet floor = new Facet(new WorldCoord(0,650,0), new WorldCoord(700,650,0), new WorldCoord(0,650,700));
		cnv.setFloor(floor);
		//cnv.setDrawShadows(true);
		
		ObjectTiedLightSource<LightSource> l1 = new ObjectTiedLightSource<>(LightSource.class, 0,0,-500);
		l1.getLightSource().setColour(new Color(255, 0, 0));
		cnv.addLightSource(l1.getLightSource());
		Lantern lantern1 = new Lantern();
		lantern1.attachLightsource(l1);
		cnv.registerObject(lantern1, new Point(0,0,-500), ShaderFactory.NONE);
		
		ObjectTiedLightSource<LightSource> l2 = new ObjectTiedLightSource<>(LightSource.class, 500,200,-100);
		l2.getLightSource().setColour(new Color(0, 255, 0));
		cnv.addLightSource(l2.getLightSource());
		Lantern lantern2 = new Lantern();
		lantern2.attachLightsource(l2);
		cnv.registerObject(lantern2, new Point(500,200,-100), ShaderFactory.NONE);
		
		ObjectTiedLightSource<DirectionalLightSource> l3 = new ObjectTiedLightSource<>(DirectionalLightSource.class, 400,100,100);
		l3.getLightSource().setColour(new Color(0, 0, 255));
		cnv.addLightSource(l3.getLightSource());
		Lantern lantern3 = new Lantern();
		

		lantern3.addTrait(new OrientableTrait()).setOrientation(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		cnv.registerObject(lantern3, new Point(400,100,100), ShaderFactory.NONE);
		l3.getLightSource().setDirection(() -> {return lantern3.getTrait(IOrientable.class).get().getOrientation().getForward();});
		l3.getLightSource().setLightConeAngle(40);
		lantern3.attachLightsource(l3);
		lantern3.getFacetList().stream().filter(f -> f.getNormal().getZ() <= 0).forEach(f -> f.setColour(Color.BLACK));
		Transform l3spin = new RepeatingTransform<Rotation>(Axis.X.getRotation(4), 0);
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(lantern3, l3spin);
		
		this.add(cnv, BorderLayout.CENTER);
		
		slave = new JFrame("Slave");
		slave.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		slave.setLayout(new BorderLayout());
		slave.setSize(300, 300);
		slave.setLocation(750, 50);
		
		IPlugin<IPlugable, Void> explode = TestUtils.getExplodePlugin(clipLibrary);
		
		CanvasObject camcube = new Cuboid(20,20,20);
		cnv.registerObject(camcube, new Point(1560, 200, 350), ShaderFactory.FLAT);
		
		Whale whale = new Whale(); 
		whale.addTrait(new PlugableTrait()).registerPlugin(Events.EXPLODE, explode, false);
		whale.setColour(Color.cyan);
		cnv.registerObject(whale, new Point(1515, 300, 400), ShaderFactory.GORAUD);
		
		FlapTest flap = new FlapTest(); 
		flap.setColour(Color.ORANGE);
		cnv.registerObject(flap, new Point(1000, 500, 200), ShaderFactory.GORAUD);
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutPoint(flap, new Point(1200, 500, 200), new RepeatingTransform<Rotation>(Axis.Y.getRotation(2),0));
		
		ViewAngleCamera slaveCam = new ViewAngleCamera(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		slaveCam.setPosition(new Point(1550, 200, 350));
		slaveCam.addTransform("INIT", new PanCamera(Axis.Y, -90));
		slaveCam.doTransforms();
		SlaveCanvas3D scnv = new SlaveCanvas3D(slaveCam);
		slave.add(scnv);
		slave.setVisible(true);
		cnv.addObserver(scnv);
		this.setVisible(true);
		
		
		IPointFinder leftOffset = () -> {
			Point pos = new Point(cam.getPosition());
			Vector right = cam.getOrientation().getRight();
			pos.x -= right.getX() * 25;
			pos.y -= right.getY() * 25;
			pos.z -= right.getZ() * 25;
			return pos;
		};
		
		IPointFinder rightOffset = () -> {
			Point pos = new Point(cam.getPosition());
			Vector right = cam.getOrientation().getRight();
			pos.x += right.getX() * 25;
			pos.y += right.getY() * 25;
			pos.z += right.getZ() * 25;
			return pos;
		};
		
		Ship ship = new Ship (100, 100, 50);
		ship.addWeapon(new LaserWeapon(() -> {
			Point pos = new Point(cnv.getCamera().getPosition());
			Vector down = cam.getOrientation().getDown();
			pos.x += down.getX() * 15;
			pos.y += down.getY() * 15;
			pos.z += down.getZ() * 15;
			return pos;
		},
		() -> {
			return cam.getOrientation().getForward();
		},ship));
		
		Projectile bp = new BouncyProjectile();
		bp.setClipLibary(clipLibrary);
		ship.addWeapon(new ProjectileWeapon(bp, leftOffset, () -> {
			return cam.getOrientation().getForward();
		}, ship));
		
		DeflectionProjectile dp = new DeflectionProjectile();
		dp.setSpeed(20);
		dp.setRange(8000);
		dp.setClipLibary(clipLibrary);
		dp.setTargetFinder(() -> {
			return this.selectedObject;
		});
		ship.addWeapon(new ProjectileWeapon(dp, leftOffset, () -> {
			return cam.getOrientation().getForward();
		}, ship));
		
		TrackingProjectile tp = new TrackingProjectile();
		tp.setSpeed(20);
		tp.setRange(8000);
		tp.setClipLibary(clipLibrary);
		tp.setTargetFinder(() -> {
			return this.selectedObject;
		});
		ship.addWeapon(new ProjectileWeapon(tp, leftOffset, () -> {
			return cam.getOrientation().getForward();
		}, ship));
		
		ExplodingProjectile ep = new ExplodingProjectile();
		ep.setSpeed(20);
		ep.setRange(1200);
		ep.setClipLibary(clipLibrary);

		ship.addWeapon(new ProjectileWeapon(ep, rightOffset, () -> {
			return cam.getOrientation().getForward();
		}, ship));
		
		ship.addWeapon(new ProjectileWeapon(ep, leftOffset, () -> {
			return cam.getOrientation().getForward();
		}, ship));
		
		ship.setColour(new Color(50, 50, 50));
		ship.addTrait(new OrientableTrait()).setOrientation(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		ship.applyTransform(Axis.Y.getRotation(180));
		cnv.registerObject(ship, new Point(350, 350, -50), ShaderFactory.GORAUD);

		Gate torus = new Gate(50,50,20);
		torus.addTrait(new PlugableTrait()).registerPlugin(Events.EXPLODE, explode, false);
		torus.setColour(new Color(250, 250, 250));
		//torus.setLightIntensityFinder(Utils.getShadowLightIntensityFinder(() -> { return cnv.getShapes();})); //for testing shadows falling on the torus
		cnv.registerObject(torus, new Point(200,200,450), ShaderFactory.GORAUD);
		Transform torust1 = new RepeatingTransform<Rotation>(Axis.Y.getRotation(3), 60);
		Transform torust2 = new RepeatingTransform<Rotation>(Axis.X.getRotation(3), 60);
		SequenceTransform torust = new SequenceTransform();
		torust.add(torust1).add(torust2);
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(torus, torust);
		torus.addFlag(Events.EXPLODE_PERSIST);
		//torus.setCastsShadow(false);
		
		torus.setPassThroughPluginForGate((obj) -> {
		    Transform rot = new RepeatingTransform<Rotation>(Axis.X.getRotation(3), 60);
		    CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(obj, rot);
			return null;
		});
		
		torus.setPassThroughPluginForObject((obj) -> {
            obj.setColour(Color.pink);
            return null;
        });
		
		TexturedCuboid cube = new TexturedCuboid(200,200,200);
		cube.addTrait(new PlugableTrait());
		cnv.registerObject(cube, new Point(500,500,500), ShaderFactory.TEXGORAUD);
		Transform cubet2 = new RepeatingTransform<Rotation>(Axis.Z.getRotation(3), 30);
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(cube, cubet2);
		cube.addFlag(Events.STICKY);
		
		MovementTransform cubet = new MovementTransform(new Vector(-1,0,0), 5);
		cubet.moveUntil(t -> t.getDistanceMoved() >= 350);
		
		cube.addTransform(cubet);
		
		Sphere ball = new Sphere(100,15);
		ball.addTrait(new TexturableTrait()).addTexture(new BmpTexture("smily")).mapTexture(new OvoidTextureMapper());
		ball.addTrait(new PlugableTrait()).registerPlugin(Events.EXPLODE, explode, false);
		ball.setColour(new Color(255, 255, 0));
		ball.addFlag(Events.EXPLODE_PERSIST);
		cnv.registerObject(ball, new Point(500,200,450), ShaderFactory.TEXGORAUD);
		
		for (int i = 0; i < ball.getFacetList().size() ; i++)
		{
			if (i % (ball.getPointsPerCircle()/3) == 1 || i % (ball.getPointsPerCircle()/3) == 0)
			{
			    ball.getFacetList().get(i).setColour(new Color(150,0,150));
			}
		}
		
		
		Wall wall = new Wall(40,40);
		wall.setColour(new Color(240, 240, 240));
		wall.setLightIntensityFinder(Utils.getShadowLightIntensityFinder(() -> { return cnv.getShapes();}));
		wall.setVisible(false);
		cnv.registerObject(wall, new Point(350,350,700), ShaderFactory.GORAUD);		
		
		ScaleTransform st = new ScaleTransform(0.95);
		RepeatingTransform<ScaleTransform> rpt = new RepeatingTransform<ScaleTransform>(st,15){
			@Override
			public void onComplete(){
				st.setScaling(1.05);
			}
		};
		rpt.setResetAfterComplete(true);
		RepeatingTransform<?> spheret = new RepeatingTransform<RepeatingTransform<?>>(rpt,30);
			
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(ball, spheret);
		
		//CameraTiedLightSource l4 = new CameraTiedLightSource(cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
		//l4.tieTo(cam);
		DirectionalLightSource l4 = new DirectionalLightSource();
		l4.setPosition(() -> {return cam.getPosition();});
		l4.setDirection(() -> {return cam.getOrientation().getForward().getUnitVector();});
		l4.setLightConeAngle(45);
		l4.setColour(new Color(255, 255, 255));
		cnv.addLightSource(l4);
		
		try {
			this.addKeyListener(new ShipControls(ship));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.addKeyListener(new KeyListener(){
			@Override
			public void keyTyped(KeyEvent key) {
				if (key.getKeyChar() == 'l') l4.toggle();
				
				else if (key.getKeyChar() == 'y'){
				    Cuboid movingTarget = new Cuboid(20,20,20);
					MovementTransform move = new MovementTransform(new Vector(1,1,0).getUnitVector(), 4);
					move.moveUntil(t -> t.getDistanceMoved() > 5000);
					movingTarget.addTransform(move);
					movingTarget.setCastsShadow(false);
					movingTarget.deleteAfterTransforms();
					movingTarget.addTrait(new PlugableTrait()).registerPlugin(Events.EXPLODE, explode, false);
					cnv.registerObject(movingTarget , new Point(0, 0, 0), ShaderFactory.FLAT);
				}
				
				else if (key.getKeyChar() == 'z'){
					double viewAngle = cam.getViewAngle() - 5;
					cam.setViewAngle(viewAngle < 5 ? 5 : viewAngle);
				}
				
				else if (key.getKeyChar() == 'x'){
					double viewAngle = cam.getViewAngle() + 5;
					cam.setViewAngle(viewAngle > 85 ? 85 : viewAngle);
				}
				
				else if (key.getKeyChar() == 'c'){
					double angle = l4.getLightConeAngle() - 5;
					l4.setLightConeAngle(angle < 5 ? 5 : angle);
				}
				
				else if (key.getKeyChar() == 'v'){
					double angle = l4.getLightConeAngle() + 5;
					l4.setLightConeAngle(angle > 85 ? 85 : angle);
				}
				
				else if (key.getKeyChar() == 'n'){
					wall.setVisible(!wall.isVisible());
				}
				
				else if (key.getKeyChar() == 'm'){
					cnv.setDrawShadows(!cnv.isDrawShadows());
				}
				
				else if (key.getKeyChar() == '.' && l3.getLightSource().getIntensity() <= 0.9 && l3.getLightSource().isOn()){
					l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() + 0.1);
					lantern3.setColour(l3.getLightSource().getActualColour());
				}
				
				else if (key.getKeyChar() == ',' && l3.getLightSource().getIntensity() >= 0.1 && l3.getLightSource().isOn()){
					l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() - 0.1);
					lantern3.setColour(l3.getLightSource().getActualColour());
				}
				
				else if (key.getKeyChar() == '1'){
					l1.toggle();
				}
				else if (key.getKeyChar() == '2'){
					l2.toggle();
				}
				else if (key.getKeyChar() == '3'){
					l3.toggle();
				}
				
			}
			
			@Override
			public void keyPressed(KeyEvent key) {
				checkEngineSound(ship);
			}
			
			@Override
			public void keyReleased(KeyEvent key) {
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
			ICanvasObject selectedObject = getSelectedObject();
			if (selectedObject == null || !selectedObject.isVisible()) return;
			
			WorldCoord pt = selectedObject.getVertexList().get(0);
			double maxX = pt.getTransformed(cam).x;
			double maxY = pt.getTransformed(cam).y;
			double minX = pt.getTransformed(cam).x;
			double minY = pt.getTransformed(cam).y;
			for (WorldCoord p : selectedObject.getVertexList())
			{
				if (p.hasTags()) continue;
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
		cnv.addDrawOperation((c, g) -> {
			//more messing with collectors for ammo counts
			Map<String, Integer> ammoCounts = ship.getWeapons().stream().filter(w -> w instanceof ProjectileWeapon)
					.map(w -> (ProjectileWeapon)w).collect(
						Collectors.groupingBy(w -> w.getProjectile().getClass().getSimpleName(), 
								Collectors.summingInt(ProjectileWeapon::getAmmoCount))
					);	
			g.setColor(new Color(0,0,0,150));
			g.fillRect(0, c.getHeight() - 20, c.getWidth(), 20);
			g.setColor(Color.WHITE);
			g.drawString(ammoCounts.toString(), 40, c.getHeight()-5);
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
	
	private synchronized ICanvasObject getSelectedObject() {
		return selectedObject;
	}

	private synchronized void setSelectedObject(ICanvasObject selectedObject) {
		this.selectedObject = selectedObject;
	}

	private void checkEngineSound(CanvasObject source){
		if (source.getTransformsOfType(MovementTransform.class).stream().filter(t -> !t.isCancelled() && !t.isComplete() && t.getAcceleration() != 0).count() > 0){
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
