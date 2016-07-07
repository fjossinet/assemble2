package fr.unistra.ibmc.assemble2.model;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TertiaryStructure {

    private List<Residue3D> residues;
    private Molecule m;
    private String id;
    private String name;

    public TertiaryStructure(String name) {
        this.residues = new ArrayList<Residue3D>();
        this.name = name;
        this.id = new ObjectId().toString();
    }

    public TertiaryStructure(Molecule m) {
        this("Tertiary Structure");
        this.m = m;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setMolecule(Molecule m) {
        this.m = m;
    }

    public Molecule getMolecule() {
        return m;
    }

    public void addResidue3D(Residue3D r) {
        this.residues.add(r);
    }

    public Residue3D addResidue3D(int absolutePosition) {
        Residue3D r =  null;
        String residueName = this.m.getResidueAt(absolutePosition);
        if ("A".equals(residueName))
            r = new Adenine3D(this.m,absolutePosition);
        else if ("U".equals(residueName))
            r = new Uracil3D(this.m,absolutePosition);
        else if ("G".equals(residueName))
            r = new Guanine3D(this.m,absolutePosition);
        else if ("C".equals(residueName))
            r = new Cytosine3D(this.m,absolutePosition);
        if (r != null) {
            this.removeResidue3D(absolutePosition);
            this.residues.add(r);
        }
        return r;
    }

    public List<Residue3D> getResidues3D() {
        Collections.sort(this.residues, new Comparator<Residue3D>() {
            public int compare(Residue3D r1, Residue3D r2) {
            return r1.getAbsolutePosition() - r2.getAbsolutePosition();
            }
        });
        return new ArrayList<Residue3D>(this.residues);
    }

    public Residue3D getResidue3DAt(int position) {
        for (Residue3D r:this.residues)
            if (r.getAbsolutePosition() == position)
                return r;
        return null;
    }

    public void removeResidue3D(int absolutePosition) {
        for (Residue3D r:this.residues)
            if (r.getAbsolutePosition() == absolutePosition) {
                this.residues.remove(r);
                return;
            }
    }

    @Override
    public String toString() {
        return this.getName()+" ("+this.getMolecule().size()+" nts)";
    }
}
