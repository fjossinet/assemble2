package fr.unistra.ibmc.assemble2.io.computations.rnart;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Java version of incnuc.f 13/09/2005 (Thomas Ludwig)<br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original incnuc.f is at the end of this file<br/>
 * <br/>
 * Program INCNUC<br/>
 * PROGRAM TO INCREASE THE CYCLE NUMBER IN LSQ.DAT<br/>
 * THE PROGRAM WILL INSERT DAMP(JABN+1)=DAMP(JABN).<br/>
 * 30 CYCLES ARE ALLOWED.<br/>
 * <br/>
 * E.WESTHOF 02/1982<br/>
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

public class IncNuc {
    /**
     * path
     */
    public String tmpPath;
    public String inPath;
    public String outPath;


    public IncNuc(String tmpPath) throws Exception {
        this.tmpPath = tmpPath;
        inPath = tmpPath;
        outPath = tmpPath;
        //line formatting of the output differs from fortran formatting (but anyway its meant for java)
        double[] damp = new double[1000];
        double[] damb = new double[1000];
        double[] damq = new double[1000];
        double[] shftk = new double[1000];

        String lsqdat = inPath + "LSQ.DAT";
        BufferedReader inlsqdat = new BufferedReader(new FileReader(lsqdat));
        String line = inlsqdat.readLine();
        StringTokenizer tok = new StringTokenizer(line);
        String ik = tok.nextToken();
        line = inlsqdat.readLine();
        tok = new StringTokenizer(line);
        int i1 = Integer.parseInt(tok.nextToken());
        int i2 = Integer.parseInt(tok.nextToken());
        int i3 = Integer.parseInt(tok.nextToken());
        int i4 = Integer.parseInt(tok.nextToken());
        int i5 = Integer.parseInt(tok.nextToken());
        int i6 = Integer.parseInt(tok.nextToken());
        int i7 = Integer.parseInt(tok.nextToken());
        int i8 = Integer.parseInt(tok.nextToken());
        int idaliz = Integer.parseInt(tok.nextToken());
        line = inlsqdat.readLine();
        tok = new StringTokenizer(line);
        int na = Integer.parseInt(tok.nextToken());
        i1 = Integer.parseInt(tok.nextToken());
        i2 = Integer.parseInt(tok.nextToken());
        i3 = Integer.parseInt(tok.nextToken());
        i4 = Integer.parseInt(tok.nextToken());
        i5 = Integer.parseInt(tok.nextToken());
        i6 = Integer.parseInt(tok.nextToken());
        i7 = Integer.parseInt(tok.nextToken());
        i8 = Integer.parseInt(tok.nextToken());
        int i9 = Integer.parseInt(tok.nextToken());
        int nocc = Integer.parseInt(tok.nextToken());
        int itemp = Integer.parseInt(tok.nextToken());
        int nsgr = Integer.parseInt(tok.nextToken());
        for (int i = 0; i < 10; i++) {
            line = inlsqdat.readLine();
        }
        line = inlsqdat.readLine();
        tok = new StringTokenizer(line);
        int jabn = Integer.parseInt(tok.nextToken());
        int nb = tok.countTokens();
        for (int i = 0; i < nb; i++) {
            damp[i] = Double.parseDouble(tok.nextToken());
        }
        if (jabn > 15) {
            line = inlsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 15; i < 15 + nb; i++) {
                damp[i] = Double.parseDouble(tok.nextToken());
            }
        }
        if (itemp != 0) {
            line = inlsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 0; i < nb; i++) {
                damb[i] = Double.parseDouble(tok.nextToken());
            }
            if (jabn > 15) {
                line = inlsqdat.readLine();
                tok = new StringTokenizer(line);
                nb = tok.countTokens();
                for (int i = 15; i < 15 + nb; i++) {
                    damb[i] = Double.parseDouble(tok.nextToken());
                }
            }
        }
        if (nocc != 0) {
            line = inlsqdat.readLine();
            tok = new StringTokenizer(line);
            nb = tok.countTokens();
            for (int i = 0; i < nb; i++) {
                damq[i] = Double.parseDouble(tok.nextToken());
            }
            if (jabn > 15) {
                line = inlsqdat.readLine();
                tok = new StringTokenizer(line);
                nb = tok.countTokens();
                for (int i = 15; i < 15 + nb; i++) {
                    damq[i] = Double.parseDouble(tok.nextToken());
                }
            }
        }
        line = inlsqdat.readLine();
        tok = new StringTokenizer(line);
        int irtest = Integer.parseInt(tok.nextToken());
        int nsampl = 0, japn = 0, jabn1 = 0;
        if (tok.countTokens() > 0) {
            nsampl = Integer.parseInt(tok.nextToken());
        }
        if (tok.countTokens() > 0) {
            japn = Integer.parseInt(tok.nextToken());
        }
        if (tok.countTokens() > 0) {
            jabn1 = Integer.parseInt(tok.nextToken());
        }
        if (tok.countTokens() > 0) {
            for (int i = 0; i < jabn1; i++) {
                shftk[i] = Double.parseDouble(tok.nextToken());
            }
        }
        inlsqdat.close();
        PrintWriter outlsqdattmp = new PrintWriter(new FileWriter(lsqdat + ".tmp"));
        inlsqdat = new BufferedReader(new FileReader(lsqdat));
        while ((line = inlsqdat.readLine()) != null) {
            outlsqdattmp.println(line);
        }
        inlsqdat.close();
        outlsqdattmp.close();

        BufferedReader inlsqdattmp = new BufferedReader(new FileReader(lsqdat + ".tmp"));
        PrintWriter outlsqdat = new PrintWriter(new FileWriter(lsqdat));
        for (int i = 0; i < 13; i++) {
            line = inlsqdattmp.readLine();
            outlsqdat.println(line);
        }
        jabn++;
        if (jabn == 1) {
            if (damp[0] == 0) {
                damp[0] = 0.35;
            }
            if (damb[0] == 0) {
                damb[0] = 0.35;
            }
            if (damq[0] == 0) {
                damq[0] = 0.35;
            }
        } else {
            damp[jabn - 1] = damp[jabn - 2];
            damb[jabn - 1] = damb[jabn - 2];
            damq[jabn - 1] = damq[jabn - 2];
        }
        String tmp = jabn + "";
        for (int i = 0; i < 15; i++) {
            tmp += " " + damp[i];
        }
        outlsqdat.println(tmp);
        if (jabn > 15) {
            tmp = damp[15] + "";
            for (int i = 16; i < jabn; i++) {
                tmp += " " + damp[i];
            }
            outlsqdat.println(tmp);
        }
        if (itemp != 0) {
            tmp = damb[0] + "";
            for (int i = 1; i < 15; i++) {
                tmp += " " + damb[i];
            }
            outlsqdat.println(tmp);
            if (jabn > 15) {
                tmp = damb[15] + "";
                for (int i = 16; i < jabn; i++) {
                    tmp += " " + damb[i];
                }
                outlsqdat.println(tmp);
            }
        }
        if (nocc != 0) {
            tmp = damq[0] + "";
            for (int i = 1; i < 15; i++) {
                tmp += " " + damq[i];
            }
            outlsqdat.println(tmp);
            if (jabn > 15) {
                tmp = damq[15] + "";
                for (int i = 16; i < jabn; i++) {
                    tmp += " " + damq[i];
                }
                outlsqdat.println(tmp);
            }
        }
        tmp = irtest + " " + nsampl + " " + japn + " " + jabn1;
        for (int i = 0; i < jabn1; i++) {
            tmp += " " + shftk[i];
        }
        outlsqdat.println(tmp);
        inlsqdattmp.close();
        outlsqdat.close();
    }
}
