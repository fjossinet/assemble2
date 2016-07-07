package fr.unistra.ibmc.assemble2.utils;

import java.text.DecimalFormat;

public class TBMath {

    /**
     * Constant : conversion from degree to radian (1 deg = <b>0.017453293</b> rad)
     */
    public static double DegreeToRadian = Math.PI / 180;

    /**
     * Constant : conversion from radian to degree (1 rad = <b>57.29578</b> deg)
     */
    public static double RadianToDegree = 180 / Math.PI;

    public static String round(float f, int n) throws Exception {
        String s = "" + f;
        float offset = Float.parseFloat("10E-" + (n + 1));
        for (int i = 0; i < n + 2; i++) {
            s += "0";
        }
        String s2 = s.substring(0, s.indexOf('.') + n + 1);
        char round = s.charAt(s.indexOf('.') + n + 1);
        if (round >= '5') {
            s2 = "" + (float) (Float.parseFloat(s2) + offset);
        }

        return s2;
    }

    public static String getCoord(float[] f) throws Exception {
        String ret = "";
        if (f.length > 0) {
            ret += round(f[0], 3);
        }
        for (int i = 1; i < f.length; i++) {
            ret += " " + round(f[i], 3);
        }
        return ret;
    }

    public static String doubleFormat(double value, int total, int afterDot) {
        String ret = "";
        String pattern = "0.";
        for (int i = 0; i < afterDot; i++)
            pattern += "0";
        for (int i = 0; i < total - (afterDot + 2); i++)
            pattern = "#" + pattern;
        DecimalFormat form = new DecimalFormat(pattern);
        ret = form.format(value).replace(',', '.');
        while (ret.length() < total)
            ret = " " + ret;

        if (ret.length() > total) {
            System.out.println("inapropriate string length while formatting the text " + value + " in format " + total + "F" + afterDot);
            System.out.println("pattern "+pattern);
            System.out.println("result "+ret);
            if (ret.length() > total)
                ret = ret.substring(0, total);
            System.out.println("after fix: "+ret);
        }

        return ret;
    }

    public static String doubleFormatWithE(double value, int total, int exp, boolean before) throws Exception {
        String ret = "";
        try {
            if (total < exp + 4)
                throw new Exception("inapropriate string length for number " + value + " size " + total + " exponant size " + exp);

            int numlength = total - 2;
            int exVal;
            if (value == 0)
                exVal = 0;
            else
                exVal = (int) Math.log10(value);
            String exStr = "" + Math.abs(exVal);
            while (exStr.length() < exp) {
                exStr = "0" + exStr;
            }
            numlength -= exStr.length();

            ret = "" + ((value) / Math.pow(10, exVal));

            if (ret.length() > numlength)
                ret = ret.substring(0, numlength);

            while (ret.length() < numlength) {
                ret = ret += "0";
            }
            ret += "E";
            if (exVal < 0)
                ret += "-";
            else
                ret += "+";
            ret += exStr;
            if (ret.length() > total) {
                throw new Exception("inapropriate string length (" + ret.length() + " > " + total + ") while formatting the text " + value);
            }
        }
        catch (Exception e) {
            System.err.println("inapropriate string length for number " + value + " size " + total + " exponant size " + exp);
            e.printStackTrace();
        }
        return ret;
    }

    public static String scientificDoubleFormat(double value, int total, int afterDot, int exponent) throws Exception {

        int entire = total - (3 + afterDot + exponent);
        double tmp = value;
        char signe = '+';
        int power = 0;
        while (Math.abs(tmp) > 1) {
            tmp = tmp / 10;
            power++;
        }
        while (Math.abs(tmp) < 0.1) {
            signe = '-';
            tmp = tmp * 10;
            power++;
        }
        String head = doubleFormat(tmp, entire + 1 + afterDot, afterDot);
        String queue = "" + power;
        if (power < 10) {
            queue = "0" + queue;
        }
        String ret = head + "E" + signe + queue;


        while (ret.length() < total) {
            ret = " " + ret;
        }
            if (ret.length() > total) {
                throw new Exception("inapropriate string length while formatting the text " + value + " in format " + total + "F" + afterDot);
            }

        return ret;
    }


    public static String intFormat(int value, int total) throws Exception {
        String ret = "" + value;
        while (ret.length() < total) {
            ret = " " + ret;
        }
            if (ret.length() > total) {
                throw new Exception("inapropriate string length while formatting the text " + value + "in format I" + total);
            }

        return ret;
    }


