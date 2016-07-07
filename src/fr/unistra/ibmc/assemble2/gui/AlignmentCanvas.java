package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


public class AlignmentCanvas extends JPanel implements MouseWheelListener, ActionListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener {

    public static final char STANDARD = 's',  INSERT = 'i',  DELETE = 'd',  USER_DEFINED_INTERACTIONS = 'u';
    GraphicContext gc;
    int pressedX, pressedY, mouseX, mouseY;

    //**** the parameters for the user defined interactions ****
    int firstUserDefinedInteractionX = -1, firstUserDefinedInteractionY = -1, secondUserDefinedInteractionX = -1, secondUserDefinedInteractionY = -1;
    //*************
    char mode = AlignmentCanvas.STANDARD;
    public static final int MAXLABELSIZE = 20;
    private AlignmentView mouseOverAlignmentView;
    int leftMarge = 1, topMarge = 1, bottomMarge = 0;
    private Rectangle drawingArea;
    Mediator mediator;
    private java.util.List<AlignmentView> alignmentViews;
    private AlignmentView selectedView, pressedView;
    private StructuralAlignment mainAlignment;
    protected Annotation most_downstream_annotation = null, most_upstream_annotation = null;
    private AlignmentToolBar alignmentToolBar;
    private boolean displayAlignmentToolbar = true, editSequences = false;

