package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.computations.Rnart;
import fr.unistra.ibmc.assemble2.utils.HD2PDB;
import fr.unistra.ibmc.assemble2.utils.IoUtils;

import java.io.File;

/**
 * Java version of nucmult.f 13/09/2005<br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original nucmult.f is at the end of this file<br/>
 * <br/>
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

public class NucMult extends Thread {
    /**
     * path
     */
    public String tmpPath = null;//"workingDir"+System.getProperty("file.separator");
    public String inPath = null;//tmpPath;
    public String outPath = null;//tmpPath;

    public float oldV = Float.MAX_VALUE;
    public float curV = Float.MAX_VALUE;
    public float tmpV = Float.MAX_VALUE;
    public int npass = 0;
    public float value = 0;
    public int pourcent = 0;

    private int passOver;
    private Mediator mediator;

    public NucMult(Mediator mediator, String mode, String pass, String tmpPath) throws Exception {
        int distances_from_deviation = Integer.MAX_VALUE;
        this.mediator = mediator;
        System.err.println("Starting rnart with " + mode + " " + pass + " " + tmpPath);
        this.tmpPath = tmpPath;
        inPath = tmpPath;
        outPath = tmpPath;
        oldV = Float.MAX_VALUE;
        curV = Float.MAX_VALUE;
        tmpV = Float.MAX_VALUE;
        npass = 0;
        value = 0;
        pourcent = 0;

        char flag = mode.toLowerCase().charAt(0);

        if (flag == 'n') {
            npass = Integer.parseInt(pass);
        }
        if (flag == 'v') {
            value = Float.parseFloat(pass);
        }
        if (flag == 'p') {
            pourcent = Integer.parseInt(pass);
        }

        String lsqout = outPath + "LSQ.OUT";
        String atomshd = outPath + "ATOMS.hd";

        Nuclin nuclin = new Nuclin(this.tmpPath);
        // System.err.println("Nuclin Done");
        int i = 1;
        while (i < 101) {
            //   System.err.println("Step "+i);
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Refinement iteration #" + i, null, null);
            mediator.getSecondaryCanvas().repaint();
            //  System.err.println("NucLSQ Done");
            String name1 = outPath + "LSQ_";
            String name2 = outPath + "ATOMS_";
            NucLSQ nucLSQ = new NucLSQ(this);
            distances_from_deviation = nucLSQ.main();

            IncNuc incNuc = new IncNuc(this.tmpPath);
            // System.err.println("IncNuc Done");
            MgrNuc mgrNuc = new MgrNuc(this.tmpPath, nucLSQ);
            //  System.err.println("MgrNuc Done");
            if (i < 10) {
                name1 += "0" + i + ".OUT";
            } else {
                name1 += i + ".OUT";
            }

            IoUtils.copyFile(new File(lsqout), new File(name1));

            if (i < 10) {
                name2 += "0" + i + ".hd";
            } else {
                name2 += i + ".hd";
            }

            IoUtils.copyFile(new File(atomshd), new File(name2));
            HD2PDB.convert(name2, name2 + ".pdb");

            oldV = curV;
            curV = tmpV;

            if (flag == 'n' && i > npass - 1) {
                System.out.println("Stopping : number of iterations = " + i);
                break;
            }
            if (flag == 'p' && curV / oldV > (pourcent / 100f)) {
                int tV = (int) (100 * curV / oldV);
                System.out.println("Stopping : average deviation between the 2 last iterations, are similar at  = " + tV + "% < " + pourcent + "%");
                break;
            }
            if (flag == 'v' && curV < value) {
                System.out.println("Stopping : average deviation = " + curV + " > " + value);
                break;
            }
            i++;
        }
        passOver = i;
        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Refinement done: "+distances_from_deviation+" distances deviate from ideality by more than 2 sigma", null, null);
        mediator.getSecondaryCanvas().repaint();
    }

    public int getNumberOfPass() {
        return passOver;
    }
}
