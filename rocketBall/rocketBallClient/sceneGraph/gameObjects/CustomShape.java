package rocketBallClient.sceneGraph.gameObjects;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import graphicslib3D.Matrix3D;
import rocketBallClient.events.CrashEvent;
import sage.event.IEventListener;
import sage.event.IGameEvent;
import sage.scene.TriMesh;

public class CustomShape extends TriMesh implements IEventListener {
	Random rand = new Random();
	private float [] vrts = /*{
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,    //front
				-1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f,    //front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
				1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,      // smaller right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
				-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    // smaller left
				-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,      // top
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
   		};*/
		{
				-1.0f, -1.0f, 1.0f,
				1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 
				-1.0f, 1.0f, 1.0f,
				1.0f, -1.0f, -1.0f,
				0.0f, 1.0f, 0.0f,
				-1.0f, -1.0f, -1.0f
		};
	
	private float [] cl = new float [(vrts.length / 3) * 4];
	
	private int [] index = //new int [vrts.length];
		{ 	0,1,2,
			0,2,3,
			1,4,5,
			2,1,5,
			//4,6,5, // the door
			6,0,5,
			0,3,5,
			3,2,5,
			6,1,0,
			1,6,4};


	public CustomShape() {
		FloatBuffer vertBuf =
				com.jogamp.common.nio.
				Buffers.newDirectFloatBuffer(vrts);

		// make random color		
		int i = 0;
		while (i < cl.length) {
			
			float [] put = { rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1.0f };
			cl[i] = put[0];
			cl[i + 1] = put[1];
			cl[i + 2] = put[2];
			cl[i + 3] = put[3];
			
			i += 4;
		}
		
		FloatBuffer colorBuffer1 =
				com.jogamp.common.nio.
				Buffers.newDirectFloatBuffer(cl);
		
		IntBuffer triangleBuf =
				com.jogamp.common.nio.
				Buffers.newDirectIntBuffer(index);
		
		this.setRenderMode(RENDER_MODE.TRANSPARENT);

		this.setVertexBuffer(vertBuf);
		this.setColorBuffer(colorBuffer1);
		this.setIndexBuffer(triangleBuf);
		
	}
   
	public float[] getVertices() {
		return vrts;
	}
	
	public void nextColor() {
		int i = 0;
		while (i < cl.length) {
			
			float [] put = { rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1.0f };
			cl[i] = put[0];
			cl[i + 1] = put[1];
			cl[i + 2] = put[2];
			cl[i + 3] = put[3];
			
			i += 4;
		}
		
		FloatBuffer newColorBuffer =
				com.jogamp.common.nio.
				Buffers.newDirectFloatBuffer(cl);
		
		this.setColorBuffer(newColorBuffer);
		
	}

	@Override
	public boolean handleEvent(IGameEvent event) {
		CrashEvent cevent = (CrashEvent) event;
		int crashCount = cevent.getWhichCrash();
		
		Matrix3D test = this.getLocalTranslation();
		test.scale((1 + (crashCount / 10)), (1 + (crashCount / 10)), (1 + (crashCount / 10)));
		this.setLocalTranslation(test);
		return true;
	}
	
	public void debugStuff() {

		System.out.println("vertices: " + vrts.length);
		int j = 0;
		while (j < cl.length) {
			System.out.println(cl[j]);
			j++;
		}
		int h = 0;
		while (h < index.length) {
			System.out.println(index[h]);
			h++;
		}

	}

}
