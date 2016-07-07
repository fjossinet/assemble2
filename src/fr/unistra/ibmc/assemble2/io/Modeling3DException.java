package fr.unistra.ibmc.assemble2.io;


public class Modeling3DException extends Exception{
    public static final int MISSING_ATOMS_IN_MOTIFS = 0;

    private int status;

    public Modeling3DException(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
