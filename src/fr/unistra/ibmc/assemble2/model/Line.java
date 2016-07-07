package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.geom.Line2D;

public class Line extends AbstractShape {

    public Line(Mediator mediator, final java.awt.geom.Point2D p1, final java.awt.geom.Point2D p2, GraphicContext gc) {
        super(mediator,gc);
        this.shape = new Line2D.Double(p1, p2);
    }

    public void draw(final java.awt.Graphics2D g2, char orientation) {
        g2.draw(this.shape);
    }

    public String getSVG(BaseBaseInteraction bbi) {
        return "<path d=\"M"+((Line2D.Double)this.shape).getX1()+","+((Line2D.Double)this.shape).getY1()+"L"+((Line2D.Double)this.shape).getX2()+","+((Line2D.Double)this.shape).getY2()+"\" style=\"fill:none;stroke-width:1px;stroke:#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+";\"/>";
    }

}
