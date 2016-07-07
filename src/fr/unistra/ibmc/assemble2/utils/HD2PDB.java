package fr.unistra.ibmc.assemble2.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HD2PDB {

    public static void convert(String HDFile, String PDBFile, ArrayList<Integer> cut) throws Exception {

        BufferedReader hd = new BufferedReader(new FileReader(HDFile));
        PrintWriter pdb = new PrintWriter(PDBFile);
        int atn = 1;

        int cur = -1;

        char ch = 'A';
        char oldresName;


        String line;
        HD atom = null;
        while ((line = hd.readLine()) != null) {
            atom = new HD(line);

            if (atom.residueNumber == cur + 1) {
                oldresName = atom.residueName;
                if (myContains(cut, cur)) {
                    pdb.println(PDB.getTerString(atn++, oldresName + "", ch, cur));
                    ch++;
                }

            }
            cur = atom.residueNumber;
            pdb.println(PDB.getAtomString(atn++, atom.atomName, atom.residueName + "", ch, atom.residueNumber, atom.x, atom.y, atom.z));
        }
        if (atom != null) {
            pdb.println(PDB.getTerString(atn++, atom.residueName + "", ch, atom.residueNumber));
        }
        hd.close();
        pdb.close();
    }

    public static void convert(String HDFile, String PDBFile) throws Exception {

        BufferedReader hd = new BufferedReader(new FileReader(HDFile));
        PrintWriter pdb = new PrintWriter(PDBFile);
        int atn = 1;

        int cur = -1;

        char ch = 'A';
        char oldresName;


        String line;
        HD atom = null;
        while ((line = hd.readLine()) != null) {
            atom = new HD(line);

            if (atom.residueNumber == cur + 1) {
                oldresName = atom.residueName;
            }
            cur = atom.residueNumber;
            pdb.println(PDB.getAtomString(atn++, atom.atomName, atom.residueName + "", ch, atom.residueNumber, atom.x, atom.y, atom.z));
        }
        if (atom != null) {
            pdb.println(PDB.getTerString(atn++, atom.residueName + "", ch, atom.residueNumber));
        }
        hd.close();
        pdb.close();
    }

    private static boolean myContains(ArrayList<Integer> cut, int cur) {
        for (Integer v : cut) {
            if (v == cur) {
                return true;
            }
        }
        return false;
    }
}
