package rocketBallServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.vecmath.Point3d;

import graphicslib3D.Point3D;
import sage.networking.server.GameConnectionServer;
import sage.networking.server.IClientInfo;
import sage.scene.shape.Cube;

public class GameServer extends GameConnectionServer<UUID> {
	
	private int numNPC = 10;
	
	private Cube [] NPClist = new Cube [numNPC];
	private Point3D [] npcLocations = new Point3D [numNPC];
	private int[] npcHeading = new int[numNPC];
	private Random rand = new Random();
	private long startTime;
	private long lastUpdateTime;
	private NPCcontroller npcCtrl;
	private int xMod = 1;
	private ArrayList<Integer> npcsDisabled = new ArrayList<Integer>();

	public GameServer(int localPort) throws IOException {
		super(localPort, ProtocolType.TCP);
		startTime = System.nanoTime();
		lastUpdateTime = startTime;
//		npcCtrl = new NPCcontroller();
//		npcCtrl.setupNPCs();
		setupNPCs();
		npcLoop();
	}

	public void npcLoop() {
		while (true) {
			long frameStartTime = System.nanoTime();
			float elapMilSecs = (frameStartTime - lastUpdateTime) / (1000000.0f);
			
			if (elapMilSecs >= 50.0f) {
				lastUpdateTime = frameStartTime;
//				npcCtrl.updateNPCs();
				sendNPCinfo();
			}
			Thread.yield();
		}
			
	}
	
	public void setupNPCs() {
		for (int i = 0; i < NPClist.length; i++) {
			npcLocations[i] = new Point3D(rand.nextInt(100), 0, rand.nextInt(100));
			npcHeading[i] = 1;
		}
	}
	
	public double evalNpcLoc(double c, int index) {
		double out;
		
		if (c + 1 >= 100) {
			npcHeading[index] = -1;
		}
		if (c - 1 <= 0) {
			npcHeading[index] = 1;
		}
		
		if (c < 100 && c > 0) {
			out = c + (1*npcHeading[index]);
		} else {
			out = c;
		}
		
		
		
		return out;
	}

	public void sendNPCinfo() {
		for (int i = 0; i < NPClist.length; i++) {
			
			if (this.npcsDisabled.contains(i) == false) {
				try {
					String message = new String("mnpc," + Integer.toString(i));
					double temp = evalNpcLoc(npcLocations[i].getX(), i);
//					System.out.println(temp);
					npcLocations[i].setX(temp);
					message += "," + temp;//(npcCtrl.getNPC(i)).getX();
					message += "," + npcLocations[i].getY();//(npcCtrl.getNPC(i)).getY();
					message += "," + npcLocations[i].getZ();//rand.nextInt(100);//(npcCtrl.getNPC(i)).getZ();
					sendPacketToAll(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

	// override
	public void acceptClient(IClientInfo ci, Object o) {
		String message = (String) o;
		String [] messageTokens = message.split(",");
		
		if (messageTokens.length > 0) {
			// received "join"
			if (messageTokens[0].compareTo("join") == 0) {
				// format: join,localid
				UUID clientID = UUID.fromString(messageTokens[1]);
				addClient(ci, clientID);
				sendJoinedMessage(clientID, true);
			}
		}
	}

	public void processPacket(Object o, InetAddress senderIP, int sndPort) {
		String message = (String) o;
		String [] msgTokens = message.split(",");
		
		if (msgTokens.length > 0) {
			
			UUID clientID = UUID.fromString(msgTokens[1]);

			switch(msgTokens[0]) {
				case "needNPC":
					//TODO npc
					break;
				case "collide":
					System.out.println(msgTokens[2]);
					npcsDisabled.add(Integer.parseInt(msgTokens[2]));
					break;
				// receive "bye"
				case "bye":
					// format: bye,localid
					sendByeMessages(clientID);
					removeClient(clientID);
					break;
				// receive "create"
				case "create":
					// format: create,localid,x,y,z
					String [] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
					sendCreateMessages(clientID, pos);
					sendWantsDetailsMessages(clientID);
					break;
				
				// receive "details for"
				case "dsfr":
					UUID remoteID = UUID.fromString(msgTokens[2]);
					String [] position = {msgTokens[3], msgTokens[4], msgTokens[5]};
					
					sendDetailsMessage(clientID, remoteID, position);
					break;
				// receive "move"	
				case "move":
					String [] movePos = {msgTokens[2], msgTokens[3], msgTokens[4]};
					sendMoveMessages(clientID, movePos);
					break;

			}
		}
	}

	public void sendJoinedMessage(UUID clientID, boolean success) {
		// format: 	join, success
		// or
		//			join, failure
		try {
			String message = new String("join,");
			
			if (success) {
				message += "success";
			} else {
				message += "failure";
			}

			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendCreateMessages(UUID clientID, String [] position) {
		// format: create, remoteId, x, y, z
		try {
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playerCloseToNPC(Point3D p) {
		for (int i = 0; i < NPClist.length; i++) {
			
			if (p.getX() < npcLocations[i].getX() + 5 
					&& p.getX() > npcLocations[i].getX()
					&& p.getZ() < npcLocations[i].getZ() + 5 
					&& p.getZ() > npcLocations[i].getZ()) {
				npcHeading[i] = npcHeading[i] * -1;
				
			}
			else if (p.getX() > npcLocations[i].getX() - 5 
					&& p.getX() < npcLocations[i].getX()
					&& p.getZ() > npcLocations[i].getZ() - 5 
					&& p.getZ() < npcLocations[i].getZ()) {
				npcHeading[i] = npcHeading[i] * -1;
			}

			
			
			
		}
	}
	
	public void sendDetailsMessage(UUID clientID, UUID remoteId, String [] position) {
		try {
			String message = new String("dsfr," + clientID.toString());
			message += "," + Float.parseFloat(position[0]);
			message += "," + Float.parseFloat(position[1]);
			message += "," + Float.parseFloat(position[2]);
			
			
			
			sendPacket(message, remoteId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendWantsDetailsMessages(UUID clientID) {
		try {
			String message = new String("wsds," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMoveMessages(UUID clientID, String [] position) {
		try {
			String message = new String("move," + clientID.toString());
			message += "," + Float.parseFloat(position[0]);
			message += "," + Float.parseFloat(position[1]);
			message += "," + Float.parseFloat(position[2]);
			Point3D playerPos = new Point3D(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2]));
			playerCloseToNPC(playerPos);
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendByeMessages(UUID clientID) {
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	class NPCcontroller {
		private NPC [] NPClist = new NPC [5];
		public Random rand = new Random();
		public NPCcontroller() {
			
		}
		
		public NPC getNPC(int i) {
			return NPClist[i];
		}

		public int getNumOfNPCs() {
			return NPClist.length;
		}

		public void setupNPCs() {
			for (int i = 0; i < NPClist.length; i++) {
				NPClist[i].location = new Point3D(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
			}
			
		}

		public void updateNPCs() {
			for (int i = 0; i < NPClist.length; i++) {
				NPClist[i].updateLocation();
			}
		}
		
	}
	
	class NPC {
		Point3D location;
		public Random rand = new Random();
		public double getX() {
			return location.getX();
		}
		
		public double getY() {
			return location.getY();
		}
		
		public double getZ() {
			return location.getZ();
		}
		
		public void updateLocation() {
			location = new Point3D(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
		}
	}
	
	
}

