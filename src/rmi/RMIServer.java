package rmi;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import game.Controllable;
import game.GameObject;

import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * server class that handles the rmi clients
 */
public class RMIServer extends UnicastRemoteObject implements RMIController, Controllable {
	private static final long serialVersionUID = 1L;
	
	private int nextClientId = 0; //!< next free client id to use
	public HashMap<Integer, GameClientInfo> clients; //!< all clients that so far connected
	
	/**
	 * constructor
	 * @throws RemoteException
	 */
	public RMIServer() throws RemoteException {
		super();
		
		clients = new HashMap<Integer, GameClientInfo>();
	}
	
	/**
	 * refresh the client activity date (can be used to detect afk or disconnected players)
	 * @param id
	 */
	private void refreshActivityClient(int id)
	{
		clients.get(id).lastActiveTime = Calendar.getInstance().getTime();
	}
	
	/**
	 * transmit the movement params of a client
	 * @param id the client id
	 * @param param the movement data
	 */
	@Override
	public void move(int id, Point param) throws RemoteException {
		refreshActivityClient(id);
		
		clients.get(id).moveParams = param;
	}

	/**
	 * registers a new player on the server
	 * @param name client name
	 * @param _client client rmi interface
	 * @returns the client id
	 */
	@SuppressWarnings("static-access")
	@Override
	public int registerPlayer(String name, RMIClient _client) throws RemoteException {
		// Store Client information
		GameClientInfo tempClient = new GameClientInfo();
		tempClient.id = (int) nextClientId++;
		tempClient.client = _client;
		tempClient.lastActiveTime = new Date();
		tempClient.name = name;
		try {
			tempClient.ip = this.getClientHost();
		} catch (ServerNotActiveException e1) {
			e1.printStackTrace();
		}
		clients.put(tempClient.id, tempClient);
		
		// Broadcast to all clients
		for(GameClientInfo client : clients.values())
		{
			client.client.writeLog("[System] Player '"+tempClient.name+"' (" + tempClient.ip + ") connected\n");
		}
		
		return tempClient.id;
	}

	/**
	 * write a chat message to the server (all clients)
	 * @param id the client id
	 * @param message the text message
	 */
	@Override
	public void writeMessage(int id, String message) throws RemoteException {
		refreshActivityClient(id);
		message = "[" + clients.get(id).name + "] " + message + "\n";
		
		// Broadcast to all clients
		for(GameClientInfo client : clients.values())
		{
			client.client.writeLog(message);
		}
	}

	/**
	 * start the game on the server
	 * @param id the client id
	 */
	@Override
	public void start(int id) throws RemoteException {
		refreshActivityClient(id);
		String message = "[System] Player '" + clients.get(id).name + "' started the game\n";
		
		// Broadcast to all clients
		for(GameClientInfo client : clients.values())
		{
			client.client.writeLog(message);
			client.client.start();
		}
	}
	
	/**
	 * end the game on the server
	 * @param id the client id
	 */
	@Override
	public void end(int id) throws RemoteException {
		refreshActivityClient(id);
		
		// Broadcast to all clients
		for(GameClientInfo client : clients.values())
		{
			client.client.end();
		}
	}

	/**
	 * notification from controller
	 * @param command the command
	 * @param param a optional string parameter
	 * @param param2 a optional string parameter
	 */
	@Override
	public void notify(String command, String param, String param2, Object param3) {
		switch (command) {
			case "FrameCalculated" : {
				@SuppressWarnings("unchecked")
				ConcurrentHashMap<Integer, GameObject> gameObjects = (ConcurrentHashMap<Integer, GameObject>) param3;
				
				// Broadcast to all clients
				for(GameClientInfo client : clients.values())
				{
					try {
						client.client.displayFrame(gameObjects);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			case "Level" : {
				
				// Broadcast to all clients
				for(GameClientInfo client : clients.values())
				{
					try {
						client.client.level(param);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}
}
