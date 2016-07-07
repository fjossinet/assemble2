package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.components.MessagingSystemAction;
import fr.unistra.ibmc.assemble2.gui.components.ToolTip;
import fr.unistra.ibmc.assemble2.gui.components.SecondaryCanvasMessagingSystem;
import fr.unistra.ibmc.assemble2.io.computations.DataHandler;
import fr.unistra.ibmc.assemble2.io.computations.Rnaplot;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.model.Button;
import fr.unistra.ibmc.assemble2.model.TertiaryFragmentHit;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;
import fr.unistra.ibmc.assemble2.utils.Pair;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecondaryCanvas extends JPanel implements CanvasInterface, MouseWheelListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener {

    private SecondaryStructureToolBar secondaryStructureToolBar;
    private TertiaryStructureToolBar tertiaryStructureToolBar;
    private ActivityToolbar activityToolBar;
    private boolean displaySecondaryStructureToolBar = true, displayTertiaryStructureToolBar = true, displayActivityToolBar = true;
    protected GraphicContext gc;
    protected SecondaryStructure secondaryStructure;
    protected java.awt.Image offScreenBuffer;
    private List<Residue> selectedResidues;
    private Junction selectedJunction;
    private SingleStrand selectedSingleStrand;
    private Helix selectedHelix;
    private BaseBaseInteraction selectedBaseBaseInteraction;
    protected Rectangle2D selectionRectangle;
    private Mediator mediator;
    public static int NUMBERING_FREQUENCY = 5;
    public List<Button> buttons;
    private Rectangle2D quantitativeDataGradient;
    private HelpDialog helpDialog;
    private double maxY;
    private ToolTip mouseOverHelpModeToolTip, permanentTutorialModeTooTip, permanentHelpModeToolTip;
    private SecondaryCanvasMessagingSystem messagingSystem;

    //here we define the attributes necessary for the translation/rotation/zoom actions
    private int translateX, translateY;
    private java.awt.geom.Point2D pickingCenter;
    private Point2D centerOfRotationForSelection;
    private Residue residueOver;

    public SecondaryCanvas(final Mediator mediator) {
        this.mediator = mediator;
        this.selectedResidues = new ArrayList<Residue>();
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.setBackground(java.awt.Color.white);
        this.mediator.setSecondaryCanvas(this);
        this.buttons = new ArrayList<Button>();
        this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        this.secondaryStructureToolBar = new SecondaryStructureToolBar(this.mediator);
        this.tertiaryStructureToolBar = new TertiaryStructureToolBar(this.mediator);
        this.activityToolBar = new ActivityToolbar(mediator);
        if (AssembleConfig.showHelpToolTip())
            this.helpDialog = new HelpDialog(mediator);
        this.mouseOverHelpModeToolTip = new ToolTip(mediator);
        this.mouseOverHelpModeToolTip.setHeight(40);
        this.messagingSystem = new SecondaryCanvasMessagingSystem(mediator, 100, 40);
    }

    public SecondaryCanvasMessagingSystem getMessagingSystem() {
        return messagingSystem;
    }

    public boolean isDisplaySecondaryStructureToolBar() {
        return displaySecondaryStructureToolBar;
    }

    public void setDisplaySecondaryStructureToolBar(boolean displaySecondaryStructureToolBar) {
        this.displaySecondaryStructureToolBar = displaySecondaryStructureToolBar;
    }

    public boolean isDisplayTertiaryStructureToolBar() {
        return displayTertiaryStructureToolBar;
    }

    public void setDisplayTertiaryStructureToolBar(boolean displayTertiaryStructureToolBar) {
        this.displayTertiaryStructureToolBar = displayTertiaryStructureToolBar;
    }

    public boolean isDisplayActivityToolBar() {
        return displayActivityToolBar;
    }

    public void setDisplayActivityToolBar(boolean displayActivityToolBar) {
        this.displayActivityToolBar = displayActivityToolBar;
    }

    public BufferedImage getImage(boolean onlySelection) {
        BufferedImage bufferedImage = new BufferedImage(this.getSize().width,
                this.getSize().height, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle(0, 0, this.getSize().width,
                this.getSize().height));
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setBackground(java.awt.Color.white);
        g2.setFont(this.gc.getFont());
        //todo is the next line necessary ??
        this.setFont(this.gc.getFont());
        //we start with the BLACK color
        g2.setColor(Color.BLACK);
        this.gc.setDrawOnlySelectedMotif(onlySelection);
        if (this.secondaryStructure != null) {
            this.gc.setDrawingArea(new Rectangle(0, 0, this.getWidth(), this
                    .getHeight()));
            this.secondaryStructure.draw(g2, this.gc);
        }
        g2.dispose();
        this.gc.setDrawOnlySelectedMotif(false);
        if (onlySelection)
            try {
                //if the selection is not in the drawing area, this.gc.getMotifArea() null
                if (this.gc.getMotifArea()== null)
                    throw new RuntimeException();
                bufferedImage = bufferedImage.getSubimage((int)this.gc.getMotifArea().getMinX(), (int)this.gc.getMotifArea().getMinY(), (int)this.gc.getMotifArea().getWidth(), (int)this.gc.getMotifArea().getHeight());
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(mediator.getAssemble().getFrame(),"Try to recenter your selection on the 2D panel to create the capture");
                return null;
            }
        return bufferedImage;
    }

    public Mediator getMediator() {
        return mediator;
    }

    public boolean displaySingleHBonds() {
        return this.gc.displaySingleHBonds();
    }

    public void setDisplaySingleHBonds(boolean displaySingleHBonds) {
        this.gc.setDisplaySingleHBonds(displaySingleHBonds);
    }

    public boolean displayTertiaryInteractions() {
        return this.gc.isTertiaryInteractionsDisplayed();
    }

    public void setDisplayTertiaryInteractions(boolean displayTertiaryInteractions) {
        this.gc.setDisplayTertiaryInteractions(displayTertiaryInteractions);
    }

    public SingleStrand getSelectedSingleStrand() {
        return selectedSingleStrand;
    }

    public Junction getSelectedJunction() {
        return selectedJunction;
    }

    public Helix getSelectedHelix() {
        return selectedHelix;
    }

    public BaseBaseInteraction getSelectedBaseBaseInteraction() {
        return selectedBaseBaseInteraction;
    }

    public StructuralDomain getStructuralDomain(int pos) {
        return this.secondaryStructure.getEnclosingStructuralDomain(this.secondaryStructure.getResidue(pos));
    }

    public SecondaryStructure getSecondaryStructure() {
        return secondaryStructure;
    }

    public void addButton(Button button) {
        this.buttons.add(button);
    }

    public void paintComponent(final java.awt.Graphics g) {
        super.paintComponent(g);
        final java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setBackground(java.awt.Color.white);
        //we start with the BLACK color
        g2.setColor(Color.BLACK);
        this.buttons.clear();
        if (this.secondaryStructure != null && this.gc != null) {
            g2.setFont(this.gc.getFont());
            g2.setColor(Color.BLACK);
            if (this.selectionRectangle != null) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.fill(this.selectionRectangle);
                g2.setColor(Color.BLACK);
            }
            this.gc.setDrawingArea(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
            if (this.gc.isEditStructure() && this.selectedHelix != null) {
                Residue[] ends = this.selectedHelix.get5PrimeEnds();
                double distance = DrawingUtils.getDistance(ends[0].getCurrentCoordinates(this.gc),ends[1].getCurrentCoordinates(this.gc));
                Point2D[] point2Ds = DrawingUtils.fit(ends[0].getCurrentCenterX(gc), ends[0].getCurrentCenterY(gc), ends[1].getCurrentCenterX(gc), ends[1].getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                this.buttons.add(new ButtonDeleteHelix(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),this.selectedHelix,gc));
            }
            else if (this.gc.isEditStructure() && this.selectedResidues.size() == 2) {
                Residue r1 = this.selectedResidues.get(0), r2 = this.selectedResidues.get(1);
                boolean alreadyInInteraction = false;
                for (Helix h:this.secondaryStructure.getHelices())
                    for (BaseBaseInteraction bbi:h.getSecondaryInteractions())
                        if (bbi.getResidue() == r1 && bbi.getPartnerResidue() == r2 || bbi.getResidue() == r2 && bbi.getPartnerResidue() == r1) {
                            alreadyInInteraction = true;
                            break;
                        }
                if (!alreadyInInteraction)
                    for (BaseBaseInteraction bbi:this.secondaryStructure.getTertiaryInteractions())
                        if (bbi.getResidue() == r1 && bbi.getPartnerResidue() == r2 || bbi.getResidue() == r2 && bbi.getPartnerResidue() == r1) {
                            alreadyInInteraction = true;
                            break;
                        }
                if (!alreadyInInteraction) {
                    double distance = DrawingUtils.getDistance(r1.getCurrentCoordinates(this.gc),r2.getCurrentCoordinates(this.gc));
                    Point2D[] point2Ds = DrawingUtils.fit(r1.getCurrentCenterX(gc), r1.getCurrentCenterY(gc), r2.getCurrentCenterX(gc), r2.getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                    if (r1.getAbsolutePosition() < r2.getAbsolutePosition())
                        this.buttons.add(new ButtonCreateTertiaryInteraction(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r1,r2,gc));
                    else
                        this.buttons.add(new ButtonCreateTertiaryInteraction(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r2,r1,gc));
                }
            }
            else if (this.gc.isEditStructure() && this.selectedResidues.size() == 3) {
                Residue r1 = this.selectedResidues.get(0), r2 = this.selectedResidues.get(1), r3 = this.selectedResidues.get(2);
                if (r1.getAbsolutePosition() < r2.getAbsolutePosition()) {
                    int length = r2.getAbsolutePosition()-r1.getAbsolutePosition()+1;
                    Residue r4 = secondaryStructure.getResidue(r3.getAbsolutePosition()+length-1);
                    if (r4 != null) {
                        this.select(r4);
                        double distance = DrawingUtils.getDistance(r1.getCurrentCoordinates(this.gc),r3.getCurrentCoordinates(this.gc));
                        Point2D[] point2Ds = DrawingUtils.fit(r1.getCurrentCenterX(gc), r1.getCurrentCenterY(gc), r3.getCurrentCenterX(gc), r3.getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                        this.buttons.add(new ButtonCreateHelix(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r1,r2,r3,r4,length,gc));
                    }
                }
                else if (r1.getAbsolutePosition() > r2.getAbsolutePosition()) {
                    int length = r1.getAbsolutePosition()-r2.getAbsolutePosition()+1;
                    Residue r4 = secondaryStructure.getResidue(r3.getAbsolutePosition()-length+1);
                    if (r4 != null) {
                        this.select(r4);
                        double distance = DrawingUtils.getDistance(r2.getCurrentCoordinates(this.gc),r4.getCurrentCoordinates(this.gc));
                        Point2D[] point2Ds = DrawingUtils.fit(r2.getCurrentCenterX(gc), r2.getCurrentCenterY(gc), r4.getCurrentCenterX(gc), r4.getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                        this.buttons.add(new ButtonCreateHelix(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r2,r1,r4,r3,length,gc));
                    }
                }
            }
            else if (this.gc.isEditStructure() && this.selectedResidues.size() == 4) {
                Residue r1 = this.selectedResidues.get(0), r2 = this.selectedResidues.get(1), r3 = this.selectedResidues.get(2), r4 = this.selectedResidues.get(3);
                if (r1.getAbsolutePosition() < r2.getAbsolutePosition()) {
                    int length = r2.getAbsolutePosition()-r1.getAbsolutePosition()+1;
                    double distance = DrawingUtils.getDistance(r1.getCurrentCoordinates(this.gc),r3.getCurrentCoordinates(this.gc));
                    Point2D[] point2Ds = DrawingUtils.fit(r1.getCurrentCenterX(gc), r1.getCurrentCenterY(gc), r3.getCurrentCenterX(gc), r3.getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                    this.buttons.add(new ButtonCreateHelix(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r1,r2,r3,r4,length,gc));
                }
                else if (r1.getAbsolutePosition() > r2.getAbsolutePosition()) {
                    int length = r1.getAbsolutePosition()-r2.getAbsolutePosition()+1;
                    double distance = DrawingUtils.getDistance(r2.getCurrentCoordinates(this.gc),r4.getCurrentCoordinates(this.gc));
                    Point2D[] point2Ds = DrawingUtils.fit(r2.getCurrentCenterX(gc), r2.getCurrentCenterY(gc), r4.getCurrentCenterX(gc), r4.getCurrentCenterY(gc), distance/2-gc.getCurrentHalf()/2);
                    this.buttons.add(new ButtonCreateHelix(mediator, secondaryStructure,point2Ds[0],new Point2D.Double((point2Ds[0].getX()+point2Ds[1].getX())/2,(point2Ds[0].getY()+point2Ds[1].getY())/2),r2,r1,r3,r4,length,gc));
                }
            }

            this.secondaryStructure.draw(g2,this.gc);

            if (this.residueOver != null) {
                this.mouseOverHelpModeToolTip.setX((float) this.residueOver.getCurrentCenterX(this.gc));
                this.mouseOverHelpModeToolTip.setY((float) this.residueOver.getCurrentCenterY(this.gc));
                if (!this.residueOver.isSelected())
                    this.mouseOverHelpModeToolTip.setText("Click to select me.");
                else if (this.residueOver.getSecondaryInteraction() != null && !this.residueOver.getSecondaryInteraction().isSelected())
                    this.mouseOverHelpModeToolTip.setText("One more click to select the interaction.");
                else if (!this.residueOver.getStructuralDomain().isSelected()) {
                    String type = Helix.class.isInstance(this.residueOver.getStructuralDomain()) ? "helix" : "single-strand";
                    this.mouseOverHelpModeToolTip.setText("One more click to select the " + type + ".");
                } else {
                    if (Helix.class.isInstance(this.residueOver.getStructuralDomain()))
                        this.mouseOverHelpModeToolTip.setText("Double-click on a blank area to unselect everything.");
                    else {
                        boolean found = false;
                        for (final Junction junction : this.secondaryStructure.getJunctions()) {
                            if (junction.hasPosition(this.residueOver.getAbsolutePosition())) {
                                found = true;
                                if (!junction.isSelected()) {
                                    this.mouseOverHelpModeToolTip.setText("One more click to select the junction.");
                                } else {
                                    this.mouseOverHelpModeToolTip.setText("Double-click on a blank area to unselect everything.");
                                }
                                break;
                            }
                        }
                        if (!found)
                            this.mouseOverHelpModeToolTip.setText("Double-click on a blank area to unselect everything.");
                    }
                }
                this.mouseOverHelpModeToolTip.draw(g2);
            }

            if (this.permanentTutorialModeTooTip != null && mediator.getSecondaryCanvas().getMessagingSystem().isUnResponsive())
                this.permanentTutorialModeTooTip.draw(g2);
            if (this.permanentHelpModeToolTip != null && Assemble.HELP_MODE)
                this.permanentHelpModeToolTip.draw(g2);

            for (Button button:this.buttons)
                button.draw(g2);

            //we draw the gradient for quantitative values
            if (secondaryStructureToolBar.getRenderingMode() == secondaryStructureToolBar.QUANTITATIVE_DATA) {
                Rectangle2D noValue = new Rectangle2D.Double(20, this.getSize().getHeight()-280, 20, 20);
                g2.setColor(this.gc.noQuantitativeValueColor);
                g2.fill(noValue);
                g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                g2.setColor(Color.BLACK);
                g2.drawString("No Value", (int)(noValue.getMaxX()+10), (int) (noValue.getMinY()+(noValue.getMaxY()-noValue.getMinY())/2));

                GradientPaint paint = new GradientPaint(20,(int)this.getSize().getHeight()-250, this.getGraphicContext().getStartGradientColor(), 20,(int)this.getSize().getHeight()-50, this.getGraphicContext().getEndGradientColor());
                g2.setPaint(paint);
                this.quantitativeDataGradient = new Rectangle2D.Double(20, this.getSize().getHeight()-250, 20, 200);
                g2.fillRect((int)this.quantitativeDataGradient.getMinX(), (int)this.quantitativeDataGradient.getMinY(), (int)this.quantitativeDataGradient.getWidth(), (int)this.quantitativeDataGradient.getHeight());
                g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                g2.setColor(Color.BLACK);
                g2.drawString("" + this.getGraphicContext().getMinQuantitativeValue(), (int) this.quantitativeDataGradient.getMaxX() + 10, (int) this.quantitativeDataGradient.getMinY()+10);
                g2.drawString(""+this.getGraphicContext().getMaxQuantitativeValue(), (int)this.quantitativeDataGradient.getMaxX()+10, (int)this.quantitativeDataGradient.getMaxY());
                maxY = this.quantitativeDataGradient.getMaxY();
            } else if (secondaryStructureToolBar.getRenderingMode() == secondaryStructureToolBar.QUALITATIVE_DATA) {
                this.quantitativeDataGradient = null;

                Rectangle2D noValue = new Rectangle2D.Double(20, this.getSize().getHeight()-280, 20, 20);
                g2.setColor(this.gc.noQualitativeValueColor);
                g2.fill(noValue);
                g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                g2.setColor(Color.BLACK);
                g2.drawString("No Value", (int)(noValue.getMaxX()+10), (int) (noValue.getMinY()+(noValue.getMaxY()-noValue.getMinY())/2));

                List<String> categoryNames = new ArrayList<String>(this.gc.getQualitativeNames().keySet());
                Collections.sort(categoryNames);
                g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                for (int i = 0 ; i < categoryNames.size() ;i++) {
                    Rectangle2D rect = new Rectangle2D.Double(20, this.getSize().getHeight()-250+(i*200/categoryNames.size()), 20, 200/categoryNames.size());
                    g2.setColor(this.gc.getQualitative2Colors().get(categoryNames.get(i)));
                    g2.fill(rect);
                    g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                    g2.setColor(Color.BLACK);
                    g2.drawString(categoryNames.get(i), (int)(rect.getMaxX()+10), (int) (rect.getMinY()+(rect.getMaxY()-rect.getMinY())/2));
                    maxY = rect.getMaxY();
                }
            }  else if (secondaryStructureToolBar.getRenderingMode() == secondaryStructureToolBar.BP_PROBABILITIES) {
                this.quantitativeDataGradient = null;
                GradientPaint paint = new GradientPaint(20,(int)this.getSize().getHeight()-250, this.getGraphicContext().startBpsProbColor, 20,(int)this.getSize().getHeight()-50, this.getGraphicContext().endBpsProbColor);
                g2.setPaint(paint);
                Rectangle2D bpProbsGradient = new Rectangle2D.Double(20, this.getSize().getHeight()-250, 20, 200);
                g2.fillRect((int)bpProbsGradient.getMinX(), (int)bpProbsGradient.getMinY(), (int)bpProbsGradient.getWidth(), (int)bpProbsGradient.getHeight());
                g2.setFont(this.getFont().deriveFont(this.getFont().getSize() * 3));
                g2.setColor(Color.BLACK);
                g2.drawString("0", (int) bpProbsGradient.getMaxX() + 10, (int) bpProbsGradient.getMinY());
                g2.drawString("1", (int) bpProbsGradient.getMaxX()+10, (int) bpProbsGradient.getMaxY());
                maxY = bpProbsGradient.getMaxY();
            }
        }
        g2.setColor(Color.BLACK);
        if (this.displaySecondaryStructureToolBar)
            this.secondaryStructureToolBar.draw(g2, 10, 10);
        if (this.displayTertiaryStructureToolBar)
            this.tertiaryStructureToolBar.draw(g2, this.getWidth()-35, 10);
        if (this.displayActivityToolBar)
            this.activityToolBar.draw(g2, this.getWidth()-75, this.getHeight()-35);
        if (this.helpDialog != null)
            this.helpDialog.draw(g2, this.getWidth() - 20, this.getHeight() - 40);
        if (this.messagingSystem != null && this.messagingSystem.hasSomethingToPrint())
            this.messagingSystem.draw(g2);
    }

    public void setPermanentTutorialModeTooTip(ToolTip permanentTutorialModeTooTip) {
        this.permanentTutorialModeTooTip = permanentTutorialModeTooTip;
    }

    public void setPermanentHelpModeToolTip(ToolTip permanentHelpModeToolTip) {
        this.permanentHelpModeToolTip = permanentHelpModeToolTip;
    }

    public double getMaxY() {
        return maxY;
    }

    public void eraseHelpDialog() {
        this.helpDialog = null;
        this.repaint();
    }

    public SecondaryStructureToolBar getSecondaryStructureToolBar() {
        return secondaryStructureToolBar;
    }

    public TertiaryStructureToolBar getTertiaryStructureToolBar() {
        return tertiaryStructureToolBar;
    }

    public void update(final java.awt.Graphics g) {
        final java.awt.Graphics2D gr;
        if (this.offScreenBuffer == null ||
                (!(offScreenBuffer.getWidth(this) == this.getSize().width
                        && offScreenBuffer.getHeight(this) == this.getSize().height))) {
            this.offScreenBuffer = this.createImage(this.getSize().width, this.getSize().height);
        }
        // We need to use our buffer Image as a Graphics object:
        gr = (java.awt.Graphics2D) this.offScreenBuffer.getGraphics();
        paintComponent(gr);
        g.drawImage(this.offScreenBuffer, 0, 0, this);
    }

    /**
     * Return all the selected Residue within this canvas.
     *
     * @return all the selected Residue in a <b>sorted</b> List
     */
    public List<Residue> getSelectedResidues() {
        List<Residue> residues = new ArrayList<Residue>(this.selectedResidues);
        Collections.sort(residues);
        return residues;
    }

    public ActivityToolbar getActivityToolBar() {
        return activityToolBar;
    }

    public double getViewX() {
        return this.gc.getViewX();
    }

    public double getViewY() {
        return this.gc.getViewY();
    }

    public double getFinalZoomLevel() {
        return this.gc.getFinalZoomLevel();
    }

    Point2D getGravityPoint() {
        return this.gc.getGravityPoint();
    }

    public void mouseClicked(final MouseEvent e) {
        if (this.displayActivityToolBar)
            this.activityToolBar.mouseClicked(e, this.getWidth()-75, this.getHeight()-35);
        if (this.displaySecondaryStructureToolBar)
            this.secondaryStructureToolBar.mouseClicked(e, 10, 10);
        if (this.displayTertiaryStructureToolBar)
            this.tertiaryStructureToolBar.mouseClicked(e, this.getWidth()-35, 10);
        if (this.messagingSystem != null)
            this.messagingSystem.mouseClicked(e);
        if (this.secondaryStructure == null)
            return;
        this.requestFocus();
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            boolean found = false;
            if (gc.isEditStructure()) {
                for (Button button:this.buttons)
                    found = button.mouseClicked(e);
                this.repaint();
            }
            if (!found && e.getClickCount() == 2) {
                this.clearSelection();
                mediator.getTertiaryFragmentsPanel().clearList();
                if (mediator.getChimeraDriver() != null)
                    mediator.getChimeraDriver().selectionCleared();
                mediator.getAlignmentCanvas().clearSelectedPositions();
            }
            if (this.quantitativeDataGradient != null && this.quantitativeDataGradient.contains(e.getX(), e.getY()) && e.getClickCount() == 2) {

                List<JComponent> inputs = new ArrayList<JComponent>();

                inputs.add(new JLabel("Min value"));
                final JTextField minValue = new JTextField(""+this.getGraphicContext().getMinQuantitativeValue());
                inputs.add(minValue);

                inputs.add(new JLabel("Max value"));
                final JTextField maxValue = new JTextField(""+this.getGraphicContext().getMaxQuantitativeValue());
                inputs.add(maxValue);


                if (JOptionPane.OK_OPTION ==  JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "New min and max values", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {

                    String newMinValue =  minValue.getText().trim(),
                            newMaxValue = maxValue.getText().trim();

                    if (newMinValue.length() != 0 && newMaxValue.length() != 0 && Float.parseFloat(newMinValue) <= Float.parseFloat(newMaxValue) && Float.parseFloat(newMinValue) >= 0 && Float.parseFloat(newMaxValue) >= 0) {
                        this.getGraphicContext().setMinQuantitativeValue(Float.parseFloat(newMinValue));
                        this.getGraphicContext().setMaxQuantitativeValue(Float.parseFloat(newMaxValue));
                        this.repaint();

                        mediator.getFoldingLandscape().getGraphicContext().setMinQuantitativeValue(Float.parseFloat(newMinValue));
                        mediator.getFoldingLandscape().getGraphicContext().setMaxQuantitativeValue(Float.parseFloat(newMaxValue));
                        mediator.getFoldingLandscape().repaint();

                    }
                }
            }
            for (Residue r:this.secondaryStructure.getResidues())
                if (r.contains(e.getX(),e.getY(),this.gc)) {
                    if (!r.isSelected()) {
                        if (e.isShiftDown() && !this.selectedResidues.isEmpty()) {
                            List<Residue> tobeSelected = new ArrayList<Residue>();
                            tobeSelected.add(r);
                            Residue previousR = r.getPreviousResidue();
                            while (previousR != null && !previousR.isSelected()) {
                                tobeSelected.add(previousR);
                                previousR = previousR.getPreviousResidue();
                            }
                            if (previousR == null) {//this means that we have reached the 5' end of the molecule and we found no selected residue
                                tobeSelected.clear();
                                tobeSelected.add(r);
                                //perhaps we can get a selected residue towards the 3'-end
                                Residue nextR = r.getNextResidue();
                                while (nextR != null && !nextR.isSelected()) {
                                    tobeSelected.add(nextR);
                                    nextR = nextR.getNextResidue();
                                }
                                if (nextR == null) //this means that we have reached the 3' end of the molecule and we found no selected residue
                                    tobeSelected.clear();
                                else
                                    for (Residue _r:tobeSelected)
                                        this.select(_r);
                            }
                            else
                                for (Residue _r:tobeSelected)
                                    this.select(_r);
                        } else if (e.isAltDown() || e.isControlDown()) {
                            this.select(r);
                            //if Alt or Ctrl is pressed, this means that the user can have selected an interaction
                            if (r.getSecondaryInteraction() != null && r.getSecondaryInteraction().isSelected()) {
                                this.select(r.getSecondaryInteraction().getPartnerResidue());
                                this.select(r.getSecondaryInteraction().getResidue());
                                this.selectedBaseBaseInteraction = r.getSecondaryInteraction();
                            } else {
                                for (BaseBaseInteraction interaction : r.getTertiaryInteractions()) {
                                    if (interaction.isSelected()) {
                                        this.select(interaction.getPartnerResidue());
                                        this.select(interaction.getResidue());
                                        this.selectedBaseBaseInteraction = interaction;
                                        break;
                                    }
                                }

                            }

                        } else if (!gc.isEditStructure()) {
                            this.clearSelection();
                            this.select(r);
                        } else if (gc.isEditStructure()) {
                            if (this.selectedResidues.size() >= 4)
                                this.clearSelection();
                            this.select(r);
                        }
                        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Residue #" + r.getAbsolutePosition() + " selected.", null, null);
                        mediator.getSecondaryCanvas().repaint();
                        if (SingleStrand.class.isInstance(r.getStructuralDomain()) && r.getStructuralDomain().getLength() == 1) {
                            this.selectedSingleStrand = (SingleStrand) r.getStructuralDomain();
                            java.util.List<String> texts = new ArrayList<String>();
                            java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                                    nextActions = new ArrayList<MessagingSystemAction>();
                            texts.add("You have selected a single-strand. Click the arrow to search for 3D fragments.");
                            closeActions.add(null);
                            nextActions.add(new MessagingSystemAction() {
                                @Override
                                public void run() {
                                    new SwingWorker() {
                                        @Override
                                        protected Object doInBackground() throws Exception {
                                            try {
                                                mediator.getTertiaryFragmentsPanel().clearList();
                                                List<TertiaryFragmentHit> singleStrandHits = new DataHandler(mediator).findSingleStrandInJunctions(selectedSingleStrand);

                                                for (TertiaryFragmentHit hit : singleStrandHits)
                                                    mediator.getTertiaryFragmentsPanel().addRow(hit);

                                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep(mediator.getTertiaryFragmentsPanel().length() + " 3D fragments found.", null, null);
                                                mediator.getSecondaryCanvas().repaint();
                                                if (AssembleConfig.popupLateralPanels())
                                                    mediator.getTertiaryFragmentsPanel().getToolWindow().setVisible(true);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }.execute();
                                }
                            });
                            if (Assemble.HELP_MODE) {
                                texts.add("Check now the lateral panel \"3D Folds\".");
                                closeActions.add(null);
                                nextActions.add(null);
                            }
                            messagingSystem.addThread(texts, closeActions, nextActions);
                        }
                    } else if (r.getSecondaryInteraction() != null && !r.getSecondaryInteraction().isSelected()) {
                        this.clearSelection();
                        this.select(r.getSecondaryInteraction().getResidue());
                        this.select(r.getSecondaryInteraction().getPartnerResidue());
                        this.selectedBaseBaseInteraction = r.getSecondaryInteraction();
                    } else if (!r.getStructuralDomain().isSelected() && Helix.class.isInstance(r.getStructuralDomain())) {
                        this.selectHelix((Helix) r.getStructuralDomain());
                        if (Assemble.HELP_MODE) {
                            java.util.List<String> texts = new ArrayList<String>();
                            java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                                    nextActions = new ArrayList<MessagingSystemAction>();
                            texts.add("Great! You have selected an helix.");
                            closeActions.add(null);
                            nextActions.add(null);
                            texts.add("You can translate it by moving the mouse with Alt + right click pressed.");
                            closeActions.add(null);
                            nextActions.add(null);
                            texts.add("You can rotate it by moving the mouse with Alt + left click pressed");
                            closeActions.add(null);
                            nextActions.add(null);
                            messagingSystem.addThread(texts, closeActions, nextActions);
                        } else {
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Helix "+r.getStructuralDomain().getName()+" selected.", null, null);
                            mediator.getSecondaryCanvas().repaint();
                        }
                    } else if (!gc.isEditStructure() && SingleStrand.class.isInstance(r.getStructuralDomain())) {

                        java.util.List<String> texts = new ArrayList<String>();
                        java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                                nextActions = new ArrayList<MessagingSystemAction>();

                        if (((SingleStrand) r.getStructuralDomain()).isAtFivePrimeEnd() || ((SingleStrand) r.getStructuralDomain()).isAtThreePrimeEnd() || !r.getStructuralDomain().isSelected()) {
                            try {
                                texts.add("You have selected a single-strand. Click the arrow to search for 3D fragments.");
                                closeActions.add(null);

                                this.clearSelection();
                                this.selectedSingleStrand = (SingleStrand) r.getStructuralDomain();
                                for (Residue _r : this.selectedSingleStrand.getResidues())
                                    this.select(_r);
                                if (((SingleStrand) r.getStructuralDomain()).isApicalLoop()) { //we search for junctions and single strands fragments
                                    for (final Junction junction : this.secondaryStructure.getJunctions()) {
                                        if (junction.hasPosition(r.getAbsolutePosition())) {
                                            this.selectedJunction = junction;
                                            nextActions.add(new MessagingSystemAction() {
                                                @Override
                                                public void run() {
                                                    new SwingWorker() {
                                                        @Override
                                                        protected Object doInBackground() throws Exception {
                                                            List<String> query = new ArrayList<String>();
                                                            for (MutablePair<Molecule, Location> fragment : selectedJunction.getFragments()) {
                                                                if (fragment.right.getLength() == 2) //two helices directy linked
                                                                    query.add("-");
                                                                else {
                                                                    if (fragment.right.getLength() == 3)
                                                                        query.add("^[AUGC]$"); //"^.$" is not good since this also means "-"
                                                                    else
                                                                        query.add("^.{" + (fragment.right.getLength() - 2) + "}$");
                                                                }
                                                            }
                                                            try {
                                                                mediator.getTertiaryFragmentsPanel().clearList();
                                                                List<TertiaryFragmentHit> tertiaryFragmentHits = new DataHandler(mediator).findJunctions(query);

                                                                for (TertiaryFragmentHit hit : tertiaryFragmentHits)
                                                                    mediator.getTertiaryFragmentsPanel().addRow(hit);

                                                                List<TertiaryFragmentHit> singleStrandHits = new DataHandler(mediator).findSingleStrandInJunctions(selectedSingleStrand);

                                                                for (TertiaryFragmentHit hit : singleStrandHits)
                                                                    mediator.getTertiaryFragmentsPanel().addRow(hit);

                                                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep(mediator.getTertiaryFragmentsPanel().length() + " 3D fragments found.", null, null);
                                                                mediator.getSecondaryCanvas().repaint();
                                                                if (AssembleConfig.popupLateralPanels())
                                                                    mediator.getTertiaryFragmentsPanel().getToolWindow().setVisible(true);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            return null;
                                                        }
                                                    }.execute();
                                                }
                                            });
                                            break;
                                        }
                                    }
                                } else if (!this.selectedSingleStrand.isAtFivePrimeEnd() && !this.selectedSingleStrand.isAtThreePrimeEnd()) {
                                    nextActions.add(new MessagingSystemAction() {
                                        @Override
                                        public void run() {
                                            new SwingWorker() {
                                                @Override
                                                protected Object doInBackground() throws Exception {
                                                    try {
                                                        mediator.getTertiaryFragmentsPanel().clearList();
                                                        List<TertiaryFragmentHit> singleStrandHits = new DataHandler(mediator).findSingleStrandInJunctions(selectedSingleStrand);

                                                        for (TertiaryFragmentHit hit : singleStrandHits)
                                                            mediator.getTertiaryFragmentsPanel().addRow(hit);

                                                        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();

                                                        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep(mediator.getTertiaryFragmentsPanel().length() + " 3D fragments found.", null, null);
                                                        mediator.getSecondaryCanvas().repaint();
                                                        if (AssembleConfig.popupLateralPanels())
                                                            mediator.getTertiaryFragmentsPanel().getToolWindow().setVisible(true);

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    return null;
                                                }
                                            }.execute();
                                        }
                                    });
                                }
                                if (Assemble.HELP_MODE) {
                                    texts.add("Check now the lateral panel \"3D Folds\".");
                                    closeActions.add(null);
                                    nextActions.add(null);
                                }
                                messagingSystem.addThread(texts, closeActions, nextActions);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } else if (!((SingleStrand) r.getStructuralDomain()).isApicalLoop()) { //if it is an apical loop, everything has been done before
                            for (final Junction junction : secondaryStructure.getJunctions()) {
                                if (junction.hasPosition(r.getAbsolutePosition()) && !junction.isSelected()) {
                                    clearSelection();
                                    selectedJunction = junction;
                                    selectedSingleStrand = (SingleStrand) r.getStructuralDomain();
                                    for (MutablePair<Molecule, Location> f : junction.getFragments()) {
                                        int index = 0;
                                        for (int pos : f.right.getSinglePositions()) {
                                            if (index != 0 && index != f.right.getSinglePositions().length - 1) //we're not selecting the helical residues for each fragment of the junction
                                                select(secondaryStructure.getResidue(pos));
                                            index++;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (selectedJunction != null) {
                                texts = new ArrayList<String>();
                                closeActions = new ArrayList<MessagingSystemAction>();
                                nextActions = new ArrayList<MessagingSystemAction>();
                                texts.add("You have selected a junction. Click the arrow to search for 3D fragments.");
                                closeActions.add(null);
                                nextActions.add(new MessagingSystemAction() {
                                    @Override
                                    public void run() {
                                        new SwingWorker() {
                                            @Override
                                            protected Object doInBackground() throws Exception {
                                                List<String> query = new ArrayList<String>();
                                                for (MutablePair<Molecule, Location> fragment : selectedJunction.getFragments()) {
                                                    if (fragment.right.getLength() == 2) //two helices directy linked
                                                        query.add("-");
                                                    else {
                                                        if (fragment.right.getLength() == 3)
                                                            query.add("^[AUGC]$"); //"^.$" is not good since this also means "-"
                                                        else
                                                            query.add("^.{" + (fragment.right.getLength() - 2) + "}$");
                                                    }
                                                }
                                                try {
                                                    List<TertiaryFragmentHit> tertiaryFragmentHits = new DataHandler(mediator).findJunctions(query);
                                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                                    mediator.getTertiaryFragmentsPanel().clearList();
                                                    for (TertiaryFragmentHit hit : tertiaryFragmentHits)
                                                        mediator.getTertiaryFragmentsPanel().addRow(hit);
                                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep(mediator.getTertiaryFragmentsPanel().length() + " 3D fragments found.", null, null);
                                                    mediator.getSecondaryCanvas().repaint();
                                                    if (AssembleConfig.popupLateralPanels())
                                                        mediator.getTertiaryFragmentsPanel().getToolWindow().setVisible(true);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }
                                        }.execute();

                                    }
                                });
                                if (Assemble.HELP_MODE) {
                                    texts.add("Check now the lateral panel \"3D Folds\".");
                                    closeActions.add(null);
                                    nextActions.add(null);
                                }
                                messagingSystem.addThread(texts, closeActions, nextActions);
                            }
                        }
                    }
                    this.repaint();
                    this.emitSelectedResidues();
                    return;
                }
        }
    }

    public GraphicContext getGraphicContext() {
        return this.gc;
    }

    public void selectHelix(Helix helix) {
        //this.clearSelection();
        this.selectedHelix = helix;
        for (Residue _r:helix.getResidues())
            this.select(_r);
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectionCleared();
        List<String> positions = new ArrayList<String>(1);
        for (Residue _r:this.selectedResidues)
            positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectResidues(positions);
    }

    public void selectSingleStrand(SingleStrand singleStrand) {
        for (Residue _r:singleStrand.getResidues())
            this.select(_r);
        this.selectedSingleStrand = singleStrand;
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectionCleared();
        List<String> positions = new ArrayList<String>(1);
        for (Residue _r:this.selectedResidues)
            positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectResidues(positions);
    }

    public void selectBaseBaseInteraction(BaseBaseInteraction bbi) {
        this.select(bbi.getResidue());
        this.select(bbi.getPartnerResidue());
        this.selectedBaseBaseInteraction =  bbi;
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectionCleared();
        List<String> positions = new ArrayList<String>(1);
        for (Residue _r:this.selectedResidues)
            positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition() );
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectResidues(positions);
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
        //translation
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            translateX = e.getX();
            translateY = e.getY();
        }
        //rotation
        else if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            //rotation of the selection only
            if (e.isAltDown() || e.isControlDown()) {
                this.pickingCenter = new java.awt.geom.Point2D.Double(e.getPoint().getX(), e.getPoint().getY());
                double centerX = 0, centerY = 0;
                List<Residue> residues = this.getSelectedResidues();
                for (Residue r:residues) {
                    centerX+=r.getCurrentCenterX(this.gc);
                    centerY+=r.getCurrentCenterY(this.gc);
                }
                centerX = centerX /residues.size();
                centerY = centerY /residues.size();
                this.centerOfRotationForSelection = new Point2D.Double(centerX,centerY);
            }
        }
    }

    public void mouseReleased(final MouseEvent e) {
        //translation
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            translateX = 0;
            translateY = 0;
        }
    }

    public void mouseDragged(final MouseEvent e) {
        //translation
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            //translation of the selection only
            if (e.isAltDown() || e.isControlDown()) {
                this.updateGravityPoint();
                if (getGravityPoint() !=null) {
                    final double trX = e.getX() - this.getGravityPoint().getX();
                    final double trY = e.getY() - this.getGravityPoint().getY();
                    this.translateSelection(trX, trY);
                }
            }
            //translation of the full 2D
            else {
                final int transX = e.getX() - translateX;
                final int transY = e.getY() - translateY;
                this.translateView(transX, transY);
                translateX = e.getX();
                translateY = e.getY();
            }
        }
        //rotation
        else if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            //rotation of the selection only
            if (e.isAltDown() || e.isControlDown())
                this.rotateSelection(e.isShiftDown() ? 5.0 : (e.getX() - this.pickingCenter.getX() + e.getY() - this.pickingCenter.getY()) / 10, this.centerOfRotationForSelection);
        }
    }

    public void mouseMoved(final MouseEvent e) {
        if (this.displayActivityToolBar)
            this.activityToolBar.mouseMoved(e, this.getWidth()-75, this.getHeight()-35);
        if (this.displaySecondaryStructureToolBar)
            this.secondaryStructureToolBar.mouseMoved(e, 10, 10);
        if (this.displayTertiaryStructureToolBar)
            this.tertiaryStructureToolBar.mouseMoved(e, this.getWidth()-35, 10);
        if (Assemble.HELP_MODE && this.secondaryStructure != null) {
            this.residueOver = null;
            for (Residue r : this.secondaryStructure.getResidues())
                if (r.contains(e.getX(), e.getY(), this.gc)) {
                    this.residueOver = r;
                    break;
                }
            this.repaint();
        }
    }

    //for the zoom function
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (this.secondaryStructure != null) {
            Point2D.Double realMouse = new Point2D.Double(((double)e.getX()-this.gc.getViewX())/this.getFinalZoomLevel(),((double)e.getY()-this.gc.getViewY())/this.getFinalZoomLevel());
            int notches = e.getWheelRotation();
            if (notches < 0)
                this.gc.zoomParameters(1.5);
            if (notches > 0)
                this.gc.zoomParameters(1.0/1.5);
            Point2D.Double newRealMouse = new Point2D.Double(((double)e.getX()-this.gc.getViewX())/this.getFinalZoomLevel(),((double)e.getY()-this.gc.getViewY())/this.getFinalZoomLevel());
            this.translateView((newRealMouse.getX()-realMouse.getX())*this.gc.getFinalZoomLevel(),(newRealMouse.getY()-realMouse.getY())*this.gc.getFinalZoomLevel());
        }
    }

    void updateGravityPoint() {
        Rectangle2D r = null;
        for (Residue b : this.getSelectedResidues()) {
            if (b.isInsideHelix()) {
                if (r == null)
                    r = b.getArea(this.gc).getBounds2D();
                else
                    r.add(b.getArea(this.gc).getBounds2D());
            }
        }
        if (r != null)
            this.gc.setGravityPoint(new Point2D.Double(r.getCenterX(), r.getCenterY()));
        else
            this.gc.setGravityPoint(null);
    }

    /**
     * Move the view to center the model's center on canvas'center
     */
    void centerView() {
        if (this.secondaryStructure != null) {
            double centerX = 0, centerY = 0;
            List<Residue> residues = this.secondaryStructure.getResidues();
            for (Residue r:residues) {
                centerX+=r.getCurrentCenterX(this.gc);
                centerY+=r.getCurrentCenterY(this.gc);
            }
            centerX = centerX /residues.size();
            centerY = centerY /residues.size();
            this.translateView(this.getWidth() / 2
                    - centerX, this.getHeight()/ 2 - centerY);
            this.repaint();
        }
    }

    public void select(final List<Integer> positions) {
        for (int pos:positions)
            this.select(this.secondaryStructure.getResidue(secondaryStructure.getMolecule().getFivePrimeEndGenomicPosition() != -1 ? (secondaryStructure.getMolecule().isPlusOrientation() ? pos-secondaryStructure.getMolecule().getFivePrimeEndGenomicPosition()+1 : secondaryStructure.getMolecule().getFivePrimeEndGenomicPosition()+secondaryStructure.getMolecule().size()-1 - pos +1): pos));
        this.repaint();
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectionCleared();
        List<String> _positions = new ArrayList<String>(1);
        List<Integer> absPos = new ArrayList<Integer>();
        for (Residue _r:this.selectedResidues) {
            _positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
            absPos.add(_r.getGenomicPosition());
        }
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectResidues(_positions);
    }

    public void select(final Residue residue) {
        if (residue != null && !this.selectedResidues.contains(residue)) {
            this.selectedResidues.add(residue);
            residue.isSelected(true);
        }
    }

    public void emitSelectedResidues() {
        if (mediator.getChimeraDriver()!= null)
            mediator.getChimeraDriver().selectionCleared();
        List<String> positions = new ArrayList<String>(1);
        List<Integer> absPos = new ArrayList<Integer>();
        for (Residue _r:this.selectedResidues) {
            positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
            absPos.add(_r.getGenomicPosition());
        }
        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().selectResidues(positions);
        mediator.getAlignmentCanvas().residuesSelectedFrom2DPanel(absPos, false);
        if (this.selectedJunction == null)
            mediator.getTertiaryFragmentsPanel().clearList();
    }

    public void clearSelection() {
        for (Residue r : this.selectedResidues)
            if (r != null)
                r.isSelected(false);
        this.selectedResidues.clear();
        this.selectedHelix = null;
        this.selectedJunction = null;
        this.selectedBaseBaseInteraction = null;
        this.selectedSingleStrand = null;
        this.repaint();
    }

    public void translateView(final double transX, final double transY) {
        if (this.gc != null)
            this.gc.moveView(transX, transY);
        this.repaint();
    }

    public void zoomView(final double zoomLevel) {
        if (this.gc != null)
            this.gc.zoomParameters(zoomLevel);
        this.repaint();
    }

    void translateSelection(final double transX, final double transY) {
        this.updateGravityPoint();
        for (Helix h:this.secondaryStructure.getHelices())
            if (h.isSelected())
                h.translateEnds(transX, transY,this.gc);
        this.repaint();
    }

    void rotateSelection(final double angle, final Point2D centerOfRotation) {
        for (Helix h:this.secondaryStructure.getHelices())
            if (h.isSelected())
                h.rotateEnds(centerOfRotation, Math.toRadians(angle),this.gc);
        this.repaint();
    }

    void flipSelection() {
        this.updateGravityPoint();
        for (Helix h:this.secondaryStructure.getHelices())
            if (h.isSelected())
                h.flipEnds(this.getGravityPoint(),this.gc);
        this.repaint();
    }

    public void setSelectionRectangle(Rectangle2D rectangle) {
        this.selectionRectangle = rectangle;
    }

    public boolean isLabelsDisplayed() {
        return this.gc.isLabelsDisplayed();
    }

    public void setDisplayLabels(boolean display) {
        this.gc.setDisplayLabels(display);
        this.repaint();
    }

    public void setSecondaryStructure(Molecule m, List<Location> helices, List<MutablePair<Location,String>> non_canonical_secondary_interactions, List<MutablePair<Location,String>> tertiary_interactions) {
        this.clearSelection();
        double viewX = -1, viewY = -1, zoom = -1;
        if (this.gc != null) {
            viewX = this.gc.getViewX();
            viewY = this.gc.getViewY();
            zoom = this.gc.getFinalZoomLevel();
        }
        this.gc = new GraphicContext(this);
        this.secondaryStructure = new SecondaryStructure(this.mediator, m, helices, non_canonical_secondary_interactions, tertiary_interactions);
        this.secondaryStructure.findJunctions();
        try {
            Pair<Double, Double> sizes =  new Rnaplot(mediator).plot(this.secondaryStructure);
            if (sizes.getFirst() > this.getWidth() && sizes.getSecond() > this.getHeight()) {
                Residue first = this.secondaryStructure.getResidue(1);
                double transX = first.getX()-this.getWidth()/2,
                        transY = first.getY()-this.getHeight()/2;
                for (Residue r:this.secondaryStructure.getResidues())
                    r.setRealCoordinates(r.getX()-transX,r.getY()-transY);
            }
            this.gc.initialize(this.secondaryStructure);
        } catch (Exception e) {
            e.printStackTrace();
            this.secondaryStructure = null;
            this.gc = null;
        }
        if (viewX != -1 && viewY != -1 && zoom != -1) {
            this.gc.viewX = viewX;
            this.gc.viewY = viewY;
            this.gc.finalZoomLevel = zoom;
        }
        mediator.getSecondaryStructureNavigator().reconstructTree();

        if (mediator.getAlignmentCanvas().getMainAlignment() == null) {  //the molecule of this 2D initiates a new structural alignment
            AlignedMolecule referenceMolecule = new AlignedMolecule(mediator,   this.secondaryStructure.getMolecule());
            mediator.getAlignmentCanvas().setMainAlignment(new StructuralAlignment(mediator, referenceMolecule, new ReferenceStructure(mediator, referenceMolecule, this.secondaryStructure),new ArrayList<AlignedMolecule>()));
        }
        this.repaint();
    }

    public void centerSecondaryStructure() {
        Point2D center = null;
        if (!this.selectedResidues.isEmpty()) {
            center = this.mediator.getSecondaryStructure().getCenter(this.gc, this.selectedResidues);
        } else
            center = this.mediator.getSecondaryStructure().getCenter(this.gc, this.secondaryStructure.getResidues());
        double dragx =  center.getX() - this.getWidth()/2,
                dragY = center.getY() - this.getHeight()/2;

        this.getGraphicContext().viewX -= dragx;
        this.getGraphicContext().viewY -= dragY;
        this.repaint();
    }

    public void clear() {
        this.secondaryStructure = null;
        this.repaint();
    }

    /**
     * Load a preconstructed secondary structure
     * @param ss
     */
    public void setSecondaryStructure(SecondaryStructure ss) {
        this.clearSelection();
        double viewX = -1, viewY = -1, zoom = -1;
        if (this.gc != null) {
            viewX = this.gc.getViewX();
            viewY = this.gc.getViewY();
            zoom = this.gc.getFinalZoomLevel();
        }
        this.gc = new GraphicContext(this);
        this.secondaryStructure = ss;
        this.secondaryStructure.findJunctions();
        if (this.secondaryStructure.isPlotted())
            this.gc.initialize(this.secondaryStructure);
        else {
            try {
                Pair<Double, Double> sizes =  new Rnaplot(mediator).plot(this.secondaryStructure);
                if (sizes.getFirst() > this.getWidth() && sizes.getSecond() > this.getHeight()) {
                    Residue first = this.secondaryStructure.getResidue(1);
                    double transX = first.getX()-this.getWidth()/2,
                            transY = first.getY()-this.getHeight()/2;
                    for (Residue r:this.secondaryStructure.getResidues())
                        r.setRealCoordinates(r.getX()-transX,r.getY()-transY);
                }
                this.gc.initialize(this.secondaryStructure);
            } catch (Exception e) {
                e.printStackTrace();
                this.secondaryStructure = null;
                this.gc = null;
            }
        }
        if (viewX != -1 && viewY != -1 && zoom != -1) {
            this.gc.viewX = viewX;
            this.gc.viewY = viewY;
            this.gc.finalZoomLevel = zoom;
        }
        mediator.getSecondaryStructureNavigator().reconstructTree();
        if (mediator.getAlignmentCanvas().getMainAlignment() == null) { //the molecule of this 2D initiates a new structural alignment
            AlignedMolecule referenceMolecule = new AlignedMolecule(mediator, this.secondaryStructure.getMolecule());
            mediator.getAlignmentCanvas().setMainAlignment(new StructuralAlignment(mediator, referenceMolecule, new ReferenceStructure(mediator, referenceMolecule, this.secondaryStructure),new ArrayList<AlignedMolecule>()));
        }
        this.repaint();
    }

    public void closeSession() {
        this.secondaryStructure = null;
        this.gc = null;
        this.repaint();
    }

    public void setSelectedHelix(Helix helix) {
        this.selectedHelix = helix;
    }

    public void fitToPage() {
        double minX = this.secondaryStructure.getCurrentMinX(this.gc, this.secondaryStructure.getResidues()),
                minY = this.secondaryStructure.getCurrentMinY(this.gc, this.secondaryStructure.getResidues());
        this.getGraphicContext().viewX -= minX-2*this.gc.getCurrentWidth();
        this.getGraphicContext().viewY -= minY-2*this.gc.getCurrentHeight();
        this.repaint();
    }
}
