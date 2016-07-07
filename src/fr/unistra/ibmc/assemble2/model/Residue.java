package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class Residue implements Comparable {

    private StructuralDomain structuralDomain;
    private BaseBaseInteraction secondaryInteraction;
    private java.util.List<BaseBaseInteraction> tertiaryInteractions;
    private String quantitativeValue = null,qualitativeValue = null;
    private double x, y;
    private boolean selected, updated;
    private int absolutePosition;
    private SecondaryStructure ss;
    private Molecule molecule;
    private Mediator mediator;
    private Ellipse2D circle;
    private Color customColor;

    public Residue(Mediator mediator, Molecule molecule, int absolutePosition) {
        this.absolutePosition = absolutePosition;
        this.updated = true;
        this.molecule = molecule;
        this.tertiaryInteractions = new ArrayList<BaseBaseInteraction>();
        this.mediator = mediator;
    }

    public Residue(Mediator mediator, SecondaryStructure ss, int absolutePosition) {
        this(mediator, ss.getMolecule(), absolutePosition);
        this.ss = ss;
    }

    public Color getCustomColor() {
        return customColor;
    }

    public void setCustomColor(Color customColor) {
        this.customColor = customColor;
    }

    /**
     * Return the circle drawn for this residue
     * @return
     */
    public Ellipse2D getCircle() {
        return circle;
    }

    public void setAbsolutePosition(int absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    public String getQuantitativeValue() {
        return quantitativeValue;
    }

    public void setQuantitativeValue(String quantitativeValue) {
        this.quantitativeValue = quantitativeValue;
    }

    public String getQualitativeValue() {
        return qualitativeValue;
    }

    public void setQualitativeValue(String qualitativeValue) {
        this.qualitativeValue = qualitativeValue;
    }

    public BaseBaseInteraction getSecondaryInteraction() {
        return secondaryInteraction;
    }

    public List<BaseBaseInteraction> getTertiaryInteractions() {
        return tertiaryInteractions;
    }

    public void setSecondaryInteraction(BaseBaseInteraction secondaryInteraction) {
        this.secondaryInteraction = secondaryInteraction;
    }

    public void addTertiaryInteraction(BaseBaseInteraction tertiaryInteraction) {
        this.tertiaryInteractions.add(tertiaryInteraction);
    }

    public void removeSecondaryInteraction() {
        this.secondaryInteraction = null;
    }

    public void removeTertiaryInteraction(BaseBaseInteraction tertiaryInteraction) {
        this.tertiaryInteractions.remove(tertiaryInteraction);
    }

    public Molecule getMolecule() {
        return this.ss.getMolecule();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getAbsolutePosition() {
        return absolutePosition;
    }

    /**
     * If the molecule containing this residue is not a genomic annotation, this method returns the same result that the method getAbsolutePosition().
     * @return
     */
    public int getGenomicPosition() {
        return this.molecule.isGenomicAnnotation() ? (this.molecule.isPlusOrientation() ?  this.getAbsolutePosition()+this.molecule.getFivePrimeEndGenomicPosition()-1 : this.molecule.getFivePrimeEndGenomicPosition()+this.molecule.size()-1-this.getAbsolutePosition()+1) : this.getAbsolutePosition();
    }

    public boolean isInsideDrawingArea(GraphicContext gc) {
        Rectangle r = gc.getDrawingArea();
        return r.contains(this.getCurrentX(gc), this.getCurrentY(gc))
                || r.contains(this.getCurrentX(gc) + gc.getCurrentWidth(), this.getCurrentY(gc))
                || r.contains(this.getCurrentX(gc), this.getCurrentY(gc) - gc.getCurrentHeight())
                || r.contains(this.getCurrentX(gc) + gc.getCurrentWidth(), this.getCurrentY(gc) - gc.getCurrentHeight());
    }

    public void isSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public double getCurrentX(GraphicContext gc) {
        return this.x * gc.getFinalZoomLevel() + gc.getViewX();
    }

    public double getCurrentY(GraphicContext gc) {
        return this.y * gc.getFinalZoomLevel() + gc.getViewY();
    }

    public boolean contains(final double x, final double y,GraphicContext gc) {
        return this.getArea(gc).contains(x, y);
    }

    public void setRealCoordinates(final java.awt.geom.Point2D coord) {
        this.setRealCoordinates(coord.getX(), coord.getY());
    }

    public double getCurrentCenterX(GraphicContext gc) {
        return this.getCurrentX(gc) + gc.getCurrentWidth() / 2;
    }

    public double getRealCenterX(GraphicContext gc) {
        return this.x + gc.getRealWidth() / 2;
    }

    public double getCurrentCenterY(GraphicContext gc) {
        return this.getCurrentY(gc) - gc.getCurrentHeight() / 2;
    }

    public double getRealCenterY(GraphicContext gc) {
        return this.y - gc.getRealHeight() / 2;
    }

    public java.awt.geom.Point2D getCurrentCenter(GraphicContext gc) {
        return new java.awt.geom.Point2D.Double(this.getCurrentCenterX(gc), this.getCurrentCenterY(gc));
    }

    public java.awt.geom.Point2D getRealCenter(GraphicContext gc) {
        return new java.awt.geom.Point2D.Double(this.getRealCenterX(gc), this.getRealCenterY(gc));
    }

    public void setRealCoordinates(final double x, final double y) {
        this.x = x;
        this.y = y;
        this.updated = true;
    }

    public Point2D getRealCoordinates() {
        return new Point2D.Double(this.x, this.y);
    }

    public Point2D getCurrentCoordinates(GraphicContext gc) {
        return new Point2D.Double(this.getCurrentX(gc), this.getCurrentY(gc));
    }

    public Area getArea(GraphicContext gc) {
        return new Area(new java.awt.geom.Rectangle2D.Double(this.getCurrentX(gc), this.getCurrentY(gc) - gc.getCurrentHeight(), gc.getCurrentWidth(), gc.getCurrentHeight()));
    }

    public java.awt.Color getFinalColor(GraphicContext gc) {
        if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().QUANTITATIVE_DATA) {
            if (this.quantitativeValue != null)
                return gc.getQuantitativeValue2Color(Float.parseFloat(this.quantitativeValue));
            else
                return gc.noQuantitativeValueColor;
        } else if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().QUALITATIVE_DATA) {
            if (this.qualitativeValue != null)
                return gc.getQualitative2Colors().get(this.qualitativeValue);
            else
                return gc.noQualitativeValueColor;
        } else if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE) {
            return mediator.getAlignmentCanvas().getConsensusColor(this.getGenomicPosition());
        } else if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().BP_PROBABILITIES) {
            return gc.getBpProbabilityColor(mediator.getFoldingLandscape().getBpProbability(this.absolutePosition));
        }
        else  {
            Color c = null;
            if (this.customColor != null) {
                c = this.customColor;
            }
            if (c == null && this.getSecondaryInteraction() != null && this.getSecondaryInteraction().getCustomColor() != null) {
                c = this.getSecondaryInteraction().getCustomColor();
            }
            if (c == null && !this.getTertiaryInteractions().isEmpty()) {
                for (BaseBaseInteraction bbi: this.getTertiaryInteractions())
                    if (bbi.getCustomColor() != null) {
                        c = bbi.getCustomColor();
                        break;
                    }
            }
            if (c == null && this.getStructuralDomain().getCustomColor() != null) {
                c = this.getStructuralDomain().getCustomColor();
            }
            if (c == null) {
                switch (this.getSymbol()) {
                    case 'A':
                        c = Assemble.A_Color;
                        break;
                    case 'U':
                    case 'T':
                        c = Assemble.U_Color;
                        break;
                    case 'G':
                        c = Assemble.G_Color;
                        break;
                    case 'C':
                        c = Assemble.C_Color;
                        break;
                    default:
                        c = Color.BLACK;
                }
            }
            return c;
        }
    }

    public Residue getNextResidue() {
        return this.ss.getResidue(this.absolutePosition+1);
    }

    public Residue getPreviousResidue() {
        return this.ss.getResidue(this.absolutePosition-1);
    }

    public char getSymbol() {
        return this.ss.getMolecule().getResidueAt(this.absolutePosition ).charAt(0);
    }

    public void isUpdated(final boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return this.updated;
    }

    public void draw(final java.awt.Graphics2D g, GraphicContext gc) {
        if (!this.isSelected() &&  gc.isDrawOnlySelectedMotif())
            return;
        if (this.isInsideDrawingArea(gc) || gc.isDrawInPNG()) {
            drawStringCentered(g, "" + this.getSymbol(), this.getCurrentCenterX(gc), this.getCurrentCenterY(gc), gc);
            g.setColor(Color.BLACK);
            Residue nextR = this.getNextResidue(), previousR = this.getPreviousResidue();
            if (nextR != null && previousR == null)
                g.drawString("5'", (float) (this.getCurrentX(gc) - (nextR.getCurrentX(gc) - this.getCurrentX(gc))), (float) (this.getCurrentY(gc) - (nextR.getCurrentY(gc) - this.getCurrentY(gc))));
            else if (previousR != null && nextR == null)
                g.drawString("3'", (float) (this.getCurrentX(gc) + this.getCurrentX(gc) - previousR.getCurrentX(gc)), (float) (this.getCurrentY(gc) + this.getCurrentY(gc) - previousR.getCurrentY(gc)));
            if (this.isSelected() && gc.isDrawOnlySelectedMotif()) {
                //if the previous or next residue are not exported in the motif capture, we display the id of this residue
                //if (this.getNextResidue() == null || !this.getNextResidue().isSelected() || this.getPreviousResidue()== null || !this.getPreviousResidue().isSelected())
                //    gc.addToSelectionArea(this.drawAbsoluteId(g2).getBounds2D()); // the area of the id extend the selection area
                gc.addToSelectionArea(this.getArea(gc).getBounds2D());
                gc.addResidueFromMotif(this);
            }
        }
    }

    private void drawStringCentered(Graphics2D g2, String res, double x, double y, GraphicContext gc) {
        g2.setFont(gc.getFont());
        Rectangle2D r2d = g2.getFontMetrics(gc.getFont()).getStringBounds(res, g2);
        Dimension d = getStringDimension(g2, res, gc);
        x -= (double)d.height/2.0;
        y -= (double)d.height/2.0;
        this.circle = new Ellipse2D.Double(x, y, d.height, d.height);
        g2.setColor(this.getFinalColor(gc));
        if (this.isSelected() &&  !gc.isDrawOnlySelectedMotif()) {
            Stroke previousStroke = g2.getStroke();
            if (g2.getFont().getSize() > 15 )
                g2.setStroke(new BasicStroke(2));
            else
                g2.setStroke(new BasicStroke(1));
            g2.draw(this.circle);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.fill(this.circle);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setStroke(previousStroke);

            Font _newFont = g2.getFont().deriveFont((float) g2.getFont().getSize() * 0.6f);
            g2.setFont(_newFont);
            Rectangle2D _r2d = g2.getFontMetrics(g2.getFont()).getStringBounds(res, g2);
            if (g2.getFont().getSize() > 15 && gc.isDisplayResidues()) {
                g2.drawString(res, (int)(this.circle.getMinX()+r2d.getWidth()*(1-_r2d.getWidth()/r2d.getWidth())/1.5), (int)(this.circle.getMaxY()-r2d.getHeight()*(1-_r2d.getHeight()/r2d.getHeight())/3));
                g2.setColor(Color.BLACK);
            }
        }
        else {
            g2.fill(this.circle);
            Font _newFont = g2.getFont().deriveFont((float) g2.getFont().getSize() * 0.6f);
            g2.setFont(_newFont);
            Rectangle2D _r2d = g2.getFontMetrics(g2.getFont()).getStringBounds(res, g2);
            if (g2.getFont().getSize() > 15 && gc.isDisplayResidues()) {
                g2.setColor(Color.WHITE);
                g2.drawString(res, (int)(this.circle.getMinX()+r2d.getWidth()*(1-_r2d.getWidth()/r2d.getWidth())/1.5), (int)(this.circle.getMaxY()-r2d.getHeight()*(1-_r2d.getHeight()/r2d.getHeight())/3));
                g2.setColor(Color.BLACK);
            }
        }

    }

    private Dimension getStringDimension(Graphics2D g, String s, GraphicContext gc) {
        FontMetrics fm = g.getFontMetrics(gc.getFont());
        Rectangle2D r = fm.getStringBounds(s, g);
        return (new Dimension((int)r.getWidth(),(int)fm.getAscent()-fm.getDescent()));
    }

    public void translate(double transX, double transY,GraphicContext gc) {
        transX /= gc.getFinalZoomLevel();
        transY /= gc.getFinalZoomLevel();
        this.setRealCoordinates(this.x +transX,this.y + transY);
    }

    public void rotate(java.awt.geom.Point2D centerPoint, double angle,GraphicContext gc) {
        Point2D p = DrawingUtils.getRotatedPoint(this.getCurrentCoordinates(gc), angle, centerPoint);
        this.setRealCoordinates((p.getX() - gc.getViewX()) / gc.getFinalZoomLevel(), (p.getY() - gc.getViewY()) / gc.getFinalZoomLevel());
    }

    public void flip(final Point2D center,GraphicContext gc) {
        this.translate(-2.0 * (this.getCurrentX(gc) - center.getX()), 0.0,gc);
    }

    public boolean isInsideHelix() {
        return Helix.class.isInstance(this.structuralDomain);
    }

    public StructuralDomain getStructuralDomain() {
        return structuralDomain;
    }

    public void setStructuralDomain(StructuralDomain structure) {
        this.structuralDomain = structure;
    }

    public int compareTo(Object o) {
        if (!Residue.class.isInstance(o))
            return -1;
        else if (this.ss.getMolecule() != ((Residue)o).ss.getMolecule())
            return -1;
        else
            return this.getAbsolutePosition()-((Residue)o).getAbsolutePosition();
    }
}
