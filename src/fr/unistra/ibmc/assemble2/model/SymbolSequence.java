package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

abstract public class SymbolSequence {
    Color color = Color.BLACK;
    boolean selected;
    private int countingFrom = 1;
    protected java.util.List<Symbol> symbols;
    protected Mediator mediator;

    SymbolSequence(final Mediator mediator) {
        this.symbols = new ArrayList<Symbol>();
        this.mediator = mediator;
    }

    SymbolSequence(final Mediator mediator, int size) {
        this.symbols = new ArrayList<Symbol>(size);
        this.mediator = mediator;
    }

    public Symbol getPreviousSymbol(Symbol s) {
        int previousIndex = this.symbols.indexOf(s)-1;
        return previousIndex < 0 ? null : this.symbols.get(previousIndex);
    }

    public Symbol getNextSymbol(Symbol s) {
        int nextIndex = this.symbols.indexOf(s)+1;
        return nextIndex >= this.symbols.size() ? null : this.symbols.get(nextIndex);
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public String getSequence(int startIndex, int lastIndex) {
        final StringBuffer buff = new StringBuffer(lastIndex-startIndex+1);
        for (int i=startIndex ; i<=lastIndex ; i++)
            buff.append(this.symbols.get(i).getSymbol());
        return buff.toString();
    }

    //this method return the index of a Symbol for a given molecular position. Since gap symbols return the molecular position of the upstream non-gap symbol, this method search the last symbol with this molecularPosition
    public int getSymbolIndexForMolecularPosition(int startSearch, int endSearch, int molecularPosition) {
        int lastIndex = -1;
        for (int i=startSearch; i <= endSearch ; i++)
            if (this.symbols.get(i).getPositionInSequence() == molecularPosition)
                lastIndex = i;
            else if (lastIndex != -1)
                return lastIndex;
        return lastIndex;
    }

    /**
     * Return the List of Symbol objects as a String
     *
     * @return the symbols as a String
     */
    abstract public String getSequence();

    /**
     * Return the moleculesCount of the symbols
     *
     * @return
     */
    abstract public int size();

    /**
     * Insert a gap character at the multiple alignment's index position
     *
     * @param index
     */
    abstract public void insertGap(int index);

    /**
     * Remove a gap character at the multiple alignment's index position
     *
     * @param index
     * @return
     */
    abstract public boolean removeGap(int index);

    abstract public void increaseSize();

    abstract public boolean isGap(int index);

    /**
     *
     * @param index from 0 to length-1
     * @return
     */
    abstract public Symbol getSymbol(int index);

    public java.util.List<Symbol> getSymbols() {
        return new ArrayList<Symbol>(this.symbols);
    }

    public void addSymbol(Symbol s) {
        this.symbols.add(s);
    }

    public void addSymbolAt(int index, Symbol s) {
        this.symbols.add(index, s);
    }

    public void addSymbolsAt(int index, Collection<Symbol> symbols) {
        this.symbols.addAll(index,symbols);
    }

    abstract public void drawSymbol(final StructuralAlignment alignment, final Graphics2D g, int currentX, int viewX, int currentY, int viewY, final GraphicContext gc, final int index);

    public Color getColor() {
        return this.color;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public void setCountingFrom(int countingFrom) {
        this.countingFrom = countingFrom;
    }

    public int getCountingFrom() {
        return countingFrom;
    }

    /**
     * Return the position of a Symbol in this SymbolSequence
     * @param s
     * @return the position from 0 to length-1
     */
    public int getIndex(Symbol s) {
        return this.symbols.indexOf(s);
    }

    abstract public void setSymbolAt(Symbol s, int i);
}
