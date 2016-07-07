package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PhosphodiesterBond extends BaseBaseInteraction {

    public PhosphodiesterBond(Mediator mediator, SecondaryStructure ss, Location location) {
        super(mediator, ss, location);
    }

    public void draw(Graphics2D g2,GraphicContext gc) {
        if (!this.isSelected() &&  gc.isDrawOnlySelectedMotif())
            return;
        if (this.getResidue() != null && this.getPartnerResidue() != null)
            if (this.isSecondaryInteraction() || gc.isTertiaryInteractionsDisplayed()) {
                if (this.isInsideDrawingArea(gc)  || gc.isDrawInPNG() ) {
                    if (this.isUpdated() || this.real_op == null) {
                        this.setOutsidePoints(gc);
                    }
                    //TODO find how to put some calculation steps in the previous if statement to improve 2D draw
                    if (this.real_op != null) {
                        //calculate the outside points fitted with the current view and zoom level
                        this.op = new java.awt.geom.Point2D[2];
                        this.op[0] = new java.awt.geom.Point2D.Double(this.real_op[0]
                                .getX()
                                * gc.getFinalZoomLevel() + gc.getViewX(),
                                this.real_op[0].getY() * gc.getFinalZoomLevel()
                                        + gc.getViewY());
                        this.op[1] = new java.awt.geom.Point2D.Double(this.real_op[1]
                                .getX()
                                * gc.getFinalZoomLevel() + gc.getViewX(),
                                this.real_op[1].getY() * gc.getFinalZoomLevel()
                                        + gc.getViewY());
                    }
                    //we remove the precedent op points if no real_op
                    else
                        this.op = null;
                    g2.setColor(this.getFinalColor());
                    Rectangle2D globalArea = this.setShapes(gc);
                    for (Shape s: this.shapes)
                        s.draw(g2, this.orientation);
                    if (this.isSelected() && gc.isDrawOnlySelectedMotif())
                        gc.addToSelectionArea(globalArea);
                }
                else
                    this.real_op = null;
            }
    }

    @Override
    public Color getFinalColor() {
        if (SingleStrand.class.isInstance(this.getResidue().getStructuralDomain()) && this.getResidue().getStructuralDomain().getCustomColor() != null)
            return this.getResidue().getStructuralDomain().getCustomColor();
        else if (SingleStrand.class.isInstance(this.getPartnerResidue().getStructuralDomain()) && this.getPartnerResidue().getStructuralDomain().getCustomColor() != null)
            return this.getPartnerResidue().getStructuralDomain().getCustomColor();
        else
            return Color.GRAY;
    }

    @Override
    public boolean isSelected() {
        return this.getResidue().isSelected() && this.getPartnerResidue().isSelected();
    }

    protected Rectangle2D setShapes(GraphicContext gc) {
        Rectangle2D globalArea = null;
        this.shapes.clear();
        if (op != null) {
            //if we have a secondary interaction
            if (this.isSecondaryInteraction())
                this.shapes.add(new Line(mediator, op[0], op[1], gc));
                //if we have a tertiary interaction
            else {
                //we determine the distance between the two new points
                final double distance = DrawingUtils.getDistance(op[0], op[1]);
                //the points outside the symbol
                Point2D[] ip1 = null;
                if (distance >= gc.getCurrentSymbolSize())
                    ip1 = DrawingUtils.fit(op[0].getX(), op[0].getY(), op[1].getX(), op[1].getY(), (distance - gc.getCurrentSymbolSize()) / 2);
                if (ip1 != null) {
                    this.shapes.add(new Line(mediator, op[0], ip1[0], gc));
                    this.shapes.add(new Triangle(mediator, ip1[0], ip1[1], this.ss.getResidue(this.location.getStart()),this.ss.getResidue(this.location.getEnd()),gc));
                    this.shapes.add(new Line(mediator, ip1[1], op[1], gc));
                }
                //if we cannot draw the symbol (not enough space), we draw only a line
                else
                    this.shapes.add(new Line(mediator, op[0], op[1], gc));
            }
            for (Shape s:this.shapes) {
                if (globalArea == null)
                    globalArea = s.getShape().getBounds2D();
                else
                    globalArea = globalArea.createUnion(s.getShape().getBounds2D());
            }
        }
        return globalArea;
    }

}