    /**
     * Computes the norm of a vector
     *
     * @param v float[] - the vector
     * @return float - the norm
     */
    public static float norm(float[] v) {
        float s = 0;
        for (int i = 0; i < v.length; i++) {
            s += v[i] * v[i];
        }
        return (float) Math.sqrt(s);
    }

    /**
     * Returns true if the string is a number (int or double)
     *
     * @param s String
     * @return boolean
     */
    public static boolean isNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c != '0') && (c != '1') && (c != '2') && (c != '3') && (c != '4') && (c != '5') && (c != '6') && (c != '7') && (c != '8') && (c != '9') && (c != '-') && (c != '.')) {
                return false;
            }

        }
        return true;
    }

    /**
     * Returns the center of two points
     *
     * @param a - the first point
     * @param b - the second point
     * @return - the middle of the two points ; null if a & b are not of the same dimension
     */
    public static float[] middle(float[] a, float b[]) {
        if (a.length == b.length) {
            float ret[] = new float[a.length];
            for (int i = 0; i < a.length; i++) {
                ret[i] = (a[i] + b[i]) / 2;
            }
            return ret;
        }
        System.out.println("length of vector mismatch");
        return null;
    }

    /**
     * computes the norm of a vector
     *
     * @param v double[] - the vector
     * @return double - the norm
     */
    public static double norm(double[] v) {
        double s = 0;
        for (int i = 0; i < v.length; i++) {
            s += v[i] * v[i];
        }
        return Math.sqrt(s);
    }

    /**
     * computes the distance between to point (of any dimension)
     *
     * @param a float[] - the coordinates of the 1st point
     * @param b float[] - the coordinates of the 2nd point
     * @return float - the distance
     */
    public static float distance(float[] a, float[] b) {
        if (a.length != b.length) {
            System.out.println("TBMath.distance(a,b) size of vectors mismatch");
            return Float.MAX_VALUE;
        }

        float s = 0;
        for (int i = 0; i < a.length; i++) {
            s += (a[i] - b[i]) * (a[i] - b[i]);
        }
        float ret = (float) Math.sqrt(s);
        return ret;
    }

    /**
     * computes the distance between to point (of any dimension)
     *
     * @param a float[] - the coordinates of the 1st point
     * @param b float[] - the coordinates of the 2nd point
     * @return float - the distance
     */
    public static double distance(double[] a, double[] b) {
        if (a.length != b.length) {
            System.out.println("TBMath.distance(a,b) size of vectors mismatch");
            return Double.MAX_VALUE;
        }

        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += (a[i] - b[i]) * (a[i] - b[i]);
        }
        double ret = Math.sqrt(s);
        return ret;
    }

    /**
     * Modify of fields of a vector so that it becomes a unit vector
     *
     * @param v float[] - the vector
     */
    public static void normalize(float[] v) {
        float n = norm(v);
        for (int i = 0; i < v.length; i++) {
            v[i] = v[i] / n;
        }
    }

    /**
     * Modify of fields of a vector so that it becomes a unit vector
     *
     * @param v double[] - the vector
     */
    public static void normalize(double[] v) {
        double n = norm(v);
        for (int i = 0; i < v.length; i++) {
            v[i] = v[i] / n;
        }
    }

    /**
     * Outputs an unit vector colinear to the given vecotr
     *
     * @param v float[] - the vector
     * @return float[] - the unit vector
     */
    public static float[] normalized(float[] v) {
        float[] ret = new float[v.length];
        float n = norm(v);
        for (int i = 0; i < v.length; i++) {
            ret[i] = v[i] / n;
        }
        return ret;
    }

    /**
     * Outputs an unit vector colinear to the given vecotr
     *
     * @param v float[] - the vector
     * @return float[] - the unit vector
     */
    public static double[] normalized(double[] v) {
        double[] ret = new double[v.length];
        double n = norm(v);
        for (int i = 0; i < v.length; i++) {
            ret[i] = v[i] / n;
        }
        return ret;
    }

    public static float[] multiMatrixVector(float[] m, float[] v) {
        if (v.length == 3) {
            if (m.length == 9) {
                return m9m3v(m, v);
            } else {
                System.out.println("Matrix and vetor size mismatch");
                return null;
            }
        }
        if (v.length == 4) {
            if (m.length == 16) {
                return m16m4v(m, v);
            } else {
                System.out.println("Matrix and vetor size mismatch");
                return null;
            }
        }
        System.out.println("Excpeting vector of 3 or 4 elements");
        return null;
    }

    public static float[] multiMatrixVector(float[][] m, float[] v) {
        if (v.length == 3) {
            if (m.length == 3 && m[0].length == 3) {
                return m33m3v(m, v);
            } else {
                System.out.println("Matrix and vetor size mismatch");
                return null;
            }
        }
        if (v.length == 4) {
            if (m.length == 4 && m[0].length == 4) {
                return m44m4v(m, v);
            } else {
                System.out.println("Matrix and vetor size mismatch");
                return null;
            }
        }
        System.out.println("Excpeting vector of 3 or 4 elements");
        return null;
    }

    private static float[] m9m3v(float[] m, float[] v) {
        float[] ret = new float[3];
        ret[0] = m[0] * v[0] + m[1] * v[1] + m[2] * v[2];
        ret[1] = m[3] * v[0] + m[4] * v[1] + m[5] * v[2];
        ret[2] = m[6] * v[0] + m[7] * v[1] + m[8] * v[2];
        return ret;
    }

    private static float[] m16m4v(float[] m, float[] v) {
        float[] ret = new float[4];
        ret[0] = m[0] * v[0] + m[1] * v[1] + m[2] * v[2] + m[3] * v[3];
        ret[1] = m[4] * v[0] + m[5] * v[1] + m[6] * v[2] + m[7] * v[3];
        ret[2] = m[8] * v[0] + m[9] * v[1] + m[10] * v[2] + m[11] * v[3];
        ret[3] = m[12] * v[0] + m[13] * v[1] + m[14] * v[2] + m[15] * v[3];
        return ret;
    }

    private static float[] m33m3v(float[][] m, float[] v) {
        float[] ret = new float[3];
        ret[0] = m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2];
        ret[1] = m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2];
        ret[2] = m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2];
        return ret;
    }

    private static float[] m44m4v(float[][] m, float[] v) {
        float[] ret = new float[4];
        ret[0] = m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2] + m[0][3] * v[3];
        ret[1] = m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2] + m[1][3] * v[3];
        ret[2] = m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2] + m[2][3] * v[3];
        ret[3] = m[3][0] * v[0] + m[3][1] * v[1] + m[3][2] * v[2] + m[3][3] * v[3];
        return ret;
    }

    /**
     * Computes the cross product of b & c and store the result in a
     *
     * @param b double[] - double[3] vector
     * @param c double[] - double[3] vector
     * @param a double[] - double[3] vector
     */
    public static void crossProduct(double[] b, double[] c, double[] a) {
        a[0] = b[1] * c[2] - b[2] * c[1];
        a[1] = b[2] * c[0] - b[0] * c[2];
        a[2] = b[0] * c[1] - b[1] * c[0];
    }

    /**
     * Returns the cross product of b & c
     *
     * @param b double[] - double[3] vector
     * @param c double[] - double[3] vector
     * @return double[] - double[3] vector
     */
    public static double[] crossProduct(double[] b, double[] c) {
        double[] a = new double[b.length];
        crossProduct(b, c, a);
        return a;
    }

    /**
     * Computes the cross product of b & c and store the result in a
     *
     * @param b double[] - double[3] vector
     * @param c double[] - double[3] vector
     * @param a double[] - double[3] vector
     */
    public static void crossProduct(float[] b, float[] c, float[] a) {
        a[0] = b[1] * c[2] - b[2] * c[1];
        a[1] = b[2] * c[0] - b[0] * c[2];
        a[2] = b[0] * c[1] - b[1] * c[0];
    }

    /**
     * Returns the cross product of b & c
     *
     * @param b double[] - double[3] vector
     * @param c double[] - double[3] vector
     * @return double[] - double[3] vector
     */
    public static float[] crossProduct(float[] b, float[] c) {
        float[] a = new float[b.length];
        crossProduct(b, c, a);
        return a;
    }

    /**
     * Returns a torsion (dihedral) angle, being given 4 (3D)points
     *
     * @param x1 double[] - 1st point
     * @param x2 double[] - 2nd point
     * @param x3 double[] - 3rd point
     * @param x4 double[] - 4th point
     * @return double - the angle
     */
    public static double torsx(double[] x1, double[] x2, double[] x3, double[] x4) {
        double the;

        double u1[] = new double[3];
        double u2[] = new double[3];
        double u3[] = new double[3];
        double uu[] = new double[3];
        double vv[] = new double[3];

        for (int i = 0; i < 3; i++) {
            u1[i] = x3[i] - x2[i];
            u2[i] = x1[i] - x2[i];
            u3[i] = x4[i] - x3[i];
        }
        TBMath.crossProduct(u1, u2, uu);
        TBMath.crossProduct(u1, u3, vv);

        TBMath.normalize(uu);
        TBMath.normalize(vv);
        double ecos = 0;

        for (int i = 0; i < 3; i++) {
            ecos += uu[i] * vv[i];
        }
        TBMath.crossProduct(uu, vv, u2);

        double esin = 0;

        for (int i = 0; i < 3; i++) {
            esin += u2[i] * u2[i];
        }
        esin = Math.sqrt(esin);

        double sum = 0;

        for (int i = 0; i < 3; i++) {
            sum += u1[i] * u2[i];

        }
        if (sum >= 0) {
            esin = Math.abs(esin);
        } else {
            esin = -Math.abs(esin);

        }
        the = Math.atan2(esin, ecos) * 180 / Math.PI;
        if (the > 180) {
            the = the - 360;
        }
        return the;
    }

    public static void singleToDouble(double in[], double[][] ret) {
        int t = ret.length;
        for (int i = 0; i < in.length; i++) {
            int x = i / t;
            int y = i % t;

            ret[x][y] = in[i];
        }
    }

    public static void DoubleToSingle(double in[][], double[] ret) {
        int n = in.length;
        int a = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ret[a] = in[i][j];
                a++;
            }
        }
    }

    /**
     * Returns the position of the first char not beeing a digit (usefull to separe string as <i>56N5</i>
     *
     * @param s String
     * @return int
     */
    public static int endOfInt(String s) {
        int ret = -1;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4') || (c == '5') || (c == '6') || (c == '7') || (c == '8') || (c == '9')) {
                ret++;
            } else {
                break;
            }
        }

        return ret;
    }

    /**
     * returns a double with the value of the first argument and the sign of the second
     *
     * @param valueOf double
     * @param signOf  double
     * @return double
     */
    public static double sign(double valueOf, double signOf) {
        if (signOf > 0) {
            return Math.abs(valueOf);
        } else {
            return -Math.abs(valueOf);
        }
    }

    /**
     * returns the angle formed by 2 vector (the last point of v1 is supposed to be the first of v2)
     *
     * @param v1 float[] - the first vector
     * @param v2 float[] - the second vector
     * @return float - the angle (in degree)
     */
    public static float angle(float[] v1, float[] v2) {
        float ret = dotProduct(v1, v2) / norm(v1) / norm(v2);
        if (ret < -1) {//1.0000000000001 -> 1
            ret = -1;
        }
        if (ret > 1) {
            ret = 1;
        }
        return (float) (Math.acos(ret));
    }

    /**
     * returns the dihedral angle formed by 3 vector (a-b,b-c,c-d)
     *
     * @param a float[] - the point a
     * @param b float[] - the point b
     * @param c float[] - the point c
     * @param d float[] - the point d
     * @return float - the dihedral angle (in degree)
     */
    public static float dihedral(float[] a, float[] b, float[] c, float[] d) {
        float[] v1 = vector(a, b);
        float[] v2 = vector(b, c);
        float[] v3 = vector(c, d);

        float[] w1 = crossProduct(v1, v2);
        float[] w2 = crossProduct(v2, v3);
        float[] w = crossProduct(w1, w2);

        float arg = dotProduct(w1, w2) / norm(w1) / norm(w2);

        if (arg > 1)  // avoids error acos(1.00000000001)
        {
            arg = 1;
        }
        if (arg < -1) {
            arg = -1;
        }

        float ret = (float) Math.acos(arg);

        if (dotProduct(v2, w) > 0) {
            return ret;
        } else {
            return -ret;
        }
    }

    public static float[] vector(float[] a, float[] b) {
        float[] ret = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = b[i] - a[i];
        }
        return ret;
    }

    public static double[] vector(double[] a, double[] b) {
        double[] ret = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = b[i] - a[i];
        }
        return ret;
    }

    public static float dotProduct(float[] v1, float[] v2) {
        float ret = 0;
        for (int i = 0; i < v1.length; i++) {
            ret += v1[i] * v2[i];
        }
        return ret;
    }

    public static double dotProduct(double[] v1, double[] v2) {
        double ret = 0;
        for (int i = 0; i < v1.length; i++) {
            ret += v1[i] * v2[i];
        }
        return ret;

    }

    /**
     * applies a rotation to a vector
     *
     * @param coord    float[] - the coordinates of the the fix point
     * @param rotation float[] - the vector around wich we are turning
     * @param angle    int - the angle (in degree)
     * @return float[]
     */
    public static float[] rotateVector(float[] coord, float[] rotation, float angle) {
        float[] ret = new float[3];
        float ang = angle * ((float) Math.PI) / 180;

        float x = rotation[0];
        float y = rotation[1];
        float z = rotation[2];

        float c = 1 - (float) Math.cos(ang);
        float s = (float) Math.sin(ang);

        float[][] matrix = {{1 + c * (x * x - 1), -z * s + c * x * y, y * s + c * x * z}, {z * s + c * x * y, 1 + c * (y * y - 1), -x * s + c * y * z}, {-y * s + c * x * z, x * s + c * y * z, 1 + c * (z * z - 1)}};

        ret[0] = matrix[0][0] * coord[0] + matrix[1][0] * coord[1] + matrix[2][0] * coord[2];
        ret[1] = matrix[0][1] * coord[0] + matrix[1][1] * coord[1] + matrix[2][1] * coord[2];
        ret[2] = matrix[0][2] * coord[0] + matrix[1][2] * coord[1] + matrix[2][2] * coord[2];

        return ret;
    }

    public static float[] multMatrices44(float[] a, float[] b) {
        float[] c = new float[16];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                c[pos(i, j)] = a[pos(i, 0)] * b[pos(0, j)] + a[pos(i, 1)] * b[pos(1, j)] + a[pos(i, 2)] * b[pos(2, j)] + a[pos(i, 3)] * b[pos(3, j)];
            }
        return c;
    }

    public static float[][] times(float[][] a, float[][] b) {
        int m = a.length;
        float c[][] = new float[m][m];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                c[i][j] = 0;
                for (int k = 0; k < m; k++)
                    c[i][j] += a[i][k] * b[k][j];

            }
        }
        return c;
    }

    public static double[][] doubleFromFloat(float[][] f) {
        double[][] d = new double[f.length][f[0].length];

        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                d[i][j] = f[i][j];
            }
        }

        return d;
    }

    public static float[][] floatFromDouble(double[][] d) {
        float[][] f = new float[d.length][d[0].length];

        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[i].length; j++) {
                f[i][j] = (float) d[i][j];
            }
        }

        return f;
    }

    public static float[][] negate(float[][] m) {
        float[][] f = new float[m.length][m[0].length];

        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[i].length; j++) {
                f[i][j] = -m[i][j];
            }
        }

        return f;
    }

    public static float[][] inverse(float[][] m) {
        Matrix mat = new Matrix(doubleFromFloat(m));
        return floatFromDouble(mat.inverse().getArray());
    }

    public static int pos(int i, int j) {
        return (i * 4 + j);
    }

    public static float[][] multiMatrices33(float[][] a, float[][] b) {
        float c[][] = new float[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                c[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return c;
    }

    public static float[][] inverseMatrix33(float[][] a) {
        float c[][] = new float[3][3];
        float d = detMatrix33(a);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                c[i][j] = detMatrix33(a, j, i) / d;
            }
        }
        return c;
    }

    public static float detMatrix33(float[][] a) {
        return a[0][0] * a[1][1] * a[2][2] - a[0][0] * a[1][2] * a[2][1] + a[0][1] * a[1][2] * a[2][0] - a[0][1] * a[1][0] * a[2][2] + a[0][2] * a[1][0] * a[2][1] - a[0][2] * a[1][1] * a[2][0];
    }

    public static float detMatrix33(float[][] m, int i, int j) {
        int l1, l2, c1, c2;

        l1 = (i + 1) % 3;
        l2 = (i + 2) % 3;
        c1 = (j + 1) % 3;
        c2 = (j + 2) % 3;

        return m[l1][c1] * m[l2][c2] - m[l1][c2] * m[l2][c1];
    }


    public static float[] getRotationMatrix(float[] vect, float angle) {
        float[] v = normalized(vect);
        float[] ret = new float[9];
        float x = v[0];
        float y = v[1];
        float z = v[2];
        /*
                1 + (1-cos(angle))*(x*x-1)  	-z*sin(angle)+(1-cos(angle))*x*y  	y*sin(angle)+(1-cos(angle))*x*z
        z*sin(angle)+(1-cos(angle))*x*y 	1 + (1-cos(angle))*(y*y-1) 	-x*sin(angle)+(1-cos(angle))*y*z
        -y*sin(angle)+(1-cos(angle))*x*z 	x*sin(angle)+(1-cos(angle))*y*z 	1 + (1-cos(angle))*(z*z-1)
        */
        float ca = (float) Math.cos(angle);
        float sa = (float) Math.sin(angle);
        float mca = 1 - ca;


        ret[0] = 1 + mca * (x * x - 1);
        ret[1] = -z * sa + mca * x * y;
        ret[2] = y * sa + mca * x * z;
        ret[3] = z * sa + mca * x * y;
        ret[4] = 1 + mca * (y * y - 1);
        ret[5] = -x * sa + mca * y * z;
        ret[6] = -y * sa + mca * x * z;
        ret[7] = x * sa + mca * y * z;
        ret[8] = 1 + mca * (z * z - 1);

        System.out.println("somme " + (ret[0] + ret[4] + ret[8]));
        System.out.println("out " + (1 + 2 * Math.cos(angle)));

        return ret;
    }

    public static float[] multVectorScalar(float[] vector, float scalar) {
        float[] ret = new float[vector.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = scalar * vector[i];
        return ret;
    }

    public static float[] sumVectors(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length)
            return null;
        float[] ret = new float[vector1.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = vector1[i] + vector2[i];
        return ret;
    }

    /**
     * Give the number of intersection points between a line and a circle
     *
     * @param x - first point of the line
     * @param y - second point of the line
     * @param c - center of the circle
     * @param r - radius of the circle
     * @return - number of the intersection points
     */
    public static int lineIntersectsCircle(float[] x, float[] y, float[] c, float r) {
        float[] a = {x[0] - c[0], x[1] - c[1]};
        float[] b = {y[0] - c[0], y[1] - c[1]};
        float d = (float) Math.sqrt((b[0] - a[0]) * (b[0] - a[0]) + (b[1] - a[1]) * (b[1] - a[1]));
        float D = a[0] * b[1] - b[0] * a[1];
        float delta = r * r + d * d - D * D;
        if (delta < 0)
            return 0;
        if (delta == 0)
            return 1;
        return 2;
    }

    /**
     * Tells is a plane intersects a sphere
     *
     * @param o - first point of the plane
     * @param a - second point of the plane
     * @param b - third point of the plane
     * @param c - center of the sphere
     * @param r - radius of the sphere
     * @return - true if the plane intersects the sphere
     */
    public static boolean planeIntersectsSphere(float[] o, float[] a, float[] b, float[] c, float r) {
        float oa[] = TBMath.vector(o, a);
        float ob[] = TBMath.vector(o, b);
        float n[] = TBMath.normalized(TBMath.crossProduct(oa, ob));
        float d = -TBMath.dotProduct(n, o);
        float dist = n[0] * c[0] + n[1] * c[1] + n[2] * c[2] + d;

        return dist < r;
    }

    /**
     * Tells is a line intersects a sphere
     *
     * @param a - first point of the line
     * @param b - second point of the line
     * @param c - center of the sphere
     * @param r - radius of the sphere
     * @return - true if the line intersects the sphere
     */
    public static boolean lineIntersectsSphere(float[] a, float[] b, float[] c, float r) {
        float[] ab = TBMath.vector(a, b);
        float dist = TBMath.norm(TBMath.crossProduct(ab, TBMath.vector(c, a))) / TBMath.norm(ab);

        return dist < r;
    }

    public static Matrix getTransformationMatrix(float[] s1, float[] d1, float[] s2, float[] d2, float[] s3, float[] d3, float[] s4, float[] d4) {
        double[][] mr = {
                {s1[0], s2[0], s3[0], s4[0]},
                {s1[1], s2[1], s3[1], s4[1]},
                {s1[2], s2[2], s3[2], s4[2]},
                {1, 1, 1, 1}};
        double[][] mo = {
                {d1[0], d2[0], d3[0], d4[0]},
                {d1[1], d2[1], d3[1], d4[1]},
                {d1[2], d2[2], d3[2], d4[2]},
                {1, 1, 1, 1}};
        Matrix r = new Matrix(mr);
        Matrix o = new Matrix(mo);
        return o.times(r.inverse());
    }
}
