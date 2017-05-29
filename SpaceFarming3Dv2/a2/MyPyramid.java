package a2;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import myGameEngine.CrashEvent;
import sage.event.IEventListener;
import sage.event.IGameEvent;
import sage.scene.TriMesh;

public class MyPyramid extends TriMesh implements IEventListener {
	private Random rand = new Random();
	
	private static float [] vrts = new float [] {
		0, 1, 0, -1, -1, 1, 1, -1, 1, 1, -1, -1, -1, -1, -1
	};
	private static float [] cl = new float [] {
		1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1
	};
	/*private static float [] cl1 = new float [] {
			1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1
		};*/
	private static int [] triangles = new int [] {
		0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 1, 1, 4, 2, 4, 3, 2
	};
	
	private FloatBuffer colorBuffer1;
	/*private FloatBuffer colorBuffer2 =
			com.jogamp.common.nio.
			Buffers.newDirectFloatBuffer(cl1);*/
	
	public MyPyramid() {
		//int i;
		FloatBuffer vertBuf =
				com.jogamp.common.nio.
				Buffers.newDirectFloatBuffer(vrts);
		colorBuffer1 =
				com.jogamp.common.nio.
				Buffers.newDirectFloatBuffer(cl);
		IntBuffer triangleBuf =
				com.jogamp.common.nio.
				Buffers.newDirectIntBuffer(triangles);
		
		this.setShowBound(true);
		
		this.setVertexBuffer(vertBuf);
		this.setColorBuffer(colorBuffer1);
		this.setIndexBuffer(triangleBuf);
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
		// if the event has programmer-defined information in it,
		// it must be cast to the programmer-defined event type.
		
		CrashEvent cevent = (CrashEvent) event;
		int crashCount = cevent.getWhichCrash();
		
		if (crashCount % 2 == 0) {
			this.setColorBuffer(colorBuffer1);
		} else {
			nextColor();
			//this.setColorBuffer(colorBuffer2);
		}
		return true;
	}
	
	
}
