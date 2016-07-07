package fr.unistra.ibmc.assemble2.model;

public class LineEdgeShape extends BaseEdgeShape {

    public LineEdgeShape(final java.awt.geom.Point2D p1, final java.awt.geom.Point2D p2) {
        this.shape = new java.awt.geom.Line2D.Double(p1, p2);
    }

    public void draw(final java.awt.Graphics2D g2) {
        g2.draw(this.shape);
    }

}
