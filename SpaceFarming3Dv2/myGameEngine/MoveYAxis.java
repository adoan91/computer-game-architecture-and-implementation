package myGameEngine;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.SceneNode;

public class MoveYAxis extends AbstractInputAction implements IAction {
	private float speed = 0.015f;
	private SceneNode player;
	public MoveYAxis(SceneNode p) {
		player = p;
	}
	
	@Override
	public void performAction(float time, net.java.games.input.Event e) {
		Matrix3D playerMatrix = player.getLocalRotation();
		Vector3D zAxis = new Vector3D(0, 0, 1)
				.mult(playerMatrix);

		if (e.getValue() < -0.2) { 
			zAxis.scale(-speed * time);
		} else {
			if (e.getValue() > 0.2) { 
				zAxis.scale(speed * time);
			} 
		}

		player.translate(
				(float) zAxis.getX(), 
				(float) zAxis.getY(), 
				(float) zAxis.getZ()
				);
	}

}
