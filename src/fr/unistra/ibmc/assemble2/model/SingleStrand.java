package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class SingleStrand extends AbstractStructuralDomain {

    private SecondaryStructure ss;
    private Location location;
    public static final Color DEFAULT_COLOR = Color.BLACK;
    static final int VERSE = 0;
    public static final int REVERSE = 1;
    private boolean reverse;
    int orientation = SingleStrand.VERSE;
    private double crossProduct;
    Point2D centerPoint;
    private double rayon;
    private double midX, midY;
    protected boolean flipped;
    private double dragX, dragY;
    /**
     * rotation angle between two bases
     */
    double rotationAngle;
    private Residue crossProductResidue;
    private String name;

    public SingleStrand(SecondaryStructure ss, Location location, String name) {
        this.ss = ss;
        this.location = location;
        for (Residue r:this.getResidues())
            r.setStructuralDomain(this);
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SecondaryStructure getSecondaryStructure() {
        return ss;
    }

    public boolean isApicalLoop() {
        Residue previousBase = this.getBase5().getPreviousResidue(),
                nextBase = this.getBase3().getNextResidue();
        if (previousBase == null || nextBase == null)
            return false;
        return this.ss.getEnclosingStructuralDomain(previousBase) == this.ss.getEnclosingStructuralDomain(nextBase);
    }

    public String getName() {
        return this.name;
    }

    public boolean isAtFivePrimeEnd() {
        return this.location.getStart() == 1;
    }

    public boolean isAtThreePrimeEnd() {
        return this.location.getEnd() == this.ss.getMolecule().size();
    }

    public Location getLocation() {
        return location;
    }

    public boolean contains(final double x, final double y, GraphicContext gc) {
        for (Residue r:this.getResidues())
            if (r.contains(x,y,gc))
                return true;
        return false;
    }

    public int getLength() {
        return this.location.getLength();
    }

    public java.util.List<Residue> getResidues() {
        java.util.List<Residue> residues = new ArrayList<Residue>(this.getLength());
        for (Residue r : ss.getResidues())
            if (location.hasPosition(r.getAbsolutePosition()))
                residues.add(r);
        return residues;
    }

    public void flipEnds(Point2D center,GraphicContext gc) {
    }

    public void rotateEnds(Point2D centerPoint, double angle,GraphicContext gc) {
    }

    public void translateEnds(double dragX, double dragY,GraphicContext gc) {
    }

    public boolean isSelected() {
        for (Residue r:this.getResidues())
            if (!r.isSelected())
                return false;
        return true;
    }

    public void draw(final java.awt.Graphics2D g, GraphicContext gc) {
        if (this.isInsideDrawingArea(gc) || gc.isDrawInPNG()) {
            if (this.customColor != null)
                g.setColor(this.customColor);
            else
                g.setColor(Color.GRAY);
            for (Residue r:this.getResidues())
                r.draw(g,gc);
        }
    }

    public boolean isInsideDrawingArea(GraphicContext gc) {
        Residue base = this.getBase5(), previousR = base.getPreviousResidue(), nextR = base.getNextResidue();
        while (base != null && base.getAbsolutePosition() <= this.location.getEnd()) {
            if (base.isInsideDrawingArea(gc))
                return true;
            base = base.getNextResidue();
        }
        if (nextR != null && nextR.isInsideDrawingArea(gc))
            return true;
        if (previousR != null && previousR.isInsideDrawingArea(gc))
            return true;
        return false;
    }

    public Residue getBase5() {
        return this.ss.getResidue(this.location.getStart());
    }

    public Residue getBase3() {
        return this.ss.getResidue(this.location.getEnd());
    }

    /**
     * Sets the dragDistances Attribute of the TerminalSstrand2D object
     *
     * @param base1 The new dragDistances value
     * @param base2 The new dragDistances value
     */
    private void setDragDistances(final Residue base1, final Residue base2) {
        if (this.location.getStart()-1 != 0) {
            if (orientation == SingleStrand.VERSE) {
                this.dragX = (base2.getX() - base1.getX());
                this.dragY = (base2.getY() - base1.getY());
            }
            /*
             *  else if (orientation==Sstrand2D.REVERSE) {
             *  this.dragX=(base2.getX()-base2.getPairedResidue().getX())/2;
             *  this.dragY=(base2.getY()-base2.getPairedResidue().getY())/2;
             *  }
             */
        } else {
            if (orientation == SingleStrand.VERSE) {
                this.dragX = (base1.getX() - base2.getX());
                this.dragY = (base1.getY() - base2.getY());
            }
            /*
             *  else if (orientation==Sstrand2D.REVERSE) {
             *  this.dragX=(base1.getX()-base1.getPairedResidue().getX())/2;
             *  this.dragY=(base1.getY()-base1.getPairedResidue().getY())/2;
             *  }
             */
        }
    }

    void setCenterPoint(final double x, final double y) {
        this.centerPoint = new Point2D.Double(x, y);
    }

    private void setCenterPoint(final double boundariesDistance, final double horizontal, final double vertical) {

        final Residue base5 = this.getBase5().getPreviousResidue();
        final Residue base3 = this.getBase3().getNextResidue();

        //angle pour placer le centre du cercle, exprime en radians
        double angle;

        angle = Math.PI / 3;

        /*
        if (200<boundariesDistance) {angle=Math.PI/3;}
        else if (200>=boundariesDistance && boundariesDistance>150) {angle=Math.PI/10;}
        else if (150>=boundariesDistance && boundariesDistance>100) {angle=0;}
        else if (100>=boundariesDistance && boundariesDistance>50) {angle=-Math.PI/10;}
        else {angle=-Math.PI/7;}*/
        //calcul de l'angle pour faire la rotation d'une residue
        this.crossProduct = DrawingUtils.crossProduct(base5.getRealCoordinates(), base3.getRealCoordinates(), this.crossProductResidue.getRealCoordinates());
        //calcul de l'angle de rotation
        this.rotationAngle = (Math.PI - 2 * angle) / (this.getLength() + 1);
        if (this.crossProduct >= 0) this.rotationAngle = -this.rotationAngle;
        if (reverse) this.rotationAngle = -this.rotationAngle;

        //calcul des coordonnees du centre en fonction de l'extremite 5' et de l'angle qui vient d'etre fixe

        if (this.crossProduct >= 0) angle = -angle;

        if (reverse) this.rotationAngle = -this.rotationAngle;

        if (boundariesDistance == 0)
            this.centerPoint = new Point2D.Double(base5.getX(), base5.getY());
        else if (base5.getX() <= base3.getX())
            this.centerPoint = new Point2D.Double(base5.getX() + Math.sin(Math.PI / 2 + angle + Math.asin(vertical / boundariesDistance)) * boundariesDistance / (2 * Math.cos(-angle)),
                    base5.getY() + Math.cos(Math.PI / 2 + angle + Math.asin(vertical / boundariesDistance)) * boundariesDistance / (2 * Math.cos(-angle)));
        else
            this.centerPoint = new Point2D.Double(base5.getX() - Math.sin(Math.PI / 2 - angle + Math.asin(vertical / boundariesDistance)) * boundariesDistance / (2 * Math.cos(angle)),
                    base5.getY() + Math.cos(Math.PI / 2 - angle + Math.asin(vertical / boundariesDistance)) * boundariesDistance / (2 * Math.cos(angle)));

    }

    /**
     * cette methode rend un point ayant subit une rotation d'un certain "angle" autour du point centerPoint
     * partant du point base5Boundarie par translation-rotation-translation
     */

    Point2D setRotatedCoordinates(final double angle) {
        //l'extremite 5' est translatee pour que le centre du cercle soit en (0,0)
        final Point2D.Double translatedPoint = new Point2D.Double();
        translatedPoint.setLocation(this.getBase5().getPreviousResidue().getX() - this.centerPoint.getX(), this.getBase5().getPreviousResidue().getY() - this.centerPoint.getY());

        //calcul des coordonnees cartesiennes du point subissant la rotation "angle"
        double newX = translatedPoint.getX() * Math.cos(-angle) - translatedPoint.getY() * Math.sin(-angle);
        double newY = translatedPoint.getX() * Math.sin(-angle) + translatedPoint.getY() * Math.cos(-angle);

        //les nouvelles coordonnees sont retranslatees
        newX += this.centerPoint.getX();
        newY += this.centerPoint.getY();
        return new Point2D.Double(newX, newY);
    }

    private void setCrossProductResidue() {
        if (this.getBase3().getNextResidue() != null) {
            this.crossProductResidue = this.ss.getPairedResidueInSecondaryInteraction(this.getBase3().getNextResidue());
        }
        else {
            this.crossProductResidue = this.ss.getPairedResidueInSecondaryInteraction(this.getBase5().getPreviousResidue());
        }
    }

    public void setCoordinates(GraphicContext gc) {
        //if this single-strand is the only structural domain. Consequently, draw itself as a circle
        if (this.getLength() == this.ss.getMolecule().size()) {
            double x = gc.getDrawingArea().getCenterX(), y = gc.getDrawingArea().getCenterY();
            double circonference = this.getLength()*gc.getRealHeight()+2 ; //the 2 parameter is for the 5' and 3' letters
            double rayon = (circonference/Math.PI)/2;
            Point2D startPoint = new Point2D.Double(x,y+rayon);
            Residue base5 = this.getBase5();
            int j = 0;
            double angle = (2*Math.PI)/(double)(this.getLength()+2);
            while (base5 != null && base5.getAbsolutePosition() <= this.location.getEnd()) {
                j++;
                base5.setRealCoordinates(DrawingUtils.getRotatedPoint(startPoint, angle * j, new Point2D.Double(x, y)));
                base5 = base5.getNextResidue();
            }
        }
        else {
            this.setCrossProductResidue();
            Residue r1 = this.getBase5().getPreviousResidue(), r2 = this.getBase3().getNextResidue();
            StructuralDomain sd1, sd2;
            if (r1 == null || r2 == null) {   //TerminalSstrand
                if (this.location.getStart()-1 != 0) {
                    this.setDragDistances(this.getBase5().getPreviousResidue().getPreviousResidue(), this.getBase5().getPreviousResidue());
                    Residue base5 = this.getBase5();
                    while (base5 != null && base5.getAbsolutePosition() <= this.location.getEnd()) {
                        base5.setRealCoordinates(base5.getPreviousResidue().getX() + this.dragX, base5.getPreviousResidue().getY() + this.dragY);
                        base5 = base5.getNextResidue();
                    }
                } else {
                    this.setDragDistances(this.getBase3().getNextResidue(), this.getBase3().getNextResidue().getNextResidue());
                    //si cette methode est appelee, la sstrand est une 5'terminale
                    Residue base3 = this.getBase3();
                    while (base3 != null && base3.getAbsolutePosition() >= this.location.getStart()) {
                        base3.setRealCoordinates(base3.getNextResidue().getX() + this.dragX, base3.getNextResidue().getY() + this.dragY);
                        base3 = base3.getPreviousResidue();
                    }
                }
            }
            else if (r1 != null && r2 != null) {
                sd1 = this.ss.getEnclosingStructuralDomain(r1);
                sd2 = this.ss.getEnclosingStructuralDomain(r2);
                if (sd1 != null && sd2 != null && sd1 == sd2 ) { //TerminalLoop
                    //we determine the center of the last basepair in the helix
                    this.midX = (this.getBase5().getPreviousResidue().getX() + this.getBase3().getNextResidue().getX()) / 2;
                    this.midY = (this.getBase5().getPreviousResidue().getY() + this.getBase3().getNextResidue().getY()) / 2;
                    //we determine the translation beetween the two last basepairs in the helix
                    final double trX = this.getBase5().getPreviousResidue().getX() - this.getBase5().getPreviousResidue().getPreviousResidue().getX();
                    final double trY = this.getBase5().getPreviousResidue().getY() - this.getBase5().getPreviousResidue().getPreviousResidue().getY();
                    //we apply the same translation to the center of the last basepair, this is the center of the terminal loop
                    //as an option we can fit the center proportionnal to the residue number inside the Loop (here this.moleculesCount()/4)
                    setCenterPoint(midX + trX * this.getLength() / 4, midY + trY * this.getLength() / 4);
                    //we determine the angles made with the centerPoint and the bases of the last bases in the helix
                    final double angle = DrawingUtils.getAngle(DrawingUtils.getDistance(centerPoint.getX(), centerPoint.getY(), midX, midY),
                            DrawingUtils.getDistance(midX, midY, this.getBase5().getPreviousResidue().getX(), this.getBase5().getPreviousResidue().getY()));
                    //we determine the angle beetween two bases in the loop
                    //we determine the orientation of the helix with the crossProduct
                    final double crossProduct = DrawingUtils.crossProduct(this.getBase5().getPreviousResidue().getRealCoordinates(), centerPoint, new Point2D.Double(this.midX, this.midY));
                    //calcul of the rotation angle
                    if (crossProduct <= 0.0)
                        rotationAngle = (Math.PI + 2 * angle) / (this.getLength() + 1);
                    else
                        rotationAngle = -(Math.PI + 2 * angle) / (this.getLength() + 1);
                    Residue base5 = this.getBase5();
                    int j = 0;
                    while (base5 != null && base5.getAbsolutePosition() <= this.location.getEnd()) {
                        j++;
                        base5.setRealCoordinates(setRotatedCoordinates(j * rotationAngle));
                        base5 = base5.getNextResidue();
                    }
                }
                else  {//regular Sstrand
                    final Residue base5 = this.getBase5().getPreviousResidue();
                    final Residue base3 = this.getBase3().getNextResidue();
                    final double[] basePosition = new double[2];

                    basePosition[0] = base5.getX();
                    basePosition[1] = base5.getY();

                    //nombre de bases dans le Sstrand2D
                    final int basesNb = this.getLength();

                    //on calcule l'ecartement horizontal et vertical des bases aux extremites
                    final double horizontal = base5.getX() - base3.getX();
                    final double vertical = base5.getY() - base3.getY();

                    //calcul de la distance entre les deux bases des helices
                    final double boundariesDistance = Math.sqrt(horizontal * horizontal + vertical * vertical);
                    //placement du centre du cercle en fonction de la distance entre les bases boundaries
                    this.setCenterPoint(boundariesDistance, horizontal, vertical);
                    for (int i = this.location.getStart(), j = 1; i <= this.location.getEnd(); i++, j++)
                        this.ss.getResidue(i).setRealCoordinates(setRotatedCoordinates((j) * this.rotationAngle));
                }
            }
        }
    }
}
