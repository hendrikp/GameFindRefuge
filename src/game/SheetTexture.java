package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Texture sheet for animated characters
 */
@SuppressWarnings("serial")
class SheetTexture extends ImageIcon {
	private int cells; //!< cells in x direction
	private int cell; //!< currently active cell
	
	/**
	 * Create a new texture sheet for an character
	 * @param resource the url to the image
	 * @param cells_ how much cells are present in the image x axis
	 */
    public SheetTexture(URL resource, int cells_) {
    	super(resource);
    	cells = cells_;
	}

    /**
     * set the current animation cell
     * @param cell_ cell in x direction (starting with 0)
     */
    public void setCell(int cell_)
    {
    	cell = cell_;
    }
    
    /**
     * paint the cell
     */
	@Override
    public void paintIcon(Component comp, Graphics g, int x, int y) {
    	Graphics2D grap = (Graphics2D)g.create();
    	grap.translate(-getIconWidth()*cell, 0);
    	super.paintIcon(comp, grap, x, y);
    }

	/**
	 * get size of one animation cell
	 * @return the width of one cell
	 */
	@Override
	public int getIconWidth() {
		return super.getIconWidth() / cells;
	}
}