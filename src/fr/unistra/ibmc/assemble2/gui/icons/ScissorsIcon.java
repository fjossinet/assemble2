package fr.unistra.ibmc.assemble2.gui.icons;

import fr.unistra.ibmc.assemble2.utils.SvgPath;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ScissorsIcon implements Icon {

    private int width = 32;
    private int height = 32;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setColor(Color.BLACK);

        try {
            Shape create = new SvgPath("M11.108,10.271c1.083-1.876,0.159-4.443-2.059-5.725C8.231,4.074,7.326,3.825,6.433,3.825c-1.461,0-2.721,0.673-3.373,1.801C2.515,6.57,2.452,7.703,2.884,8.814C3.287,9.85,4.081,10.751,5.12,11.35c0.817,0.473,1.722,0.723,2.616,0.723c0.673,0,1.301-0.149,1.849-0.414c0.669,0.387,1.566,0.904,2.4,1.386c1.583,0.914,0.561,3.861,5.919,6.955c5.357,3.094,11.496,1.535,11.496,1.535L10.75,10.767C10.882,10.611,11.005,10.449,11.108,10.271zM9.375,9.271c-0.506,0.878-2.033,1.055-3.255,0.347C5.474,9.245,4.986,8.702,4.749,8.09C4.541,7.555,4.556,7.035,4.792,6.626c0.293-0.509,0.892-0.801,1.64-0.801c0.543,0,1.102,0.157,1.616,0.454C9.291,6.996,9.898,8.366,9.375,9.271zM17.246,15.792c0,0.483-0.392,0.875-0.875,0.875c-0.037,0-0.068-0.017-0.104-0.021l0.667-1.511C17.121,15.296,17.246,15.526,17.246,15.792zM16.371,14.917c0.037,0,0.068,0.017,0.104,0.021l-0.666,1.51c-0.188-0.16-0.312-0.39-0.312-0.656C15.496,15.309,15.887,14.917,16.371,14.917zM29.4,10.467c0,0-6.139-1.559-11.496,1.535c-0.537,0.311-0.995,0.618-1.415,0.924l4.326,2.497L29.4,10.467zM13.171,17.097c-0.352,0.851-0.575,1.508-1.187,1.859c-0.833,0.481-1.73,0.999-2.399,1.386c-0.549-0.265-1.176-0.414-1.85-0.414c-0.894,0-1.798,0.249-2.616,0.721c-2.218,1.282-3.143,3.851-2.06,5.726c0.651,1.127,1.912,1.801,3.373,1.801c0.894,0,1.799-0.25,2.616-0.722c1.04-0.601,1.833-1.501,2.236-2.536c0.432-1.112,0.368-2.245-0.178-3.189c-0.103-0.178-0.226-0.34-0.356-0.494l3.982-2.3C14.044,18.295,13.546,17.676,13.171,17.097zM9.42,24.192c-0.238,0.612-0.725,1.155-1.371,1.528c-1.221,0.706-2.75,0.532-3.257-0.347C4.27,24.47,4.878,23.099,6.12,22.381c0.514-0.297,1.072-0.453,1.615-0.453c0.749,0,1.346,0.291,1.64,0.8C9.612,23.138,9.628,23.657,9.42,24.192z").getShape();
            g2d.translate(x, y);
            g2d.fill(create);
            g2d.translate(-x, -y);
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
