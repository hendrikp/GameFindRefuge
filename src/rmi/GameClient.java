package rmi;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import game.Controller;
import game.GameObject;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

/**
 * implementation of the game clients
 * they receive the commands from the server and
 * broadcast them through the controller
 */
public class GameClient implements RMIClient {
	/**
	 * constructor
	 */
    public GameClient() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

	/**
	 * display a new game frame on the client
	 * @param data the frame data
	 * @throws RemoteException
	 */
	@Override
	public void displayFrame(ConcurrentHashMap<Integer, GameObject> data) throws RemoteException {
		Controller.getInstance().actionPerformed("FrameReceived", data);
	}
	
	/**
	 * write a text to the client chat log
	 * @param message text
	 * @throws RemoteException
	 */
	@Override
	public void writeLog(String message) {
		Controller.getInstance().actionPerformed("Message", message);
	}
	
	/**
	 * starts the game on the client
	 * @throws RemoteException
	 */
	@Override
	public void start() throws RemoteException {
		Controller.getInstance().actionPerformed("Started");
	}
	
	/**
	 * ends the game on the client
	 * @throws RemoteException
	 */
	@Override
	public void end() throws RemoteException {
		Controller.getInstance().actionPerformed("Ended");
	}
	
	/**
	 * notify the client that a new level has been reached
	 * @param name name of the level
	 * @throws RemoteException
	 */
	@Override
	public void level(String name) throws RemoteException {
		Controller.getInstance().actionPerformed("LevelReceived", name);
	}
}
