package fr.unistra.ibmc.assemble2.event;


import java.util.List;

public interface SelectionListener {

    public void residuesSelected(List<Integer> positions);

    public void residuesUnSelected(List<Integer> positions);

    public void selectionCleared();

}
