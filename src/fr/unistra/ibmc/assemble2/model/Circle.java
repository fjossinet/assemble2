/*
 * Circle.java
 *
 * Created on 29 janvier 2003, 10:20
 */

package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

/**
 * @author fabricej
 */
class Circle extends AbstractShape {

    static private int steps = 30;
    protected java.awt.geom.Point2D firstPoint, centerPoint;

    Circle(Mediator mediator, final java.awt.geom.Point2D firstPoint, final java.awt.geom.Point2D centerPoint, GraphicContext gc) {
        super(mediator, gc);
        this.firstPoint = firstPoint;
        this.centerPoint = centerPoint;
        this.shape = new java.awt.geom.GeneralPath();
        final DoubleArray points = new DoubleArray(1);
        for (int i = 1; i <= steps; i++) {
            final java.awt.geom.Point2D point = DrawingUtils.getRotatedPoint(this.firstPoint, 2 * Math.PI / steps * i, this.centerPoint);
            points.add(point.getX());
            points.add(point.getY());
        }
        final double[] coordinates = points.getArray();
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
        for (int i = 0; i < coordinates.length; i = i + 2) {
            ((java.awt.geom.GeneralPath) this.shape).lineTo((float) coordinates[i], (float) coordinates[i + 1]);
        }
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }

    static private class DoubleArray {

        private int position;
        private double[] array;

        private DoubleArray(final int size) {
            this.array = new double[size];
        }

        private void add(final double value) {
            if (position >= this.array.length) {
                final double[] tmp = new double[this.array.length + (position + 1 - this.array.length)];
                System.arraycopy(this.array, 0, tmp, 0, this.array.length);
                this.array = tmp;
            }
            this.array[position++] = value;
        }

        private int length() {
            return this.array.length;
        }

        private double[] getArray() {
            return this.array;
        }
    }

    public String getSVG(BaseBaseInteraction bbi) {
        if (bbi.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS)
            return "<circle cx=\""+this.centerPoint.getX()+"\" cy=\""+this.centerPoint.getY()+"\" r=\""+DrawingUtils.getDistance(this.centerPoint, this.firstPoint)+"\" fill=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"0\"/>";
        else
            return "<circle cx=\""+this.centerPoint.getX()+"\" cy=\""+this.centerPoint.getY()+"\" r=\""+DrawingUtils.getDistance(this.centerPoint, this.firstPoint)+"\" fill=\"none\" stroke=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"1\"/>";
    }

}
