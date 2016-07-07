package fr.unistra.ibmc.assemble2.model;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class Junction {
    private List<StructuralDomain> structuralDomains;
    private List<Object[]> fragments;
    private SecondaryStructure ss;

    protected Junction(SecondaryStructure ss) {
        this.fragments = new ArrayList<Object[]>();
        this.structuralDomains = new ArrayList<StructuralDomain>();
        this.ss = ss;
    }

    public boolean hasPosition(int pos) {
        for (MutablePair<Molecule,Location> p:getFragments())
            if (p.right.hasPosition(pos))
                return true;
        return false;
    }

    public boolean isSelected() {
        for (MutablePair<Molecule,Location> p:getFragments())
            for (int pos:p.right.getSinglePositions())
                if (SingleStrand.class.isInstance(this.ss.getResidue(pos).getStructuralDomain()) && !this.ss.getResidue(pos).isSelected())
                    return false;
        return true;
    }

    public boolean contains(int position, Molecule molecule) {
        for (Object[] fragment:fragments) {
            if (fragment[0].equals(molecule) && ((Location)fragment[1]).hasPosition(position))
                return true;
        }
        return false;
    }

    public void addFragment(MutablePair<Molecule, Location> fragment) {
        this.fragments.add(new Object[]{fragment.left,fragment.right});
    }

    public void addStructuralDomain(StructuralDomain sd) {
        this.structuralDomains.add(sd);
    }

    public List<MutablePair<Molecule,Location>> getFragments() {
        List<MutablePair<Molecule,Location>> _fragments = new ArrayList<MutablePair<Molecule, Location>>();
        for (Object[] fragment:this.fragments)
            _fragments.add(new MutablePair<Molecule, Location>((Molecule)fragment[0],(Location)fragment[1]));
        return _fragments;
    }

    public List<StructuralDomain> getStructuralDomains() {
        return structuralDomains;
    }

}