    public AlignmentCanvas(Mediator mediator, int screenWidth, int screenHeight) {
        this.gc = new GraphicContext();
        this.mediator = mediator;
        this.mediator.setAlignmentCanvas(this);
        this.setPreferredSize(new Dimension((int) (screenWidth * 0.8), (int) (screenHeight * 0.8)));
        this.setBackground(java.awt.Color.WHITE);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.registerKeyboardAction(this, "standard", KeyStroke.getKeyStroke('s'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "insert", KeyStroke.getKeyStroke('i'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "delete", KeyStroke.getKeyStroke('d'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "user-defined-interactions", KeyStroke.getKeyStroke('u'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "move-view-right", KeyStroke.getKeyStroke('r'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "move-view-left", KeyStroke.getKeyStroke('e'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "move-residues-right", KeyStroke.getKeyStroke('p'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(this, "move-residues-left", KeyStroke.getKeyStroke('o'), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.alignmentViews = new ArrayList<AlignmentView>();
        this.alignmentViews.add(new AlignmentView(mediator, this, this.mainAlignment));
        this.alignmentViews.add(new AlignmentView(mediator, this, this.mainAlignment));
        this.alignmentToolBar = new AlignmentToolBar(mediator);
    }

    public boolean isEditSequences() {
        return editSequences;
    }

    public void setEditSequences(boolean editSequences) {
        this.editSequences = editSequences;
    }

    public boolean isDisplayAlignmentToolbar() {
        return displayAlignmentToolbar;
    }

    public void setDisplayAlignmentToolbar(boolean displayAlignmentToolbar) {
        this.displayAlignmentToolbar = displayAlignmentToolbar;
    }

    public void removeBaseBaseInteraction(BaseBaseInteraction interaction) {
        for (Symbol s:this.mainAlignment.getReferenceStructure().getSymbols()) {
            if (s.getPositionInSequence() == interaction.getResidue().getAbsolutePosition() && this.mainAlignment.getBiologicalReferenceSequence().getMolecule().equals(interaction.getResidue().getMolecule())) {
                ((ReferenceStructureSymbol)s).removeBaseBaseInteraction(interaction);
                this.repaint();
                return;
            }
        }
    }

    public void addBaseBaseInteraction(BaseBaseInteraction interaction) {
        ReferenceStructureSymbol symbol = null, pairedSymbol=null;
        for (Symbol s:this.mainAlignment.getReferenceStructure().getSymbols()) {
            if (s.getPositionInSequence() == interaction.getResidue().getAbsolutePosition() && this.mainAlignment.getBiologicalReferenceSequence().getMolecule().equals(interaction.getResidue().getMolecule()))
                symbol = (ReferenceStructureSymbol)s;
            else if (s.getPositionInSequence() == interaction.getPartnerResidue().getAbsolutePosition() && this.mainAlignment.getBiologicalReferenceSequence().getMolecule().equals(interaction.getPartnerResidue().getMolecule())) {
                pairedSymbol = (ReferenceStructureSymbol)s;
            }
            if (symbol!= null && pairedSymbol != null) {
                symbol.addReferenceBaseBaseInteraction(interaction, pairedSymbol);
                this.repaint();
                return;
            }

        }
    }

    public GraphicContext getGraphicContext() {
        return gc;
    }

    public void clear() {
        this.mainAlignment = null;
        this.selectedView = null;
        this.repaint();
    }

    public java.util.List<AlignmentView> getAlignmentViews() {
        return new ArrayList<AlignmentView>(alignmentViews);
    }

    public AlignmentView getSelectedView() {
        return this.selectedView;
    }

    public void setSelectedView(AlignmentView view) {
        this.selectedView = view;
    }

    public StructuralAlignment getMainAlignment() {
        return mainAlignment;
    }

    public void removeLastView() {
        this.alignmentViews.remove(this.alignmentViews.get(this.alignmentViews.size() - 1));
        this.alignmentViews.remove(this.alignmentViews.get(this.alignmentViews.size() - 1));
        this.repaint();
    }

    void removeBiologicalSequenceAt(int index) {
        this.mainAlignment.removeBiologicalSequenceAt(index);
        this.repaint();
    }

    public void setMainAlignment(final StructuralAlignment alignment) {
        this.mainAlignment = alignment;
        for (AlignmentView view:this.alignmentViews)
            view.setAlignment(alignment);
        this.repaint();
        //mediator.getAssemble().getAlignmentPanelWindow().setVisible(true);
    }

    void setMediator(final Mediator mediator) {
        this.mediator = mediator;
    }

    public void setQuickDraw(final boolean quickDraw) {
        this.gc.setQuickDraw(quickDraw);
        this.repaint();
    }

    public void modifyFontSize(final int level) {
        final Font f = this.gc.getAlignmentFont();
        this.gc.setAlignmentFont(f.deriveFont((float) (f.getSize() + level)));
        this.repaint();
    }

    public AlignmentToolBar getAlignmentToolBar() {
        return alignmentToolBar;
    }

    public void paintComponent(final java.awt.Graphics g) {
        super.paintComponent(g);
        final java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        if (this.mainAlignment != null) {
            g2.setFont(this.gc.getAlignmentFont());
            //we start with the BLACK color
            g2.setColor(Color.BLACK);
            final FontMetrics fm = g2.getFontMetrics();
            int previousHorizontalAdvance = this.gc.getHorizontalAdvance();
            this.gc.setHorizontalAdvance(fm.stringWidth("A"));
            float ratio = (float)previousHorizontalAdvance / (float)this.gc.getHorizontalAdvance();
            for (AlignmentView view : this.alignmentViews)
                view.viewX = ((int)(view.viewX/ratio));
            this.gc.setVerticalAdvance(fm.getHeight());
            this.gc.setLetterHeight(fm.getAscent() + fm.getDescent());
            this.gc.setLetterWidth(fm.charWidth('A'));
            this.gc.currentX = (this.displayAlignmentToolbar ? (int)this.alignmentToolBar.getWidth() : 0 ) + this.leftMarge * this.gc.getHorizontalAdvance();
            this.gc.currentY = this.topMarge * this.gc.getVerticalAdvance();
            int totalHeight = this.getTotalHeight(gc);
            this.setPreferredSize(new Dimension(this.getSize().width, totalHeight));
            this.revalidate();
            this.gc.currentWidth = this.getSize().width - 2 * this.leftMarge * this.gc.getHorizontalAdvance();
            this.drawingArea = new Rectangle(gc.currentX, gc.currentY, gc.currentWidth, this.getTotalHeight(gc));
            //g2.setColor(Color.RED);
            //g2.draw(this.drawingArea);
            //g2.setColor(Color.BLACK);
            for (AlignmentView view : new ArrayList<AlignmentView>(this.alignmentViews)) {
                gc.currentY += view.getTopMarge() * gc.getVerticalAdvance();
                gc.currentX += view.getLeftMarge() * gc.getHorizontalAdvance();
                gc.currentWidth -= 2 * view.getLeftMarge() * gc.getHorizontalAdvance();
                view.draw(g2, gc);
                gc.currentY += view.getBottomMarge() * gc.getVerticalAdvance();
                gc.currentX -= view.getLeftMarge() * gc.getHorizontalAdvance();
                gc.currentWidth += 2 * view.getLeftMarge() * gc.getHorizontalAdvance();
            }
            gc.setCurrentX(gc.getCurrentX() - this.leftMarge * gc.getHorizontalAdvance());
            gc.setCurrentY(gc.getCurrentY() + this.bottomMarge * gc.getVerticalAdvance());
            gc.currentWidth += 2 * this.leftMarge * gc.getHorizontalAdvance();

            /*int alignmentViewNb = this.alignmentViews.size();
            for (int i = 0; i < alignmentViewNb - 1; i++) {
                if (this.alignmentViews.get(i).alignment == this.mainAlignment && this.alignmentViews.get(i + 1).alignment == this.mainAlignment) {
                    this.drawBaseBaseInteractions(g2, gc, this.alignmentViews.get(i), this.alignmentViews.get(i + 1));
                }
            }
            if (this.mode == AlignmentCanvas.USER_DEFINED_INTERACTIONS && this.firstUserDefinedInteractionX != -1 && this.firstUserDefinedInteractionY != -1 && this.secondUserDefinedInteractionX != -1 && this.secondUserDefinedInteractionY != -1) {
                g.drawLine(this.firstUserDefinedInteractionX, this.firstUserDefinedInteractionY, this.secondUserDefinedInteractionX, this.secondUserDefinedInteractionY);
            }*/
        }
        if (this.displayAlignmentToolbar)
            this.alignmentToolBar.draw(g2, 10, 10);
    }

    private void drawBaseBaseInteractions(final java.awt.Graphics2D g, GraphicContext gc, AlignmentView upperView, AlignmentView lowerView) {
        for (int j = upperView.getFirstPos(); j <= upperView.getLastPos(); j++) {
            if (upperView.selectedPositionsToDrawInteractions.isEmpty() || upperView.selectedPositionsToDrawInteractions.contains(upperView.getAlignment().getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence())) {
                ReferenceStructureSymbol s = (ReferenceStructureSymbol)upperView.getAlignment().getReferenceStructure().getSymbol(j);
                //if the position is not a gap and not a MutatableSymbol (meaning not a non-gap symbol corresponding a structural position of the structure of reference)
                if (!s.isGap()) {
                    for (BaseBaseInteraction interaction : s.getReferenceBaseBaseInteractions()) {
                        if ((interaction.isSecondaryInteraction() && upperView.displaySecondaryInteractions || (!interaction.isSecondaryInteraction() && upperView.displayTertiaryInteractions))) {
                            ReferenceStructureSymbol pairedSymbol = s.getPairedSymbol(interaction);
                            int partnerPositionInAlignment = upperView.getAlignment().getReferenceStructure().getIndex(pairedSymbol);
                            //[fjossinet] the first part of the next test is to avoid to draw twice the interaction if the two residues establishing this interaction are displayed both in the upperView and lowerView
                            if ( /*(partnerPositionInAlignment < j && partnerPositionInAlignment >= upperView.firstPos && partnerPositionInAlignment <= upperView.lastPos) && */partnerPositionInAlignment >= lowerView.getFirstPos() && partnerPositionInAlignment <= lowerView.getLastPos()) {
                                int x1 = upperView.getCurrentColumnCoordinate(j),
                                        y1 = (int) upperView.getDrawingArea().getMaxY(),
                                        x2 = lowerView.getCurrentColumnCoordinate(partnerPositionInAlignment),
                                        y2 = (int) lowerView.getDrawingArea().getMinY();


                                //****************************
                                Color c = (interaction.isSecondaryInteraction()) ? Assemble.SecondaryInteraction_Color : Assemble.TertiaryInteraction_Color;

                                g.setColor(c.darker());
                                java.util.List<BaseEdgeShape> shapes = new ArrayList<BaseEdgeShape>();
                                final double distance = DrawingUtils.getDistance(x1, y1, x2, y2);
                                //using the VerticalAdvance for the symbolSize, this size will be fit the zoom level (since the VerticalAdvance is updated each time the font size change)
                                double symbolSize = 2 * gc.getVerticalAdvance();
                                //the points outside the symbol
                                Point2D[] ip1 = null;
                                if (distance >= symbolSize) {
                                    ip1 = DrawingUtils.fit(x1, y1, x2, y2, (distance - symbolSize) / 2);
                                }
                                if (ip1 != null) {
                                    String type = s.getEdge(interaction) + "" + pairedSymbol.getEdge(interaction);
                                    if ("[]".equals(type)) {
                                        if (!upperView.displayCisHH && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransHH && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            this.drawHHSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                        }
                                    } else if ("{}".equals(type)) {
                                        if (!upperView.displayCisSESE && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransSESE && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            this.drawSESESymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                        }
                                    } else if ("()".equals(type)) {
                                        if (!upperView.displayCisWCWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransWCWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            this.drawWCWCSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                        }
                                    } else if ("{]".equals(type) || "[}".equals(type)) {
                                        if (!upperView.displayCisHSE && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransHSE && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            if ("{]".equals(type)) {
                                                this.drawSEHSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            } else {
                                                this.drawHSESymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            }
                                        }
                                    } else if ("[)".equals(type) || "(]".equals(type)) {
                                        if (!upperView.displayCisHWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransHWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            if ("[)".equals(type)) {
                                                this.drawHWCSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            } else {
                                                this.drawWCHSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            }
                                        }
                                    } else if ("(}".equals(type) || "{)".equals(type)) {
                                        if (!upperView.displayCisSEWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_CIS) {

                                        } else if (!upperView.displayTransSEWC && interaction.getOrientation() == BaseBaseInteraction.ORIENTATION_TRANS) {

                                        } else {
                                            shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), ip1[0]));
                                            shapes.add(new LineEdgeShape(ip1[1], new Point2D.Double(x2, y2)));
                                            if ("(}".equals(type)) {
                                                this.drawWCSESymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            } else {
                                                this.drawSEWCSymbol(shapes, ip1[0], ip1[1], symbolSize, interaction.getOrientation());
                                            }
                                        }
                                    }
                                } //if we cannot draw the symbol (not enough space), we draw only a line
                                else {
                                    shapes.add(new LineEdgeShape(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2)));
                                }
                                for (BaseEdgeShape shape : shapes) {
                                    shape.draw(g);
                                }
                                //******************
                            }
                        }
                    }
                }
            }
        }
        g.setColor(Color.BLACK);
    }

    private void drawHHSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        //we divide the symbol in three part each enclosing 1/3 of the distance
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we draw the lines
        shapes.add(new LineEdgeShape(point1, points[0]));
        shapes.add(new LineEdgeShape(point2, points[1]));
        //we draw the squarre
        shapes.add(new SquarreEdgeShape(point1, point2, points[0], points[1], distance, orientation));
    }

    private void drawWCWCSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        //we divide the symbol in three part each enclosing 1/3 of the distance
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we draw the lines
        shapes.add(new LineEdgeShape(point1, points[0]));
        shapes.add(new LineEdgeShape(point2, points[1]));
        //we draw the circle
        //the center of the circle
        final Point2D center = new Point2D.Double((point1.getX() + point2.getX()) / 2, (point1.getY() + point2.getY()) / 2);
        shapes.add(new CircleEdgeShape(points[0], center, orientation));
    }


    //TODO fjossinet the symbol is symetric but the interaction is asymetric, so fix that (take a look at how it is done in the RNA2DViewer tool)

    private void drawSESESymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we draw the lines
        shapes.add(new LineEdgeShape(point1, points[0]));
        shapes.add(new LineEdgeShape(point2, points[1]));
        shapes.add(new TriangleEdgeShape(point1, point2, points[0], points[1], distance, orientation));
    }

    private void drawSEHSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we construct the square
        shapes.add(new TriangleEdgeShape(point1, point2, point1, points[0], distance, orientation));
        //we construct the triangle
        shapes.add(new SquarreEdgeShape(point1, point2, points[1], point2, distance, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private void drawHSESymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we construct the square
        shapes.add(new SquarreEdgeShape(point1, point2, point1, points[0], distance, orientation));
        //we construct the triangle
        shapes.add(new TriangleEdgeShape(point1, point2, points[1], point2, distance, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private void drawHWCSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        //we divide the symbol in three part each enclosing 1/3 of the distance
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we draw the circle
        //the center of the circle
        final Point2D center = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 6)[1];
        //we construct the squarre
        shapes.add(new SquarreEdgeShape(point1, point2, point1, points[0], distance, orientation));
        shapes.add(new CircleEdgeShape(point2, center, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private void drawWCHSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        //we divide the symbol in three part each enclosing 1/3 of the distance
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        //we draw the circle
        //the center of the circle
        final Point2D center = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 6)[0];
        //we construct the squarre
        shapes.add(new CircleEdgeShape(point1, center, orientation));
        shapes.add(new SquarreEdgeShape(point1, point2, point2, points[1], distance, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private void drawWCSESymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        final Point2D center = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 6)[0];
        //we construct the circle
        shapes.add(new CircleEdgeShape(point1, center, orientation));
        //we construct the triangle
        shapes.add(new TriangleEdgeShape(point1, point2, points[1], point2, distance, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private void drawSEWCSymbol(java.util.List<BaseEdgeShape> shapes, final Point2D point1, final Point2D point2, final double distance, char orientation) {
        final Point2D[] points = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 3);
        final Point2D center = DrawingUtils.fit(point1.getX(), point1.getY(), point2.getX(), point2.getY(), distance / 6)[1];
        //we construct the triangle
        shapes.add(new TriangleEdgeShape(point1, point2, points[0], point1, distance, orientation));
        //we construct the circle
        shapes.add(new CircleEdgeShape(point2, center, orientation));
        //we draw the line between the two forms
        shapes.add(new LineEdgeShape(points[0], points[1]));
    }

    private Color getStructuralAlignmentRelation(char symbol) {
        switch (symbol) {
            case '.':
                return Color.GREEN;
            case '{':
            case '}':
            case ')':
            case '(':
            case '>':
            case '<':
                return Color.BLUE;
            default:
                return Color.BLACK;
        }
    }

    private int getTotalHeight(GraphicContext gc) {
        int height = 0;
        for (AlignmentView view : this.alignmentViews) {
            height += view.getTopMarge() * gc.getVerticalAdvance();
            height += view.getHeight(gc);
            height += view.getBottomMarge() * gc.getVerticalAdvance();
        }
        height += this.topMarge * gc.getVerticalAdvance();
        height += this.bottomMarge * gc.getVerticalAdvance();
        return height;
    }

    public void mousePressed(final MouseEvent e) {
        this.pressedX = e.getX();
        this.pressedY = e.getY();
        for (AlignmentView v : this.alignmentViews) {
            if (v.getDrawingArea().contains(e.getX(), e.getY())) {
                v.mousePressed(e);
                this.pressedView = v;
                break;
            }
        }
        this.repaint();
    }

    public void mouseDragged(final java.awt.event.MouseEvent e) {
        if (this.pressedView != null ) //to be able to move the residues even if the mouse is dragged outside the pressedView
            this.pressedView.mouseDragged(e);
        this.repaint();
    }

    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            mediator.getSecondaryCanvas().clearSelection();
            mediator.getTertiaryFragmentsPanel().clearList();
            if (mediator.getChimeraDriver() != null)
                mediator.getChimeraDriver().selectionCleared();
            this.clearSelectedPositions();
        } else {
            if (this.displayAlignmentToolbar)
                this.alignmentToolBar.mouseClicked(e, 10, 10);
            if (this.mainAlignment != null)
                for (AlignmentView v : this.alignmentViews) {
                    v.mouseClicked(e);
                    this.repaint();
                }
        }
    }

    public void mouseReleased(final MouseEvent e) {
        if (this.mainAlignment != null)
            for (AlignmentView v : this.alignmentViews) {
                if (v.getDrawingArea().contains(e.getX(), e.getY())) {
                    v.mouseReleased(e);
                    break;
                }
            }
        this.repaint();
    }


    public void mouseMoved(final MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        if (this.displayAlignmentToolbar)
            this.alignmentToolBar.mouseMoved(e, 10, 10);
        if (this.mode == AlignmentCanvas.USER_DEFINED_INTERACTIONS) {
            this.secondUserDefinedInteractionX = e.getX();
            this.secondUserDefinedInteractionY = e.getY();
        } else {
            this.mouseOverAlignmentView = null;
            if (this.mainAlignment != null)
                for (AlignmentView v : this.alignmentViews) {
                    if (v.contains(e.getX(), e.getY())) {
                        this.mouseOverAlignmentView = v;
                        break;
                    }
                }
        }
        this.repaint();
    }

    public void mouseEntered(MouseEvent e) {
        this.requestFocus();
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            if (this.mouseOverAlignmentView != null) {
                this.mouseOverAlignmentView.setViewY(this.mouseOverAlignmentView.getViewY() + e.getScrollAmount() * gc.getVerticalAdvance());
            }
        } else {
            if (this.mouseOverAlignmentView != null) {
                this.mouseOverAlignmentView.setViewY(this.mouseOverAlignmentView.getViewY() - e.getScrollAmount() * gc.getVerticalAdvance());
            }
        }
    }

    public BufferedImage getImage() {
        BufferedImage bufferedImage = new BufferedImage(this.getSize().width, this.getSize().height, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g2 = bufferedImage.createGraphics();
        this.paintComponent(g2);
        g2.dispose();
        return bufferedImage;
    }

    void unselectAllSequences() {
        for (int i = 0; i < this.mainAlignment.getBiologicalSequenceCount(); i++) {
            this.mainAlignment.getBiologicalSequenceAt(i).setSelected(false);
        }
        this.repaint();
    }

    void setMode(char mode) {
        this.mode = mode;
    }

    public void actionPerformed(ActionEvent event) {
        if ("move-view-right".equals(event.getActionCommand())) {
            if (this.selectedView != null) {
                this.selectedView.setViewX(this.getSelectedView().getViewX()+mediator.getHorizontalStep()*gc.getHorizontalAdvance());
                this.repaint();
            }
        }
        else if ("move-view-left".equals(event.getActionCommand())) {
            if (this.selectedView != null) {
                this.selectedView.setViewX(this.getSelectedView().getViewX()-mediator.getHorizontalStep()*gc.getHorizontalAdvance());
                this.repaint();
            }
        }
        else if ("move-residues-right".equals(event.getActionCommand())) {
            if (this.selectedView != null) {
                this.selectedView.moveResidues(true);
                if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE)
                    mediator.getSecondaryCanvas().repaint();
            }
        }
        else if ("move-residues-left".equals(event.getActionCommand())) {
            if (this.selectedView != null) {
                this.selectedView.moveResidues(false);
                if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE)
                    mediator.getSecondaryCanvas().repaint();
            }
        }
    }

    public void clearSelectedPositions() {
        if (this.mainAlignment != null) {
            for (AlignedMolecule s : this.mainAlignment.getAlignedMolecules())
                s.clearSelectedPositions();
            this.repaint();
        }
    }

    public void clearSelection() {
        this.clearSelectedPositions();
    }

    public void residuesSelectedFrom2DPanel(List<Integer> positions, boolean isShiftDown) {
        Iterator<Integer> it = positions.iterator();
        if (!isShiftDown)
            this.clearSelection();
        while (it.hasNext()) {
            this.mainAlignment.getBiologicalReferenceSequence().addSelectedPosition(it.next());
            this.repaint();
        }
    }

    public Color getConsensusColor(int position) {
        int index = this.mainAlignment.getBiologicalReferenceSequence().getSymbolIndexForMolecularPosition(0,  this.mainAlignment.getLength()-1, position);
        if (index != -1) {
            return this.mainAlignment.getConsensusStructure().getSymbol(index).getColor();
        } else
            return null;
    }
}
