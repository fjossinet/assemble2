package fr.unistra.ibmc.assemble2.utils;

import java.io.*;

public class HD {
    public int atomNumber;
    public char residueName;
    public int residueNumber;
    public String atomName;
    public int atomCode;
    public float x;
    public float y;
    public float z;
    public float b;
    public float q = 0;

    public HD(int atomNum, char residueType, int residueNum, String atomType, float mx, float my, float mz, float mb) {
        atomNumber = atomNum;
        residueName = residueType;
        residueNumber = residueNum;
        atomName = atomType;
        x = mx;
        y = my;
        z = mz;
        b = mb;
    }

    public HD(int atomNum, char residueType, int residueNum, String atomType, double mx, double my, double mz, double mb) {
        atomNumber = atomNum;
        residueName = residueType;
        residueNumber = residueNum;
        atomName = atomType;
        x = (float) mx;
        y = (float) my;
        z = (float) mz;
        b = (float) mb;
    }

    public HD(String line) {
        //25	read(2,'(7x,a1,i3,a4,i3,3f10.4)',end=85)
        //00000000001111111111222222222233333333334444444444555555555
        //01234567890123456789012345678901234567890123456789012345678
        //   10  A  1C1'   1    2.7701   -5.0388    0.1000    1.0000
        atomNumber = Integer.parseInt(line.substring(0, 6).replaceAll(" ", ""));
        residueName = line.charAt(7);
        residueNumber = Integer.parseInt(line.substring(8, 11).replaceAll(" ", ""));
        atomName = line.substring(11, 15).replaceAll(" ", "");
        atomCode = atomCode(atomName);
        x = Float.parseFloat(line.substring(18, 28).replaceAll(" ", ""));
        y = Float.parseFloat(line.substring(28, 38).replaceAll(" ", ""));
        z = Float.parseFloat(line.substring(38, 48).replaceAll(" ", ""));
        b = Float.parseFloat(line.substring(48, 58).replaceAll(" ", ""));
        if (line.length() < 68) {
            q = 0;
        } else {
            q = Float.parseFloat(line.substring(58, 68).replaceAll(" ", ""));
        }
    }

    /**
     * formatting atom serial number for hb
     *
     * @param nb int - the number
     * @return String - length = 5
     */
    private static String atomSerialNumber(int nb) throws Exception {
        String ret = nb + "";
        while (ret.length() < 5)
            ret = " " + ret;
        if (ret.length() != 5)
            throw new Exception("inapropriate string length while formatting the text");
        return ret;
    }

    /**
     * formatting atom name for hb
     *
     * @param name String - the atom name
     * @return String - length = 5
     */
    private static String atomName(String name) throws Exception {
        String ret = name;
        while (ret.length() < 5)
            ret += " ";
        if (ret.length() != 5)
            throw new Exception("inapropriate string length while formatting the text");

        return ret;
    }

    /**
     * formatting residue sequence number for hb
     *
     * @param num int - the residue number
     * @return String - length = 4
     */
    private static String residueSequenceNumber(int num) throws Exception {
        String ret = num + "";
        while (ret.length() < 3)
            ret = " " + ret;
        if (ret.length() != 3)
            throw new Exception("inapropriate string length while formatting the text");

        return ret;
    }

    /**
     * formatting atom coordinates for hb
     *
     * @param f float - one of the coordinates
     * @return String - length = 10 : 5.4 (xxxxx.yyyy or -xxxx.yyyy)
     */
    private static String coord(float f) throws Exception {
        String ret = "";
            String fl = f + "";

            if (fl.indexOf("E") != -1) {
                String nb = fl.substring(0, fl.indexOf("E"));
                int mult = Integer.parseInt(fl.substring(fl.indexOf("E") + 1));
                if (mult < -3) {
                    ret = "    0.0000";
                } else if (mult > 4) {
                    ret = "99999.9999";
                    throw new Exception("in HD.coord(" + f + ") the number exceeds 99999.9999");
                }
            } else {
                int p = fl.indexOf(".");
                if (p == -1) {
                    throw new Exception("in HD.coord() no '.' in : " + fl);
                }
                String head = fl.substring(0, p);
                String queue = fl.substring(p);
                if (queue.length() > 5 && Integer.parseInt(queue.charAt(5) + "") > 4) {
                    if (f > 0) {
                        fl = (f + 0.0001f) + "";
                    } else {
                        fl = (f - 0.0001f) + "";
                    }
                    head = fl.substring(0, p);
                    queue = fl.substring(p);
                }

                while (queue.length() < 5) {
                    queue += "0";
                }
                while (head.length() < 5) {
                    head = " " + head;
                }
                ret = head + queue.substring(0, 5);
            }
            if (ret.length() != 10) {
                throw new Exception("in HD.coord(" + f + ") : inapropriate string length while formatting the text");
            }

        return ret;
    }

