package fr.unistra.ibmc.assemble2.io.computations;


import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.rnart.NucMult;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.HD;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.TBMath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Rnart extends Computation {

    public Rnart(Mediator mediator) {
        super(mediator);
    }

    public List<Residue3D> refine(List<Residue3D> residues3D, List<BaseBaseInteraction> interactions, String threshold) throws Exception {
        File rnartDir =  IoUtils.createTemporaryDirectory("rnart"+System.nanoTime());

        System.out.println(rnartDir.getAbsolutePath());
        rnartDir.mkdir();
        File    globalPDB = new File(rnartDir,"GLOBAL.PDB"),
                atomFile = new File(rnartDir,"ATOMS.DAT"),
                nuclinFile = new File(rnartDir,"NUCLIN.DAT"),
                hbndFile = new File(rnartDir,"HBND.DAT");

        String mode = "n";

        //WRITE GLOBAL.PDB FILE
        int currentPosition = -1;
        StringBuffer sequence = new StringBuffer();
        for (Residue3D r:residues3D)
            sequence.append(r.getName().charAt(0));

        //we use the simplePDB method to generate the PDB file, to avoid to re-sort the residues. This could reorganize the helices differently as in the _3dMolecules List. And we need to have the molecules sorted in the same way
        //between the GLOBAL.PDB (and consequently in the ATOMS.DAT) and the NUCLIN.DAT (and consequently in the NUCLIN.OUT).
        FileParser.writePDBFile(residues3D, false, new FileWriter(globalPDB));

        //WRITE NUCLIN.DAT FILE
        String nuclindat = "";

        nuclindat += "Rnart Service\n\n";
        nuclindat += formatSequenceforRnaRT(sequence) + "\n";

        nuclindat += "    3\n";
        String tmp = "";

        //here we don't add the basepairs, this will be taken care off via the AtomAtomInteraction in HBND.DAT

        nuclindat += "\n" + "MODEL\n" + "    0    2    0    0    0\n" + "    4   " + residues3D.size() + "\n";

        List<Integer> sugars = getSugars(residues3D);

        for (int i = 0; i < sugars.size(); i++) {
            tmp += " " + sugars.get(i);
            if ((i % 35) == 34) {
                nuclindat += tmp + "\n";
                tmp = "";
            }
        }
        if (!tmp.equals(""))
            nuclindat += tmp + "\n";

        PrintWriter out = new PrintWriter(new FileWriter(nuclinFile));
        out.print(nuclindat);
        out.flush();
        out.close();

        //WRITE HBND.DAT FILE
        String hbnddat = "";
        List<String> hbndLines = getHbndLines(interactions, residues3D);

        for (String hbndline : hbndLines)
            hbnddat += hbndline + "\n";

        out = new PrintWriter(new FileWriter(hbndFile));
        out.print(hbnddat);
        out.flush();
        out.close();

        //WRITE ATOMS.DAT FILE
        HD.convertPDBasHD(atomFile, globalPDB);

        NucMult nm = new NucMult(mediator, mode, threshold, rnartDir.getAbsolutePath()+System.getProperty("file.separator"));
        int pass = nm.getNumberOfPass();
        String p = pass + "";
        if (pass < 10)
            p = "0" + p;
        File f = new File(rnartDir, "ATOMS_" + p + ".hd.pdb");

        TertiaryStructure refinedTs = FileParser.parsePDB(mediator, new FileReader(f)).get(0);

        return refinedTs.getResidues3D();
    }

    private String formatSequenceforRnaRT(StringBuffer sequence) {
        StringBuffer out = new StringBuffer();
        if (sequence.length() > 0) {
            out.append(sequence.charAt(0));
            int line = 1;
            for (int i = 1; i < sequence.length(); i++) {
                out.append(sequence.charAt(i));
                line++;
                if (line == 70 && i < sequence.length() - 1) {
                    line = 0;
                    out.append("-\n");
                }
            }
            out.append("\n");
        }
        return out.toString();
    }

    List<Integer> getSugars(List<Residue3D> residues3D) throws Exception {
        List<Integer> ret = new ArrayList<Integer>();
        for (Residue3D r : residues3D)
            ret.add(r.getSugarPucker());
        return ret;
    }

    List<String> getHbndLines(List<BaseBaseInteraction> interactions, List<Residue3D> residues3D) throws Exception {
        List<String> ret = new ArrayList<String>();

        for (BaseBaseInteraction bbi : interactions) {
            for (AtomAtomInteraction aai:bbi.getAtomAtomInteractions()) {
                double maxDist = 0;
                double minDist = 0;

                if (HBond.class.isInstance(aai)) {
                    minDist = 2.7f;
                    maxDist = 3.1f;
                }
                else if (CH.class.isInstance(aai)) {
                    minDist = 2.8f;
                    maxDist = 4.0f;
                }
                else if (Water.class.isInstance(aai)) {
                    minDist = 3.3f;
                    maxDist = 5.0f;
                }
                /*
                else if (MAGNESIUM.class.isInstance(aai)) {
                    minDist = 1.8f; //???
                    maxDist = 2.2f; //???
                }*/
                else if (Sodium.class.isInstance(aai)) {
                     minDist = 3.0f;
                     maxDist = 3.5f;
                }


                Location l = aai.getLocation();

                Residue3D r1 = null, r2 = null;

                for (Residue3D residue3D:residues3D) {
                    if (residue3D.getAbsolutePosition() == l.getStart())
                        r1 = residue3D;
                    else if (residue3D.getAbsolutePosition() == l.getEnd())
                        r2 = residue3D;
                    if (r1 != null && r2 != null)
                        break;
                }

                if (r1 != null && r2 != null) {

                    String fNName = "   " + r1.getName().charAt(0);
                    String fNNum = TBMath.intFormat(Integer.parseInt(r1.getLabel()), 3);
                    String fAName = aai.getAtom1();
                    while (fAName.length() < 4)
                        fAName += " ";

                    String sNName = " " + r2.getName().charAt(0);
                    String sNNum = TBMath.intFormat(Integer.parseInt(r2.getLabel()), 3);
                    String sAName = aai.getAtom2();
                    while (sAName.length() < 4)
                        sAName += " ";

                    String min = TBMath.doubleFormat(minDist, 6, 1);
                    String max = "   " + TBMath.doubleFormat(maxDist, 6, 1);

                    ret.add(fNName + fNNum + fAName + sNName + sNNum + sAName + min + max);
                }
            }
        }
        return ret;
    }
}
