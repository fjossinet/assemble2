package fr.unistra.ibmc.assemble2.model;

import java.awt.*;

public class BaseEdgeShape {

    java.awt.Shape shape;
    private char orientation = '?';

    public BaseEdgeShape() {

    }

    public BaseEdgeShape(char orientation) {
        this.orientation = orientation;
    }

    public void draw(final java.awt.Graphics2D g2) {
        if (this.orientation == BaseBaseInteraction.ORIENTATION_CIS) {
            g2.setStroke(new BasicStroke(0));
            Color c = g2.getColor();
            g2.setColor(c.brighter());
            g2.fill(this.shape);
            g2.setColor(c);
        }
        g2.setStroke(new BasicStroke(1));
        g2.draw(this.shape);
    }
}
