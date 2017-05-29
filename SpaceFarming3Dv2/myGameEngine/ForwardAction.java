package myGameEngine;


import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.SceneNode;

public class ForwardAction extends AbstractInputAction implements IAction {
	
	private SceneNode player;
	private double speed = 0.015;
	
	public ForwardAction(SceneNode c) {
		this.player = c;
	}
	
	@Override
	public void performAction(float time, Event e) {
		Matrix3D playerMatrix = player.getLocalRotation();
		Vector3D zAxis = new Vector3D(0, 0, 1)
				.mult(playerMatrix);
		zAxis.scale(speed * time);
		
		player.translate(
				(float) zAxis.getX(), 
				(float) zAxis.getY(), 
				(float) zAxis.getZ()
				);
	}
}
