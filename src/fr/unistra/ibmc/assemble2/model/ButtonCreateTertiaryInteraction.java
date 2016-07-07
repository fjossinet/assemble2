package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonCreateTertiaryInteraction extends Circle implements Button {

    protected SecondaryStructure secondaryStructure;
    protected Residue r1,r2;
    protected int length;

    public ButtonCreateTertiaryInteraction(Mediator mediator, SecondaryStructure secondaryStructure, Point2D firstPoint, Point2D centerPoint, Residue r1, Residue r2, GraphicContext gc) {
        super(mediator, firstPoint,centerPoint, gc);
        this.secondaryStructure = secondaryStructure;
        this.r1 = r1;
        this.r2 = r2;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(121, 226, 255));
        g2.fill(this.shape);
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r1.getCurrentCenterX(this.gc), (int)r1.getCurrentCenterY(this.gc));
        g2.setColor(Color.WHITE);
        //g2.setFont(new java.awt.Font("Courier", java.awt.Font.PLAIN, (int) gc.getCurrentHeight()/2));
        //g2.drawString("\u260D",(int)this.shape.getBounds2D().getMinX(),(int)this.shape.getBounds2D().getMaxY());
        //g2.setFont(new java.awt.Font("Courier", java.awt.Font.PLAIN, (int) gc.getCurrentHeight()));
        g2.setColor(new Color(121, 226, 255));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r2.getCurrentCenterX(this.gc), (int)r2.getCurrentCenterY(this.gc));
        g2.setColor(currentColor);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            BaseBaseInteraction bbi = this.secondaryStructure.addTertiaryInteraction(new Location(new Location(r1.getAbsolutePosition()),new Location(r2.getAbsolutePosition())),BaseBaseInteraction.ORIENTATION_CIS,'(',')');
            this.gc.getCanvas().selectBaseBaseInteraction(bbi);
            this.gc.getCanvas().repaint();
            this.gc.getCanvas().getMediator().getAlignmentCanvas().getMainAlignment().getReferenceStructure().addInteraction(bbi);
            this.gc.getCanvas().getMediator().getAlignmentCanvas().repaint();
            return true;
        }
        return false;
    }
}
