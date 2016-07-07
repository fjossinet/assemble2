package fr.unistra.ibmc.assemble2.model;

import java.util.ArrayList;
import java.util.List;

public class Uracil3D extends RiboNucleotide3D {

    public Uracil3D(Molecule molecule, int absolutePosition) {
        super("U",molecule,absolutePosition);
    }

    protected List<Atom> getDefaultBaseAtoms(boolean withDefaultCoordinates) {
        Object[] atoms = {
                "N1", 5.671f, -4.305f, 1.390f,
                "C6", 4.402f, -4.837f, 1.380f,
                "C5", 3.337f, -4.092f, 1.040f,
                "O4", 2.584f, -1.954f, 0.320f,
                "C4", 3.492f, -2.709f, 0.660f,
                "N3", 4.805f, -2.261f, 0.690f,
                "O2", 7.028f, -2.502f, 1.040f,
                "C2", 5.913f, -3.000f, 1.040f
        };
        List<Atom> ret = new ArrayList<Atom>();

        for (int i = 0; i < atoms.length; i += 4) {
            Atom a = new Atom((String) atoms[i]);
            if (withDefaultCoordinates)
                a.setCoordinates((Float) atoms[i + 1], (Float) atoms[i + 2], (Float) atoms[i + 3]);
            ret.add(a);
        }
        return ret;
    }
}
