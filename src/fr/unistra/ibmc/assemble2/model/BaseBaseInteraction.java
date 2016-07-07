package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BaseBaseInteraction {

    public static final char ORIENTATION_TRANS = 'T',ORIENTATION_CIS = 'C', UNDEFINED = '?';

    protected SecondaryStructure ss;
    protected Location location;
    protected java.awt.geom.Point2D[] op = null, real_op = null;
    protected boolean selected;
    protected java.util.List<Shape> shapes;
    protected char orientation, edge, partnerEdge;
    protected ButtonEdge buttonEdge;
    protected ButtonPartnerEdge buttonPartnerEdge;
    protected Color customColor;
    protected Mediator mediator;

    public static Map<String, String> isostericFamiliesInstances;

    static {
        isostericFamiliesInstances = new HashMap<String, String>();

        isostericFamiliesInstances.put("AC()A", "C()-I4");
        isostericFamiliesInstances.put("AC()C", "C()-I2");
        isostericFamiliesInstances.put("AC()G", "C()-I3");
        isostericFamiliesInstances.put("AC()U", "C()-I1");
        isostericFamiliesInstances.put("UC()A", "C()-I1");
        isostericFamiliesInstances.put("CC()C", "C()-I6");
        isostericFamiliesInstances.put("GC()C", "C()-I1");
        isostericFamiliesInstances.put("CC()G", "C()-I1");
        isostericFamiliesInstances.put("CC()U", "C()-I5");
        isostericFamiliesInstances.put("GC()U", "C()-I1");
        isostericFamiliesInstances.put("UC()G", "C()-I1");
        isostericFamiliesInstances.put("UC()U", "C()-I6");

        isostericFamiliesInstances.put("AT()A", "T()-I4");
        isostericFamiliesInstances.put("AT()C", "T()-I3");
        isostericFamiliesInstances.put("AT()U", "T()-I1");
        isostericFamiliesInstances.put("CT()C", "T()-I6");
        isostericFamiliesInstances.put("CT()G", "T()-I2");
        isostericFamiliesInstances.put("CT()U", "T()-I5");
        isostericFamiliesInstances.put("GT()G", "T()-I4");
        isostericFamiliesInstances.put("GT()U", "T()-I3");
        isostericFamiliesInstances.put("UT()U", "T()-I6");

        isostericFamiliesInstances.put("AC(]G", "C(]-I3");
        isostericFamiliesInstances.put("AC(]U", "C(]-I3");
        isostericFamiliesInstances.put("CC(]C", "C(]-I2");
        isostericFamiliesInstances.put("CC(]G", "C(]-I1");
        isostericFamiliesInstances.put("CC(]U", "C(]-I1");
        isostericFamiliesInstances.put("GC(]A", "C(]-I3");
        isostericFamiliesInstances.put("GC(]G", "C(]-I4");
        isostericFamiliesInstances.put("UC(]A", "C(]-I1");
        isostericFamiliesInstances.put("UC(]G", "C(]-I1");
        isostericFamiliesInstances.put("UC(]U", "C(]-I2");

        isostericFamiliesInstances.put("AT(]A", "T(]-I4");
        isostericFamiliesInstances.put("AT(]G", "T(]-I4");
        isostericFamiliesInstances.put("CT(]A", "T(]-I2");
        isostericFamiliesInstances.put("CT(]C", "T(]-I1");
        isostericFamiliesInstances.put("CT(]G", "T(]-I2");
        isostericFamiliesInstances.put("GT(]G", "T(]-I5");
        isostericFamiliesInstances.put("GT(]U", "T(]-I4");
        isostericFamiliesInstances.put("UT(]A", "T(]-I1");
        isostericFamiliesInstances.put("UT(]G", "T(]-I3");
        isostericFamiliesInstances.put("UT(]U", "T(]-I2");

        isostericFamiliesInstances.put("AC(}A", "C(}-I1");
        isostericFamiliesInstances.put("AC(}C", "C(}-I1");
        isostericFamiliesInstances.put("AC(}G", "C(}-I1");
        isostericFamiliesInstances.put("AC(}U", "C(}-I1");
        isostericFamiliesInstances.put("CC(}A", "C(}-I2");
        isostericFamiliesInstances.put("CC(}C", "C(}-I2");
        isostericFamiliesInstances.put("CC(}G", "C(}-I2");
        isostericFamiliesInstances.put("CC(}U", "C(}-I2");
        isostericFamiliesInstances.put("GC(}A", "C(}-I3");
        isostericFamiliesInstances.put("GC(}C", "C(}-I3");
        isostericFamiliesInstances.put("GC(}G", "C(}-I5");
        isostericFamiliesInstances.put("GC(}U", "C(}-I3");
        isostericFamiliesInstances.put("UC(}A", "C(}-I4");
        isostericFamiliesInstances.put("UC(}C", "C(}-I4");
        isostericFamiliesInstances.put("UC(}G", "C(}-I4");
        isostericFamiliesInstances.put("UC(}U", "C(}-I4");

        isostericFamiliesInstances.put("AT(}A", "T(}-I1");
        isostericFamiliesInstances.put("AT(}C", "T(}-I1");
        isostericFamiliesInstances.put("AT(}G", "T(}-I1");
        isostericFamiliesInstances.put("AT(}U", "T(}-I1");
        isostericFamiliesInstances.put("CT(}A", "T(}-I1");
        isostericFamiliesInstances.put("CT(}C", "T(}-I1");
        isostericFamiliesInstances.put("CT(}G", "T(}-I1");
        isostericFamiliesInstances.put("CT(}U", "T(}-I1");
        isostericFamiliesInstances.put("GT(}C", "T(}-I2");
        isostericFamiliesInstances.put("GT(}U", "T(}-I2");
        isostericFamiliesInstances.put("UT(}A", "T(}-I3");
        isostericFamiliesInstances.put("UT(}C", "T(}-I3");
        isostericFamiliesInstances.put("UT(}G", "T(}-I4");
        isostericFamiliesInstances.put("UT(}U", "T(}-I3");

        isostericFamiliesInstances.put("AC[]G", "C[]-I2");
        isostericFamiliesInstances.put("CC[]G", "C[]-I1");
        isostericFamiliesInstances.put("GC[]G", "C[]-I1");

        isostericFamiliesInstances.put("AT[]A", "T[]-I1");
        isostericFamiliesInstances.put("AT[]C", "T[]-I1");
        isostericFamiliesInstances.put("AT[]G", "T[]-I2");
        isostericFamiliesInstances.put("AT[]U", "T[]-I2");
        isostericFamiliesInstances.put("CT[]G", "T[]-I1");
        isostericFamiliesInstances.put("CT[]U", "T[]-I2");
        isostericFamiliesInstances.put("GT[]G", "T[]-I3");
        isostericFamiliesInstances.put("UT[]C", "T[]-I2");

        isostericFamiliesInstances.put("AC[}A", "C[}-I1");
        isostericFamiliesInstances.put("AC[}C", "C[}-I1");
        isostericFamiliesInstances.put("AC[}G", "C[}-I1");
        isostericFamiliesInstances.put("AC[}U", "C[}-I1");
        isostericFamiliesInstances.put("CC[}A", "C[}-I1");
        isostericFamiliesInstances.put("CC[}C", "C[}-I1");
        isostericFamiliesInstances.put("CC[}G", "C[}-I1");
        isostericFamiliesInstances.put("CC[}U", "C[}-I1");
        isostericFamiliesInstances.put("GC[}A", "C[}-I1");
        isostericFamiliesInstances.put("GC[}G", "C[}-I1");
        isostericFamiliesInstances.put("UC[}A", "C[}-I2");
        isostericFamiliesInstances.put("UC[}C", "C[}-I1");
        isostericFamiliesInstances.put("UC[}G", "C[}-I1");
        isostericFamiliesInstances.put("UC[}U", "C[}-I1");

        isostericFamiliesInstances.put("AT[}A", "T[}-I1");
        isostericFamiliesInstances.put("AT[}C", "T[}-I1");
        isostericFamiliesInstances.put("AT[}G", "T[}-I1");
        isostericFamiliesInstances.put("AT[}U", "T[}-I1");
        isostericFamiliesInstances.put("CT[}A", "T[}-I1");
        isostericFamiliesInstances.put("CT[}C", "T[}-I1");
        isostericFamiliesInstances.put("CT[}U", "T[}-I1");
        isostericFamiliesInstances.put("GT[}G", "T[}-I2");
        isostericFamiliesInstances.put("UT[}A", "T[}-I2");
        isostericFamiliesInstances.put("UT[}G", "T[}-I2");

        isostericFamiliesInstances.put("AC{}A", "C{}-I1");
        isostericFamiliesInstances.put("AC{}C", "C{}-I1");
        isostericFamiliesInstances.put("AC{}G", "C{}-I1");
        isostericFamiliesInstances.put("AC{}U", "C{}-I1");
        isostericFamiliesInstances.put("CC{}C", "C{}-I1");
        isostericFamiliesInstances.put("CC{}G", "C{}-I1");
        isostericFamiliesInstances.put("CC{}U", "C{}-I1");
        isostericFamiliesInstances.put("GC{}G", "C{}-I1");
        isostericFamiliesInstances.put("GC{}U", "C{}-I1");
        isostericFamiliesInstances.put("UC{}U", "C{}-I1");

        isostericFamiliesInstances.put("AT{}A", "T{}-I1");
        isostericFamiliesInstances.put("AT{}C", "T{}-I1");
        isostericFamiliesInstances.put("AT{}G", "T{}-I1");
        isostericFamiliesInstances.put("AT{}U", "T{}-I1");
        isostericFamiliesInstances.put("GT{}A", "T{}-I2");
        isostericFamiliesInstances.put("GT{}C", "T{}-I2");
        isostericFamiliesInstances.put("GT{}G", "T{}-I2");
        isostericFamiliesInstances.put("GT{}U", "T{}-I2");

        isostericFamiliesInstances.put("AC{}A", "C{}-I1");
        isostericFamiliesInstances.put("AC{}C", "C{}-I1");
        isostericFamiliesInstances.put("AC{}G", "C{}-I1");
        isostericFamiliesInstances.put("AC{}U", "C{}-I1");
        isostericFamiliesInstances.put("CC{}A", "C{}-I1");
        isostericFamiliesInstances.put("CC{}C", "C{}-I1");
        isostericFamiliesInstances.put("CC{}G", "C{}-I1");
        isostericFamiliesInstances.put("CC{}U", "C{}-I1");
        isostericFamiliesInstances.put("GC{}A", "C{}-I1");
        isostericFamiliesInstances.put("GC{}C", "C{}-I1");
        isostericFamiliesInstances.put("GC{}G", "C{}-I1");
        isostericFamiliesInstances.put("GC{}U", "C{}-I1");
        isostericFamiliesInstances.put("UC{}A", "C{}-I1");
        isostericFamiliesInstances.put("UC{}C", "C{}-I1");
        isostericFamiliesInstances.put("UC{}G", "C{}-I1");
        isostericFamiliesInstances.put("UC{}U", "C{}-I1");

        isostericFamiliesInstances.put("AT{}A", "T{}-I1");
        isostericFamiliesInstances.put("AT{}C", "T{}-I1");
        isostericFamiliesInstances.put("AT{}G", "T{}-I1");
        isostericFamiliesInstances.put("AT{}U", "T{}-I1");
        isostericFamiliesInstances.put("GT{}A", "T{}-I2");
        isostericFamiliesInstances.put("GT{}C", "T{}-I2");
        isostericFamiliesInstances.put("GT{}G", "T{}-I2");
        isostericFamiliesInstances.put("GT{}U", "T{}-I2");

        isostericFamiliesInstances.put("AC{}A", "C{}-I1");
        isostericFamiliesInstances.put("AC{}C", "C{}-I1");
        isostericFamiliesInstances.put("AC{}G", "C{}-I1");
        isostericFamiliesInstances.put("AC{}U", "C{}-I1");
        isostericFamiliesInstances.put("CC{}A", "C{}-I1");
        isostericFamiliesInstances.put("CC{}C", "C{}-I1");
        isostericFamiliesInstances.put("CC{}G", "C{}-I1");
        isostericFamiliesInstances.put("CC{}U", "C{}-I1");
        isostericFamiliesInstances.put("GC{}A", "C{}-I1");
        isostericFamiliesInstances.put("GC{}C", "C{}-I1");
        isostericFamiliesInstances.put("GC{}G", "C{}-I1");
        isostericFamiliesInstances.put("GC{}U", "C{}-I1");
        isostericFamiliesInstances.put("UC{}A", "C{}-I1");
        isostericFamiliesInstances.put("UC{}C", "C{}-I1");
        isostericFamiliesInstances.put("UC{}G", "C{}-I1");
        isostericFamiliesInstances.put("UC{}U", "C{}-I1");

        isostericFamiliesInstances.put("AT{}A", "T{}-I1");
        isostericFamiliesInstances.put("AT{}C", "T{}-I1");
        isostericFamiliesInstances.put("AT{}G", "T{}-I1");
        isostericFamiliesInstances.put("AT{}U", "T{}-I1");
        isostericFamiliesInstances.put("GT{}A", "T{}-I2");
        isostericFamiliesInstances.put("GT{}C", "T{}-I2");
        isostericFamiliesInstances.put("GT{}G", "T{}-I2");
        isostericFamiliesInstances.put("GT{}U", "T{}-I2");
    }

    public BaseBaseInteraction(Mediator mediator, SecondaryStructure ss,Location location) {
        this.mediator = mediator;
        this.ss = ss;
        this.location = location;
        this.shapes = new ArrayList<Shape>();
        this.orientation = ORIENTATION_CIS;
        this.edge ='(';
        this.partnerEdge = ')';
    }

    public ButtonEdge getButtonEdge() {
        return buttonEdge;
    }

    public ButtonPartnerEdge getButtonPartnerEdge() {
        return buttonPartnerEdge;
    }

    public boolean isCanonical() {
        return this.orientation == ORIENTATION_CIS && this.edge == '(' && this.partnerEdge == ')' && (
                this.getResidue().getSymbol() == 'A' && this.getPartnerResidue().getSymbol() == 'U' ||
                        this.getResidue().getSymbol() == 'U' && this.getPartnerResidue().getSymbol() == 'A' ||
                        this.getResidue().getSymbol() == 'G' && this.getPartnerResidue().getSymbol() == 'C' ||
                        this.getResidue().getSymbol() == 'C' && this.getPartnerResidue().getSymbol() == 'G' ||
                        this.getResidue().getSymbol() == 'G' && this.getPartnerResidue().getSymbol() == 'U' ||
                        this.getResidue().getSymbol() == 'U' && this.getPartnerResidue().getSymbol() == 'G');
    }

    public static boolean isCanonical(char orientation, char edge, char residue, char partnerEdge, char partnerResidue) {
        return orientation == ORIENTATION_CIS && edge == '(' && partnerEdge == ')' && (
                residue == 'A' && partnerResidue == 'U' ||
                        residue == 'U' && partnerResidue == 'A' ||
                        residue == 'G' && partnerResidue == 'C' ||
                        residue == 'C' && partnerResidue == 'G' ||
                        residue == 'G' && partnerResidue == 'U' ||
                        residue == 'U' && partnerResidue == 'G');
    }

    /**
     * @param b
     * @param baseId
     * @param pb
     * @param partnerBaseId
     * @return 0 if the description of the base-pair given as argument has never been observed for the family of the current object
     *         1 if the description of the base-pair given as argument is a member of the geometric family of the current object
     *         2 if the description of the base-pair given as argument is a member of the isosteric family of the current object
     */
    public int isIsosteric(char b, int baseId, char pb, int partnerBaseId) {
        if (this.getClass() == SingleHBond.class )
            return 2; // no isoteric rule for single-h bonds. So isosteric by default
        char base = (baseId == this.getResidue().getAbsolutePosition()) ? b : pb,
                partnerBase = (partnerBaseId == this.getPartnerResidue().getAbsolutePosition()) ? pb : b;
        String iso1 = isostericFamiliesInstances.get(new StringBuffer().append(base).append(this.getOrientation()).append(this.getEdge(this.getResidue())).append(this.getEdge(this.getPartnerResidue())).append(partnerBase).toString()),
                iso2 = isostericFamiliesInstances.get(new StringBuffer().append(this.getResidue().getSymbol()).append(this.getOrientation()).append(this.getEdge(this.getResidue())).append(this.getEdge(this.getPartnerResidue())).append(this.getPartnerResidue().getSymbol()).toString());
        if (iso2 == null)
            //[fjossinet] try the opposite because the isostericFamiliesInstances table store only one possibility (only ACWHG for ACWHG and GCHWA)
            iso2 = isostericFamiliesInstances.get(new StringBuffer().append(this.getPartnerResidue().getSymbol()).append(this.getOrientation()).append(this.getEdge(this.getPartnerResidue())).append(this.getEdge(this.getResidue())).append(this.getResidue().getSymbol()).toString());
        if (iso1 == null)
            //[fjossinet] try the opposite because the isostericFamiliesInstances table store only one possibility (only ACWHG for ACWHG and GCHWA)
            iso1 = isostericFamiliesInstances.get(new StringBuffer().append(partnerBase).append(this.getOrientation()).append(this.getEdge(this.getPartnerResidue())).append(this.getEdge(this.getResidue())).append(base).toString());
        if (iso2 == null) {//if still null
            //not possible according to the leontis-westhof publication
            return 0;
        }
        if (iso1 == null) //if still null
            //this combination has never been observed
            return 0;
        else if (iso1.equals(iso2)) //isosteric
            return 2;
        else if (iso1.split("-")[0].equals(iso2.split("-")[0])) //same geometric family
            return 1;
        else //different
            return 0;
    }

    public BaseBaseInteraction(Mediator mediator, SecondaryStructure ss, Location location, char orientation, char edge, char partnerEdge) {
        this(mediator, ss,location);
        this.orientation = orientation;
        this.edge = edge;
        this.partnerEdge = partnerEdge;
    }

    public char getOrientation() {
        return orientation;
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }

    public char getEdge(Residue r) {
        if (r.equals(this.getPartnerResidue()))
            return this.partnerEdge;
        else if (r.equals(this.getResidue()))
            return this.edge;
        return '?';
    }

    public java.util.List<Residue> getResidues() {
        java.util.List<Residue> residues = new ArrayList<Residue>(2);
        for (Residue r : ss.getResidues())
            if (location.hasPosition(r.getAbsolutePosition()))
                residues.add(r);
        return residues;
    }

    public java.util.List<Shape> getShapes() {
        return shapes;
    }


    public boolean isUpdated() {
        return this.getResidue().isUpdated() || this.getPartnerResidue().isUpdated();
    }

    public Location getLocation() {
        return location;
    }

    public boolean isSecondaryInteraction() {
        return !this.ss.getTertiaryInteractions().contains(this);
    }

    public boolean isInsideDrawingArea(GraphicContext gc){
        return this.getResidue().isInsideDrawingArea(gc) || this.getPartnerResidue().isInsideDrawingArea(gc);
    }

    public void draw(Graphics2D g2,GraphicContext gc) {
        if (!this.isSelected() &&  gc.isDrawOnlySelectedMotif())
            return;
        if (this.getResidue() != null && this.getPartnerResidue() != null)
            if (this.isSecondaryInteraction() || gc.isTertiaryInteractionsDisplayed()) {
                if (this.isInsideDrawingArea(gc) || gc.isDrawInPNG()) {
                    if (this.isUpdated() || this.real_op == null)
                        this.setOutsidePoints(gc);
                    if (this.real_op != null) {
                        //calculate the outside points fitted with the current view and zoom level
                        this.op = new java.awt.geom.Point2D[2];
                        this.op[0] = new java.awt.geom.Point2D.Double(this.real_op[0]
                                .getX()
                                * gc.getFinalZoomLevel() + gc.getViewX(),
                                this.real_op[0].getY() * gc.getFinalZoomLevel()
                                        + gc.getViewY());
                        this.op[1] = new java.awt.geom.Point2D.Double(this.real_op[1]
                                .getX()
                                * gc.getFinalZoomLevel() + gc.getViewX(),
                                this.real_op[1].getY() * gc.getFinalZoomLevel()
                                        + gc.getViewY());
                    }
                    //we remove the precedent op points if no real_op
                    else
                        this.op = null;
                    g2.setColor(this.getFinalColor());
                    Rectangle2D globalArea = this.setShapes(gc);
                    for (Shape s:this.shapes)
                        s.draw(g2,this.orientation);
                    if (gc.isEditStructure() && this.isSelected()) {
                        Point2D[] point2Ds = DrawingUtils.fit(this.getResidue().getCurrentCenterX(gc), this.getResidue().getCurrentCenterY(gc), this.getPartnerResidue().getCurrentCenterX(gc), this.getPartnerResidue().getCurrentCenterY(gc), -gc.getCurrentHalf());
                        Point2D[] point2Ds_2 = DrawingUtils.fit(this.getResidue().getCurrentCenterX(gc), this.getResidue().getCurrentCenterY(gc), this.getPartnerResidue().getCurrentCenterX(gc), this.getPartnerResidue().getCurrentCenterY(gc), -2*gc.getCurrentHalf());
                        Point2D[] point2Ds_3 = DrawingUtils.fit(this.getResidue().getCurrentCenterX(gc), this.getResidue().getCurrentCenterY(gc), this.getPartnerResidue().getCurrentCenterX(gc), this.getPartnerResidue().getCurrentCenterY(gc), -3*gc.getCurrentHalf());
                        if (!this.isSecondaryInteraction())
                            gc.getCanvas().addButton(new ButtonDeleteTertiaryInteraction(mediator, this.ss,this,
                                    new java.awt.geom.Point2D.Double(point2Ds_3[0].getX(),point2Ds_3[0].getY()),
                                    new java.awt.geom.Point2D.Double((point2Ds_3[0].getX()+point2Ds_2[0].getX())/2,(point2Ds_3[0].getY()+point2Ds_2[0].getY())/2),
                                    gc));
                        if (this.isSecondaryInteraction() && this.getResidue().getStructuralDomain() != gc.getCanvas().getSelectedHelix() || !this.isSecondaryInteraction()) {
                            gc.getCanvas().addButton(new ButtonEdge(mediator, this.ss,this,
                                    new java.awt.geom.Point2D.Double(point2Ds_2[0].getX(),point2Ds_2[0].getY()),
                                    new java.awt.geom.Point2D.Double((point2Ds_2[0].getX()+point2Ds[0].getX())/2,(point2Ds_2[0].getY()+point2Ds[0].getY())/2),
                                    gc));
                            gc.getCanvas().addButton(new ButtonPartnerEdge(mediator, this.ss,this,
                                    new java.awt.geom.Point2D.Double(point2Ds[1].getX(),point2Ds[1].getY()),
                                    new java.awt.geom.Point2D.Double((point2Ds_2[1].getX()+point2Ds[1].getX())/2,(point2Ds_2[1].getY()+point2Ds[1].getY())/2),
                                    gc));
                            gc.getCanvas().addButton(new ButtonOrientation(mediator, this.ss,this,
                                    new java.awt.geom.Point2D.Double(point2Ds_2[1].getX(),point2Ds_2[1].getY()),
                                    new java.awt.geom.Point2D.Double((point2Ds_3[1].getX()+point2Ds_2[1].getX())/2,(point2Ds_3[1].getY()+point2Ds_2[1].getY())/2),
                                    gc));
                        }
                    }
                    if (this.isSelected() && gc.isDrawOnlySelectedMotif()) {
                        gc.addToSelectionArea(globalArea);
                        if (!this.isSecondaryInteraction())
                            gc.addInteractionFromMotif(this);
                    }
                }
                else
                    this.real_op = null;
            }
    }

    public void setCustomColor(Color customColor) {
        this.customColor = customColor;
    }

    public Color getCustomColor() {
        return this.customColor;
    }

    public Color getFinalColor() {
        if (this.isSecondaryInteraction()) {
            if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE)
                return mediator.getAlignmentCanvas().getConsensusColor(this.getResidue().getGenomicPosition());
            else if (this.customColor != null)
                return this.customColor;
            else if (this.getResidue().getStructuralDomain().getCustomColor() != null)
                return this.getResidue().getStructuralDomain().getCustomColor();
            else
                return Assemble.SecondaryInteraction_Color;
        }
        else {
            if (this.customColor != null)
                return this.customColor;
            else
                return Assemble.TertiaryInteraction_Color;
        }
    }

    protected Rectangle2D setShapes(GraphicContext gc) {
        Rectangle2D globalArea = null;
        if (op != null) {
            this.shapes.clear();
            //we determine the distance between the two new points
            final double distance = DrawingUtils.getDistance(op[0], op[1]);
            //the points outside the symbol
            Point2D[] ip0 = null, ip1 = null;
            ip0 = DrawingUtils.fit(op[0].getX(), op[0].getY(), op[1].getX(), op[1].getY(), (distance-gc.getCurrentSymbolSize())/2);
            if (ip0 != null) {
                if ((!this.isSecondaryInteraction() && edge == '(' && partnerEdge == ')') || (this.isSecondaryInteraction()  && !this.isCanonical() && edge == '(' && partnerEdge == ')')) {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip1[0], gc));
                    this.shapes.add(new Circle(mediator, ip1[0], new Point2D.Double((ip1[0].getX()+ip1[1].getX())/2,(ip1[0].getY()+ip1[1].getY())/2), gc));
                    this.shapes.add(new Line(mediator, ip1[1], op[1], gc));
                }
                else if (edge == '(' && partnerEdge == ']') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Circle(mediator, ip0[0], new Point2D.Double((ip0[0].getX()+ip1[0].getX())/2,(ip0[0].getY()+ip1[0].getY())/2),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Squarre(mediator, ip1[1], ip0[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));
                }
                else if (edge == '(' && partnerEdge == '}') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Circle(mediator, ip0[0], new Point2D.Double((ip0[0].getX()+ip1[0].getX())/2,(ip0[0].getY()+ip1[0].getY())/2),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Triangle(mediator, ip1[1], ip0[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));
                }
                else if (edge == '{' && partnerEdge == ')') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Triangle(mediator, ip1[0], ip0[0], this.getPartnerResidue(),this.getResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Circle(mediator, ip1[1], new Point2D.Double((ip1[1].getX()+ip0[1].getX())/2,(ip1[1].getY()+ip0[1].getY())/2), gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));
                }
                else if (edge == '[' && partnerEdge == ')') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Squarre(mediator, ip0[0], ip1[0], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Circle(mediator, ip1[1], new Point2D.Double((ip1[1].getX()+ip0[1].getX())/2,(ip1[1].getY()+ip0[1].getY())/2), gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));

                }
                else if (edge == '[' && partnerEdge == ']') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip1[0], gc));
                    this.shapes.add(new Squarre(mediator, ip1[0], ip1[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[1], op[1], gc));
                }
                else if (edge == '[' && partnerEdge == '}') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Squarre(mediator, ip0[0], ip1[0], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Triangle(mediator, ip1[1], ip0[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));
                }
                else if (edge == '{' && partnerEdge == ']') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip0[0], gc));
                    this.shapes.add(new Triangle(mediator, ip1[0], ip0[0], this.getPartnerResidue(),this.getResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[0], ip1[1], gc));
                    this.shapes.add(new Squarre(mediator, ip1[1], ip0[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip0[1], op[1], gc));
                }
                else if (edge == '{' && partnerEdge == '}') {
                    ip1 = DrawingUtils.fit(ip0[0].getX(), ip0[0].getY(), ip0[1].getX(), ip0[1].getY(), gc.getCurrentSymbolSize()/3);
                    this.shapes.add(new Line(mediator, op[0], ip1[0], gc));
                    this.shapes.add(new Triangle(mediator, ip1[0], ip1[1], this.getResidue(),this.getPartnerResidue(),gc));
                    this.shapes.add(new Line(mediator, ip1[1], op[1], gc));
                }
                else
                    this.shapes.add(new Line(mediator, op[0], op[1], gc));
            }
            //if we cannot draw the symbol (not enough space), we draw only a line
            else
                this.shapes.add(new Line(mediator, op[0], op[1], gc));
            for (Shape s: this.shapes) {
                if (globalArea == null)
                    globalArea = s.getShape().getBounds2D();
                else
                    globalArea = globalArea.createUnion(s.getShape().getBounds2D());
            }
        }
        return globalArea;
    }

    protected void setOutsidePoints(GraphicContext gc) {
        final Residue residue = this.getResidue();
        final Residue partnerResidue = this.getPartnerResidue();
        //we calculate the points outside the bases
        final double dist = DrawingUtils.getDistance(residue.getX(), residue.getY(), partnerResidue.getX(), partnerResidue.getY());
        if (dist >= gc.getRealHalf() * 2)
            this.real_op = DrawingUtils.fit(residue.getRealCenterX(gc), residue.getRealCenterY(gc), partnerResidue.getRealCenterX(gc), partnerResidue.getRealCenterY(gc), gc.getRealHalf());
        else
            this.real_op = null;
    }

    public void isSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.getResidue().isSelected() && this.getPartnerResidue().isSelected();
    }

    public Residue getResidue() {
        return this.ss.getResidue(this.location.getStart());
    }

    public Residue getPartnerResidue() {
        return this.ss.getResidue(this.location.getEnd());
    }

    public Residue getPairedResidue(Residue r) {
        if (r.equals(this.getResidue()))
            return this.getPartnerResidue();
        else if (r.equals(this.getPartnerResidue()))
            return this.getResidue();
        return null;
    }

    public boolean contains(final double x, final double y,GraphicContext gc) {
        for (Residue r:this.getResidues())
            if (r.contains(x,y,gc))
                return true;
        return false;
    }

    public void setEdge(Residue residue, char edge) {
        if (this.getResidue() == residue)
            this.edge = edge;
        else if (this.getPartnerResidue() == residue)
            this.partnerEdge = edge;
    }

    public class ShapesArray {

        public java.util.List<AbstractShape> symbols;

        public ShapesArray() {
            this.symbols = new ArrayList<AbstractShape>();
        }

        public Iterator iterator() {
            return new Iterator();
        }

        public void addShape(final AbstractShape symbol) {
            this.symbols.add(symbol);
        }

        public void clear() {
            this.symbols.clear();
        }

        public int size() {
            return this.symbols.size();
        }

        public class Iterator {

            java.util.Iterator it;

            Iterator() {
                this.it = ShapesArray.this.symbols.iterator();
            }

            boolean hasNext() {
                return this.it.hasNext();
            }

            AbstractShape next() {
                return (AbstractShape) this.it.next();
            }

        }

    }

    public java.util.List<AtomAtomInteraction> getAtomAtomInteractions() {
        java.util.List<AtomAtomInteraction> interactions = new ArrayList<AtomAtomInteraction>();
        if (this.orientation == ORIENTATION_CIS && this.edge == '(' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N1", this.location));
                            interactions.add(new CH("N1", "C2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("N1", "N6", this.location));
                            interactions.add(new CH("C2", "N1", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N1", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O6", this.location));
                            interactions.add(new HBond("N1", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O4", this.location));
                            interactions.add(new HBond("N1", "N3", this.location));
                            interactions.add(new CH("C2", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new HBond( "O2", "N3", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("N4", "N3", this.location));
                            interactions.add(new HBond("N3", "O2", this.location));
                             */
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "O6", this.location));
                            interactions.add(new HBond("N3", "N1", this.location));
                            interactions.add(new HBond("O2", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O4", this.location));
                            interactions.add(new Water("N3", "N3", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O6", "N6", this.location));
                            interactions.add(new HBond("N1", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O6", "N4", this.location));
                            interactions.add(new HBond("N1", "N3", this.location));
                            interactions.add(new HBond("N2", "O2", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new HBond("O6", "N3", this.location));
                            interactions.add(new HBond("N1", "O2", this.location));
                            interactions.add(new Water("N2", "O2'", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O4", "N6", this.location));
                            interactions.add(new HBond("N3", "N1", this.location));
                            interactions.add(new CH("O2", "C2", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O4", "N4", this.location));
                            interactions.add(new Water("N3", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N3", "O6", this.location));
                            interactions.add(new HBond("O2", "N1", this.location));
                            interactions.add(new Water("O2'", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O4", "N3", this.location));
                            interactions.add(new HBond("N3", "O2",  this.location));
                            interactions.add(new Water("O2", "O2'", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("N3", "O4", this.location));
                            interactions.add(new HBond("O2", "N3", this.location));
                             */
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '(' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N1", this.location));
                            interactions.add(new HBond("N1", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N1", "N4", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O2", this.location));
                            interactions.add(new HBond("N1", "N3", this.location));
                            interactions.add(new CH("C2", "O4", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N1", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new HBond("O2", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N3", "N2", this.location));
                            interactions.add(new HBond("O2", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new Sodium("O2", "O4", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            break;
                        case 'C':
                            interactions.add(new HBond("N1", "O2", this.location));
                            interactions.add(new HBond("N2", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O6", "N1", this.location));
                            interactions.add(new HBond("N1", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O6", "N3", this.location));
                            interactions.add(new HBond("N1", "O4", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O4", "C2", this.location));
                            interactions.add(new HBond("N3", "N1", this.location));
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new HBond("O2", "N4", this.location));
                            interactions.add(new Sodium("O4", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O4", "N1", this.location));
                            interactions.add(new HBond("N3", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O4", "N3", this.location));
                            interactions.add(new HBond("N3", "O4", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '(' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "O2", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O2", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N3", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "N3", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O6", "C2", this.location));
                            interactions.add(new HBond("N1", "N3", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N1", "O2", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O6", "N2", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            interactions.add(new Water("N1", "N3", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N1", "O2", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O4", "C2", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "O2", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O4", "N2", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N3", "O2", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '(' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new CH("N1", "C2", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "O2'", this.location));
                            interactions.add(new HBond("N6", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O2'", this.location));
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N1", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O2'", this.location));
                            interactions.add(new HBond("N6", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "O2'", this.location));
                            interactions.add(new HBond("N4", "N3", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N4", "O2'", this.location));
                            interactions.add(new HBond("N4", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "O2'", this.location));
                            interactions.add(new HBond("N4", "N3", this.location));
                            interactions.add(new HBond("N3", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O2'", this.location));
                            interactions.add(new HBond("N4", "O2", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            break;
                        case 'C':
                            interactions.add(new HBond("O6", "O2'", this.location));
                            interactions.add(new HBond("N1", "O2", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new HBond("O6", "O2'", this.location));
                            interactions.add(new HBond("N1", "O2", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new CH("O2", "C2", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O4", "O2'", this.location));
                            interactions.add(new HBond("N3", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2", "N2", this.location));
                            interactions.add(new Water("N3", "N3", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O4", "O2'", this.location));
                            interactions.add(new HBond("N3", "O2", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '(' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O6", this.location));
                            interactions.add(new HBond("N1", "N7", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O4", this.location));
                            interactions.add(new CH("N1", "C5", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new CH("O2", "C5", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "O6", this.location));
                            interactions.add(new HBond("N3", "N7", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O4", this.location));
                            interactions.add(new CH("N3", "C5", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O6", "N6", this.location));
                            interactions.add(new HBond("N1", "N7", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("N1", "O6", this.location));
                            interactions.add(new HBond("N2", "N7", this.location));
                            break;
                        case 'U':
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O4", "N6", this.location));
                            interactions.add(new HBond("N3", "N7", this.location));
                            interactions.add(new CH("O2", "C8", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("N3", "N7", this.location));
                            interactions.add(new CH("O2", "C8", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N3", "O4", this.location));
                            interactions.add(new CH("O2", "C5", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '(' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N7", this.location));
                            interactions.add(new HBond("N1", "N6", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "N7", this.location));
                            interactions.add(new HBond("N1", "O6", this.location));
                            break;
                        case 'U':
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N7", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("N3", "C5", this.location));
                            interactions.add(new HBond("O2", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "N7", this.location));
                            interactions.add(new HBond("N3", "O6", this.location));
                            break;
                        case 'U':
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            break;
                        case 'C':
                        case 'G':
                            interactions.add(new HBond("N1", "N7", this.location));
                            interactions.add(new HBond("N2", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("O6", "C5", this.location));
                            interactions.add(new HBond("N1", "O4", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N7", this.location));
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new CH("O4", "C8", this.location));
                            interactions.add(new HBond("N3", "N7", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("O4", "C5", this.location));
                            interactions.add(new HBond("N3", "O4", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '{' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new CH("C2", "O6", this.location));
                            interactions.add(new HBond("N3", "N1", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C2", "O4", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2", "N4", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2", "N1", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O2", "N3", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                        case 'G':
                            interactions.add(new HBond("N2", "O6", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            interactions.add(new Water("N3", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N2", "O4", this.location));
                            interactions.add(new HBond("N3", "N3", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2", "N4", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2", "N1", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O2", "N3", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '{' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new CH("C2", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "N4", this.location));
                            interactions.add(new HBond("N3", "N4", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new HBond("N3", "N3", this.location));
                            interactions.add(new CH("C2", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "N6", this.location));
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "N4", this.location));
                            interactions.add(new HBond("O2", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O6", this.location));
                            interactions.add(new HBond("O2", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O4", this.location));
                            interactions.add(new HBond("O2", "N3", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "N6", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("N2", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "N4", this.location));
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new HBond("N2", "N3", this.location));
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new HBond("N2", "O2", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "N6", this.location));
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "N4", this.location));
                            interactions.add(new HBond("O2", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O6", this.location));
                            interactions.add(new HBond("O2", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O4", this.location));
                            interactions.add(new HBond("O2", "N3", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '{' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new CH("N3", "C2'", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            interactions.add(new CH("C2", "N3", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'",  this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            interactions.add(new Water("C2", "O2", this.location));
                             */
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new HBond("N3", "N2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("N3", "O2'", this.location));
                            interactions.add(new CH(C2", "N3", this.location));
                            interactions.add(new HBond("N1", "N2", this.location));
                             */
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("N3", "O2'", this.location));
                             interactions.add(new CH("C2", "O2", this.location));
                             */
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3",  this.location));
                            interactions.add(new Water("O2", "C2",  this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("O2", "O2'",  this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "O2'",  this.location));
                            interactions.add(new HBond("O2'", "O2",  this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("O2", "O2'",  this.location));
                             */
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new HBond("O2", "N2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("O2", "O2'", this.location));*/
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("O2", "O2'", this.location));
                             interactions.add(new Water("O2", "O2", this.location));*/
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new CH("N3", "C2", this.location));
                            interactions.add(new HBond("N2", "N1", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("N3", "O2'", this.location));
                             interactions.add(new HBond("N2", "N3", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'", this.location));
                             interactions.add(new HBond("N3", "O2'", this.location));
                             interactions.add(new HBond("N2", "O2", this.location));
                             */
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new HBond("N3", "N2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'",  this.location));
                             interactions.add(new HBond("N3", "O2'", this.location));
                             interactions.add(new HBond("N2", "N3", this.location));
                             */
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new HBond("O2'", "O2'",  this.location));
                             interactions.add(new HBond("N3", "O2'", this.location));
                             interactions.add(new HBond("N2", "O2", this.location));
                             */
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new CH("O2", "C2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            interactions.add(new Water("O2", "O2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                             */
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "N3", this.location));
                            interactions.add(new HBond("O2", "N2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                             */
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2", "O2'", this.location));
                             */
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '{' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("N3", "C2", this.location));
                            interactions.add(new CH("C2", "N3", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new CH("C2", "N3", this.location));
                            interactions.add(new CH("N3", "C2", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new CH("C2", "O2", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N3", "N2", this.location));
                            interactions.add(new CH("C2", "N3", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new CH("C2", "N3", this.location));
                            interactions.add(new HBond("N3", "N2", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                             */
                            break;
                        case 'U':
                            interactions.add(new CH("C2", "O2", this.location));
                            interactions.add(new HBond("N1", "O2'", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O2", "C2", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("O2", "N2", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("O2'", "O2'", this.location));
                            interactions.add(new HBond("O2'", "O2", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("N3", "C2", this.location));
                            interactions.add(new HBond("N2", "N3", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            /**
                             The other solution since this interaction is ambiguous
                             interactions.add(new CH("N3", "C2", this.location));
                             interactions.add(new HBond("N2", "N3", this.location));
                             interactions.add(new HBond("O2'", "N1", this.location));
                             */
                            break;
                        case 'C':
                            interactions.add(new HBond("N2", "O2", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O2'", "N2", this.location));
                            interactions.add(new HBond("N3", "N2", this.location));
                            interactions.add(new HBond("N2", "N3", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N2", "O2", this.location));
                            interactions.add(new HBond("N2", "O2'", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O2", "C2", this.location));
                            interactions.add(new HBond("O2'", "N1", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("O2", "N2", this.location));
                            interactions.add(new HBond("O2'", "N2", this.location));
                            break;
                        case 'U':
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '{' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N3", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new CH("C2", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C2", "O4", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("O2'", "C6", this.location));
                            interactions.add(new HBond("O2'", "N4", this.location));
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            interactions.add(new CH("O2", "C5", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N2", "N7", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N2", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N2", "O4", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2", "N4", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new CH("O2", "C5", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '{' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("C2", "N7", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2'", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N3", "N4", this.location));
                            interactions.add(new HBond("O2'", "N4", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            interactions.add(new CH("C2", "O4", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            interactions.add(new HBond("O2'", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2", "N4", this.location));
                            interactions.add(new HBond("O2'", "N4", this.location));
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N2", "N7", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2'", "N6", this.location));
                            break;
                        case 'C':
                            break;
                        case 'G':
                            interactions.add(new HBond("N2", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N2", "O4", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O2", "N6", this.location));
                            interactions.add(new HBond("O2'", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O2", "N4", this.location));
                            interactions.add(new HBond("O2'", "N4", this.location));
                            break;
                        case 'G':
                            break;
                        case 'U':
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '[' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':

                            break;
                        case 'C':

                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O6", this.location));
                            interactions.add(new HBond("N7", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O4", this.location));
                            interactions.add(new HBond("N7", "N3", this.location));
                            interactions.add(new CH("C8", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            interactions.add(new CH("C5", "O2", this.location));
                            interactions.add(new HBond("N4", "N3", this.location));
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O6", "N6", this.location));
                            interactions.add(new HBond("N7", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("O6", "N4", this.location));
                            interactions.add(new HBond("N7", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O6", "N1", this.location));
                            interactions.add(new HBond("N7", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C8", "O2", this.location));
                            interactions.add(new HBond("N7", "N3", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("C5", "N1", this.location));
                            interactions.add(new HBond("O4", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("C5", "N3", this.location));
                            interactions.add(new HBond("O4", "N4", this.location));
                            break;
                        case 'G':

                            break;
                        case 'U':
                            interactions.add(new CH("C5", "O2", this.location));
                            interactions.add(new HBond("O4", "N3", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '[' && this.partnerEdge == ')') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N7", "N6", this.location));
                            interactions.add(new HBond("N6", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N7", "N4", this.location));
                            interactions.add(new HBond("N6", "N3", this.location));
                            break;
                        case 'G':
                            //nothing
                            break;
                        case 'U':
                            interactions.add(new HBond("N7", "N3", this.location));
                            interactions.add(new HBond("N6", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            interactions.add(new CH("C5", "N3", this.location));
                            interactions.add(new HBond("N4", "O2", this.location));
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N7", "N6", this.location));
                            interactions.add(new HBond("O6", "N1", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N7", "N4", this.location));
                            interactions.add(new HBond("O6", "N3", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N7", "N1", this.location));
                            interactions.add(new HBond("O6", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C8", "O4", this.location));
                            interactions.add(new HBond("N7", "N3", this.location));
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new CH("C5", "O6", this.location));
                            interactions.add(new HBond("O4", "N1", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C5", "O4", this.location));
                            interactions.add(new HBond("O4", "N3", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '[' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("N7", "C2", this.location));
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N6", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "O2", this.location));
                            interactions.add(new HBond("N6", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N7", "N2", this.location));
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N6", "O2'", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O2", this.location));
                            interactions.add(new HBond("N6", "O2'", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N3", this.location));
                            interactions.add(new HBond("N4", "O2'", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N4", "O2'", this.location));
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O2", this.location));
                            interactions.add(new HBond("N4", "O2'", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new HBond("O6", "N2", this.location));
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O4", "C2", this.location));
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new HBond("O4", "N2", this.location));
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '[' && this.partnerEdge == '}') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N6", "N3", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N6", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N7", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O2", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N3", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("C6", "O2'", this.location));
                            interactions.add(new HBond("N4", "O2'", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "N3", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O2", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O6", "C2", this.location));
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new HBond("O6", "N2", this.location));
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new CH("O4", "C2", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("C5", "C2", this.location));
                            interactions.add(new Water("O4", "O2", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("O4", "N2", this.location));
                            break;
                        case 'U':
                            interactions.add(new CH("C5", "O2", this.location));
                            interactions.add(new Water("O4", "O2", this.location));
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_CIS && this.edge == '[' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            //nothing
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O6", this.location));
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new CH("C6", "O6", this.location));
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O6", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new CH("O6", "C6", this.location));
                            break;
                        case 'G':
                            interactions.add(new CH("O6", "C8", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new CH("C8", "O6", this.location));
                            */
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            //Nothing
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            //nothing
                            break;
                    }
                    break;
            }
        }
        else if (this.orientation == ORIENTATION_TRANS && this.edge == '[' && this.partnerEdge == ']') {
            switch (this.getResidue().getSymbol()) {
                case 'A':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N7", "N6", this.location));
                            interactions.add(new HBond("N6", "N3", this.location));
                            interactions.add(new HBond("N6", "O2P", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new HBond("N6", "N7", this.location));
                            interactions.add(new HBond("N3", "N6", this.location));
                            interactions.add(new HBond("O2P", "N6", this.location));
                            */
                            break;
                        case 'C':
                            interactions.add(new HBond("N7", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new HBond("N6", "O6", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N6", "O4", this.location));
                            break;
                    }
                    break;
                case 'C':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("N4", "N7", this.location));
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            interactions.add(new HBond("N4", "N7", this.location));
                            break;
                        case 'U':
                            interactions.add(new HBond("N4", "O4", this.location));
                            break;
                    }
                    break;
                case 'G':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O6", "N6", this.location));
                            break;
                        case 'C':
                            interactions.add(new HBond("N7", "N4", this.location));
                            break;
                        case 'G':
                            interactions.add(new CH("O6", "C8", this.location));
                            /*
                            The other solution since this interaction is ambiguous
                            interactions.add(new CH("C8", "O6", this.location));*/
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
                case 'U':
                    switch (this.getPartnerResidue().getSymbol()) {
                        case 'A':
                            interactions.add(new HBond("O4", "N6", this.location));
                            break;
                        case 'C':
                            //Nothing
                            break;
                        case 'G':
                            //Nothing
                            break;
                        case 'U':
                            //Nothing
                            break;
                    }
                    break;
            }
        }
        return interactions;
    }
}

