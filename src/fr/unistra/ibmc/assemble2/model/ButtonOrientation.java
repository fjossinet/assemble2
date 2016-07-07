package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonOrientation extends Circle implements Button {

    protected BaseBaseInteraction baseBaseInteraction;
    protected SecondaryStructure secondaryStructure;

    public ButtonOrientation(Mediator mediator, SecondaryStructure secondaryStructure,BaseBaseInteraction baseBaseInteraction, Point2D firstPoint, Point2D centerPoint, GraphicContext gc) {
        super(mediator, firstPoint, centerPoint, gc);
        this.baseBaseInteraction = baseBaseInteraction;
        this.secondaryStructure = secondaryStructure;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(255, 129, 234));
        g2.fill(this.shape);
        g2.setColor(currentColor);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            baseBaseInteraction.setOrientation(baseBaseInteraction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS ? BaseBaseInteraction.ORIENTATION_TRANS : BaseBaseInteraction.ORIENTATION_CIS);
            if (baseBaseInteraction.isSecondaryInteraction())
                gc.getCanvas().getMediator().getSecondaryStructureNavigator().updateNode(baseBaseInteraction.getResidue().getStructuralDomain(), baseBaseInteraction);
            else
                gc.getCanvas().getMediator().getSecondaryStructureNavigator().updateNode(secondaryStructure.getMolecule(), baseBaseInteraction);
            gc.getCanvas().getMediator().getAlignmentCanvas().repaint(); //to update the structural mask
            return true;
        }
        return false;
    }
}
