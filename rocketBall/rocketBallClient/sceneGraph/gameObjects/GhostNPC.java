package rocketBallClient.sceneGraph.gameObjects;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.scene.shape.Cube;

public class GhostNPC extends Cube{
//	public Cube body;
	public int id;
	
	public GhostNPC(int id, Vector3D position) {
		this.id = id;
//		this.body = new Cube(Integer.toString(id));
		setPosition(position);
	}

	public void setPosition(Vector3D position) {
		Matrix3D trans = new Matrix3D();
		trans.translate(position.getX(), position.getY(), position.getZ());
		this.setLocalTranslation(trans);
		
	}
	
	
}
