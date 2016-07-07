package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class ReversedAlignmentView extends AlignmentView {

    private GeneralPath iWantMoreResiduesOnTheRightSide,
                        iWantNextAnnotationOnTheRightSide;

    public ReversedAlignmentView(Mediator mediator, AlignmentCanvas canvas, StructuralAlignment alignment) {
        super(mediator, canvas, alignment);
    }

    public void setNumberOfSequencesToDisplay(int nb) {
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

    public void moveViewOnLeft() {
        //do nothing if the first position of the alignment is already displayed
        if (this.lastPos == this.alignment.getLength()-1)
            return;
        //test to avoid to go beyond the last position
        int length = (this.alignment.getLength()-this.lastPos)*canvas.gc.getVerticalAdvance(),
                width = (int)this.getDrawingArea().getWidth();
        this.viewX -= (length < width ? length : width);
        if (this.viewX < 0)
            this.viewX = 0;
        if (this.nextView != null)
            this.nextView.moveViewOnLeft();
        this.canvas.repaint();

    }

    public void moveViewOnRight() {
        //do nothing is the first position of the alignment is already displayed (important for the ReversedAlignmentView)
        if (this.firstPos == 0)
            return;
        int length = this.firstPos*this.canvas.gc.getVerticalAdvance(),
                width = (int)this.getDrawingArea().getWidth();
        //test to avoid to go beyond the first position
        this.viewX += (length < width ? length : width);
        if (this.viewX > this.alignment.getLength()*this.canvas.gc.getVerticalAdvance())
            this.viewX = this.alignment.getLength()*this.canvas.gc.getVerticalAdvance()-width;
        if (this.nextView != null)
            this.nextView.moveViewOnRight();
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
            this.iWantMoreResiduesOnTheRightSide = null;
            this.drawingArea = new Rectangle(gc.currentX, gc.currentY, gc.currentWidth, this.getHeight(gc));
            this.selectionBox = new Rectangle(gc.currentX-this.leftMarge*gc.getHorizontalAdvance(),gc.currentY-this.leftMarge*gc.getVerticalAdvance()/4,this.leftMarge*gc.getVerticalAdvance()/2,this.leftMarge*gc.getVerticalAdvance()/2);
            if (this.selected)
                g2.setColor(Color.RED);
            if (this.alignment == canvas.getMainAlignment())
                g2.fillOval((int)this.selectionBox.getMinX(), (int)this.selectionBox.getMinY(),(int)this.selectionBox.getWidth(), (int)this.selectionBox.getHeight());
            else
                g2.fill(this.selectionBox);
            g2.setColor(Color.BLACK);
            this.currentColumnCoordinates.clear();
            this.mouseOverResidueFromRefSeq = null;
            this.mouseOverSequence = null;
            this.mouseOverPositionInAlignment = -1;
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
                seq.drawSymbol(this.alignment, g2, currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, this.alignment.getBiologicalSequenceAt(0).getSymbol(seq.size()-1-j).isGap() ? -1 : this.alignment.getBiologicalSequenceAt(0).getSymbol(seq.size()-1-j).getPositionInSequence());
                this.currentColumnCoordinates.put(seq.size()-1-j,currentX+gc.currentX - viewX+gc.getLetterWidth()/2);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        mouseOverSequence = seq;
                        mouseOverPositionInAlignment = seq.size()-1-j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(seq.size()-1-j)) {
                            mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(seq.size()-1-j).getPositionInSequence());
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
            }

            if (this.firstPos == 0 && this.alignment.getBiologicalReferenceSequence().getMolecule().isGenomicAnnotation()) {
                iWantMoreResiduesOnTheRightSide = new GeneralPath();
                iWantMoreResiduesOnTheRightSide.moveTo((this.alignment.getMaxLabelSize()+1) *gc.getHorizontalAdvance()+gc.getLetterWidth()/2, this.currentY+gc.getCurrentY()- gc.getLetterHeight());
                iWantMoreResiduesOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()+1) * gc.getHorizontalAdvance() + gc.getLetterWidth(), this.currentY + gc.getCurrentY() - gc.getLetterHeight());
                iWantMoreResiduesOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()+1) * gc.getHorizontalAdvance() + gc.getLetterWidth(), this.currentY + gc.getCurrentY());
                iWantMoreResiduesOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()+1) * gc.getHorizontalAdvance() + gc.getLetterWidth() / 2, this.currentY + gc.getCurrentY());
                iWantMoreResiduesOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()+1) * gc.getHorizontalAdvance(), this.currentY + gc.getCurrentY() - gc.getLetterHeight() / 2);
                iWantMoreResiduesOnTheRightSide.moveTo((this.alignment.getMaxLabelSize()+1) * gc.getHorizontalAdvance() + gc.getLetterWidth() / 2, this.currentY + gc.getCurrentY() - gc.getLetterHeight());
                g2.fill(iWantMoreResiduesOnTheRightSide);

                g2.setColor(Color.GRAY);
                iWantNextAnnotationOnTheRightSide = new GeneralPath();
                iWantNextAnnotationOnTheRightSide.moveTo((this.alignment.getMaxLabelSize()-1/2) *gc.getHorizontalAdvance()+gc.getLetterWidth()/2, this.currentY+gc.getCurrentY()- gc.getLetterHeight());
                iWantNextAnnotationOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()-1/2) * gc.getHorizontalAdvance() + gc.getLetterWidth(), this.currentY + gc.getCurrentY() - gc.getLetterHeight());
                iWantNextAnnotationOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()-1/2) * gc.getHorizontalAdvance() + gc.getLetterWidth(), this.currentY + gc.getCurrentY());
                iWantNextAnnotationOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()-1/2) * gc.getHorizontalAdvance() + gc.getLetterWidth() / 2, this.currentY + gc.getCurrentY());
                iWantNextAnnotationOnTheRightSide.lineTo((this.alignment.getMaxLabelSize()-1/2) * gc.getHorizontalAdvance(), this.currentY + gc.getCurrentY() - gc.getLetterHeight() / 2);
                iWantNextAnnotationOnTheRightSide.moveTo((this.alignment.getMaxLabelSize()-1/2) * gc.getHorizontalAdvance() + gc.getLetterWidth() / 2, this.currentY + gc.getCurrentY() - gc.getLetterHeight());
                //g2.fill(iWantNextAnnotationOnTheRightSide);
                g2.setColor(Color.BLACK);

            }

            seq = this.alignment.getConsensusStructure();
            this.currentY += gc.getVerticalAdvance();
            this.currentX = 0;
            this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();
            for (int j = this.firstPos; j <= this.lastPos; j++) {
                ((ConsensusStructure)seq).drawSymbol(this,this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, seq.size()-1-j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        mouseOverSequence = seq;
                        mouseOverPositionInAlignment = seq.size()-1-j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(seq.size()-1-j)) {
                            mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(seq.size()-1-j).getPositionInSequence());
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
                ((ReferenceStructure)seq).drawSymbol(this,this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, seq.size()-1-j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        mouseOverSequence = seq;
                        mouseOverPositionInAlignment = seq.size()-1-j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(seq.size()-1-j)) {
                            mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(seq.size()-1-j).getPositionInSequence());
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
            }

            seq = this.alignment.getBiologicalSequenceAt(0);
            this.currentY += gc.getVerticalAdvance();
            this.currentX = 0;
            g2.drawString("S1", gc.currentX, gc.currentY+currentY);
            this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();
            for (int j = this.firstPos; j <= this.lastPos; j++) {
                seq.drawSymbol(this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, seq.size()-1-j);
                if (!found) {
                    r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY()- gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                    if (r.contains(canvas.mouseX, canvas.mouseY)) {
                        found = true;
                        mouseOverSequence = seq;
                        mouseOverPositionInAlignment = seq.size()-1-j;
                        if (!this.alignment.getBiologicalReferenceSequence().isGap(seq.size()-1-j)) {
                            mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(seq.size()-1-j).getPositionInSequence());
                            realPositionFromMouseOverSequence = mouseOverSequence.getSymbol(mouseOverPositionInAlignment).getPositionInSequence();
                        }
                    }
                }
                this.currentX += gc.getHorizontalAdvance();
                this.firstSeq = 1;
                this.lastSeq = 1;
            }

            int totalSeq = this.alignment.getBiologicalSequenceCount();

            SEQLOOP : for (int i = 1; i < totalSeq; i++) {
                if (i+1 > AssembleConfig.getNumberOfSequencesToDisplay(mediator.getAlignmentCanvas().getMainAlignment()))
                    break SEQLOOP;
                seq = this.alignment.getBiologicalSequenceAt(i);
                this.currentY += gc.getVerticalAdvance();
                //the 5 parameter is for : SequenceMeter+Structure 2D/3D + Consensus+ Sequence of reference + first non-reference to display
                // if not enough place then the current sequence is not the first non-reference sequence to display
                if (this.currentY-this.viewY < 5*gc.getVerticalAdvance())
                    continue SEQLOOP;
                if (this.firstSeq == 1)
                    this.firstSeq = i;
                this.lastSeq = i;
                this.currentX = 0;
                g2.drawString("S"+(i+1), gc.currentX, gc.currentY+this.currentY-this.viewY);
                this.currentX = this.alignment.getMaxLabelSize()*gc.getHorizontalAdvance() + this.firstPos * gc.getHorizontalAdvance();
                for (int j = this.firstPos; j <= this.lastPos; j++) {
                    seq.drawSymbol(this.alignment, g2,currentX+gc.currentX, viewX, this.currentY+gc.getCurrentY(), viewY, gc, seq.size()-1-j);
                    if (!found) {
                        r = new Rectangle2D.Float(this.currentX+gc.currentX - this.viewX, this.currentY+gc.getCurrentY() - this.viewY - gc.getLetterHeight()+(int)(0.25*gc.getLetterHeight()),gc.getLetterWidth() , gc.getLetterHeight());
                        if (r.contains(canvas.mouseX, canvas.mouseY)) {
                            found = true;
                            mouseOverSequence = seq;
                            mouseOverPositionInAlignment = seq.size()-1-j;
                            if (!this.alignment.getBiologicalReferenceSequence().isGap(seq.size()-1-j)) {
                                mouseOverResidueFromRefSeq = new Residue(mediator, this.alignment.getBiologicalReferenceSequence().getMolecule(), this.alignment.getBiologicalReferenceSequence().getSymbol(seq.size()-1-j).getPositionInSequence());
                                realPositionFromMouseOverSequence = mouseOverSequence.getSymbol(mouseOverPositionInAlignment).getPositionInSequence();
                            }
                        }
                    }
                    this.currentX += gc.getHorizontalAdvance();
                }
            }

            gc.currentY += this.currentY;

            int tmp = this.firstPos;
            this.firstPos = seq.size()-1-this.lastPos;
            this.lastPos =  seq.size()-1-tmp;
        }
    }

    public int getCurrentColumnCoordinate(int column) {
        return this.currentColumnCoordinates.get(column);
    }

    public int getHeight(GraphicContext gc) {
        return (AssembleConfig.getNumberOfSequencesToDisplay(mediator.getAlignmentCanvas().getMainAlignment())+3)*gc.getVerticalAdvance(); // the "3" parameter is for the SequenceMeter + Consensus + the Structure 2D/3D of reference
    }

    public boolean contains(int x, int y) {
        if (this.drawingArea == null)
            return false;
        return this.drawingArea.contains(x,y);
    }

    public void moveResidues(boolean right) { //just the reverse behavior according to the AlignmentView class
        if (this.selected) {  //only if the view is selected
            for (AlignedMolecule seq:this.canvas.getMainAlignment().getAlignedMolecules()) {
                if (seq.hasSelectedPositions()) {
                    int firstIndex = seq.getIndex(seq.getSelection().get(0)),
                            lastIndex = seq.getIndex(seq.getSelection().get(seq.getSelection().size()-1));
                    if (!right) {
                        for (int i=firstIndex ; i<= lastIndex ; i++) {
                            if (seq.isSelectedPosition(seq.getSymbol(i))) {
                                int start = i, currentPosition = i;
                                while (currentPosition < this.canvas.getMainAlignment().getLength() && seq.getSymbol(currentPosition).isGap() || seq.isSelectedPosition(seq.getSymbol(currentPosition)))
                                    currentPosition++;
                                currentPosition--;
                                if (seq.isGap(currentPosition) && currentPosition < this.canvas.getMainAlignment().getLength()) {
                                    seq.removeGap(currentPosition);
                                    seq.insertGap(start);
                                    if (alignment.getBiologicalReferenceSequence() == seq) {
                                        canvas.getMainAlignment().getReferenceStructure().removeGap(currentPosition);
                                        canvas.getMainAlignment().getReferenceStructure().insertGap(start);
                                    }
                                    canvas.repaint();
                                }
                                i+=currentPosition;
                            }
                        }
                    }
                    else {
                        for (int i=lastIndex ; i>= firstIndex ; i--) {
                            if (seq.isSelectedPosition(seq.getSymbol(i))) {
                                int start = i, currentPosition = i;
                                while (currentPosition >=0 && (seq.getSymbol(currentPosition).isGap() || seq.isSelectedPosition(seq.getSymbol(currentPosition))) )
                                    currentPosition--;
                                currentPosition++;
                                if (seq.isGap(currentPosition) && currentPosition >= 0) {
                                    seq.removeGap(currentPosition);
                                    seq.insertGap(start);
                                    if (alignment.getBiologicalReferenceSequence() == seq) {
                                        canvas.getMainAlignment().getReferenceStructure().removeGap(currentPosition);
                                        canvas.getMainAlignment().getReferenceStructure().insertGap(start);
                                    }
                                    canvas.repaint();
                                }
                                i-=currentPosition;
                            }
                        }
                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        //since the insertion or deletion of gaps are made for all the sequences => no modification in the conservation scores
        //not necessary to recalculate them
        if (alignment.getSequenceMeter() == this.mouseOverSequence) {
            if (canvas.mode == AlignmentCanvas.INSERT) {
                for (int i = 0; i < alignment.getSymbolSequenceCount(); i++)
                    alignment.getSymbolSequenceAt(i).insertGap(this.mouseOverPositionInAlignment+1);
            } else if (canvas.mode == AlignmentCanvas.DELETE) {
                for (int i = 0; i < alignment.getSymbolSequenceCount(); i++)
                    if (!alignment.getSymbolSequenceAt(i).isGap(this.mouseOverPositionInAlignment))
                        return;
                for (int i = 0; i < alignment.getSymbolSequenceCount(); i++)
                    alignment.getSymbolSequenceAt(i).removeGap(this.mouseOverPositionInAlignment);
            }
        }
        else if (alignment.getConsensusStructure() == this.mouseOverSequence) {
            if (canvas.mode == AlignmentCanvas.STANDARD) {
                switch (this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).getSymbol()) {
                    case '(': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol(')'); break;
                    case ')': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol('.'); break;
                    case '.': this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment).setSymbol('('); break;
                }
                ((ConsensusStructure)this.mouseOverSequence).findBasePairs();
            }
        }
        else if (alignment.getReferenceStructure() == this.mouseOverSequence) {
        }
        else if (this.mouseOverSequence != null) { //its a biological sequence
            Symbol b = this.mouseOverSequence.getSymbol(this.mouseOverPositionInAlignment);
            if (!b.isGap()) {
                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Residue #" + b.getPositionInSequence()+" clicked.", null, null);
                mediator.getSecondaryCanvas().repaint();
                if (!e.isShiftDown())
                    canvas.clearSelectedPositions();
                if (!alignment.getBiologicalReferenceSequence().isSelectedPosition(b))
                    ((AlignedMolecule)this.mouseOverSequence).addSelectedPosition(b);
                else if (alignment.getBiologicalReferenceSequence().isSelectedPosition(b))
                    ((AlignedMolecule)this.mouseOverSequence).removeFromSelection(b);
                if (e.isShiftDown()) {
                    Symbol nextSymbol = this.mouseOverSequence.getNextSymbol(b);
                    while (nextSymbol != null && !((AlignedMolecule)this.mouseOverSequence).isSelectedPosition(nextSymbol)) {
                        if (!nextSymbol.isGap())
                            ((AlignedMolecule)this.mouseOverSequence).addSelectedPosition(nextSymbol);
                        nextSymbol = this.mouseOverSequence.getNextSymbol(nextSymbol);
                    }
                }
                if (this.mouseOverSequence == alignment.getBiologicalReferenceSequence()) {
                    mediator.getSecondaryCanvas().clearSelection();
                    java.util.List<Integer> selectedPositions = new ArrayList<Integer>();
                    for (Symbol s: ((AlignedMolecule)this.mouseOverSequence).getSelection())
                        if (!s.isGap())
                            selectedPositions.add(s.getPositionInSequence());
                    mediator.getSecondaryCanvas().select(selectedPositions);
                }
            }
        } else if (this.iWantMoreResiduesOnTheRightSide != null && this.iWantMoreResiduesOnTheRightSide.contains(e.getX(), e.getY())) {
            this.mediator.getGenomicAnnotationsPanel().iWantMoreResiduesOnTheRight();
            canvas.repaint();
        } else if (this.iWantNextAnnotationOnTheRightSide != null && this.iWantNextAnnotationOnTheRightSide.contains(e.getX(), e.getY())) {
            java.util.List<Symbol> symbols = this.mediator.getGenomicAnnotationsPanel().iWantNextAnnotationOnTheRight(canvas.most_downstream_annotation, canvas.most_upstream_annotation);
            for (Symbol s: symbols) {
                alignment.getSequenceMeter().insertGap(alignment.getSequenceMeter().size());
                alignment.getConsensusStructure().insertGap(alignment.getConsensusStructure().size());
                alignment.getReferenceStructure().addSymbolAt(alignment.getReferenceStructure().size(), new ReferenceStructureSymbol(mediator, alignment.getReferenceStructure(), '.'));
                for (int j=1 ; j < alignment.getBiologicalSequenceCount() ; j++)
                    alignment.getBiologicalSequenceAt(j).insertGap(alignment.getBiologicalSequenceAt(j).size());
            }

            alignment.getBiologicalReferenceSequence().addSymbolsAt(alignment.getBiologicalReferenceSequence().size(),symbols);

            canvas.repaint();
        }
    }

    void translateViewX(final int transX) {
        this.viewX += transX;
        if (this.viewX < 0)
            this.viewX = 0;
        if (this.previousView != null)
            this.previousView.translateViewX(transX);
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
}
