package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.utils.HD;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.TBMath;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Java version of nuclsq.f 13/09/2005 <br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original nuclsq.f is at the end of this file<br/>
 * <br/>
 * PROGRAM NUCLSQ
 * <ul>
 * <li>03/1987</li>
 * <li>11/1987</li>
 * <li>02/1994</li>
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

public class NucLSQ {
    NucMult nucMult;

    public NucLSQ(NucMult nucMult) throws Exception {
        this.nucMult = nucMult;
        this.tmpPath = nucMult.tmpPath;
        inPath = tmpPath/*+"outnuclin/"*/;
        outPath = tmpPath/*+"outnuclsq/"*/;
    }

    /**
     * path
     */
    public String tmpPath;
    public String inPath;
    public String outPath;

    private double ak[] = new double[350000];
    private double bk[] = new double[35000];
    private double ck[] = new double[35000];
    public PrintWriter isysw, ipnch, jdisk, idisk, ifofc, ishftw;
    private BufferedReader isysr, iatmr, irefr, ishftr;

    private int na = 0, nv = 0, ndis = 0, npln = 0, nchr = 0, nvdw = 0, nocc = 0, itemp = 0, isp = 0;
    private double wpsum = 0, wbsum = 0, wqsum = 0;

    private int nbet = 0, iatmn = 0, ibet1 = 0, ibetn = 0, nsf = 0;

    private Date firstCall;

    private String atom[] = new String[9000];
    private double[][] xyz = new double[9000][3];
    private double[] bet = new double[9000];
    private int[] isf = new int[9000];
    private double[] qocc = new double[9000];

    private int[] no = new int[35000];
    private int[] nt = new int[35000];
    private int[] nwtb = new int[35000];

    private double ctb[] = new double[1024];
    private double stb[] = new double[1024];
    private double etb[] = new double[1024];
    private int ipostv = 0;

    private int[] h = new int[4];

    private double[] fii = new double[10];
    private double[] sc = new double[18];
    private double to = 0;
    private double killdv = 0;

    private double wdskal = 0;
    private double wpskal = 0;
    private double wbskal = 0;
    private double wcskal = 0;
    private double wvskal = 0;
    private double wtskal = 0;
    private double wpckal = 0;
    private double wsskal = 0;
    private double wqskal = 0;
    private double sigd1 = 0;
    private double sigd2 = 0;
    private double sigd3 = 0;
    private double sigd4 = 0;
    private double sigd5 = 0;
    private double sigp = 0;
    private double sigb1 = 0;
    private double sigb2 = 0;
    private double sigb3 = 0;
    private double sigb4 = 0;
    private double sigb5 = 0;
    private double sigc = 0;
    private double sigv = 0;
    private double sigt1 = 0;
    private double sigt2 = 0;
    private double sigt3 = 0;
    private double sigt4 = 0;
    private double sigp1 = 0;
    private double sigp2 = 0;
    private double sigp3 = 0;
    private double sigp4 = 0;
    private double sigsp1 = 0;
    private double sigsp2 = 0;
    private double sigsp3 = 0;
    private double sigsb1 = 0;
    private double sigsb2 = 0;
    private double sigsb3 = 0;
    private double[] dinc = new double[3];
    private double a1 = 1, a2 = 1, a3 = 1, al = 90, be = 90, ga = 90;

    private double lgx = 0, lgy = 0, lgz = 0, nvl = 0, qbar = 0, lq = 0;
    private int l2 = 0, l3 = 0;

    private double[] bidon = new double[128000];
    private double[] sk = new double[35000];
    private double[] p = new double[35000];
    private double[] f = new double[35000];
    private double[] aii = new double[35000];
    private double[] dis = new double[35000];
    private double[] disa = new double[35000];
    private int[] lo = new int[35000];

    private int[] lt = new int[35000];
    private double[][] dir = new double[4][3];

