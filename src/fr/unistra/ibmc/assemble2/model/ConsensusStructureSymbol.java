package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.util.Random;

public class ConsensusStructureSymbol extends Symbol {

    private ConsensusStructureSymbol pairedSymbol;
    private Color color;

    public ConsensusStructureSymbol(final Mediator mediator, char symbol, ConsensusStructure sequence) {
        super(mediator, symbol, sequence);
        Random rand = new Random();
        this.color = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

    public ConsensusStructureSymbol getPairedSymbol() {
        return pairedSymbol;
    }

    public void setPairedSymbol(ConsensusStructureSymbol pairedSymbol) {
        this.pairedSymbol = pairedSymbol;
    }

    public Color getColor() {
        if (this.color == null || this.pairedSymbol == null || this.symbol == '.')
            return ConsensusStructure.SINGLE_STRAND_COLOR;
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
