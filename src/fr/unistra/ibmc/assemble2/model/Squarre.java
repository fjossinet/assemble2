package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

public class Squarre extends AbstractShape {

    private java.awt.geom.Point2D[] startPoints, endPoints;

    public Squarre(Mediator mediator, final java.awt.geom.Point2D startPoint, final java.awt.geom.Point2D endPoint, final Residue base5, final Residue base3, GraphicContext gc) {
        super(mediator, gc);
        this.shape = new java.awt.geom.GeneralPath();
        this.startPoints = DrawingUtils.getPerpendicular(startPoint, base5.getRealCoordinates(), base3.getRealCoordinates(),gc.getCurrentSymbolSize() / 6);
        this.endPoints = DrawingUtils.getPerpendicular(endPoint, base5.getRealCoordinates(), base3.getRealCoordinates(), gc.getCurrentSymbolSize() / 6);
        ((java.awt.geom.GeneralPath) this.shape).moveTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoints[0].getX(), (float) endPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) endPoints[1].getX(), (float) endPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[1].getX(), (float) startPoints[1].getY());
        ((java.awt.geom.GeneralPath) this.shape).lineTo((float) startPoints[0].getX(), (float) startPoints[0].getY());
        ((java.awt.geom.GeneralPath) this.shape).closePath();
    }

    public String getSVG(BaseBaseInteraction bbi) {
        if (bbi.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS)
            return "<polygon points=\""+startPoints[0].getX()+","+startPoints[0].getY()+" "+endPoints[0].getX()+","+endPoints[0].getY()+" "+endPoints[1].getX()+","+endPoints[1].getY()+" "+startPoints[1].getX()+","+startPoints[1].getY()+"\" fill=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"0\"/>";
        else
            return "<polygon points=\""+startPoints[0].getX()+","+startPoints[0].getY()+" "+endPoints[0].getX()+","+endPoints[0].getY()+" "+endPoints[1].getX()+","+endPoints[1].getY()+" "+startPoints[1].getX()+","+startPoints[1].getY()+"\" fill=\"none\" stroke=\"#"+Integer.toHexString(bbi.getFinalColor().getRGB()).substring(2)+"\" stroke-width=\"1\"/>";
    }

}
