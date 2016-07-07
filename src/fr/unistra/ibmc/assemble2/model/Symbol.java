package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;

/**
 * @author fjossinet
 */
public class Symbol {

    protected char symbol;
    protected int positionInSequence = -1;
    protected SymbolSequence sequence;
    private Mediator mediator;

    public Symbol(final Mediator mediator, final char symbol, SymbolSequence sequence) {
        this.mediator = mediator;
        this.symbol = symbol;
        this.positionInSequence = -1;
        this.sequence = sequence;
    }

    public boolean isGap() {
        return this.getSymbol() == '-';
    }

    public int getPositionInSequence() {
        if (this.isGap()) {//a gap doesn't have a position in sequence (since it is a gap!!). We get the position in the sequence of the non-gap upstream symbol
            Symbol nextSymbol = this.sequence.getNextSymbol(this);
            while (nextSymbol != null) {
                if (!nextSymbol.isGap())
                    return nextSymbol.getPositionInSequence();
                else
                    nextSymbol = this.sequence.getNextSymbol(nextSymbol);
            }
            //if we reach the end of the sequence, then we go upstream to get the position of the first non-gap symbol
            Symbol previousSymbol = this.sequence.getPreviousSymbol(this.sequence.getSymbol(this.sequence.size()-1));
            while (previousSymbol != null) {
                if (!previousSymbol.isGap())
                    return previousSymbol.getPositionInSequence();
                else
                    previousSymbol = this.sequence.getPreviousSymbol(previousSymbol);
            }
            return -1;
        }
        else
            return positionInSequence;
    }

    public void setPositionInSequence(int positionInSequence) {
        this.positionInSequence = positionInSequence;
    }

    /**
     * Return the character stored
     *
     * @return the character stored
     */
    public char getSymbol() {
        return this.symbol;
    }

    /**
     * Return the Symbol object corresponding to a character
     *
     * @param symbol the character for which we want the Symbol object in memory
     * @return the corresponding Symbol object
     */
    public static Symbol getSymbol(final Mediator mediator, final char symbol, SymbolSequence sequence) {
        switch (symbol) {
            case 'A':
            case 'T':
            case 'U':
            case 'G':
            case 'C':
            case 'N':
                //we create a new object since each object will be attached to its own positionInSequence
                return new Symbol(mediator, symbol, sequence);
            //if we have an unusual character
            default :
                return new Symbol(mediator, symbol, sequence);
        }
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public Color getColor() {
        if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE) {
            return mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSymbol(sequence.getIndex(this)).getColor();
        } else {
            int positionInSequence = -1;
            if (sequence == mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence())
                positionInSequence = this.getPositionInSequence();
            else
                positionInSequence = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(this.sequence.getIndex(this)).getPositionInSequence();
            Residue r = mediator.getSecondaryCanvas().getSecondaryStructure().getResidue(positionInSequence);
            return r.getFinalColor(mediator.getSecondaryCanvas().getGraphicContext());
        }
    }

}
