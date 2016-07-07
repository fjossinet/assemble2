package fr.unistra.ibmc.assemble2.model;

import java.util.ArrayList;
import java.util.List;

public class Cytosine3D extends RiboNucleotide3D {

    public Cytosine3D(Molecule molecule, int absolutePosition) {
        super("C",molecule,absolutePosition);
    }

    protected List<Atom> getDefaultBaseAtoms(boolean withDefaultCoordinates) {
        Object[] atoms = {
                "N1", 5.671f, -4.305f, 1.390f,
                "C6", 4.403f, -4.822f, 1.380f,
                "C5", 3.339f, -4.065f, 1.030f,
                "N4", 2.610f, -1.903f, 0.310f,
                "C4", 3.603f, -2.696f, 0.670f,
                "N3", 4.845f, -2.198f, 0.680f,
                "O2", 7.062f, -2.556f, 1.060f,
                "C2", 5.900f, -2.980f, 1.040f
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
