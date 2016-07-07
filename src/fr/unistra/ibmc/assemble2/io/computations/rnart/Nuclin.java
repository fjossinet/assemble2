package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.utils.HD;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.TBMath;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Java version of nuclin.f 22/08/2005<br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original nuclin.f is at the end of this file<br/>
 * <br/>
 * Written in March-1987<br/>
 * <ul>
 * <li>November-1987 :<br/> up to 999 residues</li>
 * <li>August-October 1988 :<br/> SYMTRY + TORFIX modified</li>
 * <li>May-1989 :<br/> chirals (up to 30 in #11)</li>
 * <li>1990<br/>Introduction of DRUG.DC</li>
 * <li>March 1991<br/>
 * Up to 10000 atoms (BNDS2, NBLIST, TORFIX, SYMTRY).<br/>
 * With 200000 distances (FCVT).<br/>
 * BNDSRT up to 20000</li>
 * <li>August 1992<br/>
 * Possible use of PRENUC.</li>
 * </ul>
 * <p>Title: Fortran To Java</p>
 * <p/>
 * <p>Description: Java version of old Fortran Tools (nahelix, fragment, nuclin,
 * nuclsq...)</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: IBMC/CNRS - Strasbourg</p>
 *
 */
public class Nuclin {
    /**
     * path
     */
    public String tmpPath;
    public String inPath;
    public String outPath;

    /**
     * input
     */
    String nuclindat;
    String abcdat;
    String atomsdat;
    String hbnddat;

    /**
     * output
     */
    String nuclinout;
    String lsqdat;
    String lsqinp;
    String lsqpar;

    /**
     * tmp files
     */
    String scratch;
    String wrentmp;
    String distbin;
    String chirlsbin;
    String plansbin;
    String vdwdstbin;
    String torfixbin;
    String pseubin;
    String symtrybin;
    String chirls2bin;
    String bndtntmp;
    NuclinDc nuclinDc;

    public Nuclin(String tmpPath) throws Exception {
        /**
         * path
         */
        this.tmpPath = tmpPath;
        inPath = tmpPath/*+"original/"*/;
        outPath = tmpPath/*+"outnuclin/"*/;

        /**
         * input
         */
        nuclindat = inPath + "NUCLIN.DAT";
        abcdat = inPath + "ABC.DAT";
        atomsdat = inPath + "ATOMS.DAT";
        hbnddat = inPath + "HBND.DAT";

        /**
         * output
         */
        nuclinout = outPath + "NUCLIN.OUT";
        lsqdat = outPath + "LSQ.DAT";
        lsqinp = outPath + "LSQ.INP";
        lsqpar = outPath + "LSQ.PAR";

        /**
         * tmp files
         */
        scratch = tmpPath + "SCRATCH";
        wrentmp = tmpPath + "WREN.TMP";
        distbin = tmpPath + "DIST.BIN";
        chirlsbin = tmpPath + "CHIRLS.BIN";
        plansbin = tmpPath + "PLANS.BIN";
        vdwdstbin = tmpPath + "VDWDST.BIN";
        torfixbin = tmpPath + "TORFIX.BIN";
        pseubin = tmpPath + "PSEU.BIN";
        symtrybin = tmpPath + "SYMTRY.BIN";
        chirls2bin = tmpPath + "CHIRLS_2.BIN";
        bndtntmp = tmpPath + "BNDTN.TMP";

        nuclinDc = new NuclinDc(this);
        nuclinDc.parseNuclinDc();
        outnuclinout = new PrintWriter(new FileWriter(nuclinout));
        String bid = "Systeme orthogonal";
        String abcS = "1.0,1.0,1.0,90.0,90.0,90.0";
        StringTokenizer tok = new StringTokenizer(abcS, ",");
        abc = new double[6];
        for (int i = 0; i < 6; i++) {
            abc[i] = Double.parseDouble(tok.nextToken());
        }
        innuclindat = new BufferedReader(new FileReader(nuclindat));
        refinm();
        bnds2();

        String line = "";
        while (line.length() < 4) {
            line = innuclindat.readLine();
        }
        String amod = line.substring(0, 4);
        int itemp = 0;
        if (amod.toLowerCase().equals("mode")) {
            itemp = 0;
        }
        if (amod.toLowerCase().equals("xray")) {
            itemp = 1;
        }
        line = innuclindat.readLine();
        tok = new StringTokenizer(line);
        int ip = Integer.parseInt(tok.nextToken());
        int ig = Integer.parseInt(tok.nextToken());
        int it = Integer.parseInt(tok.nextToken());
        int is = Integer.parseInt(tok.nextToken());
        int io = Integer.parseInt(tok.nextToken());

        if (ip == 1) {
            String tmp = "\n Pseudorotation will be restrained ";
            outnuclinout.println(tmp);
        }
        if (ig > 1) {
            String tmp = "\n Sugar chiral volumes will be restrained ";
            outnuclinout.println(tmp);
        }
        if (it == 1) {
            String tmp = "\n Torsion angles will be restrained ";
            outnuclinout.println(tmp);
        }
        if (is == 1) {
            String tmp = "\n Symmetry restraints are on ";
            outnuclinout.println(tmp);
        }
        if (io == 1) {
            String tmp = "\n Fixed but variable occupancies ";
            outnuclinout.println(tmp);
        }
        bndsrt();
        nblist(ioutnb);

        if (ip == 1) {
            pseu();
        }
        if (ig == 1) {
            sugr(ip);
        }
        if (ig == 2) {
            sugar();
        }
        if (it == 1) {
            torfix();
        }
        if (is == 1) {
            symtry();
        }

        double qf1 = 0;
        int nbg1 = 0, ned1 = 0, nbg2 = 0, ned2 = 0;

        if (io == 1) {
            line = innuclindat.readLine();
            tok = new StringTokenizer(line);
            nbg1 = Integer.parseInt(tok.nextToken());
            ned1 = Integer.parseInt(tok.nextToken());
            nbg2 = Integer.parseInt(tok.nextToken());
            ned2 = Integer.parseInt(tok.nextToken());
            qf1 = Double.parseDouble(tok.nextToken());
        }
        fcvt(ip, ig, it, is, io, nbg1, ned1, nbg2, ned2, qf1, itemp, amod);
        outnuclinout.close();
    }

    /**
     * dictionaries and other files
     */

    public String nuclindc = Assemble.getTertiaryDataDirectory().getAbsolutePath() + System.getProperty("file.separator") + "dc" + System.getProperty("file.separator") + "NUCLIN.DC";
    public String restraintdc = Assemble.getTertiaryDataDirectory().getAbsolutePath() + System.getProperty("file.separator") + "dc" + System.getProperty("file.separator") + "RESTRAINT.DC";
    public String restmoddc = Assemble.getTertiaryDataDirectory().getAbsolutePath() + System.getProperty("file.separator") + "dc" + System.getProperty("file.separator") + "RESTMOD.DC";
    public String drugdc = Assemble.getTertiaryDataDirectory().getAbsolutePath() + System.getProperty("file.separator") + "dc2" + System.getProperty("file.separator") + "DRUG.DC";

    BufferedReader inscratch;
    BufferedReader innuclindat;
    BufferedReader inabcdat;
    BufferedReader innuclindc;
    BufferedReader inhbnddat;
    BufferedReader inwrentmp;
    BufferedReader indistbin;
    BufferedReader inlsqdat;
    BufferedReader inchirlsbin;
    BufferedReader inplansbin;
    BufferedReader invdwdstbin;
    BufferedReader intorfixbin;
    BufferedReader inpseubin;
    BufferedReader insymtrybin;

    BufferedReader inchirls2bin;
    BufferedReader inatomsdat;

    BufferedReader inrestraintdc;
    BufferedReader inrestmoddc;
    BufferedReader indrugdc;
    BufferedReader inbndtntmp;

    PrintWriter outnuclinout;

    PrintWriter outscratch;
    PrintWriter outhbnddat;
    PrintWriter outwrentmp;
    PrintWriter outdistbin;
    PrintWriter outlsqdat;
    PrintWriter outchirlsbin;
    PrintWriter outplansbin;
    PrintWriter outvdwdstbin;
    PrintWriter outtorfixbin;
    PrintWriter outpseubin;
    PrintWriter outsymtrybin;
    PrintWriter outlsqpar;
    PrintWriter outchirls2bin;
    PrintWriter outatomsdat;
    PrintWriter outlsqinp;

    PrintWriter outbndtntmp;

    private String blnk = "";

    private double rmx1 = 0, rmx2 = 0, rmx3 = 0, rmx4 = 0, rmx5 = 0;
    private double abc[] = new double[6];

    private int restyp[][] = new int[2][999];
    private String idb[][] = new String[27][10];
    private String ids[] = new String[100];
    private String l1 = "";
    private String l2 = "";
    private String l3 = "";
    private int kss = 0;
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private double xyzr[][] = new double[27][3];
    private int ksr[] = new int[27];
    private String lb2r[] = new String[27];
    private String lb1r[] = new String[27];
    private String lb3r[] = new String[27];
    private double xyzb1[][] = new double[100][3];
    private double xyzb2[][] = new double[100][3];
    private int ksb1[] = new int[100];
    private int ksb2[] = new int[100];
    private String lb1b1[] = new String[100];
    private String lb2b1[] = new String[100];
    private String lb1b2[] = new String[100];
    private String lb2b2[] = new String[100];
    private String lb3b1[] = new String[100];
    private String lb3b2[] = new String[100];
    private int nnb1[] = new int[100];
    private int nnb2[] = new int[100];
    private int rrbnd[][] = new int[2][70];
    private int rbbnd[][][] = new int[2][23][10];
    private int bbbnd[][][] = new int[2][800][11];
    private double rrdst[] = new double[70];
    private double rbdst[][] = new double[23][10];
    private double bbdst[][] = new double[800][11];
    private int nrr = 0;
    private int nrb[] = new int[10];
    private int nbb[] = new int[11];
    private int bbchr[][][] = new int[4][30][11];
    private int rrchr[][] = new int[4][3];
    private int rbchr[][] = new int[4][10];
    private double bbchv[][][] = new double[4][30][11];
    private double rbchv[] = new double[10];
    private double rrchv[] = new double[3];
    private int nch = 0;
    private int ncrr = 0;
    private int ncrb[] = new int[10];
    private int ncbb[] = new int[11];
    private int iplan[][] = new int[60][11];

    private double rr1 = 0;
    private double rr2 = 0;
    private double rr3 = 0;
    private double rr4 = 0;
    private double rr5 = 0;
    private double rr6 = 0;

    private double r1 = 1;
    private double r2 = 0;
    private double r3 = 0;
    private double r4 = 0;
    private double r5 = 1;
    private String idr[] = new String[15];
    private int[] nnr = new int[27];

    private int ioutds = 0;
    private int ioutpl = 0;
    private int ioutnb = 0;
    private int ioutin = 0;

    private int nnn = 0;

    void refinm() throws Exception {
        int n = 0, m = 0, nn = 0, mpl = 0, ires = 0, ires1 = 0, ibs2 = 0, irib2 = 0, ibs1 = 0, irib1 = 0, ibr1 = 0, ibr2 = 0, npl = 0;
        String ll1 = "";
        String ll2 = "";
        double btmp, qocc;

        refins();

            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            outbndtntmp = new PrintWriter(new FileWriter(bndtntmp));
            outplansbin = new PrintWriter(new FileWriter(plansbin));
            outchirlsbin = new PrintWriter(new FileWriter(chirlsbin));

            String tmpr = "Number of distances in sugar : " + nrr;
            outnuclinout.println(tmpr);
            tmpr = "Number of distances in sugar-base : " + nrb[0] + " " + nrb[1] + " " + nrb[2] + " " + nrb[3] + " " + nrb[4] + " " + nrb[5] + " " + nrb[6] + " " + nrb[7] + " " + nrb[8] + " " + nrb[9];
            outnuclinout.println(tmpr);
            tmpr = "Number of distances in bases/ions : " + nbb[0] + " " + nbb[1] + " " + nbb[2] + " " + nbb[3] + " " + nbb[4] + " " + nbb[5] + " " + nbb[6] + " " + nbb[7] + " " + nbb[8] + " " + nbb[9] + " " + nbb[10];
            outnuclinout.println(tmpr);
            tmpr = "Number of chirals in sugar : " + ncrr;
            outnuclinout.println(tmpr);
            tmpr = "Number of chirals in sugar-base : " + ncrb[0] + " " + ncrb[1] + " " + ncrb[2] + " " + ncrb[3] + " " + ncrb[4] + " " + ncrb[5] + " " + ncrb[6] + " " + ncrb[7] + " " + ncrb[8] + " " + ncrb[9];
            outnuclinout.println(tmpr);
            tmpr = "Number of chirals in bases/ions : " + ncbb[0] + " " + ncbb[1] + " " + ncbb[2] + " " + ncbb[3] + " " + ncbb[4] + " " + ncbb[5] + " " + ncbb[6] + " " + ncbb[7] + " " + ncbb[8] + " " + ncbb[9] + " " + ncbb[10];
            outnuclinout.println(tmpr);

            for (int i = 0; i < 27; i++) {
                nnr[i] = 0;
            }
            for (int i = 0; i < 100; i++) {
                nnb1[i] = 0;
                nnb2[i] = 0;
            }
            n = 0;
            m = 0;
            nnn = 1;
            mpl = 0;
            ires = 0;
            ibs2 = restyp[1][0];
            irib2 = restyp[0][0];
            int i = 1;
            boolean doIt = true;

            String line = inatomsdat.readLine();
            HD tmp = new HD(line);
            nn = tmp.atomNumber;
            l1 = tmp.residueName + "";
            l2 = tmp.residueNumber + "";
            l3 = tmp.atomName;
            kss = tmp.atomCode;
            x = tmp.x;
            y = tmp.y;
            z = tmp.z;
            btmp = tmp.b;
            qocc = tmp.q;
            while (true) {
                ires++;
                if (restyp[1][ires - 1] == 0) {
                    break;
                }
                m = 0;
                npl = 0;
                ibs1 = ibs2;
                ibs2 = restyp[1][ires - 1];
                irib1 = irib2;
                irib2 = Math.abs(restyp[0][ires - 1]);
                ibr1 = ibr2;
                ibr2 = ibs2;
                ll1 = l1;
                ll2 = l2;
                tmpr = "   RESIDUE " + ll1 + " " + ll2 + "\n   STARTING WITH ATOM " + nnn + ", BACK BONE TYPE " + irib2 + ", SIDE CHAIN TYPE " + ibs2 + "\n\n";
                outnuclinout.println(tmpr);
                ires1 = ires - 1;
                if (ires1 <= 0) {
                    ires1 = 1;
                }
                if (restyp[1][ires - 1] != 0) {
                    doIt = false;
                }

                while (true) {
                    if (doIt) {
                        line = inatomsdat.readLine();
                        if (line == null) {
                            restyp[1][ires] = 0;
                            restyp[0][ires] = 0;
                            break;
                        }
                            tmp = new HD(line);
                        nn = tmp.atomNumber;
                        l1 = tmp.residueName + "";
                        l2 = tmp.residueNumber + "";
                        l3 = tmp.atomName;
                        kss = tmp.atomCode;
                        x = tmp.x;
                        y = tmp.y;
                        z = tmp.z;
                        btmp = tmp.b;
                        qocc = tmp.q;
                        if (!l1.equals(ll1) || !l2.equals(ll2)) {
                            break;
                        }
                    }
                    doIt = true;
                    atomid(ll1, ll2, irib2, ibs2);
                }
                while (true) {
                    disget(irib1, irib2, ibs1, ibs2);
                    int[] param = {irib1, irib2, ibs1, ibs2, mpl, ires};
                    plnchr(param);
                    irib1 = param[0];
                    irib2 = param[1];
                    ibs1 = param[2];
                    ibs2 = param[3];
                    mpl = param[4];
                    ires = param[5];

                    for (i = 0; i < 14; i++) {
                        for (int j = 0; j < 3; j++) {
                            xyzr[i][j] = xyzr[i + 13][j];
                        }
                        nnr[i] = nnr[i + 13];
                        nnr[i + 13] = 0;
                        ksr[i] = ksr[i + 13];
                        lb1r[i] = lb1r[i + 13];
                        lb2r[i] = lb2r[i + 13];
                        lb3r[i] = lb3r[i + 13];
                    }

                    for (i = 0; i < 100; i++) {
                        for (int j = 0; j < 3; j++) {
                            xyzb1[i][j] = xyzb2[i][j];
                        }
                        nnb1[i] = nnb2[i];
                        nnb2[i] = 0;
                        ksb1[i] = ksb2[i];
                        lb1b1[i] = lb1b2[i];
                        lb2b1[i] = lb2b2[i];
                        lb3b1[i] = lb3b2[i];
                    }

                    if (restyp[0][ires - 1] > 0) {
                        break;
                    }
                    npl = 0;
                    m = 0;
                    ibs1 = ibs2;
                    irib1 = irib2;
                    ibr1 = ibr2;
                    restyp[0][ires - 1] = 1;
                }
            }
            nnn--;
            inatomsdat.close();
            outplansbin.close();
            outchirlsbin.close();
    }

    void refins() throws Exception {
        int iin = 0, ind = 0, idd1 = 0, idd2 = 0, j = 1, iq = 0, nread = 0, i = 0, j1 = 0, j2 = 0, j3 = 0, nbs = 0, nn = 0,
                i1 = 0, i2 = 0, i3 = 0, i4 = 0, nab = 0;
        char atyp[] = new char[999];
        for (int iz = 0; iz < 999; iz++) {
            atyp[iz] = ' ';
        }
        char bcode[] = {'G', 'g', 'A', 'a', 'U', 'u', 'C', 'c', 'T', 't', 'P', 'p', 'D', 'd', 'Y', 'y', 'S', 's', 'I', 'i', 'O', 'o', 'W', 'w', 'N', 'n'};
        String ad1 = "";
        String ad2 = "";
        double d = 0;
        String idp[] = new String[40];
            String title = innuclindat.readLine();
            outnuclinout.println(title);
            String tmpr = "Unit cell parameters are : " + abc[0] + " " + abc[1] + " " + abc[2] + " " + abc[3] + " " + abc[4] + " " + abc[5];
            outnuclinout.println(tmpr);
            String line = innuclindat.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            if (tok.countTokens() == 0) {
                ioutin = 0;
            } else {
                ioutin = Integer.parseInt(tok.nextToken());
            }
            if (tok.countTokens() == 0) {
                ioutds = 0;
            } else {
                ioutds = Integer.parseInt(tok.nextToken());
            }
            if (tok.countTokens() == 0) {
                ioutpl = 0;
            } else {
                ioutpl = Integer.parseInt(tok.nextToken());
            }
            if (tok.countTokens() == 0) {
                ioutnb = 0;
            } else {
                ioutnb = Integer.parseInt(tok.nextToken());
            }
            if (ioutin == 1) {
                tmpr = "Print request for input";
                outnuclinout.println(tmpr);
            }
            if (ioutds == 1) {
                tmpr = "Print request for distances";
                outnuclinout.println(tmpr);
            }
            if (ioutpl == 1) {
                tmpr = "Print request for planes";
                outnuclinout.println(tmpr);
            }
            if (ioutnb == 1) {
                tmpr = "Print request for vdw distances";
                outnuclinout.println(tmpr);
            }
            outnuclinout.println();
            double cosa = Math.cos(abc[3]) * TBMath.DegreeToRadian;
            double cosb = Math.cos(abc[4]) * TBMath.DegreeToRadian;
            double cosc = Math.cos(abc[5]) * TBMath.DegreeToRadian;
            double deta = (1 - cosa * cosa - cosb * cosb - cosc * cosc) + 2 * cosa * cosb * cosc;
            double sins = 1 - cosc * cosc;
            double sing = Math.sqrt(sins);
            rr1 = abc[0] * sing;
            rr2 = abc[2] * (cosb - cosa * cosc) / sing;
            rr3 = abc[0] * cosc;
            rr4 = abc[2] * cosa;
            rr5 = abc[2] * Math.sqrt(deta / sins);
            int kpr = 0;
            int kturn = 0;
            int nresidu = 0;
            char atir = '\\';
            while (((line = innuclindat.readLine()) != null)) {
                if ((line.length() < 1) || (line.charAt(0) == ' ') || (line.charAt(0) == '0')) {
                    break;
                }
                atir = line.charAt(line.length() - 1);
                for (i = 0; i < line.length(); i++) {
                    atyp[i] = line.charAt(i);
                }
                tmpr = line;
                outnuclinout.println(tmpr);
                kturn++;
                for (i = 0; i < 70 && i < line.length(); i++) {
                    if (line.charAt(i) != ' ') {
                        j = nresidu + i + 1;
                        for (int k = 1; k <= 26; k++) {
                            if (line.charAt(i) == bcode[k - 1])//added i < line.length()
                            {
                                if (k <= 4) {
                                    restyp[1][j - 1] = 1;
                                }
                                if (k > 4 && k <= 10) {
                                    restyp[1][j - 1] = 2;
                                }
                                if (k == 11 || k == 12) {
                                    restyp[1][j - 1] = 3;
                                }
                                if (k == 13 || k == 14) {
                                    restyp[1][j - 1] = 4;
                                }
                                if (k == 15 || k == 16) {
                                    restyp[1][j - 1] = 5;
                                }
                                if (k == 17 || k == 18) {
                                    restyp[1][j - 1] = 6;
                                }
                                if (k == 19 || k == 20) {
                                    restyp[1][j - 1] = 7;
                                }
                                if (k == 21 || k == 22) {
                                    restyp[1][j - 1] = 8;
                                }
                                if (k == 23 || k == 24) {
                                    restyp[1][j - 1] = 9;
                                }
                                if (k == 25 || k == 26) {
                                    restyp[1][j - 1] = 11;
                                    nread = 1;
                                }
                                if (k <= 16) {
                                    restyp[0][j - 1] = 1;
                                } else {
                                    restyp[0][j - 1] = 0;
                                    kpr = 1;
                                }
                            }
                        }
                    }
                }
                nresidu = j;

                if (atir == '-') {
                    kturn--;
                } else {
                    if (restyp[0][nresidu - 1] == 1) {
                        restyp[0][nresidu - 1] = -1;
                    }
                }
            }
            outnuclinout.println();

            if (kturn == 1) {
                tmpr = "Single-stranded nucleic acid";
                outnuclinout.println(tmpr);
            }
            if (kturn == 2) {
                if (kpr == 0) {
                    tmpr = "Double-stranded nucleic acid";
                    outnuclinout.println(tmpr);
                } else {
                    tmpr = "Single-stranded nucleic acid with ligand";
                    outnuclinout.println(tmpr);
                }
            }
            if (kturn == 3) {
                if (kpr == 0) {
                    tmpr = "Double-stranded with single-stranded";
                    outnuclinout.println(tmpr);
                } else {
                    tmpr = "Double-stranded nucleic acid with ligand";
                    outnuclinout.println(tmpr);
                }
            }
            if (kturn == 4) {
                if (kpr == 0) {
                    tmpr = "Two double-stranded nucleic acids";
                    outnuclinout.println(tmpr);
                } else {
                    tmpr = "Double-stranded and single-stranded with ligand";
                    outnuclinout.println(tmpr);
                }
            }
            if (kturn == 5) {
                tmpr = "Two double-stranded with ligands";
                outnuclinout.println(tmpr);
            }
            outnuclinout.println();
            if (nread == 1) {
                indrugdc = new BufferedReader(new FileReader(drugdc));
            }
            String[] tmp = (String[]) nuclinDc.names.get(0);
            for (int zzz = 0; zzz < tmp.length; zzz++) {
                idr[zzz] = tmp[zzz];
            }
            for (int zz = 1; zz < nuclinDc.names.size(); zz++) {
                tmp = (String[]) nuclinDc.names.get(zz);
                for (int zzz = 0; zzz < tmp.length; zzz++) {
                    idb[zzz][zz - 1] = tmp[zzz];
                }
            }
            if (nread == 1) {
                for (int ii = 0; ii < 10; ii++) {
                    line = indrugdc.readLine();
                    tok = new StringTokenizer(line);
                    for (i = 0; i < 10; i++) {
                        ids[(10 * ii) + i] = tok.nextToken();
                    }
                }
            }
            for (i = 0; i < 70; i++) {
                rrdst[i] = 0;
            }
            for (j = 0; j < 10; j++) {
                for (i = 0; i < 23; i++) {
                    rbdst[i][j] = 0;
                }
            }
            for (j = 0; j < 11; j++) {
                for (i = 0; i < 800; i++) {
                    bbdst[i][j] = 0;
                }
            }
            int iq1 = 1;
            int jq1 = 1;
            i = 0;
            int iin1 = -99;
            int limit = 0;
            boolean go65 = false;
            while (true) {
                if (!go65) {
                    if (limit >= nuclinDc.distances.size()) {
                        break;
                    }
                    NuclinDcDistance tm = (NuclinDcDistance) nuclinDc.distances.get(limit);
                    limit++;
                    iq = tm.iq;
                    j = tm.j;
                    iin = tm.iin;
                    ind = tm.ind;
                    ad1 = tm.ad1;
                    ad2 = tm.ad2;
                    d = tm.d;
                    idd1 = tm.idd1;
                    idd2 = tm.idd2;
                }

                if (iq == 999 || go65) {
                    if (nread == 1) {
                        //todo read in drug.dc
                    } else {
                        iq = 0;
                    }
                }
                go65 = false;
                if (iq == 0 && j == 0) {
                    break;
                }
                while (true) {
                    boolean bigbreak = false;
                    if (iq == iq1 && j == jq1) // always true after a cycle of loop
                    {
                        switch (iq1) {
                            case 1: {
                                if (iin != iin1) {
                                    i++;
                                    iin1 = iin;
                                    rrbnd[0][i - 1] = iin1;
                                    rrbnd[1][i - 1] = 0;
                                    rrdst[i - 1] = 0;
                                }
                                i++;
                                rrbnd[0][i - 1] = ind;
                                rrbnd[1][i - 1] = idd1;
                                rrdst[i - 1] = d;
                                bigbreak = true;
                            }
                            break;
                            case 2: {
                                if (iin != iin1) {
                                    i++;
                                    iin1 = iin;
                                    rbbnd[0][i - 1][j - 1] = iin1;
                                    rbbnd[1][i - 1][j - 1] = 0;
                                    rbdst[i - 1][j - 1] = 0;
                                }
                                i++;
                                rbbnd[0][i - 1][j - 1] = ind;
                                rbbnd[1][i - 1][j - 1] = idd1;
                                rbdst[i - 1][j - 1] = d;
                                bigbreak = true;
                            }
                            break;
                            case 4: {
                                j += 5;
                            }
                            case 3: {
                                if (iin != iin1) {
                                    i++;
                                    iin1 = iin;
                                    bbbnd[0][i - 1][j - 1] = iin1;
                                    bbbnd[1][i - 1][j - 1] = 0;
                                    bbdst[i - 1][j - 1] = 0;
                                }
                                i++;
                                bbbnd[0][i - 1][j - 1] = ind;
                                bbbnd[1][i - 1][j - 1] = idd1;
                                bbdst[i - 1][j - 1] = d;
                                bigbreak = true;
                            }
                            break;
                            case 5: {
                                if (iin != iin1) {
                                    i++;
                                    iin1 = iin;
                                    bbbnd[0][i - 1][j - 1] = iin1;
                                    bbbnd[1][i - 1][j - 1] = 0;
                                    bbdst[i - 1][j - 1] = 0;
                                }
                                i++;
                                bbbnd[0][i - 1][j - 1] = ind;
                                bbbnd[1][i - 1][j - 1] = idd1;
                                bbdst[i - 1][j - 1] = d;
                                bigbreak = true;
                                go65 = true;
                            }
                            break;
                        }
                        if (bigbreak) {
                            break;
                        }
                    }
                    i = 0;
                    iin1 = -99;
                    iq1 = iq;
                    jq1 = j;
                }
            }
            ncrr = 0;
            for (j = 0; j < 3; j++) {
                NuclinDcChiral tm = (NuclinDcChiral) nuclinDc.chirals.get(j);
                rrchr[0][j] = tm.r1;
                rrchr[1][j] = tm.r2;
                rrchr[2][j] = tm.r3;
                rrchr[3][j] = tm.r4;
                rrchv[j] = tm.v;

                if (rrchr[0][j] != 0) {
                    ncrr++;
                }
            }

            for (i = 0; i < 10; i++) {
                ncrb[i] = 0;
                NuclinDcChiral tm = (NuclinDcChiral) nuclinDc.chirals.get(i + 3);
                rbchr[0][i] = tm.r1;
                rbchr[1][i] = tm.r2;
                rbchr[2][i] = tm.r3;
                rbchr[3][i] = tm.r4;
                rbchv[i] = tm.v;
                if (rbchr[0][i] != 0) {
                    ncrb[i]++;
                }
            }
            for (i = 0; i < 10; i++) {
                ncbb[i] = 0;
                for (j = 0; j < 30; j++) {
                    bbchv[0][j][i] = 0;
                    for (int k = 0; k < 4; k++) {
                        bbchr[k][j][i] = 0;
                    }
                }
            }
            /** lecture des 10 planes */
            for (i = 0; i < 10; i++) {
                int[] tm = (int[]) nuclinDc.planes.get(i);
                for (int k = 0; k < 40; k++) {
                    iplan[k][i] = tm[k];
                }
            }

            if (nread == 1) {
                i = 10;
                ncbb[i] = 0;
                for (j = 0; j < 145; j++) {
                    line = indrugdc.readLine();
                    tok = new StringTokenizer(line);
                    for (int k = 0; k < 4; k++) {
                        bbchr[k][j][i] = Integer.parseInt(tok.nextToken());
                    }
                    bbchv[0][j][i] = Double.parseDouble(tok.nextToken());
                    if (bbchr[0][j][i] != 0) {
                        ncbb[i]++;
                    }
                }
                line = indrugdc.readLine();
                tok = new StringTokenizer(line);
                for (int k = 0; k < 60; k++) {
                    iplan[k][i] = Integer.parseInt(tok.nextToken());
                }
                indrugdc.close();
            }
        j = 1;
        nrr = 0;
        if (ioutin != 0) {
            tmpr = "????"; //todo find the real error message
            outnuclinout.println(tmpr);
        }
        for (i = 1; i <= 70; i++) {
            if (rrbnd[0][i - 1] == 0) {
                break;
            }
            if (rrbnd[1][i - 1] <= 0) {
                j1 = rrbnd[0][i - 1];
                j3 = i;
                if (j1 > 13) {
                    j1 = j1 - 13;
                }
                nrr++;
            } else {
                j2 = rrbnd[0][i - 1];
                if (j2 > 14) {
                    j2 = j2 - 13;
                }
                nrr++;
                if (ioutin != 0) {
                    tmpr = idr[j1 - 1] + " " + idr[j2 - 1] + " " + rrdst[i - 1] + " " + rrbnd[1][i - 1] + " " + j1 + " " + j2 + " " + j + " " + i;
                    outnuclinout.println(tmpr);
                }
            }
        }
        i = 1;
        nn = ncrr;
        if (nn != 0) {
            for (j = 1; j <= nn; j++) {
                i1 = rrchr[0][j - 1];
                i2 = rrchr[1][j - 1];
                i3 = rrchr[2][j - 1];
                i4 = rrchr[3][j - 1];
                if (ioutin != 0) {
                   tmpr = "chiral center " + idr[i1 - 1] + " " + idr[i2 - 1] + " " + idr[i3 - 1] + " -- " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + rrchv[j - 1];
                    outnuclinout.println(tmpr);
                }
            }
        }
        for (nbs = 1; nbs <= 10; nbs++) {
            j = 1;
            nrb[nbs - 1] = 0;
            if (ioutin != 0) {
                tmpr = "?????"; //todo find the real error message
                outnuclinout.println(tmpr);
            }
            for (i = 1; i <= 23; i++) {
                if (rbbnd[0][i - 1][nbs - 1] == 0) {
                    break;
                }
                if (rbbnd[1][i - 1][nbs - 1] <= 0) {
                    j1 = rbbnd[0][i - 1][nbs - 1];
                    j3 = i;
                    nrb[nbs - 1]++;
                } else {
                    j2 = rbbnd[0][i - 1][nbs - 1];
                    nrb[nbs - 1]++;
                    if (ioutin != 0) {
                        tmpr = idr[j1 - 1] + " " + idb[j2 - 1][nbs - 1] + " " + rbdst[0][nbs - 1] + " " + rbbnd[1][i - 1][nbs - 1] + " " + j1 + " " + j2 + " " + nbs + " " + i;
                        outnuclinout.println(tmpr);
                    }
                }
            }
            i = nbs;
            nn = ncrb[i - 1];
            if (nn != 0) {
                for (j = 1; j <= nn; j++) {
                    i1 = rbchr[0][j - 1];
                    i2 = rbchr[1][j - 1];
                    i3 = rbchr[2][j - 1];
                    i4 = rbchr[3][j - 1];
                    if (ioutin != 0) {
                        tmpr = idr[i1 - 1] + " " + idr[i2 - 1] + " " + idr[i3 - 1] + " " + idb[i4 - 1][i - 1] + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + rbchv[j - 1];
                        outnuclinout.println(tmpr);
                    }
                }
            }
            if (ioutin != 0) {
                tmpr = "???"; //todo find the real error message
                outnuclinout.println(tmpr);
            }
            nab = 150;
            nbb[nbs - 1] = 0;
            for (i = 1; i <= nab; i++) {
                if (bbbnd[0][i - 1][nbs - 1] == 0) {
                    break;
                }
                if (bbbnd[1][i - 1][nbs - 1] <= 0) {
                    j1 = bbbnd[0][i - 1][nbs - 1];
                    nbb[nbs - 1]++;
                    j3 = i;
                } else {
                    j2 = bbbnd[0][i - 1][nbs - 1];
                    nbb[nbs - 1]++;
                    if (ioutin != 0) {
                        tmpr = idb[j1 - 1][nbs - 1] + " " + idb[j2 - 1][nbs - 1] + " " + bbdst[i - 1][nbs - 1] + " " + bbbnd[1][i - 1][nbs - 1] + " " + j1 + " " + j2 + " " + nbs + " " + i;
                        outnuclinout.println(tmpr);
                    }
                }
            }
            i = nbs;
            nn = ncbb[i - 1];
            if (nn != 0) {
                for (j = 1; j <= nn; j++) {
                    i1 = bbchr[0][j - 1][i - 1];
                    i2 = bbchr[1][j - 1][i - 1];
                    i3 = bbchr[2][j - 1][i - 1];
                    i4 = bbchr[3][j - 1][i - 1];
                    if (ioutin != 0) {
                        tmpr = idb[i1 - 1][i - 1] + " " + idb[i2 - 1][i - 1] + " " + idb[i3 - 1][i - 1] + " " + idb[i4 - 1][i - 1] + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + bbchv[j - 1][i - 1];
                        outnuclinout.println(tmpr);
                    }
                }
            }
            for (j = 1; j <= 40; j++) {
                idp[j - 1] = blnk;
                if (iplan[j - 1][i - 1] != 0) {
                    int k = iplan[j - 1][i - 1];
                    if (k < 0) {
                        idp[j - 1] = idr[Math.abs(k)];
                    }
                    if (k < 0) {
                        idp[j - 1] = idb[j - 1][i - 1];
                    }
                }
            }
            if (ioutin != 0) {
                String stmp = idp[0] + "";
                for (int zz = 1; zz < 20; zz++) {
                    stmp += " " + idp[zz];
                }
                tmpr = "planar atoms " + stmp;
                outnuclinout.println(tmpr);
            }
        }
        if (ioutin != 0) {
            tmpr = "???"; //todo find the real error message
            outnuclinout.println(tmpr);
        }
        nbs = 11;
        nbb[nbs - 1] = 0;
        for (i = 1; i <= 800; i++) {
            if (bbbnd[0][i - 1][nbs - 1] == 0) {
                break;
            }
            if (bbbnd[1][i - 1][nbs - 1] <= 0) {
                j1 = bbbnd[0][i - 1][nbs - 1];
                nbb[nbs - 1]++;
                j3 = i;
            } else {
                nbb[nbs - 1]++;
                if (ioutin != 0) {
                    tmpr = ids[j1 - 1] + " " + ids[j2 - 1] + " " + bbdst[i - 1][nbs - 1] + " " + bbbnd[1][i - 1][nbs - 1] + " " + j1 + " " + j2 + " " + nbs + " " + i;
                    outnuclinout.println(tmpr);
                }
            }
        }
        i = nbs;
        nn = ncbb[nbs - 1];
        if (nn != 0) {
            for (j = 1; j <= nn; j++) {
                i1 = bbchr[0][j - 1][i - 1];
                i2 = bbchr[1][j - 1][i - 1];
                i3 = bbchr[2][j - 1][i - 1];
                i4 = bbchr[3][j - 1][i - 1];
                if (ioutin != 0) {
                    tmpr = ids[i1 - 1] + " " + ids[i2 - 1] + " " + ids[i3 - 1] + " " + ids[i4 - 1] + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + bbchv[j - 1][i - 1];
                    outnuclinout.println(tmpr);
                }
            }
        }
        for (j = 1; j <= 40; j++) {
            idp[j - 1] = blnk;
            if (iplan[j - 1][i - 1] != 0) {
                int k = iplan[j - 1][i - 1];
                if (k < 0) {
                    idp[j - 1] = idr[Math.abs(k)];
                }
                if (k > 0) {
                    idp[j - 1] = ids[j - 1];
                }
            }
        }
        if (ioutin != 0) {
            String stmp = idp[0] + "";
            for (int zz = 1; zz < idp.length; zz++) {
                stmp += " " + idp[zz];
            }
            outnuclinout.println(stmp);
        }
    }

    double[] ortho(double[] pt) {
        double[] ret = new double[3];
        ret[1] = abc[1] * pt[1] + r3 * pt[0] + r4 * pt[2];
        ret[0] = r1 * pt[0] + r2 * pt[2];
        ret[2] = r5 * pt[2];
        return ret;
    }

    double[] deorth(double[] pt) {
        double[] ret = new double[3];
        ret[2] = pt[2] / r5;
        ret[0] = (pt[0] - r2 * ret[2]) / r1;
        ret[1] = (pt[1] - r3 * ret[0] - r4 * ret[2]) / abc[1];
        return ret;
    }

    void atomid(String ll1, String ll2, int irib2, int ibs2) throws Exception {
        int i;
        if (irib2 != 0) {
            boolean doIt = false;
            for (i = 0; i < 14; i++) {
                if (l3.equals(idr[i])) {
                    doIt = true;
                    break;
                }
            }
            if (doIt) {
                int i13 = i + 13;
                double[] tmp = {x, y, z};
                xyzr[i13] = ortho(tmp);
                lb1r[i13] = l1;
                lb2r[i13] = l2;
                lb3r[i13] = l3;
                nnr[i13] = nnn;
                nnn++;
                return;
            }
            doIt = true;

            for (i = 0; i < 27; i++) {
                if (l3.equals(idb[i][ibs2 - 1])) {
                    doIt = false;
                    break;
                }
            }
            if (doIt) {
                String tmpr = "no match for " + l1 + " " + l2 + " " + l3;
                outnuclinout.println(tmpr);
                throw new Exception("Refinement aborted. The refinement algorithm cannot handle atom "+l3+" in a "+l1+". You should remove it from the original file.");
            }
            double tmp[] = {x, y, z};
            xyzb2[i] = ortho(tmp);
            ksb2[i] = kss;
            lb1b2[i] = l1;
            lb2b2[i] = l2;
            lb3b2[i] = idb[i][ibs2 - 1];
            nnb2[i] = nnn;
            nnn++;
            return;
        }

        int ibs = ibs2;
        if (ibs != 11) {
            boolean doIt = true;
            for (i = 1; i <= 27; i++) {
                if (l3.equals(idb[i - 1][ibs - 1])) {
                    doIt = false;
                    break;
                }
            }
            if (doIt) {
                String tmpr = l1 + " " + l2 + " " + l3;
                outnuclinout.println(tmpr);
            } else {
                double tmp[] = {x, y, z};
                xyzb2[i - 1] = ortho(tmp);
                ksb2[i - 1] = kss;
                lb1b1[i - 1] = l1;
                lb2b2[i - 1] = l2;
                lb3b2[i - 1] = idb[i - 1][ibs - 1];
                nnb2[i - 1] = nnn;
                nnn++;
            }
            return;
        }
        boolean doIt = true;
        for (i = 1; i <= 100; i++) {
            if (l3.equals(ids[i - 1])) {
                doIt = false;
                break;
            }
        }
        if (doIt) {
            String tmpr = l1 + " " + l2 + " " + l3;
            outnuclinout.println(tmpr);

        } else {
            double[] tmp = {x, y, z};
            ksb2[i - 1] = kss;
            lb1b2[i - 1] = l1;
            lb2b2[i - 1] = l2;
            lb3b2[i - 1] = ids[i - 1];
            nnb2[i - 1] = nnn;
            nnn++;
        }
    }

    void disget(int irib1, int irib2, int ibs1, int ibs2) {
        int n = 0, j1 = 0, j2 = 0;

        int ibr1 = 0;
        int m = 0;
        if (irib2 != 0) {
            int mrr = nrr;
            for (int i = 1; i <= mrr; i++) {
                if (rrbnd[1][i - 1] <= 0) {
                    j1 = rrbnd[0][i - 1];
                    continue;
                }
                j2 = rrbnd[0][i - 1];
                if ((nnr[j1 - 1] == 0) || (nnr[j2 - 1]) == 0) {
                    continue;
                }
                double dst = 0;
                for (int j = 0; j < 3; j++) {
                    dst += (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]) * (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]);
                }
                dst = Math.sqrt(dst);
                n++;
                String outLine = nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + rrdst[i - 1] + " " + rrbnd[1][i - 1];
                outbndtntmp.println(outLine);
                if (ioutds != 0) {
                    String tmpr = n + " " + nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + lb1r[j1 - 1] + lb2r[j1 - 1] + " " + lb3r[j1 - 1] + " " + lb1r[j2 - 1] + " " + lb2r[j2 - 1] + " " + lb3r[j2 - 1] + " " + rrdst[i - 1] + " " + dst + " " + rrbnd[1][i - 1];
                    outnuclinout.println(tmpr);
                }
            }

            ibr1 = ibs1;
            int mrb = nrb[ibr1 - 1];

            for (int i = 0; i < mrb; i++) {
                if (rbbnd[1][i][ibr1 - 1] <= 0) {
                    j1 = rbbnd[0][i][ibr1 - 1];
                    continue;
                }
                j2 = rbbnd[0][i][ibr1 - 1];
                if ((nnr[j1 - 1] == 0) || (nnb1[j2 - 1] == 0)) {
                    continue;
                }
                double dst = 0;
                for (int j = 0; j < 3; j++) {
                    dst += (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]) * (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]);
                }
                n++;
                String outLine = nnr[j1 - 1] + " " + nnb1[j2 - 1] + " " + rbdst[i][ibr1 - 1] + " " + rbbnd[1][i][ibr1 - 1];
                outbndtntmp.println(outLine);
                if (ioutds != 0) {
                    String tmpr = n + " " + nnr[j1 - 1] + " " + nnb1[j2 - 1] + " " + lb1r[j1 - 1] + lb2r[j1 - 1] + " " + lb3r[j1 - 1] + " - " + lb1b1[j2 - 1] + " " + lb2b1[j2 - 1] + " " + lb3b1[j2 - 1] + " " + rbdst[i][ibr1 - 1] + " " + dst + " " + rbbnd[1][i][ibr1 - 1];
                    outnuclinout.println(tmpr);
                }
            }
        }

        int ibss1 = ibs1;
        if (ibss1 == 0) {
            ibss1++;
        }
        int mbb = nbb[ibss1 - 1];
        for (int i = 0; i < mbb; i++) {
            if (bbbnd[1][i][ibss1 - 1] <= 0) {
                j1 = bbbnd[0][i][ibss1 - 1];
                continue;
            }
            j2 = bbbnd[0][i][ibss1 - 1];
            if ((nnb1[j1 - 1] == 0) || (nnb1[j2 - 1] == 0)) {
                continue;
            }
            double dst = 0;
            for (int j = 0; j < 3; j++) {
                dst += (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]) * (xyzr[j1 - 1][j] - xyzr[j2 - 1][j]);
            }
            dst = Math.sqrt(dst);
            n++;
            String outLine = nnb1[j1 - 1] + " " + nnb1[j2 - 1] + " " + bbdst[i][ibss1 - 1] + " " + bbbnd[1][i][ibss1 - 1];
            outbndtntmp.println(outLine);
            if (ioutds != 0) {
                String tmpr = n + " " + nnr[j1 - 1] + " " + nnb1[j2 - 1] + " " + lb1r[j1 - 1] + lb2r[j1 - 1] + " " + lb3r[j1 - 1] + " - " + lb1b1[j2 - 1] + " " + lb2b1[j2 - 1] + " " + lb3b1[j2 - 1] + " " + rbdst[i][ibr1 - 1] + " " + dst + " " + rbbnd[1][i][ibr1 - 1];
                outnuclinout.println(tmpr);
            }
        }
    }

    void plnchr(int[] param) {
        int irib1 = param[0];
        int irib2 = param[1];
        int ibs1 = param[2];
        int ibs2 = param[3];
        int mpl = param[4];
        int ires = param[5];
        int[] iin = new int[100];
        int npl = 0;
        int jp1 = 1;
        int jp2 = 59;
        int na = 0;
        if (ibs1 > 0) {
            l510:
            for (int i = jp1; i <= jp2; i++) {
                if (iplan[i - 1][ibs1 - 1] != 0) {
                    if (iplan[i - 1][ibs1 - 1] > 0) {
                        int ip1 = iplan[i - 1][ibs1 - 1];
                        if (nnb1[ip1 - 1] == 0) {
                            continue;
                        }
                        na++;
                        iin[na - 1] = nnb1[ip1 - 1];
                    } else {
                        int ip1 = iplan[i - 1][ibs1 - 1];
                        na++;
                        iin[na - 1] = nnr[-(ip1 + 1)];
                    }
                    int jp3 = i + 1;
                    l520:
                    for (int i1 = jp3; i1 < 60; i1++) {
                        int ip2 = iplan[i1 - 1][ibs1 - 1];
                        if (ip2 == 0) {
                            continue l510;
                        }
                        if (nnb1[ip2 - 1] == 0) {
                            continue l520;
                        }
                        npl++;
                    }
                    continue l510;
                }
                if (npl == 0) {
                    continue l510;
                }
                mpl++;
                if (ioutpl != 0) {
                    outnuclinout.println(" Planar atoms of " + lb1b1[0] + " " + lb2b1[0]);
                    String tmp = "";
                    for (int j = 0; j < na; j++) {
                        tmp += " " + iin[j];
                    }
                    outnuclinout.println(tmp);
                }
                String tmpr = mpl + " " + na;
                for (int j = 0; j < na; j++) {
                    tmpr += " " + iin[j];
                }
                outplansbin.println(tmpr);
                npl = 0;
                na = 0;
            }
        }
        if (irib2 != 0) {
            irib1 = 1;
            int mrr = ncrr;
            if (mrr != 0) {
                for (int i = 1; i <= mrr; i++) {
                    int j1 = rrchr[0][i - 1];
                    int j2 = rrchr[1][i - 1];
                    int j3 = rrchr[2][i - 1];
                    int j4 = rrchr[3][i - 1];
                    if (nnr[j1 - 1] == 0 || nnr[j2 - 1] == 0 || nnr[j3 - 1] == 0 || nnr[j4 - 1] == 0) {
                        continue;
                    }
                    nch++;
                    if (ioutpl != 0) {
                        outnuclinout.println("Chiral centers of " + lb1b1[0] + " " + lb2b1[0] + " " + nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + nnr[j3 - 1] + " " + nnr[j4 - 1] + " " + rrchv[i - 1]);
                    }
                    String tmpr = nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + nnr[j3 - 1] + " " + nnr[j4 - 1] + " " + rrchv[i - 1];
                    outchirlsbin.println(tmpr);
                }
            }
            int ibr1 = ibs1;
            int mrb = ncrb[ibr1];
            if (mrb != 0) {
                for (int i = 1; i <= mrb; i++) {
                    int j1 = rbchr[0][i - 1];
                    int j2 = rbchr[1][i - 1];
                    int j3 = rbchr[2][i - 1];
                    int j4 = rbchr[3][i - 1];
                    if (nnr[j1 - 1] == 0 || nnb1[j4 - 1] == 0 || nnr[j2 - 1] == 0 || nnr[j3 - 1] == 0) {
                        continue;
                    }
                    nch++;
                    if (ioutpl != 0) {
                        outnuclinout.println("chiral centers of " + lb1b1[0] + " " + lb2b1[0] + " " + nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + nnr[j3 - 1] + " " + nnb1[j4 - 1] + " " + rbchv[i - 1]);
                    }
                    String tmpr = nnr[j1 - 1] + " " + nnr[j2 - 1] + " " + nnr[j3 - 1] + " " + nnb1[j4 - 1] + " " + rbchv[i - 1];
                    outchirlsbin.println(tmpr);
                }
            }
        }
        int ibss1 = ibs1;
        int mbb = ncbb[ibss1 - 1];
        if (mbb != 0) {
            for (int i = 1; i <= mbb; i++) {
                int j1 = bbchr[0][i - 1][ibss1 - 1];
                int j2 = bbchr[1][i - 1][ibss1 - 1];
                int j3 = bbchr[2][i - 1][ibss1 - 1];
                int j4 = bbchr[3][i - 1][ibss1 - 1];
                if (nnb1[j1 - 1] == 0 || nnb1[j2 - 1] == 0 || nnb1[j3 - 1] == 0 || nnb1[j4 - 1] == 0) {
                    continue;
                }
                nch++;
                if (ioutpl != 0) {
                    outnuclinout.println("chiral centers of " + lb1b1[0] + " " + lb2b1[0] + " " + nnb1[j1 - 1] + " " + nnb1[j2 - 1] + " " + nnb1[j3 - 1] + " " + nnb1[j4 - 1] + " " + bbchv[i - 1][ibss1 - 1]);
                }
                String tmpr = nnb1[j1 - 1] + " " + nnb1[j2 - 1] + " " + nnb1[j3 - 1] + " " + nnb1[j4 - 1] + " " + bbchv[i - 1][ibss1 - 1];
                outchirlsbin.println(tmpr);
            }
        }
        param[0] = irib1;
        param[1] = irib2;
        param[2] = ibs1;
        param[3] = ibs2;
        param[4] = mpl;
        param[5] = ires;
    }

    void bnds2() throws Exception {
        String[] id = new String[10000];
        double[][] xyz = new double[10000][3];
        int[][] ires = new int[2][999];
        String[] idres = new String[999];
        String[][] ib = new String[2][250];
        for (int i = 0; i < 250; i++) {
            ib[0][i] = ib[1][i] = " ";
        }
        int[][] ir = new int[2][250];
        double d = 0;
        float dd = 0;
        int m = 0;
        String id1 = "";
        String id2 = "";
        String[] jd = new String[2];
        int ir1 = 0;
        int ir2 = 0;
            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            int l0 = 0;
            int i = 0;
            String line;
            while ((line = inatomsdat.readLine()) != null) {
                HD hd = new HD(line);
                int nn = hd.atomNumber;
                l1 = hd.residueName + "";
                int l2 = hd.residueNumber;
                String ida = hd.atomName;
                kss = hd.atomCode;
                x = hd.x;
                y = hd.y;
                z = hd.z;
                double bt = hd.b;
                double qocc = hd.q;
                while (l2 != l0) {
                    l2 = Math.abs(l2);
                    if (l0 > 0) {
                        ires[1][l0 - 1] = i;
                    }
                    l0 = l2;
                    idres[l0 - 1] = l1;
                    ires[0][l0 - 1] = i + 1;
                }
                i++;
                id[i - 1] = ida;
                double tmp[] = {x, y, z};
                xyz[i - 1] = ortho(tmp);
            }
            inatomsdat.close();
            boolean go219 = false;
            ires[1][l0 - 1] = i;
            outnuclinout.println();
            String tmpr = "Number of atoms and number of residues : " + i + " " + l0;
            outnuclinout.println(tmpr);
            tmpr = "List of hydrogen bonds restrained : ";
            outnuclinout.println(tmpr);
            double j11 = 0;
            int j1 = 1;
            int j2 = 5;
            int jend = 0;
            line = innuclindat.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            int jcode = Integer.parseInt(tok.nextToken());
            if (jcode == 0) {
                go219 = true;
            } else {
                outscratch = new PrintWriter(new FileWriter(scratch));
                while (true) {
                    line = innuclindat.readLine();
                    tok = new StringTokenizer(line);
                    for (int j = j1; j <= j2; j++) {
                        if (tok.countTokens() > 3) {
                            ib[0][j - 1] = tok.nextToken();
                            ir[0][j - 1] = Integer.parseInt(tok.nextToken());
                            ib[1][j - 1] = tok.nextToken();
                            ir[1][j - 1] = Integer.parseInt(tok.nextToken());
                        } else {
                            ib[0][j - 1] = " ";
                        }
                    }

                    boolean go530 = false;
                    for (int j = j1; j <= j2; j++) {
                        if (ib[0][j - 1].equals(" ")) {
                            jend = j - 1;
                            go530 = true;
                            break;
                        }
                    }
                    if (!go530) {
                        j1 += 5;
                        j2 += 5;
                    } else {
                        break;
                    }
                }
                d = 2.9;
                dd = 0;
                int k1 = 1;
                int k2 = 2;
                for (int k = 1; k <= 2; k++) {
                    for (int j = 1; j <= jend; j++) {
                        if (ib[k1 - 1][j - 1].charAt(0) == 'G' || ib[k1 - 1][j - 1].charAt(0) == 'g') {
                            if (ib[k2 - 1][j - 1].charAt(0) == 'C' || ib[k2 - 1][j - 1].charAt(0) == 'c') {
                                ir1 = ir[k1 - 1][j - 1];
                                ir2 = ir[k2 - 1][j - 1];
                                jd[k1 - 1] = "O6";
                                jd[k2 - 1] = "N4";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "N3";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N2";
                                jd[k2 - 1] = "O2";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "C4";
                                dd = 3.75f;
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                                jd[k2 - 1] = "C2";
                                dd = 3.77f;
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                            }
                            if (ib[k2 - 1][j - 1].charAt(0) == 'U' || ib[k2 - 1][j - 1].charAt(0) == 'u' || ib[k2 - 1][j - 1].charAt(0) == 'T' || ib[k2 - 1][j - 1].charAt(0) == 't') {
                                ir1 = ir[k1 - 1][j - 1];
                                ir2 = ir[k2 - 1][j - 1];
                                jd[k1 - 1] = "O6";
                                jd[k2 - 1] = "N3";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "O2";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "C2";
                                dd = 3.67f;
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "N3";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                            }
                        }
                        if (ib[k1 - 1][j - 1].charAt(0) == 'A' || ib[k1 - 1][j - 1].charAt(0) == 'a') {
                            if (ib[k2 - 1][j - 1].charAt(0) == 'U' || ib[k2 - 1][j - 1].charAt(0) == 'u' || ib[k2 - 1][j - 1].charAt(0) == 'T' || ib[k2 - 1][j - 1].charAt(0) == 't') {
                                ir1 = ir[k1 - 1][j - 1];
                                ir2 = ir[k2 - 1][j - 1];
                                jd[k1 - 1] = "N6";
                                jd[k2 - 1] = "O4";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N1";
                                jd[k2 - 1] = "N3";
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], d, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "N6";
                                jd[k2 - 1] = "C4";
                                dd = 3.7f;
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                                jd[k1 - 1] = "C6";
                                jd[k2 - 1] = "O4";
                                dd = 3.75f;
                                tmpr = scratch(ir1, jd[k1 - 1], ir2, jd[k2 - 1], dd, jcode);
                                outscratch.println(tmpr);
                            }
                        }
                    }
                    k1 = 2;
                    k2 = 1;
                }
                outscratch.close();
                inhbnddat = new BufferedReader(new FileReader(scratch));
            }
            while (true) {
                boolean go222 = false;
                boolean bigbreak = false;
                if (!go219) {

                    line = inhbnddat.readLine();
                    if (line != null) {
                        tok = new StringTokenizer(line);
                        String tt = tok.nextToken();
                        int pos = TBMath.endOfInt(tt);
                        ir1 = Integer.parseInt(tt.substring(0, pos + 1));
                        id1 = tt.substring(pos + 1);
                        tt = tok.nextToken();
                        pos = TBMath.endOfInt(tt);
                        ir2 = Integer.parseInt(tt.substring(0, pos + 1));
                        id2 = tt.substring(pos + 1);
                        d = Double.parseDouble(tok.nextToken());
                        j1 = Integer.parseInt(tok.nextToken());
                        go222 = true;
                    }
                    if (!go222) {
                        jcode = 999;
                    }
                }
                if (!go222 && !go219) {
                    inhbnddat.close();
                    inhbnddat = new BufferedReader(new FileReader(hbnddat));
                }
                while (true) {
                    if (!go222) {
                        line = inhbnddat.readLine();
                        if (line == null) {
                            bigbreak = true;
                            break;
                        }
                        ir1 = Integer.parseInt(line.substring(4, 7).replaceAll(" ", ""));
                        id1 = line.substring(7, 10).replaceAll(" ", "");
                        ir2 = Integer.parseInt(line.substring(13, 16).replaceAll(" ", ""));
                        id2 = line.substring(16, 19).replaceAll(" ", "");
                        d = Double.parseDouble(line.substring(23, 26));
                        j1 = (int) Double.parseDouble(line.substring(32, 33));
                    }
                    if (ir1 == 0 && ir2 == 0) {
                        bigbreak = true;
                        break;
                    }
                    int i11 = ires[0][ir1 - 1];
                    int i12 = ires[1][ir1 - 1];
                    int i21 = ires[0][ir2 - 1];
                    int i22 = ires[1][ir2 - 1];
                    int i1 = 0;
                    int i2 = 0;
                    boolean go232 = false;
                    for (i1 = i11; i1 <= i12; i1++) {
                        if (i1 == 0)//new
                        {
                            go222 = false;
                            break; //new
                        }
                        if (id1.equals(id[i1 - 1])) {
                            go232 = true;
                            break;
                        }
                    }
                    if (!go232) {
                        tmpr = idres[ir1 - 1] + " " + ir1 + " " + id1 + " not found";
                        outnuclinout.println(tmpr);
                        continue;
                    }
                    go232 = false;
                    boolean go242 = false;
                    for (i2 = i21; i2 <= i22; i2++) {
                        if (id2.equals(id[i2 - 1])) {
                            go242 = true;
                            break;
                        }
                    }
                    if (!go242) {
                        tmpr = idres[ir2 - 1] + " " + ir2 + " " + id2 + " not found";
                        outnuclinout.println(tmpr);
                        continue;
                    }
                    go242 = false;
                    dd = 0;
                    for (int k = 0; k < 3; k++) {
                        dd += (xyz[i1 - 1][k] - xyz[i2 - 1][k]) * (xyz[i1 - 1][k] - xyz[i2 - 1][k]);
                    }
                    dd = (float) Math.sqrt(dd);
                    m++;
                    tmpr = m + " " + i1 + " " + i2 + " " + idres[ir1 - 1] + " " + ir1 + " " + id1 + " - " + idres[ir2 - 1] + " " + ir2 + " " + id2 + " " + d + " " + dd + " " + j1;
                    outnuclinout.println(tmpr);
                    int i1i4 = i1;
                    int i2i4 = i2;
                    int j1i4 = j1;
                    String outLine = i1i4 + " " + i2i4 + " " + d + " " + j1i4;
                    outbndtntmp.println(outLine);
                    if (jcode == 999) {
                        jcode = 0;
                        go219 = true;
                        break;
                    }
                    if (jcode != 0) {
                        break;
                    }
                }
                if (bigbreak) {
                    break;
                }
            }
            inhbnddat.close();
            outbndtntmp.close();
    }

    void bndsrt() throws Exception {
        int h = 0;
        int blanks = 0;
        int nn = 0;
        int nmax = 100000;// changed here, not enough space 20,000 -> 100,000
        int[] iatm1 = new int[nmax + 1];
        int[] iatm2 = new int[nmax + 1];
        int[] ibndt = new int[nmax + 1];
        double bnddst[] = new double[nmax + 1];
        int jatm1 = 0;
        int jatm2 = 0;
        int jbndt = 0;
        double dx = 0;
        int nd1 = 1;
        int nd2 = nmax;
        int nd3 = 1;
        int idx = 0;
        int isort = 0;
        int isym = 0;
        boolean go351 = false;
        boolean end = true;
        boolean go550 = false;

        boolean inited = false;
        nd1 = nmax;
        nd2 = 1;
        nd3 = -1;
        isort = -1;
            outwrentmp = new PrintWriter(new FileWriter(wrentmp));
            outbndtntmp.close();
            inbndtntmp = new BufferedReader(new FileReader(bndtntmp));
            boolean go4 = false;
            while (true) {
                if (!go4) {
                    if (idx >= nmax) {
                        break;
                    }
                }
                go4 = false;
                String line = inbndtntmp.readLine();
                if (line == null) {
                    inbndtntmp.close();
                    outbndtntmp = new PrintWriter(new FileWriter(bndtntmp));
                    break;
                }
                StringTokenizer tok = new StringTokenizer(line);
                jatm1 = Integer.parseInt(tok.nextToken());
                jatm2 = Integer.parseInt(tok.nextToken());
                double dst = Double.parseDouble(tok.nextToken());
                jbndt = Integer.parseInt(tok.nextToken());
                if (jatm1 == 0 || jatm2 == 0 || jatm1 == jatm2) {
                    String tmpr = h + " " + jatm1 + " " + jatm2 + " " + dst + " " + jbndt + " - bad bound";
                    outnuclinout.println(tmpr);
                    go4 = true;
                } else {
                    if (jatm1 > jatm2) {
                        h = jatm1;
                        jatm1 = jatm2;
                        jatm2 = h;
                    }
                    idx++;
                    dx++;
                    iatm1[idx - 1] = jatm1;
                    bnddst[idx - 1] = dst;
                    iatm2[idx - 1] = jatm2;
                    ibndt[idx - 1] = jbndt;
                    if (jbndt == 21) {
                        throw new Exception("idx = " + idx + " iatm1 = " + jatm1 + " bnddst = " + dst + " iatmn2 = " + jatm2);
                    }
                }
            }
            if (idx != 0) {
                int id1 = 1;
                int id2 = idx;
                int id3 = -1;
                sort(idx, iatm1, iatm2, bnddst, ibndt);
                id1 = idx;
                id2 = 1;
                id3 = -1;
                for (int i = id1; i >= id2; i += id3) {
                    nn = i; //nos[i-1];
                    String line = iatm1[i - 1] + " " + iatm2[nn - 1] + " " + bnddst[nn - 1] + " " + ibndt[nn - 1];
                    outwrentmp.println(line);
                }
            }
            idx = 0;
            int ncycle = 0;
            int nend = 0;
            int nturn = 0;
            boolean go667 = false;
            while (true) {
                ncycle = (int) ((dx - 1) / nmax);
                nend = (int) (dx - ncycle * nmax);
                nturn = (int) ((dx - nend) / nmax);

                if (ncycle <= 0) {
                    go667 = true;
                    break;
                }
                if (nend != 0) {
                    break;
                }
                nmax++;
            }
            if (!go667) {
                for (int i = 1; i <= ncycle; i++) {
                    outwrentmp.close();
                    inwrentmp = new BufferedReader(new FileReader(wrentmp));
                    inbndtntmp.close();
                    outbndtntmp = new PrintWriter(new FileWriter(bndtntmp));
                    if (nend != 0) {
                        for (int j = 1; j <= nend; j++) {
                            String line = inwrentmp.readLine();
                            StringTokenizer tok = new StringTokenizer(line);
                            iatm1[j - 1] = Integer.parseInt(tok.nextToken());
                            iatm2[j - 1] = Integer.parseInt(tok.nextToken());
                            bnddst[j - 1] = Double.parseDouble(tok.nextToken());
                            ibndt[j - 1] = Integer.parseInt(tok.nextToken());
                            outbndtntmp.println(line);
                        }
                    }
                    for (int j = 1; j <= nturn; j++) {
                        for (int jj = 1; jj <= nmax; jj++) {
                            String line = inwrentmp.readLine();
                            StringTokenizer tok = new StringTokenizer(line);
                            iatm1[jj - 1] = Integer.parseInt(tok.nextToken());
                            iatm2[jj - 1] = Integer.parseInt(tok.nextToken());
                            bnddst[jj - 1] = Double.parseDouble(tok.nextToken());
                            ibndt[jj - 1] = Integer.parseInt(tok.nextToken());
                        }
                        sort(nmax, iatm1, iatm2, bnddst, ibndt);
                        for (int jj = nd1; jj >= nd2; jj += nd3) {
                            nn = jj; //nos[jj - 1];
                            String outLine = iatm1[jj - 1] + " " + iatm2[nn - 1] + " " + bnddst[nn - 1] + " " + ibndt[nn - 1];
                            outbndtntmp.println(outLine);
                        }
                    }
                    inwrentmp.close();
                    outwrentmp = new PrintWriter(new FileWriter(wrentmp));
                    outbndtntmp.close();
                    inbndtntmp = new BufferedReader(new FileReader(bndtntmp));
                    for (int j = 1; j <= nturn; j++) {
                        for (int jj = 1; jj <= nmax; jj++) {
                            String line = inbndtntmp.readLine();
                            StringTokenizer tok = new StringTokenizer(line);
                            iatm1[jj - 1] = Integer.parseInt(tok.nextToken());
                            iatm2[jj - 1] = Integer.parseInt(tok.nextToken());
                            bnddst[jj - 1] = Double.parseDouble(tok.nextToken());
                            ibndt[jj - 1] = Integer.parseInt(tok.nextToken());
                        }
                        sort(nmax, iatm1, iatm2, bnddst, ibndt);
                        for (int jj = nd1; jj >= nd2; jj += nd3) {
                            nn = jj; //nos[jj - 1];
                            String tmpr = iatm1[jj - 1] + " " + iatm2[nn - 1] + " " + bnddst[nn - 1] + " " + ibndt[nn - 1];
                            outwrentmp.println(tmpr);
                        }
                    }
                    for (int jj = 1; jj <= nend; jj++) {
                        String line = inbndtntmp.readLine();
                        StringTokenizer tok = new StringTokenizer(line);
                        iatm1[jj - 1] = Integer.parseInt(tok.nextToken());
                        iatm2[jj - 1] = Integer.parseInt(tok.nextToken());
                        bnddst[jj - 1] = Double.parseDouble(tok.nextToken());
                        ibndt[jj - 1] = Integer.parseInt(tok.nextToken());
                    }
                    for (int jj = 1; jj <= nend; jj++) {
                        String tmpr = iatm1[jj - 1] + " " + iatm2[jj - 1] + " " + bnddst[jj - 1] + " " + ibndt[jj - 1];
                        outwrentmp.println(tmpr);
                    }
                }
            }
            outwrentmp.close();
            inwrentmp = new BufferedReader(new FileReader(wrentmp));
            inbndtntmp.close();
    }

    void sort(int idx, int[] a, int[] b, double[] c, int[] d) {
        /** a = iatm1 : b = nos */
        sort(a, b, c, d, 0, idx - 1);
    }

    void sort(int a[], int[] b, double[] c, int[] d, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        } else if (lo == hi - 1) {
            /* * sort a two element list by swapping if necessary */
            if (a[lo] < a[hi] || (a[lo] == a[hi] && b[lo] <= b[hi])) {
                int T = a[lo];
                a[lo] = a[hi];
                a[hi] = T;
                int T2 = b[lo];
                b[lo] = b[hi];
                b[hi] = T2;
                double T3 = c[lo];
                c[lo] = c[hi];
                c[hi] = T3;
                int T4 = d[lo];
                d[lo] = d[hi];
                d[hi] = T4;
            }
            return;
        }
        /* * Pick a pivot and move it out of the way */
        int pivot1 = a[(lo + hi) / 2];
        a[(lo + hi) / 2] = a[hi];
        a[hi] = pivot1;
        int pivot2 = b[(lo + hi) / 2];
        b[(lo + hi) / 2] = b[hi];
        b[hi] = pivot2;
        double pivot3 = c[(lo + hi) / 2];
        c[(lo + hi) / 2] = c[hi];
        c[hi] = pivot2;
        int pivot4 = d[(lo + hi) / 2];
        d[(lo + hi) / 2] = d[hi];
        d[hi] = pivot4;

        while (lo < hi) {
            /* * Search forward from a[lo] until an element is found that * is greater than the pivot or lo >= hi */
            while ((a[lo] > pivot1 || (a[lo] == pivot1 && b[lo] >= pivot2)) && lo < hi) {
                lo++;
            }
            /* * Search backward from a[hi] until element is found that * is less than the pivot, or lo >= hi */
            while ((pivot1 > a[hi] || (pivot1 == a[hi] && pivot2 >= b[hi])) && lo < hi) {
                hi--;
            }
            /* * Swap elements a[lo] and a[hi] */
            if (lo < hi) {
                int T = a[lo];
                a[lo] = a[hi];
                a[hi] = T;
                int T2 = b[lo];
                b[lo] = b[hi];
                b[hi] = T2;
                double T3 = c[lo];
                c[lo] = c[hi];
                c[hi] = T3;
                int T4 = d[lo];
                d[lo] = d[hi];
                d[hi] = T4;
            }
        }
        /* * Put the median in the "center" of the list */
        a[hi0] = a[hi];
        b[hi0] = b[hi];
        c[hi0] = c[hi];
        d[hi0] = d[hi];
        a[hi] = pivot1; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
        b[hi] = pivot2; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
        c[hi] = pivot3; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
        d[hi] = pivot4; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
        sort(a, b, c, d, lo0, lo - 1);
        sort(a, b, c, d, hi + 1, hi0);
    }

    void nblist(int ioutnb) throws Exception {
        String l3[] = new String[10000];
        double xyz[][] = new double[10000][3];
        int ks[] = new int[10000];
        double dddst[] = new double[20];
        int ibnd[] = new int[20];
        int ibndt[] = new int[20];
        char l1[] = new char[10000];
        int l2[] = new int[10000];
        int na = 0, nb = 0, ntyp = 0;
        int ii4 = 0, i2i4 = 0, iti4 = 0;
        double[][] bdst = {{3.40, 3.20, 3.10, 3.70, 3.60, 3.60}, {3.20, 3.00, 3.00, 3.50, 2.20, 2.60}, {3.10, 3.00, 3.10, 3.50, 2.20, 2.60}, {3.70, 3.50, 3.50, 3.80, 3.60, 3.60}, {3.60, 2.20, 2.20, 3.60, 3.60, 3.60}, {3.60, 2.60, 2.60, 3.60, 3.60, 3.60}};
        double dstmx = 16;
        boolean go1401 = false;
            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            int i = 0;
            for (i = 1; i <= 10000; i++) {
                String line = inatomsdat.readLine();
                if (line == null) {
                    i--;
                    break;
                }
                HD hd= new HD(line);
                l1[i - 1] = hd.residueName;
                l2[i - 1] = hd.residueNumber;
                l3[i - 1] = hd.atomName;
                ks[i - 1] = hd.atomCode;
                double tmp[] = {hd.x, hd.y, hd.z};
                xyz[i - 1] = ortho(tmp);
            }
            i++;
            int in = i - 1;
            inatomsdat.close();
            outdistbin = new PrintWriter(new FileWriter(distbin));
            outvdwdstbin = new PrintWriter(new FileWriter(vdwdstbin));
            if (ioutnb == 1) {
                outnuclinout.println();
                outnuclinout.println(" List of van der waals contacts : \n");
            }
            int in1 = in - 1;
            int i1 = 0;
            int jn = 0;
            String line = inwrentmp.readLine();
            if (line != null) {
                StringTokenizer tok = new StringTokenizer(line);
                na = Integer.parseInt(tok.nextToken());
                nb = Integer.parseInt(tok.nextToken());
                double ddst = Double.parseDouble(tok.nextToken());
                ntyp = Integer.parseInt(tok.nextToken());
                if (ddst == 0) {
                    outnuclinout.println("Distance is zero between " + na + " " + nb + "\n");
                }
                if (na != 0 || nb != 0) {
                    if (ntyp <= 5 || ntyp == 8) {
                        outdistbin.println(line);
                    }
                    na = Math.abs(na);
                    int nn = 0;
                    for (i = 1; i <= in1; i++) {
                        while (na == i) {
                            jn++;
                            if (jn > 20) {
                                throw new Exception("Bug jn > 20");
                            }
                            ibnd[jn - 1] = nb;
                            ibndt[jn - 1] = ntyp;
                            dddst[jn - 1] = ddst;
                            line = inwrentmp.readLine();
                            if (line != null) {
                                go1401 = false;
                                tok = new StringTokenizer(line);
                                na = Integer.parseInt(tok.nextToken());
                                nb = Integer.parseInt(tok.nextToken());
                                ddst = Double.parseDouble(tok.nextToken());
                                ntyp = Integer.parseInt(tok.nextToken());
                                if (ddst == 0) {
                                    outnuclinout.println(" Distance is zero between " + na + " " + nb + "\n");
                                }
                                if (ntyp <= 5 || ntyp == 8) {
                                    outdistbin.println(line);
                                }
                                na = Math.abs(na);
                            } else {
                                go1401 = true;
                                break;
                            }
                        }

                        if (!go1401) {
                            i1 = i + 1;
                            for (int i2 = i1; i2 <= in; i2++) {
                                double dst = 0;
                                for (int k = 0; k < 3; k++) {
                                    dst += (xyz[i - 1][k] - xyz[i2 - 1][k]) * (xyz[i - 1][k] - xyz[i2 - 1][k]);
                                }
                                boolean doIt = false;
                                if (dst <= dstmx) {
                                    doIt = true;
                                }
                                while (doIt) {
                                    doIt = false;
                                    dst = Math.sqrt(dst);
                                    int it = 2;
                                    if (jn != 0) {
                                        int j = 0;
                                        boolean skip = false;
                                        for (j = 1; j <= jn; j++) {
                                            if (ibnd[j - 1] == i2) {
                                                skip = true;
                                                break;
                                            }
                                        }
                                        if (skip) {
                                            if (ibndt[j - 1] <= 5) {
                                                break;
                                            }
                                            if (ibndt[j - 1] == 7) {
                                                it = 3;
                                            }
                                            if (ibndt[j - 1] == 8 || ibndt[j - 1] == 6) {
                                                it = 1;
                                            }
                                        }
                                    }
                                    int ii1 = ks[i - 1];
                                    if (ii1 > 6) {
                                        ii1 = 4;
                                    }
                                    int ii2 = ks[i2 - 1];
                                    if (ii2 > 6) {
                                        ii2 = 4;
                                    }
                                    nn++;
                                    ii4 = i;
                                    i2i4 = i2;
                                    iti4 = it;
                                    String tmpr = ii4 + " " + i2i4 + " " + bdst[ii1 - 1][ii2 - 1] + " " + iti4;
                                    outvdwdstbin.println(tmpr);

                                    if ((it == 2) && (dst <= bdst[ii1 - 1][ii2 - 1] + 0.4) && (ioutnb != 0)) {
                                        outnuclinout.println(nn + " " + l1[i - 1] + " " + l2[i - 1] + " " + l3[i - 1] + " - " + l1[i2 - 1] + " " + l2[i2 - 1] + " " + l3[i2 - 1] + bdst[ii1 - 1][ii2 - 1] + " " + dst + " " + it + " " + i + " " + i2 + " " + ii1 + " " + ii2);
                                    }
                                    if ((it != 2) && (dst <= bdst[ii1 - 1][ii2 - 1]) && (ioutnb != 0)) {
                                        outnuclinout.println(nn + " " + l1[i - 1] + " " + l2[i - 1] + " " + l3[i - 1] + " - " + l1[i2 - 1] + " " + l2[i2 - 1] + " " + l3[i2 - 1] + bdst[ii1 - 1][ii2 - 1] + " " + dst + " " + it + " " + i + " " + i2 + " " + ii1 + " " + ii2);
                                    }
                                    if (dst == 0) {
                                        outnuclinout.println(" Distance is zero between " + i + " " + i2);
                                    }
                                }
                            }
                        }
                        jn = 0;
                    }
                }
            }
            inwrentmp.close();
            outdistbin.close();
            outvdwdstbin.close();
    }

    void nblist2(int ioutnb) throws Exception {
        String l3[] = new String[10000];
        String l1[] = new String[10000];
        String l2[] = new String[10000];
        double xyz[][] = new double[10000][3];
        int ks[] = new int[10000];
        double[] dddst = new double[20];
        int ibnd[] = new int[20];
        double ibndt[] = new double[20];
        double[][] bdst = {{3.4, 3.2, 3.1, 3.7, 3.6, 3.6}, {3.2, 3, 3, 3.5, 2.2, 2.6}, {3.1, 3, 3.1, 3.5, 2.2, 2.6}, {3.7, 3.5, 3.5, 3.8, 3.6, 3.6}, {3.6, 2.2, 2.2, 3.6, 3.6, 3.6}, {3.6, 2.6, 2.6, 3.6, 3.6, 3.6}};
        double dstmx = 16;
            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            String line;
            int i = 0;
            boolean secBreak = false;
            while ((line = inatomsdat.readLine()) != null) {

                HD hd= new HD(line);
                l1[i] = hd.residueName + "";
                l2[i] = hd.residueNumber + "";
                l3[i] = hd.atomName;
                ks[i] = hd.atomCode;
                double x = hd.x;
                double y = hd.y;
                double z = hd.z;
                double tmp[] = {x, y, z};
                xyz[i] = ortho(tmp);
                i++;
            }
            int in = i;
            inatomsdat.close();
            outdistbin = new PrintWriter(new FileWriter(distbin));
            outvdwdstbin = new PrintWriter(new FileWriter(vdwdstbin));
            if (ioutnb == 1) {
                outnuclinout.println("");
                String tmpr = "(' List of van der waals contacts : ',/)";
                outnuclinout.println(tmpr);
            }
            int in1 = in - 1;
            int jn = 0;
            line = inwrentmp.readLine();
            if (line != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int na = Integer.parseInt(tok.nextToken());
                int nb = Integer.parseInt(tok.nextToken());
                double ddst = Double.parseDouble(tok.nextToken());
                double ntyp = Double.parseDouble(tok.nextToken());
                if (ddst == 0) {
                    String tmpr = "(' Distance is zero between ' " + na + " " + nb + " /)";
                    outnuclinout.println(tmpr);
                }
                if (na != 0 || nb != 0) {
                    if (ntyp < 5 || ntyp == 8) {
                        outdistbin.println(line);
                    }
                    na = Math.abs(na);
                    int nn = 0;
                    for (i = 1; i <= in1; i++) {
                        while (na == i) {
                            jn++;
                            if (jn > 20) {
                                throw new Exception(i + " bug in nblist(" + ioutnb + ") jn = " + jn + " > 20");
                            }
                            ibnd[jn - 1] = nb;
                            ibndt[jn - 1] = ntyp;
                            dddst[jn - 1] = ddst;
                            line = inwrentmp.readLine();
                            if (line == null) {
                                secBreak = true;
                                break;
                            }
                            tok = new StringTokenizer(line);
                            na = Integer.parseInt(tok.nextToken());
                            nb = Integer.parseInt(tok.nextToken());
                            ddst = Double.parseDouble(tok.nextToken());
                            ntyp = Double.parseDouble(tok.nextToken());
                            if (ddst == 0) {
                                String tmpr = " Distance is zero between  " + na + " " + nb + "\n";
                                outnuclinout.println(tmpr);
                            }
                            if (ntyp < 5 || ntyp == 8) {
                                outdistbin.println(line);
                            }
                            na = Math.abs(na);
                        }
                        if (!secBreak) {
                            int i1 = i + 1;
                            for (int i2 = i1; i2 <= in; i2++) {

                                double dst = 0;
                                for (int k = 0; k < 3; k++) {
                                    dst += (xyz[i - 1][k] - xyz[i2 - 1][k]) * (xyz[i - 1][k] - xyz[i2 - 1][k]);
                                }
                                if (dst > dstmx) {
                                    continue;
                                }
                                dst = Math.sqrt(dst);
                                int it = 2;

                                boolean bigbreak = false;
                                if (jn == 0) {
                                    for (int j = 0; j < jn; j++) {
                                        if (ibnd[j] == i2) {
                                            if (ibndt[j] < 5) {
                                                bigbreak = true;
                                            }
                                            if (ibndt[j] == 7) {
                                                it = 3;
                                            }
                                            if (ibndt[j] == 8 || ibndt[j] == 6) {
                                                it = 1;
                                            }
                                            break;
                                        }
                                    }
                                    if (bigbreak) {
                                        break;
                                    }
                                }
                                int ii1 = ks[i - 1];
                                if (ii1 > 6) {
                                    ii1 = 4;
                                }
                                int ii2 = ks[i2 - 1];
                                if (ii2 > 6) {
                                    ii2 = 4;
                                }
                                nn++;
                                int ii4 = i - 1;
                                int i2i4 = i2 - 1;
                                int iti4 = it;
                                String tmpr = ii4 + " " + i2i4 + " " + bdst[ii1][ii2] + " " + iti4;
                                outvdwdstbin.println(tmpr);

                                if (((it == 2) && (dst <= (bdst[ii1][ii2] + 0.4)) && (ioutnb != 0)) || ((it != 2) && (dst <= (bdst[ii1][ii2])) && (ioutnb != 0))) {
                                    tmpr = nn + " " + l1[i - 1] + " " + l2[i - 1] + " " + l3[i - 1] + " " + l1[i2 - 1] + " " + l2[i2 - 1] + " " + l3[i2 - 1] + " " + bdst[ii1][ii2] + " " + dst + " " + it + " " + i + " " + i2 + " " + ii1 + " " + ii2;
                                    outnuclinout.println(tmpr);
                                }
                                if (dst == 0) {
                                    tmpr = " Distance is zero between  " + i + " " + i2 + "\n";
                                    outnuclinout.println(tmpr);
                                }
                            }
                        }
                        secBreak = false;
                        jn = 0;
                    }
                }
            }
            inwrentmp.close();
            outdistbin.close();
            outvdwdstbin.close();
    }

    void sugr(int ip) throws Exception {

        int iatom[] = new int[4];
        int iat[][] = new int[4][4];
        int it[] = new int[9];
        int nn[] = new int[30];

        double vn[] = {0.018, -1.198, 1.969, -1.884, 1.096};
        double vids[] = {2.52, 2.49, 2.74, 2.44};
        double vs[] = {-1.106, 1.835, -1.955, 1.240, -0.013};

            String line = innuclindat.readLine();
            int nucl = Integer.parseInt(line);
            line = innuclindat.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            int ntim = Integer.parseInt(tok.nextToken());
            for (int i = 0; i < ntim; i++) {
                nn[i] = Integer.parseInt(tok.nextToken());
            }
            if (ntim != 0) {
                inchirlsbin = new BufferedReader(new FileReader(chirlsbin));
                outchirls2bin = new PrintWriter(new FileWriter(chirls2bin));

                int ninc = 3;
                if (ip == 1) {
                    ninc = 4;
                }
                for (int k = 0; k < ntim; k += ninc) {
                    int n1 = nn[k];
                    int n2 = nn[k + 1];
                    int n3 = nn[k + 2];
                    int n4 = 1;
                    if (ip == 1) {
                        n4 = nn[k + 3];
                    }
                    if (n1 != 0) {
                        for (int j = 0; j < n1; j++) {
                            for (int ik = 0; ik < nucl; ik++) {
                                line = inchirlsbin.readLine();
                                outchirls2bin.println(line);
                                tok = new StringTokenizer(line);
                                iatom[0] = Integer.parseInt(tok.nextToken());
                                iatom[1] = Integer.parseInt(tok.nextToken());
                                iatom[2] = Integer.parseInt(tok.nextToken());
                                iatom[3] = Integer.parseInt(tok.nextToken());
                                int vid = Integer.parseInt(tok.nextToken());
                                for (int kj = 0; kj < 4; kj++) {
                                    iat[ik][kj] = iatom[kj];
                                }
                            }
                            it[0] = iat[0][3];
                            it[1] = iat[1][1];
                            it[2] = iat[1][0];
                            it[3] = iat[0][2];
                            it[4] = iat[0][0];
                            if (nucl == 4) {
                                it[1] = iat[2][0];
                                it[2] = iat[2][2];
                            }

                            for (int kj = 0; kj < 4; kj++) {
                                it[kj + 5] = it[kj];
                            }
                            for (int ik = 0; ik < 5; ik++) {
                                outchirls2bin.println(it[ik + 1] + " " + it[ik + 2] + " " + it[ik + 4] + " " + it[ik] + " " + vn[ik]);
                            }
                        }
                    }

                    if (n2 != 0) {
                        for (int j = 0; j < n2; j++) {
                            for (int ik = 0; ik < nucl; ik++) {
                                line = inchirlsbin.readLine();
                                tok = new StringTokenizer(line);
                                for (int i = 0; i < 4; i++) {
                                    iatom[i] = Integer.parseInt(tok.nextToken());
                                }
                                double vid = Double.parseDouble(tok.nextToken());
                                if (nucl == 4) {
                                    outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[ik]);
                                } else {
                                    if (ik >= 0 && ik < 3) {
                                        outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[ik]);
                                    }
                                }
                                for (int kj = 0; kj < 4; kj++) {
                                    iat[ik][kj] = iatom[kj];
                                }
                            }

                            it[0] = iat[0][3];
                            it[1] = iat[1][1];
                            it[2] = iat[1][0];
                            it[3] = iat[0][2];
                            it[4] = iat[0][0];
                            if (nucl != 4) {
                                it[1] = iat[2][0];
                                it[2] = iat[2][2];
                            }
                            for (int kj = 0; kj < 4; kj++) {
                                it[kj + 5] = it[kj];
                            }
                            for (int ik = 0; ik < 5; ik++) {
                                outchirls2bin.println(it[ik + 1] + " " + it[ik + 2] + " " + it[ik + 4] + " " + it[ik] + " " + vs[ik]);
                            }
                        }
                    }

                    if (ip == 0 || n4 == 0) {
                        continue;
                    }

                    for (int j = 0; j < n4; j++) {
                        int ncl = nucl + 5;
                        for (int ik = 0; ik < ncl; ik++) {
                            line = inchirlsbin.readLine();
                            outchirls2bin.println(line);
                        }
                    }
                }

                while ((line = inchirlsbin.readLine()) != null) {
                    outchirls2bin.println(line);
                }
                inchirlsbin.close();
                outchirls2bin.close();
                outchirlsbin.close();
                inchirls2bin.close();
                IoUtils.moveFile(new File(chirls2bin), new File(chirlsbin));
                inchirls2bin = new BufferedReader(new FileReader(chirlsbin));
            }
    }

    void fcvt(int ip, int ig, int it, int is, int io, int nbg1, int ned1, int nbg2, int ned2, double qf1, int temp, String amod) throws Exception {
        int lat4[] = new int[20];
        int ia4;
        int ja4;
        int ktyp4;
        int ktyp;
        int iatom4[] = new int[4];
        int[] idist4 = new int[10];
        int[] ns4 = new int[4];
        int iat4[] = new int[150];
        int ntor4;
        int iang4;
        int ires4;
        int iwt4;
        int tag;
        int m;
        int l;
        int nps4;
        int it4[] = new int[6];
        int tagp;
        int tagtm;
        int nx4 = 0;
        int[] id4 = new int[8];
        int mpl4 = 0;
        int ntot;
        int snum;
        int sn;
        int snu;
        double shell[] = {5.0, 3.5, 2.8, 2.2, 1.9, 1.65, 1.4};
        int no[] = new int[200000];
        int nt[] = new int[200000];
        double rest[] = new double[10];
        int noat4 = 0;
        int nochrl4 = 0;
        int novdw4 = 0;
        int n4;
        int igr4;
        int jabn;
        int na4;
        int nb4;
        double ntyp4;
        int afsig;
        int bfsig;
        int itemp = 0;
        int id = 0;
        int iwp4;
        int iwtm4;
        int iat1 = 0;
        int iat2;

            innuclindat = new BufferedReader(new FileReader(nuclindat));
            String title = innuclindat.readLine();
            innuclindat.close();

            outlsqinp = new PrintWriter(new FileWriter(lsqinp));
            inatomsdat = new BufferedReader(new FileReader(atomsdat));

            int noat = 0;
            String line;
            while ((line = inatomsdat.readLine()) != null) {

                String label = line.substring(7, 15);
                HD hd= new HD(line);
                int ksatm = hd.atomCode;
                double xf = hd.x;
                double yf = hd.y;
                double zf = hd.z;
                double bt = hd.b;
                double qocc = hd.q;
                noat++;
                if (qocc == 0) {
                    qocc = 1;
                }
                if (io != 0) {
                    if (noat >= nbg1 && noat <= ned1) {
                        qocc = qf1;
                    }
                    if (noat >= nbg2 && noat <= ned2) {
                        qocc = 1 - qf1;
                    }
                }

                noat4 = noat;
                int ksatm4 = ksatm;
                char resName = label.charAt(0);
                int resNum = Integer.parseInt(label.substring(1, 4).replaceAll(" ", ""));
                String atomName = label.substring(4);
                line = HD.getAtomString(noat4, resName, resNum, atomName, ksatm4, xf, yf, zf, bt, qocc);
                outlsqinp.println(line);
            }
            inatomsdat.close();
            indistbin = new BufferedReader(new FileReader(distbin));
            int nip4 = 0;
            while ((line = indistbin.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int na = Integer.parseInt(tok.nextToken());
                int nb = Integer.parseInt(tok.nextToken());
                double ddst = Double.parseDouble(tok.nextToken());
                double ntyp = Double.parseDouble(tok.nextToken());

                if (ddst == 0) {
                    break;
                }

                nip4++;
                if (ntyp == 8) {
                    ntyp = 0;

                }
                na4 = na;
                nb4 = nb;
                ntyp4 = ntyp;

                outlsqinp.println(nip4 + " " + na4 + " " + nb4 + " " + ddst + " " + (int) ntyp4);

                no[nip4 - 1] = na4;
                nt[nip4 - 1] = nb4;
            }
            indistbin.close();
            inplansbin = new BufferedReader(new FileReader(plansbin));
            while ((line = inplansbin.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int mpl = Integer.parseInt(tok.nextToken());
                int na = Integer.parseInt(tok.nextToken());
                int[] lat = new int[na];
                for (int j = 0; j < na; j++) {
                    lat[j] = Integer.parseInt(tok.nextToken());
                }
                mpl4 = mpl;
                na4 = na;
                for (int j = 0; j < na; j++) {
                    lat4[j] = lat[j];
                }
                String tmp = mpl4 + " " + na4;
                for (int j = 0; j < na; j++) {
                    tmp += " " + lat4[j];
                }
                outlsqinp.println(tmp);
            }
            inplansbin.close();
            inchirlsbin = new BufferedReader(new FileReader(chirlsbin));
            int nochrl = 0;
            for (int i = 0; i < 10; i++) {
                idist4[i] = 0;
            }

            while ((line = inchirlsbin.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int iatom[] = new int[4];
                for (int i = 0; i < 4; i++) {
                    iatom[i] = Integer.parseInt(tok.nextToken());
                }
                double videal = Double.parseDouble(tok.nextToken());
                nochrl++;
                nochrl4 = nochrl;
                for (int i = 0; i < 4; i++) {
                    iatom4[i] = iatom[i];
                }
                m = 0;

                for (int ia = 1; ia <= 3; ia++) {
                    l = ia + 1;
                    iat1 = iatom4[ia - 1];
                    for (int ja = l; ja <= 4; ja++) {
                        m++;
                        iat2 = iatom4[ja - 1];
                        for (int j1 = 1; j1 <= nip4; j1++) {
                            if (no[j1 - 1] == iat1 && nt[j1 - 1] == iat2) {
                                idist4[m - 1] = j1;
                                break;
                            }
                            if (no[j1 - 1] == iat2 && nt[j1 - 1] == iat1) {
                                idist4[m - 1] = -j1;
                                break;
                            }
                        }
                    }
                }
                //Here the output is not the same than in the fortran version
                /* this is due to the fact that le dist.bin though carrying the same information in both version are different
				 * (in fact only the line order changes)
				 * this is caused by the fact that the sort methods are different
				 * for now i think it will have no consequences in nuclsq, but if this is not the case the sort method will have
				 * to be rewritten identicaly to the fortran one (and reuse the nos table ? ) */
                outlsqinp.println(nochrl4 + " " + iatom4[0] + " " + iatom4[1] + " " + iatom4[2] + " " + iatom4[3] + " " + idist4[0] + " " + idist4[1] + " " + idist4[2] + " " + idist4[3] + " " + idist4[4] + " " + idist4[5] + " " + videal);
            }
            inchirlsbin.close();
            invdwdstbin = new BufferedReader(new FileReader(vdwdstbin));
            int novdw = 0;
            while ((line = invdwdstbin.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int ia1 = Integer.parseInt(tok.nextToken());
                int ja1 = Integer.parseInt(tok.nextToken());
                double dnbc = Double.parseDouble(tok.nextToken());
                ktyp = Integer.parseInt(tok.nextToken());
                if (dnbc == 0) {
                    break;
                }
                novdw++;
                novdw4 = novdw;
                ia4 = ia1;
                ja4 = ja1;
                ktyp4 = ktyp;
                outlsqinp.println(novdw4 + " " + ia4 + " " + ja4 + " " + dnbc + " " + ktyp4);
            }
            invdwdstbin.close();
            for (int i = 0; i < 10; i++) {
                idist4[i] = 0;
            }
            ntor4 = 0;
            ns4[0] = 0;
            ns4[1] = 0;
            ns4[2] = 0;
            ns4[3] = 0;
            if (it != 0) {
                intorfixbin = new BufferedReader(new FileReader(torfixbin));
                while ((line = intorfixbin.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line);
                    nx4 = Integer.parseInt(tok.nextToken());
                    ires4 = Integer.parseInt(tok.nextToken());
                    iang4 = Integer.parseInt(tok.nextToken());
                    for (int i = 0; i < 4; i++) {
                        iatom4[i] = Integer.parseInt(tok.nextToken());
                    }
                    for (int i = 0; i < 6; i++) {
                        idist4[i] = Integer.parseInt(tok.nextToken());
                    }
                    iwt4 = Integer.parseInt(tok.nextToken());
                    tag = Integer.parseInt(tok.nextToken());
                    m = 0;
                    boolean bigbreak = false;
                    for (int ia = 1; ia <= 3; ia++) {
                        l = ia + 1;
                        for (int ja = l; ja <= 4; ja++) {
                            m++;
                            iat2 = iatom4[ja - 1];
                            for (int j1 = 1; j1 <= nip4; j1++) {
                                if ((no[j1 - 1] == iat1) && (nt[j1 - 1] == iat2)) {
                                    idist4[m - 1] = j1;
                                    bigbreak = true;
                                    break;
                                }
                                if ((no[j1 - 1] == iat2) && (nt[j1 - 1] == iat1)) {
                                    idist4[m - 1] = -j1;
                                    bigbreak = true;
                                    break;
                                }
                            }
                            if (bigbreak) {
                                break;
                            }
                        }
                    }
                    ntor4++;
                    outlsqinp.println(nx4 + " " + ires4 + " " + iang4 + " " + iatom4[0] + " " + iatom4[1] + " " + iatom4[2] + " " + iatom4[3] + " " + idist4[0] + " " + idist4[1] + " " + idist4[2] + " " + idist4[3] + " " + idist4[4] + " " + idist4[5] + " " + iwt4 + " " + tag);
                }
                intorfixbin.close();
                for (int i = 0; i < 10; i++) {
                    idist4[i] = 0;
                }
            }
            nps4 = 0;
            if (ip != 0) {
                inpseubin = new BufferedReader(new FileReader(pseubin));
                while ((line = inpseubin.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line);
                    nx4 = Integer.parseInt(tok.nextToken());
                    ires4 = Integer.parseInt(tok.nextToken());
                    for (int i = 0; i < 5; i++) {
                        it4[i] = Integer.parseInt(tok.nextToken());
                    }
                    iwp4 = Integer.parseInt(tok.nextToken());
                    iwtm4 = Integer.parseInt(tok.nextToken());
                    tagp = Integer.parseInt(tok.nextToken());
                    tagtm = Integer.parseInt(tok.nextToken());
                    m = 0;
                    for (int ia = 1; ia <= 4; ia++) {
                        l = ia + 1;
                        iat1 = it4[ia - 1];
                        boolean bigbreak = false;
                        for (int ja = l; ja <= 5; ja++) {
                            m++;
                            iat2 = it4[ja - 1];
                            for (int j1 = 1; j1 <= nip4; j1++) {
                                if ((no[j1 - 1] == iat1) && (nt[j1 - 1] == iat2)) {
                                    idist4[m - 1] = j1;
                                    bigbreak = true;
                                    break;
                                }
                                if ((no[j1 - 1] == iat2) && (nt[j1 - 1] == iat1)) {
                                    idist4[m - 1] = -j1;
                                    bigbreak = true;
                                    break;
                                }
                            }
                            if (bigbreak) {
                                break;
                            }
                        }
                    }

                    outlsqinp.println(nx4 + " " + ires4 + " " + it4[0] + " " + it4[1] + " " + it4[2] + " " + it4[3] + " " + it4[4] + " " + idist4[0] + " " + idist4[1] + " " + idist4[2] + " " + idist4[3] + " " + idist4[4] + " " + idist4[5] + " " + idist4[6] + " " + idist4[7] + " " + idist4[8] + " " + idist4[9] + " " + iwp4 + " " + iwtm4 + " " + tagp + " " + tagtm);
                }
                inpseubin.close();
                nps4 = nx4;
            }
            if (is != 0) {
                insymtrybin = new BufferedReader(new FileReader(symtrybin));
                line = insymtrybin.readLine();
                igr4 = Integer.parseInt(line);
                for (int ik = 1; ik <= igr4; ik++) {
                    line = insymtrybin.readLine();
                    StringTokenizer tok = new StringTokenizer(line);
                    na4 = Integer.parseInt(tok.nextToken());
                    iwt4 = Integer.parseInt(tok.nextToken());
                    for (int j = 0; j <= na4; j++) {
                        id4[j] = Integer.parseInt(tok.nextToken());
                    }
                    outlsqinp.println(line); /** formattage possible */
                    if (iwt4 != 0) {
                        for (int ix = 1; ix <= na4; ix++) {
                            line = insymtrybin.readLine();
                        }
                        outlsqinp.println(line);
                    }
                    line = insymtrybin.readLine();
                    ns4[ik - 1] = Integer.parseInt(line);
                    ntot = ns4[ik - 1];
                    for (int i = 1; i <= ntot; i++) {
                        line = insymtrybin.readLine();
                        outlsqinp.println(line);
                    }
                }
                insymtrybin.close();
            }
            outlsqinp.close();
            if (true) {
                outlsqdat = new PrintWriter(new FileWriter(lsqdat));
                inrestraintdc = new BufferedReader(new FileReader(restraintdc));
                double dams = 0.35;
                if (amod.toLowerCase().equals("mode")) {
                    inrestraintdc.close();
                    inrestraintdc = new BufferedReader(new FileReader(restmoddc));
                    dams = 0.75;
                }
                String bid = inrestraintdc.readLine();
                outlsqdat.println(title);
                int num = 0;
                if (itemp == 0) {
                    id = 1;
                }
                String tmpr = num + " " + num + " " + num + " " + num + " " + num + " " + num + " " + num + " " + num + " " + id + " " + num + " " + num;
                outlsqdat.println(tmpr);
                tmpr = noat4 + " " + nip4 + " " + mpl4 + " " + nochrl4 + " " + novdw4 + " " + ntor4 + " " + ns4[0] + " " + ns4[1] + " " + ns4[2] + " " + ns4[3] + " " + num + " " + itemp + " " + nps4;
                outlsqdat.println(tmpr);
                tmpr = num + "";
                outlsqdat.println(tmpr);
                tmpr = abc[0] + " " + abc[1] + " " + abc[2] + " " + abc[3] + " " + abc[4] + " " + abc[5];
                outlsqdat.println(tmpr);
                snum = 0;
                sn = 1;
                snu = 2;
                tmpr = num + " " + snum + " " + snum + " " + sn + " " + snu;
                outlsqdat.println(tmpr);
                num = 7;
                tmpr = num + " " + shell[0] + " " + shell[1] + " " + shell[2] + " " + shell[3] + " " + shell[4] + " " + shell[5] + " " + shell[6];
                outlsqdat.println(tmpr);
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                StringTokenizer tok = new StringTokenizer(line, ", ");
                for (int i = 0; i < 6; i++) {
                    rest[i] = Double.parseDouble(tok.nextToken());
                }
                num = 3;
                afsig = 0;
                bfsig = 0;
                tmpr = num + " " + afsig + " " + bfsig + " " + rest[0] + " " + rest[1] + " " + rest[2] + " " + rest[3] + " " + rest[4] + " " + rest[5];
                outlsqdat.println(tmpr);
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[0] = Double.parseDouble(tok.nextToken());
                rest[1] = Double.parseDouble(tok.nextToken());
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[2] = Double.parseDouble(tok.nextToken());
                rest[3] = Double.parseDouble(tok.nextToken());
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[4] = Double.parseDouble(tok.nextToken());
                rest[5] = Double.parseDouble(tok.nextToken());
                rest[6] = Double.parseDouble(tok.nextToken());
                rest[7] = Double.parseDouble(tok.nextToken());
                rest[8] = Double.parseDouble(tok.nextToken());
                rest[9] = Double.parseDouble(tok.nextToken());
                String tmp = rest[0] + "";
                for (int i = 1; i < 10; i++) {
                    tmp += " " + rest[i];
                }
                outlsqdat.println(tmp);
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[0] = Double.parseDouble(tok.nextToken());
                rest[1] = Double.parseDouble(tok.nextToken());
                rest[2] = Double.parseDouble(tok.nextToken());
                rest[3] = Double.parseDouble(tok.nextToken());
                rest[4] = Double.parseDouble(tok.nextToken());
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[5] = Double.parseDouble(tok.nextToken());
                rest[6] = Double.parseDouble(tok.nextToken());
                rest[7] = Double.parseDouble(tok.nextToken());
                rest[8] = Double.parseDouble(tok.nextToken());
                rest[9] = Double.parseDouble(tok.nextToken());
                tmp = rest[0] + "";
                for (int i = 1; i < 10; i++) {
                    tmp += " " + rest[i];
                }
                outlsqdat.println(tmp);
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[0] = Double.parseDouble(tok.nextToken());
                rest[1] = Double.parseDouble(tok.nextToken());
                rest[2] = Double.parseDouble(tok.nextToken());
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[3] = Double.parseDouble(tok.nextToken());
                rest[4] = Double.parseDouble(tok.nextToken());
                rest[5] = Double.parseDouble(tok.nextToken());
                rest[6] = Double.parseDouble(tok.nextToken());
                rest[7] = Double.parseDouble(tok.nextToken());
                rest[8] = Double.parseDouble(tok.nextToken());
                rest[9] = Double.parseDouble(tok.nextToken());
                tmp = rest[0] + "";
                for (int i = 1; i < 10; i++) {
                    tmp += " " + rest[i];
                }
                outlsqdat.println(tmp);
                bid = inrestraintdc.readLine();
                line = inrestraintdc.readLine();
                tok = new StringTokenizer(line, ", ");
                rest[0] = Double.parseDouble(tok.nextToken());
                rest[1] = Double.parseDouble(tok.nextToken());
                rest[2] = Double.parseDouble(tok.nextToken());
                rest[3] = Double.parseDouble(tok.nextToken());
                rest[4] = Double.parseDouble(tok.nextToken());
                rest[5] = Double.parseDouble(tok.nextToken());
                tmp = rest[0] + "";
                for (int i = 1; i < 6; i++) {
                    tmp += " " + rest[i];
                }
                outlsqdat.println(tmp);
                snum = 0;
                num = 2;
                snu = 2;
                tmpr = snum + " " + num + " " + snum + " " + snu;
                outlsqdat.println(tmpr);
                jabn = 0;
                tmpr = jabn + " " + dams;
                outlsqdat.println(tmpr);
                if (itemp == 1) {
                    tmpr = dams + "";
                    outlsqdat.println(tmpr);
                }
                tmpr = jabn + "";
                outlsqdat.println(tmpr);
                inrestraintdc.close();
                outlsqdat.close();
            } else {
                outlsqpar = new PrintWriter(new FileWriter(lsqpar));
                outlsqpar.println("$NO. OF  ATOMS = " + noat4 + " DISTANCES = " + nip4 + " PLANES = " + mpl4 + " CHIRALS = " + nochrl4 + " VDW CONTACTS = " + novdw4 + " TORSIONS = " + ntor4 + " NSYM1 = " + ns4[0] + " NSYM2 = " + ns4[1] + " NSYM3 = " + ns4[2] + " NSYM4 = " + ns4[3] + " NPSEUD = " + nps4);
                outlsqpar.close();
            }
    }

    void torfix() throws Exception {
        double idist[] = new double[6];
        int nb[] = new int[9];
        double th[] = new double[6];
        String l3[] = new String[10000];
        char l1[] = new char[10000];
        String l3r[] = {"O3'", "P", "O5'", "C5'", "C4'", "C3'", "O3'", "P", "O5'", "O4'", "C1'"};
        String l3b[] = {"N9", "C8", "N1", "C6", "C6H2", "C5", "C2", "C4"};
        String l3d[] = {"N1", "C2", "N3", "C4", "C5H2", "C6H2"};
        char l1b[] = {'G', 'A', 'U', 'C', 'T', 'D', 'P'};
        int[] nr = new int[1000];
        int[] res_beg = new int[999];
            outtorfixbin = new PrintWriter(new FileWriter(torfixbin));
            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            int nx = 0;
            int iold = 0;
            int i = 1;
            String line;
            while ((line = inatomsdat.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int nn = Integer.parseInt(tok.nextToken());
                l1[i - 1] = tok.nextToken().charAt(0);
                nr[i - 1] = Integer.parseInt(tok.nextToken());
                l3[i - 1] = tok.nextToken();
                int ks = Integer.parseInt(tok.nextToken());
                double x = Double.parseDouble(tok.nextToken());
                double y = Double.parseDouble(tok.nextToken());
                double z = Double.parseDouble(tok.nextToken());
                double bt = Double.parseDouble(tok.nextToken());
                double qocc = Double.parseDouble(tok.nextToken());
                for (int j = 0; j < 7; j++) {
                    if (l1[i - 1] == l1b[j]) {
                        if (nr[i - 1] != iold) {
                            iold = nr[i - 1];
                            res_beg[nr[i - 1] - 1] = i;
                        }
                        i++;
                        break;
                    }
                }
                break;
            }
            int numb_atoms = i - 1;
            int last_res = nr[numb_atoms - 1];
            while ((line = innuclindat.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                int ires = Integer.parseInt(tok.nextToken());
                int iang = Integer.parseInt(tok.nextToken());
                int iwt = Integer.parseInt(tok.nextToken());
                double tag = Double.parseDouble(tok.nextToken());
                if (ires == 0) {
                    break;
                }
                int n1 = 0, n2 = 0, n3 = 0, n4 = 0, n5 = 0;
                int iatbeg = res_beg[ires - 1];
                int iatend = res_beg[ires];
                if (iang != 8) {
                    if (iang != 7) {
                        if (iang > 1 && iang < 5) {
                            iatbeg = res_beg[ires - 1];
                            if (ires != last_res) {
                                iatend = res_beg[ires] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg - 1; i < iatend; i++) {
                                if (l3[i].equals(l3r[iang - 1])) {
                                    n1 = i + 1;
                                }
                                if (l3[i].equals(l3r[iang])) {
                                    n1 = i + 1;
                                }
                                if (l3[i].equals(l3r[iang + 1])) {
                                    n1 = i + 1;
                                }
                                if (l3[i].equals(l3r[iang + 2])) {
                                    n1 = i + 1;
                                }
                            }
                        } else if (iang == 1) {
                            iatbeg = res_beg[ires - 1];
                            if (ires != last_res) {
                                iatend = res_beg[ires] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg; i <= iatend; i++) {
                                if (l3[i - 1].equals(l3r[iang])) {
                                    n2 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang + 1])) {
                                    n3 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang + 2])) {
                                    n4 = i;
                                }
                            }
                            if (ires != 1) {
                                iatbeg = res_beg[ires];
                                iatend = res_beg[ires - 1] - 1;
                                for (i = iatbeg; i <= iatend; i++) {
                                    if (l3[i - 1].equals(l3r[iang - 1])) {
                                        n1 = i;
                                    }
                                }
                            }
                        } else if (iang == 5) {
                            iatbeg = res_beg[ires - 1];
                            if (ires != last_res) {
                                iatend = res_beg[ires] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg; i <= iatend; i++) {
                                if (l3[i - 1].equals(l3r[iang - 1])) {
                                    n1 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang])) {
                                    n2 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang + 1])) {
                                    n3 = i;
                                }
                            }
                            iatbeg = res_beg[ires];
                            if (ires != last_res - 1) {
                                iatend = res_beg[ires + 1] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg; i <= iatend; i++) {
                                if (l3[i - 1].equals(l3r[iang + 2])) {
                                    n4 = i;
                                }
                            }
                        } else if (iang == 6) {
                            iatbeg = res_beg[ires - 1];
                            if (ires != last_res) {
                                iatend = res_beg[ires] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg; i <= iatend; i++) {
                                if (l3[i - 1].equals(l3r[iang - 1])) {
                                    n1 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang])) {
                                    n2 = i;
                                }
                            }
                            iatbeg = res_beg[ires];
                            if (ires != last_res - 1) {
                                iatend = res_beg[ires + 1] - 1;
                            } else {
                                iatend = numb_atoms;
                            }
                            for (i = iatbeg; i <= iatend; i++) {
                                if (l3[i - 1].equals(l3r[iang + 1])) {
                                    n3 = i;
                                }
                                if (l3[i - 1].equals(l3r[iang + 2])) {
                                    n4 = i;
                                }
                            }
                        }
                        nx++;
                        outtorfixbin.println(nx + " " + ires + " " + iang + " " + n1 + " " + n2 + " " + n3 + " " + n4 + " " + idist[0] + " " + idist[1] + " " + idist[2] + " " + idist[3] + " " + idist[4] + " " + idist[5] + " " + iwt + " " + tag);
                        continue;
                    }
                    iatbeg = res_beg[ires - 1];
                    if (ires != last_res) {
                        iatend = res_beg[ires] - 1;
                    } else {
                        iatend = numb_atoms;
                    }
                    int j = res_beg[ires - 1];
                    if ((l1[j - 1] == l1b[2]) || (l1[j - 1] == l1b[3]) || (l1[j - 1] == l1b[4])) {
                        for (i = iatbeg; i <= iatend; i++) {
                            if (l3[i - 1].equals(l3r[9])) {
                                n1 = i;
                            }
                            if (l3[i - 1].equals(l3r[10])) {
                                n2 = i;
                            }
                            if (l3[i - 1].equals(l3b[2])) {
                                n3 = i;
                            }
                            if (l3[i - 1].equals(l3b[3])) {
                                n4 = i;
                            }
                            if (l3[i - 1].equals(l3b[6])) {
                                n5 = i;
                            }
                        }
                    } else if ((l1[j - 1] == l1b[0]) || (l1[j - 1] == l1b[1])) {
                        for (i = iatbeg; i <= iatend; i++) {
                            if (l3[i - 1].equals(l3r[9])) {
                                n1 = i;
                            }
                            if (l3[i - 1].equals(l3r[10])) {
                                n2 = i;
                            }
                            if (l3[i - 1].equals(l3b[0])) {
                                n3 = i;
                            }
                            if (l3[i - 1].equals(l3b[1])) {
                                n4 = i;
                            }
                            if (l3[i - 1].equals(l3b[7])) {
                                n5 = i;
                            }
                        }
                    } else if (l1[j - 1] == l1b[5]) {
                        for (i = iatbeg; i <= iatend; i++) {
                            if (l3[i - 1].equals(l3r[9])) {
                                n1 = i;
                            }
                            if (l3[i - 1].equals(l3r[10])) {
                                n2 = i;
                            }
                            if (l3[i - 1].equals(l3b[2])) {
                                n3 = i;
                            }
                            if (l3[i - 1].equals(l3b[4])) {
                                n4 = i;
                            }
                            if (l3[i - 1].equals(l3b[6])) {
                                n5 = i;
                            }
                        }
                    } else if (l1[j - 1] == l1b[6]) {
                        for (i = iatbeg; i <= iatend; i++) {
                            if (l3[i - 1].equals(l3r[9])) {
                                n1 = i;
                            }
                            if (l3[i - 1].equals(l3r[10])) {
                                n2 = i;
                            }
                            if (l3[i - 1].equals(l3b[5])) {
                                n3 = i;
                            }
                            if (l3[i - 1].equals(l3b[3])) {
                                n4 = i;
                            }
                            if (l3[i - 1].equals(l3b[7])) {
                                n5 = i;
                            }
                        }
                    }
                    nx++;
                    outtorfixbin.println(nx + " " + ires + " " + iang + " " + n1 + " " + n2 + " " + n3 + " " + n4 + " " + idist[0] + " " + idist[1] + " " + idist[2] + " " + idist[3] + " " + idist[4] + " " + idist[5] + " " + iwt + " " + tag);

                    nx++;
                    outtorfixbin.println(nx + " " + ires + " " + iang + " " + n1 + " " + n2 + " " + n3 + " " + n5 + " " + idist[0] + " " + idist[1] + " " + idist[2] + " " + idist[3] + " " + idist[4] + " " + idist[5] + " " + iwt + " " + tag);
                    continue;
                }
                line = innuclindat.readLine();
                tok = new StringTokenizer(line);
                for (i = 0; i < 6; i++) {
                    th[i] = Double.parseDouble(tok.nextToken());
                }
                for (int k = 0; k < 6; k++) {
                    nb[k] = 0;
                }
                for (i = 0; i < 19; i++) {
                    line = inatomsdat.readLine();
                    tok = new StringTokenizer(line);
                    int nn = Integer.parseInt(tok.nextToken());
                    tok.nextToken();
                    tok.nextToken();
                    String il3 = tok.nextToken();
                    for (int k = 0; k < 6; k++) {
                        if (il3.equals(l3d[k])) {
                            nb[k] = nn;
                        }
                    }
                }
                nb[6] = nb[0];
                nb[7] = nb[1];
                nb[8] = nb[2];

                for (i = 0; i < 6; i++) {
                    nx++;
                    outtorfixbin.println(nx + " " + ires + " " + iang + " " + nb[i] + " " + nb[i + 1] + " " + nb[i + 2] + " " + nb[i + 3] + " " + idist[0] + " " + idist[1] + " " + idist[2] + " " + idist[3] + " " + idist[4] + " " + idist[5] + " " + iwt + " " + th[i]);
                }
            }
            inatomsdat.close();
            outtorfixbin.close();
    }

    void symtry() throws Exception {
        int nbe[] = new int[8];
        int nen[] = new int[8];
        int id[] = new int[8];
        int nchn;
        int knownr;
        double[][] r = new double[3][3];
        double[] t = new double[3];
        int[][] iat = new int[10000][8];
        int[][] iw = new int[10000][8];
        int[] iwg = new int[3];
        String idr1[] = {"P", "O5'", "O1P", "O2P", "O3'", "O3P"};
        String idr2[] = {"C5'", "C4'", "C3'", "C2'", "C1'", "O4'", "O2'"};
            inatomsdat = new BufferedReader(new FileReader(atomsdat));
            outsymtrybin = new PrintWriter(new FileWriter(symtrybin));
            String line = innuclindat.readLine();
            int igr = Integer.parseInt(line);
            outsymtrybin.println(igr);
            for (int ikj = 0; ikj < igr; ikj++) {
                line = innuclindat.readLine();
                StringTokenizer tok = new StringTokenizer(line);
                nchn = Integer.parseInt(tok.nextToken());
                knownr = Integer.parseInt(tok.nextToken());
                for (int k = 0; k < nchn; k++) {
                    id[k] = Integer.parseInt(tok.nextToken());
                }
                outsymtrybin.println(line);
                line = innuclindat.readLine();
                tok = new StringTokenizer(line);
                for (int k = 0; k < nchn; k++) {
                    nbe[k] = Integer.parseInt(tok.nextToken());
                }
                for (int k = 0; k < nchn; k++) {
                    nen[k] = Integer.parseInt(tok.nextToken());
                }
                if (knownr != 0) {
                    for (int k = 0; k < nchn; k++) {
                        line = innuclindat.readLine();
                        tok = new StringTokenizer(line);
                        for (int ik = 0; ik < 3; ik++) {
                            for (int j = 0; j < 3; j++) {
                                r[ik][j] = Double.parseDouble(tok.nextToken());
                            }
                        }
                        for (int ik = 0; ik < 3; ik++) {
                            t[ik] = Double.parseDouble(tok.nextToken());

                        }
                        outsymtrybin.println(line);
                    }
                }
                line = innuclindat.readLine();
                tok = new StringTokenizer(line);
                for (int i = 0; i < 3; i++) {
                    iwg[i] = Integer.parseInt(tok.nextToken());
                }
                int nk = 0;
                for (int k = 0; k < nchn; k++) {
                    int nip = 0;
                    nk = 0;
                    int i1 = nbe[k] - 1;
                    int i2 = nen[k] - i1;
                    inatomsdat.close();
                    inatomsdat = new BufferedReader(new FileReader(atomsdat));
                    if (i1 != 0) {
                        for (int i = 0; i < i1; i++) {
                            line = inatomsdat.readLine();
                        }
                    }

                    do {
                        line = inatomsdat.readLine();
                        tok = new StringTokenizer(line);
                        int nn = Integer.parseInt(tok.nextToken());
                        String l1 = tok.nextToken();
                        String l2 = tok.nextToken();
                        String l3 = tok.nextToken();
                        nip++;
                        nk++;
                        int iwt = 0;
                        boolean go1 = true, go2 = true;
                        for (int j = 0; j < 6; j++) {
                            if (l3.equals(idr1[j])) {
                                iwt = iwg[0];
                                go1 = false;
                                break;
                            }
                        }
                        if (go1) {
                            for (int j = 0; j < 7; j++) {
                                if (l3.equals(idr2[j])) {
                                    iwt = iwg[1];
                                    go2 = false;
                                    break;
                                }
                            }

                            if (go2) {
                                iwt = iwg[2];
                            }
                        }
                        iw[nk][k] = iwt;
                        iat[nk][k] = nn;
                    }
                    while (nip < i2);
                }
                outsymtrybin.println(nk);
                for (int n = 0; n < nk; n++) {
                    int kt = (nchn % 2) + 1;
                    String tmp = "" + iat[n][0];
                    for (int k = 1; k < nchn; k++) {
                        tmp += " " + iat[n][k];

                    }
                    outsymtrybin.println(n + " " + iw[n][kt] + " " + tmp);
                }
            }
            inatomsdat.close();
            outsymtrybin.close();
    }

    void pseu() throws Exception {
        int[] iatom = new int[9];
        int[] it = new int[5];
        int[][] iat = new int[4][4];

        double[] vn = new double[4];
        double[] dn = new double[5];
        double[] vp = new double[5];

        int na, nb, ntyp;
            inchirlsbin = new BufferedReader(new FileReader(chirlsbin));
            indistbin = new BufferedReader(new FileReader(distbin));

            outpseubin = new PrintWriter(new FileWriter(pseubin));
            outchirls2bin = new PrintWriter(new FileWriter(chirls2bin));
            outdistbin = new PrintWriter(new FileWriter(distbin));
            int nx = 0;
            int nbg = 0;

            String line = innuclindat.readLine();
            int nucl = Integer.parseInt(line);

            while (true) {
                line = innuclindat.readLine();
                StringTokenizer tok = new StringTokenizer(line);
                int ires = Integer.parseInt(tok.nextToken());
                int iwp = Integer.parseInt(tok.nextToken());
                int iwtm = Integer.parseInt(tok.nextToken());
                double tagp = Double.parseDouble(tok.nextToken());
                double tagtm = Double.parseDouble(tok.nextToken());
                if (ires != 0) {
                    vchrd(tagp, tagtm, vn, dn, vp);
                    int ned = ((ires - 1) * nucl) - nbg;
                    if (ned != 0) {
                        for (int j1 = 0; j1 < ned; j1++) {
                            line = inchirlsbin.readLine();
                            tok = new StringTokenizer(line);
                            for (int i = 0; i < 4; i++) {
                                iatom[i] = Integer.parseInt(tok.nextToken());
                            }
                            int vid = Integer.parseInt(tok.nextToken());
                            outchirls2bin.println(line);
                        }
                    }

                    for (int ik = 0; ik < nucl; ik++) {
                        line = inchirlsbin.readLine();
                        tok = new StringTokenizer(line);
                        for (int i = 0; i < 4; i++) {
                            iatom[i] = Integer.parseInt(tok.nextToken());
                        }
                        double vid = Integer.parseInt(tok.nextToken());

                        if (ik == 0) {
                            vid = vn[3];
                        }
                        if (nucl == 4 && ik == 1) {
                            vid = vn[1];
                        }
                        if (nucl == 3 && ik == 1) {
                            vid = vn[2];
                        }
                        if (nucl == 4 && ik == 2) {
                            vid = vn[2];
                        }
                        if (nucl == 4 && ik == 2) {
                            vid = vn[0];
                        }
                        if (ik == 3) {
                            vid = vn[0];

                        }
                        outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vid);

                        for (int kj = 0; kj < 4; kj++) {
                            iat[ik][kj] = iatom[kj];
                        }
                    }

                    it[0] = iat[0][3];
                    it[1] = iat[1][1];
                    it[2] = iat[1][0];
                    it[3] = iat[0][2];
                    it[4] = iat[0][0];
                    if (nucl == 3) {
                        it[1] = iat[2][0];
                        it[2] = iat[2][2];
                    }

                    for (int kj = 0; kj < 4; kj++) {
                        iatom[kj] = it[kj];
                        iatom[kj + 5] = it[kj];
                    }

                    iatom[4] = it[4];

                    for (int ik = 0; ik < 5; ik++) {
                        outchirls2bin.println(iatom[ik + 1] + " " + iatom[ik + 2] + " " + iatom[ik + 4] + " " + iatom[ik] + " " + vp[ik]);
                    }
                    nx++;
                    outpseubin.println(nx + " " + ires + " " + it[0] + " " + it[1] + " " + it[2] + " " + it[3] + " " + iwp + " " + iwtm + " " + tagp + " " + tagtm);
                    nbg += ned + nucl;
                    int nct = 0;
                    while ((line = indistbin.readLine()) != null) {
                        tok = new StringTokenizer(line);
                        na = Integer.parseInt(tok.nextToken());
                        nb = Integer.parseInt(tok.nextToken());
                        int ddst = Integer.parseInt(tok.nextToken());
                        ntyp = Integer.parseInt(tok.nextToken());
                        if (na == it[0] && nb == it[3]) {
                            outdistbin.println(na + " " + nb + " " + dn[0] + " " + ntyp);
                            nct++;
                            if (nct == 0) {
                                break;
                            } else {
                                continue;
                            }
                        }

                        if (na == it[0] && nb == it[3]) {
                            outdistbin.println(na + " " + nb + " " + dn[3] + " " + ntyp);
                            nct++;
                            if (nct == 0) {
                                break;
                            } else {
                                continue;
                            }
                        }

                        if (na == it[1] && nb == it[3]) {
                            outdistbin.println(na + " " + nb + " " + dn[1] + " " + ntyp);
                            nct++;
                            if (nct == 0) {
                                break;
                            } else {
                                continue;
                            }
                        }

                        if (na == it[1] && nb == it[4]) {
                            outdistbin.println(na + " " + nb + " " + dn[4] + " " + ntyp);
                            nct++;
                            if (nct == 0) {
                                break;
                            } else {
                                continue;
                            }
                        }

                        if (na == it[2] && nb == it[4]) {
                            outdistbin.println(na + " " + nb + " " + dn[2] + " " + ntyp);
                            nct++;
                            if (nct == 0) {
                                break;
                            } else {
                                continue;
                            }
                        }

                        outdistbin.println(line);
                    }
                } else {
                    break;
                }
            }

            while ((line = inchirlsbin.readLine()) != null) {
                outchirls2bin.println(line);
            }
            inchirlsbin.close();
            outpseubin.close();
            outchirls2bin.close();
            indistbin.close();
            outdistbin.close();

            //mv
            outchirlsbin.close();
            inchirls2bin.close();
            IoUtils.moveFile(new File(chirls2bin), new File(chirlsbin));
            inchirls2bin = new BufferedReader(new FileReader(chirlsbin));
    }

    /**
     * Replaces the values of the chiral volumes and of the sugar ring endocyclic
     * angles-distances according to the values of p and tm following empirical
     * equations.
     *
     * @param p  double
     * @param tm double
     * @param vn double[]
     * @param dn double[]
     * @param vp double[]
     */
    void vchrd(double p, double tm, double[] vn, double[] dn, double[] vp) {
        double[][][] a = {{{-2.4124, -0.0441, 0.0235, -0.4653}, {0.2933, 0.1274, 2.5618, 0.1488}}, {{-0.0183, 0.02346, -0.0773, 0.02985}, {2.5911, -0.1252, -0.0158, 0.01986}}, {{0.1158, 0.2719, -2.4699, 0.0293}, {-0.0033, -0.2371, -0.1246, 0.0014}}};
        double[][][] b = {{{2.3906, -0.0092, -0.2593, 0.2305, 2.4502}, {0.0051, -0.4390, 0.1902, 2.4589, 0.0010}}, {{-0.4218, 0.2247, 2.4064, 0.0080, -0.3129}, {0.1982, 2.3819, -0.0031, -0.4344, 0.1963}}};
        double[][] c = {{-0.6583, -7.4145, 0.7051, 7.8865, -0.7728}, {-8.3720, -0.7287, -8.0725, 0.6630, 7.4320}};
        double cp = Math.cos(p * TBMath.DegreeToRadian);
        double tm2 = tm * tm * 0.0001;
        for (int i = 0; i < 4; i++) {
            vn[i] = (a[0][0][i] + a[0][1][i] * tm2) + (a[1][0][i] + a[1][1][i] * tm2) * cp + (a[2][0][i] + a[2][1][i] * tm2) * cp * cp;
        }
        vn[0] = -vn[0];
        vn[3] = -vn[3];
        for (int i = 0; i < 5; i++) {
            dn[i] = (b[0][0][i] + b[0][1][i] * tm2) + (b[1][0][i] + b[1][1][i]) * Math.cos((2 * p - 72 * (i + 1)) * TBMath.DegreeToRadian);
        }
        for (int i = 0; i < 3; i++) {
            vp[i] = (c[0][i] + c[1][i] * tm2) * Math.cos((p - (108 + 36 * i)) * TBMath.DegreeToRadian);
        }
        for (int i = 3; i < 5; i++) {
            vp[i] = (c[0][i] + c[1][i] * tm2) * Math.cos((p - 36 * (i - 2)) * TBMath.DegreeToRadian);
        }
    }

    void sugar() throws Exception {
        int[] iatom = new int[4];
        int[][] iat = new int[4][4];
        int[] it = new int[9];

        double[] vn = {0.018, -1.198, 1.969, -1.884, 1.096};
        double[] vids = {2.52, 2.49, 2.74, 2.44};
        double[] vs = {-1.106, 1.835, -1.955, 1.240, -0.013};

            String line = innuclindat.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            int nucl = Integer.parseInt(tok.nextToken());
            int ntim = Integer.parseInt(tok.nextToken());
            if (ntim > 0) {
                int[] nn = new int[ntim];
                line = innuclindat.readLine();
                tok = new StringTokenizer(line);
                for (int i = 0; i < ntim; i++) {
                    if (tok.countTokens() == 0) {
                        line = innuclindat.readLine();
                        tok = new StringTokenizer(line);
                    }
                    nn[i] = Integer.parseInt(tok.nextToken());
                }
                inchirlsbin = new BufferedReader(new FileReader(chirlsbin));
                outchirls2bin = new PrintWriter(new FileWriter(chirls2bin));
                for (int k = 0; k < ntim; k++) {
                    if (nn[k] == 0) {
                        for (int ik = 0; ik < nucl; ik++) {
                            line = inchirlsbin.readLine();
                            if (line != null) {
                                tok = new StringTokenizer(line);
                                for (int i = 0; i < 4; i++) {
                                    iatom[i] = Integer.parseInt(tok.nextToken());
                                }
                                double vid = Double.parseDouble(tok.nextToken());
                                outchirls2bin.println(line);
                                for (int kj = 0; kj < 4; kj++) {
                                    iat[ik][kj] = iatom[kj];
                                }
                            }
                        }
                        it[0] = iat[0][3];
                        it[1] = iat[1][1];
                        it[2] = iat[1][0];
                        it[3] = iat[0][2];
                        it[4] = iat[0][0];

                        if (nucl != 4) {
                            it[1] = iat[2][0];
                            it[2] = iat[2][2];
                        }


                        for (int kj = 0; kj < 4; kj++) {
                            it[kj + 5] = it[kj];
                        }
                        for (int ik = 0; ik < 5; ik++) {
                            outchirls2bin.println(it[ik + 1] + " " + it[ik + 2] + " " + it[ik + 4] + " " + it[ik] + " " + vn[ik]);
                        }
                    } else if (nn[k] != 2) {
                        for (int ik = 0; ik < nucl; ik++) {
                            line = inchirlsbin.readLine();
                            if (line != null) {
                                tok = new StringTokenizer(line);
                                for (int i = 0; i < 4; i++) {
                                    iatom[i] = Integer.parseInt(tok.nextToken());
                                }
                                double vid = Double.parseDouble(tok.nextToken());
                                if (nucl == 4) {
                                    outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[ik]);
                                } else {
                                    if (ik == 0) {
                                        outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[0]);
                                    }
                                    if (ik == 1) {
                                        outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[2]);
                                    }
                                    if (ik == 2) {
                                        outchirls2bin.println(iatom[0] + " " + iatom[1] + " " + iatom[2] + " " + iatom[3] + " " + vids[3]);
                                    }
                                }
                                for (int kj = 0; kj < 4; kj++) {
                                    iat[ik][kj] = iatom[kj];
                                }
                            }
                        }
                        it[0] = iat[0][3];
                        it[1] = iat[1][1];
                        it[2] = iat[1][0];
                        it[3] = iat[0][2];
                        it[4] = iat[0][0];

                        if (nucl != 4) {
                            it[1] = iat[2][0];
                            it[2] = iat[2][2];
                        }

                        for (int kj = 0; kj < 4; kj++) {
                            it[kj + 5] = it[kj];
                        }
                        for (int ik = 0; ik < 5; ik++) {
                            outchirls2bin.println(it[ik + 1] + " " + it[ik + 2] + " " + it[ik + 4] + " " + it[ik] + " " + vs[ik]);
                        }
                    } else {
                        for (int ik = 0; ik < nucl; ik++) {
                            line = inchirlsbin.readLine();
                            if (line != null) {
                                tok = new StringTokenizer(line);
                                for (int i = 0; i < 4; i++) {
                                    iatom[i] = Integer.parseInt(tok.nextToken());
                                }
                                int vid = Integer.parseInt(tok.nextToken());
                                outchirls2bin.println(line);
                            }
                        }
                    }
                }

                while ((line = inchirlsbin.readLine()) != null) {
                    tok = new StringTokenizer(line);
                    for (int i = 0; i < 4; i++) {
                        iatom[i] = Integer.parseInt(tok.nextToken());
                    }
                    double vid = Double.parseDouble(tok.nextToken());
                    outchirls2bin.println(line);
                }
                inchirlsbin.close();
                outchirls2bin.close();

                //mv
                outchirlsbin.close();
                IoUtils.moveFile(new File(chirls2bin), new File(chirlsbin));
            }
    }


    public String scratch(int n1, String a1, int n2, String a2, double d, int j) throws Exception {
        String ret = "";
        ret += "    " + TBMath.intFormat(n1, 3) + a1;
        ret += "    " + TBMath.intFormat(n2, 3) + a2;
        ret += "     " + TBMath.doubleFormat(d, 5, 3);
        ret += "    " + TBMath.intFormat(j, 1);
        return ret;
    }
}
