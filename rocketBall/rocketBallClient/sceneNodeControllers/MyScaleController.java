package rocketBallClient.sceneNodeControllers;

import graphicslib3D.Matrix3D;
import sage.scene.Controller;
import sage.scene.SceneNode;

public class MyScaleController extends Controller {

	private double scaleRate = 1; // scale per second
	
	private double totalTime;
	private double direction = 1.0;
	
	@Override
	public void update(double time) { 
		totalTime += (time / (1000000000 / 2)) // very slowly
				;
		float transAmount = (float) (scaleRate + totalTime);

		transAmount = (float) (direction * transAmount);
		
		Matrix3D newTrans = new Matrix3D();
		newTrans.scale(transAmount, transAmount, transAmount);
		
		for (SceneNode node : controlledNodes) {
			Matrix3D curTrans = node.getLocalScale();
			curTrans.concatenate(newTrans);
			node.setLocalTranslation(curTrans);
		}
	}
	
}
