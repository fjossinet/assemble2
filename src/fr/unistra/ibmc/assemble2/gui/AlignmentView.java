package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.components.AssembleHeaderMenuItem;
import fr.unistra.ibmc.assemble2.gui.components.AssembleMenuItem;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class AlignmentView {

    protected int viewX, viewY, currentX, currentY, currentHeight;
    protected int leftMarge = 1, topMarge = 1, bottomMarge = 3;
    protected int firstPos = -1, lastPos = -2, firstSeq = -1, lastSeq = -2;
    protected Rectangle drawingArea, selectionBox;
    protected Residue mouseOverResidueFromRefSeq;
    protected SymbolSequence mouseOverSequence;
    protected int realPositionFromMouseOverSequence, mouseOverPositionInAlignment = -1;
    protected AlignmentCanvas canvas;
    protected StructuralAlignment alignment;
    protected boolean selected;
    //store the x coordinates of the center of each column of the alignment displayed in the current view
    protected Map<Integer, Integer> currentColumnCoordinates;
    protected boolean   displayCisWCWC = true,
            displayTransWCWC = true,
            displayCisHH = true,
            displayTransHH = true,
            displayCisSESE = true,
            displayTransSESE = true,
            displayCisHSE = true,
            displayTransHSE = true,
            displayCisHWC = true,
            displayTransHWC = true,
            displayCisSEWC = true,
            displayTransSEWC = true,
            displayTertiaryInteractions = true,
            displaySecondaryInteractions = true;
    //the positions for the reference sequence for which the interactions can be displayed
    protected java.util.List<Integer> selectedPositionsToDrawInteractions;
    protected AlignmentView previousView, nextView;
    protected Mediator mediator;
    protected Map<SymbolSequence, Integer> annotationLayersDrawn;
    protected Map<SymbolSequence, List<Pair<Annotation,GeneralPath>>> annotationsDrawn;
    protected JPopupMenu popupMenu;

    public AlignmentView(Mediator mediator, AlignmentCanvas canvas, StructuralAlignment alignment) {
        this.mediator = mediator;
        this.canvas = canvas;
        this.alignment = alignment;
        this.selectedPositionsToDrawInteractions = new ArrayList<Integer>();
        this.currentColumnCoordinates = new HashMap<Integer, Integer>();
        this.annotationsDrawn = new HashMap<SymbolSequence, List<Pair<Annotation,GeneralPath>>>();
    }

    public java.util.List<Integer> getSelectedPositionsToDrawInteractions() {
        return selectedPositionsToDrawInteractions;
    }

    public void setAlignment(StructuralAlignment alignment) {
        this.alignment = alignment;
        this.viewX = 0;
        this.viewY = 0;
    }

    public StructuralAlignment getAlignment() {
        return alignment;
    }

    public AlignmentView getNextView() {
        return nextView;
    }

    public boolean isLinkedToNextView() {
        return this.nextView != null ;
    }

    public void setNextView(AlignmentView nextView) {
        this.nextView = nextView;
    }

    public void setNumberOfSequencesToDisplay(int nb) {
        AssembleConfig.setNumberOfSequencesToDisplay(nb);
        this.canvas.repaint();
    }

    public int getLeftMarge() {
        return leftMarge;
    }

    public int getTopMarge() {
        return topMarge;
    }

    public int getBottomMarge() {
        return bottomMarge;
    }

    public int getFirstPos() {
        return firstPos;
    }

    public int getLastPos() {
        return lastPos;
    }

    public void setViewX(int viewX) {
        this.viewX = viewX;
        if (this.viewX < 0 )
            this.viewX = 0;
        if (this.nextView != null)
            this.nextView.setViewX(viewX); //the next view will do the repaint for all the canvas
        else
            this.canvas.repaint();
    }

    public void setViewY(int viewY) {
        this.viewY = viewY;
        if (this.viewY < 0)
            this.viewY = 0;
        this.canvas.repaint();
    }

    public int getViewX() {
        return viewX;
    }

    public int getViewY() {
        return viewY;
    }

    public void moveViewOnLeft() {
        //do nothing if the first position of the alignment is already displayed
        if (this.firstPos == 0)
            return;
        int length = this.firstPos*this.canvas.gc.getVerticalAdvance(),
                width = (int)this.getDrawingArea().getWidth();
        //test to avoid to go beyond the first position
        if (length < width) {
            this.viewX = 0 ;
        }
        else {
            this.viewX -= width;
            //a test when the length is larger that the width bu the with still too large for the view (without this test, place the view at position 35 of the alignment and press once on the left button => the display is too far before the first position)
            if (this.viewX < 0)
                this.viewX = 0;
        }
        if (this.nextView != null)
            this.nextView.moveViewOnLeft();  //the next view will do the repaint for all the canvas
        else
            this.canvas.repaint();
    }

    public void moveViewOnRight() {
        //do nothing if the end of the alignment is already displayed
        if (this.lastPos == this.alignment.getLength()-1)
            return;
        //test to avoid to go beyond the last position
        int length = (this.alignment.getLength()-this.lastPos)*canvas.gc.getVerticalAdvance(),
                width = (int)this.getDrawingArea().getWidth();
        this.viewX += (length < width ? length : width);
        if (this.nextView != null)
            this.nextView.moveViewOnRight(); //the next view will do the repaint for all the canvas
        else
            this.canvas.repaint();
    }

    public void draw(Graphics2D g2, GraphicContext gc) {
        if (this.alignment != null) {
            SymbolSequence seq = null;
            Rectangle2D r = null;
            boolean found = false;
            this.currentX = 0;
            this.currentY = 0;
            this.firstPos = -1;
            this.lastPos = -2;
            this.drawingArea = new Rectangle(gc.currentX, gc.currentY, gc.currentWidth, this.getHeight(gc));
            /*this.selectionBox = new Rectangle(gc.currentX-this.leftMarge*gc.getHorizontalAdvance(),gc.currentY-this.leftMarge*gc.getVerticalAdvance()/4,this.leftMarge*gc.getVerticalAdvance()/2,this.leftMarge*gc.getVerticalAdvance()/2);
            if (this.selected)
                g2.setColor(Color.RED);
            if (this.alignment == canvas.getMainAlignment())
                g2.fillOval((int)this.selectionBox.getMinX(), (int)this.selectionBox.getMinY(),(int)this.selectionBox.getWidth(), (int)this.selectionBox.getHeight());
            else
                g2.fill(this.selectionBox);*/
            g2.setColor(Color.BLACK);
            this.currentColumnCoordinates.clear();
            this.mouseOverResidueFromRefSeq = null;
            this.mouseOverSequence = null;
            this.mouseOverPositionInAlignment = -1;
            this.annotationsDrawn.clear();
            for (AlignedMolecule am:this.alignment.getAlignedMolecules())
                this.annotationsDrawn.put(am, new ArrayList<Pair<Annotation, GeneralPath>>());
            this.annotationLayersDrawn = new HashMap<SymbolSequence, Integer>();
            for (AlignedMolecule am:this.alignment.getAlignedMolecules())
                this.annotationLayersDrawn.put(am,0);
            canvas.most_downstream_annotation = null;
            canvas.most_upstream_annotation = null;
            this.alignment.getConsensusStructure().setUnbalanced(false);

            seq = this.alignment.getSequenceMeter();
            this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance();
            this.currentY += gc.getVerticalAdvance();
            for (int j = 0; j < seq.size(); j++) {
                if (this.currentX - this.viewX < this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance()) {
                    this.currentX += gc.getHorizontalAdvance();
                    continue;
                }
                if (this.drawingArea.getWidth() < (this.currentX - this.viewX + gc.getHorizontalAdvance()))
                    break;
                if (this.firstPos == -1)
                    this.firstPos = j;
                this.lastPos = j;
                seq.drawSymbol(this.alignment, g2, currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, this.alignment.getBiologicalSequenceAt(0).getSymbol(j).isGap() ? -1 : this.alignment.getBiologicalSequenceAt(0).getSymbol(j).getPositionInSequence());
                this.currentColumnCoordinates.put(j,currentX+gc.currentX - viewX+gc.getLetterWidth()/2);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        this.mouseOverSequence = seq;
                        this.mouseOverPositionInAlignment = j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(j)) {
                            this.mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence());
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
            }


            seq = this.alignment.getConsensusStructure();
            this.currentY += gc.getVerticalAdvance();
            this.currentX = 0;
            this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();

            for (int j = this.firstPos; j <= this.lastPos; j++) {
                ((ConsensusStructure)seq).drawSymbol(this,this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        this.mouseOverSequence = seq;
                        this.mouseOverPositionInAlignment = j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(j)) {
                            this.mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence());
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
            }

            seq = this.alignment.getReferenceStructure();
            this.currentY += gc.getVerticalAdvance();
            this.currentX = 0;
            this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();
            for (int j = this.firstPos; j <= this.lastPos; j++) {
                ((ReferenceStructure)seq).drawSymbol(this,this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        this.mouseOverSequence = seq;
                        this.mouseOverPositionInAlignment = j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(j)) {
                            this.mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence());
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
            }

            seq = this.alignment.getBiologicalSequenceAt(0);
            this.currentY += gc.getVerticalAdvance();
            this.currentX = 0;
            g2.drawString("S1", gc.currentX, gc.currentY+currentY);
            this.currentX = alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos* gc.getHorizontalAdvance();
            for (int j = this.firstPos; j <= this.lastPos; j++) {
                seq.drawSymbol(this.alignment, g2, currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        this.mouseOverSequence = seq;
                        this.mouseOverPositionInAlignment = j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(j)) {
                            this.mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(),this.alignment.getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence());
                            this.realPositionFromMouseOverSequence = this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).getPositionInSequence();
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
                this.firstSeq = 1;
                this.lastSeq = 1;
            }

            //now the drawing of the annotations linked to this reference sequence
            int molecularStart = seq.getSymbol(this.firstPos).getPositionInSequence(),
                    molecularEnd = seq.getSymbol(this.lastPos).getPositionInSequence();

            List<Annotation> annotations = null;

            if (((AlignedMolecule) seq).getMolecule().isPlusOrientation())
                annotations = ((AlignedMolecule)seq).getMolecule().getAnnotations(molecularStart, molecularEnd);
            else
                annotations = ((AlignedMolecule)seq).getMolecule().getAnnotations(molecularEnd, molecularStart);

            if (!annotations.isEmpty()) {

                g2.setColor(Color.BLACK);

                this.currentY += (0.5f*(float)gc.getVerticalAdvance());

                this.currentX = 0;
                this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();

                ANNOTATIONS: for (Annotation annotation: annotations) {

                    int annotationStartIndex = -1,
                            annotationEndIndex = -1;

                    if (((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                        annotationStartIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getStart());
                        annotationEndIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getEnd());
                    } else {
                        annotationStartIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getEnd());
                        annotationEndIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getStart());
                    }

                    if (annotationStartIndex == -1)
                        annotationStartIndex = this.firstPos;
                    if (annotationEndIndex == -1)
                        annotationEndIndex = this.lastPos;
                    int length = annotationEndIndex-annotationStartIndex+1;

                    int _annotationLayer = 0;

                    GeneralPath path = new GeneralPath();
                    if (annotation.isPlusOrientation() && ((AlignedMolecule) seq).getMolecule().isPlusOrientation() || !annotation.isPlusOrientation() && !((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                        double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                        path.moveTo(upperLeftX,upperLeftY);
                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY);
                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance()/2);
                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                        path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance());
                        path.lineTo(upperLeftX,upperLeftY);
                        path.closePath();
                    }
                    else {
                        double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                        path.moveTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY);
                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance());
                        path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                        path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance()/2);
                        path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                        path.closePath();
                    }

                    for (Map.Entry<SymbolSequence,List<Pair<Annotation, GeneralPath>>> entry: this.annotationsDrawn.entrySet()) {
                        List<Pair<Annotation, GeneralPath>> annotationsDrawn = entry.getValue();
                        for (int i=0; i< annotationsDrawn.size();i++) {
                            Pair<Annotation, GeneralPath> annotationDrawn =  annotationsDrawn.get(i);
                            if (path.getBounds2D().intersects(annotationDrawn.getSecond().getBounds2D())) {
                                _annotationLayer++;
                                path = new GeneralPath();
                                if (annotation.isPlusOrientation() && ((AlignedMolecule) seq).getMolecule().isPlusOrientation() || !annotation.isPlusOrientation() && !((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                                    double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                            upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                                    path.moveTo(upperLeftX,upperLeftY);
                                    path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY);
                                    path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance()/2);
                                    path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                                    path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance());
                                    path.lineTo(upperLeftX,upperLeftY);
                                    path.closePath();
                                }
                                else {
                                    double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                            upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                                    path.moveTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                                    path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY);
                                    path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance());
                                    path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                                    path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance()/2);
                                    path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                                    path.closePath();
                                }
                                i= -1;
                                continue;
                            }
                        }
                    }

                    if (_annotationLayer+1 > this.annotationLayersDrawn.get(seq))
                        this.annotationLayersDrawn.put(seq, _annotationLayer+1);

                    this.annotationsDrawn.get(seq).add(new Pair<Annotation, GeneralPath>(annotation, path));

                    if (canvas.most_downstream_annotation == null || annotation.getStart() < canvas.most_downstream_annotation.getStart())
                        canvas.most_downstream_annotation = annotation;
                    if (canvas.most_upstream_annotation == null || annotation.getEnd() > canvas.most_upstream_annotation.getEnd())
                        canvas.most_upstream_annotation = annotation;

                    g2.setColor(Assemble.getColorForGenomicFeature(annotation.getAnnotationClass()));
                    Stroke previousStroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(path);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2.fill(path);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2.setStroke(previousStroke);
                    g2.setColor(Color.BLACK);

                }
            }

            this.currentY += 1.5f*(float)gc.getVerticalAdvance()*this.annotationLayersDrawn.get(seq);

            int upperlimit = this.currentY;

            int totalSeq = this.alignment.getBiologicalSequenceCount();

            SEQLOOP :for (int i = 1; i < totalSeq ; i++) {
                if (i+1 > AssembleConfig.getNumberOfSequencesToDisplay(mediator.getAlignmentCanvas().getMainAlignment()))
                    break SEQLOOP;
                seq = this.alignment.getBiologicalSequenceAt(i);
                this.currentY += gc.getVerticalAdvance();
                if (this.currentY-this.viewY < upperlimit)
                    continue SEQLOOP;
                if (this.firstSeq == 1)
                    this.firstSeq = i;
                this.lastSeq = i;
                this.currentX = 0;
                g2.drawString("S"+(i+1), gc.currentX, gc.currentY+this.currentY-this.viewY);
                this.currentX = alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();
                for (int j = this.firstPos; j <= this.lastPos; j++) {
                    seq.drawSymbol(this.alignment, g2, currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, j);
                    if (!found) {
                        r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY() - this.viewY - gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                        if (r.contains(canvas.mouseX, canvas.mouseY)) {
                            found = true;
                            this.mouseOverSequence = seq;
                            this.mouseOverPositionInAlignment = j;
                            if (!this.alignment.getBiologicalReferenceSequence().isGap(j)) {
                                this.mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(j).getPositionInSequence());
                                this.realPositionFromMouseOverSequence = this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).getPositionInSequence();
                            }
                        }
                    }
                    this.currentX += gc.getHorizontalAdvance();
                }

                //now the drawing of the annotations linked to this reference sequence
                molecularStart = seq.getSymbol(this.firstPos).getPositionInSequence();
                molecularEnd = seq.getSymbol(this.lastPos).getPositionInSequence();

                if (((AlignedMolecule) seq).getMolecule().isPlusOrientation())
                    annotations = ((AlignedMolecule)seq).getMolecule().getAnnotations(molecularStart, molecularEnd);
                else
                    annotations = ((AlignedMolecule)seq).getMolecule().getAnnotations(molecularEnd, molecularStart);

                if (!annotations.isEmpty()) {
                    g2.setColor(Color.BLACK);

                    this.currentY += (0.5f*(float)gc.getVerticalAdvance());

                    this.currentX = 0;
                    this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();

                    ANNOTATIONS: for (Annotation annotation: annotations) {

                        int annotationStartIndex = -1,
                                annotationEndIndex = -1;

                        if (((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                            annotationStartIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getStart());
                            annotationEndIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getEnd());
                        } else {
                            annotationStartIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getEnd());
                            annotationEndIndex = seq.getSymbolIndexForMolecularPosition(this.firstPos, this.lastPos, annotation.getStart());
                        }

                        if (annotationStartIndex == -1)
                            annotationStartIndex = this.firstPos;
                        if (annotationEndIndex == -1)
                            annotationEndIndex = this.lastPos;
                        int length = annotationEndIndex-annotationStartIndex+1;

                        int _annotationLayer = 0;

                        GeneralPath path = new GeneralPath();
                        if (annotation.isPlusOrientation() && ((AlignedMolecule) seq).getMolecule().isPlusOrientation() || !annotation.isPlusOrientation() && !((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                            double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                    upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                            path.moveTo(upperLeftX,upperLeftY);
                            path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY);
                            path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance()/2);
                            path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                            path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance());
                            path.lineTo(upperLeftX,upperLeftY);
                            path.closePath();
                        }
                        else {
                            double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                    upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                            path.moveTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                            path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY);
                            path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance());
                            path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                            path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance()/2);
                            path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                            path.closePath();
                        }

                        for (Map.Entry<SymbolSequence,List<Pair<Annotation, GeneralPath>>> entry: this.annotationsDrawn.entrySet()) {
                            List<Pair<Annotation, GeneralPath>> annotationsDrawn = entry.getValue();
                            for (int l=0; l < annotationsDrawn.size();l++) {
                                Pair<Annotation, GeneralPath> annotationDrawn =  annotationsDrawn.get(l);
                                if (path.getBounds2D().intersects(annotationDrawn.getSecond().getBounds2D())) {
                                    _annotationLayer++;
                                    path = new GeneralPath();
                                    if (annotation.isPlusOrientation() && ((AlignedMolecule) seq).getMolecule().isPlusOrientation() || !annotation.isPlusOrientation() && !((AlignedMolecule) seq).getMolecule().isPlusOrientation()) {
                                        double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                                upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                                        path.moveTo(upperLeftX,upperLeftY);
                                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY);
                                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance()/2);
                                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance()-gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                                        path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance());
                                        path.lineTo(upperLeftX,upperLeftY);
                                        path.closePath();
                                    }
                                    else {
                                        double upperLeftX = this.currentX+gc.currentX+(annotationStartIndex-this.firstPos)*gc.getHorizontalAdvance()- viewX,
                                                upperLeftY = this.currentY+gc.getCurrentY()+_annotationLayer*(1.5f*(float)gc.getVerticalAdvance());
                                        path.moveTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY);
                                        path.lineTo(upperLeftX+length*gc.getHorizontalAdvance(),upperLeftY+gc.getVerticalAdvance());
                                        path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2,upperLeftY+gc.getVerticalAdvance());
                                        path.lineTo(upperLeftX,upperLeftY+gc.getVerticalAdvance()/2);
                                        path.lineTo(upperLeftX+gc.getHorizontalAdvance()/2, upperLeftY);
                                        path.closePath();
                                    }
                                    l= -1;
                                    continue;
                                }
                            }
                        }

                        if (_annotationLayer+1 > this.annotationLayersDrawn.get(seq))
                            this.annotationLayersDrawn.put(seq,  _annotationLayer+1);

                        this.annotationsDrawn.get(seq).add(new Pair<Annotation, GeneralPath>(annotation, path));

                        if (canvas.most_downstream_annotation == null || annotation.getStart() < canvas.most_downstream_annotation.getStart())
                            canvas.most_downstream_annotation = annotation;
                        if (canvas.most_upstream_annotation == null || annotation.getEnd() > canvas.most_upstream_annotation.getEnd())
                            canvas.most_upstream_annotation = annotation;


                        g2.setColor(Assemble.getColorForGenomicFeature(annotation.getAnnotationClass()));
                        Stroke previousStroke = g2.getStroke();
                        g2.setStroke(new BasicStroke(2));
                        g2.draw(path);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                        g2.fill(path);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        g2.setStroke(previousStroke);
                        g2.setColor(Color.BLACK);

                    }
                }
                this.currentY += 1.5f*(float)gc.getVerticalAdvance()*this.annotationLayersDrawn.get(seq);
            }

            gc.currentY += this.currentY;
            this.currentHeight = gc.currentY;
        }

    }

    public int getCurrentColumnCoordinate(int column) {
        return this.currentColumnCoordinates.get(column);
    }

    public int getHeight(GraphicContext gc) {
        return this.currentHeight;
    }

    public boolean contains(int x, int y) {
        if (this.drawingArea == null)
            return false;
        return this.drawingArea.contains(x,y);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.popupMenu.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }

    public void mouseDragged(final java.awt.event.MouseEvent e) {
        this.translateViewX(canvas.pressedX - e.getX());
        canvas.pressedX = e.getX();
        canvas.pressedY = e.getY();
    }

    public void moveResidues(boolean right) {
        if (this.selected) {  //only if the view is selected
            for (AlignedMolecule seq:this.canvas.getMainAlignment().getAlignedMolecules()) {
                if (seq.hasSelectedPositions()) {
                    int firstIndex = seq.getIndex(seq.getSelection().get(0)),
                            lastIndex = seq.getIndex(seq.getSelection().get(seq.getSelection().size()-1));
                    if (right) {
                        while (lastIndex < this.canvas.getMainAlignment().getLength() && !seq.getSymbol(lastIndex).isGap())
                            lastIndex++;
                        if (lastIndex == this.canvas.getMainAlignment().getLength())
                            for (SymbolSequence s: this.canvas.getMainAlignment().getAllSymbolSequences())
                                s.increaseSize();
                        seq.removeGap(lastIndex);
                        seq.insertGap(firstIndex);
                        if (alignment.getBiologicalReferenceSequence() == seq) {
                            canvas.getMainAlignment().getReferenceStructure().removeGap(lastIndex);
                            canvas.getMainAlignment().getReferenceStructure().insertGap(firstIndex);
                        }
                        canvas.repaint();
                    }
                    else {
                        while (firstIndex > 0 && !seq.getSymbol(firstIndex).isGap())
                            firstIndex--;
                        if (firstIndex == 0 && !seq.getSymbol(firstIndex).isGap()) {
                            for (SymbolSequence s: this.canvas.getMainAlignment().getAllSymbolSequences())
                                s.insertGap(0);
                            seq.removeGap(firstIndex);
                            seq.insertGap(lastIndex+1);
                            if (alignment.getBiologicalReferenceSequence() == seq) {
                                canvas.getMainAlignment().getReferenceStructure().removeGap(firstIndex);
                                canvas.getMainAlignment().getReferenceStructure().insertGap(lastIndex+1);
                            }
                        } else {
                            seq.removeGap(firstIndex);
                            seq.insertGap(lastIndex);
                            if (alignment.getBiologicalReferenceSequence() == seq) {
                                canvas.getMainAlignment().getReferenceStructure().removeGap(firstIndex);
                                canvas.getMainAlignment().getReferenceStructure().insertGap(lastIndex);
                            }
                        }

                        canvas.repaint();
                    }
                }
            }
        }
    }

    public void mousePressed(final MouseEvent e) {
        this.popupMenu = new JPopupMenu();
        for (Map.Entry<SymbolSequence,List<Pair<Annotation, GeneralPath>>> entry: this.annotationsDrawn.entrySet()) {
            for (final Pair<Annotation, GeneralPath> annotationDrawn: entry.getValue()) {
                if (annotationDrawn.getSecond().contains(e.getX(), e.getY())) {

                    popupMenu.add(new AssembleHeaderMenuItem(annotationDrawn.getFirst().getAnnotationClass(), Assemble.getColorForGenomicFeature(annotationDrawn.getFirst().getAnnotationClass())));
                    JMenuItem menuItem;

                    menuItem = new AssembleMenuItem("View details");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            final JTextField strand = new JTextField(annotationDrawn.getFirst().isPlusOrientation() ? "Plus" : "Minus"),
                                    score = new JTextField(""+annotationDrawn.getFirst().getScore()),
                                    _class = new JTextField(""+annotationDrawn.getFirst().getAnnotationClass());
                            strand.setEditable(false);
                            score.setEditable(false);
                            _class.setEditable(false);
                            List<JComponent> inputs = new ArrayList<JComponent>();
                            List<String> keys2Hide = new ArrayList<String>();
                            keys2Hide.add("_id");
                            keys2Hide.add("genome");
                            keys2Hide.add("alignment");
                            for (String key: annotationDrawn.getFirst().getBasicDBObject().keySet()) {
                                if (!keys2Hide.contains(key) && annotationDrawn.getFirst().getBasicDBObject().get(key) != null && annotationDrawn.getFirst().getBasicDBObject().get(key).toString().trim().length() != 0) {
                                    inputs.add(new JLabel(key));
                                    if (key.equals("sequence") || key.equals("translation") || key.equals("note")) {
                                        String sequence = annotationDrawn.getFirst().getBasicDBObject().get(key).toString().replaceAll("\\s+"," ");
                                        StringBuffer formattedSequence = new StringBuffer();
                                        int c = 0, line_count = 1;
                                        while (c < sequence.length()) {
                                            int d = Math.min(sequence.length(), c + 300);
                                            formattedSequence.append(sequence.substring(c,d)+'\n');
                                            c += 300;
                                            line_count ++;
                                        }
                                        JTextArea area = new JTextArea(formattedSequence.toString());
                                        if (key.equals("sequence") || key.equals("translation"))
                                            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                                        JScrollPane scroll = new JScrollPane(area);
                                        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                                        scroll.setPreferredSize(new Dimension(500, line_count*20));
                                        inputs.add(scroll);
                                    }
                                    else
                                        inputs.add(new JTextField(annotationDrawn.getFirst().getBasicDBObject().get(key).toString()));
                                }
                            }

                            JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "Annotation details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                    this.popupMenu.add(menuItem);

                    menuItem = new JMenuItem("Load");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                        mediator.getGenomicAnnotationsPanel().loadAnnotation(annotationDrawn.getFirst().getBasicDBObject());
                        }
                    });
                    this.popupMenu.add(menuItem);

                    break;
                }
            }
        }
        maybeShowPopup(e);
    }

    public void mouseReleased(final MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseClicked(MouseEvent e) {
        if (this.mouseOverSequence != null)
            this.setSelected(true);
        if (this.alignment.getSequenceMeter() == this.mouseOverSequence) {
                for (int i = 0; i < alignment.getSymbolSequenceCount(); i++)
                    if (!ConsensusStructure.class.isInstance(alignment.getSymbolSequenceAt(i)) && !alignment.getSymbolSequenceAt(i).isGap(this.mouseOverPositionInAlignment)) {
                        //not filled with gaps, we add a new column of gaps
                        if (e.isShiftDown())
                            for (int j = 0; j < alignment.getSymbolSequenceCount(); j++)
                                alignment.getSymbolSequenceAt(j).insertGap(this.mouseOverPositionInAlignment);
                        else
                            for (int j = 0; j < alignment.getSymbolSequenceCount(); j++)
                                alignment.getSymbolSequenceAt(j).insertGap(this.mouseOverPositionInAlignment+1);
                        return;
                    }
                //deletion of a column filled with gaps
                for (int i = 0; i < alignment.getSymbolSequenceCount(); i++)
                    if (!StructuralAlignment.SequenceMeter.class.isInstance(alignment.getSymbolSequenceAt(i))) {
                        alignment.getSymbolSequenceAt(i).removeGap(this.mouseOverPositionInAlignment);
                        alignment.getSymbolSequenceAt(i).increaseSize();
                    }
        }
        else if (this.alignment.getConsensusStructure() == this.mouseOverSequence) {
                switch (this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).getSymbol()) {
                    case '(': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol(')'); break;
                    case ')': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol('.'); break;
                    case '.': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol('('); break;
                }
                alignment.getConsensusStructure().findBasePairs();
                if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() ==SecondaryStructureToolBar.CONSENSUS_STRUCTURE) {
                    mediator.getSecondaryCanvas().repaint();
                    mediator.getFoldingLandscape().repaint();
                }
        }
        else if (this.alignment.getReferenceStructure() == this.mouseOverSequence) {
        }
        else if (this.mouseOverSequence != null) { //its a biological sequence
            Symbol b = this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment);
            if (canvas.isEditSequences() && this.alignment.getBiologicalReferenceSequence() != this.mouseOverSequence) {
                switch (b.getSymbol()) {
                    case 'A':b.setSymbol('U'); break;
                    case 'U':b.setSymbol('G'); break;
                    case 'G':b.setSymbol('C'); break;
                    case 'C':b.setSymbol('-'); break;
                    case '-':b.setSymbol('A'); break;
                    default: b.setSymbol('A');
                }
                ((AlignedMolecule)this.mouseOverSequence).getMolecule().setSequence(this.mouseOverSequence.getSequence().replace("-",""));
                ((AlignedMolecule)this.mouseOverSequence).renumber();
                canvas.repaint();
            }
            else if (!b.isGap()) {
                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Residue #" + b.getPositionInSequence()+" clicked.", null, null);
                mediator.getSecondaryCanvas().repaint();
                if (!e.isShiftDown() && !e.isAltDown() && !e.isControlDown())
                    canvas.clearSelectedPositions();
                if (!alignment.getBiologicalReferenceSequence().isSelectedPosition(b))
                    ((AlignedMolecule)this.mouseOverSequence).addSelectedPosition(b);
                else if (alignment.getBiologicalReferenceSequence().isSelectedPosition(b))
                    ((AlignedMolecule)this.mouseOverSequence).removeFromSelection(b);
                if (e.isShiftDown()) {
                    Symbol previousSymbol = this.mouseOverSequence.getPreviousSymbol(b);
                    while (previousSymbol != null && !((AlignedMolecule)this.mouseOverSequence).isSelectedPosition(previousSymbol)) {
                        if (!previousSymbol.isGap())
                            ((AlignedMolecule)this.mouseOverSequence).addSelectedPosition(previousSymbol);
                        previousSymbol = this.mouseOverSequence.getPreviousSymbol(previousSymbol);
                    }
                }
                if (this.mouseOverSequence == alignment.getBiologicalReferenceSequence()) {
                    mediator.getSecondaryCanvas().clearSelection();
                    List<Integer> selectedPositions = new ArrayList<Integer>();
                    for (Symbol s: ((AlignedMolecule)this.mouseOverSequence).getSelection())
                        if (!s.isGap())
                            selectedPositions.add(s.getPositionInSequence());
                    mediator.getSecondaryCanvas().select(selectedPositions);
                }
            }
        } else {
            for (Map.Entry<SymbolSequence,List<Pair<Annotation, GeneralPath>>> entry: this.annotationsDrawn.entrySet()) {
                List<Pair<Annotation, GeneralPath>> annotationsDrawn = entry.getValue();
                for (int j=0; j< annotationsDrawn.size();j++) {
                    Pair<Annotation, GeneralPath> annotationDrawn =  annotationsDrawn.get(j);
                    if (annotationDrawn.getSecond().contains(e.getX(), e.getY())) {
                        this.alignment.getBiologicalReferenceSequence().clearSelectedPositions();
                        for (int i=0 ; i < this.alignment.getLength() ; i++)
                            if (!this.alignment.getBiologicalReferenceSequence().getSymbol(i).isGap() && this.alignment.getBiologicalReferenceSequence().getSymbol(i).getPositionInSequence() >= annotationDrawn.getFirst().getStart() && this.alignment.getBiologicalReferenceSequence().getSymbol(i).getPositionInSequence() <= annotationDrawn.getFirst().getEnd()) {
                                this.alignment.getBiologicalReferenceSequence().addSelectedPosition(this.alignment.getBiologicalReferenceSequence().getSymbol(i).getPositionInSequence());
                            }
                        this.canvas.repaint();

                        mediator.getSecondaryCanvas().clearSelection();
                        List<Integer> selectedPositions = new ArrayList<Integer>();
                        for (Symbol s: alignment.getBiologicalReferenceSequence().getSelection())
                            selectedPositions.add(s.getPositionInSequence());
                        mediator.getSecondaryCanvas().select(selectedPositions);
                        break;
                    }
                }
            }
        }

    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            for (AlignmentView view:canvas.getAlignmentViews())
                view.setSelected(false);
            canvas.setSelectedView(this);
        }
        this.selected = selected;
    }

    void translateViewX(final int transX) {
        this.viewX += transX;
        if (this.viewX < 0)
            this.viewX = 0;
        if (this.nextView != null)
            this.nextView.translateViewX(transX);
    }

    void translateViewY(final int transY) {
        this.viewY += transY;
        if (this.viewY < 0)
            this.viewY = 0;
    }

    public Rectangle getDrawingArea() {
        return this.drawingArea;
    }

    public void setDisplayCisWCWC(Boolean display) {
        this.displayCisWCWC = display;
        this.canvas.repaint();
    }

    public boolean isDisplayCisWCWC() {
        return this.displayCisWCWC;
    }

    public void setDisplayTransWCWC(Boolean display) {
        this.displayTransWCWC = display;
        this.canvas.repaint();
    }

    public boolean isDisplayTransWCWC() {
        return this.displayTransWCWC;
    }

    public boolean isDisplayCisHH() {
        return displayCisHH;
    }

    public void setDisplayCisHH(Boolean displayCisHH) {
        this.displayCisHH = displayCisHH;
        this.canvas.repaint();
    }

    public boolean isDisplayTransHH() {
        return displayTransHH;
    }

    public void setDisplayTransHH(Boolean displayTransHH) {
        this.displayTransHH = displayTransHH;
        this.canvas.repaint();
    }

    public boolean isDisplayCisHSE() {
        return displayCisHSE;
    }

    public void setDisplayCisHSE(Boolean displayCisHSE) {
        this.displayCisHSE = displayCisHSE;
        this.canvas.repaint();
    }

    public boolean isDisplayTransHSE() {
        return displayTransHSE;
    }

    public void setDisplayTransHSE(Boolean displayTransHSE) {
        this.displayTransHSE = displayTransHSE;
        this.canvas.repaint();
    }

    public boolean isDisplayCisHWC() {
        return displayCisHWC;
    }

    public void setDisplayCisHWC(Boolean displayCisHWC) {
        this.displayCisHWC = displayCisHWC;
        this.canvas.repaint();
    }

    public boolean isDisplayTransHWC() {
        return displayTransHWC;
    }

    public void setDisplayTransHWC(Boolean displayTransHWC) {
        this.displayTransHWC = displayTransHWC;
        this.canvas.repaint();
    }

    public boolean isDisplayCisSESE() {
        return displayCisSESE;
    }

    public void setDisplayCisSESE(Boolean displayCisSESE) {
        this.displayCisSESE = displayCisSESE;
        this.canvas.repaint();
    }

    public boolean isDisplayTransSESE() {
        return displayTransSESE;
    }

    public void setDisplayTransSESE(Boolean displayTransSESE) {
        this.displayTransSESE = displayTransSESE;
        this.canvas.repaint();
    }

    public boolean isDisplayCisSEWC() {
        return displayCisSEWC;
    }

    public void setDisplayCisSEWC(Boolean displayCisSEWC) {
        this.displayCisSEWC = displayCisSEWC;
        this.canvas.repaint();
    }

    public boolean isDisplayTransSEWC() {
        return displayTransSEWC;
    }

    public void setDisplayTransSEWC(Boolean displayTransSEWC) {
        this.displayTransSEWC = displayTransSEWC;
        this.canvas.repaint();
    }

    public boolean isDisplayTertiaryInteractions() {
        return displayTertiaryInteractions;
    }

    public void setDisplayTertiaryInteractions(Boolean displayTertiaryInteractions) {
        this.displayTertiaryInteractions = displayTertiaryInteractions;
        this.canvas.repaint();
    }

    public boolean isDisplaySecondaryInteractions() {
        return displaySecondaryInteractions;
    }

    public void setDisplaySecondaryInteractions(Boolean displaySecondaryInteractions) {
        this.displaySecondaryInteractions = displaySecondaryInteractions;
        this.canvas.repaint();
    }

    public void setDisplayAllFamilies(Boolean displayAllFamilies) {
        this.displayCisHH = displayAllFamilies;
        this.displayCisHSE = displayAllFamilies;
        this.displayCisHWC = displayAllFamilies;
        this.displayCisSESE = displayAllFamilies;
        this.displayCisSEWC = displayAllFamilies;
        this.displayCisWCWC = displayAllFamilies;
        this.displayTransHH = displayAllFamilies;
        this.displayTransHSE = displayAllFamilies;
        this.displayTransHWC = displayAllFamilies;
        this.displayTransSESE = displayAllFamilies;
        this.displayTransSEWC = displayAllFamilies;
        this.displayTransWCWC = displayAllFamilies;
        this.canvas.repaint();
    }
}
