package rocketBallClient.inputActions;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.SceneNode;

public class RightAction extends AbstractInputAction implements IAction {
	
	private SceneNode player;
	private double speed = 0.015;

	public RightAction(SceneNode player) {
		this.player = player;
	}

	public void performAction(float time, Event e) {
		Matrix3D playerMatrix = player.getLocalRotation();
		Vector3D xAxis = new Vector3D(1, 0, 0)
				.mult(playerMatrix);
		xAxis.scale(-speed * time);
		
		player.translate(
				(float) xAxis.getX(), 
				(float) xAxis.getY(), 
				(float) xAxis.getZ()
				);
	}

}
