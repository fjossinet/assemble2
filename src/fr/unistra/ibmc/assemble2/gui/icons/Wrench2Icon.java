package fr.unistra.ibmc.assemble2.gui.icons;

import fr.unistra.ibmc.assemble2.utils.SvgPath;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Wrench2Icon implements Icon {

    private int width = 32;
    private int height = 32;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setColor(Color.BLACK);

        try {
            Shape create = new SvgPath("M24.946,9.721l-2.872-0.768l-0.771-2.874l3.188-3.231c-1.992-0.653-4.268-0.192-5.848,1.391c-1.668,1.668-2.095,4.111-1.279,6.172l-3.476,3.478l-3.478,3.478c-2.062-0.816-4.504-0.391-6.173,1.277c-1.583,1.581-2.043,3.856-1.39,5.849l3.231-3.188l2.874,0.77l0.769,2.872l-3.239,3.197c1.998,0.665,4.288,0.207,5.876-1.384c1.678-1.678,2.1-4.133,1.271-6.202l3.463-3.464l3.464-3.463c2.069,0.828,4.523,0.406,6.202-1.272c1.592-1.589,2.049-3.878,1.384-5.876L24.946,9.721z").getShape();
            g2d.translate(x ,y);
            g2d.fill(create);
            g2d.translate(-x ,-y);
        } catch (IOException e) {
            e.printStackTrace();
        }

        g2d.dispose();
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
}
