package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.util.ArrayList;
import java.util.List;

public class CircleEdgeShape extends BaseEdgeShape {

    static private int steps = 30;
    private java.awt.geom.Point2D firstPoint, centerPoint;

    /**
     * Creates a new instance of Circle
     */
    public CircleEdgeShape(final java.awt.geom.Point2D firstPoint, final java.awt.geom.Point2D centerPoint, final char orientation) {
        super(orientation);
        this.firstPoint = firstPoint;
        this.centerPoint = centerPoint;
        this.shape = new java.awt.geom.GeneralPath();
        final List<Double> points = new ArrayList<Double>(1);
        for (int i = 1; i <= steps; i++) {
            final java.awt.geom.Point2D point = DrawingUtils.getRotatedPoint(this.firstPoint, 2 * Math.PI / steps * i, this.centerPoint);
            points.add(point.getX());
            points.add(point.getY());
        }
        final Double[] coordinates = points.toArray(new Double[]{});
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
        for (int i = 0; i < coordinates.length; i = i + 2) {
            ((java.awt.geom.GeneralPath) this.shape).lineTo((float) coordinates[i].doubleValue(), (float) coordinates[i + 1].doubleValue());
        }
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }
}
