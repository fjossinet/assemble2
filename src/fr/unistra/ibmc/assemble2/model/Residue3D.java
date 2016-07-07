package fr.unistra.ibmc.assemble2.model;


import java.util.ArrayList;
import java.util.List;

public class Residue3D {
    protected List<Atom> atoms;
    protected String name;
    protected int absolutePosition;
    protected String label;
    protected Molecule molecule;
    protected int sugarPucker;

    protected Residue3D(String name, Molecule molecule, int absolutePosition) {
        this.atoms = new ArrayList<Atom>();
        this.name = name;
        this.absolutePosition = absolutePosition;
        this.molecule = molecule;
    }

    public void setMolecule(Molecule molecule) {
        this.molecule = molecule;
    }

    public void setAbsolutePosition(int absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    public Molecule getMolecule() {
        return molecule;
    }

    public String getLabel() {
        if (label == null)
            return ""+this.absolutePosition;
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getAbsolutePosition() {
        return this.absolutePosition;
    }

    public Atom setAtomCoordinates(String atomName, float x, float y, float z) {
        atomName = atomName.replace('*', '\'');
        if (atomName.equals("OP1") || atomName.equals("O1P"))
            atomName = RiboNucleotide3D.O1P;
        if (atomName.equals("OP2") || atomName.equals("O2P"))
            atomName = RiboNucleotide3D.O2P;
        if (atomName.equals("OP3") || atomName.equals("O3P"))
            atomName = RiboNucleotide3D.O3P;
        Atom a = this.getAtom(atomName);
        if (a != null)
            a.setCoordinates(x, y, z);
        return a;
    }

    public List<Atom> getAtoms() {
        return new ArrayList<Atom>(this.atoms);
    }

    /**
     * Return an Atom registered with its id given as argument. The id of an atom has to be according to the PDB file format
     *
     * @param atomName
     * @return null if no Atom registered with this id
     */
    public Atom getAtom(String atomName) {
        for (Atom a : this.atoms)
            if (a.getName().equals(atomName))
                return a;
        return null;
    }

    /**
     * This method is used internally to remove atoms during the construction of some specialized Residue3D concepts
     *
     * @param atomName
     */
    protected void removeAtom(String atomName) {
        Atom hit = null;
        for (Atom a : this.atoms)
            if (atomName.equals(a.getName()))
                hit = a;
        this.atoms.remove(hit);
    }

    public List<String> getAtomNames() {
        List<String> names = new ArrayList<String>(this.atoms.size());
        for (Atom a : this.atoms)
            names.add(a.getName());
        return names;
    }

    public String getName() {
        return name;
    }

    public int getSugarPucker() {
        return sugarPucker;
    }

    public void setSugarPucker(int sugarPucker) {
        this.sugarPucker = sugarPucker;
    }

    public class Atom {

        private String name;
        private float[] coordinates;

        protected Atom(String atomName) {
            this.name = atomName;
        }

        /**
         * The array of coordinates is reinitialized to null. After a call to this method, the hasCoordinatesFilled() method will return false
         */
        public void eraseCoordinates() {
            this.coordinates = null;
        }

        public String getName() {

            return this.name;
        }

        public void setCoordinates(float x, float y, float z) {
            if (this.coordinates == null) {
                this.coordinates = new float[]{x, y, z};
            } else {
                this.coordinates[0] = x;
                this.coordinates[1] = y;
                this.coordinates[2] = z;
            }
        }

        public Residue3D getResidue3D() {
            return Residue3D.this;
        }

        public float getX() {
            return this.coordinates[0];
        }

        public float getY() {
            return this.coordinates[1];
        }

        public float getZ() {
            return this.coordinates[2];
        }

        public float[] getCoordinates() {
            return new float[]{this.getX(),this.getY(),this.getZ()};
        }

        public boolean hasCoordinatesFilled() {
            return this.coordinates != null;
        }
    }


}
