package fr.unistra.ibmc.assemble2.model;


import java.util.ArrayList;
import java.util.List;

public class Adenine3D extends RiboNucleotide3D {

    public Adenine3D(Molecule molecule, int absolutePosition) {
        super("A", molecule, absolutePosition);
    }

    protected List<Atom> getDefaultBaseAtoms(boolean withDefaultCoordinates) {
        Object[] atoms = {
                "N9", 5.671f, -4.305f, 1.390f,
                "C8", 4.358f, -4.673f, 1.330f,
                "N7", 3.565f, -3.717f, 0.950f,
                "C5", 4.410f, -2.640f, 0.750f,
                "N6", 2.967f, -0.828f, 0.050f,
                "C6", 4.189f, -1.313f, 0.340f,
                "N1", 5.256f, -0.506f, 0.240f,
                "C2", 6.465f, -0.989f, 0.540f,
                "N3", 6.800f, -2.209f, 0.930f,
                "C4", 5.707f, -2.984f, 1.010f};
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
