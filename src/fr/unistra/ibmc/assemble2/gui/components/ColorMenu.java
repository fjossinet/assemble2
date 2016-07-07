package fr.unistra.ibmc.assemble2.gui.components;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.icons.BucketIcon;
import fr.unistra.ibmc.assemble2.model.ConsensusStructure;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

/**
* Created with IntelliJ IDEA.
* User: fjossinet
* Date: 01/10/13
* Time: 09:54
* To change this template use File | Settings | File Templates.
*/
public class ColorMenu extends JMenu {

    public static final int SECONDARY_INTERACTIONS = 0, TERTIARY_INTERACTIONS = 2, ADENINE = 3, GUANINE = 4, CYTOSINE = 5, URACIL = 6, QUANTITATIVE_VALUES_START_GRADIENT = 7, QUANTITATIVE_VALUES_END_GRADIENT = 8, QUANTITATIVE_VALUES_NO_VALUE = 9, QUALITATIVE_VALUES_NO_VALUE = 10, QUALITATIVE_VALUES_CATEGORY = 11, BP_PROBS_START_GRADIENT = 12, BP_PROBS_END_GRADIENT = 13, GENOMIC_ANNOTATION = 14, CONSENSUS_STRUCTURE_SINGLE_STRANDS = 15 ;

    protected Border unselectedBorder;

    protected Border selectedBorder;

    protected Border activeBorder;

    protected Hashtable paneTable;

    protected ColorPane colorPane;

    protected int mode;

    private Mediator mediator;

    public ColorMenu(Mediator mediator, String name, int mode) {
        super(name);
        this.setIcon(new BucketIcon());
        this.mediator = mediator;
        this.mode = mode;
        unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                getBackground()), new BevelBorder(BevelBorder.LOWERED,
                Color.white, Color.gray));
        selectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                Color.red), new MatteBorder(1, 1, 1, 1, getBackground()));
        activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                Color.blue), new MatteBorder(1, 1, 1, 1, getBackground()));

        JPanel p = new JPanel();
        p.setBorder(new EmptyBorder(5, 5, 5, 5));
        p.setLayout(new GridLayout(8, 8));
        paneTable = new Hashtable();

        int[] values = new int[] { 0 ,128, 192, 255 };

        for (int r = 0; r < values.length; r++) {
            for (int g = 0; g < values.length; g++) {
                for (int b = 0; b < values.length; b++) {
                    Color c = new Color(values[r], values[g], values[b]);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    paneTable.put(c, pn);
                }
            }
        }
        add(p);
    }

    public void setColor(Color c) {
        Object obj = paneTable.get(c);
        if (obj == null)
            return;
        if (colorPane != null)
            colorPane.setSelected(false);
        colorPane = (ColorPane) obj;
        colorPane.setSelected(true);
    }

    public Color getColor() {
        if (colorPane == null)
            return null;
        return colorPane.getColor();
    }

    public void doSelection() {
        fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                getActionCommand()));
    }

    private class ColorPane extends JPanel implements MouseListener {
        protected Color color;

        protected boolean isSelected;

        public ColorPane(Color c) {
            color = c;
            setBackground(c);
            setBorder(unselectedBorder);
            String msg = "R " + c.getRed() + ", G " + c.getGreen() + ", B "
                    + c.getBlue();
            setToolTipText(msg);
            addMouseListener(this);
        }

        public Color getColor() {
            return color;
        }

        public Dimension getPreferredSize() {
            return new Dimension(15, 15);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            if (isSelected)
                setBorder(selectedBorder);
            else
                setBorder(unselectedBorder);
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {
            switch (mode) {
                case ColorMenu.SECONDARY_INTERACTIONS :
                    Assemble.SecondaryInteraction_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.TERTIARY_INTERACTIONS :
                    Assemble.TertiaryInteraction_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.ADENINE:
                    Assemble.A_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.URACIL:
                    Assemble.U_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.GUANINE :
                    Assemble.G_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.CYTOSINE :
                    Assemble.C_Color = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.QUANTITATIVE_VALUES_NO_VALUE:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().noQuantitativeValueColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().noQuantitativeValueColor = this.color;
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.QUANTITATIVE_VALUES_START_GRADIENT:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().startGradientColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().startGradientColor = this.color;
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.QUANTITATIVE_VALUES_END_GRADIENT:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().endGradientColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().endGradientColor = this.color;
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.QUALITATIVE_VALUES_NO_VALUE:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().noQualitativeValueColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().noQualitativeValueColor = this.color;
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.QUALITATIVE_VALUES_CATEGORY:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().getQualitative2Colors().put(ColorMenu.this.getText(),color);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().getQualitative2Colors().put(ColorMenu.this.getText(),color);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.BP_PROBS_START_GRADIENT:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().startBpsProbColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
                case ColorMenu.BP_PROBS_END_GRADIENT:
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().endBpsProbColor = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getAlignmentCanvas().repaint();
                case ColorMenu.GENOMIC_ANNOTATION:
                    Assemble.genomic_features_classes_to_colors.put(mediator.getGenomicAnnotationsPanel().getSelectedClass(), this.color);
                    mediator.getGenomicAnnotationsPanel().repaint();
                    mediator.getAlignmentCanvas().repaint();
                case ColorMenu.CONSENSUS_STRUCTURE_SINGLE_STRANDS:
                    ConsensusStructure.SINGLE_STRAND_COLOR = this.color;
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getAlignmentCanvas().repaint();
                    break;
            }
        }

        public void mouseEntered(MouseEvent e) {
            setBorder(activeBorder);
        }

        public void mouseExited(MouseEvent e) {
            setBorder(isSelected ? selectedBorder : unselectedBorder);
        }
    }
}
