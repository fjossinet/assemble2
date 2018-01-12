package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.SecondaryCanvas;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Helix extends AbstractStructuralDomain {

    private SecondaryStructure ss;
    private Location location;
    public static final Color DEFAULT_COLOR = Color.BLUE;
    private Area area;
    private List<BaseBaseInteraction> secondaryInteractions;
    private String name;
    protected Mediator mediator;

    public Helix(Mediator mediator, SecondaryStructure ss,Location location,String name) {
        this.mediator = mediator;
        this.ss = ss;
        this.location = location;
        this.secondaryInteractions = new ArrayList<BaseBaseInteraction>();
        for (int i=0; i<this.getLength();i++) {
            BaseBaseInteraction interaction = new BaseBaseInteraction(mediator, this.ss, new Location(new Location(this.location.getStart()+i),new Location(this.location.getEnd()-i)));
            this.secondaryInteractions.add(interaction);
            interaction.getResidue().setSecondaryInteraction(interaction);
            interaction.getPartnerResidue().setSecondaryInteraction(interaction);
        }
        for (Residue r:this.getResidues())
            r.setStructuralDomain(this);
        this.name = name;
    }

    public SecondaryStructure getSecondaryStructure() {
        return ss;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean contains(final double x, final double y,GraphicContext gc) {
        for (Residue r:this.getResidues())
            if (r.contains(x,y,gc))
                return true;
        return false;
    }

    public List<BaseBaseInteraction> getSecondaryInteractions() {
        return secondaryInteractions;
    }

    public Residue getPairedResidue(Residue r) {
        for (BaseBaseInteraction interaction:this.secondaryInteractions) {
            if (interaction.getLocation().getStart() == r.getAbsolutePosition())
                return this.ss.getResidue(interaction.getLocation().getEnd());
            else if (interaction.getLocation().getEnd() == r.getAbsolutePosition())
                return this.ss.getResidue(interaction.getLocation().getStart());
        }
        return null;
    }

    public Residue[] get5PrimeEnds() {
        Residue[] residues = new Residue[2];
        residues[0] = this.ss.getResidue(this.location.getStart());
        residues[1] = this.ss.getResidue(this.location.getEnd()-this.getLength()+1);
        return residues;
    }

    public Residue get5PrimeEnd(Residue base3) {
        if (base3.getAbsolutePosition() == this.location.getEnd())
            return this.ss.getResidue(this.location.getEnd()-this.getLength()+1);
        else
            return this.ss.getResidue(this.location.getStart());
    }

    public Residue[] get3PrimeEnds() {
        Residue[] residues = new Residue[2];
        residues[0] = this.ss.getResidue(this.location.getStart()+this.getLength()-1);
        residues[1] = this.ss.getResidue(this.location.getEnd());
        return residues;
    }

    public Residue get3PrimeEnd(Residue base5) {
        if (base5.getAbsolutePosition() == this.location.getStart())
            return this.ss.getResidue(this.location.getStart()+this.getLength()-1);
        else
            return this.ss.getResidue(this.location.getEnd());
    }

    public void setCoordinates(GraphicContext gc) {
        //if no more than two bases, nothing to do
        if (this.getLength() > 2) {
            Residue base5 = this.get5PrimeEnds()[0], base3 = this.get3PrimeEnd(base5);
            base5.isUpdated(true); //to recompute its secondary interaction
            base3.isUpdated(true); //to recompute its secondary interaction
            double length = DrawingUtils.getDistance(base5.getRealCoordinates(), base3.getRealCoordinates());
            Residue base5_0 = base5.getNextResidue();
            int i = 1;
            while (base5_0.getAbsolutePosition() < base3.getAbsolutePosition()) {
                base5_0.setRealCoordinates(DrawingUtils.fit(base5.getRealCoordinates(), base3.getRealCoordinates(), length/(this.getLength()-1) * i)[0]);
                base5_0 = base5_0.getNextResidue();
                i++;
            }
            base5 = this.getPairedResidue(base3);
            base5.isUpdated(true); //to recompute its secondary interaction
            base3 = this.get3PrimeEnd(base5);
            base3.isUpdated(true); //to recompute its secondary interaction
            base5_0 = base5.getNextResidue();
            i = 1;
            while (base5_0.getAbsolutePosition() < base3.getAbsolutePosition()) {
                base5_0.setRealCoordinates(DrawingUtils.fit(base5.getRealCoordinates(), base3.getRealCoordinates(), length/(this.getLength()-1) * i)[0]);
                base5_0 = base5_0.getNextResidue();
                i++;
            }
        }

        for (Residue r:this.get5PrimeEnds())
            if (r.getPreviousResidue() != null && SingleStrand.class.isInstance(r.getPreviousResidue().getStructuralDomain()))
                r.getPreviousResidue().getStructuralDomain().setCoordinates(gc);
        for (Residue r:this.get3PrimeEnds())
            if (r.getNextResidue() != null && SingleStrand.class.isInstance(r.getNextResidue().getStructuralDomain()))
                r.getNextResidue().getStructuralDomain().setCoordinates(gc);
    }

    public void flipEnds(final Point2D center, GraphicContext gc) {
        final Residue[] bases5 = this.get5PrimeEnds();
        final Residue[] bases3 = this.get3PrimeEnds();
        bases5[0].flip(center,gc);
        bases5[1].flip(center,gc);
        bases3[0].flip(center,gc);
        bases3[1].flip(center,gc);
        this.setCoordinates(gc);
    }

    public void translateEnds(final double dragX, final double dragY, GraphicContext gc) {
        final Residue[] bases5 = this.get5PrimeEnds();
        final Residue[] bases3 = this.get3PrimeEnds();
        bases5[0].translate(dragX, dragY,gc);
        bases3[0].translate(dragX, dragY,gc);
        bases5[1].translate(dragX, dragY,gc);
        bases3[1].translate(dragX, dragY,gc);
        this.setCoordinates(gc);
    }

    public void rotateEnds(final java.awt.geom.Point2D centerPoint, final double angle, GraphicContext gc) {
        final Residue[] bases5 = this.get5PrimeEnds();
        final Residue[] bases3 = this.get3PrimeEnds();
        bases5[0].rotate(centerPoint, angle,gc);
        bases3[0].rotate(centerPoint, angle,gc);
        bases5[1].rotate(centerPoint, angle,gc);
        bases3[1].rotate(centerPoint, angle,gc);
        this.setCoordinates(gc);
    }

    public void setEnds(Residue r1, Point2D p1, Residue r2, Point2D p2, Residue r3, Point2D p3, Residue r4, Point2D p4,GraphicContext gc) {
        r1.setRealCoordinates(p1);
        r2.setRealCoordinates(p2);
        r3.setRealCoordinates(p3);
        r4.setRealCoordinates(p4);
        this.setCoordinates(gc);
    }

    public int getLength() {
        return this.location.getLength()/2;
    }

    public List<Residue> getResidues() {
        List<Residue> residues = new ArrayList<Residue>(this.getLength());
        for (Residue r : ss.getResidues())
            if (location.hasPosition(r.getAbsolutePosition()))
                residues.add(r);
        return residues;
    }

    public boolean isSelected() {
        for (Residue r:this.getResidues())
            if (!r.isSelected())
                return false;
        return true;
    }

    public boolean isInsideDrawingArea(GraphicContext gc) {
        for (Residue r:this.getResidues())
            if (r.isInsideDrawingArea(gc))
                return true;
        return false;
    }

    public void draw(final java.awt.Graphics2D g2, GraphicContext gc) {
        if (this.isInsideDrawingArea(gc) || gc.isDrawInPNG()) {
            if (this.customColor != null)
                g2.setColor(this.customColor);
            else
                g2.setColor(Color.GRAY);
            for (BaseBaseInteraction inter:this.secondaryInteractions)
                inter.draw(g2,gc);
            for (Residue r:this.getResidues())
                r.draw(g2,gc);
            if (gc.isLabelsDisplayed()) {
                g2.setColor(Color.GRAY);
                //########## draw helix label ###################
                int middle = (this.getLength()+1)/2;
                //if the residues choosen can display their residues id (to avoid to overlap with the Helix label)
                Residue  _5PrimeEnd = this.get5PrimeEnds()[0],
                        _3PrimeEnd = this.get3PrimeEnd(_5PrimeEnd) ;
                if ((_5PrimeEnd.getAbsolutePosition()+middle-1)% SecondaryCanvas.NUMBERING_FREQUENCY == 0 && (_3PrimeEnd.getAbsolutePosition()-middle+1)% SecondaryCanvas.NUMBERING_FREQUENCY == 0) {
                    //if length enough long, we try to move more the Helix label
                    if (this.getLength()>3)
                        middle+=2;
                    else
                        middle++;
                }
                Residue r1 = this.ss.getResidue(_5PrimeEnd.getAbsolutePosition() + middle - 1),
                        r2 = this.ss.getResidue(_3PrimeEnd.getAbsolutePosition() - middle + 1);
                FontMetrics fm = g2.getFontMetrics(g2.getFont());
                Point2D[] points = DrawingUtils.fit(r1.getCurrentCenter(gc), r2.getCurrentCenter(gc), -(gc.getCurrentHalf() + fm.stringWidth(this.getLabel()) / 2));
                /*
                if (r1.getAbsolutePosition()%5 == 0 )
                    DrawingUtils.drawString(points[1],this.getLabel(),g2);
                else
                    DrawingUtils.drawString(points[0],this.getLabel(),g2);*/
                //########## draw residues'ids ###################
                Residue _5_0=this.get5PrimeEnds()[0],
                        _3_1=this.get3PrimeEnds()[1];
                int i=1;
                while(i<=this.getLength()) {
                    if (gc.isDisplayPositions() && _5_0.getGenomicPosition()% SecondaryCanvas.NUMBERING_FREQUENCY == 0 && (_5_0.isInsideDrawingArea(gc) || gc.isDrawInPNG())) {
                        String id = ""+_5_0.getGenomicPosition();
                        g2.setFont(gc.getFont().deriveFont(gc.getFont().getSize2D() / 1.5f));
                        fm = g2.getFontMetrics(g2.getFont());
                        Point2D[] lines = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(gc.getCurrentHalf())),
                                _lines = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(2*gc.getCurrentHalf()));
                        g2.drawLine((int)lines[0].getX(),(int)lines[0].getY(), (int)_lines[0].getX(), (int)_lines[0].getY());
                        points = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(2*gc.getCurrentHalf()+fm.stringWidth(id)/2));
                        DrawingUtils.drawString(points[0],id,g2);
                        g2.setFont(gc.getFont());
                    }
                    if (gc.isDisplayPositions() && _3_1.getGenomicPosition()%SecondaryCanvas.NUMBERING_FREQUENCY == 0 && (_3_1.isInsideDrawingArea(gc)|| gc.isDrawInPNG())) {
                        String id = ""+_3_1.getGenomicPosition();
                        g2.setFont(gc.getFont().deriveFont(gc.getFont().getSize2D() / 1.5f));
                        fm = g2.getFontMetrics(g2.getFont());
                        Point2D[] lines = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(gc.getCurrentHalf())),
                                _lines = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(2*gc.getCurrentHalf()));
                        g2.drawLine((int)lines[1].getX(),(int)lines[1].getY(), (int)_lines[1].getX(), (int)_lines[1].getY());
                        points = DrawingUtils.fit(_5_0.getCurrentCenter(gc),_3_1.getCurrentCenter(gc),-(2*gc.getCurrentHalf()+fm.stringWidth(id)/2));
                        DrawingUtils.drawString(points[1],id, g2);
                        g2.setFont(gc.getFont());
                    }
                    _5_0 = _5_0.getNextResidue();
                    _3_1 = _3_1.getPreviousResidue();
                    i++;
                }
            }
        }
        /*else
            //if the helix is not displayed, we draw at least the phosphodiesterbonds at its 3' ends
            for (Residue r:this.get3PrimeEnds()) {
                if (r.getPhosphodiesterBond2D()!= null)
                    if (this.isDisplayed() && this.isInsideDrawingArea() || r.getNextResidue().isDisplayed() && r.getNextResidue().isInsideDrawingArea())
                        r.getPhosphodiesterBond2D().draw(g2);
            }*/
        //instead to calculate the area each time the getArea method is called, this area is calculated and stored now. This improve the time speed of the AutomaticLayout (see SecondaryCanvas class). This layout uses the helices area to reorganize automatically the helices.
        Polygon p = new Polygon();
        final Residue[] bases5 = this.get5PrimeEnds();
        final Residue[] bases3 = this.get3PrimeEnds();
        p.addPoint((int) bases5[0].getCurrentCenterX(gc), (int) bases5[0].getCurrentCenterY(gc));
        p.addPoint((int) bases3[1].getCurrentCenterX(gc), (int) bases3[1].getCurrentCenterY(gc));
        p.addPoint((int) bases5[1].getCurrentCenterX(gc), (int) bases5[1].getCurrentCenterY(gc));
        p.addPoint((int) bases3[0].getCurrentCenterX(gc), (int) bases3[0].getCurrentCenterY(gc));
        this.area = new Area(p);
    }

    public String getLabel() {
        return this.name;
    }

    public BaseBaseInteraction addSecondaryInteraction(Location location, char orientation, char edge, char partnerEdge) {
        for (BaseBaseInteraction bbi:this.secondaryInteractions) {
            if (bbi.getResidue().getAbsolutePosition() == location.getStart() && bbi.getPartnerResidue().getAbsolutePosition() == location.getEnd()) {
                this.secondaryInteractions.remove(bbi);
                break;
            }
        }
        BaseBaseInteraction newBBi = null;
        if (edge == '!' && partnerEdge == '!')
            newBBi = new SingleHBond(mediator, this.ss,location);
        else
            newBBi = new BaseBaseInteraction(mediator, this.ss,location,orientation,edge,partnerEdge);
        newBBi.getResidue().setSecondaryInteraction(newBBi);
        newBBi.getPartnerResidue().setSecondaryInteraction(newBBi);
        newBBi.getResidue().setStructuralDomain(this);
        newBBi.getPartnerResidue().setStructuralDomain(this);
        this.secondaryInteractions.add(newBBi);
        this.location = this.location.unionOf(location);
        return newBBi;
    }
}
