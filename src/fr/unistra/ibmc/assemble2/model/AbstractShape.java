package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.geom.Area;

public abstract class AbstractShape implements Shape {

    protected java.awt.Shape shape;
    protected GraphicContext gc;
    protected Mediator mediator;

    AbstractShape(Mediator mediator, GraphicContext gc) {
        this.mediator = mediator;
        this.gc = gc;
    }

    public boolean contains(final double x, final double y) {
        return this.shape.contains(x, y);
    }

    public void draw(final java.awt.Graphics2D g2, char orientation) {
        if (orientation == BaseBaseInteraction.ORIENTATION_CIS)
            g2.fill(this.shape);
        else if (orientation == BaseBaseInteraction.ORIENTATION_TRANS)
            g2.draw(this.shape);
    }

    public java.awt.Shape getShape() {
        return shape;
    }

    public String getSVG(BaseBaseInteraction bbi) {
        return "";
    }
}
