package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.geom.Point2D;

public class SquarreEdgeShape extends BaseEdgeShape {

    private java.awt.geom.Point2D[] startPoints, endPoints;

    /**
     * Creates a new instance of Squarre
     */
    public SquarreEdgeShape(final Point2D base1, final Point2D base2, final java.awt.geom.Point2D startPoint, final java.awt.geom.Point2D endPoint, final double distance, final char orientation) {
        super(orientation);
        this.shape = new java.awt.geom.GeneralPath();
        this.startPoints = DrawingUtils.getPerpendicular(startPoint, base1, base2, distance / 6);
        this.endPoints = DrawingUtils.getPerpendicular(endPoint, base1, base2, distance / 6);
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoints[0].getX(), (float) endPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoints[1].getX(), (float) endPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[1].getX(), (float) startPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }
}
