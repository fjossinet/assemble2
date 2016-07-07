package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.utils.HD;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Java version of mrgnuc.f 13/09/2005 (Thomas Ludwig)<br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original mrgnuc.f is at the end of this file<br/>
 * <br/>
 * Program MRGNUC<br/>
 * PROGRAM TO ADD THE SHIFTS FROM THE PROGRAM NUCLSQ<br/>
 * TO THE  COORDINATES CONTAINED IN LSQ.INP.  THE OUTPUTTED COORDINATES
 * ARE IN THE SO-CALLED MIT FORMAT.<br/>
 * <br/>
 * E.Westhof
 * <ul>
 * <li>05/1986 : Changed so that bmin = sc(2)</li>
 * <li>11/1987 : More than 99 residues</li>
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

public class MgrNuc {
    public String tmpPath;
    public String inPath;
    public String outPath;

    public MgrNuc(String tmpPath, NucLSQ nucLSQ) throws Exception {
        this.tmpPath = tmpPath;
        inPath = tmpPath;
        outPath = tmpPath;

        double[] sc = new double[8];
        double shtp[] = new double[1000];
        double damb[] = new double[1000];
        double damq[] = new double[1000];
        double[] dv = new double[50000];
        double[] d = new double[50000];

        int nbets = 0;
        int kb = 0;
        int iqzero = 0;
        int kno = 0;
        String lsqda = inPath + "LSQ.DAT";
        BufferedReader lsqdat = new BufferedReader(new FileReader(lsqda));
        String line = lsqdat.readLine();
        line = lsqdat.readLine();
        StringTokenizer tok = new StringTokenizer(line);
        for (int i = 0; i < 8; i++) {
            tok.nextToken();
        }
        int idaliz = Integer.parseInt(tok.nextToken());
        line = lsqdat.readLine();
        tok = new StringTokenizer(line);
        int na = Integer.parseInt(tok.nextToken());
        for (int i = 0; i < 9; i++) {
            tok.nextToken();
        }
        int nocc = Integer.parseInt(tok.nextToken());
        int itemp = Integer.parseInt(tok.nextToken());
        int nsgr = Integer.parseInt(tok.nextToken());
        if (idaliz == 1) {
            nocc = 0;
            itemp = 0;
        }
        int nv = 0;
        if (itemp == 0) {
            nv = 3 * na + nocc + 2;
        }
        if (itemp == 1) {
            nv = 4 * na + nocc + 1;
        }
        int nbet = na;
        if (itemp == 0) {
            nbet = 1;
        }
        int iatmn = 3 * na + 1;
        int ibet1 = iatmn + 1;
        int ibetn = iatmn + nbet;
        for (int i = 0; i < 9; i++) {
            line = lsqdat.readLine();
        }
        line = lsqdat.readLine();
        tok = new StringTokenizer(line);
        double to = Double.parseDouble(tok.nextToken());
        int no = Integer.parseInt(tok.nextToken());
        for (int i = 0; i < no; i++) {
            sc[i] = Double.parseDouble(tok.nextToken());
        }
        line = lsqdat.readLine();
        tok = new StringTokenizer(line);
        int jabn = Integer.parseInt(tok.nextToken());
        int nb = tok.countTokens();
        for (int i = 0; i < nb; i++) {
            shtp[i] = Double.parseDouble(tok.nextToken());
        }
        if (jabn > 15) {
            line = lsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 15; i < 15 + nb; i++) {
                shtp[i] = Double.parseDouble(tok.nextToken());
            }
        }
        if (itemp != 0) {
            line = lsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 0; i < nb; i++) {
                damb[i] = Double.parseDouble(tok.nextToken());
            }
            if (jabn > 15) {
                line = lsqdat.readLine();
                tok = new StringTokenizer(line);
                nb = tok.countTokens();
                for (int i = 15; i < 15 + nb; i++) {
                    damb[i] = Double.parseDouble(tok.nextToken());
                }
            }
        }
        if (nocc != 0) {
            line = lsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 0; i < nb; i++) {
                damq[i] = Double.parseDouble(tok.nextToken());
            }
            if (jabn > 15) {
                line = lsqdat.readLine();
                tok = new StringTokenizer(line);
                nb = tok.countTokens();
                for (int i = 15; i < 15 + nb; i++) {
                    damq[i] = Double.parseDouble(tok.nextToken());
                }
            }
        }
        lsqdat.close();
        String shftsbi = tmpPath + "SHFTS.BIN";

        nucLSQ.ishftw.close();

        System.gc();

        BufferedReader shftsbin = new BufferedReader(new FileReader(shftsbi));
        for (int i = 0; i < nv; i++) {
            dv[i] = 0;
        }
        for (int jab1 = 1; jab1 <= jabn; jab1++) {
            line = shftsbin.readLine();
            if (line == null) {
                throw new Exception("No more data");
            }
            tok = new StringTokenizer(line);
            for (int ic = 0; ic < nv; ic++) {
                if (tok.countTokens() > 0) {
                    d[ic] = Double.parseDouble(tok.nextToken());
                } else {
                    System.out.println("failed to read token " + (ic + 1) + " / " + nv);
                }
            }
            double sksh = shtp[jab1 - 1];
            if (sksh > 0) {
                for (int i = 1; i < iatmn; i++) {
                    dv[i] += sksh * d[i];
                }
                if (itemp != 0) {
                    sksh = damb[jab1 - 1];
                    if (sksh > 0) {
                        nbets = na;
                        if (nocc > 0 && damq[jab1 - 1] > 0) {
                            nbets = na - nocc;
                        }
                        for (int i = 0; i < nbets; i++) {
                            int j = iatmn + i;
                            dv[j] += sksh * d[j];
                        }
                    }
                }
                if (nocc != 0) {
                    sksh = damq[jab1 - 1];
                    if (sksh > 0) {
                        for (int i = 0; i < nocc; i++) {
                            int j = ibetn + i;
                            dv[j] += sksh * d[j];
                        }
                    }
                }
            }
        }
        shftsbin.close();
        String lsqin = inPath + "LSQ.INP";
        BufferedReader inlsqinp = new BufferedReader(new FileReader(lsqin));
        String atomsne = outPath + "ATOMS.hd";
        PrintWriter outatomsnew = new PrintWriter(atomsne);
        for (int i = 1; i <= na; i++) {
            line = inlsqinp.readLine();
            HD hd= new HD(line);
            int nn = hd.atomNumber;
            char residueName = hd.residueName;
            int residueNumber = hd.residueNumber;
            String atom = hd.atomName;
            int kss = hd.atomCode;
            double x = hd.x;
            double y = hd.y;
            double z = hd.z;
            double bt = hd.b;
            double q = hd.q;

            x += dv[3 * i - 2];
            y += dv[3 * i - 1];
            z += dv[3 * i];
            int i4 = i;
            int k4 = kss;
            if (itemp != 0 && kb <= nbets) {
                kb++;
                bt += dv[iatmn + i - 1];
                if (sc[1] > bt) {
                    bt = sc[1];
                }
            }
            if (nocc != 0 && i > iqzero && q >= 0 && kno <= nocc) {
                kno++;
                int m = i - iqzero;
                q += dv[ibetn + m - 1];
                if (q < 0.01) {
                    q = 0.01;
                }
                if (q > 1) {
                    q = 1;
                }
            }
            outatomsnew.println(HD.getAtomString(i4, residueName, residueNumber, atom, x, y, z, bt, q));
        }
        inlsqinp.close();
        outatomsnew.close();
    }
}
