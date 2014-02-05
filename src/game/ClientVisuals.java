package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.net.URL;

/**
 * Each client needs its own texture sheet since they move in different directions
 * This class holds per client visual information
 */
public class ClientVisuals {
	public SheetTexture icon; //!< the texture sheet to be used for the player
	
	/**
	 * Create a new texture sheet for a player
	 * @param urlIcon the url
	 * @param cells how many cells(x-direction) does the sheet have
	 */
	public ClientVisuals(URL urlIcon, int cells)
	{
		icon = new SheetTexture(urlIcon, cells);
	}
}
