package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlignedMolecule extends SymbolSequence {

    protected Molecule molecule;
    protected List<Symbol> selection;

    public AlignedMolecule(Mediator mediator, Molecule molecule) {
        this(mediator, molecule, molecule.printSequence());
    }

    public AlignedMolecule(Mediator mediator, Molecule molecule, String gappedSequence) {
        super(mediator);

        this.selection = new ArrayList<Symbol>();
        this.molecule = molecule;
        if (!molecule.isGenomicAnnotation()) {

            int moleculePos = 1;
            char[] characters = gappedSequence.toCharArray();
            for (char c:characters) {
                if (c != '-' ) {
                    Symbol s = Symbol.getSymbol(mediator, c, this);
                    s.setPositionInSequence(moleculePos++);
                    this.addSymbol(s);
                }
                else
                    this.addSymbol(new Symbol(mediator, '-', this));
            }
        } else {
            int moleculePos = molecule.isPlusOrientation() ? molecule.getFivePrimeEndGenomicPosition() : molecule.getFivePrimeEndGenomicPosition()+molecule.size()-1;
            char[] characters = gappedSequence.toCharArray();
            for (char c:characters) {
                if (c != '-' ) {
                    Symbol s = Symbol.getSymbol(mediator, c, this);
                    s.setPositionInSequence(moleculePos);
                    moleculePos = molecule.isPlusOrientation() ? moleculePos+1 : moleculePos-1;
                    this.addSymbol(s);
                }
                else
                    this.addSymbol(new Symbol(mediator, '-', this));
            }
        }
    }

    public void renumber() {
        int moleculePos = 1;
        if (molecule.isGenomicAnnotation())
            moleculePos = molecule.isPlusOrientation() ? molecule.getFivePrimeEndGenomicPosition() : molecule.getFivePrimeEndGenomicPosition()+molecule.size()-1;
        for (Symbol s: this.symbols) {
            if (!molecule.isGenomicAnnotation()) {
                if (s.getSymbol() != '-' )
                    s.setPositionInSequence(moleculePos++);
                else
                    s.setPositionInSequence(-1);
            } else {
                if (s.getSymbol() != '-' ) {
                    s.setPositionInSequence(moleculePos);
                    moleculePos = molecule.isPlusOrientation() ? moleculePos+1 : moleculePos-1;
                }
                else
                    s.setPositionInSequence(-1);
            }
        }
    }

    public Location getGapsLocation() {
        Location l = new Location();
        for (int i=0 ; i < this.size() ; i++)
            if (this.getSymbol(i).isGap())
                l.add(i+1);
        return l;
    }
    public boolean isSelectedPosition(Symbol s) {
        return this.selection.contains(s);
    }

    public boolean hasSelectedPositions() {
        return this.selection.size() != 0;
    }

    public void setMolecule(Molecule molecule) {
        this.molecule = molecule;
    }

    public Molecule getMolecule() {
        return molecule;
    }

    public Residue getResidue(final int index) {
        return new Residue(mediator, this.molecule, index);
    }

    public void extenseAtEnd() {
        this.insertGap(this.symbols.size());
    }

    public void modifySequence(final String label, final List<Symbol> sequence) {
        this.molecule.setName(label);
        this.symbols.clear();
        this.symbols.addAll(sequence);
    }

    public Symbol getSymbol(final int index) {
        return this.symbols.get(index);
    }

    public void drawSymbol(final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int index) {
        Symbol s = this.getSymbol(index);
        char letter = s.getSymbol();
        if (this.getSymbol(index).isGap()) {
            g.setColor(Color.BLACK);
            if (this == alignment.getBiologicalSequenceAt(0))
                g.drawString(new StringBuffer().append(letter).toString(), currentX - viewX, currentY);
            else
                g.drawString(new StringBuffer().append(letter).toString(), currentX - viewX, currentY - viewY);
        }
        else {
            g.setColor(s.getColor());
            Rectangle2D r = null;
            if (this == alignment.getBiologicalSequenceAt(0))
                r = new Rectangle2D.Float(currentX - viewX, currentY - gc.getLetterHeight() + (int) (0.25 * gc.getLetterHeight()), gc.getLetterWidth(), gc.getLetterHeight());
            else
                r = new Rectangle2D.Float(currentX - viewX, currentY - viewY - gc.getLetterHeight() + (int) (0.25 * gc.getLetterHeight()), gc.getLetterWidth(), gc.getLetterHeight());
            if (!isSelectedPosition(s)) {
                if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE) {
                    if (this == alignment.getBiologicalSequenceAt(0)) {
                        g.fill(r);
                        g.setColor(Color.WHITE);
                    }
                    else {
                        if (alignment.getConsensusStructure().getSymbol(index).getSymbol() != '.') {//if a single-strand, no rectangle and a character of the same color
                            ConsensusStructureSymbol pairedconsensusSymbol = alignment.getConsensusStructure().getSymbol(index).getPairedSymbol();
                            if (pairedconsensusSymbol != null) {
                                char pairedLetter = this.getSymbol(alignment.getConsensusStructure().getIndex(pairedconsensusSymbol)).getSymbol();
                                boolean isCanonical = BaseBaseInteraction.isCanonical(BaseBaseInteraction.ORIENTATION_CIS, '(', letter, ')', pairedLetter);
                                if (isCanonical) {//a filled rectangle and a white character
                                    g.setColor(s.getColor());
                                    g.fill(r);
                                    g.setColor(Color.WHITE);
                                } else //no rectangle, just a black character
                                    g.setColor(Color.BLACK);
                            } else // no pairing, no rectangle, just a black character
                                g.setColor(Color.BLACK);
                        }
                    }
                } else if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().REFERENCE_STRUCTURE) {//rendering of the reference structure
                    if (this == alignment.getBiologicalSequenceAt(0)) {
                        g.fill(r);
                        g.setColor(Color.WHITE);
                    }
                    else {
                        ReferenceStructureSymbol referenceStructureSymbol = (ReferenceStructureSymbol) alignment.getReferenceStructure().getSymbol(index);
                        List<BaseBaseInteraction> interactions = referenceStructureSymbol.getReferenceBaseBaseInteractions();
                        if (!interactions.isEmpty()) {
                            if (this == alignment.getBiologicalSequenceAt(0))
                                r = new Rectangle2D.Float(currentX - viewX, currentY - gc.getLetterHeight() + (int) (0.25 * gc.getLetterHeight()), gc.getLetterWidth() / interactions.size(), gc.getLetterHeight());
                            else
                                r = new Rectangle2D.Float(currentX - viewX, currentY - viewY - gc.getLetterHeight() + (int) (0.25 * gc.getLetterHeight()), gc.getLetterWidth() / interactions.size(), gc.getLetterHeight());
                            int isostericity = -1;
                            for (int i = 0; i < interactions.size(); i++) {
                                BaseBaseInteraction interaction = interactions.get(i);
                                currentX = currentX + i * gc.getLetterWidth() / interactions.size();
                                ReferenceStructureSymbol pairedReferenceStructureSymbol = referenceStructureSymbol.getPairedSymbol(interaction);
                                char pairedLetter = this.getSymbol(alignment.getReferenceStructure().getIndex(pairedReferenceStructureSymbol)).getSymbol();
                                isostericity = interaction.isIsosteric(letter, referenceStructureSymbol.getResidue().getAbsolutePosition(), pairedLetter, pairedReferenceStructureSymbol.getResidue().getAbsolutePosition());
                                if (isostericity == 2) {
                                    g.setColor(s.getColor());
                                    //a filled rectangle and a white character
                                    g.fill(r);
                                    //now the color for the character
                                    g.setColor(Color.WHITE);
                                } else if (isostericity == 1) {
                                    g.setColor(s.getColor());
                                    //an empty rectangle and a character of the same color
                                    g.draw(r);
                                } else {
                                    //no rectangle, just a black character
                                    g.setColor(Color.BLACK);
                                }
                            }
                        }
                    }
                } else {//rendering of bp probabilities or qualitative/quantitative values => nothing to do with bp conservations
                    g.fill(r);
                    g.setColor(Color.WHITE);
                }
            }
            else {
                g.draw(r);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g.fill(r);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            if (this == alignment.getBiologicalSequenceAt(0))
                g.drawString(new StringBuffer().append(letter).toString(), currentX - viewX, currentY);
            else
                g.drawString(new StringBuffer().append(letter).toString(), currentX - viewX, currentY - viewY);

        }
        g.setColor(Color.BLACK);
    }

    public int size() {
        return this.symbols.size();
    }

    public void insertGap(final int index) {
        this.symbols.add(index, new Symbol(mediator, '-', this));
    }

    public boolean removeGap(final int index) {
        if (this.isGap(index)) {
            this.symbols.remove(index);
            return true;
        }
        return false;
    }

    public void increaseSize() {
        this.insertGap(this.symbols.size());
    }

    public boolean isGap(final int index) {
        return this.symbols.get(index).isGap();
    }

    public String getSequence() {
        final StringBuffer buff = new StringBuffer(this.symbols.size());
        for (Iterator i = this.symbols.iterator(); i.hasNext();)
            buff.append(((Symbol) i.next()).getSymbol());
        return buff.toString();

    }

    public void setSymbolAt(Symbol s, int i) {
        this.symbols.set(i,s);
    }

    public void searchFor(String motif) {
        Pattern pattern = Pattern.compile(motif);
        Matcher matcher = pattern.matcher(this.getSequence().replace("-",""));
        while (matcher.find()) {
            Location l = new Location(matcher.start(), matcher.end()-1);
            int j = 0;
            for (Symbol s: this.getSymbols()) {
                if (!s.isGap()) {
                    if (l.hasPosition(j))
                        this.addSelectedPosition(s);
                    j++;
                }
            }
        }

    }

    public void addSelectedPosition(int position) {
        for (Symbol s:this.symbols) {
            if (s.getPositionInSequence() == position && !s.isGap()) { //careful: a gap symbol returns the molecular position of the first upstream non-gap symbol
                this.addSelectedPosition(s);
                return;
            }
        }
    }

    public void addSelectedPosition(Symbol s) {
        this.selection.add(s);
        Collections.sort(this.selection,new Comparator<Symbol>() {
            @Override
            public int compare(Symbol symbol, Symbol symbol2) {
                if (molecule.isPlusOrientation())
                    return symbol.getPositionInSequence()-symbol2.getPositionInSequence();
                else
                    return symbol2.getPositionInSequence()-symbol.getPositionInSequence();
            }
        });
    }

    public void removeFromSelection(Symbol s) {
        this.selection.remove(s);
    }

    public void clearSelectedPositions() {
        this.selection.clear();
    }

    public List<Symbol> getSelection() {
        return this.selection;
    }

    public String getSelectionAsString() {
        StringBuffer buff = new StringBuffer();
        for (Symbol s:this.selection)
            buff.append(s.getSymbol());
        return buff.toString();
    }
}
