package rocketBallClient.inputActions;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.SceneNode;

public class MoveXAxis extends AbstractInputAction implements IAction {
	private SceneNode player;
	private float speed = 0.015f;
	
	public MoveXAxis(SceneNode p) {
		player = p;
	}
	
	@Override
	public void performAction(float time, net.java.games.input.Event e) {
		Matrix3D playerMatrix = player.getLocalRotation();
		Vector3D xAxis = new Vector3D(1, 0, 0)
				.mult(playerMatrix);
		
		if (e.getValue() < -0.2) { 
			xAxis.scale(-speed * time);
		} else {
			if (e.getValue() > 0.2) { 
				xAxis.scale(speed * time);
			}
		}
		player.translate(
				(float) xAxis.getX(), 
				(float) xAxis.getY(), 
				(float) xAxis.getZ()
				);
	}

}
