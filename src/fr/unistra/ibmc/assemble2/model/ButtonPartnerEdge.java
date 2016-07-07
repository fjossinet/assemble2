package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;


public class ButtonPartnerEdge extends ButtonEdge {
    public ButtonPartnerEdge(Mediator mediator, SecondaryStructure secondaryStructure,BaseBaseInteraction baseBaseInteraction, Point2D firstPoint, Point2D centerPoint, GraphicContext gc) {
        super(mediator, secondaryStructure,baseBaseInteraction,firstPoint, centerPoint, gc);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            switch (baseBaseInteraction.getEdge(baseBaseInteraction.getPartnerResidue())) {
                case ')' : baseBaseInteraction.setEdge(baseBaseInteraction.getPartnerResidue(), '}') ; break;
                case '}' : baseBaseInteraction.setEdge(baseBaseInteraction.getPartnerResidue(),']') ; break;
                case ']' : baseBaseInteraction.setEdge(baseBaseInteraction.getPartnerResidue(),')') ; break;
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
