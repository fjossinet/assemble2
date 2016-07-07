package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonDeleteHelix extends Circle implements Button {

    protected SecondaryStructure secondaryStructure;
    protected int length;
    protected Helix helix;

    public ButtonDeleteHelix(Mediator mediator, SecondaryStructure secondaryStructure,Point2D firstPoint, Point2D centerPoint, Helix helix, GraphicContext gc) {
        super(mediator, firstPoint,centerPoint, gc);
        this.secondaryStructure = secondaryStructure;
        this.helix = helix;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(255, 46, 51));
        g2.fill(this.shape);
        Residue[] ends = this.helix.get5PrimeEnds();
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)ends[0].getCurrentCenterX(this.gc), (int)ends[0].getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)this.helix.get3PrimeEnd(ends[0]).getCurrentCenterX(this.gc), (int)this.helix.get3PrimeEnd(ends[0]).getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)ends[1].getCurrentCenterX(this.gc), (int)ends[1].getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)this.helix.get3PrimeEnd(ends[1]).getCurrentCenterX(this.gc), (int)this.helix.get3PrimeEnd(ends[1]).getCurrentCenterY(this.gc));
        g2.setColor(currentColor);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            java.util.List<BaseBaseInteraction> interactions2Remove = this.helix.getSecondaryInteractions();
            Residue[] ends = this.helix.get5PrimeEnds();
            this.secondaryStructure.removeHelix(this.helix);
            this.secondaryStructure.findJunctions();
            for (SingleStrand ss:this.secondaryStructure.getSingleStrands())
                ss.setCoordinates(this.gc);
            this.gc.getCanvas().clearSelection();
            this.gc.getCanvas().select(ends[0]);
            this.gc.getCanvas().select(this.helix.get3PrimeEnd(ends[0]));
            this.gc.getCanvas().select(ends[1]);
            this.gc.getCanvas().select(this.helix.get3PrimeEnd(ends[1]));
            for (BaseBaseInteraction bbi: interactions2Remove)
                this.gc.getCanvas().getMediator().getAlignmentCanvas().getMainAlignment().getReferenceStructure().removeInteraction(bbi);
            this.gc.getCanvas().getMediator().getAlignmentCanvas().repaint();
            return true;
        }
        return false;
    }
}
