package rmi;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * rmi server interface
 */
public interface RMIController extends Remote {
	/**
	 * registers a new player on the server
	 * @param name client name
	 * @param _client client rmi interface
	 * @returns the client id
	 */
	public int registerPlayer(String name, RMIClient client) throws RemoteException;
	
	/**
	 * write a chat message to the server (all clients)
	 * @param id the client id
	 * @param message the text message
	 */
	public void writeMessage(int id, String message) throws RemoteException;
	
	/**
	 * transmit the movement params of a client
	 * @param id the client id
	 * @param param the movement data
	 */
	public void move(int id, Point params) throws RemoteException;
	
	/**
	 * start the game on the server
	 * @param id the client id
	 */
	public void start(int id) throws RemoteException;
	
	/**
	 * end the game on the server
	 * @param id the client id
	 */
	public void end(int id) throws RemoteException;
}
