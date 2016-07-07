package fr.unistra.ibmc.assemble2.gui.components;


import javax.swing.*;
import java.awt.*;

/**
 * A great code from https://weblogs.java.net/blog/kirillcool/archive/2005/03/how_to_create_c.html.
 *
 * Thanks Kirill Grouchnikov!!
 */
public class AssembleMenuItem extends JMenuItem {

    public AssembleMenuItem(String s) {
        super("<html>"+s+"</html");
    }

    @Override
    protected final void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        Object oldHint = graphics.getRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.fillRect(0,0,this.getWidth(), this.getHeight());
        super.paintComponent(graphics);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                oldHint);
    }
}
