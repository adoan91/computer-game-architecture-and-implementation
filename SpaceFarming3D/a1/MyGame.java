package a1;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Random;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import myGameEngine.BackwardAction;
import myGameEngine.CrashEvent;
import myGameEngine.DownAction;
import myGameEngine.ForwardAction;
import myGameEngine.LeftAction;
import myGameEngine.MoveRXAxis;
import myGameEngine.MoveRYAxis;
import myGameEngine.MoveXAxis;
import myGameEngine.MoveYAxis;
import myGameEngine.PitchDownAction;
import myGameEngine.PitchUpAction;
import myGameEngine.QuitGameAction;
import myGameEngine.RightAction;
import myGameEngine.RollLeftAction;
import myGameEngine.RollRightAction;
import myGameEngine.SetSpeedAction;
import myGameEngine.UpAction;
import myGameEngine.YawLeftAction;
import myGameEngine.YawRightAction;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.action.IAction;
import sage.scene.HUDString;
import sage.scene.shape.Line;
import sage.scene.shape.Sphere;


public class MyGame extends BaseGame {

	private IDisplaySystem display;
	private ICamera camera;
	private IInputManager im;
	private IEventManager eventMgr;
	private String gpName;
	private String kb;
	//private MyPyramid aPyr;
	//private Matrix3D pyramidMatrix;
	private Random rand = new Random();
	private MyPyramid [] plants = new MyPyramid [10];
	private Matrix3D [] plantsMatrix = new Matrix3D [10];
	private boolean [] plantsCapturedTracker = new boolean [plants.length];
	private Sphere aSphere;
	private CustomShape myShape;
	
	
	private Matrix3D msM;
	
	private Matrix3D sphereMatrix;
	
	private double scale = 1;
	private int numCrashes = 0;
	private int plantsCaptured = 0;
	
	private int score = 0;
	private float time = 0; // game elapsed time
	private HUDString scoreString;
	private HUDString timeString;
	
	private boolean sphereGotHit = false;
	
	private boolean gameOverFlag = false;
	
