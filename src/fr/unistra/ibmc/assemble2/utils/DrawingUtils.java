package fr.unistra.ibmc.assemble2.utils;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

public class DrawingUtils {



    /**
     * Draw a String according to its center and not its lower left corner
     * @param center
     * @param s
     * @param g
     */
    public static void drawString(Point2D center, String s, Graphics2D g) {
        final FontMetrics fm = g.getFontMetrics(g.getFont());
        g.drawString(s,(float)(center.getX()-(float)(fm.stringWidth(s))/2f), (float)(center.getY()+ (float) (fm.getAscent() + fm.getDescent()) / 2f));
    }

    public static double getDistance(final double x1, final double y1, final double x2, final double y2) {
        final double horizontal = x1 - x2;
        final double vertical = y1 - y2;
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    public static double getDistance(final Point2D p1, final Point2D p2) {
        return getDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static Point2D[] fit(final double x1, final double y1, final double x2, final double y2, final double distance) {
        final Point2D[] drawingPoints = new Point2D[2];
        final double angle = getAngle(getDistance(x1, y1, x1, y2), getDistance(x1, y2, x2, y2));
        final double newx1;
        final double newy1;
        final double newx2;
        final double newy2;

        if (x1 >= x2) {
            newx2 = x2 + DrawingUtils.getAdjacentSide(angle, distance);
            newx1 = x1 - DrawingUtils.getAdjacentSide(angle, distance);
        } else {
            newx2 = x2 - DrawingUtils.getAdjacentSide(angle, distance);
            newx1 = x1 + DrawingUtils.getAdjacentSide(angle, distance);
        }
        if (y1 >= y2) {
            newy2 = y2 + DrawingUtils.getOppositeSide(angle, distance);
            newy1 = y1 - DrawingUtils.getOppositeSide(angle, distance);
        } else {
            newy2 = y2 - DrawingUtils.getOppositeSide(angle, distance);
            newy1 = y1 + DrawingUtils.getOppositeSide(angle, distance);
        }

        drawingPoints[0] = new Point2D.Double(newx1, newy1);
        drawingPoints[1] = new Point2D.Double(newx2, newy2);

        return drawingPoints;
    }

    public static Point2D[] fit(final Point2D p1, final Point2D p2, final double distance) {
        return fit(p1.getX(), p1.getY(), p2.getX(), p2.getY(), distance);
    }

    public static double getAdjacentSide(final double angle, final double hypothenuse) {
        return Math.cos(angle) * hypothenuse;
    }

    public static double getAngle(final double oppositeSide, final double adjacentSide) {
        return Math.atan(oppositeSide / adjacentSide);
    }

    public static double getOppositeSide(final double angle, final double hypothenuse) {
        return Math.sin(angle) * hypothenuse;
    }

    /**
     * Calculate the two points defining a line perpendicular to the one passing through p1 and p2.
     * This perpendicular line cut the (p1,p2) line on the p0 point.
     *
     * @param p0       the location on the line defined by p1 and p2 where we want to place the perpendicular points
     * @param p1
     * @param p2
     * @param distance the distance between each perpendicular point and pO
     * @return the perpendicular points
     */

    public static Point2D[] getPerpendicular(final Point2D p0, final Point2D p1, final Point2D p2, final double distance) {
        final Point2D[] pp = new Point2D[2];
        pp[0] = (Point2D) p0.clone();
        pp[1] = (Point2D) p0.clone();
        final double angle = getAngle(p1.getY() - p2.getY(), p1.getX() - p2.getX());
        pp[0].setLocation(pp[0].getX() + DrawingUtils.getOppositeSide(angle, distance), pp[0].getY() - DrawingUtils.getAdjacentSide(angle, distance));
        pp[1].setLocation(pp[1].getX() - DrawingUtils.getOppositeSide(angle, distance), pp[1].getY() + DrawingUtils.getAdjacentSide(angle, distance));
        return pp;
    }

    /**
     * Calculate a new point from an original one after a rotation step
     * defined by a specific angle value and a center point.
     *
     * @param p the original point
     * @param a the angle of rotation in radians
     * @param c the center of rotation
     * @return the new point after the rotation step
     */
    //@todo can we return the original point rotated or a new one ?
    public static Point2D getRotatedPoint(final Point2D p, final double a, final Point2D c) {
        if (a != 0.0) {
            //we set the rotation
            final AffineTransform rot = new AffineTransform();
            rot.setToRotation(a, c.getX(), c.getY());
            //we get the rotated point with this transformation
            final Point2D pointRot = rot.transform(p, null);
            return new Point2D.Double(pointRot.getX(), pointRot.getY());
        }
        return p;
    }

    public static String colorToString(final Color color) {
        return new StringBuffer("rgb(").append(color.getRed()).append(",").append(color.getGreen()).append(",")
                .append(color.getBlue()).append(")").toString();
    }

    /**
     * Gives the cross product for three points.
     * p1 is the common point for the two vectors (p1p2 and p1p3)
     *
     * @param p1
     * @param p2
     * @param p3
     * @return the cross product
     */

    public static double crossProduct(final Point2D p1, final Point2D p2, final Point2D p3) {
        return crossProduct(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
    }

    /**
     * Gives the cross product for three points.
     * p1 is the common point for the two vectors (p1p2 and p1p3)
     *
     * @param p1x
     * @param p1y
     * @param p2x
     * @param p2y
     * @param p3x
     * @param p3y
     * @return the cross product
     */
    public static double crossProduct(final double p1x, final double p1y, final double p2x, final double p2y, final double p3x, final double p3y) {
        final double a1 = p2x - p1x;
        final double a2 = p2y - p1y;
        final double b1 = p3x - p1x;
        final double b2 = p3y - p1y;
        return a1 * b2 - a2 * b1;
    }


}

