package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Race controller class (starts and stops race)
 */
public class Controller implements ActionListener {	
	private Vector<Controllable> controllables; //!< all controllable registered in this controller
	private static Controller controller; //!< current controller
	
	/**
	 * get instance of last created controller
	 * @return current controller
	 */
	public static Controller getInstance()
	{
		return controller;
	}
	
	/**
	 * Constructor of controller
	 */
	public Controller() {
		controllables = new Vector<Controllable>();
		controller = this;
	}
	
	/**
	 * add controllable to this controller
	 * @param controllable the controllable to add
	 */
	public void addControllable(Controllable controllable)
	{
		controllables.add(controllable);
	}
	
	/**
	 * Action triggered by view
	 * @param e action event to perform
	 */
	public void actionPerformed(ActionEvent e) {
		actionPerformed(e.getActionCommand(), "");
	}

	/**
	 * Action triggered by view
	 * @param e action event to perform
	 */
	public void actionPerformed(ActionEvent e, String param) {
		actionPerformed(e.getActionCommand(), param);
	}
	
	/**
	 * notify all controllable
	 * @param command the command
	 * @param param optional string parameter
	 * @param param2 optional string parameter
	 * @param param3 optional object parameter
	 */
	private void notifyAll(String command, String param, String param2, Object param3)
	{
		for(int i = 0; i < controllables.size(); i++)
		{
			controllables.get(i).notify(command, param, param2, param3);
		}
	}
	
	/**
	 * Action triggered by view
	 * @param e action event to perform
	 */
	public void actionPerformed(String command) 
	{
		System.out.println(command + "()");
		notifyAll(command, "", "", null);
	}

	// see notifyAll for variations
	public void actionPerformed(String command, String param) 
	{
		System.out.println(command + "(" + param + ")");
		notifyAll(command, param, "", null);
	}
	
	public void actionPerformed(String command, String param, String param2) 
	{
		System.out.println(command + "(" + param + ", "  + param2 + ")");
		notifyAll(command, param, param2, null);
	}
	
	public void actionPerformed(String command, Object param) 
	{
		notifyAll(command, "", "", param);
	}
}
