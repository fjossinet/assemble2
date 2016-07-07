package fr.unistra.ibmc.assemble2.model;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.SvgPath;


public class View2DFold {

    private java.awt.Shape shape;
    private SecondaryStructure ss;
    private Mediator mediator;

    public View2DFold(Mediator mediator, SecondaryStructure ss) {
        try {
            this.shape = new SvgPath("M16,8.286C8.454,8.286,2.5,16,2.5,16s5.954,7.715,13.5,7.715c5.771,0,13.5-7.715,13.5-7.715S21.771,8.286,16,8.286zM16,20.807c-2.649,0-4.807-2.157-4.807-4.807s2.158-4.807,4.807-4.807s4.807,2.158,4.807,4.807S18.649,20.807,16,20.807zM16,13.194c-1.549,0-2.806,1.256-2.806,2.806c0,1.55,1.256,2.806,2.806,2.806c1.55,0,2.806-1.256,2.806-2.806C18.806,14.451,17.55,13.194,16,13.194z").getShape();
            this.ss = ss;
            this.mediator = mediator;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mouseClicked(MouseEvent e, double x, double y) {
        Rectangle2D rect = new Rectangle2D.Double(this.shape.getBounds2D().getMinX()+x, this.shape.getBounds2D().getMinY()+y, this.shape.getBounds2D().getWidth(), this.shape.getBounds2D().getHeight());
        if (rect.contains(e.getX(), e.getY())) {
            mediator.getMoleculesList().clearList();
            mediator.getAlignmentCanvas().clear();
            mediator.getSecondaryCanvas().clear();
            SecondaryStructure ss = new SecondaryStructure(this.ss);
            ss.findJunctions();
            for (int i=1 ; i <= this.ss.getMolecule().size() ; i++) { //we migrate the values to the new 2D
                ss.getResidue(i).setQuantitativeValue(this.ss.getResidue(i).getQuantitativeValue());
                ss.getResidue(i).setQualitativeValue(this.ss.getResidue(i).getQualitativeValue());
            }

            this.mediator.loadRNASecondaryStructure(ss, false, true);

            mediator.getSecondaryCanvas().getGraphicContext().setMinQuantitativeValue(mediator.getFoldingLandscape().getGraphicContext().getMinQuantitativeValue());
            mediator.getSecondaryCanvas().getGraphicContext().setMaxQuantitativeValue(mediator.getFoldingLandscape().getGraphicContext().getMaxQuantitativeValue());

            mediator.getSecondaryCanvas().getGraphicContext().endGradientColor = mediator.getFoldingLandscape().getGraphicContext().getEndGradientColor();
            mediator.getSecondaryCanvas().getGraphicContext().startGradientColor = mediator.getFoldingLandscape().getGraphicContext().getStartGradientColor();
            mediator.getSecondaryCanvas().getGraphicContext().noQuantitativeValueColor = mediator.getFoldingLandscape().getGraphicContext().noQuantitativeValueColor;

            mediator.getSecondaryCanvas().getGraphicContext().qualitative2Colors = mediator.getFoldingLandscape().getGraphicContext().qualitative2Colors;
            mediator.getSecondaryCanvas().getGraphicContext().qualitativeNames = mediator.getFoldingLandscape().getGraphicContext().qualitativeNames;
            mediator.getSecondaryCanvas().getGraphicContext().noQualitativeValueColor = mediator.getFoldingLandscape().getGraphicContext().noQualitativeValueColor;

            this.mediator.getSecondaryCanvas().repaint();
            this.mediator.getFoldingLandscape().repaint();

        }
    }

    public void mouseMoved(MouseEvent e, double x, double y, JComponent component) {
        Rectangle2D rect = new Rectangle2D.Double(this.shape.getBounds2D().getMinX()+x, this.shape.getBounds2D().getMinY()+y, this.shape.getBounds2D().getWidth(), this.shape.getBounds2D().getHeight());
        if (rect.contains(e.getPoint()))
            component.setToolTipText("Click me to select this 2D prediction.");
        else {
            component.setToolTipText(null);
        }
    }

    public void draw(Graphics2D g2, double x, double y, String secondaryStructureSource) {
        if (this.shape != null) {
            Rectangle2D buttonShape = this.shape.getBounds2D();
            g2.translate(x,y);
            g2.setColor(Color.BLACK);
            g2.fill(this.shape);
            g2.translate(-x,-y);
            Font f = g2.getFont();
            g2.setFont(f.deriveFont(15f));
            g2.drawString(secondaryStructureSource, (float)(buttonShape.getMaxX()+x+5), (float)(buttonShape.getMaxY()+y));
            g2.setFont(f);
        }
    }
}
