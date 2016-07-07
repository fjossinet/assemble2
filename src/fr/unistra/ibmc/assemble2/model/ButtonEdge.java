package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.components.ToolTip;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonEdge extends Circle implements Button {

    protected BaseBaseInteraction baseBaseInteraction;
    protected SecondaryStructure secondaryStructure;

    public ButtonEdge(Mediator mediator, SecondaryStructure secondaryStructure,BaseBaseInteraction baseBaseInteraction, Point2D firstPoint, Point2D centerPoint, GraphicContext gc) {
        super(mediator, firstPoint, centerPoint, gc);
        this.baseBaseInteraction = baseBaseInteraction;
        this.secondaryStructure = secondaryStructure;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(255, 176, 97));
        g2.fill(this.shape);
        g2.setColor(currentColor);
        if (Assemble.HELP_MODE)
            new ToolTip(mediator, (int)this.shape.getBounds().getMaxX(), (int)this.shape.getBounds().getCenterY(), 100,40, "test").draw(g2);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            switch (baseBaseInteraction.getEdge(baseBaseInteraction.getResidue())) {
                case '(' : baseBaseInteraction.setEdge(baseBaseInteraction.getResidue(),'{') ; break;
                case '{' : baseBaseInteraction.setEdge(baseBaseInteraction.getResidue(),'[') ; break;
                case '[' : baseBaseInteraction.setEdge(baseBaseInteraction.getResidue(),'(') ; break;
            }
            if (baseBaseInteraction.isSecondaryInteraction()) {
                gc.getCanvas().getMediator().getSecondaryStructureNavigator().updateNode(baseBaseInteraction.getResidue().getStructuralDomain(), baseBaseInteraction);
                gc.getCanvas().getMediator().getAlignmentCanvas().getMainAlignment().getReferenceStructure().updateSymbols(baseBaseInteraction); //to update the symbols fot he interaction in the reference bracket notation
            }
            else
                gc.getCanvas().getMediator().getSecondaryStructureNavigator().updateNode(secondaryStructure.getMolecule(), baseBaseInteraction);
            gc.getCanvas().getMediator().getAlignmentCanvas().repaint(); //to update the structural mask
            return true;
        }
        return false;
    }

}
