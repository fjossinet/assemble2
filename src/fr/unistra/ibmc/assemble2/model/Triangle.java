package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.geom.Line2D;

class Triangle extends AbstractShape {

    private java.awt.geom.Point2D[] startPoints;
    private java.awt.geom.Point2D endPoint;

    /**
     * Creates a new instance of Triangle
     */
    Triangle(Mediator mediator, final java.awt.geom.Point2D startPoint, final java.awt.geom.Point2D endPoint, Residue base5, Residue base3, GraphicContext gc) {
        super(mediator,gc);
        this.shape = new java.awt.geom.GeneralPath();
        this.endPoint = endPoint;
        this.startPoints = DrawingUtils.getPerpendicular(startPoint, base5.getRealCoordinates(), base3.getRealCoordinates(), gc.getCurrentSymbolSize() / 6);
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[1].getX(), (float) startPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoint.getX(), (float) endPoint.getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }

    public String getSVG(BaseBaseInteraction bbi) {
        if (bbi.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS)
            return "<polygon points=\""+startPoints[0].getX()+","+startPoints[0].getY()+" "+startPoints[1].getX()+","+startPoints[1].getY()+" "+endPoint.getX()+","+endPoint.getY()+"\" fill=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"0\"/>";
        else
            return "<polygon points=\""+startPoints[0].getX()+","+startPoints[0].getY()+" "+startPoints[1].getX()+","+startPoints[1].getY()+" "+endPoint.getX()+","+endPoint.getY()+"\" fill=\"none\" stroke=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"1\"/>";
    }

}
