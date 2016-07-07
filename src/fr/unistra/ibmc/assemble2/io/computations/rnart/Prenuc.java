package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.utils.TBMath;

/**
 * Java version of prenuc.f 03/08/2005 <br/>
 * Not translated but rewritten !!!!<br/>
 * FYI : the original prenuc.f is at the end of this file<br/>
 * Program PRENUC<br/>
 * <br/>
 * Program to generate a first NUCLIN.DAT file.<br/>
 * <br/>
 * If you do not want to retraint severely a sugar pucker set its value to 2.<br/>
 * <br/>
 * It calculates also the backbone torsion angles, the glycosyl torsion angle, and the pseudorotation parameters (contained in TORSION.OUT).<br/>
 * <br/>
 * It takes a HD file (ATOMS.DAT) and needs a ABC.DAT file which should contain the cell parameters.<br/>
 * <br/>
 * To run it type : PRENUC I J K L M N<br/>
 * Where I J K L M N are the residue numbers where a new chain (different from the first one) starts.<br/>
 * <br/>
 * Thus, the double-strand d(AGCT)2 will give : prenuc 5<br/>
 * <br/>
 * <br/>
 * E.WESTHOF .1984/9.<br/>
 * <br/>
 * For more than 99 residues.  November 19, 1987.<br/>
 * <br/>
 * August 1992.<br/>
 * <p>Title: Fortran To Java</p>
 * <p/>
 * <p>Description: Java version of old Fortran Tools (nahelix, fragment, nuclin,
 * nuclsq...)</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: IBMC/CNRS - Strasbourg</p>
 */

public class Prenuc {
    double[] x1 = new double[3];
    double[] x2 = new double[3];
    double[] x3 = new double[3];
    double[] x4 = new double[3];
    double[] x = new double[14];
    double[] y = new double[14];
    double[] z = new double[14];
    double gxx = 0, gxy = 0, gxz = 0, gyy = 0, gyz = 0, gzz = 0;

    public Prenuc() {
        double alpha = 0, beta = 0, gamma = 0, a1 = 0, a2 = 0, a3 = 0;
        double ca = Math.cos(alpha * TBMath.DegreeToRadian);
        double cb = Math.cos(beta * TBMath.DegreeToRadian);
        double cg = Math.cos(gamma * TBMath.DegreeToRadian);
        double sg = Math.sin(gamma * TBMath.DegreeToRadian);

        gxx = a1;
        gxy = a2 * cg;
        gxz = a3 * cb;
        gyy = a2 * sg;
        gyz = a3 * (ca - cb * cg) / sg;
        gzz = a3 * Math.sqrt(1 - ca * ca - cb * cb - cg * cg + 2 * ca * cb * cg) / sg;
    }

    void ident(int n1, int n2, int n3, int n4) {
        n1--;
        n2--;
        n3--;
        n4--;

        x1[0] = gxx * x[n1] + gxy * y[n1] + gxz * z[n1];
        x1[1] = gyy * y[n1] + gyz * z[n1];
        x1[2] = gzz * z[n1];

        x2[0] = gxx * x[n2] + gxy * y[n2] + gxz * z[n2];
        x2[1] = gyy * y[n2] + gyz * z[n2];
        x2[2] = gzz * z[n2];

        x3[0] = gxx * x[n3] + gxy * y[n3] + gxz * z[n3];
        x3[1] = gyy * y[n3] + gyz * z[n3];
        x3[2] = gzz * z[n3];

        x4[0] = gxx * x[n4] + gxy * y[n4] + gxz * z[n4];
        x4[1] = gyy * y[n4] + gyz * z[n4];
        x4[2] = gzz * z[n4];
    }
}
