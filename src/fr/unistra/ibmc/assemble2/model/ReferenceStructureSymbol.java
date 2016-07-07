package fr.unistra.ibmc.assemble2.model;


import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenceStructureSymbol extends Symbol {
    private Residue residue;
    //one base can establish several interactions
    //these interactions are stored in a Map to store the "paired" symbols for each interaction established by the current symbol
    protected Map<BaseBaseInteraction, ReferenceStructureSymbol> referenceStructureInteractions;
    private ReferenceStructure referenceStructure;

    public ReferenceStructureSymbol(final Mediator mediator, final ReferenceStructure referenceStructure, char symbol) {
        super(mediator, symbol, referenceStructure);
        this.referenceStructure = referenceStructure;
        this.referenceStructureInteractions = new HashMap<BaseBaseInteraction,ReferenceStructureSymbol>();
    }

    public ReferenceStructureSymbol(final Mediator mediator, final ReferenceStructure referenceStructure, final Residue residue) {
        this(mediator, referenceStructure, '.');
        this.residue = residue;
    }

    public boolean isGap() {
        return this.getSymbol() == '-';
    }

    public boolean isSingleStrand() {
        return this.getSymbol() == '.';
    }

    public void addReferenceBaseBaseInteraction(BaseBaseInteraction interaction, ReferenceStructureSymbol pairedSymbol) {
        this.referenceStructureInteractions.put(interaction, pairedSymbol);
        if (interaction.isSecondaryInteraction()) {
            if (interaction.getResidue().equals(this.residue))
                this.symbol = interaction.getEdge(interaction.getResidue());
            else if (interaction.getPartnerResidue().equals(this.residue))
                this.symbol = interaction.getEdge(interaction.getPartnerResidue());
        }
        if (!pairedSymbol.referenceStructureInteractions.containsKey(interaction))
            pairedSymbol.addReferenceBaseBaseInteraction(interaction,this);
    }

    public void updateSymbol(BaseBaseInteraction interaction) {
        if (interaction.isSecondaryInteraction()) {
            if (interaction.getResidue().equals(this.residue))
                this.symbol = interaction.getEdge(interaction.getResidue());
            else if (interaction.getPartnerResidue().equals(this.residue))
                this.symbol = interaction.getEdge(interaction.getPartnerResidue());
        }
    }

    public void removeInteraction(BaseBaseInteraction interaction) {
        if (interaction.isSecondaryInteraction())
            this.setSymbol('.');
        referenceStructureInteractions.remove(interaction);
    }

    public List<BaseBaseInteraction> getReferenceBaseBaseInteractions() {
        return new ArrayList<BaseBaseInteraction>(this.referenceStructureInteractions.keySet());
    }

    public Residue getResidue() {
        return residue;
    }

    public char getEdge(BaseBaseInteraction interaction) {
        if (this.referenceStructureInteractions.containsKey(interaction)) {
            if (interaction.getResidue().equals(this.residue))
                return interaction.getEdge(interaction.getResidue());
            else if (interaction.getPartnerResidue().equals(this.residue))
                return interaction.getEdge(interaction.getPartnerResidue());
        }
        //[fjossinet] TODO improve that
        throw new RuntimeException();
    }

    public ReferenceStructureSymbol getPairedSymbol(BaseBaseInteraction interaction) {
        return this.referenceStructureInteractions.get(interaction);
    }

    public BaseBaseInteraction getSecondaryInteraction() {
        for (BaseBaseInteraction interaction:this.getReferenceBaseBaseInteractions())
            if (interaction.isSecondaryInteraction())
                return interaction;
        return null;
    }

    public List<BaseBaseInteraction> getTertiaryInteractions() {
        List<BaseBaseInteraction> interactions = new ArrayList<BaseBaseInteraction>();
        for (BaseBaseInteraction interaction:this.getReferenceBaseBaseInteractions())
            if (!interaction.isSecondaryInteraction())
                interactions.add(interaction);
        return interactions;
    }

    public void removeBaseBaseInteraction(BaseBaseInteraction interaction) {
        if (this.referenceStructureInteractions.containsKey(interaction)) {
            ReferenceStructureSymbol pairedSymbol = (ReferenceStructureSymbol)this.referenceStructureInteractions.get(interaction);
            pairedSymbol.referenceStructureInteractions.remove(interaction);
            this.referenceStructureInteractions.remove(interaction);
            if (interaction.isSecondaryInteraction()) {
                this.symbol = '.';
                pairedSymbol.symbol = '.';
            }
            return;
        }
    }

    public Color getColor() {
        return Color.BLACK;
    }

    public static ReferenceStructureSymbol createGap(Mediator mediator, ReferenceStructure referenceStructure) {
        return new ReferenceStructureSymbol(mediator, referenceStructure, '-');
    }
}
