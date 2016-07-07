package fr.unistra.ibmc.assemble2.model;

import java.util.ArrayList;
import java.util.List;

public class Guanine3D extends RiboNucleotide3D {

    public Guanine3D(Molecule molecule, int absolutePosition) {
        super("G", molecule,absolutePosition);
    }

    protected List<Atom> getDefaultBaseAtoms(boolean withDefaultCoordinates) {
        Object[] atoms = {
                "N9", 5.671f, -4.305f, 1.390f,
                "C8", 4.338f, -4.651f, 1.320f,
                "N7", 3.550f, -3.676f, 0.940f,
                "C5", 4.420f, -2.604f, 0.740f,
                "O6", 3.067f, -0.759f, 0.040f,
                "C6", 4.148f, -1.276f, 0.330f,
                "N1", 5.325f, -0.513f, 0.260f,
                "N2", 7.579f, -0.093f, 0.420f,
                "C2", 6.597f, -0.986f, 0.550f,
                "N3", 6.848f, -2.225f, 0.940f,
                "C4", 5.712f, -2.974f, 1.010f
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