	@Override
	protected void initGame() {
		
		eventMgr = EventManager.getInstance();
		initGameObjects();
		im = getInputManager();
		
		kb = im.getKeyboardName();
		
		IAction xAxisMove = new MoveXAxis(camera, 0.01f);
		IAction yAxisMove = new MoveYAxis(camera, 0.01f);
		IAction rxAxisMove = new MoveRXAxis(camera, 0.05f);
		IAction ryAxisMove = new MoveRYAxis(camera, 0.05f);
		IAction setSpeed = new SetSpeedAction();
		IAction mvForward = new ForwardAction(camera, (SetSpeedAction) setSpeed);
		IAction mvBackward = new BackwardAction(camera, (SetSpeedAction) setSpeed);
		IAction quit = new QuitGameAction(this);
		IAction mvLeft = new LeftAction(camera, (SetSpeedAction) setSpeed);
		IAction mvRight = new RightAction(camera, (SetSpeedAction) setSpeed);
		IAction mvUp = new UpAction(camera, (SetSpeedAction) setSpeed);
		IAction mvDown = new DownAction(camera, (SetSpeedAction) setSpeed);
		IAction rotateLeft = new YawLeftAction(camera, (SetSpeedAction) setSpeed);
		IAction rotateRight = new YawRightAction(camera, (SetSpeedAction) setSpeed);
		IAction rotateUp = new PitchUpAction(camera, (SetSpeedAction) setSpeed);
		IAction rotateDown = new PitchDownAction(camera, (SetSpeedAction) setSpeed);
		IAction rollLeft = new RollLeftAction(camera, (SetSpeedAction) setSpeed);
		IAction rollRight = new RollRightAction(camera, (SetSpeedAction) setSpeed);
		
		gpName = im.getFirstGamepadName();
		// System.out.println(gpName);
		// System.out.println(kb);
		if (gpName != null) {
			im.associateAction( // left and right
					gpName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					xAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			im.associateAction( // forward and backward
					gpName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					yAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			im.associateAction( // yaw left and right
					gpName, 
					net.java.games.input.Component.Identifier.Axis.RX, 
					rxAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			im.associateAction( // pitch up and down
					gpName, 
					net.java.games.input.Component.Identifier.Axis.RY, 
					ryAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
		}

		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.W, 
				mvForward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.A, 
				mvLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.D, 
				mvRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.S, 
				mvBackward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.X, 
				mvUp,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.Z, 
				mvDown,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.Q, 
				rollLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.E, 
				rollRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.LEFT, 
				rotateLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.RIGHT, 
				rotateRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.UP, 
				rotateUp,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.DOWN, 
				rotateDown,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.ESCAPE, 
				quit,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);

//		super.update(0.0f);
	}
	
	public void checkGameOver() {
		/*int flag = 0;
		int i = 0;
		while (i < plantsCapturedTracker.length) {
			if (plantsCapturedTracker[i] == false) {
				flag = 1;
			}
			i++;
		}
		
		if (flag == 0) {
			// game over
			gameOverFlag = true;
			System.out.print("You win");
			scoreString.setText("YOU WIN");
			//setGameOver(true);
		}*/
		
		if (plantsCaptured == 11) {
			// game over
			gameOverFlag = true;
			System.out.print("You win");
			scoreString.setText("YOU WIN");
			//setGameOver(true);
		}
	}
	
	@Override
	public void update(float elapsedTimeMS) {
		
		if (gameOverFlag != true) {
			
			if (sphereGotHit == false) {
			
				if (aSphere.getWorldBound().contains(camera.getLocation())) {
					numCrashes++;
	//				CrashEvent newCrash = new CrashEvent(numCrashes);
	//				eventMgr.triggerEvent(newCrash);
					
					sphereMatrix.setToIdentity();
					sphereMatrix.translate(5, -10 + (plantsCaptured + 5), 0);
					aSphere.setLocalTranslation(sphereMatrix);
					plantsCaptured++;
					
					scale += 0.01;
					
					msM.scale(scale,
							scale,
							scale
							);
					myShape.setLocalTranslation(msM);
					myShape.nextColor(); // change to random color
					
					checkGameOver();
					sphereGotHit = true;
				}
			
			}
			
			int i = 0;
			while (i < plants.length) {
				
				
				
				if (plants[i].getWorldBound().contains(camera.getLocation()) && plantsCapturedTracker[i] == false) {
					
					
					numCrashes++;
					CrashEvent newCrash = new CrashEvent(numCrashes);
					eventMgr.triggerEvent(newCrash);
					
					plantsMatrix[i].setToIdentity();
					plantsMatrix[i].translate(5, -10 + (plantsCaptured + 5), 0);
					
					plants[i].setLocalTranslation(plantsMatrix[i]);
					plantsCaptured++;
					plantsCapturedTracker[i] = true;
					checkGameOver();
					
					// gets a bit bigger each time we get a plant
					scale += 0.01;
					
					msM.scale(scale,
							scale,
							scale
							);
					myShape.setLocalTranslation(msM);
					myShape.nextColor(); // change to random color
				}
			
				
				i++;
			}
			i = 0;
			
			// update the HUD
			scoreString.setText("Score = " +
					plantsCaptured
					);
			time += elapsedTimeMS;
			DecimalFormat df = new DecimalFormat("0.0");
			timeString.setText("Time = " +
					df.format(time / 1000)
					);
			// tell BaseGame to update game world state
			
			/*if (aSphere.getWorldBound().contains(camera.getLocation())) {
				numCrashes++;
				CrashEvent newCrash = new CrashEvent(numCrashes);
				eventMgr.triggerEvent(newCrash);
				
				sphereMatrix.setToIdentity();
				sphereMatrix.translate(0, ((5 * plantsCaptured) + 5), 0);
				aSphere.setLocalTranslation(sphereMatrix);
				plantsCaptured++;
			}
			if (aPyr.getWorldBound().contains(camera.getLocation())) {
				numCrashes++;
				
				CrashEvent newCrash = new CrashEvent(numCrashes);
				eventMgr.triggerEvent(newCrash);
				
				pyramidMatrix.setToIdentity();
				pyramidMatrix.translate(0, ((5 * plantsCaptured) + 5), 0);
				aPyr.setLocalTranslation(pyramidMatrix);
				plantsCaptured++;
			}*/
		} else {
			// you win
			checkGameOver();
		}
		
		super.update(elapsedTimeMS);
	}
	
	private void initGameObjects() {
		display = getDisplaySystem();
		display.setTitle("Space Farming 3D");
		camera = display.getRenderer().getCamera();
		camera.setPerspectiveFrustum(45, 1, 0.01, 1000);
		camera.setLocation(new Point3D(1, 1, 70));
		
		
		aSphere = new Sphere();
		sphereMatrix = aSphere.getLocalTranslation();
		sphereMatrix.scale(1 + rand.nextDouble(),
				1 + rand.nextDouble(),
				1 + rand.nextDouble()
				);
		sphereMatrix.translate((rand.nextInt(40) - 20),
				(rand.nextInt(40) - 20),
				(rand.nextInt(40) - 20)
				);
		aSphere.setLocalTranslation(sphereMatrix);
		addGameWorldObject(aSphere);
		aSphere.updateWorldBound();
//		eventMgr.addListener((IEventListener) aSphere, CrashEvent.class);
		
		int i = 0;
		while (i < plants.length) {
			plantsCapturedTracker[i] = false;
			plants[i] = new MyPyramid();
			plantsMatrix[i] = plants[i].getLocalTranslation();
			plantsMatrix[i].scale((1 + rand.nextDouble()),
					(1 + rand.nextDouble()),
					(1 + rand.nextDouble())
					);
			plantsMatrix[i].translate((rand.nextInt(40) - 20),
					(rand.nextInt(40) - 20),
					(rand.nextInt(40) - 20)
					);
			plants[i].setLocalTranslation(plantsMatrix[i]);
			addGameWorldObject(plants[i]);
			plants[i].updateWorldBound();
			eventMgr.addListener(plants[i], CrashEvent.class);
			i++;
		}
		i = 0;
		/*
		aPyr = new MyPyramid();
		pyramidMatrix = aPyr.getLocalTranslation();
		pyramidMatrix.translate(2, 0, -12);
		aPyr.setLocalTranslation(pyramidMatrix);
		addGameWorldObject(aPyr);
		*/
		
		
		myShape = new CustomShape();
		msM = myShape.getLocalTranslation();
		msM.translate(5, 0, 0);
		msM.rotateY(180);
		msM.scale(10, 10, 10);
		myShape.setLocalTranslation(msM);
		addGameWorldObject(myShape);
		
		Point3D x1 = new Point3D(-100, 0, 0);
		Point3D x2 = new Point3D(100, 0, 0);
		Point3D y1 = new Point3D(0, -100, 0);
		Point3D y2 = new Point3D(0, 100, 0);
		Point3D z1 = new Point3D(0, 0, -100);
		Point3D z2 = new Point3D(0, 0, 100);
		Line xAxis = new Line(x1, x2, Color.red, 2);
		Line yAxis = new Line(y1, y2, Color.green, 2);
		Line zAxis = new Line(z1, z2, Color.blue, 2);
		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);
		
		// a HUD
		timeString = new HUDString("Time = " +
				time
				);
		timeString.setLocation(0, 0.1); // (0,0) [lower-left] to (1,1)
		addGameWorldObject(timeString);
		scoreString = new HUDString ("Score = " +
				score
				); //default is (0,0)
		scoreString.setLocation(0, 0.05);
		addGameWorldObject(scoreString);
		
		/*aSphere.updateWorldBound();
		aPyr.updateWorldBound();
		eventMgr.addListener(aPyr, CrashEvent.class);*/
		
/*		Cylinder myCylinder = new Cylinder();
		Matrix3D myCylinderMatrix = myCylinder.getLocalTranslation();
		myCylinderMatrix.translate(2, 10, -12);
		myCylinder.setLocalTranslation(myCylinderMatrix);
		addGameWorldObject(myCylinder);
*/
	}
	
	public static void main(String [] args) {
		//new MyGame().start();
		
	}
	
}
