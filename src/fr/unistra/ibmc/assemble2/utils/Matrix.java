package fr.unistra.ibmc.assemble2.utils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Matrix implements Cloneable, java.io.Serializable {

    /* ------------------------
        Class variables
      * ------------------------ */

    /**
     * Array for internal storage of elements.
     *
     * @serial internal array storage.
     */
    private double[][] A;

    /**
     * Row and column dimensions.
     *
     * @serial row dimension.
     * @serial column dimension.
     */
    private int m, n;

    /* ------------------------
        Constructors
      * ------------------------ */

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */

    public Matrix(int m, int n) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
    }

    /**
     * Construct an m-by-n constant matrix.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @param s Fill the matrix with this scalar value.
     */

    public Matrix(int m, int n, double s) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
            }
        }
    }

    /**
     * Construct a matrix from a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @throws IllegalArgumentException All rows must have the same length
     * @see #constructWithCopy
     */

    public Matrix(double[][] A) {
        m = A.length;
        n = A[0].length;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        this.A = A;
    }

    /**
     * Construct a matrix quickly without checking arguments.
     *
     * @param A Two-dimensional array of doubles.
     * @param m Number of rows.
     * @param n Number of colums.
     */

    public Matrix(double[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m    Number of rows.
     * @throws IllegalArgumentException Array length must be a multiple of m.
     */

    public Matrix(double vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length / m : 0);
        if (m * n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i + j * m];
            }
        }
    }

    /* ------------------------
        Public Methods
      * ------------------------ */

    /**
     * Construct a matrix from a copy of a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @throws IllegalArgumentException All rows must have the same length
     */

    public static Matrix constructWithCopy(double[][] A) {
        int m = A.length;
        int n = A[0].length;
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }

    /**
     * Make a deep copy of a matrix
     */

    public Matrix copy() {
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }

    /**
     * Clone the Matrix object.
     */

    public Object clone() {
        return this.copy();
    }

    /**
     * Access the internal two-dimensional array.
     *
     * @return Pointer to the two-dimensional array of matrix elements.
     */

    public double[][] getArray() {
        return A;
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */

    public double[][] getArrayCopy() {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return C;
    }

    /**
     * Make a one-dimensional column packed copy of the internal array.
     *
     * @return Matrix elements packed in a one-dimensional array by columns.
     */

    public double[] getColumnPackedCopy() {
        double[] vals = new double[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                vals[i + j * m] = A[i][j];
            }
        }
        return vals;
    }

    /**
     * Make a one-dimensional row packed copy of the internal array.
     *
     * @return Matrix elements packed in a one-dimensional array by rows.
     */

    public double[] getRowPackedCopy() {
        double[] vals = new double[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                vals[i * n + j] = A[i][j];
            }
        }
        return vals;
    }

    /**
     * Get row dimension.
     *
     * @return m, the number of rows.
     */

    public int getRowDimension() {
        return m;
    }

    /**
     * Get column dimension.
     *
     * @return n, the number of columns.
     */

    public int getColumnDimension() {
        return n;
    }

    /**
     * Get a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @return A(i,j)
     * @throws ArrayIndexOutOfBoundsException
     */

    public double get(int i, int j) {
        return A[i][j];
    }

    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(i0:i1,j0:j1)
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public Matrix getMatrix(int i0, int i1, int j0, int j1) {
        Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i - i0][j - j0] = A[i][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @return A(r(:),c(:))
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public Matrix getMatrix(int[] r, int[] c) {
        Matrix X = new Matrix(r.length, c.length);
        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i][j] = A[r[i]][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c  Array of column indices.
     * @return A(i0:i1,c(:))
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public Matrix getMatrix(int i0, int i1, int[] c) {
        Matrix X = new Matrix(i1 - i0 + 1, c.length);
        double[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i - i0][j] = A[i][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r  Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(r(:),j0:j1)
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public Matrix getMatrix(int[] r, int j0, int j1) {
        Matrix X = new Matrix(r.length, j1 - j0 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i][j - j0] = A[r[i]][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Set a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @param s A(i,j).
     * @throws ArrayIndexOutOfBoundsException
     */

    public void set(int i, int j, double s) {
        A[i][j] = s;
    }

    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X  A(i0:i1,j0:j1)
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public void setMatrix(int i0, int i1, int j0, int j1, Matrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    A[i][j] = X.get(i - i0, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @param X A(r(:),c(:))
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public void setMatrix(int[] r, int[] c, Matrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    A[r[i]][c[j]] = X.get(i, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r  Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X  A(r(:),j0:j1)
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public void setMatrix(int[] r, int j0, int j1, Matrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    A[r[i]][j] = X.get(i, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c  Array of column indices.
     * @param X  A(i0:i1,c(:))
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    public void setMatrix(int i0, int i1, int[] c, Matrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    A[i][c[j]] = X.get(i - i0, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    public static double hypot(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b / a;
            r = Math.abs(a) * Math.sqrt(1 + r * r);
        } else if (b != 0) {
            r = a / b;
            r = Math.abs(b) * Math.sqrt(1 + r * r);
        } else {
            r = 0.0;
        }
        return r;
    }

    /**
     * Matrix transpose.
     *
     * @return A'
     */

    public Matrix transpose() {
        Matrix X = new Matrix(n, m);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    /**
     * One norm
     *
     * @return maximum column sum.
     */

    public double norm1() {
        double f = 0;
        for (int j = 0; j < n; j++) {
            double s = 0;
            for (int i = 0; i < m; i++) {
                s += Math.abs(A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Two norm
     *
     * @return maximum singular value.
     */

    public double norm2() {
        return (new SingularValueDecomposition(this).norm2());
    }

    /**
     * Infinity norm
     *
     * @return maximum row sum.
     */

    public double normInf() {
        double f = 0;
        for (int i = 0; i < m; i++) {
            double s = 0;
            for (int j = 0; j < n; j++) {
                s += Math.abs(A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Frobenius norm
     *
     * @return sqrt of sum of squares of all elements.
     */

    public double normF() {
        double f = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                f = hypot(f, A[i][j]);
            }
        }
        return f;
    }

    /**
     * Unary minus
     *
     * @return -A
     */

    public Matrix uminus() {
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = -A[i][j];
            }
        }
        return X;
    }

    /**
     * C = A + B
     *
     * @param B another matrix
     * @return A + B
     */

    public Matrix plus(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }

    /**
     * A = A + B
     *
     * @param B another matrix
     * @return A + B
     */

    public Matrix plusEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return this;
    }

    /**
     * C = A - B
     *
     * @param B another matrix
     * @return A - B
     */

    public Matrix minus(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return X;
    }

    /**
     * A = A - B
     *
     * @param B another matrix
     * @return A - B
     */

    public Matrix minusEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element multiplication, C = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */

    public Matrix arrayTimes(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element multiplication in place, A = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */

    public Matrix arrayTimesEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element right division, C = A./B
     *
     * @param B another matrix
     * @return A./B
     */

    public Matrix arrayRightDivide(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element right division in place, A = A./B
     *
     * @param B another matrix
     * @return A./B
     */

    public Matrix arrayRightDivideEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element left division, C = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */

    public Matrix arrayLeftDivide(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element left division in place, A = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */

    public Matrix arrayLeftDivideEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return this;
    }

    /**
     * Multiply a matrix by a scalar, C = s*A
     *
     * @param s scalar
     * @return s*A
     */

    public Matrix times(double s) {
        Matrix X = new Matrix(m, n);
        double[][] C = X.A;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = s * A[i][j];
            }
        }
        return X;
    }

    /**
     * Multiply a matrix by a scalar in place, A = s*A
     *
     * @param s scalar
     * @return replace A by s*A
     */

    public Matrix timesEquals(double s) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s * A[i][j];
            }
        }
        return this;
    }

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return Matrix product, A * B
     * @throws IllegalArgumentException Matrix inner dimensions must agree.
     */

    public Matrix times(Matrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix X = new Matrix(m, B.n);
        double[][] C = X.getArray();
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.A[k][j];
            }
            for (int i = 0; i < m; i++) {
                double[] Arowi = A[i];
                double s = 0;
                for (int k = 0; k < n; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    /**
     * LU Decomposition
     *
     * @return LUDecomposition
     * @see LUDecomposition
     */

    public LUDecomposition lu() {
        return new LUDecomposition(this);
    }

    /**
     * QR Decomposition
     *
     * @return QRDecomposition
     * @see QRDecomposition
     */

    public QRDecomposition qr() {
        return new QRDecomposition(this);
    }

    /**
     * Cholesky Decomposition
     *
     * @return CholeskyDecomposition
     * @see CholeskyDecomposition
     */

    public CholeskyDecomposition chol() {
        return new CholeskyDecomposition(this);
    }

    /**
     * Singular Value Decomposition
     *
     * @return SingularValueDecomposition
     * @see SingularValueDecomposition
     */

    public SingularValueDecomposition svd() {
        return new SingularValueDecomposition(this);
    }

    /**
     * Eigenvalue Decomposition
     *
     * @return EigenvalueDecomposition
     * @see EigenvalueDecomposition
     */

    public EigenvalueDecomposition eig() {
        return new EigenvalueDecomposition(this);
    }

    /**
     * Solve A*X = B
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise
     */

    public Matrix solve(Matrix B) {
        return (m == n ? (new LUDecomposition(this)).solve(B) : (new QRDecomposition(this)).solve(B));
    }

    /**
     * Solve X*A = B, which is also A'*X' = B'
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise.
     */

    public Matrix solveTranspose(Matrix B) {
        return transpose().solve(B.transpose());
    }

    /**
     * Matrix inverse or pseudoinverse
     *
     * @return inverse(A) if A is square, pseudoinverse otherwise.
     */

    public Matrix inverse() {
        return solve(identity(m, m));
    }

    /**
     * Matrix determinant
     *
     * @return determinant
     */

    public double det() {
        return new LUDecomposition(this).det();
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */

    public int rank() {
        return new SingularValueDecomposition(this).rank();
    }

    /**
     * Matrix condition (2 norm)
     *
     * @return ratio of largest to smallest singular value.
     */

    public double cond() {
        return new SingularValueDecomposition(this).cond();
    }

    /**
     * Matrix trace.
     *
     * @return sum of the diagonal elements.
     */

    public double trace() {
        double t = 0;
        for (int i = 0; i < Math.min(m, n); i++) {
            t += A[i][i];
        }
        return t;
    }

    /**
     * Generate matrix with random elements
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with uniformly distributed random elements.
     */

    public static Matrix random(int m, int n) {
        Matrix A = new Matrix(m, n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = Math.random();
            }
        }
        return A;
    }

    /**
     * Generate identity matrix
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */

    public static Matrix identity(int m, int n) {
        Matrix A = new Matrix(m, n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1.0 : 0.0);
            }
        }
        return A;
    }


    /**
     * Print the matrix to stdout.   Line the elements up in columns
     * with a Fortran-like 'Fw.d' style format.
     *
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */

    public void print(int w, int d) {
        print(new PrintWriter(System.out, true), w, d);
    }

    /**
     * Print the matrix to the output stream.   Line the elements up in
     * columns with a Fortran-like 'Fw.d' style format.
     *
     * @param output Output stream.
     * @param w      Column width.
     * @param d      Number of digits after the decimal.
     */

    public void print(PrintWriter output, int w, int d) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(d);
        format.setMinimumFractionDigits(d);
        format.setGroupingUsed(false);
        print(output, format, w + 2);
    }

    /**
     * Print the matrix to stdout.  Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters.
     * Note that is the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to US Locale.
     *
     * @param format A  Formatting object for individual elements.
     * @param width  Field width for each column.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */

    public void print(NumberFormat format, int width) {
        print(new PrintWriter(System.out, true), format, width);
    }

    // DecimalFormat is a little disappointing coming from Fortran or C's printf.
    // Since it doesn't pad on the left, the elements will come out different
    // widths.  Consequently, we'll pass the desired column width in as an
    // argument and do the extra padding ourselves.

    /**
     * Print the matrix to the output stream.  Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters.
     * Note that is the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to US Locale.
     *
     * @param output the output stream.
     * @param format A formatting object to format the matrix elements
     * @param width  Column width.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */

    public void print(PrintWriter output, NumberFormat format, int width) {
        output.println();  // start on new line.
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                String s = format.format(A[i][j]); // format the number
                int padding = Math.max(1, width - s.length()); // At _least_ 1 space
                for (int k = 0; k < padding; k++) {
                    output.print(' ');
                }
                output.print(s);
            }
            output.println();
        }
        output.println();   // end with blank line.
    }

    /**
     * Read a matrix from a stream.  The format is the same the print method,
     * so printed matrices can be read back in (provided they were printed using
     * US Locale).  Elements are separated by
     * whitespace, all the elements for each row appear on a single line,
     * the last row is followed by a blank line.
     *
     * @param input the input stream.
     */

    public static Matrix read(BufferedReader input) throws java.io.IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(input);

        // Although StreamTokenizer will parse numbers, it doesn't recognize
        // scientific notation (E or D); however, Double.valueOf does.
        // The strategy here is to disable StreamTokenizer's number parsing.
        // We'll only get whitespace delimited words, EOL's and EOF's.
        // These words should all be numbers, for Double.valueOf to parse.

        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.eolIsSignificant(true);
        java.util.Vector v = new java.util.Vector();

        // Ignore initial empty lines
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
            ;
        }
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new java.io.IOException("Unexpected EOF on matrix read.");
        }
        do {
            v.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st row.
        } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

        int n = v.size();  // Now we've got the number of columns!
        double row[] = new double[n];
        for (int j = 0; j < n; j++)  // extract the elements of the 1st row.
        {
            row[j] = ((Double) v.elementAt(j)).doubleValue();
        }
        v.removeAllElements();
        v.addElement(row);  // Start storing rows instead of columns.
        while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
            // While non-empty lines
            v.addElement(row = new double[n]);
            int j = 0;
            do {
                if (j >= n) {
                    throw new java.io.IOException("Row " + v.size() + " is too long.");
                }
                row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
            } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
            if (j < n) {
                throw new java.io.IOException("Row " + v.size() + " is too short.");
            }
        }
        int m = v.size();  // Now we've got the number of rows.
        double[][] A = new double[m][];
        v.copyInto(A);  // copy the rows out of the vector
        return new Matrix(A);
    }

    /* ------------------------
        Private Methods
      * ------------------------ */

    /**
     * Check if size(A) == size(B) *
     */

    private void checkMatrixDimensions(Matrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

    public class CholeskyDecomposition implements java.io.Serializable {

        /* ------------------------
         Class variables
           * ------------------------ */

        /**
         * Array for internal storage of decomposition.
         *
         * @serial internal array storage.
         */
        private double[][] L;

        /**
         * Row and column dimension (square matrix).
         *
         * @serial matrix dimension.
         */
        private int n;

        /**
         * Symmetric and positive definite flag.
         *
         * @serial is symmetric and positive definite flag.
         */
        private boolean isspd;

        /* ------------------------
         Constructor
           * ------------------------ */

        /**
         * Cholesky algorithm for symmetric and positive definite matrix.
         *
         * @param Arg Square, symmetric matrix.
         * @return Structure to access L and isspd flag.
         */

        public CholeskyDecomposition(Matrix Arg) {
            // Initialize.
            double[][] A = Arg.getArray();
            n = Arg.getRowDimension();
            L = new double[n][n];
            isspd = (Arg.getColumnDimension() == n);
            // Main loop.
            for (int j = 0; j < n; j++) {
                double[] Lrowj = L[j];
                double d = 0.0;
                for (int k = 0; k < j; k++) {
                    double[] Lrowk = L[k];
                    double s = 0.0;
                    for (int i = 0; i < k; i++) {
                        s += Lrowk[i] * Lrowj[i];
                    }
                    Lrowj[k] = s = (A[j][k] - s) / L[k][k];
                    d = d + s * s;
                    isspd = isspd & (A[k][j] == A[j][k]);
                }
                d = A[j][j] - d;
                isspd = isspd & (d > 0.0);
                L[j][j] = Math.sqrt(Math.max(d, 0.0));
                for (int k = j + 1; k < n; k++) {
                    L[j][k] = 0.0;
                }
            }
        }

        /* ------------------------
         Temporary, experimental code.
           * ------------------------ *\

         \** Right Triangular Cholesky Decomposition.
         <P>
         For a symmetric, positive definite matrix A, the Right Cholesky
         decomposition is an upper triangular matrix R so that A = R'*R.
         This constructor computes R with the Fortran inspired column oriented
         algorithm used in LINPACK and MATLAB.  In Java, we suspect a row oriented,
         lower triangular decomposition is faster.  We have temporarily included
         this constructor here until timing experiments confirm this suspicion.
         *\

         \** Array for internal storage of right triangular decomposition. **\
         private transient double[][] R;

         \** Cholesky algorithm for symmetric and positive definite matrix.
         @param  A           Square, symmetric matrix.
         @param  rightflag   Actual value ignored.
         @return             Structure to access R and isspd flag.
         *\

         public CholeskyDecomposition (Matrix Arg, int rightflag) {
            // Initialize.
            double[][] A = Arg.getArray();
            n = Arg.getColumnDimension();
            R = new double[n][n];
            isspd = (Arg.getColumnDimension() == n);
            // Main loop.
            for (int j = 0; j < n; j++) {
               double d = 0.0;
               for (int k = 0; k < j; k++) {
                  double s = A[k][j];
                  for (int i = 0; i < k; i++) {
                     s = s - R[i][k]*R[i][j];
                  }
                  R[k][j] = s = s/R[k][k];
                  d = d + s*s;
                  isspd = isspd & (A[k][j] == A[j][k]);
               }
               d = A[j][j] - d;
               isspd = isspd & (d > 0.0);
               R[j][j] = Math.sqrt(Math.max(d,0.0));
               for (int k = j+1; k < n; k++) {
                  R[k][j] = 0.0;
               }
            }
         }

         \** Return upper triangular factor.
         @return     R
         *\

         public Matrix getR () {
            return new Matrix(R,n,n);
         }

          \* ------------------------
         End of temporary code.
           * ------------------------ */

        /* ------------------------
         Public Methods
           * ------------------------ */

        /**
         * Is the matrix symmetric and positive definite?
         *
         * @return true if A is symmetric and positive definite.
         */

        public boolean isSPD() {
            return isspd;
        }

        /**
         * Return triangular factor.
         *
         * @return L
         */

        public Matrix getL() {
            return new Matrix(L, n, n);
        }

        /**
         * Solve A*X = B
         *
         * @param B A Matrix with as many rows as A and any number of columns.
         * @return X so that L*L'*X = B
         * @throws IllegalArgumentException Matrix row dimensions must agree.
         * @throws RuntimeException         Matrix is not symmetric positive definite.
         */

        public Matrix solve(Matrix B) {
            if (B.getRowDimension() != n) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!isspd) {
                throw new RuntimeException("Matrix is not symmetric positive definite.");
            }

            // Copy right hand side.
            double[][] X = B.getArrayCopy();
            int nx = B.getColumnDimension();

            // Solve L*Y = B;
            for (int k = 0; k < n; k++) {
                for (int i = k + 1; i < n; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * L[i][k];
                    }
                }
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= L[k][k];
                }
            }

            // Solve L'*X = Y;
            for (int k = n - 1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= L[k][k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * L[k][i];
                    }
                }
            }
            return new Matrix(X, n, nx);
        }
    }

    /**
     * Eigenvalues and eigenvectors of a real matrix.
     * <p/>
     * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is
     * diagonal and the eigenvector matrix V is orthogonal.
     * I.e. A = V.times(D.times(V.transpose())) and
     * V.times(V.transpose()) equals the identity matrix.
     * <p/>
     * If A is not symmetric, then the eigenvalue matrix D is block diagonal
     * with the real eigenvalues in 1-by-1 blocks and any complex eigenvalues,
     * lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda].  The
     * columns of V represent the eigenvectors in the sense that A*V = V*D,
     * i.e. A.times(V) equals V.times(D).  The matrix V may be badly
     * conditioned, or even singular, so the validity of the equation
     * A = V*D*inverse(V) depends upon V.cond().
     */

    public class EigenvalueDecomposition implements java.io.Serializable {

        /* ------------------------
            Class variables
          * ------------------------ */

        /**
         * Row and column dimension (square matrix).
         *
         * @serial matrix dimension.
         */
        private int n;

        /**
         * Symmetry flag.
         *
         * @serial internal symmetry flag.
         */
        private boolean issymmetric;

        /**
         * Arrays for internal storage of eigenvalues.
         *
         * @serial internal storage of eigenvalues.
         */
        private double[] d, e;

        /**
         * Array for internal storage of eigenvectors.
         *
         * @serial internal storage of eigenvectors.
         */
        private double[][] V;

        /**
         * Array for internal storage of nonsymmetric Hessenberg form.
         *
         * @serial internal storage of nonsymmetric Hessenberg form.
         */
        private double[][] H;

        /**
         * Working storage for nonsymmetric algorithm.
         *
         * @serial working storage for nonsymmetric algorithm.
         */
        private double[] ort;

        /* ------------------------
            Private Methods
          * ------------------------ */

        // Symmetric Householder reduction to tridiagonal form.

        private void tred2() {

            //  This is derived from the Algol procedures tred2 by
            //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
            //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
            //  Fortran subroutine in EISPACK.

            for (int j = 0; j < n; j++) {
                d[j] = V[n - 1][j];
            }

            // Householder reduction to tridiagonal form.

            for (int i = n - 1; i > 0; i--) {

                // Scale to avoid under/overflow.

                double scale = 0.0;
                double h = 0.0;
                for (int k = 0; k < i; k++) {
                    scale = scale + Math.abs(d[k]);
                }
                if (scale == 0.0) {
                    e[i] = d[i - 1];
                    for (int j = 0; j < i; j++) {
                        d[j] = V[i - 1][j];
                        V[i][j] = 0.0;
                        V[j][i] = 0.0;
                    }
                } else {

                    // Generate Householder vector.

                    for (int k = 0; k < i; k++) {
                        d[k] /= scale;
                        h += d[k] * d[k];
                    }
                    double f = d[i - 1];
                    double g = Math.sqrt(h);
                    if (f > 0) {
                        g = -g;
                    }
                    e[i] = scale * g;
                    h = h - f * g;
                    d[i - 1] = f - g;
                    for (int j = 0; j < i; j++) {
                        e[j] = 0.0;
                    }

                    // Apply similarity transformation to remaining columns.

                    for (int j = 0; j < i; j++) {
                        f = d[j];
                        V[j][i] = f;
                        g = e[j] + V[j][j] * f;
                        for (int k = j + 1; k <= i - 1; k++) {
                            g += V[k][j] * d[k];
                            e[k] += V[k][j] * f;
                        }
                        e[j] = g;
                    }
                    f = 0.0;
                    for (int j = 0; j < i; j++) {
                        e[j] /= h;
                        f += e[j] * d[j];
                    }
                    double hh = f / (h + h);
                    for (int j = 0; j < i; j++) {
                        e[j] -= hh * d[j];
                    }
                    for (int j = 0; j < i; j++) {
                        f = d[j];
                        g = e[j];
                        for (int k = j; k <= i - 1; k++) {
                            V[k][j] -= (f * e[k] + g * d[k]);
                        }
                        d[j] = V[i - 1][j];
                        V[i][j] = 0.0;
                    }
                }
                d[i] = h;
            }

            // Accumulate transformations.

            for (int i = 0; i < n - 1; i++) {
                V[n - 1][i] = V[i][i];
                V[i][i] = 1.0;
                double h = d[i + 1];
                if (h != 0.0) {
                    for (int k = 0; k <= i; k++) {
                        d[k] = V[k][i + 1] / h;
                    }
                    for (int j = 0; j <= i; j++) {
                        double g = 0.0;
                        for (int k = 0; k <= i; k++) {
                            g += V[k][i + 1] * V[k][j];
                        }
                        for (int k = 0; k <= i; k++) {
                            V[k][j] -= g * d[k];
                        }
                    }
                }
                for (int k = 0; k <= i; k++) {
                    V[k][i + 1] = 0.0;
                }
            }
            for (int j = 0; j < n; j++) {
                d[j] = V[n - 1][j];
                V[n - 1][j] = 0.0;
            }
            V[n - 1][n - 1] = 1.0;
            e[0] = 0.0;
        }

        // Symmetric tridiagonal QL algorithm.

        private void tql2() {

            //  This is derived from the Algol procedures tql2, by
            //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
            //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
            //  Fortran subroutine in EISPACK.

            for (int i = 1; i < n; i++) {
                e[i - 1] = e[i];
            }
            e[n - 1] = 0.0;

            double f = 0.0;
            double tst1 = 0.0;
            double eps = Math.pow(2.0, -52.0);
            for (int l = 0; l < n; l++) {

                // Find small subdiagonal element

                tst1 = Math.max(tst1, Math.abs(d[l]) + Math.abs(e[l]));
                int m = l;
                while (m < n) {
                    if (Math.abs(e[m]) <= eps * tst1) {
                        break;
                    }
                    m++;
                }

                // If m == l, d[l] is an eigenvalue,
                // otherwise, iterate.

                if (m > l) {
                    int iter = 0;
                    do {
                        iter = iter + 1;  // (Could check iteration count here.)

                        // Compute implicit shift

                        double g = d[l];
                        double p = (d[l + 1] - g) / (2.0 * e[l]);
                        double r = hypot(p, 1.0);
                        if (p < 0) {
                            r = -r;
                        }
                        d[l] = e[l] / (p + r);
                        d[l + 1] = e[l] * (p + r);
                        double dl1 = d[l + 1];
                        double h = g - d[l];
                        for (int i = l + 2; i < n; i++) {
                            d[i] -= h;
                        }
                        f = f + h;

                        // Implicit QL transformation.

                        p = d[m];
                        double c = 1.0;
                        double c2 = c;
                        double c3 = c;
                        double el1 = e[l + 1];
                        double s = 0.0;
                        double s2 = 0.0;
                        for (int i = m - 1; i >= l; i--) {
                            c3 = c2;
                            c2 = c;
                            s2 = s;
                            g = c * e[i];
                            h = c * p;
                            r = hypot(p, e[i]);
                            e[i + 1] = s * r;
                            s = e[i] / r;
                            c = p / r;
                            p = c * d[i] - s * g;
                            d[i + 1] = h + s * (c * g + s * d[i]);

                            // Accumulate transformation.

                            for (int k = 0; k < n; k++) {
                                h = V[k][i + 1];
                                V[k][i + 1] = s * V[k][i] + c * h;
                                V[k][i] = c * V[k][i] - s * h;
                            }
                        }
                        p = -s * s2 * c3 * el1 * e[l] / dl1;
                        e[l] = s * p;
                        d[l] = c * p;

                        // Check for convergence.

                    } while (Math.abs(e[l]) > eps * tst1);
                }
                d[l] = d[l] + f;
                e[l] = 0.0;
            }

            // Sort eigenvalues and corresponding vectors.

            for (int i = 0; i < n - 1; i++) {
                int k = i;
                double p = d[i];
                for (int j = i + 1; j < n; j++) {
                    if (d[j] < p) {
                        k = j;
                        p = d[j];
                    }
                }
                if (k != i) {
                    d[k] = d[i];
                    d[i] = p;
                    for (int j = 0; j < n; j++) {
                        p = V[j][i];
                        V[j][i] = V[j][k];
                        V[j][k] = p;
                    }
                }
            }
        }

        // Nonsymmetric reduction to Hessenberg form.

        private void orthes() {

            //  This is derived from the Algol procedures orthes and ortran,
            //  by Martin and Wilkinson, Handbook for Auto. Comp.,
            //  Vol.ii-Linear Algebra, and the corresponding
            //  Fortran subroutines in EISPACK.

            int low = 0;
            int high = n - 1;

            for (int m = low + 1; m <= high - 1; m++) {

                // Scale column.

                double scale = 0.0;
                for (int i = m; i <= high; i++) {
                    scale = scale + Math.abs(H[i][m - 1]);
                }
                if (scale != 0.0) {

                    // Compute Householder transformation.

                    double h = 0.0;
                    for (int i = high; i >= m; i--) {
                        ort[i] = H[i][m - 1] / scale;
                        h += ort[i] * ort[i];
                    }
                    double g = Math.sqrt(h);
                    if (ort[m] > 0) {
                        g = -g;
                    }
                    h = h - ort[m] * g;
                    ort[m] = ort[m] - g;

                    // Apply Householder similarity transformation
                    // H = (I-u*u'/h)*H*(I-u*u')/h)

                    for (int j = m; j < n; j++) {
                        double f = 0.0;
                        for (int i = high; i >= m; i--) {
                            f += ort[i] * H[i][j];
                        }
                        f = f / h;
                        for (int i = m; i <= high; i++) {
                            H[i][j] -= f * ort[i];
                        }
                    }

                    for (int i = 0; i <= high; i++) {
                        double f = 0.0;
                        for (int j = high; j >= m; j--) {
                            f += ort[j] * H[i][j];
                        }
                        f = f / h;
                        for (int j = m; j <= high; j++) {
                            H[i][j] -= f * ort[j];
                        }
                    }
                    ort[m] = scale * ort[m];
                    H[m][m - 1] = scale * g;
                }
            }

            // Accumulate transformations (Algol's ortran).

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    V[i][j] = (i == j ? 1.0 : 0.0);
                }
            }

            for (int m = high - 1; m >= low + 1; m--) {
                if (H[m][m - 1] != 0.0) {
                    for (int i = m + 1; i <= high; i++) {
                        ort[i] = H[i][m - 1];
                    }
                    for (int j = m; j <= high; j++) {
                        double g = 0.0;
                        for (int i = m; i <= high; i++) {
                            g += ort[i] * V[i][j];
                        }
                        // Double division avoids possible underflow
                        g = (g / ort[m]) / H[m][m - 1];
                        for (int i = m; i <= high; i++) {
                            V[i][j] += g * ort[i];
                        }
                    }
                }
            }
        }

        // Complex scalar division.

        private transient double cdivr, cdivi;

        private void cdiv(double xr, double xi, double yr, double yi) {
            double r, d;
            if (Math.abs(yr) > Math.abs(yi)) {
                r = yi / yr;
                d = yr + r * yi;
                cdivr = (xr + r * xi) / d;
                cdivi = (xi - r * xr) / d;
            } else {
                r = yr / yi;
                d = yi + r * yr;
                cdivr = (r * xr + xi) / d;
                cdivi = (r * xi - xr) / d;
            }
        }

        // Nonsymmetric reduction from Hessenberg to real Schur form.

        private void hqr2() {

            //  This is derived from the Algol procedure hqr2,
            //  by Martin and Wilkinson, Handbook for Auto. Comp.,
            //  Vol.ii-Linear Algebra, and the corresponding
            //  Fortran subroutine in EISPACK.

            // Initialize

            int nn = this.n;
            int n = nn - 1;
            int low = 0;
            int high = nn - 1;
            double eps = Math.pow(2.0, -52.0);
            double exshift = 0.0;
            double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

            // Store roots isolated by balanc and compute matrix norm

            double norm = 0.0;
            for (int i = 0; i < nn; i++) {
                if (i < low | i > high) {
                    d[i] = H[i][i];
                    e[i] = 0.0;
                }
                for (int j = Math.max(i - 1, 0); j < nn; j++) {
                    norm = norm + Math.abs(H[i][j]);
                }
            }

            // Outer loop over eigenvalue index

            int iter = 0;
            while (n >= low) {

                // Look for single small sub-diagonal element

                int l = n;
                while (l > low) {
                    s = Math.abs(H[l - 1][l - 1]) + Math.abs(H[l][l]);
                    if (s == 0.0) {
                        s = norm;
                    }
                    if (Math.abs(H[l][l - 1]) < eps * s) {
                        break;
                    }
                    l--;
                }

                // Check for convergence
                // One root found

                if (l == n) {
                    H[n][n] = H[n][n] + exshift;
                    d[n] = H[n][n];
                    e[n] = 0.0;
                    n--;
                    iter = 0;

                    // Two roots found

                } else if (l == n - 1) {
                    w = H[n][n - 1] * H[n - 1][n];
                    p = (H[n - 1][n - 1] - H[n][n]) / 2.0;
                    q = p * p + w;
                    z = Math.sqrt(Math.abs(q));
                    H[n][n] = H[n][n] + exshift;
                    H[n - 1][n - 1] = H[n - 1][n - 1] + exshift;
                    x = H[n][n];

                    // Real pair

                    if (q >= 0) {
                        if (p >= 0) {
                            z = p + z;
                        } else {
                            z = p - z;
                        }
                        d[n - 1] = x + z;
                        d[n] = d[n - 1];
                        if (z != 0.0) {
                            d[n] = x - w / z;
                        }
                        e[n - 1] = 0.0;
                        e[n] = 0.0;
                        x = H[n][n - 1];
                        s = Math.abs(x) + Math.abs(z);
                        p = x / s;
                        q = z / s;
                        r = Math.sqrt(p * p + q * q);
                        p = p / r;
                        q = q / r;

                        // Row modification

                        for (int j = n - 1; j < nn; j++) {
                            z = H[n - 1][j];
                            H[n - 1][j] = q * z + p * H[n][j];
                            H[n][j] = q * H[n][j] - p * z;
                        }

                        // Column modification

                        for (int i = 0; i <= n; i++) {
                            z = H[i][n - 1];
                            H[i][n - 1] = q * z + p * H[i][n];
                            H[i][n] = q * H[i][n] - p * z;
                        }

                        // Accumulate transformations

                        for (int i = low; i <= high; i++) {
                            z = V[i][n - 1];
                            V[i][n - 1] = q * z + p * V[i][n];
                            V[i][n] = q * V[i][n] - p * z;
                        }

                        // Complex pair

                    } else {
                        d[n - 1] = x + p;
                        d[n] = x + p;
                        e[n - 1] = z;
                        e[n] = -z;
                    }
                    n = n - 2;
                    iter = 0;

                    // No convergence yet

                } else {

                    // Form shift

                    x = H[n][n];
                    y = 0.0;
                    w = 0.0;
                    if (l < n) {
                        y = H[n - 1][n - 1];
                        w = H[n][n - 1] * H[n - 1][n];
                    }

                    // Wilkinson's original ad hoc shift

                    if (iter == 10) {
                        exshift += x;
                        for (int i = low; i <= n; i++) {
                            H[i][i] -= x;
                        }
                        s = Math.abs(H[n][n - 1]) + Math.abs(H[n - 1][n - 2]);
                        x = y = 0.75 * s;
                        w = -0.4375 * s * s;
                    }

                    // MATLAB's new ad hoc shift

                    if (iter == 30) {
                        s = (y - x) / 2.0;
                        s = s * s + w;
                        if (s > 0) {
                            s = Math.sqrt(s);
                            if (y < x) {
                                s = -s;
                            }
                            s = x - w / ((y - x) / 2.0 + s);
                            for (int i = low; i <= n; i++) {
                                H[i][i] -= s;
                            }
                            exshift += s;
                            x = y = w = 0.964;
                        }
                    }

                    iter = iter + 1;   // (Could check iteration count here.)

                    // Look for two consecutive small sub-diagonal elements

                    int m = n - 2;
                    while (m >= l) {
                        z = H[m][m];
                        r = x - z;
                        s = y - z;
                        p = (r * s - w) / H[m + 1][m] + H[m][m + 1];
                        q = H[m + 1][m + 1] - z - r - s;
                        r = H[m + 2][m + 1];
                        s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        p = p / s;
                        q = q / s;
                        r = r / s;
                        if (m == l) {
                            break;
                        }
                        if (Math.abs(H[m][m - 1]) * (Math.abs(q) + Math.abs(r)) < eps * (Math.abs(p) * (Math.abs(H[m - 1][m - 1]) + Math.abs(z) + Math.abs(H[m + 1][m + 1])))) {
                            break;
                        }
                        m--;
                    }

                    for (int i = m + 2; i <= n; i++) {
                        H[i][i - 2] = 0.0;
                        if (i > m + 2) {
                            H[i][i - 3] = 0.0;
                        }
                    }

                    // Double QR step involving rows l:n and columns m:n

                    for (int k = m; k <= n - 1; k++) {
                        boolean notlast = (k != n - 1);
                        if (k != m) {
                            p = H[k][k - 1];
                            q = H[k + 1][k - 1];
                            r = (notlast ? H[k + 2][k - 1] : 0.0);
                            x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                            if (x != 0.0) {
                                p = p / x;
                                q = q / x;
                                r = r / x;
                            }
                        }
                        if (x == 0.0) {
                            break;
                        }
                        s = Math.sqrt(p * p + q * q + r * r);
                        if (p < 0) {
                            s = -s;
                        }
                        if (s != 0) {
                            if (k != m) {
                                H[k][k - 1] = -s * x;
                            } else if (l != m) {
                                H[k][k - 1] = -H[k][k - 1];
                            }
                            p = p + s;
                            x = p / s;
                            y = q / s;
                            z = r / s;
                            q = q / p;
                            r = r / p;

                            // Row modification

                            for (int j = k; j < nn; j++) {
                                p = H[k][j] + q * H[k + 1][j];
                                if (notlast) {
                                    p = p + r * H[k + 2][j];
                                    H[k + 2][j] = H[k + 2][j] - p * z;
                                }
                                H[k][j] = H[k][j] - p * x;
                                H[k + 1][j] = H[k + 1][j] - p * y;
                            }

                            // Column modification

                            for (int i = 0; i <= Math.min(n, k + 3); i++) {
                                p = x * H[i][k] + y * H[i][k + 1];
                                if (notlast) {
                                    p = p + z * H[i][k + 2];
                                    H[i][k + 2] = H[i][k + 2] - p * r;
                                }
                                H[i][k] = H[i][k] - p;
                                H[i][k + 1] = H[i][k + 1] - p * q;
                            }

                            // Accumulate transformations

                            for (int i = low; i <= high; i++) {
                                p = x * V[i][k] + y * V[i][k + 1];
                                if (notlast) {
                                    p = p + z * V[i][k + 2];
                                    V[i][k + 2] = V[i][k + 2] - p * r;
                                }
                                V[i][k] = V[i][k] - p;
                                V[i][k + 1] = V[i][k + 1] - p * q;
                            }
                        }  // (s != 0)
                    }  // k loop
                }  // check convergence
            }  // while (n >= low)

            // Backsubstitute to find vectors of upper triangular form

            if (norm == 0.0) {
                return;
            }

            for (n = nn - 1; n >= 0; n--) {
                p = d[n];
                q = e[n];

                // Real vector

                if (q == 0) {
                    int l = n;
                    H[n][n] = 1.0;
                    for (int i = n - 1; i >= 0; i--) {
                        w = H[i][i] - p;
                        r = 0.0;
                        for (int j = l; j <= n; j++) {
                            r = r + H[i][j] * H[j][n];
                        }
                        if (e[i] < 0.0) {
                            z = w;
                            s = r;
                        } else {
                            l = i;
                            if (e[i] == 0.0) {
                                if (w != 0.0) {
                                    H[i][n] = -r / w;
                                } else {
                                    H[i][n] = -r / (eps * norm);
                                }

                                // Solve real equations

                            } else {
                                x = H[i][i + 1];
                                y = H[i + 1][i];
                                q = (d[i] - p) * (d[i] - p) + e[i] * e[i];
                                t = (x * s - z * r) / q;
                                H[i][n] = t;
                                if (Math.abs(x) > Math.abs(z)) {
                                    H[i + 1][n] = (-r - w * t) / x;
                                } else {
                                    H[i + 1][n] = (-s - y * t) / z;
                                }
                            }

                            // Overflow control

                            t = Math.abs(H[i][n]);
                            if ((eps * t) * t > 1) {
                                for (int j = i; j <= n; j++) {
                                    H[j][n] = H[j][n] / t;
                                }
                            }
                        }
                    }

                    // Complex vector

                } else if (q < 0) {
                    int l = n - 1;

                    // Last vector component imaginary so matrix is triangular

                    if (Math.abs(H[n][n - 1]) > Math.abs(H[n - 1][n])) {
                        H[n - 1][n - 1] = q / H[n][n - 1];
                        H[n - 1][n] = -(H[n][n] - p) / H[n][n - 1];
                    } else {
                        cdiv(0.0, -H[n - 1][n], H[n - 1][n - 1] - p, q);
                        H[n - 1][n - 1] = cdivr;
                        H[n - 1][n] = cdivi;
                    }
                    H[n][n - 1] = 0.0;
                    H[n][n] = 1.0;
                    for (int i = n - 2; i >= 0; i--) {
                        double ra, sa, vr, vi;
                        ra = 0.0;
                        sa = 0.0;
                        for (int j = l; j <= n; j++) {
                            ra = ra + H[i][j] * H[j][n - 1];
                            sa = sa + H[i][j] * H[j][n];
                        }
                        w = H[i][i] - p;

                        if (e[i] < 0.0) {
                            z = w;
                            r = ra;
                            s = sa;
                        } else {
                            l = i;
                            if (e[i] == 0) {
                                cdiv(-ra, -sa, w, q);
                                H[i][n - 1] = cdivr;
                                H[i][n] = cdivi;
                            } else {

                                // Solve complex equations

                                x = H[i][i + 1];
                                y = H[i + 1][i];
                                vr = (d[i] - p) * (d[i] - p) + e[i] * e[i] - q * q;
                                vi = (d[i] - p) * 2.0 * q;
                                if (vr == 0.0 & vi == 0.0) {
                                    vr = eps * norm * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(z));
                                }
                                cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi);
                                H[i][n - 1] = cdivr;
                                H[i][n] = cdivi;
                                if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                    H[i + 1][n - 1] = (-ra - w * H[i][n - 1] + q * H[i][n]) / x;
                                    H[i + 1][n] = (-sa - w * H[i][n] - q * H[i][n - 1]) / x;
                                } else {
                                    cdiv(-r - y * H[i][n - 1], -s - y * H[i][n], z, q);
                                    H[i + 1][n - 1] = cdivr;
                                    H[i + 1][n] = cdivi;
                                }
                            }

                            // Overflow control

                            t = Math.max(Math.abs(H[i][n - 1]), Math.abs(H[i][n]));
                            if ((eps * t) * t > 1) {
                                for (int j = i; j <= n; j++) {
                                    H[j][n - 1] = H[j][n - 1] / t;
                                    H[j][n] = H[j][n] / t;
                                }
                            }
                        }
                    }
                }
            }

            // Vectors of isolated roots

            for (int i = 0; i < nn; i++) {
                if (i < low | i > high) {
                    for (int j = i; j < nn; j++) {
                        V[i][j] = H[i][j];
                    }
                }
            }

            // Back transformation to get eigenvectors of original matrix

            for (int j = nn - 1; j >= low; j--) {
                for (int i = low; i <= high; i++) {
                    z = 0.0;
                    for (int k = low; k <= Math.min(j, high); k++) {
                        z = z + V[i][k] * H[k][j];
                    }
                    V[i][j] = z;
                }
            }
        }

        /* ------------------------
            Constructor
          * ------------------------ */

        /**
         * Check for symmetry, then construct the eigenvalue decomposition
         *
         * @return Structure to access D and V.
         */

        public EigenvalueDecomposition(Matrix Arg) {
            double[][] A = Arg.getArray();
            n = Arg.getColumnDimension();
            V = new double[n][n];
            d = new double[n];
            e = new double[n];

            issymmetric = true;
            for (int j = 0; (j < n) & issymmetric; j++) {
                for (int i = 0; (i < n) & issymmetric; i++) {
                    issymmetric = (A[i][j] == A[j][i]);
                }
            }

            if (issymmetric) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        V[i][j] = A[i][j];
                    }
                }

                // Tridiagonalize.
                tred2();

                // Diagonalize.
                tql2();

            } else {
                H = new double[n][n];
                ort = new double[n];

                for (int j = 0; j < n; j++) {
                    for (int i = 0; i < n; i++) {
                        H[i][j] = A[i][j];
                    }
                }

                // Reduce to Hessenberg form.
                orthes();

                // Reduce Hessenberg to real Schur form.
                hqr2();
            }
        }

        /* ------------------------
            Public Methods
          * ------------------------ */

        /**
         * Return the eigenvector matrix
         *
         * @return V
         */

        public Matrix getV() {
            return new Matrix(V, n, n);
        }

        /**
         * Return the real parts of the eigenvalues
         *
         * @return real(diag(D))
         */

        public double[] getRealEigenvalues() {
            return d;
        }

        /**
         * Return the imaginary parts of the eigenvalues
         *
         * @return imag(diag(D))
         */

        public double[] getImagEigenvalues() {
            return e;
        }

        /**
         * Return the block diagonal eigenvalue matrix
         *
         * @return D
         */

        public Matrix getD() {
            Matrix X = new Matrix(n, n);
            double[][] D = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    D[i][j] = 0.0;
                }
                D[i][i] = d[i];
                if (e[i] > 0) {
                    D[i][i + 1] = e[i];
                } else if (e[i] < 0) {
                    D[i][i - 1] = e[i];
                }
            }
            return X;
        }
    }

    /**
     * LU Decomposition.
     * <p/>
     * For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n
     * unit lower triangular matrix L, an n-by-n upper triangular matrix U,
     * and a permutation vector piv of length m so that A(piv,:) = L*U.
     * If m < n, then L is m-by-m and U is m-by-n.
     * <p/>
     * The LU decompostion with pivoting always exists, even if the matrix is
     * singular, so the constructor will never fail.  The primary use of the
     * LU decomposition is in the solution of square systems of simultaneous
     * linear equations.  This will fail if isNonsingular() returns false.
     */

    public class LUDecomposition implements java.io.Serializable {

        /* ------------------------
         Class variables
           * ------------------------ */

        /**
         * Array for internal storage of decomposition.
         *
         * @serial internal array storage.
         */
        private double[][] LU;

        /**
         * Row and column dimensions, and pivot sign.
         *
         * @serial column dimension.
         * @serial row dimension.
         * @serial pivot sign.
         */
        private int m, n, pivsign;

        /**
         * Internal storage of pivot vector.
         *
         * @serial pivot vector.
         */
        private int[] piv;

        /* ------------------------
         Constructor
           * ------------------------ */

        /**
         * LU Decomposition
         *
         * @param A Rectangular matrix
         * @return Structure to access L, U and piv.
         */

        public LUDecomposition(Matrix A) {

            // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

            LU = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            piv = new int[m];
            for (int i = 0; i < m; i++) {
                piv[i] = i;
            }
            pivsign = 1;
            double[] LUrowi;
            double[] LUcolj = new double[m];

            // Outer loop.

            for (int j = 0; j < n; j++) {

                // Make a copy of the j-th column to localize references.

                for (int i = 0; i < m; i++) {
                    LUcolj[i] = LU[i][j];
                }

                // Apply previous transformations.

                for (int i = 0; i < m; i++) {
                    LUrowi = LU[i];

                    // Most of the time is spent in the following dot product.

                    int kmax = Math.min(i, j);
                    double s = 0.0;
                    for (int k = 0; k < kmax; k++) {
                        s += LUrowi[k] * LUcolj[k];
                    }

                    LUrowi[j] = LUcolj[i] -= s;
                }

                // Find pivot and exchange if necessary.

                int p = j;
                for (int i = j + 1; i < m; i++) {
                    if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                        p = i;
                    }
                }
                if (p != j) {
                    for (int k = 0; k < n; k++) {
                        double t = LU[p][k];
                        LU[p][k] = LU[j][k];
                        LU[j][k] = t;
                    }
                    int k = piv[p];
                    piv[p] = piv[j];
                    piv[j] = k;
                    pivsign = -pivsign;
                }

                // Compute multipliers.

                if (j < m & LU[j][j] != 0.0) {
                    for (int i = j + 1; i < m; i++) {
                        LU[i][j] /= LU[j][j];
                    }
                }
            }
        }

        /* ------------------------
         Temporary, experimental code.
         ------------------------ *\

         \** LU Decomposition, computed by Gaussian elimination.
         <P>
         This constructor computes L and U with the "daxpy"-based elimination
         algorithm used in LINPACK and MATLAB.  In Java, we suspect the dot-product,
         Crout algorithm will be faster.  We have temporarily included this
         constructor until timing experiments confirm this suspicion.
         <P>
         @param  A             Rectangular matrix
         @param  linpackflag   Use Gaussian elimination.  Actual value ignored.
         @return               Structure to access L, U and piv.
         *\

         public LUDecomposition (Matrix A, int linpackflag) {
            // Initialize.
            LU = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            piv = new int[m];
            for (int i = 0; i < m; i++) {
               piv[i] = i;
            }
            pivsign = 1;
            // Main loop.
            for (int k = 0; k < n; k++) {
               // Find pivot.
               int p = k;
               for (int i = k+1; i < m; i++) {
                  if (Math.abs(LU[i][k]) > Math.abs(LU[p][k])) {
                     p = i;
                  }
               }
               // Exchange if necessary.
               if (p != k) {
                  for (int j = 0; j < n; j++) {
                     double t = LU[p][j]; LU[p][j] = LU[k][j]; LU[k][j] = t;
                  }
                  int t = piv[p]; piv[p] = piv[k]; piv[k] = t;
                  pivsign = -pivsign;
               }
               // Compute multipliers and eliminate k-th column.
               if (LU[k][k] != 0.0) {
                  for (int i = k+1; i < m; i++) {
                     LU[i][k] /= LU[k][k];
                     for (int j = k+1; j < n; j++) {
                        LU[i][j] -= LU[i][k]*LU[k][j];
                     }
                  }
               }
            }
         }

          \* ------------------------
         End of temporary code.
           * ------------------------ */

        /* ------------------------
         Public Methods
           * ------------------------ */

        /**
         * Is the matrix nonsingular?
         *
         * @return true if U, and hence A, is nonsingular.
         */

        public boolean isNonsingular() {
            for (int j = 0; j < n; j++) {
                if (LU[j][j] == 0) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Return lower triangular factor
         *
         * @return L
         */

        public Matrix getL() {
            Matrix X = new Matrix(m, n);
            double[][] L = X.getArray();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (i > j) {
                        L[i][j] = LU[i][j];
                    } else if (i == j) {
                        L[i][j] = 1.0;
                    } else {
                        L[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        /**
         * Return upper triangular factor
         *
         * @return U
         */

        public Matrix getU() {
            Matrix X = new Matrix(n, n);
            double[][] U = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i <= j) {
                        U[i][j] = LU[i][j];
                    } else {
                        U[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        /**
         * Return pivot permutation vector
         *
         * @return piv
         */

        public int[] getPivot() {
            int[] p = new int[m];
            for (int i = 0; i < m; i++) {
                p[i] = piv[i];
            }
            return p;
        }

        /**
         * Return pivot permutation vector as a one-dimensional double array
         *
         * @return (double) piv
         */

        public double[] getDoublePivot() {
            double[] vals = new double[m];
            for (int i = 0; i < m; i++) {
                vals[i] = (double) piv[i];
            }
            return vals;
        }

        /**
         * Determinant
         *
         * @return det(A)
         * @throws IllegalArgumentException Matrix must be square
         */

        public double det() {
            if (m != n) {
                throw new IllegalArgumentException("Matrix must be square.");
            }
            double d = (double) pivsign;
            for (int j = 0; j < n; j++) {
                d *= LU[j][j];
            }
            return d;
        }

        /**
         * Solve A*X = B
         *
         * @param B A Matrix with as many rows as A and any number of columns.
         * @return X so that L*U*X = B(piv,:)
         * @throws IllegalArgumentException Matrix row dimensions must agree.
         * @throws RuntimeException         Matrix is singular.
         */

        public Matrix solve(Matrix B) {
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!this.isNonsingular()) {
                throw new RuntimeException("Matrix is singular.");
            }

            // Copy right hand side with pivoting
            int nx = B.getColumnDimension();
            Matrix Xmat = B.getMatrix(piv, 0, nx - 1);
            double[][] X = Xmat.getArray();

            // Solve L*Y = B(piv,:)
            for (int k = 0; k < n; k++) {
                for (int i = k + 1; i < n; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * LU[i][k];
                    }
                }
            }
            // Solve U*X = Y;
            for (int k = n - 1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= LU[k][k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * LU[i][k];
                    }
                }
            }
            return Xmat;
        }
    }

    /**
     * QR Decomposition.
     * <p/>
     * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
     * orthogonal matrix Q and an n-by-n upper triangular matrix R so that
     * A = Q*R.
     * <p/>
     * The QR decompostion always exists, even if the matrix does not have
     * full rank, so the constructor will never fail.  The primary use of the
     * QR decomposition is in the least squares solution of nonsquare systems
     * of simultaneous linear equations.  This will fail if isFullRank()
     * returns false.
     */

    public class QRDecomposition implements java.io.Serializable {

        /* ------------------------
            Class variables
          * ------------------------ */

        /**
         * Array for internal storage of decomposition.
         *
         * @serial internal array storage.
         */
        private double[][] QR;

        /**
         * Row and column dimensions.
         *
         * @serial column dimension.
         * @serial row dimension.
         */
        private int m, n;

        /**
         * Array for internal storage of diagonal of R.
         *
         * @serial diagonal of R.
         */
        private double[] Rdiag;

        /* ------------------------
            Constructor
          * ------------------------ */

        /**
         * QR Decomposition, computed by Householder reflections.
         *
         * @param A Rectangular matrix
         * @return Structure to access R and the Householder vectors and compute Q.
         */

        public QRDecomposition(Matrix A) {
            // Initialize.
            QR = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            Rdiag = new double[n];

            // Main loop.
            for (int k = 0; k < n; k++) {
                // Compute 2-norm of k-th column without under/overflow.
                double nrm = 0;
                for (int i = k; i < m; i++) {
                    nrm = hypot(nrm, QR[i][k]);
                }

                if (nrm != 0.0) {
                    // Form k-th Householder vector.
                    if (QR[k][k] < 0) {
                        nrm = -nrm;
                    }
                    for (int i = k; i < m; i++) {
                        QR[i][k] /= nrm;
                    }
                    QR[k][k] += 1.0;

                    // Apply transformation to remaining columns.
                    for (int j = k + 1; j < n; j++) {
                        double s = 0.0;
                        for (int i = k; i < m; i++) {
                            s += QR[i][k] * QR[i][j];
                        }
                        s = -s / QR[k][k];
                        for (int i = k; i < m; i++) {
                            QR[i][j] += s * QR[i][k];
                        }
                    }
                }
                Rdiag[k] = -nrm;
            }
        }

        /* ------------------------
            Public Methods
          * ------------------------ */

        /**
         * Is the matrix full rank?
         *
         * @return true if R, and hence A, has full rank.
         */

        public boolean isFullRank() {
            for (int j = 0; j < n; j++) {
                if (Rdiag[j] == 0) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Return the Householder vectors
         *
         * @return Lower trapezoidal matrix whose columns define the reflections
         */

        public Matrix getH() {
            Matrix X = new Matrix(m, n);
            double[][] H = X.getArray();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (i >= j) {
                        H[i][j] = QR[i][j];
                    } else {
                        H[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        /**
         * Return the upper triangular factor
         *
         * @return R
         */

        public Matrix getR() {
            Matrix X = new Matrix(n, n);
            double[][] R = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i < j) {
                        R[i][j] = QR[i][j];
                    } else if (i == j) {
                        R[i][j] = Rdiag[i];
                    } else {
                        R[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        /**
         * Generate and return the (economy-sized) orthogonal factor
         *
         * @return Q
         */

        public Matrix getQ() {
            Matrix X = new Matrix(m, n);
            double[][] Q = X.getArray();
            for (int k = n - 1; k >= 0; k--) {
                for (int i = 0; i < m; i++) {
                    Q[i][k] = 0.0;
                }
                Q[k][k] = 1.0;
                for (int j = k; j < n; j++) {
                    if (QR[k][k] != 0) {
                        double s = 0.0;
                        for (int i = k; i < m; i++) {
                            s += QR[i][k] * Q[i][j];
                        }
                        s = -s / QR[k][k];
                        for (int i = k; i < m; i++) {
                            Q[i][j] += s * QR[i][k];
                        }
                    }
                }
            }
            return X;
        }

        /**
         * Least squares solution of A*X = B
         *
         * @param B A Matrix with as many rows as A and any number of columns.
         * @return X that minimizes the two norm of Q*R*X-B.
         * @throws IllegalArgumentException Matrix row dimensions must agree.
         * @throws RuntimeException         Matrix is rank deficient.
         */

        public Matrix solve(Matrix B) {
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!this.isFullRank()) {
                throw new RuntimeException("Matrix is rank deficient.");
            }

            // Copy right hand side
            int nx = B.getColumnDimension();
            double[][] X = B.getArrayCopy();

            // Compute Y = transpose(Q)*B
            for (int k = 0; k < n; k++) {
                for (int j = 0; j < nx; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k] * X[i][j];
                    }
                    s = -s / QR[k][k];
                    for (int i = k; i < m; i++) {
                        X[i][j] += s * QR[i][k];
                    }
                }
            }
            // Solve R*X = Y;
            for (int k = n - 1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= Rdiag[k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * QR[i][k];
                    }
                }
            }
            return (new Matrix(X, n, nx).getMatrix(0, n - 1, 0, nx - 1));
    	}
    }

    /**
     * Singular Value Decomposition.
     * <p/>
     * For an m-by-n matrix A with m >= n, the singular value decomposition is
     * an m-by-n orthogonal matrix U, an n-by-n diagonal matrix S, and
     * an n-by-n orthogonal matrix V so that A = U*S*V'.
     * <p/>
     * The singular values, sigma[k] = S[k][k], are ordered so that
     * sigma[0] >= sigma[1] >= ... >= sigma[n-1].
     * <p/>
     * The singular value decompostion always exists, so the constructor will
     * never fail.  The matrix condition number and the effective numerical
     * rank can be computed from this decomposition.
     */

    public class SingularValueDecomposition implements java.io.Serializable {

        /* ------------------------
         Class variables
           * ------------------------ */

        /**
         * Arrays for internal storage of U and V.
         *
         * @serial internal storage of U.
         * @serial internal storage of V.
         */
        private double[][] U, V;

        /**
         * Array for internal storage of singular values.
         *
         * @serial internal storage of singular values.
         */
        private double[] s;

        /**
         * Row and column dimensions.
         *
         * @serial row dimension.
         * @serial column dimension.
         */
        private int m, n;

        /* ------------------------
         Constructor
           * ------------------------ */

        /**
         * Construct the singular value decomposition
         *
         * @param Arg Rectangular matrix
         * @return Structure to access U, S and V.
         */

        public SingularValueDecomposition(Matrix Arg) {

            // Derived from LINPACK code.
            // Initialize.
            double[][] A = Arg.getArrayCopy();
            m = Arg.getRowDimension();
            n = Arg.getColumnDimension();
            int nu = Math.min(m, n);
            s = new double[Math.min(m + 1, n)];
            U = new double[m][nu];
            V = new double[n][n];
            double[] e = new double[n];
            double[] work = new double[m];
            boolean wantu = true;
            boolean wantv = true;

            // Reduce A to bidiagonal form, storing the diagonal elements
            // in s and the super-diagonal elements in e.

            int nct = Math.min(m - 1, n);
            int nrt = Math.max(0, Math.min(n - 2, m));
            for (int k = 0; k < Math.max(nct, nrt); k++) {
                if (k < nct) {

                    // Compute the transformation for the k-th column and
                    // place the k-th diagonal in s[k].
                    // Compute 2-norm of k-th column without under/overflow.
                    s[k] = 0;
                    for (int i = k; i < m; i++) {
                        s[k] = hypot(s[k], A[i][k]);
                    }
                    if (s[k] != 0.0) {
                        if (A[k][k] < 0.0) {
                            s[k] = -s[k];
                        }
                        for (int i = k; i < m; i++) {
                            A[i][k] /= s[k];
                        }
                        A[k][k] += 1.0;
                    }
                    s[k] = -s[k];
                }
                for (int j = k + 1; j < n; j++) {
                    if ((k < nct) & (s[k] != 0.0)) {

                        // Apply the transformation.

                        double t = 0;
                        for (int i = k; i < m; i++) {
                            t += A[i][k] * A[i][j];
                        }
                        t = -t / A[k][k];
                        for (int i = k; i < m; i++) {
                            A[i][j] += t * A[i][k];
                        }
                    }

                    // Place the k-th row of A into e for the
                    // subsequent calculation of the row transformation.

                    e[j] = A[k][j];
                }
                if (wantu & (k < nct)) {

                    // Place the transformation in U for subsequent back
                    // multiplication.

                    for (int i = k; i < m; i++) {
                        U[i][k] = A[i][k];
                    }
                }
                if (k < nrt) {

                    // Compute the k-th row transformation and place the
                    // k-th super-diagonal in e[k].
                    // Compute 2-norm without under/overflow.
                    e[k] = 0;
                    for (int i = k + 1; i < n; i++) {
                        e[k] = hypot(e[k], e[i]);
                    }
                    if (e[k] != 0.0) {
                        if (e[k + 1] < 0.0) {
                            e[k] = -e[k];
                        }
                        for (int i = k + 1; i < n; i++) {
                            e[i] /= e[k];
                        }
                        e[k + 1] += 1.0;
                    }
                    e[k] = -e[k];
                    if ((k + 1 < m) & (e[k] != 0.0)) {

                        // Apply the transformation.

                        for (int i = k + 1; i < m; i++) {
                            work[i] = 0.0;
                        }
                        for (int j = k + 1; j < n; j++) {
                            for (int i = k + 1; i < m; i++) {
                                work[i] += e[j] * A[i][j];
                            }
                        }
                        for (int j = k + 1; j < n; j++) {
                            double t = -e[j] / e[k + 1];
                            for (int i = k + 1; i < m; i++) {
                                A[i][j] += t * work[i];
                            }
                        }
                    }
                    if (wantv) {

                        // Place the transformation in V for subsequent
                        // back multiplication.

                        for (int i = k + 1; i < n; i++) {
                            V[i][k] = e[i];
                        }
                    }
                }
            }

            // Set up the final bidiagonal matrix or order p.

            int p = Math.min(n, m + 1);
            if (nct < n) {
                s[nct] = A[nct][nct];
            }
            if (m < p) {
                s[p - 1] = 0.0;
            }
            if (nrt + 1 < p) {
                e[nrt] = A[nrt][p - 1];
            }
            e[p - 1] = 0.0;

            // If required, generate U.

            if (wantu) {
                for (int j = nct; j < nu; j++) {
                    for (int i = 0; i < m; i++) {
                        U[i][j] = 0.0;
                    }
                    U[j][j] = 1.0;
                }
                for (int k = nct - 1; k >= 0; k--) {
                    if (s[k] != 0.0) {
                        for (int j = k + 1; j < nu; j++) {
                            double t = 0;
                            for (int i = k; i < m; i++) {
                                t += U[i][k] * U[i][j];
                            }
                            t = -t / U[k][k];
                            for (int i = k; i < m; i++) {
                                U[i][j] += t * U[i][k];
                            }
                        }
                        for (int i = k; i < m; i++) {
                            U[i][k] = -U[i][k];
                        }
                        U[k][k] = 1.0 + U[k][k];
                        for (int i = 0; i < k - 1; i++) {
                            U[i][k] = 0.0;
                        }
                    } else {
                        for (int i = 0; i < m; i++) {
                            U[i][k] = 0.0;
                        }
                        U[k][k] = 1.0;
                    }
                }
            }

            // If required, generate V.

            if (wantv) {
                for (int k = n - 1; k >= 0; k--) {
                    if ((k < nrt) & (e[k] != 0.0)) {
                        for (int j = k + 1; j < nu; j++) {
                            double t = 0;
                            for (int i = k + 1; i < n; i++) {
                                t += V[i][k] * V[i][j];
                            }
                            t = -t / V[k + 1][k];
                            for (int i = k + 1; i < n; i++) {
                                V[i][j] += t * V[i][k];
                            }
                        }
                    }
                    for (int i = 0; i < n; i++) {
                        V[i][k] = 0.0;
                    }
                    V[k][k] = 1.0;
                }
            }

            // Main iteration loop for the singular values.

            int pp = p - 1;
            int iter = 0;
            double eps = Math.pow(2.0, -52.0);
            while (p > 0) {
                int k, kase;

                // Here is where a test for too many iterations would go.

                // This section of the program inspects for
                // negligible elements in the s and e arrays.  On
                // completion the variables kase and k are set as follows.

                // kase = 1     if s(p) and e[k-1] are negligible and k<p
                // kase = 2     if s(k) is negligible and k<p
                // kase = 3     if e[k-1] is negligible, k<p, and
                //              s(k), ..., s(p) are not negligible (qr step).
                // kase = 4     if e(p-1) is negligible (convergence).

                for (k = p - 2; k >= -1; k--) {
                    if (k == -1) {
                        break;
                    }
                    if (Math.abs(e[k]) <= eps * (Math.abs(s[k]) + Math.abs(s[k + 1]))) {
                        e[k] = 0.0;
                        break;
                    }
                }
                if (k == p - 2) {
                    kase = 4;
                } else {
                    int ks;
                    for (ks = p - 1; ks >= k; ks--) {
                        if (ks == k) {
                            break;
                        }
                        double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
                        if (Math.abs(s[ks]) <= eps * t) {
                            s[ks] = 0.0;
                            break;
                        }
                    }
                    if (ks == k) {
                        kase = 3;
                    } else if (ks == p - 1) {
                        kase = 1;
                    } else {
                        kase = 2;
                        k = ks;
                    }
                }
                k++;

                // Perform the task indicated by kase.

                switch (kase) {

                    // Deflate negligible s(p).

                    case 1: {
                        double f = e[p - 2];
                        e[p - 2] = 0.0;
                        for (int j = p - 2; j >= k; j--) {
                            double t = hypot(s[j], f);
                            double cs = s[j] / t;
                            double sn = f / t;
                            s[j] = t;
                            if (j != k) {
                                f = -sn * e[j - 1];
                                e[j - 1] = cs * e[j - 1];
                            }
                            if (wantv) {
                                for (int i = 0; i < n; i++) {
                                    t = cs * V[i][j] + sn * V[i][p - 1];
                                    V[i][p - 1] = -sn * V[i][j] + cs * V[i][p - 1];
                                    V[i][j] = t;
                                }
                            }
                        }
                    }
                    break;

                    // Split at negligible s(k).

                    case 2: {
                        double f = e[k - 1];
                        e[k - 1] = 0.0;
                        for (int j = k; j < p; j++) {
                            double t = hypot(s[j], f);
                            double cs = s[j] / t;
                            double sn = f / t;
                            s[j] = t;
                            f = -sn * e[j];
                            e[j] = cs * e[j];
                            if (wantu) {
                                for (int i = 0; i < m; i++) {
                                    t = cs * U[i][j] + sn * U[i][k - 1];
                                    U[i][k - 1] = -sn * U[i][j] + cs * U[i][k - 1];
                                    U[i][j] = t;
                                }
                            }
                        }
                    }
                    break;

                    // Perform one qr step.

                    case 3: {

                        // Calculate the shift.

                        double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(s[p - 1]), Math.abs(s[p - 2])), Math.abs(e[p - 2])), Math.abs(s[k])), Math.abs(e[k]));
                        double sp = s[p - 1] / scale;
                        double spm1 = s[p - 2] / scale;
                        double epm1 = e[p - 2] / scale;
                        double sk = s[k] / scale;
                        double ek = e[k] / scale;
                        double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                        double c = (sp * epm1) * (sp * epm1);
                        double shift = 0.0;
                        if ((b != 0.0) | (c != 0.0)) {
                            shift = Math.sqrt(b * b + c);
                            if (b < 0.0) {
                                shift = -shift;
                            }
                            shift = c / (b + shift);
                        }
                        double f = (sk + sp) * (sk - sp) + shift;
                        double g = sk * ek;

                        // Chase zeros.

                        for (int j = k; j < p - 1; j++) {
                            double t = hypot(f, g);
                            double cs = f / t;
                            double sn = g / t;
                            if (j != k) {
                                e[j - 1] = t;
                            }
                            f = cs * s[j] + sn * e[j];
                            e[j] = cs * e[j] - sn * s[j];
                            g = sn * s[j + 1];
                            s[j + 1] = cs * s[j + 1];
                            if (wantv) {
                                for (int i = 0; i < n; i++) {
                                    t = cs * V[i][j] + sn * V[i][j + 1];
                                    V[i][j + 1] = -sn * V[i][j] + cs * V[i][j + 1];
                                    V[i][j] = t;
                                }
                            }
                            t = hypot(f, g);
                            cs = f / t;
                            sn = g / t;
                            s[j] = t;
                            f = cs * e[j] + sn * s[j + 1];
                            s[j + 1] = -sn * e[j] + cs * s[j + 1];
                            g = sn * e[j + 1];
                            e[j + 1] = cs * e[j + 1];
                            if (wantu && (j < m - 1)) {
                                for (int i = 0; i < m; i++) {
                                    t = cs * U[i][j] + sn * U[i][j + 1];
                                    U[i][j + 1] = -sn * U[i][j] + cs * U[i][j + 1];
                                    U[i][j] = t;
                                }
                            }
                        }
                        e[p - 2] = f;
                        iter = iter + 1;
                    }
                    break;

                    // Convergence.

                    case 4: {

                        // Make the singular values positive.

                        if (s[k] <= 0.0) {
                            s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
                            if (wantv) {
                                for (int i = 0; i <= pp; i++) {
                                    V[i][k] = -V[i][k];
                                }
                            }
                        }

                        // Order the singular values.

                        while (k < pp) {
                            if (s[k] >= s[k + 1]) {
                                break;
                            }
                            double t = s[k];
                            s[k] = s[k + 1];
                            s[k + 1] = t;
                            if (wantv && (k < n - 1)) {
                                for (int i = 0; i < n; i++) {
                                    t = V[i][k + 1];
                                    V[i][k + 1] = V[i][k];
                                    V[i][k] = t;
                                }
                            }
                            if (wantu && (k < m - 1)) {
                                for (int i = 0; i < m; i++) {
                                    t = U[i][k + 1];
                                    U[i][k + 1] = U[i][k];
                                    U[i][k] = t;
                                }
                            }
                            k++;
                        }
                        iter = 0;
                        p--;
                    }
                    break;
                }
            }
        }

        /* ------------------------
         Public Methods
           * ------------------------ */

        /**
         * Return the left singular vectors
         *
         * @return U
         */

        public Matrix getU() {
            return new Matrix(U, m, Math.min(m + 1, n));
        }

        /**
         * Return the right singular vectors
         *
         * @return V
         */

        public Matrix getV() {
            return new Matrix(V, n, n);
        }

        /**
         * Return the one-dimensional array of singular values
         *
         * @return diagonal of S.
         */

        public double[] getSingularValues() {
            return s;
        }

        /**
         * Return the diagonal matrix of singular values
         *
         * @return S
         */

        public Matrix getS() {
            Matrix X = new Matrix(n, n);
            double[][] S = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    S[i][j] = 0.0;
                }
                S[i][i] = this.s[i];
            }
            return X;
        }

        /**
         * Two norm
         *
         * @return max(S)
         */

        public double norm2() {
            return s[0];
        }

        /**
         * Two norm condition number
         *
         * @return max(S)/min(S)
         */

        public double cond() {
            return s[0] / s[Math.min(m, n) - 1];
        }

        /**
         * Effective numerical matrix rank
         *
         * @return Number of nonnegligible singular values.
         */

        public int rank() {
            double eps = Math.pow(2.0, -52.0);
            double tol = Math.max(m, n) * s[0] * eps;
            int r = 0;
            for (int i = 0; i < s.length; i++) {
                if (s[i] > tol) {
                    r++;
                }
            }
            return r;
        }
    }

    public static class StackingMatrix {

        private static final float[] cC4 = {6.614f, -7.068f, 3.100f};
        private static final float[] cC3 = {6.646f, -5.859f, 4.020f};
        private static final float[] cC2 = {7.380f, -4.830f, 3.170f};
        private static final float[] cO3 = {7.297f, -6.145f, 5.250f};

        private static final float[] nC4 = {9.384f, -2.375f, 5.910f};
        private static final float[] nC3 = {8.758f, -1.340f, 6.830f};
        private static final float[] nC2 = {8.820f, -0.077f, 5.980f};
        private static final float[] nO3 = {9.461f, -1.229f, 8.061f};

        private static final Matrix current = getMatrixFor(cC2, cC3, cC4, cO3);
        private static final Matrix next = getMatrixFor(nC2, nC3, nC4, nO3);
        private static final Matrix toNext = current.solveTranspose(next).transpose();

        public static Matrix getReferenceToResidueMatrix(float[] oC2O3, float[] oC3O3, float[] oC4O3, float[] oO3) {
            Matrix residue = getMatrixFor(oC2O3, oC3O3, oC4O3, oO3);
            return current.solveTranspose(residue).transpose();
        }

        public static Matrix getResidueToReferenceMatrix(float[] oC2O3, float[] oC3O3, float[] oC4O3, float[] oO3) {
            return getReferenceToResidueMatrix(oC2O3, oC3O3, oC4O3, oO3).inverse();
        }

        public static Matrix getToNextMatrix() {
            return toNext;
        }

        public static Matrix getToPreviousMatrix() {
            return getToNextMatrix().inverse();
        }

        public static float[] applyMatrixToPoint(Matrix M, float[] point) {
            return applyMatrixToPoint(M, point[0], point[1], point[2]);
        }

        public static float[] applyMatrixToPoint(Matrix M, float x, float y, float z) {
            float[] ret = new float[3];
            ret[0] = (float) (M.get(0, 0) * x + M.get(0, 1) * y + M.get(0, 2) * z + M.get(0, 3));
            ret[1] = (float) (M.get(1, 0) * x + M.get(1, 1) * y + M.get(1, 2) * z + M.get(1, 3));
            ret[2] = (float) (M.get(2, 0) * x + M.get(2, 1) * y + M.get(2, 2) * z + M.get(2, 3));
            return ret;
        }

        public static Matrix getMatrixFor(float[] a, float[] b, float[] c, float[] d) {
            double[][] m = {{a[0], b[0], c[0], d[0]},
                    {a[1], b[1], c[1], d[1]},
                    {a[2], b[2], c[2], d[2]},
                    {1, 1, 1, 1}};
            return new Matrix(m);
        }
    }

}
