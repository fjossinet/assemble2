package fr.unistra.ibmc.assemble2.gui.icons;

import fr.unistra.ibmc.assemble2.utils.SvgPath;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ConfigureIcon implements Icon {

    private int width = 32;
    private int height = 32;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setColor(Color.BLACK);

        try {
            Shape create = new SvgPath("M28.537,9.859c-0.473-0.259-1.127-0.252-1.609-0.523c-0.943-0.534-1.186-1.316-1.226-2.475c-2.059-2.215-5.138-4.176-9.424-4.114c-1.162,0.017-2.256-0.035-3.158,0.435c-0.258,0.354-0.004,0.516,0.288,0.599c-0.29,0.138-0.692,0.147-0.626,0.697c2.72-0.383,7.475,0.624,7.116,2.966c-0.08,0.521-0.735,1.076-1.179,1.563c-1.263,1.382-2.599,2.45-3.761,3.667l0.336,0.336c0.742-0.521,1.446-0.785,2.104-0.785c0.707,0,1.121,0.297,1.276,0.433c0.575-0.618,1.166-1.244,1.839-1.853c0.488-0.444,1.047-1.099,1.566-1.178l0.949-0.101c1.156,0.047,1.937,0.29,2.471,1.232c0.27,0.481,0.262,1.139,0.521,1.613c0.175,0.324,0.937,1.218,1.316,1.228c0.294,0.009,0.603-0.199,0.899-0.49l1.033-1.034c0.291-0.294,0.501-0.6,0.492-0.896C29.754,10.801,28.861,10.035,28.537,9.859zM13.021,15.353l-0.741-0.741c-3.139,2.643-6.52,5.738-9.531,8.589c-0.473,0.443-1.452,1.021-1.506,1.539c-0.083,0.781,0.95,1.465,1.506,2c0.556,0.533,1.212,1.602,1.994,1.51c0.509-0.043,1.095-1.029,1.544-1.502c2.255-2.374,4.664-4.976,6.883-7.509c-0.312-0.371-0.498-0.596-0.498-0.596C12.535,18.451,11.779,17.272,13.021,15.353zM20.64,15.643c-0.366-0.318-1.466,0.143-1.777-0.122c-0.311-0.266,0.171-1.259-0.061-1.455c-0.482-0.406-0.77-0.646-0.77-0.646s-0.862-0.829-2.812,0.928L7.44,6.569C7.045,6.173,7.203,4.746,7.203,4.746L3.517,2.646L2.623,3.541l2.1,3.686c0,0,1.428-0.158,1.824,0.237l7.792,7.793c-1.548,1.831-0.895,2.752-0.895,2.752s0.238,0.288,0.646,0.771c0.196,0.23,1.188-0.249,1.455,0.061c0.264,0.312-0.196,1.41,0.12,1.777c2.666,3.064,6.926,7.736,8.125,7.736c0.892,0,2.021-0.724,2.948-1.64c0.925-0.917,1.639-2.055,1.639-2.947C28.377,22.567,23.704,18.309,20.64,15.643z").getShape();
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
