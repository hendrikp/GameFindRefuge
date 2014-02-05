package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

/**
 * Controllable interface to be registered with the Controller
 */
public interface Controllable {
	/**
	 * notify the controllable
	 * @param command controller command or End
	 * @param param optional string parameter
	 * @param param2 optional string parameter
	 * @param param3 optional object parameter
	 */
	public abstract void notify(String command, String param, String param2, Object param3);
}
