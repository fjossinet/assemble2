package fr.unistra.ibmc.assemble2.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PDB {
    String type; // TER or ATOM
    int atomNumber;
    String atomName;
    int residueNumber;
    String residueName;
    String chain;
    double x;
    double y;
    double z;
    double b;
    double q;
    char altLoc = ' ';

    public String getType() {
        return type;
    }

    public static boolean isProtein(String r) {
        String[] prot = {"ALA", "VAL", "PHE", "PRO", "MET", "ILE", "LEU", "ASP", "GLU", "LYS", "ARG", "SER", "THR", "TYR", "HIS", "CYS", "ASN", "GLN", "TRP", "GLY", "MSE"};
        for (int i = 0; i < prot.length; i++) {
            if (prot[i].equals(r))
                return true;
        }
        return false;
    }

    public static String getResidue(String line) {
        return line.substring(17, 20).replaceAll(" ", "");
    }


    public static void renumberPDB(String filename) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            PrintWriter out = new PrintWriter(new FileWriter(filename.substring(0, filename.lastIndexOf(".pdb")) + "numbered.pdb"));

            String line;
            char chain = 'A';
            int readResidue = -1;
            int residue = 0;
            int atom = 1;
            String rname = "";
            while ((line = in.readLine()) != null) {
                if (line.substring(0, 4).equals("ATOM")) {
                    PDB pdb = new PDB(line);
                    if (readResidue != pdb.residueNumber) {
                        readResidue = pdb.residueNumber;
                        residue++;
                    }
                    rname = pdb.residueName;
                    out.println(PDB.getAtomString(atom, pdb.atomName, rname, chain, residue, (float) pdb.x, (float) pdb.y, (float) pdb.z));
                }
                if (line.substring(0, 3).equals("TER")) {
                    out.println(PDB.getTerString(atom, rname, chain, residue));
                    chain++;
                }
                atom++;
            }

            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean belong(List<Integer> list, int x) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > x) {
                return false;
            }
            if (list.get(i) == x) {
                return true;
            }
        }
        return false;
    }

    public static void ter(String filein, String fileout, List<Integer> list) throws Exception {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filein));
            PrintWriter out = new PrintWriter(new FileWriter(fileout));
            String line;
            char chain = 'A';
            int atom = 1;
            int last = -1;
            int cur = 0;
            String resname = "";
            int resnumber = 0;
            while ((line = in.readLine()) != null) {
                PDB pdb = new PDB(line);
                last = cur;
                cur = pdb.getResidueNumber();
                if (cur > last && belong(list, last)) {
                    //  System.err.println(cur+" != "+last+"  "+getTerString(atom,resname,chain,resnumber));
                    out.println(getTerString(atom, resname, chain, resnumber));
                    atom++;
                    chain++;
                }
                resname = pdb.getResidueName();
                resnumber = pdb.getResidueNumber();
                if (line.substring(0, 4).equals("ATOM")) {
                    out.println(getAtomString(atom, pdb.getAtomName(), pdb.getResidueName(), chain, pdb.getResidueNumber(), (float) pdb.getX(), (float) pdb.getY(), (float) pdb.getZ()));
                    atom++;
                }
            }
            out.println(getTerString(atom, resname, chain, resnumber));

            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void sortPDBFile(String filenameA, String filenameB, String tmpDir) throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filenameA));
            PrintWriter out = null;
            String line;
            while ((line = in.readLine()) != null) {
                if (out == null) {
                    int index = new PDB(line).getResidueNumber();
                    list.add(index);
                    out = new PrintWriter(new FileWriter(tmpDir + index + ".pdb"));
                }
                out.println(line);
                if (line.charAt(0) != 'A') {
                    out.close();
                    out = null;
                }
            }
            in.close();

            Collections.sort(list);

            int atom = 1;
            out = new PrintWriter(new FileWriter(filenameB));
            char chain = 'A';
            for (int i = 0; i < list.size(); i++) {
                in = new BufferedReader(new FileReader(tmpDir + list.get(i) + ".pdb"));
                while ((line = in.readLine()) != null) {
                    PDB p = new PDB(line);
                    if (p.getType().equals("ATOM  ")) {
                        out.println(PDB.getAtomString(atom, p.getAtomName(), p.getResidueName(), chain, p.getResidueNumber(), (float) p.getX(), (float) p.getY(), (float) p.getZ()));
                    } else {
                        // System.err.println(line);
                        out.println(PDB.getTerString(atom, p.getResidueName(), chain, p.getResidueNumber()));
                    }
                    atom++;
                }
                in.close();
                chain++;
                /*   File f = new File(list.getInt(i)+".pdb");
                f.delete();*/
            }
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeProteines(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            PrintWriter out = new PrintWriter(new FileWriter(filename.substring(0, filename.lastIndexOf(".pdb")) + "noprot.pdb"));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() > 4) {
                    String h = line.substring(0, 3);
                    if (h.equals("ATO") || h.equals("TER")) {
                        String res = getResidue(line);
                        if (!isProtein(res)) {
                            out.println(line);
                        }
                    }
                }
            }

            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getAtomCode() {

        //there is a priority in the name, H is to check before O and O is to check before P
        if (atomName.indexOf("H") != -1) {
            return 0;
        }
        if (atomName.indexOf("O") != -1) {
            return 3;
        }
        if (atomName.indexOf("N") != -1) {
            return 2;
        }
        if (atomName.indexOf("C") != -1 || atomName.indexOf("M") != -1) {
            return 1;
        }
        if (atomName.indexOf("P") != -1) {
            return 4;
        }
        if (atomName.indexOf("S") != -1) {
            return 5;
        }
        return 0;
    }


    public int getAtomNumber() {
        return atomNumber;
    }

    public String getAtomName() {
        return atomName;
    }

    public int getResidueNumber() {
        return residueNumber;
    }

    public String getResidueName() {
        return residueName;
    }

    public String getChain() {
        return chain;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getB() {
        return b;
    }

    public double getQ() {
        return q;
    }

    public char getAltLoc() {
        return altLoc;
    }

    public PDB() {
    }

    public PDB(String line){
        type = line.substring(0, 6);
        if (type.equals("ATOM  ")) {
            atomNumber = Integer.parseInt(line.substring(6, 11).trim());
            atomName = line.substring(12, 16).trim();
            residueName = line.substring(17, 21).trim();
            chain = line.substring(21, 22).trim();
            residueNumber = Integer.parseInt(line.substring(22, 27).trim());
            x = Double.parseDouble(line.substring(30, 38).trim());
            y = Double.parseDouble(line.substring(38, 46).trim());
            z = Double.parseDouble(line.substring(46, 54).trim());
            altLoc = line.charAt(16);
            if (line.length() > 60) {
                String bS = line.substring(54, 60).replaceAll(" ", "");
                if (bS.equals("")) {
                    b = 0;
                } else {
                    b = Double.parseDouble(bS);
                }
                if (line.length() > 66) {
                    String qS = line.substring(60, 66).replaceAll(" ", "");
                    if (qS.equals("")) {
                        q = 0;
                    } else {
                        q = Double.parseDouble(qS);
                    }
                }
            }
        }
        if (type.equals("TER   ")) {
            atomNumber = Integer.parseInt(line.substring(6, 11).trim());
            residueName = line.substring(17, 20).replaceAll(" ", "");
            chain = line.substring(21, 22).trim();
            residueNumber = Integer.parseInt(line.substring(22, 27).trim());
        }

    }

    public static String getType(String line) {
        return line.substring(0, 6);
    }

    public static String getAtomName(String line) {
        return line.substring(12, 16).replaceAll(" ", "");
    }

    /**
     * formatting atom serial number for pdb
     *
     * @param nb int - the number
     * @return String - length = 5
     */
    static String atomSerialNumber(int nb) throws Exception  {
        String ret = nb + "";
            while (ret.length() < 5) {
                ret = " " + ret;
            }
            if (ret.length() != 5) {
                throw new Exception("inapropriate string length while formatting the text : " + nb);
            }

        return ret;
    }

    /**
     * formatting atom name for pdb
     *
     * @param name String - the atom name
     * @return String - length = 5
     */
    static String atomName(String name) throws Exception  {
        String ret = " " + name.replace('\'', '*').trim();
        char first = ret.charAt(1);
        if (first < '0' || first > '9') {
            ret = " " + ret;
        }
            while (ret.length() < 5) {
                ret = ret + " ";
            }
            if (ret.length() != 5) {
                throw new Exception("inapropriate string length while formatting the text : " + name);
            }

        return ret;
    }

    /**
     * formatting residue name for pdb
     *
     * @param type String - the residue name
     * @return String - length = 3
     */
    static String residueName(String type) throws Exception {
        String ret = type;
            while (ret.length() < 3) {
                ret = " " + ret;
            }
            if (ret.length() != 3) {
                throw new Exception("inapropriate string length while formatting the text : " + type);
            }

        return ret;
    }

    /**
     * formatting residue sequence number for pdb
     *
     * @param num int - the residue number
     * @return String - length = 4
     */
    static String residueSequenceNumber(int num) throws Exception {
        String ret = num + "";
            while (ret.length() < 4) {
                ret = " " + ret;
            }
            if (ret.length() != 4) {
                throw new Exception("inapropriate string length while formatting the text : " + num);
            }

        return ret;
    }

    /**
     * formatting atom coordinates for pdb
     *
     * @param f float - one of the coordinates
     * @return String - length = 8 : 5.3 (xxxx.yyy or -xxx.yyy)
     */
    static String coord(float f) throws Exception {
        String ret = "";
            String fl = f + "";
            if (fl.indexOf("E") != -1) {
                String nb = fl.substring(0, fl.indexOf("E"));
                int mult = Integer.parseInt(fl.substring(fl.indexOf("E") + 1));
                if (mult < -3) {
                    ret = "   0.000";
                } else if (mult > 4) {
                    ret = "9999.999";
                    throw new Exception("in PDB.coord(" + f + ") the number exceeds 9999.999 : " + f);
                }
            } else {
                int p = fl.indexOf(".");
                String head = fl.substring(0, p);
                String queue = fl.substring(p);
                if (queue.length() > 4 && Integer.parseInt(queue.charAt(4) + "") > 4) {
                    if (f > 0) {
                        fl = (f + 0.001f) + "";
                    } else {
                        fl = (f - 0.001f) + "";
                    }
                    head = fl.substring(0, p);
                    queue = fl.substring(p);
                }

                while (queue.length() < 4) {
                    queue += "0";
                }
                while (head.length() < 4) {
                    head = " " + head;
                }
                ret = head + queue.substring(0, 4);
            }
            if (ret.length() != 8) {
                throw new Exception("in PDB.coord(" + f + ") : inapropriate string length while formatting the text ");
            }

        return ret;
    }

    public static String getAtomString(int nb, String name, String type, char chId, int num, double x, double y, double z) throws Exception {
        return getAtomString(nb, name, type, chId, num, (float) x, (float) y, (float) z);
    }

    /**
     * Generates a string matching the .pdb ATOM line format
     *
     * @param nb   int - the atom number
     * @param name String - the name of the atom
     * @param type String - the type of the residue
     * @param chId char - the chain ID
     * @param num  int - the residue number
     * @param x    float - coordinates of the atom (x)
     * @param y    float - coordinates of the atom (y)
     * @param z    float - coordinates of the atom (z)
     * @return String
     */
    public static String getAtomString(int nb, String name, String type, char chId, int num, float x, float y, float z) throws Exception {
        String ret = "";

        String o2 = "      ";
        String o3 = "    ";
        char o4 = ' '; //not modified
        String o5 = "    ";
        char o6 = ' ';
        String o7 = "    ";
        String o8 = "    "; //not modified
        String o9 = "        ";
        String o10 = "        ";
        String o11 = "        ";
        String o12 = "     "; //not modified

        o2 = atomSerialNumber(nb);
        o3 = atomName(name);
        o5 = residueName(type);
        o6 = chId;
        o7 = residueSequenceNumber(num);
        o9 = coord(x);
        o10 = coord(y);
        o11 = coord(z);

        /**
         * The PDB ATOM FORMAT :
         *
         * 00 : ATOM                                                6     05
         * 06 : Atom serial number : integer                   o2   5     11
         * 12 : Atom name                                      o3   4     15
         * 16 : alternate location indicator ' ','A','B'....   o4   1     16
         * 17 : Residue name                                   o5   3     19
         * 20 : space
         * 21 : Chain identifier                               o6   1     20
         * 22 : Residue sequence number                        o7   4     24
         * 26 : code for insertion of residue                  o8   4     25
         * 30 : atom x 5.3                                     o9   8     33
         * 38 : atom y 5.3                                     o10  8     41
         * 46 : atom z 5.3                                     o11  8     49
         * 54 : <reste>
         */

        ret = "ATOM  " + o2 + o3 + o4 + o5 + " " + o6 + o7 + o8 + o9 + o10 + o11;
        return ret;
    }

    /**
     * Generates a string matching the .pdb ATOM line format
     *
     * @param nb   int - the atom number (number of the last know atom + 1)
     * @param type String - the type of the last know residue
     * @param chId char - the ID of the chain which of terminating
     * @param num  int - the number of the mast known residue
     * @return String
     */
    public static String getTerString(int nb, String type, char chId, int num) throws Exception {
        String ret = "";

        String o2 = "      ";
        String o5 = "    ";
        char o6 = ' ';
        String o7 = "    ";
        String o8 = "    "; //not modified

        o2 = atomSerialNumber(nb);
        o5 = residueName(type);
        o6 = chId;
        o7 = residueSequenceNumber(num);

        /**
         * The PDB TER FORMAT :
         * 00 : TER
         * 06 : serial number                                  o2
         * 12 : spaces                                         o12
         * 17 : residue name                                   o5
         * 21 : chain identifier                               o6
         * 22 : residue sequence number                        o7
         * 26 : code for insertion of residue                  o8
         * 30 : <end>
         */
        ret = "TER   " + o2 + "      " + o5 + " " + o6 + o7 + o8;
        return ret;
    }


    public static void printPDBFromHD(String PDBFile, String HDFile) throws Exception {
        int lastA = 0;
        int lastR = 0;
        char lastN = 'A';
        try {
            BufferedReader in = new BufferedReader(new FileReader(HDFile));
            PrintWriter out = new PrintWriter(new FileWriter(PDBFile));

            String line;
            while ((line = in.readLine()) != null) {
                HD hd = new HD(line);
                lastA = hd.atomNumber;
                lastR = hd.residueNumber;
                lastN = hd.residueName;
                out.println(getAtomString(hd.atomNumber, hd.atomName, hd.residueName + "", 'A', hd.residueNumber, hd.x, hd.y, hd.z));
            }
            out.println(PDB.getTerString(lastA + 1, lastN + "", 'A', lastR));
            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
