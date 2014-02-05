package rmi;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import game.GameObject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rmi interface for client views
 */
public interface RMIView extends Remote {
	/**
	 * starts the game on the client
	 * @throws RemoteException
	 */
	public void start() throws RemoteException;
	
	/**
	 * ends the game on the client
	 * @throws RemoteException
	 */
	public void end() throws RemoteException;
	
	/**
	 * write a text to the client chat log
	 * @param message text
	 * @throws RemoteException
	 */
	public void writeLog(String message) throws RemoteException;
	
	/**
	 * display a new game frame on the client
	 * @param data the frame data
	 * @throws RemoteException
	 */
	public void displayFrame(ConcurrentHashMap<Integer, GameObject> data) throws RemoteException;
	
	/**
	 * notify the client that a new level has been reached
	 * @param name name of the level
	 * @throws RemoteException
	 */
	public void level(String name) throws RemoteException;
}
