package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonCreateHelix extends Circle implements Button {

    protected SecondaryStructure secondaryStructure;
    protected Residue r1,r2,r3,r4;
    protected int length;

    public ButtonCreateHelix(Mediator mediator, SecondaryStructure secondaryStructure,Point2D firstPoint, Point2D centerPoint, Residue r1, Residue r2, Residue r3, Residue r4, int length,GraphicContext gc) {
        super(mediator, firstPoint,centerPoint, gc);
        this.secondaryStructure = secondaryStructure;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.r4 = r4;
        this.length = length;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(255, 46, 51));
        g2.fill(this.shape);
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r1.getCurrentCenterX(this.gc), (int)r1.getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r2.getCurrentCenterX(this.gc), (int)r2.getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r3.getCurrentCenterX(this.gc), (int)r3.getCurrentCenterY(this.gc));
        g2.drawLine((int)this.shape.getBounds2D().getCenterX(),(int)this.shape.getBounds2D().getCenterY(),(int)r4.getCurrentCenterX(this.gc), (int)r4.getCurrentCenterY(this.gc));
        g2.setColor(currentColor);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            Helix h = this.secondaryStructure.addHelix(new Location(new Location(r1.getAbsolutePosition(),r1.getAbsolutePosition()+length-1),new Location(r4.getAbsolutePosition()-length+1,r4.getAbsolutePosition())),"H"+this.secondaryStructure.getHelices().size());
            if (h == null &&  JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure to create a pseudoknot?"))
                h = this.secondaryStructure.addPseudoknot(new Location(new Location(r1.getAbsolutePosition(),r1.getAbsolutePosition()+length-1),new Location(r4.getAbsolutePosition()-length+1,r4.getAbsolutePosition())),"H"+this.secondaryStructure.getHelices().size());
            if (h != null) {
                this.secondaryStructure.findJunctions();
                this.gc.getCanvas().clearSelection();
                gc.getCanvas().setSelectedHelix(h);
                Residue[] ends = h.get5PrimeEnds();
                this.gc.getCanvas().select(ends[0]);
                this.gc.getCanvas().select(h.get3PrimeEnd(ends[0]));
                this.gc.getCanvas().select(ends[1]);
                this.gc.getCanvas().select(h.get3PrimeEnd(ends[1]));
                int     x = (int)(h.get5PrimeEnds()[0].getX()+h.get5PrimeEnds()[1].getX())/2,
                        y = (int)(h.get5PrimeEnds()[0].getY()+h.get5PrimeEnds()[1].getY())/2;
                h.setEnds(h.get5PrimeEnds()[0], new Point2D.Double(x, y),
                        h.get3PrimeEnds()[0], new Point2D.Double(x, y - gc.getRealHeight() * (h.getLength() - 1)),
                        h.get3PrimeEnds()[1], new Point2D.Double(x + gc.getRealWidth()*4, y),
                        h.get5PrimeEnds()[1], new Point2D.Double(x + gc.getRealWidth()*4, y - gc.getRealHeight() * (h.getLength() - 1)), gc);
            }
            for (BaseBaseInteraction bbi: h.getSecondaryInteractions())
                this.gc.getCanvas().getMediator().getAlignmentCanvas().getMainAlignment().getReferenceStructure().addInteraction(bbi);
            this.gc.getCanvas().getMediator().getAlignmentCanvas().repaint();
            return true;
        }
        return false;
    }
}
