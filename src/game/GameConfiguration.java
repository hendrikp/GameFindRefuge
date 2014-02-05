package game;

/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.io.IOException;
import java.lang.reflect.Field;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * game configuration class (static class singleton)
 */
public class GameConfiguration {
	private GameConfiguration() {}
	
	public static int gameCycleTime = 50; //!< how fast does the game update in ms
	
	public static int gameSizeX = 1000; //!< size in pixel
	public static int gameSizeY = 600; //!< size in pixel
	
	public static int levelSafeZone = 60; //!< safe zone for initial/level spawn
	public static int levelStartHouses = 4; //!< house count in first level (gets decreased for each level)
	public static int levelStartTrees = 10; //!< tree count in first level (gets decreased for each level)
	public static int levelMinEnemys = 20; //!< minimal enemy count present in first level
	public static int levelEnemyMultiplier = 2; //!< level * this is the increased enemy count per level
	public static int playerSpeed = 3; //!< the max. speed of the player in pixels per frame
	public static int minCollisionArea = 200; //!< the minimal area to register a collision with the player
	
	/**
	 * reads the game configuration from the gamekonfig.xml file
	 * it will read all static int params declared in this configuration class using reflection
	 */
	public static void readGameConfig()
	{
		// prepare XML DOM
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
		Document doc = null;
		if(builder != null)
		{
			try {
				doc = builder.parse("gamekonfig.xml");
			} catch (SAXException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		// Now read all field using xpath and reflection
		XPathFactory xpfactory = XPathFactory.newInstance();
	    XPath xpath = xpfactory.newXPath();
		if(doc != null) {
			String c = "//config/";

			Field[] declaredFields = GameConfiguration.class.getDeclaredFields();
			for (Field field : declaredFields) {
			    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			    	try {
			    		int val = Integer.parseInt( xpath.evaluate( c + field.getName(), doc ).trim());
						field.set(null, val);
						
						System.out.println( field.getName() + " = " + val);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
			    }
			}
		}
	}
}
