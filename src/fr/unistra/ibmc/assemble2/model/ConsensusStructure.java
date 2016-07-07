package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.AlignmentView;
import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.ReversedAlignmentView;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ConsensusStructure extends SymbolSequence {

    private boolean unbalanced;
    public static Color SINGLE_STRAND_COLOR = Color.LIGHT_GRAY;

    public ConsensusStructure(final Mediator mediator, final String consensusStructure) {
        super(mediator, consensusStructure.length());
        char[] residues = consensusStructure.toCharArray();
        for (int i=0 ; i < residues.length ; i++)
            this.addSymbol(new ConsensusStructureSymbol(mediator, residues[i], this));
        this.changeColors();
    }

    public void changeColors() {
        Random rand = new Random();
        for (Symbol s:this.symbols) {
            Color newColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
            ((ConsensusStructureSymbol)s).setColor(newColor);
        }
        this.findBasePairs();
    }

    public boolean isUnbalanced() {
        return unbalanced;
    }

    public void setUnbalanced(boolean unbalanced) {
        this.unbalanced = unbalanced;
    }

    public void findBasePairs() {
        List<ConsensusStructureSymbol> leftBrackets =  new ArrayList<ConsensusStructureSymbol>();
        for (int i=0 ; i < this.symbols.size() ; i++) {
            ConsensusStructureSymbol s = (ConsensusStructureSymbol)this.symbols.get(i);
            switch (s.getSymbol()) {
                case '(': leftBrackets.add(s); break;
                case ')':
                    if (!leftBrackets.isEmpty()) {
                        ConsensusStructureSymbol leftBracket = leftBrackets.get(leftBrackets.size()-1);
                        s.setPairedSymbol(leftBracket);
                        leftBracket.setPairedSymbol(s);
                        s.setColor(leftBracket.getColor());
                        leftBrackets.remove(leftBracket);
                    } else {
                        s.setPairedSymbol(null);
                    }
                    break;
                case '.':
                    s.setPairedSymbol(null);
            }
        }
        for (ConsensusStructureSymbol s:leftBrackets)
            s.setPairedSymbol(null);

        for (int i = 0 ; i < this.symbols.size()-1 ; i++ ) {
            ConsensusStructureSymbol consensusStructureSymbol = this.getSymbol(i);
            if (consensusStructureSymbol.getSymbol() == '(') { //WE DEFINE HERE THE COLORS OF THE CONSENSUS HELICES
                //it is a "pushing" strategy. A consensus helix will have the color of its first 5'-residue
                ConsensusStructureSymbol nextConsensusStructureSymbol = this.getSymbol(i+1);
                if (nextConsensusStructureSymbol.getSymbol() == '(' && nextConsensusStructureSymbol.getPairedSymbol() != null) {//same helix in the consensus
                    nextConsensusStructureSymbol.setColor(consensusStructureSymbol.getColor());
                    nextConsensusStructureSymbol.getPairedSymbol().setColor(consensusStructureSymbol.getColor());
                }
            }
        }
    }

    public void addSymbol(Symbol s) {
        this.symbols.add(s);
    }

    public boolean removeGap(final int index) {
        if (this.isGap(index)) {
            this.symbols.remove(index);
            return true;
        }
        return false;
    }

    public void insertGap(final int index) {
        this.symbols.add(index, new ConsensusStructureSymbol(this.mediator, '.', this));
    }

    public boolean isGap(final int index) {
        return /*this.getSymbol(index).getSymbol() == '.'*/ true;
    }

    public void increaseSize() {
        this.symbols.add(this.symbols.size(), new ConsensusStructureSymbol(this.mediator, '.', this));
    }

    public ConsensusStructureSymbol getSymbol(final int index) {
        return (ConsensusStructureSymbol)this.symbols.get(index);
    }

    public void drawSymbol(final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int index) {

    }

    //TODO dirty trick to avoid the inversion of brackets with the reversed view
    public void drawSymbol(final AlignmentView view, final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int index) {
        g.setColor(Color.BLACK);
        ConsensusStructureSymbol consensusStructureSymbol =  this.getSymbol(index);
        if ((consensusStructureSymbol.getSymbol() == ')' || consensusStructureSymbol.getSymbol() == '(') && consensusStructureSymbol.getPairedSymbol() == null) {
            g.setColor(Color.RED);
            this.unbalanced = true;
        }
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
            buff.append(((ConsensusStructureSymbol) i.next()).getSymbol());
        return buff.toString();
    }

    public void setSymbolAt(Symbol s, int i) {

    }


}
