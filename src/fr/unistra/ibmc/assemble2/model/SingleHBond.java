package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SingleHBond extends BaseBaseInteraction {

    public SingleHBond(Mediator mediator, SecondaryStructure ss, Location location) {
        super(mediator, ss, location);
        this.edge = '!';
        this.partnerEdge = '!';
    }

    public void draw(Graphics2D g2,GraphicContext gc) {
        if (!this.isSelected() &&  gc.isDrawOnlySelectedMotif())
            return;
        if (this.getResidue() != null && this.getPartnerResidue() != null)
            if (this.isSecondaryInteraction() || gc.isTertiaryInteractionsDisplayed()) {
                if (this.isInsideDrawingArea(gc) ) {
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
                    if (this.isSelected() && gc.isDrawOnlySelectedMotif()) {
                        gc.addToSelectionArea(globalArea);
                        if (!this.isSecondaryInteraction())
                            gc.addInteractionFromMotif(this);
                    }
                }
                else
                    this.real_op = null;
            }
    }

    @Override
    public boolean isSelected() {
        return this.getResidue().isSelected() && this.getPartnerResidue().isSelected();
    }

    protected Rectangle2D setShapes(GraphicContext gc) {
        Rectangle2D globalArea = null;
        this.shapes.clear();
        if (op != null) {
            this.addSegment(op[0], op[1], 5.0/*5px for each dot line*/, gc);
            for (Shape s:this.shapes) {
                if (globalArea == null)
                    globalArea = s.getShape().getBounds2D();
                else
                    globalArea = globalArea.createUnion(s.getShape().getBounds2D());
            }
        }
        return globalArea;
    }

    private void addSegment (final Point2D point1, final Point2D point2, final double distance, GraphicContext gc) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance);
        this.shapes.add(new Line(mediator, point1, points[0], gc));
        if (DrawingUtils.getDistance(points[0], point2) > distance) {
            final Point2D[] npoints = DrawingUtils.fit(points[0].getX(), points[0].getY(), point2.getX(), point2.getY(), distance);
            this.addSegment(npoints[0], point2, distance, gc);
        }
    }
}
