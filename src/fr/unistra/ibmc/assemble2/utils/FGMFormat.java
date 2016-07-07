package fr.unistra.ibmc.assemble2.utils;

public class FGMFormat {
    private static final int ATOM = 0;
    private static final int CHI = 1;

    private int type;
    private int atomNumber;
    private int residueNumber;
    private String atomName;
    private float x;
    private float y;
    private float z;
    private float chi;

    public FGMFormat(String line) {
        if (line.substring(0, 5).replaceAll(" ", "").length() > 0) {
            atomNumber = Integer.parseInt(line.substring(0, 5).replaceAll(" ", ""));
            residueNumber = Integer.parseInt(line.substring(9, 11).replaceAll(" ", ""));
            atomName = line.substring(11, 15).replaceAll(" ", "");
            x = Float.parseFloat(line.substring(18, 28).replaceAll(" ", ""));
            y = Float.parseFloat(line.substring(28, 38).replaceAll(" ", ""));
            z = Float.parseFloat(line.substring(38, 48).replaceAll(" ", ""));
            type = ATOM;
        } else {
            residueNumber = Integer.parseInt(line.substring(8, 11).replaceAll(" ", ""));
            chi = Float.parseFloat(line.substring(18, 28).replaceAll(" ", ""));
            type = CHI;
        }
    }

    public String getAtomName() {
        return atomName;
    }

    public float getChi() {
        return chi;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    /**
     * formatting atom serial number for fgm
     *
     * @param nb int - the number
     * @return String - length = 5
     */
    private static String atomSerialNumber(int nb) throws Exception {
        String ret = nb + "";
        while (ret.length() < 5) {
            ret = " " + ret;
        }
        if (ret.length() != 5) {
            throw new Exception("inapropriate string length while formatting the text");
        }
        return ret;
    }

    /**
     * formatting atom name for fgm
     *
     * @param name String - the atom name
     * @return String - length = 5
     */
    private static String atomName(String name) throws Exception {
        String ret = name.replaceAll(" ", "");
        while (ret.length() < 5) {
            ret += " ";
        }
        if (ret.length() != 5) {
            throw new Exception("inapropriate string length while formatting the text");
        }
        return ret;
    }

    /**
     * formatting residue sequence number for fgm
     *
     * @param num int - the residue number
     * @return String - length = 4
     */
    private static String residueSequenceNumber(int num) throws Exception {
        String ret = num + "";
        while (ret.length() < 4) {
            ret = " " + ret;
        }
        if (ret.length() != 4) {
            throw new Exception("inapropriate string length while formatting the text");
        }


        return ret;
    }

    /**
     * formatting atom coordinates for fgm
     *
     * @param f float - one of the coordinates
     * @return String - length = 10 : 5.4 (xxxx.yyy or -xxx.yyy)
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
                throw new Exception("in FGM.coord(" + f + ") the number exceeds 99999.9999");
            }
        } else {
            int p = fl.indexOf(".");
            if (p == -1) {
                throw new Exception("FGM.coord(" + fl + ") : no dot in this string");
            }
            p++;
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
            throw new Exception("in FGM.coord(" + f + ") : inapropriate string length while formatting the text");
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
            if (s.charAt(i) == 'C') {
                return 1;
            }
            if (s.charAt(i) == 'M') {
                return 1;
            }
            if (s.charAt(i) == 'N') {
                return 2;
            }
            if (s.charAt(i) == 'O') {
                return 3;
            }
            if (s.charAt(i) == 'P') {
                return 4;
            }
        }
        return 0;
    }

    public static String chi(String ch) {
        String chi = ch + "    ";
        return chi.substring(0, 4);
    }

    public static String getAtomString(int nn, int nres, String atomn, double mx, double my, double mz) throws Exception {
        String atomNumber = FGMFormat.atomSerialNumber(nn);
        String residueNumber = FGMFormat.residueSequenceNumber(nres);
        String atomName = FGMFormat.atomName(atomn);
        int atomCode = FGMFormat.atomCode(atomn);
        String x = FGMFormat.coord((float) mx);
        String y = FGMFormat.coord((float) my);
        String z = FGMFormat.coord((float) mz);
        String ret = "" + atomNumber + "  " + residueNumber + atomName + " " + atomCode + x + y + z;
        return ret;
    }

    public static String getChiString(int nres, String ch, double ator) throws Exception{
        String residueNumber = FGMFormat.residueSequenceNumber(nres);
        String chi = FGMFormat.chi(ch);
        String angle = FGMFormat.coord((float) ator);
        String ret = "       " + residueNumber + chi + "   " + angle;
        return ret;

    }
}
