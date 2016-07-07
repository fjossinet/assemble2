package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.AlignmentView;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.ReversedAlignmentView;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ReferenceStructure extends SymbolSequence {

    SecondaryStructure secondaryStructure;

    public ReferenceStructure(final Mediator mediator, final AlignedMolecule referenceSequence, final SecondaryStructure secondaryStructure) {
        super(mediator, referenceSequence.size());
        this.secondaryStructure = secondaryStructure;
        //first step, construction of the symbols list without the gaps in the reference sequence

        List<BaseBaseInteraction> interactions = this.secondaryStructure.getAllBaseBaseInteractions();
        Symbol symbol = null;
        int moleculePos = 1;
        int i =0;
        for (Symbol _s: referenceSequence.getSymbols()) {
            if (_s.isGap()) {
                symbol = ReferenceStructureSymbol.createGap(mediator, this);
                symbol.setPositionInSequence(_s.getPositionInSequence());
            }
            else {
                Residue r = secondaryStructure.getResidue(moleculePos);
                symbol = new ReferenceStructureSymbol(mediator, this,r) ;
                symbol.setPositionInSequence(moleculePos);

                List<BaseBaseInteraction> interactionsRegistered = new ArrayList<BaseBaseInteraction>();
                INTERACTIONS:for (BaseBaseInteraction interaction:interactions) {
                    if (!interactionsRegistered.contains(interaction)) {
                        if (interaction.getResidue().equals(r)) {
                            for (Symbol s:this.symbols) {
                                if (((ReferenceStructureSymbol)s).getResidue()!= null && ((ReferenceStructureSymbol)s).getResidue().equals(interaction.getPartnerResidue())) {
                                    ((ReferenceStructureSymbol)symbol).addReferenceBaseBaseInteraction(interaction, (ReferenceStructureSymbol)s);
                                    interactionsRegistered.add(interaction);
                                    continue INTERACTIONS;
                                }
                            }
                        }
                        else if (interaction.getPartnerResidue().equals(r)) {
                            for (Symbol s:this.symbols) {
                                if (((ReferenceStructureSymbol)s).getResidue()!= null && ((ReferenceStructureSymbol)s).getResidue().equals(interaction.getResidue())) {
                                    ((ReferenceStructureSymbol)symbol).addReferenceBaseBaseInteraction(interaction, (ReferenceStructureSymbol)s);
                                    interactionsRegistered.add(interaction);
                                    continue INTERACTIONS;
                                }
                            }
                        }
                    }
                }
                interactions.removeAll(interactionsRegistered);
                moleculePos++;
            }
            this.symbols.add(symbol);
        }
    }

    public SecondaryStructure getSecondaryStructure() {
        return secondaryStructure;
    }

    public boolean removeGap(final int index) {
        Symbol s = this.getSymbol(index);
        if (s.isGap()) {
            this.symbols.remove(index);
            return true;
        }
        return false;
    }

    public void insertGap(final int index) {
        this.symbols.add(index, ReferenceStructureSymbol.createGap(mediator, this));
    }

    public boolean isGap(final int index) {
        return this.getSymbol(index).isGap();
    }

    public void increaseSize() {
        this.symbols.add(this.symbols.size(), ReferenceStructureSymbol.createGap(mediator, this));
    }

    public Symbol getSymbol(final int index) {
        return this.symbols.get(index);
    }

    public void updateSymbols(BaseBaseInteraction bbi) {
        for (Symbol s: symbols) {
            if (((ReferenceStructureSymbol)s).getSecondaryInteraction() == bbi)
                ((ReferenceStructureSymbol)s).updateSymbol(bbi);
        }
    }

    public void addInteraction(BaseBaseInteraction bbi) {
        ReferenceStructureSymbol symbol = null, pairedSymbol = null;
        for (Symbol s: symbols) {
            if (((ReferenceStructureSymbol)s).getResidue() == bbi.getResidue())
                symbol = (ReferenceStructureSymbol)s;
            else if(((ReferenceStructureSymbol)s).getResidue() == bbi.getPartnerResidue())
                pairedSymbol = (ReferenceStructureSymbol)s;
            if (symbol != null && pairedSymbol != null)
                break;
        }
        if (symbol != null && pairedSymbol != null)
            symbol.addReferenceBaseBaseInteraction(bbi, pairedSymbol);
    }

    public void removeInteraction(BaseBaseInteraction bbi) {
        for (Symbol s: symbols) {
            if (((ReferenceStructureSymbol)s).getSecondaryInteraction() == bbi || ((ReferenceStructureSymbol)s).getReferenceBaseBaseInteractions().contains(bbi))
                ((ReferenceStructureSymbol)s).removeInteraction(bbi);
        }
    }

    public void drawSymbol(final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int index) {
        g.setColor(this.symbols.get(index).getColor());
        g.drawString(new StringBuffer().append(this.getSymbol(index).getSymbol()).toString(), currentX - viewX, currentY);
        g.setColor(Color.BLACK);
    }

    //TODO dirty trick to avoid the inversion of brackets with the reversed view
    public void drawSymbol(final AlignmentView view, final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int index) {
        g.setColor(Color.BLACK);
        char s = this.getSymbol(index).getSymbol();
        if (ReversedAlignmentView.class.isInstance(view)) {
            switch (s) {
                case '(' : s = ')'; break;
                case ')' : s = '('; break;
                case '<' : s = '>'; break;
                case '>' : s = '<'; break;
            }
            if (s == '(' || s == ')') {
                g.setColor(this.getSymbol(index).getColor());
                g.drawString(new StringBuffer().append(s).toString(), currentX - viewX, currentY);
            } else
                g.drawString(new StringBuffer().append(s).toString(), currentX - viewX, currentY);
        }
        else {
            if (s == '(' || s == ')') {
                g.setColor(this.getSymbol(index).getColor());
                g.drawString(new StringBuffer().append(s).toString(), currentX - viewX, currentY);
            } else
                g.drawString(new StringBuffer().append(s).toString(), currentX - viewX, currentY);
        }
        g.setColor(Color.BLACK);
    }

    public void setSelected(final boolean selected) {
        this.selected = false;
    }

    public int size() {
        return this.symbols.size();
    }

    public String getSequence() {
        final StringBuffer buff = new StringBuffer(this.symbols.size());
        for (Iterator i = this.symbols.iterator(); i.hasNext();)
            buff.append(((Symbol) i.next()).getSymbol());
        return buff.toString();

    }

    public void setSymbolAt(Symbol s, int i) {
        //does nothing
    }

}
