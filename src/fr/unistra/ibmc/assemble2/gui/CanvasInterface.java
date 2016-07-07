package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.model.BaseBaseInteraction;
import fr.unistra.ibmc.assemble2.model.Button;
import fr.unistra.ibmc.assemble2.model.Helix;
import fr.unistra.ibmc.assemble2.model.Residue;

public interface CanvasInterface {

    void addButton(Button button);

    void repaint();

    void clearSelection();

    void setSelectedHelix(Helix h);

    void select(Residue residue);

    Helix getSelectedHelix();

    void selectBaseBaseInteraction(BaseBaseInteraction bbi);

    Mediator getMediator();
}
