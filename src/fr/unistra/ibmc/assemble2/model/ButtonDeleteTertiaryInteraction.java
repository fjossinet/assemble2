package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ButtonDeleteTertiaryInteraction extends Circle implements Button {
    protected BaseBaseInteraction baseBaseInteraction;
    protected SecondaryStructure secondaryStructure;

    public ButtonDeleteTertiaryInteraction(Mediator mediator, SecondaryStructure secondaryStructure,BaseBaseInteraction baseBaseInteraction, Point2D firstPoint, Point2D centerPoint, GraphicContext gc) {
        super(mediator, firstPoint, centerPoint, gc);
        this.baseBaseInteraction = baseBaseInteraction;
        this.secondaryStructure = secondaryStructure;
    }

    public void draw(final Graphics2D g2) {
        Color currentColor = g2.getColor();
        g2.setColor(new Color(255, 46, 51));
        g2.fill(this.shape);
        g2.setColor(currentColor);
    }

    public boolean mouseClicked(MouseEvent e) {
        if (this.shape.contains(e.getX(),e.getY())) {
            secondaryStructure.removeTertiaryInteraction(baseBaseInteraction);
            this.gc.getCanvas().repaint();
            gc.getCanvas().getMediator().getSecondaryStructureNavigator().removeNode(secondaryStructure.getMolecule(), baseBaseInteraction);
            this.gc.getCanvas().getMediator().getAlignmentCanvas().getMainAlignment().getReferenceStructure().removeInteraction(baseBaseInteraction);
            this.gc.getCanvas().getMediator().getAlignmentCanvas().repaint();
            return true;
        }
        return false;
    }
}