    public int main() throws Exception {
        int distances_from_deviation = Integer.MAX_VALUE;
        double[] kill = new double[9000];
        int kiat[] = new int[15];
        int nsym[] = new int[4];
        String title = "";

        String shftsbin = tmpPath + "SHFTS.BIN";
        String shftsbisbin = tmpPath + "SHFTS_BIS.BIN";
        double[] shftk = new double[10];
        double damp[] = new double[1000];
        double damb[] = new double[1000];
        double damq[] = new double[1000];
        String label1 = "         CONTROL CARD LISTINGS          ";
        String label2 = "     DISTANCE RESTRAINT INFORMATION     ";
        String label3 = "      PLANE RESTRAINT INFORMATION       ";
        String label4 = "      STRUCTURE FACTOR INFORMATION      ";
        String label5 = "      CONJUGATE GRADIENT SOLUTION       ";
        String label6 = "            PARAMETER SHIFTS            ";
        String label7 = "             R-VALUE SURVEY             ";
        String label8 = "   ISOTROPIC THERMAL FACTOR RESTRAINTS  ";
        String label9 = "      NON-BONDED CONTACT RESTRAINTS     ";
        String labela = "        CHIRAL CENTER RESTRAINTS        ";
        String labelb = "      CONFORMATIONAL TORSION ANGLES     ";
        String labelc = "      NON-CRYSTALLOGRAPHIC SYMMETRY     ";
        String labeld = "       PSEUDOROTATION RESTRAINTS        ";
        String labele = "      OCCUPANCY FACTOR RESTRAINTS       ";
        double[] dmin = {5.0, 3.0, 2.5, 2.0, 1.8, 1.5, 1.3};
        int report = 0;

        int nmtrx = 350000;
        int nvect = 35000;
        int mxatm = 9000;
        int mxdis = 35000;
        int mxocc = 9000;
        wpsum = 0;
        wbsum = 0;
        wqsum = 0;
        isysr = new BufferedReader(new FileReader(inPath + "LSQ.DAT"));
        isysw = new PrintWriter(outPath + "LSQ.OUT");
        iatmr = new BufferedReader(new FileReader(inPath + "LSQ.INP"));
        for (int i = 0; i < mxatm; i++) {
            kill[i] = 0;
        }
        title = isysr.readLine();
        clck(0, isysw);
        String line = isysr.readLine();
        StringTokenizer tok = new StringTokenizer(line);
        int ncyccg = Integer.parseInt(tok.nextToken());
        int listf = Integer.parseInt(tok.nextToken());
        int lista = Integer.parseInt(tok.nextToken());
        lgx = Integer.parseInt(tok.nextToken());
        lgy = Integer.parseInt(tok.nextToken());
        lgz = Integer.parseInt(tok.nextToken());
        lq = Integer.parseInt(tok.nextToken());
        report = Integer.parseInt(tok.nextToken());
        int idaliz = Integer.parseInt(tok.nextToken());
        isp = Integer.parseInt(tok.nextToken());
        nsf = Integer.parseInt(tok.nextToken());
        double dvpcut = 0.01 * lista;
        if (ncyccg <= 0) {
            ncyccg = 50;
        }
        if (lq <= 0) {
            lq = 10;
        }
        isysw.println("\n" + TBMath.intFormat((int) ncyccg, 5) + TBMath.intFormat((int) listf, 5) + TBMath.intFormat((int) lista, 5) + TBMath.intFormat((int) lgx, 5) + TBMath.intFormat((int) lgy, 5) + TBMath.intFormat((int) lgz, 5) + TBMath.intFormat((int) lq, 5) + TBMath.intFormat(report, 5) + TBMath.intFormat(idaliz, 5) + TBMath.intFormat((int) isp, 5) + TBMath.intFormat((int) nsf, 5));
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        na = Integer.parseInt(tok.nextToken());
        ndis = Integer.parseInt(tok.nextToken());
        npln = Integer.parseInt(tok.nextToken());
        nchr = Integer.parseInt(tok.nextToken());
        nvdw = Integer.parseInt(tok.nextToken());
        int ntor = Integer.parseInt(tok.nextToken());
        nsym[0] = Integer.parseInt(tok.nextToken());
        nsym[1] = Integer.parseInt(tok.nextToken());
        nsym[2] = Integer.parseInt(tok.nextToken());
        nsym[3] = Integer.parseInt(tok.nextToken());
        nocc = Integer.parseInt(tok.nextToken());
        itemp = Integer.parseInt(tok.nextToken());
        int nsgr = Integer.parseInt(tok.nextToken());
        isysw.println(TBMath.intFormat((int) na, 5) + TBMath.intFormat((int) ndis, 5) + TBMath.intFormat((int) npln, 5) + TBMath.intFormat((int) nchr, 5) + TBMath.intFormat((int) nvdw, 5) + TBMath.intFormat((int) ntor, 5) + TBMath.intFormat((int) nsym[0], 5) + TBMath.intFormat((int) nsym[1], 5) + TBMath.intFormat((int) nsym[2], 5) + TBMath.intFormat((int) nsym[3], 5) + TBMath.intFormat((int) nocc, 5) + TBMath.intFormat((int) itemp, 5) + TBMath.intFormat((int) nsgr, 5));
        if (idaliz == 1) {
            nocc = 0;
            itemp = 0;
            nsf = 5;
        }
        if (itemp == 0) {
            nv = 3 * na + nocc + 2;
        }
        if (itemp == 1) {
            nv = 4 * na + nocc + 1;
        }
        nbet = na;
        if (itemp == 0) {
            nbet = 1;
        }
        iatmn = 3 * na + 1;
        ibet1 = iatmn + 1;
        ibetn = iatmn + nbet;
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        int nkill = Integer.parseInt(tok.nextToken());
        String tmp2 = TBMath.intFormat(nkill, 5);
        for (int i = 0; i < nkill; i++) {
            kiat[i] = Integer.parseInt(tok.nextToken());
            tmp2 += TBMath.intFormat(kiat[i], 5);
        }
        isysw.println(tmp2);
        if (nkill != 0) {
            for (int i = 0; i < nkill; i++) {
                kill[kiat[i] - 1] = 1;
            }
        }
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        a1 = Double.parseDouble(tok.nextToken());
        a2 = Double.parseDouble(tok.nextToken());
        a3 = Double.parseDouble(tok.nextToken());
        al = Double.parseDouble(tok.nextToken());
        be = Double.parseDouble(tok.nextToken());
        ga = Double.parseDouble(tok.nextToken());
        isysw.println(TBMath.doubleFormat(a1, 8, 3) + TBMath.doubleFormat(a2, 8, 3) + TBMath.doubleFormat(a3, 8, 3) + TBMath.doubleFormat(al, 8, 3) + TBMath.doubleFormat(be, 8, 3) + TBMath.doubleFormat(ga, 8, 3));
        double ca = Math.cos(al * TBMath.DegreeToRadian);
        double cb = Math.cos(be * TBMath.DegreeToRadian);
        double cg = Math.cos(ga * TBMath.DegreeToRadian);
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        int nobs = Integer.parseInt(tok.nextToken());
        double fmin = Double.parseDouble(tok.nextToken());
        double smin = Double.parseDouble(tok.nextToken());
        double smax = Double.parseDouble(tok.nextToken());
        double sigmin = Double.parseDouble(tok.nextToken());
        isysw.println(TBMath.intFormat(nobs, 10) + TBMath.doubleFormat(fmin, 10, 6) + TBMath.doubleFormat(smin, 10, 6) + TBMath.doubleFormat(smax, 10, 6) + TBMath.doubleFormat(sigmin, 10, 6));
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        int n = Integer.parseInt(tok.nextToken());
        tmp2 = TBMath.intFormat(n, 5);
        for (int i = 0; i < n; i++) {
            dmin[i] = Double.parseDouble(tok.nextToken());
            tmp2 += TBMath.doubleFormat(dmin[i], 5, 2);
        }
        isysw.println(tmp2);
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        int kfwgt = Integer.parseInt(tok.nextToken());
        double afsig = Double.parseDouble(tok.nextToken());
        double bfsig = Double.parseDouble(tok.nextToken());
        wdskal = Double.parseDouble(tok.nextToken());
        sigd1 = Double.parseDouble(tok.nextToken());
        sigd2 = Double.parseDouble(tok.nextToken());
        sigd3 = Double.parseDouble(tok.nextToken());
        sigd4 = Double.parseDouble(tok.nextToken());
        sigd5 = Double.parseDouble(tok.nextToken());
        isysw.println(TBMath.intFormat(kfwgt, 8) + TBMath.doubleFormat(afsig, 8, 3) + TBMath.doubleFormat(bfsig, 8, 3) + "        " + TBMath.doubleFormat(wdskal, 8, 3) + TBMath.doubleFormat(sigd1, 8, 3) + TBMath.doubleFormat(sigd2, 8, 3) + TBMath.doubleFormat(sigd3, 8, 3) + TBMath.doubleFormat(sigd4, 8, 3) + TBMath.doubleFormat(sigd5, 8, 3));
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        wpskal = Double.parseDouble(tok.nextToken());
        sigp = Double.parseDouble(tok.nextToken());
        wcskal = Double.parseDouble(tok.nextToken());
        sigc = Double.parseDouble(tok.nextToken());
        wbskal = Double.parseDouble(tok.nextToken());
        sigb1 = Double.parseDouble(tok.nextToken());
        sigb2 = Double.parseDouble(tok.nextToken());
        sigb3 = Double.parseDouble(tok.nextToken());
        sigb4 = Double.parseDouble(tok.nextToken());
        sigb5 = Double.parseDouble(tok.nextToken());
        if (tok.countTokens() == 0) {
            isysw.println(TBMath.doubleFormat(wpskal, 8, 3) + TBMath.doubleFormat(sigp, 8, 3) + TBMath.doubleFormat(wcskal, 8, 3) + TBMath.doubleFormat(sigc, 8, 3) + TBMath.doubleFormat(wbskal, 8, 3) + TBMath.doubleFormat(sigb1, 8, 3) + TBMath.doubleFormat(sigb2, 8, 3) + TBMath.doubleFormat(sigb3, 8, 3) + TBMath.doubleFormat(sigb4, 8, 3) + TBMath.doubleFormat(sigb5, 8, 3));
            line = isysr.readLine();
            tok = new StringTokenizer(line);
        }
        wvskal = Double.parseDouble(tok.nextToken());
        sigv = Double.parseDouble(tok.nextToken());
        dinc[0] = Double.parseDouble(tok.nextToken());
        dinc[1] = Double.parseDouble(tok.nextToken());
        dinc[2] = Double.parseDouble(tok.nextToken());
        wtskal = Double.parseDouble(tok.nextToken());
        sigt1 = Double.parseDouble(tok.nextToken());
        sigt2 = Double.parseDouble(tok.nextToken());
        sigt3 = Double.parseDouble(tok.nextToken());
        sigt4 = Double.parseDouble(tok.nextToken());
        if (tok.countTokens() == 0) {
            isysw.println(TBMath.doubleFormat(wvskal, 8, 3) + TBMath.doubleFormat(sigv, 8, 3) + TBMath.doubleFormat(dinc[0], 8, 3) + TBMath.doubleFormat(dinc[1], 8, 3) + TBMath.doubleFormat(dinc[2], 8, 3) + TBMath.doubleFormat(wtskal, 8, 3) + TBMath.doubleFormat(sigt1, 8, 3) + TBMath.doubleFormat(sigt2, 8, 3) + TBMath.doubleFormat(sigt3, 8, 3) + TBMath.doubleFormat(sigt4, 8, 3));
            line = isysr.readLine();
            tok = new StringTokenizer(line);
        }

        double pdel = Double.parseDouble(tok.nextToken());
        double bdel = Double.parseDouble(tok.nextToken());
        double qdel = Double.parseDouble(tok.nextToken());
        wsskal = Double.parseDouble(tok.nextToken());
        sigsp1 = Double.parseDouble(tok.nextToken());
        sigsp2 = Double.parseDouble(tok.nextToken());
        sigsp3 = Double.parseDouble(tok.nextToken());
        sigsb1 = Double.parseDouble(tok.nextToken());
        sigsb2 = Double.parseDouble(tok.nextToken());
        sigsb3 = Double.parseDouble(tok.nextToken());
        if (tok.countTokens() == 0) {
            isysw.println(TBMath.doubleFormat(pdel, 8, 3) + TBMath.doubleFormat(bdel, 8, 3) + TBMath.doubleFormat(qdel, 8, 3) + TBMath.doubleFormat(wsskal, 8, 3) + TBMath.doubleFormat(sigsp1, 8, 3) + TBMath.doubleFormat(sigsp2, 8, 3) + TBMath.doubleFormat(sigsp3, 8, 3) + TBMath.doubleFormat(sigsb1, 8, 3) + TBMath.doubleFormat(sigsb2, 8, 3) + TBMath.doubleFormat(sigsb3, 8, 3));
            line = isysr.readLine();
            tok = new StringTokenizer(line);
        }

        wpckal = Double.parseDouble(tok.nextToken());
        sigp1 = Double.parseDouble(tok.nextToken());
        sigp2 = Double.parseDouble(tok.nextToken());
        sigp3 = Double.parseDouble(tok.nextToken());
        sigp4 = Double.parseDouble(tok.nextToken());
        wqskal = Double.parseDouble(tok.nextToken());
        isysw.println(TBMath.doubleFormat(wpckal, 8, 3) + TBMath.doubleFormat(sigp1, 8, 3) + TBMath.doubleFormat(sigp2, 8, 3) + TBMath.doubleFormat(sigp3, 8, 3) + TBMath.doubleFormat(sigp4, 8, 3) + TBMath.doubleFormat(wqskal, 8, 3));
        line = isysr.readLine();
        tok = new StringTokenizer(line);
        to = Double.parseDouble(tok.nextToken());
        tmp2 = TBMath.doubleFormat(to, 8, 3);
        int nq = Integer.parseInt(tok.nextToken());
        tmp2 += TBMath.intFormat(nq, 8);
        for (int i = 0; i < nq; i++) {
            sc[i] = Double.parseDouble(tok.nextToken());
            tmp2 += TBMath.doubleFormat(sc[i], 8, 5);
        }
        isysw.println(tmp2);

        line = isysr.readLine();
        tok = new StringTokenizer(line);
        int jabn = Integer.parseInt(tok.nextToken());
        tmp2 = TBMath.intFormat(jabn, 5);
        for (int i = 0; i < 15; i++) {
            if (tok.countTokens() == 0) {
                break;
            }
            damp[i] = Double.parseDouble(tok.nextToken());
            tmp2 += TBMath.doubleFormat(damp[i], 5, 2);
        }
        isysw.println(tmp2);
        tmp2 = "";
        if (jabn > 15) {
            line = isysr.readLine();
            tok = new StringTokenizer(line);
            for (int i = 15; i < jabn; i++) {
                if (tok.countTokens() == 0) {
                    break;
                }
                damp[i] = Double.parseDouble(tok.nextToken());
                tmp2 += TBMath.doubleFormat(damp[i], 5, 2);
            }
            isysw.println(tmp2);
        }
        if (itemp != 0) {
            line = isysr.readLine();
            tmp2 = "";
            tok = new StringTokenizer(line);
            for (int i = 0; i < 15; i++) {
                if (tok.countTokens() == 0) {
                    break;
                }
                damb[i] = Double.parseDouble(tok.nextToken());
                tmp2 += TBMath.doubleFormat(damb[i], 5, 2);
            }
            isysw.println(tmp2);
            if (jabn > 15) {
                line = isysr.readLine();
                tmp2 = "";
                tok = new StringTokenizer(line);
                for (int i = 15; i < jabn; i++) {
                    if (tok.countTokens() == 0) {
                        break;
                    }
                    damb[i] = Double.parseDouble(tok.nextToken());
                    tmp2 += TBMath.doubleFormat(damb[i], 5, 2);
                }
                isysw.println(tmp2);
            }
        } else {
            isysw.println();
        }
        if (nocc != 0) {
            line = isysr.readLine();
            tmp2 = "";
            tok = new StringTokenizer(line);
            for (int i = 0; i < 15; i++) {
                if (tok.countTokens() == 0) {
                    break;
                }
                damq[i] = Double.parseDouble(tok.nextToken());
                tmp2 += TBMath.doubleFormat(damq[i], 5, 2);
            }
            isysw.println(tmp2);
            if (jabn > 15) {
                line = isysr.readLine();
                tmp2 = "";
                tok = new StringTokenizer(line);
                for (int i = 15; i < jabn; i++) {
                    if (tok.countTokens() == 0) {
                        break;
                    }
                    damq[i] = Double.parseDouble(tok.nextToken());
                    tmp2 += TBMath.doubleFormat(damq[i], 5, 2);
                    isysw.println(tmp2);
                }
            }
        } else {
            isysw.println();
        }
        int jmtrx = 0;
        if (itemp == 0) {
            jmtrx = 6 * na + 9 * ndis + nocc + 2;
        }
        if (itemp == 1) {
            jmtrx = 7 * na + 10 * ndis + nocc + 1;
        }
        isysw.println(TBMath.intFormat(na, 9) + " ATOMS\n" + TBMath.intFormat(ndis, 9) + " DISTANCES\n" + TBMath.intFormat(npln, 9) + " PLANES\n" + TBMath.intFormat(nchr, 9) + " CHIRAL CENTERS\n" + TBMath.intFormat(nvdw, 9) + " POSSIBLE CONTACTS\n" + TBMath.intFormat(ntor, 9) + " TORSION ANGLES\n\n" + TBMath.intFormat(nv, 9) + " VARIABLES\n" + TBMath.intFormat(jmtrx, 9) + " MATRIX ELEMENTS\n");
        if (jmtrx > nmtrx || nv > nvect) {
            isysw.println("THE NUMBER OF VECTOR OR MATRIX ELEMENTS EXCEEDS AVAILABLE STORAGE OF " + nvect + " OR " + nmtrx + ", RESPECTIVELY COMPUTATION HALTED.");
        } else {
            if (na > mxatm || ndis > mxdis) {
                isysw.println(" THE NUMBER OF ATOMS OR DISTANCES EXCEEDS AVAILABLE STORAGE OF " + mxatm + " OR " + mxdis + ", RESPECTIVELY'/' COMPUTATION HALTED.");
            } else {
                if (nocc > mxocc) {
                    isysw.println("THE NUMBER OF OCCUPANCY FACTORS EXCEEDS AVAILABLE STORAGE OF " + mxocc + " COMPUTATION HALTED.");
                } else {
                    int iqzero = na - nocc;
                    int nloop = 0;
                    for (int i = 0; i < na; i++) {
                        line = iatmr.readLine();
                        HD hd= new HD(line);
                        int nix = hd.atomNumber;
                        atom[i] = line.substring(7, 14);
                        isf[i] = hd.atomCode;
                        xyz[i][0] = hd.x;
                        xyz[i][1] = hd.y;
                        xyz[i][2] = hd.z;
                        bet[i] = hd.b;
                        qocc[i] = hd.q;
                        if (idaliz != 1) {
                            if (isf[i] > nsf) {
                                isysw.println("Scattering factor for '" + atom[i] + "' incorrect ");
                                nloop = 1;
                            }
                        }
                    }
                    if (nloop != 1) {
                        for (int i = 0; i < na; i++) {
                            bet[i] += to;
                        }
                        if (jabn <= 0) {
                            ishftw = new PrintWriter(shftsbin);
                        }
                        double sksh = 1;

                        if (jabn > 0) {
                            for (int i = 0; i < nvect; i++) {
                                ck[i] = 0;
                            }

                            ishftr = new BufferedReader(new FileReader(shftsbin));
                            ishftw = new PrintWriter(shftsbisbin);
                            double pang = 0;
                            for (int jab1 = 0; jab1 < jabn; jab1++) {
                                line = ishftr.readLine();
                                ishftw.println(line);
                                tok = new StringTokenizer(line);
                                for (int ic = 0; ic < nv; ic++) {
                                    bk[ic] = Double.parseDouble(tok.nextToken());
                                }
                                sksh = damp[jab1];
                                if (sksh > 0) {
                                    for (int i = 1; i <= na; i++) {
                                        xyz[i - 1][0] += bk[3 * i - 2] * sksh;
                                        xyz[i - 1][1] += bk[3 * i - 1] * sksh;
                                        xyz[i - 1][2] += bk[3 * i] * sksh;
                                        ck[i - 1] += bk[3 * i - 2] * sksh;
                                        ck[i + na - 1] += bk[3 * i - 1] * sksh;
                                        ck[i + 2 * na - 1] += bk[3 * i] * sksh;
                                    }
                                }
                                if (itemp != 0) {
                                    sksh = damb[jab1];
                                    if (sksh > 0) {
                                        int nbets = na;
                                        if (nocc > 0 && damq[jab1] > 0) {
                                            nbets = na - nocc;
                                        }
                                        for (int i = 0; i < nbets; i++) {
                                            bet[i] += bk[iatmn + i] * sksh;
                                            if (bet[i] < sc[1]) {
                                                bet[i] = sc[1];
                                            }
                                        }
                                    }
                                }
                                if (nocc != 0) {
                                    sksh = damq[jab1];
                                    if (sksh > 0) {
                                        int iqz = iqzero + 1;
                                        for (int i = iqz - 1; i < na; i++) {
                                            qocc[i] += bk[ibetn + i - iqzero] * sksh;
                                            if (qocc[i] < 0.01) {
                                                qocc[i] = 0.01;
                                            }
                                            if (qocc[i] > 1) {
                                                qocc[i] = 1;
                                            }
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < na; i++) {
                                double dva = ck[i] * a1;
                                double dvb = ck[i + na] * a2;
                                double dvc = ck[i + na + na] * a3;
                                double dvp = dva * dva + dvb * dvb + dvc * dvc + 2 * (dvb * dvc * ca + dva * dvc * cb + dva * dvb * cg);
                                pang += dvp;
                            }
                            pang = Math.sqrt(pang / na);
                            isysw.println(TBMath.doubleFormat(pang, 9, 3) + " ANGSTROMS IS THE RMS TOTAL SHIFT FROM INITIAL ATOMIC POSITIONS");
                            ishftr.close();

                            boolean old = !true;
                            File tmpF = new File(shftsbisbin);
                            long length = tmpF.length();
                            tmpF = null;
                            System.gc();
                            if (old) {
                                new File(shftsbisbin).renameTo(new File(shftsbin));
                            } else {
                                ishftw.close();

                                IoUtils.copyFile(new File(shftsbisbin), new File(shftsbin));
                                ishftw = new PrintWriter(new FileWriter(shftsbin, true));

                                File s = new File(shftsbisbin);
                                if (s.exists())
                                    if (!s.delete());
                            }
                        }

                        int no = 0;
                        if (nobs != 0) {
                            double zap = Math.PI / 512;
                            for (int i = 0; i < 1024; i++) {
                                double arge1 = i * 0.01;
                                etb[i] = Math.exp(-arge1);
                                double arge = i * zap;
                                ctb[i] = Math.cos(arge);
                                stb[i] = Math.sin(arge);
                            }

                            int multip = 1024 * 1024;
                            ipostv = multip * 1024;
                            idisk = new PrintWriter(tmpPath + "FOR003.TMP");
                            irefr = new BufferedReader(new FileReader(outPath + "LSQ.HKL"));
                            for (int iobs = 0; iobs < nobs; iobs++) {
                                line = irefr.readLine();
                                if (line == null) {
                                    break;
                                }
                                tok = new StringTokenizer(line);
                                int ih1 = Integer.parseInt(tok.nextToken());
                                int ih2 = Integer.parseInt(tok.nextToken());
                                int ih3 = Integer.parseInt(tok.nextToken());
                                double yo = Double.parseDouble(tok.nextToken());
                                double sigyoa = Double.parseDouble(tok.nextToken());
                                double sthol = Double.parseDouble(tok.nextToken());
                                for (int i = 0; i < nsf; i++) {
                                    fii[i] = Double.parseDouble(tok.nextToken());
                                }
                                if (sthol < smin || sthol > smax) {
                                    continue;
                                }
                                if (yo < fmin || yo < sigmin * sigyoa) {
                                    continue;
                                }
                                h[0] = ih1;
                                h[1] = ih2;
                                h[2] = ih3;
                                h[3] = 1;
                                String tmp = h[0] + " " + h[1] + " " + h[2] + " " + h[3] + " " + yo + " " + sigyoa + " " + sthol;
                                for (int nng = 0; nng < nsf; nng++) {
                                    tmp += " " + fii[nng];
                                }
                                idisk.println(tmp);
                                no++;
                            }
                            irefr.close();
                        }
                        isysw.println(TBMath.intFormat(no, 9) + " STRUCTURE FACTOR OBSERVATIONS");
                        if (report != 2) {
                            for (int i = 0; i < nmtrx; i++) {
                                ak[i] = 0;
                            }
                            for (int i = 0; i < nvect; i++) {
                                bk[i] = 0;
                            }
                            if (pdel != 0) {
                                for (int ia = 1; ia <= na; ia++) {
                                    ak[6 * ia - 5] = (a1 / pdel) * (a1 / pdel);
                                    ak[6 * ia - 2] = (a2 / pdel) * (a2 / pdel);
                                    ak[6 * ia] = (a3 / pdel) * (a3 / pdel);
                                }
                            }
                            isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label2 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                            distances_from_deviation = disref();
                            isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label3 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                            int nplat = plnref();
                            isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + labela + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                            chiref();
                            isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label9 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                            vdwref();
                            ndis += nvdw;
                            if (itemp == 0) {
                                jmtrx = 6 * na + 9 * ndis + nocc + 2;
                            }
                            if (itemp == 1) {
                                jmtrx = 7 * na + 10 * ndis - nvdw + nocc + 1;
                            }
                            isysw.println("\n\n\n THERE NOW ARE \n" + TBMath.intFormat(nvdw, 9) + " ACTUAL CONTACTS \n" + TBMath.intFormat(ndis, 9) + " TOTAL DISTANCES \n" + TBMath.intFormat(jmtrx, 9) + " MATRIX ELEMENTS");
                            if (jmtrx > nmtrx) {
                                isysw.println(" THE NUMBER OF VECTOR OR MATRIX ELEMENTS EXCEEDS AVAILABLE STORAGE OF " + nvect + " OR " + nmtrx + " RESPECTIVELY COMPUTATION HALTED.");
                            } else {
                                if (ndis > mxdis) {
                                    isysw.println(" THE NUMBER OF ATOMS OR DISTANCES EXCEEDS AVAILABLE STORAGE OF " + mxatm + " OR " + mxdis + ", RESPECTIVELY'/' COMPUTATION HALTED.");
                                } else {
                                    if (ntor != 0) {
                                        isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + labelb + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                        torref(ntor);
                                    }

                                    if (nsgr != 0) {
                                        isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + labeld + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                        nsgr = pseudo(nsgr);
                                    }
                                    int l = 6 * na + 9 * ndis + 1;
                                    if (itemp != 0) {
                                        if (bdel != 0) {
                                            for (int ia = 0; ia < na; ia++) {
                                                ak[l + ia] = 1 / (bdel * bdel);
                                            }
                                        }
                                    }
                                    if (nsym[0] != 0) {
                                        isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + labelc + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                        symref(nsym);
                                    }
                                    if (qdel != 0) {
                                        if (itemp == 0) {
                                            l = 6 * na + 9 * ndis + 3;
                                        }
                                        if (itemp == 1) {
                                            l = 7 * na + 10 * ndis - nvdw + 1;
                                        }
                                        for (int iq = 0; iq < nocc; iq++) {
                                            ak[l + iq] = 1 / (qdel * qdel);
                                        }
                                    }
                                    if (itemp != 0 && iqzero != 0) {
                                        isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label8 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                        bref(bet);
                                    }
                                    if (nocc != 0) {
                                        if (wqskal != 0) {
                                            isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + labele + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                            qref(qocc);
                                        }
                                    }
                                }
                                if (idaliz == 1) {
                                    ak[0] = 1;
                                    int l = 6 * na + 9 * ndis + 1;
                                    ak[l] = 1;
                                    ak[l + 1] = 0;
                                    bk[0] = 0;
                                    bk[ibet1 - 1] = 0;
                                } else {
                                    isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label4 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                    if (isp == 0) {
                                        isysw.println("WRONG CHOICE OF SPACE GROUP");
                                    }
                                }
                                if (report != 2) {
                                    isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label5 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------\n");
                                    cgsolv(ncyccg, nkill, kill);

                                    String tmp = (float) ck[0] + "";
                                    for (int i = 1; i < nv; i++) {
                                        tmp += " " + (float) ck[i];
                                    }

                                    ishftw.println(tmp);
                                    ishftw.flush();
                                    ishftw.close();
                                    System.gc();

                                    int nref = no;
                                    if (idaliz == 1) {
                                        nref = nv;
                                    }

                                    double pnorm = wpsum / (nref + ndis + nplat + nchr + ntor - nv);
                                    double bnorm = wbsum / (nref + ndis - nvdw - nv);
                                    double qnorm = wqsum / nref;
                                    for (int i = 0; i < iatmn; i++) {
                                        bk[i] = Math.sqrt(Math.abs(pnorm * bk[i]));
                                    }
                                    if (itemp != 0) {
                                        for (int i = ibet1 - 1; i < ibetn; i++) {
                                            bk[i] = Math.sqrt(Math.abs(bnorm * bk[i]));
                                        }
                                        if (nocc != 0) {
                                            for (int i = 0; i < nocc; i++) {
                                                int j = ibetn + i;
                                                bk[j] = Math.sqrt(Math.abs(qnorm * bk[j]));
                                            }
                                        }
                                    }
                                }

                                isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label6 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------");
                                int k = 2;
                                double xbar = 0;
                                double ybar = 0;
                                double zbar = 0;
                                double bbar = 0;
                                double xavg = 0;
                                double yavg = 0;
                                double zavg = 0;
                                double bavg = 0;
                                double bmean = 0;
                                double pang = 0;
                                double sang = 0;
                                double bang = 0;
                                for (n = 0; n < na; n++) {
                                    xbar += ck[k - 1] * ck[k - 1];
                                    ybar += ck[k] * ck[k];
                                    zbar += ck[k + 1] * ck[k + 1];
                                    xavg += ck[k - 1];
                                    yavg += ck[k];
                                    zavg += ck[k + 1];
                                    bmean += bet[n];
                                    if (itemp != 0) {
                                        bbar += ck[iatmn + n] * ck[iatmn + n];
                                        bavg += ck[iatmn + n];
                                        bang += bk[iatmn + n] * bk[iatmn + n];
                                    }
                                    k += 3;
                                }
                                isysw.println("\n   N   ATOM          DX        DY        DZ        DP      SIGP       DB     SIGB       B         DQ     SIGQ       Q\n");
                                iqzero = na - nocc;
                                for (n = 1; n <= na; n++) {
                                    double dva = ck[3 * n - 2] * a1;
                                    double dvb = ck[3 * n - 1] * a2;
                                    double dvc = ck[3 * n] * a3;
                                    double sva = bk[3 * n - 2] * a1;
                                    double svb = bk[3 * n - 1] * a2;
                                    double svc = bk[3 * n] * a3;
                                    double dvp = dva * dva + dvb * dvb + dvc * dvc + 2 * (dvb * dvc * ca + dva * dvc * cb + dva * dvb * cg);
                                    double svp = sva * sva + svb * svb + svc * svc + 2 * (svb * svc * ca + sva * svc * cb + sva * svb * cg);
                                    pang += dvp;
                                    sang += svp;
                                    dvp = Math.sqrt(dvp);
                                    svp = Math.sqrt(svp);
                                    if (lista == 0) {
                                        continue;
                                    }
                                    if (lista != 1) {
                                        if (dvp < dvpcut) {
                                            continue;
                                        }
                                    }
                                    if (itemp != 1) {
                                        isysw.println(n + " " + atom[n] + " " + dva + " " + dvb + " " + dvc + " " + dvp + " " + svp);
                                        continue;
                                    }
                                    if (nocc != 0 && n > iqzero) {
                                        int m = n - iqzero;
                                        isysw.println(n + " " + atom[n] + " " + dva + " " + dvb + " " + dvc + " " + dvp + " " + svp + " " + ck[iatmn + n - 1] + " " + bk[iatmn + n - 1] + " " + bet[n - 1] + " " + ck[ibetn + m - 1] + " " + bk[ibetn + m - 1] + " " + qocc[n - 1]);
                                        continue;
                                    }
                                    isysw.println(n + " " + atom[n] + " " + dva + " " + dvb + " " + dvc + " " + dvp + " " + svp + " " + ck[iatmn + n - 1] + " " + bk[iatmn + n - 1] + " " + bet[n] + " " + bidon + " " + bidon + " " + qocc[n]);
                                }
                                xavg = xavg / na;
                                yavg = yavg / na;
                                zavg = zavg / na;
                                bavg = bavg / na;
                                double xang = a1 * xavg;
                                double yang = a2 * yavg;
                                double zang = a3 * zavg;
                                bmean = bmean / na;
                                isysw.println(" MEAN SHIFTS\n   FRACTIONAL  " + TBMath.doubleFormat(xavg, 10, 4) + TBMath.doubleFormat(yavg, 10, 4) + TBMath.doubleFormat(zavg, 10, 4) + "\n   ANGSTROMS   " + TBMath.doubleFormat(xang, 10, 4) + TBMath.doubleFormat(yang, 10, 4) + TBMath.doubleFormat(zang, 10, 4) + "                  " + TBMath.doubleFormat(bavg, 10, 2) + "        " + TBMath.doubleFormat(bmean, 10, 2));
                                xbar = Math.sqrt(xbar / na);
                                ybar = Math.sqrt(ybar / na);
                                zbar = Math.sqrt(zbar / na);
                                bbar = Math.sqrt(bbar / na);
                                xang = a1 * xbar;
                                yang = a2 * ybar;
                                zang = a3 * zbar;
                                pang = Math.sqrt(pang / na);
                                sang = Math.sqrt(sang / na);
                                bang = Math.sqrt(bang / na);
                                isysw.println(" RMS SHIFTS\n   FRACTIONAL  " + TBMath.doubleFormat(xbar, 10, 4) + TBMath.doubleFormat(ybar, 10, 4) + TBMath.doubleFormat(zbar, 10, 4) + "\n   ANGSTROMS   " + TBMath.doubleFormat(xang, 10, 3) + TBMath.doubleFormat(yang, 10, 3) + TBMath.doubleFormat(zang, 10, 3) + TBMath.doubleFormat(pang, 10, 3) + TBMath.doubleFormat(sang, 8, 3) + TBMath.doubleFormat(bbar, 10, 2) + TBMath.doubleFormat(bang, 8, 2));

                                double dk = 0, dt = 0;
                                isysw.println("\n\n          CORRELATED  DIAGONAL\n K SHIFT" + TBMath.doubleFormat(ck[0], 10, 3) + TBMath.doubleFormat(dk, 10, 3));
                                if (itemp == 0) {
                                    isysw.println(" B SHIFT" + TBMath.doubleFormat(ck[ibet1 - 1], 10, 3) + TBMath.doubleFormat(dt, 10, 3));
                                }
                                ck[0] = dk;
                                if (report != 0 && report != 3) {
                                    for (int i = 0; i < na; i++) {
                                        xang = a1 * xyz[i][0];
                                        yang = a2 * xyz[i][1];
                                        zang = a3 * xyz[i][2];
                                        double occncy = 0;
                                        if (i > iqzero - 1) {
                                            occncy = qocc[i - iqzero];
                                        }
                                    }
                                    ifofc.close();
                                }
                                if (idaliz != 1) {
                                    line = isysr.readLine();
                                    tok = new StringTokenizer(line);
                                    int irtest = Integer.parseInt(tok.nextToken());
                                    int nsampl = Integer.parseInt(tok.nextToken());
                                    int japn = Integer.parseInt(tok.nextToken());
                                    jabn = Integer.parseInt(tok.nextToken());
                                    for (int i = 0; i < jabn; i++) {
                                        shftk[i] = Double.parseDouble(tok.nextToken());
                                    }
                                    if (irtest > 0) {
                                        isysw.println(" -----------------------------------------------------------------------------------------------\n***** " + label7 + " ***** " + title + " *****\n -----------------------------------------------------------------------------------------------");
                                        isysw.println(line);
                                        killdv = 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        clck(1, isysw);
        isysr.close();
        isysw.close();
        iatmr.close();
        return distances_from_deviation;
    }

    void clck(int n, PrintWriter lun) {

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());

        String DATE1_FORMAT = "dd";
        String DATE2_FORMAT = "MMM";
        String DATE3_FORMAT = "yy";
        String TIME_FORMAT = "HH:mm:ss";
        java.text.SimpleDateFormat dat1 = new java.text.SimpleDateFormat(DATE1_FORMAT);
        java.text.SimpleDateFormat dat2 = new java.text.SimpleDateFormat(DATE2_FORMAT);
        java.text.SimpleDateFormat dat3 = new java.text.SimpleDateFormat(DATE3_FORMAT);
        java.text.SimpleDateFormat tim = new java.text.SimpleDateFormat(TIME_FORMAT);
        dat1.setTimeZone(TimeZone.getDefault());
        dat2.setTimeZone(TimeZone.getDefault());
        dat3.setTimeZone(TimeZone.getDefault());
        tim.setTimeZone(TimeZone.getDefault());

        if (n == 0) {
            firstCall = cal.getTime();
            String time = " TIME:  " + tim.format(firstCall);
            String datx = "" + dat2.format(firstCall).toUpperCase().charAt(0) + "" + dat2.format(firstCall).charAt(1) + "" + dat2.format(firstCall).charAt(2) + "";
            String date = " DATE:  " + dat1.format(firstCall) + "-" + datx + "-" + dat3.format(firstCall);

            isysw.println("\n" + date + "\n" + time);
        } else {
            Date secondCall = cal.getTime();
            String time = " TIME:  " + tim.format(secondCall);
            int sec = secondCall.compareTo(firstCall);
            isysw.println(" TIME:  " + tim.format(secondCall) + "   ELAPSED TIME:  " + TBMath.doubleFormat(sec, 7, 1) + " SEC.  SINCE LAST CALL:  " + TBMath.doubleFormat(sec, 7, 1) + " SEC.");
        }
    }

    int disref() throws Exception {
        double sumr = 0;
        double cosal = Math.cos(al * TBMath.DegreeToRadian);
        double cosbe = Math.cos(be * TBMath.DegreeToRadian);
        double cosga = Math.cos(ga * TBMath.DegreeToRadian);
        int natt = 6 * na + 2;

        int nclass = 5;
        double sigd[] = new double[5];
        sigd[0] = sigd1;
        sigd[1] = sigd2;
        sigd[2] = sigd3;
        sigd[3] = sigd4;
        sigd[4] = sigd5;
        double dist[] = new double[nclass];
        double dsum[] = new double[nclass];
        double nsum[] = new double[nclass];
        double wwtt[] = new double[nclass];
        for (int i = 0; i < nclass; i++) {
            dist[i] = 0;
            dsum[i] = 0;
            nsum[i] = 0;
            wwtt[i] = wdskal / sigd[i];
        }

        for (int n = 0; n < ndis; n++) {
            String line = iatmr.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            int nip = Integer.parseInt(tok.nextToken());
            no[n] = Integer.parseInt(tok.nextToken());
            nt[n] = Integer.parseInt(tok.nextToken());
            dis[n] = Double.parseDouble(tok.nextToken());
            nwtb[n] = Integer.parseInt(tok.nextToken());
        }
        double dir[] = new double[6];

        for (int jn = 0; jn < ndis; jn++) {
            int iwt = nwtb[jn];
            if (iwt == 0) {
                continue;
            }
            double wtt = wwtt[iwt - 1];
            int n5 = no[jn];
            int j5 = nt[jn];

            double dx = (xyz[n5 - 1][0] - xyz[j5 - 1][0]) * a1;
            double dy = (xyz[n5 - 1][1] - xyz[j5 - 1][1]) * a2;
            double dz = (xyz[n5 - 1][2] - xyz[j5 - 1][2]) * a3;
            disa[jn] = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double ds = wtt * (dis[jn] - disa[jn]);
            sumr += ds * ds;
            dist[iwt - 1] += disa[jn];
            dsum[iwt - 1] += (dis[jn] - disa[jn]) * (dis[jn] - disa[jn]);
            nsum[iwt - 1]++;

            dir[0] = a1 * (dx + dz * cosbe + dy * cosga) / disa[jn];
            dir[1] = a2 * (dz * cosal + dy + dx * cosga) / disa[jn];
            dir[2] = a3 * (dy * cosal + dx * cosbe + dz) / disa[jn];
            dir[3] = -dir[0];
            dir[4] = -dir[1];
            dir[5] = -dir[2];
            for (int jk = 0; jk < 6; jk++) {
                dir[jk] *= wtt;
            }

            int n = 6 * (n5 - 1) + 2;
            int m = 6 * (j5 - 1) + 2;
            for (int jt = 0; jt < 3; jt++) {
                for (int jo = jt; jo < 3; jo++) {
                    ak[n - 1] += dir[jt] * dir[jo];
                    ak[m - 1] += dir[jt + 3] * dir[jo + 3];
                    n++;
                    m++;
                }
            }

            int nn = 3 * (n5 - 1) + 2;
            int mm = 3 * (j5 - 1) + 2;
            for (int jt = 0; jt < 3; jt++) {
                bk[nn - 1] += ds * dir[jt];
                bk[mm - 1] += ds * dir[jt + 3];
                nn++;
                mm++;
            }

            int j = natt + 9 * (jn);
            for (int jt = 0; jt < 3; jt++) {
                for (int jo = 3; jo < 6; jo++) {
                    ak[j - 1] += dir[jt] * dir[jo];
                    j++;
                }
            }
        }
        int j = 0;
        for (int i = 0; i < ndis; i++) {
            if (nwtb[i] != 0) {
                if (Math.abs(dis[i] - disa[i]) >= 2 * sigd[nwtb[i] - 1]) {
                    j++;
                    dis[j - 1] = dis[i];
                    disa[j - 1] = disa[i];
                    lo[j - 1] = no[i];
                    lt[j - 1] = nt[i];
                }
            }
        }
        System.out.println(j + " distances deviate from ideality by more than 2 sigma");
        isysw.println(TBMath.intFormat(j, 6) + " DISTANCES DEVIATE FROM IDEALITY BY MORE THAN TWO SIGMA\n\n   ATOM(I) ATOM(J)  IDEAL MODEL   ATOM(I) ATOM(J)  IDEAL MODEL   ATOM(I) ATOM(J)  IDEAL MODEL   ATOM(I) ATOM(J)  IDEAL MODEL\n");
        if (j > 0) {
            String tmp = "";
            for (int i = 0; i < j; i++) {
                tmp += "   " + atom[lo[i] - 1] + " " + atom[lt[i] - 1] + " " + TBMath.doubleFormat(dis[i], 6, 3) + TBMath.doubleFormat(disa[i], 6, 3);
                if (i % 4 == 3) {
                    tmp += "\n";
                }
            }
            isysw.println(tmp);
        }
        double avgr = sumr / ndis;
        nucMult.tmpV = (float) avgr;
        System.out.println("Global deviation : sum=" + sumr + " average=" + avgr);
        isysw.println(" SUM(WGT*DELTA**2) =" + TBMath.scientificDoubleFormat(sumr, 14, 7, 2) + "\n AVG(WGT*DELTA**2) =" + TBMath.doubleFormat(avgr, 12, 3));
        isysw.println("       ROOT          AVG     RMS\n  TYPE  WGT NUMBER   DIST   DELTA   SIGMA");
        double rmsd[] = new double[5];
        for (int i = 0; i < nclass; i++) {
            rmsd[i] = 0;
            if (nsum[i] != 0) {
                rmsd[i] = Math.sqrt(dsum[i] / nsum[i]);
                dist[i] = dist[i] / nsum[i];
            }
            isysw.println(TBMath.intFormat(i + 1, 5) + TBMath.doubleFormat(wwtt[i], 6, 1) + TBMath.intFormat((int) nsum[i], 6) + TBMath.doubleFormat(dist[i], 8, 3) + TBMath.doubleFormat(rmsd[i], 8, 3) + TBMath.doubleFormat(sigd[i], 8, 3));
        }
        isysw.println("  ------ TYPE CODE ------\n   1 = BOND DISTANCE \n   2 = ANGLE DISTANCE \n   3 = PHOSPHATE DISTANCE \n   4 = PHOSPHATE ANGLE, \n       H-BOND, METAL COORD.,ETC. \n   5 = NOT CONSIDERED");

        double avgtau = Math.acos(1 - 0.5 * (dist[1] / dist[0]) * (dist[1] / dist[0]));
        double sigtau = 0;
        double arg = rmsd[1] * rmsd[1] - ((dist[1] / dist[0]) * rmsd[0]) * ((dist[1] / dist[0]) * rmsd[0]);
        if (arg > 0) {
            sigtau = (dist[1] * Math.sqrt(arg) / (Math.sin(avgtau) * dist[0] * dist[0])) * TBMath.RadianToDegree;
        }
        avgtau = avgtau * TBMath.RadianToDegree;
        isysw.println("  ESTIMATED BOND ANGLE VALUES:\n     AVERAGE ANGLE =" + TBMath.doubleFormat(avgtau, 6, 1) + "\n     RMS DEVIATION =" + TBMath.doubleFormat(sigtau, 6, 1));
        wpsum += sumr;
        return j;
    }

    int plnref() throws Exception {
        int nnap = 0;
        int lat[] = new int[20];
        double del[] = new double[20];
        double vm[] = new double[3];
        double x[][] = new double[20][3];
        double dir[] = new double[3];

        double sumr = 0;
        double ssum = 0;
        int nw = 0;
        double wtt = wpskal / sigp;
        isysw.println("              ---EQUATION OF THE PLANE---    RMS     -----DEVIATIONS  OF INDIVIDUAL ATOMS-----\n   N  RESIDUE    M1     M2     M3     D   DEVIATION  ATOM  DEL  ...   ...");

        double[] tmp = new double[6];
        recip(a1, a2, a3, al, be, ga, tmp);
        double b1 = tmp[0];
        double b2 = tmp[1];
        double b3 = tmp[2];
        double cosa = tmp[3];
        double cosb = tmp[4];
        double cosc = tmp[5];
        double[][] g = {{b1 * b1, b1 * b2 * cosc, b1 * b3 * cosb}, {b1 * b2 * cosc, b2 * b2, b2 * b3 * cosa}, {b1 * b3 * cosb, b2 * b3 * cosa, b3 * b3}};

        for (int iii = 0; iii < npln; iii++) {
            String line = iatmr.readLine();
            StringTokenizer tok = new StringTokenizer(line);

            int noplan = Integer.parseInt(tok.nextToken());
            int nap = Integer.parseInt(tok.nextToken());
            for (int ia = 0; ia < nap; ia++) {
                lat[ia] = Integer.parseInt(tok.nextToken());
            }

            for (int j = 0; j < nap; j++) {
                int ka = lat[j] - 1;
                x[j][0] = xyz[ka][0];
                x[j][1] = xyz[ka][1];
                x[j][2] = xyz[ka][2];
            }

            double d = plane(nap, x, vm, g);

            double sumd = 0;
            for (int ja = 0; ja < nap; ja++) {
                del[ja] = 0;
                for (int i = 0; i < 3; i++) {
                    dir[i] = wtt * vm[i];
                    del[ja] += vm[i] * x[ja][i];
                }
                del[ja] = del[ja] - d;
                double ds = wtt * del[ja];
                sumr += ds * ds;
                sumd += del[ja] * del[ja];

                int ka = lat[ja];
                int nn = 6 * (ka - 1) + 2;
                for (int j1 = 0; j1 < 3; j1++) {
                    for (int j2 = j1; j2 < 3; j2++) {
                        ak[nn - 1] += dir[j1] * dir[j2];
                        nn++;
                    }
                }
                nn = 3 * (ka - 1) + 2;
                for (int jj = 0; jj < 3; jj++) {
                    bk[nn - 1] += -ds * dir[jj];
                    nn++;
                }
            }
            ssum += sumd;
            nnap += nap;
            double rmsd = Math.sqrt(sumd / nap);
            if (rmsd >= 2 * sigp) {
                nw++;
                String temp = noplan + " " + atom[lat[0] - 1] + " " + vm[0] + " " + vm[1] + " " + vm[2] + " " + d + " " + rmsd;
                for (int i = 0; i < nap; i++) {
                    temp += " " + lat[i] + " " + del[i];
                }
                isysw.println(temp);
            }
        }

        double avgr = sumr / nnap;
        isysw.println("\n SUM(WGT*DELTA**2) =" + TBMath.scientificDoubleFormat(sumr, 14, 7, 2) + "\n AVG(WGT*DELTA**2) =" + TBMath.doubleFormat(avgr, 7, 3));
        double rmsd = Math.sqrt(ssum / nnap);
        isysw.println(" ROOT WEIGHT =" + TBMath.doubleFormat(wtt, 6, 2) + "\n   RMS DELTA =" + TBMath.doubleFormat(rmsd, 6, 3) + "\n       SIGMA =" + TBMath.doubleFormat(sigp, 6, 3) + "\n  RESTRAINTS =" + TBMath.intFormat(nnap, 6) + "\n DELTA > 2.0*sigma = " + TBMath.intFormat(nw, 6));
        wpsum += sumr;
        return nnap;
    }

    double plane(int n, double[][] x, double[] vm, double[][] g) {
        double xs[] = new double[3];
        double xxs[][] = new double[3][3];
        double adj[][] = new double[3][3];
        double a[][] = new double[3][3];
        double b[][] = new double[3][3];
        double bv[] = new double[3];
        double vmi[] = new double[3];
        double zip = 1.0E-5;
        int mm = 10;

        int sn = n;
        for (int i = 0; i < 3; i++) {
            xs[i] = 0;
            for (int k = 0; k < n; k++) {
                xs[i] += x[k][i];
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                xxs[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    xxs[i][j] += x[k][i] * x[k][j];
                }
                a[i][j] = xxs[i][j] - xs[i] * xs[j] / sn;
            }
        }

        adj[0][0] = a[1][1] * a[2][2] - a[1][2] * a[2][1];
        adj[1][0] = a[2][0] * a[1][2] - a[1][0] * a[2][2];
        adj[2][0] = a[1][0] * a[2][1] - a[2][0] * a[1][1];
        adj[0][1] = a[2][1] * a[0][2] - a[0][1] * a[2][2];
        adj[1][1] = a[0][0] * a[2][2] - a[2][0] * a[0][2];
        adj[2][1] = a[2][0] * a[0][1] - a[0][0] * a[2][1];
        adj[0][2] = a[0][1] * a[1][2] - a[0][2] * a[1][1];
        adj[1][2] = a[1][0] * a[0][2] - a[0][0] * a[1][2];
        adj[2][2] = a[0][0] * a[1][1] - a[1][0] * a[0][1];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                b[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    b[i][j] += adj[i][k] * g[k][j];
                }
            }
        }

        bv[0] = b[0][0] * b[0][0] + b[1][0] * b[1][0] + b[2][0] * b[2][0];
        bv[1] = b[0][1] * b[0][1] + b[1][1] * b[1][1] + b[2][1] * b[2][1];
        bv[2] = b[0][2] * b[0][2] + b[1][2] * b[1][2] + b[2][2] * b[2][2];

        int kk = 1;
        if (bv[1] > bv[0]) {
            kk = 2;
        }
        if (bv[2] > bv[kk - 1]) {
            kk = 3;
        }
        double vm1 = b[0][kk - 1];
        for (int i = 0; i < 3; i++) {
            vmi[i] = b[i][kk - 1] / vm1;
        }

        for (int nnn = 0; nnn < mm; nnn++) {
            vm[0] = b[0][0] * vmi[0] + b[0][1] * vmi[1] + b[0][2] * vmi[2];
            vm[1] = b[1][0] * vmi[0] + b[1][1] * vmi[1] + b[1][2] * vmi[2];
            vm[2] = b[2][0] * vmi[0] + b[2][1] * vmi[1] + b[2][2] * vmi[2];
            double ratio1 = vm[0] / vmi[0];
            double ratio2 = vm[1] / vmi[1];
            double ratio3 = vm[2] / vmi[2];
            double rat12 = Math.abs(ratio2 / ratio1 - 1);
            double rat13 = Math.abs(ratio3 / ratio1 - 1);

            if (rat12 < zip && rat13 < zip) {
                break;
            }

            for (int i = 0; i < 3; i++) {
                vmi[i] = vm[i] / vm[0];
            }
        }

        double orm = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                orm += vm[i] * vm[j] * g[i][j]; //g = 0 ?
            }
        }
        orm = Math.sqrt(orm);
        for (int i = 0; i < 3; i++) {
            vm[i] = vm[i] / orm;
        }

        double d = (vm[0] * xs[0] + vm[1] * xs[1] + vm[2] * xs[2]) / sn;
        return d;
    }

    void recip(double ar, double br, double cr, double alr, double ber, double gar, double[] arguments) {
        double alg = alr * TBMath.DegreeToRadian;
        double beg = ber * TBMath.DegreeToRadian;
        double gag = gar * TBMath.DegreeToRadian;
        double cosar = Math.cos(alg);
        double cosbr = Math.cos(beg);
        double cosgr = Math.cos(gag);
        double vr = ar * br * cr * Math.sqrt(1 - cosar * cosar - cosbr * cosbr - cosgr * cosgr + 2 * cosar * cosbr * cosgr);
        double sinar = Math.sin(alg);
        double sinbr = Math.sin(beg);
        double singr = Math.sin(gag);
        double aa = br * cr * sinar / vr;
        double bb = ar * cr * sinbr / vr;
        double cc = ar * br * singr / vr;
        double cosal = (cosbr * cosgr - cosar) / (sinbr * singr);
        double cosbe = (cosar * cosgr - cosbr) / (sinar * singr);
        double cosga = (cosar * cosbr - cosgr) / (sinar * sinbr);
        arguments[0] = aa;
        arguments[1] = bb;
        arguments[2] = cc;
        arguments[3] = cosal;
        arguments[4] = cosbe;
        arguments[5] = cosga;
    }

    void chiref() throws Exception {
        int l1 = 6 * na + 1;
        double sumr = 0;
        double sumd = 0;
        double wtt = wcskal / sigc;
        double ca = Math.cos(al * TBMath.DegreeToRadian);
        double cb = Math.cos(be * TBMath.DegreeToRadian);
        double cg = Math.cos(ga * TBMath.DegreeToRadian);
        double sg = Math.sin(ga * TBMath.DegreeToRadian);
        double gxx = a1;
        double gxy = a2 * cg;
        double gxz = a3 * cb;
        double gyy = a2 * sg;
        double gyz = a3 * (ca - cb * cg) / sg;
        double gzz = a3 * Math.sqrt(1 - ca * ca - cb * cb - cg * cg + 2 * ca * cb * cg) / sg;
        double gt[][] = {{gxx, 0, 0}, {gxy, gyy, 0}, {gxz, gyz, gzz}};
        double a[][] = new double[3][3];
        double b[][] = new double[3][3];

        double[] vi = new double[nchr];
        double[] vm = new double[nchr];
        int[] la = new int[nchr];
        for (int ic = 0; ic < nchr; ic++) {
            String line = iatmr.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            tok.nextToken();

            int[] iatom = new int[4];
            int[] idist = new int[6];
            double videal = 0;
            iatom[0] = Integer.parseInt(tok.nextToken());
            iatom[1] = Integer.parseInt(tok.nextToken());
            iatom[2] = Integer.parseInt(tok.nextToken());
            iatom[3] = Integer.parseInt(tok.nextToken());
            idist[0] = Integer.parseInt(tok.nextToken());
            idist[1] = Integer.parseInt(tok.nextToken());
            idist[2] = Integer.parseInt(tok.nextToken());
            idist[3] = Integer.parseInt(tok.nextToken());
            idist[4] = Integer.parseInt(tok.nextToken());
            idist[5] = Integer.parseInt(tok.nextToken());
            videal = Double.parseDouble(tok.nextToken());

            int ia = iatom[0];
            for (int i = 0; i < 3; i++) {
                int iq = iatom[i + 1];
                double dx = xyz[iq - 1][0] - xyz[ia - 1][0];
                double dy = xyz[iq - 1][1] - xyz[ia - 1][1];
                double dz = xyz[iq - 1][2] - xyz[ia - 1][2];
                a[i][0] = gxx * dx + gyz * dy + gxz * dz;
                a[i][1] = gyy * dy + gyz * dz;
                a[i][2] = gzz * dz;
            }
            double vobs = det3(a);
            vi[ic] = videal;
            vm[ic] = vobs;
            la[ic] = iatom[0];
            double delv = videal - vobs;
            double ds = wtt * delv;
            sumr += ds * ds;
            sumd += delv * delv;

            for (ia = 0; ia < 3; ia++) {
                for (int ix = 0; ix < 3; ix++) {
                    for (int j = 0; j < 3; j++) {
                        for (int i = 0; i < 3; i++) {
                            b[i][j] = a[i][j];
                        }
                        b[ia][j] = gt[ix][j];
                    }
                    dir[ia + 1][ix] = wtt * det3(b);
                }
            }
            for (int ix = 0; ix < 3; ix++) {
                dir[0][ix] = -(dir[1][ix] + dir[2][ix] + dir[3][ix]);
            }

            for (ia = 1; ia <= 4; ia++) {
                int iq = iatom[ia - 1];
                int nn = 6 * (iq - 1) + 1;
                for (int i = 1; i <= 3; i++) {
                    for (int j = i; j <= 3; j++) {
                        nn++;
                        ak[nn - 1] += dir[ia - 1][i - 1] * dir[ia - 1][j - 1];
                    }
                }
            }

            int m = 0;

            for (ia = 1; ia <= 3; ia++) {
                int l = ia + 1;
                for (int ja = l; ja <= 4; ja++) {
                    m++;
                    int id = idist[m - 1];
                    if (id >= 0) {
                        int nn = l1 + 9 * (id - 1);
                        for (int ix = 1; ix <= 3; ix++) {
                            for (int jx = 1; jx <= 3; jx++) {
                                nn++;
                                ak[nn - 1] += dir[ia - 1][ix - 1] * dir[ja - 1][jx - 1];
                            }
                        }
                    } else {
                        id = -id;
                        int nn = l1 + 9 * (id - 1);
                        for (int jx = 1; jx <= 3; jx++) {
                            for (int ix = 1; ix <= 3; ix++) {
                                nn++;
                                ak[nn - 1] += dir[ia - 1][ix - 1] * dir[ja - 1][jx - 1];
                                if (nn == 52278) {
                                    System.out.println("chiref3 ak[" + nn + "] = " + ak[nn - 1] + " offset " + (dir[ia - 1][ix - 1] * dir[ja - 1][jx - 1]));
                                    System.out.println("ia " + ia + " ja " + ja + " ix " + ix + " jx " + jx);
                                }
                            }
                        }
                    }
                }
            }

            for (ia = 0; ia < 4; ia++) {
                int iq = iatom[ia];
                int nn = 3 * (iq - 1) + 1;
                for (int ix = 0; ix < 3; ix++) {
                    nn++;
                    bk[nn - 1] += ds * dir[ia][ix];
                }
            }
        }

        isysw.println(" CENTRAL  CHIRAL VOLUME    CENTRAL  CHIRAL VOLUME    CENTRAL  CHIRAL VOLUME    CENTRAL  CHIRAL VOLUME    CENTRAL  CHIRAL VOLUME   ");
        isysw.println("   ATOM    IDEAL MODEL       ATOM    IDEAL MODEL       ATOM    IDEAL MODEL       ATOM    IDEAL MODEL       ATOM    IDEAL MODEL    ");
        int iambad = 0;

        double[] vi2 = new double[nchr];
        double[] vm2 = new double[nchr];
        int[] la2 = new int[nchr];
        for (int ic = 0; ic < nchr; ic++) {
            if (Math.abs(vi[ic] - vm[ic]) >= 2 * sigc) {
                iambad++;
                la2[iambad - 1] = la[ic];
                vi2[iambad - 1] = vi[ic];
                vm2[iambad - 1] = vm[ic];
            }
        }
        String tmp = "";
        for (int i = 0; i < iambad; i++) {
            tmp += "  " + atom[la2[i] - 1] + " " + TBMath.doubleFormat(vi2[i], 6, 2) + TBMath.doubleFormat(vm2[i], 6, 2) + "    ";
            if (i % 5 == 4) {
                tmp += "\n";
            }
        }
        isysw.println(tmp);
        double avgr = sumr / nchr;
        isysw.println(" SUM(WGT*DELTA**2) =" + TBMath.scientificDoubleFormat(sumr, 14, 7, 2) + "\n AVG(WGT*DELTA**2) =" + TBMath.doubleFormat(avgr, 15, 3));
        double rmsd = Math.sqrt(sumd / nchr);
        isysw.println(" ROOT WEIGHT =" + TBMath.doubleFormat(wtt, 6, 2) + "\n   RMS DELTA =" + TBMath.doubleFormat(rmsd, 6, 3) + "\n       SIGMA =" + TBMath.doubleFormat(sigc, 6, 3) + "\n DELTA > 2.0*sigma =" + TBMath.intFormat(iambad, 6));
        wpsum += sumr;
    }

    double det3(double[][] a) {
        double ret = a[0][0] * (a[1][1] * a[2][2] - a[1][2] * a[2][1]) - a[0][1] * (a[1][0] * a[2][2] - a[1][2] * a[2][0]) + a[0][2] * (a[1][0] * a[2][1] - a[1][1] * a[2][0]);
        return ret;
    }

    void vdwref() throws Exception {
        double dsum[] = new double[4];
        double nsum[] = new double[4];
        double rmsd[] = new double[4];
        double dir[] = new double[6];

        double cosal = Math.cos(al * TBMath.DegreeToRadian);
        double cosbe = Math.cos(be * TBMath.DegreeToRadian);
        double cosga = Math.cos(ga * TBMath.DegreeToRadian);
        double sumr = 0;
        int nexp = 2;
        int l1 = 6 * na + 9 * ndis + 1;
        int ivdw = 0;
        int ihb = 0;
        int jv = 0;

        int nclass = 3;
        for (int i = 0; i < nclass; i++) {
            dsum[i] = 0;
            nsum[i] = 0;
        }
        sigv = Math.pow(sigv, nexp);
        double wtt = wvskal / sigv;

        if (nvdw == 0) {
            return;
        }

        for (int i = 0; i < nvdw; i++) {
            String line = iatmr.readLine();
            StringTokenizer tok = new StringTokenizer(line);
            int nip = Integer.parseInt(tok.nextToken());
            int ia = Integer.parseInt(tok.nextToken());
            int ja = Integer.parseInt(tok.nextToken());
            double dvdw = Double.parseDouble(tok.nextToken());
            int ktyp = Integer.parseInt(tok.nextToken());

            double dx = (xyz[ia - 1][0] - xyz[ja - 1][0]) * a1;
            double dy = (xyz[ia - 1][1] - xyz[ja - 1][1]) * a2;
            double dz = (xyz[ia - 1][2] - xyz[ja - 1][2]) * a3;
            double dobs = Math.sqrt(dx * dx + dy * dy + dz * dz + 2 * (dy * dz * cosal + dx * dz * cosbe + dx * dy * cosga));

            double contct = dvdw + dinc[ktyp - 1];
            if (dobs >= contct && ktyp != 3) {
                if (ktyp != 3 || dobs > (dvdw - dinc[2])) //toujours vrai que ktyp != 3
                {
                    continue;
                }
                ihb++;
                int jhb = ndis - ihb;
                dis[jhb - 1] = contct;
                disa[jhb - 1] = dobs;
                lo[jhb - 1] = ia;
                lt[jhb - 1] = ja;
                continue;
            }
            ivdw++;
            int idis = ndis + ivdw;
            no[idis - 1] = ia;
            nt[idis - 1] = ja;
            dis[idis - 1] = contct;
            disa[idis - 1] = dobs;
            nwtb[idis - 1] = ktyp;
            double delta = contct - dobs;
            double ds = wtt * Math.pow(delta, nexp);
            sumr += ds * ds;
            dsum[ktyp - 1] += delta * delta;
            nsum[ktyp - 1]++;
            double disfn = (nexp * Math.pow(delta, nexp - 1)) / dobs;
            dir[0] = a1 * (dx + dz * cosbe + dy * cosga) * disfn;
            dir[1] = a2 * (dz * cosal + dy + dx * cosga) * disfn;
            dir[2] = a3 * (dy * cosal + dx * cosbe + dz) * disfn;
            dir[3] = -dir[0];
            dir[4] = -dir[1];
            dir[5] = -dir[2];

            for (int jk = 0; jk < 6; jk++) {
                dir[jk] *= wtt;
            }

            int n = 6 * (ia - 1) + 2;
            int m = 6 * (ja - 1) + 2;
            for (int jt = 0; jt < 3; jt++) {
                for (int jo = jt; jo < 3; jo++) {
                    ak[n - 1] += dir[jt] * dir[jo];
                    ak[m - 1] += dir[jt + 3] * dir[jo + 3];
                    n++;
                    m++;
                }
            }

            int nn = 3 * (ia - 1) + 2;
            int mm = 3 * (ja - 1) + 2;
            for (int jt = 0; jt < 3; jt++) {
                bk[nn - 1] += ds * dir[jt];
                bk[mm - 1] += ds * dir[jt + 3];
                nn++;
                mm++;
            }

            for (int jt = 0; jt < 3; jt++) {
                for (int jo = 3; jo < 6; jo++) {
                    jv++;
                    ak[l1 + jv - 1] += dir[jt] * dir[jo];
                }
            }
        }

        for (int ktyp = 1; ktyp <= 3; ktyp++) {
            int j = 0;
            for (int i = 0; i < ivdw; i++) {
                int id = ndis + i;
                if (ktyp != nwtb[id]) {
                    continue;
                }
                if (Math.abs(dis[id] - disa[id]) < 2 * sigv) {
                    continue;
                }
                j++;
                dis[j - 1] = dis[id];
                disa[j - 1] = disa[id];
                lo[j - 1] = no[id];
                lt[j - 1] = nt[id];
            }
            if (ktyp == 1) {
                isysw.println("  **** SINGLE-TORSION CONTACTS ****    " + TBMath.intFormat(j, 5) + " deviate > 2.*sigma\n");
            }
            if (ktyp == 2) {
                isysw.println("  **** MULTIPLE-TORSION CONTACTS ****    " + TBMath.intFormat(j, 5) + " deviate > 2.*sigma \n");
            }
            if (ktyp == 3) {
                isysw.println("  **** POSSIBLE HYDROGEN BONDS ****    " + TBMath.intFormat(j, 5) + " deviate > 2.*sigma\n ");
            }

            if (j > 0) {
                String tmp = "";
                for (int i = 0; i < j; i++) {
                    tmp += "   " + atom[lo[i] - 1] + " " + atom[lt[i] - 1] + " " + TBMath.doubleFormat(dis[i], 6, 3) + TBMath.doubleFormat(disa[i], 6, 3);
                    if (i % 4 == 3) {
                        tmp += "\n";
                    }
                }
                isysw.println(tmp);
            }
        }

        double delneg = -dinc[2];
        isysw.println(" RESTRAINTS HAVE NOT BEEN APPLIED FOR THE" + TBMath.intFormat(ihb, 4) + " POSSIBLE HYDROGEN BONDS WITH DISTANCES IN THE RANGE (IDEAL < D < VAN DER WAALS+" + TBMath.doubleFormat(delneg, 5, 2) + ")\n");
        int k = 0;
        if (ihb > 0) {
            for (int j = 1; j <= ihb; j++) {
                int id = ndis - j;
                if (Math.abs(dis[id - 1] - disa[id - 1]) < 2 * sigv) {
                    continue;
                }
                k++;
                dis[k - 1] = dis[id - 1];
                disa[k - 1] = disa[id - 1];
                lo[k - 1] = lo[id - 1];
                lt[k - 1] = lt[id - 1];
            }
            String tmp = "";
            for (int i = 0; i < k; i++) {
                tmp += "   " + atom[lo[i] - 1] + " " + atom[lt[i] - 1] + " " + TBMath.doubleFormat(dis[i], 6, 3) + TBMath.doubleFormat(disa[i], 6, 3);
            }
            isysw.println(tmp);
        }

        double avgr = 0;
        if (ivdw != 0) {
            avgr = sumr / ivdw;
        }

        isysw.println(" SUM(WGT*DELTA**2) =" + TBMath.scientificDoubleFormat(sumr, 14, 7, 2) + "\n AVG(WGT*DELTA**2) = " + TBMath.doubleFormat(avgr, 10, 3));
        isysw.println("       ROOT          RMS\n  TYPE  WGT NUMBER  DELTA   SIGMA    DINC");
        for (int i = 0; i < nclass; i++) {
            rmsd[i] = 0;
            if (nsum[i] != 0) {
                rmsd[i] = Math.sqrt(dsum[i] / nsum[i]);
            }
            isysw.println(TBMath.intFormat(i + 1, 5) + TBMath.doubleFormat(wtt, 6, 1) + TBMath.intFormat((int) nsum[i], 6) + TBMath.doubleFormat(rmsd[i], 8, 3) + TBMath.doubleFormat(sigv, 8, 3) + TBMath.doubleFormat(dinc[i], 8, 3));
        }
        isysw.println("  --------- TYPE CODE ---------\n   1 = SINGLE-TORSION CONTACT\n   2 = MULTIPLE-TORSION CONTACT\n   3 = POSSIBLE HYDROGEN BOND");
        nvdw = ivdw;
        wpsum += sumr;
    }

    void bref(double[] be) {
    }

    void cgsolv(int ncyccg, int nkill, double[] kill) throws Exception {
        double l2 = lgx + lgy;
        double l3 = l2 + lgz;
        nvl = nv + l3;
        double epslon = 1.0E-20;

        scale();
        if (nkill > 0) {
            freeze(kill);
        }
        double dum1 = 0;
        for (int n = 0; n < nvl; n++) {
            aii[n] = 0;
            ck[n] = 0;
            p[n] = bk[n];
            dum1 += bk[n] * bk[n];
        }
        isysw.println("                             PROGRESS OF THE SOLUTION\n         CYCLE    RMS    SHIFT\n");

        boolean go130 = false;
        double rmshift[] = new double[ncyccg];

        for (int n = 0; n < ncyccg; n++) {
            matmul();
            double dum = dum1;
            double den = 0;
            for (int l = 0; l < nvl; l++) {
                den += p[l] * f[l];
            }
            double al = dum / den;
            double dum0 = 0;
            dum1 = 0;
            double dum2 = 0;

            for (int l = 0; l < nvl; l++) {
                bk[l] += -al * f[l];
                ck[l] += al * p[l];
                aii[l] += (p[l] * p[l]) / den;
                dum0 += aii[l];
                dum1 += bk[l] * bk[l];
                dum2 += ck[l] * ck[l];
            }
            double rmsx = Math.sqrt(dum2 / nvl);
            double rmsr = Math.sqrt(dum1 / nvl);
            double rmsa = Math.sqrt(Math.abs(dum0 / nvl));
            double b = dum1 / dum;
            for (int l = 0; l < nvl; l++) {
                p[l] = bk[l] + b * p[l];
            }
            rmshift[n] = rmsx;
            if (rmsr < epslon) {
                go130 = true;
                break;
            }
        }

        if (!go130) {
            for (int i = 1; i <= ncyccg; i += 10) {
                int k1 = i;
                int k2 = i + 9;
                String tmp = TBMath.intFormat(k1, 4) + "";
                for (int k = k1; k <= k2; k++) {
                    tmp += TBMath.doubleFormat(rmshift[k - 1], 10, 5);
                }
                isysw.println(tmp);
            }
        }

        for (int i = 0; i < nv; i++) {
            bk[i] = aii[i] * sk[i] * sk[i];
            ck[i] *= sk[i];
        }
    }

    void torref(int ntor) {
    }

    void torshn() {
    }

    void symref(int[] nsym) {
    }

    void toss(int ntab, double[][] r, double[] t, BufferedReader isysw) {
    }

    void polar(double r[][], double[] out) {
    }

    void matinv(double[][] a, double[] b, int n1, int m1) {
        int index[][] = new int[31][3];
        int m = 1;
        int n = n1;
        int irow = 0;
        int icolum = 0;
        for (int j = 0; j < n; j++) {
            index[j][2] = 0;
        }
        for (int i = 0; i < n; i++) {
            double amax = 0;
            for (int j = 0; j < n; j++) {
                if (index[j][2] == 1) {
                    continue;
                }
                for (int k = 0; k < n; k++) {
                    if ((index[k][2] - 1) > 0) {
                        System.out.println("36H JOB TERMINATED  MATRIX IS SINGULAR");
                        return;
                    }
                    if (index[k][2] == 0) {
                        continue;
                    }
                    if ((amax - Math.abs(a[j][k])) >= 0) {
                        continue;
                    }
                    irow = j + 1;
                    icolum = k + 1;
                    amax = Math.abs(a[j][k]);
                }
            }
            index[icolum - 1][2]++;
            index[i][0] = irow;
            index[i][1] = icolum;

            if (irow != icolum) {
                for (int l = 0; l < n; l++) {
                    double swap = a[irow - 1][l];
                    a[irow - 1][l] = a[icolum - 1][l];
                    a[icolum - 1][l] = swap;
                }
                if (m > 0) {
                    for (int l = 0; l < m; l++) {
                        double swap = b[irow - 1];
                        b[irow - 1] = b[icolum - 1];
                        b[icolum - 1] = swap;
                    }
                }
            }

            double pivot = a[icolum - 1][icolum - 1];
            a[icolum - 1][icolum - 1] = 1;
            for (int l = 0; l < n; l++) {
                a[icolum - 1][l] = a[icolum - 1][l] / pivot;
            }
            if (m > 0) {
                for (int l = 0; l < m; l++) {
                    b[icolum - 1] = b[icolum - 1] / pivot;
                }
            }

            for (int l1 = 1; l1 <= n; l1++) {
                if (l1 != icolum) {
                    double t = a[l1 - 1][icolum - 1];
                    a[l1 - 1][icolum - 1] = 0;
                    for (int l = 0; l < n; l++) {
                        a[l1 - 1][l] = a[l1 - 1][l] - a[icolum - 1][l] * t;
                    }
                    if (m > 0) {
                        for (int l = 0; l < m; l++) {
                            b[l1 - 1] = b[l1 - 1] - b[icolum - 1] * t;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            int l = n + 1 - i;
            if (index[l - 1][0] != index[l - 1][1]) {
                int jrow = index[l - 1][0];
                int jcolum = index[l - 1][1];
                for (int k = 0; k < n; k++) {
                    double swap = a[k][jrow - 1];
                    a[k][jrow - 1] = a[k][jcolum - 1];
                    a[k][jcolum - 1] = swap;
                }
            }
        }

        for (int k = 0; k < n; k++) {
            if (index[k][2] != -1) {
                System.out.println("36H JOB TERMINATED  MATRIX IS SINGULAR");
            }
        }
    }

    void matmul() {
        for (int n = 0; n < nvl; n++) {
            f[n] = 0;
        }
        f[0] = ak[0] * p[0];
        int j = -1;
        int l = 2;
        for (int n = 0; n < na; n++) {
            j += 3;
            int j5 = j + 2;
            for (int ik = j; ik <= j5; ik++) {
                for (int lk = ik; lk <= j5; lk++) {
                    f[ik - 1] += ak[l - 1] * p[lk - 1];
                    if (ik != lk) {
                        f[lk - 1] += ak[l - 1] * p[ik - 1];
                    }
                    l++;
                }
            }
        }
        int ndis2 = 0;
        for (int jk = 0; jk < ndis; jk++) {
            int n = 3 * (no[jk] - 1) + 2;
            int m = 3 * (nt[jk] - 1) + 2;
            int m5 = m + 2;
            int n5 = n + 2;
            for (int ik = n; ik <= n5; ik++) {
                for (int lk = m; lk <= m5; lk++) {
                    f[ik - 1] += ak[l - 1] * p[lk - 1];
                    f[lk - 1] += ak[l - 1] * p[ik - 1];
                    l++;
                }
            }
        }

        if (itemp == 0) {
            f[ibet1 - 1] = ak[l - 1] * p[ibet1 - 1];
            l++;
            f[0] += ak[l - 1] * p[ibet1 - 1];
            f[ibet1 - 1] += ak[l - 1] * p[0];
        } else {
            l = 6 * na + 9 * ndis + 1;
            for (int i = 0; i < na; i++) {
                f[iatmn + i] = ak[l + i - 1] * p[iatmn + i];
            }
            l += na;
            ndis2 = ndis - nvdw;
            for (int id = 0; id < ndis2; id++) {
                int io = no[id];
                int it = nt[id];
                f[iatmn + io - 1] += ak[l + id] * p[iatmn + it - 1];
                f[iatmn + it - 1] += ak[l + id] * p[iatmn + io - 1];
            }
        }

        if (nocc != 0) {
            if (itemp == 1) {
                l += ndis2;
            }
            for (int iq = 0; iq < nocc; iq++) {
                f[ibetn + iq] = ak[l + iq] * p[ibetn + iq];
            }
        }

        if (l3 != 0) {
            if (lgx != 0) {
                for (int ia = 1; ia <= na; ia++) {
                    f[3 * ia - 2] += qbar * sk[3 * ia - 2] * p[nv];
                    f[nv] += qbar * sk[3 * ia - 2] * p[3 * ia - 2];
                }
            }
            if (lgy != 0) {
                for (int ia = 1; ia <= na; ia++) {
                    f[3 * ia - 1] += qbar * sk[3 * ia - 1] * p[nv + l2 - 1];
                    f[nv + l2 - 1] += qbar * sk[3 * ia - 1] * p[3 * ia - 1];
                }
            }
            if (lgz != 0) {
                for (int ia = 1; ia <= na; ia++) {
                    f[3 * ia] += qbar * sk[3 * ia] * p[nv + l3 - 1];
                    f[nv + l3 - 1] += qbar * sk[3 * ia] * p[3 * ia];
                }
            }
        }
    }

    void scale() {
        double abar = 0;
        ck[0] = ak[0];
        for (int ia = 1; ia <= na; ia++) {
            ck[3 * ia - 2] = ak[6 * ia - 5];
            ck[3 * ia - 1] = ak[6 * ia - 2];
            ck[3 * ia] = ak[6 * ia];
        }

        int l = 6 * na + 9 * ndis + 1;
        if (itemp == 0) {
            ck[ibet1 - 1] = ak[l];
        } else {
            for (int ia = 0; ia < na; ia++) {
                ck[iatmn + ia] = ak[l + ia];
            }
        }
        if (nocc != 0) {
            if (itemp == 0) {
                l += 2;
            }
            if (itemp == 1) {
                l += na + ndis - nvdw;
            }
            for (int iq = 0; iq < nocc; iq++) {
                ck[ibetn + iq] = ak[l + iq];
            }
        }
        for (int i = 0; i < nv; i++) {
            abar += ck[i];
        }
        abar = abar / nv;
        for (int i = 0; i < nv; i++) {
            sk[i] = Math.sqrt(abar / ck[i]);
        }
        double qbar = abar / lq;
        ak[0] *= sk[0] * sk[0];
        for (int ia = 1; ia <= na; ia++) {
            ak[6 * ia - 5] *= sk[3 * ia - 2];
            ak[6 * ia - 5] *= sk[3 * ia - 2];
            ak[6 * ia - 4] *= sk[3 * ia - 2];
            ak[6 * ia - 4] *= sk[3 * ia - 1];
            ak[6 * ia - 3] *= sk[3 * ia - 2];
            ak[6 * ia - 3] *= sk[3 * ia];
            ak[6 * ia - 2] *= sk[3 * ia - 1];
            ak[6 * ia - 2] *= sk[3 * ia - 1];
            ak[6 * ia - 1] *= sk[3 * ia - 1];
            ak[6 * ia - 1] *= sk[3 * ia];
            ak[6 * ia] *= sk[3 * ia];
            ak[6 * ia] *= sk[3 * ia];
        }
        for (int id = 0; id < ndis; id++) {
            int io = no[id];
            int it = nt[id];
            l = 6 * na + 9 * id + 1;
            ak[l] *= sk[3 * io - 2] * sk[3 * it - 2];
            ak[l + 1] *= sk[3 * io - 2] * sk[3 * it - 1];
            ak[l + 2] *= sk[3 * io - 2] * sk[3 * it];
            ak[l + 3] *= sk[3 * io - 1] * sk[3 * it - 2];
            ak[l + 4] *= sk[3 * io - 1] * sk[3 * it - 1];
            ak[l + 5] *= sk[3 * io - 1] * sk[3 * it];
            ak[l + 6] *= sk[3 * io] * sk[3 * it - 2];
            ak[l + 7] *= sk[3 * io] * sk[3 * it - 1];
            ak[l + 8] *= sk[3 * io] * sk[3 * it];
        }
        l = 6 * na + 9 * ndis + 1;
        int ndis2 = 0;
        if (itemp == 0) {
            ak[l] *= sk[ibet1 - 1] * sk[ibet1 - 1];
            ak[l + 1] *= sk[ibet1 - 1] * sk[0];
        } else {
            for (int ia = 0; ia < na; ia++) {
                ak[l + ia] *= sk[iatmn + ia];
                ak[l + ia] *= sk[iatmn + ia];
            }
            l += na;
            ndis2 = ndis - nvdw;
            for (int id = 0; id < ndis2; id++) {
                int io = no[id];
                int it = nt[id];
                ak[l + id] *= sk[iatmn + io - 1];
                ak[l + id] *= sk[iatmn + it - 1];
            }
        }

        if (nocc != 0) {
            if (itemp == 0) {
                l += 2;
            }
            if (itemp == 1) {
                l += ndis2;
            }
            for (int iq = 0; iq < nocc; iq++) {
                ak[l + iq] *= sk[ibetn + iq];
                ak[l + iq] *= sk[ibetn + iq];
            }
        }

        for (int i = 0; i < nv; i++) {
            bk[i] *= sk[i];
        }
    }

    void freeze(double[] kill) {

    }

    int pseudo(int nsgr) {
        return 0;
    }

    void qref(double qocc[]) {
    }
}