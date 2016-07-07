package fr.unistra.ibmc.assemble2.model;


import java.util.List;

public abstract class RiboNucleotide3D extends Residue3D {

    public static final int C3ENDO = 0, C2ENDO = 1;
    public static final String
            C1="C1'",
            C2="C2'",
            C3="C3'",
            C4="C4'",
            C5="C5'",
            O2="O2'",
            O3="O3'",
            O4="O4'",
            O5="O5'",
            O1P = "O1P",
            O2P = "O2P",
            O3P = "O3P",
            O1A="O1A",
            O2A="O2A",
            O3A="O3A",
            O1B="O1B",
            O2B="O2B",
            O3B="O3B",
            O1G="O1G",
            O2G="O2G",
            O3G="O3G";
    public static final String[] P = {"P", "PA", "PB", "PG"};

    protected RiboNucleotide3D(String name, Molecule molecule, int absolutePosition) {
        super(name, molecule, absolutePosition);
        this.sugarPucker = RiboNucleotide3D.C3ENDO;
        for (String p:P)
            this.atoms.add(new Atom(p));
        this.atoms.add(new Atom(O1P));
        this.atoms.add(new Atom(O2P));
        this.atoms.add(new Atom(O3P));
        //if tri-phosphate (i.e. SARS PDB provided with S2S)
        this.atoms.add(new Atom(O1A));
        this.atoms.add(new Atom(O2A));
        this.atoms.add(new Atom(O3A));
        this.atoms.add(new Atom(O1B));
        this.atoms.add(new Atom(O2B));
        this.atoms.add(new Atom(O3B));
        this.atoms.add(new Atom(O1G));
        this.atoms.add(new Atom(O2G));
        this.atoms.add(new Atom(O3G));
        this.atoms.add(new Atom(C1));
        this.atoms.add(new Atom(C2));
        this.atoms.add(new Atom(O2));
        this.atoms.add(new Atom(C3));
        this.atoms.add(new Atom(O3));
        this.atoms.add(new Atom(C4));
        this.atoms.add(new Atom(O4));
        this.atoms.add(new Atom(C5));
        this.atoms.add(new Atom(O5));
        this.atoms.addAll(this.getDefaultBaseAtoms(false));
    }

    public Atom setAtomCoordinates(String atomName, float x, float y, float z) {
        Atom a;
        a = super.setAtomCoordinates(atomName, x, y, z);
        //TODO each time some Atom coordinates are registered, check if a new TorsionAngle can be calculated
        return a;
    }

    public int getSugarPucker() {
        return sugarPucker;
    }

    public void setSugarPucker(int sugarPucker) {
        this.sugarPucker = sugarPucker;
    }

    protected abstract List<Atom> getDefaultBaseAtoms(boolean withDefaultCoordinates);

}
