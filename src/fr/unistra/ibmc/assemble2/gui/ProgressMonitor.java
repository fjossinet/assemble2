package fr.unistra.ibmc.assemble2.gui;

public interface ProgressMonitor {

    public void printMessage(String message);

    public void printException(Exception e);

}