    /**
     * outputs the code of an atom (C, M, N, O, P) -> (1,1,2,3,4)
     *
     * @param s String - the atom name (ex : C3, O3P, O5'....)
     * @return int
     */
    private static int atomCode(String s) {
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case 'C':
                case 'M':
                    return 1;
                case 'N':
                    return 2;
                case 'O':
                    return 3;
                case 'P':
                    return 4;
            }
        }
        return 0;
    }

    /**
     * Generates a string in the .hd line format
     *
     * @param atomNumber    int - the atom number
     * @param residueType   char - the residue type
     * @param residueNumber int - the residue number
     * @param atomType      String - the atom name
     * @param x             float - coordinates of the atom (x)
     * @param y             float - coordinates of the atom (y)
     * @param z             float - coordinates of the atom (z)
     * @param b             float -
     * @param q             float -
     * @return String
     */
    public static String getAtomString(int atomNumber, char residueType, int residueNumber, String atomType, float x, float y, float z, float b, float q) throws Exception {
            String anum, rnum, atyp, mx, my, mz, mb, mq;
            int acod;
            anum = atomSerialNumber(atomNumber);
            rnum = residueSequenceNumber(residueNumber);
            atyp = atomName(atomType);
            acod = atomCode(atomType);

            mx = coord(x);
            my = coord(y);
            mz = coord(z);
            mb = coord(b);
            mq = coord(q);

            return "" + anum + "  " + residueType + rnum + atyp + " " + acod + mx + my + mz + mb + mq;
    }

    /**
     * Generates a string in the .hd line format
     *
     * @param atomNumber    int - the atom number
     * @param residueType   char - the residue type
     * @param residueNumber int - the residue number
     * @param atomType      String - the atom name
     * @param x             double - coordinates of the atom (x)
     * @param y             double - coordinates of the atom (y)
     * @param z             double - coordinates of the atom (z)
     * @param b             double -
     * @param q             double -
     * @return String
     */
    public static String getAtomString(int atomNumber, char residueType, int residueNumber, String atomType, double x, double y, double z, double b, double q) throws Exception {
        return getAtomString(atomNumber, residueType, residueNumber, atomType, (float) x, (float) y, (float) z, (float) b, (float) q);
    }


    public static String getAtomString(int atomNumber, char residueType, int residueNumber, String atomType, int atomCode, double x, double y, double z, double b, double q) throws Exception {
            String anum, rnum, atyp, mx, my, mz, mb, mq;
            anum = atomSerialNumber(atomNumber);
            rnum = residueSequenceNumber(residueNumber);
            atyp = atomName(atomType);

            mx = coord((float) x);
            my = coord((float) y);
            mz = coord((float) z);
            mb = coord((float) b);
            mq = coord((float) q);

            return "" + anum + "  " + residueType + rnum + atyp + " " + atomCode + mx + my + mz + mb + mq;
    }


    public String getAtomString() throws Exception {
            String anum, rnum, atyp, mx, my, mz, mb, mq;
            int acod;
            anum = atomSerialNumber(atomNumber);
            rnum = residueSequenceNumber(residueNumber);
            atyp = atomName(atomName);
            acod = atomCode(atomName);

            mx = coord(x);
            my = coord(y);
            mz = coord(z);
            mb = coord(b);
            mq = coord(q);

            return "" + anum + "  " + residueName + rnum + atyp + " " + acod + mx + my + mz + mb + mq;
    }

    public static void convertPDBasHD(File HDFile, File PDBFile) throws Exception {
            BufferedReader in = new BufferedReader(new FileReader(PDBFile));
            PrintWriter out = new PrintWriter(new FileWriter(HDFile));
            String line;
            //String _03PrimeLine = null; //variable used to export the 03' line just before the P line
            while ((line = in.readLine()) != null)
                if (line.startsWith("ATOM")) {
                    PDB pdb = new PDB(line);
                    out.println(HD.getAtomString(pdb.atomNumber, pdb.residueName.charAt(0), pdb.residueNumber, pdb.atomName.replace('*', '\''), pdb.x, pdb.y, pdb.z, pdb.b, pdb.q));
                }

            in.close();
            out.close();
    }
}
