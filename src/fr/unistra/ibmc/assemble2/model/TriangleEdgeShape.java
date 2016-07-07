package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.geom.Point2D;

public class TriangleEdgeShape extends BaseEdgeShape {
    private java.awt.geom.Point2D[] startPoints;
    private java.awt.geom.Point2D endPoint;

    /**
     * Creates a new instance of Triangle
     */
    public TriangleEdgeShape(final Point2D base1, final Point2D base2, final java.awt.geom.Point2D startPoint, final java.awt.geom.Point2D endPoint, final double distance, final char orientation) {
        super(orientation);
        this.shape = new java.awt.geom.GeneralPath();
        this.endPoint = endPoint;
        this.startPoints = DrawingUtils.getPerpendicular(startPoint, base1, base2, distance / 6);
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[1].getX(), (float) startPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoint.getX(), (float) endPoint.getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }
}
