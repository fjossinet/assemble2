package fr.unistra.ibmc.assemble2.io.computations;


import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.Modeling3DUtils;

import java.util.ArrayList;
import java.util.List;

public class Fragment extends Computation {
    public static final String REFERENCE_RESIDUES = "reference residues", TARGET_LOCATION = "target location";

    public Fragment(Mediator mediator) {
        super(mediator);
    }

    public void infer(Molecule targetMolecule, TertiaryStructure newTertiaryStructure, List<Residue3D> referenceResidues3D, Location targetLocation) {
        List<Residue3D> residues = new ArrayList<Residue3D>();
        boolean phosphateAdded = false;
        RESIDUES : for (Residue3D r:referenceResidues3D) {
            residues.add(r);
            //the Fragment webservice cannot work efficiently if the residue at the 5'-end doesn't have a phosphate group
            if (residues.size()==1) {
                for (String phosphate: RiboNucleotide3D.P)
                    if (r.getAtom(phosphate).hasCoordinatesFilled())
                        continue RESIDUES;
                phosphateAdded = true;
                //if no phosphate atom found
                r.setAtomCoordinates("P", 0f, 0f, 0f);
                r.setAtomCoordinates(RiboNucleotide3D.O1P, 0f, 0f, 0f); //difficult to have O1P or O2P if no P detected
                r.setAtomCoordinates(RiboNucleotide3D.O2P, 0f, 0f, 0f);
                if (!r.getAtom(RiboNucleotide3D.O5).hasCoordinatesFilled()) //the O5 atom could be present even if no phosphate atom
                    r.setAtomCoordinates(RiboNucleotide3D.O5, 0f, 0f, 0f);
            }
        }
        try {
            List<Residue3D> computedResidues = Modeling3DUtils.thread(mediator, newTertiaryStructure, targetLocation.getStart(), targetLocation.getLength(), Modeling3DUtils.RNA, residues);
            if (phosphateAdded) {
                //we erase the fake coordinates in the tertiary structure inferred
                for (Residue3D r:computedResidues) {
                    if (r.getAbsolutePosition() == 1) {
                        r.getAtom("P").eraseCoordinates();
                        r.getAtom(RiboNucleotide3D.O1P).eraseCoordinates();
                        r.getAtom(RiboNucleotide3D.O2P).eraseCoordinates();
                        if (r.getAtom(RiboNucleotide3D.O5).getX() == 0f && r.getAtom(RiboNucleotide3D.O5).getY() == 0f && r.getAtom(RiboNucleotide3D.O5).getZ() == 0f ) //if the phosphate has been added, the O5 has been added is its coordinates are 0,0,0 (few chances that the O5 had "naturally" these coordinates)
                            r.getAtom(RiboNucleotide3D.O5).eraseCoordinates();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
